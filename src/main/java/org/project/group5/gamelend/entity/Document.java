package org.project.group5.gamelend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un documento o archivo en el sistema.
 * Puede almacenar tanto metadatos como contenido binario.
 */
@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    /**
     * Identificador único del documento
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre descriptivo del documento dado por el usuario
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Nombre único del archivo en el sistema
     * Generalmente un UUID con extensión
     */
    @Column(name = "file_name", nullable = false, length = 255, unique = true)
    private String fileName;

    /**
     * Tipo MIME del contenido
     * Ejemplos: image/jpeg, image/png
     */
    @Column(name = "content_type", length = 100)
    private String contentType;

    /**
     * Extensión del archivo (sin punto)
     * Ejemplos: jpg, png, pdf
     */
    @Column(length = 10)
    private String extension;

    /**
     * Tamaño del archivo en bytes
     */
    private Long size;

    /**
     * Fecha y hora de subida del documento
     */
    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    /**
     * Estado del documento
     * A = Activo, D = Eliminado
     */
    @Column(length = 10)
    private String status;

    /**
     * Indicador de borrado lógico
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    /**
     * Ruta relativa del archivo en el sistema
     * Ejemplo: users/123/avatar.jpg
     */
    @Column(name = "local_path", length = 512)
    private String localPath;

    /**
     * Contenido binario del archivo
     * Solo se usa si se almacena en BD
     */
    @Lob
    @Column(name = "image_data", columnDefinition = "LONGBLOB")
    private byte[] image;

    /**
     * Obtiene el nombre completo del archivo para descarga
     * @return nombre del archivo con su extensión
     */
    public String getOriginalFileNameForDownload() {
        if (this.name != null && this.name.contains(".")) {
            return this.name;
        } else if (this.name != null && this.extension != null && !this.extension.isBlank()) {
            return this.name + "." + this.extension;
        } else if (this.fileName != null) {
            return this.fileName;
        }
        return "downloaded_file" + (this.extension != null && !this.extension.isBlank() ? "." + this.extension : "");
    }
}
