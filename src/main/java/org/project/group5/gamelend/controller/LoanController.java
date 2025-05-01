package org.project.group5.gamelend.controller;

import java.util.List;

import org.project.group5.gamelend.dto.LoanDTO;
import org.project.group5.gamelend.dto.LoanResponseDTO;
import org.project.group5.gamelend.dto.LoanReturnDTO;
import org.project.group5.gamelend.entity.Game;
import org.project.group5.gamelend.entity.Loan;
import org.project.group5.gamelend.entity.User;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.mapper.LoanMapper;
import org.project.group5.gamelend.service.GameService;
import org.project.group5.gamelend.service.LoanService;
import org.project.group5.gamelend.service.UserService;
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

@Slf4j
@RestController
@RequestMapping("api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final UserService userService;
    private final GameService gameService;
    private final LoanMapper loanMapper;
    
    /**
     * Crea un nuevo préstamo
     */
    @PostMapping
    public ResponseEntity<LoanResponseDTO> createLoan(@RequestBody LoanDTO loanDTO) {
        log.info("Creando nuevo préstamo");
        
        if (loanDTO == null) {
            throw new BadRequestException("Los datos del préstamo no pueden ser nulos");
        }
        
        Game game = gameService.findById(loanDTO.getGameId());
        User borrower = userService.getUserById(loanDTO.getBorrowerId());
        User lender = game.getUser();
        
        // Usar el mapper para crear la entidad
        Loan loan = loanMapper.toEntity(loanDTO);
        loan.setGame(game);
        loan.setBorrower(borrower);
        loan.setLender(lender);
        
        Loan savedLoan = loanService.saveLoan(loan);
        
        log.info("Préstamo creado correctamente con ID: {}", savedLoan.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(loanMapper.toResponseDTO(savedLoan));
    }
    
    /**
     * Obtiene todos los préstamos
     */
    @GetMapping
    public ResponseEntity<List<LoanResponseDTO>> getAllLoans() {
        log.info("Solicitando lista de préstamos");
        
        List<Loan> loans = loanService.getAllLoans();
        if (loans.isEmpty()) {
            log.info("No se encontraron préstamos");
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(loanMapper.toResponseDTOList(loans));
    }
    
    /**
     * Obtiene un préstamo por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponseDTO> getLoanById(@PathVariable Long id) {
        log.info("Buscando préstamo con ID: {}", id);
        
        if (id == null) {
            throw new BadRequestException("ID de préstamo no puede ser nulo");
        }
        
        Loan loan = loanService.getLoanById(id);
        return ResponseEntity.ok(loanMapper.toResponseDTO(loan));
    }
    
    /**
     * Actualiza un préstamo existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<LoanResponseDTO> updateLoan(
            @PathVariable Long id, @RequestBody LoanDTO loanDTO) {
        log.info("Actualizando préstamo con ID: {}", id);
        
        if (id == null || loanDTO == null) {
            throw new BadRequestException("ID y datos del préstamo no pueden ser nulos");
        }
        
        Game game = gameService.findById(loanDTO.getGameId());
        User borrower = userService.getUserById(loanDTO.getBorrowerId());
        Loan existingLoan = loanService.getLoanById(id);
        
        // Usar el mapper con datos actualizados
        Loan loan = loanMapper.toEntity(loanDTO);
        loan.setId(id);
        loan.setGame(game);
        loan.setBorrower(borrower);
        loan.setLender(existingLoan.getLender());
        
        Loan updatedLoan = loanService.updateLoan(id, loan);
        
        log.info("Préstamo actualizado correctamente con ID: {}", updatedLoan.getId());
        return ResponseEntity.ok(loanMapper.toResponseDTO(updatedLoan));
    }
    
    /**
     * Elimina un préstamo por su ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        log.info("Eliminando préstamo con ID: {}", id);
        
        if (id == null) {
            throw new BadRequestException("ID de préstamo no puede ser nulo");
        }
        
        loanService.deleteLoan(id);
        log.info("Préstamo con ID {} eliminado correctamente", id);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Registra la devolución de un préstamo
     */
    @PutMapping("/{id}/return")
    public ResponseEntity<LoanResponseDTO> returnLoan(
            @PathVariable Long id, @RequestBody LoanReturnDTO returnDTO) {
        log.info("Registrando devolución del préstamo con ID: {}", id);
        
        if (id == null) {
            throw new BadRequestException("ID de préstamo no puede ser nulo");
        }
        
        Loan loan = loanService.getLoanById(id);
        
        // Usar el mapper para actualizar la fecha de devolución
        loanMapper.updateLoanWithReturn(returnDTO, loan);
        
        Loan returnedLoan = loanService.returnLoanWithGameUpdate(loan);
        
        log.info("Préstamo con ID {} devuelto correctamente", id);
        return ResponseEntity.ok(loanMapper.toResponseDTO(returnedLoan));
    }
}
