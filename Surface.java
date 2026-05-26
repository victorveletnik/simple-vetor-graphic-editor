import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.GeneralPath;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;

/**
 * Główny panel rysunkowy edytora graficznego.
 * Obsługuje tworzenie, zaznaczanie, przesuwanie, skalowanie i obracanie figur.
 * Wielokąt rysowany jest przez klikanie kolejnych wierzchołków —
 * podwójne kliknięcie (przy min. 3 punktach) zamyka figurę.
 * Menu kontekstowe umożliwia zmianę koloru aktywnej figury.
 *
 * @author Victor Veletnik INA
 * @version 1.0
 */
public class Surface extends JPanel {

    /** Lista wszystkich ukończonych figur na panelu. */
    private final List<Object> shapes = new ArrayList<>();

    /** Aktualnie zaznaczona figura (lub {@code null} jeśli żadna). */
    private Object selectedShape = null;

    /**
     * Enum dostępnych narzędzi rysowania/edycji.
     */
    public enum Tool { SELECT, RECTANGLE, CIRCLE, POLYGON }

    /** Aktualnie wybrane narzędzie. */
    private Tool currentTool = Tool.SELECT;

    // ── Stan rysowania wielokąta ─────────────────────────────────────────────

    /**
     * Tymczasowa lista wierzchołków wielokąta w trakcie rysowania.
     * Pusta gdy nie rysujemy aktualnie wielokąta.
     */
    private final List<Point2D.Double> polyPoints = new ArrayList<>();

    /**
     * Aktualna pozycja kursora — używana do rysowania linii "gumki"
     * od ostatniego klikniętego wierzchołka do kursora.
     */
    private Point2D mousePos = null;

    /** Referencja do paska statusu (opcjonalna, do wyświetlania podpowiedzi). */
    private JLabel statusBar = null;

