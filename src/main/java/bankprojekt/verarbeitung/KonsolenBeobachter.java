package bankprojekt.verarbeitung;

public class KonsolenBeobachter implements KontoBeobachter {
    @Override
    public void kontostandGeaendert(Konto konto, Geldbetrag alterKontostand, Geldbetrag neuerKontostand) {
        System.out.printf("Kontostand geändert für Konto %d:%n", konto.getKontonummer());
        System.out.printf("Alter Kontostand: %s%n", alterKontostand);
        System.out.printf("Neuer Kontostand: %s%n", neuerKontostand);
        System.out.println("------------------------");
    }

    @Override
    public void dispoGeaendert(Girokonto konto, Geldbetrag alterDispo, Geldbetrag neuerDispo) {
        System.out.printf("Dispo geändert für Konto %d:%n", konto.getKontonummer());
        System.out.printf("Alter Dispo: %s%n", alterDispo);
        System.out.printf("Neuer Dispo: %s%n", neuerDispo);
        System.out.println("------------------------");
    }
}
