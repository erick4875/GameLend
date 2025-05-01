package org.project.group5.gamelend.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.project.group5.gamelend.dto.GameSummaryDTO;
import org.project.group5.gamelend.dto.LoanDTO;
import org.project.group5.gamelend.dto.LoanResponseDTO;
import org.project.group5.gamelend.dto.LoanReturnDTO;
import org.project.group5.gamelend.dto.UserSummaryDTO;
import org.project.group5.gamelend.entity.Game;
import org.project.group5.gamelend.entity.Loan;
import org.project.group5.gamelend.entity.User;

@Mapper(componentModel = "spring")
public interface LoanMapper {
    
    // ====== MÉTODOS DE ENTITY A DTO =======
    
    /**
     * Convierte un Loan a un LoanResponseDTO completo con información de relaciones
     */
    @Mapping(target = "gameId", source = "game.id")
    @Mapping(target = "gameTitle", source = "game.title")
    @Mapping(target = "borrowerId", source = "borrower.id")
    @Mapping(target = "borrowerName", source = "borrower.publicName")
    @Mapping(target = "loanDate", source = "loanDate", qualifiedByName = "formatDateTime")
    @Mapping(target = "expectedReturnDate", source = "expectedReturnDate", qualifiedByName = "formatDateTime")
    @Mapping(target = "returnDate", source = "returnDate", qualifiedByName = "formatDateTime")
    @Mapping(target = "status", expression = "java(loan.getReturnDate() == null ? \"ACTIVE\" : \"RETURNED\")")
    @Mapping(target = "notes", source = "notes")
    @Mapping(target = "game", source = "game", qualifiedByName = "toGameSummary")
    @Mapping(target = "lender", source = "lender", qualifiedByName = "toUserSummary")
    @Mapping(target = "borrower", source = "borrower", qualifiedByName = "toUserSummary")
    LoanResponseDTO toResponseDTO(Loan loan);
    
    /**
     * Convierte una lista de Loan a lista de LoanResponseDTO
     */
    List<LoanResponseDTO> toResponseDTOList(List<Loan> loans);
    
    // ====== MÉTODOS DE DTO A ENTITY =======
    
    /**
     * Crea un préstamo desde un DTO de creación
     * Los objetos relacionales (game, users) deben establecerse externamente
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "game", ignore = true)
    @Mapping(target = "lender", ignore = true)
    @Mapping(target = "borrower", ignore = true)
    @Mapping(target = "loanDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "returnDate", ignore = true)
    @Mapping(target = "expectedReturnDate", source = "expectedReturnDate")
    @Mapping(target = "notes", source = "notes")
    Loan toEntity(LoanDTO dto);
    
    /**
     * Actualiza un préstamo con la información de devolución
     */
    @Mapping(target = "returnDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "game", ignore = true)
    @Mapping(target = "lender", ignore = true)
    @Mapping(target = "borrower", ignore = true)
    @Mapping(target = "loanDate", ignore = true)
    @Mapping(target = "expectedReturnDate", ignore = true)
    @Mapping(target = "notes", ignore = true)
    void updateLoanWithReturn(LoanReturnDTO returnDTO, @MappingTarget Loan loan);
    
    // ====== MÉTODOS AUXILIARES =======
    
    /**
     * Formatea una fecha LocalDateTime a String con formato legible
     */
    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    /**
     * Método auxiliar para obtener el id de un Game de forma segura
     */
    default Long gameToId(Game game) {
        return game != null ? game.getId() : null;
    }
    
    /**
     * Método auxiliar para obtener el id de un User de forma segura
     */
    default Long userToId(User user) {
        return user != null ? user.getId() : null;
    }
    
    /**
     * Método auxiliar para obtener el nombre de un User de forma segura
     */
    default String userToName(User user) {
        return user != null ? user.getPublicName() : null;
    }
    
    @Named("toGameSummary")
    default GameSummaryDTO toGameSummary(Game game) {
        if (game == null) {
            return null;
        }
        return GameSummaryDTO.builder()
                .id(game.getId())
                .title(game.getTitle())
                .platform(game.getPlatform())
                .status(game.getStatus() != null ? game.getStatus().toString() : null)
                .build();
    }

    @Named("toUserSummary")
    default UserSummaryDTO toUserSummary(User user) {
        if (user == null) {
            return null;
        }
        return UserSummaryDTO.builder()
                .id(user.getId())
                .publicName(user.getPublicName())
                .build();
    }
}
