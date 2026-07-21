package com.example.springapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeautyServiceForm {

    private Long id;

    @NotBlank(message = "서비스명을 입력해주세요.")
    @Size(max = 100, message = "서비스명은 100자 이하로 입력해주세요.")
    private String serviceName;

    // Integer + @NotNull : 빈값 제출 시 typeMismatch 대신 커스텀 메시지 출력 [S3]
    @NotNull(message = "가격을 입력해주세요.")
    @Positive
    private Integer price;

    @NotNull(message = "소요 시간을 입력해주세요.")
    @Positive
    private Integer duration;

    // 설명: 필수 아님, 길이만 제한 [S3 갱신]
    @Size(max = 1000, message = "설명은 1000자 이하로 입력해주세요.")
    private String description;
}