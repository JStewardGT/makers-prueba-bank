package com.makers.loansystem.application.port.in;

import com.makers.loansystem.application.dto.LoanStatusEvent;
import reactor.core.publisher.Flux;

public interface LoanEventStreamUseCase {
    Flux<LoanStatusEvent> getLoanEvents();
    void publishEvent(LoanStatusEvent event);
}
