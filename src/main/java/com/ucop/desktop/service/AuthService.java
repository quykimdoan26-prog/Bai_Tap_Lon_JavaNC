package com.ucop.desktop.service;

import com.ucop.desktop.domain.Account;
import com.ucop.desktop.repo.AccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final AccountRepository accounts;
    private final PasswordEncoder encoder;
    public AuthService(AccountRepository accounts, PasswordEncoder encoder) {
        this.accounts = accounts;
        this.encoder = encoder;
    }
    @Transactional(readOnly = true)
    public Account authenticate(String username, String password) {
        Account account = accounts.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Tên đăng nhập không tồn tại."));
        if (!"ACTIVE".equalsIgnoreCase(account.getStatus()) || !encoder.matches(password, account.getPasswordHash())) {
            throw new IllegalArgumentException("Tài khoản bị khóa hoặc mật khẩu không đúng.");
        }
        return account;
    }

    @Transactional
    public void changePassword(Account account, String oldPassword, String newPassword) {
        if (account == null || !encoder.matches(oldPassword, account.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }
        account.setPasswordHash(encoder.encode(newPassword));
        accounts.save(account);
    }
}
