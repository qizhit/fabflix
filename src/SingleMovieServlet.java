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
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "SELECT m.id, m.title, m.year, m.director, r.rating, m.price, " +
                    "GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', ') AS genres, " +
                    "GROUP_CONCAT(DISTINCT s.name ORDER BY movie_count DESC, s.name SEPARATOR ', ') AS stars, " +
                    "GROUP_CONCAT(DISTINCT s.id ORDER BY movie_count DESC, s.name SEPARATOR ', ') AS star_ids " +
                    "FROM movies AS m " +
                    "LEFT JOIN ratings AS r ON m.id = r.movieId " +
                    "LEFT JOIN genres_in_movies AS gim ON m.id = gim.movieId " +
                    "LEFT JOIN genres AS g ON gim.genreId = g.id " +
                    "LEFT JOIN stars_in_movies AS sim ON m.id = sim.movieId " +
                    "LEFT JOIN stars AS s ON sim.starId = s.id " +
                    "LEFT JOIN (SELECT starId, COUNT(*) AS movie_count FROM stars_in_movies GROUP BY starId) AS star_counts ON s.id = star_counts.starId " +
                    "WHERE m.id = ? " +
                    "GROUP BY m.id;";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String movieGenres = rs.getString("genres");
                String movieStars = rs.getString("stars");
                String movieRating = rs.getString("rating");
                String movieStarIds = rs.getString("star_ids");
                String moviePrice = rs.getString("price");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("title", movieTitle);
                jsonObject.addProperty("year", movieYear);
                jsonObject.addProperty("director", movieDirector);
                jsonObject.addProperty("genres", movieGenres);
                jsonObject.addProperty("stars", movieStars);
                jsonObject.addProperty("rating", movieRating);
                jsonObject.addProperty("star_ids", movieStarIds);
                jsonObject.addProperty("price", moviePrice);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
