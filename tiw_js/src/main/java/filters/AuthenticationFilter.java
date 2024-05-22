package filters;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebFilter("/*")
public class AuthenticationFilter implements Filter {
    private static final List<String> ALLOWED_PATHS = Arrays.asList(
            "/login", "/register", "/logout",
            "/login.html", "/register.html", "/login.js", "/register.js"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Filter initialization code, if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        boolean isAllowedPath = ALLOWED_PATHS.contains(path)
                || path.startsWith("/api/folders/")
                || path.startsWith("/api/documents/")
                || path.startsWith("/css/");

        if (isAllowedPath) {
            chain.doFilter(request, response);
        } else {
            HttpSession session = httpRequest.getSession(false);
            if (session != null && session.getAttribute("userId") != null) {
                chain.doFilter(request, response);
            } else {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");
            }
        }
    }

    @Override
    public void destroy() {
        // Filter cleanup code, if needed
    }
}
