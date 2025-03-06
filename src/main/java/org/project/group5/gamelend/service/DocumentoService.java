package org.project.group5.gamelend.service;

import java.io.IOException;
import java.util.UUID;

import org.project.group5.gamelend.entity.Documento;
import org.project.group5.gamelend.repository.DocumentoRepository;
import org.project.group5.gamelend.util.RespuestaGeneral;
import static org.project.group5.gamelend.util.RespuestaGlobal.OPER_CORRECTA;
import static org.project.group5.gamelend.util.RespuestaGlobal.RESP_ERROR;
import static org.project.group5.gamelend.util.RespuestaGlobal.RESP_OK;
import static org.project.group5.gamelend.util.RespuestaGlobal.TIPO_DATA;
import static org.project.group5.gamelend.util.RespuestaGlobal.TIPO_RESULTADO;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

/**
 * Servicio para la gestión de documentos (imágenes)
 */
@Service
@Transactional
public class DocumentoService {

    private final DocumentoRepository docRepository;
    private final FileStorageService storageService;

    public DocumentoService(DocumentoRepository repo, FileStorageService storageService) {
        this.docRepository = repo;
        this.storageService = storageService;
    }

    /**
     * Listar todos los documentos
     * @return Respuesta con la lista de documentos
     */
    public RespuestaGeneral<Iterable<Documento>> list() {
        return new RespuestaGeneral<>(TIPO_RESULTADO, RESP_OK, OPER_CORRECTA, docRepository.findAll());
    }

    /**
     * Buscar documento por ID
     * @param id ID del documento
     * @return Respuesta con el documento encontrado o mensaje de error
     */
    public RespuestaGeneral<Documento> find(Long id) {
        if (id == null) {
            return new RespuestaGeneral<>(TIPO_DATA, RESP_ERROR, "ID no puede ser nulo", null);
        }
        
        return docRepository.findById(id)
            .map(doc -> new RespuestaGeneral<>(TIPO_DATA, RESP_OK, "Documento encontrado", doc))
            .orElse(new RespuestaGeneral<>(TIPO_DATA, RESP_ERROR, "Documento no encontrado", null));
    }

    /**
     * Guardar un nuevo documento con archivo
     * @param file Archivo a guardar
     * @param obj Datos del documento
     * @return Respuesta con el documento guardado o mensaje de error
     * @throws IOException Si hay problemas al leer el archivo
     */
    public RespuestaGeneral<Documento> save(MultipartFile file, Documento obj) throws IOException {
        // Validación de entrada
        if (file == null || file.isEmpty()) {
            return new RespuestaGeneral<>(TIPO_DATA, RESP_ERROR, "Archivo vacío o nulo", null);
        }
        
        if (obj == null) {
            return new RespuestaGeneral<>(TIPO_DATA, RESP_ERROR, "Datos del documento nulos", null);
        }
        
        try {
            // Asignamos el nombre y la extensión del archivo
            obj.setFileName(UUID.randomUUID().toString());
            obj.setExtension(getFileExtension(file));
            obj.setEstado("D"); // Estado de documento "D" para Documento en estado de borrador
            obj.setEliminado(false);
            obj.setImagen(file.getBytes()); // Guardamos los bytes del archivo

            // Guardamos el documento en la base de datos
            Documento saved = docRepository.save(obj);
            return new RespuestaGeneral<>(TIPO_DATA, RESP_OK, "Documento guardado correctamente", saved);
        } catch (Exception e) {
            return new RespuestaGeneral<>(TIPO_DATA, RESP_ERROR, "Error al guardar: " + e.getMessage(), null);
        }
    }

    /**
     * Descargar un archivo por nombre de archivo
     * @param fileName Nombre del archivo
     * @param request Solicitud HTTP
     * @return ResponseEntity con el archivo para descargar
     */
    public ResponseEntity<Resource> download(String fileName, HttpServletRequest request) {
        // Validación de entrada
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Nombre de archivo vacío o nulo");
        }
        
        // Recuperamos el documento desde la base de datos
        Documento documento = docRepository.findByFileName(fileName);
        if (documento != null && documento.getImagen() != null) {
            byte[] fileData = documento.getImagen();
            ByteArrayResource resource = new ByteArrayResource(fileData);

            String contentType = request.getServletContext().getMimeType(fileName);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } else {
            throw new RuntimeException("Archivo no encontrado: " + fileName);
        }
    }

    /**
     * Método auxiliar para obtener la extensión de un archivo
     * @param file Archivo
     * @return Extensión del archivo
     */
    private String getFileExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }
}