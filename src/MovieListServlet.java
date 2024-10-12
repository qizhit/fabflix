import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movie_list")
public class MovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
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
            Statement statement = conn.createStatement();

//            String query = "SELECT * from stars";
            String query = "SELECT m.id, m.title, m.year, m.director, r.rating,\n" +
                    "    (SELECT GROUP_CONCAT(g.name ORDER BY g.name SEPARATOR ', ')\n" +
                    "     FROM genres_in_movies gm\n" +
                    "     JOIN genres g ON gm.genreId = g.id\n" +
                    "     WHERE gm.movieId = m.id\n" +
                    "     LIMIT 3) AS genres,\n" +
                    "    (SELECT GROUP_CONCAT(star_name SEPARATOR ', ')\n" +
                    "     FROM (\n" +
                    "         SELECT s.name AS star_name\n" +
                    "         FROM stars_in_movies sm\n" +
                    "         JOIN stars s ON sm.starId = s.id\n" +
                    "         WHERE sm.movieId = m.id\n" +
                    "         ORDER BY s.name\n" +
                    "         LIMIT 3\n" +
                    "     ) AS limited_stars) AS stars\n" +
                    "FROM movies m\n" +
                    "JOIN ratings r ON m.id = r.movieId\n" +
                    "ORDER BY r.rating DESC\n" +
                    "LIMIT 20;";
            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {

                String movieId = rs.getString("id");
                String title = rs.getString("title");
                String year = rs.getString("year");
                String director = rs.getString("director");
                String rating = rs.getString("rating");
                String genres = rs.getString("genres"); // First 3 genres
                String stars = rs.getString("stars");   // First 3 stars

                // Create a JsonObject for each movie
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("title", title);
                jsonObject.addProperty("year", year);
                jsonObject.addProperty("director", director);
                jsonObject.addProperty("rating", rating);
                jsonObject.addProperty("genres", genres);
                jsonObject.addProperty("stars", stars);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
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

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
