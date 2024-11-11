package bankprojekt.verwaltung;

import bankprojekt.geld.Waehrung;
import bankprojekt.verarbeitung.Geldbetrag;
import bankprojekt.verarbeitung.GesperrtException;
import bankprojekt.verarbeitung.Kunde;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BankTest {


    Kunde anna = new Kunde("Anna", "Müller", "hier", LocalDate.parse("1979-05-14"));
    Kunde berta = new Kunde("Berta", "Beerenbaum", "hier", LocalDate.parse("1980-03-15"));
    Kunde chris = new Kunde("Chris", "Tall", "hier", LocalDate.parse("1979-01-07"));
    Kunde anton = new Kunde("Anton", "Meier", "hier", LocalDate.parse("1982-10-23"));
    Kunde bert = new Kunde("Bert", "Chokowski", "hier", LocalDate.parse("1970-12-24"));
    Kunde doro = new Kunde("Doro", "Hubrich", "hier", LocalDate.parse("1976-07-13"));
    Bank dkb = new Bank(12030000L);


    @Test
    void girokontoErstellen() {
        Assertions.assertEquals(dkb.getBankleitzahl(), 12030000L);
        assertEquals(dkb.girokontoErstellen(anna), 0L);
        assertEquals(dkb.girokontoErstellen(berta), 1L);
    }

    @Test
    void sparbuchErstellen() {
        assertEquals(dkb.getBankleitzahl(), 12030000L);
        assertEquals(dkb.sparbuchErstellen(berta), 0L);
        assertEquals(dkb.sparbuchErstellen(chris), 1L);
    }

    @Test
    void getAlleKonten() {
        assertEquals(dkb.girokontoErstellen(anna), 0L);
        assertEquals(dkb.girokontoErstellen(berta), 1L);
        assertEquals(dkb.sparbuchErstellen(berta), 2L);
        assertEquals(dkb.sparbuchErstellen(chris), 3L);
        System.out.print(dkb.getAlleKonten());
    }

    @Test
    void getAlleKontonummern() {
        assertEquals(dkb.getAlleKontonummern().size(), 0);
        assertEquals(dkb.girokontoErstellen(anna), 0L);
        assertEquals(dkb.girokontoErstellen(berta), 1L);
        assertEquals(dkb.sparbuchErstellen(berta), 2L);
        assertEquals(dkb.sparbuchErstellen(chris), 3L);
        assertEquals(dkb.getAlleKontonummern().size(), 4);
    }

    @Test
    void geldAbheben() throws GesperrtException {
        assertEquals(dkb.girokontoErstellen(anna), 0L);
        assertEquals(dkb.girokontoErstellen(berta), 1L);
        assertEquals(dkb.sparbuchErstellen(berta), 2L);
        assertEquals(dkb.sparbuchErstellen(chris), 3L);
        assertEquals(dkb.geldAbheben(0L, new Geldbetrag(99, Waehrung.EUR)), true);
        assertFalse(dkb.geldAbheben(4L, new Geldbetrag(99, Waehrung.EUR)));
        assertFalse(dkb.geldAbheben(2L, new Geldbetrag(99, Waehrung.EUR)));

    }

    @Test
    void kontoLoeschen() {
        assertEquals(dkb.girokontoErstellen(anna), 0L);
        assertEquals(dkb.girokontoErstellen(berta), 1L);
        assertEquals(dkb.sparbuchErstellen(berta), 2L);
        assertEquals(dkb.sparbuchErstellen(chris), 3L);
        assertTrue(dkb.kontoLoeschen(2L));
        assertFalse(dkb.kontoLoeschen(5L));
    }


    @Test
    void geldUeberweisen() throws GesperrtException {
        assertEquals(dkb.girokontoErstellen(anna), 0L);
        assertEquals(dkb.girokontoErstellen(berta), 1L);
        assertEquals(dkb.girokontoErstellen(berta), 2L);
        assertEquals(dkb.girokontoErstellen(chris), 3L);
        dkb.geldEinzahlen(0L, new Geldbetrag(99, Waehrung.EUR));
        dkb.geldEinzahlen(3L, new Geldbetrag(1, Waehrung.EUR));
        assertTrue(dkb.geldUeberweisen(0L, 3L, new Geldbetrag(99), "Geschenk"));
        assertEquals(new Geldbetrag(100, Waehrung.EUR), dkb.getKontostand(3L));
        assertEquals(new Geldbetrag(0, Waehrung.EUR), dkb.getKontostand(0L));
        assertFalse(dkb.geldUeberweisen(0L, 3L, new Geldbetrag(99), "Geschenk"));
    }


}