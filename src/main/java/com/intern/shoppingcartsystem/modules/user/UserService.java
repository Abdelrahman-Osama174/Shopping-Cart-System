package com.intern.shoppingcartsystem.modules.user;

import com.intern.shoppingcartsystem.common.pagination.PageResponse;
import com.intern.shoppingcartsystem.common.exception.AdminRoleModificationNotAllowedException;
import com.intern.shoppingcartsystem.common.exception.AdminStatusModificationNotAllowedException;
import com.intern.shoppingcartsystem.common.exception.AdminRoleAssignmentNotAllowedException;
import com.intern.shoppingcartsystem.common.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;


    public PageResponse<UserResponse> getAllUsers(String email, Role role, Boolean active, int page, int size) {

        int pageSize = Math.min(size, 50);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> users = userRepository.searchUsers(email, role, active, pageable);

        Page<UserResponse> userResponses = users.map(userMapper::toResponse);

        PageResponse<UserResponse> responses = PageResponse.from(userResponses);
        return responses;
    }


    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        UserResponse response = userMapper.toResponse(user);
        return response;
    }


    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.role() == Role.ADMIN) {
            throw new AdminRoleAssignmentNotAllowedException("Cannot assign ADMIN role through this operation");
        }

        if (user.getRole() == Role.ADMIN && request.role() != Role.ADMIN) {
            throw new AdminRoleModificationNotAllowedException("Cannot change the role of an ADMIN account.");
        }

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        user.setRole(request.role());

        userRepository.save(user);

        UserResponse response = userMapper.toResponse(user);
        return response;
    }


    public UserResponse updateUserStatus(Long id, @Valid UserStatusRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (user.getRole() == Role.ADMIN) {
            throw new AdminStatusModificationNotAllowedException("Admin account status cannot be modified.");
        }

        user.setActive(request.active());

        userRepository.save(user);

        UserResponse response = userMapper.toResponse(user);
        return response;
    }


    public UserResponse getMyProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        UserResponse response = userMapper.toResponse(user);
        return response;
    }


    public UserResponse updateMyProfile(Long id, @Valid ProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());

        userRepository.save(user);

        UserResponse response = userMapper.toResponse(user);
        return response;
    }
}
