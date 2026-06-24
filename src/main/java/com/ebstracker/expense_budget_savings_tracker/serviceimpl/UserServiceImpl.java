package com.ebstracker.expense_budget_savings_tracker.serviceimpl;

import com.ebstracker.expense_budget_savings_tracker.entity.User;
import com.ebstracker.expense_budget_savings_tracker.exception.AuthenticationException;
import com.ebstracker.expense_budget_savings_tracker.exception.ResourceNotFoundException;
import com.ebstracker.expense_budget_savings_tracker.repository.UserRepository;
import com.ebstracker.expense_budget_savings_tracker.service.UserService;
import com.ebstracker.expense_budget_savings_tracker.util.PasswordEncoderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoderUtil passwordEncoderUtil;

    @Override
    public User registerUser(User user) {
        if (emailExists(user.getEmail())) {
            throw new AuthenticationException("Email already registered: " + user.getEmail());
        }

        // Encrypt password before saving
        String encryptedPassword = passwordEncoderUtil.encode(user.getPassword());
        user.setPassword(encryptedPassword);

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!passwordEncoderUtil.matches(password, user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User updateUser(User user) {
        User existingUser = getUserById(user.getId());
        existingUser.setFullName(user.getFullName());
        existingUser.setEmail(user.getEmail());

        // Only update password if it's provided and not already encrypted
        if (user.getPassword() != null && !user.getPassword().isEmpty()
                && !user.getPassword().startsWith("$2a$")) {
            String encryptedPassword = passwordEncoderUtil.encode(user.getPassword());
            existingUser.setPassword(encryptedPassword);
        }

        return userRepository.save(existingUser);
    }
}