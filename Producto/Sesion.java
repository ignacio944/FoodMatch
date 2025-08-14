import java.sql.Connection;

//clase que mantiene el estado del usuario autenticado y la conexión activa
public class Sesion {
    private Usuario usuario;
    private Connection connection;

    //constructor que recibe el usuario actual y la conexión a la base de datos
    public Sesion(Usuario usuario, Connection connection) {
        this.usuario = usuario;
        this.connection = connection;
    }

    //devuelve el usuario autenticado
    public Usuario getUsuario() {
        return usuario;
    }

    //devuelve la conexión activa a la base de datos
    public Connection getConnection() {
        return connection;
    }

    //actualiza el usuario actual de la sesión
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    //actualiza la conexión asociada a la sesión
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
