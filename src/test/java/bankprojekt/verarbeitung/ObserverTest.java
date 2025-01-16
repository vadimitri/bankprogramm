package bankprojekt.verarbeitung;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;

public class ObserverTest {
    private Girokonto girokonto;
    private KontoBeobachter mockBeobachter;
    private Kunde testKunde;

    @BeforeEach
    public void setUp() {
        testKunde = new Kunde("Max", "Mustermann", "Teststraße 1", LocalDate.now());
        girokonto = new Girokonto(testKunde, 12345, new Geldbetrag(1000));
        mockBeobachter = mock(KontoBeobachter.class);
        girokonto.addBeobachter(mockBeobachter);
    }

    @Test
    public void testKontostandAenderungBenachrichtigung() {
        Geldbetrag einzahlung = new Geldbetrag(500);
        girokonto.einzahlen(einzahlung);

        verify(mockBeobachter).kontostandGeaendert(
                eq(girokonto),
                any(Geldbetrag.class),
                any(Geldbetrag.class)
        );
    }

    @Test
    public void testDispoAenderungBenachrichtigung() {
        Geldbetrag neuerDispo = new Geldbetrag(2000);
        girokonto.setDispo(neuerDispo);

        verify(mockBeobachter).dispoGeaendert(
                eq(girokonto), any(Geldbetrag.class),
                eq(neuerDispo)
        );
    }

    @Test
    public void testBeobachterEntfernen() {
        girokonto.removeBeobachter(mockBeobachter);
        girokonto.einzahlen(new Geldbetrag(500));

        verify(mockBeobachter, never()).kontostandGeaendert( any(Konto.class),
                any(Geldbetrag.class),
                any(Geldbetrag.class)
        );
    }
}