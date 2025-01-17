package ru.practicum.ewm.stat.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.stat.dto.EndpointHitDto;
import ru.practicum.ewm.stat.dto.ViewStatsDto;
import ru.practicum.ewm.stat.exception.StatClientException;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class StatClient {

    private final RestClient restClient;
    private final String serverUri;
    private final String hitPath;
    private final String statsPath;

    public StatClient(@Value("${stats-server.uri:http://localhost:9090}") String serverUri) {
        this.restClient = RestClient.create();
        this.serverUri = serverUri;
        this.hitPath = "/hit";
        this.statsPath = "/stats";
    }

    public void hit(EndpointHitDto endpointHitDto) {
        String uri = UriComponentsBuilder.fromHttpUrl(serverUri)
                .path(hitPath)
                .toUriString();

        restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(endpointHitDto)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new StatClientException(response.getStatusCode().value(), response.getBody().toString());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new StatClientException(response.getStatusCode().value(), response.getBody().toString());
                })
                .toBodilessEntity();
    }

    public List<ViewStatsDto> getStat(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        String uri = UriComponentsBuilder.fromHttpUrl(serverUri)
                .path(statsPath)
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", uris)
                .queryParam("unique", unique)
                .toUriString();

        return restClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new StatClientException(response.getStatusCode().value(), response.getBody().toString());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new StatClientException(response.getStatusCode().value(), response.getBody().toString());
                })
                .body(new ParameterizedTypeReference<>() {
                });
    }
}