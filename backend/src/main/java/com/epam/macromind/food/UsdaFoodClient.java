package com.epam.macromind.food;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import java.util.List;

@Component
class UsdaFoodClient {

    private final RestClient restClient;
    private final String apiKey;

    UsdaFoodClient(RestClient.Builder builder,
                   @Value("${usda.api.base-url:https://api.nal.usda.gov}") String baseUrl,
                   @Value("${USDA_API_KEY:DEMO_KEY}") String apiKey) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    List<UsdaFoodResult> search(String query) {
        try {
            UsdaSearchDto dto = restClient.get()
                    .uri("/fdc/v1/foods/search?query={q}&api_key={key}&pageSize=10", query, apiKey)
                    .retrieve()
                    .body(UsdaSearchDto.class);
            if (dto == null || dto.foods() == null) return List.of();
            return dto.foods().stream()
                    .filter(h -> h.fdcId() != null && h.description() != null)
                    .map(h -> new UsdaFoodResult(h.fdcId(), h.description()))
                    .toList();
        } catch (HttpClientErrorException e) {
            throw new UsdaServiceUnavailableException("USDA search error: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            throw new UsdaServiceUnavailableException("USDA API unreachable", e);
        }
    }

    UsdaFoodDto fetch(int fdcId) {
        try {
            return restClient.get()
                    .uri("/fdc/v1/food/{fdcId}?api_key={key}", fdcId, apiKey)
                    .retrieve()
                    .body(UsdaFoodDto.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new UsdaFoodNotFoundException(fdcId);
        } catch (HttpClientErrorException e) {
            throw new UsdaServiceUnavailableException("USDA API error: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            throw new UsdaServiceUnavailableException("USDA API unreachable", e);
        }
    }
}
