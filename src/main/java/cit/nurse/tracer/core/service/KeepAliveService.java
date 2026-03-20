package cit.nurse.tracer.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class KeepAliveService {

    private static final Logger logger = LoggerFactory.getLogger(KeepAliveService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final String renderUrl;

    public KeepAliveService(@Value("${app.render.url}") String renderUrl) {
        this.renderUrl = renderUrl;
    }

    @Scheduled(fixedRate = 840000)
    public void selfPing() {
        String normalizedBaseUrl = renderUrl.endsWith("/")
            ? renderUrl.substring(0, renderUrl.length() - 1)
            : renderUrl;
        String pingUrl = normalizedBaseUrl + "/api/v1/public/ping";

        try {
            restTemplate.getForObject(pingUrl, String.class);
            logger.info("Self-ping successful");
        } catch (Exception exception) {
            logger.warn("Self-ping failed: {}", exception.getMessage());
        }
    }
}
