package com.iudigital.conexiontester;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Aplicación de escritorio que permite ingresar la configuración de una base de
 * datos (URL JDBC, usuario y contraseña) y validar si la conexión se puede
 * establecer, mostrando el resultado o el error técnico en pantalla.
 *
 * La clase hereda de JFrame (es la ventana principal) e implementa
 * ActionListener para atender el clic del botón "Probar conexión".
 *
 * Asignatura: Programación Orientada a Objetos II - IU Digital
 * Actividad Evaluativa 3 - Unidad 3
 *
 * @author Santiago López Restrepo
 */
public class ConexionTester extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    /** Colores usados para el mensaje de resultado. */
    private static final Color COLOR_EXITO = new Color(0, 128, 0);
    private static final Color COLOR_ERROR = new Color(200, 0, 0);

    /** Tiempo máximo (en segundos) que se espera al intentar conectar. */
    private static final int TIEMPO_ESPERA_SEGUNDOS = 5;

    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Componentes de la interfaz (encapsulados como atributos privados)
    private JTextField txtUrl;
    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JButton btnProbar;
    private JTextArea txtResultado;

    /**
     * Construye la ventana principal y arma todos sus componentes.
     */
    public ConexionTester() {
        super("Probador de conexión JDBC");
        construirInterfaz();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(620, 420);
        setLocationRelativeTo(null);
    }

    /**
     * Crea los paneles y componentes. Se usa BorderLayout para el panel
     * principal y GridLayout para el formulario de datos.
     */
    private void construirInterfaz() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // --- Formulario (parte superior) ---
        JPanel panelFormulario = new JPanel(new GridLayout(3, 2, 8, 8));
        panelFormulario.setBorder(
                BorderFactory.createTitledBorder("Configuración de la base de datos"));

        txtUrl = new JTextField("jdbc:sqlite:inventario.db");
        txtUsuario = new JTextField();
        txtContrasena = new JPasswordField();

        panelFormulario.add(new JLabel("URL de conexión:"));
        panelFormulario.add(txtUrl);
        panelFormulario.add(new JLabel("Usuario (vacío si es SQLite):"));
        panelFormulario.add(txtUsuario);
        panelFormulario.add(new JLabel("Contraseña (vacía si es SQLite):"));
        panelFormulario.add(txtContrasena);

        // --- Área de resultados (centro) ---
        txtResultado = new JTextArea();
        txtResultado.setEditable(false);
        txtResultado.setLineWrap(true);
        txtResultado.setWrapStyleWord(true);
        txtResultado.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        txtResultado.setText("Ingrese los datos y presione \"Probar conexión\".");

        JScrollPane scrollResultado = new JScrollPane(txtResultado);
        scrollResultado.setBorder(BorderFactory.createTitledBorder("Resultado de la prueba"));

        // --- Botón (parte inferior) ---
        btnProbar = new JButton("Probar conexión");
        btnProbar.addActionListener(this);
        JPanel panelBoton = new JPanel();
        panelBoton.add(btnProbar);

        panelPrincipal.add(panelFormulario, BorderLayout.NORTH);
        panelPrincipal.add(scrollResultado, BorderLayout.CENTER);
        panelPrincipal.add(panelBoton, BorderLayout.SOUTH);

        setContentPane(panelPrincipal);
        getRootPane().setDefaultButton(btnProbar);
    }

    /**
     * Evento del botón "Probar conexión".
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnProbar) {
            probarConexion();
        }
    }

    /**
     * Intenta abrir la conexión con DriverManager y muestra el resultado.
     * La conexión se abre dentro de un try-with-resources, por lo que se
     * cierra automáticamente tanto si la prueba sale bien como si falla.
     */
    private void probarConexion() {
        String url = txtUrl.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());

        if (url.isEmpty()) {
            mostrarError("Datos incompletos",
                    "Debe ingresar la URL de conexión (ej.: jdbc:sqlite:inventario.db).");
            return;
        }

        try {
            // 1. Cargar el driver según la URL (puede lanzar ClassNotFoundException)
            String claseDriver = obtenerClaseDriver(url);
            if (claseDriver != null) {
                Class.forName(claseDriver);
            }

            DriverManager.setLoginTimeout(TIEMPO_ESPERA_SEGUNDOS);

            // 2. Abrir la conexión con try-with-resources (cierre automático)
            try (Connection conexion = abrirConexion(url, usuario, contrasena)) {
                DatabaseMetaData info = conexion.getMetaData();
                boolean valida = conexion.isValid(TIEMPO_ESPERA_SEGUNDOS);
                mostrarExito("Conexión exitosa"
                        + "\n\nMotor:           " + info.getDatabaseProductName()
                        + " " + info.getDatabaseProductVersion()
                        + "\nDriver:          " + info.getDriverName() + " " + info.getDriverVersion()
                        + "\nURL:             " + info.getURL()
                        + "\nConexión válida: " + (valida ? "sí" : "no")
                        + "\n\nLa conexión se cerró correctamente al terminar la prueba.");
            }

        } catch (ClassNotFoundException ex) {
            mostrarError("Fallo del driver",
                    "No se encontró la clase del driver JDBC: " + ex.getMessage()
                    + "\nVerifique que el archivo .jar del driver esté agregado al classpath"
                    + " del proyecto.");
        } catch (SQLException ex) {
            mostrarError("Fallo de conexión",
                    "Mensaje técnico: " + ex.getMessage()
                    + "\nSQLState: " + ex.getSQLState()
                    + " | Código de error: " + ex.getErrorCode());
        }
    }

    /**
     * Abre la conexión. Si no se ingresó usuario (caso SQLite) se usa la
     * versión de getConnection que solo recibe la URL.
     */
    private Connection abrirConexion(String url, String usuario, String contrasena)
            throws SQLException {
        if (usuario.isEmpty() && contrasena.isEmpty()) {
            return DriverManager.getConnection(url);
        }
        return DriverManager.getConnection(url, usuario, contrasena);
    }

    /**
     * Devuelve el nombre de la clase del driver según el prefijo de la URL.
     * Si el motor no está en la lista, retorna null y se deja que
     * DriverManager busque un driver registrado.
     */
    private String obtenerClaseDriver(String url) {
        String u = url.toLowerCase();
        if (u.startsWith("jdbc:sqlite:")) {
            return "org.sqlite.JDBC";
        } else if (u.startsWith("jdbc:mysql:")) {
            return "com.mysql.cj.jdbc.Driver";
        } else if (u.startsWith("jdbc:mariadb:")) {
            return "org.mariadb.jdbc.Driver";
        } else if (u.startsWith("jdbc:postgresql:")) {
            return "org.postgresql.Driver";
        } else if (u.startsWith("jdbc:sqlserver:")) {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        } else if (u.startsWith("jdbc:oracle:")) {
            return "oracle.jdbc.OracleDriver";
        }
        return null;
    }

    /** Muestra un mensaje de éxito en color verde. */
    private void mostrarExito(String mensaje) {
        txtResultado.setForeground(COLOR_EXITO);
        txtResultado.setText("[" + horaActual() + "] " + mensaje);
        txtResultado.setCaretPosition(0);
    }

    /** Muestra un mensaje de error en color rojo con su detalle técnico. */
    private void mostrarError(String titulo, String detalle) {
        txtResultado.setForeground(COLOR_ERROR);
        txtResultado.setText("[" + horaActual() + "] " + titulo + "\n\n" + detalle);
        txtResultado.setCaretPosition(0);
    }

    private String horaActual() {
        return LocalDateTime.now().format(FORMATO_HORA);
    }

    /**
     * Punto de entrada. La ventana se crea en el hilo de eventos de Swing.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignorada) {
                // Si no se puede aplicar el estilo del sistema, se usa el de Java por defecto.
            }
            new ConexionTester().setVisible(true);
        });
    }
}
