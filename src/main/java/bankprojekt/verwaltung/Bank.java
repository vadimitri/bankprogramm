package bankprojekt.verwaltung;

import bankprojekt.verarbeitung.*;

import java.util.*;


/**
 * Bankverwaltungsklasse für das Erstellen von Konten und manipulieren dieser
 */
public class Bank {

    /**
     * Sortierte Map an Kontos
     */
   private TreeMap<Long, Konto> kontos = new TreeMap<>();
    /**
     * Bankleitzahl einer Bank
     */
   private final long bankleitzahl;


    /**
     * Konstruktor, welcher eine Bank erstellt
     * @param bankleitzahl Weitergeleitete Bankleitzahl
     */
    public Bank(long bankleitzahl) {
        // Recherche ergab, dass Bankleitzahlen immer 8 Ziffern lang sind und nie mit 0 anfangen
        if (bankleitzahl < 10000000 || bankleitzahl > 99999999) {
            throw new IllegalArgumentException("Invalide Bankleitzahl");
        }
        this.bankleitzahl = bankleitzahl;
    }

    /**
     * Gibt Bankleitzahl zurück
     * @return Bankleitzahl
     */
    public long getBankleitzahl() {
        return bankleitzahl;
    }


    /**
     * Erstellt ein Girokonto
     * @param inhaber Kunde, der Inhaber des Kontos sein wird
     * @return Kontonummmer des Girokontos
     */
    public long girokontoErstellen(Kunde inhaber) {
        // speichert den Eintrag, der die hoechste Nummer in der Map hat oder null wenn die Map leer ist
        Map.Entry<Long, Konto> hoechsterEntry = kontos.floorEntry(100000000L);
        if (hoechsterEntry == null) {
            kontos.put(0L, new Girokonto(inhaber, 0L, new Geldbetrag(100)));
            return 0L;
        }
        // Neue Girokontonummer wird um 1 größer als der Größte Wert gespeichert
        long neueNummer = hoechsterEntry.getKey() + 1L;
        kontos.put(neueNummer, new Girokonto(inhaber, neueNummer, new Geldbetrag(100)));
        return neueNummer;
    }

    /**
     * Erstellt ein Sparbuch
     * @param inhaber Kunde, der Inhaber des Sparbuchs sein wird
     * @return Kontonummer des Sparbuchs
     */
    public long sparbuchErstellen(Kunde inhaber) {
        // speichert den Eintrag, der die hoechste Nummer in der Map hat oder null wenn die Map leer ist
        Map.Entry<Long, Konto> hoechsterEntry = kontos.floorEntry(100000000L);
        if (hoechsterEntry == null) {
            kontos.put(0L, new Sparbuch(inhaber, 0L));
            return 0L;
        }
        // Neue Girokontonummer wird um 1 größer als der Größte Wert gespeichert
        long neueNummer = hoechsterEntry.getKey() + 1L;
        kontos.put(neueNummer, new Sparbuch(inhaber, neueNummer));
        return neueNummer;
    }

