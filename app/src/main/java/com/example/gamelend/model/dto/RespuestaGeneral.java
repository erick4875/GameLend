package com.example.gamelend.model.dto;

public class RespuestaGeneral<T> {
    // Campos existentes
    private String tipo;
    private int respuesta;  // Cambiado a int (en lugar de String)
    private String mensaje;
    private T cuerpo;
    private boolean exito;  // Este campo determina el éxito

    // Constantes actualizadas
    public static final int RESP_OK = 1;      // Valor numérico
    public static final int RESP_ERROR = -1;  // Valor numérico

    // Constructor y otros métodos...

    // Método corregido para verificar éxito
    public boolean isExito() {
        return exito;  // Usar directamente el campo boolean
    }

    // Getters y setters actualizados
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getRespuesta() { return respuesta; }
    public void setRespuesta(int respuesta) { this.respuesta = respuesta; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public T getCuerpo() { return cuerpo; }
    public void setCuerpo(T cuerpo) { this.cuerpo = cuerpo; }

    // Añadir getter y setter para exito
    public boolean getExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }
}
