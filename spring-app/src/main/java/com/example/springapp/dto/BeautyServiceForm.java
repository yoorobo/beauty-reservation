package com.example.springapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeautyServiceForm {

    @NotBlank
    private String serviceName;

    @Positive
    private int price;

    @Positive
    private int duration;

    private String description;
}
