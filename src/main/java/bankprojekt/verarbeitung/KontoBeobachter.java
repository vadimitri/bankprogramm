package bankprojekt.verarbeitung;

public interface KontoBeobachter {
    /**
     * Wenn sich Kontostand ändert
     * @param konto Das Konto, dessen Kontostand sich geändert hat
     * @param alterKontostand Vorheriger Kontostand
     * @param neuerKontostand Neuer Kontostand
     */
    void kontostandGeaendert(Konto konto, Geldbetrag alterKontostand, Geldbetrag neuerKontostand);

    /**
     * Wenn sich Dispo bei Girokonto ändert
     * @param konto Das Girokonto, dessen Dispo sich geändert hat
     * @param alterDispo alter Dispo
     * @param neuerDispo neuer Dispo
     */
    void dispoGeaendert(Girokonto konto, Geldbetrag alterDispo, Geldbetrag neuerDispo);
}
