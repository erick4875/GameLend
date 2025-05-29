package org.project.group5.gamelend.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.project.group5.gamelend.dto.GameSummaryDTO;
import org.project.group5.gamelend.dto.LoanDTO;
import org.project.group5.gamelend.dto.LoanRequestDTO;
import org.project.group5.gamelend.dto.LoanResponseDTO;
import org.project.group5.gamelend.dto.LoanReturnDTO;
import org.project.group5.gamelend.dto.UserSummaryDTO;
import org.project.group5.gamelend.entity.Game;
import org.project.group5.gamelend.entity.Loan;
import org.project.group5.gamelend.entity.User;

/**
 * Mapper para conversiones entre Loan y sus DTOs usando MapStruct.
 */
@Mapper(componentModel = "spring", uses = {GameMapper.class, UserMapper.class})
public interface LoanMapper {

    /**
     * Convierte un Loan a LoanResponseDTO
     */
    @Mapping(target = "game", source = "game", qualifiedByName = "gameToGameSummaryDTO")
    @Mapping(target = "lender", source = "lender", qualifiedByName = "userToUserSummaryDTO")
    @Mapping(target = "borrower", source = "borrower", qualifiedByName = "userToUserSummaryDTO")
    LoanResponseDTO toResponseDTO(Loan loan);

    /**
     * Convierte una lista de Loan a lista de LoanResponseDTO
     */
    List<LoanResponseDTO> toResponseDTOList(List<Loan> loans);

    /**
     * Convierte LoanDTO a Loan
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "game", ignore = true)
    @Mapping(target = "lender", ignore = true)
    @Mapping(target = "borrower", ignore = true)
    Loan toEntity(LoanDTO dto);

    /**
     * Convierte LoanRequestDTO a Loan
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "game.id", source = "gameId")
    @Mapping(target = "lender", ignore = true)
    @Mapping(target = "borrower", ignore = true)
    @Mapping(target = "loanDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "expectedReturnDate", expression = "java(java.time.LocalDateTime.now().plusWeeks(2))")
    @Mapping(target = "returnDate", ignore = true)
    Loan toEntity(LoanRequestDTO dto);

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
    @Mapping(target = "returnDate", source = "returnDate")
    void updateLoanWithReturnInfo(LoanReturnDTO returnDTO, @MappingTarget Loan loan);

    /**
     * Método de ayuda para convertir un Game a GameSummaryDTO
     */
    @Named("gameToGameSummaryDTO")
    default GameSummaryDTO gameToGameSummaryDTO(Game game) {
        if (game == null) {
            return null;
        }
        return new GameSummaryDTO(
                game.getId(),
                game.getTitle(),
                game.getPlatform(),
                game.getStatus());
    }

    /**
     * Método de ayuda para convertir un User a UserSummaryDTO
     */
    @Named("userToUserSummaryDTO")
    default UserSummaryDTO userToUserSummaryDTO(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryDTO(
                user.getId(),
                user.getPublicName());
    }
}
