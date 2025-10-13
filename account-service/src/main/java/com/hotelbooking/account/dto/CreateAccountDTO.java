package com.hotelbooking.account.dto;

import com.hotelbooking.account.interfaces.CreateAccountForm;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAccountDTO {
    @NotBlank(message = "Username is required", groups = {CreateAccountForm.Create.class, CreateAccountForm.Update.class})
    private String username;
    @NotBlank(message = "Email is required", groups = {CreateAccountForm.Create.class, CreateAccountForm.Update.class})
    @Email(message = "Email should be valid", groups = {CreateAccountForm.Create.class, CreateAccountForm.Update.class})
    private String email;
    @NotBlank(message = "Phone number is required", groups = {CreateAccountForm.Create.class, CreateAccountForm.Update.class})
    private String phone;
    @NotBlank(message = "Password is required", groups = {CreateAccountForm.Create.class, CreateAccountForm.Update.class})
    private String password;
    private String imageUrl;
    @NotBlank(message = "Role is required", groups = {CreateAccountForm.Create.class, CreateAccountForm.Update.class})
    private String role;
}
