package club.tempvs.user.interceptor;

import club.tempvs.user.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String CHAR_ENCODING = "UTF-8";

    @Value("${authorization.token}")
    private String token;

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeaderValue = request.getHeader(AUTHORIZATION_HEADER);
        byte[] tokenBytes = token.getBytes(CHAR_ENCODING);
        String tokenHash = DigestUtils.md5DigestAsHex(tokenBytes);
        String requestUri = request.getRequestURI();
        boolean isSwaggerPath = isSwaggerPath(requestUri);

        if ((authHeaderValue == null || !authHeaderValue.equals(tokenHash)) && !isSwaggerPath) {
            log.warn("Security token validation failed");
            throw new UnauthorizedException("Authentication failed. Wrong token is received.");
        }

        return true;
    }

    private boolean isSwaggerPath(String path) {
        return path.contains("/v3/api-docs") || path.contains("swagger-ui");
    }
}
