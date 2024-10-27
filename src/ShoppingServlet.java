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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

@WebServlet(name = "ShoppingServlet", urlPatterns = "/api/checkout")
public class ShoppingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    @Override
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles GET requests to retrieve session and cart information.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("sessionID", session.getId());
        responseJson.addProperty("lastAccessTime", new Date(session.getLastAccessedTime()).toString());

        JsonArray cartJsonArray = new JsonArray();
        for (CartItem item : user.getShoppingCart()) {
            JsonObject itemJson = new JsonObject();
            itemJson.addProperty("movieId", item.getMovieId());
            itemJson.addProperty("title", item.getTitle());
            itemJson.addProperty("price", item.getPrice());
            itemJson.addProperty("quantity", item.getQuantity());
            cartJsonArray.add(itemJson);
        }

        responseJson.add("shoppingCart", cartJsonArray);
        response.getWriter().write(responseJson.toString());
    }

    /**
     * Handles POST requests to add, update, or remove items in the shopping cart.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        JsonObject responseJson = new JsonObject();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String action = request.getParameter("action");
        String movieId = request.getParameter("movieId");
        String title = request.getParameter("title");
        int quantity = Integer.parseInt(request.getParameter("quantity"));

        try (Connection conn = dataSource.getConnection()) {
            double price = retrieveMoviePrice(conn, movieId);

            if (price < 0) {
                responseJson.addProperty("success", false);
                responseJson.addProperty("message", "Movie not found.");
                response.getWriter().write(responseJson.toString());
                return;
            }

            synchronized (user.getShoppingCart()) {
                switch (action) {
                    case "add":
                        user.addToCart(movieId, title, price, quantity);
                        break;
                    case "update":
                        user.updateQuantity(movieId, quantity);
                        break;
                    case "remove":
                        user.removeFromCart(movieId);
                        break;
                    default:
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return;
                }
            }

            JsonObject responseJsonObject = new JsonObject();
            JsonArray cartJsonArray = new JsonArray();
            for (CartItem item : user.getShoppingCart()) {
                JsonObject itemJson = new JsonObject();
                itemJson.addProperty("movieId", item.getMovieId());
                itemJson.addProperty("title", item.getTitle());
                itemJson.addProperty("price", item.getPrice());
                itemJson.addProperty("quantity", item.getQuantity());
                cartJsonArray.add(itemJson);
            }

            responseJsonObject.add("shoppingCart", cartJsonArray);
            response.getWriter().write(responseJsonObject.toString());

        } catch (Exception e) {
            e.printStackTrace();
            responseJson.addProperty("success", false);
            responseJson.addProperty("message", "An error occurred while processing your request.");
            response.getWriter().write(responseJson.toString());
        }
    }

    /**
     * Retrieves the price of a movie from the database based on its ID.
     * @param conn The database connection
     * @param movieId The ID of the movie to retrieve
     * @return The price of the movie, or -1 if not found
     */
    private double retrieveMoviePrice(Connection conn, String movieId) throws Exception {
        String priceQuery = "SELECT price FROM movies WHERE id = ?";
        try (PreparedStatement priceStmt = conn.prepareStatement(priceQuery)) {
            priceStmt.setString(1, movieId);
            try (ResultSet rs = priceStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("price");
                }
            }
        }
        return -1;
    }
}
