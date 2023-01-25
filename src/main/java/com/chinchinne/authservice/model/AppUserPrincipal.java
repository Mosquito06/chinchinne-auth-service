package com.chinchinne.authservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.jackson.JsonMixin;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

@Data
@NoArgsConstructor
@JsonRootName("principal")
@JsonIgnoreProperties( ignoreUnknown = true )
public class AppUserPrincipal implements UserDetails
{
   private AppUser user;

   public AppUserPrincipal(AppUser appUser) {
      this.user = appUser;
   }

   @JsonProperty("user")
   public AppUser getUser() {
      return user;
   }

   @Override
   @JsonIgnore
   @JsonProperty("authorities")
   public Collection<? extends GrantedAuthority> getAuthorities() {
      return Collections.singletonList(new SimpleGrantedAuthority("USER"));
   }

   @Override
   @JsonProperty("password")
   public String getPassword() {
      return user.getPassword();
   }

   @Override
   @JsonProperty("username")
   public String getUsername() {
      return user.getLoginId();
   }

   @Override
   @JsonProperty("accountNonExpired")
   public boolean isAccountNonExpired() {
      return true;
   }

   @Override
   @JsonProperty("accountNonLocked")
   public boolean isAccountNonLocked() {
      return true;
   }

   @Override
   @JsonProperty("credentialsNonExpired")
   public boolean isCredentialsNonExpired() {
      return true;
   }

   @Override
   @JsonProperty("enabled")
   public boolean isEnabled() {
      return true;
   }
}
