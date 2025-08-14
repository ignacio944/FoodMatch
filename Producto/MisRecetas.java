import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

public class MisRecetas extends JFrame implements ActionListener {
    private Sesion sesion;
    private Usuario usuario;
    private ClaseReceta claseReceta;
    private JFrame ventanaAnterior;
    private JLabel tituloLabel;
    private JPanel recetasPanel;
    private JScrollPane scrollPane;
    private Boton atrasButton;

    public MisRecetas(String titulo, JFrame ventanaAnterior, Sesion sesion) {
        super(titulo);
        this.sesion = sesion;
        this.usuario = sesion.getUsuario();
        this.claseReceta = new ClaseReceta(sesion.getConnection());
        this.ventanaAnterior = ventanaAnterior;

        crear(); //inicializa y configura la interfaz
        cargarMisRecetas(); //carga las recetas del usuario en pantalla
    }

    private void crear() {
        tituloLabel = new JLabel("Mis Recetas");
        tituloLabel.setFont(new Font("arial", Font.BOLD, 24));

        recetasPanel = new JPanel();
        recetasPanel.setLayout(new BoxLayout(recetasPanel, BoxLayout.Y_AXIS)); //usa boxlayout para listar recetas verticalmente
        scrollPane = new JScrollPane(recetasPanel); //añade scroll si hay muchas recetas
        scrollPane.setPreferredSize(new Dimension(400, 350));

        atrasButton = new Boton("Atrás");
        atrasButton.addActionListener(this);
        atrasButton.setBackground(Color.LIGHT_GRAY);

        Container contenedor = getContentPane(); //contenedor principal
        contenedor.setLayout(new GridBagLayout()); //usa gridbaglayout para controlar mejor posiciones
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(20, 20, 20, 20); //espaciado entre componentes

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        contenedor.add(tituloLabel, constraints); //añade el título arriba

        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.BOTH;
        contenedor.add(scrollPane, constraints); //añade el panel de recetas con scroll

        constraints.gridy = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.NONE;
        contenedor.add(atrasButton, constraints); //añade botón de volver

        setSize(650, 650);
        setLocationRelativeTo(null); //centra la ventana
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.requestFocusInWindow();
    }

    private void cargarMisRecetas() {
        recetasPanel.removeAll(); //limpia el panel antes de volver a cargar

        try {
            List<Receta> recetasDelUsuario = claseReceta.obtenerRecetasDelUsuario(usuario.getId()); //obtiene recetas del usuario
            for (Receta receta : recetasDelUsuario) {
                if (receta.getCreadorId() == usuario.getId()) {
                    double mediaValoracion = claseReceta.obtenerMediaValoracion(receta.getId()); //valoración media
                    int votos = claseReceta.obtenerVotos(receta.getId()); //número de votos

                    JPanel recetaPanel = new JPanel(new BorderLayout());
                    recetaPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                    recetaPanel.setBackground(Color.WHITE);

                    String mensaje = "<html><b>" + receta.getNombre() + "</b><br/>⭐ "
                            + String.format("%.1f", mediaValoracion) + " (" + votos + " votos)</html>";

                    JLabel textoLabel = new JLabel(mensaje);
                    textoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    JLabel imagenLabel = new JLabel(); //se llenará luego con la imagen de la receta
                    imagenLabel.setPreferredSize(new Dimension(100, 100));
                    imagenLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                    CargaImagen.cargar(receta.getId(), claseReceta, imagenLabel); //carga la imagen con swingworker

                    JButton eliminarBtn = new JButton("❌"); //botón para eliminar receta
                    eliminarBtn.setForeground(Color.RED);
                    eliminarBtn.setToolTipText("Eliminar receta");
                    eliminarBtn.setBorderPainted(false);
                    eliminarBtn.setFocusPainted(false);
                    eliminarBtn.setContentAreaFilled(false);

                    eliminarBtn.addActionListener(ev -> {
                        int confirm = JOptionPane.showConfirmDialog(this,
                                "¿Deseas eliminar la receta \"" + receta.getNombre() + "\"?",
                                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

                        if (confirm == JOptionPane.YES_OPTION) {
                            try {
                                claseReceta.eliminarReceta(receta.getId()); //borra receta de bd
                                cargarMisRecetas(); //recarga después de eliminar
                            } catch (SQLException ex) {
                                JOptionPane.showMessageDialog(this, "Error al eliminar receta: " + ex.getMessage());
                            }
                        }
                    });

                    JPanel topPanel = new JPanel(new BorderLayout());
                    topPanel.add(textoLabel, BorderLayout.CENTER);
                    topPanel.add(eliminarBtn, BorderLayout.EAST); //texto a la izquierda, botón eliminar a la derecha

                    recetaPanel.add(imagenLabel, BorderLayout.WEST); //imagen a la izquierda
                    recetaPanel.add(topPanel, BorderLayout.CENTER); //info y botón en el centro

                    final int idReceta = receta.getId();
                    recetaPanel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (e.getSource() != eliminarBtn) {
                                new VentanaReceta(idReceta, MisRecetas.this, sesion); //abre ventana de receta
                                setVisible(false); //oculta la actual
                            }
                        }
                    });

                    recetasPanel.add(recetaPanel); //añade receta al panel
                }
            }

            recetasPanel.revalidate(); //actualiza el layout
            recetasPanel.repaint(); //redibuja
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar recetas: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == atrasButton) {
            ventanaAnterior.setVisible(true); //vuelve a la ventana anterior
            dispose(); //cierra esta ventana
        }
    }
}

