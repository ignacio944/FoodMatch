public class AlimentoDisponible {
    //representa un alimento específico con cantidad y unidad disponibles
    private Alimento alimento;
    private double cantidad;
    private String unidad;

    //constructor que inicializa el alimento con su cantidad y unidad asociadas
    public AlimentoDisponible(Alimento alimento, double cantidad, String unidad) {
        this.alimento = alimento;
        this.cantidad = cantidad;
        this.unidad = unidad;
    }
    //devuelve el alimento asociado
    public Alimento getAlimento() {
        return alimento;
    }
    //devuelve la cantidad disponible del alimento
    public double getCantidad() {
        return cantidad;
    }
    //actualiza la cantidad disponible del alimento
    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }
    //devuelve la unidad de medida usada
    public String getUnidad() {
        return unidad;
    }
    //actualiza la unidad de medida del alimento
    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }
}
