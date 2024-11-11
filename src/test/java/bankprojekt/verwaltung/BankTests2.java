package bankprojekt.verwaltung;

import bankprojekt.verarbeitung.*;
        import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
        import static org.junit.jupiter.api.Assertions.*;

public class BankTests2 {
    private Bank bank;
    @Mock
    private UeberweisungsfaehigesKonto mockGirokontoVon;
    @Mock
    private UeberweisungsfaehigesKonto mockGirokontoNach;
    @Mock
    private Konto mockKonto;
    @Mock
    private Kunde mockKunde;

    private final long BANK_BLZ = 12345678;

    @BeforeEach
    void setUp() {
        // Initialisiere die Mocks
        MockitoAnnotations.openMocks(this);
        bank = new Bank(BANK_BLZ);

        // Setup für mockKunde
        when(mockKunde.getName()).thenReturn("Max Mustermann");

        // Setup für die Konten
        when(mockGirokontoVon.getInhaber()).thenReturn(mockKunde);
        when(mockGirokontoNach.getInhaber()).thenReturn(mockKunde);
    }

    @Test
    void testKontoLoeschen_ExistingAccount() {
        // Arrange
        long kontoNr = bank.mockEinfuegen(mockKonto);

        // Act
        boolean result = bank.kontoLoeschen(kontoNr);

        // Assert
        assertTrue(result, "Kontolöschung sollte erfolgreich sein");
    }

    @Test
    void testKontoLoeschen_NonExistingAccount() {
        // Act
        boolean result = bank.kontoLoeschen(999999);

        // Assert
        assertFalse(result, "Kontolöschung sollte fehlschlagen bei nicht existierendem Konto");
    }

    @Test
    void testGeldUeberweisen_Successful() throws GesperrtException {
        // Arrange
        long vonKontoNr = bank.mockEinfuegen(mockGirokontoVon);
        long nachKontoNr = bank.mockEinfuegen(mockGirokontoNach);
        Geldbetrag betrag = new Geldbetrag(100.0);

        when(mockGirokontoVon.getKontostand()).thenReturn(betrag);
        when(mockGirokontoVon.ueberweisungAbsenden(any(), anyString(), anyLong(), anyLong(), anyString()))
                .thenReturn(true);

        // Act
        boolean result = bank.geldUeberweisen(vonKontoNr, nachKontoNr, betrag, "Test");
        String mockName = mockKunde.getName();

        // Assert
        assertTrue(result, "Überweisung sollte erfolgreich sein");
        verify(mockGirokontoVon).ueberweisungAbsenden(eq(betrag), eq(mockName),
                eq(nachKontoNr), eq(BANK_BLZ), eq("Test"));
        verify(mockGirokontoNach).ueberweisungEmpfangen(eq(betrag), eq(mockKunde.getName()),
                eq(vonKontoNr), eq(BANK_BLZ), eq("Test"));
    }

    @Test
    void testGeldUeberweisen_InsufficientFunds() throws GesperrtException {
        // Arrange
        long vonKontoNr = bank.mockEinfuegen(mockGirokontoVon);
        long nachKontoNr = bank.mockEinfuegen(mockGirokontoNach);
        Geldbetrag betrag = new Geldbetrag(100.0);

        when(mockGirokontoVon.getKontostand()).thenReturn(new Geldbetrag(50.0));

        // Act
        boolean result = bank.geldUeberweisen(vonKontoNr, nachKontoNr, betrag, "Test");

        // Assert
        assertFalse(result, "Überweisung sollte bei unzureichendem Kontostand fehlschlagen");
        verify(mockGirokontoVon, never()).ueberweisungAbsenden(any(), anyString(), anyLong(), anyLong(), anyString());
        verify(mockGirokontoNach, never()).ueberweisungEmpfangen(any(), anyString(), anyLong(), anyLong(), anyString());
    }

    @Test
    void testGeldUeberweisen_AccountLocked() throws GesperrtException {
        // Arrange
        long vonKontoNr = bank.mockEinfuegen(mockGirokontoVon);
        long nachKontoNr = bank.mockEinfuegen(mockGirokontoNach);
        Geldbetrag betrag = new Geldbetrag(100.0);

        when(mockGirokontoVon.getKontostand()).thenReturn(betrag);
        when(mockGirokontoVon.ueberweisungAbsenden(any(), anyString(), anyLong(), anyLong(), anyString()))
                .thenThrow(new GesperrtException(vonKontoNr));

        // Act & Assert
        assertThrows(GesperrtException.class, () ->
                bank.geldUeberweisen(vonKontoNr, nachKontoNr, betrag, "Test"));
        verify(mockGirokontoNach, never()).ueberweisungEmpfangen(any(), anyString(), anyLong(), anyLong(), anyString());
    }

    @Test
    void testGeldUeberweisen_InvalidParameters() {
        // Arrange
        long vonKontoNr = bank.mockEinfuegen(mockGirokontoVon);
        long nachKontoNr = bank.mockEinfuegen(mockGirokontoNach);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                bank.geldUeberweisen(vonKontoNr, nachKontoNr, null, "Test"));
        assertThrows(IllegalArgumentException.class, () ->
                bank.geldUeberweisen(vonKontoNr, nachKontoNr, new Geldbetrag(100.0), null));
        assertThrows(IllegalArgumentException.class, () ->
                bank.geldUeberweisen(-1, nachKontoNr, new Geldbetrag(100.0), "Test"));
    }
}
