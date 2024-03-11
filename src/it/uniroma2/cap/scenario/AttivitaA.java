package it.uniroma2.cap.scenario;

public class AttivitaA {
	
	String attivita; //nome dell'attivita
	float durata; //durata dell'attivita
	int costo; //costo dell'attivita
	
	public AttivitaA(String attivita, float durata, int costo) {
		this.attivita = attivita;
		this.durata = durata;
		this.costo = costo;
	}
	

	/**
	 * @return il codice dell'attività
	 */
	public String getAttivita() {
		return attivita;
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
