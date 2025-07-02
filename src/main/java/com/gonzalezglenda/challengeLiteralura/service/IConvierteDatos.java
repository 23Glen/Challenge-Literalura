package com.gonzalezglenda.challengeLiteralura.service;

public interface IConvierteDatos {
    <T> T obtenerDatos (String json, Class<T> clase);
}
