package org.project.group5.gamelend.dto;

import java.time.format.DateTimeFormatter;

import org.project.group5.gamelend.entity.Prestamo;

// Clase para representar un objeto de transferencia de datos (DTO) de respuesta de préstamo
// Se utiliza para enviar datos de préstamo a través de la API
// salida de datos (servidor -> cliente)

public class PrestamoResponseDTO {
    private Long id;
    private JuegoResumenDTO juego;
    private UsuarioResumenDTO prestador;
    private UsuarioResumenDTO receptor;
    private String fechaPrestamo;
    private String fechaDevolucion;

    // Constante para formato de fecha
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // Constructor vacío
    public PrestamoResponseDTO() {
    }

    // Constructor completo para facilitar la creación de instancias
    public PrestamoResponseDTO(Long id, JuegoResumenDTO juego, UsuarioResumenDTO prestador,
            UsuarioResumenDTO receptor, String fechaPrestamo, String fechaDevolucion) {
        this.id = id;
        this.juego = juego;
        this.prestador = prestador;
        this.receptor = receptor;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
    }

    // Método de conversión
    public static PrestamoResponseDTO fromEntity(Prestamo prestamo) {
        if (prestamo == null)
            return null;

        PrestamoResponseDTO dto = new PrestamoResponseDTO();
        dto.setId(prestamo.getId());

        // Convertir juego usando JuegoResumenDTO
        if (prestamo.getJuego() != null) {
            dto.setJuego(JuegoResumenDTO.fromEntity(prestamo.getJuego()));
        }

        // Usar la clase UsuarioResumenDTO independiente
        // Cambiado de getUsuarioPrestador() a getUsuario() para consistencia
        if (prestamo.getUsuario() != null) {
            dto.setPrestador(UsuarioResumenDTO.fromEntity(prestamo.getUsuario()));
        }

        if (prestamo.getUsuarioReceptor() != null) {
            dto.setReceptor(UsuarioResumenDTO.fromEntity(prestamo.getUsuarioReceptor()));
        }

        // Conversión segura de fechas
        if (prestamo.getFechaPrestamo() != null) {
            dto.setFechaPrestamo(prestamo.getFechaPrestamo().format(DATE_FORMATTER));
        }

        if (prestamo.getFechaDevolucion() != null) {
            dto.setFechaDevolucion(prestamo.getFechaDevolucion().format(DATE_FORMATTER));
        }

        return dto;
    }

    // Getters y setters corregidos
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JuegoResumenDTO getJuego() {
        return juego;
    }

    public void setJuego(JuegoResumenDTO juego) {
        this.juego = juego;
    }

    public UsuarioResumenDTO getPrestador() {
        return prestador;
    }

    public void setPrestador(UsuarioResumenDTO prestador) {
        this.prestador = prestador;
    }

    public UsuarioResumenDTO getReceptor() {
        return receptor;
    }

    public void setReceptor(UsuarioResumenDTO receptor) {
        this.receptor = receptor;
    }

    public String getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(String fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public String getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(String fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    // La clase interna UsuarioResumenDTO se elimina porque ya existe como clase
    // independiente
}