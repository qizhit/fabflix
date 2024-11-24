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
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// Declaring a WebServlet called MovieListServlet, which maps to url "/api/movie_list"
@WebServlet(name = "DashboardMainServlet", urlPatterns = "/api/_dashboard_main")
public class DashboardMainServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/readconnect");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        StringBuilder jsonResponse = new StringBuilder();
        jsonResponse.append("[");

        try (Connection conn = dataSource.getConnection()) {
            String tableQuery = "SELECT table_name FROM information_schema.tables WHERE table_schema = ?";
            PreparedStatement tableStmt = conn.prepareStatement(tableQuery);

            tableStmt.setString(1, "moviedb");
            ResultSet tablesRs = tableStmt.executeQuery();

            boolean firstTable = true;
            // Step 2: For each table, retrieve column names and data types
            while (tablesRs.next()) {
                if (!firstTable) {
                    jsonResponse.append(",");
                }
                firstTable = false;

                String tableName = tablesRs.getString("table_name");
                jsonResponse.append("{");
                jsonResponse.append("\"tableName\":\"").append(tableName).append("\",");


                // Step 3: Retrieve column metadata for each table using PreparedStatement
                String columnQuery = "SELECT column_name, data_type FROM information_schema.columns WHERE table_schema = ? AND table_name = ?";
                PreparedStatement columnStmt = conn.prepareStatement(columnQuery);
                columnStmt.setString(1, "moviedb");
                columnStmt.setString(2, tableName);
                ResultSet columnsRs = columnStmt.executeQuery();

                jsonResponse.append("\"columns\":[");
                boolean firstColumn = true;
                while (columnsRs.next()) {
                    if (!firstColumn) {
                        jsonResponse.append(",");
                    }
                    firstColumn = false;

                    String columnName = columnsRs.getString("column_name");
                    String dataType = columnsRs.getString("data_type");

                    jsonResponse.append("{");
                    jsonResponse.append("\"columnName\":\"").append(columnName).append("\",");
                    jsonResponse.append("\"dataType\":\"").append(dataType).append("\"");
                    jsonResponse.append("}");
                }
                jsonResponse.append("]");  // Close columns array
                jsonResponse.append("}");  // Close table object

                // Close column ResultSet and statement
                columnsRs.close();
                columnStmt.close();
            }

            jsonResponse.append("]");  // Close tables array

            // Send JSON response
            response.getWriter().write(jsonResponse.toString());

            // Close tables ResultSet and statement
            tablesRs.close();
            tableStmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("{\"error\": \"Error retrieving metadata.\"}");
        }
    }
}

