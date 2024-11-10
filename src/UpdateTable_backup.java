import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class UpdateTable_backup{

    public static void main(String[] args) throws Exception {
//        // Establish database connection
//        Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
//        Connection dbConnection = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&useSSL=false",
//                "mytestuser", "My6$Password");
//
//        // Call the add_star method to parse and insert stars
//        System.out.println("Database connection established.");
//        add_star(dbConnection);
//
//        // Close the database connection
//        dbConnection.close();
//        System.out.println("Database connection closed.");
    }

    public static void add_star(Connection dbConnection) throws Exception {
        // Retrieve existing stars from the database to avoid duplicates
        PreparedStatement statement = dbConnection.prepareStatement("SELECT name, birthYear FROM stars;");
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
        StarsSAXParser starsParser = new StarsSAXParser(existingStars);
        starsParser.parseDocument("parse/actors63.xml");  // Adjust the path as needed
        List<String[]> parsedStars = starsParser.getStarsList();
        starsParser.printInconsistentEntries(); //write the inconsistent
        System.out.println("Running time" + starsParser.exe_time);

        // Prepare the SQL statement for inserting new stars
        String insertStarSQL = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
        PreparedStatement insertStatement = dbConnection.prepareStatement(insertStarSQL);

        // Insert each new star
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

            // Execute the insertion
            insertStatement.executeUpdate();
            System.out.println("Inserted star: " + starName + " with ID: " + newStarId);
        }

        // Close the prepared statement

        insertStatement.close();
    }

    // Helper method to generate a new unique star ID
    private static String generateNewStarId(Connection dbConnection) throws Exception {
        String newId = null;
        String query = "SELECT COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) AS last_numeric_part FROM stars";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int lastNumericPart = rs.getInt("last_numeric_part");
                newId = String.format("nm%07d", lastNumericPart + 1);
            }
        }
        return newId;
    }


//    public static void add_movie(Connection dbConnection) throws Exception {
//        //existing movie
//        PreparedStatement movieStatement = dbConnection.prepareStatement("SELECT title, year, director FROM movies;");
//        HashSet<String> existingMovies = new HashSet<>();
//        ResultSet movieResultSet = movieStatement.executeQuery();
//        while (movieResultSet.next()) {
//            String title = movieResultSet.getString("title");
//            int year = movieResultSet.getInt("year");
//            String director = movieResultSet.getString("director");
//            existingMovies.add(title + year + director);
//        }
//        movieStatement.close();
//
//        //prevent duplication of genre
//        PreparedStatement genreStatement = dbConnection.prepareStatement("SELECT id, name FROM genres;");
//        HashMap<String, Integer> existingGenres = new HashMap<>();
//        ResultSet genreResultSet = genreStatement.executeQuery();
//        while (genreResultSet.next()) {
//            String name = genreResultSet.getString("name");
//            int id = genreResultSet.getInt("id");
//            existingGenres.put(name.toLowerCase(), id); // Store lowercase for case-insensitive match
//        }
//        genreStatement.close();
//
//        // Load existing star-movie relationships into a HashSet to avoid duplicates
//        PreparedStatement starInMovieStatement = dbConnection.prepareStatement("SELECT starId, movieId FROM stars_in_movies;");
//        HashSet<String> existingStarInMovies = new HashSet<>();
//        try (ResultSet starInMovieResultSet = starInMovieStatement.executeQuery()) {
//            while (starInMovieResultSet.next()) {
//                String starId = starInMovieResultSet.getString("starId");
//                String movieId = starInMovieResultSet.getString("movieId");
//                existingStarInMovies.add(starId + "-" + movieId); // Concatenate starId and movieId as a unique key
//            }
//        }
//        starInMovieStatement.close();
//
//
//        MainsSAXParser mainsParser = new MainsSAXParser();
//        mainsParser.parseDocument();
//        List<MovieItem> parsedMovies = mainsParser.getMoviesList(); // Assume MovieItem includes genres list
//        mainsParser.printInconsistentEntries(); // Log inconsistent entries
//
//        System.out.println("Running time: ");
//
//
//        //Assuming all correct
//        String insertMovieSQL = "INSERT INTO movies (id, title, year, director, price) VALUES (?, ?, ?, ?, ?)";
//        String insertGenreSQL = "INSERT INTO genres (name) VALUES (?)";
//        String insertGenreInMovieSQL = "INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?)";
//
//        PreparedStatement insertMovieStatement = dbConnection.prepareStatement(insertMovieSQL);
//        PreparedStatement insertGenreStatement = dbConnection.prepareStatement(insertGenreSQL, Statement.RETURN_GENERATED_KEYS);
//        PreparedStatement insertGenreInMovieStatement = dbConnection.prepareStatement(insertGenreInMovieSQL);
//        double randomPrice = Math.round((5.00 + (Math.random() * (30.00 - 5.00))) * 100.0) / 100.0;
//
//        insertMovieStatement.setString(1, movieId);
//        insertMovieStatement.setString(2, title);
//        insertMovieStatement.setInt(3, year);
//        insertMovieStatement.setString(4, director);
//        insertMovieStatement.setDouble(5, randomPrice);
//        insertMovieStatement.executeUpdate();
//
//    }

    // Utility method to generate a new unique movie ID
    private static String generateNewMovieId(Connection dbConnection) throws SQLException, SQLException {
        String newId = "tt0000001"; // Default ID if there are no movies yet

        // SQL query to get the maximum numeric part of the movie IDs
        String getMaxNumericPartSQL =
                "SELECT COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) AS last_numeric_part " +
                        "FROM movies " +
                        "WHERE id LIKE 'tt%';";

        try (PreparedStatement maxIdStatement = dbConnection.prepareStatement(getMaxNumericPartSQL);
             ResultSet resultSet = maxIdStatement.executeQuery()) {

            if (resultSet.next()) {
                int lastNumericPart = resultSet.getInt("last_numeric_part");
                int nextIdNum = lastNumericPart + 1;
                newId = "tt" + String.format("%07d", nextIdNum);
            }
        }

        return newId;
    }



}
