import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;


@WebServlet(name = "AddStarServlet", urlPatterns = "/api/_dashboard_add-star")
public class AddStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    private DataSource readDataSource;
    private DataSource writeDataSource;


    public void init(ServletConfig config) {
        System.out.println("Initializing AddStarServlet");
        try {
            InitialContext context = new InitialContext();

            // Lookup DataSources directly without checking the environment
            readDataSource = (DataSource) context.lookup("java:comp/env/jdbc/readconnect");
            writeDataSource = (DataSource) context.lookup("java:comp/env/jdbc/writeconnect");
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
            response.getWriter().write("{\"success\": false, \"message\": \"Star name is required.\"}");
            return;
        }

        if (birthYearStr != null && !birthYearStr.isEmpty()) {
            try {
                birthYear = Integer.parseInt(birthYearStr.trim());
            } catch (NumberFormatException e) {
                response.getWriter().write("{\"success\": false, \"message\": \"Invalid birth year format.\"}");
                return;
            }
        }

        try (Connection conn1 = readDataSource.getConnection(); Connection conn2 = writeDataSource.getConnection()) {
            // Prepare the callable statement for the stored procedure
            CallableStatement stmt = conn2.prepareCall("{CALL add_star(?, ?)}");
            stmt.setString(1, starName);  // Set star name

            if (birthYear != null) {
                stmt.setInt(2, birthYear);  // Set birth year if provided
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);  // Set birth year to NULL
            }
            //Get id

            // Execute the stored procedure
            stmt.executeUpdate();

            String getStarIdSQL = "SELECT id FROM stars WHERE name = ? AND (birthYear = ? OR birthYear IS NULL) LIMIT 1";
            try (PreparedStatement stmt1 = conn1.prepareStatement(getStarIdSQL)) {
                stmt1.setString(1, starName);
                if (birthYear != null) {
                    stmt1.setInt(2, birthYear);
                } else {
                    stmt1.setNull(2, java.sql.Types.INTEGER);
                }

                try (ResultSet rs = stmt1.executeQuery()) {
                    if (rs.next()) {
                        String starId = rs.getString("id");
                        response.getWriter().write("{\"success\": true, \"starId\": \"" + starId + "\"}");
                    } else {
                        response.getWriter().write("{\"success\": false, \"message\": \"Star not found.\"}");
                    }
                }
            }



        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().write("{\"success\": false, \"message\": \"An error occurred while adding the star.\"}");
        }
    }
}

