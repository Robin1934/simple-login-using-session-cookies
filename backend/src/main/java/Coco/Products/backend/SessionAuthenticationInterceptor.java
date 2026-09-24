package Coco.Products.backend;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class SessionAuthenticationInterceptor
        implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        // A missing or timed-out session means the protected API is unauthorized.
        HttpSession session = request.getSession(false);

        if (session != null &&
                session.getAttribute("customerId") != null) {
            return true;
        }

        if (session != null) {
            session.invalidate();
        }

        // Clear an expired or invalid browser cookie as well as rejecting the request.
        ResponseCookie clearedCookie = ResponseCookie.from(
                        "JSESSIONID",
                        "")
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader("Set-Cookie", clearedCookie.toString());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Please login first\"}");

        return false;
    }
}