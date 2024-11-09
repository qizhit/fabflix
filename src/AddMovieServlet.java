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

@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/add_movie")
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
        String movieTitle = request.getParameter("movie_title");
        String movieYearStr = request.getParameter("movie_year");
        String movieDirector = request.getParameter("movie_director");
        String starName = request.getParameter("star_name");
        String genreName = request.getParameter("genre_name");


        if (movieTitle == null || movieYearStr == null || movieDirector == null || starName == null || genreName == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"All parameters are required.\"}");
            return;
        }

        int movieYear;
        try {
            movieYear = Integer.parseInt(movieYearStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid year format.\"}");
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            // Prepare to call the stored procedure
            CallableStatement stmt = conn.prepareCall("{CALL add_movie(?, ?, ?, ?, ?)}");
            stmt.setString(1, movieTitle);
            stmt.setInt(2, movieYear);
            stmt.setString(3, movieDirector);
            stmt.setString(4, starName);
            stmt.setString(5, genreName);

            // Execute the stored procedure
            stmt.execute();

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"success\": \"Movie added successfully.\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"An error occurred while adding the movie.\"}");
        }
    }
}

