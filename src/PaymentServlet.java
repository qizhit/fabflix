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
import java.util.*;
import java.util.ArrayList;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    private DataSource readDataSource;  // For read operations
    private DataSource writeDataSource; // For write operations


    public void init(ServletConfig config) {
        try {
            InitialContext context = new InitialContext();
            readDataSource = (DataSource) context.lookup("java:comp/env/jdbc/readconnect");
            writeDataSource = (DataSource) context.lookup("java:comp/env/jdbc/writeconnect");

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

        JsonObject responseJson = new JsonObject();

        try (Connection readConn = readDataSource.getConnection()) {
            // Verify credit card information
            String cardQuery = "SELECT cc.id AS cardNumber, c.id AS customerId FROM creditcards AS cc, customers AS c " +
                    "WHERE cc.id = ? AND cc.firstName = ? AND cc.lastName = ? AND cc.expiration = ? AND cc.id = c.ccId";
            PreparedStatement cardStmt = readConn.prepareStatement(cardQuery);
            cardStmt.setString(1, creditCardNumber);
            cardStmt.setString(2, firstName);
            cardStmt.setString(3, lastName);
            cardStmt.setString(4, expirationDate);

            try (ResultSet rs = cardStmt.executeQuery()) {
                if (!rs.next()) {
                    responseJson.addProperty("success", false);
                    responseJson.addProperty("message", "Invalid credit card details.");
                } else {
                    // Check whether the cart is empty
                    ArrayList<CartItem> cart = (ArrayList<CartItem>) session.getAttribute("shoppingCart");
                    if (cart == null || cart.isEmpty()) {
                        responseJson.addProperty("success", false);
                        responseJson.addProperty("message", "Your shopping cart is empty.");
                    } else {
                        // Insert sales record
                        try (Connection writeConn = writeDataSource.getConnection()) {
                            String saleInsert = "INSERT INTO sales (customerId, movieId, saleDate, quantity) VALUES (?, ?, ?, ?)";
                            PreparedStatement saleStmt = writeConn.prepareStatement(saleInsert);

                            int customerId = Integer.parseInt(rs.getString("customerId"));
                            for (CartItem item : cart) {
                                saleStmt.setInt(1, customerId);
                                saleStmt.setString(2, item.getMovieId());
                                saleStmt.setDate(3, new java.sql.Date(new Date().getTime()));
                                saleStmt.setInt(4, item.getQuantity());
                                saleStmt.executeUpdate();
                            }

                            responseJson.addProperty("success", true);
                            responseJson.addProperty("message", "Order placed successfully.");
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            responseJson.addProperty("success", false);
            responseJson.addProperty("message", "An error occurred during payment processing.");
            response.setStatus(500);
        }

        response.setContentType("application/json");
        response.getWriter().write(responseJson.toString());
    }
}
