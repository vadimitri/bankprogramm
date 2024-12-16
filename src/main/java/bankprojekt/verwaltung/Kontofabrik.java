package bankprojekt.verwaltung;

import bankprojekt.verarbeitung.Konto;
import bankprojekt.verarbeitung.Kunde;

public abstract class Kontofabrik {
    protected abstract Konto erzeugeKonto(Kunde inhaber, double startGeld);
}
