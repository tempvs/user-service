package club.tempvs.user.configuration;

import club.tempvs.user.domain.User;
import club.tempvs.user.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private static final String REFRESH_COOKIES_HEADER = "Tempvs-Refresh-Cookies";
    private static final String CONTINUE_PARAM = "continue";

    private final UserService userService;
    private final ConversionService conversionService;

    @Value("${app.oauth-success-url:${app.ui-url:http://localhost:3000}}")
    private String defaultSuccessUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken oauthToken
                && oauthToken.getPrincipal() instanceof OAuth2User principal
                && "google".equals(oauthToken.getAuthorizedClientRegistrationId())) {
            String externalId = principal.getName();
            String email = principal.getAttribute("email");
            User user = userService.createExternalUser(externalId, email);
            String userInfo = conversionService.convert(user, String.class);
            if (StringUtils.hasText(userInfo)) {
                response.addHeader(REFRESH_COOKIES_HEADER, userInfo);
            }
        }

        response.sendRedirect(resolveRedirectUrl(request));
    }

    private String resolveRedirectUrl(HttpServletRequest request) {
        String continueUrl = request.getParameter(CONTINUE_PARAM);
        return StringUtils.hasText(continueUrl) ? continueUrl : defaultSuccessUrl;
    }
}
