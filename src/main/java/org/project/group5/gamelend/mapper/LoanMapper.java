package org.project.group5.gamelend.mapper;

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

/**
 * Mapper para conversiones entre Loan y sus DTOs usando MapStruct.
 */
@Mapper(componentModel = "spring")
public interface LoanMapper {

    /**
     * Convierte un préstamo (Loan) a su DTO de respuesta.
     */
    @Mapping(target = "game", source = "game", qualifiedByName = "loanEntityToGameSummary")
    @Mapping(target = "lender", source = "lender", qualifiedByName = "loanEntityToUserSummary")
    @Mapping(target = "borrower", source = "borrower", qualifiedByName = "loanEntityToUserSummary")
    LoanResponseDTO toResponseDTO(Loan loan);

    /**
     * Convierte una lista de préstamos a una lista de DTOs de respuesta.
     */
    List<LoanResponseDTO> toResponseDTOList(List<Loan> loans);

    /**
     * Convierte un LoanDTO a la entidad Loan para creación.
     * Las relaciones (game, lender, borrower) deben establecerse en el servicio.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "game", ignore = true)
    @Mapping(target = "lender", ignore = true)
    @Mapping(target = "borrower", ignore = true)
    @Mapping(target = "returnDate", ignore = true)
    Loan toEntity(LoanDTO dto);

    /**
     * Actualiza una entidad Loan con la información de devolución.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "game", ignore = true)
    @Mapping(target = "lender", ignore = true)
    @Mapping(target = "borrower", ignore = true)
    @Mapping(target = "loanDate", ignore = true)
    @Mapping(target = "expectedReturnDate", ignore = true)
    @Mapping(target = "notes", ignore = true)
    void updateLoanWithReturnInfo(LoanReturnDTO returnDTO, @MappingTarget Loan loan);

    /**
     * Convierte un Game a GameSummaryDTO.
     */
    @Named("loanEntityToGameSummary")
    default GameSummaryDTO entityToGameSummary(Game game) {
        if (game == null) return null;
        return new GameSummaryDTO(
                game.getId(),
                game.getTitle(),
                game.getPlatform(),
                game.getStatus()
        );
    }

    /**
     * Convierte un User a UserSummaryDTO.
     */
    @Named("loanEntityToUserSummary")
    default UserSummaryDTO entityToUserSummary(User user) {
        if (user == null) return null;
        return new UserSummaryDTO(
                user.getId(),
                user.getPublicName()
        );
    }
}
