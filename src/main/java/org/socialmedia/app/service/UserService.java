package org.socialmedia.app.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.socialmedia.app.dao.UserDAO;
import org.socialmedia.app.model.user.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@Service
public class UserService {
    private final UserDAO userDAO;
    public Optional<User> getUserById(Long id) {
        User user = userDAO.findById(id).orElse(null);
        return Optional.ofNullable(user);
    }
    public Optional<List<User>> getAllUsers() {
        List<User> users = userDAO.findAll();
        return Optional.of(users);
    }
    
    public Optional<User> createUser(User user) {
        User savedUser = userDAO.save(user);
        return Optional.ofNullable(savedUser);
    }
}
