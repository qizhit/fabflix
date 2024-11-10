import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class UpdateTable{

    public static void main(String[] args) throws Exception {
        // Establish database connection
        Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        Connection dbConnection = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&useSSL=false",
                "mytestuser", "My6$Password");

        // Call the add_star method to parse and insert stars
        System.out.println("Database connection established.");
        add_star(dbConnection);

        // Close the database connection
        dbConnection.close();
        System.out.println("Database connection closed.");
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
        starsParser.parseDocument("/Users/x/Desktop/122B/2024-fall-cs-122b-cs122b-project1-ys/parse/actors63.xml");  // Adjust the path as needed
        List<String[]> parsedStars = starsParser.getStarsList();

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
}
