package com.chinchinne.authservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@Getter
@AllArgsConstructor
public enum ErrorCode
{
     UNKNOWN_MEMBER(1010, "존재하지 않는 사용자 입니다.")
    ,INCORRECT_PASSWORD(1011, "비밀번호가 일치하지 않습니다.");

    private final int code;
    private final String detail;
}
