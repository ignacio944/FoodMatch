import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.imageio.ImageIO;

public class PantallaPrincipal extends JFrame implements ActionListener {
    //crear variables
    private JButton perfil, historial, guardado, añadir, buscar;
    private JLabel bienvenidaLabel;
    private Connection connection; //conexión a la base de datos
    private Sesion sesion;
    private Usuario usuario;
    //constructor
    public PantallaPrincipal(String inTitol, Sesion sesion) {
        super(inTitol);
        this.sesion = sesion;
        this.usuario = sesion.getUsuario();
        this.connection = sesion.getConnection();
        crear();
    }

    private void crear() { //crear ventana
        //crear J's
        ClaseUsuario claseUsuario = new ClaseUsuario(connection);
        String nombre = "";
        try {
            nombre = claseUsuario.obtenerNombrePorId(usuario.getId());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar el nombre de usuario.", "Error", JOptionPane.ERROR_MESSAGE);
            nombre = "(usuario)";
        }

        bienvenidaLabel = new JLabel("Bienvenido " + nombre);
        bienvenidaLabel.setFont(new Font("Arial", Font.BOLD, 24));

        //añadir imagenes a los botones
        perfil = crearBotonConIcono("imagen_pantallaprincipal/perfil.png");
        historial = crearBotonConIcono("imagen_pantallaprincipal/historial.png");
        guardado = crearBotonConIcono("imagen_pantallaprincipal/guardado.png");
        añadir = crearBotonConIcono("imagen_pantallaprincipal/añadir.png");
        buscar = crearBotonConIcono("imagen_pantallaprincipal/buscar.png");
            
        añadir.addActionListener(this);
        buscar.addActionListener(this);
        historial.addActionListener(this);
        guardado.addActionListener(this);
        perfil.addActionListener(this);

        JLabel perfilLabel = new JLabel("Perfil");
        JLabel historialLabel = new JLabel("Historial de recetas");
        JLabel guardadoLabel = new JLabel("Recetas guardadas");
        JLabel añadirLabel = new JLabel("Añadir recetas");
        JLabel buscarLabel = new JLabel("Buscador de recetas");

        Container contenedor = getContentPane(); //crear contenedor
        contenedor.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        //añadir titulo de bienvenida a la ventana
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3; 
        contenedor.add(bienvenidaLabel, gbc);

        //añadir boton de perfil a la ventana
        gbc.gridwidth = 1; 
        gbc.gridy = 1;
        gbc.gridx = 0;
        contenedor.add(perfil, gbc);
        gbc.gridy = 2;
        contenedor.add(perfilLabel, gbc);

        //añadir boton de buscar a la ventana
        gbc.gridx = 1;
        gbc.gridy = 1;
        contenedor.add(buscar, gbc);
        gbc.gridy = 2;
        contenedor.add(buscarLabel, gbc);

        //añadir boton de añadir a la ventana
        gbc.gridx = 2;
        gbc.gridy = 1;
        contenedor.add(añadir, gbc);
        gbc.gridy = 2;
        contenedor.add(añadirLabel, gbc);

        //añadir boton de historial a la ventana
        gbc.gridx = 0;
        gbc.gridy = 3;
        contenedor.add(historial, gbc);
        gbc.gridy = 4;
        contenedor.add(historialLabel, gbc);

        //añadir boton de guardado a la ventana
        gbc.gridx = 2;
        gbc.gridy = 3;
        contenedor.add(guardado, gbc);
        gbc.gridy = 4;
        contenedor.add(guardadoLabel, gbc);

        setSize(700,700);
        setLocationRelativeTo(null); //centrar ventana en la pantalla del ordenador
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.requestFocusInWindow(); //que no este el focus en ninguna parte
    }

    private JButton crearBotonConIcono(String rutaIcono) { //crear boton con la imagen seleccionada
        //que la imagen se vea entera en el boton
        ImageIcon icono = redimensionarIcono(rutaIcono, 150, 150); //tamaño a la imagen 
        JButton boton = new JButton(icono != null ? icono : new ImageIcon()); // icono vacío por defecto
        boton.setPreferredSize(new Dimension(150, 150)); //tamaño al boton
        boton.setBackground(Color.white); 
        return boton;
    }

    private ImageIcon redimensionarIcono(String rutaIcono, int ancho, int alto) { //redimensionar la imagen al tamaño del boton
        try {
            BufferedImage imagenOriginal = ImageIO.read(new File(rutaIcono));
            Image imagenRedimensionada = imagenOriginal.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH); //imagen redimensionada
            return new ImageIcon(imagenRedimensionada);
        } 
        catch (IOException e) {
            return null;
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {   
        if (e.getSource() == perfil) { //entrar al perfil
            new Perfil("",this, sesion);
            this.setVisible(false);
        }else if (e.getSource() == añadir) { //entrar a añadir receta
            new AñadirReceta("",this, sesion);
            this.setVisible(false);
        }else if (e.getSource() == buscar) { //entrar al buscador
            new Buscar("",this, sesion);
            this.setVisible(false);
        }else if (e.getSource() == guardado) { //entrar a recetas guardadas
            new RecetasGuardadas("",this, sesion); 
            this.setVisible(false);
        }else if (e.getSource() == historial) { //entrar al historial
            new HistorialRecetas("",this, sesion);
            this.setVisible(false);}
}   

}