package com.ecommerce.project.repositories;

import com.ecommerce.project.model.ForgotPassword;
import com.ecommerce.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Integer> {

    @Query("SELECT fp FROM ForgotPassword fp WHERE fp.otp = ?1 AND fp.user = ?2")
    Optional<ForgotPassword> findByOtpAndUser(Integer otp, User user);

    @Query("SELECT fp FROM ForgotPassword fp WHERE fp.user = ?1")
    Optional<ForgotPassword> findByUser(User user);

    @Transactional
    @Modifying
    @Query("DELETE FROM ForgotPassword fp WHERE fp.fpid = ?1")
    void deleteByFpid(Integer fpid);

    Optional<ForgotPassword> findByOtp(int otp);
}
