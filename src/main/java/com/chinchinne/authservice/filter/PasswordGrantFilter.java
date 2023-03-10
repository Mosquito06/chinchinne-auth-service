package com.chinchinne.authservice.filter;
import com.chinchinne.authservice.model.CustomException;
import com.chinchinne.authservice.model.GrantType;
import com.chinchinne.authservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.web.util.matcher.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class PasswordGrantFilter extends OncePerRequestFilter
{
   private static final String DEFAULT_AUTH_ENDPOINT_URI = "/oauth/token";

   private final RequestMatcher authEndpointMatcher;
   private final AuthService authService;

   @Autowired
   public PasswordGrantFilter(AuthService authService)
   {
      this.authService = authService;
      this.authEndpointMatcher = createDefaultRequestMatcher();
   }

   @SuppressWarnings("NullableProblems")
   @Override
   protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, CustomException
   {
      if (!this.authEndpointMatcher.matches(request))
      {
         filterChain.doFilter(request, response);
         return;
      }

      String GRANT_TYPE = request.getParameter("grant_type").toUpperCase();
      byte[] entity = null;

      if( GRANT_TYPE.equals(GrantType.REFRESH_TOKEN.name()) )
      {
         entity = authService.getRefreshTokenForRequest(request).getBytes(StandardCharsets.UTF_8);
      }
      else
      {
         entity = authService.getAccessTokenForRequest(request).getBytes(StandardCharsets.UTF_8);
      }

      response.getOutputStream().write(entity);
      response.setContentType("application/json");
   }

   private static RequestMatcher createDefaultRequestMatcher()
   {
      RequestMatcher authorizationRequestGetMatcher = new AntPathRequestMatcher(DEFAULT_AUTH_ENDPOINT_URI, HttpMethod.GET.name());
      RequestMatcher authorizationRequestPostMatcher = new AntPathRequestMatcher(DEFAULT_AUTH_ENDPOINT_URI, HttpMethod.POST.name());

      RequestMatcher openidScopeMatcher = request ->
      {
         String scope = request.getParameter(OAuth2ParameterNames.SCOPE);
         return StringUtils.hasText(scope) && scope.contains(OidcScopes.OPENID);
      };

      RequestMatcher responseTypeParameterMatcher = request -> request.getParameter(OAuth2ParameterNames.RESPONSE_TYPE) != null;

      RequestMatcher authorizationRequestMatcher = new OrRequestMatcher
      (
          authorizationRequestGetMatcher
         ,new AndRequestMatcher(authorizationRequestPostMatcher, responseTypeParameterMatcher, openidScopeMatcher)
      );

      RequestMatcher authorizationConsentMatcher = new AndRequestMatcher
      (
          authorizationRequestPostMatcher
         ,new NegatedRequestMatcher(responseTypeParameterMatcher)
      );

      return new OrRequestMatcher(authorizationRequestMatcher, authorizationConsentMatcher);
   }
}
