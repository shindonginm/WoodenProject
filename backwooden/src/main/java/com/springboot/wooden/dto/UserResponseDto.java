package com.springboot.wooden.dto;

import com.springboot.wooden.domain.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private Long userNo;
    private String loginId;   // 이메일
    private String userName;

    public static UserResponseDto from(User u) {
        return UserResponseDto.builder()
                .userNo(u.getUserNo())
                .loginId(u.getLoginId())
                .userName(u.getUserName())
                .build();
    }
}
