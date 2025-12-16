package com.mycompany.clipper.controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.mycompany.clipper.models.ClipboardEntry;

public class Portapapeles {

    public void createTablePortapapeles() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:clipper.db")) {

            // Creación de la tabla si no existe
            String createTableSQL = "CREATE TABLE IF NOT EXISTS portapapeles (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "texto TEXT NOT NULL," +
                    "fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
            }

            // Cierre de la conexión
            conn.close();

        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
        }
    }

    public void insertPortapapeles(String texto) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:clipper.db")) {
            // Insertar datos
            String insertSQL = "INSERT INTO portapapeles (texto, fecha) VALUES (?, CURRENT_TIMESTAMP)";

            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, texto);
                pstmt.executeUpdate();
                // JOptionPane.showMessageDialog(null, "Texto guardado en portapapeles");
            }
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
        }
    }

    // retornar arraylist con los resultados de la consulta
    // retornar arraylist con los resultados de la consulta
    public List<ClipboardEntry> selectPortapapeles() {
        List<ClipboardEntry> clipboardEntries = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:clipper.db")) {
            // Consultar datos
            String selectSQL = "SELECT id, texto, fecha FROM portapapeles ORDER BY fecha DESC";

            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(selectSQL)) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String texto = rs.getString("texto");
                        String fecha = rs.getString("fecha");
                        clipboardEntries.add(new ClipboardEntry(id, texto, fecha));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
        }
        return clipboardEntries;
    }

    public void deletePortapapeles(int id) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:clipper.db")) {
            // Eliminar datos
            String deleteSQL = "DELETE FROM portapapeles WHERE id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }

            // Cierre de la conexión
            conn.close();

        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
        }
    }

    public void resetPortapapeles() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:clipper.db")) {
            // Eliminar datos
            String deleteSQL = "DELETE FROM portapapeles";

            try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Portapapeles reseteado exitosamente");
            }

            // Cierre de la conexión
            conn.close();

        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
        }
    }

}
