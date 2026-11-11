package com.makers.loansystem.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;

@Service
public class ReactiveCreditScoreService {

    private static final Logger log = LoggerFactory.getLogger(ReactiveCreditScoreService.class);
    private final WebClient webClient;

    public ReactiveCreditScoreService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.mockcreditbureau.local").build();
    }

    public record CreditEvaluation(int score, String riskLevel, boolean eligible) {}

    public Mono<CreditEvaluation> evaluateCreditRisk(Long userId, BigDecimal amount, Integer termMonths) {
        log.info("Evaluando riesgo crediticio de forma no bloqueante con WebFlux para usuario: {}, monto: {}, plazo: {}",
                userId, amount, termMonths);

        // Simula llamada reactiva no bloqueante a un buró de crédito externo con latencia controlada
        return Mono.defer(() -> {
            Integer baseScore = 720;
            if (amount.compareTo(new BigDecimal("50000000")) > 0) {
                baseScore -= 60;
            }
            if (termMonths > 48) {
                baseScore -= 30;
            }

            String risk = switch (baseScore) {
                case Integer s when s >= 700 -> "LOW";
                case Integer s when s >= 600 -> "MEDIUM";
                default -> "HIGH";
            };

            boolean eligible = baseScore >= 600;
            CreditEvaluation eval = new CreditEvaluation(baseScore, risk, eligible);
            log.info("Resultado de evaluación crediticia reactiva: {}", eval);
            return Mono.just(eval);
        }).delayElement(Duration.ofMillis(80));
    }
}
