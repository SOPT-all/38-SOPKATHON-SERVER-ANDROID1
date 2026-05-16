package org.sopt.android1.global.response;

import lombok.Getter;

@Getter
public enum SuccessCode {

    OK(200, "요청이 성공했습니다."),
    CREATED(201, "리소스가 생성되었습니다."),
    NO_CONTENT(204, "응답 본문이 없습니다.");

    private final int status;
    private final String message;

    SuccessCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
