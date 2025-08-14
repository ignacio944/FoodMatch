import java.sql.*;

public class ClaseUsuario {
    private Connection connection;

    //constructor que recibe una conexión a la base de datos
    public ClaseUsuario(Connection connection) {
        this.connection = connection;
    }

    //verifica si existe un usuario con nombre y contraseña válidos
    public Usuario verificarCredenciales(String nombreUsuario, String password) throws SQLException {
        String sql = "SELECT * FROM USUARIO WHERE nombre = ? AND contraseña = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            //asigna los parámetros seguros para evitar inyección sql
            stmt.setString(1, nombreUsuario);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            //si encuentra una fila, crea y retorna el objeto usuario
            if (rs.next()) {
                return new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("nombre"),
                    rs.getString("contraseña"),
                    rs.getBoolean("celiaco"),
                    rs.getBoolean("vegano"),
                    rs.getBoolean("vegetariano")
                );
            } else {
                //si no hay coincidencia, retorna null
                return null;
            }
        }
    }

    //verifica si ya existe un usuario con el nombre dado
    public boolean existeUsuario(String nombre) throws SQLException {
        String sql = "SELECT COUNT(*) FROM USUARIO WHERE nombre = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                //devuelve true si el recuento es mayor a cero
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    //registra un nuevo usuario en la base de datos y devuelve su id generado
    public int registrarUsuario(Usuario u) throws SQLException {
        String sql = "INSERT INTO USUARIO (nombre, contraseña, celiaco, vegano, vegetariano) VALUES (?, ?, ?, ?, ?)";
        //se solicita que devuelva las claves generadas (el id)
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, u.getNombre());
            stmt.setString(2, u.getContraseña());
            stmt.setBoolean(3, u.isCeliaco());
            stmt.setBoolean(4, u.isVegano());
            stmt.setBoolean(5, u.isVegetariano());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            //si hay id generado, se retorna
            if (rs.next()) {
                return rs.getInt(1); // id generado
            }
        }
        //si falla, se retorna -1
        return -1;
    }

    //devuelve el nombre de usuario a partir de su id
    public String obtenerNombrePorId(int id_usuario) throws SQLException {
        String nombre = "";
        String sql = "SELECT nombre FROM USUARIO WHERE id_usuario = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id_usuario);
            ResultSet rs = stmt.executeQuery();
            //si encuentra la fila, obtiene el nombre
            if (rs.next()) {
                nombre = rs.getString("nombre");
            }
        }
        return nombre;
    }

    //devuelve el objeto usuario a partir de su id
    public Usuario obtenerUsuarioPorId(int id) throws SQLException {
        String sql = "SELECT * FROM USUARIO WHERE id_usuario = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            //si encuentra la fila, crea y retorna el usuario
            if (rs.next()) {
                return new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("nombre"),
                    rs.getString("contraseña"),
                    rs.getBoolean("celiaco"),
                    rs.getBoolean("vegano"),
                    rs.getBoolean("vegetariano")
                );
            }
        }
        //si no hay coincidencia, retorna null
        return null;
    }

    //actualiza las preferencias alimentarias de un usuario existente
    public void actualizarPreferencias(Usuario usuario) throws SQLException {
        String sql = "UPDATE USUARIO SET celiaco = ?, vegano = ?, vegetariano = ? WHERE id_usuario = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, usuario.isCeliaco());
            stmt.setBoolean(2, usuario.isVegano());
            stmt.setBoolean(3, usuario.isVegetariano());
            stmt.setInt(4, usuario.getId());
            stmt.executeUpdate();
        }
    }
}
