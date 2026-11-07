package com.ucop.desktop.service;

import com.ucop.desktop.domain.Account;
import com.ucop.desktop.domain.AccountProfile;
import com.ucop.desktop.domain.Role;
import com.ucop.desktop.repo.AccountRepository;
import com.ucop.desktop.repo.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {
    private final AccountRepository accounts;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;

    public RegistrationService(AccountRepository accounts, RoleRepository roles, PasswordEncoder encoder) {
        this.accounts = accounts;
        this.roles = roles;
        this.encoder = encoder;
    }

    @Transactional
    public Account registerCustomer(String username, String password, String fullName,
                                    String email, String phone) {
        if (username == null || username.isBlank() || password == null || password.length() < 6
                || fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Tên đăng nhập, họ tên không trống; mật khẩu tối thiểu 6 ký tự.");
        }
        if (accounts.findByUsername(username.trim()).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
        }
        Role customer = roles.findAll().stream()
                .filter(role -> "CUSTOMER".equalsIgnoreCase(role.getRoleName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Chưa cấu hình role CUSTOMER."));
        Account account = new Account();
        account.setUsername(username.trim());
        account.setPasswordHash(encoder.encode(password));
        account.setStatus("ACTIVE");
        account.setRole(customer);
        AccountProfile profile = new AccountProfile();
        profile.setAccount(account);
        profile.setFullName(fullName.trim());
        profile.setEmail(email == null || email.isBlank() ? null : email.trim());
        profile.setPhone(phone == null || phone.isBlank() ? null : phone.trim());
        account.setProfile(profile);
        return accounts.save(account);
    }

    @Transactional
    public void updateProfile(Account account, String fullName, String email, String phone) {
        if (account == null || account.getProfile() == null) {
            throw new IllegalArgumentException("Tài khoản chưa có hồ sơ.");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Họ tên không được để trống.");
        }
        AccountProfile profile = account.getProfile();
        profile.setFullName(fullName.trim());
        profile.setEmail(email == null || email.isBlank() ? null : email.trim());
        profile.setPhone(phone == null || phone.isBlank() ? null : phone.trim());
        accounts.save(account);
    }
}
