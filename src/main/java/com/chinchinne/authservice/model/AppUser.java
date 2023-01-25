package com.chinchinne.authservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.boot.jackson.JsonMixin;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Data
@Entity
@Table(name = "app_users")
@NoArgsConstructor
@JsonRootName("user")
@JsonIgnoreProperties( ignoreUnknown = true )
public class AppUser implements Serializable {
   private static final long serialVersionUID = -1L;
   @Id
   @GeneratedValue(generator = "UUID")
   @Type(type = "uuid-char")
   private UUID id;
   private String password;
   private String firstName;
   private String lastName;
   private String loginId;
}
