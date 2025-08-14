import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClaseAlimento {
    private Connection connection;

    public ClaseAlimento(Connection connection) {
        this.connection = connection;
    }

    public List<Alimento> obtenerTodosLosAlimentos() throws SQLException {
        //crear una lista vacía para almacenar los alimentos obtenidos de la base de datos
        List<Alimento> alimentos = new ArrayList<>();
        
        //definir la consulta sql para seleccionar todos los registros de la tabla alimento
        String query = "SELECT * FROM ALIMENTO";
        
        //crear un statement y ejecutar la consulta
        try (Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {

            //recorrer el resultado fila a fila
            while (rs.next()) {
                //crear un nuevo objeto alimento con los datos de la fila actual
                Alimento alimento = new Alimento(
                    rs.getInt("id_alimento"),
                    rs.getString("nombre"),
                    rs.getInt("valor_energetico"),
                    rs.getBoolean("celiaco"),
                    rs.getBoolean("vegano"),
                    rs.getBoolean("vegetariano"),
                    rs.getDouble("proteinas"),
                    rs.getDouble("glucidos"),
                    rs.getDouble("lipidos")
                );

                //añadir el alimento a la lista
                alimentos.add(alimento);
            }
        }

        //devolver la lista completa de alimentos obtenidos
        return alimentos;
    }


    public List<Alimento> obtenerAlimentosProhibidos(int id_usuario) throws SQLException {
        //crear una lista vacía para guardar los alimentos prohibidos del usuario
        List<Alimento> alimentosProhibidos = new ArrayList<>();

        //consulta sql para obtener los alimentos prohibidos del usuario mediante join
        String query = "SELECT a.* FROM ALIMENTO a " +
                    "INNER JOIN ALIMENTO_PROHIBIDO ap ON a.id_alimento = ap.id_alimento " +
                    "WHERE ap.id_usuario = ?";

        //preparar la sentencia con parámetro
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            //establecer el id del usuario en la consulta
            stmt.setInt(1, id_usuario);

            //ejecutar la consulta
            ResultSet rs = stmt.executeQuery();

            //recorrer los resultados
            while (rs.next()) {
                //crear un objeto alimento con los datos obtenidos
                Alimento alimento = new Alimento(
                    rs.getInt("id_alimento"),
                    rs.getString("nombre"),
                    rs.getInt("valor_energetico"),
                    rs.getBoolean("celiaco"),
                    rs.getBoolean("vegano"),
                    rs.getBoolean("vegetariano"),
                    rs.getDouble("proteinas"),
                    rs.getDouble("glucidos"),
                    rs.getDouble("lipidos")
                );

                //añadir el alimento a la lista de prohibidos
                alimentosProhibidos.add(alimento);
            }
        }

        //devolver la lista de alimentos prohibidos
        return alimentosProhibidos;
    }

    public void eliminarAlimentoProhibido(Alimento alimento, int id_usuario) throws SQLException {
        //sentencia sql para eliminar un alimento prohibido de un usuario concreto
        String deleteQuery = "DELETE FROM ALIMENTO_PROHIBIDO WHERE id_usuario = ? AND id_alimento = ?";

        //preparar la sentencia con los parámetros necesarios
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
            //establecer el id del usuario
            deleteStmt.setInt(1, id_usuario);

            //establecer el id del alimento a eliminar
            deleteStmt.setInt(2, alimento.getId());

            //ejecutar la sentencia de eliminación
            deleteStmt.executeUpdate();
        }
    }

    public void guardarAlimentosProhibidos(List<Alimento> alimentos, int id_usuario) throws SQLException {
        //recorrer todos los alimentos que se desean guardar como prohibidos
        for (Alimento alimento : alimentos) {
            int id_alimento = alimento.getId();

            //consulta para verificar si ya existe la relación usuario-alimento en la tabla
            String checkQuery = "SELECT COUNT(*) FROM ALIMENTO_PROHIBIDO WHERE id_usuario = ? AND id_alimento = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                //establecer el id del usuario
                checkStmt.setInt(1, id_usuario);

                //establecer el id del alimento a comprobar
                checkStmt.setInt(2, id_alimento);

                //ejecutar la consulta
                ResultSet checkRs = checkStmt.executeQuery();

                //si no existe esa combinación, se inserta como nuevo alimento prohibido
                if (checkRs.next() && checkRs.getInt(1) == 0) {
                    //consulta para insertar la relación en la tabla ALIMENTO_PROHIBIDO
                    String insertQuery = "INSERT INTO ALIMENTO_PROHIBIDO (id_usuario, id_alimento) VALUES (?, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        //asignar id del usuario
                        insertStmt.setInt(1, id_usuario);

                        //asignar id del alimento
                        insertStmt.setInt(2, id_alimento);

                        //ejecutar la inserción
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }


    public String obtenerNombreAlimento(int idAlimento) throws SQLException {
        //consulta sql para obtener el nombre de un alimento a partir de su id
        String sql = "SELECT nombre FROM ALIMENTO WHERE id_alimento = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            //se establece el id como parámetro en la consulta
            stmt.setInt(1, idAlimento);

            //se ejecuta la consulta
            ResultSet rs = stmt.executeQuery();

            //si existe un resultado, se devuelve el nombre
            if (rs.next()) {
                return rs.getString("nombre");
            }
        }

        //si no se encuentra el alimento, se devuelve null
        return null;
    }

    public Alimento obtenerAlimentoPorNombre(String nombre) throws SQLException {
        //consulta sql para obtener todos los datos de un alimento dado su nombre
        String sql = "SELECT * FROM ALIMENTO WHERE nombre = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            //se establece el nombre como parámetro
            stmt.setString(1, nombre);

            //se ejecuta la consulta
            ResultSet rs = stmt.executeQuery();

            //si existe un resultado, se crea y devuelve el objeto Alimento
            if (rs.next()) {
                return new Alimento(
                    rs.getInt("id_alimento"),
                    rs.getString("nombre"),
                    rs.getInt("valor_energetico"),
                    rs.getBoolean("celiaco"),
                    rs.getBoolean("vegano"),
                    rs.getBoolean("vegetariano"),
                    rs.getDouble("proteinas"),
                    rs.getDouble("glucidos"),
                    rs.getDouble("lipidos")
                );
            }
        }

        //si no se encuentra el alimento, se devuelve null
        return null;
    }

    public List<String> obtenerNombresDeAlimentos() throws SQLException {
        //lista para almacenar los nombres de todos los alimentos
        List<String> nombres = new ArrayList<>();

        //consulta sql para seleccionar los nombres de todos los alimentos
        String sql = "SELECT nombre FROM ALIMENTO";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            //se recorren los resultados y se añaden los nombres a la lista
            while (rs.next()) {
                nombres.add(rs.getString("nombre"));
            }
        }

        //se devuelve la lista de nombres
        return nombres;
    }
}