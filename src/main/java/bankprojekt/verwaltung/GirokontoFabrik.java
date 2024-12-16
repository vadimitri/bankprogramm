package bankprojekt.verwaltung;

import bankprojekt.verarbeitung.Girokonto;
import bankprojekt.verarbeitung.Konto;

public class GirokontoFabrik extends Kontofabrik {
    @Override
    public Konto erzeugeKonto(String inhaber, double anfangsbetrag) {
        return new Girokonto(inhaber, anfangsbetrag);
    }
}