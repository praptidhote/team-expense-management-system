package com.expensemanagement.config;

import com.expensemanagement.entity.Expense;
import com.expensemanagement.entity.ExpenseCategory;
import com.expensemanagement.entity.ExpenseStatusHistory;
import com.expensemanagement.entity.Role;
import com.expensemanagement.entity.User;
import com.expensemanagement.enums.ExpenseStatus;
import com.expensemanagement.enums.PaymentMethod;
import com.expensemanagement.enums.RoleName;
import com.expensemanagement.repository.ExpenseCategoryRepository;
import com.expensemanagement.repository.ExpenseRepository;
import com.expensemanagement.repository.ExpenseStatusHistoryRepository;
import com.expensemanagement.repository.RoleRepository;
import com.expensemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseStatusHistoryRepository historyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing baseline system data (Roles, Categories, Seed Accounts)...");

        // 1. Initialize Roles
        Role employeeRole = roleRepository.findByName(RoleName.ROLE_EMPLOYEE)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(RoleName.ROLE_EMPLOYEE)
                        .description("Standard employee with submission privileges")
                        .build()));

        Role managerRole = roleRepository.findByName(RoleName.ROLE_MANAGER)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(RoleName.ROLE_MANAGER)
                        .description("Manager who can review direct reports expenses")
                        .build()));

        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(RoleName.ROLE_ADMIN)
                        .description("System administrator with all permissions")
                        .build()));

        // 2. Initialize Categories
        List<String[]> standardCategories = List.of(
                new String[]{"Travel", "Business flights, train tickets, and travel reimbursements"},
                new String[]{"Food & Meals", "Client entertainment, team dinners, and meal allowances"},
                new String[]{"Accommodation", "Hotel stays and lodging for official corporate trips"},
                new String[]{"Office Supplies", "Stationery, cables, monitors, and workspace equipment"},
                new String[]{"Internet & Phone", "Monthly work-from-home internet bills and phone calls"},
                new String[]{"Software & Tools", "Developer software licenses, cloud tools, SaaS subscriptions"},
                new String[]{"Transportation", "Taxis, rideshare, fuel, tolls, and public transit"},
                new String[]{"Other", "Miscellaneous business operational expenses"}
        );

        for (String[] cat : standardCategories) {
            if (!categoryRepository.existsByNameIgnoreCase(cat[0])) {
                categoryRepository.save(ExpenseCategory.builder()
                        .name(cat[0])
                        .description(cat[1])
                        .active(true)
                        .build());
            }
        }

        // 3. Initialize Demo Users
        User admin = userRepository.findByEmail("admin@company.com").orElseGet(() -> {
            User user = User.builder()
                    .email("admin@company.com")
                    .password(passwordEncoder.encode("Admin123!"))
                    .firstName("Alex")
                    .lastName("Morgan")
                    .department("Executive Management")
                    .jobTitle("Operations Director")
                    .roles(Set.of(adminRole))
                    .active(true)
                    .build();
            return userRepository.save(user);
        });

        User manager = userRepository.findByEmail("manager@company.com").orElseGet(() -> {
            User user = User.builder()
                    .email("manager@company.com")
                    .password(passwordEncoder.encode("Manager123!"))
                    .firstName("Jordan")
                    .lastName("Lee")
                    .department("Engineering")
                    .jobTitle("Engineering Manager")
                    .roles(Set.of(managerRole))
                    .active(true)
                    .build();
            return userRepository.save(user);
        });

        User employee = userRepository.findByEmail("employee@company.com").orElseGet(() -> {
            User user = User.builder()
                    .email("employee@company.com")
                    .password(passwordEncoder.encode("Employee123!"))
                    .firstName("Taylor")
                    .lastName("Reed")
                    .department("Engineering")
                    .jobTitle("Software Engineer")
                    .manager(manager)
                    .roles(Set.of(employeeRole))
                    .active(true)
                    .build();
            return userRepository.save(user);
        });

        // Ensure employee has manager assigned
        if (employee.getManager() == null) {
            employee.setManager(manager);
            userRepository.save(employee);
        }

        // 4. Seed sample expenses if none exist
        if (expenseRepository.count() == 0) {
            ExpenseCategory travel = categoryRepository.findByNameIgnoreCase("Travel").orElse(null);
            ExpenseCategory food = categoryRepository.findByNameIgnoreCase("Food & Meals").orElse(null);
            ExpenseCategory software = categoryRepository.findByNameIgnoreCase("Software & Tools").orElse(null);

            if (travel != null) {
                Expense e1 = Expense.builder()
                        .user(employee)
                        .category(travel)
                        .title("Client Onsite Visit Flight Ticket")
                        .description("Roundtrip flight ticket for quarterly client architecture review in Chicago")
                        .amount(new BigDecimal("345.50"))
                        .currency("USD")
                        .expenseDate(LocalDate.now().minusDays(5))
                        .paymentMethod(PaymentMethod.PERSONAL_CREDIT_CARD)
                        .status(ExpenseStatus.PENDING)
                        .notes("Receipt attached physically and submitted to accounting")
                        .build();
                Expense s1 = expenseRepository.save(e1);
                historyRepository.save(ExpenseStatusHistory.builder()
                        .expense(s1)
                        .newStatus(ExpenseStatus.PENDING)
                        .changedBy(employee)
                        .changeReason("Submitted initial expense")
                        .build());
            }

            if (food != null) {
                Expense e2 = Expense.builder()
                        .user(employee)
                        .category(food)
                        .title("Team Sprint Planning Lunch")
                        .description("Lunch meeting with client stakeholders following Q4 release planning")
                        .amount(new BigDecimal("82.00"))
                        .currency("USD")
                        .expenseDate(LocalDate.now().minusDays(12))
                        .paymentMethod(PaymentMethod.COMPANY_CARD)
                        .status(ExpenseStatus.APPROVED)
                        .build();
                Expense s2 = expenseRepository.save(e2);
                historyRepository.save(ExpenseStatusHistory.builder()
                        .expense(s2)
                        .previousStatus(ExpenseStatus.PENDING)
                        .newStatus(ExpenseStatus.APPROVED)
                        .changedBy(manager)
                        .changeReason("Approved: verified receipt and attendee list")
                        .build());
            }

            if (software != null) {
                Expense e3 = Expense.builder()
                        .user(employee)
                        .category(software)
                        .title("IntelliJ IDEA Ultimate Yearly Renewal")
                        .description("Annual IDE subscription for full-stack Spring and React development")
                        .amount(new BigDecimal("169.00"))
                        .currency("USD")
                        .expenseDate(LocalDate.now().minusDays(20))
                        .paymentMethod(PaymentMethod.PERSONAL_CREDIT_CARD)
                        .status(ExpenseStatus.APPROVED)
                        .build();
                Expense s3 = expenseRepository.save(e3);
                historyRepository.save(ExpenseStatusHistory.builder()
                        .expense(s3)
                        .previousStatus(ExpenseStatus.PENDING)
                        .newStatus(ExpenseStatus.APPROVED)
                        .changedBy(manager)
                        .changeReason("Approved software license")
                        .build());
            }

            log.info("Demo expenses seeded successfully.");
        }

        log.info("Baseline data initialization completed.");
    }
}
