package bankprojekt;

import java.time.LocalDate;
import java.util.*;

import bankprojekt.verarbeitung.*;

/**
 * verwaltet eine Menge von Kunden
 * @author Doro
 *
 */
public class Kundenmenge {
	

	/**
	 * erstellt eine Menge von Kunden und löscht die unnötigen
	 * wieder
	 * @param args Argumente
	 */
	public static void main(String[] args) {
		Kunde anna = new Kunde("Anna", "Müller", "hier", LocalDate.parse("1979-05-14"));
		Kunde berta = new Kunde("Berta", "Beerenbaum", "hier", LocalDate.parse("1980-03-15"));
		Kunde chris = new Kunde("Chris", "Tall", "hier", LocalDate.parse("1979-01-07"));
		Kunde anton = new Kunde("Anton", "Meier", "hier", LocalDate.parse("1982-10-23"));
		Kunde bert = new Kunde("Bert", "Chokowski", "hier", LocalDate.parse("1970-12-24"));
		Kunde doro = new Kunde("Doro", "Hubrich", "hier", LocalDate.parse("1976-07-13"));

		//ToDo: TreeSet mit den vorhandenen Kunden anlegen, Aufgaben 1-3
		TreeSet<Kunde> kunden = new TreeSet<>();
		kunden.add(anna);
		kunden.add(berta);
		kunden.add(chris);
		kunden.add(doro);
		kunden.add(bert);
		kunden.add(anton);

		for (Kunde k : kunden) {
			System.out.println(k);
		}

		System.out.println(kunden.size());

		Scanner tastatur = new Scanner(System.in);
		System.out.println("Nach welchem Namen wollen Sie suchen? ");
		String gesucht = tastatur.nextLine();
		
		// ToDo: Aufgabe 4-6



		for (Kunde k : kunden) {
			if (gesucht.equals(k.getNachname())) {
				System.out.println("Gefundene Person:");
				System.out.println(k);
			}
		}


		Iterator<Kunde> it = kunden.iterator();
		while (it.hasNext()) {
			Kunde k = it.next();
			if (k.getVorname().startsWith("A")) {
				System.out.println("Gelöschte Leute:");
				System.out.println(k);
				it.remove();
			}
		}




		Map<Long, Konto> kontenliste = Map.of(
				1L, new Girokonto(bert, 1, new Geldbetrag(1000)),
				2L, new Girokonto(chris, 2,  new Geldbetrag(1000)),
				3L, new Sparbuch(chris, 3),
				4L, new Girokonto(berta, 4,  new Geldbetrag(1000)),
				5L, new Sparbuch(berta, 5),
				6L, new Girokonto(bert, 6, new Geldbetrag(1000)),
				7L, new Girokonto(chris, 7, new Geldbetrag(1000)),
				8L, new Girokonto(bert, 8, new Geldbetrag(1000)),
				9L, new Sparbuch(chris, 9));
		
		//ToDo: Aufgabe 7 und 8
	}

}
