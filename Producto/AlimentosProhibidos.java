import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//clase que permite seleccionar y gestionar alimentos prohibidos para el usuario
public class AlimentosProhibidos extends JFrame implements ActionListener {
    //componentes de interfaz y datos
    private JTextField buscarField;
    private JPanel seleccionadosPanel;
    private JPopupMenu sugerenciasMenu;
    private List<Alimento> alimentos;
    private List<Alimento> seleccionados;    
    private Boton aceptarButton;
    private JFrame ventanaAnterior;
    private Usuario usuario;
    private ClaseAlimento claseAlimento;
    private Connection connection; //conexión a la base de datos
    private Sesion sesion;

    //constructor que inicializa la interfaz y recupera datos de sesión y usuario
    public AlimentosProhibidos(String titulo, JFrame ventanaAnterior, Sesion sesion) {
        super(titulo);
        try {
            this.sesion = sesion;
            this.usuario = this.sesion.getUsuario();
            this.connection = this.sesion.getConnection();

            this.ventanaAnterior = ventanaAnterior;
            this.claseAlimento = new ClaseAlimento(connection);
            this.alimentos = claseAlimento.obtenerTodosLosAlimentos();
            this.seleccionados = claseAlimento.obtenerAlimentosProhibidos(usuario.getId());
        } catch (SQLException e) {
            this.alimentos = new ArrayList<>();
            this.seleccionados = new ArrayList<>();
        }
        crear();
    }

    //configura los componentes gráficos de la ventana
    private void crear() {
        JLabel tituloLabel = new JLabel("Buscar Alimentos Prohibidos");
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 30)); 

        buscarField = new JTextField("Introducir alimento");

        //borra el texto de ayuda cuando el campo gana foco
        buscarField.addFocusListener(new FocusListener() {
            @Override 
            public void focusGained(FocusEvent e) {
                if (buscarField.getText().equals("Introducir alimento")) {
                    buscarField.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (buscarField.getText().isEmpty()) {
                    buscarField.setText("Introducir alimento");
                }
            }
        });

        //panel que muestra los alimentos seleccionados
        seleccionadosPanel = new JPanel();
        seleccionadosPanel.setLayout(new BoxLayout(seleccionadosPanel, BoxLayout.Y_AXIS));
        seleccionadosPanel.setBackground(Color.WHITE);
        JScrollPane scrollPaneSeleccionados = new JScrollPane(seleccionadosPanel);
        scrollPaneSeleccionados.setPreferredSize(new Dimension(50, 300));
        scrollPaneSeleccionados.setBorder(BorderFactory.createTitledBorder("Alimentos Seleccionados"));

        sugerenciasMenu = new JPopupMenu();

        aceptarButton = new Boton("Aceptar");
        aceptarButton.addActionListener(this); 
        aceptarButton.setBackground(Color.LIGHT_GRAY);

        //diseño con gridbaglayout
        Container contenedor = getContentPane();
        contenedor.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20); 

        //añadir titulo a la ventana
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contenedor.add(tituloLabel, gbc);

        //añadir buscador a la ventana
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contenedor.add(buscarField, gbc);

        //añadir panel de alimentos seleccionados
        gbc.gridy = 3;
        contenedor.add(scrollPaneSeleccionados, gbc);

        //añadir botón de aceptar
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        contenedor.add(aceptarButton, gbc);

        setSize(650,650); 
        setLocationRelativeTo(null); 
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE); 
        this.requestFocusInWindow(); 

        //mostrar selección inicial del usuario
        mostrarAlimentosSeleccionados();

        //escucha cambios en el campo de búsqueda
        buscarField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarSugerencias(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarSugerencias(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarSugerencias(); }
        });
    }

    //actualiza los botones con los alimentos seleccionados
    private void mostrarAlimentosSeleccionados() {
        seleccionadosPanel.removeAll();
        for (Alimento alimento : seleccionados) {
            JButton alimentoButton = new JButton(alimento.getNombre());
            alimentoButton.setBackground(Color.LIGHT_GRAY);
            alimentoButton.addActionListener(e -> quitarAlimento(alimento, alimentoButton));
            seleccionadosPanel.add(alimentoButton);
        }
        seleccionadosPanel.revalidate();
        seleccionadosPanel.repaint();
    }

    //muestra sugerencias en el menú desplegable
    private void actualizarSugerencias() {
        String texto = buscarField.getText().toLowerCase();
        sugerenciasMenu.setVisible(false); 
        sugerenciasMenu.removeAll(); 

        if (!texto.isEmpty() && !texto.equals("Introducir alimento")) {
            for (Alimento alimento : alimentos) {
                String nombre = alimento.getNombre().toLowerCase();

                //evitar duplicados
                boolean yaSeleccionado = false;
                for (Alimento s : seleccionados) {
                    if (s.getNombre().equalsIgnoreCase(alimento.getNombre())) {
                        yaSeleccionado = true;
                        break;
                    }
                }

                //filtrar según dieta del usuario
                boolean incompatible = 
                    (usuario.isVegano() && !alimento.isVegano()) ||
                    (usuario.isVegetariano() && !alimento.isVegetariano()) ||
                    (usuario.isCeliaco() && !alimento.isCeliaco());

                //añadir sugerencia válida al menú
                if (nombre.startsWith(texto) && !yaSeleccionado && !incompatible) {
                    JMenuItem alimentoSugerido = new JMenuItem(alimento.getNombre());
                    alimentoSugerido.setBackground(Color.LIGHT_GRAY);
                    alimentoSugerido.addActionListener(e -> agregarAlimento(alimento));
                    sugerenciasMenu.add(alimentoSugerido);
                }
            }

            if (sugerenciasMenu.getComponentCount() > 0) {
                Point location = buscarField.getLocationOnScreen();
                sugerenciasMenu.show(this,
                    location.x - this.getLocationOnScreen().x,
                    location.y - this.getLocationOnScreen().y + buscarField.getHeight());
                buscarField.requestFocusInWindow();
            }
        }
    }

    //agrega alimento a la lista de seleccionados
    private void agregarAlimento(Alimento alimento) {
        seleccionados.add(alimento);
        JButton alimentoButton = new JButton(alimento.getNombre());
        alimentoButton.setBackground(Color.LIGHT_GRAY);
        alimentoButton.addActionListener(e -> quitarAlimento(alimento, alimentoButton));
        seleccionadosPanel.add(alimentoButton);
        seleccionadosPanel.revalidate();
        seleccionadosPanel.repaint();
        sugerenciasMenu.setVisible(false);
    }

    //elimina alimento del panel y de la base de datos
    private void quitarAlimento(Alimento alimento, JButton button) {
        seleccionados.remove(alimento);
        seleccionadosPanel.remove(button);
        seleccionadosPanel.revalidate();
        seleccionadosPanel.repaint();
        try {
            claseAlimento.eliminarAlimentoProhibido(alimento, usuario.getId());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar el alimento prohibido: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        };
    }

    //guarda la selección y vuelve a la ventana anterior
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == aceptarButton) {
            try {
                claseAlimento.guardarAlimentosProhibidos(seleccionados, usuario.getId());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al actualizar alimentos prohibidos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            ventanaAnterior.setVisible(true); 
            dispose(); 
        }
    }  
}

