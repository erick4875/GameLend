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

/**
 * Mapper para conversiones entre Loan y sus DTOs.
 * Utiliza MapStruct.
 */
@Mapper(componentModel = "spring")
public interface LoanMapper {

    DateTimeFormatter DTO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // ====== ENTITY -> DTO ======

    /**
     * Convierte Loan (entidad) a LoanResponseDTO.
     */
    @Mapping(target = "loanDate", source = "loanDate", qualifiedByName = "formatDateTimeToString")
    @Mapping(target = "expectedReturnDate", source = "expectedReturnDate", qualifiedByName = "formatDateTimeToString")
    @Mapping(target = "returnDate", source = "returnDate", qualifiedByName = "formatDateTimeToString")
    @Mapping(target = "game", source = "game", qualifiedByName = "loanEntityToGameSummary")
    @Mapping(target = "lender", source = "lender", qualifiedByName = "loanEntityToUserSummary")
    @Mapping(target = "borrower", source = "borrower", qualifiedByName = "loanEntityToUserSummary")
    LoanResponseDTO toResponseDTO(Loan loan);

    /**
     * Convierte una lista de Loan (entidad) a lista de LoanResponseDTO.
     */
    List<LoanResponseDTO> toResponseDTOList(List<Loan> loans);

    // ====== DTO -> ENTITY ======

    /**
     * Convierte LoanDTO a Loan (entidad) para creación.
     * IDs de relaciones (game, lender, borrower) deben establecerse en el servicio.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "game", ignore = true)
    @Mapping(target = "lender", ignore = true)
    @Mapping(target = "borrower", ignore = true)
    @Mapping(target = "loanDate", expression = "java(dto.getLoanDateAsDateTime() != null ? dto.getLoanDateAsDateTime() : java.time.LocalDateTime.now())")
    @Mapping(target = "returnDate", ignore = true)
    @Mapping(target = "expectedReturnDate", expression = "java(dto.getExpectedReturnDateAsDateTime())")
    Loan toEntity(LoanDTO dto);

    /**
     * Actualiza una entidad Loan con información de LoanReturnDTO.
     */
    @Mapping(target = "returnDate", expression = "java(returnDTO.getReturnDateAsDateTime())")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "game", ignore = true)
    @Mapping(target = "lender", ignore = true)
    @Mapping(target = "borrower", ignore = true)
    @Mapping(target = "loanDate", ignore = true)
    @Mapping(target = "expectedReturnDate", ignore = true)
    @Mapping(target = "notes", ignore = true)
    void updateLoanWithReturnInfo(LoanReturnDTO returnDTO, @MappingTarget Loan loan);

    // ====== MÉTODOS AUXILIARES (@Named) ======

    /**
     * Formatea LocalDateTime a String (yyyy-MM-dd'T'HH:mm:ss).
     */
    @Named("formatDateTimeToString")
    default String formatDateTimeToString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DTO_DATE_FORMATTER);
    }

    /**
     * Convierte entidad Game a GameSummaryDTO.
     */
    @Named("loanEntityToGameSummary")
    default GameSummaryDTO entityToGameSummary(Game game) {
        if (game == null) {
            return null;
        }
        // GameSummaryDTO espera: Long id, String title, String platform, GameStatus status
        return new GameSummaryDTO(
                game.getId(),
                game.getTitle(),
                game.getPlatform(), 
                game.getStatus()
        );
    }

    /**
     * Convierte entidad User a UserSummaryDTO.
     */
    @Named("loanEntityToUserSummary")
    default UserSummaryDTO entityToUserSummary(User user) {
        if (user == null) {
            return null;
        }
        // UserSummaryDTO espera: Long id, String publicName
        return new UserSummaryDTO(
                user.getId(),
                user.getPublicName()
        );
    }
}
