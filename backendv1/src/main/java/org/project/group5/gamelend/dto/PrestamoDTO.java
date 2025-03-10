package org.project.group5.gamelend.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.project.group5.gamelend.entity.Juego;
import org.project.group5.gamelend.entity.Prestamo;
import org.project.group5.gamelend.entity.Usuario;

// Clase para representar un objeto de transferencia de datos (DTO) de préstamo
// Se utiliza para recibir datos de préstamo a través de la API
// entrada de datos (cliente -> servidor)

public class PrestamoDTO {
    private Long id;
    private Long juegoId;
    private Long usuarioPrestadorId;
    private Long usuarioReceptorId;
    private String fechaPrestamo;
    private String fechaDevolucion;

    // Formato estándar para fechas
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // Constructor vacío
    public PrestamoDTO() {
    }

    // Constructor completo
    public PrestamoDTO(Long id, Long juegoId, Long usuarioPrestadorId, Long usuarioReceptorId,
            String fechaPrestamo, String fechaDevolucion) {
        this.id = id;
        this.juegoId = juegoId;
        this.usuarioPrestadorId = usuarioPrestadorId;
        this.usuarioReceptorId = usuarioReceptorId;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
    }

    // Método para convertir a entidad
    public Prestamo toEntity() throws DateTimeParseException {
        Prestamo prestamo = new Prestamo();

        // Solo asignar ID si no es nulo
        if (this.id != null) {
            prestamo.setId(this.id);
        }

        // Para estas relaciones, solo asignamos el ID y dejamos que JPA cargue la
        // entidad completa
        if (this.juegoId != null) {
            Juego juego = new Juego();
            juego.setId(this.juegoId);
            prestamo.setJuego(juego);
        }

        if (this.usuarioPrestadorId != null) {
            Usuario prestador = new Usuario();
            prestador.setId(this.usuarioPrestadorId);
            // Usar setUsuario para mantener consistencia
            prestamo.setUsuario(prestador);
        }

        if (this.usuarioReceptorId != null) {
            Usuario receptor = new Usuario();
            receptor.setId(this.usuarioReceptorId);
            prestamo.setUsuarioReceptor(receptor);
        }

        // Convertir String a LocalDateTime
        if (this.fechaPrestamo != null && !this.fechaPrestamo.isEmpty()) {
            prestamo.setFechaPrestamo(LocalDateTime.parse(this.fechaPrestamo, DATE_FORMATTER));
        }

        // Convertir String a LocalDateTime (solo si no es nulo)
        if (this.fechaDevolucion != null && !this.fechaDevolucion.isEmpty()) {
            prestamo.setFechaDevolucion(LocalDateTime.parse(this.fechaDevolucion, DATE_FORMATTER));
        }

        return prestamo;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJuegoId() {
        return juegoId;
    }

    public void setJuegoId(Long juegoId) {
        this.juegoId = juegoId;
    }

    public Long getUsuarioPrestadorId() {
        return usuarioPrestadorId;
    }

    public void setUsuarioPrestadorId(Long usuarioPrestadorId) {
        this.usuarioPrestadorId = usuarioPrestadorId;
    }

    public Long getUsuarioReceptorId() {
        return usuarioReceptorId;
    }

    public void setUsuarioReceptorId(Long usuarioReceptorId) {
        this.usuarioReceptorId = usuarioReceptorId;
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
}