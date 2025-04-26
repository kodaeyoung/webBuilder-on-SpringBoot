package com.project.webBuilder.global.gpt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GptApi {

    @Value("${openai.api.key}")
    private static String openaiApiKey;

    private static final RestTemplate restTemplate = new RestTemplate();

    public static String gpt(String systemContent, String userContent, int maxToken) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemContent));
            messages.add(Map.of("role", "user", "content", userContent));
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", maxToken);
            requestBody.put("temperature", 0.7);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.openai.com/v1/chat/completions",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            List<Map> choices = (List<Map>) response.getBody().get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("No choices found in GPT response");
            }

            Map message = (Map) choices.get(0).get("message");
            if (message == null || !message.containsKey("content")) {
                throw new RuntimeException("Invalid message format in GPT response");
            }

            return ((String) message.get("content")).trim();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // API 호출 관련 오류
            System.err.println("HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return "GPT 호출 중 오류가 발생했습니다: " + e.getMessage();

        } catch (Exception e) {
            // 그 외 모든 오류
            e.printStackTrace();
            return "GPT 호출 실패: " + e.getMessage();
        }
    }
}
