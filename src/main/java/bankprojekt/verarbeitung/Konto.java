package bankprojekt.verarbeitung;

import bankprojekt.geld.Waehrung;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * stellt ein allgemeines Bank-Konto dar
 */
public abstract class Konto implements Comparable<Konto>, Serializable
{
	/**
	 * gibt this auf der Konsole aus
	 * und gehört hier nicht her!!!!! 
	 * Trennen Sie Verarbeitung von Ein-/Ausgabe!
	 */
	public void ausgeben() {
		System.out.println(this);
	}
	/** 
	 * der Kontoinhaber
	 */
	private Kunde inhaber;

	/**
	 * die Kontonummer
	 */
	private final long nummer;

	/**
	 * der aktuelle Kontostand
	 */
	private bankprojekt.verarbeitung.Geldbetrag kontostand;

	/**
	 * Liste von Kontobeobachtern
	 */
	protected List<KontoBeobachter> beobachter = new ArrayList<>();

	/**
	 * setzt den aktuellen Kontostand
	 * @param kontostand neuer Kontostand, darf nicht null sein
	 */
	protected void setKontostand(Geldbetrag kontostand) {
		if (kontostand != null) {
			Geldbetrag alterKontostand = this.kontostand;
			this.kontostand = kontostand;
			benachrichtigen(alterKontostand, kontostand);
		}
	}

	/**
	 * Wenn das Konto gesperrt ist (gesperrt = true), können keine Aktionen daran mehr vorgenommen werden,
	 * die zum Schaden des Kontoinhabers wären (abheben, Inhaberwechsel)
	 */
	private boolean gesperrt;

	/**
	 * Setzt die beiden Eigenschaften kontoinhaber und kontonummer auf die angegebenen Werte,
	 * der anfängliche Kontostand wird auf 0 gesetzt.
	 *
	 * @param inhaber der Inhaber
	 * @param kontonummer die gewünschte Kontonummer
	 * @throws IllegalArgumentException wenn der inhaber null ist
	 */
	public Konto(Kunde inhaber, long kontonummer) {
		if(inhaber == null)
			throw new IllegalArgumentException("Inhaber darf nicht null sein!");
		this.inhaber = inhaber;
		this.nummer = kontonummer;
		this.kontostand = new Geldbetrag();
		this.gesperrt = false;
	}
	
	/**
	 * setzt alle Eigenschaften des Kontos auf Standardwerte
	 */
	public Konto() {
		this(Kunde.MUSTERMANN, 1234567);
	}

	/**
	 * liefert den Kontoinhaber zurück
	 * @return   der Inhaber
	 */
	public Kunde getInhaber() {
		return this.inhaber;
	}
	
	/**
	 * setzt den Kontoinhaber
	 * @param kinh   neuer Kontoinhaber
	 * @throws GesperrtException wenn das Konto gesperrt ist
	 * @throws IllegalArgumentException wenn kinh null ist
	 */
	public void setInhaber(Kunde kinh) throws GesperrtException{
		if (kinh == null)
			throw new IllegalArgumentException("Der Inhaber darf nicht null sein!");
		if(this.gesperrt)
			throw new GesperrtException(this.nummer);        
		this.inhaber = kinh;

	}
	
	/**
	 * liefert den aktuellen Kontostand
	 * @return   Kontostand
	 */
	public Geldbetrag getKontostand() {
		return kontostand;
	}

	/**
	 * liefert die Kontonummer zurück
	 * @return   Kontonummer
	 */
	public long getKontonummer() {
		return nummer;
	}

	/**
	 * liefert zurück, ob das Konto gesperrt ist oder nicht
	 * @return true, wenn das Konto gesperrt ist
	 */
	public boolean isGesperrt() {
		return gesperrt;
	}
	
	/**
	 * Erhöht den Kontostand um den eingezahlten Betrag.
	 *
	 * @param betrag double
	 * @throws IllegalArgumentException wenn der betrag negativ ist 
	 */
	public void einzahlen(Geldbetrag betrag) {
		if (betrag == null || betrag.isNegativ()) {
			throw new IllegalArgumentException("Falscher Betrag");
		}
		setKontostand(getKontostand().plus(betrag));
	}
	
	@Override
	public String toString() {
		String ausgabe;
		ausgabe = "Kontonummer: " + this.getKontonummerFormatiert()
				+ System.getProperty("line.separator");
		ausgabe += "Inhaber: " + this.inhaber;
		ausgabe += "Aktueller Kontostand: " + getKontostand() + " ";
		ausgabe += this.getGesperrtText() + System.getProperty("line.separator");
		return ausgabe;
	}

