package ru.practicum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;
import java.util.Map;


@Component
public class StatsClient {

    private final RestTemplate rest;

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        rest =
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build();
    }

    public void createHit(EndpointHitDto dto) {
        HttpEntity<EndpointHitDto> entity = new HttpEntity<>(dto, headers());
        rest.exchange("/hit", HttpMethod.POST, entity, Object.class).getStatusCodeValue();
    }

    public List<ViewStatsDto> findStats(String start, String end, Boolean unique, List<String> uris) {
        Map<String, Object> parameters = Map.of(
                "start", start,
                "end", end,
                "unique", unique,
                "uris", String.join(",", uris)
        );

        return rest.exchange("/stats?start={start}&end={end}&unique={unique}&uris={uris}",
                HttpMethod.GET, new HttpEntity<>(headers()), new ParameterizedTypeReference<List<ViewStatsDto>>() {
                },
                parameters).getBody();
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
