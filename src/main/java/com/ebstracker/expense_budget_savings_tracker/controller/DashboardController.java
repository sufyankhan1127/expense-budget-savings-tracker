package com.ebstracker.expense_budget_savings_tracker.controller;

import com.ebstracker.expense_budget_savings_tracker.entity.Expense;
import com.ebstracker.expense_budget_savings_tracker.entity.Income;
import com.ebstracker.expense_budget_savings_tracker.entity.User;
import com.ebstracker.expense_budget_savings_tracker.service.ExpenseService;
import com.ebstracker.expense_budget_savings_tracker.service.IncomeService;
import com.ebstracker.expense_budget_savings_tracker.service.SavingsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final SavingsService savingsService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        return dashboardWithFilter(session, model, "thisMonth", null, null);
    }

    @GetMapping("/dashboard/filter")
    public String dashboardWithFilter(
            HttpSession session,
            Model model,
            @RequestParam(name = "filterType", defaultValue = "thisMonth") String filterType,
            @RequestParam(name = "fromDate", required = false) String fromDateStr,
            @RequestParam(name = "toDate", required = false) String toDateStr) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Long userId = loggedInUser.getId();

        // Parse dates manually to avoid format issues
        LocalDate fromDate = parseDate(fromDateStr);
        LocalDate toDate = parseDate(toDateStr);

        // Calculate date range based on filter
        DateRange range = calculateDateRange(filterType, fromDate, toDate);
        LocalDate startDate = range.getStart();
        LocalDate endDate = range.getEnd();

        // Fetch ALL expenses and income for the user
        List<Expense> allExpenses = expenseService.getAllExpensesByUser(userId);
        List<Income> allIncome = incomeService.getAllIncomeByUser(userId);

        // Filter by date range
        List<Expense> expensesInRange = new ArrayList<>();
        for (Expense e : allExpenses) {
            if (!e.getExpenseDate().isBefore(startDate) && !e.getExpenseDate().isAfter(endDate)) {
                expensesInRange.add(e);
            }
        }

        List<Income> incomeInRange = new ArrayList<>();
        for (Income i : allIncome) {
            if (!i.getIncomeDate().isBefore(startDate) && !i.getIncomeDate().isAfter(endDate)) {
                incomeInRange.add(i);
            }
        }

        // Calculate totals for the filtered range
        BigDecimal totalIncome = BigDecimal.ZERO;
        for (Income i : incomeInRange) {
            totalIncome = totalIncome.add(i.getAmount());
        }

        BigDecimal totalExpenses = BigDecimal.ZERO;
        for (Expense e : expensesInRange) {
            totalExpenses = totalExpenses.add(e.getAmount());
        }

        // All-time savings
        BigDecimal totalSavings = savingsService.getTotalSavings(userId);
        BigDecimal savingsTarget = savingsService.getTotalSavingsTarget(userId);

        BigDecimal savingsPercentage = BigDecimal.ZERO;
        if (savingsTarget.compareTo(BigDecimal.ZERO) > 0) {
            savingsPercentage = totalSavings
                    .multiply(BigDecimal.valueOf(100))
                    .divide(savingsTarget, 0, RoundingMode.HALF_UP);
        }

        BigDecimal balance = totalIncome.subtract(totalExpenses);

        // Recent transactions (top 5 from filtered range)
        List<Expense> recentExpenses = expensesInRange.size() > 5 ? expensesInRange.subList(0, 5) : expensesInRange;
        List<Income> recentIncome = incomeInRange.size() > 5 ? incomeInRange.subList(0, 5) : incomeInRange;

        // Chart data - Monthly breakdown for last 6 months
        List<String> monthLabels = new ArrayList<>();
        List<BigDecimal> monthlyIncomeList = new ArrayList<>();
        List<BigDecimal> monthlyExpensesList = new ArrayList<>();

        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(5).withDayOfMonth(1);
        for (int i = 0; i < 6; i++) {
            LocalDate monthStart = sixMonthsAgo.plusMonths(i);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
            String monthLabel = monthStart.getMonth().toString().substring(0, 3);

            BigDecimal monthInc = BigDecimal.ZERO;
            BigDecimal monthExp = BigDecimal.ZERO;

            for (Income inc : allIncome) {
                if (!inc.getIncomeDate().isBefore(monthStart) && !inc.getIncomeDate().isAfter(monthEnd)) {
                    monthInc = monthInc.add(inc.getAmount());
                }
            }

            for (Expense exp : allExpenses) {
                if (!exp.getExpenseDate().isBefore(monthStart) && !exp.getExpenseDate().isAfter(monthEnd)) {
                    monthExp = monthExp.add(exp.getAmount());
                }
            }

            monthLabels.add(monthLabel);
            monthlyIncomeList.add(monthInc);
            monthlyExpensesList.add(monthExp);
        }

        // Category data for pie chart from filtered range
        java.util.Map<String, BigDecimal> categoryMap = new java.util.HashMap<>();
        for (Expense e : expensesInRange) {
            categoryMap.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
        }

        List<String> catLabels = new ArrayList<>();
        List<BigDecimal> catValues = new ArrayList<>();
        for (java.util.Map.Entry<String, BigDecimal> entry : categoryMap.entrySet()) {
            catLabels.add(entry.getKey());
            catValues.add(entry.getValue());
        }

        // If no category data, add placeholder
        if (catLabels.isEmpty()) {
            catLabels.add("No Data");
            catValues.add(BigDecimal.ONE);
        }

        // Add to model
        model.addAttribute("userName", loggedInUser.getFullName());
        model.addAttribute("currentDate", LocalDate.now().toString());
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("totalSavings", totalSavings);
        model.addAttribute("savingsTarget", savingsTarget);
        model.addAttribute("savingsPercentage", savingsPercentage);
        model.addAttribute("balance", balance);
        model.addAttribute("recentExpenses", recentExpenses);
        model.addAttribute("recentIncome", recentIncome);
        model.addAttribute("catLabels", catLabels);
        model.addAttribute("catValues", catValues);
        model.addAttribute("monthLabels", monthLabels);
        model.addAttribute("monthlyIncome", monthlyIncomeList);
        model.addAttribute("monthlyExpenses", monthlyExpensesList);
        model.addAttribute("filterType", filterType);
        model.addAttribute("fromDate", fromDateStr);
        model.addAttribute("toDate", toDateStr);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "dashboard";
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    private DateRange calculateDateRange(String filterType, LocalDate fromDate, LocalDate toDate) {
        LocalDate now = LocalDate.now();
        LocalDate start;
        LocalDate end;

        switch (filterType) {
            case "today":
                start = now;
                end = now;
                break;
            case "thisMonth":
                start = now.withDayOfMonth(1);
                end = now.withDayOfMonth(now.lengthOfMonth());
                break;
            case "thisYear":
                start = now.withDayOfYear(1);
                end = now.withDayOfYear(now.lengthOfYear());
                break;
            case "custom":
                start = fromDate != null ? fromDate : now.withDayOfMonth(1);
                end = toDate != null ? toDate : now;
                break;
            case "allTime":
            default:
                start = LocalDate.of(2000, 1, 1);
                end = now;
                break;
        }

        return new DateRange(start, end);
    }

    private static class DateRange {
        private final LocalDate start;
        private final LocalDate end;

        public DateRange(LocalDate start, LocalDate end) {
            this.start = start;
            this.end = end;
        }

        public LocalDate getStart() { return start; }
        public LocalDate getEnd() { return end; }
    }
}