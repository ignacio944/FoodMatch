import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.List;

public class BuscadorRecetas extends JFrame implements ActionListener {
    //ventana principal de búsqueda de recetas con filtros y campo de texto

    //componentes de interfaz
    private JFrame ventanaPrincipal;
    private JTextField buscarField;
    private JPanel resultadosPanel;
    private JScrollPane scrollPaneResultados;
    private Boton atrasButton;

    //sesión y conexión con base de datos
    private Sesion sesion;
    private Connection connection;
    private Usuario usuario;

    //filtros activos para tiempo, dificultad, calorías y macronutrientes
    private Integer filtroMinTiempo = null;
    private Integer filtroMaxTiempo = null;
    private String filtroDificultad = "";
    private Integer filtroMinCalorias = null;
    private Integer filtroMaxCalorias = null;
    private Double filtroMinProteinas = null;
    private Double filtroMaxProteinas = null;
    private Double filtroMinGlucidos = null;
    private Double filtroMaxGlucidos = null;
    private Double filtroMinLipidos = null;
    private Double filtroMaxLipidos = null;

    //constructor principal que recibe ventana anterior y sesión actual
    public BuscadorRecetas(String titulo, JFrame ventanaPrincipal, Sesion sesion) {
        super(titulo);
        this.ventanaPrincipal = ventanaPrincipal;
        this.sesion = sesion;
        this.connection = sesion.getConnection();
        this.usuario = sesion.getUsuario();
        crear(); //inicializa la interfaz gráfica
    }

