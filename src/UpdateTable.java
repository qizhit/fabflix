import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class UpdateTable{
    private static List<MovieDetail> moviesArray = new ArrayList<>(); // store list of MovieDetail, prepare for insert
    private static int new_movies;
    private static int new_stars;
    private static int new_genres;

    private static int currentMaxMovieIdNumericPart = -1; // The numeric part of the current maximum MovieID
    private static final String MovieID_PREFIX = "tt";
    private static int currentMaxStarIdNumericPart = -1; // The numeric part of the current maximum StarID
    private static final String StarID_PREFIX = "nm";
    private static HashMap<String, HashSet<String>> insertedStarsInMovies = new HashMap<>();
    private static HashMap<String, HashSet<Integer>> insertedGenresInMovies = new HashMap<>();
    private static DataSource readDataSource;
    private static DataSource writeDataSource;

    public static void main(String[] args) throws Exception {
        try {
            InitialContext context = new InitialContext();
            readDataSource = (DataSource) context.lookup("java:comp/env/jdbc/readconnect");
            writeDataSource = (DataSource) context.lookup("java:comp/env/jdbc/writeconnect");
        } catch (NamingException e) {
            e.printStackTrace();
        }

        try (Connection dbConnection = readDataSource.getConnection()) {
            // Call the add_star method to parse and insert stars
            System.out.println("Database connection established.");

            //Call add_star
            add_star(dbConnection);

            // Call add_movie
            get_movie();
            add_movie(dbConnection);

            // Close the database connection
            dbConnection.close();
            System.out.println("Database connection closed.");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Insert New Movies: " + new_movies);
        System.out.println("Insert New Stars: " + new_stars);
        System.out.println("Insert New Genres: " + new_genres);

    }

    private static void initializeMaxId(Connection dbConnection) throws SQLException {
        if (currentMaxMovieIdNumericPart == -1) {
            String query = "SELECT COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) " +
                    "AS last_numeric_part FROM movies WHERE id LIKE 'tt%'";
            try (PreparedStatement statement = dbConnection.prepareStatement(query);
                 ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    currentMaxMovieIdNumericPart = rs.getInt("last_numeric_part");
                }
            }
        }
        if (currentMaxStarIdNumericPart == -1) {
            String query = "SELECT COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) AS last_numeric_part FROM stars";
            try (PreparedStatement statement = dbConnection.prepareStatement(query);
                 ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    currentMaxStarIdNumericPart = rs.getInt("last_numeric_part");
                }
            }
        }
    }


    // Helper method to generate a new unique star ID
    private static String generateNewStarId(Connection dbConnection) throws SQLException {
        currentMaxStarIdNumericPart++; // Increasing value part
        return StarID_PREFIX + String.format("%07d", currentMaxStarIdNumericPart);
    }

    private static String generateNewMovieId(Connection dbConnection) throws SQLException {
        currentMaxMovieIdNumericPart++; // Increasing value part
        return MovieID_PREFIX + String.format("%07d", currentMaxMovieIdNumericPart);
    }


    private static void add_star(Connection dbConnection) throws SQLException {
        // Retrieve existing stars from the database to avoid duplicates
        PreparedStatement statement = dbConnection.prepareStatement("SELECT name, birthYear FROM stars");
        HashMap<String, Integer> existingStars = new HashMap<>();
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            int birthYear = resultSet.getInt("birthYear");
            String name = resultSet.getString("name");

            if (resultSet.wasNull()) {
                existingStars.put(name, null);
            } else {
                existingStars.put(name, birthYear);
            }
        }
        statement.close();

        // Parse the XML and get the list of new stars
        dbConnection.setAutoCommit(false);
        StarsSAXParser starsParser = new StarsSAXParser(existingStars);  // pass existingStars in parse
        starsParser.parseDocument("parse/actors63.xml");  // Adjust the path as needed
        List<String[]> parsedStars = starsParser.getStarsList();  // get unique stars
        starsParser.writeAndDisplayResult(); //write the inconsistent
        new_stars += parsedStars.size();

        // Prepare the SQL statement for inserting new stars
        String insertStarSQL = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
        PreparedStatement insertStatement = dbConnection.prepareStatement(insertStarSQL);
        initializeMaxId(dbConnection);
        // Insert each new star
        int batchSize = 1000;  // Set batch size limit, e.g., 1000 records per batch
        int count = 0;

        for (String[] star : parsedStars) {
            String starName = star[0];
            String birthYearStr = star[1];

            // Generate a new unique ID for the star
            String newStarId = generateNewStarId(dbConnection);

            // Set parameters for insertion
            insertStatement.setString(1, newStarId);
            insertStatement.setString(2, starName);
            if (birthYearStr != null && !birthYearStr.isEmpty()) {
                insertStatement.setInt(3, Integer.parseInt(birthYearStr));
            } else {
                insertStatement.setNull(3, java.sql.Types.INTEGER);
            }

            insertStatement.addBatch();
            count++;

            if (count % batchSize == 0) {
                insertStatement.executeBatch();
                System.out.println("Executed batch of size: " + batchSize);
            }
        }

        dbConnection.commit();
        insertStatement.executeBatch();
        System.out.println("Executed final batch with remaining records.");
        dbConnection.setAutoCommit(true);
        insertStatement.close();

    }

    private static void get_movie (){
        // Create an instance of MainsSAXParser and parse the XML to populate myMovies
        MainsSAXParser mainsParser = new MainsSAXParser();
        mainsParser.run();
        List<MainsItem> mainsItems = mainsParser.getMyMovies();  // Get parsed movies list

        // Similarly, create an instance of CastSAXParser to get the casts data
        CastsSAXParser castParser = new CastsSAXParser();
        castParser.run();
        List<CastsItem> castsItems = castParser.getMyCasts();

        // Create a map to associate filmId with CastsItem to quickly find stars by filmId
        HashMap<String, List<String>> filmStarsMap = new HashMap<>();
        for (CastsItem castsItem : castsItems) {
            filmStarsMap.put(castsItem.getFilmId(), castsItem.getStarStageNames());
        }

        // Iterate through MainsItems, match with CastsItems, and create MovieDetail objects
        for (MainsItem mainsItem : mainsItems) {
            String filmId = mainsItem.getFilmId();
            List<String> stars = filmStarsMap.getOrDefault(filmId, new ArrayList<>());

            // Only add if stars, director, title, and year are all valid
            if (mainsItem.getFilmTitle() != null && !stars.isEmpty() && mainsItem.getDirector() != null) {
                MovieDetail movieDetail = new MovieDetail(
                        mainsItem.getFilmTitle(),
                        Integer.parseInt(mainsItem.getYear()),
                        mainsItem.getDirector(),
                        mainsItem.getGenres(),
                        stars
                );
                moviesArray.add(movieDetail);
            }
        }
    }

    private static void add_movie(Connection dbConnection) throws SQLException {
        dbConnection.setAutoCommit(false);  // Enable transaction for batch processing

        // Prepare batch inserts
        try (PreparedStatement movieInsertStatement = dbConnection.prepareStatement(
                "INSERT INTO movies (id, title, year, director, price) VALUES (?, ?, ?, ?, ?)");
             PreparedStatement genreInsertStatement = dbConnection.prepareStatement(
                     "INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?)");
             PreparedStatement starInsertStatement = dbConnection.prepareStatement(
                     "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?)")) {

            initializeMaxId(dbConnection);

            for (MovieDetail movie : moviesArray) {
                String movieId = findMovieId(dbConnection, movie);

                if (movieId == null) {  // Movie does not exist, so insert everything
                    new_movies++;
                    movieId = generateNewMovieId(dbConnection);
                    // generate a random price for movies table
                    Random random = new Random();
                    double price = 5 + (random.nextDouble() * 45);
                    BigDecimal roundedPrice = new BigDecimal(price).setScale(2, RoundingMode.HALF_UP);
                    // Add movie to batch
                    movieInsertStatement.setString(1, movieId);
                    movieInsertStatement.setString(2, movie.getFilmTitle());
                    movieInsertStatement.setInt(3, movie.getYear());
                    movieInsertStatement.setString(4, movie.getDirector());
                    movieInsertStatement.setBigDecimal(5, roundedPrice);  // Set default price
                    movieInsertStatement.addBatch();

                    // Insert genres_in_movies and possibly new genres
                    for (String genre : movie.getGenres()) {
                        int genreId = getOrInsertGenreId(dbConnection, genre);

                        if (!insertedGenresInMovies.containsKey(movieId)) {
                            insertedGenresInMovies.put(movieId, new HashSet<>());
                        }
                        if (!insertedGenresInMovies.get(movieId).contains(genreId)) {
                            genreInsertStatement.setInt(1, genreId);
                            genreInsertStatement.setString(2, movieId);
                            genreInsertStatement.addBatch();
                            insertedGenresInMovies.get(movieId).add(genreId);
                        }
                    }

                    // Insert stars_in_movies and possibly new stars
                    for (String starName : movie.getStars()) {
                        String starId = findOrInsertStar(dbConnection, starName);

                        if (!insertedStarsInMovies.containsKey(movieId)) {
                            insertedStarsInMovies.put(movieId, new HashSet<>());
                        }
                        if (!insertedStarsInMovies.get(movieId).contains(starId)) {
                            starInsertStatement.setString(1, starId);
                            starInsertStatement.setString(2, movieId);
                            starInsertStatement.addBatch();
                            insertedStarsInMovies.get(movieId).add(starId);
                        }
                    }
                }
            }

            // Execute batch inserts
            movieInsertStatement.executeBatch();
            genreInsertStatement.executeBatch();
            starInsertStatement.executeBatch();

            dbConnection.commit();  // Commit transaction
            dbConnection.setAutoCommit(true);
        } catch (SQLException e) {
            dbConnection.rollback();  // Rollback transaction if error occurs
            e.printStackTrace();
        }
    }

    private static String findMovieId(Connection dbConnection, MovieDetail movie) throws SQLException {
        String query = "SELECT id FROM movies WHERE title = ? AND year = ? AND director = ?";
        try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
            statement.setString(1, movie.getFilmTitle());
            statement.setInt(2, movie.getYear());
            statement.setString(3, movie.getDirector());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("id");  // Movie exists, return id
                }
            }
        }
        return null;  // Movie does not exist
    }

    private static int getOrInsertGenreId(Connection dbConnection, String genre) throws SQLException {
        String selectQuery = "SELECT id FROM genres WHERE name = ?";
        try (PreparedStatement selectStmt = dbConnection.prepareStatement(selectQuery)) {
            selectStmt.setString(1, genre);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");  // Genre exists, return id
                }
            }
        }
        // Genre doesn't exist, insert new genre
        new_genres++;
        String insertQuery = "INSERT INTO genres (name) VALUES (?)";
        try (PreparedStatement insertStmt = dbConnection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, genre);
            insertStmt.executeUpdate();
            try (ResultSet rs = insertStmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);  // Return new genre id
                }
            }
        }
        throw new SQLException("Failed to insert new genre: " + genre);
    }

    private static String findOrInsertStar(Connection dbConnection, String starName) throws SQLException {
        String selectQuery = "SELECT id FROM stars WHERE name = ?";
        try (PreparedStatement selectStmt = dbConnection.prepareStatement(selectQuery)) {
            selectStmt.setString(1, starName);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("id");  // Star exists, return id
                }
            }
        }
        // Star doesn't exist, insert new star
        new_stars++;
        String starId = generateNewStarId(dbConnection);
        String insertQuery = "INSERT INTO stars (id, name) VALUES (?, ?)";
        try (PreparedStatement insertStmt = dbConnection.prepareStatement(insertQuery)) {
            insertStmt.setString(1, starId);
            insertStmt.setString(2, starName);
            insertStmt.executeUpdate();
        }
        return starId;
    }

}
