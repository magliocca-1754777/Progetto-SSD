package it.uniroma2.cap.events;

import java.util.ArrayList;

public class EventoRemotoA extends Event {
	
	private ArrayList<String> listaPezzi = new ArrayList<String>(); //pezzi da richiedere a ImpiantoM
	
	public EventoRemotoA(EventType type, Long time, ArrayList<String> listaPezzi) {
		super(type, time);
		this.listaPezzi = listaPezzi;
	}	
	
	/**
	 * @return la lista dei pezzi da passare ad M
	 */
	public ArrayList<String> getListaPezzi() {
		return listaPezzi;
	}

}
