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

    private DataSource readDataSource;
    private DataSource writeDataSource;

    public void init(ServletConfig config) {
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
        response.setContentType("application/json");
        String movieTitle = request.getParameter("movieTitle");
        String movieYearStr = request.getParameter("movieYear");
        String movieDirector = request.getParameter("movieDirector");
        String starName = request.getParameter("starName");
        String starBirthYearStr = request.getParameter("starBirthYear");  // Optional field for star birth year
        String genreName = request.getParameter("genreName");

        if (movieTitle.isEmpty() || movieYearStr.isEmpty() || movieDirector.isEmpty() || starName.isEmpty() || genreName.isEmpty()) {
            response.getWriter().write("{\"success\": false, \"message\": \"All fields except birth year of actor are required.\"}");
            return;
        }

        int movieYear;
        try {
            movieYear = Integer.parseInt(movieYearStr);
        } catch (NumberFormatException e) {
            response.getWriter().write("{\"success\": false, \"message\": \"Invalid year format.\"}");
            return;
        }

        Integer starBirthYear = null;
        if (starBirthYearStr != null && !starBirthYearStr.isEmpty()) {
            try {
                starBirthYear = Integer.parseInt(starBirthYearStr);
            } catch (NumberFormatException e) {
                response.getWriter().write("{\"success\": false, \"message\": \"Invalid star birth year format.\"}");
                return;
            }
        }

        try {
            Connection conn1 = readDataSource.getConnection();
            String checkQuery = "SELECT id FROM movies WHERE title = ? AND year = ? AND director = ?";
            PreparedStatement checkStmt = conn1.prepareStatement(checkQuery);
            checkStmt.setString(1, movieTitle);
            checkStmt.setInt(2, movieYear);
            checkStmt.setString(3, movieDirector);

            ResultSet rs = checkStmt.executeQuery();
            checkStmt.close();

            if (rs.next()) {
                // Movie with the same title, year, and director exists
                response.getWriter().write("{\"success\": false, " +
                        "\"message\": \"A movie with the same title, year, and director already exists.\"}");
                return;
            }

            Connection conn2 = writeDataSource.getConnection();
            // Prepare to call the stored procedure
            CallableStatement stmt = conn2.prepareCall("{CALL add_movie(?, ?, ?, ?, ?,?)}");
            stmt.setString(1, movieTitle);
            stmt.setInt(2, movieYear);
            stmt.setString(3, movieDirector);
            stmt.setString(4, starName);
            if (starBirthYear != null) {
                stmt.setInt(5, starBirthYear);  // Set star birth year if provided
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);  // Set to NULL if birth year is not provided
            }
            stmt.setString(6, genreName);

            // Execute the stored procedure
            stmt.execute();
            stmt.close();

            // Retrieve the inserted IDs for movie, star, and genre
            String movieId = getMovieId(conn1, movieTitle, movieYear, movieDirector);
            String starId = getStarId(conn1, starName, starBirthYear);
            String genreId = getGenreId(conn1, genreName);

            response.getWriter().write(String.format(
                    "{\"success\": true, \"message\": \"Movie, star, and genre added successfully.\", \"movieId\": \"%s\", \"starId\": \"%s\", \"genreId\": \"%s\"}",
                    movieId, starId, genreId
            ));

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().write("{\"success\": false, \"message\": \"An error occurred while adding the movie.\"}");
        }
    }
    private String getMovieId(Connection conn, String title, int year, String director) throws SQLException {
        String query = "SELECT id FROM movies WHERE title = ? AND year = ? AND director = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, title);
            stmt.setInt(2, year);
            stmt.setString(3, director);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("id");
            }
        }
        return null;
    }

    private String getStarId(Connection conn, String starName, Integer birthYear) throws SQLException {
        String query;
        if (birthYear != null) {
            query = "SELECT id FROM stars WHERE name = ? AND birthYear = ?";
        } else {
            query = "SELECT id FROM stars WHERE name = ?";
        }
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, starName);
            if (birthYear != null) {
                stmt.setInt(2, birthYear);
            }
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("id");
            }
        }
        return null;
    }

    private String getGenreId(Connection conn, String genreName) throws SQLException {
        String query = "SELECT id FROM genres WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, genreName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("id");
            }
        }
        return null;
    }

}

