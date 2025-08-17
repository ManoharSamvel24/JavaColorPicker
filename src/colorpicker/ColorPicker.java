package colorpicker;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.datatransfer.StringSelection;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.swing.FontIcon;


public class ColorPicker {

    private static JLabel hexLabel, hslLabel, hsvLabel, cmykLabel;
    private static JButton hexCopy, hslCopy, hsvCopy, cmykCopy;
    private static JSlider redSlider, greenSlider, blueSlider, alphaSlider;
    private static JTextField redField, greenField, blueField, alphaField;
    private static RoundedPanel colorPreview;

    // ===== NEW: Added these class-level variables =====
    private static Robot robot;
    private static JWindow tooltip;
    private static JWindow overlay;
    private static Timer colorPickTimer;

    //Initiazlizing Robot class in static - out of methods
    static {
        try {
            robot = new Robot(); // Initialize Robot once
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ColorPicker::createUI); // Start app
    }

    // ============= UI Code ================
    static void createUI() {
        
        // UI Manager to set look and feel - FlatlafLight 
        // Set Font to 'Segoe UI' of size 13
        try{
            UIManager.put("Panel.arc", 20);
            UIManager.setLookAndFeel(new FlatLightLaf());
            Font uiFont = new Font("Segoe UI", Font.PLAIN, 13);
            java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof Font) {
                    UIManager.put(key, uiFont);
                }
            }
        }
        catch(UnsupportedLookAndFeelException e){
            e.printStackTrace();
        }
        JFrame frame = new JFrame("Color Picker");                  // Main Frame Window initialization
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);       // Set close opearion
        frame.setSize(400, 500);
        frame.setLayout(new GridBagLayout());                       // Setting GridBag Layout for UI
        frame.setResizable(false);
        
        JButton infoBtn = new JButton();
        infoBtn.setIcon(FontIcon.of(FontAwesome.INFO_CIRCLE, 18, new Color(70, 130, 180)));
        infoBtn.setPreferredSize(new Dimension(32, 32));
        infoBtn.setFocusPainted(false);
        infoBtn.setBorderPainted(false);
        infoBtn.setContentAreaFilled(false);
        infoBtn.setOpaque(false);
        infoBtn.setToolTipText("About Developer");
        infoBtn.setFocusable(false);
        infoBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame,
                "Color Picker\nMade by Manohar Samvel\nInspired by Powertoys colorpicker.",
                "About Color Picker",
                JOptionPane.INFORMATION_MESSAGE);
        });
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        infoPanel.add(infoBtn);
        
        // Continue with rest of UI


        

        GridBagConstraints gbc = new GridBagConstraints();          // Grid bag constraints are used to specify the size,position and layout behaviour of components
        gbc.insets = new Insets(5, 10, 5, 10);                      // add pading space top,left,bottom,right
        gbc.fill = GridBagConstraints.HORIZONTAL;                   // components strecth horizontally to fill the width of the grid cell

        int row = 0;
        gbc.gridx = 2; // right column
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        frame.add(infoPanel, gbc);
        row++;
        
        redSlider = new JSlider(0, 255, 0);                         // slider for red color     R
        greenSlider = new JSlider(0, 255, 0);                       // slider for green color   G
        blueSlider = new JSlider(0, 255, 0);                        // slider for blue color    B
        alphaSlider = new JSlider(0, 255, 255);                     // slider for alpha value   A

        ChangeListener cl = e -> updateColor();                     // Listener object for color update dynamically
        redSlider.addChangeListener(cl);                            // add the listener object to red slider
        greenSlider.addChangeListener(cl);                          // add the listener object to green slider
        blueSlider.addChangeListener(cl);                           // add the listener object to blue slider
        alphaSlider.addChangeListener(cl);                          // add the listener object to alpha slider
        
        redSlider.setFocusable(false);
        greenSlider.setFocusable(false);
        blueSlider.setFocusable(false);
        alphaSlider.setFocusable(false);

        redField = createValueField(redSlider);                     /*create the text box field for color value showing in it , for the 4 colors*/
        greenField = createValueField(greenSlider);
        blueField = createValueField(blueSlider);
        alphaField = createValueField(alphaSlider);
        alphaField.setText("255");
        
