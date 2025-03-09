package com.example.gamelend.dto;

public class LoginResponse {
    private String NombreUsuario;
    private String Token;
    private String tipo;
    private int respuesta;
    private String mensaje;
    private LoginResponseBody cuerpo;
    private boolean exito;

    // Constructor vacío necesario para Gson
    public LoginResponse() {}

    public String getNombreUsuario() {
        return NombreUsuario;
    }

    public String getToken() {
        return Token;
    }

    public static class LoginResponseBody {
        private String token;
        private String nombrePublico;
        private long userId;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getNombrePublico() { return nombrePublico; }
        public void setNombrePublico(String nombrePublico) { this.nombrePublico = nombrePublico; }

        public long getUserId() { return userId; }  // Cambiado método getter
        public void setUserId(long userId) { this.userId = userId; }  // Cambiado método setter
    }

    // Getters y setters
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getRespuesta() { return respuesta; }
    public void setRespuesta(int respuesta) { this.respuesta = respuesta; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    // Añadido getter para cuerpo
    public LoginResponseBody getCuerpo() { return cuerpo; }
    public void setCuerpo(LoginResponseBody cuerpo) { this.cuerpo = cuerpo; }

    // Cambiado a isExito (convención Java para booleanos)
    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }
}