import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.border.TitledBorder;

public class pruebas extends JFrame {
    private int codeConstantes = 600;
    private int codeIdentificadores = 400;
    private int numToken = 1;

    private static final long serialVersionUID = 1L;
    private JPanel panelContenido;
    private JTextArea areaTexto;
    private JTable tablaTokens, tablaIdentificadores, tablaConstantes;
    private DefaultTableModel modeloTokens, modeloIdentificadores, modeloConstantes;

    private static final Map<String, Integer> RESERVED_WORDS = new HashMap<>();
    private static final Map<String, Integer> DELIMITERS = new HashMap<>();
    private static final Map<String, Integer> OPERATORS = new HashMap<>();
    private static final Map<String, Integer> RELATIONAL_OPERATORS = new HashMap<>();
    private final Map<String, Integer> identificadoresRegistrados = new HashMap<>();
    private final Map<String, Integer> constantesRegistradas = new HashMap<>();

    static {
        RESERVED_WORDS.put("SELECT", 10);
        RESERVED_WORDS.put("FROM", 11);
        RESERVED_WORDS.put("WHERE", 12);
        RESERVED_WORDS.put("AND", 14);
        RESERVED_WORDS.put("OR", 15);
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

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*|\\d+|'[^']*'|[<>]=?|!=|=|\\*|\\+|\\-|/|\\(|\\)|,|;");

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                pruebas ventana = new pruebas();
                ventana.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @SuppressWarnings("unused")
    public pruebas() {
        setTitle("Analizador de SQL");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 800, 600);
        panelContenido = new JPanel(new BorderLayout(10, 10));
        setContentPane(panelContenido);

        JPanel panelSuperior = new JPanel(new BorderLayout());
        areaTexto = new JTextArea(5, 50);
        areaTexto.setBorder(new TitledBorder("Consulta SQL"));
        panelSuperior.add(new JScrollPane(areaTexto), BorderLayout.CENTER);

        JButton botonAnalizar = new JButton("Analizar");
        botonAnalizar.addActionListener(e -> analizarConsultaSQL());
        panelSuperior.add(botonAnalizar, BorderLayout.SOUTH);

        panelContenido.add(panelSuperior, BorderLayout.NORTH);

        modeloTokens = new DefaultTableModel(new String[]{"No.", "Línea", "TOKEN", "Tipo", "Código"}, 0);
        modeloIdentificadores = new DefaultTableModel(new String[]{"Identificador", "Código", "Línea"}, 0);
        modeloConstantes = new DefaultTableModel(new String[]{"No.", "Constante", "Tipo", "Línea"}, 0);

        tablaTokens = new JTable(modeloTokens);
        tablaIdentificadores = new JTable(modeloIdentificadores);
        tablaConstantes = new JTable(modeloConstantes);

        JPanel panelTablas = new JPanel(new GridLayout(3, 1));
        panelTablas.add(crearPanelTabla("Tokens", tablaTokens));
        panelTablas.add(crearPanelTabla("Identificadores", tablaIdentificadores));
        panelTablas.add(crearPanelTabla("Constantes", tablaConstantes));

        panelContenido.add(new JScrollPane(panelTablas), BorderLayout.CENTER);
    }

    private JPanel crearPanelTabla(String titulo, JTable tabla) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder(titulo));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    private void analizarConsultaSQL() {
        String input = areaTexto.getText().trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese una consulta SQL", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Limpiar las tablas antes de un nuevo análisis
        modeloTokens.setRowCount(0);
        modeloIdentificadores.setRowCount(0);
        modeloConstantes.setRowCount(0);

        String[] lineas = input.split("\\r?\\n");

        for (int numLinea = 0; numLinea < lineas.length; numLinea++) {
            Matcher matcher = TOKEN_PATTERN.matcher(lineas[numLinea]);

            while (matcher.find()) {
                String token = matcher.group();
                int tipo = obtenerTipo(token); // Obtener el tipo
                int codigo = obtenerCodigo(token, tipo); // Obtener el código basado en el tipo

                // Agregar a la tabla de Tokens
                modeloTokens.addRow(new Object[]{numToken++, numLinea + 1, token, tipo, codigo});

                // Si es un identificador, registrar en la tabla de Identificadores
                if (tipo == 4) {
                    modeloIdentificadores.addRow(new Object[]{token, codigo, numLinea + 1});
                }

                // Si es una constante (numérica o de texto), registrar en la tabla de Constantes
                if (tipo == 6) {
                    String valor = token.replace("'", ""); // Remover comillas en caso de cadenas
                    modeloConstantes.addRow(new Object[]{modeloConstantes.getRowCount() + 1, valor, tipo, numLinea + 1});
                }
            }
        }
    }

    private int obtenerTipo(String token) {
        // Primero, verificamos si el token está en las palabras reservadas
        if (RESERVED_WORDS.containsKey(token.toUpperCase())) return 1;
        // Luego, verificamos si está en los delimitadores
        if (DELIMITERS.containsKey(token)) return 5;
        // Verificamos si es un operador
        if (OPERATORS.containsKey(token)) return 7;
        // Verificamos si es un operador relacional
        if (RELATIONAL_OPERATORS.containsKey(token)) return 8;
        
        // Luego verificamos si es una constante (número o texto)
        if (token.startsWith("'") && token.endsWith("'")) return 6; // Constantes de texto
        if (token.matches("\\d+")) return 6; // Constantes numéricas

        // Si no es ninguna de las anteriores, lo consideramos un identificador
        return 4;
    }

    private int obtenerCodigo(String token, int tipo) {
        // Obtener el código según el tipo de token
        if (tipo == 1) return RESERVED_WORDS.get(token.toUpperCase()); // Palabras reservadas
        if (tipo == 5) return DELIMITERS.get(token); // Delimitadores
        if (tipo == 7) return OPERATORS.get(token); // Operadores
        if (tipo == 8) return RELATIONAL_OPERATORS.get(token); // Operadores relacionales
        if (tipo == 6) { // Constantes
            // Si la constante no está registrada, la registramos
            if (!constantesRegistradas.containsKey(token)) {
                constantesRegistradas.put(token, codeConstantes++);
            }
            return constantesRegistradas.get(token); // Retorna el código único de la constante
        }
        if (tipo == 4) { // Identificadores
            // Si el identificador no está registrado, lo registramos
            if (!identificadoresRegistrados.containsKey(token)) {
                identificadoresRegistrados.put(token, codeIdentificadores++);
            }
            return identificadoresRegistrados.get(token); // Retorna el código único del identificador
        }
        return 0; // Si no es ninguno de los anteriores, retorna 0
    }
}
