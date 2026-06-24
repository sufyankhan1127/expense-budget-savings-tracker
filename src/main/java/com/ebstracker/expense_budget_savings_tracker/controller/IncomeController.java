package com.ebstracker.expense_budget_savings_tracker.controller;

import com.ebstracker.expense_budget_savings_tracker.entity.Income;
import com.ebstracker.expense_budget_savings_tracker.entity.User;
import com.ebstracker.expense_budget_savings_tracker.service.IncomeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    @GetMapping("/income")
    public String listIncome(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        List<Income> incomes = incomeService.getAllIncomeByUser(loggedInUser.getId());
        BigDecimal totalIncome = incomeService.getTotalIncomeForCurrentMonth(loggedInUser.getId());

        model.addAttribute("incomes", incomes);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("userName", loggedInUser.getFullName());

        return "income";
    }

    @GetMapping("/income/add")
    public String showAddIncomeForm(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("categories", getIncomeSources());
        model.addAttribute("userName", loggedInUser.getFullName());

        return "add-income";
    }

    @PostMapping("/income/add")
    public String addIncome(
            @RequestParam("incomeDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate incomeDate,
            @RequestParam("source") String source,
            @RequestParam("description") String description,
            @RequestParam("amount") BigDecimal amount,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Income income = new Income();
        income.setUser(loggedInUser);
        income.setIncomeDate(incomeDate);
        income.setSource(source);
        income.setDescription(description);
        income.setAmount(amount);

        incomeService.addIncome(income);

        redirectAttributes.addFlashAttribute("successMessage", "Income added successfully!");
        return "redirect:/income";
    }

    @GetMapping("/income/edit/{id}")
    public String showEditIncomeForm(@PathVariable Long id, HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Income income = incomeService.getIncomeById(id);

        if (!income.getUser().getId().equals(loggedInUser.getId())) {
            return "redirect:/income";
        }

        model.addAttribute("income", income);
        model.addAttribute("categories", getIncomeSources());
        model.addAttribute("userName", loggedInUser.getFullName());

        return "edit-income";
    }

    @PostMapping("/income/edit/{id}")
    public String updateIncome(
            @PathVariable Long id,
            @RequestParam("incomeDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate incomeDate,
            @RequestParam("source") String source,
            @RequestParam("description") String description,
            @RequestParam("amount") BigDecimal amount,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Income existingIncome = incomeService.getIncomeById(id);

        if (!existingIncome.getUser().getId().equals(loggedInUser.getId())) {
            return "redirect:/income";
        }

        existingIncome.setIncomeDate(incomeDate);
        existingIncome.setSource(source);
        existingIncome.setDescription(description);
        existingIncome.setAmount(amount);

        incomeService.updateIncome(existingIncome);

        redirectAttributes.addFlashAttribute("successMessage", "Income updated successfully!");
        return "redirect:/income";
    }

    @GetMapping("/income/delete/{id}")
    public String deleteIncome(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Income income = incomeService.getIncomeById(id);

        if (income.getUser().getId().equals(loggedInUser.getId())) {
            incomeService.deleteIncome(id);
            redirectAttributes.addFlashAttribute("successMessage", "Income deleted successfully!");
        }

        return "redirect:/income";
    }

    private List<String> getIncomeSources() {
        return Arrays.asList(
                "Salary", "Freelance", "Business", "Investments",
                "Rental", "Bonus", "Dividend", "Other"
        );
    }
}