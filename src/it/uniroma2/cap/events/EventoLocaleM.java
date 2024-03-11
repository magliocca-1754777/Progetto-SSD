package it.uniroma2.cap.events;

import java.util.ArrayList;

import it.uniroma2.cap.scenario.Pezzo;

public class EventoLocaleM extends Event implements Comparable<EventoLocaleM>{
	
	private Pezzo pezzo; //pezzo che deve costruire ImpiantoM
	private ArrayList<String> listaPezzi = new ArrayList <String>(); //pezzi da costruire
	
	public EventoLocaleM(EventType eventType, Long time, Pezzo pezzo) {
		super(eventType, time);
		this.pezzo = pezzo;
	}
    
	public EventoLocaleM(EventType eventType, Long time, ArrayList<String> listaPezzi) {
		super(eventType, time);
		for (String pezzo : listaPezzi) {
			this.listaPezzi.add(pezzo);
		}
	}

	/**
	 * @return pezzo
	 */
	public Pezzo getPezzo() {
		return pezzo;
	}

	/**
	 * @param pezzo da inserire
	 */
	public void setPezzo(Pezzo pezzo) {
		this.pezzo = pezzo;
	}
	
	/**
	 * @return la lista dei pezzi da costruire
	 */
	public ArrayList<String> getListaPezzi() {
		return listaPezzi;
	}
	
	@Override
	public int compareTo(EventoLocaleM o) {
		if(this.getTime() == o.getTime()) 
			return 0; 
		else if (this.getTime() > o.getTime())
			return 1;
		else return -1;
		
	}
	
	

}
