import javax.swing.*;
import java.awt.*;

//clase que extiende jcombobox para personalizar la apariencia y el estilo
public class ComboBox<E> extends JComboBox<E> {

    //constructor que recibe un array de elementos y configura el diseño visual
    public ComboBox(E[] items) {
        super(items);
        setFont(new Font("Segoe UI", Font.PLAIN, 13)); //tipografía uniforme
        setBackground(Color.LIGHT_GRAY); //fondo claro para mejor contraste
        setForeground(Color.BLACK); //texto visible en fondo claro
        setFocusable(false); //elimina borde de selección al hacer clic
        setCursor(new Cursor(Cursor.HAND_CURSOR)); //cambia el cursor a "mano"
        setOpaque(true); //permite que el fondo de color se muestre correctamente
    }
}
