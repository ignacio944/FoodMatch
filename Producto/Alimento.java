public class Alimento {
    //atributos de cada alimento con su información nutricional y restricciones dietéticas
    private int id;
    private String nombre;
    private int valorEnergetico;
    private boolean celiaco;
    private boolean vegano;
    private boolean vegetariano;
    private double proteinas;
    private double glucidos;
    private double lipidos;

    //constructor que inicializa todos los campos del alimento
    public Alimento(int id, String nombre, int valorEnergetico, boolean celiaco,
                    boolean vegano, boolean vegetariano,
                    double proteinas, double glucidos, double lipidos) {
        this.id = id;
        this.nombre = nombre;
        this.valorEnergetico = valorEnergetico;
        this.celiaco = celiaco;
        this.vegano = vegano;
        this.vegetariano = vegetariano;
        this.proteinas = proteinas;
        this.glucidos = glucidos;
        this.lipidos = lipidos;
    }

    //getters que permiten acceder a los datos del alimento
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getValorEnergetico() { return valorEnergetico; }
    public boolean isCeliaco() { return celiaco; }
    public boolean isVegano() { return vegano; }
    public boolean isVegetariano() { return vegetariano; }
    public double getProteinas() { return proteinas; }
    public double getGlucidos() { return glucidos; }
    public double getLipidos() { return lipidos; }
}
