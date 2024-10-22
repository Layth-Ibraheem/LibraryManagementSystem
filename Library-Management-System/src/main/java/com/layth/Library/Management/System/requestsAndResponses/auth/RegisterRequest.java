package com.layth.Library.Management.System.requestsAndResponses.auth;

public class RegisterRequest {
    private String userName;
    private String password;
    private Integer roles;

    public RegisterRequest() {
    }

    public RegisterRequest(String userName, String password, Integer roles) {
        this.userName = userName;
        this.password = password;
        this.roles = roles;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getRoles() {
        return roles;
    }

    public void setRoles(Integer roles) {
        this.roles = roles;
    }
}
