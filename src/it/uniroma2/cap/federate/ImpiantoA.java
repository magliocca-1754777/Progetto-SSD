package it.uniroma2.cap.federate;

import it.uniroma2.cap.events.*;
import it.uniroma2.cap.scenario.AttivitaA;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Random;

import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.DataElement;
import hla.rti1516e.encoding.DataElementFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.encoding.HLAvariableArray;
import hla.rti1516e.time.HLAinteger64Time;
import hla.rti1516e.time.HLAinteger64TimeFactory;
import hla.rti1516e.exceptions.AlreadyConnected;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.ConnectionFailed;
import hla.rti1516e.exceptions.CouldNotCreateLogicalTimeFactory;
import hla.rti1516e.exceptions.CouldNotOpenFDD;
import hla.rti1516e.exceptions.ErrorReadingFDD;
import hla.rti1516e.exceptions.FederateAlreadyExecutionMember;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederateOwnsAttributes;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.InconsistentFDD;
import hla.rti1516e.exceptions.InvalidLocalSettingsDesignator;
import hla.rti1516e.exceptions.InvalidResignAction;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.OwnershipAcquisitionPending;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;
import hla.rti1516e.exceptions.UnsupportedCallbackModel;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.time.HLAinteger64Interval;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandle;

public class ImpiantoA {

	//ImpiantoA proprietà
	private String nome; //codice impianto A
	private ArrayList<Float> report; //insieme di attività sotto la responsabilità dell'impianto A
	private SortedSet<EventoLocaleA> eventsList; //ordered list of events to be processed
	private ArrayList<String> listaPezzi = new ArrayList<String>(); //la lista dei pezzi da inviare a ImpiantoM
	private ArrayList<AttivitaA> veicoliElettrici = new ArrayList<AttivitaA>(); //lista di attività effettuate da ImpiantoA per la produzione dei veicoli elettrici
	private ArrayList<AttivitaA> veicoliTermici = new ArrayList<AttivitaA>(); //lista di attività effettuate da ImpiantoA per la produzione dei veicoli termici
	private int pezziAssemblati; //numero pezzi assemblati finora
	private long M1; //pezzi di M1 da richiedere a ImpiantoM
	private long M2; //pezzi di M2 da richiedere a ImpiantoM
    private boolean reportRicevuto; //true se ImpiantoA ha ricevuto il report da ImpiantoM
    private long assemblaggioTerminato; //tempo in cui ha terminato di assemblare un pezzo
    
	//simulation properties
	private int _seed;
	
	//HLA-related properties
	protected RTIambassador rtiAmb;
	protected final String FEDERATION_NAME = "CAP Simulation";
	protected FederateAmbassadorImplA fedAmbassador;
	protected String federateName;
	protected HLAinteger64TimeFactory timeFactory; 
	protected EncoderFactory encoderFactory;
	
	// handles types 	
	protected InteractionClassHandle icRemoteEventAHandle;
	protected InteractionClassHandle icRemoteEventMHandle;
	protected ParameterHandle timeHandle;
	protected ParameterHandle startingRequestHandle;
	protected ParameterHandle pezzoHandle;
	protected ParameterHandle reportHandle;
	protected ParameterHandle typeHandleA;
	protected ParameterHandle typeHandleM;
	
	public ImpiantoA(String nome, int seed, long M1, long M2) {
		this.nome = nome;
		eventsList = new TreeSet<EventoLocaleA>(); 
		federateName = "ImpiantoA";
		this._seed = seed;	
		this.M1 = M1;
		this.M2 = M2;
		this.listaPezzi = new ArrayList<String>();
		this.veicoliElettrici = new ArrayList<AttivitaA>();
		this.veicoliTermici = new ArrayList<AttivitaA>();
		this.report = new ArrayList<Float>();
	}
    
	/*
	 * aggiunge evento alla lista degli eventi
	 */
	public void addEvent(EventoLocaleA e) {
		this.eventsList.add(e);
	}
	/*
	 * restituisce il prossimo evento nella lista
	 * 
	 */
	private EventoLocaleA getNextEvent() {
		return this.eventsList.first();
	}
	
