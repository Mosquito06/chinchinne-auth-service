package com.chinchinne.authservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse
{
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat( shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String error;
    private final int code;
    private final String message;

    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode)
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body
                            (
                                ErrorResponse.builder()
                                            .status(errorCode.getCode())
                                            .error(errorCode.name())
                                            .code(errorCode.getCode())
                                            .message(errorCode.getDetail())
                                            .build()
                            );
    }

    public static void toResponseEntity(HttpServletResponse response, ErrorCode errorCode)
        {
            ObjectMapper objectMapper = new ObjectMapper();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.valueOf("application/json; charset=UTF-8").toString());

        try
        {
            response.getWriter().write(objectMapper.writeValueAsString
            (
                 ErrorResponse.builder()
                             .status(errorCode.getCode())
                             .error(errorCode.name())
                             .code(errorCode.getCode())
                             .message(errorCode.getDetail())
                             .build()
            ));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
