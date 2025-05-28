package org.project.group5.gamelend.entity; // Asegúrate que este sea tu paquete de entidades

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob; // Para el campo byte[] image si lo usas
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name; // Nombre descriptivo o dado por el usuario

    @Column(name = "file_name", nullable = false, length = 255, unique = true)
    private String fileName; // Nombre del archivo físico almacenado (ej. UUID.jpg)

    @Column(name = "content_type", length = 100) // <-- AÑADIDO/ASEGURADO
    private String contentType; // ej. "image/jpeg", "image/png"

    @Column(length = 10)
    private String extension;

    private Long size;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    @Column(length = 10)
    private String status; // Ej: "A" (Activo), "D" (Eliminado)

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "local_path", length = 512) // Ruta relativa donde se guarda, ej. "users/uuid.jpg"
    private String localPath;

    @Lob
    @Column(name = "image_data", columnDefinition = "LONGBLOB") // Si decides guardar la imagen en la BD
    private byte[] image; // Contenido binario del archivo (opcional si guardas en sistema de archivos)

    /**
     * Devuelve el nombre completo del archivo incluyendo su extensión,
     * útil para la cabecera Content-Disposition al descargar.
     * Si el 'name' original no tiene extensión, usa la almacenada.
     */
    public String getOriginalFileNameForDownload() {
        if (this.name != null && this.name.contains(".")) {
            return this.name; // Si 'name' ya tiene una extensión, úsalo.
        } else if (this.name != null && this.extension != null && !this.extension.isBlank()) {
            // Si 'name' no tiene extensión pero 'extension' sí, combínalos.
            return this.name + "." + this.extension;
        } else if (this.fileName != null) {
            // Fallback al nombre de archivo almacenado (que debería tener extensión).
            return this.fileName;
        }
        // Último fallback si todo lo demás falla.
        return "downloaded_file" + (this.extension != null && !this.extension.isBlank() ? "." + this.extension : "");
    }
}
