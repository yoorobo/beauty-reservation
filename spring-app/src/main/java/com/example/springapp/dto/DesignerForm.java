package com.example.springapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DesignerForm {

    private Long id;

    @NotBlank(message = "디자이너 이름을 입력해주세요.")
    @Size(max = 100, message = "디자이너 이름은 100자 이하로 입력해주세요.")
    private String name;

    @NotBlank(message = "전문 분야를 입력해 주세요")
    @Size(max = 100, message = "전문 분야는 100자 이하로 입력해주세요.")
    private String specialty;

    // 소개: 필수 아님, 길이만 제한 [B5 갱신]
    @Size(max = 1000, message = "소개는 1000자 이하로 입력해주세요.")
    private String introduction;
}