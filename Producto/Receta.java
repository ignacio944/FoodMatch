//clase que representa una receta de cocina con sus atributos y métodos
public class Receta {

    //atributos principales de la receta
    private int id;  //identificador único de la receta
    private String nombre;  //nombre de la receta
    private int creadorId;  //id del usuario que creó la receta
    private String dificultad;  //nivel de dificultad (por ejemplo: fácil, media, difícil)
    private int tiempoPreparacion;  //tiempo estimado de preparación en minutos

    //indicadores de restricciones alimentarias
    private boolean vegano;  //si es apta para veganos
    private boolean vegetariano;  //si es apta para vegetarianos
    private boolean celiaco;  //si es apta para celíacos

    private String procedimiento;  //pasos para preparar la receta
    private byte[] imagen;  //imagen de la receta en forma de arreglo de bytes

    //información de valoración de los usuarios
    private int votos;  //número total de votos recibidos
    private float valoracion;  //valoración media (por ejemplo, entre 0.0 y 5.0)

    //información nutricional (por porción o total, según convención usada)
    private int valorEnergetico; //valor energético en kilocalorías
    private double proteinas;  //cantidad de proteínas en gramos
    private double glucidos;  //cantidad de glúcidos (carbohidratos) en gramos
    private double lipidos;  //cantidad de lípidos (grasas) en gramos

    //constructor que inicializa todos los atributos
    public Receta(int id, String nombre, int creadorId, String dificultad, int tiempoPreparacion,
                  boolean vegano, boolean vegetariano, boolean celiaco, String procedimiento,
                  byte[] imagen, int votos, float valoracion, int valorEnergetico,
                  double proteinas, double glucidos, double lipidos) {
        this.id = id;
        this.nombre = nombre;
        this.creadorId = creadorId;
        this.dificultad = dificultad;
        this.tiempoPreparacion = tiempoPreparacion;
        this.vegano = vegano;
        this.vegetariano = vegetariano;
        this.celiaco = celiaco;
        this.procedimiento = procedimiento;
        this.imagen = imagen;
        this.votos = votos;
        this.valoracion = valoracion;
        this.valorEnergetico = valorEnergetico;
        this.proteinas = proteinas;
        this.glucidos = glucidos;
        this.lipidos = lipidos;
    }

    //métodos getter: devuelven el valor de cada atributo
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getCreadorId() { return creadorId; }
    public String getDificultad() { return dificultad; }
    public int getTiempoPreparacion() { return tiempoPreparacion; }
    public boolean isVegano() { return vegano; }
    public boolean isVegetariano() { return vegetariano; }
    public boolean isCeliaco() { return celiaco; }
    public String getProcedimiento() { return procedimiento; }
    public byte[] getImagen() { return imagen; }
    public int getVotos() { return votos; }
    public float getValoracion() { return valoracion; }
    public int getValorEnergetico() { return valorEnergetico; }
    public double getProteinas() { return proteinas; }
    public double getGlucidos() { return glucidos; }
    public double getLipidos() { return lipidos; }

    //métodos setter: permiten modificar el valor de los atributos
    public void setId(int id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCreadorId(int creadorId) { this.creadorId = creadorId; }
    public void setDificultad(String dificultad) { this.dificultad = dificultad; }
    public void setTiempoPreparacion(int tiempoPreparacion) { this.tiempoPreparacion = tiempoPreparacion; }
    public void setVegano(boolean vegano) { this.vegano = vegano; }
    public void setVegetariano(boolean vegetariano) { this.vegetariano = vegetariano; }
    public void setCeliaco(boolean celiaco) { this.celiaco = celiaco; }
    public void setProcedimiento(String procedimiento) { this.procedimiento = procedimiento; }
    public void setImagen(byte[] imagen) { this.imagen = imagen; }
    public void setVotos(int votos) { this.votos = votos; }
    public void setValoracion(float valoracion) { this.valoracion = valoracion; }
    public void setValorEnergetico(int valorEnergetico) { this.valorEnergetico = valorEnergetico; }
    public void setProteinas(double proteinas) { this.proteinas = proteinas; }
    public void setGlucidos(double glucidos) { this.glucidos = glucidos; }
    public void setLipidos(double lipidos) { this.lipidos = lipidos; }
}
