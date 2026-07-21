package com.example.springapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DesignerForm {

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    // 시술 스펙 준용(설명 무검증) : 전문분야·소개는 검증 없음 [B5]
    private String specialty;

    private String introduction;
}
