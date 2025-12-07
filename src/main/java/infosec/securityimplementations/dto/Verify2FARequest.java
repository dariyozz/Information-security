package infosec.securityimplementations.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Verify2FARequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "2FA code is required")
    private String code;
}
