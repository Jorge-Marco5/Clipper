/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.clipper.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;

public class database {

    public static void connect() {
        // La cadena de conexión (URL)
        // "jdbc:sqlite:" es el protocolo
        // "clipper.db" es el nombre del archivo.
        // Si usas una ruta relativa, se creará en la raíz del proyecto.
        String url = "jdbc:sqlite:clipper.db";

        // Usamos try-with-resources para asegurar que la conexión se cierre
        // automáticamente si hay un error o al finalizar.
        try (Connection conn = DriverManager.getConnection(url)) {

            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("El nombre del driver es: " + meta.getDriverName());
                System.out.println("¡Conexión a SQLite establecida exitosamente!");
            }

        } catch (SQLException e) {
            System.out.println("Ocurrió un error al conectar: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        connect();
    }
}