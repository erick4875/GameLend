package org.project.group5.gamelend.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class UserResponseDTO {
    private Long id;
    private String publicName;
    private String email;
    private String registrationDate;
    private String province;
    private String city;

    @Builder.Default
    private List<GameResponseDTO> games = new ArrayList<>();

    @Builder.Default
    private List<GameSummaryDTO> gamesLent = new ArrayList<>();

    @Builder.Default
    private List<String> roles = new ArrayList<>();
}
