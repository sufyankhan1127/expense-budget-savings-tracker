package com.ebstracker.expense_budget_savings_tracker.controller;

import com.ebstracker.expense_budget_savings_tracker.entity.User;
import com.ebstracker.expense_budget_savings_tracker.repository.UserRepository;
import com.ebstracker.expense_budget_savings_tracker.service.ExpenseService;
import com.ebstracker.expense_budget_savings_tracker.service.IncomeService;
import com.ebstracker.expense_budget_savings_tracker.service.SavingsService;
import com.ebstracker.expense_budget_savings_tracker.service.UserService;
import com.ebstracker.expense_budget_savings_tracker.util.PasswordEncoderUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final SavingsService savingsService;
    private final PasswordEncoderUtil passwordEncoderUtil;

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Long userId = loggedInUser.getId();

        BigDecimal totalIncome = incomeService.getTotalIncome(userId);
        BigDecimal totalExpenses = expenseService.getTotalExpenses(userId);
        BigDecimal totalSavings = savingsService.getTotalSavings(userId);
        BigDecimal savingsTarget = savingsService.getTotalSavingsTarget(userId);

        int incomeCount = incomeService.getAllIncomeByUser(userId).size();
        int expenseCount = expenseService.getAllExpensesByUser(userId).size();
        int savingsCount = savingsService.getAllSavingsByUser(userId).size();

        model.addAttribute("user", loggedInUser);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("totalSavings", totalSavings);
        model.addAttribute("savingsTarget", savingsTarget);
        model.addAttribute("incomeCount", incomeCount);
        model.addAttribute("expenseCount", expenseCount);
        model.addAttribute("savingsCount", savingsCount);
        model.addAttribute("userName", loggedInUser.getFullName());

        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        if (!loggedInUser.getEmail().equals(email) && userService.emailExists(email)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email already in use!");
            return "redirect:/profile";
        }

        User user = userService.getUserById(loggedInUser.getId());
        user.setFullName(fullName);
        user.setEmail(email);
        userRepository.save(user);

        session.setAttribute("loggedInUser", user);
        session.setAttribute("userName", fullName);

        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Verify current password
        if (!passwordEncoderUtil.matches(currentPassword, loggedInUser.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect!");
            return "redirect:/profile";
        }

        // Check new password match
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match!");
            return "redirect:/profile";
        }

        // Check password length
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters!");
            return "redirect:/profile";
        }

        // Get fresh user and update password
        User user = userService.getUserById(loggedInUser.getId());
        String encryptedPassword = passwordEncoderUtil.encode(newPassword);
        user.setPassword(encryptedPassword);
        userRepository.save(user);

        // Update session
        session.setAttribute("loggedInUser", user);

        redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
        return "redirect:/profile";
    }
}