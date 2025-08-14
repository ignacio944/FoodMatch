import javax.swing.*;
import java.awt.*;

//clase que extiende jbutton para aplicar un diseño personalizado con estilo moderno
public class Boton extends JButton {

    //constructor que recibe el texto del botón y aplica personalización visual
    public Boton(String texto) {
        super(texto);
        setFont(new Font("", Font.PLAIN, 13)); //fuente estándar y legible
        setBackground(Color.LIGHT_GRAY); //color de fondo claro
        setForeground(Color.BLACK); //color de texto negro para buen contraste
        setFocusPainted(false); //elimina el borde azul de enfoque al hacer clic
        setCursor(new Cursor(Cursor.HAND_CURSOR)); //cambia el cursor al pasar sobre el botón
        setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14)); //espaciado interno personalizado
        setContentAreaFilled(false); //permite pintar el fondo manualmente (usado en paintComponent)
        setOpaque(false); //hace que el fondo sea transparente hasta que se pinte
    }

    //sobrescribe paintcomponent para dibujar un botón con bordes redondeados
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(getBackground());
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); //dibuja un rectángulo redondeado
        super.paintComponent(g);
        g2.dispose();
    }

}
