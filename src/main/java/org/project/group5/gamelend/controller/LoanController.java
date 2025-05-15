package org.project.group5.gamelend.controller;

import java.util.List;

import org.project.group5.gamelend.dto.LoanDTO;
import org.project.group5.gamelend.dto.LoanResponseDTO;
import org.project.group5.gamelend.dto.LoanReturnDTO;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.service.LoanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Slf4j: Lombok para logging.
 * @RestController: Define esta clase como un controlador REST.
 * @RequestMapping("api/loans"): Ruta base para los endpoints de préstamos.
 * @RequiredArgsConstructor: Lombok para inyección de dependencias (LoanService).
 *
 * Controlador para gestionar las operaciones CRUD de préstamos.
 */
@Slf4j
@RestController
@RequestMapping("api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService; // Servicio para la lógica de préstamos.

    /**
     * Crea un nuevo préstamo.
     * POST /api/loans
     * @param loanDTO Datos para crear el préstamo.
     * @return Préstamo creado y estado 201 (CREATED).
     */
    @PostMapping
    public ResponseEntity<LoanResponseDTO> createLoan(@RequestBody LoanDTO loanDTO) {
        log.info("POST /api/loans - Creando préstamo: {}", loanDTO);
        if (loanDTO == null) {
            throw new BadRequestException("Datos del préstamo no pueden ser nulos.");
        }
        LoanResponseDTO createdLoan = loanService.createLoanFromDTO(loanDTO);
        log.info("Préstamo creado con ID: {}", createdLoan.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLoan);
    }

    /**
     * Obtiene todos los préstamos.
     * GET /api/loans
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
     * @param id ID del préstamo a buscar.
     * @return Préstamo encontrado y estado 200 (OK).
     */
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponseDTO> getLoanById(@PathVariable Long id) {
        log.info("GET /api/loans/{} - Buscando préstamo.", id);
        if (id == null) {
            throw new BadRequestException("ID de préstamo no puede ser nulo.");
        }
        LoanResponseDTO loan = loanService.getLoanByIdDTO(id);
        return ResponseEntity.ok(loan);
    }

    /**
     * Actualiza un préstamo existente.
     * PUT /api/loans/{id}
     * @param id ID del préstamo a actualizar.
     * @param loanDTO Nuevos datos para el préstamo.
     * @return Préstamo actualizado y estado 200 (OK).
     */
    @PutMapping("/{id}")
    public ResponseEntity<LoanResponseDTO> updateLoan(
            @PathVariable Long id, @RequestBody LoanDTO loanDTO) {
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
     * PUT /api/loans/{id}/return
     * @param id ID del préstamo a devolver.
     * @param returnDTO Datos de la devolución.
     * @return Préstamo actualizado (con fecha de devolución) y estado 200 (OK).
     */
    @PutMapping("/{id}/return")
    public ResponseEntity<LoanResponseDTO> returnLoan(
            @PathVariable Long id, @RequestBody LoanReturnDTO returnDTO) {
        log.info("PUT /api/loans/{}/return - Registrando devolución: {}", id, returnDTO);
        if (id == null || returnDTO == null) {
            throw new BadRequestException("ID de préstamo y datos de devolución no pueden ser nulos.");
        }
        LoanResponseDTO returnedLoan = loanService.recordLoanReturn(id, returnDTO);
        log.info("Préstamo ID {} devuelto.", returnedLoan.id());
        return ResponseEntity.ok(returnedLoan);
    }
}
