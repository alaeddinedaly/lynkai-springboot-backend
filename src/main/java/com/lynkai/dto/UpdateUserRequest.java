package com.lynkai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UpdateUserRequest {

    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters.")
    private String username;

    @Email(message = "Invalid email format.")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters.")
    private String newPassword;

    private String oldPassword;

    // Constructors
    public UpdateUserRequest() {}

    public UpdateUserRequest(String username, String email, String oldPassword, String newPassword) {
        this.username = username;
        this.email = email;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
