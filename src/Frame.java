import java.awt.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class Frame extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel panelContenido;
    private JTextArea areaTexto;
    private JTable tablaResultados;
    private DefaultTableModel modeloTabla;

    private static final String PATRON_REGEX = "\\$?\\d{1,6}(?:,\\d{3})*(?:\\.\\d+)?%?|\\b\\d+\\b";
    
    /*
     * ----Explicacion expresion regular----
     * 
     * 1. "\\$?"  Detecta simbolo de pesos
     * 2. "\\d{1,6}"  -> Captura los primeros 1 a 6 dígitos de un num
     * 3. "(?:,\\d{3})*"  -> Captura miles con comas (ejemplo: "1,000")
     * 4. "(?:\\.\\d+)?"  -> Captura decimales opcionales
     * 5. "%?"  -> Captura un porcentaje opcional
     * 6. "|"  -> Alternativa para capturar números enteros aislados
     * 7. "\\b\\d+\\b"  -> Captura num enteros que no están dentro de palabras
     */
    
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
            	Frame ventana = new Frame();
                ventana.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @SuppressWarnings("unused")
    public Frame() {
        setTitle("Extractor de Números");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 800, 500);
        panelContenido = new JPanel();
        panelContenido.setBackground(new Color(207, 235, 239));
        panelContenido.setBorder(new EmptyBorder(5, 5, 5, 5));
        panelContenido.setLayout(new BorderLayout(10, 10));
        setContentPane(panelContenido);

        areaTexto = new JTextArea(5, 50);
        JScrollPane scrollTexto = new JScrollPane(areaTexto);
        scrollTexto.setPreferredSize(new Dimension(750, 100));

        JButton botonAnalizar = new JButton("Analizar");
        botonAnalizar.setForeground(Color.WHITE);
        botonAnalizar.setBackground(new Color(32, 101, 128));
        botonAnalizar.setPreferredSize(new Dimension(120, 40));
        botonAnalizar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botonAnalizar.setFont(new Font("Tahoma", Font.BOLD, 14));
        botonAnalizar.addActionListener(e -> analizarTexto());

        String[] columnas = {"No.", "No. Línea", "Número", "Tipo"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaResultados = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaResultados);
        scrollTabla.setPreferredSize(new Dimension(750, 250));

        // Panel superior
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(new Color(207, 235, 239));
        JLabel etiquetaTexto = new JLabel("Ingrese el texto:");
        etiquetaTexto.setFont(new Font("Tahoma", Font.BOLD, 13));
        panelSuperior.add(etiquetaTexto, BorderLayout.NORTH);
        panelSuperior.add(scrollTexto, BorderLayout.CENTER);

        // Panel central
        JPanel panelCentral = new JPanel();
        panelCentral.setBackground(new Color(207, 235, 239));
        panelCentral.add(botonAnalizar);

        // Panel inferior
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(new Color(207, 235, 239));
        JLabel etiquetaResultados = new JLabel("Resultados");
        etiquetaResultados.setFont(new Font("Tahoma", Font.BOLD, 13));
        panelInferior.add(etiquetaResultados, BorderLayout.NORTH);
        panelInferior.add(scrollTabla, BorderLayout.CENTER);

        
        panelContenido.add(panelSuperior, BorderLayout.NORTH);
        panelContenido.add(panelCentral, BorderLayout.CENTER);
        panelContenido.add(panelInferior, BorderLayout.SOUTH);
    }

    private void analizarTexto() {
        String texto = areaTexto.getText();
        String[] lineas = texto.split("\\n");
        modeloTabla.setRowCount(0); // Limpiar la tabla

        Pattern patron = Pattern.compile(PATRON_REGEX);
        int contador = 1;

        for (int i = 0; i < lineas.length; i++) {
            Matcher matcher = patron.matcher(lineas[i]);
            while (matcher.find()) {
                String numero = matcher.group();
                String tipo = clasificarNumero(numero);
                modeloTabla.addRow(new Object[]{contador++, i + 1, numero, tipo});
            }
        }
    }

    private String clasificarNumero(String numero) {
        String numSinComas = numero.replace(",", "");

        if (numSinComas.matches("^\\d+$")) return "Natural";
        if (numSinComas.matches("^\\d+\\.\\d+$")) return "Real";
        if (numSinComas.matches("^\\d+(\\.\\d+)?%$")) return "Porcentaje";
        if (numSinComas.matches("^\\$\\d+(\\.\\d+)?$")) return "Valor monetario";
        return "Desconocido";
    }
}