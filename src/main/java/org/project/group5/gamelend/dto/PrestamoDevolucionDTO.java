package org.project.group5.gamelend.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.project.group5.gamelend.entity.Prestamo;

/**
 * DTO específico para registrar la devolución de un préstamo.
 * Se usa cuando un usuario devuelve un juego prestado.
 * Solo contiene la fecha de devolución, ya que es el único dato necesario
 * para completar un préstamo.
 */
public class PrestamoDevolucionDTO {
    // Campo como String para mantener consistencia con otros DTOs
    private String fechaDevolucion;

    // Formato estándar compartido con otros DTOs
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public PrestamoDevolucionDTO() {
    }

    public PrestamoDevolucionDTO(String fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    // Sobrecarga del constructor para aceptar LocalDateTime
    public PrestamoDevolucionDTO(LocalDateTime fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion != null ? fechaDevolucion.format(DATE_FORMATTER) : null;
    }

    // Getters y setters con conversiones
    public String getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(String fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    // Método de utilidad para obtener la fecha como LocalDateTime
    public LocalDateTime getFechaDevolucionAsDateTime() throws DateTimeParseException {
        return fechaDevolucion != null && !fechaDevolucion.isEmpty()
                ? LocalDateTime.parse(fechaDevolucion, DATE_FORMATTER)
                : null;
    }

    // Método para establecer la fecha desde un LocalDateTime
    public void setFechaDevolucion(LocalDateTime fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion != null
                ? fechaDevolucion.format(DATE_FORMATTER)
                : null;
    }

    // Método para aplicar esta devolución a un préstamo existente
    public void aplicarA(Prestamo prestamo) throws DateTimeParseException {
        if (prestamo != null && this.fechaDevolucion != null && !this.fechaDevolucion.isEmpty()) {
            prestamo.setFechaDevolucion(LocalDateTime.parse(this.fechaDevolucion, DATE_FORMATTER));
        }
    }
}