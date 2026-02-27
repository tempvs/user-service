package club.tempvs.user.dto;

import club.tempvs.user.dto.validation.Scope;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CredentialsDto {

    @Email(groups = {Scope.Register.class, Scope.Login.class})
    @Null(groups = Scope.Verify.class)
    @NotBlank(groups = {Scope.Register.class, Scope.Login.class})
    private String email;
    @Null(groups = Scope.Register.class)
    @NotBlank(groups = {Scope.Verify.class, Scope.Login.class})
    private String password;
}
