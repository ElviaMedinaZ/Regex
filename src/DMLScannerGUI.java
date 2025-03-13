import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.regex.*;

public class DMLScannerGUI extends JFrame {
    private int codeConstantes = 1;
    private int codeIdentificadores = 1;

    private static final long serialVersionUID = 1L;
    private JPanel panelContenido;
    private JTextArea areaTexto;

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

        DELIMITERS.put(",", 50);
        DELIMITERS.put(";", 51);
        DELIMITERS.put("(", 52);
        DELIMITERS.put(")", 53);
        DELIMITERS.put("'", 54);

        OPERATORS.put("+", 70);
        OPERATORS.put("-", 71);
        OPERATORS.put("*", 72);
        OPERATORS.put("/", 73);

        RELATIONAL_OPERATORS.put("=", 83);
        RELATIONAL_OPERATORS.put(">", 81);
        RELATIONAL_OPERATORS.put("<", 82);
        RELATIONAL_OPERATORS.put(">=", 84);
        RELATIONAL_OPERATORS.put("<=", 85);
    }

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[A-Za-z_#][A-Za-z0-9_#]*|\\d+|'[^']*'|[(),;]");

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

    @SuppressWarnings("unused")
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
        modeloConstantes = new DefaultTableModel(new String[]{"Constante", "Línea","Tipo", "Valor"}, 0);
        modeloIdentificadores = new DefaultTableModel(new String[]{"Identificador", "Línea", "Valor"}, 0);
        modeloTokens = new DefaultTableModel(new String[]{"No.", "Línea", "Token", "Tipo", "Código"}, 0);

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
        panelInferior.add(new JLabel("Tabla Léxica"));
        panelInferior.add(scrollTablaTokens);

        JScrollPane scrollPanelInferior = new JScrollPane(panelInferior);
        scrollPanelInferior.setPreferredSize(new Dimension(750, 300));

        panelContenido.add(panelSuperior, BorderLayout.NORTH);
        panelContenido.add(panelCentral, BorderLayout.CENTER);
        panelContenido.add(scrollPanelInferior, BorderLayout.SOUTH);
    }

    @SuppressWarnings("unused")
    private void analizarConsultaSQL() {
        String input = areaTexto.getText().trim();
    
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Esta consulta es inválida", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        input = input.replace("‘", "'").replace("’", "'");
    
        modeloPalabrasReservadas.setRowCount(0);
        modeloConstantes.setRowCount(0);
        modeloIdentificadores.setRowCount(0);
        modeloTokens.setRowCount(0);
    
        codeConstantes = 1;
        codeIdentificadores = 1;
        identificadoresRegistrados.clear();
        constantesRegistradas.clear();
    
        if (!esConsultaSQLValida(input)) {
            JOptionPane.showMessageDialog(this, "Error en la sintaxis de la consulta SQL.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        String[] lineas = input.split("\\r?\\n");
        int numLinea = 1;
    
        Map<String, Set<Integer>> identificadoresConLineas = new LinkedHashMap<>(); // Mantiene el orden de aparición
    
        for (String linea : lineas) {
            Matcher matcher = TOKEN_PATTERN.matcher(linea);
            while (matcher.find()) {
                String token = matcher.group();
                int tipo = obtenerTipo(token);
                int codigo = obtenerCodigo(token);
    
                if (token.equals("'")) {
                    continue;
                }
    
                if (tipo == 1) {
                    modeloPalabrasReservadas.addRow(new Object[]{token, numLinea, codigo});
                } else if (tipo == 6) {
                    String constanteSinComillas = token.replaceAll("^'(.*)'$", "$1");
                    int tipoConstante = token.matches("\\d+") ? 61 : 62; // 61 para números, 62 para texto
                    modeloConstantes.addRow(new Object[]{constanteSinComillas, numLinea, tipoConstante, codigo});
                } else if (tipo == 4) { // Solo identificadores (evita delimitadores y operadores)
                    identificadoresConLineas.computeIfAbsent(token, k -> new LinkedHashSet<>()).add(numLinea);
                }
    
                modeloTokens.addRow(new Object[]{modeloTokens.getRowCount() + 1, numLinea, token, tipo, codigo});
            }
            numLinea++;
        }
    
        // Agregar identificadores con líneas separadas por comas
        for (Map.Entry<String, Set<Integer>> entry : identificadoresConLineas.entrySet()) {
            String token = entry.getKey();
            String lineasTexto = entry.getValue().toString().replaceAll("[\\[\\]]", ""); // Formatear líneas
            int codigo = obtenerCodigo(token);
            modeloIdentificadores.addRow(new Object[]{token, lineasTexto, codigo});
        }
    }    

    private boolean esConsultaSQLValida(String consulta) {
        String regex = "(?i)\\s*SELECT\\s+.+?\\s+FROM\\s+.+?(\\s+WHERE\\s+.+)?;?";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(consulta);

        if (matcher.matches()) {
            return true;
        }

        String[] palabras = consulta.split("\\s+");
        for (String palabra : palabras) {
            if (!RESERVED_WORDS.containsKey(palabra.toUpperCase()) &&
                !palabra.matches("[A-Za-z_][A-Za-z0-9_]*") &&
                !palabra.matches("\\d+") &&
                !palabra.matches("'[^']*'")) {
                return false;
            }
        }
        return true;
    }

    private final Map<String, Integer> identificadoresRegistrados = new HashMap<>();
    private final Map<String, Integer> constantesRegistradas = new HashMap<>();

    private int obtenerTipo(String token) {
        if (RESERVED_WORDS.containsKey(token.toUpperCase())) {
            return 1;
        }
        if (DELIMITERS.containsKey(token)) {
            return 5;
        }
        if (OPERATORS.containsKey(token)) {
            return 7;
        }
        if (RELATIONAL_OPERATORS.containsKey(token)) {
            return 8;
        }
        if (token.matches("'[^']*'") || token.matches("\\d+")) {
            return 6;
        }
        return 4;
    }

    private int obtenerCodigo(String token) {
        if (RESERVED_WORDS.containsKey(token.toUpperCase())) {
            return RESERVED_WORDS.get(token.toUpperCase());
        }
        if (token.matches("'[^']*'") || token.matches("\\d+")) {
            if (!constantesRegistradas.containsKey(token)) {
                constantesRegistradas.put(token, 600 + codeConstantes);
                codeConstantes++;
            }
            return constantesRegistradas.get(token);
        }
        if (!identificadoresRegistrados.containsKey(token)) {
            identificadoresRegistrados.put(token, 400 + codeIdentificadores);
            codeIdentificadores++;
        }
        return identificadoresRegistrados.get(token);
    }
}
