package com.chinchinne.authservice.filter;

import com.chinchinne.authservice.model.CustomException;
import com.chinchinne.authservice.model.ErrorCode;
import com.chinchinne.authservice.model.ErrorResponse;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ExceptionHandlerFilter extends OncePerRequestFilter
{
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
    {
        try
        {
            filterChain.doFilter(request, response);
        }
        catch (CustomException e)
        {
            ErrorResponse.toResponseEntity(response, e.getErrorCode());
        }
    }
}
