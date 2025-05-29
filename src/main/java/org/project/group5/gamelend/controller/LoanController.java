package org.project.group5.gamelend.controller;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import org.project.group5.gamelend.dto.ErrorResponse;
import org.project.group5.gamelend.dto.LoanDTO;
import org.project.group5.gamelend.dto.LoanRequestDTO;
import org.project.group5.gamelend.dto.LoanResponseDTO;
import org.project.group5.gamelend.dto.LoanReturnDTO;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.service.LoanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Slf4j: Lombok para logging.
 * @RestController: Define esta clase como un controlador REST.
 *                  @RequestMapping("api/loans"): Ruta base para los endpoints
 *                  de préstamos.
 * @RequiredArgsConstructor: Lombok para inyección de dependencias
 *                           (LoanService).
 *
 *                           Controlador para gestionar las operaciones CRUD de
 *                           préstamos.
 */
@Slf4j
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Validated // Añadir esta anotación
@SecurityRequirement(name = "bearerAuth") // Si usas Swagger/OpenAPI
public class LoanController {
    private final LoanService loanService; // Servicio para la lógica de préstamos.

    /**
     * Crea un nuevo préstamo.
     * POST /api/loans
     * 
     * @param loanDTO Datos para crear el préstamo.
     * @return Préstamo creado y estado 201 (CREATED).
     */
    @Operation(summary = "Crear nuevo préstamo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Préstamo creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos"),
            @ApiResponse(responseCode = "409", description = "Juego no disponible")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<LoanResponseDTO> createLoan(@Valid @RequestBody LoanRequestDTO loanDTO) {
        log.info("POST /api/loans - Creando préstamo: {}", loanDTO);
        if (loanDTO == null) {
            throw new BadRequestException("Datos del préstamo no pueden ser nulos.");
        }
        LoanResponseDTO createdLoan = loanService.createLoan(loanDTO);
        log.info("Préstamo creado con ID: {}", createdLoan.id());
        return ResponseEntity.created(URI.create("/api/loans/" + createdLoan.id()))
                .body(createdLoan);
    }

    /**
     * Obtiene todos los préstamos.
     * GET /api/loans
     * 
     * @return Lista de préstamos y estado 200 (OK) o 204 (NO CONTENT).
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
     * Obtiene un préstamo por su ID.
     * GET /api/loans/{id}
     * 
     * @param id ID del préstamo a buscar.
     * @return Préstamo encontrado y estado 200 (OK).
     */
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponseDTO> getLoanById(@PathVariable Long id) {
        log.info("GET /api/loans/{} - Buscando préstamo.", id);
        if (id == null) {
            throw new BadRequestException("ID de préstamo no puede ser nulo.");
        }
        LoanResponseDTO loan = loanService.getLoanDTO(id);
        return ResponseEntity.ok(loan);
    }

    /**
     * Actualiza un préstamo existente.
     * PUT /api/loans/{id}
     * 
     * @param id      ID del préstamo a actualizar.
     * @param loanDTO Nuevos datos para el préstamo.
     * @return Préstamo actualizado y estado 200 (OK).
     */
    @PutMapping("/{id}")
    public ResponseEntity<LoanResponseDTO> updateLoan(
            @PathVariable Long id, @Valid @RequestBody LoanDTO loanDTO) {
        log.info("PUT /api/loans/{} - Actualizando préstamo con datos: {}", id, loanDTO);
        if (id == null || loanDTO == null) {
            throw new BadRequestException("ID y datos del préstamo no pueden ser nulos.");
        }
        LoanResponseDTO updatedLoan = loanService.updateLoanFromDTO(id, loanDTO);
        log.info("Préstamo ID {} actualizado.", updatedLoan.id());
        return ResponseEntity.ok(updatedLoan);
    }

    /**
     * Elimina un préstamo por su ID.
     * DELETE /api/loans/{id}
     * 
     * @param id ID del préstamo a eliminar.
     * @return Estado 204 (NO CONTENT).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        log.info("DELETE /api/loans/{} - Eliminando préstamo.", id);
        if (id == null) {
            throw new BadRequestException("ID de préstamo no puede ser nulo.");
        }
        loanService.deleteLoan(id);
        log.info("Préstamo ID {} eliminado.", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Registra la devolución de un préstamo.
     * PATCH /api/loans/{id}/return
     * 
     * @param id        ID del préstamo a devolver.
     * @param returnDTO Datos de la devolución.
     * @return Préstamo actualizado (con fecha de devolución) y estado 200 (OK).
     */
    @PatchMapping("/{id}/return")
    public ResponseEntity<LoanResponseDTO> returnLoan(
            @PathVariable Long id, @Valid @RequestBody LoanReturnDTO returnDTO) {
        log.info("PATCH /api/loans/{}/return - Registrando devolución: {}", id, returnDTO);
        if (id == null || returnDTO == null) {
            throw new BadRequestException("ID de préstamo y datos de devolución no pueden ser nulos.");
        }
        LoanResponseDTO returnedLoan = loanService.recordLoanReturn(id, returnDTO);
        log.info("Préstamo ID {} devuelto.", returnedLoan.id());
        return ResponseEntity.ok(returnedLoan);
    }

    /**
     * Maneja excepciones de tipo BadRequestException.
     * 
     * @param ex La excepción lanzada.
     * @return Respuesta con detalles del error y estado 400 (BAD REQUEST).
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
