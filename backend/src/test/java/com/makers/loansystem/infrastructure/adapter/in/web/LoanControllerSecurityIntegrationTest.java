package com.makers.loansystem.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makers.loansystem.application.dto.CreateLoanRequest;
import com.makers.loansystem.application.dto.LoginRequest;
import com.makers.loansystem.application.dto.RejectLoanRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Pruebas de Integración y Seguridad - LoanController")
class LoanControllerSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String obtainToken(String email, String password) throws Exception {
        LoginRequest login = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("token").asText();
    }

    @Test
    @DisplayName("Sin Token: Debe retornar 401 Unauthorized al listar préstamos")
    void shouldReturn401WhenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/api/v1/loans"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Usuario estándar: Debe retornar 403 Forbidden al intentar aprobar un préstamo")
    void shouldReturn403WhenStandardUserAttemptsToApprove() throws Exception {
        String userToken = obtainToken("usuario@test.com", "123");

        mockMvc.perform(patch("/api/v1/loans/1/approve")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Usuario estándar: Puede solicitar un préstamo válido (201 Created)")
    void shouldAllowUserToRequestLoan() throws Exception {
        String userToken = obtainToken("usuario@test.com", "123");
        CreateLoanRequest request = new CreateLoanRequest(new BigDecimal("7500.00"), 18);

        mockMvc.perform(post("/api/v1/loans")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.amount").value(7500.00))
                .andExpect(jsonPath("$.termMonths").value(18))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Validación Bean: Retorna 400 Bad Request si el monto es menor al mínimo ($100)")
    void shouldReturn400WhenAmountIsInvalid() throws Exception {
        String userToken = obtainToken("usuario@test.com", "123");
        CreateLoanRequest request = new CreateLoanRequest(new BigDecimal("20.00"), 12);

        mockMvc.perform(post("/api/v1/loans")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Error de Validación"))
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    @Test
    @DisplayName("Administrador: Puede listar todos los préstamos (200 OK)")
    void shouldAllowAdminToGetAllLoans() throws Exception {
        String adminToken = obtainToken("admin@test.com", "123");

        mockMvc.perform(get("/api/v1/loans")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Administrador: Puede aprobar un préstamo en estado PENDING y rechaza re-aprobación con 409")
    void shouldApproveLoanAndRejectSecondAttemptWith409() throws Exception {
        String userToken = obtainToken("usuario@test.com", "123");
        String adminToken = obtainToken("admin@test.com", "123");

        // 1. Crear solicitud nueva
        CreateLoanRequest createReq = new CreateLoanRequest(new BigDecimal("12000.00"), 24);
        MvcResult createRes = mockMvc.perform(post("/api/v1/loans")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        long loanId = objectMapper.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        // 2. Admin aprueba el préstamo
        mockMvc.perform(patch("/api/v1/loans/" + loanId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // 3. Intento de re-aprobar el mismo préstamo -> Debe devolver 409 Conflict
        mockMvc.perform(patch("/api/v1/loans/" + loanId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Conflicto en Transición de Préstamo"));
    }

    @Test
    @DisplayName("Administrador: Puede rechazar un préstamo en estado PENDING con motivo")
    void shouldRejectLoanWithReason() throws Exception {
        String userToken = obtainToken("usuario@test.com", "123");
        String adminToken = obtainToken("admin@test.com", "123");

        CreateLoanRequest createReq = new CreateLoanRequest(new BigDecimal("80000.00"), 48);
        MvcResult createRes = mockMvc.perform(post("/api/v1/loans")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        long loanId = objectMapper.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        RejectLoanRequest rejectReq = new RejectLoanRequest("Capacidad de pago insuficiente");
        mockMvc.perform(patch("/api/v1/loans/" + loanId + "/reject")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value("Capacidad de pago insuficiente"));
    }
}
