import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.List;

public class HistorialRecetas extends JFrame implements ActionListener {
    //componentes de la interfaz y datos necesarios
    private JFrame ventanaPrincipal;
    private JLabel tituloLabel;
    private JPanel historialPanel;
    private JScrollPane scrollPaneHistorial;
    private Boton atrasButton, borrarHistorialButton;
    private int id_usuario;
    private Connection connection;
    private Sesion sesion;
    private ClaseReceta claseReceta;

    //constructor
    public HistorialRecetas(String titulo, JFrame ventanaPrincipal, Sesion sesion) {
        super(titulo);
        this.ventanaPrincipal = ventanaPrincipal;
        this.sesion = sesion;
        this.id_usuario = sesion.getUsuario().getId();
        this.connection = sesion.getConnection();
        this.claseReceta = new ClaseReceta(sesion.getConnection());

        crear();
    }

    private void crear() {
        tituloLabel = new JLabel("Recetas Vistas");
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 24));

        historialPanel = new JPanel();
        historialPanel.setLayout(new BoxLayout(historialPanel, BoxLayout.Y_AXIS));
        scrollPaneHistorial = new JScrollPane(historialPanel);
        scrollPaneHistorial.setPreferredSize(new Dimension(400, 350));

        try {
            List<Receta> recetas = claseReceta.obtenerRecetasVistas(id_usuario);
            ClaseUsuario claseUsuario = new ClaseUsuario(connection);
            ClaseAlimento claseAlimento = new ClaseAlimento(connection);
            Usuario usuario = sesion.getUsuario();

            for (Receta receta : recetas) {
                if (!claseReceta.esAptaPara(receta, usuario, claseAlimento)) continue;

                String creador = claseUsuario.obtenerNombrePorId(receta.getCreadorId());

                JPanel recetaPanel = new JPanel(new BorderLayout());
                recetaPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                recetaPanel.setBackground(Color.WHITE);

                double mediaValoracion = claseReceta.obtenerMediaValoracion(receta.getId());
                int votos = claseReceta.obtenerVotos(receta.getId());

                String mensaje = "<html><b>" + receta.getNombre() + "</b><br/>por " + creador +
                                "<br/>⭐ " + String.format("%.1f", mediaValoracion) + " (" + votos + " votos)</html>";

                JLabel textoLabel = new JLabel(mensaje);
                textoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                JLabel imagenLabel = new JLabel();
                imagenLabel.setPreferredSize(new Dimension(100, 100));
                imagenLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                recetaPanel.add(imagenLabel, BorderLayout.WEST); //se añade etiqueta de imagen vacía

                CargaImagen.cargar(receta.getId(), claseReceta, imagenLabel); //se carga la imagen de la receta
                recetaPanel.add(imagenLabel, BorderLayout.WEST); //se añade imagen cargada al panel
                recetaPanel.add(textoLabel, BorderLayout.CENTER); //se añade el texto al centro del panel

                int idReceta = receta.getId();
                recetaPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        new VentanaReceta(idReceta, HistorialRecetas.this, sesion); //se abre ventana de receta
                        setVisible(false); //se oculta esta ventana
                    }
                });

                historialPanel.add(recetaPanel); //se añade el panel de receta al historial
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar el historial: " + e.getMessage());
        }

        //boton de borrar historial y botón de atrás
        borrarHistorialButton = new Boton("Borrar Historial");
        borrarHistorialButton.addActionListener(this);
        borrarHistorialButton.setBackground(Color.LIGHT_GRAY);

        atrasButton = new Boton("Atrás");
        atrasButton.addActionListener(this);
        atrasButton.setBackground(Color.LIGHT_GRAY);

        //configuración de la ventana
        Container contenedor = getContentPane();
        contenedor.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(20, 20, 20, 20);

        //añadir titulo
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        contenedor.add(tituloLabel, constraints); //se añade el título

        //añadir panel de historial
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.BOTH;
        contenedor.add(scrollPaneHistorial, constraints); //se añade el scroll con recetas

        //añadir botones
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.NONE;
        contenedor.add(atrasButton, constraints); //se añade botón atrás

        //añadir botón de borrar historial
        constraints.gridy = 3;
        contenedor.add(borrarHistorialButton, constraints); //se añade botón borrar historial

        //configuración final de la ventana
        setSize(650, 650);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.requestFocusInWindow();
    }

    private void borrarHistorial() { //borrar historial
        try {
            claseReceta.borrarHistorial(id_usuario); //se borra historial de la base de datos
            historialPanel.removeAll(); //se limpian los elementos del panel
            historialPanel.revalidate(); //se revalida para refrescar layout
            historialPanel.repaint(); //se repinta la interfaz
        } catch (SQLException e) {
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == atrasButton) {
            ventanaPrincipal.setVisible(true); //se muestra ventana anterior
            dispose(); //se cierra esta ventana
        } else if (e.getSource() == borrarHistorialButton) {
            borrarHistorial(); //se borra el historial si se pulsa el botón
        } else {
            int id_receta = Integer.parseInt(e.getActionCommand());
            new VentanaReceta(id_receta, this, sesion); //se abre receta seleccionada
            this.setVisible(false);
        }
    }
}
