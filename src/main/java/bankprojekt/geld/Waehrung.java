package bankprojekt.geld;

/**
 * Enumeration von ausgwählten Währungen und deren Umrechnungskurs ggü. dem Euro
 */
public enum Waehrung {
    /**
     * Euro
     */
    EUR(1.0),
    /**
     * Escudo
     */
    ESCUDO(109.8269),
    /**
     * Dobra
     */
    DOBRA(24304.7429),
    /**
     * Franc
     */
    FRANC(490.92);

    private final double umrechnungskurs;


    private Waehrung(double umrechnungskurs) {
        this.umrechnungskurs = umrechnungskurs;
    }

    public double getUmrechnungskurs() {
        return umrechnungskurs;
    }
}
