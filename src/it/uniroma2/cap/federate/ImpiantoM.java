package it.uniroma2.cap.federate;

import it.uniroma2.cap.events.*;
import it.uniroma2.cap.scenario.Pezzo;

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
import hla.rti1516e.encoding.HLAfloat64BE;
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

public class ImpiantoM {

	//ImpiantoM proprietà
	private String nome; //codice impianto M	
	private SortedSet<EventoLocaleM> eventsList; //ordered list of events to be processed
	private ArrayList<String>  listaPezzi; //pezzi da costruire richiesti da ImpiantoA
	private ArrayList<Pezzo> pezziCostruiti; //pezzi costruiti
	private ArrayList<Float> reportM; //report finale da mandare a ImpiantoA
	private int numeroPezziCostruiti; //numero pezzi costruiti da ImpiantoM finora

	//simulation properties
	private int _seed;
	
	//HLA-related properties
	protected RTIambassador rtiAmb;
	protected final String FEDERATION_NAME = "CAP Simulation";
	protected FederateAmbassadorImplM fedAmbassador;
	protected String federateName;
	protected HLAinteger64TimeFactory timeFactory;
	protected EncoderFactory encoderFactory;
	
	// handles types	
	protected InteractionClassHandle icRemoteEventMHandle;
	protected InteractionClassHandle icRemoteEventAHandle;
	protected ParameterHandle startingRequestHandle;
	protected ParameterHandle timeHandle;
	protected ParameterHandle pezzoHandle;
	protected ParameterHandle reportHandle;
	protected ParameterHandle typeHandleA;
	protected ParameterHandle typeHandleM;
	
	public ImpiantoM(String nome, int seed) {
		this.nome = nome;
		eventsList = new TreeSet<EventoLocaleM>(); 
		federateName = "ImpiantoM";
		this._seed = seed;
		listaPezzi = new ArrayList<String>();
		pezziCostruiti = new ArrayList<Pezzo>();
		reportM = new ArrayList<Float>();
	}
    

	/*
	 * aggiunge evento alla lista degli eventi
	 */
	public void addEvent(EventoLocaleM e) {
		this.eventsList.add(e);
	}
	
	/*
	 * restituisce il prossimo evento nella lista
	 * 
	 */
	private EventoLocaleM getNextEvent() {
		return this.eventsList.first();
	}
	
	/**
	 * @return federation name
	 */
	public String getFederateName() {
		return federateName;
	}

