import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import jakarta.servlet.ServletConfig;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "MainServlet", urlPatterns = "/api/genres-titles")
public class MainServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Data source object
    private DataSource dataSource;

    // Initialize the data source and connect to the database connection pool
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/ReadOnly");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            //change this line to prepareStatement
            //Statement statement = conn.createStatement();
            String query = "SELECT name FROM genres ORDER BY name;";
            PreparedStatement statement = conn.prepareStatement(query);
            // Perform the query
            ResultSet genreRs = statement.executeQuery(query);

            // Add Genres' name
            JsonArray genreArray = new JsonArray();
            while (genreRs.next()) {
                genreArray.add(genreRs.getString("name"));
            }

            // Alphanumeric titles (0-9, A-Z, *)
            JsonArray titleArray = new JsonArray();
            for (char c = '0'; c <= '9'; c++) {
                titleArray.add(String.valueOf(c));  // Add numbers 0-9
            }
            for (char c = 'A'; c <= 'Z'; c++) {
                titleArray.add(String.valueOf(c));  // Add letters A-Z
            }
            titleArray.add("*");  // Add special symbol * (Non-alphanumerical)

            // Build the final JSON response
            JsonObject responseJson = new JsonObject();
            responseJson.add("genres", genreArray);
            responseJson.add("titles", titleArray);

            genreRs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("Genres retrieved: " + genreArray.size());
            request.getServletContext().log("Titles retrieved: " + titleArray.size());

            // Write the JSON object as the response
            out.write(responseJson.toString());
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
