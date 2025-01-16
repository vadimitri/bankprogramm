package bankprojekt.verwaltung;

import bankprojekt.verarbeitung.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class BankTests {

    private Bank bank;
    private Kontofabrik mockFactory;

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
        mockFactory = mock(Kontofabrik.class);

        when(mockKundeA.getName()).thenReturn("Kunde A");
        when(mockKundeB.getName()).thenReturn("Kunde B");
        when(mockKontoVon.getInhaber()).thenReturn(mockKundeA);
        when(mockKontoNach.getInhaber()).thenReturn(mockKundeB);
    }

    @Test
    void testKontoLoeschenUndKontoExistiert() {
        when(mockFactory.erzeugeKonto(anyLong(), any(Kunde.class))).thenReturn(mockKonto);
        long kontoNr = bank.kontoErstellen(mockFactory, mockKundeA);

        boolean result = bank.kontoLoeschen(kontoNr);

        assertTrue(result);
        assertFalse(bank.getAlleKontonummern().contains(kontoNr));
    }

    @Test
    void testKontoLoeschenUndKontoExistiertNicht() {
        boolean result = bank.kontoLoeschen(5L);
        assertFalse(result);
    }

    @Test
    void testAllesSuper() throws GesperrtException {
        Kontofabrik factoryVon = mock(Kontofabrik.class);
        Kontofabrik factoryNach = mock(Kontofabrik.class);
        when(factoryVon.erzeugeKonto(anyLong(), any(Kunde.class))).thenReturn(mockKontoVon);
        when(factoryNach.erzeugeKonto(anyLong(), any(Kunde.class))).thenReturn(mockKontoNach);

        when(mockKundeA.getName()).thenReturn("Kunde A");
        when(mockKundeB.getName()).thenReturn("Kunde B");
        when(mockKontoVon.getInhaber()).thenReturn(mockKundeA);
        when(mockKontoNach.getInhaber()).thenReturn(mockKundeB);

        long kontoNrVon = bank.kontoErstellen(factoryVon, mockKundeA);
        long kontoNrNach = bank.kontoErstellen(factoryNach, mockKundeB);

        Geldbetrag betrag = new Geldbetrag(100.0);
        String verwendungszweck = "Test";

        when(mockKontoVon.getKontostand()).thenReturn(betrag);
        when(mockKontoVon.ueberweisungAbsenden(any(), anyString(), anyLong(), anyLong(), anyString())).thenReturn(true);

        assertTrue(bank.geldUeberweisen(kontoNrVon, kontoNrNach, betrag, verwendungszweck));
        verify(mockKontoVon).ueberweisungAbsenden(any(), anyString(), anyLong(), anyLong(), anyString());
        verify(mockKontoNach).ueberweisungEmpfangen(any(), anyString(), anyLong(), anyLong(), anyString());
    }

    @Test
    void testKontoVonGesperrt() throws GesperrtException {
        Kontofabrik factoryVon = mock(Kontofabrik.class);
        Kontofabrik factoryNach = mock(Kontofabrik.class);
        when(factoryVon.erzeugeKonto(anyLong(), any(Kunde.class))).thenReturn(mockKontoVon);
        when(factoryNach.erzeugeKonto(anyLong(), any(Kunde.class))).thenReturn(mockKontoNach);

        long kontoNrVon = bank.kontoErstellen(factoryVon, mockKundeA);
        long kontoNrNach = bank.kontoErstellen(factoryNach, mockKundeB);

        Geldbetrag betrag = new Geldbetrag(100.0);
        String verwendungszweck = "Test";

        when(mockKontoVon.getKontostand()).thenReturn(betrag);
        when(mockKontoVon.ueberweisungAbsenden(any(), anyString(), anyLong(), anyLong(), anyString()))
                .thenThrow(new GesperrtException(kontoNrVon));

        assertThrows(GesperrtException.class, () ->
                bank.geldUeberweisen(kontoNrVon, kontoNrNach, betrag, verwendungszweck));

        verify(mockKontoNach, never()).ueberweisungEmpfangen(any(), anyString(), anyLong(), anyLong(), anyString());
        verify(mockKontoVon).ueberweisungAbsenden(any(), anyString(), anyLong(), anyLong(), anyString());
    }

    @Test
    void testKontoVonNull() throws GesperrtException {
        Kontofabrik factory = mock(Kontofabrik.class);
        when(factory.erzeugeKonto(anyLong(), any(Kunde.class))).thenReturn(mockKontoNach);

        long kontoNrNach = bank.kontoErstellen(factory, mockKundeB);
        long nichtExistierendeKontoNr = 99999;

        Geldbetrag betrag = new Geldbetrag(100.0);
        String verwendungszweck = "Test";

        assertFalse(bank.geldUeberweisen(nichtExistierendeKontoNr, kontoNrNach, betrag, verwendungszweck));
    }

    @Test
    void testKontoNachNull() throws GesperrtException {
        Kontofabrik factory = mock(Kontofabrik.class);
        when(factory.erzeugeKonto(anyLong(), any(Kunde.class))).thenReturn(mockKontoVon);

        long kontoNrVon = bank.kontoErstellen(factory, mockKundeA);
        long nichtExistierendeKontoNr = 99999;

        Geldbetrag betrag = new Geldbetrag(100.0);
        String verwendungszweck = "Test";

        assertFalse(bank.geldUeberweisen(kontoNrVon, nichtExistierendeKontoNr, betrag, verwendungszweck));
    }

    @Test
    void testVerwendungszweckNull() {
        Kontofabrik factoryVon = mock(Kontofabrik.class);
        Kontofabrik factoryNach = mock(Kontofabrik.class);
        when(factoryVon.erzeugeKonto(anyLong(), any(Kunde.class))).thenReturn(mockKontoVon);
        when(factoryNach.erzeugeKonto(anyLong(), any(Kunde.class))).thenReturn(mockKontoNach);

        long kontoNrVon = bank.kontoErstellen(factoryVon, mockKundeA);
        long kontoNrNach = bank.kontoErstellen(factoryNach, mockKundeB);

        Geldbetrag betrag = new Geldbetrag(100.0);

        assertThrows(IllegalArgumentException.class, () ->
                bank.geldUeberweisen(kontoNrVon, kontoNrNach, betrag, null));
    }

    @Test
    void testBetragNull() {
        Kontofabrik factoryVon = mock(Kontofabrik.class);
        Kontofabrik factoryNach = mock(Kontofabrik.class);
        when(factoryVon.erzeugeKonto(anyLong(), any(Kunde.class))).thenReturn(mockKontoVon);
        when(factoryNach.erzeugeKonto(anyLong(), any(Kunde.class))).thenReturn(mockKontoNach);

        long kontoNrVon = bank.kontoErstellen(factoryVon, mockKundeA);
        long kontoNrNach = bank.kontoErstellen(factoryNach, mockKundeB);

        String verwendungszweck = "Test";

        assertThrows(IllegalArgumentException.class, () ->
                bank.geldUeberweisen(kontoNrVon, kontoNrNach, null, verwendungszweck));
    }

    @Test
    void testKontoVonNichtUeberweisungsfaehigesKonto() throws GesperrtException {
        Kontofabrik factoryVon = mock(Kontofabrik.class);
        Kontofabrik factoryNach = mock(Kontofabrik.class);
        when(factoryVon.erzeugeKonto(anyLong(), any(Kunde.class))).thenReturn(mockKonto);
        when(factoryNach.erzeugeKonto(anyLong(), any(Kunde.class))).thenReturn(mockKontoNach);

        long kontoNrVon = bank.kontoErstellen(factoryVon, mockKundeA);
        long kontoNrNach = bank.kontoErstellen(factoryNach, mockKundeB);

        Geldbetrag betrag = new Geldbetrag(100.0);
        String verwendungszweck = "Test";

        assertFalse(bank.geldUeberweisen(kontoNrVon, kontoNrNach, betrag, verwendungszweck));
    }

    @Test
    void testZuArmFuerUeberweisung() throws GesperrtException {
        Kontofabrik factoryVon = mock(Kontofabrik.class);
        Kontofabrik factoryNach = mock(Kontofabrik.class);
        when(factoryVon.erzeugeKonto(anyLong(), any(Kunde.class))).thenReturn(mockKontoVon);
        when(factoryNach.erzeugeKonto(anyLong(), any(Kunde.class))).thenReturn(mockKontoNach);

        long kontoNrVon = bank.kontoErstellen(factoryVon, mockKundeA);
        long kontoNrNach = bank.kontoErstellen(factoryNach, mockKundeB);

        Geldbetrag betrag = new Geldbetrag(100.0);
        String verwendungszweck = "Test";

        when(mockKontoVon.getKontostand()).thenReturn(new Geldbetrag(50.0));
        assertFalse(bank.geldUeberweisen(kontoNrVon, kontoNrNach, betrag, verwendungszweck));
    }
}