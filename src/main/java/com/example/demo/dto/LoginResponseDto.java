package com.example.demo.dto;

import java.util.List;

public class LoginResponseDto {
    private String token;
    private AdminDto user;

    public LoginResponseDto(String token, AdminDto user){
        this.token = token;
        this.user = user;
    }

    // getters y setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public AdminDto getUser() { return user; }
    public void setUser(AdminDto user) { this.user = user; }
}
