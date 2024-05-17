import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.IOException;

public class AIH {

    private final JFrame frame;
    private final JTextField txtNumeroInicial;
    private final JTextField txtNumeroFinal;
    private static final float SPACING_DISTANCE_MM = 12f;

    // Store the document for later access
    private Document document;

    public AIH() {
        frame = new JFrame("Gerador de Etiquetas");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a panel with a BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create a panel for the hospital name at the top
        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createTitledBorder("Hospital"));
        JLabel hospitalNameLabel = new JLabel("HOSPITAL GETÚLIO VARGAS");
        hospitalNameLabel.setForeground(new Color(0, 100, 0));  // Dark green
        hospitalNameLabel.setHorizontalAlignment(JLabel.CENTER); // Center the label
        topPanel.add(hospitalNameLabel);

        // Create a panel for the main content using GridLayout
        JPanel contentPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        contentPanel.setBorder(BorderFactory.createTitledBorder("Configurações"));

        JLabel labelInicial = new JLabel("Número inicial de AIH:");
        labelInicial.setHorizontalAlignment(JLabel.CENTER); // Center the label
        txtNumeroInicial = new JTextField();
        txtNumeroInicial.setDocument(new JTextFieldLimit(50));

        JLabel labelFinal = new JLabel("Número final de AIH:");
        labelFinal.setHorizontalAlignment(JLabel.CENTER); // Center the label
        txtNumeroFinal = new JTextField();
        txtNumeroFinal.setDocument(new JTextFieldLimit(50));

        JButton gerarButton = new JButton("Gerar Etiquetas");
        gerarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Validate input before processing
                    validateInput();

                    long numeroInicial = Long.parseLong(txtNumeroInicial.getText());
                    long numeroFinal = Long.parseLong(txtNumeroFinal.getText());

                    gerarEtiquetas(numeroInicial, numeroFinal);
                    JOptionPane.showMessageDialog(frame, "Etiqueta gerada com sucesso!");

                    // Abrir o arquivo PDF gerado
                    Desktop.getDesktop().open(new File("etiquetas.pdf"));

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Por favor, insira números válidos!", "Erro", JOptionPane.ERROR_MESSAGE);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                } catch (DocumentException | IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Ocorreu um erro ao gerar o documento PDF.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        contentPanel.add(labelInicial);
        contentPanel.add(txtNumeroInicial);
        contentPanel.add(labelFinal);
        contentPanel.add(txtNumeroFinal);
        contentPanel.add(new JLabel());
        contentPanel.add(gerarButton);

        // Add the topPanel and contentPanel to the mainPanel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        frame.getContentPane().add(mainPanel);
        frame.setSize(400, 250);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void validateInput() {
        if (txtNumeroInicial.getText().isEmpty() || txtNumeroFinal.getText().isEmpty()) {
            throw new IllegalArgumentException("Por favor, preencha todos os campos.");
        }
    }

    private void gerarEtiquetas(long numeroInicial, long numeroFinal) throws DocumentException, IOException {
        Document document = new Document(new com.itextpdf.text.Rectangle(42f, 98f), 0, 0, 4, 0);
        document.setPageSize(document.getPageSize().rotate());

        try {
            PdfWriter.getInstance(document, new FileOutputStream("etiquetas.pdf"));
            document.open();

            for (long numero = numeroInicial; numero <= numeroFinal; numero++) {
                document.newPage();

                // Número AIH atual
                String numeroAIH = formatarNumeroAIH(numero);

                // Adicione o número AIH como um novo Paragraph à página
                Paragraph paragraph1 = new Paragraph(numeroAIH, new Font(Font.FontFamily.HELVETICA, 6));
                paragraph1.setAlignment(Paragraph.ALIGN_CENTER);
                paragraph1.setSpacingBefore(SPACING_DISTANCE_MM);
                paragraph1.setSpacingAfter(SPACING_DISTANCE_MM);
                document.add(paragraph1);

                // Segundo número AIH com sufixo "CÓPIA"
                String numeroAIHCopia = numeroAIH + " CÓPIA";

                // Adicione o segundo número AIH como um novo Paragraph à página
                Paragraph paragraph2 = new Paragraph(numeroAIHCopia, new Font(Font.FontFamily.HELVETICA, 6));
                paragraph2.setAlignment(Paragraph.ALIGN_CENTER);
                paragraph2.setSpacingBefore(SPACING_DISTANCE_MM);
                paragraph2.setSpacingAfter(SPACING_DISTANCE_MM);
                document.add(paragraph2);
            }
        } finally {
            document.close();
        }
    }
    private float getFontSizeForLabelSize(com.itextpdf.text.Rectangle pageSize) {
        // Calculate the font size based on label size
        float labelWidth = pageSize.getWidth() - document.leftMargin() - document.rightMargin();
        float labelHeight = pageSize.getHeight() - document.topMargin() - document.bottomMargin();

        // Choose a suitable font size, adjust as needed
        float fontSize = Math.min(labelWidth / 10, labelHeight / 10);

        return fontSize;
    }

    private String formatarNumeroAIH(long numero) {
        String numeroString = String.valueOf(numero);
        int length = numeroString.length();

        if (length >= 2) {
            // Insira hífen como penúltimo dígito
            return numeroString.substring(0, length - 1) + "-" + numeroString.substring(length - 1);
        } else {
            return numeroString;
        }
    }

    class JTextFieldLimit extends PlainDocument {
        private int limit;

        JTextFieldLimit(int limit) {
            super();
            this.limit = limit;
        }

        public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
            if (str == null)
                return;

            if ((getLength() + str.length()) <= limit) {
                super.insertString(offset, str, attr);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AIH::new);
    }
}