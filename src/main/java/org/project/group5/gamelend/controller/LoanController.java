package org.project.group5.gamelend.controller;

import java.net.URI;
import java.time.LocalDateTime; // No se usa directamente aquí, pero sí en DTOs
import java.util.List;

import org.project.group5.gamelend.dto.ErrorResponse; // Para ExceptionHandler
import org.project.group5.gamelend.dto.LoanDTO; // DTO genérico para actualización
import org.project.group5.gamelend.dto.LoanRequestDTO; // Para NUEVAS solicitudes (gameId, notes)
import org.project.group5.gamelend.dto.LoanResponseDTO;
import org.project.group5.gamelend.dto.LoanReturnDTO;  // Para registrar devoluciones (returnDate)
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.service.LoanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Para seguridad a nivel de método
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST para gestión de préstamos de juegos.
 * Maneja solicitudes, devoluciones y administración de préstamos.
 */
@Slf4j
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
public class LoanController {

    private final LoanService loanService;

    /**
     * Solicita un nuevo préstamo de juego
     */
    @PostMapping("/request")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Solicitar nuevo préstamo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Préstamo creado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "No autorizado"),
        @ApiResponse(responseCode = "404", description = "Juego no encontrado"),
        @ApiResponse(responseCode = "409", description = "Juego no disponible")
    })
    public ResponseEntity<LoanResponseDTO> requestNewLoan(
            @Valid @RequestBody LoanRequestDTO loanRequestDto) {
        log.info("Solicitando préstamo: {}", loanRequestDto);
        LoanResponseDTO createdLoan = loanService.createLoanRequest(loanRequestDto);
        return ResponseEntity
            .created(URI.create("/api/loans/" + createdLoan.id()))
            .body(createdLoan);
    }

    /**
     * Lista todos los préstamos
     */
    @GetMapping
    public ResponseEntity<List<LoanResponseDTO>> getAllLoans() {
        log.info("GET /api/loans - Obteniendo todos los préstamos.");
        List<LoanResponseDTO> loans = loanService.getAllLoansDTO();
        if (loans.isEmpty()) {
            log.info("No se encontraron préstamos.");
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(loans);
    }

    /**
     * Obtiene un préstamo por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponseDTO> getLoanById(@PathVariable Long id) {
        log.info("GET /api/loans/{} - Buscando préstamo.", id);
        LoanResponseDTO loan = loanService.getLoanByIdAsResponseDTO(id); 
        return ResponseEntity.ok(loan);
    }

    /**
     * Actualiza un préstamo existente
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @loanSecurityService.isOwnerOfLoan(#id, principal.username) or @loanSecurityService.isBorrowerOfLoan(#id, principal.username)")
    public ResponseEntity<LoanResponseDTO> updateLoan(
            @PathVariable Long id, 
            @Valid @RequestBody LoanDTO loanDTO) {
        log.info("PUT /api/loans/{} - Actualizando préstamo con datos: {}", id, loanDTO);
        LoanResponseDTO updatedLoan = loanService.updateLoanFromDTO(id, loanDTO);
        log.info("Préstamo ID {} actualizado.", updatedLoan.id());
        return ResponseEntity.ok(updatedLoan);
    }

    /**
     * Elimina un préstamo (solo admin)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        log.info("DELETE /api/loans/{} - Eliminando préstamo.", id);
        loanService.deleteLoan(id);
        log.info("Préstamo ID {} eliminado.", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Registra la devolución de un préstamo
     */
    @PutMapping("/{id}/return")
    @PreAuthorize("hasRole('ADMIN') or @loanSecurityService.isBorrowerOfLoan(#id, principal.username)")
    public ResponseEntity<LoanResponseDTO> returnLoan(
            @PathVariable Long id,
            @Valid @RequestBody LoanReturnDTO returnDTO) {
        log.info("PUT /api/loans/{}/return - Registrando devolución: {}", id, returnDTO);
        LoanResponseDTO returnedLoan = loanService.recordLoanReturn(id, returnDTO);
        log.info("Préstamo ID {} devuelto.", returnedLoan.id());
        return ResponseEntity.ok(returnedLoan);
    }

    /**
     * Manejo de excepciones de solicitud inválida
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        log.error("Error de solicitud inválida: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now());
        return ResponseEntity.badRequest().body(error);
    }
    

}
