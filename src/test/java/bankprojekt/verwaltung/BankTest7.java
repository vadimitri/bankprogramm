import bankprojekt.verarbeitung.Geldbetrag;
import bankprojekt.verarbeitung.Konto;
import bankprojekt.verarbeitung.Kunde;
import bankprojekt.verwaltung.Bank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class BankTest7 {
    private Bank bank;
    private static final long VALID_BLZ = 12345678;

    @BeforeEach
    void setup() {
        bank = new Bank(VALID_BLZ);
    }

    // Tests für schenkungAnNeuerwachsene
    @Test
    void testSchenkungAnNeuerwachseneNullBetrag() {
        assertThrows(NullPointerException.class,
                () -> bank.schenkungAnNeuerwachsene(null));
    }

    @Test
    void testSchenkungAnNeuerwachseneWird18() {
        LocalDate heute = LocalDate.now();
        LocalDate geburtstag = heute.minusYears(18);

        Kunde kunde = mock(Kunde.class);
        when(kunde.getGeburtstag()).thenReturn(geburtstag);

        Konto konto = mock(Konto.class);
        when(konto.getInhaber()).thenReturn(kunde);

        bank.mockEinfuegen(konto);

        Geldbetrag geschenk = new Geldbetrag(100);

        bank.schenkungAnNeuerwachsene(geschenk);

        verify(konto).einzahlen(geschenk);
    }

    @Test
    void testSchenkungAnNeuerwachseneNicht18() {
        LocalDate heute = LocalDate.now();
        LocalDate geburtstag = heute.minusYears(19);

        Kunde kunde = mock(Kunde.class);
        when(kunde.getGeburtstag()).thenReturn(geburtstag);

        Konto konto = mock(Konto.class);
        when(konto.getInhaber()).thenReturn(kunde);

        bank.mockEinfuegen(konto);

        Geldbetrag geschenk = new Geldbetrag(100);

        bank.schenkungAnNeuerwachsene(geschenk);

        verify(konto, never()).einzahlen(any());
    }

    // Tests für getKundenMitLeeremKonto
    @Test
    void testGetKundenMitLeeremKontoLeereBankGibtLeereListeZurueck() {
        List<Kunde> result = bank.getKundenMitLeeremKonto();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetKundenMitLeeremKontoEinNegativesKonto() {
        Kunde kunde = mock(Kunde.class);
        Konto konto = mock(Konto.class);
        Geldbetrag negativerBetrag = mock(Geldbetrag.class);

        when(konto.getInhaber()).thenReturn(kunde);
        when(konto.getKontostand()).thenReturn(negativerBetrag);
        when(negativerBetrag.isNegativ()).thenReturn(true);

        bank.mockEinfuegen(konto);

        List<Kunde> result = bank.getKundenMitLeeremKonto();

        assertEquals(1, result.size());
        assertTrue(result.contains(kunde));
    }

    @Test
    void testGetKundenMitLeeremKontoMehrereKontenEinKunde() {
        Kunde kunde = mock(Kunde.class);

        Konto konto1 = mock(Konto.class);
        Konto konto2 = mock(Konto.class);

        Geldbetrag negativerBetrag = mock(Geldbetrag.class);
        Geldbetrag positiverBetrag = mock(Geldbetrag.class);

        when(konto1.getInhaber()).thenReturn(kunde);
        when(konto2.getInhaber()).thenReturn(kunde);
        when(konto1.getKontostand()).thenReturn(negativerBetrag);
        when(konto2.getKontostand()).thenReturn(positiverBetrag);
        when(negativerBetrag.isNegativ()).thenReturn(true);
        when(positiverBetrag.isNegativ()).thenReturn(false);

        bank.mockEinfuegen(konto1);
        bank.mockEinfuegen(konto2);

        List<Kunde> result = bank.getKundenMitLeeremKonto();

        assertEquals(1, result.size());
        assertTrue(result.contains(kunde));
    }

    // Tests für getAnzahlSenioren
    @Test
    void testGetAnzahlSeniorenLeereBankGibtNullZurueck() {
        assertEquals(0, bank.getAnzahlSenioren());
    }

    @Test
    void testGetAnzahlSeniorenEinSenior() {
        LocalDate heute = LocalDate.now();
        LocalDate geburtstag = heute.minusYears(68);

        Kunde kunde = mock(Kunde.class);
        when(kunde.getGeburtstag()).thenReturn(geburtstag);

        Konto konto = mock(Konto.class);
        when(konto.getInhaber()).thenReturn(kunde);

        bank.mockEinfuegen(konto);

        assertEquals(1, bank.getAnzahlSenioren());
    }

    @Test
    void testGetAnzahlSeniorenGrenzfall67Jahre() {
        LocalDate heute = LocalDate.now();
        LocalDate geburtstag = heute.minusYears(67);

        Kunde kunde = mock(Kunde.class);
        when(kunde.getGeburtstag()).thenReturn(geburtstag);

        Konto konto = mock(Konto.class);
        when(konto.getInhaber()).thenReturn(kunde);

        bank.mockEinfuegen(konto);

        assertEquals(1, bank.getAnzahlSenioren());
    }

    @Test
    void testGetAnzahlSeniorenMehrereKontenEinSenior() {
        LocalDate heute = LocalDate.now();
        LocalDate geburtstag = heute.minusYears(68);

        Kunde kunde = mock(Kunde.class);
        when(kunde.getGeburtstag()).thenReturn(geburtstag);

        Konto konto1 = mock(Konto.class);
        Konto konto2 = mock(Konto.class);
        when(konto1.getInhaber()).thenReturn(kunde);
        when(konto2.getInhaber()).thenReturn(kunde);

        bank.mockEinfuegen(konto1);
        bank.mockEinfuegen(konto2);

        assertEquals(1, bank.getAnzahlSenioren());
    }
}