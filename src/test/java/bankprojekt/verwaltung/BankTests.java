package bankprojekt.verwaltung;

import bankprojekt.verarbeitung.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class BankTests {

    private Bank bank;

    @Mock
    private UeberweisungsfaehigesKonto mockKontoVon;

    @Mock
    private UeberweisungsfaehigesKonto mockKontoNach;

    @Mock
    private Konto mockKonto;

    @Mock
    private Kunde mockKundeA;

    @Mock
    private Kunde mockKundeB;

    private final long BANKLEITZAHL = 11111111;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bank = new Bank(BANKLEITZAHL);

        when(mockKundeA.getName()).thenReturn("Olaf Scholz");
        when(mockKundeB.getName()).thenReturn("Christian Lindner");


        when(mockKontoVon.getInhaber()).thenReturn(mockKundeA);
        when(mockKontoVon.getKontonummer()).thenReturn(1L);
        when(mockKontoNach.getInhaber()).thenReturn(mockKundeB);
    }

    @Test
    void testKontoLoeschenUndKontoExistiert() {
        long kontoNr = bank.mockEinfuegen(mockKonto);

        boolean result = bank.kontoLoeschen(kontoNr);

        assertTrue(result);
        assertFalse(bank.getAlleKontonummern().contains(1L));
    }

    @Test
    void testKontoLoeschenUndKontoExistiertNicht() {
        boolean result = bank.kontoLoeschen(5L);

        assertFalse(result);
    }

    @Test
    void testAllesSuper() throws GesperrtException {
        long kontoNrVon = bank.mockEinfuegen(mockKontoVon);
        long kontoNrNach = bank.mockEinfuegen(mockKontoNach);
        Geldbetrag betrag = new Geldbetrag(100.0);
        String string = "Test";

        when(mockKontoVon.getKontostand()).thenReturn(betrag);
        when(mockKontoVon.ueberweisungAbsenden(any(),anyString(),anyLong(),anyLong(),anyString())).thenReturn(true);

        assertTrue(bank.geldUeberweisen(kontoNrVon, kontoNrNach, betrag, string));
        verify(mockKontoVon).ueberweisungAbsenden(any(),anyString(),anyLong(),anyLong(),anyString());
        verify(mockKontoNach).ueberweisungEmpfangen(any(),anyString(),anyLong(),anyLong(),anyString());
    }

    @Test
    void testKontoVonGesperrt() throws GesperrtException {
        long kontoNrVon = bank.mockEinfuegen(mockKontoVon);
        long kontoNrNach = bank.mockEinfuegen(mockKontoNach);
        Geldbetrag betrag = new Geldbetrag(100.0);
        String string = "Test";

        when(mockKontoVon.getKontostand()).thenReturn(betrag);
        when(mockKontoVon.ueberweisungAbsenden(any(), anyString(), anyLong(), anyLong(),anyString())).thenThrow(new GesperrtException(kontoNrVon));

        assertThrows(GesperrtException.class, () -> bank.geldUeberweisen(kontoNrVon, kontoNrNach, betrag, string));
        verify(mockKontoNach, never()).ueberweisungEmpfangen(any(), anyString(), anyLong(), anyLong(), anyString());

        Mockito.verify(mockKontoVon).ueberweisungAbsenden(any(), anyString(), anyLong(), anyLong(), anyString());

    }

    // Warum kann ein Gesperrtes Konto Beträge erhalten?
    @Test
    void testKontoNachGesperrt() throws GesperrtException {
        long kontoNrVon = bank.mockEinfuegen(mockKontoVon);
        long kontoNrNach = bank.mockEinfuegen(mockKontoNach);
        Geldbetrag betrag = new Geldbetrag(100.0);

        when(mockKontoVon.getKontostand()).thenReturn(betrag);
        //doThrow(new GesperrtException(kontoNrNach)).when(mockKontoNach).ueberweisungEmpfangen(any(), anyString(), anyLong(), anyLong(),anyString());

       // assertThrows(GesperrtException.class, () -> bank.geldUeberweisen(kontoNrVon, kontoNrNach, betrag, "Gesperrt"));
        verify(mockKontoNach, never()).ueberweisungEmpfangen(any(), anyString(), anyLong(), anyLong(), anyString());
    }


    @Test
    void testKontoVonNull() throws GesperrtException {
        long nichtExistierendeKontoNr = 99999;
        long kontoNrNach = bank.mockEinfuegen(mockKontoVon);
        Geldbetrag betrag = new Geldbetrag(100.0);
        String string = "Test";


        assertFalse(bank.geldUeberweisen(nichtExistierendeKontoNr, kontoNrNach, betrag, string));
    }

    @Test
    void testKontoNachNull() throws GesperrtException {
        long nichtExistierendeKontoNr = 99999;
        long kontoNrVon = bank.mockEinfuegen(mockKontoNach);
        Geldbetrag betrag = new Geldbetrag(100.0);

        assertFalse(bank.geldUeberweisen(kontoNrVon, nichtExistierendeKontoNr, betrag, "Test"));
    }

    @Test
    void testVerwendungszweckNull() {
        long kontoNrVon = bank.mockEinfuegen(mockKontoVon);
        long kontoNrNach = bank.mockEinfuegen(mockKontoNach);
        Geldbetrag betrag = new Geldbetrag(100.0);
        String string = null;

        assertThrows(IllegalArgumentException.class, () ->
                bank.geldUeberweisen(kontoNrVon, kontoNrNach, betrag, string));
    }

    @Test
    void testBetragNull() {
        long kontoNrVon = bank.mockEinfuegen(mockKontoVon);
        long kontoNrNach = bank.mockEinfuegen(mockKontoNach);
        Geldbetrag betrag = null;
        String string = "Test";

        assertThrows(IllegalArgumentException.class, () ->
                bank.geldUeberweisen(kontoNrVon, kontoNrNach, betrag, string));
    }

    @Test
    void testKontoVonNichtUeberweisungsfaehigesKonto() throws GesperrtException {
        long kontoNrVon = bank.mockEinfuegen(mockKonto);
        long kontoNrNach = bank.mockEinfuegen(mockKontoNach);
        Geldbetrag betrag = new Geldbetrag(100.0);
        String string = "Test";

        assertFalse(bank.geldUeberweisen(kontoNrVon, kontoNrNach, betrag, string));

    }

    @Test
    void testKontoNachNichtUeberweisungsfaehigesKonto() throws GesperrtException {
        long kontoNrVon = bank.mockEinfuegen(mockKontoVon);
        long kontoNrNach = bank.mockEinfuegen(mockKonto);
        Geldbetrag betrag = new Geldbetrag(100.0);
        String string = "Test";

        assertFalse(bank.geldUeberweisen(kontoNrVon, kontoNrNach, betrag, string));
    }


    @Test
    void testZuArmFuerUeberweisung() throws GesperrtException {
        long kontoNrVon = bank.mockEinfuegen(mockKontoVon);
        long kontoNrNach = bank.mockEinfuegen(mockKontoNach);
        Geldbetrag betrag = new Geldbetrag(100.0);
        String string = "Test";

        when(mockKontoVon.getKontostand()).thenReturn(new Geldbetrag(50.0));
        assertFalse(bank.geldUeberweisen(kontoNrVon, kontoNrNach, betrag, string));

    }

}

