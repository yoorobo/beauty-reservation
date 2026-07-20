package com.example.springapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DesignerForm {

    @NotBlank
    private String name;

    private String specialty;

    private String introduction;
}
