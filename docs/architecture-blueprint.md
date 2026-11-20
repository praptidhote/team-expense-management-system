# Team Expense Approval & Management System
## Architecture & Technical Specification Blueprint (Phase 1)

This blueprint documents the complete system architecture, database schema, entity relationships, REST API contract, and frontend/backend project structures for the **Team Expense Approval & Management System**.

Please refer to the detailed artifact in your IDE:
- [`phase-1-architecture-blueprint.md`](file:///C:/Users/Prapti%20Dhote/.gemini/antigravity/brain/a676ef74-4ed9-4d13-aeef-2f6e8ba58a08/phase-1-architecture-blueprint.md)

### Key Architectural Highlights:
1. **Multi-Tier Separation**: Single Page Application (React 18 + Tailwind CSS) communicating strictly over REST with Spring Boot 3.x backend.
2. **Database Integrity**: Normalized MySQL 8 schema with strict `DECIMAL(12, 2)` monetary columns mapped to Java `BigDecimal` (zero floating-point inaccuracy).
3. **Enterprise Security**: Spring Security 6 stateless JWT authentication, BCrypt password hashing, role-based authorization (EMPLOYEE, MANAGER, ADMIN), and IDOR prevention checks.
4. **Clean Code & SOLID Principles**: Controller -> Service -> Repository layered separation, DTO abstractions preventing entity leakage, dynamic JPA Specifications for filtering.
5. **Auditing & Workflow**: Immutable state transitions, mandatory rejection reasons, and complete audit trail.
