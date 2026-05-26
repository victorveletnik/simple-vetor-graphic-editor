import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

/**
 * Klasa reprezentująca prostokąt na płótnie edytora graficznego.
 * Rozszerza {@link Rectangle2D.Double} i implementuje {@link Serializable}
 * umożliwiając zapis do pliku.
 *
 * @author Victor Veletnik INA
 * @version 1.0
 */
public class Rectangle extends Rectangle2D.Double implements Serializable {

    /** Wersja serializacji. */
    private static final long serialVersionUID = 1L;

    /** Kolor wypełnienia prostokąta. */
    public Color color;

    /** Kąt obrotu w stopniach. */
    public double rotation = 0;

    /**
     * Tworzy nowy prostokąt o podanych parametrach.
     *
     * @param x      współrzędna X lewego górnego rogu
     * @param y      współrzędna Y lewego górnego rogu
     * @param width  szerokość prostokąta
     * @param height wysokość prostokąta
     * @param color  kolor wypełnienia
     */
    public Rectangle(double x, double y, double width, double height, Color color) {
        super(x, y, width, height);
        this.color = color;
    }

    /**
     * Zwraca kolor wypełnienia prostokąta.
     *
     * @return kolor wypełnienia
     */
    public Color getColor() {
        return color;
    }

    /**
     * Ustawia kolor wypełnienia prostokąta.
     *
     * @param color nowy kolor wypełnienia
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Przesuwa prostokąt o podany wektor przesunięcia.
     *
     * @param dx przesunięcie w osi X
     * @param dy przesunięcie w osi Y
     */
    public void Move(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }

    /**
     * Zmienia rozmiar prostokąta o podaną wartość.
     * Korekta pozycji zapewnia, że prostokąt pozostaje wyśrodkowany.
     *
     * @param delta zmiana rozmiaru (dodatnia = powiększenie, ujemna = pomniejszenie)
     */
    public void ChangeSize(double delta) {
        // Zabezpieczenie przed zerowym rozmiarem
        if (this.width + delta < 4 || this.height + delta < 4) return;
        this.x -= delta / 2;
        this.y -= delta / 2;
        this.width += delta;
        this.height += delta;
    }

    /**
     * Rysuje prostokąt na podanym kontekście graficznym z uwzględnieniem obrotu.
     *
     * @param g kontekst graficzny 2D
     */
    public void draw(Graphics2D g) {
        // Zapamiętaj poprzednią transformację
        AffineTransform old = g.getTransform();

        // Środek prostokąta do obrotu
        double cx = this.x + this.width / 2;
        double cy = this.y + this.height / 2;

        // Zastosuj obrót względem środka
        g.rotate(Math.toRadians(rotation), cx, cy);

        g.setColor(color);
        g.fill(this);

        // Przywróć transformację
        g.setTransform(old);
    }
}
