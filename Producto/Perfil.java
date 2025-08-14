import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Perfil extends JFrame implements ActionListener {
    //crear variables
    private JFrame ventanaPrincipal;
    private JLabel perfilTitulo, nomusuario, contraseña;
    private JLabel celiacoLabel, veganoLabel, vegetarianoLabel;
    private JCheckBox celiacoCheckBox, veganoCheckBox, vegetarianoCheckBox;
    private Boton introducirAlimentosButton, salir, cerrarsesion; 
    private Sesion sesion;
    private Usuario usuario;
    private ClaseUsuario claseUsuario;
    private Boton misRecetasButton;


    //constructor
    public Perfil(String titulo, JFrame ventanaPrincipal, Sesion sesion) {
        super(titulo);
        this.sesion = sesion;
        this.usuario = sesion.getUsuario();
        this.ventanaPrincipal = ventanaPrincipal;
        this.claseUsuario = new ClaseUsuario(sesion.getConnection());
        crear();
        cargarDatosUsuario();  // se basa ahora en el objeto usuario
    }

    private void crear() { //crear ventana
        //crear J's
        perfilTitulo = new JLabel("Perfil");
        perfilTitulo.setFont(new Font("Arial", Font.BOLD, 24)); 

        nomusuario = new JLabel(); //según el inicio de sesión
        nomusuario.setBorder(BorderFactory.createTitledBorder("Usuario"));

        contraseña = new JLabel(); //según el inicio de sesión
        contraseña.setBorder(BorderFactory.createTitledBorder("Contraseña"));

        celiacoLabel = new JLabel("Celíaco"); //según la encuesta
        veganoLabel = new JLabel("Vegano"); //según la encuesta
        vegetarianoLabel = new JLabel("Vegetariano"); //según la encuesta

        celiacoCheckBox = new JCheckBox(); //según la encuesta
        veganoCheckBox = new JCheckBox(); //según la encuesta
        vegetarianoCheckBox = new JCheckBox(); //según la encuesta

        introducirAlimentosButton = new Boton("Introducir alimentos incomestibles para el usuario");
        misRecetasButton = new Boton("Mis Recetas");
        salir = new Boton("Guardar y salir");
        cerrarsesion = new Boton("Cerrar sesión");

        Dimension checkBoxSize = new Dimension(20, 20);
        celiacoCheckBox.setPreferredSize(checkBoxSize);
        veganoCheckBox.setPreferredSize(checkBoxSize);
        vegetarianoCheckBox.setPreferredSize(checkBoxSize);

        Dimension fieldSize = new Dimension(400, 40); 
        introducirAlimentosButton.setPreferredSize(fieldSize);
        salir.setPreferredSize(fieldSize);
        cerrarsesion.setPreferredSize(fieldSize);

        introducirAlimentosButton.setBackground(Color.LIGHT_GRAY);
        salir.setBackground(Color.LIGHT_GRAY);
        cerrarsesion.setBackground(Color.LIGHT_GRAY);

        //crear paneles
        JPanel celiacoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        celiacoPanel.add(celiacoCheckBox);
        celiacoPanel.add(celiacoLabel);

        JPanel veganoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        veganoPanel.add(veganoCheckBox);
        veganoPanel.add(veganoLabel);

        JPanel vegetarianoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        vegetarianoPanel.add(vegetarianoCheckBox);
        vegetarianoPanel.add(vegetarianoLabel);

        Container contenidor = getContentPane(); //crear contenedor
        contenidor.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);

        //añadir el titulo a la ventana
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contenidor.add(perfilTitulo, constraints);

        //añadir el nombre de usuario a la ventana
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.CENTER;
        contenidor.add(nomusuario, constraints);

        //añadir la contraseña a la ventana
        constraints.gridy = 2;
        contenidor.add(contraseña, constraints);

        //añadir el panel de celíaco a la ventana
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        contenidor.add(celiacoPanel, constraints);

        //añadir el panel de vegano a la ventana
        constraints.gridy = 4;
        contenidor.add(veganoPanel, constraints);

        //añadir el panel de vegetariano a la ventana
        constraints.gridy = 5;
        contenidor.add(vegetarianoPanel, constraints);

        //añadir el botón de introducir alimentos a la ventana
        constraints.gridy = 6;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contenidor.add(introducirAlimentosButton, constraints);

        constraints.gridy = 7;
        contenidor.add(misRecetasButton, constraints);


        //añadir el botón de salir a la ventana
        constraints.gridy = 8;
        contenidor.add(salir, constraints);

        //añadir el boton de cerrar sesión a la ventana
        constraints.gridy = 9;
        contenidor.add(cerrarsesion, constraints);

        introducirAlimentosButton.addActionListener(this);
        salir.addActionListener(this);
        cerrarsesion.addActionListener(this);
        misRecetasButton.addActionListener(this);

        this.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
            int confirm = JOptionPane.showConfirmDialog(null,
                "¿Quieres guardar tus cambios antes de salir?",
                "Confirmar salida", JOptionPane.YES_NO_CANCEL_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                guardarCambios();
                System.exit(0); //cerrar todo
            } else if (confirm == JOptionPane.NO_OPTION) {
                System.exit(0); //cerrar sin guardar
            }
            //si es CANCEL_OPTION o se cierra el diálogo, no se hace nada
        }
    });

        setSize(650,650);
        setLocationRelativeTo(null); //centrar ventana en la pantalla del ordenador
        setVisible(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.requestFocusInWindow(); //que no este el focus en ninguna parte
    }

    //método para cargar los datos del usuario desde la BD
    private void cargarDatosUsuario() {
        try {
            Usuario actualizado = claseUsuario.obtenerUsuarioPorId(usuario.getId());
            if (actualizado != null) {
                usuario = actualizado; //actualizar referencia local
                nomusuario.setText(usuario.getNombre());
                contraseña.setText("******");
                celiacoCheckBox.setSelected(usuario.isCeliaco());
                veganoCheckBox.setSelected(usuario.isVegano());
                vegetarianoCheckBox.setSelected(usuario.isVegetariano());
            } else {
                JOptionPane.showMessageDialog(this, "Usuario no encontrado", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    

    //método para guardar los cambios al salir
    private void guardarCambios() {
        usuario.setCeliaco(celiacoCheckBox.isSelected());
        usuario.setVegano(veganoCheckBox.isSelected());
        usuario.setVegetariano(vegetarianoCheckBox.isSelected());

        try {
            claseUsuario.actualizarPreferencias(usuario);

            //actualizar el usuario en la sesión global
            sesion.setUsuario(usuario);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al guardar cambios: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    

    @Override
    public void actionPerformed(ActionEvent e) {  
        if (e.getSource() == introducirAlimentosButton) { 
            new AlimentosProhibidos("", this, sesion);
            setVisible(false);
        } else if (e.getSource() == salir) { 
            guardarCambios();
            ventanaPrincipal.setVisible(true);
            this.dispose();
        } else if (e.getSource() == cerrarsesion) { 
            new InicioDeSesion("");
            this.dispose();
        } else if (e.getSource() == misRecetasButton) {
            new MisRecetas("", this, sesion);
            setVisible(false);
        }
    }
}
