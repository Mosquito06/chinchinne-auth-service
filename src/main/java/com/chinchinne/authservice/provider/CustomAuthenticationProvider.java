package com.chinchinne.authservice.provider;

import com.chinchinne.authservice.model.AppUser;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.UserDetailsManager;

public class CustomAuthenticationProvider implements AuthenticationProvider
{
    UserDetailsService userDetailsService;

    public CustomAuthenticationProvider(UserDetailsService userDetailsService)
    {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        String loginId = (String) authentication.getPrincipal();
        String loginPass = (String) authentication.getCredentials();

        UserDetails user = userDetailsService.loadUserByUsername(loginId);

        return new UsernamePasswordAuthenticationToken(loginId, loginPass, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
