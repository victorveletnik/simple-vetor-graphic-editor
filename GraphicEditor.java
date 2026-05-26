import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Główna klasa edytora graficznego.
 * Tworzy okno aplikacji z menu głównym (Plik, Narzędzia, Pomoc),
 * panelem rysunkowym ({@link Surface}), paskiem statusu z podpowiedziami
 * oraz przyciskiem Info.
 *
 * <p>Funkcjonalności:
 * <ul>
 *   <li>Tworzenie figur: okrąg, prostokąt, wielokąt (klikanie wierzchołków)</li>
 *   <li>Zaznaczanie i przesuwanie figur (przeciąganie LPM)</li>
 *   <li>Skalowanie figur (scroll)</li>
 *   <li>Obracanie figur (Ctrl + scroll)</li>
 *   <li>Zmiana koloru figury (PPM → menu kontekstowe)</li>
 *   <li>Zapis figur do pliku i odczyt z pliku (Ctrl+S / Ctrl+O)</li>
 * </ul>
 *
 * @author Victor Veletnik INA
 * @version 1.0
 */
public class GraphicEditor extends JFrame {

    /** Panel rysunkowy — główny obszar roboczy. */
    private Surface surface;

    /** Pasek statusu na dole okna - wyświetla podpowiedzi. */
    private JLabel statusBar;

    /**
     * Tworzy nową instancję edytora i inicjalizuje interfejs.
     */
    public GraphicEditor() {
        initUI();
    }

