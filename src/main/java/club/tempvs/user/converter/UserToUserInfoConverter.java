package club.tempvs.user.converter;

import club.tempvs.user.domain.User;
import club.tempvs.user.dto.UserInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class UserToUserInfoConverter implements Converter<User, String> {

    private final JsonMapper jsonMapper;

    @Override
    @SneakyThrows
    public String convert(User source) {
        UserInfoDto target = new UserInfoDto();
        BeanUtils.copyProperties(source, target);
        target.setUserId(source.getId());
        return jsonMapper.writeValueAsString(target);
    }
}
