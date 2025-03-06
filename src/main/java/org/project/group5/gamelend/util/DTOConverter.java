package org.project.group5.gamelend.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.project.group5.gamelend.dto.*;
import org.project.group5.gamelend.entity.*;

/**
 * Clase utilitaria para convertir entidades a DTOs y viceversa
 */
public class DTOConverter {

    // Constante para formateo de fechas
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Convierte una entidad Prestamo a un PrestamoResponseDTO
     * 
     * @param prestamo La entidad a convertir
     * @return DTO con la información del préstamo, o null si la entidad es null
     */
    public static PrestamoResponseDTO convertToPrestamoResponseDTO(Prestamo prestamo) {
        if (prestamo == null) return null;

        PrestamoResponseDTO dto = new PrestamoResponseDTO();
        dto.setId(prestamo.getId());
        
        // Convertir juego
        if (prestamo.getJuego() != null) {
            dto.setJuego(convertToJuegoResumenDTO(prestamo.getJuego()));
        }
        
        // Convertir usuario prestador
        if (prestamo.getUsuario() != null) {
            dto.setPrestador(UsuarioResumenDTO.fromEntity(prestamo.getUsuario()));
        }
        
        // Convertir usuario receptor
        if (prestamo.getUsuarioReceptor() != null) {
            dto.setReceptor(UsuarioResumenDTO.fromEntity(prestamo.getUsuarioReceptor()));
        }
        
        // Convertir fechas
        if (prestamo.getFechaPrestamo() != null) {
            dto.setFechaPrestamo(prestamo.getFechaPrestamo().format(DATE_FORMATTER));
        }
        
        if (prestamo.getFechaDevolucion() != null) {
            dto.setFechaDevolucion(prestamo.getFechaDevolucion().format(DATE_FORMATTER));
        }

        return dto;
    }

    /**
     * Convierte una entidad Juego a un JuegoResponseDTO
     * 
     * @param juego La entidad a convertir
     * @return DTO con la información del juego, o null si la entidad es null
     */
    public static JuegoResponseDTO convertToJuegoResponseDTO(Juego juego) {
        if (juego == null) return null;
        
        JuegoResponseDTO dto = new JuegoResponseDTO();
        dto.setId(juego.getId());
        dto.setTitulo(juego.getTitulo());
        dto.setPlataforma(juego.getPlataforma());
        dto.setGenero(juego.getGenero());
        dto.setDescripcion(juego.getDescripcion());
        
        // Convertir enum a string si existe
        if (juego.getEstado() != null) {
            dto.setEstado(juego.getEstado());
        }

        // Convertir propietario si existe
        if (juego.getUsuario() != null) {
            dto.setUsuarioId(juego.getUsuario().getId());
            dto.setUsuarioNombre(juego.getUsuario().getNombrePublico());
        }
        
        return dto;
    }
            
    /**
     * Convierte una entidad Usuario a un UsuarioResponseDTO
     * 
     * @param usuario La entidad a convertir
     * @return DTO con la información del usuario, o null si la entidad es null
     */
    public static UsuarioResponseDTO convertToUsuarioResponseDTO(Usuario usuario) {
        if (usuario == null) return null;

        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        
        // El resto del método está bien
        dto.setNombrePublico(usuario.getNombrePublico());
        dto.setEmail(usuario.getEmail());
        dto.setProvincia(usuario.getProvincia());
        dto.setLocalidad(usuario.getLocalidad());
        
        // Usar el método adecuado para establecer la fecha
        if (usuario.getFechaRegistro() != null) {
            dto.setFechaRegistro(usuario.getFechaRegistro());  // Este método acepta LocalDateTime
        }
        
        return dto;
    }

    /**
     * Convierte una lista de préstamos a DTOs
     * 
     * @param prestamos Lista de entidades a convertir
     * @return Lista de DTOs, o lista vacía si la entrada es null
     */
    public static List<PrestamoResponseDTO> convertToPrestamoDTOList(List<Prestamo> prestamos) {
        if (prestamos == null) return Collections.emptyList();

        return prestamos.stream()
                .map(DTOConverter::convertToPrestamoResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una lista de juegos a DTOs
     * 
     * @param juegos Lista de entidades a convertir
     * @return Lista de DTOs, o lista vacía si la entrada es null
     */
    public static List<JuegoResponseDTO> convertToJuegoDTOList(List<Juego> juegos) {
        if (juegos == null) return Collections.emptyList();
        
        return juegos.stream()
                .map(DTOConverter::convertToJuegoResponseDTO)
                .collect(Collectors.toList());
    }
                
    /**
     * Convierte una lista de usuarios a DTOs
     * 
     * @param usuarios Lista de entidades a convertir
     * @return Lista de DTOs, o lista vacía si la entrada es null
     */
    public static List<UsuarioResponseDTO> convertToUsuarioDTOList(List<Usuario> usuarios) {
        if (usuarios == null) return Collections.emptyList();
                
        return usuarios.stream()
                .map(DTOConverter::convertToUsuarioResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una entidad Juego a un JuegoResumenDTO
     * 
     * @param juego La entidad a convertir
     * @return DTO resumido con información básica del juego, o null si la entidad es null
     */
    public static JuegoResumenDTO convertToJuegoResumenDTO(Juego juego) {
        if (juego == null) return null;
        return new JuegoResumenDTO(
            juego.getId(), 
            juego.getTitulo(), 
            juego.getPlataforma(), 
            juego.getEstado() != null ? juego.getEstado().name() : null
        );
    }
}