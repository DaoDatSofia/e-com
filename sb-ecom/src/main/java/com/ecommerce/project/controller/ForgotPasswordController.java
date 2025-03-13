package com.ecommerce.project.controller;

import com.ecommerce.project.model.ForgotPassword;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.ChangePassword;
import com.ecommerce.project.payload.MailBody;
import com.ecommerce.project.repositories.ForgotPasswordRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

@RestController
@RequestMapping("/forgotPassword")
public class ForgotPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;

    @PostMapping("/verifyMail/{email}")
    public ResponseEntity<String> verifyMail(@PathVariable String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide a valid email!"));

        ForgotPassword existingOtp = forgotPasswordRepository.findByUser(user).orElse(null);
        if (existingOtp != null && existingOtp.getExpirationTime().before(Date.from(Instant.now()))) {
            forgotPasswordRepository.deleteByFpid(existingOtp.getFpid());
        }

        if (existingOtp != null && existingOtp.getExpirationTime().after(Date.from(Instant.now()))) {
            return new ResponseEntity<>("An OTP has already been sent. Please wait before requesting a new one!",
                    HttpStatus.TOO_MANY_REQUESTS);
        }

        int otp = otpGenerator();
        MailBody mailBody = MailBody.builder()
                .to(email)
                .text("This is the OTP for your forgot password request : " + otp)
                .build();

        ForgotPassword fp = ForgotPassword.builder()
                .otp(otp)
                .expirationTime(new Date(System.currentTimeMillis() + 5 * 60 * 1000))
                .user(user)
                .build();

        emailService.sendSimpleMessage(mailBody);
        forgotPasswordRepository.save(fp);

        return ResponseEntity.ok("Email sent for verification!");
    }

    @PostMapping("/verifyOtp/{otp}/{email}")
    public ResponseEntity<String> verifyOtp(@PathVariable Integer otp, @PathVariable String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide a valid email!"));

        ForgotPassword fp = forgotPasswordRepository.findByOtpAndUser(otp, user)
                .orElse(null);

        if (fp == null) {
            return new ResponseEntity<>("Invalid OTP!", HttpStatus.BAD_REQUEST);
        }

        if (fp.getExpirationTime().before(Date.from(Instant.now()))) {
            forgotPasswordRepository.deleteByFpid(fp.getFpid());
            return new ResponseEntity<>("OTP has expired!", HttpStatus.EXPECTATION_FAILED);
        }

        return ResponseEntity.ok("OTP verified!");
    }

    @PostMapping("/changePassword/{otp}/{email}")
    public ResponseEntity<String> changePasswordHandler(@RequestBody ChangePassword changePassword,
                                                        @PathVariable String email,
                                                        @PathVariable Integer otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide a valid email!"));

        ForgotPassword fp = forgotPasswordRepository.findByOtpAndUser(otp, user)
                .orElse(null);

        if (fp == null) {
            return new ResponseEntity<>("Invalid OTP!", HttpStatus.BAD_REQUEST);
        }
        
        if(!Objects.equals(changePassword.password(), changePassword.repeatPassword())) {
            return new ResponseEntity<>("Please enter the password again!", HttpStatus.EXPECTATION_FAILED);
        }

        String encoderPassword = passwordEncoder.encode(changePassword.password());
        user.setPassword(encoderPassword);
        userRepository.save(user);
        forgotPasswordRepository.deleteByFpid(fp.getFpid());

        return ResponseEntity.ok("Password has been changed!");
    }



    private Integer otpGenerator() {
        Random random = new Random();
        int otp;
        do {
            otp = random.nextInt(100_000, 999_999);
        } while (forgotPasswordRepository.findByOtp(otp).isPresent());
        return otp;
    }
}