	/**
	 * Mit dieser Methode wird der geforderte Betrag vom Konto abgehoben, wenn es nicht gesperrt ist
	 * und die speziellen Abheberegeln des jeweiligen Kontotyps die Abhebung erlauben
	 *
	 * @param betrag abzuhebender Betrag
	 * @throws GesperrtException wenn das Konto gesperrt ist
	 * @throws IllegalArgumentException wenn der betrag negativ oder unendlich oder NaN ist 
	 * @return true, wenn die Abhebung geklappt hat, 
	 * 		   false, wenn sie abgelehnt wurde
	 */
	public final boolean abheben(Geldbetrag betrag) throws GesperrtException {
		if (betrag == null || betrag.isNegativ()) {
			throw new IllegalArgumentException("Betrag ungültig");
		}
		if(this.isGesperrt())
			throw new GesperrtException(this.getKontonummer());

		if (betrag.compareTo(getGrenzwert(betrag)) <= 0) {
			setKontostand(getKontostand().minus(betrag));
			abspeichern(betrag);
			return true;
		}
		else
			return false;
	}

	protected abstract Geldbetrag getGrenzwert(Geldbetrag betrag);

	protected void abspeichern(Geldbetrag betrag) {}
	
	/**
	 * sperrt das Konto, Aktionen zum Schaden des Benutzers sind nicht mehr möglich.
	 */
	public void sperren() {
		this.gesperrt = true;
	}

	/**
	 * entsperrt das Konto, alle Kontoaktionen sind wieder möglich.
	 */
	public void entsperren() {
		this.gesperrt = false;
	}
	
	
	/**
	 * liefert eine String-Ausgabe, wenn das Konto gesperrt ist
	 * @return "GESPERRT", wenn das Konto gesperrt ist, ansonsten ""
	 */
	public String getGesperrtText()
	{
		if (this.gesperrt)
		{
			return "GESPERRT";
		}
		else
		{
			return "";
		}
	}
	
	/**
	 * liefert die ordentlich formatierte Kontonummer
	 * @return auf 10 Stellen formatierte Kontonummer
	 */
	public String getKontonummerFormatiert()
	{
		return String.format("%10d", this.nummer);
	}
	
	/**
	 * Vergleich von this mit other; Zwei Konten gelten als gleich,
	 * wen sie die gleiche Kontonummer haben
	 * @param other das Vergleichskonto
	 * @return true, wenn beide Konten die gleiche Nummer haben
	 */
	@Override
	public boolean equals(Object other)
	{
		if(this == other)
			return true;
		if(other == null)
			return false;
		if(this.getClass() != other.getClass())
			return false;
		if(this.nummer == ((Konto)other).nummer)
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode()
	{
		return 31 + (int) (this.nummer ^ (this.nummer >>> 32));
	}

	@Override
	public int compareTo(Konto other)
	{
		if(other.getKontonummer() > this.getKontonummer())
			return -1;
		if(other.getKontonummer() < this.getKontonummer())
			return 1;
		return 0;
	}

	/**
	 * Wechselt die Währung eines Kontos
	 * @param neu neue Währung
	 */
	public void waehrungswechsel(Waehrung neu) {
		if (neu == null) {
			throw new NullPointerException();
		}
		this.kontostand.umrechnen(neu);
	}

	/**
	 * Fügt einen Beobachter hinzu
	 * @param beobachter Beobachter
	 */
	public void addBeobachter(KontoBeobachter beobachter) {
		if (beobachter != null) {
			//TODO: Addpropertychangelistener
			this.beobachter.add(beobachter);
		}
	}

	/**
	 * Entfernt einen Beobachter
	 * @param beobachter Beobachter
	 */
	public void removeBeobachter(KontoBeobachter beobachter) {
		this.beobachter.remove(beobachter);
	}

	/**
	 * Benachrichtigt alle Beobachter
	 * @param alterKontostand Alter Kontostand
	 * @param neuerKontostand Neuer Kontostand
	 */
	protected void benachrichtigen(Geldbetrag alterKontostand, Geldbetrag neuerKontostand) {
		for (KontoBeobachter b : beobachter) {
			b.kontostandGeaendert(this, alterKontostand, neuerKontostand);
		}
	}

	public static void main(String[] args) {
		Konto k = new bankprojekt.verarbeitung.Girokonto(new bankprojekt.verarbeitung.Kunde(), 123456789, new bankprojekt.verarbeitung.Geldbetrag(500));
		k.einzahlen(new bankprojekt.verarbeitung.Geldbetrag(100, Waehrung.EUR));
		System.out.println(k);
		k.waehrungswechsel(Waehrung.ESCUDO);
		System.out.println(k);

	}
}
