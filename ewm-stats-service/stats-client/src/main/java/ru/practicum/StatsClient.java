package ru.practicum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

public class StatsClient extends BaseClient {

    public StatsClient(@Value("${ewm-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> createHit(EndpointHitDto dto) {
        return post("/hit", dto);
    }

    public ResponseEntity<Object> findStats(LocalDateTime start, LocalDateTime end, Boolean unique) {
        String encodedStartTime = URLEncoder.encode(start.toString(), StandardCharsets.UTF_8);
        String endStartTime = URLEncoder.encode(end.toString(), StandardCharsets.UTF_8);
        Map<String, Object> parameters = Map.of(
                "start", encodedStartTime,
                "end", endStartTime,
                "unique", unique
        );

        return get("/stats?start={start}&end={end}&unique={unique}", parameters);
    }
}
