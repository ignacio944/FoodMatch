import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//clase que permite al usuario crear y publicar una receta personalizada
public class AñadirReceta extends JFrame implements ActionListener {
    //variables de interfaz, estado y conexión
    private JFrame ventanaPrincipal;
    private JTextField tituloRecetaField;
    private JPanel ingredientesPanel;
    private JPanel pasosPanel;
    private JScrollPane ingredientesScrollPane;
    private JScrollPane pasosScrollPane;
    private Boton añadirIngredienteButton;
    private Boton añadirPasoButton;
    private Boton quitarIngredienteButton;
    private Boton quitarPasoButton;
    private Boton publicarButton;
    private Boton atrasButton;
    private ComboBox<String> dificultadComboBox;
    private JTextField tiempoProduccionField;
    private int contadorPasos = 1;
    private ArrayList<JPanel> listaIngredientes;
    private ArrayList<JPanel> listaPasos;
    private List<String> alimentos;
    private int id_usuario;
    private JPopupMenu sugerenciasMenu;
    private JTextField ingredienteField;
    private List<String> seleccionados = new ArrayList<>(); //lista de ingredientes ya seleccionados
    private Connection connection; 
    private Sesion sesion;
    private JTextField rutaImagenField; 

    //constructor
    public AñadirReceta(String titulo, JFrame ventanaPrincipal, Sesion sesion) {
        super(titulo);
        this.ventanaPrincipal = ventanaPrincipal;
        this.sesion = sesion;
        this.id_usuario = this.sesion.getUsuario().getId();
        this.connection = this.sesion.getConnection();

        this.alimentos = new ArrayList<>();

        try {
            ClaseAlimento claseAlimento = new ClaseAlimento(connection);
            this.alimentos = claseAlimento.obtenerNombresDeAlimentos();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar alimentos desde la base de datos.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        crear();
    }


    private void crear() {
        listaIngredientes = new ArrayList<>(); //inicializa lista que almacenará los ingredientes añadidos
        listaPasos = new ArrayList<>(); //inicializa lista que almacenará los pasos de la receta

        //título principal de la ventana
        JLabel ventanaTitulo = new JLabel("Añadir Receta");
        ventanaTitulo.setFont(new Font("Arial", Font.BOLD, 26)); //fuente destacada
        ventanaTitulo.setAlignmentX(Component.CENTER_ALIGNMENT); //centrado horizontal

        //panel para datos generales como título, dificultad, tiempo e imagen
        JPanel panelInfo = new JPanel(new GridBagLayout()); //usa gridbag para control preciso
        panelInfo.setBorder(BorderFactory.createTitledBorder("Información General")); //borde con título

        GridBagConstraints c = new GridBagConstraints(); //restricciones de posicionamiento
        c.insets = new Insets(5, 5, 5, 5); //márgenes internos
        c.fill = GridBagConstraints.HORIZONTAL; //los elementos se expanden horizontalmente

        //campo título
        c.gridx = 0; c.gridy = 0; c.gridwidth = 1;
        panelInfo.add(new JLabel("Título:"), c);

        c.gridx = 1; c.gridy = 0; c.gridwidth = 2;
        tituloRecetaField = new JTextField("Título de receta", 20);
        tituloRecetaField.setFont(new Font("Arial", Font.PLAIN, 14));
        tituloRecetaField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (tituloRecetaField.getText().equals("Título de receta")) {
                    tituloRecetaField.setText(""); //borra texto de ayuda al enfocar
                }
            }
            public void focusLost(FocusEvent e) {
                if (tituloRecetaField.getText().isEmpty()) {
                    tituloRecetaField.setText("Título de receta"); //restaura placeholder si se deja vacío
                }
            }
        });
        panelInfo.add(tituloRecetaField, c);

        //selector de dificultad
        c.gridx = 0; c.gridy = 1; c.gridwidth = 1;
        panelInfo.add(new JLabel("Dificultad:"), c);

        c.gridx = 1; c.gridy = 1;
        dificultadComboBox = new ComboBox<>(new String[]{"Elegir dificultad", "Fácil", "Intermedia", "Difícil"});
        panelInfo.add(dificultadComboBox, c);

        //campo de duración (tiempo estimado en minutos)
        c.gridx = 0; c.gridy = 2;
        panelInfo.add(new JLabel("Tiempo (min):"), c);

        //campo de texto para introducir el tiempo de producción
        c.gridx = 1; c.gridy = 2;
        tiempoProduccionField = new JTextField(10);
        panelInfo.add(tiempoProduccionField, c);

        //campo de selección de imagen (archivo .png obligatorio)
        c.gridx = 0; c.gridy = 3;
        panelInfo.add(new JLabel("Imagen (.png):"), c);

        //campo de texto para mostrar la ruta de la imagen seleccionada
        c.gridx = 1; c.gridy = 3;
        rutaImagenField = new JTextField(20);
        rutaImagenField.setEditable(false); //campo no editable manualmente
        panelInfo.add(rutaImagenField, c);

        //botón que abre filechooser para seleccionar imagen .png
        c.gridx = 2; c.gridy = 3;
        Boton seleccionarImagenButton = new Boton("Seleccionar");
        seleccionarImagenButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imágenes PNG", "png"));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile.getName().toLowerCase().endsWith(".png")) {
                    rutaImagenField.setText(selectedFile.getAbsolutePath()); //muestra ruta
                } else {
                    JOptionPane.showMessageDialog(this, "Solo se permiten archivos .png", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panelInfo.add(seleccionarImagenButton, c);

        //panel vertical con scroll que contendrá los ingredientes
        ingredientesPanel = new JPanel();
        ingredientesPanel.setLayout(new BoxLayout(ingredientesPanel, BoxLayout.Y_AXIS));
        ingredientesScrollPane = new JScrollPane(ingredientesPanel);
        ingredientesScrollPane.setPreferredSize(new Dimension(300, 200)); //altura y ancho fijos
        ingredientesScrollPane.setBorder(BorderFactory.createTitledBorder("Ingredientes (1 persona)"));

        //botones para añadir y quitar ingredientes
        añadirIngredienteButton = new Boton("Añadir Ingrediente");
        añadirIngredienteButton.addActionListener(e -> añadirIngredientePanel());

        quitarIngredienteButton = new Boton("Quitar Ingrediente");
        quitarIngredienteButton.addActionListener(e -> quitarIngredientePanel());

        JPanel panelIngBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelIngBotones.add(añadirIngredienteButton);
        panelIngBotones.add(quitarIngredienteButton);

        //panel vertical con scroll que contendrá los pasos de preparación
        pasosPanel = new JPanel();
        pasosPanel.setLayout(new BoxLayout(pasosPanel, BoxLayout.Y_AXIS));
        pasosScrollPane = new JScrollPane(pasosPanel);
        pasosScrollPane.setPreferredSize(new Dimension(400, 200));
        pasosScrollPane.setBorder(BorderFactory.createTitledBorder("Pasos"));

        //botones para añadir y quitar pasos
        añadirPasoButton = new Boton("Añadir Paso");
        añadirPasoButton.addActionListener(e -> añadirPasoPanel());

        quitarPasoButton = new Boton("Quitar Paso");
        quitarPasoButton.addActionListener(e -> quitarPasoPanel());

        JPanel panelPasoBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelPasoBotones.add(añadirPasoButton);
        panelPasoBotones.add(quitarPasoButton);

        //botones de navegación final: publicar o volver atrás
        publicarButton = new Boton("Publicar");
        publicarButton.addActionListener(this);

        atrasButton = new Boton("Atrás");
        atrasButton.addActionListener(this);

        JPanel panelFinal = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelFinal.add(atrasButton);
        panelFinal.add(publicarButton);

        //panel principal con layout vertical para organizar todo
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); //margen interno

        //se añaden los componentes por bloques, con espaciado vertical
        panelPrincipal.add(ventanaTitulo);
        panelPrincipal.add(Box.createVerticalStrut(10));
        panelPrincipal.add(panelInfo);
        panelPrincipal.add(Box.createVerticalStrut(10));
        panelPrincipal.add(ingredientesScrollPane);
        panelPrincipal.add(panelIngBotones);
        panelPrincipal.add(Box.createVerticalStrut(10));
        panelPrincipal.add(pasosScrollPane);
        panelPrincipal.add(panelPasoBotones);
        panelPrincipal.add(Box.createVerticalStrut(20));
        panelPrincipal.add(panelFinal);

        //se carga el panel principal en la ventana
        setContentPane(panelPrincipal);
        setSize(700, 800); //dimensiones de la ventana
        setLocationRelativeTo(null); //centra la ventana en pantalla
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true); //hace visible la ventana

        //se añaden automáticamente un ingrediente y un paso vacíos al abrir
        añadirIngredientePanel();
        añadirPasoPanel();
    }


    private void añadirIngredientePanel() {
        //panel horizontal para un solo ingrediente con separación entre componentes
        JPanel panelIngrediente = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        //campo de texto para escribir el nombre del ingrediente
        ingredienteField = new JTextField("Ingrediente", 12);
        ingredienteField.setFont(new Font("Arial", Font.PLAIN, 14));

        //sistema de autocompletado con sugerencias en un menú contextual
        sugerenciasMenu = new JPopupMenu();
        ingredienteField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarSugerencias(ingredienteField); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarSugerencias(ingredienteField); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarSugerencias(ingredienteField); }
        });

        //placeholder dinámico para campo de ingrediente
        ingredienteField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (ingredienteField.getText().equals("Ingrediente")) {
                    ingredienteField.setText(""); //borra texto de ayuda
                }
            }

            public void focusLost(FocusEvent e) {
                if (ingredienteField.getText().isEmpty()) {
                    ingredienteField.setText("Ingrediente"); //restaura texto si se deja vacío
                }
            }
        });

        //campo numérico para cantidad del ingrediente
        JTextField cantidadField = new JTextField("0", 5);
        cantidadField.setFont(new Font("Arial", Font.PLAIN, 14));
        cantidadField.setHorizontalAlignment(JTextField.RIGHT); //alineación a la derecha

        //selector de unidad de medida
        ComboBox<String> unidadCombo = new ComboBox<>(new String[]{"g", "ml", "unidad", "a ojo"});
        unidadCombo.setFont(new Font("Arial", Font.PLAIN, 13));

        //si se elige "a ojo", el campo cantidad se bloquea y se pone a 0 automáticamente
        unidadCombo.addActionListener(e -> {
            String unidadSeleccionada = (String) unidadCombo.getSelectedItem();
            if ("a ojo".equals(unidadSeleccionada)) {
                cantidadField.setText("0");
                cantidadField.setEditable(false);
            } else {
                cantidadField.setEditable(true);
            }
        });

        //se ensamblan los tres componentes en el panel de ingrediente
        panelIngrediente.add(ingredienteField);
        panelIngrediente.add(cantidadField);
        panelIngrediente.add(unidadCombo);

        listaIngredientes.add(panelIngrediente); //se añade a la lista lógica
        ingredientesPanel.add(panelIngrediente); //se añade a la vista
        ingredientesPanel.revalidate(); //actualiza layout del panel
        ingredientesPanel.repaint(); //fuerza repintado en pantalla
    }

    private void actualizarSugerencias(JTextField field) {
        //obtiene texto actual del campo en minúsculas para comparación
        String texto = field.getText().toLowerCase();
        sugerenciasMenu.setVisible(false); //oculta menú si estaba abierto
        sugerenciasMenu.removeAll(); //limpia sugerencias anteriores

        //si el campo no está vacío ni es el placeholder
        if (!texto.isEmpty() && !texto.equals("ingrediente")) {
            for (String alimento : alimentos) {
                String alimentoLower = alimento.toLowerCase();
                //evita duplicados comparando con los ya seleccionados
                boolean yaSeleccionado = seleccionados.stream().anyMatch(s -> s.equalsIgnoreCase(alimento));

                //si el texto coincide con el inicio del nombre del alimento y no está repetido
                if (alimentoLower.startsWith(texto) && !yaSeleccionado) {
                    JMenuItem item = new JMenuItem(alimento); //crea opción del menú
                    item.setBackground(Color.LIGHT_GRAY); //estilo visual
                    item.addActionListener(e -> {
                        field.setText(alimento); //pone el texto seleccionado
                        seleccionados.add(alimento); //marca como ya seleccionado
                        sugerenciasMenu.setVisible(false); //oculta menú al seleccionar
                    });
                    sugerenciasMenu.add(item); //añade al menú
                }
            }

            //si hay sugerencias válidas, se muestra el menú bajo el campo
            if (sugerenciasMenu.getComponentCount() > 0) {
                Point location = field.getLocationOnScreen();
                sugerenciasMenu.show(this,
                    location.x - this.getLocationOnScreen().x,
                    location.y - this.getLocationOnScreen().y + field.getHeight());
                field.requestFocusInWindow(); //mantiene el foco tras desplegar menú
            }
        }
    }


    public void guardarRecetaEnDB() throws Exception {
        //instancia para manejar operaciones con recetas e ingredientes
        ClaseReceta claseReceta = new ClaseReceta(connection);
        ClaseAlimento claseAlimento = new ClaseAlimento(connection);

        //obtiene datos básicos desde los campos de la interfaz
        String nombre = tituloRecetaField.getText().trim();
        String dificultad = (String) dificultadComboBox.getSelectedItem();
        int tiempo = Integer.parseInt(tiempoProduccionField.getText().trim());

        //validación: imagen obligatoria
        if (rutaImagenField.getText().trim().isEmpty()) {
            throw new Exception("Debe seleccionar una imagen .png para la receta.");
        }

        //lee el archivo de imagen y lo convierte a array de bytes
        String rutaOriginal = rutaImagenField.getText().trim();
        byte[] imagenBytes;
        try {
            imagenBytes = Files.readAllBytes(Paths.get(rutaOriginal));
        } catch (IOException e) {
            throw new Exception("Error al leer la imagen seleccionada.\n" + e.getMessage());
        }

        //concatena todos los pasos introducidos por el usuario
        StringBuilder pasos = new StringBuilder();
        int pasoNum = 1;
        for (JPanel panel : listaPasos) {
            JTextField pasoField = (JTextField) panel.getComponent(0);
            pasos.append(pasoNum).append(". ").append(pasoField.getText().trim()).append(" ");
            pasoNum++;
        }

        //valores iniciales de propiedades nutricionales y restricciones dietéticas
        boolean esVegano = true;
        boolean esVegetariano = true;
        boolean esCeliaco = true;
        double valorEnergetico = 0;
        double totalProteinas = 0;
        double totalGlucidos = 0;
        double totalLipidos = 0;

        //crea objeto receta con valores por defecto y sin nutrientes aún
        Receta receta = new Receta(
            0, nombre, id_usuario, dificultad, tiempo,
            true, true, true,
            pasos.toString().trim(), imagenBytes,
            0, 0, 0, 0, 0, 0
        );

        //guarda receta en base de datos y recupera su id generado
        int idReceta = claseReceta.guardarReceta(receta);

        //recorre todos los ingredientes añadidos por el usuario
        for (JPanel panel : listaIngredientes) {
            JTextField ingField = (JTextField) panel.getComponent(0);
            JTextField cantField = (JTextField) panel.getComponent(1);
            @SuppressWarnings("unchecked")
            ComboBox<String> unidadBox = (ComboBox<String>) panel.getComponent(2);

            String nombreAlimento = ingField.getText().trim();
            String unidad = (String) unidadBox.getSelectedItem();
            String cantidadStr = cantField.getText().trim();

            //formato final de cantidad (ej. "50 g" o "a ojo")
            String cantidadCompleta;
            if ("a ojo".equalsIgnoreCase(unidad)) {
                cantidadCompleta = "a ojo";
            } else {
                cantidadCompleta = cantidadStr + " " + unidad;
            }

            //obtiene objeto Alimento desde la base de datos
            Alimento alimento = claseAlimento.obtenerAlimentoPorNombre(nombreAlimento);
            if (alimento != null) {
                //actualiza propiedades booleanas (si todos cumplen, se mantiene en true)
                esVegano &= alimento.isVegano();
                esVegetariano &= alimento.isVegetariano();
                esCeliaco &= alimento.isCeliaco();

                //convierte cantidad a número, adaptable a unidad personalizada
                double cantidadNum = extraerValorNumerico(cantidadStr);
                if (cantidadStr.toLowerCase().contains("unidad")) {
                    cantidadNum *= 150;  //aproximación por unidad (150g)
                }

                //cálculo proporcional respecto a 100g del alimento
                valorEnergetico += (alimento.getValorEnergetico() * cantidadNum / 100.0);
                totalProteinas += (alimento.getProteinas() * cantidadNum / 100.0);
                totalGlucidos += (alimento.getGlucidos() * cantidadNum / 100.0);
                totalLipidos += (alimento.getLipidos() * cantidadNum / 100.0);

                //asocia ingrediente con la receta recién creada
                claseReceta.guardarIngrediente(idReceta, alimento.getId(), cantidadCompleta);
            }
        }

        //actualiza receta con información nutricional completa
        claseReceta.actualizarPropiedadesNutricionales(
            idReceta,
            valorEnergetico,
            esVegano,
            esVegetariano,
            esCeliaco,
            totalProteinas,
            totalGlucidos,
            totalLipidos
        );
    }


    private void añadirPasoPanel() { //añadir un paso al panel
        final int pasoActual = contadorPasos; //capturamos el número del paso actual
        JPanel panelPaso = new JPanel(new BorderLayout());
        JTextField pasoField = new JTextField("Paso " + pasoActual); //texto por defecto "Paso X"
        pasoField.setFont(new Font("Arial", Font.PLAIN, 14));

        //gestión del placeholder: borra al enfocar y restaura si se deja vacío
        pasoField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (pasoField.getText().equals("Paso " + pasoActual)) {
                    pasoField.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (pasoField.getText().isEmpty()) {
                    pasoField.setText("Paso " + pasoActual); //restaurar el texto si está vacío
                }
            }
        });

        panelPaso.add(pasoField, BorderLayout.CENTER); //añade campo al centro del panel
        listaPasos.add(panelPaso); //lo registra en la lista lógica

        pasosPanel.add(panelPaso); //añadir paso nuevo al panel de pasos
        pasosPanel.revalidate();   //refresca layout visual
        pasosPanel.repaint();      //vuelve a pintar para que se muestre

        contadorPasos++; //incrementamos el contador después de añadir el paso
    }

    //elimina el último ingrediente añadido si hay alguno
    private void quitarIngredientePanel() {
        if (!listaIngredientes.isEmpty()) {
            JPanel panelIngrediente = listaIngredientes.remove(listaIngredientes.size() - 1);
            ingredientesPanel.remove(panelIngrediente);
            ingredientesPanel.revalidate();
            ingredientesPanel.repaint();
        }
    }

    //elimina el último paso añadido y actualiza contador
    private void quitarPasoPanel() {
        if (!listaPasos.isEmpty()) {
            JPanel panelPaso = listaPasos.remove(listaPasos.size() - 1);
            pasosPanel.remove(panelPaso);
            pasosPanel.revalidate();
            pasosPanel.repaint();
            contadorPasos--;
        }
    }

    //extrae valor numérico desde un string que puede contener texto, símbolos o comas
    private double extraerValorNumerico(String texto) {
        try {
            String valorNumerico = texto.trim().replaceAll("[^\\d.,]", ""); //filtra solo números y separadores
            valorNumerico = valorNumerico.replace(",", "."); //convierte coma decimal a punto
            return Double.parseDouble(valorNumerico);
        } catch (Exception e) {
            return 0; //si falla el parseo, devuelve 0 como valor de respaldo
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == publicarButton) {
            //verifica que todos los campos estén completos antes de guardar
            if (tituloRecetaField.getText().trim().isEmpty() ||
                listaPasos.size() < 1 ||
                listaIngredientes.size() < 1 ||
                tiempoProduccionField.getText().trim().isEmpty() ||
                dificultadComboBox.getSelectedIndex() == 0) {

                JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            //valida que todos los ingredientes existan en la base de datos
            try {
                ClaseReceta claseReceta = new ClaseReceta(connection);
                for (JPanel panel : listaIngredientes) {
                    JTextField ingField = (JTextField) panel.getComponent(0);
                    String nombreAlimento = ingField.getText().trim();

                    if (!claseReceta.existeAlimento(nombreAlimento)) {
                        JOptionPane.showMessageDialog(this,
                            "El alimento \"" + nombreAlimento + "\" no existe en la base de datos.",
                            "Ingrediente inválido", JOptionPane.ERROR_MESSAGE);
                        return; //se interrumpe el guardado si hay un ingrediente inválido
                    }
                }

                //si todo está correcto, guarda en la base de datos
                guardarRecetaEnDB();
                JOptionPane.showMessageDialog(this, "Receta publicada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                ventanaPrincipal.setVisible(true); //vuelve a mostrar la ventana principal
                dispose(); //cierra la ventana actual

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al validar ingredientes: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error inesperado al guardar la receta.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } else if (e.getSource() == atrasButton) {
            //si se pulsa "Atrás", se cierra esta ventana y se muestra la principal
            ventanaPrincipal.setVisible(true);
            dispose();
        }
    }


}
