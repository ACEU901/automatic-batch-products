package com.transfer.evaluation.service.DAO;

import com.transfer.evaluation.service.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO extends Conexion{
    public List<Producto> obtenerProductos() {
        List<Producto> productos = new ArrayList<>();

        try (Connection conexion = Conexion.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement("SELECT * FROM PRODUCTO");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                float numSuc = resultSet.getFloat("NumSuc");
                String sku = resultSet.getString("Sku");
                String codBarra = resultSet.getString("CODBARRA");
                String descripcion = resultSet.getString("Descripcion");
                float fam = resultSet.getFloat("Fam");
                float salFisSuc = resultSet.getFloat("SalFisSuc");
                float valor = resultSet.getFloat("Valor");

                Producto producto = new Producto(numSuc, sku, codBarra, descripcion, fam, salFisSuc, valor);
                productos.add(producto);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener los datos del producto: " + e.getMessage());
        }

        return productos;
    }
}
