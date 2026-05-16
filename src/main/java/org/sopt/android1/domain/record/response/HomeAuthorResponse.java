package org.sopt.android1.domain.record.response;

import org.sopt.android1.domain.user.entity.UserEntity;

public record HomeAuthorResponse(
    String name,
    Integer age,
    String profileImageUrl
) {

    public static HomeAuthorResponse from(UserEntity user) {
        return new HomeAuthorResponse(user.getName(), user.getAge(), user.getProfileImageUrl());
    }
}
