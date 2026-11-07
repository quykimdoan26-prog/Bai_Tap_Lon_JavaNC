package com.ucop.desktop.domain;

import javax.persistence.*;

@Entity
@Table(name = "ACCOUNT_PROFILE")
public class AccountProfile {
    @Id
    @Column(name = "AccountID")
    private Integer accountId;
    @OneToOne
    @MapsId
    @JoinColumn(name = "AccountID")
    private Account account;
    @Column(nullable = false, length = 100)
    private String fullName;
    @Column(unique = true, length = 100)
    private String email;
    @Column(length = 20)
    private String phone;
    public Integer getAccountId() { return accountId; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
