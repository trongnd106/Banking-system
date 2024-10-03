package com.trongdev.banking_system.configuration;

import com.trongdev.banking_system.dto.request.OtpRequest;
import com.trongdev.banking_system.dto.response.ApiResponse;
import com.trongdev.banking_system.dto.response.UserResponse;
import com.trongdev.banking_system.service.OtpService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/otp")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OtpController {
    OtpService otpService;

    @PostMapping
    ApiResponse<UserResponse> verifiedToCreate(@RequestBody OtpRequest request){
        return ApiResponse.<UserResponse>builder()
                .code(2000)
                .result(otpService.verifyOtp(request))
                .message("User created and verified successfully!")
                .build();
    }
}