	/*
	* listaPezzi risultante avrà la lista di M1 e M2 da costruire in ordine casuale es listaPezzi = [M1, M2, M2, ...]
	*/
	public void addPezzi(long M1, long M2) {
		Random rand = new Random(_seed);
		while(M1 + M2 != 0) {
			int n = rand.nextInt(2);
			if (n == 0 && M1 != 0) {
				listaPezzi.add("M1");
				M1--;
			}else if (n == 1 && M2 != 0) {
				listaPezzi.add("M2");
				M2--;
			}else if(n == 0 && M1 == 0) {
				for(int i =1; i<= M2; i++) {
					listaPezzi.add("M2");
					M2--;
				}
			}else {
				for(int i =1; i<= M1; i++) {
					listaPezzi.add("M1");
					M1--;
				}
			}
		}
	}
	
	
	/**
	 * @return federation name
	 */
	public String getFederateName() {
		return federateName;
	}


	public void startFederate(String host) {
		EventoLocaleA event;         //evento da processare
		long nextEventTimestamp; 	// timestamp del prossimo evento da processare
		long currentTime;			// tempo logico corrente del federato
		long nextTime;				// timestamp del prossimo evento da schedulare
		long timeStep = 1;			// timestep usato per determinare il valore di nextTime
		
		//Federate Initialization
		initFederate(host);
		
		//---------- 10 Simulation Main Loop ---------------
		System.out.println("\n" +"___________________________________________");
		System.out.println("Simulation Begins....");
		System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Simulation Start ");
		
		
		System.out.println("******** L'impianto A invia il numero di pezzi da costruire all'impianto M ********");
		//si invia l'interazione all'impianto M contenente la lista di pezzi
		addPezzi(M1,M2);
		Random rand = new Random(_seed);
		EventoRemotoA eventoRemotoA = new EventoRemotoA(EventType.STARTING_REQUEST, (long)(2 + rand.nextInt(10)), listaPezzi);
		sendRemoteEvent(eventoRemotoA);
		
		
		try {
			while(true) {
				currentTime = fedAmbassador.getFederateTime();	
				//se c'è almeno un evento nella lista degli eventi il federato chiede di avanzare al tempo del prossimo evento da processare
				if (!eventsList.isEmpty()) {				
					nextEventTimestamp = getNextEvent().getTime();	
					System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Next Message Time: " + nextEventTimestamp);
					fedAmbassador.isAdvancing = true;
					System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Ask a Time Advance");
					rtiAmb.nextMessageRequest(timeFactory.makeTime(nextEventTimestamp));
					//attende che gli venga accordata la richiesta di avanzamento	
					while(fedAmbassador.isAdvancing)
						Thread.sleep(10);
					System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Time Advance Grant");
					//processa l'evento
					event = getNextEvent();					
					process(event);
					//rimuove l'evento dalla lista degli eventi da processare
					eventsList.remove(event);
				}
				//se ImpiantoA riceve il report da ImpiantoM termina la simulazione
				else if(reportRicevuto) {
					break;
				}
				else {					
					//se non ci sono eventi locali, il tempo avanza a timeStep fissi
					nextTime = currentTime + timeStep;
					fedAmbassador.isAdvancing = true;
					rtiAmb.nextMessageRequest(timeFactory.makeTime(nextTime));
					while(fedAmbassador.isAdvancing)
						Thread.sleep(10);		
				}
			
			}
			System.out.println("Simulation Completed");
			System.out.println("___________________________________________" + "\n");
			
			//il federato esce dalla federazione e se possibile la distrugge
			leaveSimulationExecution();
			//stampa il report finale
			displayFederateState();
			
		}catch (Exception e) {e.printStackTrace();}		
			
	}
	
