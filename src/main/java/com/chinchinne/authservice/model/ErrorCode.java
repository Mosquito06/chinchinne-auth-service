package com.chinchinne.authservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@Getter
@AllArgsConstructor
public enum ErrorCode
{
     UNKNOWN_MEMBER(HttpStatus.UNAUTHORIZED, "존재하지 않는 사용자 입니다.")
    ,INCORRECT_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String detail;
}
