package org.project.group5.gamelend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Clase para representar un objeto de transferencia de datos (DTO) de resumen de usuario
// Se utiliza para enviar datos de usuario a través de la API
// salida de datos (servidor -> cliente)

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDTO {
    private Long id;
    private String publicName;

}