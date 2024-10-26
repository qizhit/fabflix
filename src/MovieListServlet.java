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
import java.sql.SQLException;
import java.sql.Statement;


// Declaring a WebServlet called MovieListServlet, which maps to url "/api/movie_list"
@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movie_list")
public class MovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
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
        PrintWriter out = response.getWriter();

        // Default values for pagination and sorting
        int page = 1;
        int pageSize = 10;
        String sortOption = "title";
        String sortOrder = "ASC";

        // Retrieve parameters
        try{
            page = Integer.parseInt(request.getParameter("page"));
            pageSize = Integer.parseInt(request.getParameter("size"));
            sortOption = request.getParameter("sortOption") != null ? request.getParameter("sortOption") : "title";
            sortOrder = "desc".equalsIgnoreCase(request.getParameter("sortOrder")) ? "DESC" : "ASC";
        } catch (Exception ignored) {}

        // Ensure pageSize is within acceptable limits
        pageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (page - 1) * pageSize;

//        String query = "SELECT m.id, m.title, m.year, m.director, r.rating,\n" +
//                "(SELECT GROUP_CONCAT(g.name ORDER BY g.name SEPARATOR ', ')\n" +
//                "FROM genres_in_movies gm\n" +
//                "JOIN genres g ON gm.genreId = g.id\n" +
//                "WHERE gm.movieId = m.id\n" +
//                "LIMIT 3) AS genres,\n" +
//                "(SELECT GROUP_CONCAT(limited_stars.name ORDER BY limited_stars.name SEPARATOR ', ')\n" +
//                "FROM (\n" +
//                "SELECT s.name FROM stars_in_movies sm\n" +
//                "JOIN stars s ON sm.starId = s.id\n" +
//                "WHERE sm.movieId = m.id\n" +
//                "ORDER BY s.name\n" +
//                "LIMIT 3\n" +
//                ") AS limited_stars) AS stars,\n" +
//                "(SELECT GROUP_CONCAT(limited_ids.id ORDER BY limited_ids.name SEPARATOR ', ')\n" +
//                "FROM (\n" +
//                "SELECT s.id, s.name FROM stars_in_movies sm\n" +
//                "JOIN stars s ON sm.starId = s.id\n" +
//                "WHERE sm.movieId = m.id\n" +
//                "ORDER BY s.name\n" +
//                "LIMIT 3) AS limited_ids) AS star_ids\n" +
//                "FROM movies m\n" +
//                "JOIN ratings r ON m.id = r.movieId\n" +
//                "ORDER BY r.rating DESC\n" +
//                "LIMIT 20;";


        String movieQuery = "SELECT M.id, M.title, M.year, M.director, R.rating " +
                "FROM movies M JOIN ratings R ON M.id = R.movieId " +
                "ORDER BY " + sortOption + " " + sortOrder + ", rating DESC " +
                "LIMIT " + pageSize + " OFFSET " + offset;
        //String movieQuery = "SELECT M.id, M.title, M.year, M.director, R.rating FROM movies M JOIN ratings R ON M.id = R.movieId LIMIT 10";

        try (Connection conn = dataSource.getConnection()){
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(movieQuery);
            JsonArray jsonArray = new JsonArray();

            // Process each movie
            while ( rs.next()) {
                String movieId =  rs.getString("id");
                JsonObject movieJson = new JsonObject();
                movieJson.addProperty("movie_id", movieId);
                movieJson.addProperty("title",  rs.getString("title"));
                movieJson.addProperty("year",  rs.getString("year"));
                movieJson.addProperty("director",  rs.getString("director"));
                movieJson.addProperty("rating",  rs.getString("rating"));

                // Fetch genres for each movie
                JsonArray genreArray = new JsonArray();
                String genreQuery = "SELECT G.id, G.name FROM genres_in_movies GIM, genres G " +
                        "WHERE GIM.genreId = G.id AND GIM.movieId = '" + movieId + "' " +
                        "ORDER BY G.name ASC LIMIT 3";

                Statement genreStmt = conn.createStatement();
                ResultSet genreRs = genreStmt.executeQuery(genreQuery);
                while (genreRs.next()) {
                    JsonObject genreJson = new JsonObject();
                    genreJson.addProperty("genre_id", genreRs.getString("id"));
                    genreJson.addProperty("genre_name", genreRs.getString("name"));
                    genreArray.add(genreJson);
                }

                movieJson.add("genres", genreArray);

            // Fetch stars for each movie
                JsonArray starArray = new JsonArray();
                String starQuery = "SELECT S.id, S.name FROM stars S, stars_in_movies SIM " +
                        "WHERE S.id = SIM.starId AND SIM.movieId = '" + movieId + "' " +
                        "ORDER BY (SELECT COUNT(*) FROM stars_in_movies WHERE starId = S.id) DESC, S.name ASC LIMIT 3";
                Statement starStmt = conn.createStatement();
                ResultSet starRs = starStmt.executeQuery(starQuery);
                while (starRs.next()) {
                    JsonObject starJson = new JsonObject();
                    starJson.addProperty("star_id", starRs.getString("id"));
                    starJson.addProperty("star_name", starRs.getString("name"));
                    starArray.add(starJson);
                }

                movieJson.add("stars", starArray);

                jsonArray.add(movieJson);
            }
//            // Iterate through each row of rs
//            while (rs.next()) {
//
//                String movieId = rs.getString("id");
//                String title = rs.getString("title");
//                String year = rs.getString("year");
//                String director = rs.getString("director");
//                String rating = rs.getString("rating");
//                String genres = rs.getString("genres"); // First 3 genres
//                String stars = rs.getString("stars");   // First 3 stars
//                String star_ids = rs.getString("star_ids"); // First 3 stars' id
//
//                // Create a JsonObject for each movie
//                JsonObject jsonObject = new JsonObject();
//                jsonObject.addProperty("movie_id", movieId);
//                jsonObject.addProperty("title", title);
//                jsonObject.addProperty("year", year);
//                jsonObject.addProperty("director", director);
//                jsonObject.addProperty("rating", rating);
//                jsonObject.addProperty("genres", genres);
//                jsonObject.addProperty("stars", stars);
//                jsonObject.addProperty("star_ids", star_ids);
//
//                jsonArray.add(jsonObject);
//            }
//            rs.close();
            //statement.close();

            // Log to localhost log
            //request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
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
