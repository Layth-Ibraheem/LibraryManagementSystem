package com.layth.Library.Management.System.utils.jwt;

public class CurrentUser {
    private Integer id;
    private String userName;
    private Integer roles;

    public CurrentUser(Integer id, String userName, Integer roles) {
        this.id = id;
        this.userName = userName;
        this.roles = roles;
    }

    public CurrentUser() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getRoles() {
        return roles;
    }

    public void setRoles(Integer roles) {
        this.roles = roles;
    }
}
