import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.border.MatteBorder;

public class DMLScannerGUI extends JFrame {
    private int codeConstantes = 1;
    private int codeIdentificadores = 1;

    private static final long serialVersionUID = 1L;
    private JPanel panelContenido;
    private JTextArea areaTexto;

    private JTable tablaPalabrasReservadas;
    private JTable tablaConstantes;
    private JTable tablaIdentificadores;
    private JTable tablaTokens;

    private DefaultTableModel modeloPalabrasReservadas;
    private DefaultTableModel modeloConstantes;
    private DefaultTableModel modeloIdentificadores;
    private DefaultTableModel modeloTokens;

 

    private static final Map<String, Integer> RESERVED_WORDS = new HashMap<>();
    private static final Map<String, Integer> DELIMITERS = new HashMap<>();
    private static final Map<String, Integer> OPERATORS = new HashMap<>();
    private static final Map<String, Integer> RELATIONAL_OPERATORS = new HashMap<>();

    static {
        // Palabras reservadas
        RESERVED_WORDS.put("SELECT", 10);
        RESERVED_WORDS.put("FROM", 11);
        RESERVED_WORDS.put("WHERE", 12);
        RESERVED_WORDS.put("AND", 14);
        RESERVED_WORDS.put("OR", 15);
        RESERVED_WORDS.put("INSERT", 27);
        RESERVED_WORDS.put("INTO", 28);
        RESERVED_WORDS.put("VALUES", 29);

        // Delimitadores
        DELIMITERS.put(",", 50);
        DELIMITERS.put(";", 51);
        DELIMITERS.put("(", 52);
        DELIMITERS.put(")", 53);
        DELIMITERS.put("'", 54);

        // Operadores
        OPERATORS.put("+", 70);
        OPERATORS.put("-", 71);
        OPERATORS.put("*", 72);
        OPERATORS.put("/", 73);

        // Operadores relacionales
        RELATIONAL_OPERATORS.put("=", 83);
        RELATIONAL_OPERATORS.put(">", 81);
        RELATIONAL_OPERATORS.put("<", 82);
        RELATIONAL_OPERATORS.put(">=", 84);
        RELATIONAL_OPERATORS.put("<=", 85);
    }

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[A-Za-z_#][A-Za-z0-9_#]*|\\d+|'[^']*'");


    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
            	DMLScannerGUI ventana = new DMLScannerGUI();
                ventana.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public DMLScannerGUI() {
        setTitle("ANALIZADOR DE CONSULTAS SQL");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 800, 600);
        panelContenido = new JPanel();
        panelContenido.setBackground(new Color(230, 255, 240));
        panelContenido.setLayout(new BorderLayout(10, 10));
        setContentPane(panelContenido);

        areaTexto = new JTextArea(5, 50);
        areaTexto.setBackground(new Color(248, 248, 255));
        JScrollPane scrollTexto = new JScrollPane(areaTexto);
        scrollTexto.setPreferredSize(new Dimension(750, 100));

        JButton botonAnalizar = new JButton("Analizar");
        botonAnalizar.setForeground(Color.WHITE);
        botonAnalizar.setBackground(new Color(0, 153, 153));
        botonAnalizar.setPreferredSize(new Dimension(120, 40));
        botonAnalizar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botonAnalizar.setFont(new Font("Bodoni MT", Font.BOLD, 14));
        botonAnalizar.addActionListener(e -> analizarConsultaSQL());

        modeloPalabrasReservadas = new DefaultTableModel(new String[]{"Token", "Línea", "Valor"}, 0);
        modeloConstantes = new DefaultTableModel(new String[]{"Constante", "Línea", "Valor"}, 0);
        modeloIdentificadores = new DefaultTableModel(new String[]{"Identificador", "Línea", "Valor"}, 0);
        modeloTokens = new DefaultTableModel(new String[]{"No.", "Línea", "Token", "Tipo", "Código"}, 0);

        tablaPalabrasReservadas = new JTable(modeloPalabrasReservadas);
        tablaConstantes = new JTable(modeloConstantes);
        tablaIdentificadores = new JTable(modeloIdentificadores);
        tablaTokens = new JTable(modeloTokens);

        JScrollPane scrollTablaConstantes = new JScrollPane(tablaConstantes);
        JScrollPane scrollTablaIdentificadores = new JScrollPane(tablaIdentificadores);
        JScrollPane scrollTablaTokens = new JScrollPane(tablaTokens);

        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(new Color(230, 255, 240));
        JLabel etiquetaTexto = new JLabel("Consulta SQL:");
        etiquetaTexto.setBackground(new Color(230, 255, 240));
        etiquetaTexto.setFont(new Font("Bodoni MT", Font.BOLD, 13));
        panelSuperior.add(etiquetaTexto, BorderLayout.NORTH);
        panelSuperior.add(scrollTexto, BorderLayout.CENTER);

        JPanel panelCentral = new JPanel();
        panelCentral.setBackground(new Color(230, 255, 240));
        panelCentral.add(botonAnalizar);

        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));
        panelInferior.setBackground(new Color(230, 255, 240));
        panelInferior.add(new JLabel("Constantes"));
        panelInferior.add(scrollTablaConstantes);
        panelInferior.add(new JLabel("Identificadores"));
        panelInferior.add(scrollTablaIdentificadores);
        panelInferior.add(new JLabel("Tabla Lexica"));
        panelInferior.add(scrollTablaTokens);

        JScrollPane scrollPanelInferior = new JScrollPane(panelInferior);
        scrollPanelInferior.setPreferredSize(new Dimension(750, 300));

        panelContenido.add(panelSuperior, BorderLayout.NORTH);
        panelContenido.add(panelCentral, BorderLayout.CENTER);
        panelContenido.add(scrollPanelInferior, BorderLayout.SOUTH);
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

                
                int tipoCodigo = obtenerCodigoTipo(token);

                // Si el token ya existe, agregar las líneas
                if (tokensConLineas.containsKey(token)) {
                    String lineasExistentes = tokensConLineas.get(token);
                    Set<String> lineasUnicas = new HashSet<>(Arrays.asList(lineasExistentes.split(","))); // Convierte las líneas a un conjunto para eliminar duplicados
                    lineasUnicas.add(String.valueOf(numLinea));
                    tokensConLineas.put(token, String.join(",", lineasUnicas)); // Guarda las líneas únicas
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

            // Reemplazar las comillas tipográficas (‘ y ’) por comillas simples (')
            token = token.replace("‘", "'").replace("’", "'");

            // Eliminar las comillas (tanto simples como tipográficas) del token antes de añadirlo a la tabla
            String tokenSinComillas = token.replace("'", "");

            // Si el token es una comilla simple o comillas tipográficas, no agregarlo a la tabla de tokens
            if (token.equals("'")) {
                continue; // Salta este token y pasa al siguiente
            }

            // Asegurarse de que se agregue correctamente a la tabla de palabras reservadas o identificadores
            if (RESERVED_WORDS.containsKey(token.toUpperCase())) {
                modeloPalabrasReservadas.addRow(new Object[]{token, lineasToken, RESERVED_WORDS.get(token.toUpperCase())});
            } else if (token.matches("'[^']*'") || token.matches("\\d+")) {
                // Si es una constante (número o cadena) y no ha sido registrada, agregarla a la tabla de constantes
                String constanteSinComillas = token.replaceAll("^'(.*)'$", "$1"); // Eliminar comillas de las constantes
                modeloConstantes.addRow(new Object[]{constanteSinComillas, lineasToken, tipoCodigo});
            } else {
            	modeloIdentificadores.addRow(new Object[]{token, lineasToken, tipoCodigo});
                
            }

            // Agregar a la tabla de Tokens, pero solo si no es una comilla simple ni tipográfica
            if (!token.equals("'")) {
                modeloTokens.addRow(new Object[]{modeloTokens.getRowCount() + 1, lineasToken, tokenSinComillas, tipoCodigo, tipoCodigo});
            }
        }



    }


    
    // Método para validar la sintaxis de la consulta SQL
    private boolean esConsultaSQLValida(String consulta) {
        String regex = "(?i)\\s*SELECT\\s+.+?\\s+FROM\\s+.+?(\\s+WHERE\\s+.+)?;?";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
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
        // Comprobar si es una palabra reservada
        if (RESERVED_WORDS.containsKey(token.toUpperCase())) {
            return RESERVED_WORDS.get(token.toUpperCase());
        }

        // Comprobar si es un delimitador
        if (DELIMITERS.containsKey(token)) {
            return DELIMITERS.get(token);
        }

        // Comprobar si es un operador
        if (OPERATORS.containsKey(token)) {
            return OPERATORS.get(token);
        }

        // Comprobar si es un operador relacional
        if (RELATIONAL_OPERATORS.containsKey(token)) {
            return RELATIONAL_OPERATORS.get(token);
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
