import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.SQLException;

public class Encuesta extends JFrame implements ActionListener {
    //crear variables
    private JLabel encuestaTitulo;
    private JLabel celiacoLabel, veganoLabel, vegetarianoLabel;
    private JCheckBox celiacoCheckBox, veganoCheckBox, vegetarianoCheckBox;
    private Boton introducirAlimentosButton, aceptarButton;
    private Connection connection; //conexión a la base de datos
    private Usuario usuario;
    private ClaseUsuario claseUsuario;
    private Sesion sesion;


    public Encuesta(Sesion sesion) {
        super("Encuesta");
        this.sesion = sesion;
        this.usuario = sesion.getUsuario();
        this.connection = sesion.getConnection();
        this.claseUsuario = new ClaseUsuario(connection);
        crear();    
    }


    private void crear() { //crear ventana 
        //JLables
        encuestaTitulo = new JLabel("Encuesta necesaria para mejor experiencia");
        encuestaTitulo.setFont(new Font("Arial", Font.BOLD, 20)); 
        celiacoLabel = new JLabel("Celíaco");
        veganoLabel = new JLabel("Vegano");
        vegetarianoLabel = new JLabel("Vegetariano");

        //Botons
        introducirAlimentosButton = new Boton("Introducir alimentos incomestibles para el usuario");
        aceptarButton = new Boton("Aceptar");

        //JCheckBoxes
        celiacoCheckBox = new JCheckBox();
        veganoCheckBox = new JCheckBox();
        vegetarianoCheckBox = new JCheckBox();

        Dimension checkBoxSize = new Dimension(20, 20);
        celiacoCheckBox.setPreferredSize(checkBoxSize);
        veganoCheckBox.setPreferredSize(checkBoxSize);
        vegetarianoCheckBox.setPreferredSize(checkBoxSize);

        Dimension fieldSize = new Dimension(350, 40); 
        introducirAlimentosButton.setPreferredSize(fieldSize);
        aceptarButton.setPreferredSize(fieldSize);

        introducirAlimentosButton.setBackground(Color.LIGHT_GRAY);
        aceptarButton.setBackground(Color.LIGHT_GRAY);

        //JPanels
        JPanel celiacoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        celiacoPanel.add(celiacoCheckBox);
        celiacoPanel.add(celiacoLabel);

        JPanel veganoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        veganoPanel.add(veganoCheckBox);
        veganoPanel.add(veganoLabel);

        JPanel vegetarianoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        vegetarianoPanel.add(vegetarianoCheckBox);
        vegetarianoPanel.add(vegetarianoLabel);

        //crear contenedor
        Container contenedor = getContentPane();
        contenedor.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(20, 20, 20, 20);

        //añadir el título a la ventana
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contenedor.add(encuestaTitulo, constraints);

        //añadir el panel de celíaco a la ventana
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.CENTER;
        contenedor.add(celiacoPanel, constraints);

        //añadir el panel de vegano a la ventana
        constraints.gridy = 2;
        contenedor.add(veganoPanel, constraints);

        //añadir el panel de vegetariano a la ventana
        constraints.gridy = 3;
        contenedor.add(vegetarianoPanel, constraints);

        //añadir el botón de añadir alimentos prhibidos a la ventana
        constraints.gridy = 4;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contenedor.add(introducirAlimentosButton, constraints);

        //añadir el botón de aceptar a la ventana
        constraints.gridy = 5;
        contenedor.add(aceptarButton, constraints);

        introducirAlimentosButton.addActionListener(this);
        aceptarButton.addActionListener(this);

        setSize(650,650);
        setLocationRelativeTo(null); //centrar la ventana en el ordenador
        setVisible(true);
        this.requestFocusInWindow(); //que no este el focus en ninguna parte

    }

    @Override
    public void actionPerformed(ActionEvent e) {  
        //si se pulsa el botón para introducir alimentos prohibidos
        if (e.getSource() == introducirAlimentosButton) {
            try {
                //se registra el usuario si aún no se ha registrado
                registrarUsuarioSiNecesario();
                //abre la pantalla para introducir alimentos prohibidos
                new AlimentosProhibidos("Alimentos", this, sesion);
                //oculta la ventana actual
                setVisible(false);
            } catch (SQLException ex) {
                //muestra un mensaje de error si falla el registro
                JOptionPane.showMessageDialog(this, "Error al registrar el usuario: " + ex.getMessage());
            }
        }

        //si se pulsa el botón de aceptar (sin introducir alimentos)
        if (e.getSource() == aceptarButton) {
            try {
                //se registra el usuario si aún no se ha registrado
                registrarUsuarioSiNecesario();
                //abre la pantalla principal
                new PantallaPrincipal("", sesion);
                //cierra esta ventana
                this.dispose();
            } catch (SQLException ex) {
                //muestra un mensaje de error si falla el registro
                JOptionPane.showMessageDialog(this, "Error al registrar el usuario: " + ex.getMessage());
            }
        }        
    }

    //variable para asegurar que el registro solo se haga una vez
    private boolean usuarioRegistrado = false;

    private void registrarUsuarioSiNecesario() throws SQLException {
        //si no se ha seleccionado ninguna preferencia alimentaria, se pide confirmación
        if (!celiacoCheckBox.isSelected() && !veganoCheckBox.isSelected() && !vegetarianoCheckBox.isSelected()) {
            int opcion = JOptionPane.showConfirmDialog(this,
                "No has marcado ninguna preferencia alimentaria. ¿Deseas continuar sin seleccionarlas?",
                "Confirmación",
                JOptionPane.YES_NO_OPTION);
            //si el usuario no acepta continuar sin seleccionar, se cancela el registro
            if (opcion != JOptionPane.YES_OPTION) return;
        }

        //si el usuario aún no ha sido registrado, se procede a registrarlo
        if (!usuarioRegistrado) {
            //se actualizan los valores del usuario según los checkbox seleccionados
            usuario.setCeliaco(celiacoCheckBox.isSelected());
            usuario.setVegano(veganoCheckBox.isSelected());
            usuario.setVegetariano(vegetarianoCheckBox.isSelected());

            //se registra el usuario en la base de datos y se obtiene su id
            int idGenerado = claseUsuario.registrarUsuario(usuario);
            usuario.setId(idGenerado);
            //marca que el usuario ya está registrado
            usuarioRegistrado = true;
        }
    }
}