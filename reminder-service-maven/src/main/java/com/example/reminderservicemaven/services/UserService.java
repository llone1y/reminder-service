package com.example.reminderservicemaven.services;

import com.example.reminderservicemaven.models.User;
import com.example.reminderservicemaven.repositories.UserRepository;
import com.example.reminderservicemaven.util.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void saveUser(User user) {
        String username = user.getUsername();
        Optional<User> existingUser = userRepository.findAllByUsername(username);

        existingUser.orElseGet(() -> userRepository.save(user));
    }

    public User findUserByUsernameOrThrowException(String username) {
        return userRepository.findAllByUsername(username).orElseThrow(UserNotFoundException::new);
    }

    public User findUserByChatIdOrThrowException(String chatId) {
        return userRepository.findByChatId(chatId).orElseThrow(UserNotFoundException::new);
    }

    public User findUserByEmailOrThrowException(String email) {
        return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }

    public void addChatId(String chatId, User user) {
        user.setChatId(chatId);
        userRepository.save(user);
    }
}
