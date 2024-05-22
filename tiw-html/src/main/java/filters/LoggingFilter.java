package filters;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter("/*") // Apply this filter to all URLs
public class LoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Filter initialization code, if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        System.out.println("Request URL: " + httpRequest.getRequestURL());
        System.out.println("Request Method: " + httpRequest.getMethod());
        System.out.println("Remote Address: " + httpRequest.getRemoteAddr());

        // Continue with the next filter or target resource
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Filter cleanup code, if needed
    }
}
