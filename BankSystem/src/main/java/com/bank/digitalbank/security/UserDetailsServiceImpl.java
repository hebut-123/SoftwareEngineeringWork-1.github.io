package com.bank.digitalbank.security;

import com.bank.digitalbank.entity.User;
import com.bank.digitalbank.entity.enums.UserStatus; // 修正枚举导入路径
import com.bank.digitalbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 用户详情服务实现类
 * 负责从数据库加载用户信息并转换为Spring Security的UserDetails对象
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 根据用户名加载用户详情
     * 支持用户名/手机号/邮箱登录（可扩展）
     *
     * @param username 用户名/手机号/邮箱
     * @return UserDetails Spring Security用户详情对象
     * @throws UsernameNotFoundException 用户不存在异常
     */
    @Override
    @Transactional(readOnly = true) // 只读事务，提升性能并保证数据一致性
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("开始加载用户信息，登录标识: {}", username);

        // 1. 从数据库查询用户（建议在Repository中实现多条件查询：用户名/手机号/邮箱）
        // 如需支持多条件登录，可修改为 userRepository.findByUsernameOrPhoneOrEmail(username)
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("用户不存在，登录标识: {}", username);
                    return new UsernameNotFoundException("用户不存在或密码错误");
                });

        // 2. 校验用户状态（核心业务校验，银行系统对用户状态敏感）
        validateUserStatus(user);

        // 3. 构建用户权限列表（无角色，默认分配ROLE_USER）
        List<GrantedAuthority> authorities = buildUserAuthorities();

        log.info("用户加载成功，用户ID: {}, 用户名: {}", user.getId(), user.getUsername());

        // 4. 构建并返回Spring Security的UserDetails对象
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(), // 账户是否启用
                user.isAccountNonExpired(), // 账户是否未过期
                user.isCredentialsNonExpired(), // 凭证是否未过期
                user.isAccountNonLocked(), // 账户是否未锁定
                authorities // 权限列表
        );
    }

    /**
     * 校验用户状态是否合法
     * 银行系统中常见的状态：启用、禁用、锁定、冻结等
     *
     * @param user 用户实体
     * @throws UsernameNotFoundException 状态不合法时抛出异常
     */
    private void validateUserStatus(User user) {
        UserStatus status = user.getStatus();
        log.debug("校验用户状态，用户ID: {}, 状态: {}", user.getId(), status);

        if (status == null) {
            log.error("用户状态为空，用户ID: {}", user.getId());
            throw new UsernameNotFoundException("用户状态异常，请联系管理员");
        }

        switch (status) {
            case INACTIVE:
                throw new UsernameNotFoundException("用户未激活，请先完成邮箱/手机验证");
            case LOCKED:
                throw new UsernameNotFoundException("用户账户已被锁定，请联系客服解锁");
            case FROZEN:
                throw new UsernameNotFoundException("用户账户已被冻结，涉嫌违规操作");
            case DISABLED:
                throw new UsernameNotFoundException("用户账户已被禁用，请联系管理员");
            case ACTIVE:
                // 状态正常，无需处理
                break;
            default:
                throw new UsernameNotFoundException("未知的用户状态: " + status);
        }
    }

    /**
     * 构建用户权限列表
     * 无角色时，默认给所有用户分配ROLE_USER角色
     *
     * @return 权限列表
     */
    private List<GrantedAuthority> buildUserAuthorities() {
        // 所有用户默认拥有ROLE_USER权限
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
}