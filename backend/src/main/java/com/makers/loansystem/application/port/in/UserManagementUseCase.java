package com.makers.loansystem.application.port.in;

import com.makers.loansystem.application.dto.UserResponse;

import java.util.List;

public interface UserManagementUseCase {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    void deleteUser(Long id);
}
