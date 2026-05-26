import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.GeneralPath;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * Klasa reprezentująca wielokąt na płótnie edytora graficznego.
 * Implementuje {@link Serializable} umożliwiając zapis do pliku.
 *
 * @author Victor Veletnik INA
 * @version 1.0
 */
public class Poli implements Serializable {

    /** Wersja serializacji. */
    private static final long serialVersionUID = 1L;

    /** Lista wierzchołków wielokąta. */
    public List<Point2D.Double> points;

    /** Kolor wypełnienia wielokąta. */
    public Color color;

    /** Kąt obrotu w stopniach. */
    public double rotation = 0;

    /** Współrzędna X środka ciężkości (ułatwia przesuwanie i sprawdzanie trafień). */
    public double centerX, centerY;

    /**
     * Tworzy nowy wielokąt na podstawie listy wierzchołków i koloru.
     *
     * @param points lista wierzchołków wielokąta
     * @param color  kolor wypełnienia
     */
    public Poli(List<Point2D.Double> points, Color color) {
        this.points = new ArrayList<>(points);
        this.color = color;
        updateCenter();
    }

    /**
     * Aktualizuje środek ciężkości wielokąta na podstawie aktualnych wierzchołków.
     */
    private void updateCenter() {
        double sumX = 0, sumY = 0;
        for (Point2D.Double p : points) {
            sumX += p.x;
            sumY += p.y;
        }
        centerX = sumX / points.size();
        centerY = sumY / points.size();
    }

    /**
     * Buduje obiekt {@link GeneralPath} z listy wierzchołków.
     *
     * @return zamknięty wielokąt jako GeneralPath
     */
    private GeneralPath buildPath() {
        GeneralPath path = new GeneralPath();
        path.moveTo(points.get(0).x, points.get(0).y);
        for (int i = 1; i < points.size(); i++) {
            path.lineTo(points.get(i).x, points.get(i).y);
        }
        // Łączenie ostatniego punktu z pierwszym
        path.closePath();
        return path;
    }

    /**
     * Rysuje wielokąt na podanym kontekście graficznym z uwzględnieniem obrotu.
     *
     * @param g kontekst graficzny 2D
     */
    public void draw(Graphics2D g) {
        if (points.isEmpty()) return;

        // Zapamiętaj poprzednią transformację
        AffineTransform old = g.getTransform();

        // Zastosuj obrót względem środka ciężkości
        g.rotate(Math.toRadians(rotation), centerX, centerY);

        g.setColor(color);
        g.fill(buildPath());

        // Przywróć transformację
        g.setTransform(old);
    }

    /**
     * Zwraca kolor wypełnienia wielokąta.
     *
     * @return kolor wypełnienia
     */
    public Color getColor() {
        return color;
    }

    /**
     * Ustawia kolor wypełnienia wielokąta.
     *
     * @param color nowy kolor wypełnienia
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Przesuwa wielokąt o podany wektor przesunięcia.
     *
     * @param dx przesunięcie w osi X
     * @param dy przesunięcie w osi Y
     */
    public void Move(double dx, double dy) {
        for (Point2D.Double p : points) {
            p.x += dx;
            p.y += dy;
        }
        updateCenter();
    }

    /**
     * Zmienia rozmiar wielokąta przez skalowanie wierzchołków względem środka ciężkości.
     *
     * @param delta zmiana rozmiaru (dodatnia = powiększenie, ujemna = pomniejszenie)
     */
    public void ChangeSize(double delta) {
        // Współczynnik skalowania względem środka ciężkości
        double scaleFactor = 1.0 + delta / 100.0;
        if (scaleFactor <= 0.01) return;
        for (Point2D.Double p : points) {
            p.x = centerX + (p.x - centerX) * scaleFactor;
            p.y = centerY + (p.y - centerY) * scaleFactor;
        }
        // Środek ciężkości nie zmienia się przy skalowaniu względem siebie
    }

    /**
     * Sprawdza, czy punkt (x, y) leży wewnątrz wielokąta.
     * Używane przy zaznaczaniu figury myszą.
     *
     * @param x współrzędna X punktu do sprawdzenia
     * @param y współrzędna Y punktu do sprawdzenia
     * @return {@code true} jeśli punkt leży wewnątrz wielokąta
     */
    public boolean contains(double x, double y) {
        return buildPath().contains(x, y);
    }
}