//        redField.setFocusable(false);
//        greenField.setFocusable(false);
//        blueField.setFocusable(false);
//        alphaField.setFocusable(false);
        
        
        colorPreview = new RoundedPanel(20);
        colorPreview.setBackground(Color.BLACK);
        colorPreview.setPreferredSize(new Dimension(50, 50));
        

        hexLabel = new JLabel("HEX: #000000");
        hslLabel = new JLabel("HSL: 0°, 0%, 0%");
        hsvLabel = new JLabel("HSV: 0°, 0%, 0%");
        cmykLabel = new JLabel("CMYK: 0%, 0%, 0%, 0%");

        hexCopy = createCopyButton(() -> hexLabel.getText().replace("HEX: ", ""));
        hslCopy = createCopyButton(() -> hslLabel.getText().replace("HSL: ", ""));
        hsvCopy = createCopyButton(() -> hsvLabel.getText().replace("HSV: ", ""));
        cmykCopy = createCopyButton(() -> cmykLabel.getText().replace("CMYK: ", ""));

        JButton pickBtn = new JButton("Press & Drag to Pick Color");
        JButton chooseBtn = new JButton("Choose Color");


        chooseBtn.addActionListener(e -> {
            Color selected = JColorChooser.showDialog(frame, "Select a Color", colorPreview.getBackground());
            if (selected != null) {
                redSlider.setValue(selected.getRed());
                greenSlider.setValue(selected.getGreen());
                blueSlider.setValue(selected.getBlue());
                alphaSlider.setValue(selected.getAlpha());
                updateColor();
            }
        });
        
        pickBtn.setFocusable(false);
        chooseBtn.setFocusable(false);

        setScaledIcon(pickBtn,"/resources/dropper.png",20,20);
        pickBtn.setFocusable(false);
        pickBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startColorPicking();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                stopColorPicking();
            }
        });

        //int row = 0;
        gbc.gridx = 0; gbc.gridy = row;
        frame.add(new JLabel("Red:"), gbc);
        gbc.gridx = 1; frame.add(redSlider, gbc);
        gbc.gridx = 2; frame.add(redField, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        frame.add(new JLabel("Green:"), gbc);
        gbc.gridx = 1; frame.add(greenSlider, gbc);
        gbc.gridx = 2; frame.add(greenField, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        frame.add(new JLabel("Blue:"), gbc);
        gbc.gridx = 1; frame.add(blueSlider, gbc);
        gbc.gridx = 2; frame.add(blueField, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        frame.add(new JLabel("Alpha:"), gbc);
        gbc.gridx = 1; frame.add(alphaSlider, gbc);
        gbc.gridx = 2; frame.add(alphaField, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        frame.add(new JLabel("Preview:"), gbc);
        gbc.gridx = 1; frame.add(colorPreview, gbc);

        // === Group color value labels + copy buttons inside a bordered panel ===
        JPanel colorValuesPanel = new JPanel(new GridBagLayout());
        colorValuesPanel.setBorder(BorderFactory.createTitledBorder("Color Values"));

        GridBagConstraints valGbc = new GridBagConstraints();
        valGbc.insets = new Insets(5, 5, 5, 5);
        valGbc.anchor = GridBagConstraints.WEST;

        valGbc.gridx = 0; valGbc.gridy = 0;
        colorValuesPanel.add(hexLabel, valGbc);
        valGbc.gridx = 1; colorValuesPanel.add(hexCopy, valGbc);

        valGbc.gridx = 0; valGbc.gridy++;
        colorValuesPanel.add(hslLabel, valGbc);
        valGbc.gridx = 1; colorValuesPanel.add(hslCopy, valGbc);

        valGbc.gridx = 0; valGbc.gridy++;
        colorValuesPanel.add(hsvLabel, valGbc);
        valGbc.gridx = 1; colorValuesPanel.add(hsvCopy, valGbc);

        valGbc.gridx = 0; valGbc.gridy++;
        colorValuesPanel.add(cmykLabel, valGbc);
        valGbc.gridx = 1; colorValuesPanel.add(cmykCopy, valGbc);

        // === Add panel to frame ===
        gbc.gridx = 0; gbc.gridy = ++row;
        gbc.gridwidth = 3;
        frame.add(colorValuesPanel, gbc);
        
        // === Panel for both buttons ===
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(pickBtn);
        buttonPanel.add(chooseBtn);

        // === Add both buttons panel ===
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        frame.add(buttonPanel, gbc);


        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private static void setScaledIcon(JButton button, String resourcePath, int width, int height) {
        ImageIcon icon = new ImageIcon(ColorPicker.class.getResource(resourcePath));
        Image scaledImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        button.setIcon(new ImageIcon(scaledImage));
    }

    
    // ===== NEW: Replaced pickColor() with these two methods =====
    private static void startColorPicking() {
        if (tooltip == null) {
            // Initialize tooltip (only once)
            tooltip = new JWindow();
            tooltip.setAlwaysOnTop(true);
            tooltip.setBackground(new Color(0, 0, 0, 0));
            tooltip.setSize(130, 40);
            tooltip.setShape(new RoundRectangle2D.Double(0, 0, 130, 40, 20, 20));

            JPanel panel = new JPanel(new BorderLayout(5, 5));
            panel.setBackground(new Color(0, 0, 0, 200));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

            JPanel colorBox = new JPanel();
            colorBox.setPreferredSize(new Dimension(40, 25));
            colorBox.setBackground(Color.WHITE);

            JLabel hex = new JLabel("#FFFFFF");
            hex.setForeground(Color.WHITE);
            hex.setFont(new Font("Consolas", Font.BOLD, 13));
            hex.setHorizontalAlignment(SwingConstants.CENTER);

            panel.add(colorBox, BorderLayout.WEST);
            panel.add(hex, BorderLayout.CENTER);
            tooltip.add(panel);

            // Initialize overlay (only once)
            overlay = new JWindow();
            overlay.setBackground(new Color(0, 0, 0, 1));
            overlay.setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
            overlay.setAlwaysOnTop(true);

            // Initialize timer (only once)
            colorPickTimer = new Timer(20, evt -> {
                PointerInfo info = MouseInfo.getPointerInfo();
                if (info != null) {
                    Point p = info.getLocation();
                    Color c = robot.getPixelColor(p.x, p.y);
                    colorBox.setBackground(c);
                    hex.setText(String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()));

                    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                    int x = p.x + 15;
                    int y = p.y + 15;
                    
                    if (x + tooltip.getWidth() > screen.width)
                        x = p.x - tooltip.getWidth() - 15;
                    
                    if (y + tooltip.getHeight() > screen.height)
                        y = p.y - tooltip.getHeight() - 15;

                    tooltip.setLocation(x, y);
                    tooltip.setVisible(true);
                }
            });
        }

        overlay.setVisible(true);
        overlay.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        colorPickTimer.start();
    }

    private static void stopColorPicking() {
        if (colorPickTimer != null) {
            colorPickTimer.stop();
        }
        if (tooltip != null) {
            tooltip.setVisible(false);
        }
        if (overlay != null) {
            overlay.setVisible(false);
        }

        Point p = MouseInfo.getPointerInfo().getLocation();
        Color c = robot.getPixelColor(p.x, p.y);
        redSlider.setValue(c.getRed());
        greenSlider.setValue(c.getGreen());
        blueSlider.setValue(c.getBlue());
        updateColor();
    }

    static JTextField createValueField(JSlider slider) {
        JTextField field = new JTextField("0", 3);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.addActionListener(e -> {
            try {
                int val = Integer.parseInt(field.getText().trim());
                slider.setValue(Math.min(255, Math.max(0, val)));
            } catch (NumberFormatException ignored) {}
        });
        slider.addChangeListener(e -> field.setText(String.valueOf(slider.getValue())));
        return field;
    }

    static void updateColor() {
        Color color = new Color(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue(), alphaSlider.getValue());
        colorPreview.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
        hexLabel.setText("HEX: " + String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));
        hslLabel.setText("HSL: " + toHSLString(color));
        hsvLabel.setText("HSV: " + toHSVString(color));
        cmykLabel.setText("CMYK: " + toCMYKString(color));
    }

    static String toHSLString(Color color) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float h = 0, s = 0, l = (max + min) / 2f;

        if (max != min) {
            float d = max - min;
            s = l > 0.5f ? d / (2f - max - min) : d / (max + min);
            if (max == r) h = (g - b) / d + (g < b ? 6 : 0);
            else if (max == g) h = (b - r) / d + 2;
            else h = (r - g) / d + 4;
            h /= 6;
        }
        return String.format("%.0f°, %.0f%%, %.0f%%", h * 360, s * 100, l * 100);
    }

    static String toHSVString(Color color) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float h = 0, s, v = max;

        float d = max - min;
        s = max == 0 ? 0 : d / max;

        if (max != min) {
            if (max == r) h = (g - b) / d + (g < b ? 6 : 0);
            else if (max == g) h = (b - r) / d + 2;
            else h = (r - g) / d + 4;
            h /= 6;
        }

        return String.format("%.0f°, %.0f%%, %.0f%%", h * 360, s * 100, v * 100);
    }

    static String toCMYKString(Color color) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float k = 1 - Math.max(r, Math.max(g, b));
        if (k == 1) return "0%, 0%, 0%, 100%";
        float c = (1 - r - k) / (1 - k);
        float m = (1 - g - k) / (1 - k);
        float y = (1 - b - k) / (1 - k);
        return String.format("%.0f%%, %.0f%%, %.0f%%, %.0f%%", c * 100, m * 100, y * 100, k * 100);
    }

    static JButton createCopyButton(StringPropertyGetter getter) {
        JButton copyBtn = new JButton("Copy");
        copyBtn.setFocusable(false);
        copyBtn.setMargin(new Insets(2, 5, 2, 5));
        copyBtn.addActionListener(e -> {
            StringSelection sel = new StringSelection(getter.get());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
        });
        return copyBtn;
    }

    interface StringPropertyGetter {
        String get();
    }
    
    public static class RoundedPanel extends JPanel {
        private final int radius;

        public RoundedPanel(int rad) {
            this.radius = rad;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw background
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            // Draw border
            g2.setColor(Color.lightGray);
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            super.paintComponent(g2);
            g2.dispose();
        }
    }
}