package com.layth.Library.Management.System.requestsAndResponses.auth;

public class AuthResponse {
    private Integer id;
    private String userName;

    private Integer roles;
    private String token;

    public AuthResponse() {
    }

    public AuthResponse(Integer id, String userName,  Integer roles, String token) {
        this.id = id;
        this.userName = userName;
        this.roles = roles;
        this.token = token;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
