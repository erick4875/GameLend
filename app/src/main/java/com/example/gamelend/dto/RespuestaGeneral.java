package com.example.gamelend.dto;

public class RespuestaGeneral<T> {
    private String tipo;
    private int respuesta;  // Cambiado a int (en lugar de String)
    private String mensaje;
    private T cuerpo;
    private boolean exito;

    public static final int RESP_OK = 1;
    public static final int RESP_ERROR = -1;

    public boolean isExito() {
        return exito;
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
