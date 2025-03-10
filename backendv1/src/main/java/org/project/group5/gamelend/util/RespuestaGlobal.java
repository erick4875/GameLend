package org.project.group5.gamelend.util;

/**
 * Constantes globales para respuestas de API
 * Contiene códigos de respuesta y mensajes estándar
 */
public class RespuestaGlobal {
    
    /**
     * Enum para estados de entidades
     * D = Disponible
     * P = Prestado
     */
    public enum Estado {
        DISPONIBLE, 
        PRESTADO
    }

    // Tipos de respuesta
    public static final String TIPO_RESULTADO = "result";
    public static final String TIPO_DATA = "data";
    public static final String TIPO_AUTH = "auth";
    public static final String TIPO_EXCEPTION = "exception";

    // Códigos de estado
    public static final int RESP_OK = 1;     // Operación exitosa
    public static final int RESP_ADV = 0;    // Advertencia
    public static final int RESP_ERROR = -1; // Error 

    // Mensajes estándar
    public static final String OPER_CORRECTA = "Operación finalizada correctamente";
    public static final String OPER_INCORRECTA = "No se ha podido culminar la operación";
    public static final String OPER_ERRONEA = "Ha ocurrido un error al realizar la operación";
    
}
