import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;



public class VentanaReceta extends JFrame implements ActionListener, FocusListener {
    //crear variables
    private JLabel tituloLabel;
    private JLabel creadorLabel;
    private JTextArea ingredientesTextArea;
    private JTextArea pasosTextArea;
    private JTextField personasTextField;
    private JLabel valoracionLabel;
    private Boton votarButton;
    private Boton atrasButton;
    private Boton guardarEstadoButton; 
    private JFrame ventanaAnterior;
    private JLabel imagenLabel;
    private Usuario usuario;
    private ClaseReceta claseReceta;
    private ClaseUsuario claseUsuario;
    private Receta receta;


    //constructor
    public VentanaReceta(int idReceta, JFrame ventanaAnterior, Sesion sesion) {
        super("Receta");
        this.ventanaAnterior = ventanaAnterior;
        this.usuario = sesion.getUsuario();
        this.claseReceta = new ClaseReceta(sesion.getConnection());
        this.claseUsuario = new ClaseUsuario(sesion.getConnection());

        try {
            this.receta = claseReceta.obtenerRecetaPorId(idReceta);
            claseReceta.añadirRecetaAlHistorial(usuario.getId(), receta.getId());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar la receta: " + e.getMessage());
            this.dispose(); // cerrar ventana si no se puede continuar
            return;
        }

        crear(); // construir interfaz
        }

        

