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

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String creditCardNumber = request.getParameter("creditCardNumber");
        String expirationDate = request.getParameter("expirationDate");
        HttpSession session = request.getSession();

        boolean orderSuccess = false;
        String message;

        if (firstName == null || lastName == null || creditCardNumber == null || expirationDate == null) {
            message = "Missing payment details. Please fill out all fields.";
        } else {
            try (Connection conn = dataSource.getConnection()) {
                // Validate credit card
                String cardQuery = "SELECT id FROM creditcards WHERE firstName = ? AND lastName = ? AND id = ? AND expiration = ?";
                PreparedStatement cardStmt = conn.prepareStatement(cardQuery);
                cardStmt.setString(1, firstName);
                cardStmt.setString(2, lastName);
                cardStmt.setString(3, creditCardNumber);
                cardStmt.setString(4, expirationDate);

                try (ResultSet rs = cardStmt.executeQuery()) {
                    if (!rs.next()) {
                        message = "Invalid credit card details. Please try again.";
                    } else {
                        // Check if the shopping cart has items
                        List<CartItem> cart = (List<CartItem>) session.getAttribute("shoppingCart");
                        if (cart == null || cart.isEmpty()) {
                            message = "Your shopping cart is empty.";
                        } else {
                            String saleInsert = "INSERT INTO sales (customerId, movieId, saleDate, quantity) VALUES (?, ?, ?, ?)";
                            PreparedStatement saleStmt = conn.prepareStatement(saleInsert);
                            int customerId = (Integer) session.getAttribute("customerId");

                            for (CartItem item : cart) {
                                saleStmt.setInt(1, customerId);
                                saleStmt.setString(2, item.getMovieId());
                                saleStmt.setDate(3, new java.sql.Date(new Date().getTime()));
                                saleStmt.setInt(4, item.getQuantity());
                                saleStmt.executeUpdate();
                            }

                            // Order successful
                            orderSuccess = true;
                            message = "Your order has been placed successfully!";
                            session.removeAttribute("shoppingCart");  // Clear the cart
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                message = "An error occurred during payment processing. Please try again.";
            }
        }

        // Store order status and message in the session and redirect
        session.setAttribute("orderSuccess", orderSuccess);
        session.setAttribute("confirmationMessage", message);
        if (orderSuccess) {
            response.sendRedirect("confirmation.html");
        } else {
            response.sendRedirect("payment.html");
        }
    }
}
