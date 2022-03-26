package FOO;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

/**
 * Simulation class containing all pertinent events, queues and statistics
 */
public class BankSim {


	/**
	 * Average time between clients arrival
	 */
	private final double INTERARRIVAL_TIME = 120.0;

	/**
	 * Average time of process per transaction
	 */
	private final double AVG_TIME_PER_TRNSACTION = 60;

	/**
	 * variable keeping track of time, set when an event is processed at that event time
	 */
	private int time = 0;

	/**
	 * Statistic object to keep track the statistics for simulation
	 */
	private final Statistics stats = new Statistics();

	/**
	 * Priority queue containing event, with events being prioritized by smallest time of processing
	 */
	private final PriorityQueue<Event> eventQueue = new PriorityQueue<>();

	/**
	 * Queue used when clients arrive and reception desk is occupied
	 */
	private final Queue<Client> receptionQueue = new LinkedList<>();

	/**
	 * Queue used when clients arrive and teller desk is occupied
	 */
	private final Queue<Client> tellerQueue = new LinkedList<>();

	/**
	 * Desk holding client being served by reception
	 */
	private Client recepDest = null;

	/**
	 * variable to hold client on route to teller
	 */
	private Client transitToTeller;

	/**
	 * Desk holding client being served by teller
	 */
	private Client tellerDesk = null;

	/**
	 * bank simulation constructor
	 * */
	public BankSim() {
	}

	/**
	 * starts simulation and removes events from event queue while the time is less than
	 * simTime
	 * @param simTime Amount of time to run the simulation for
	 * */
	public void run(int simTime) {
		eventQueue.add(new RECEP_Arrival());
		while (simTime > getTime()) {
			eventQueue.remove().process();
		}
	}

	/**
	 * class of tools to generate random numbers
	 * */
	public static class RandBox extends Random {

		/**
		 * Generates a random value which is averages out overtime at mean
		 * @param mean mean of values to have random generated at
		 * @return Double random value with more closer to the mean
		 * */
		static double expo(double mean) {
			double x = Math.random();
			x = -mean * Math.log(x);
			return x;
		}
	}

	/**
	 * Client object which will go through the desks of the bank
	 * */
	public static class Client {

		/**
		 * Time of arrival of the client, mostly used for statistics sake
		 * */
		private final int arrivalTime;

		/**
		 * Random number of transaction to be performed by the desks at the bank
		 * */
		private final int numberOfTransaction;

		/**
		 * Gets and return arrival time
		 * @return private variable arrivalTime
		 * */
		public int getArrivalTime() {
			return arrivalTime;
		}

		/**
		 * Gets and return numberOfTransaction
		 * @return number of transactions to be performed
		 * */
		public int getNumberOfTransaction() {
			return numberOfTransaction;
		}

		/**
		 * Constructor setting the arrival time at the current time
		 * and generating a random number from 1 to 100 as the amount of
		 * transactions
		 * @param arrivalTime Time at which client arrives at the bank
		 * */
		public Client(int arrivalTime) {
			this.arrivalTime = arrivalTime;
			Random rand = new Random();
			numberOfTransaction = rand.nextInt(100) + 1;
		}
	}

	/**
	 * Gets and returns current time of the simulation
	 * @return int number of the current in the simulation
	 * */
	private int getTime() {
		return time;
	}

	/**
	 * Sets time of simulation at provided time
	 * @param time int number of the new time to be set
	 * */
	private void setTime(int time) {
		this.time = time;
	}



	/**
	 * Event Class representing arrival of a client to the bank
	 */
	private class RECEP_Arrival extends Event {

		/**
		 * constructs an event to be processed at current time + random amount added
		 */
		public RECEP_Arrival() {
			timeOfProcessing = (int) (getTime() + RandBox.expo(INTERARRIVAL_TIME));
		}

		/**
		 * when called, will set time of simulation to the time of processing,
		 * create a new client and send it to the desk or queue if desk is not free
		 * and create next arrival at reception
		 */
		@Override
		public void process() {

			setTime(getTimeOfProcessing());
			Client client = new Client(getTime());

			if (recepDest == null) {
				recepDest = client;
				eventQueue.add(new RECEP_Departure());
			} else
				receptionQueue.add(client);

			eventQueue.add(new RECEP_Arrival());
		}

	}


	/**
	 * Event class representing a departure of a client to the teller desk
	 */
	private class RECEP_Departure extends Event {

		/**
		 * constructs an event to be processed at amount of
		 * transaction client at the desk has to do times the average time per transaction
		 */
		public RECEP_Departure() {
			timeOfProcessing = getTime() +
					(int) (recepDest.getNumberOfTransaction() * AVG_TIME_PER_TRNSACTION);
		}

