package bankprojekt.verwaltung;

import bankprojekt.verarbeitung.Geldbetrag;
import bankprojekt.verarbeitung.Girokonto;
import bankprojekt.verarbeitung.Konto;
import bankprojekt.verarbeitung.Kunde;

public class GirokontoFabrik extends Kontofabrik {
    @Override
    public Konto erzeugeKonto(long kontonummer, Kunde kunde) {
        return new Girokonto(kunde, kontonummer, new Geldbetrag());
    }
}