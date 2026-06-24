package com.ebstracker.expense_budget_savings_tracker.controller;

import com.ebstracker.expense_budget_savings_tracker.entity.Savings;
import com.ebstracker.expense_budget_savings_tracker.entity.User;
import com.ebstracker.expense_budget_savings_tracker.service.SavingsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class SavingsController {

    private final SavingsService savingsService;

    @GetMapping("/savings")
    public String listSavings(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        List<Savings> savings = savingsService.getAllSavingsByUser(loggedInUser.getId());
        BigDecimal totalSavings = savingsService.getTotalSavings(loggedInUser.getId());
        BigDecimal totalTarget = savingsService.getTotalSavingsTarget(loggedInUser.getId());

        model.addAttribute("savings", savings);
        model.addAttribute("totalSavings", totalSavings);
        model.addAttribute("totalTarget", totalTarget);
        model.addAttribute("userName", loggedInUser.getFullName());

        return "savings";
    }

    @GetMapping("/savings/add")
    public String showAddSavingsForm(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("userName", loggedInUser.getFullName());
        return "add-savings";
    }

    @PostMapping("/savings/add")
    public String addSavings(
            @RequestParam("goalName") String goalName,
            @RequestParam("description") String description,
            @RequestParam("targetAmount") BigDecimal targetAmount,
            @RequestParam("savedAmount") BigDecimal savedAmount,
            @RequestParam("savingsDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate savingsDate,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Savings savings = new Savings();
        savings.setUser(loggedInUser);
        savings.setGoalName(goalName);
        savings.setDescription(description);
        savings.setTargetAmount(targetAmount);
        savings.setSavedAmount(savedAmount);
        savings.setSavingsDate(savingsDate);

        savingsService.addSavings(savings);

        redirectAttributes.addFlashAttribute("successMessage", "Savings goal added successfully!");
        return "redirect:/savings";
    }

    @GetMapping("/savings/edit/{id}")
    public String showEditSavingsForm(@PathVariable Long id, HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Savings savings = savingsService.getSavingsById(id);

        if (!savings.getUser().getId().equals(loggedInUser.getId())) {
            return "redirect:/savings";
        }

        model.addAttribute("savings", savings);
        model.addAttribute("userName", loggedInUser.getFullName());

        return "edit-savings";
    }

    @PostMapping("/savings/edit/{id}")
    public String updateSavings(
            @PathVariable Long id,
            @RequestParam("goalName") String goalName,
            @RequestParam("description") String description,
            @RequestParam("targetAmount") BigDecimal targetAmount,
            @RequestParam("savedAmount") BigDecimal savedAmount,
            @RequestParam("savingsDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate savingsDate,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Savings existingSavings = savingsService.getSavingsById(id);

        if (!existingSavings.getUser().getId().equals(loggedInUser.getId())) {
            return "redirect:/savings";
        }

        existingSavings.setGoalName(goalName);
        existingSavings.setDescription(description);
        existingSavings.setTargetAmount(targetAmount);
        existingSavings.setSavedAmount(savedAmount);
        existingSavings.setSavingsDate(savingsDate);

        savingsService.updateSavings(existingSavings);

        redirectAttributes.addFlashAttribute("successMessage", "Savings goal updated successfully!");
        return "redirect:/savings";
    }

    @GetMapping("/savings/delete/{id}")
    public String deleteSavings(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Savings savings = savingsService.getSavingsById(id);

        if (savings.getUser().getId().equals(loggedInUser.getId())) {
            savingsService.deleteSavings(id);
            redirectAttributes.addFlashAttribute("successMessage", "Savings goal deleted successfully!");
        }

        return "redirect:/savings";
    }
}