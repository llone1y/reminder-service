package com.example.reminderservicemaven.repositories;

import com.example.reminderservicemaven.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findAllByUsername(String username);

    Optional <User> findByChatId(String chatId);

    Optional <User> findByEmail(String email);
}
