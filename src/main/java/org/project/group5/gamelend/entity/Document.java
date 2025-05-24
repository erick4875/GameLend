package org.project.group5.gamelend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un documento o archivo almacenado en el sistema
 * Puede ser una imagen de un juego o un avatar de usuario
 */
@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    /**
     * Identificador único del documento.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre descriptivo o alias del documento, proporcionado por el usuario o sistema
     */
    @Column(nullable = false, length = 100)
    @Builder.Default
    private String name = "";

    /**
     * Nombre del archivo tal como se almacena en el sistema de archivos o blob storage,
     */
    @Column(nullable = false, unique = true, length = 100)
    @Builder.Default
    private String fileName = "";

    /**
     * Ej: ".jpg", ".png". Incluye el punto
     */
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String extension = "";

    /**
     * Ej: 'A' (Activo), 'P' (Pendiente), 'I' (Inactivo), 'D' (Default/Draft)
     */
    @Column(nullable = false, length = 1)
    @Builder.Default
    private String status = "D"; // Default status, e.g., Draft or Default

    /**
     * true si el documento ha sido marcado como eliminado, false en caso contrario
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    /**
     * Contenido binario del archivo, típicamente para imágenes pequeñas almacenadas directamente en la BD
     * Para archivos grandes, es preferible usar localPath y almacenar el archivo externamente
     */
    @Lob
    @Column(columnDefinition="LONGBLOB") // Especificar tipo para bases de datos como MySQL
    private byte[] image;

    /**
     * Ruta local en el servidor donde se almacena el archivo físico, si no se guarda en la BD
     * Ej: "/var/www/uploads/images/uuid_del_archivo_zelda.jpg"
     */
    @Column(length = 255)
    private String localPath;

    /**
     * URL completa para acceder al archivo.
     * Este campo no se persiste en la base de datos, se calcula o se establece en tiempo de ejecución
     * Ej: "https://servidor.com/api/documents/download/uuid_del_archivo_zelda.jpg"
     */
    @Transient
    private String urlFile;

    /**
     * Devuelve el nombre completo del archivo, combinando el nombre base y la extensión
     * @return El nombre completo del archivo (ej: "nombre_archivo.jpg")
     */
    public String getCompleteFileName() {
        return fileName + extension;
    }
}