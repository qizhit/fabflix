import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class UpdateTable{
    private static List<MovieDetail> moviesArray = new ArrayList<>(); // store list of MovieDetail, prepare for insert

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
        get_movie();
    }

    private static void add_star(Connection dbConnection) throws Exception {
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

        // Output the result
        System.out.println("Movies Array: ");
        for (MovieDetail movie : moviesArray) {
            System.out.println(movie);
        }
    }
}
