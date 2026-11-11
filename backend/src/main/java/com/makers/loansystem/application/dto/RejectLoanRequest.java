package com.makers.loansystem.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Motivo opcional/informativo de rechazo del préstamo")
public record RejectLoanRequest(
    @Schema(description = "Motivo del rechazo", example = "Capacidad de endeudamiento insuficiente")
    String reason
) {}
