import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.sql.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MovieSAXParser extends DefaultHandler {

    private Connection connection;
    private CallableStatement callableStmt;

    private String tempVal;       // Stores temporary text values
    private String movieTitle;
    private int movieYear;
    private String movieDirector;
    private String starName;
    private String genreName;

    public MovieSAXParser(Connection connection) {
        this.connection = connection;
        prepareProcedureCall();
    }

    public void runExample() {
        parseDocument();
        //printData();
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            System.out.println("Starting parsing main xml");
            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("mains243.xml", this);
            System.out.println("Finishing parsing main xml");

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Iterate through the list and print
     * the contents
     */
    private void prepareProcedureCall() {
        try {
            String callSQL = "{CALL add_movie(?, ?, ?, ?, ?)}";
            callableStmt = connection.prepareCall(callSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Event handler for start of an element
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            // Reset movie details at the start of each <film> element
            movieTitle = null;
            movieYear = 0;
            movieDirector = null;
            starName = null;
            genreName = null;
        }
    }


    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length).trim();
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            if (qName.equalsIgnoreCase("t")) {
                movieTitle = tempVal;
                System.out.println("Parsed movie title: " + movieTitle);
            } else if (qName.equalsIgnoreCase("year")) {
                movieYear = Integer.parseInt(tempVal);
                System.out.println("Parsed movie year: " + movieYear);
            } else if (qName.equalsIgnoreCase("director")) {
                movieDirector = tempVal;
                System.out.println("Parsed movie director: " + movieDirector);
            } else if (qName.equalsIgnoreCase("star")) {
                starName = tempVal;
                System.out.println("Parsed star name: " + starName);
            } else if (qName.equalsIgnoreCase("genre")) {
                genreName = tempVal;
                System.out.println("Parsed genre name: " + genreName);
            } else if (qName.equalsIgnoreCase("film")) {
                // Call stored procedure after finishing each <film> element
                if (movieTitle != null && movieDirector != null && movieYear != 0) {
                    addMovieToDatabase();
                } else {
                    System.out.println("Skipping insertion: Missing required data for movie.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addMovieToDatabase() {
        try {
            // Set parameters for the stored procedure
            callableStmt.setString(1, movieTitle);
            callableStmt.setInt(2, movieYear);
            callableStmt.setString(3, movieDirector);
            callableStmt.setString(4, starName);
            callableStmt.setString(5, genreName);

            // Execute stored procedure
            callableStmt.execute();
            System.out.println("Movie added: " + movieTitle + " (" + movieYear + "), Director: " + movieDirector);

            String query = "SELECT * FROM movies WHERE title = ? AND year = ? AND director = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, movieTitle);
                stmt.setInt(2, movieYear);
                stmt.setString(3, movieDirector);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    System.out.println("Verification: Movie exists in the database - " +
                            "ID: " + rs.getString("id") +
                            ", Title: " + rs.getString("title") +
                            ", Year: " + rs.getInt("year") +
                            ", Director: " + rs.getString("director") +
                            ", Price: " + rs.getBigDecimal("price"));
                } else {
                    System.out.println("Verification failed: Movie not found in the database.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        String loginUser = "mytestuser";         // Your MySQL username
        String loginPasswd = "My6$Password";      // Your MySQL password
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";  // Your database URL

        try {
            // Set up the database connection using login credentials
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

            // Initialize the parser with the database connection
            MovieSAXParser parser = new MovieSAXParser(connection);
            parser.runExample();

            // Clean up resources
            parser.callableStmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}