    //método que construye la interfaz gráfica y organiza los componentes
    private void crear() {
        //campo de búsqueda con placeholder y actualización dinámica de sugerencias
        buscarField = new JTextField("Introducir receta de cocina", 35);
        buscarField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                actualizarSugerencias(); //cuando se inserta texto
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                actualizarSugerencias(); //cuando se elimina texto
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                actualizarSugerencias(); //cuando cambia el texto
            }
        });

        //comportamiento del campo cuando se enfoca o se pierde el foco
        buscarField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                //limpia el texto si contiene el mensaje por defecto
                if (buscarField.getText().equals("Introducir receta de cocina")) {
                    buscarField.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                //restaura el mensaje por defecto si está vacío
                if (buscarField.getText().isEmpty()) {
                    buscarField.setText("Introducir receta de cocina");
                }
            }
        });

        //panel donde se mostrarán los resultados de búsqueda
        resultadosPanel = new JPanel();
        resultadosPanel.setLayout(new BoxLayout(resultadosPanel, BoxLayout.Y_AXIS));

        //scroll que contiene el panel de resultados
        scrollPaneResultados = new JScrollPane(resultadosPanel);
        scrollPaneResultados.setPreferredSize(new Dimension(400, 300));
        scrollPaneResultados.setBorder(BorderFactory.createTitledBorder("Recetas encontradas"));

        //botón para volver a la ventana anterior
        atrasButton = new Boton("Atrás");
        atrasButton.addActionListener(this);
        atrasButton.setBackground(Color.lightGray);

        //botón para abrir el cuadro de diálogo de filtros avanzados
        Boton filtroButton = new Boton("Filtros avanzados");
        filtroButton.setBackground(Color.lightGray);
        filtroButton.addActionListener(e -> mostrarDialogoFiltros());

        //título de la ventana
        JLabel tituloLabel = new JLabel("Buscar recetas de cocina");
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 30));

        //organización del layout con GridBagLayout
        Container contenedor = getContentPane();
        contenedor.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);

        //añadir el título
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contenedor.add(tituloLabel, constraints);

        //añadir el campo de búsqueda
        constraints.gridy = 1;
        constraints.gridx = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contenedor.add(buscarField, constraints);

        //añadir botón de filtros avanzados
        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.NONE;
        contenedor.add(filtroButton, constraints);

        //añadir el scroll de resultados
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        contenedor.add(scrollPaneResultados, constraints);

        //añadir botón atrás
        constraints.gridy = 3;
        constraints.fill = GridBagConstraints.NONE;
        contenedor.add(atrasButton, constraints);

        //configuración final de la ventana
        setSize(650, 650);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.requestFocusInWindow();
    }

    //método que actualiza dinámicamente las sugerencias a medida que el usuario escribe
    private void actualizarSugerencias() {
        String texto = buscarField.getText().toLowerCase();

        //si el texto está vacío o es el placeholder, se limpian los resultados
        if (texto.isEmpty() || texto.equals("Introducir receta de cocina")) {
            resultadosPanel.removeAll();
            resultadosPanel.revalidate();
            resultadosPanel.repaint();
            return;
        }

        try {
            //se buscan recetas que coincidan con el nombre o creador
            ClaseReceta claseReceta = new ClaseReceta(connection);
            List<Receta> recetas = claseReceta.buscarRecetasPorNombreOCreador(texto);
            mostrarRecetasFiltradas(recetas); //muestra las recetas en pantalla
        } catch (SQLException e) {
            //mensaje de error si falla la consulta
            JOptionPane.showMessageDialog(this, "Error al buscar recetas: " + e.getMessage());
        }
    }


    private void mostrarDialogoFiltros() {
        JDialog dialogo = new JDialog(this, "Filtros de búsqueda", true);
        dialogo.setSize(350, 350);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        //tiempo
        gbc.gridx = 0; gbc.gridy = 0;
        dialogo.add(new JLabel("Tiempo (min)"), gbc);
        JTextField minTiempo = new JTextField(5);
        JTextField maxTiempo = new JTextField(5);
        if (filtroMinTiempo != null) minTiempo.setText(String.valueOf(filtroMinTiempo));
        if (filtroMaxTiempo != null) maxTiempo.setText(String.valueOf(filtroMaxTiempo));
        gbc.gridx = 1; dialogo.add(minTiempo, gbc);
        gbc.gridx = 2; dialogo.add(maxTiempo, gbc);

        //dificultad
        gbc.gridx = 0; gbc.gridy = 1;
        dialogo.add(new JLabel("Dificultad"), gbc);
        gbc.gridx = 1;
        String[] dificultades = {"", "Fácil", "Media", "Difícil"};
        ComboBox<String> dificultadCombo = new ComboBox<>(dificultades);
        dificultadCombo.setSelectedItem(filtroDificultad);
        gbc.gridwidth = 2;
        dialogo.add(dificultadCombo, gbc);
        gbc.gridwidth = 1;

        //calorías
        gbc.gridx = 0; gbc.gridy = 2;
        dialogo.add(new JLabel("Calorías (kcal)"), gbc);
        JTextField minCalorias = new JTextField(5);
        JTextField maxCalorias = new JTextField(5);
        if (filtroMinCalorias != null) minCalorias.setText(String.valueOf(filtroMinCalorias));
        if (filtroMaxCalorias != null) maxCalorias.setText(String.valueOf(filtroMaxCalorias));
        gbc.gridx = 1; dialogo.add(minCalorias, gbc);
        gbc.gridx = 2; dialogo.add(maxCalorias, gbc);

        //proteínas
        gbc.gridx = 0; gbc.gridy = 3;
        dialogo.add(new JLabel("Proteínas (g)"), gbc);
        JTextField minProteinas = new JTextField(5);
        JTextField maxProteinas = new JTextField(5);
        if (filtroMinProteinas != null) minProteinas.setText(String.valueOf(filtroMinProteinas));
        if (filtroMaxProteinas != null) maxProteinas.setText(String.valueOf(filtroMaxProteinas));
        gbc.gridx = 1; dialogo.add(minProteinas, gbc);
        gbc.gridx = 2; dialogo.add(maxProteinas, gbc);

        //glúcidos
        gbc.gridx = 0; gbc.gridy = 4;
        dialogo.add(new JLabel("Glúcidos (g)"), gbc);
        JTextField minGlucidos = new JTextField(5);
        JTextField maxGlucidos = new JTextField(5);
        if (filtroMinGlucidos != null) minGlucidos.setText(String.valueOf(filtroMinGlucidos));
        if (filtroMaxGlucidos != null) maxGlucidos.setText(String.valueOf(filtroMaxGlucidos));
        gbc.gridx = 1; dialogo.add(minGlucidos, gbc);
        gbc.gridx = 2; dialogo.add(maxGlucidos, gbc);

        //lípidos
        gbc.gridx = 0; gbc.gridy = 5;
        dialogo.add(new JLabel("Lípidos (g)"), gbc);
        JTextField minLipidos = new JTextField(5);
        JTextField maxLipidos = new JTextField(5);
        if (filtroMinLipidos != null) minLipidos.setText(String.valueOf(filtroMinLipidos));
        if (filtroMaxLipidos != null) maxLipidos.setText(String.valueOf(filtroMaxLipidos));
        gbc.gridx = 1; dialogo.add(minLipidos, gbc);
        gbc.gridx = 2; dialogo.add(maxLipidos, gbc);

        //botón eliminar filtros
        Boton eliminar = new Boton("Eliminar filtros");
        eliminar.addActionListener(e -> {
            filtroMinTiempo = filtroMaxTiempo = null;
            filtroMinCalorias = filtroMaxCalorias = null;
            filtroMinProteinas = filtroMaxProteinas = null;
            filtroMinGlucidos = filtroMaxGlucidos = null;
            filtroMinLipidos = filtroMaxLipidos = null;
            filtroDificultad = "";

            minTiempo.setText(""); maxTiempo.setText("");
            dificultadCombo.setSelectedIndex(0);
            minCalorias.setText(""); maxCalorias.setText("");
            minProteinas.setText(""); maxProteinas.setText("");
            minGlucidos.setText(""); maxGlucidos.setText("");
            minLipidos.setText(""); maxLipidos.setText("");

            actualizarSugerencias();
            dialogo.dispose();
        });

        //botón aplicar filtros
        Boton aplicar = new Boton("Aplicar filtros");
        aplicar.addActionListener(e -> {
            try {
                filtroMinTiempo = parseEntero(minTiempo.getText());
                filtroMaxTiempo = parseEntero(maxTiempo.getText());
                filtroMinCalorias = parseEntero(minCalorias.getText());
                filtroMaxCalorias = parseEntero(maxCalorias.getText());
                filtroMinProteinas = parseDouble(minProteinas.getText());
                filtroMaxProteinas = parseDouble(maxProteinas.getText());
                filtroMinGlucidos = parseDouble(minGlucidos.getText());
                filtroMaxGlucidos = parseDouble(maxGlucidos.getText());
                filtroMinLipidos = parseDouble(minLipidos.getText());
                filtroMaxLipidos = parseDouble(maxLipidos.getText());
                filtroDificultad = (String) dificultadCombo.getSelectedItem();

                aplicarFiltrosAvanzados(filtroMinTiempo, filtroMaxTiempo, filtroDificultad, filtroMinCalorias, filtroMaxCalorias);
                dialogo.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialogo, "Introduce valores numéricos válidos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        //añadir botones
        gbc.gridy = 6;
        gbc.gridx = 0; dialogo.add(eliminar, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; dialogo.add(aplicar, gbc);

        dialogo.setVisible(true);
    }

    //convierte un texto en entero, devuelve null si está vacío
    private Integer parseEntero(String texto) {
        return texto.isEmpty() ? null : Integer.parseInt(texto);
    }

    //convierte un texto en double, cambiando coma por punto si es necesario
    private Double parseDouble(String texto) {
        return texto.isEmpty() ? null : Double.parseDouble(texto.replace(",", "."));
    }

    //aplica filtros avanzados a las recetas buscadas por nombre o creador
    private void aplicarFiltrosAvanzados(Integer minTiempo, Integer maxTiempo, String dificultad,
                                        Integer minCalorias, Integer maxCalorias) {
        try {
            //instancia la clase que accede a las recetas en la base de datos
            ClaseReceta claseReceta = new ClaseReceta(connection);
            
            //obtiene y normaliza el texto introducido en el campo de búsqueda
            String texto = buscarField.getText().trim().toLowerCase();

            //si no se ha escrito nada o se dejó el texto por defecto, se limpia el panel de resultados
            if (texto.isEmpty() || texto.equals("introducir receta de cocina")) {
                resultadosPanel.removeAll(); //elimina componentes anteriores
                resultadosPanel.revalidate(); //actualiza la estructura del panel
                resultadosPanel.repaint(); //redibuja el panel
                return;
            }

            //busca recetas que coincidan con el texto (nombre o creador)
            List<Receta> recetas = claseReceta.buscarRecetasPorNombreOCreador(texto);

            //muestra solo las recetas que cumplen los filtros establecidos
            mostrarRecetasFiltradas(recetas);
        } catch (SQLException ex) {
            //muestra un mensaje de error si falla la consulta a la base de datos
            JOptionPane.showMessageDialog(this, "error al aplicar filtros: " + ex.getMessage());
        }
    }

    //muestra solo las recetas que cumplen todos los filtros activos
    private void mostrarRecetasFiltradas(List<Receta> recetas) throws SQLException {
        resultadosPanel.removeAll(); //limpia el panel de resultados anterior

        //instancia las clases necesarias para acceder a datos de usuario, alimento y receta
        ClaseUsuario claseUsuario = new ClaseUsuario(connection);
        ClaseAlimento claseAlimento = new ClaseAlimento(connection);
        ClaseReceta claseReceta = new ClaseReceta(connection);

        //itera sobre cada receta para verificar si cumple con los filtros
        for (Receta receta : recetas) {
            //filtra recetas que no sean aptas para el usuario (por restricciones u otros criterios)
            if (!claseReceta.esAptaPara(receta, usuario, claseAlimento)) continue;

            //filtros de tiempo de preparación
            if (filtroMinTiempo != null && receta.getTiempoPreparacion() < filtroMinTiempo) continue;
            if (filtroMaxTiempo != null && receta.getTiempoPreparacion() > filtroMaxTiempo) continue;

            //filtros de calorías
            if (filtroMinCalorias != null && receta.getValorEnergetico() < filtroMinCalorias) continue;
            if (filtroMaxCalorias != null && receta.getValorEnergetico() > filtroMaxCalorias) continue;

            //filtros de proteínas
            if (filtroMinProteinas != null && receta.getProteinas() < filtroMinProteinas) continue;
            if (filtroMaxProteinas != null && receta.getProteinas() > filtroMaxProteinas) continue;

            //filtros de glúcidos
            if (filtroMinGlucidos != null && receta.getGlucidos() < filtroMinGlucidos) continue;
            if (filtroMaxGlucidos != null && receta.getGlucidos() > filtroMaxGlucidos) continue;

            //filtros de lípidos
            if (filtroMinLipidos != null && receta.getLipidos() < filtroMinLipidos) continue;
            if (filtroMaxLipidos != null && receta.getLipidos() > filtroMaxLipidos) continue;

            //filtro por dificultad (comparación insensible a mayúsculas/minúsculas)
            if (filtroDificultad != null && !filtroDificultad.isEmpty()
                    && !receta.getDificultad().equalsIgnoreCase(filtroDificultad)) continue;

            //obtiene el nombre del creador de la receta a partir de su id
            String creador = claseUsuario.obtenerNombrePorId(receta.getCreadorId());

            //crea el panel visual para la receta
            JPanel recetaPanel = crearPanelReceta(receta, creador, claseReceta);

            //añade el panel de la receta al contenedor principal
            resultadosPanel.add(recetaPanel);
        }

        resultadosPanel.revalidate(); //actualiza el layout
        resultadosPanel.repaint(); //redibuja el panel con los nuevos resultados
    }

    //crea el panel visual con nombre, autor e imagen de una receta
    private JPanel crearPanelReceta(Receta receta, String creador, ClaseReceta claseReceta) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY)); //borde exterior
        panel.setBackground(Color.WHITE); //fondo blanco

        //crea etiqueta con el nombre de la receta y el nombre del creador
        JLabel textoLabel = new JLabel("<html><b>" + receta.getNombre() + "</b><br/>por " + creador + "</html>");
        textoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); //espaciado interno

        //crea etiqueta para mostrar la imagen de la receta
        JLabel imagenLabel = new JLabel();
        imagenLabel.setPreferredSize(new Dimension(100, 100)); //define tamaño de imagen
        imagenLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); //espaciado interno

        //carga la imagen de la receta en el label correspondiente
        CargaImagen.cargar(receta.getId(), claseReceta, imagenLabel);

        //añade los componentes al panel
        panel.add(imagenLabel, BorderLayout.WEST);
        panel.add(textoLabel, BorderLayout.CENTER);

        //abre la ventana con detalles de la receta al hacer clic en el panel
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new VentanaReceta(receta.getId(), BuscadorRecetas.this, sesion); //abre nueva ventana
                setVisible(false); //oculta la ventana actual
            }
        });

        return panel;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == atrasButton) { //volver a ventana principal al clicar atrás
            ventanaPrincipal.setVisible(true);
            dispose();
        }
    }
}
