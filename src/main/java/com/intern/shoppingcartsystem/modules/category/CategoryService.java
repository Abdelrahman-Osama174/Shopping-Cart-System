package com.intern.shoppingcartsystem.modules.category;

import com.intern.shoppingcartsystem.common.exception.ResourceAlreadyExist;
import com.intern.shoppingcartsystem.common.exception.ResourceNotFoundException;
import com.intern.shoppingcartsystem.common.pagination.PageResponse;
import com.intern.shoppingcartsystem.modules.user.Role;
import com.intern.shoppingcartsystem.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;


    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ResourceAlreadyExist("Category name already exists: " + name);
        }

        Category parent = getParent(request.parentId());

        Category category = categoryMapper.toCategory(request);
        category.setName(name);
        category.setParent(parent);

        categoryRepository.save(category);

        CategoryResponse response = categoryMapper.toResponse(category);
        return response;
    }


    public CategoryResponse getById(Long id) {
        Category category = categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active category not found with id: " + id));

        CategoryResponse response = categoryMapper.toResponse(category);

        return response;
    }


    public PageResponse<CategoryResponse> getAllCategories(String name, int page, int size) {

        int pageSize = Math.min(size, 50);
        String keyword = name == null ? "" : name.trim();

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Category> categories = null;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        if (principal.getUser().getRole() == Role.ADMIN) {
            categories = categoryRepository.searchCategory(keyword, pageable);
        } else {
            categories = categoryRepository.searchActiveCategories(keyword, pageable);
        }

        Page<CategoryResponse> categoryResponses = categories.map(categoryMapper::toResponse);

        PageResponse<CategoryResponse> responses = PageResponse.from(categoryResponses);
        return responses;
    }


    public List<CategoryResponse> getRootCategories() {
        List<Category> categories = categoryRepository.findAllByParentIsNullAndActiveTrue();

        List<CategoryResponse> responses = categories.stream()
                .map(categoryMapper::toResponse)
                .toList();

        return responses;
    }


    public List<CategoryResponse> getChildren(Long parentId) {
        Category category = categoryRepository.findByIdAndActiveTrue(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Active category not found with id: " + parentId));

        List<Category> categories = categoryRepository.findAllByParentIdAndActiveTrue(parentId);

        List<CategoryResponse> responses = categories.stream()
                .map(categoryMapper::toResponse)
                .toList();

        return responses;
    }


    public CategoryResponse toggleActive(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active category not found with id: " + id));

        category.setActive(!category.isActive());

        categoryRepository.save(category);

        CategoryResponse response = categoryMapper.toResponse(category);
        return response;
    }


    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        String name = request.name().trim();

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ResourceAlreadyExist("Category name already exists: " + name);
        }

        Category parent = getParent(request.parentId());

        if (parent != null && isDescendant(parent, id)) {
            throw new IllegalArgumentException("A category cannot be assigned under itself or its descendant");
        }

        category.setName(name);
        category.setDescription(request.description());
        category.setImageUrl(request.imageUrl());
        category.setParent(parent);
        if (request.active() != null) {
            category.setActive(request.active());
        }

        categoryRepository.save(category);

        CategoryResponse response = categoryMapper.toResponse(category);
        return response;
    }


    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (categoryRepository.existsByParentId(id)) {
            throw new IllegalArgumentException("Cannot delete a category that has child categories");
        }

        categoryRepository.delete(category);
    }


    /// Helper methods
    private Category getParent(Long parentId) {
        if (parentId == null)
            return null;

        Category parent = categoryRepository.findByIdAndActiveTrue(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + parentId));

        return parent;
    }


    private boolean isDescendant(Category candidate, Long categoryId) {
        Category current = candidate;

        while (current != null) {
            if (Objects.equals(current.getId(), categoryId)) {
                return true;
            }

            current = current.getParent();
        }

        return false;
    }

}
