//clase que representa a un usuario del sistema
public class Usuario {
    //atributos del usuario
    private int id;
    private String nombre;
    private String contraseña;
    private boolean celiaco;
    private boolean vegano;
    private boolean vegetariano;

    //constructor para inicializar todos los atributos del usuario
    public Usuario(int id, String nombre, String contraseña, boolean celiaco, boolean vegano, boolean vegetariano) {
        this.id = id;
        this.nombre = nombre;
        this.contraseña = contraseña;
        this.celiaco = celiaco;
        this.vegano = vegano;
        this.vegetariano = vegetariano;
    }

    //getter del id del usuario
    public int getId() { return id; }

    //getter del nombre del usuario
    public String getNombre() { return nombre; }

    //getter de la contraseña del usuario
    public String getContraseña() { return contraseña; }

    //getter que indica si el usuario es celiaco
    public boolean isCeliaco() { return celiaco; }

    //getter que indica si el usuario es vegano
    public boolean isVegano() { return vegano; }

    //getter que indica si el usuario es vegetariano
    public boolean isVegetariano() { return vegetariano; }

    //setter para cambiar el id del usuario
    public void setId(int id) { this.id = id; }

    //setter para modificar si el usuario es celiaco
    public void setCeliaco(boolean celiaco) { this.celiaco = celiaco; }

    //setter para modificar si el usuario es vegano
    public void setVegano(boolean vegano) { this.vegano = vegano; }

    //setter para modificar si el usuario es vegetariano
    public void setVegetariano(boolean vegetariano) { this.vegetariano = vegetariano; }
}
