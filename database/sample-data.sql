-- =============================================================================
-- Team Expense Approval & Management System
-- Initial Seed Data (MySQL 8.x)
-- =============================================================================

USE `expense_management_db`;

-- 1. Insert Standard Roles
INSERT INTO `roles` (`id`, `name`, `description`) VALUES
(1, 'ROLE_EMPLOYEE', 'Standard employee with submission and self-tracking privileges'),
(2, 'ROLE_MANAGER', 'Manager who can review, approve, and reject direct reports expenses'),
(3, 'ROLE_ADMIN', 'System administrator with user management, category setup, and all-expense reporting')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

-- 2. Insert Standard Categories
INSERT INTO `expense_categories` (`name`, `description`, `is_active`) VALUES
('Travel', 'Business trips, flights, train tickets, and mileage reimbursement', TRUE),
('Food & Meals', 'Client entertainment, team dinners, and daily meal allowances during travel', TRUE),
('Accommodation', 'Hotel bookings and temporary lodging for business trips', TRUE),
('Office Supplies', 'Stationery, desk equipment, cables, and minor office hardware', TRUE),
('Internet & Communication', 'Monthly work-from-home internet bills, phone calls, and data allowances', TRUE),
('Software & Subscriptions', 'Developer licenses, cloud services, and productivity tools', TRUE),
('Transportation', 'Taxis, rideshare, fuel, tolls, and public transit', TRUE),
('Other', 'Miscellaneous operational expenses with approval', TRUE)
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

-- 3. Sample Users
-- Note: Passwords below are hashed using BCrypt (strength 10) for "Password123!"
-- Hash: $2a$10$w8.B90r5bMvAOm2Jq931QeY9uR5rR2X9u8pD7dYxG/23b0u69uQW. (Example standard BCrypt representation)
-- In Phase 2, backend DataInitializer will populate with verified BCrypt encoded passwords dynamically if not present.
