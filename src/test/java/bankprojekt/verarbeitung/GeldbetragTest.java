package bankprojekt.verarbeitung;

import bankprojekt.geld.Waehrung;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeldbetragTest {

    @Test
    void umrechnen() {

    }

    @Test
    void plus() {
    }
    @Test
    public void testUmrechnenInAndereWaehrung() {
        Geldbetrag betrag = new Geldbetrag(100, Waehrung.EUR);
        betrag.umrechnen(Waehrung.ESCUDO);
        assertEquals(10982.69, betrag.getBetrag(), 0.01, "100 EUR sollte 10982.69 ESCUDO entsprechen.");
        assertEquals(Waehrung.ESCUDO, betrag.getWaehrung(), "Die Währung sollte in ESCUDO geändert sein.");
    }

    @Test
    public void testUmrechnenInGleicheWaehrung() {
        Geldbetrag betrag = new Geldbetrag(100, Waehrung.EUR);
        betrag.umrechnen(Waehrung.EUR);
        assertEquals(100.00, betrag.getBetrag(), "Der Betrag sollte unverändert bleiben, da die Zielwährung EUR ist.");
        assertEquals(Waehrung.EUR, betrag.getWaehrung(), "Die Währung sollte weiterhin EUR sein.");
    }

    @Test
    public void testUmrechnenMitNullWaehrung() {
        Geldbetrag betrag = new Geldbetrag(100, Waehrung.EUR);
        assertThrows(NullPointerException.class, () -> betrag.umrechnen(null), "Eine Null-Zielwährung sollte eine NullPointerException auslösen.");
    }


    // Test s für Methode plus()

    @Test
    public void testPlusGleicheWaehrung() {
        Geldbetrag betrag1 = new Geldbetrag(100.50, Waehrung.EUR);
        Geldbetrag betrag2 = new Geldbetrag(49.50, Waehrung.EUR);
        Geldbetrag result = betrag1.plus(betrag2);
        assertEquals(150.00, result.getBetrag(), "Die Summe sollte 150.00 EUR betragen.");
        assertEquals(Waehrung.EUR, result.getWaehrung(), "Die Währung des Ergebnisses sollte EUR sein.");
    }

    @Test
    public void testPlusVerschiedeneWaehrungen() {
        Geldbetrag betrag1 = new Geldbetrag(100, Waehrung.EUR);
        Geldbetrag betrag2 = new Geldbetrag(10982.69, Waehrung.ESCUDO);
        Geldbetrag result = betrag1.plus(betrag2);
        result.umrechnen(Waehrung.EUR);
        assertEquals(200.00, result.getBetrag(), 0.01, "Die Summe sollte 200.00 EUR betragen, nachdem ESCUDO in EUR umgerechnet wurde.");
        assertEquals(Waehrung.EUR, result.getWaehrung(), "Die Währung des Ergebnisses sollte EUR sein.");
    }

    @Test
    public void testPlusMitRundung() {
        Geldbetrag betrag1 = new Geldbetrag(49.999, Waehrung.EUR);
        Geldbetrag betrag2 = new Geldbetrag(0.001, Waehrung.EUR);
        Geldbetrag result = betrag1.plus(betrag2);
        assertEquals(50.00, result.getBetrag(), "Das Ergebnis sollte auf 50.00 EUR gerundet sein.");
    }

    @Test
    public void testPlusMitNegativenSummand() {
        Geldbetrag betrag1 = new Geldbetrag(100, Waehrung.EUR);
        Geldbetrag betrag2 = new Geldbetrag(-50, Waehrung.EUR);
        assertThrows(IllegalArgumentException.class, () -> betrag1.plus(betrag2), "Negative Summanden sollten eine IllegalArgumentException auslösen.");
    }

    @Test
    public void testPlusMitNullSummand() {
        Geldbetrag betrag = new Geldbetrag(100, Waehrung.EUR);
        assertThrows(IllegalArgumentException.class, () -> betrag.plus(null), "Ein Null-Summand sollte eine IllegalArgumentException auslösen.");
    }

    @Test
    public void testPlusMitZeroSummand() {
        Geldbetrag betrag = new Geldbetrag(100, Waehrung.EUR);
        Geldbetrag zeroBetrag = new Geldbetrag(0, Waehrung.EUR);
        Geldbetrag result = betrag.plus(zeroBetrag);
        assertEquals(100.00, result.getBetrag(), "Das Hinzufügen eines Summanden mit 0 sollte den Betrag nicht ändern.");
        assertEquals(Waehrung.EUR, result.getWaehrung(), "Die Währung sollte weiterhin EUR sein.");
    }
}
