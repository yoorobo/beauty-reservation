package com.example.springapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeautyServiceForm {

    @NotBlank(message = "서비스명을 입력해주세요.")
    private String serviceName;

    // Integer + @NotNull : 빈값 제출 시 typeMismatch 대신 커스텀 메시지 출력 [S3]
    @NotNull(message = "가격을 입력해주세요.")
    @Positive
    private Integer price;

    @NotNull(message = "소요 시간을 입력해주세요.")
    @Positive
    private Integer duration;

    // 설명은 검증 없음 [S3]
    private String description;
}
