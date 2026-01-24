package udtale.config.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        String BASE_URL = "http://127.0.0.1:5000/transliterate";

        return RestClient.builder()
                .baseUrl(BASE_URL)
                .build();
    }

}
