package com.trongdev.banking_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trongdev.banking_system.dto.request.OtpRequest;
import com.trongdev.banking_system.dto.response.ApiResponse;
import com.trongdev.banking_system.dto.response.UserResponse;
import com.trongdev.banking_system.entity.User;
import com.trongdev.banking_system.exception.AppException;
import com.trongdev.banking_system.exception.ErrorCode;
import com.trongdev.banking_system.mapper.UserMapper;
import com.trongdev.banking_system.repository.UserRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OtpService {

    @NonFinal
    @Value("${twilio.account-sid}")
    String accountSid;

    @NonFinal
    @Value("${twilio.auth-token}")
    String authToken;

    @NonFinal
    @Value("${twilio.from-phone}")
    String fromPhone;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    UserMapper userMapper;

    private static final long OTP_EXPIRATION = 5;

    @PostConstruct
    public void initTwilio() {
        // Init Twilio with accountSid and authToken
        Twilio.init(accountSid, authToken);
    }

    public void saveOtp(String phoneNumber, String otp) {
        // Save OTP and phone number to Redis
        redisTemplate.opsForValue().set("otp:" + phoneNumber, otp, OTP_EXPIRATION, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("otp:" + otp, phoneNumber, OTP_EXPIRATION, TimeUnit.MINUTES);
    }

    public String generateOtp(String phoneNumber) {
        String otp = String.format("%06d", new Random().nextInt(999999)); // Random OTP 6 characters
        saveOtp(phoneNumber, otp); // Save Otp to redis
        return otp;
    }

    public void sendOtpToPhoneNumber(String phoneNumber, String otp) {
        String messageBody = "Your OTP is: " + otp;

        // Gửi tin nhắn SMS qua Twilio
        Message message = Message.creator(
                new PhoneNumber(phoneNumber), // receiver number
                new PhoneNumber(fromPhone),   // sender number
                messageBody                    // content
        ).create();

        System.out.println("Sent message with SID: " + message.getSid());
    }

    public String getOtp(String phoneNumber) {
        return redisTemplate.opsForValue().get("otp:" + phoneNumber);
    }

    public void deleteOtp(String phoneNumber) {
        redisTemplate.delete("otp:" + phoneNumber); // delete otp after verified
    }

    public User getUserTemporary(String phoneNumber) {
        try {
            // Get JSON from Redis
            String userJson = redisTemplate.opsForValue().get("user:" + phoneNumber);
            // convert JSON to User object
            return objectMapper.readValue(userJson, User.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveUserTemporary(User user) {
        try {
            // Convert User object to JSON
            String userJson = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set("user:" + user.getPhone(), userJson, OTP_EXPIRATION, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
    public String getPhoneNumberByOtp(String otp) {
        return redisTemplate.opsForValue().get("otp:" + otp);
    }

    public UserResponse verifyOtp(@RequestBody OtpRequest otpRequest) {
        // Get phone from Redis with Otp
        String phoneNumber = getPhoneNumberByOtp(otpRequest.getOtp());

        // Check phone exist
        if (phoneNumber != null) {
            // Get saved OTP from Redis
            String savedOtp = getOtp(phoneNumber);

            // Check OTP valid ?
            if (savedOtp != null && savedOtp.equals(otpRequest.getOtp())) {
                // Delete OTP when authenticated
                deleteOtp(phoneNumber);

                // Get current user from Redis
                User user = getUserTemporary(phoneNumber);
                if (user != null) {
                    user.setIsVerified(1);
                    userRepository.save(user);

                    return userMapper.toUserResponse(userRepository.save(user));
                } else
                    throw new RuntimeException("User not found");
            } else
                throw new RuntimeException("Invalid or expired OTP");
        } else
            throw new RuntimeException("Phone number not found for the given OTP");
    }
}

