import java.util.Comparator;

public class ProcessArrivalSorter implements Comparator<Object> {

	@Override
	public int compare(Object first, Object second) {
		Process processOne = (Process) first;
		Process processTwo = (Process) second;
		return Integer.compare(processOne.arrivalTime, processTwo.arrivalTime);
	}
}