    /**
     * Inicjalizuje wszystkie elementy interfejsu użytkownika.
     */
    public void initUI() {
        surface = new Surface();
        add(surface, BorderLayout.CENTER);

        // Pasek statusu na dole
        statusBar = new JLabel("Wybierz narzędzie z menu 'Narzędzia'.");
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        statusBar.setFont(statusBar.getFont().deriveFont(11f));

        // Podłącz pasek statusu do panelu
        surface.setStatusBar(statusBar);

        setTitle("Edytor Graficzny");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // ── Pasek menu ──────────────────────────────────────────────────────

        JMenuBar menuBar = new JMenuBar();

        // - Menu Plik 
        JMenu fileMenu = new JMenu("Plik");

        JMenuItem saveItem = new JMenuItem("Zapisz figury...");
        saveItem.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        saveItem.addActionListener(e -> saveShapes());

        JMenuItem loadItem = new JMenuItem("Wczytaj figury...");
        loadItem.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        loadItem.addActionListener(e -> loadShapes());

        JMenuItem exitItem = new JMenuItem("Wyjście");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // -- Menu Narzędzia --
        JMenu toolsMenu = new JMenu("Narzędzia");

        JMenuItem selectItem = new JMenuItem("Zaznacz  [S]");
        selectItem.setAccelerator(KeyStroke.getKeyStroke("S"));
        selectItem.addActionListener(e -> surface.setCurrentTool(Surface.Tool.SELECT));

        JMenuItem rectItem = new JMenuItem("Prostokąt  [R]");
        rectItem.setAccelerator(KeyStroke.getKeyStroke("R"));
        rectItem.addActionListener(e -> surface.setCurrentTool(Surface.Tool.RECTANGLE));

        JMenuItem circleItem = new JMenuItem("Okrąg  [C]");
        circleItem.setAccelerator(KeyStroke.getKeyStroke("C"));
        circleItem.addActionListener(e -> surface.setCurrentTool(Surface.Tool.CIRCLE));

        JMenuItem poliItem = new JMenuItem("Wielokąt  [P]");
        poliItem.setAccelerator(KeyStroke.getKeyStroke("P"));
        poliItem.addActionListener(e -> surface.setCurrentTool(Surface.Tool.POLYGON));

        toolsMenu.add(selectItem);
        toolsMenu.addSeparator();
        toolsMenu.add(rectItem);
        toolsMenu.add(circleItem);
        toolsMenu.add(poliItem);

        // -- Menu Pomoc --
        JMenu helpMenu = new JMenu("Pomoc");

        JMenuItem helpItem = new JMenuItem("Instrukcja użytkownika");
        helpItem.addActionListener(e -> showHelp());

        JMenuItem infoMenuItem = new JMenuItem("O programie");
        infoMenuItem.addActionListener(e -> showInfo());

        helpMenu.add(helpItem);
        helpMenu.addSeparator();
        helpMenu.add(infoMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        // ── Panel dolny: status + przycisk Info ────────────────────────────

        JButton infoButton = new JButton("ℹ  Info");
        infoButton.addActionListener(e -> showInfo());

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(statusBar, BorderLayout.CENTER);
        southPanel.add(infoButton, BorderLayout.EAST);
        add(southPanel, BorderLayout.SOUTH);
    }

    /**
     * Zapisuje figury z panelu do pliku wybranego przez użytkownika
     * przy użyciu serializacji Java.
     */
    private void saveShapes() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Zapisz figury");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Pliki edytora (*.gfx)", "gfx"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".gfx"))
                file = new File(file.getAbsolutePath() + ".gfx");
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(new ArrayList<>(surface.getShapes()));
                JOptionPane.showMessageDialog(this,
                        "Zapisano " + surface.getShapes().size() + " figur(y) do:\n" + file.getName(),
                        "Zapis", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Błąd zapisu: " + ex.getMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Wczytuje figury z pliku wybranego przez użytkownika.
     * Po wczytaniu możliwa jest dalsza edycja figur.
     */
    @SuppressWarnings("unchecked")
    private void loadShapes() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Wczytaj figury");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Pliki edytora (*.gfx)", "gfx"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                List<Object> loaded = (List<Object>) ois.readObject();
                surface.loadShapes(loaded);
                JOptionPane.showMessageDialog(this,
                        "Wczytano " + loaded.size() + " figur(y) z:\n" + file.getName(),
                        "Odczyt", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "Błąd odczytu: " + ex.getMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Wyświetla okno z informacjami o programie. */
    private void showInfo() {
        JOptionPane.showMessageDialog(this,
                "Edytor Graficzny\n"
                + "Wersja: 1.0\n"
                + "Autor: Victor Veletnik INA\n\n"
                + "Przeznaczenie:\n"
                + "Prosta aplikacja do tworzenia i edycji figur geometrycznych\n"
                + "(prostokąt, okrąg, wielokąt dowolny).",
                "O programie", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Wyświetla instrukcję użytkownika. */
    private void showHelp() {
        JOptionPane.showMessageDialog(this,
                "INSTRUKCJA UŻYTKOWNIKA\n\n"
                + "1. TWORZENIE FIGUR:\n"
                + "   • Prostokąt / Okrąg: wybierz narzędzie, kliknij na płótnie.\n"
                + "   • Wielokąt: wybierz 'Wielokąt', klikaj kolejne wierzchołki,\n"
                + "     podwójne kliknięcie zamyka figurę (wymagane min. 3 punkty).\n"
                + "     PPM lub Esc = anulowanie rysowania.\n\n"
                + "2. ZAZNACZANIE I PRZESUWANIE:\n"
                + "   • Wybierz 'Zaznacz' [S], kliknij i przeciągnij figurę.\n\n"
                + "3. SKALOWANIE:\n"
                + "   • Zaznacz figurę, kręć kółkiem myszy.\n\n"
                + "4. OBRACANIE:\n"
                + "   • Zaznacz figurę, przytrzymaj Ctrl i kręć kółkiem.\n\n"
                + "5. ZMIANA KOLORU:\n"
                + "   • Kliknij prawym przyciskiem na figurę → 'Zmień kolor'.\n\n"
                + "6. ZAPIS / ODCZYT:\n"
                + "   • Plik → Zapisz figury (Ctrl+S)\n"
                + "   • Plik → Wczytaj figury (Ctrl+O)",
                "Instrukcja", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Punkt wejścia aplikacji. Uruchamia edytor w wątku EDT.
     *
     * @param args argumenty wiersza poleceń (nieużywane)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GraphicEditor ex = new GraphicEditor();
            ex.setVisible(true);
        });
    }
}
