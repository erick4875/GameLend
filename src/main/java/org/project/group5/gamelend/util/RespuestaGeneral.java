package org.project.group5.gamelend.util;

import java.io.Serializable;

/**
 * Clase genérica para estandarizar las respuestas de API
 * Permite formatear de manera consistente los resultados de las operaciones
 * @param <T> Tipo de datos que contiene el cuerpo de la respuesta
 */
public class RespuestaGeneral<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tipo;      // Tipo de respuesta (result, data, auth)
    private int respuesta;    // Código de respuesta (1=OK, 0=Advertencia, -1=Error)
    private String mensaje;   // Mensaje descriptivo
    private T cuerpo;         // Datos de respuesta (opcional)

    /**
     * Constructor para respuesta estándar con datos
     * @param tipo Tipo de respuesta (usar constantes de RespuestaGlobal)
     * @param respuesta Código de respuesta (usar constantes de RespuestaGlobal)
     * @param mensaje Mensaje descriptivo
     * @param cuerpo Datos de respuesta
     */
    public RespuestaGeneral(String tipo, int respuesta, String mensaje, T cuerpo) {
        this.tipo = tipo;
        this.respuesta = respuesta;
        this.mensaje = mensaje;
        this.cuerpo = cuerpo;
    }

    /**
     * Constructor sin cuerpo, útil para respuestas de solo estado
     * @param tipo Tipo de respuesta
     * @param respuesta Código de respuesta
     * @param mensaje Mensaje descriptivo
     */
    public RespuestaGeneral(String tipo, int respuesta, String mensaje) {
        this(tipo, respuesta, mensaje, null);
    }

    /**
     * Crea una respuesta de éxito con datos
     * @param <T> Tipo de datos del cuerpo
     * @param mensaje Mensaje descriptivo
     * @param cuerpo Datos de respuesta
     * @return Instancia de RespuestaGeneral configurada para éxito
     */
    public static <T> RespuestaGeneral<T> ok(String mensaje, T cuerpo) {
        return new RespuestaGeneral<>(RespuestaGlobal.TIPO_RESULTADO, RespuestaGlobal.RESP_OK, mensaje, cuerpo);
    }

    /**
     * Crea una respuesta de error
     * @param <T> Tipo de datos del cuerpo (normalmente null)
     * @param mensaje Mensaje de error
     * @return Instancia de RespuestaGeneral configurada para error
     */
    public static <T> RespuestaGeneral<T> error(String mensaje) {
        return new RespuestaGeneral<>(RespuestaGlobal.TIPO_RESULTADO, RespuestaGlobal.RESP_ERROR, mensaje, null);
    }

    /**
     * Crea una respuesta de advertencia
     * @param <T> Tipo de datos del cuerpo
     * @param mensaje Mensaje de advertencia
     * @param cuerpo Datos opcionales
     * @return Instancia de RespuestaGeneral configurada para advertencia
     */
    public static <T> RespuestaGeneral<T> advertencia(String mensaje, T cuerpo) {
        return new RespuestaGeneral<>(RespuestaGlobal.TIPO_RESULTADO, RespuestaGlobal.RESP_ADV, mensaje, cuerpo);
    }

    // Getters y Setters
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(int respuesta) {
        this.respuesta = respuesta;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public T getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(T cuerpo) {
        this.cuerpo = cuerpo;
    }
    
    /**
     * Verifica si la respuesta indica éxito
     * @return true si el código de respuesta es RESP_OK
     */
    public boolean isExito() {
        return respuesta == RespuestaGlobal.RESP_OK;
    }
    
    @Override
    public String toString() {
        return "RespuestaGeneral [tipo=" + tipo + 
               ", respuesta=" + respuesta + 
               ", mensaje=" + mensaje + 
               ", cuerpo=" + (cuerpo != null ? "presente" : "null") + "]";
    }
}
