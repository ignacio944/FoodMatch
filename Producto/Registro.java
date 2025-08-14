import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

//ventana de registro de usuario
public class Registro extends JFrame implements ActionListener {
    private JTextField usuario;
    private JPasswordField contraseña;
    private JPasswordField confcontraseña;
    private JLabel registro;
    private Boton existente, crear;
    private ClaseUsuario claseUsuario;
    private Sesion sesion;

    //constructor
    public Registro(String inTitol, Sesion sesion) {
        super(inTitol);
        this.sesion = sesion;
        this.claseUsuario = new ClaseUsuario(sesion.getConnection());
        crear(); //inicializa la interfaz gráfica
    }

    private void crear() {
        //componentes del formulario
        usuario = new JTextField("Introducir nombre de usuario", 29);
        contraseña = new JPasswordField("Introducir contraseña", 29);
        contraseña.setEchoChar((char) 0); //muestra texto por defecto visible
        confcontraseña = new JPasswordField("Confirmar contraseña", 29);
        confcontraseña.setEchoChar((char) 0);
        registro = new JLabel("Registro");
        registro.setFont(new Font("Arial", Font.BOLD, 24));

        crear = new Boton("Crear");
        existente = new Boton("Usuario existente");

        crear.setBackground(Color.lightGray);
        existente.setBackground(Color.lightGray);

        //focus listeners para borrar texto por defecto al hacer clic
        usuario.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (usuario.getText().equals("Introducir nombre de usuario")) {
                    usuario.setText("");
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (usuario.getText().isEmpty()) {
                    usuario.setText("Introducir nombre de usuario");
                }
            }
        });

        contraseña.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (String.valueOf(contraseña.getPassword()).equals("Introducir contraseña")) {
                    contraseña.setText("");
                    contraseña.setEchoChar('*'); //oculta caracteres
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (String.valueOf(contraseña.getPassword()).isEmpty()) {
                    contraseña.setText("Introducir contraseña");
                    contraseña.setEchoChar((char) 0);
                }
            }
        });

        confcontraseña.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (String.valueOf(confcontraseña.getPassword()).equals("Confirmar contraseña")) {
                    confcontraseña.setText("");
                    confcontraseña.setEchoChar('*');
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (String.valueOf(confcontraseña.getPassword()).isEmpty()) {
                    confcontraseña.setText("Confirmar contraseña");
                    confcontraseña.setEchoChar((char) 0);
                }
            }
        });

        //ajuste de tamaño de campos
        Dimension fieldSize = new Dimension(250, 40);
        usuario.setPreferredSize(fieldSize);
        contraseña.setPreferredSize(fieldSize);
        confcontraseña.setPreferredSize(fieldSize);
        crear.setPreferredSize(fieldSize);
        existente.setPreferredSize(fieldSize);

        //añadiendo componentes al contenedor
        Container contenidor = getContentPane();
        contenidor.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(20, 20, 20, 20);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        contenidor.add(registro, constraints); //añade título "Registro"

        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contenidor.add(usuario, constraints); //añade campo de usuario

        constraints.gridy = 2;
        contenidor.add(contraseña, constraints); //añade campo de contraseña

        constraints.gridy = 3;
        contenidor.add(confcontraseña, constraints); //añade campo de confirmación

        constraints.gridy = 4;
        constraints.gridwidth = 1;
        contenidor.add(crear, constraints); //añade botón "Crear"

        constraints.gridx = 1;
        contenidor.add(existente, constraints); //añade botón "Usuario existente"

        //ajustes generales de la ventana
        setSize(650, 650);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.requestFocusInWindow();

        //registro de eventos
        crear.addActionListener(this);
        existente.addActionListener(this);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        // si se hace clic en el botón "Usuario existente", se abre la ventana de inicio de sesión
        if (e.getSource() == existente) {
            new InicioDeSesion("Inicio"); // abrir ventana de inicio de sesión
            this.dispose(); // cerrar la ventana de registro actual
        } 
        // si se hace clic en el botón "Crear", se intenta registrar un nuevo usuario
        else if (e.getSource() == crear) {
            String nombreUsuario = usuario.getText();
            String contraseñaStr = String.valueOf(contraseña.getPassword());
            String confirmarContraseña = String.valueOf(confcontraseña.getPassword());

            // comprobación de campos vacíos o con texto por defecto
            if (nombreUsuario.equals("Introducir nombre de usuario") || 
                contraseñaStr.equals("Introducir contraseña") || 
                confirmarContraseña.equals("Confirmar contraseña")) {
                JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.");
                return;
            }

            // comprobar si las contraseñas coinciden
            if (!contraseñaStr.equals(confirmarContraseña)) {
                JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.");
                return;
            }

            // comprobar longitud mínima de usuario y contraseña
            if (nombreUsuario.length() < 4 || contraseñaStr.length() < 4) {
                JOptionPane.showMessageDialog(this, "El nombre de usuario y la contraseña deben tener al menos 4 caracteres.");
                return;
            }

            try {
                // verificar si el nombre de usuario ya existe
                if (claseUsuario.existeUsuario(nombreUsuario)) {
                    JOptionPane.showMessageDialog(this, "El nombre de usuario ya está en uso.");
                    return;
                }

                // crear nuevo usuario con valores por defecto para restricciones dietéticas
                Usuario nuevo = new Usuario(0, nombreUsuario, contraseñaStr, false, false, false);
                sesion.setUsuario(nuevo); // guardar el usuario en la sesión

                // pasar a la encuesta de preferencias
                new Encuesta(sesion);
                this.dispose(); // cerrar ventana de registro
            } catch (SQLException ex) {
                // mostrar error si falla la conexión o la consulta
                JOptionPane.showMessageDialog(this, "Error al crear el usuario: " + ex.getMessage());
            }
        }
    }

}
