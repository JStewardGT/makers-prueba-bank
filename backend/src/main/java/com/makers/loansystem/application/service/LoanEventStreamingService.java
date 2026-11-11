package com.makers.loansystem.application.service;

import com.makers.loansystem.application.dto.LoanStatusEvent;
import com.makers.loansystem.application.port.in.LoanEventStreamUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class LoanEventStreamingService implements LoanEventStreamUseCase {

    private final Sinks.Many<LoanStatusEvent> sink = Sinks.many().multicast().onBackpressureBuffer();

    @Override
    public Flux<LoanStatusEvent> getLoanEvents() {
        return sink.asFlux();
    }

    @Override
    public void publishEvent(LoanStatusEvent event) {
        sink.tryEmitNext(event);
    }
}
