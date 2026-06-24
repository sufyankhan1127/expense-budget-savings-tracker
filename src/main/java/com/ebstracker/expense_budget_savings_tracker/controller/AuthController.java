package com.ebstracker.expense_budget_savings_tracker.controller;

import com.ebstracker.expense_budget_savings_tracker.dto.LoginDTO;
import com.ebstracker.expense_budget_savings_tracker.dto.UserDTO;
import com.ebstracker.expense_budget_savings_tracker.entity.User;
import com.ebstracker.expense_budget_savings_tracker.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // ========== REGISTER ==========
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("userDTO", new UserDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("userDTO") UserDTO userDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Check validation errors
        if (bindingResult.hasErrors()) {
            return "register";
        }

        // Check password match
        if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            model.addAttribute("passwordError", "Passwords do not match");
            return "register";
        }

        // Check email exists
        if (userService.emailExists(userDTO.getEmail())) {
            model.addAttribute("emailError", "Email already registered");
            return "register";
        }

        // Create user
        User user = User.builder()
                .fullName(userDTO.getFullName())
                .email(userDTO.getEmail())
                .password(userDTO.getPassword())
                .build();

        userService.registerUser(user);

        redirectAttributes.addFlashAttribute("successMessage",
                "Registration successful! Please login.");
        return "redirect:/login";
    }

    // ========== LOGIN ==========
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("loginDTO", new LoginDTO());
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(
            @Valid @ModelAttribute("loginDTO") LoginDTO loginDTO,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "login";
        }

        try {
            User user = userService.loginUser(loginDTO.getEmail(), loginDTO.getPassword());

            // Store user in session
            session.setAttribute("loggedInUser", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getFullName());

            return "redirect:/dashboard";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Invalid email or password");
            return "login";
        }
    }

    // ========== LOGOUT ==========
    @GetMapping("/logout")
    public String logoutUser(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out successfully");
        return "redirect:/";
    }
}