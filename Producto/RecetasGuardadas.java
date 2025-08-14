import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

public class RecetasGuardadas extends JFrame implements ActionListener {
    //crear variables
    private Sesion sesion;
    private Usuario usuario;
    private ClaseReceta claseReceta;
    private JFrame ventanaAnterior;
    private JLabel tituloLabel;
    private JPanel guardadoPanel;
    private JScrollPane scrollPaneGuardado;
    private Boton atrasButton;


    //constructor
    public RecetasGuardadas(String titulo, JFrame ventanaAnterior, Sesion sesion) {
        super(titulo);
        this.sesion = sesion;
        this.usuario = sesion.getUsuario();
        this.claseReceta = new ClaseReceta(sesion.getConnection());
        this.ventanaAnterior = ventanaAnterior;

        crear();
        cargarRecetas();
    }

    private void crear() {
        //crear etiqueta del título y aplicar estilo
        tituloLabel = new JLabel("Recetas Guardadas");
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 24));

        //crear panel donde se mostrarán las recetas guardadas, con disposición vertical
        guardadoPanel = new JPanel();
        guardadoPanel.setLayout(new BoxLayout(guardadoPanel, BoxLayout.Y_AXIS));

        //envolver el panel de recetas en un scroll para manejar muchas recetas
        scrollPaneGuardado = new JScrollPane(guardadoPanel);
        scrollPaneGuardado.setPreferredSize(new Dimension(400, 350));

        //crear botón para volver a la ventana anterior
        atrasButton = new Boton("Atrás");
        atrasButton.addActionListener(this);
        atrasButton.setBackground(Color.LIGHT_GRAY);

        //obtener el contenedor principal de la ventana
        Container contenedor = getContentPane();
        contenedor.setLayout(new GridBagLayout()); //usar GridBagLayout para posicionamiento flexible
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(20, 20, 20, 20); //espaciado entre componentes

        //añadir el título en la parte superior centrada
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        contenedor.add(tituloLabel, constraints); //añade el JLabel al contenedor

        //añadir el scroll con las recetas debajo del título
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.BOTH; //que ocupe espacio horizontal y vertical
        contenedor.add(scrollPaneGuardado, constraints); //añade el JScrollPane al contenedor

        //añadir el botón "Atrás" en la parte inferior
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.NONE;
        contenedor.add(atrasButton, constraints); //añade el botón al contenedor

        //configurar propiedades de la ventana
        setSize(650, 650); //tamaño de la ventana
        setLocationRelativeTo(null); //centrar en pantalla
        setVisible(true); //mostrar la ventana
    }

    private void cargarRecetas() {
        //elimina todos los componentes actuales del panel para recargar desde cero
        guardadoPanel.removeAll();

        try {
            //obtiene la lista de recetas guardadas por el usuario actual
            List<Receta> recetas = claseReceta.obtenerRecetasGuardadas(usuario.getId());
            ClaseUsuario claseUsuario = new ClaseUsuario(sesion.getConnection());

            //si no hay recetas guardadas, se muestra un mensaje indicándolo
            if (recetas.isEmpty()) {
                guardadoPanel.add(new JLabel("No tienes recetas guardadas."));
            }

            //recorre cada receta guardada para mostrarla
            for (Receta receta : recetas) {
                //obtiene el nombre del creador de la receta
                String creador = claseUsuario.obtenerNombrePorId(receta.getCreadorId());

                //obtiene la media de valoraciones y el número total de votos
                double mediaValoracion = claseReceta.obtenerMediaValoracion(receta.getId());
                int votos = claseReceta.obtenerVotos(receta.getId());

                //crea un panel individual para la receta
                JPanel recetaPanel = new JPanel(new BorderLayout());
                recetaPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY)); //borde gris
                recetaPanel.setBackground(Color.WHITE); //fondo blanco

                //prepara el texto a mostrar: nombre, creador y valoración con estrellas
                String mensaje = "<html><b>" + receta.getNombre() + "</b><br/>por " + creador +
                                "<br/>⭐ " + String.format("%.1f", mediaValoracion) + " (" + votos + " votos)</html>";

                //etiqueta con el texto, con algo de relleno interno
                JLabel textoLabel = new JLabel(mensaje);
                textoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                //etiqueta para la imagen de la receta
                JLabel imagenLabel = new JLabel();
                imagenLabel.setPreferredSize(new Dimension(100, 100)); //tamaño fijo
                imagenLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                recetaPanel.add(imagenLabel, BorderLayout.WEST); //se añade aunque esté vacía

                //se carga la imagen de la receta desde la base de datos o disco
                CargaImagen.cargar(receta.getId(), claseReceta, imagenLabel);

                //botón para quitar la receta de los guardados
                JButton desguardarBtn = new JButton("❌");
                desguardarBtn.setToolTipText("Eliminar de guardados"); //texto emergente
                desguardarBtn.setBorderPainted(false);
                desguardarBtn.setFocusPainted(false);
                desguardarBtn.setContentAreaFilled(false);
                desguardarBtn.setForeground(Color.RED); //color rojo para indicar acción destructiva

                //al pulsar el botón, se pide confirmación al usuario y se elimina la receta
                final int idReceta = receta.getId();
                desguardarBtn.addActionListener(ev -> {
                    int confirm = JOptionPane.showConfirmDialog(this,
                        "¿Deseas quitar la receta \"" + receta.getNombre() + "\" de tus guardados?",
                        "Confirmar", JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            claseReceta.desguardarReceta(usuario.getId(), idReceta);
                            cargarRecetas(); //recarga la lista para reflejar el cambio
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(this, "Error al quitar receta: " + ex.getMessage());
                        }
                    }
                });

                //panel para agrupar el texto y el botón de quitar
                JPanel textoYBoton = new JPanel(new BorderLayout());
                textoYBoton.add(textoLabel, BorderLayout.CENTER); //texto centrado
                textoYBoton.add(desguardarBtn, BorderLayout.EAST); //botón alineado a la derecha

                //se añade todo al panel de la receta
                recetaPanel.add(imagenLabel, BorderLayout.WEST); //imagen a la izquierda
                recetaPanel.add(textoYBoton, BorderLayout.CENTER); //texto y botón en el centro

                //al hacer clic en el panel (pero no en el botón), se abre la vista de la receta
                recetaPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getSource() != desguardarBtn) {
                            new VentanaReceta(idReceta, RecetasGuardadas.this, sesion);
                            setVisible(false); //oculta la ventana actual
                        }
                    }
                });

                //se añade el panel de la receta al contenedor principal
                guardadoPanel.add(recetaPanel);
            }

            //se actualiza el panel tras haber añadido todos los componentes
            guardadoPanel.revalidate();
            guardadoPanel.repaint();

        } catch (SQLException e) {
            //en caso de error de base de datos, se muestra un mensaje
            JOptionPane.showMessageDialog(this, "Error al cargar recetas: " + e.getMessage());
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        //si se pulsa el botón "Atrás"
        if (e.getSource() == atrasButton) {
            //se vuelve visible la ventana anterior
            ventanaAnterior.setVisible(true);
            //se cierra la ventana actual
            dispose();
        }
    }

}