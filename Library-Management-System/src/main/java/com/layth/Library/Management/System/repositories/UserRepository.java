package com.layth.Library.Management.System.repositories;

import com.layth.Library.Management.System.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findByUserNameAndPassword(String username, String password);
}
