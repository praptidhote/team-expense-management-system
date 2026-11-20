-- =============================================================================
-- Team Expense Approval & Management System
-- Database Schema (MySQL 8.x)
-- =============================================================================

CREATE DATABASE IF NOT EXISTS `expense_management_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `expense_management_db`;

-- Drop existing tables in reverse order of foreign key dependencies
DROP TABLE IF EXISTS `audit_logs`;
DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `expense_status_history`;
DROP TABLE IF EXISTS `expense_approvals`;
DROP TABLE IF EXISTS `expenses`;
DROP TABLE IF EXISTS `expense_categories`;
DROP TABLE IF EXISTS `user_roles`;
DROP TABLE IF EXISTS `roles`;
DROP TABLE IF EXISTS `users`;

-- -----------------------------------------------------------------------------
-- 1. ROLES TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE `roles` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL,
    INDEX `idx_roles_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. USERS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `email` VARCHAR(150) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `first_name` VARCHAR(50) NOT NULL,
    `last_name` VARCHAR(50) NOT NULL,
    `department` VARCHAR(100) NULL,
    `job_title` VARCHAR(100) NULL,
    `manager_id` BIGINT NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_users_manager` FOREIGN KEY (`manager_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
    INDEX `idx_users_email` (`email`),
    INDEX `idx_users_manager_id` (`manager_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. USER_ROLES (M:N Mapping)
-- -----------------------------------------------------------------------------
CREATE TABLE `user_roles` (
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    CONSTRAINT `fk_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. EXPENSE_CATEGORIES
-- -----------------------------------------------------------------------------
CREATE TABLE `expense_categories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_category_name` (`name`),
    INDEX `idx_category_status` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5. EXPENSES
-- -----------------------------------------------------------------------------
CREATE TABLE `expenses` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `category_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `description` TEXT NOT NULL,
    `amount` DECIMAL(12, 2) NOT NULL,
    `currency` VARCHAR(10) NOT NULL DEFAULT 'USD',
    `expense_date` DATE NOT NULL,
    `payment_method` VARCHAR(50) NOT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    `receipt_url` VARCHAR(500) NULL,
    `receipt_original_name` VARCHAR(255) NULL,
    `receipt_content_type` VARCHAR(100) NULL,
    `receipt_size_bytes` BIGINT NULL,
    `notes` TEXT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_expenses_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_expenses_category` FOREIGN KEY (`category_id`) REFERENCES `expense_categories` (`id`) ON DELETE RESTRICT,
    INDEX `idx_expenses_user_id` (`user_id`),
    INDEX `idx_expenses_category_id` (`category_id`),
    INDEX `idx_expenses_status` (`status`),
    INDEX `idx_expenses_date` (`expense_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6. EXPENSE_APPROVALS
-- -----------------------------------------------------------------------------
CREATE TABLE `expense_approvals` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `expense_id` BIGINT NOT NULL,
    `reviewer_id` BIGINT NOT NULL,
    `decision` VARCHAR(30) NOT NULL,
    `comments` TEXT NULL,
    `reviewed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_approvals_expense` FOREIGN KEY (`expense_id`) REFERENCES `expenses` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_approvals_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
    INDEX `idx_approvals_expense_id` (`expense_id`),
    INDEX `idx_approvals_reviewer_id` (`reviewer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 7. EXPENSE_STATUS_HISTORY
-- -----------------------------------------------------------------------------
CREATE TABLE `expense_status_history` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `expense_id` BIGINT NOT NULL,
    `previous_status` VARCHAR(30) NULL,
    `new_status` VARCHAR(30) NOT NULL,
    `changed_by_user_id` BIGINT NOT NULL,
    `change_reason` TEXT NULL,
    `changed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_history_expense` FOREIGN KEY (`expense_id`) REFERENCES `expenses` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_history_user` FOREIGN KEY (`changed_by_user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
    INDEX `idx_history_expense_id` (`expense_id`),
    INDEX `idx_history_changed_at` (`changed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 8. NOTIFICATIONS
-- -----------------------------------------------------------------------------
CREATE TABLE `notifications` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `recipient_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `message` TEXT NOT NULL,
    `type` VARCHAR(50) NOT NULL,
    `reference_id` BIGINT NULL,
    `is_read` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_notifications_user` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    INDEX `idx_notifications_recipient` (`recipient_id`),
    INDEX `idx_notifications_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 9. AUDIT_LOGS
-- -----------------------------------------------------------------------------
CREATE TABLE `audit_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `actor_id` BIGINT NULL,
    `action` VARCHAR(100) NOT NULL,
    `entity_name` VARCHAR(50) NOT NULL,
    `entity_id` BIGINT NULL,
    `details` TEXT NULL,
    `ip_address` VARCHAR(45) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_audit_actor` FOREIGN KEY (`actor_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
    INDEX `idx_audit_actor_id` (`actor_id`),
    INDEX `idx_audit_entity` (`entity_name`, `entity_id`),
    INDEX `idx_audit_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
