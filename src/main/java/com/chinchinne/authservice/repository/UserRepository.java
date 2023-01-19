package com.chinchinne.authservice.repository;

import com.chinchinne.authservice.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<AppUser, UUID>
{
    Optional<AppUser> findByLoginId(String id);
}
