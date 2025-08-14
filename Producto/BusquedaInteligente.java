import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class BusquedaInteligente extends JFrame implements ActionListener {
    //componentes de la interfaz
    private JFrame ventanaAnterior;
    private JLabel tituloLabel;
    private JTextField buscarTextField;
    private JPanel ingredientesPanel;
    private JScrollPane recetasScrollPane;
    private Boton atrasButton;
    private JPanel recetasPanel;
    private JPopupMenu sugerenciasMenu;
    private Sesion sesion;
    private Connection connection;
    private List<Alimento> alimentos;
    private List<AlimentoDisponible> disponibles;
    private ClaseAlimento claseAlimento;
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


    public BusquedaInteligente(String titulo, JFrame ventanaAnterior, Sesion sesion) {
        this.sesion = sesion;
        this.connection = sesion.getConnection();
        this.ventanaAnterior = ventanaAnterior;
        this.disponibles = new ArrayList<>();

        try {
            this.claseAlimento = new ClaseAlimento(connection);
            this.alimentos = claseAlimento.obtenerTodosLosAlimentos();
        } catch (SQLException e) {
            this.alimentos = new ArrayList<>();
        }

        crear();
    }

    private void crear() {
        //establece el color de fondo del contenedor principal
        getContentPane().setBackground(new Color(245, 245, 245));

        //crea y configura el título principal
        tituloLabel = new JLabel("Búsqueda Inteligente", SwingConstants.CENTER);
        tituloLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        tituloLabel.setForeground(new Color(33, 37, 41));

        //crea el campo de texto de búsqueda con estilo y comportamiento
        buscarTextField = new JTextField("Buscar alimentos", 29);
        buscarTextField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        buscarTextField.setForeground(Color.GRAY);
        buscarTextField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        //borra el texto por defecto al enfocar el campo
        buscarTextField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (buscarTextField.getText().equals("Buscar alimentos")) {
                    buscarTextField.setText("");
                    buscarTextField.setForeground(Color.BLACK);
                }
            }

            //restaura el texto por defecto si queda vacío
            public void focusLost(FocusEvent e) {
                if (buscarTextField.getText().isEmpty()) {
                    buscarTextField.setText("Buscar alimentos");
                    buscarTextField.setForeground(Color.GRAY);
                }
            }
        });

        //detecta cambios en el texto para actualizar sugerencias
        buscarTextField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarSugerencias(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarSugerencias(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarSugerencias(); }
        });

        //crea el botón de filtros avanzados
        Boton filtroButton = new Boton("Filtros avanzados");
        filtroButton.setBackground(new Color(220, 220, 220));
        filtroButton.addActionListener(e -> mostrarDialogoFiltros());

        //panel para mostrar los ingredientes seleccionados
        ingredientesPanel = new JPanel();
        ingredientesPanel.setLayout(new BoxLayout(ingredientesPanel, BoxLayout.Y_AXIS));
        ingredientesPanel.setBackground(Color.WHITE);

        //scroll que contiene el panel de ingredientes
        JScrollPane ingredientesScrollPane = new JScrollPane(ingredientesPanel);
        ingredientesScrollPane.setPreferredSize(new Dimension(600, 140));
        ingredientesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        ingredientesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        ingredientesScrollPane.setBorder(BorderFactory.createTitledBorder("Ingredientes seleccionados"));

        //panel para mostrar recetas encontradas
        recetasPanel = new JPanel();
        recetasPanel.setLayout(new BoxLayout(recetasPanel, BoxLayout.Y_AXIS));
        recetasPanel.setBackground(Color.WHITE);

        //scroll que contiene el panel de recetas
        recetasScrollPane = new JScrollPane(recetasPanel);
        recetasScrollPane.setPreferredSize(new Dimension(600, 350));
        recetasScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180)),
            "Recetas disponibles",
            0, 0,
            new Font("SansSerif", Font.BOLD, 16),
            new Color(70, 70, 70))
        );

        //botón para volver a la ventana anterior
        atrasButton = new Boton("Atrás");
        atrasButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        atrasButton.setBackground(new Color(220, 220, 220));
        atrasButton.setFocusPainted(false);
        atrasButton.addActionListener(this);

        //menú emergente para sugerencias de alimentos
        sugerenciasMenu = new JPopupMenu();

        //layout general de la ventana
        Container contenedor = getContentPane();
        contenedor.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        //añade el título al contenedor
        contenedor.add(tituloLabel, gbc);

        //añade el scroll de ingredientes al contenedor
        gbc.gridy++;
        contenedor.add(ingredientesScrollPane, gbc);

        //añade el campo de búsqueda al contenedor
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contenedor.add(buscarTextField, gbc);

        //añade el botón de filtros al contenedor
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        contenedor.add(filtroButton, gbc);

        //añade el scroll de recetas al contenedor
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        contenedor.add(recetasScrollPane, gbc);

        //añade el botón de atrás al contenedor
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        contenedor.add(atrasButton, gbc);

        //configura tamaño, posición y visibilidad de la ventana
        setSize(800, 800);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.requestFocusInWindow();
    }


    private void actualizarSugerencias() {
        //obtiene el texto escrito y lo normaliza a minúsculas
        String texto = buscarTextField.getText().toLowerCase();

        //oculta y limpia el menú de sugerencias anterior
        sugerenciasMenu.setVisible(false);
        sugerenciasMenu.removeAll();

        //verifica que el texto no esté vacío ni sea el placeholder
        if (!texto.isEmpty() && !texto.equals("buscar alimentos")) {
            try {
                //obtiene el usuario actual de la sesión
                Usuario usuario = sesion.getUsuario();
                //obtiene los alimentos prohibidos para ese usuario
                List<Alimento> prohibidos = claseAlimento.obtenerAlimentosProhibidos(usuario.getId());

                //recorre todos los alimentos posibles
                for (Alimento alimento : alimentos) {
                    String nombre = alimento.getNombre().toLowerCase();

                    //verifica si ya ha sido seleccionado
                    boolean yaSeleccionado = disponibles.stream()
                        .anyMatch(a -> a.getAlimento().getId() == alimento.getId());

                    //comprueba si el alimento es prohibido por el usuario
                    boolean esProhibido = false;
                    for (Alimento prohibido : prohibidos) {
                        if (prohibido.getNombre().equalsIgnoreCase(alimento.getNombre())) {
                            esProhibido = true;
                            break;
                        }
                    }

                    //verifica restricciones dietéticas del usuario
                    boolean esIncompatible =
                        (usuario.isVegano() && !alimento.isVegano()) ||
                        (usuario.isVegetariano() && !alimento.isVegetariano()) ||
                        (usuario.isCeliaco() && !alimento.isCeliaco());

                    //si cumple los filtros, lo añade como opción al menú
                    if (nombre.startsWith(texto) && !yaSeleccionado && !esProhibido && !esIncompatible) {
                        JMenuItem item = new JMenuItem(alimento.getNombre());
                        item.setBackground(Color.LIGHT_GRAY);
                        item.addActionListener(e -> seleccionarAlimento(alimento));
                        sugerenciasMenu.add(item);
                    }
                }

                //si hay elementos en el menú, lo muestra debajo del campo de texto
                if (sugerenciasMenu.getComponentCount() > 0) {
                    Point location = buscarTextField.getLocationOnScreen();
                    sugerenciasMenu.show(this,
                            location.x - this.getLocationOnScreen().x,
                            location.y - this.getLocationOnScreen().y + buscarTextField.getHeight());
                    buscarTextField.requestFocusInWindow();
                }
            } catch (SQLException e) {
            }
        }
    }

    private void seleccionarAlimento(Alimento alimento) {
        //reinicia el campo de búsqueda
        buscarTextField.setText("");
        buscarTextField.setForeground(Color.BLACK);

        //verifica que no se haya añadido ya el alimento
        boolean yaExiste = disponibles.stream()
            .anyMatch(d -> d.getAlimento().getId() == alimento.getId());

        //si no existe, lo añade como nuevo alimento disponible
        if (!yaExiste) {
            AlimentoDisponible nuevo = new AlimentoDisponible(alimento, 0.0, "unidad");
            disponibles.add(nuevo);
            //lo añade visualmente al panel de ingredientes
            añadirIngredienteTag(nuevo);
        }

        //oculta el menú de sugerencias y actualiza las recetas disponibles
        sugerenciasMenu.setVisible(false);
        mostrarRecetas();
    }

    private void mostrarDialogoFiltros() {
        JDialog dialogo = new JDialog(this, "Filtros de búsqueda", true);
        dialogo.setSize(400, 400);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        //tiempo
        gbc.gridx = 0; gbc.gridy = 0;
        dialogo.add(new JLabel("Tiempo (min)"), gbc); //añadir etiqueta de tiempo
        JTextField minTiempo = new JTextField(5);
        JTextField maxTiempo = new JTextField(5);
        if (filtroMinTiempo != null) minTiempo.setText(filtroMinTiempo.toString());
        if (filtroMaxTiempo != null) maxTiempo.setText(filtroMaxTiempo.toString());
        gbc.gridx = 1; dialogo.add(minTiempo, gbc); //añadir campo de texto para tiempo mínimo
        gbc.gridx = 2; dialogo.add(maxTiempo, gbc); //añadir campo de texto para tiempo máximo

        //dificultad
        gbc.gridx = 0; gbc.gridy = 1;
        dialogo.add(new JLabel("Dificultad"), gbc); //añadir etiqueta de dificultad
        gbc.gridx = 1;
        String[] dificultades = {"", "Fácil", "Media", "Difícil"};
        ComboBox<String> dificultadCombo = new ComboBox<>(dificultades);
        dificultadCombo.setSelectedItem(filtroDificultad != null ? filtroDificultad : "");
        gbc.gridwidth = 2;
        dialogo.add(dificultadCombo, gbc); //añadir combo box de dificultad
        gbc.gridwidth = 1;

        //calorías
        gbc.gridx = 0; gbc.gridy = 2;
        dialogo.add(new JLabel("Calorías (kcal)"), gbc); //añadir etiqueta de calorías
        JTextField minCalorias = new JTextField(5);
        JTextField maxCalorias = new JTextField(5);
        if (filtroMinCalorias != null) minCalorias.setText(filtroMinCalorias.toString());
        if (filtroMaxCalorias != null) maxCalorias.setText(filtroMaxCalorias.toString());
        gbc.gridx = 1; dialogo.add(minCalorias, gbc); //añadir campo de texto para calorías mínimas
        gbc.gridx = 2; dialogo.add(maxCalorias, gbc); //añadir campo de texto para calorías máximas

        //proteínas
        gbc.gridx = 0; gbc.gridy = 3;
        dialogo.add(new JLabel("Proteínas (g)"), gbc); //añadir etiqueta de proteínas
        JTextField minProteinas = new JTextField(5);
        JTextField maxProteinas = new JTextField(5);
        if (filtroMinProteinas != null) minProteinas.setText(filtroMinProteinas.toString());
        if (filtroMaxProteinas != null) maxProteinas.setText(filtroMaxProteinas.toString());
        gbc.gridx = 1; dialogo.add(minProteinas, gbc); //añadir campo de texto para proteínas mínimas
        gbc.gridx = 2; dialogo.add(maxProteinas, gbc); //añadir campo de texto para proteínas máximas

        //glúcidos
        gbc.gridx = 0; gbc.gridy = 4;
        dialogo.add(new JLabel("Glúcidos (g)"), gbc); //añadir etiqueta de glúcidos
        JTextField minGlucidos = new JTextField(5);
        JTextField maxGlucidos = new JTextField(5);
        if (filtroMinGlucidos != null) minGlucidos.setText(filtroMinGlucidos.toString());
        if (filtroMaxGlucidos != null) maxGlucidos.setText(filtroMaxGlucidos.toString());
        gbc.gridx = 1; dialogo.add(minGlucidos, gbc); //añadir campo de texto para glúcidos mínimos
        gbc.gridx = 2; dialogo.add(maxGlucidos, gbc); //añadir campo de texto para glúcidos máximos

        //lípidos
        gbc.gridx = 0; gbc.gridy = 5;
        dialogo.add(new JLabel("Lípidos (g)"), gbc); //añadir etiqueta de lípidos
        JTextField minLipidos = new JTextField(5);
        JTextField maxLipidos = new JTextField(5);
        if (filtroMinLipidos != null) minLipidos.setText(filtroMinLipidos.toString());
        if (filtroMaxLipidos != null) maxLipidos.setText(filtroMaxLipidos.toString());
        gbc.gridx = 1; dialogo.add(minLipidos, gbc); //añadir campo de texto para lípidos mínimos
        gbc.gridx = 2; dialogo.add(maxLipidos, gbc); //añadir campo de texto para lípidos máximos

        //botón aplicar filtros
        Boton aplicar = new Boton("Aplicar filtros");
        aplicar.addActionListener(e -> {
            try {
                filtroMinTiempo = parseEntero(minTiempo.getText());
                filtroMaxTiempo = parseEntero(maxTiempo.getText());
                filtroDificultad = (String) dificultadCombo.getSelectedItem();
                filtroMinCalorias = parseEntero(minCalorias.getText());
                filtroMaxCalorias = parseEntero(maxCalorias.getText());
                filtroMinProteinas = parseDouble(minProteinas.getText());
                filtroMaxProteinas = parseDouble(maxProteinas.getText());
                filtroMinGlucidos = parseDouble(minGlucidos.getText());
                filtroMaxGlucidos = parseDouble(maxGlucidos.getText());
                filtroMinLipidos = parseDouble(minLipidos.getText());
                filtroMaxLipidos = parseDouble(maxLipidos.getText());

                mostrarRecetas();
                dialogo.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialogo, "Introduce solo valores numéricos válidos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

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

            mostrarRecetas();
            dialogo.dispose();
        });

        gbc.gridy = 6;
        gbc.gridx = 0;
        dialogo.add(eliminar, gbc); //añadir botón para eliminar filtros
        gbc.gridx = 1; gbc.gridwidth = 2;
        dialogo.add(aplicar, gbc); //añadir botón para aplicar filtros

        dialogo.setVisible(true); //mostrar el diálogo
    }


    private Integer parseEntero(String texto) {
        //convierte un texto a entero si no está vacío, si no devuelve null
        return texto.isEmpty() ? null : Integer.parseInt(texto);
    }

    private Double parseDouble(String texto) {
        //convierte un texto a double (reemplazando coma por punto) si no está vacío, si no devuelve null
        return texto.isEmpty() ? null : Double.parseDouble(texto.replace(",", "."));
    }

    private void mostrarRecetas() {
        //limpia el panel de recetas anterior
        recetasPanel.removeAll();

        if (disponibles.isEmpty()) {
            //si no hay ingredientes seleccionados, muestra mensaje al usuario
            recetasPanel.add(new JLabel("Selecciona al menos un ingrediente."));
        } else {
            try {
                ClaseReceta claseReceta = new ClaseReceta(connection);
                ClaseUsuario claseUsuario = new ClaseUsuario(connection);

                //creamos una copia de los ingredientes con cantidades adaptadas
                List<AlimentoDisponible> convertidos = new ArrayList<>();
                for (AlimentoDisponible original : disponibles) {
                    double cantidad = original.getCantidad();
                    String unidad = original.getUnidad();

                    //si la unidad es "unidad", convertimos a gramos
                    if (unidad.equals("unidad")) {
                        cantidad = cantidad * 150;
                        unidad = "g";
                    }

                    //añadimos el ingrediente convertido a la lista nueva
                    AlimentoDisponible nuevo = new AlimentoDisponible(original.getAlimento(), cantidad, unidad);
                    convertidos.add(nuevo);
                }

                //buscamos recetas compatibles con los ingredientes
                List<ResultadoBusqueda> resultados = claseReceta.buscarRecetasConResultado(convertidos);
                boolean hayAlguna = false;

                for (ResultadoBusqueda resultado : resultados) {
                    Receta r = resultado.getReceta();
                    boolean completa = resultado.isCompleta();

                    if (!completa) continue;

                    //filtros activos sobre tiempo, dificultad y valores nutricionales
                    if (filtroMinTiempo != null && r.getTiempoPreparacion() < filtroMinTiempo) continue;
                    if (filtroMaxTiempo != null && r.getTiempoPreparacion() > filtroMaxTiempo) continue;
                    if (filtroDificultad != null && !filtroDificultad.isEmpty() &&
                        !r.getDificultad().equalsIgnoreCase(filtroDificultad)) continue;
                    if (filtroMinCalorias != null && r.getValorEnergetico() < filtroMinCalorias) continue;
                    if (filtroMaxCalorias != null && r.getValorEnergetico() > filtroMaxCalorias) continue;
                    if (filtroMinProteinas != null && r.getProteinas() < filtroMinProteinas) continue;
                    if (filtroMaxProteinas != null && r.getProteinas() > filtroMaxProteinas) continue;
                    if (filtroMinGlucidos != null && r.getGlucidos() < filtroMinGlucidos) continue;
                    if (filtroMaxGlucidos != null && r.getGlucidos() > filtroMaxGlucidos) continue;
                    if (filtroMinLipidos != null && r.getLipidos() < filtroMinLipidos) continue;
                    if (filtroMaxLipidos != null && r.getLipidos() > filtroMaxLipidos) continue;

                    hayAlguna = true;
                    String creador = claseUsuario.obtenerNombrePorId(r.getCreadorId());

                    //creamos un panel para mostrar la receta
                    JPanel recetaPanel = new JPanel(new BorderLayout());
                    recetaPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                    recetaPanel.setBackground(Color.WHITE);

                    double mediaValoracion = claseReceta.obtenerMediaValoracion(r.getId());
                    int votos = claseReceta.obtenerVotos(r.getId());

                    //preparamos el mensaje con nombre, autor y valoración
                    String mensaje = "<html><b>" + r.getNombre() + "</b><br/>por " + creador +
                            "<br/>⭐ " + String.format("%.1f", mediaValoracion) + " (" + votos + " votos)</html>";

                    JLabel textoLabel = new JLabel(mensaje);
                    textoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    //cargamos la imagen de la receta
                    JLabel imagenLabel = new JLabel();
                    imagenLabel.setPreferredSize(new Dimension(100, 100));
                    imagenLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                    CargaImagen.cargar(r.getId(), claseReceta, imagenLabel);

                    //añadimos la imagen y el texto al panel de receta
                    recetaPanel.add(imagenLabel, BorderLayout.WEST);
                    recetaPanel.add(textoLabel, BorderLayout.CENTER);

                    //evento para abrir la receta al hacer clic
                    recetaPanel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            new VentanaReceta(r.getId(), BusquedaInteligente.this, sesion);
                            setVisible(false);
                        }
                    });

                    //añadimos el panel de receta al panel principal
                    recetasPanel.add(recetaPanel);
                }

                if (!hayAlguna) {
                    //si no hay resultados compatibles, mostramos mensaje
                    recetasPanel.add(new JLabel("No hay recetas completas que cumplan los filtros."));
                }

            } catch (SQLException e) {
                recetasPanel.add(new JLabel("Error al cargar recetas."));
            }
        }

        //actualizamos la vista del panel
        recetasPanel.revalidate();
        recetasPanel.repaint();

    }


   //añade un nuevo tag visual para un ingrediente disponible
    private void añadirIngredienteTag(AlimentoDisponible disponible) {
        JPanel tag = new JPanel();
        tag.setLayout(new GridBagLayout());
        tag.setBackground(new Color(230, 245, 255));
        tag.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 150, 200), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.anchor = GridBagConstraints.CENTER;

        //añade el nombre del alimento al tag
        JLabel nombreLabel = new JLabel(disponible.getAlimento().getNombre());
        nombreLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        gbc.gridx = 0;
        tag.add(nombreLabel, gbc);

        //añade campo de cantidad al tag
        JTextField cantidadField = new JTextField(4);
        cantidadField.setText("0");
        cantidadField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        gbc.gridx = 1;
        tag.add(cantidadField, gbc);

        //actualiza la cantidad del ingrediente al escribir
        cantidadField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizar(); }

            private void actualizar() {
                try {
                    double cantidad = Double.parseDouble(cantidadField.getText().trim());
                    disponible.setCantidad(cantidad);
                    mostrarRecetas();
                } catch (NumberFormatException ex) {
                    disponible.setCantidad(0.0);
                }
            }
        });

        //añade combo de unidades al tag
        ComboBox<String> unidadBox = new ComboBox<>(new String[]{"", "g", "ml", "unidad"});
        unidadBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
        disponible.setUnidad(""); //empieza vacío
        gbc.gridx = 2;
        tag.add(unidadBox, gbc);

        //maneja la selección de unidad
        unidadBox.addActionListener(e -> {
            String seleccion = (String) unidadBox.getSelectedItem();
            disponible.setUnidad(seleccion);

            if ("unidad".equals(seleccion)) {
                JOptionPane.showMessageDialog(this,
                    "Has seleccionado 'unidad'. Se estiman 150g o ml por unidad para la búsqueda.",
                    "Conversión estimada", JOptionPane.INFORMATION_MESSAGE);
            }

            mostrarRecetas();
        });

        //añade botón de eliminar al tag
        Boton eliminar = new Boton("x");
        eliminar.setFont(new Font("SansSerif", Font.BOLD, 13));
        eliminar.setMargin(new Insets(2, 8, 2, 8));
        eliminar.setForeground(Color.RED);
        eliminar.setFocusPainted(false);
        eliminar.setBorder(BorderFactory.createEmptyBorder());
        eliminar.setContentAreaFilled(false);
        gbc.gridx = 3;
        tag.add(eliminar, gbc);

        //elimina el tag al hacer clic en el botón
        eliminar.addActionListener(e -> {
            disponibles.remove(disponible);
            ingredientesPanel.remove(tag); //elimina el tag del panel
            ingredientesPanel.revalidate(); //actualiza el layout
            ingredientesPanel.repaint(); //repinta el panel
            mostrarRecetas(); //vuelve a mostrar resultados filtrados
        });

        ingredientesPanel.add(tag); //añade el tag al panel principal de ingredientes
        ingredientesPanel.revalidate(); //actualiza el layout
        ingredientesPanel.repaint(); //repinta el panel
        mostrarRecetas(); //actualiza resultados al añadir ingrediente
    }



    @Override public void actionPerformed(ActionEvent e) {
        //si se pulsa el botón de atrás, se muestra la ventana anterior y se cierra la actual
        if (e.getSource() == atrasButton) {
            ventanaAnterior.setVisible(true);
            dispose();
        }
    }
}
