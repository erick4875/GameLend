package com.example.gamelend.dto;

    public enum Estado {
        DISPONIBLE,
        PRESTADO,
        RESERVADO,
        ;
        public static Estado fromString(String value) {
            for (Estado estado : Estado.values()) {
                if (estado.name().equalsIgnoreCase(value)) {
                    return estado;
                }
            }
            return null; // o un valor por defecto
        }
    }

