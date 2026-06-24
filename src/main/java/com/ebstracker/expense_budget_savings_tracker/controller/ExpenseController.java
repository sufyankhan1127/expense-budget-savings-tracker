package com.ebstracker.expense_budget_savings_tracker.controller;

import com.ebstracker.expense_budget_savings_tracker.entity.Expense;
import com.ebstracker.expense_budget_savings_tracker.entity.User;
import com.ebstracker.expense_budget_savings_tracker.service.ExpenseService;
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
public class ExpenseController {

    private final ExpenseService expenseService;

    // List all expenses
    @GetMapping("/expenses")
    public String listExpenses(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        List<Expense> expenses = expenseService.getAllExpensesByUser(loggedInUser.getId());
        BigDecimal totalExpenses = expenseService.getTotalExpensesForCurrentMonth(loggedInUser.getId());

        model.addAttribute("expenses", expenses);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("userName", loggedInUser.getFullName());

        return "expenses";
    }

    // Show add expense form
    @GetMapping("/expenses/add")
    public String showAddExpenseForm(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Expense expense = new Expense();
        expense.setExpenseDate(LocalDate.now());
        expense.setAmount(BigDecimal.ZERO);

        model.addAttribute("expense", expense);
        model.addAttribute("categories", getExpenseCategories());
        model.addAttribute("userName", loggedInUser.getFullName());

        return "add-expense";
    }

    // Save new expense
    @PostMapping("/expenses/add")
    public String addExpense(
            @RequestParam("expenseDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expenseDate,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam("amount") BigDecimal amount,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Expense expense = new Expense();
        expense.setUser(loggedInUser);
        expense.setExpenseDate(expenseDate);
        expense.setCategory(category);
        expense.setDescription(description);
        expense.setAmount(amount);

        expenseService.addExpense(expense);

        redirectAttributes.addFlashAttribute("successMessage", "Expense added successfully!");
        return "redirect:/expenses";
    }

    // Show edit expense form
    @GetMapping("/expenses/edit/{id}")
    public String showEditExpenseForm(@PathVariable Long id, HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Expense expense = expenseService.getExpenseById(id);

        // Security check
        if (!expense.getUser().getId().equals(loggedInUser.getId())) {
            return "redirect:/expenses";
        }

        model.addAttribute("expense", expense);
        model.addAttribute("categories", getExpenseCategories());
        model.addAttribute("userName", loggedInUser.getFullName());

        return "edit-expense";
    }

    // Update expense
    @PostMapping("/expenses/edit/{id}")
    public String updateExpense(
            @PathVariable Long id,
            @RequestParam("expenseDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expenseDate,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam("amount") BigDecimal amount,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Expense existingExpense = expenseService.getExpenseById(id);

        // Security check
        if (!existingExpense.getUser().getId().equals(loggedInUser.getId())) {
            return "redirect:/expenses";
        }

        existingExpense.setExpenseDate(expenseDate);
        existingExpense.setCategory(category);
        existingExpense.setDescription(description);
        existingExpense.setAmount(amount);

        expenseService.updateExpense(existingExpense);

        redirectAttributes.addFlashAttribute("successMessage", "Expense updated successfully!");
        return "redirect:/expenses";
    }

    // Delete expense
    @GetMapping("/expenses/delete/{id}")
    public String deleteExpense(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Expense expense = expenseService.getExpenseById(id);

        // Security check
        if (expense.getUser().getId().equals(loggedInUser.getId())) {
            expenseService.deleteExpense(id);
            redirectAttributes.addFlashAttribute("successMessage", "Expense deleted successfully!");
        }

        return "redirect:/expenses";
    }

    private List<String> getExpenseCategories() {
        return Arrays.asList(
                "Food", "Travel", "Shopping", "Bills",
                "Entertainment", "Healthcare", "Education",
                "Rent", "Groceries", "Other"
        );
    }
}