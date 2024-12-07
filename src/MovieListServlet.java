import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;


// Declaring a WebServlet called MovieListServlet, which maps to url "/api/movie_list"
@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movie_list")
public class MovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

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

        // Gets browsing parameters
        String browseGenre = request.getParameter("browse_genre");
        String browseTitle = request.getParameter("browse_title");
        // Get search parameters
        String searchTitle = request.getParameter("title");
        String searchYear = request.getParameter("year");
        String searchDirector = request.getParameter("director");
        String searchStar = request.getParameter("star");
        String sortBy;
        String sortOrder1;
        String sortOrder2;
        int page;
        int pageSize;
        boolean isSessionData;

        // Get the HttpSession object: restore the sorting, pagination, currentPage when return from a single page.
        HttpSession session = request.getSession();
        if (browseGenre == null && browseTitle == null && searchTitle == null
                && searchYear == null && searchDirector == null && searchStar == null) {
            // return page, need retore from session
            browseGenre = (String) session.getAttribute("browseGenre");
            browseTitle = (String) session.getAttribute("browseTitle");
            searchTitle = (String) session.getAttribute("searchTitle");
            searchYear = (String) session.getAttribute("searchYear");
            searchDirector = (String) session.getAttribute("searchDirector");
            searchStar = (String) session.getAttribute("searchStar");
            sortBy = (String) session.getAttribute("sortBy");
            sortOrder1 = (String) session.getAttribute("sortOrder1");
            sortOrder2 = (String) session.getAttribute("sortOrder2");
            page = (int) session.getAttribute("page");
            pageSize = (int) session.getAttribute("pageSize");
            isSessionData = true;
        } else {  // new browse or search page
            // Gets the sorting parameters
            sortBy = request.getParameter("sortBy");
            sortOrder1 = request.getParameter("sortOrder1");
            sortOrder2 = request.getParameter("sortOrder2");
            // Get Pagination parameters
            page = Integer.parseInt(request.getParameter("page"));  // currentPage
            pageSize = Integer.parseInt(request.getParameter("pageSize"));  // Number of movies per page
            isSessionData = false;
        }
        // Calculated offset
        int offset = (page - 1) * pageSize;
        // Save the state to the session
        session.setAttribute("browseGenre", browseGenre);
        session.setAttribute("browseTitle", browseTitle);
        session.setAttribute("searchTitle", searchTitle);
        session.setAttribute("searchYear", searchYear);
        session.setAttribute("searchDirector", searchDirector);
        session.setAttribute("searchStar", searchStar);
        session.setAttribute("sortBy", sortBy);
        session.setAttribute("sortOrder1", sortOrder1);
        session.setAttribute("sortOrder2", sortOrder2);
        session.setAttribute("page", page);
        session.setAttribute("pageSize", pageSize);


        // Construct sort statement
        String orderByClause = "";
        if ("title".equals(sortBy)) {
            orderByClause = "ORDER BY m.title " + sortOrder1 + ", r.rating " + sortOrder2;  // ORDER BY m.title ase, r.rating ase
        } else if ("rating".equals(sortBy)) {
            orderByClause = "ORDER BY r.rating " + sortOrder1 + ", m.title " + sortOrder2;
        }

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Construct the query for selecting movies
            StringBuilder queryBuilder = new StringBuilder(
                    "SELECT DISTINCT m.id, m.title, m.year, m.director, r.rating, m.price " +
                            "FROM movies m " +
                            "LEFT JOIN ratings r ON m.id = r.movieId " +
                            "LEFT JOIN stars_in_movies sim ON m.id = sim.movieId " +
                            "LEFT JOIN stars s ON sim.starId = s.id " +
                            "WHERE 1=1 ");

            // Browsing function: filter by genres or title(first letter)
            if (browseGenre != null && !browseGenre.isEmpty()) {
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

            // No browsing, Advanced Search function: Filter by film title, year, director, star
            StringBuilder fullTextQuery = new StringBuilder();
            if (searchTitle != null && !searchTitle.isEmpty()) {
                String[] keywords = searchTitle.trim().split("\\s+");
                // Construct a query string for FULLTEXT search
                for (int i = 0; i < keywords.length; i++) {
                    if (i > 0) fullTextQuery.append(" ");
                    fullTextQuery.append("+").append(keywords[i]).append("*"); // Add "+" for AND logic and "*" for prefix search
                }
                // MySQL FULLTEXT query
                queryBuilder.append("AND MATCH(title) AGAINST (? IN BOOLEAN MODE) ");
            }
            if (searchYear != null && !searchYear.isEmpty()) {
                queryBuilder.append("AND m.year = ? ");
            }
            if (searchDirector != null && !searchDirector.isEmpty()) {
                queryBuilder.append("AND LOWER(m.director) LIKE ? ");
            }
            if (searchStar != null && !searchStar.isEmpty()) {
                queryBuilder.append("AND LOWER(s.name) LIKE ? ");
            }

            // Sorting and paginatioin function
            queryBuilder.append(orderByClause);
            queryBuilder.append(" LIMIT ? OFFSET ?");

            // prepare the query statement and pass parameters
            PreparedStatement movieStatement = conn.prepareStatement(queryBuilder.toString());

            // Browsing parameter binding
            int paramIndex = 1;
            if (browseGenre != null && !browseGenre.isEmpty()) {
                movieStatement.setString(paramIndex++, browseGenre);
            }
            if (browseTitle != null && !browseTitle.isEmpty() && !browseTitle.equals("*")) {
                // match movies that the first character is browseTitle
                movieStatement.setString(paramIndex++, browseTitle.toLowerCase() + "%");
            }

            // Advanced Searching parameter binding
            if (searchTitle != null && !searchTitle.isEmpty()) {
                movieStatement.setString(paramIndex++, fullTextQuery.toString());
            }
            if (searchYear != null && !searchYear.isEmpty()) {
                movieStatement.setInt(paramIndex++, Integer.parseInt(searchYear));
            }
            if (searchDirector != null && !searchDirector.isEmpty()) {
                movieStatement.setString(paramIndex++, "%" + searchDirector.toLowerCase() + "%");
            }
            if (searchStar != null && !searchStar.isEmpty()) {
                movieStatement.setString(paramIndex++, "%" + searchStar.toLowerCase() + "%");
            }

            // Pagination parameter binding
            movieStatement.setInt(paramIndex++, pageSize);
            movieStatement.setInt(paramIndex++, offset);

            // execute
            ResultSet movieRs = movieStatement.executeQuery();

            // key: movieId, value: jsonObject with properties
            Map<String, JsonObject> movieMap = new HashMap<>();
            // used to store all movieId for keep correct order and assign to jsonArray later
            List<String> movieIdArray = new ArrayList<>();

            while (movieRs.next()) {
                String movieId = movieRs.getString("id");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movieId", movieId);
                jsonObject.addProperty("title", movieRs.getString("title"));
                jsonObject.addProperty("year", movieRs.getString("year"));
                jsonObject.addProperty("director", movieRs.getString("director"));
                jsonObject.addProperty("rating", movieRs.getString("rating"));
                jsonObject.addProperty("price", movieRs.getString("price"));

                movieMap.put(movieId, jsonObject);  // use to correspond to first-3-genres and first-3-stars
                movieIdArray.add(movieId);
            }
            movieRs.close();
            movieStatement.close();

            if (!movieMap.isEmpty()) {

                // Format the list of movie ids into a string format in SQL, prepare for genres and stars query
                String movieIdListPlaceholders = movieMap.keySet().stream()
                        .map(id -> "?")
                        .reduce((a, b) -> a + "," + b)
                        .orElse("");

                // first three genres: sort by alphabetical order
                // Batch query "genres"
                String genreQuery = "WITH RankedGenres AS (\n" +
                        "    SELECT gm.movieId, g.name, \n" +
                        "    ROW_NUMBER() OVER (PARTITION BY gm.movieId ORDER BY g.name ASC) AS genre_rank \n" +
                        "    FROM genres_in_movies gm\n" +
                        "    JOIN genres g ON gm.genreId = g.id\n" +
                        "    WHERE gm.movieId IN (" + movieIdListPlaceholders + ")\n" +
                        "    ORDER BY g.name)\n" +
                        "SELECT movieId, GROUP_CONCAT(name ORDER BY name ASC SEPARATOR ', ') AS genres\n" +
                        "    FROM RankedGenres WHERE genre_rank <= 3\n" +
                        "    GROUP BY movieId";
                PreparedStatement genreStatement = conn.prepareStatement(genreQuery);
                int genreParamIndex = 1;
                for (String movieId : movieMap.keySet()) {
                    genreStatement.setString(genreParamIndex++, movieId);
                }
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
                String starQuery = "WITH RankedStars AS (" +
                        "SELECT sim.movieId, s.id, s.name, COUNT(sim2.movieId) AS movie_count," +
                        "    ROW_NUMBER() OVER (PARTITION BY sim.movieId ORDER BY COUNT(sim2.movieId) DESC, s.name ASC) AS star_rank" +
                        "    FROM stars_in_movies sim" +
                        "    JOIN stars s ON sim.starId = s.id" +
                        "    JOIN stars_in_movies sim2 ON s.id = sim2.starId" +
                        "    WHERE sim.movieId IN (" + movieIdListPlaceholders + ")" +
                        "    GROUP BY sim.movieId, s.id, s.name)" +
                        "SELECT movieId," +
                        "    GROUP_CONCAT(name ORDER BY movie_count DESC, name ASC SEPARATOR ', ') AS stars_name,\n" +
                        "    GROUP_CONCAT(id ORDER BY movie_count DESC, name ASC SEPARATOR ', ') AS star_ids\n" +
                        "    FROM RankedStars\n" +
                        "    WHERE star_rank <= 3\n" +
                        "    GROUP BY movieId";
                PreparedStatement starStatement = conn.prepareStatement(starQuery);
                int starParamIndex = 1;
                for (String movieId : movieMap.keySet()) {
                    starStatement.setString(starParamIndex++, movieId);
                }
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
            }

            JsonArray jsonArray = new JsonArray();  // used to store all movie jsonObject

            for (String movieId : movieIdArray) {
                JsonObject movieObject = movieMap.get(movieId);
                jsonArray.add(movieObject);
            }

            // Returns movie data and total pages
            JsonObject resultJsonObject = new JsonObject();
            resultJsonObject.add("movies", jsonArray);  // Put the list of movies into the Movies field
            resultJsonObject.addProperty("isSessionData", isSessionData);
            resultJsonObject.addProperty("browseGenre", browseGenre);
            resultJsonObject.addProperty("browseTitle", browseTitle);
            resultJsonObject.addProperty("searchTitle", searchTitle);
            resultJsonObject.addProperty("searchYear", searchYear);
            resultJsonObject.addProperty("searchDirector", searchDirector);
            resultJsonObject.addProperty("searchStar", searchStar);
            resultJsonObject.addProperty("sortBy", sortBy);
            resultJsonObject.addProperty("sortOrder1", sortOrder1);
            resultJsonObject.addProperty("sortOrder2", sortOrder2);
            resultJsonObject.addProperty("page", page); // currentPage
            resultJsonObject.addProperty("pageSize", pageSize); // currentPage
            resultJsonObject.addProperty("totalPages", getTotalPages(conn, browseGenre, browseTitle, searchTitle, searchYear, searchDirector, searchStar, pageSize));  // totalPages

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
    private int getTotalPages(Connection conn, String browseGenre, String browseTitle, String searchTitle,
                              String searchYear, String searchDirector, String searchStar, int pageSize) throws Exception {
        // Construct the statement.
        StringBuilder countQuery = new StringBuilder(
                "SELECT COUNT(DISTINCT m.id) AS total FROM movies m " +
                        "LEFT JOIN stars_in_movies sim ON m.id = sim.movieId " +
                        "LEFT JOIN stars s ON sim.starId = s.id WHERE 1=1 "
        );
        // Browsing:
        if (browseGenre != null && !browseGenre.isEmpty()) {
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
        // Searching:
        StringBuilder fullTextQuery = new StringBuilder();
        if (searchTitle != null && !searchTitle.isEmpty()) {
            String[] keywords = searchTitle.trim().split("\\s+");
            // Construct a query string for FULLTEXT search
            for (int i = 0; i < keywords.length; i++) {
                if (i > 0) fullTextQuery.append(" ");
                fullTextQuery.append("+").append(keywords[i]).append("*"); // Add "+" for AND logic and "*" for prefix search
            }
            // MySQL FULLTEXT query
            countQuery.append("AND MATCH(title) AGAINST (? IN BOOLEAN MODE) ");
        }
        if (searchYear != null && !searchYear.isEmpty()) {
            countQuery.append("AND m.year = ? ");
        }
        if (searchDirector != null && !searchDirector.isEmpty()) {
            countQuery.append("AND LOWER(m.director) LIKE ? ");
        }
        if (searchStar != null && !searchStar.isEmpty()) {
            countQuery.append("AND LOWER(s.name) LIKE ? ");
        }

        // prepare the statement, pass the ? parameters.
        PreparedStatement countStatement = conn.prepareStatement(countQuery.toString());
        int paramIndex = 1;
        // Browsing
        if (browseGenre != null && !browseGenre.isEmpty()) {
            countStatement.setString(paramIndex++, browseGenre);
        }
        if (browseTitle != null && !browseTitle.isEmpty() && !browseTitle.equals("*")) {
            countStatement.setString(paramIndex++, browseTitle.toLowerCase() + "%");
        }
        // Searching
        if (searchTitle != null && !searchTitle.isEmpty()) {
            countStatement.setString(paramIndex++, fullTextQuery.toString());
        }
        if (searchYear != null && !searchYear.isEmpty()) {
            countStatement.setInt(paramIndex++, Integer.parseInt(searchYear));
        }
        if (searchDirector != null && !searchDirector.isEmpty()) {
            countStatement.setString(paramIndex++, "%" + searchDirector.toLowerCase() + "%");
        }
        if (searchStar != null && !searchStar.isEmpty()) {
            countStatement.setString(paramIndex++, "%" + searchStar.toLowerCase() + "%");
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
