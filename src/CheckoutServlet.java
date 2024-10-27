import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "CheckoutServlet", urlPatterns = "/api/checkout")
public class CheckoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the shopping cart from session
        HttpSession session = request.getSession();
        ArrayList<CartItem> shoppingCart = (ArrayList<CartItem>) session.getAttribute("shoppingCart");

        // If the cart is empty, initialize the cart and store the session
        if (shoppingCart == null) {
            shoppingCart = new ArrayList<>();
            session.setAttribute("shoppingCart", shoppingCart);
        }

        // Convert shopping cart data to JSON format
        JsonArray cartJsonArray = new JsonArray();
        for (CartItem item : shoppingCart) {
            JsonObject itemJson = new JsonObject();
            itemJson.addProperty("movieId", item.getMovieId());
            itemJson.addProperty("title", item.getTitle());
            itemJson.addProperty("price", item.getPrice());
            itemJson.addProperty("quantity", item.getQuantity());
            cartJsonArray.add(itemJson);
        }

        // Returns a JSON response to the front-end
        JsonObject responseJson = new JsonObject();
        responseJson.add("shoppingCart", cartJsonArray);

        response.setStatus(200);
        response.setContentType("application/json");
        response.getWriter().write(responseJson.toString());
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        // Get the cart from the session and create a new cart if it is empty
        ArrayList<CartItem> shoppingCart = (ArrayList<CartItem>) session.getAttribute("shoppingCart");
        Double totalPrice = (Double) session.getAttribute("totalPrice");
        if (shoppingCart == null) {
            shoppingCart = new ArrayList<>();
            session.setAttribute("shoppingCart", shoppingCart);
        }
        if (totalPrice == null) {
            totalPrice = 0.00;
            session.setAttribute("totalPrice", totalPrice);
        }

        // Get request parameters
        String action = request.getParameter("action"); // "add", "update", "remove"
        String movieId = request.getParameter("movieId");
        String title = request.getParameter("title");
        double price = Double.parseDouble(request.getParameter("price"));
        int quantity = Integer.parseInt(request.getParameter("quantity"));

        JsonObject responseJson = new JsonObject();

        // Perform the corresponding action according to the action
        synchronized (shoppingCart) {
            switch (action) {
                case "add":
                    addItemToCart(shoppingCart, movieId, title, price, quantity);
                    System.out.println(shoppingCart);
                    break;
                case "update":
                    updateItemQuantity(shoppingCart, movieId, quantity);
                    System.out.println(shoppingCart);
                    break;
                case "remove":
                    removeItemFromCart(shoppingCart, movieId);
                    break;
                default:
                    response.setStatus(500); // Bad request
                    responseJson.addProperty("message", "Invalid action.");
                    response.getWriter().write(responseJson.toString());
                    return;
            }
        }

        // Converts the cart contents to JSON and returns them to the front end
        JsonArray cartJsonArray = new JsonArray();
        for (CartItem item : shoppingCart) {
            JsonObject itemJson = new JsonObject();
            itemJson.addProperty("movieId", item.getMovieId());
            itemJson.addProperty("title", item.getTitle());
            itemJson.addProperty("price", item.getPrice());
            itemJson.addProperty("quantity", item.getQuantity());
            cartJsonArray.add(itemJson);
        }

        responseJson.add("shoppingCart", cartJsonArray);
        response.setStatus(200);
        response.setContentType("application/json");
        response.getWriter().write(responseJson.toString());
    }

    // Add items to cart
    private void addItemToCart(ArrayList<CartItem> cart, String movieId, String title, double price, int quantity) {
        for (CartItem item : cart) {
            if (item.getMovieId().equals(movieId)) {
                // If the item is already in the cart, increase the amount
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        // If the item is not in the cart, add a new item
        cart.add(new CartItem(movieId, title, price, quantity));
    }

    // Update the amount of an item in your cart
    private void updateItemQuantity(ArrayList<CartItem> cart, String movieId, int quantity) {
        for (CartItem item : cart) {
            if (item.getMovieId().equals(movieId)) {
                if (quantity > 0) {
                    item.setQuantity(quantity);
                } else {
                    cart.remove(item); // If the quantity is 0, remove the item
                }
                return;
            }
        }
    }

    // Remove items from shopping cart
    private void removeItemFromCart(ArrayList<CartItem> cart, String movieId) {
        cart.removeIf(item -> item.getMovieId().equals(movieId));
    }

}