    private void crear() { //crear ventana
        //variables para almacenar los datos de la receta
        String nombreReceta = receta.getNombre();
        String creador = "";
        try {
            creador = claseUsuario.obtenerNombrePorId(receta.getCreadorId());
        } catch (SQLException e) {
            creador = "Desconocido";
        }
        String dificultad = receta.getDificultad();
        int tiempoProduccion = receta.getTiempoPreparacion();
        boolean vegano = receta.isVegano();
        boolean vegetariano = receta.isVegetariano();
        boolean celiaco = receta.isCeliaco();
        float valoracion = receta.getValoracion();
        int numeroVotaciones = receta.getVotos();
        int valor_energetico = receta.getValorEnergetico();
    
        //asignar los datos a los componentes de la UI
        tituloLabel = new JLabel(nombreReceta);
        tituloLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));

        creadorLabel = new JLabel("Creador: " + creador);
        creadorLabel.setFont(new Font("Arial", Font.PLAIN, 18));
    
        //crear el ComboBox de propiedades
        ComboBox<String> propiedades = new ComboBox<>(new String[] {
            "Propiedades",
            "Dificultad: " + dificultad,
            "Tiempo de Producción: " + tiempoProduccion + " minutos",
            "Apto para celiacos: " + (celiaco ? "Sí" : "No"),
            "Apto para veganos: " + (vegano ? "Sí" : "No"),
            "Apto para vegetarianos: " + (vegetariano ? "Sí" : "No"),
            "Valor energético: " + valor_energetico + " kcal/persona",
            "Proteínas: " + String.format("%.2f", receta.getProteinas()) + " g/persona",
            "Glúcidos: " + String.format("%.2f", receta.getGlucidos()) + " g/persona",
            "Lípidos: " + String.format("%.2f", receta.getLipidos()) + " g/persona"
        });

    
        valoracionLabel = new JLabel("Valoración: " + String.format("%.2f", valoracion) + " / 5 (" + numeroVotaciones + ")");
    
        //botón para guardar el estado de la receta
        guardarEstadoButton = new Boton("");
        try {
            if (claseReceta.estaGuardada(usuario.getId(), receta.getId())) {
                guardarEstadoButton.setText("Desguardar");
            } else {
                guardarEstadoButton.setText("Guardar");
            }
        } catch (SQLException e) {
            guardarEstadoButton.setText("Guardar");
        }

        imagenLabel = new JLabel();

        try {
            byte[] imagenBytes = receta.getImagen();
            if (imagenBytes != null) {
                Image imagenEscalada = new ImageIcon(imagenBytes).getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                imagenLabel.setIcon(new ImageIcon(imagenEscalada));
            } else {
                imagenLabel.setIcon(null); // sin imagen
            }
        } catch (Exception e) {
            imagenLabel.setIcon(null);
        }

        guardarEstadoButton.addActionListener(e -> alternarGuardado());
    
        ingredientesTextArea = new JTextArea(10, 20);
        ingredientesTextArea.setEditable(false);
        JScrollPane scrollPaneIngredientes = new JScrollPane(ingredientesTextArea);
        scrollPaneIngredientes.setBorder(BorderFactory.createTitledBorder("Ingredientes"));
    
        pasosTextArea = new JTextArea(10, 20);
        pasosTextArea.setEditable(false);
        JScrollPane scrollPanePasos = new JScrollPane(pasosTextArea);
        scrollPanePasos.setBorder(BorderFactory.createTitledBorder("Procedimiento"));
    
        personasTextField = new JTextField("1", 10);
        JLabel personasLabel = new JLabel("persona/s");
        personasTextField.addFocusListener(this);
        personasTextField.addActionListener(e -> actualizarCantidades());
    
        JPanel personas = new JPanel(new FlowLayout(FlowLayout.CENTER));
        personas.add(personasTextField);
        personas.add(personasLabel);
        
        votarButton = new Boton("Valorar");
        votarButton.addActionListener(e -> valorar());
    
        JPanel valoracionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        valoracionPanel.add(valoracionLabel);
    
        atrasButton = new Boton("Atrás");
        atrasButton.addActionListener(this);
    
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botonesPanel.add(atrasButton);
        botonesPanel.add(votarButton);
    
        guardarEstadoButton.setBackground(Color.lightGray);
        votarButton.setBackground(Color.lightGray);
        atrasButton.setBackground(Color.lightGray);
        propiedades.setBackground(Color.lightGray);
    
        Container contenedor = getContentPane(); //crear contenedor
        contenedor.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);
    

        //Nuevo panel superior (título + imagen juntos)
        JPanel panelSuperior = new JPanel(new BorderLayout());

        // panel izquierdo: texto alineado verticalmente
        Box textoBox = Box.createVerticalBox();
        textoBox.add(tituloLabel);
        textoBox.add(Box.createVerticalStrut(5));
        textoBox.add(creadorLabel);
        textoBox.add(Box.createVerticalStrut(5));
        textoBox.add(valoracionPanel);
        textoBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // panel derecho: imagen redimensionada
        imagenLabel.setPreferredSize(new Dimension(150, 150));
        imagenLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagenLabel.setVerticalAlignment(SwingConstants.CENTER);
        imagenLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY)); // marco opcional

        panelSuperior.add(textoBox, BorderLayout.WEST);
        panelSuperior.add(imagenLabel, BorderLayout.EAST);

        // añadir este panel completo a la ventana en fila 0
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contenedor.add(panelSuperior, constraints);

    
        //añadir el ComboBox de propiedades a la ventana
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.WEST;
        contenedor.add(propiedades, constraints);
    
        //añadir boton de guardado a la ventana
        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.CENTER;
        contenedor.add(guardarEstadoButton, constraints);

        //añadir panel de ingredientes a la ventana
        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.fill = GridBagConstraints.BOTH;
        contenedor.add(scrollPaneIngredientes, constraints);
    
        //añadir panel de procedimiento a la ventana
        constraints.gridx = 1;
        contenedor.add(scrollPanePasos, constraints);
    
        //añadir panel de personas a la ventana
        constraints.gridx = 0;
        constraints.gridy = 7;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        contenedor.add(personas, constraints);
    
        //añadir botones a la ventana
        constraints.gridy = 8;
        contenedor.add(botonesPanel, constraints);
    
            
        //obtener los ingredientes
        mostrarIngredientesYPasos();
        setSize(700,700);
        setLocationRelativeTo(null); //centrar ventana en la pantalla del ordenador
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.requestFocusInWindow(); //que no este el focus en ninguna parte
    }

    //método para alternar el estado al hacer clic en el botón
    private void alternarGuardado() {
        try {
            boolean guardada = claseReceta.estaGuardada(usuario.getId(), receta.getId());
    
            if (guardada) {
                claseReceta.desguardarReceta(usuario.getId(), receta.getId());
                guardarEstadoButton.setText("Guardar");
            } else {
                claseReceta.guardarReceta(usuario.getId(), receta.getId());
                guardarEstadoButton.setText("Desguardar");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar el estado de la receta.");
        }
    }
        
    private void mostrarIngredientesYPasos() {
        try {
            List<String> ingredientes = claseReceta.obtenerIngredientesConCantidad(receta.getId());
            String procedimiento = claseReceta.obtenerPasosProcedimiento(receta.getId());

            ingredientesTextArea.setText(String.join("\n", ingredientes));
            pasosTextArea.setText(formatearPasos(procedimiento));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al obtener los ingredientes/pasos: " + e.getMessage());
        }
        }

    
    private String formatearPasos(String pasos) {
        pasos = pasos.replaceAll("(\\d+\\.)", "\n$1");
        if (pasos.startsWith("\n1.")) {
            pasos = pasos.substring(1);
        }
        return pasos;
    }
    
    private void actualizarCantidades() {
        try {
            int personas = Integer.parseInt(personasTextField.getText().trim());
            if (personas < 1) throw new NumberFormatException();

            List<String[]> ingredientes = claseReceta.obtenerIngredientesYUnidades(receta.getId());
            StringBuilder sb = new StringBuilder();

            for (String[] ing : ingredientes) {
                String nombre = ing[0];
                String cantidad = ing[1];

                String unidad = cantidad.replaceAll("[\\d.]", "").trim().toLowerCase();

                if (unidad.equals("a ojo")) {
                    sb.append(nombre).append(": a ojo\n");
                } else {
                    try {
                        double valorBase = Double.parseDouble(cantidad.replaceAll("[^\\d.]", ""));
                        double total = valorBase * personas;

                        sb.append(nombre)
                        .append(": ")
                        .append(total % 1 == 0 ? (int) total : String.format("%.2f", total))
                        .append(" ")
                        .append(unidad)
                        .append("\n");
                    } catch (NumberFormatException ex) {
                        sb.append(nombre).append(": ").append(cantidad).append(" (formato no numérico)\n");
                    }
                }
            }

            ingredientesTextArea.setText(sb.toString());

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Introduce un número válido de personas.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al obtener ingredientes: " + e.getMessage());
        }
    }

    
    
    private void valorar() {
        try {
            if (claseReceta.yaHaValorado(usuario.getId(), receta.getId())) {
                JOptionPane.showMessageDialog(this, "Ya ha valorado esta receta.", "Información", JOptionPane.INFORMATION_MESSAGE);
            } else {
                String input = JOptionPane.showInputDialog(this, "Ingrese una valoración entre 0 y 5:", "Valorar Receta", JOptionPane.PLAIN_MESSAGE);
                if (input != null) {
                    try {
                        double valor = Double.parseDouble(input);
                        if (valor < 0 || valor > 5) {
                            JOptionPane.showMessageDialog(this, "La valoración debe estar entre 0 y 5.", "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            claseReceta.registrarValoracion(usuario.getId(), receta.getId(), valor);
                            double nuevaMedia = claseReceta.obtenerMediaValoracion(receta.getId());
                            int votos = claseReceta.obtenerVotos(receta.getId());
    
                            valoracionLabel.setText("Valoración: " + String.format("%.2f", nuevaMedia) + " / 5" + " (" + votos + " votos)");
                            JOptionPane.showMessageDialog(this, "Valoración realizada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Debe ingresar un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al procesar valoración: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == atrasButton) {
            ventanaAnterior.setVisible(true);
            this.dispose();
        } else if (e.getSource() == guardarEstadoButton) {
            try {
                if (claseReceta.estaGuardada(usuario.getId(), receta.getId())) {
                    claseReceta.desguardarReceta(usuario.getId(), receta.getId());
                    guardarEstadoButton.setText("Guardar");
                } else {
                    claseReceta.guardarReceta(usuario.getId(), receta.getId());
                    guardarEstadoButton.setText("Desguardar");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al actualizar el estado de guardado.");
            }
        } else if (e.getSource() == votarButton) {
            valorar();
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
        actualizarCantidades(); //cada vez que se pierda el foco en la pantalla
    }
}