    /**
     * Tworzy nowy panel rysunkowy z białym tłem i podłączonymi listenerami myszy.
     */
    public Surface() {
        setBackground(Color.WHITE);
        MovingAdapter mouseAdapter = new MovingAdapter(this);
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);
    }

    /**
     * Ustawia referencję do paska statusu wyświetlającego podpowiedzi.
     *
     * @param label etykieta paska statusu
     */
    public void setStatusBar(JLabel label) {
        this.statusBar = label;
    }

    /**
     * Ustawia aktywne narzędzie. Przy zmianie z trybu wielokąta
     * niedokończony wielokąt jest anulowany.
     *
     * @param tool narzędzie do ustawienia
     */
    public void setCurrentTool(Tool tool) {
        if (this.currentTool == Tool.POLYGON && tool != Tool.POLYGON) {
            cancelPolygon();
        }
        this.currentTool = tool;
        updateStatus();
        repaint();
    }

    /**
     * Zwraca listę figur (używana przy zapisie do pliku).
     *
     * @return lista figur
     */
    public List<Object> getShapes() {
        return shapes;
    }

    /**
     * Wczytuje figury z listy zastępując bieżącą zawartość panelu.
     *
     * @param loaded lista figur do wczytania
     */
    public void loadShapes(List<Object> loaded) {
        shapes.clear();
        shapes.addAll(loaded);
        selectedShape = null;
        cancelPolygon();
        repaint();
    }

    /**
     * Czyści tymczasowy stan rysowania wielokąta.
     */
    private void cancelPolygon() {
        polyPoints.clear();
        mousePos = null;
    }

    /**
     * Aktualizuje tekst paska statusu zgodnie z aktualnym stanem.
     */
    private void updateStatus() {
        if (statusBar == null) return;
        if (currentTool == Tool.POLYGON) {
            if (polyPoints.isEmpty()) {
                statusBar.setText(
                    "Wielokąt: klikaj wierzchołki  |  podwójne kliknięcie = zamknij (min. 3 pkt)  |  PPM = anuluj");
            } else {
                statusBar.setText(
                    "Wielokąt: wierzchołki: " + polyPoints.size()
                    + "  |  podwójne kliknięcie = zamknij  |  Esc = anuluj  |  PPM = anuluj");
            }
        } else if (currentTool == Tool.SELECT) {
            statusBar.setText(
                "Zaznacz: LPM = wybierz/przeciągnij  |  Scroll = rozmiar  |  Ctrl+Scroll = obrót  |  PPM = kolor");
        } else {
            statusBar.setText("Kliknij lewym przyciskiem na płótnie aby narysować figurę.");
        }
    }

    /**
     * Rysuje wszystkie figury, podgląd budowanego wielokąta
     * oraz zaznaczenie aktywnej figury.
     *
     * @param g kontekst graficzny
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Ukończone figury
        for (Object shape : shapes) {
            if (shape instanceof Rectangle)   ((Rectangle) shape).draw(g2d);
            else if (shape instanceof Circle) ((Circle) shape).draw(g2d);
            else if (shape instanceof Poli)   ((Poli) shape).draw(g2d);
        }

        // Podgląd wielokąta w trakcie rysowania
        drawPolygonPreview(g2d);

        // Obrys zaznaczonej figury
        drawSelection(g2d);
    }

    /**
     * Rysuje wizualny podgląd wielokąta podczas klikania wierzchołków:
     * linie między punktami, linię "gumkę" do kursora i podgląd zamknięcia.
     *
     * @param g2d kontekst graficzny 2D
     */
    private void drawPolygonPreview(Graphics2D g2d) {
        if (polyPoints.isEmpty()) return;

        Stroke oldStroke = g2d.getStroke();
        Color  oldColor  = g2d.getColor();

        // Linie między klikniętymi punktami
        g2d.setColor(new Color(50, 50, 210));
        g2d.setStroke(new BasicStroke(2f));
        for (int i = 1; i < polyPoints.size(); i++) {
            Point2D.Double a = polyPoints.get(i - 1);
            Point2D.Double b = polyPoints.get(i);
            g2d.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
        }

        // Linia "gumka" od ostatniego punktu do kursora
        if (mousePos != null) {
            Point2D.Double last = polyPoints.get(polyPoints.size() - 1);
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{7, 4}, 0));
            g2d.drawLine((int) last.x, (int) last.y,
                         (int) mousePos.getX(), (int) mousePos.getY());

            // Podgląd zamknięcia: kursor → pierwszy punkt (gdy ≥3 punkty)
            if (polyPoints.size() >= 3) {
                Point2D.Double first = polyPoints.get(0);
                g2d.setColor(new Color(200, 60, 60, 150));
                g2d.drawLine((int) mousePos.getX(), (int) mousePos.getY(),
                             (int) first.x, (int) first.y);
            }
        }

        // Kółka w wierzchołkach (pierwszy = czerwony = punkt zamknięcia)
        g2d.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < polyPoints.size(); i++) {
            Point2D.Double p = polyPoints.get(i);
            g2d.setColor(i == 0 ? new Color(200, 40, 40) : new Color(50, 50, 210));
            g2d.fillOval((int) p.x - 5, (int) p.y - 5, 10, 10);
            g2d.setColor(Color.WHITE);
            g2d.drawOval((int) p.x - 5, (int) p.y - 5, 10, 10);
        }

        g2d.setStroke(oldStroke);
        g2d.setColor(oldColor);
    }

    /**
     * Rysuje czerwony przerywany obrys zaznaczonej figury z uwzględnieniem obrotu.
     *
     * @param g2d kontekst graficzny 2D
     */
    private void drawSelection(Graphics2D g2d) {
        if (selectedShape == null) return;

        Stroke oldStroke = g2d.getStroke();
        Color  oldColor  = g2d.getColor();

        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[]{6, 3}, 0));

        if (selectedShape instanceof Rectangle) {
            Rectangle r = (Rectangle) selectedShape;
            java.awt.geom.AffineTransform old = g2d.getTransform();
            g2d.rotate(Math.toRadians(r.rotation), r.x + r.width / 2, r.y + r.height / 2);
            g2d.draw(r);
            g2d.setTransform(old);

        } else if (selectedShape instanceof Circle) {
            Circle c = (Circle) selectedShape;
            java.awt.geom.AffineTransform old = g2d.getTransform();
            g2d.rotate(Math.toRadians(c.rotation), c.x + c.width / 2, c.y + c.height / 2);
            g2d.draw(c);
            g2d.setTransform(old);

        } else if (selectedShape instanceof Poli) {
            Poli p = (Poli) selectedShape;
            java.awt.geom.AffineTransform old = g2d.getTransform();
            g2d.rotate(Math.toRadians(p.rotation), p.centerX, p.centerY);
            GeneralPath path = new GeneralPath();
            path.moveTo(p.points.get(0).x, p.points.get(0).y);
            for (int i = 1; i < p.points.size(); i++)
                path.lineTo(p.points.get(i).x, p.points.get(i).y);
            path.closePath();
            g2d.draw(path);
            g2d.setTransform(old);
        }

        g2d.setStroke(oldStroke);
        g2d.setColor(oldColor);
    }

    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Wewnętrzna klasa obsługująca wszystkie zdarzenia myszy na panelu.
     */
    class MovingAdapter extends MouseAdapter {

        /** Referencja do panelu. */
        private final Surface surface;

        /** Ostatnia pozycja myszy — delta przy przeciąganiu. */
        private Point2D lastPoint;

        /**
         * Tworzy adapter. Rejestruje Escape jako anulowanie wielokąta.
         *
         * @param surface panel rysunkowy
         */
        public MovingAdapter(Surface surface) {
            this.surface = surface;

            // Escape = anuluj rysowanie wielokąta
            surface.getInputMap(WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelPoly");
            surface.getActionMap().put("cancelPoly", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (surface.currentTool == Tool.POLYGON && !surface.polyPoints.isEmpty()) {
                        surface.cancelPolygon();
                        surface.updateStatus();
                        surface.repaint();
                    }
                }
            });
        }

        /**
         * Obsługuje kliknięcia myszy (liczba kliknięć jest istotna dla wielokąta).
         *
         * @param e zdarzenie myszy
         */
        @Override
        public void mouseClicked(MouseEvent e) {

            // ── TRYB WIELOKĄTA ───────────────────────────────────────────────
            if (surface.currentTool == Tool.POLYGON) {

                if (SwingUtilities.isRightMouseButton(e)) {
                    // PPM podczas rysowania = anuluj
                    surface.cancelPolygon();
                    surface.updateStatus();
                    surface.repaint();
                    return;
                }

                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (e.getClickCount() == 2 && surface.polyPoints.size() >= 3) {
                        // Podwójne kliknięcie przy ≥3 pkt → zamknij.
                        // Java wywołuje najpierw clickCount=1 (dodaliśmy punkt),
                        // potem clickCount=2 — usuwamy ten ostatnio dodany duplikat.
                        surface.polyPoints.remove(surface.polyPoints.size() - 1);
                        finishPolygon();
                    } else if (e.getClickCount() == 1) {
                        // Pojedyncze kliknięcie → nowy wierzchołek
                        surface.polyPoints.add(new Point2D.Double(e.getX(), e.getY()));
                        surface.updateStatus();
                        surface.repaint();
                    }
                }
                return;
            }

            // ── PPM (poza trybem wielokąta) ──────────────────────────────────
            if (SwingUtilities.isRightMouseButton(e)) {
                for (int i = surface.shapes.size() - 1; i >= 0; i--) {
                    Object s = surface.shapes.get(i);
                    if (checkHit(s, e.getPoint())) {
                        surface.selectedShape = s;
                        surface.repaint();
                        break;
                    }
                }
                if (surface.selectedShape != null) showColorMenu(e);
                return;
            }

            // ── TRYB ZAZNACZANIA ─────────────────────────────────────────────
            if (surface.currentTool == Tool.SELECT) {
                surface.selectedShape = null;
                for (int i = surface.shapes.size() - 1; i >= 0; i--) {
                    Object s = surface.shapes.get(i);
                    if (checkHit(s, e.getPoint())) {
                        surface.selectedShape = s;
                        break;
                    }
                }
                surface.repaint();

            // ── PROSTOKĄT / OKRĄG ────────────────────────────────────────────
            } else if (surface.currentTool != Tool.POLYGON) {
                createSimpleShape(e.getX(), e.getY());
                surface.repaint();
            }
        }

        /**
         * Zamyka wielokąt — tworzy obiekt {@link Poli} i czyści stan tymczasowy.
         */
        private void finishPolygon() {
            if (surface.polyPoints.size() < 3) return;
            surface.shapes.add(new Poli(new ArrayList<>(surface.polyPoints), Color.ORANGE));
            surface.cancelPolygon();
            surface.updateStatus();
            surface.repaint();
        }

        /** Zapamiętuje punkt startowy przeciągania. */
        @Override
        public void mousePressed(MouseEvent e) {
            lastPoint = e.getPoint();
        }

        /**
         * Przesuwa zaznaczoną figurę podczas przeciągania (tryb SELECT).
         *
         * @param e zdarzenie myszy
         */
        @Override
        public void mouseDragged(MouseEvent e) {
            if (surface.currentTool == Tool.SELECT && surface.selectedShape != null) {
                double dx = e.getX() - lastPoint.getX();
                double dy = e.getY() - lastPoint.getY();
                Object s = surface.selectedShape;
                if (s instanceof Rectangle) ((Rectangle) s).Move(dx, dy);
                else if (s instanceof Circle)  ((Circle) s).Move(dx, dy);
                else if (s instanceof Poli)    ((Poli) s).Move(dx, dy);
                lastPoint = e.getPoint();
                surface.repaint();
            }
        }

        /**
         * Aktualizuje pozycję kursora dla linii "gumki" wielokąta.
         *
         * @param e zdarzenie myszy
         */
        @Override
        public void mouseMoved(MouseEvent e) {
            if (surface.currentTool == Tool.POLYGON && !surface.polyPoints.isEmpty()) {
                surface.mousePos = e.getPoint();
                surface.repaint();
            }
        }

        /**
         * Scroll = skalowanie, Ctrl+Scroll = obrót zaznaczonej figury.
         *
         * @param e zdarzenie kółka myszy
         */
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (surface.selectedShape == null) return;
            double rotAmt  = e.getWheelRotation() * 10.0;
            double sizeAmt = e.getWheelRotation() * -5.0;
            Object s = surface.selectedShape;

            if (e.isControlDown()) {
                if (s instanceof Rectangle) ((Rectangle) s).rotation += rotAmt;
                else if (s instanceof Circle)  ((Circle) s).rotation  += rotAmt;
                else if (s instanceof Poli)    ((Poli) s).rotation    += rotAmt;
            } else {
                if (s instanceof Rectangle) ((Rectangle) s).ChangeSize(sizeAmt);
                else if (s instanceof Circle)  ((Circle) s).ChangeSize(sizeAmt);
                else if (s instanceof Poli)    ((Poli) s).ChangeSize(sizeAmt);
            }
            surface.repaint();
        }

        /** Sprawdza trafienie kursorem w figurę. */
        private boolean checkHit(Object shape, Point2D point) {
            if (shape instanceof Rectangle)   return ((Rectangle) shape).contains(point);
            else if (shape instanceof Circle) return ((Circle) shape).contains(point);
            else if (shape instanceof Poli)
                return ((Poli) shape).contains(point.getX(), point.getY());
            return false;
        }

        /** Tworzy prostokąt lub okrąg w miejscu kliknięcia. */
        private void createSimpleShape(double x, double y) {
            if (surface.currentTool == Tool.RECTANGLE) {
                surface.shapes.add(new Rectangle(x - 25, y - 15, 50, 30, Color.BLUE));
            } else if (surface.currentTool == Tool.CIRCLE) {
                surface.shapes.add(new Circle(x, y, 25, Color.GREEN));
            }
        }

        /**
         * Wyświetla menu kontekstowe z opcją zmiany koloru i usunięcia figury.
         *
         * @param e zdarzenie myszy
         */
        private void showColorMenu(MouseEvent e) {
            JPopupMenu menu = new JPopupMenu();

            JMenuItem colorItem = new JMenuItem("Zmień kolor wypełnienia");
            colorItem.addActionListener(ae -> {
                Color current = Color.WHITE;
                Object s = surface.selectedShape;
                if (s instanceof Rectangle) current = ((Rectangle) s).getColor();
                else if (s instanceof Circle) current = ((Circle) s).getColor();
                else if (s instanceof Poli)   current = ((Poli) s).getColor();

                Color chosen = JColorChooser.showDialog(surface, "Wybierz kolor", current);
                if (chosen != null) {
                    if (s instanceof Rectangle) ((Rectangle) s).setColor(chosen);
                    else if (s instanceof Circle) ((Circle) s).setColor(chosen);
                    else if (s instanceof Poli)   ((Poli) s).setColor(chosen);
                    surface.repaint();
                }
            });

            JMenuItem deleteItem = new JMenuItem("Usuń figurę");
            deleteItem.addActionListener(ae -> {
                surface.shapes.remove(surface.selectedShape);
                surface.selectedShape = null;
                surface.repaint();
            });

            menu.add(colorItem);
            menu.addSeparator();
            menu.add(deleteItem);
            menu.show(surface, e.getX(), e.getY());
        }
    }
}
