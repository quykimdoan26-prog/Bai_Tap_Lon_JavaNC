package com.ucop.desktop.domain;

import javax.persistence.*;

@Entity
@Table(name = "ACCOUNT")
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer accountId;
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    @Column(name = "PasswordHash", nullable = false)
    private String passwordHash;
    @Column(length = 20)
    private String status = "ACTIVE";
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "RoleID")
    private Role role;
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private AccountProfile profile;
    public Integer getAccountId() { return accountId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public AccountProfile getProfile() { return profile; }
    public void setProfile(AccountProfile profile) { this.profile = profile; }
}
