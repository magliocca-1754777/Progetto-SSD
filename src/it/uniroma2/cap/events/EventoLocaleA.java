package it.uniroma2.cap.events;

import java.util.ArrayList;

//*************************************************************

public class EventoLocaleA extends Event implements Comparable<EventoLocaleA>{
	
	private String pezzo; //attività generata o influenzata dall'evento
	private ArrayList<Float> report = new ArrayList<Float>();  //report ricevuto da M 
	
	public EventoLocaleA(EventType eventType, Long time, String pezzo) {
		super(eventType, time);
		this.pezzo = pezzo;
	}
	
	public EventoLocaleA(EventType eventType, Long time, ArrayList<Float> report) {
		super(eventType, time);
		for (float dato : report) {
			this.report.add(dato);
		}
	}

	/**
	 * @return attività
	 */
	public String getPezzo() {
		return pezzo;
	}
    
	/**
	 * @return attività
	 */
	public ArrayList<Float> getReport() {
		return report;
	}

	/**
	 * @param attività da inserire
	 */
	public void setPezzo(String pezzo) {
		this.pezzo = pezzo;
	}
	
	@Override
	public int compareTo(EventoLocaleA o) {
		if(this.getTime() == o.getTime()) 
			return 0; 
		else if (this.getTime() > o.getTime())
			return 1;
		else return -1;
		
	}
	

}
