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
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

@WebServlet(name = "AddStarServlet", urlPatterns = "/api/add_star")
public class AddStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    private DataSource dataSource;


    public void init(ServletConfig config) {
        System.out.println("Initializing AddStarServlet");
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("doPost method called in AddStarServlet");
        String starName = request.getParameter("starName");
        String birthYearStr = request.getParameter("birthYear");
        response.setContentType("application/json");

        System.out.println("Received starName: " + starName);
        System.out.println("Received birthYear: " + birthYearStr);

        Integer birthYear = null;
        if (starName == null || starName.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\": false, \"message\": \"Star name is required.\"}");
            return;
        }

        if (birthYearStr != null && !birthYearStr.isEmpty()) {
            try {
                birthYear = Integer.parseInt(birthYearStr.trim());
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid birth year format.\"}");
                return;
            }
        }

        try (Connection conn = dataSource.getConnection()) {
            // Prepare the callable statement for the stored procedure
            CallableStatement stmt = conn.prepareCall("{CALL add_star(?, ?)}");
            stmt.setString(1, starName);  // Set star name

            if (birthYear != null) {
                stmt.setInt(2, birthYear);  // Set birth year if provided
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);  // Set birth year to NULL
            }

            // Execute the stored procedure
            stmt.executeUpdate();

            response.getWriter().write("{\"success\": true, \"message\": \"Star added successfully.\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\": false, \"message\": \"An error occurred while adding the star.\"}");
        }
    }
}

