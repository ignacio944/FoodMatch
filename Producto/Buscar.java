import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Buscar extends JFrame implements ActionListener {
    //crear variables
    private JFrame ventanaPrincipal; //referencia a la ventana principal
    private JLabel buscador;
    private Boton botonBuscar;
    private Boton botonBusquedaInteligente;
    private Boton botonAtras;

    private Sesion sesion; //sesion del usuario

    //constructor
    public Buscar(String inTitol, JFrame ventanaPrincipal, Sesion sesion) {
    super(inTitol);
    this.ventanaPrincipal = ventanaPrincipal;
    this.sesion = sesion;

    crear();
    }


    private void crear() { //crear ventana
        //crear J's
        buscador = new JLabel("Buscador de recetas de cocina");
        buscador.setFont(new Font("Arial", Font.BOLD, 20));
        botonBuscar = new Boton("Buscar recetas de cocina");
        botonBusquedaInteligente = new Boton("Búsqueda inteligente");
        botonAtras = new Boton("Atrás");

        botonBuscar.setBackground(Color.lightGray);
        botonBusquedaInteligente.setBackground(Color.lightGray);
        botonAtras.setBackground(Color.lightGray);

        botonBuscar.addActionListener(this);
        botonBusquedaInteligente.addActionListener(this);
        botonAtras.addActionListener(this);

        //añadir mismas dimensiones a botones
        Dimension fieldSize = new Dimension(400, 50); 
        botonBuscar.setPreferredSize(fieldSize);
        botonBusquedaInteligente.setPreferredSize(fieldSize);

        Container contenedor = getContentPane(); //crear contenedor
        contenedor.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(20, 20, 20, 20);

        //añadir titulo a la ventana
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.NONE;
        contenedor.add(buscador, constraints);

        //añadir boton buscar a la ventana
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contenedor.add(botonBuscar, constraints);

        //añadir boton de busqueda inteligente a la ventana
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contenedor.add(botonBusquedaInteligente, constraints);

        //añadir boton de botonAtras a la ventana
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.CENTER;
        contenedor.add(botonAtras, constraints);

        setSize(650,650);
        setLocationRelativeTo(null); //centrar la ventana en el ordeandor
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.requestFocusInWindow(); //que no este el focus en ninguna parte
    }

    @Override
    public void actionPerformed(ActionEvent e) {  
        if (e.getSource() == botonAtras) { //volver a pantalla principal
            ventanaPrincipal.setVisible(true);
            this.dispose();
        } else if (e.getSource() == botonBusquedaInteligente) {  //ir a busqueda inteligente
            new BusquedaInteligente("Busqueda Inteligente",this, sesion);
            this.setVisible(false);
        } else if (e.getSource() == botonBuscar) { //ir a buscador de recetas
            new BuscadorRecetas("Buscador de recetas",this, sesion);
            this.setVisible(false);
        }
    }
}
