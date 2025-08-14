import java.io.*;
import java.nio.file.*;
import java.sql.*;

public class InsertarImagenesRecetas {

    private static final String CARPETA_IMAGENES = "imagen_receta"; //carpeta que contiene las imágenes .png

    public static void insertarImagenes(Connection connection) {
        try {

            //ajusta el tamaño máximo de paquete para permitir imágenes grandes
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("SET GLOBAL max_allowed_packet = 33554432");
            } catch (SQLException e) {
            }

            File carpeta = new File(CARPETA_IMAGENES); //accede a la carpeta de imágenes
            if (!carpeta.exists() || !carpeta.isDirectory()) {
                return; //sale si la carpeta no existe o no es válida
            }

            File[] archivos = carpeta.listFiles((dir, name) -> name.toLowerCase().endsWith(".png")); //filtra solo archivos .png
            if (archivos == null || archivos.length == 0) {
                return; //sale si no hay imágenes
            }

            //recorre cada imagen en la carpeta
            for (File imagen : archivos) {
                String nombreArchivo = imagen.getName().replace(".png", ""); //elimina extensión
                String nombreReceta = nombreArchivo.replace("_", " ").toLowerCase(); //formatea nombre para coincidir con bd

                byte[] imagenBytes = Files.readAllBytes(imagen.toPath()); //lee la imagen como bytes

                String sql = "UPDATE RECETA SET imagen = ? WHERE LOWER(nombre) = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setBytes(1, imagenBytes); //asigna imagen como parámetro
                    stmt.setString(2, nombreReceta); //asigna nombre formateado como parámetro
                    stmt.executeUpdate(); //ejecuta la actualización
                } catch (SQLException ignored) {} //ignora si alguna imagen no se puede insertar
            }

        } catch (Exception ignored) {} //ignora cualquier excepción general
    }
}
