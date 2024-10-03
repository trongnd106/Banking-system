package com.trongdev.banking_system.controller;

import com.trongdev.banking_system.dto.request.UserCreateRequest;
import com.trongdev.banking_system.dto.request.UserUpdateRequest;
import com.trongdev.banking_system.dto.response.*;
import com.trongdev.banking_system.entity.User;
import com.trongdev.banking_system.service.AccountService;
import com.trongdev.banking_system.service.TransactionService;
import com.trongdev.banking_system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "User Controller")
public class UserController {
    UserService userService;
    AccountService accountService;
    TransactionService transactionService;

    @Operation(summary = "Add new user",
            description = "Send a request via this API to create new user")
    @PostMapping
    ApiResponse<String> createUser(@RequestBody UserCreateRequest request){
        return ApiResponse.<String>builder()
                .code(2000)
                .result(userService.register(request))
                .message("Create user successfully!")
                .build();
    }

    @Operation(summary = "Get list of users", description = "Send a request via this API to get user list by page")
    @GetMapping
    ApiResponse<PaginatedResponse<UserResponse>> getAllUsers(@RequestParam(defaultValue = "1") int page){
        return ApiResponse.<PaginatedResponse<UserResponse>>builder()
                .code(1000)
                .result(userService.getAll(page))
                .message("Success get all users with page" + page)
                .build();
    }

    @Operation(summary = "Get user detail", description = "Send a request via this API to get user information")
    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUserDetail(@PathVariable("userId") int id){
        return ApiResponse.<UserResponse>builder()
                .code(1000)
                .result(userService.getDetail(id))
                .message("Success get user detail!")
                .build();
    }

    @Operation(summary = "Get current user", description = "Send a request via this API to get current user information")
    @GetMapping("/my-info")
    ApiResponse<UserResponse> getMyInfo(){
        return ApiResponse.<UserResponse>builder()
                .code(1000)
                .result(userService.getMyInfo())
                .message("Get self-information successfully!")
                .build();
    }

    @Operation(summary = "Update user", description = "Send a request via this API to update an user")
    @PatchMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable("userId") int id, @RequestBody UserUpdateRequest request){
        return ApiResponse.<UserResponse>builder()
                .code(2000)
                .result(userService.update(id, request))
                .message("Update user successfully!")
                .build();
    }

    @Operation(summary = "Delete user", description = "Send a request via this API to get delete user")
    @DeleteMapping("/{userId}")
    ApiResponse<Void> deleteUser(@PathVariable("userId") int id){
        userService.delete(id);
        return ApiResponse.<Void>builder()
                .code(999)
                .message("User deleted successfully!")
                .build();
    }

    @Operation(summary = "Get current user accounts",
            description = "Send a request via this API to get current user's all accounts")
    @GetMapping("/my-account")
    ApiResponse<PaginatedResponse<AccountDetailResponse>> getAccounts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "1") int isActive){
        return ApiResponse.<PaginatedResponse<AccountDetailResponse>>builder()
                .code(1000)
                .result(accountService.getMyAccount(page,isActive))
                .message("Get your all accounts successfully!")
                .build();
    }

    @Operation(summary = "Get current user transactions",
            description = "Send a request via this API to get current user's all transactions")
    @GetMapping("/my-transaction")
    ApiResponse<PaginatedResponse<TransactionResponse>> getTransaction(@RequestParam(defaultValue = "1") int page){
        return ApiResponse.<PaginatedResponse<TransactionResponse>>builder()
                .code(1000)
                .result(transactionService.getMyTransaction(page))
                .message("Get your all transactions successfully!")
                .build();
    }
}
