package club.tempvs.user.http;

import club.tempvs.user.dto.EmailDto;

public interface EmailHttpClient {
    void post(EmailDto payload);
}