		/**
		 * When called, will set time of simulation to the time of processing
		 * set transit to teller with the client at reception desk. If queue is not empty,
		 * will get a client out and create new reception departure event.
		 */
		@Override
		public void process() {
			setTime(getTimeOfProcessing());
			transitToTeller = recepDest;
			eventQueue.add(new TELLER_Arrival());
			recepDest = null;

			if (!receptionQueue.isEmpty()) {
				recepDest = receptionQueue.remove();
				eventQueue.add(new RECEP_Departure());
			}
		}
	}



	/**
	 * Event class representing a client arriving to teller, happens right after its departure
	 */
	private class TELLER_Arrival extends Event {

		/**
		 * takes the client from transit and sets the transit to null.
		 * If desk is empty, sets client to it and creates new teller departure event.
		 * Else, add it to the queue
		 */
		@Override
		public void process() {
			Client client = transitToTeller;
			transitToTeller = null;

			if (tellerDesk == null) {
				tellerDesk = client;
				eventQueue.add(new TELLER_Departure());
			} else
				tellerQueue.add(client);
		}

		/**
		 * Constructor setting time of processing to current time, making it the next event in line
		 */
		public TELLER_Arrival() {
			timeOfProcessing = getTime();
		}
	}



	/**
	 * Event class representing the departure of a client from teller desk
	 */
	private class TELLER_Departure extends Event {

		/**
		 * When called, sets time of simulation at time of processing, calls
		 * statistics to log stats and sets teller desk to null.
		 * Ff queue is not empty, take the client at front the queue and sets
		 * it to the desk and creates next departure for it.
		 */
		@Override
		public void process() {
			setTime(getTimeOfProcessing());
			stats.addStats();
			tellerDesk = null;
			if (!tellerQueue.isEmpty()) {
				tellerDesk = tellerQueue.remove();
				eventQueue.add(new TELLER_Departure());
			}

		}

		/**
		 * constructs an event to be processed at amount of
		 * transaction client at the desk has to do times the average time per transaction
		 */
		public TELLER_Departure() {
			timeOfProcessing = (int) (getTime() +
					tellerDesk.getNumberOfTransaction() * AVG_TIME_PER_TRNSACTION);
		}
	}


	/**
	 * Prints number of clients, transaction and average time passed in bank
	 */
	public void printStat() {
		stats.printStats();
	}


	/**
	 * Class containing relevant statistics of object simulation
	 */
	private class Statistics {

		/**
		 * Total amount of transaction performed since start of simulation
		 */
		private int totalTransaction;

		/**
		 * Total amount of time clients have spent in the bank
		 */
		private int totalTime;

		/**
		 * Total amount of clients that were served
		 */
		private int totalClient;

		/**
		 * Constructor creating statistics object for the simulation
		 */
		private Statistics() {
		}


		/**
		 * Gets and return private variable TotalClient
		 * @return int of number of client served up to this point in simulation
		 * */
		public int getTotalClient() {
			return totalClient;
		}

		/**
		 * Gets and return variable totalTransaction
		 * @return Number of transactions performed
		 * */
		public int getTotalTransaction() {
			return totalTransaction;
		}

		/**
		 * Adds 1 to the totClientCount counter
		 * */
		private void addTotClientCount() {
			totalClient += 1;
		}

		/**
		 * Gets and return variable totalTime
		 * @return Total amount of time clients spent in the bank
		 * */
		public int getTotalTime() {
			return totalTime;
		}

		/**
		 * Adds number of transaction of client to total counter
		 * @param amount Amount of transaction to add to total counter
		 */
		private void addToTotTrans(int amount) {
			this.totalTransaction += amount;
		}

		/**
		 * Calculates and measures average amount of time per client spent at the bank
		 * @return average amount of time clients spent at the bank
		 */
		public double getAverageTime() {
			return getTotalTime() / (double) getTotalClient();
		}

		/**
		 * Adds amount of time client spent in the bank to total time counter
		 * @param amount Int of time spent by client in the bank
		 */
		private void addTotTime(int amount) {
			totalTime += amount;
		}

		/**
		 * Takes client at teller desk and adds its statistics to the total counters
		 */
		public void addStats() {
			Client client = tellerDesk;
			addTotClientCount();
			addToTotTrans(client.getNumberOfTransaction());
			addTotTime(getTime() - client.getArrivalTime());
		}

		/**
		 * Prints number of clients, transaction and average time passed in bank
		 */
		public void printStats() {
			System.out.println("Number of clients: " + getTotalClient());
			System.out.println("Number of transaction" + getTotalTransaction());
			System.out.println("average time passed in bank: " + getAverageTime());
		}

	}
}

