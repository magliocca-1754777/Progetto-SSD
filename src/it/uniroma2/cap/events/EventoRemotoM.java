package it.uniroma2.cap.events;

import java.util.ArrayList;

public class EventoRemotoM extends Event {
	
	private String pezzo; //pezzo costruito da ImpiantoM
	private ArrayList<Float> reportM = new ArrayList<Float>(); //report generato da ImpiantoM
	
	public EventoRemotoM(EventType type, Long time, String pezzo) {
		super(type, time);
		this.pezzo = pezzo;
	}	
	
	public EventoRemotoM(EventType type, Long time, ArrayList<Float> reportM) {
		super(type, time);
		for (float dato : reportM) {
			this.reportM.add(dato);
		}
	}	
	
	/**
	 * @return il codice del pezzo
	 */
	public String getPezzo() {
		return pezzo;
	}
    
	/**
	 * @return report di M
	 */
	public  ArrayList<Float> getReport() {
		return reportM;
	}

	/**
	 * @param codice del pezzo da inserire
	 */
	public void setPezzo(String pezzo) {
		this.pezzo = pezzo;
	}

}
