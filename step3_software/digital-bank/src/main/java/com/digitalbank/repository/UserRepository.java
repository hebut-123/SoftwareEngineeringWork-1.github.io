package com.digitalbank.repository;

import com.digitalbank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 根据手机号查询用户
    Optional<User> findByPhone(String phone);

    // 根据手机号判断用户是否存在
    boolean existsByPhone(String phone);

    // 根据身份证号判断用户是否存在
    boolean existsByIdCard(String idCard);

    // 根据昵称判断用户是否存在
    boolean existsByNickname(String nickname);
}