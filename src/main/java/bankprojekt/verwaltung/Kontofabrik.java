package bankprojekt.verwaltung;

import bankprojekt.verarbeitung.Konto;
import bankprojekt.verarbeitung.Kunde;

public abstract class Kontofabrik {
    public abstract Konto erzeugeKonto(long kontonummer, Kunde kunde);
}
