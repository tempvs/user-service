package club.tempvs.user.http;

import club.tempvs.user.dto.EmailDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EmailHttpClientImpl implements EmailHttpClient {

    private final RestClient restClient;

    public EmailHttpClientImpl(@Value("${email.service.url}") String url) {
        this.restClient = RestClient.builder()
                .baseUrl(url)
                .build();
    }

    @Override
    public void post(EmailDto payload) {
        restClient.post()
                .uri("/api/send")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(String.class);
    }
}
