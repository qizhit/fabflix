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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

@WebServlet(name = "ConfirmationServlet", urlPatterns = "/api/confirm")
public class ConfirmationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    @Override
    public void init(ServletConfig config) {
        try {
            InitialContext context = new InitialContext();
            dataSource = (DataSource) context.lookup("java:comp/env/jdbc/readconnect");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();

        ArrayList<CartItem> cart = (ArrayList<CartItem>) session.getAttribute("shoppingCart");
        if (cart == null || cart.isEmpty()) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("message", "Your shopping cart is empty.");
            out.write(errorResponse.toString());
            response.setStatus(400);
            return;
        }

        double totalPrice = 0.0;

        try (Connection conn = dataSource.getConnection()) {
            String querySalesSQL =
                    "SELECT s.id AS saleId, s.movieId, m.title, m.price, s.quantity, s.saleDate " +
                            "FROM sales s JOIN movies m ON s.movieId = m.id WHERE s.saleDate = ?";

            PreparedStatement queryStmt = conn.prepareStatement(querySalesSQL);
            queryStmt.setDate(1, new java.sql.Date(new Date().getTime()));
            ResultSet rs = queryStmt.executeQuery();

            JsonArray salesArray = new JsonArray();
            while (rs.next()) {
                JsonObject saleJson = new JsonObject();
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");
                double singleMovieTotalPrice = price * quantity;  // calculate singleMovieTotalPrice

                saleJson.addProperty("saleId", rs.getInt("saleId"));
                saleJson.addProperty("movieId", rs.getString("movieId"));
                saleJson.addProperty("title", rs.getString("title"));
                saleJson.addProperty("quantity", quantity);
                saleJson.addProperty("price", price);
                saleJson.addProperty("singleMovieTotalPrice", singleMovieTotalPrice);
                saleJson.addProperty("saleDate", rs.getDate("saleDate").toString());

                totalPrice += singleMovieTotalPrice;
                salesArray.add(saleJson);
            }

            rs.close();
            queryStmt.close();

            // Create the response JSON object
            JsonObject responseJson = new JsonObject();
            responseJson.add("sales", salesArray);
            responseJson.addProperty("totalPrice", totalPrice);

            // Empty shopping cart
            session.removeAttribute("shoppingCart");

            // Returning Responses
            out.write(responseJson.toString());
            response.setStatus(200);

        } catch (SQLException e) {
            e.printStackTrace();
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("errorMessage", "An error occurred while processing your order.");
            out.write(errorResponse.toString());
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
