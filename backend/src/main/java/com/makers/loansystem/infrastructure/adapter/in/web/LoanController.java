package com.makers.loansystem.infrastructure.adapter.in.web;

import com.makers.loansystem.application.dto.CreateLoanRequest;
import com.makers.loansystem.application.dto.LoanResponse;
import com.makers.loansystem.application.dto.LoanStatusEvent;
import com.makers.loansystem.application.dto.RejectLoanRequest;
import com.makers.loansystem.application.port.in.ApproveLoanUseCase;
import com.makers.loansystem.application.port.in.CreateLoanUseCase;
import com.makers.loansystem.application.port.in.GetLoansUseCase;
import com.makers.loansystem.application.port.in.LoanEventStreamUseCase;
import com.makers.loansystem.application.port.in.RejectLoanUseCase;
import com.makers.loansystem.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@Tag(name = "2. Préstamos Bancarios", description = "Endpoints para solicitud, aprobación, rechazo, consulta y streaming en tiempo real")
@SecurityRequirement(name = "Bearer Authentication")
public class LoanController {

    private final CreateLoanUseCase createLoanUseCase;
    private final ApproveLoanUseCase approveLoanUseCase;
    private final RejectLoanUseCase rejectLoanUseCase;
    private final GetLoansUseCase getLoansUseCase;
    private final LoanEventStreamUseCase loanEventStreamUseCase;

    public LoanController(CreateLoanUseCase createLoanUseCase,
                          ApproveLoanUseCase approveLoanUseCase,
                          RejectLoanUseCase rejectLoanUseCase,
                          GetLoansUseCase getLoansUseCase,
                          LoanEventStreamUseCase loanEventStreamUseCase) {
        this.createLoanUseCase = createLoanUseCase;
        this.approveLoanUseCase = approveLoanUseCase;
        this.rejectLoanUseCase = rejectLoanUseCase;
        this.getLoansUseCase = getLoansUseCase;
        this.loanEventStreamUseCase = loanEventStreamUseCase;
    }

    @PostMapping
    @Operation(summary = "Solicitar un préstamo (Usuario)",
            description = "Permite a un usuario autenticado solicitar un préstamo indicando monto y plazo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Préstamo solicitado exitosamente en estado PENDING",
                    content = @Content(schema = @Schema(implementation = LoanResponse.class))),
            @ApiResponse(responseCode = "400", description = "Monto o plazo inválidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<LoanResponse> requestLoan(
            @Valid @RequestBody CreateLoanRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        LoanResponse response = createLoanUseCase.createLoan(request, userDetails.getId(), userDetails.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-loans")
    @Operation(summary = "Consultar mis préstamos (Usuario)",
            description = "Devuelve el historial y estado de las solicitudes del usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de préstamos propios",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = LoanResponse.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<LoanResponse>> getMyLoans(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<LoanResponse> loans = getLoansUseCase.getMyLoans(userDetails.getId());
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar estado de un préstamo por ID (Caché)",
            description = "Consulta optimizada con caché Ehcache. Un usuario solo puede consultar sus préstamos; el administrador puede consultar cualquiera.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle del préstamo",
                    content = @Content(schema = @Schema(implementation = LoanResponse.class))),
            @ApiResponse(responseCode = "403", description = "No autorizado para ver este préstamo",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Préstamo no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<LoanResponse> getLoanById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        LoanResponse loan = getLoansUseCase.getLoanById(id, userDetails.getId(), isAdmin);
        return ResponseEntity.ok(loan);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas las solicitudes (Solo Administrador)",
            description = "Devuelve todas las solicitudes de préstamo del sistema para su gestión.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado global de préstamos",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = LoanResponse.class)))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere ROLE_ADMIN)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<LoanResponse>> getAllLoans() {
        List<LoanResponse> loans = getLoansUseCase.getAllLoans();
        return ResponseEntity.ok(loans);
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aprobar solicitud de préstamo (Solo Administrador)",
            description = "Transaccional: Aprueba una solicitud en estado PENDING, desaloja la caché y emite evento reactivo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Préstamo aprobado exitosamente",
                    content = @Content(schema = @Schema(implementation = LoanResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere ROLE_ADMIN)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Préstamo no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "El préstamo ya fue resuelto previamente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<LoanResponse> approveLoan(@PathVariable Long id) {
        LoanResponse response = approveLoanUseCase.approveLoan(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rechazar solicitud de préstamo (Solo Administrador)",
            description = "Transaccional: Rechaza una solicitud en estado PENDING con motivo opcional, desaloja caché y emite evento reactivo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Préstamo rechazado exitosamente",
                    content = @Content(schema = @Schema(implementation = LoanResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere ROLE_ADMIN)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Préstamo no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "El préstamo ya fue resuelto previamente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<LoanResponse> rejectLoan(
            @PathVariable Long id,
            @RequestBody(required = false) RejectLoanRequest request) {
        LoanResponse response = rejectLoanUseCase.rejectLoan(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Streaming reactivo en tiempo real SSE (Spring WebFlux)",
            description = "Flujo reactivo Server-Sent Events (SSE) que emite actualizaciones en vivo cada vez que un préstamo es solicitado, aprobado o rechazado.")
    public Flux<LoanStatusEvent> streamLoanEvents() {
        return loanEventStreamUseCase.getLoanEvents();
    }
}
