package com.ucop.desktop.service;

import com.ucop.desktop.domain.Account;
import com.ucop.desktop.domain.Role;
import com.ucop.desktop.repo.AccountRepository;
import com.ucop.desktop.repo.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final AccountRepository accounts;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;

    public AccountService(AccountRepository accounts, RoleRepository roles, PasswordEncoder encoder) {
        this.accounts = accounts;
        this.roles = roles;
        this.encoder = encoder;
    }

    @Transactional
    public Account save(Account actor, String username, String password, String roleName) {
        requireAdmin(actor);
        if (username == null || username.isBlank() || password == null || password.length() < 6) {
            throw new IllegalArgumentException("Tên đăng nhập không trống, mật khẩu tối thiểu 6 ký tự.");
        }
        Account account = accounts.findByUsername(username.trim()).orElseGet(Account::new);
        Role role = roles.findAll().stream()
                .filter(r -> r.getRoleName().equalsIgnoreCase(roleName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Vai trò không tồn tại."));
        account.setUsername(username.trim());
        account.setPasswordHash(encoder.encode(password));
        account.setRole(role);
        account.setStatus("ACTIVE");
        return accounts.save(account);
    }

    @Transactional
    public void toggleStatus(Account actor, int accountId) {
        requireAdmin(actor);
        Account target = accounts.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại."));
        if (target.getAccountId().equals(actor.getAccountId())) {
            throw new IllegalArgumentException("Không thể khóa tài khoản đang đăng nhập.");
        }
        target.setStatus("ACTIVE".equalsIgnoreCase(target.getStatus()) ? "LOCKED" : "ACTIVE");
    }

    private void requireAdmin(Account actor) {
        if (actor == null || actor.getRole() == null
                || !"ADMIN".equalsIgnoreCase(actor.getRole().getRoleName())) {
            throw new SecurityException("Chức năng này chỉ dành cho Admin.");
        }
    }
}
