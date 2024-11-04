package bankprojekt.verarbeitung;

/**
 * alle Kontoarten, die unsere Bank anbieten kann
 * Achtung: eigentlich keine gute Enum, weil mit Erweiterungen zu rechnen ist
 */
public enum Kontoart {
	/**
	 * ein Girokonto
	 */
	GIROKONTO("ganz hoher Dispo"),
	/**
	 * ein Sparbuch
	 */
	SPARBUCH("ganz viele Zinsen"),
	/**
	 * ein Festgeldkonto
	 */
	FESTGELDKONTO("kommt später");
	
	private final String info;
	
	Kontoart(String info) { //default-Sichtbarkeit = private
		this.info = info;
	}

	/**
	 * Informationstext zu dieser Kontoart
	 * @return Informationstext zu dieser Kontoart
	 */
	public String getInfo() {
		return this.info;
	}
}
