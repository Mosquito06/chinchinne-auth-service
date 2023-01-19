package com.chinchinne.authservice.service;

import com.chinchinne.authservice.model.AppUser;
import com.chinchinne.authservice.model.AppUserPrincipal;
import com.chinchinne.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.UUID;

public class CustomUserDetailsService implements UserDetailsService
{
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        AppUser user = userRepository.findByLoginId(username).orElseThrow( () ->
        {
            throw new UsernameNotFoundException("not found loginId : " + username);
        });

        System.out.println(user.toString());

        return new AppUserPrincipal(user);
    }
}
