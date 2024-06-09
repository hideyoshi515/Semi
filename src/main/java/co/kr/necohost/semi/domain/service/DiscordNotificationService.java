package co.kr.necohost.semi.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class DiscordNotificationService {

    @Value("${discord.webhook.url}")
    private String discordWebhookUrl;

    private final RestTemplate restTemplate;

    public DiscordNotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendOrderNotification(long orderId) {
        String message = "주문 번호 " + orderId + "가 승인되었습니다.";
        Map<String, String> payload = new HashMap<>();
        payload.put("content", message);

        restTemplate.postForObject(discordWebhookUrl, payload, String.class);
    }
}
