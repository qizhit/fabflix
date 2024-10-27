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


// Declaring a WebServlet called PaymentServlet, which maps to url "/api/payment"
@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        JsonObject responseJson = new JsonObject();

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String creditCardNumber = request.getParameter("creditCardNumber");
        String expirationDate = request.getParameter("expirationDate");

        if (firstName == null || lastName == null || creditCardNumber == null || expirationDate == null) {
            responseJson.addProperty("success", false);
            responseJson.addProperty("errorMessage", "Missing payment details. Please fill out all fields.");
            response.getWriter().write(responseJson.toString());
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            // Validate credit card information
            String cardQuery = "SELECT id FROM creditcards WHERE firstName = ? AND lastName = ? AND id = ? AND expiration = ?";
            try (PreparedStatement cardStmt = conn.prepareStatement(cardQuery)) {
                cardStmt.setString(1, firstName);
                cardStmt.setString(2, lastName);
                cardStmt.setString(3, creditCardNumber);
                cardStmt.setString(4, expirationDate);

                ResultSet rs = cardStmt.executeQuery();
                if (!rs.next()) {
                    responseJson.addProperty("success", false);
                    responseJson.addProperty("errorMessage", "Invalid credit card details. Please try again.");
                    response.getWriter().write(responseJson.toString());
                    return;
                }
            }

            HttpSession session = request.getSession();
            List<CartItem> cart = (List<CartItem>) session.getAttribute("shoppingCart");

            if (cart == null || cart.isEmpty()) {
                responseJson.addProperty("success", false);
                responseJson.addProperty("errorMessage", "Your shopping cart is empty.");
                response.getWriter().write(responseJson.toString());
                return;
            }

            String saleInsert = "INSERT INTO sales (customerId, movieId, saleDate, quantity) VALUES (?, ?, ?, ?)";
            try (PreparedStatement saleStmt = conn.prepareStatement(saleInsert)) {
                int customerId = (Integer) session.getAttribute("customerId");
                for (CartItem item : cart) {
                    saleStmt.setInt(1, customerId);
                    saleStmt.setString(2, item.getMovieId());
                    saleStmt.setDate(3, new java.sql.Date(new Date().getTime()));
                    saleStmt.setInt(4, item.getQuantity());
                    saleStmt.executeUpdate();
                }
            }

            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "Your order has been placed successfully!");

            // Clear shopping cart after successful payment
            session.removeAttribute("shoppingCart");

        } catch (Exception e) {
            e.printStackTrace();
            responseJson.addProperty("success", false);
            responseJson.addProperty("errorMessage", "An error occurred during payment processing. Please try again.");
        }

        response.getWriter().write(responseJson.toString());
    }

    /**
     * Handles GET requests to check the payment details and session status
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        JsonObject responseJson = new JsonObject();

        HttpSession session = request.getSession();
        String sessionId = session.getId();
        responseJson.addProperty("sessionID", sessionId);
        responseJson.addProperty("lastAccessTime", new Date(session.getLastAccessedTime()).toString());

        // Retrieve shopping cart details
        List<CartItem> cart = (List<CartItem>) session.getAttribute("shoppingCart");
        if (cart == null || cart.isEmpty()) {
            responseJson.addProperty("cartEmpty", true);
            responseJson.addProperty("errorMessage", "Your shopping cart is empty.");
        } else {
            double totalPrice = cart.stream().mapToDouble(CartItem::getTotalPrice).sum();
            responseJson.addProperty("totalPrice", totalPrice);
            responseJson.addProperty("cartEmpty", false);
        }

        response.getWriter().write(responseJson.toString());
    }
}
