package com.codingshuttle.projects.airBnbApp.Repository;

import com.codingshuttle.projects.airBnbApp.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
   Optional<User> findByEmail(String email);
}
