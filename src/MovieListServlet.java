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
import java.util.HashMap;
import java.util.Map;


// Declaring a WebServlet called MovieListServlet, which maps to url "/api/movie_list"
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

        // Gets the genre or title parameters from the request
        String genre = request.getParameter("genre");
        String browseTitle = request.getParameter("browse_title");

        // Gets the sorting parameters from the request
        String sortBy = request.getParameter("sortBy");
        String sortOrder1 = request.getParameter("sortOrder1");
        String sortOrder2 = request.getParameter("sortOrder2");

        // Address Pagination
        int page = Integer.parseInt(request.getParameter("page"));  // currentPage
        int pageSize = Integer.parseInt(request.getParameter("pageSize"));  // Number of movies per page

        // Construct sort statement
        String orderByClause = "";
        if ("title".equals(sortBy)) {
            orderByClause = "ORDER BY m.title " + sortOrder1 + ", r.rating " + sortOrder2;  // ORDER BY m.title ase, r.rating ase
        } else if ("rating".equals(sortBy)) {
            orderByClause = "ORDER BY r.rating " + sortOrder1 + ", m.title " + sortOrder2;
        }

        // Calculated offset
        int offset = (page - 1) * pageSize;

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Construct the query for selecting movies
            StringBuilder queryBuilder = new StringBuilder(
                    "SELECT m.id, m.title, m.year, m.director, COALESCE(r.rating, 'N/A') AS rating " +
                            "FROM movies m LEFT JOIN ratings r ON m.id = r.movieId WHERE 1=1 ");

            if (genre != null && !genre.isEmpty()) {
                queryBuilder.append("AND m.id IN (SELECT gm.movieId FROM genres_in_movies gm " +
                        "JOIN genres g ON gm.genreId = g.id WHERE g.name = ?) ");
            }
            if (browseTitle != null && !browseTitle.isEmpty()) {
                if (browseTitle.equals("*")) {
                    queryBuilder.append("AND m.title REGEXP '^[^a-zA-Z0-9]' ");  // title = *, match non-alphanumerical characters.
                } else {
                    queryBuilder.append("AND LOWER(m.title) LIKE ? ");  // title = 1-9A-Z
                }
            }

            queryBuilder.append(orderByClause);  // order by title/rating
            queryBuilder.append(" LIMIT ? OFFSET ?;");  // pagination

            // prepare the query statement and pass parameters
            PreparedStatement movieStatement = conn.prepareStatement(queryBuilder.toString());
            int paramIndex = 1;
            if (genre != null && !genre.isEmpty()) {
                movieStatement.setString(paramIndex++, genre);
            }
            if (browseTitle != null && !browseTitle.isEmpty() && !browseTitle.equals("*")) {
                movieStatement.setString(paramIndex++, browseTitle.toLowerCase() + "%");
            }
            movieStatement.setInt(paramIndex++, pageSize);
            movieStatement.setInt(paramIndex, offset);
            // execute
            ResultSet movieRs = movieStatement.executeQuery();

            // key: movieId, value: jsonObject with properties
            Map<String, JsonObject> movieMap = new HashMap<>();
            JsonArray jsonArray = new JsonArray();  // used to store all movie jsonObject

            while (movieRs.next()) {
                String movieId = movieRs.getString("id");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("title", movieRs.getString("title"));
                jsonObject.addProperty("year", movieRs.getString("year"));
                jsonObject.addProperty("director", movieRs.getString("year"));
                jsonObject.addProperty("rating", movieRs.getString("rating"));

                movieMap.put(movieId, jsonObject);  // use to correspond to first-3-genres and first-3-stars
                jsonArray.add(jsonObject);
            }
            movieRs.close();
            movieStatement.close();

            // Format the list of movie ids into a string format in SQL, prepare for genres and stars query
            String movieIdList = movieMap.keySet().stream()
                    .map(id -> "'" + id + "'")
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");

            // first three genres: sort by alphabetical order
            // Batch query "genres"
            String genreQuery = "WITH RankedGenres AS (\n" +
                    "    SELECT gm.movieId, g.name, \n" +
                    "    ROW_NUMBER() OVER (PARTITION BY gm.movieId ORDER BY g.name ASC) AS genre_rank \n" +
                    "    FROM genres_in_movies gm\n" +
                    "    JOIN genres g ON gm.genreId = g.id\n" +
                    "    WHERE gm.movieId IN (" + movieIdList + ")\n" +
                    "    ORDER BY g.name)\n" +
                    "SELECT movieId, GROUP_CONCAT(name ORDER BY name ASC SEPARATOR ', ') AS genres\n" +
                    "    FROM RankedGenres WHERE genre_rank <= 3\n" +
                    "    GROUP BY movieId;";
            PreparedStatement genreStatement = conn.prepareStatement(genreQuery);
            ResultSet genreRs = genreStatement.executeQuery();

            // Fill the genres into a genre corresponding to film objects
            while (genreRs.next()) {
                String movieId = genreRs.getString("movieId");
                String genres = genreRs.getString("genres");
                movieMap.get(movieId).addProperty("genres", genres);
            }
            genreRs.close();
            genreStatement.close();

            // first three genres: sort by the star's movie count desc, then use alphabetical order to break ties.
            // Batch query "stars"
            System.out.println(String.join(",", movieMap.keySet()));
            String starQuery = "WITH RankedStars AS (" +
                    "SELECT sim.movieId, s.id, s.name, COUNT(sim2.movieId) AS movie_count," +
                    "    ROW_NUMBER() OVER (PARTITION BY sim.movieId ORDER BY COUNT(sim2.movieId) DESC, s.name ASC) AS star_rank" +
                    "    FROM stars_in_movies sim" +
                    "    JOIN stars s ON sim.starId = s.id" +
                    "    JOIN stars_in_movies sim2 ON s.id = sim2.starId" +
                    "    WHERE sim.movieId IN (" + movieIdList + ")" +
                    "    GROUP BY sim.movieId, s.id, s.name)" +
                    "SELECT movieId," +
                    "    GROUP_CONCAT(name ORDER BY movie_count DESC, name ASC SEPARATOR ', ') AS stars_name,\n" +
                    "    GROUP_CONCAT(id ORDER BY movie_count DESC, name ASC SEPARATOR ', ') AS star_ids\n" +
                    "    FROM RankedStars\n" +
                    "    WHERE star_rank <= 3\n" +
                    "    GROUP BY movieId;";
            PreparedStatement starStatement = conn.prepareStatement(starQuery);
            ResultSet starRs = starStatement.executeQuery();

            // Fill stars and star_ids into the corresponding movie object
            while (starRs.next()) {
                String movieId = starRs.getString("movieId");
                String starName = starRs.getString("stars_name");
                String starId = starRs.getString("star_ids");

                movieMap.get(movieId).addProperty("stars", starName);
                movieMap.get(movieId).addProperty("star_ids", starId);
            }
            starRs.close();
            starStatement.close();

            // Returns movie data and total pages
            JsonObject resultJsonObject = new JsonObject();
            resultJsonObject.add("movies", jsonArray);  // Put the list of movies into the Movies field
            resultJsonObject.addProperty("totalPages", getTotalPages(conn, genre, browseTitle, pageSize));  // totalPages

            out.write(resultJsonObject.toString());  // Write the result to the output
            response.setStatus(200);

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            out.close();
        }
    }


    // Get movies' count, and determine the total pages
    private int getTotalPages(Connection conn, String genre, String browseTitle, int pageSize) throws Exception {
        // Construct the statement.
        StringBuilder countQuery = new StringBuilder("SELECT COUNT(*) AS total FROM movies m WHERE 1=1 ");
        if (genre != null && !genre.isEmpty()) {
            countQuery.append("AND m.id IN (SELECT gm.movieId FROM genres_in_movies gm " +
                    "JOIN genres g ON gm.genreId = g.id WHERE g.name = ?) ");
        }
        if (browseTitle != null && !browseTitle.isEmpty()) {
            if (browseTitle.equals("*")) {
                countQuery.append("AND m.title REGEXP '^[^a-zA-Z0-9]' ");
            } else {
                countQuery.append("AND LOWER(m.title) LIKE ? ");
            }
        }

        // prepare the statement, pass the ? parameters.
        PreparedStatement countStatement = conn.prepareStatement(countQuery.toString());
        int paramIndex = 1;
        if (genre != null && !genre.isEmpty()) {
            countStatement.setString(paramIndex++, genre);
        }
        if (browseTitle != null && !browseTitle.isEmpty() && !browseTitle.equals("*")) {
            countStatement.setString(paramIndex, browseTitle.toLowerCase() + "%");
        }

        // execute and get the total pages
        ResultSet countRs = countStatement.executeQuery();
        countRs.next();
        int totalMovies = countRs.getInt("total");
        countRs.close();
        countStatement.close();

        return (int) Math.ceil((double) totalMovies / pageSize);
    }
}
