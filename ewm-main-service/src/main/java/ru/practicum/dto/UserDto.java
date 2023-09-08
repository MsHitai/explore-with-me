package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class UserDto {

    private Long id;
    @NotBlank
    @NotNull
    @Email
    @Length(min = 6, max = 254)
    private String email;
    @NotBlank
    @NotNull
    @Length(min = 2, max = 250)
    private String name;
}