	public void startFederate(String host) {
		EventoLocaleM event;        // evento da processare
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
					//processa l'evento
					System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Time Advance Grant");					
					event = getNextEvent();					
					//process event
					process(event);
					//rimuove l'evento dalla lista degli eventi da processare
					eventsList.remove(event);
				}
				else if (numeroPezziCostruiti == listaPezzi.size() && listaPezzi.size() > 0) {	
					//creazione del report
					createReport();
					//invia report a ImpiantoA e termina la simulazione
					nextTime = currentTime + (long)pezziCostruiti.get(listaPezzi.size()-1).getDurata()+1;
					EventoRemotoM e= new EventoRemotoM(EventType.SEND_REPORT, nextTime+1, reportM);
					sendRemoteEvent(e);
					break;
				}
				else{					
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
			
			fedAmbassador = new FederateAmbassadorImplM(this);
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
	      //ImpiantoM attende che ImpiantoA si unisca alla federazione
	        FederateHandle f=null;
	        
	        while(f==null) {
        		try {
        			
        			f = rtiAmb.getFederateHandle("ImpiantoA");
        			
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
			timeFactory = (HLAinteger64TimeFactory)rtiAmb.getTimeFactory();
			//ImpiantoM è time-constrained e time-regulating
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
		   
			//pubblica l'interazione Evento Remoto M e sottoscrive l'interazione Evento Remoto A
			rtiAmb.publishInteractionClass(icRemoteEventMHandle);
			rtiAmb.subscribeInteractionClass(icRemoteEventAHandle);
			
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
	
	private void process(EventoLocaleM event) {
		System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Processing Event " + event.getEventType());
 
		long currentTime = fedAmbassador.getFederateTime();		//tempo logico corrente del federato
		long nextEventTime; 	                                //tempo per schedulare il prossimo evento 	
		Pezzo pezzo;                                            //pezzo da costruire 
		
		switch(event.getEventType()) {		
		case PRODUCTION_COMPONENT_M:
			//pezzo da costruire
			pezzo = event.getPezzo();
			System.out.println("[" + fedAmbassador.federateTime + "] Impianto M inizia la produzione di " + pezzo.getPezzo() + " al tempo " + currentTime);
			//tempo in cui ImpiantoM terminerà la costruzione del pezzo
			nextEventTime = currentTime + (long)pezzo.getDurata();
			//il pezzo costruito viene aggiunto alla lista dei pezzi costruiti
			pezziCostruiti.add(pezzo);
			//contatore per tenere traccia dei pezzi costruiti finora
			numeroPezziCostruiti ++;
			//viene inviato l'evento remoto DELIVERY COMPONENT contenente il pezzo appena prodotto
			System.out.println("[" + fedAmbassador.federateTime + "] Impianto M ha terminato la produzione di " + pezzo.getPezzo() + " al tempo " + nextEventTime);
			EventoRemotoM eventoRemotoM = new EventoRemotoM(EventType.DELIVERY_COMPONENT, nextEventTime, pezzo.getPezzo());
			sendRemoteEvent(eventoRemotoM);				
			break;
				
		case STARTING_REQUEST:
			//lista dei pezzi da costruire ricevuta da ImpiantoA
			listaPezzi = event.getListaPezzi();
			//il primo evento PRODUCTION COMPONENT verrà schedulato al tempo currentTime +1
			nextEventTime = currentTime +1;
			/*
			* per ogni pezzo nella lista viene aggiunto un evento PRODUCTION COMPONENT alla lista degli eventi 
			* che deve essere schedulato quando ImpiantoM ha terminato la costruzione del pezzo precedente
			*/
			for(String p : listaPezzi) {
				//se il pezzo è di tipo M1 avrà un durata aleatoria esponenziale di media 60 e varianza 5 e un costo fisso di 7000
				if( p.equals("M1")){
					Pezzo pezzoM1 = new Pezzo("M1", ExponentialDistribution(60,5)+1, 7000);
					System.out.println("[" + fedAmbassador.federateTime + "] aggiunto pezzo M1 a lista eventi");
					addEvent(new EventoLocaleM(EventType.PRODUCTION_COMPONENT_M, nextEventTime , pezzoM1));
					nextEventTime = currentTime + (long)pezzoM1.getDurata();
					
				//se il pezzo è di tipo M2 avrà un durata aleatoria esponenziale di media 40 e varianza 2 e un costo fisso di 4000		
				}else{
					Pezzo pezzoM2 = new Pezzo("M2", ExponentialDistribution(40,2)+1, 4000);
					System.out.println("[" + fedAmbassador.federateTime + "] aggiunto pezzo M2 a lista eventi");
					addEvent(new EventoLocaleM(EventType.PRODUCTION_COMPONENT_M, nextEventTime , pezzoM2));
					nextEventTime = currentTime + (long)pezzoM2.getDurata();						
				}					
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
	
	/**
	 * crea il report finale
	 */
	public void createReport() {
		float durataTotaleM1 = 0;
	    int costoTotaleM1 = 0;
	    int numeroPezziM1= 0;
	    float durataTotaleM2 = 0;
	    int costoTotaleM2 = 0;
	    int numeroPezziM2 = 0;
	    //calcola il tempo e il costo totale de pezzi di M1 e M2 costruiti
		for (Pezzo pezzo : pezziCostruiti) {
			if (pezzo.getPezzo().equals("M1")) {
				durataTotaleM1 += (long)pezzo.getDurata();
				costoTotaleM1 += pezzo.getCosto();
			    numeroPezziM1 += 1;
			}
			else {
				durataTotaleM2 += (long)pezzo.getDurata();
				costoTotaleM2 += pezzo.getCosto();
			    numeroPezziM2 += 1;
			}
		}
		//inserisce nel report il tempo medio di costruzione dei pezzi M1
		reportM.add(durataTotaleM1/numeroPezziM1);
		//inserisce nel report il tempo medio di costruzione dei pezzi M2
		reportM.add(durataTotaleM2/numeroPezziM2);
		//inserisce nel report il costo totale della produzione dei pezzi M1
		reportM.add((float)costoTotaleM1);
		//inserisce nel report il costo totale della produzione dei pezzi M2
		reportM.add((float)costoTotaleM2);
		
	}
	
	private void sendRemoteEvent(EventoRemotoM e) {
		//l'evento remoto viene modellato come un'interazione
		switch(e.getEventType()) {
		case DELIVERY_COMPONENT:
			try {
				ParameterHandleValueMap parameters = rtiAmb.getParameterHandleValueMapFactory().create(4);				
				//encoding del pezzo
				HLAunicodeString pezzoEncoder = encoderFactory.createHLAunicodeString();
				pezzoEncoder.setValue(e.getPezzo());
				//encoding del tipo dell'evento
				HLAunicodeString typeEncoder = encoderFactory.createHLAunicodeString();
				typeEncoder.setValue(e.getEventType().name());
				
				//il pezzo e il tipo dell'evento vengono aggiunti ai parametri dell'interazione
				parameters.put(this.pezzoHandle, pezzoEncoder.toByteArray());
				parameters.put(this.typeHandleM, typeEncoder.toByteArray());
				//tempo dell'evento
				HLAinteger64Time time = timeFactory.makeTime(e.getTime());
				//viene inviata l'interazione
				rtiAmb.sendInteraction(icRemoteEventMHandle, parameters, null, time);
				System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Evento Remoto M->A che consegna " + e.getPezzo() + ", tipo evento: "+ e.getEventType() );
			} catch(Exception ex) {ex.printStackTrace();}
			break;
			
		case SEND_REPORT:
			try {
				DataElementFactory factory = new DataElementFactory(){
				    public DataElement createElement(int index)
				    {
				       return encoderFactory.createHLAfloat64BE();
				    }
				 };				
				ParameterHandleValueMap parameters = rtiAmb.getParameterHandleValueMapFactory().create(4);				
				//encoding del report
				HLAvariableArray reportEncoder = encoderFactory.createHLAvariableArray(factory);
				//scorre ogni elemento e fa l'encoding di ognuno in un tipo di HLA e lo inserisce nell'array
				for(float s: reportM) {
					HLAfloat64BE datoEncoder = encoderFactory.createHLAfloat64BE();
					datoEncoder.setValue(s);
					reportEncoder.addElement(datoEncoder);
				}								
				//encoding del tipo dell'evento
				HLAunicodeString typeEncoder = encoderFactory.createHLAunicodeString();
				typeEncoder.setValue(e.getEventType().name());
				//il report e il tipo dell'evento vengono aggiunti ai parametri dell'interazione
				parameters.put(this.reportHandle, reportEncoder.toByteArray());
				parameters.put(this.typeHandleM, typeEncoder.toByteArray());
				//tempo dell'evento
				HLAinteger64Time time = timeFactory.makeTime(e.getTime());
				//viene inviata l'interazione				
				rtiAmb.sendInteraction(icRemoteEventMHandle, parameters, null, time);
				System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Evento Remoto M->A che consegna il report finale, tipo evento: "+ e.getEventType() );
			} catch(Exception ex) {ex.printStackTrace();}
			break;
		}
		
	}
	
	//ImpiantoM lascia la federazione e se ImpiantoA ha già lasciato la federazione questa viene distrutta 
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
		System.out.println("Report finale creato da ImpiantoM:" );
		System.out.println("Costo totale motori termici: " + this.reportM.get(3) );
		System.out.println("Costo totale motori elettrici: " + this.reportM.get(2) );
		System.out.println("Tempo medio di produzione motori elettrici: " + this.reportM.get(0) );
		System.out.println("Tempo medio di produzione motori termici: " + this.reportM.get(1));
		
	}
}
