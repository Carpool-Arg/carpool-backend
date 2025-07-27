package com.carpool.carpool.dto.google;

import com.carpool.carpool.enums.user.UserStateEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleAuthResponse {

    @Schema(example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcmdjYXJwb29sQGdtYWlsLmNvbSIsImF1dGhvcml0aWVzIjoiW3tcImF1dGhvcml0eVwiOlwiUk9MRV9VU0VSXCJ9XSIsInVzZXJuYW1lasdnY2FycG9vbEBnbWFpbC5gfdgNTIyNzkyMTIsImlhdCI6MTc1MasdTYxMn0as8yxMqwmKRcyHNcDfe7zClA2qD7XJKQ")
    private String accessToken;

    @Schema(example = "IiOiJhcmdjYXJwb29sQGdtYWlsLmNvbSIsImF1dGhvcml0aWVzIjoiW3tcImF1dGhvcml0eVwiOlwiUk9MRV9VU0VSXCJ9XSIsInVzZXJuYW1lasdnY2FycG9vbEBnbW")
    private String refreshToken;

    @Schema(example = "juaNN@gmail.com")
    private String email;

    @Schema(example = "Juan")
    private String name;

    @Schema(example = "ACTIVE")
    private UserStateEnum status;

    @Schema(example = "false")
    private boolean needsAction;
}
