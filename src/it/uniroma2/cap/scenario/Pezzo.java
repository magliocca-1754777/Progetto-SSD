package it.uniroma2.cap.scenario;


public class Pezzo {
	
	String pezzo; //nome del pezzo: M1 o M2
	float durata; //duarata della costruzione del pezzo
	int costo; //costo della costruzione del pezzo
	
	public Pezzo(String pezzo, float durata, int costo) {
		this.pezzo = pezzo;
		this.durata = durata;
		this.costo = costo;
	}
	
	/**
	 * @return il codice pezzo
	 */
	public String getPezzo() {
		return pezzo;
	}
	
	/**
	 * @return la durata
	 */
	public float getDurata() {
		return durata;
	}
	
	/**
	 * @return il costo
	 */
	public int getCosto() {
		return costo;
	}

}
