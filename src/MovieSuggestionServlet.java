import java.io.IOException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet(name = "MovieSuggestionServlet", urlPatterns = "/api/movie-suggestion")
public class MovieSuggestionServlet extends HttpServlet {
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

    /*
     *
     * Match the query against superheroes and return a JSON response.
     *
     * For example, if the query is "movie":
     * The JSON response look like this:
     * [
     * 	{ "value": "movieTitle1", "data": { "movieId": "tt0094859" } },
     * 	{ "value": "movieTitle2", "data": { "movieId": "tt0094859" } }
     * ]
     *
     * The format is like this because it can be directly used by the
     *   JSON auto complete library this example is using. So that you don't have to convert the format.
     *
     * The response contains a list of suggestions.
     * In each suggestion object, the "value" is the item string shown in the dropdown list,
     *   the "data" object can contain any additional information.
     *
     *
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // setup the response json arrray
            JsonArray jsonArray = new JsonArray();

            // get the query string from parameter
            String query = request.getParameter("query");

            // return the empty json array if query is null or empty
            if (query == null || query.trim().isEmpty()) {
                response.getWriter().write(jsonArray.toString());
                return;
            }

            // Full text search with MySQL to find the matches on movies and stars, and add the results to JSON Array
            // Split the query string into tokens
            String[] keywords = query.trim().split("\\s+");

            // Construct a query string for FULLTEXT search
            StringBuilder fullTextQuery = new StringBuilder();
            for (int i = 0; i < keywords.length; i++) {
                if (i > 0) fullTextQuery.append(" ");
                fullTextQuery.append("+").append(keywords[i]).append("*"); // Add "+" for AND logic and "*" for prefix search
            }

            // MySQL FULLTEXT query
            String sql = "SELECT id, title FROM movies WHERE MATCH(title) AGAINST (? IN BOOLEAN MODE) LIMIT 10";

            try (var connection = dataSource.getConnection();
                 var statement = connection.prepareStatement(sql)) {
                // Set the full-text search query
                statement.setString(1, fullTextQuery.toString());

                try (var rs = statement.executeQuery()) {
                    while (rs.next()) {
                        String movieId = rs.getString("id");
                        String movieTitle = rs.getString("title");

                        // Construct the JSON object for each suggestion
                        JsonObject suggestion = new JsonObject();
                        suggestion.addProperty("value", movieTitle);

                        JsonObject additionalData = new JsonObject();
                        additionalData.addProperty("movieId", movieId);
                        suggestion.add("data", additionalData);

                        jsonArray.add(suggestion);
                    }
                }
            }
            // Write the JSON array back to the response
            response.getWriter().write(jsonArray.toString());
        } catch (Exception e) {
            System.out.println(e);
            response.sendError(500, e.getMessage());
        }
    }
}
