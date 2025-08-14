import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class InicioDeSesion extends JFrame implements ActionListener {
    //variables
    private JTextField usuario;          
    private JPasswordField contraseña;      
    private JLabel iniciodesesion;        
    private Boton entrar, nuevo;
    private Connection inicialConnection; //conexión a la base de datos
    private Connection connection; //conexión a la base de datos
    private ClaseUsuario claseUsuario;
    private Sesion sesion;
    private static boolean imagenesInsertadas = false;
    private static boolean baseDeDatosCreada = false;


    
    //constructor
    public InicioDeSesion(String inTitol) { 
        super(inTitol); 
        try {
            this.inicialConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "");
            try (Statement stmt = inicialConnection.createStatement()) {
                stmt.execute("SET GLOBAL max_allowed_packet = 33554432");
            } catch (SQLException e) {
            }

            if (!baseDeDatosCreada) {
                new BasesDeDatos(inicialConnection); //solo se crea bd si no se ha creado aún
                baseDeDatosCreada = true;
            }

            this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/FoodMatch", "root", "");

            if (!imagenesInsertadas) {
                InsertarImagenesRecetas.insertarImagenes(connection);
                imagenesInsertadas = true;
            }

            this.claseUsuario = new ClaseUsuario(this.inicialConnection);

        } catch (SQLException e) {
        }

        crear(); 
    }
    

    private void crear() { //crear ventana
        //añadir J's
        usuario = new JTextField("Introducir nombre de usuario", 29);
        contraseña = new JPasswordField("Introducir contraseña", 29);
        contraseña.setEchoChar((char) 0); //mostrar texto plano en el campo de contraseña
        iniciodesesion = new JLabel("Inicio de sesión");
        iniciodesesion.setFont(new Font("Arial", Font.BOLD, 24));
        entrar = new Boton("Entrar");
        nuevo = new Boton("Nuevo Usuario");

        //añadir colores
        entrar.setBackground(Color.lightGray);
        nuevo.setBackground(Color.lightGray);

        usuario.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                //limpiar texto de ayuda al ganar foco
                if (usuario.getText().equals("Introducir nombre de usuario")) {
                    usuario.setText("");
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                //mostrar texto de ayuda si el campo queda vacío
                if (usuario.getText().isEmpty()) {
                    usuario.setText("Introducir nombre de usuario");
                }
            }
        });

        contraseña.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                //limpiar texto de ayuda y mostrar asteriscos al ganar foco
                if (String.valueOf(contraseña.getPassword()).equals("Introducir contraseña")) {
                    contraseña.setText("");
                    contraseña.setEchoChar('*'); 
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                //restaurar texto de ayuda si el campo queda vacío
                if (String.valueOf(contraseña.getPassword()).isEmpty()) {
                    contraseña.setText("Introducir contraseña");
                    contraseña.setEchoChar((char) 0); 
                }
            }
        });

        //añadir mismo tamaño
        Dimension fieldSize = new Dimension(250, 40); 
        usuario.setPreferredSize(fieldSize);
        contraseña.setPreferredSize(fieldSize);
        entrar.setPreferredSize(fieldSize);
        nuevo.setPreferredSize(fieldSize);

        Container contenedor = getContentPane(); //crear contenedor
        contenedor.setLayout(new GridBagLayout()); 
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(20, 20, 20, 20); 

        //añadir titulo a la ventana
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2; 
        constraints.anchor = GridBagConstraints.CENTER;
        contenedor.add(iniciodesesion, constraints);

        //añadir introducir usuario a la ventana
        constraints.gridy = 1; 
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contenedor.add(usuario, constraints);

        //añadir campo de contraseña a la ventana
        constraints.gridy = 2; 
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contenedor.add(contraseña, constraints);

        //añadir botón de "Entrar" a la ventana
        constraints.gridy = 3; 
        constraints.gridwidth = 1; 
        contenedor.add(entrar, constraints);

        //añadir botón de "Nuevo Usuario" a la ventana
        constraints.gridx = 1; 
        contenedor.add(nuevo, constraints);

        setSize(650, 650);
        setLocationRelativeTo(null); //centrar ventana en el ordenador
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.requestFocusInWindow(); //evitar que el foco inicial esté en un campo

        entrar.addActionListener(this);
        nuevo.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == entrar) {
            String nombreUsuario = usuario.getText().trim();
            String password = new String(contraseña.getPassword()).trim();

            if (nombreUsuario.isEmpty() || nombreUsuario.equals("Introducir nombre de usuario") ||
                password.isEmpty() || password.equals("Introducir contraseña")) {
                JOptionPane.showMessageDialog(this, "Por favor, introduce un nombre de usuario y contraseña válidos.",
                                            "Campos vacíos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Usuario u = claseUsuario.verificarCredenciales(nombreUsuario, password);
                if (u != null) {
                    this.sesion = new Sesion(u, connection); // crear sesión con usuario válido
                    new PantallaPrincipal("", sesion);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Credenciales incorrectas. Intenta de nuevo.",
                                                "Error de autenticación", JOptionPane.ERROR_MESSAGE);
                    usuario.setText("");
                    contraseña.setText("");
                    contraseña.setEchoChar((char) 0);
                    contraseña.setText("Introducir contraseña");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al verificar las credenciales:\n" + ex.getMessage(),
                                            "Error de conexión", JOptionPane.ERROR_MESSAGE);
            }

        } else if (e.getSource() == nuevo) {
            this.sesion = new Sesion(null, connection); // sesión sin usuario aún
            new Registro("", sesion);
            this.dispose();
        }
    }
  

    public static void main(String[] args) { //inicializar ventana inicial
        new InicioDeSesion("");
    }
}