	private void initFederate(String host) {
		String settings;

		System.out.println( "Starting of " + getFederateName());
		
		
		try {
			//---------- 1 & 2 create RTIambassador and Connect---------------
			
			rtiAmb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
			encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
			
			fedAmbassador = new FederateAmbassadorImplA(this);
			settings = "crcAddress=" + host;
	        rtiAmb.connect(fedAmbassador, CallbackModel.HLA_IMMEDIATE, settings);
	        
	        System.out.println( "Connected to RTI" );
	        
	      //---------------- 3 create Federation Execution -------------------
	      
	        URL[] fom = new URL[]{
	        		(new File("fom/cap_fom.xml")).toURI().toURL(),
			};
	        	
		    rtiAmb.createFederationExecution(FEDERATION_NAME, fom, "HLAinteger64Time" );
	        System.out.println( "Created Federation" );

		}catch(FederationExecutionAlreadyExists e) {
			
			System.out.println("Connecting to an existing Federation Execution");
		}
		catch(ErrorReadingFDD | InconsistentFDD | CouldNotCreateLogicalTimeFactory |
                 CouldNotOpenFDD  | NotConnected | CallNotAllowedFromWithinCallback | 
                 RTIinternalError | MalformedURLException e ) {e.printStackTrace();} 
		catch (ConnectionFailed e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidLocalSettingsDesignator e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedCallbackModel e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlreadyConnected e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    try {
			
	        //---------------------- 4 Join Federation -----------------------
	        rtiAmb.joinFederationExecution( getFederateName(), FEDERATION_NAME);
	        System.out.println( "Joined Federation as " + getFederateName());

			//-------------- 5 Sync Points Registering ----------------
		

			//ImpiantoA attende che ImpiantoM si unisca alla federazione
	        FederateHandle f=null;
	        
	        while(f==null) {
        		try {
        			f = rtiAmb.getFederateHandle("ImpiantoM");
        		}
        	catch(FederateNotExecutionMember | NameNotFound ignored) {}
        	}
	        //quando entrambi i federati sono nella federazione si richiede la conferma dell'utente 
	        //per contnuare l'esecuzione in modo che i federati siano sincronizzati
	        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	        System.out.println("premere invio per continuare");
	        in.readLine();
			
	        if (!fedAmbassador.isAnnounced) {
	            //registra il punto di sincronizzazione
	        	rtiAmb.registerFederationSynchronizationPoint("ReadyToRun",null);
	        } 
			
			//semaforo posto inizialmente a false, viene messo a true quando l'rti invoca la callback federateSynch
			while(!fedAmbassador.isAnnounced) 
				Thread.sleep(10);
			
			//---------------------- 6 Time Management -------------------------
			
			// ImpiantoA è time-regulated e time-constrained		
			timeFactory = (HLAinteger64TimeFactory)rtiAmb.getTimeFactory();
			HLAinteger64Interval lookahead = timeFactory.makeInterval(fedAmbassador.federateLookahead);
			rtiAmb.enableTimeRegulation(lookahead);
					
			while(!fedAmbassador.isRegulating) {
				Thread.sleep(10);
			}
			System.out.println(federateName + " is Time Regulated");	
		    
			rtiAmb.enableTimeConstrained();
			
			while(!fedAmbassador.isConstrained) {
				Thread.sleep(10);
			}
			System.out.println(federateName + " is Time Constrained");	
			
			//---------------------- 7 Publish & Subscribe -------------------------
			
			//interazioni e parametri			
			this.icRemoteEventMHandle = rtiAmb.getInteractionClassHandle("HLAinteractionRoot.EventoRemotoM");
			this.pezzoHandle = rtiAmb.getParameterHandle(icRemoteEventMHandle, "pezzo");
			this.reportHandle = rtiAmb.getParameterHandle(icRemoteEventMHandle, "report");
			this.typeHandleM = rtiAmb.getParameterHandle(icRemoteEventMHandle, "Type");
			
			this.icRemoteEventAHandle = rtiAmb.getInteractionClassHandle("HLAinteractionRoot.EventoRemotoA");
			this.startingRequestHandle = rtiAmb.getParameterHandle(icRemoteEventAHandle, "startingRequest");
			this.typeHandleA = rtiAmb.getParameterHandle(icRemoteEventAHandle, "Type");
		
			//pubblica l'interazione Evento Remoto A e sottoscrive l'interazione Evento Remoto M
			rtiAmb.publishInteractionClass(icRemoteEventAHandle);
			rtiAmb.subscribeInteractionClass(icRemoteEventMHandle);
		
			
			
			//--------------------- 8 Synchronization Before Running -----------------------
			
			//punto di sincronizzazione raggiunto
			rtiAmb.synchronizationPointAchieved("ReadyToRun");
			while(!fedAmbassador.isReadyToRun) 
				Thread.sleep(10);	
			System.out.println(federateName + " All Federates achieved READY_TO_RUN Sync Point");
			
		}
		catch ( FederationExecutionDoesNotExist |  SaveInProgress | RestoreInProgress | FederateAlreadyExecutionMember | NotConnected
                | CallNotAllowedFromWithinCallback | RTIinternalError e) 
        {
            System.err.println("Cannot connect to the Federation");
            e.printStackTrace();
        }	
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	private void process(EventoLocaleA e) {
		System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Processing Event "  + e.getEventType());
		
		long currentTime = fedAmbassador.getFederateTime();		//tempo logico corrente del federato
		long nextEventTime; 	                                //tempo per schedulare il prossimo evento 

		String pezzo = eventsList.first().getPezzo();           //pezzo costruito da ImpiantoM
		ArrayList<Float> report = new ArrayList<Float>();;      //report generato da ImpiantoM dopo la costruzione di tutti i pezzi
		
		switch(e.getEventType()) {				
			case VEHICLE_ASSEMBLATION: 				
				//Se ImpiantoA è libero si avvia la produzione del veicolo con il pezzo ricevuto da ImpiantoM
				if(currentTime >= assemblaggioTerminato) {
				    System.out.println("[" + fedAmbassador.federateTime + "] Impianto A in lavorazione");
				    //durata aleatoria esponenziale della produzione del veicolo
				    long durata = (long)ExponentialDistribution(60,5)+1;
				    //tempo in cui ImpiantoA terminerà la produzione del veicolo
				    assemblaggioTerminato = currentTime + durata; 
				    //se il pezzo ricevuto da ImpiantoM è un motore elettrico, ImpiantoA avvia l'attività di produzione del veicolo elettrico
				    if (pezzo.equals("M1")) {
				    	AttivitaA attivita = new AttivitaA("Produzione veicolo elettrico",durata,12000);
				    	veicoliElettrici.add(attivita);
				    }
				    //altrimenti avvia l'attività di produzione del veicolo terimico
				    else {
				    	AttivitaA attivita = new AttivitaA("Produzione veicolo termico",durata,10000);
				    	veicoliTermici.add(attivita);
				    }
				    //contatore per tenere traccia dei veicoli prodotti finora
				    pezziAssemblati +=1;
				    System.out.println("[" + fedAmbassador.federateTime + "] Impianto A terminerà al tempo " + assemblaggioTerminato);
				}else {
					//se ImpiantoA è in lavorazione l'evento viene rischedulato ad un tempo in cui ImpianoA ha terminato di assemblare il veicolo
					nextEventTime =   assemblaggioTerminato;
					System.out.println("[" + fedAmbassador.federateTime + "] Impianto A essendo occupato rischedula l'evento al tempo " + nextEventTime);
					addEvent(new EventoLocaleA(EventType.VEHICLE_ASSEMBLATION, nextEventTime, pezzo));
				}
				break;
			
			case RECEIVE_REPORT_M:
				//Se ImpiantoA è libero e ha terminato di assemblare tutti i veicoli richiesti può procedere con la creazione del report finale
				if(currentTime >= assemblaggioTerminato && pezziAssemblati == M1+M2) {
				System.out.println("[" + fedAmbassador.federateTime + "] Receive Report al tempo " + currentTime );
				//ottiene report ricevuto da M
				report = e.getReport();
				//crea report finale
				//aggiunge al report il costo totale di produzione dei veicoli termici incluso quello dei motori (inviato da ImpiantoM)
				this.report.add(report.get(3)+ veicoliTermici.get(0).getCosto()*veicoliTermici.size());
				//aggiunge al report il costo totale di produzione dei veicoli elettrici incluso quello dei motori (inviato da ImpiantoM)
				this.report.add(report.get(2)+ veicoliElettrici.get(0).getCosto()*veicoliElettrici.size());
				float durataTotaleVE = 0;
				//calcola tempo medio di produzione dei veicoli elettrici
				for (AttivitaA attivita : veicoliElettrici) {
					durataTotaleVE += attivita.getDurata();					
				}
				float durataMediaVE = durataTotaleVE/veicoliElettrici.size();
				//calcola tempo medio di produzione dei veicoli termici
				float durataTotaleVT = 0;
				for (AttivitaA attivita : veicoliTermici) {
					durataTotaleVT += attivita.getDurata();					
				}
				float durataMediaVT = durataTotaleVT/veicoliTermici.size();
				//aggiunge al report il tempo medio di produzione dei veicoli elettrici
				this.report.add(durataMediaVE);
				//aggiunge al report il tempo medio di produzione dei veicoli termici
				this.report.add(durataMediaVT);		
				System.out.println("ImpiantoA ha creato il report finale");
				//dopo aver creato il report finale, reportRicevuto viene settato a true per terminare la simulazione
				reportRicevuto = true;
				
				}
				else {
					//se ImpiantoA è in lavorazione l'evento viene rischedulato ad un tempo in cui ImpianoA ha terminato di assemblare il veicolo
					nextEventTime =  assemblaggioTerminato+1;
					System.out.println("[" + fedAmbassador.federateTime + "] Impianto A essendo occupato rischedula l'evento al tempo " + nextEventTime);
					addEvent(new EventoLocaleA(EventType.RECEIVE_REPORT_M, nextEventTime, e.getReport()));
				}
				break;
		}		
	}
	
	/**
	 * @return numero casuale con distribuzione esponenziale
	 */
	public float ExponentialDistribution(double media, double varianza) {
	    double lambda = 1.0 / media; // Calcolo del tasso di decrescita
	    Random random = new Random();
	    double U = random.nextDouble(); // Genera un numero double casuale tra 0 e 1 (estremi inclusi)
	    double durata = -Math.log(U) / lambda; // Applica la formula dell'esponenziale inversa
	    // Adatta la distribuzione in base alla varianza desiderata
	    durata = durata * Math.sqrt(varianza);
	    return (float)durata;
	}
	
	private void sendRemoteEvent(EventoRemotoA e) {
		//l'evento remoto viene modellato come un'interazione
		try {			
			DataElementFactory factory = new DataElementFactory(){
			    public DataElement createElement(int index)
			    {
			       return encoderFactory.createHLAunicodeString();
			    }
			 };			
			ParameterHandleValueMap parameters = rtiAmb.getParameterHandleValueMapFactory().create(4);			
			//encoding array
			HLAvariableArray startingRequestEncoder = encoderFactory.createHLAvariableArray(factory);
			//scorre ogni elemento e fa l'encoding di ognuno in un tipo di HLA e lo inserisce nell'array
			for(String s: listaPezzi) {
				HLAunicodeString pezzoEncoder = encoderFactory.createHLAunicodeString();
				pezzoEncoder.setValue(s);
				startingRequestEncoder.addElement(pezzoEncoder);
			}
			//encoding del tipo dell'evento
			HLAunicodeString typeEncoder = encoderFactory.createHLAunicodeString();
			typeEncoder.setValue(e.getEventType().name());
			//la lista dei pezzi e il tipo dell'evento vengono aggiunti ai parametri dell'interazione
			parameters.put(this.startingRequestHandle, startingRequestEncoder.toByteArray());
			parameters.put(this.typeHandleA, typeEncoder.toByteArray());
            //tempo dell'evento
			HLAinteger64Time time = timeFactory.makeTime(e.getTime());
			//viene inviata l'interazione
			rtiAmb.sendInteraction(icRemoteEventAHandle, parameters, null, time);
			System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Evento Remoto contentenente la produzione di pezzi richiesta " + e.getListaPezzi() + " " + e.getEventType() );
		} catch(Exception ex) {ex.printStackTrace();}
	}
	
	
    //ImpiantoA lascia la federazione e se ImpiantoM ha già lasciato la federazione questa viene distrutta 
	private void leaveSimulationExecution() {
		
		//---------- 10 Simulation Main Loop ---------------
		
		try {
			rtiAmb.resignFederationExecution( ResignAction.DELETE_OBJECTS );
		} 
		catch(FederateOwnsAttributes ignored) {}
		
		catch (InvalidResignAction | OwnershipAcquisitionPending | FederateNotExecutionMember
				| NotConnected | CallNotAllowedFromWithinCallback | RTIinternalError e) {
			
			e.printStackTrace();
		}
		System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Resigned from Federation");

		try
		{
			rtiAmb.destroyFederationExecution(this.FEDERATION_NAME);
		}
		catch(FederatesCurrentlyJoined ej) {
			
			System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": did not destroy federation, federates still joined");
		}
		catch( FederationExecutionDoesNotExist  | NotConnected | RTIinternalError e )
		{
			e.printStackTrace();
		}
	}
	
	//stampa il report finale
	private void displayFederateState() {
		System.out.println("Report finale creato da ImpiantoA:");
		System.out.println("Costo totale veicoli termici (incluso motore): " + this.report.get(0) );
		System.out.println("Costo totale veicoli elettrici (incluso motore): " + this.report.get(1) );
		System.out.println("Tempo medio di produzione veicoli elettrici: " + this.report.get(2) );
		System.out.println("Tempo medio di produzione veicoli termici: " + this.report.get(3));
		
	}
}
