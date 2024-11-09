import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;


@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String url = httpRequest.getRequestURI();
        System.out.println("LoginFilter: " + url);

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(url)) {
            // Keep default action: pass along the filter chain
            System.out.println(url + "is allow to access without login");
            chain.doFilter(request, response);
            return;
        }

        // Redirect to login page if the "user"/"employee" attribute doesn't exist in session
        System.out.println("contextpath" + httpRequest.getContextPath());
        if (httpRequest.getSession().getAttribute("employee") == null && (url.contains("/_dashboard"))) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/_dashboard/login.html");  // /_dashboard/login.html
        } else if (httpRequest.getSession().getAttribute("user") == null && !(url.contains("/_dashboard"))) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");  // ./login.html
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup rules to allow accessing some resources without logging in
         Allow login related requests(html, js, servlet, CSS, etc..)
         */
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("/login.html");
        allowedURIs.add("/login.js");
        allowedURIs.add("api/login");  // user login
        allowedURIs.add("api/_dashboard_login");  // employee login
        allowedURIs.add("main.css");  // allow user main page css
        allowedURIs.add("style.css");  // allow employee main page css
        allowedURIs.add("simple-style.css");  // allow employee add action css
    }

    public void destroy() {
        // ignored.
    }

}
