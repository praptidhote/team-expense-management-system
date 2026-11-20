package com.expensemanagement.service.impl;

import com.expensemanagement.dto.request.CategoryRequest;
import com.expensemanagement.dto.response.CategoryResponse;
import com.expensemanagement.entity.ExpenseCategory;
import com.expensemanagement.enums.AuditAction;
import com.expensemanagement.exception.BadRequestException;
import com.expensemanagement.exception.ResourceNotFoundException;
import com.expensemanagement.repository.ExpenseCategoryRepository;
import com.expensemanagement.service.AuditLogService;
import com.expensemanagement.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final ExpenseCategoryRepository categoryRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        ExpenseCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseCategory", "id", id));
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
        }

        ExpenseCategory category = ExpenseCategory.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        ExpenseCategory saved = categoryRepository.save(category);

        auditLogService.log(
                AuditAction.CATEGORY_CREATE,
                "ExpenseCategory",
                saved.getId(),
                "Created category: " + saved.getName()
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        ExpenseCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseCategory", "id", id));

        if (!category.getName().equalsIgnoreCase(request.getName().trim()) &&
                categoryRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
        }

        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        ExpenseCategory updated = categoryRepository.save(category);

        auditLogService.log(
                AuditAction.CATEGORY_UPDATE,
                "ExpenseCategory",
                updated.getId(),
                "Updated category: " + updated.getName()
        );

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        ExpenseCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseCategory", "id", id));

        // Soft delete: deactivate
        category.setActive(false);
        categoryRepository.save(category);

        auditLogService.log(
                AuditAction.CATEGORY_DELETE,
                "ExpenseCategory",
                id,
                "Deactivated category: " + category.getName()
        );
    }

    private CategoryResponse mapToResponse(ExpenseCategory cat) {
        return CategoryResponse.builder()
                .id(cat.getId())
                .name(cat.getName())
                .description(cat.getDescription())
                .active(cat.isActive())
                .createdAt(cat.getCreatedAt())
                .build();
    }
}
