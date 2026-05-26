import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.io.Serializable;

/**
 * Klasa reprezentująca okrąg na płótnie edytora graficznego.
 * Rozszerza {@link Ellipse2D.Double} i implementuje {@link Serializable}
 * umożliwiając zapis do pliku.
 *
 * @author Victor Veletnik INA
 * @version 1.0
 */
public class Circle extends Ellipse2D.Double implements Serializable {

    /** Wersja serializacji. */
    private static final long serialVersionUID = 1L;

    /** Kolor wypełnienia okręgu. */
    private Color color;

    /** Kąt obrotu w stopniach. */
    public double rotation = 0;

    /**
     * Tworzy nowy okrąg o podanym środku, promieniu i kolorze.
     *
     * @param x     współrzędna X środka okręgu
     * @param y     współrzędna Y środka okręgu
     * @param r     promień okręgu
     * @param color kolor wypełnienia
     */
    public Circle(double x, double y, double r, Color color) {
        super(x - r, y - r, r * 2, r * 2);
        this.color = color;
    }

    /**
     * Zwraca kolor wypełnienia okręgu.
     *
     * @return kolor wypełnienia
     */
    public Color getColor() {
        return color;
    }

    /**
     * Ustawia kolor wypełnienia okręgu.
     *
     * @param color nowy kolor wypełnienia
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Przesuwa okrąg o podany wektor przesunięcia.
     *
     * @param dx przesunięcie w osi X
     * @param dy przesunięcie w osi Y
     */
    public void Move(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }

    /**
     * Zmienia rozmiar okręgu o podaną wartość (dodaje do szerokości i wysokości).
     * Korekta pozycji zapewnia, że okrąg pozostaje wyśrodkowany.
     *
     * @param delta zmiana rozmiaru (dodatnia = powiększenie, ujemna = pomniejszenie)
     */
    public void ChangeSize(double delta) {
        // Zabezpieczenie przed zerowym rozmiarem
        if (this.width + delta < 4) return;
        this.x -= delta / 2;
        this.y -= delta / 2;
        this.width += delta;
        this.height += delta;
    }

    /**
     * Rysuje okrąg na podanym kontekście graficznym z uwzględnieniem obrotu.
     *
     * @param g kontekst graficzny 2D
     */
    public void draw(Graphics2D g) {
        //Narysuj
        g.setColor(color);
        g.fill(this);
    }
}
