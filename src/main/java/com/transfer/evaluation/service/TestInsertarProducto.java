package com.transfer.evaluation.service;

import com.transfer.evaluation.service.DAO.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TestInsertarProducto {
    public static void main(String[] args) {
        // Datos del producto a insertar
        float numSuc = 1;
        String sku = "ABC123";
        String codBarra = "1234567890";
        String descripcion = "Producto de prueba";
        float fam = 10.5f;
        float salFisSuc = 200.0f;
        float valor = 150.75f;

        // Iniciar la conexión y preparar el SQL
        Connection conexion = Conexion.obtenerConexion();

        if (conexion != null) {
            try {
                // SQL de inserción
                String sql = "INSERT INTO PRODUCTO (NumSuc, Sku, CODBARRA, Descripcion, Fam, SalFisSuc, Valor) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";

                // Crear un PreparedStatement
                PreparedStatement statement = conexion.prepareStatement(sql);

                // Establecer los valores de los parámetros
                statement.setFloat(1, numSuc);
                statement.setString(2, sku);
                statement.setString(3, codBarra);
                statement.setString(4, descripcion);
                statement.setFloat(5, fam);
                statement.setFloat(6, salFisSuc);
                statement.setFloat(7, valor);

                // Ejecutar la inserción
                int filasAfectadas = statement.executeUpdate();

                if (filasAfectadas > 0) {
                    System.out.println("Producto insertado correctamente.");
                } else {
                    System.out.println("No se insertó el producto.");
                }

            } catch (SQLException e) {
                System.err.println("Error al insertar el producto: " + e.getMessage());
            } finally {
                // Cerrar la conexión después de la operación
                Conexion.cerrarConexion();
            }
        } else {
            System.err.println("Error: No se pudo establecer la conexión.");
        }
    }
}
