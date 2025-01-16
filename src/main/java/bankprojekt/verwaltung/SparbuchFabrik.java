package bankprojekt.verwaltung;

import bankprojekt.verarbeitung.Konto;
import bankprojekt.verarbeitung.Kunde;
import bankprojekt.verarbeitung.Sparbuch;

public class SparbuchFabrik extends Kontofabrik {
        @Override
        public Konto erzeugeKonto(long kontonummer, Kunde kunde) {
            return new Sparbuch(kunde, kontonummer);
        }
}
