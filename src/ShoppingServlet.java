import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This ShoppingServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/checkout.
 */
@WebServlet(name = "ShoppingServlet", urlPatterns = "/api/checkout")
public class ShoppingServlet extends HttpServlet {

    /**
     * handles GET requests to store session information
     */
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
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String action = request.getParameter("action");
        String movieId = request.getParameter("movieId");
        String title = request.getParameter("title");
        double price = Double.parseDouble(request.getParameter("price"));
        int quantity = Integer.parseInt(request.getParameter("quantity"));

        synchronized (user.getShoppingCart()) {
            {
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
        }
    }
}