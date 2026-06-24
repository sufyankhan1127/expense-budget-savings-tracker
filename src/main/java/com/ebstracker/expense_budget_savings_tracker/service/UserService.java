package com.ebstracker.expense_budget_savings_tracker.service;

import com.ebstracker.expense_budget_savings_tracker.entity.User;

public interface UserService {

    User registerUser(User user);

    User loginUser(String email, String password);

    User getUserById(Long id);

    User getUserByEmail(String email);

    boolean emailExists(String email);

    User updateUser(User user);
}