    /**
     * Liefert einen String mit allen Konten zurück
     * @return String mit allen Konten und deren Kontostand
     */
    public String getAlleKonten() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Long, Konto> entry : kontos.entrySet()) {
            String add = entry.getKey().toString() + ": " + entry.getValue().getKontostand().toString() + "\n";
            sb.append(add);
        }
        return sb.toString();
    }

    /**
     * @return Eine LinkedList aller Kontonummern
     */
    public List<Long> getAlleKontonummern() {
        List<Long> result = new LinkedList<>();
        for (Map.Entry<Long, Konto> entry : kontos.entrySet()) {
            result.add(entry.getKey());
        }
        return result;
    }

    /**
     * Hebt Geld von gegebenem Konto ab, wenn möglich
     * @param von Kontonummer von dem abzuhebendem Konto
     * @param betrag Geldbetrag, der abgehoben werden soll
     * @return Ob das Abheben funktioniert hat
     * @throws IllegalArgumentException wenn Kontonummer ungültig ist
     * @throws GesperrtException Wenn das abhebende Konto gesperrt ist
     */
    public boolean geldAbheben(long von, Geldbetrag betrag) throws IllegalArgumentException, GesperrtException {
        if (von < 0L) {
            throw new IllegalArgumentException("Ungültige Kontonummer");
        }
        Konto abgehobener = kontos.get(von);
        if (abgehobener != null) {
            return abgehobener.abheben(betrag);
        }
        return false;
    }

    /**
     * Zahlt geld auf ein Konto ein
     * @param auf Kontonummer des Kontos, auf das eingezahlt werden soll
     * @param betrag Geldbetrag, der eingezahlt werden soll
     * @throws IllegalArgumentException Wenn die Kontonummer ungültig ist
     */
    public void geldEinzahlen(long auf, Geldbetrag betrag) {
        if (auf < 0L) {
            throw new IllegalArgumentException("Ungültige Kontonummer");
        }
        Konto einzahlender = kontos.get(auf);
        if (einzahlender != null) {
            einzahlender.einzahlen(betrag);
        }
    }

    /**
     * Löscht Konto, falls dieses Vorhanden ist
     * @param nummer Kontonummer des Kontos, das gelöscht werden soll
     * @return Ob Kontolöschung erfolgreich verlaufen ist
     */
    public boolean kontoLoeschen(long nummer) {
        if (kontos.get(nummer) != null) {
            kontos.remove(nummer);
            return true;
        }
        return false;
    }

    /**
     * liefert Kontostand zurück
     * @param nummer Kontonummer des Kontostands, von dem Geld zurückgeliefert werden soll
     * @return Kontostand
     */
    public Geldbetrag getKontostand(long nummer) {
        return kontos.get(nummer).getKontostand();
    }

    /**
     * Überweist geld von Konto zu Konto, falls dies möglich ist.
     * @param vonKontonr Konto, von dem Überwiesen werden soll
     * @param nachKontonr Konto, zu dem das Geld überwiesen werden soll
     * @param betrag Betrag, der überwiesenw werden soll
     * @param verwendungszweck Verwendungszweck der Überweisung
     * @return Ob Überweisung funktioniert hat
     * @throws IllegalArgumentException Wenn einer der Parameter ungültig ist
     * @throws GesperrtException wenn eins der Kontos gesperrt ist
     */
    public boolean geldUeberweisen(long vonKontonr, long nachKontonr, Geldbetrag betrag, String verwendungszweck) throws IllegalArgumentException, GesperrtException {
        if (vonKontonr < 0L || nachKontonr < 0L || betrag == null || verwendungszweck == null) {
            throw new IllegalArgumentException("Invalide Überweisung");
        }

        if (kontos.get(vonKontonr) instanceof UeberweisungsfaehigesKonto vonKonto && kontos.get(nachKontonr) instanceof UeberweisungsfaehigesKonto nachKonto) {
            if (kontos.get(vonKontonr).getKontostand().compareTo(betrag) >= 0) {
                if (vonKonto.ueberweisungAbsenden(betrag, nachKonto.getInhaber().getName(), nachKontonr, this.bankleitzahl, verwendungszweck)) {
                    nachKonto.ueberweisungEmpfangen(betrag,vonKonto.getInhaber().getName(), vonKontonr, this.bankleitzahl, verwendungszweck);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Fügt ein Mock-Konto in die Bank ein
     * @param k Das einzufügende Konto (Mock)
     * @return Die zugewiesene Kontonummer
     */
    public long mockEinfuegen(Konto k) {
        Map.Entry<Long, Konto> hoechsterEntry = kontos.floorEntry(100000000L);
        if (hoechsterEntry == null) {
            kontos.put(0L, k);
            return 0L;
        }
        long neueNummer = hoechsterEntry.getKey() + 1L;
        kontos.put(neueNummer, k);
        return neueNummer;
    }
}
