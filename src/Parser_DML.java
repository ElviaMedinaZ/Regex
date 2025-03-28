import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.*;
import java.util.regex.*;

public class Parser_DML extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel panelContenido;
    private JTextArea areaTexto;
    private JTable tablaErrores;
    private JTable tablaLexica;
    private JTree arbolSintactico;
    private DefaultTableModel modeloErrores;
    private DefaultTableModel modeloLexico;
    private JButton botonAnalizar;

    private static final Map<String, Integer> RESERVED_WORDS = new HashMap<>();
    private static final Map<String, Integer> DELIMITERS = new HashMap<>();
    private static final Map<String, Integer> OPERATORS = new HashMap<>();
    private static final Map<String, Integer> RELATIONAL_OPERATORS = new HashMap<>();

    static {
        RESERVED_WORDS.put("SELECT", 10);
        RESERVED_WORDS.put("FROM", 11);
        RESERVED_WORDS.put("WHERE", 12);
        RESERVED_WORDS.put("INSERT", 27);
        RESERVED_WORDS.put("INTO", 28);
        RESERVED_WORDS.put("VALUES", 29);
        RESERVED_WORDS.put("AND", 30);
        RESERVED_WORDS.put("OR", 31);
        
        DELIMITERS.put(",", 50);
        DELIMITERS.put(";", 51);
        DELIMITERS.put("(", 52);
        DELIMITERS.put(")", 53);
        
        OPERATORS.put("=", 70);
        OPERATORS.put(">", 71);
        OPERATORS.put("<", 72);
        OPERATORS.put("!", 73);
        RELATIONAL_OPERATORS.put(">=", 84);
        RELATIONAL_OPERATORS.put("<=", 85);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Parser_DML ventana = new Parser_DML();
                ventana.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    public Parser_DML() {
        setTitle("ANALIZADOR DML");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1000, 700);
        panelContenido = new JPanel(new BorderLayout(10, 10));
        setContentPane(panelContenido);

        // Panel superior con texto y botón
        JPanel panelSuperior = new JPanel(new BorderLayout());
        
        areaTexto = new JTextArea(5, 50);
        JScrollPane scrollTexto = new JScrollPane(areaTexto);
        scrollTexto.setPreferredSize(new Dimension(950, 100));
        
        botonAnalizar = new JButton("Analizar");
        botonAnalizar.addActionListener(e -> analizarConsultaSQL());

        // Agregar botón a un subpanel con FlowLayout
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoton.add(botonAnalizar);

        // Agregar texto y botón al panel superior
        panelSuperior.add(scrollTexto, BorderLayout.CENTER);
        panelSuperior.add(panelBoton, BorderLayout.SOUTH);

        // Panel para las tablas
        modeloErrores = new DefaultTableModel(new String[]{"Código", "Línea", "Descripción"}, 0);
        tablaErrores = new JTable(modeloErrores);
        JScrollPane scrollErrores = new JScrollPane(tablaErrores);
        scrollErrores.setPreferredSize(new Dimension(950, 150));

        modeloLexico = new DefaultTableModel(new String[]{"Token", "Línea", "Tipo"}, 0);
        tablaLexica = new JTable(modeloLexico);
        JScrollPane scrollLexico = new JScrollPane(tablaLexica);
        scrollLexico.setPreferredSize(new Dimension(950, 150));

        arbolSintactico = new JTree(new DefaultMutableTreeNode("Árbol Sintáctico"));
        JScrollPane scrollArbol = new JScrollPane(arbolSintactico);
        scrollArbol.setPreferredSize(new Dimension(950, 200));

        JPanel panelTablas = new JPanel(new GridLayout(3, 1));
        panelTablas.add(scrollLexico);
        panelTablas.add(scrollErrores);
        panelTablas.add(scrollArbol);

        // Agregar paneles al contenido
        panelContenido.add(panelSuperior, BorderLayout.NORTH);  // Panel con texto y botón
        panelContenido.add(panelTablas, BorderLayout.CENTER);   // Tablas en el centro
    }

    private void analizarConsultaSQL() {
        String input = areaTexto.getText().trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Consulta vacía", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        modeloErrores.setRowCount(0);
        modeloLexico.setRowCount(0);
        
        String[] lineas = input.split("\\r?\\n");
        int numLinea = 1;
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Consulta");
        
        for (String linea : lineas) {
            DefaultMutableTreeNode lineaNodo = new DefaultMutableTreeNode("Línea " + numLinea);
            analizarLineaLL1(linea, numLinea, lineaNodo);
            root.add(lineaNodo);
            numLinea++;
        }
        
        arbolSintactico.setModel(new DefaultTreeModel(root));
    }
    
    private void analizarLineaLL1(String linea, int numLinea, DefaultMutableTreeNode nodoPadre) {
        String[] tokens = linea.split("\\s+");
        boolean reservadaEsperada = true;
        boolean fromEsperado = false;
        boolean whereEsperado = false;
        boolean identificadorEsperado = false;
        boolean operadorRelacionalEsperado = false;
        boolean logicoEsperado = false;

        for (String token : tokens) {
            modeloLexico.addRow(new Object[]{token, numLinea, identificarTipo(token)});
            nodoPadre.add(new DefaultMutableTreeNode(token));
            
            if (token.contains("#")) {
                modeloErrores.addRow(new Object[]{101, numLinea, "Símbolo desconocido: #"});
                continue;
            }

            if (reservadaEsperada) {
                if (!RESERVED_WORDS.containsKey(token.toUpperCase())) {
                    modeloErrores.addRow(new Object[]{201, numLinea, "Se esperaba Palabra Reservada"});
                } else {
                    reservadaEsperada = false;
                    fromEsperado = token.equalsIgnoreCase("SELECT");
                }
                continue;
            }
            
            if (fromEsperado) {
                if (!token.equalsIgnoreCase("FROM")) {
                    modeloErrores.addRow(new Object[]{201, numLinea, "Se esperaba FROM"});
                } else {
                    fromEsperado = false;
                    identificadorEsperado = true;
                }
                continue;
            }
            
            if (identificadorEsperado) {
                if (!token.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                    modeloErrores.addRow(new Object[]{204, numLinea, "Se esperaba Identificador"});
                } else {
                    identificadorEsperado = false;
                    operadorRelacionalEsperado = true;
                }
                continue;
            }
            
            if (operadorRelacionalEsperado) {
                if (!RELATIONAL_OPERATORS.containsKey(token) && !OPERATORS.containsKey(token)) {
                    modeloErrores.addRow(new Object[]{208, numLinea, "Se esperaba Operador Relacional"});
                } else {
                    operadorRelacionalEsperado = false;
                    logicoEsperado = true;
                }
                continue;
            }
            
            if (logicoEsperado) {
                if (!token.equalsIgnoreCase("AND") && !token.equalsIgnoreCase("OR")) {
                    modeloErrores.addRow(new Object[]{201, numLinea, "Se esperaba AND/OR"});
                }
                logicoEsperado = false;
            }
        }
    }
    
    private String identificarTipo(String token) {
        if (RESERVED_WORDS.containsKey(token.toUpperCase())) {
            return "Palabra Reservada";
        } else if (DELIMITERS.containsKey(token)) {
            return "Delimitador";
        } else if (OPERATORS.containsKey(token) || RELATIONAL_OPERATORS.containsKey(token)) {
            return "Operador";
        } else if (token.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            return "Identificador";
        } else if (token.matches("\\d+")) {
            return "Número";
        } else {
            return "Desconocido";
        }
    }
}
