package org.project.group5.gamelend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.project.group5.gamelend.entity.Documento;
import org.project.group5.gamelend.service.DocumentoService;
import org.project.group5.gamelend.util.RespuestaGeneral;
import org.project.group5.gamelend.util.RespuestaGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Controlador REST para la gestión de documentos
 * Proporciona endpoints para listar, buscar, subir y descargar documentos
 */
@RestController
@RequestMapping("api/documento")
public class DocumentoController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentoController.class);
    
    private final DocumentoService docService;

    public DocumentoController(DocumentoService docService) {
        this.docService = docService;
    }

    /**
     * Listar todos los documentos
     * @return Respuesta con la lista de documentos
     */
    @GetMapping
    public ResponseEntity<RespuestaGeneral<?>> list() {
        logger.info("Solicitando lista de documentos");
        RespuestaGeneral<?> respuesta = docService.list();
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Buscar documento por ID
     * @param id ID del documento
     * @return Respuesta con el documento encontrado o mensaje de error
     */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaGeneral<Documento>> find(@PathVariable Long id) {
        logger.info("Buscando documento con ID: {}", id);
        
        if (id == null) {
            logger.warn("ID de documento nulo");
            return ResponseEntity.badRequest().body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_DATA, 
                    RespuestaGlobal.RESP_ERROR, 
                    "ID no puede ser nulo", 
                    null
                )
            );
        }
        
        RespuestaGeneral<Documento> respuesta = docService.find(id);
        
        if (respuesta.isExito()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
        }
    }

    /**
     * Subir y guardar un archivo
     * @param file Archivo a subir
     * @param obj Metadatos del documento
     * @return Respuesta con el documento guardado o mensaje de error
     */
    @PostMapping("/upload")
    public ResponseEntity<RespuestaGeneral<Documento>> save(
            @RequestParam("file") MultipartFile file, 
            @ModelAttribute Documento obj) {
        
        logger.info("Subiendo archivo: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            logger.warn("Intento de subir archivo vacío");
            return ResponseEntity.badRequest().body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_DATA, 
                    RespuestaGlobal.RESP_ERROR, 
                    "El archivo no puede estar vacío", 
                    null
                )
            );
        }
        
        try {
            RespuestaGeneral<Documento> respuesta = docService.save(file, obj);
            
            if (respuesta.isExito()) {
                logger.info("Archivo guardado correctamente: {}", file.getOriginalFilename());
                return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
            } else {
                logger.warn("Error al guardar archivo: {}", respuesta.getMensaje());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
            }
        } catch (IOException e) {
            logger.error("Error al procesar archivo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_DATA, 
                    RespuestaGlobal.RESP_ERROR, 
                    "Error al guardar el archivo: " + e.getMessage(), 
                    null
                )
            );
        }
    }

    /**
     * Descargar un archivo por su nombre
     * @param fileName Nombre del archivo a descargar
     * @param request Solicitud HTTP
     * @return Recurso para descargar o respuesta de error
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<?> download(@PathVariable String fileName, HttpServletRequest request) {
        logger.info("Solicitando descarga de archivo: {}", fileName);
        
        if (fileName == null || fileName.trim().isEmpty()) {
            logger.warn("Nombre de archivo vacío o nulo");
            return ResponseEntity.badRequest().body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_DATA, 
                    RespuestaGlobal.RESP_ERROR, 
                    "Nombre de archivo requerido", 
                    null
                )
            );
        }
        
        try {
            ResponseEntity<Resource> respuesta = docService.download(fileName, request);
            logger.info("Archivo {} preparado para descarga", fileName);
            return respuesta;
        } catch (Exception e) {
            logger.error("Error al descargar archivo {}: {}", fileName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_DATA, 
                    RespuestaGlobal.RESP_ERROR, 
                    "Error al descargar el archivo: " + e.getMessage(), 
                    null
                )
            );
        }
    }
}