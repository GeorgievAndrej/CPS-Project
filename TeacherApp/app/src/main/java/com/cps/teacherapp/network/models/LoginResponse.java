package com.cps.teacherapp.network.models;

public class LoginResponse {
    private String access_token;
    private String token_type;
    private UserOut user;

    public String getAccessToken() { return access_token; }
    public UserOut getUser() { return user; }

    public static class UserOut {
        private int id;
        private String username;
        private String full_name;
        private String role;
        private String email;

        public String getFullName() { return full_name; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
    }
}