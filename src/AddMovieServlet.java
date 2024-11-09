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


@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/_dashboard_add-movie")
public class AddMovieServlet extends HttpServlet {
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
        response.setContentType("application/json");
        String movieTitle = request.getParameter("movieTitle");
        String movieYearStr = request.getParameter("movieYear");
        String movieDirector = request.getParameter("movieDirector");
        String starName = request.getParameter("starName");
        String genreName = request.getParameter("genreName");

        if (movieTitle.isEmpty() || movieYearStr.isEmpty() || movieDirector.isEmpty() || starName.isEmpty() || genreName.isEmpty()) {
            response.getWriter().write("{\"success\": false, \"message\": \"All fields are required.\"}");
            return;
        }

        int movieYear;
        try {
            movieYear = Integer.parseInt(movieYearStr);
        } catch (NumberFormatException e) {
            response.getWriter().write("{\"success\": false, \"message\": \"Invalid year format.\"}");
            return;
        }

        try (Connection conn = dataSource.getConnection()) {

            String checkQuery = "SELECT id FROM movies WHERE title = ? AND year = ? AND director = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, movieTitle);
            checkStmt.setInt(2, movieYear);
            checkStmt.setString(3, movieDirector);

            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Movie with the same title, year, and director exists
                response.getWriter().write("{\"success\": false, " +
                        "\"message\": \"A movie with the same title, year, and director already exists.\"}");
                return;
            }

            // Prepare to call the stored procedure
            CallableStatement stmt = conn.prepareCall("{CALL add_movie(?, ?, ?, ?, ?)}");
            stmt.setString(1, movieTitle);
            stmt.setInt(2, movieYear);
            stmt.setString(3, movieDirector);
            stmt.setString(4, starName);
            stmt.setString(5, genreName);

            // Execute the stored procedure
            stmt.execute();

            response.getWriter().write("{\"success\": true, \"message\": \"Movie added successfully.\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().write("{\"success\": false, \"message\": \"An error occurred while adding the movie.\"}");
        }
    }
}

