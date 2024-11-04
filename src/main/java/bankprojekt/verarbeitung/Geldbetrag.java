package bankprojekt.verarbeitung;

import bankprojekt.geld.Waehrung;
import org.decimal4j.util.DoubleRounder;

/**
 * Ein Geldbetrag mit Währung
 */
public class Geldbetrag implements Comparable<Geldbetrag>{
	/**
	 * Betrag in der in waehrung angegebenen Währung
	 */
	private double betrag;
	/**
	 * Die Währung
	 */
	private Waehrung waehrung = Waehrung.EUR;
	
	/**
	 * erstellt den Betrag 0€
	 */
	public Geldbetrag()
	{}
	
	/**
	 * erstellt einen Geldbetrag in der Währung Euro
	 * @param betrag Betrag in €
	 * @throws IllegalArgumentException wenn betrag unendlich oder NaN ist
	 */
	public Geldbetrag(double betrag)
	{
		if(!Double.isFinite(betrag))
			throw new IllegalArgumentException();
		this.betrag = betrag;
	}

	/**
	 * erstellt Geldbetrag in gegebener Währung
	 * @param betrag betrag in Währung w
	 * @param w Währung
	 * @throws IllegalArgumentException wenn betrag unendlich oder NaN ist
	 * @throws NullPointerException wenn Waehrung null ist
	 */
	public Geldbetrag(double betrag, Waehrung w) {
		if(!Double.isFinite(betrag))
			throw new IllegalArgumentException();
		if (w == null) {
			throw new NullPointerException();
		}
		this.betrag = betrag;
		this.waehrung = w;
	}

	/**
	 * rechnet Währung des Geldbetrags this in eine gegebene Zielwährung um
	 * @param zielwaehrung
	 * @throws NullPointerException wenn Zielwährung null ist
	 */
	public void umrechnen(Waehrung zielwaehrung) {
		if (zielwaehrung == null) {
			throw new NullPointerException();
		}
		if (zielwaehrung == null) {
			throw new NullPointerException();
		}
		if (this.waehrung != zielwaehrung) {
			double neuerBetrag = (this.betrag / this.waehrung.getUmrechnungskurs()) * zielwaehrung.getUmrechnungskurs();
			this.betrag = DoubleRounder.round(neuerBetrag, 2);
			this.waehrung = zielwaehrung;
		}
	}

	/**
	 * Betrag von this
	 * @return Betrag in der Währung von this
	 */
	public double getBetrag() {
		return betrag;
	}

	/**
	 * Währung von this
	 * @return Währung von this
	 */
	public Waehrung getWaehrung() {
		return waehrung;
	}


	/**
	 * rechnet this + summand, Währung umgerechnet falls nötig
	 * @param summand zu addierender Betrag
	 * @return this + summand in der Währung von this
	 * @throws IllegalArgumentException wenn summand null ist
	 */
	public Geldbetrag plus(Geldbetrag summand)
	{
		if(summand == null || summand.betrag < 0)
			throw new IllegalArgumentException();
		if (this.waehrung != summand.waehrung) {
			this.umrechnen(summand.waehrung);
		}
		return new Geldbetrag(DoubleRounder.round(this.betrag + summand.betrag,2), this.waehrung);
	}
	
	/**
	 * rechnet this - divisor, Währung umgerechnet falls nötig
	 * @param subtrahend abzuziehender Betrag
	 * @return this - divisor in der Währung von this
	 * @throws IllegalArgumentException wenn divisor null ist
	 */
	public Geldbetrag minus(Geldbetrag subtrahend)
	{
		if(subtrahend == null)
			throw new IllegalArgumentException();
		if (this.waehrung != subtrahend.waehrung) {
			this.umrechnen(subtrahend.waehrung);
		}
		return new Geldbetrag(DoubleRounder.round(this.betrag - subtrahend.betrag, 2), this.waehrung);
	}


	@Override
	public int compareTo(Geldbetrag o) {

		// Währungen werden angeglichen, falls nötig
		if (this.waehrung != o.waehrung) {
			this.umrechnen(o.waehrung);
		}
		return Double.compare(this.betrag, o.betrag);
	}

	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof Geldbetrag)) return false;
		if(o == this) return true;
		return this.compareTo((Geldbetrag) o) == 0;
	}
	
	/**
	 * prüft, ob this einen negativen Betrag darstellt
	 * @return true, wenn this negativ ist
	 */
	public boolean isNegativ()
	{
		return this.betrag < 0;
	}
	
	@Override
	public String toString()
	{

		return String.format("%,.2f %s", this.betrag, this.waehrung.name());
	}

	public static void main(String[] args) {
		Geldbetrag g1 = new Geldbetrag(111, Waehrung.ESCUDO);
		System.out.println(g1);
		g1 = g1.plus(new Geldbetrag(222, Waehrung.EUR));
		System.out.println(g1);
		Geldbetrag g2 = new Geldbetrag(222, Waehrung.EUR);
		System.out.println(g2);
		g2 = g2.plus(new Geldbetrag(111, Waehrung.ESCUDO));
		System.out.println(g2);
		Geldbetrag c1 = new Geldbetrag(1, Waehrung.EUR);
		Geldbetrag c2 = new Geldbetrag(Waehrung.ESCUDO.getUmrechnungskurs(), Waehrung.ESCUDO);
		System.out.println(c1);
		System.out.println(c2);
		System.out.println(c1.compareTo(c2));
	}
}
