package com.makers.loansystem.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Datos para solicitar un nuevo préstamo")
public record CreateLoanRequest(
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "100.00", message = "El monto mínimo permitido es $100.00")
    @DecimalMax(value = "100000000.00", message = "El monto máximo permitido es $100,000,000.00")
    @Schema(description = "Monto solicitado en COP/USD", example = "5000.00")
    BigDecimal amount,

    @NotNull(message = "El plazo en meses es obligatorio")
    @Min(value = 1, message = "El plazo mínimo es 1 mes")
    @Max(value = 72, message = "El plazo máximo es 72 meses")
    @Schema(description = "Plazo en meses para pagar el préstamo", example = "12")
    Integer termMonths
) {}
