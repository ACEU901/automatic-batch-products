package com.transfer.evaluation.service;

import com.transfer.evaluation.service.DAO.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class CargadorArchivo {

    public static void cargarArchivo(String rutaArchivo) {
        List<String> lineas = new ArrayList<>();

        // 1. Leer todas las líneas del archivo
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                lineas.add(linea);
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
            return;
        }

        // 2. Conexión a la base de datos
        Connection conexion = Conexion.obtenerConexion();

        if (conexion == null) {
            System.err.println("No se pudo establecer la conexión con la base de datos.");
            return;
        }

        String sql = "INSERT INTO PRODUCTO (NumSuc, Sku, CODBARRA, Descripcion, Fam, SalFisSuc, Valor) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            int registrosInsertados = 0;

            for (int i = 0; i < lineas.size(); i++) {
                String[] datos = lineas.get(i).split(";");

                if (datos.length != 7) {
                    System.err.println("Línea inválida en la posición " + (i + 1));
                    continue;
                }

                float numSuc = Float.parseFloat(datos[0].trim());
                String sku = datos[1].trim();
                String codBarra = datos[2].trim();
                String descripcion = datos[3].trim();
                float fam = Float.parseFloat(datos[4].trim());
                float salFisSuc = Float.parseFloat(datos[5].trim());
                float valor = Float.parseFloat(datos[6].trim().replace(",", "."));

                statement.setFloat(1, numSuc);
                statement.setString(2, sku);
                statement.setString(3, codBarra);
                statement.setString(4, descripcion);
                statement.setFloat(5, fam);
                statement.setFloat(6, salFisSuc);
                statement.setFloat(7, valor);

                statement.executeUpdate();
                registrosInsertados++;
            }

            System.out.println("Carga completada. Registros insertados: " + registrosInsertados);

        } catch (SQLException e) {
            System.err.println("Error al insertar en la base de datos: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Conexion.cerrarConexion();
        }
    }
}
