package it.uniroma2.cap.scenario;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import it.uniroma2.cap.federate.ImpiantoA;
import it.uniroma2.cap.federate.ImpiantoM;
import it.uniroma2.cap.events.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CAPSimulation {
	
	//Scenario
	private ImpiantoA impiantoAFederate; //ImpiantoA
	private ImpiantoM impiantoMFederate; //ImpiantoM
	private static int _seed = 67; 
	
	//main simulation method
	void run(String[] args) {
		String impianto="M";
		String code;
		String host="localhost";
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		//instatiating federate
		try {
			System.out.println("Car Assembly and Production Simulation");
			System.out.println("_______________________________"+"\n");
			System.out.println("Inserisci tipo di impianto");
			System.out.println("1: M - produzione motori elettrici e termici (default)");
			System.out.println("2: A - produzione veicoli");
		
			code = in.readLine();
			if (code.equals("2")) 
				impianto = "A";
			
			System.out.println("Enter CRC Host or press enter for localhost");
			host = in.readLine();
			if (host.length() == 0)  
				host = "localhost";
		}
		catch(Exception e) {e.printStackTrace();}
		
		//se l'utente ha digitato 1 viene creato il federato ImpiantoM e rimane in attesa dell'ImpiantoA
		if (impianto.equals("M")) {
			impiantoMFederate = new ImpiantoM(impianto, _seed);
						
			//Starting of Federate Execution
			impiantoMFederate.startFederate(host);
			
		//se l'utente ha digitato 1 viene creato il federato ImpiantoM e rimane in attesa dell'ImpiantoA
		}else {
			//parsing del JSON per prendere in input i pezzi da costruire
			String filePath = "file/CAPSimulation.json";

	        // Crea un oggetto JSONParser
	        JSONParser parser = new JSONParser();

	        try {
	            // Leggi il contenuto del file JSON
	            FileReader fileReader = new FileReader(filePath);

	            // Effettua il parsing del JSON
	            JSONObject jsonObject = (JSONObject) parser.parse(fileReader);

	            // Ora puoi accedere ai dati come un oggetto Java
	            long m1 = (long) jsonObject.get("M1");
	            long m2 = (long) jsonObject.get("M2");
	            
	           
	            impiantoAFederate = new ImpiantoA(impianto, _seed,m1,m2);
				
			    //Starting of Federate Execution
			    impiantoAFederate.startFederate(host);

	            // Chiudi il FileReader dopo l'uso
	            fileReader.close();
	        } catch (IOException | ParseException e) {
	            e.printStackTrace();
	        }			
		}
		
	}
		
	public static void main(String[] args) {
		new CAPSimulation().run(args);
		
	}
	


}
