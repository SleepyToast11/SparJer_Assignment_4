package FOO;

import java.util.Comparator;

/**
 * implementable event class with comparable type for it
 */
public abstract class Event implements Comparable<Event> {

	/**
	 * Time at which the event will be processed
	 */
	int timeOfProcessing;

	/**
	 * Method to be implemented which will determine its functioning
	 */
	public abstract void process();

	/**
	 * Gets and return private variable timeOfProcessing
	 * @return Time at which event will be processed
	 * */
	public int getTimeOfProcessing() {
		return this.timeOfProcessing;
	}

	/**
	 * Compare method to determine order in priority queue
	 * @param event other event to compare to
	 * @return 0 if timeOfProcessing are equal, -1 if the event is less than the parameter else returns 1
	 */
	public int compareTo(Event event) {
		if (this.timeOfProcessing == event.timeOfProcessing)
			return 0;
		else if (this.timeOfProcessing < event.timeOfProcessing)
			return -1;
		else
			return 1;
	}
}
