import javax.swing.*;
import java.awt.*;

public class CargaImagen {
    public static void cargar(int recetaId, ClaseReceta claseReceta, JLabel label) {
        //define el tamaño fijo de la imagen
        int ancho = 100;
        int alto = 100;

        //deja la etiqueta vacía mientras se carga la imagen
        label.setIcon(null);

        //usa un swingworker para cargar la imagen sin bloquear la interfaz
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                try {
                    //obtiene la imagen desde la base de datos como array de bytes
                    byte[] imagenBytes = claseReceta.obtenerImagenReceta(recetaId);
                    if (imagenBytes != null) {
                        //escala la imagen al tamaño deseado y la devuelve como icono
                        Image img = new ImageIcon(imagenBytes).getImage()
                                .getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
                        return new ImageIcon(img);
                    }
                } catch (Exception e) {
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    //cuando la imagen ya está lista, se muestra en la etiqueta
                    ImageIcon icon = get();
                    if (icon != null) {
                        label.setIcon(icon);
                    }
                } catch (Exception e) {
                }
            }
        }.execute(); //ejecuta el swingworker
    }
}
