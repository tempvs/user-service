package club.tempvs.user.filter;

import club.tempvs.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuthUserFilter extends GenericFilterBean {

    private final UserService userService;

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(this::isGoogleAuthentication)
                .map(OAuth2AuthenticationToken.class::cast)
                .map(OAuth2AuthenticationToken::getPrincipal)
                .ifPresent(this::createExternalUser);
        chain.doFilter(request, response);
    }

    private boolean isGoogleAuthentication(Authentication authentication) {
        return authentication instanceof OAuth2AuthenticationToken oauthToken
                && authentication.isAuthenticated()
                && "google".equals(oauthToken.getAuthorizedClientRegistrationId());
    }

    private void createExternalUser(OAuth2User principal) {
        String externalId = principal.getName();
        if (!StringUtils.hasText(externalId)) {
            return;
        }

        String email = principal.getAttribute("email");
        userService.createExternalUser(externalId, email);
    }
}
