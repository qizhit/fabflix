import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import jakarta.servlet.ServletConfig;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Data source object
    private DataSource dataSource;

    // Initialize the data source and connect to the database connection pool
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    // Using doPost instead of doGet, the username and password will not show in url (address bar)
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        response.setContentType("application/json"); // Response mime type

        // get username and password from request
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        JsonObject responseJsonObject = new JsonObject();

        try (Connection conn = dataSource.getConnection()) {
            // Step 1: Check if the email exists
            String emailQuery = "SELECT password FROM customers WHERE email = ?";
            PreparedStatement emailStatement = conn.prepareStatement(emailQuery);
            emailStatement.setString(1, username);  // email is username, pass it to emailQuery

            try (ResultSet emailResultSet = emailStatement.executeQuery()) {
                if (!emailResultSet.next()) {
                    // If no result, the email/username doesn't exist
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "User " + username + " doesn't exist");
                    request.getServletContext().log("Login failed");
                } else {
                    // Step 2: Check if the password matches
                    String userPassword = emailResultSet.getString("password");
                    if (userPassword.equals(password)) {
                        // Login success: store user in session
                        request.getSession().setAttribute("user", new User(username));

                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "success");
                    } else {
                        // Password is incorrect
                        responseJsonObject.addProperty("status", "fail");
                        responseJsonObject.addProperty("message", "Incorrect password");
                        request.getServletContext().log("Login failed");
                    }
                }
            }
        } catch (SQLException e) {
            // Handle SQL exceptions and log the error
            e.printStackTrace();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Database error: " + e.getMessage());
        }

        PrintWriter out = response.getWriter();
        out.write(responseJsonObject.toString());
        out.close();
    }
}
