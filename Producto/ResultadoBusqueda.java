//clase que representa el resultado de una búsqueda inteligente
public class ResultadoBusqueda {
    private Receta receta;
    private boolean completa;

    //constructor que recibe la receta encontrada y si está completa
    public ResultadoBusqueda(Receta receta, boolean completa) {
        this.receta = receta;
        this.completa = completa;
    }

    //devuelve la receta asociada al resultado
    public Receta getReceta() {
        return receta;
    }

    //indica si se puede preparar la receta con los ingredientes disponibles
    public boolean isCompleta() {
        return completa;
    }
}
