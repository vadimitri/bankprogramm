package spielereien;

import java.time.LocalDate;

import bankprojekt.verarbeitung.Geldbetrag;
import bankprojekt.verarbeitung.GesperrtException;
import bankprojekt.verarbeitung.Girokonto;
import bankprojekt.verarbeitung.Konto;
import bankprojekt.verarbeitung.Kontoart;
import bankprojekt.verarbeitung.Kunde;
import bankprojekt.verarbeitung.Sparbuch;

/**
 * Testprogramm für Konten
 * @author Doro
 */
public class KontenSpielereien {

	/**
	 * Testprogramm für Konten
	 * @param args wird nicht benutzt
	 * @throws GesperrtException 
	 */
	public static void main(String[] args) throws GesperrtException {
		Kontoart art1 = Kontoart.SPARBUCH;
		
		art1 = Kontoart.valueOf("FESTGELDKONTO");
		
		Kontoart[] alle = Kontoart.values();
		for(Kontoart art: alle) {
			System.out.println(art.ordinal() + " " + art.name() + ": " + art.getInfo());
		}
		Kunde ich = new Kunde("Dorothea", "Hubrich", "zuhause", LocalDate.parse("1976-07-13"));

		Konto meins = new Girokonto();
			//impliziter Up-Cast
			//Zuweisungkompatibilität
			//Polymorphie
		boolean geht = meins.abheben(new Geldbetrag(100));
		System.out.println(geht);
		
		meins.ausgeben();
		
		
		System.exit(0);
		
		Girokonto meinGiro = new Girokonto(ich, 1234, new Geldbetrag(1000));
		meinGiro.einzahlen(new Geldbetrag(50));
		System.out.println(meinGiro);
		
		Sparbuch meinSpar = new Sparbuch(ich, 9876);
		meinSpar.einzahlen(new Geldbetrag(50));
		try
		{
			boolean hatGeklappt = meinSpar.abheben(new Geldbetrag(70));
			System.out.println("Abhebung hat geklappt: " + hatGeklappt);
			System.out.println(meinSpar);
		}
		catch (GesperrtException e)
		{
			System.out.println("Zugriff auf gesperrtes Konto - Polizei rufen!");
		}
	}

}
