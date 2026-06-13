package com.kte.backend.dto.responses;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginReponse {

    private  String accessToken;
    private String tokenType;
}
