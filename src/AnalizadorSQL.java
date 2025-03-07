import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

public class AnalizadorSQL extends JFrame {
	
	private int codeConstantes= 1;
	private int codeIdentificadores=1;
	
    private static final long serialVersionUID = 1L;
    private JPanel panelContenido;
    private JTextArea areaTexto;
    
    //Tabla para cada tipo
    private JTable tablaPalabrasReservadas;
    private JTable tablaConstantes;
    private JTable tablaIdentificadores;

    // Crear tablas default
    private DefaultTableModel modeloPalabrasReservadas;
    private DefaultTableModel modeloConstantes;
    private DefaultTableModel modeloIdentificadores;

    private static final Map<String, Integer> RESERVED_WORDS = new HashMap<>();
    private static final Map<String, Integer> SYMBOLS = new HashMap<>();
    private static final Map<String, Integer> OPERATORS = new HashMap<>();
    private static final Map<String, Integer> RELATIONAL_OPERATORS = new HashMap<>();

    static {
        // Llenamos las palabras reservadas
        RESERVED_WORDS.put("SELECT", 10);
        RESERVED_WORDS.put("FROM", 11);
        RESERVED_WORDS.put("WHERE", 12);
        RESERVED_WORDS.put("IN", 13);
        RESERVED_WORDS.put("AND", 14);
        RESERVED_WORDS.put("OR", 15);
        RESERVED_WORDS.put("CREATE", 16);
        RESERVED_WORDS.put("TABLE", 17);
        RESERVED_WORDS.put("CHAR", 18);
        RESERVED_WORDS.put("NUMERIC", 19);
        RESERVED_WORDS.put("NOT", 20);
        RESERVED_WORDS.put("NULL", 21);
        RESERVED_WORDS.put("CONSTRAINT", 22);
        RESERVED_WORDS.put("KEY", 23);
        RESERVED_WORDS.put("PRIMARY", 24);
        RESERVED_WORDS.put("FOREIGN", 25);
        RESERVED_WORDS.put("REFERENCES", 26);
        RESERVED_WORDS.put("INSERT", 27);
        RESERVED_WORDS.put("INTO", 28);
        RESERVED_WORDS.put("VALUES", 29);

        // Llenamos los delimitadores
        SYMBOLS.put(",", 5);
        SYMBOLS.put(".", 5);
        SYMBOLS.put("(", 5);
        SYMBOLS.put(")", 5);
        SYMBOLS.put("'", 5);

        // Llenamos los operadores
        OPERATORS.put("+", 7);
        OPERATORS.put("-", 7);
        OPERATORS.put("*", 7);
        OPERATORS.put("/", 7);

        // Llenamos los operadores relacionales
        RELATIONAL_OPERATORS.put("=", 8);
        RELATIONAL_OPERATORS.put(">", 8);
        RELATIONAL_OPERATORS.put("<", 8);
        RELATIONAL_OPERATORS.put(">=", 8);
        RELATIONAL_OPERATORS.put("<=", 8);
    }
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
    		 "[A-Za-z_][A-Za-z0-9_]*|\\d+|'[^']*'|]"
    		
    	);


    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                AnalizadorSQL ventana = new AnalizadorSQL();
                ventana.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public AnalizadorSQL() {
        setTitle("ANALIZADOR DE CONSULTAS SQL");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 800, 600);
        panelContenido = new JPanel();
        panelContenido.setBackground(new Color(235, 254, 245));
        panelContenido.setLayout(new BorderLayout(10, 10));
        setContentPane(panelContenido);

        areaTexto = new JTextArea(5, 50);
        areaTexto.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        areaTexto.setBackground(new Color(248, 248, 255));
        JScrollPane scrollTexto = new JScrollPane(areaTexto);
        scrollTexto.setPreferredSize(new Dimension(750, 100));

        JButton botonAnalizar = new JButton("Analizar");
        botonAnalizar.setForeground(Color.WHITE);
        botonAnalizar.setBackground(new Color(0, 128, 192));
        botonAnalizar.setPreferredSize(new Dimension(120, 40));
        botonAnalizar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botonAnalizar.setFont(new Font("Arial", Font.BOLD, 14));
        botonAnalizar.addActionListener(e -> analizarConsultaSQL());

        // Crear los modelos de tabla para cada tipo
        modeloPalabrasReservadas = new DefaultTableModel(new String[]{"Token", "Línea", "Valor"}, 0);
        modeloConstantes = new DefaultTableModel(new String[]{"Constante", "Línea", "Valor"}, 0);
        modeloIdentificadores = new DefaultTableModel(new String[]{"Identificador", "Línea", "Valor"}, 0);

        // Crear las tablas para cada tipo
        tablaPalabrasReservadas = new JTable(modeloPalabrasReservadas);
        tablaConstantes = new JTable(modeloConstantes);
        tablaIdentificadores = new JTable(modeloIdentificadores);

        // Hacer la tabla adaptable al número de filas
        
        tablaConstantes.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tablaPalabrasReservadas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tablaIdentificadores.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Crear JScrollPane para las tablas
        JScrollPane scrollTablaConstantes = new JScrollPane(tablaConstantes);
        JScrollPane scrollTablaIdentificadores = new JScrollPane(tablaIdentificadores);

        // Panel superior
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(new Color(235, 254, 245));
        JLabel etiquetaTexto = new JLabel("Consulta SQL:");
        etiquetaTexto.setBackground(new Color(235, 254, 245));
        etiquetaTexto.setFont(new Font("Arial", Font.BOLD, 12));
        panelSuperior.add(etiquetaTexto, BorderLayout.NORTH);
        panelSuperior.add(scrollTexto, BorderLayout.CENTER);

        // Panel central con el botón
        JPanel panelCentral = new JPanel();
        panelCentral.setBackground(new Color(235, 254, 245));
        panelCentral.add(botonAnalizar);

        // Panel inferior con todas las tablas
        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));
        panelInferior.setBackground(new Color(235, 254, 245));
      
        JLabel label = new JLabel("Constantes");
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        panelInferior.add(label);
        panelInferior.add(scrollTablaConstantes);
        panelInferior.add(new JLabel("Identificadores"));
        panelInferior.add(scrollTablaIdentificadores);
        panelInferior.add(new JLabel("Palabras Reservadas"));
        panelInferior.add(new JScrollPane(tablaPalabrasReservadas));  

        // panel Scroll
        JScrollPane scrollPanelInferior = new JScrollPane(panelInferior);
        scrollPanelInferior.setPreferredSize(new Dimension(750, 300));
        

        panelContenido.add(panelSuperior, BorderLayout.NORTH);
        panelContenido.add(panelCentral, BorderLayout.CENTER);
        panelContenido.add(scrollPanelInferior, BorderLayout.SOUTH);  // ScrollPanel  panel inf
    }

    private void analizarConsultaSQL() {
        String input = areaTexto.getText().trim();
        
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Esta consulta es invalida", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        

        // Reemplazar comillas incorrectas
        input = input.replace("‘", "'").replace("’", "'");

        // Limpiar las tablas
        modeloPalabrasReservadas.setRowCount(0);
        modeloConstantes.setRowCount(0);
        modeloIdentificadores.setRowCount(0);


     // Resetear valores antes de procesar la consulta
        codeConstantes = 1;
        codeIdentificadores = 1;
        identificadoresRegistrados.clear();
        constantesRegistradas.clear();  // <-- Ahora también reiniciamos las constantes registradas


        // Verificar la validez general de la consulta SQL
        if (!esConsultaSQLValida(input)) {
            JOptionPane.showMessageDialog(this, "Error en la sintaxis de la consulta SQL.", "Error", JOptionPane.ERROR_MESSAGE);
            return;  // Detener el análisis si la consulta es inválida
        }

        // Separar por líneas
        String[] lineas = input.split("\\r?\\n");
        int numLinea = 1;

        // Map para almacenar los elementos con las líneas
        Map<String, String> tokensConLineas = new LinkedHashMap<>();

        // Procesar cada línea de la consulta
        for (String linea : lineas) {
            Matcher matcher = TOKEN_PATTERN.matcher(linea);
            while (matcher.find()) {
                String token = matcher.group();
                
                if (token.matches("(?i)A|AN|AD|ND|A D|A N|A\\s*D|A\\s*N|A\\s*")) {
                    JOptionPane.showMessageDialog(this, 
                        "Error: '" + token + "' no es un operador válido. Debes escribir 'AND' completo.", 
                        "Error de Sintaxis", 
                        JOptionPane.ERROR_MESSAGE);
                    return;  // Detiene el análisis
                }

                
                int tipoCodigo = obtenerCodigoTipo(token);

//                // Validar la palabra reservada correctamente escrita (en mayúsculas)
//                if (RESERVED_WORDS.containsKey(token.toUpperCase())) {
//                    // Verificar si la palabra está en mayúsculas
//                    if (!token.equals(token.toUpperCase())) {
//                        JOptionPane.showMessageDialog(this, "Palabra reservada '" + token + "' escrita incorrectamente (debe estar en mayúsculas).", "Error", JOptionPane.ERROR_MESSAGE);
//                        return; // Si hay error en la palabra reservada, terminamos el análisis
//                    }
//                }

                // Si el token ya existe, agregar las líneas
                if (tokensConLineas.containsKey(token)) {
                    String lineasExistentes = tokensConLineas.get(token);
                    lineasExistentes += "," + numLinea;
                    tokensConLineas.put(token, lineasExistentes);
                } else {
                    // Si no existe, guardar la línea inicial
                    tokensConLineas.put(token, String.valueOf(numLinea));
                }
            }
            numLinea++;
        }

        // Procesar tokens y asignarles tipo
        for (Map.Entry<String, String> entry : tokensConLineas.entrySet()) {
            String token = entry.getKey();
            String lineasToken = entry.getValue();
            int tipoCodigo = obtenerCodigoTipo(token);

            // Asegurarnos de que se agregue correctamente a la tabla de palabras reservadas o identificadores
            if (RESERVED_WORDS.containsKey(token.toUpperCase())) {
                modeloPalabrasReservadas.addRow(new Object[]{token, lineasToken, tipoCodigo});
            } else if (token.matches("'[^']*'") || token.matches("\\d+")) {
                String constanteSinComillas = token.replaceAll("^'(.*)'$", "$1");
                modeloConstantes.addRow(new Object[]{constanteSinComillas, lineasToken, tipoCodigo});
            } else {
                // Verificar que el token no sea una palabra reservada parcial (ej: "AN" de "AND")
                if (token.length() > 1) {
                    modeloIdentificadores.addRow(new Object[]{token, lineasToken, tipoCodigo});
                }
            }
        }
    }


    

    // Método para validar la sintaxis de la consulta SQL
 // Método para validar la sintaxis de la consulta SQL
    private boolean esConsultaSQLValida(String consulta) {
        // Expresión regular que valida una consulta SQL básica
        // Permite que la consulta empiece con cualquier palabra clave reservada válida (como SELECT, INSERT, etc.)
        String regex = "(?i)(SELECT|INSERT|UPDATE|DELETE|CREATE|ALTER|DROP)\\s+" +  // Permitir palabras clave iniciales
                       "(\\*|[A-Za-z_][A-Za-z0-9_,\\s]+)\\s+" + // Selección de campos
                       "FROM\\s+[A-Za-z_][A-Za-z0-9_,\\s]+\\s+" + // Tabla desde la que seleccionar
                       "(WHERE\\s+[A-Za-z_][A-Za-z0-9_\\s=><']+)?"+ // Condiciones opcionales
                       "(AND|OR\\s+[A-Za-z_][A-Za-z0-9_\\s=><']+)*"; // Operadores lógicos opcionales

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(consulta);
        if (matcher.matches()) {
            return true; // Si la consulta coincide con la expresión regular, es válida
        }
        
        // Si no es válida, verificamos que no haya palabras "raras" (no reservadas)
        String[] palabras = consulta.split("\\s+");
        for (String palabra : palabras) {
            if (!RESERVED_WORDS.containsKey(palabra.toUpperCase()) && !palabra.matches("[A-Za-z_][A-Za-z0-9_]*") && !palabra.matches("\\d+") && !palabra.matches("'[^']*'")) {
                return false;
            }
        }

        return true; // Si no se detectaron errores, la consulta es válida
    }



    private final Map<String, Integer> identificadoresRegistrados = new HashMap<>();
    private final Map<String, Integer> constantesRegistradas = new HashMap<>();

    private int obtenerCodigoTipo(String token) {
        // Comprobar si el token es una palabra reservada
        if (RESERVED_WORDS.containsKey(token.toUpperCase())) {
            return RESERVED_WORDS.get(token.toUpperCase());
        }

        // Si es una constante (número o cadena) y no ha sido registrada, asignar un código único
        if (token.matches("'[^']*'") || token.matches("\\d+")) {
            if (!constantesRegistradas.containsKey(token)) {
                constantesRegistradas.put(token, 600 + codeConstantes);
                codeConstantes++;
            }
            return constantesRegistradas.get(token);
        }

        // Si es un identificador y no ha sido registrado, asignar un código único
        if (!identificadoresRegistrados.containsKey(token)) {
            identificadoresRegistrados.put(token, 400 + codeIdentificadores);
            codeIdentificadores++;
        }

        return identificadoresRegistrados.get(token);
    }

}

