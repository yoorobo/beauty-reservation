package com.example.springapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberUpdateForm {

    @NotBlank(message = "이름을 입력해주세요.")
    @Size(max = 50, message = "이름은 50자 이하로 입력해주세요.")
    private String name;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    /*
   * 수정할 때 비밀번호를 입력하지 않으면
   * 기존 비밀번호를 유지합니다.
     */

    @Size(min = 8, max = 20, message = "비밀번호는 8~20자로 입력해주세요.")
    private String password;
}