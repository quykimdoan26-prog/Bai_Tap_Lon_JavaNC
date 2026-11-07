package com.ucop.desktop.domain;

import javax.persistence.*;

@Entity
@Table(name = "ROLE")
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;
    @Column(nullable = false, unique = true, length = 50)
    private String roleName;
    public Integer getRoleId() { return roleId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    @Override public String toString() { return roleName; }
}
