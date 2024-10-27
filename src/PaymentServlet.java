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
import java.util.ArrayList;

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

        System.out.println("ENTER PAYMENT DO POST");

        System.out.println("Received: " + firstName + ", " + lastName + ", " + creditCardNumber + ", " + expirationDate);

        JsonObject responseJson = new JsonObject();

        try (Connection conn = dataSource.getConnection()) {
            // 验证信用卡信息
            String cardQuery = "SELECT cc.id AS cardNumber, c.id AS customerId FROM creditcards AS cc, customers AS c " +
                    "WHERE cc.id = ? AND cc.firstName = ? AND cc.lastName = ? AND cc.expiration = ? AND cc.id = c.ccId;";
            PreparedStatement cardStmt = conn.prepareStatement(cardQuery);
            cardStmt.setString(1, creditCardNumber);
            cardStmt.setString(2, firstName);
            cardStmt.setString(3, lastName);
            cardStmt.setString(4, expirationDate);

            System.out.println("i m here in first try");

            try (ResultSet rs = cardStmt.executeQuery()) {
                System.out.println("i m here in second try");
                if (!rs.next()) {
                    responseJson.addProperty("success", false);
                    responseJson.addProperty("message", "Invalid credit card details.");
                } else {
                    // 检查购物车是否为空
                    ArrayList<CartItem> cart = (ArrayList<CartItem>) session.getAttribute("shoppingCart");
                    if (cart == null || cart.isEmpty()) {
                        responseJson.addProperty("success", false);
                        responseJson.addProperty("message", "Your shopping cart is empty.");
                    } else {
                        // 插入销售记录
                        System.out.println("i m here in before insert");
                        String saleInsert = "INSERT INTO sales (customerId, movieId, saleDate, quantity) VALUES (?, ?, ?, ?);";
                        PreparedStatement saleStmt = conn.prepareStatement(saleInsert);

                        int customerId = Integer.parseInt(rs.getString("customerId"));
                        for (CartItem item : cart) {
                            saleStmt.setInt(1, customerId);
                            saleStmt.setString(2, item.getMovieId());
                            saleStmt.setDate(3, new java.sql.Date(new Date().getTime()));
                            System.out.println(new java.sql.Date(new Date().getTime()));
                            saleStmt.setInt(4, item.getQuantity());
                            saleStmt.executeUpdate();
                        }
                        System.out.println("i m here in after insert");

                        responseJson.addProperty("success", true);
                        responseJson.addProperty("message", "Order placed successfully.");

                        System.out.println("responseJson: ---" + responseJson);
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
