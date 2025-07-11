import java.util.LinkedList;
import java.util.Queue;

public class ExponentialAveraging {

	// Job queue: New jobs arrive here
	Queue<Process> jobQueue;

	// Ready queue: Processes ready for CPU
	Queue<Process> readyQueue;

	// Device list: IO processes ka infinite list (kyunki multiple I/O devices ho sakti hain)
	LinkedList<Process> deviceList;

	// Degree of multiprogramming (simultaneous processes ka limit)
	int degreeOfMP;

	// Alpha value for exponential averaging (SJF ka estimation factor)
	double alpha;

	// Temporary copy of job queue for backup ya analysis
	LinkedList<Process> tempList;

	// Constructor: Initial setup for all queues and alpha value
	public ExponentialAveraging(Queue<Process> jobQueue, Queue<Process> readyQueue, LinkedList<Process> deviceList,
			int degreeOfMP, double alpha, int contix) {
		super();
		this.jobQueue = jobQueue;
		this.readyQueue = readyQueue;
		this.deviceList = deviceList;
		this.degreeOfMP = degreeOfMP;

		// Temporary list ko copy karna for safe side
		tempList = new LinkedList<Process>();
		for (Process p : jobQueue) {
			tempList.add(p);
			p.alpha = alpha;
		}
	}

	// Ye function pura scheduling ka simulation chalayega
	public void applyExpAverage() {
		int currentTime = 0;
		int capicity = degreeOfMP;

		Process runningState = null; // Abhi ka running process

		int totalTimeInTheSystem = 0; // System me total time sabka
		int totalTimeInTheReadyQueue = 0; // Ready queue me total waiting time
		int timeRunningStateIsNotWorking = 0; // Jab CPU idle tha

		int numberOfProcessesDoneSoFar = 0; // Total completed processes
		int prevTime = 0;

		boolean displayInfo = false;

		Process prevRunningState = null; // Pichla process (for tracking)

		while (true) {
			// Job queue se processes ko ready queue me shift karna (agar unka arrival time aa gaya ho)
			int temp = 0;
			for (int i = 0; i < capicity; i++) {
				if (jobQueue.size() != 0) {
					if (jobQueue.peek().arrivalTime <= currentTime) {
						readyQueue.add(jobQueue.poll());
						temp++;
					}
				}
			}
			capicity -= temp;

			// Sab queues me time badhaana (for stats)
			for (Process p : jobQueue) {
				p.timeInTheSystem++;
				totalTimeInTheSystem++;
			}
			for (Process p : readyQueue) {
				p.timeInTheSystem++;
				p.timeInTheReadyQueue++;
				totalTimeInTheSystem++;
				totalTimeInTheReadyQueue++;
			}
			for (Process p : deviceList) {
				p.timeInTheSystem++;
				totalTimeInTheSystem++;
			}
			if (runningState != null) {
				runningState.timeInTheSystem++;
				totalTimeInTheSystem++;
			}

			// Jab CPU khali ho toh ready queue se process uthao
			if (runningState == null) {
				if (readyQueue.size() != 0) {
					prevTime = currentTime;
					runningState = readyQueue.poll();
				} else {
					timeRunningStateIsNotWorking++;
					System.out.println(
							"System idle hai ya koi process nahi aaya ab tak. (Ya dono bhi ho sakte hain)");
				}
			}

			// CPU ka burst khatam hone ka check
			if (runningState != null && !runningState.isCPU()) {
				if (runningState.currentBurst >= runningState.content.length) {
					System.out.print("At time unit : " + currentTime);
					System.out.println("  ||   process (" + runningState.pID + ")" + "  CPU burst done");
					displayInfo = true;
					System.out.println("Process complete ho gaya : " + runningState.pID);
					numberOfProcessesDoneSoFar++;
					capicity++;
					if (readyQueue.size() != 0) {
						prevRunningState = runningState;
						runningState = readyQueue.poll();
					} else {
						prevRunningState = runningState;
						runningState = null;
					}

				} else {
					System.out.print("At time unit : " + currentTime);
					System.out.println("  ||   process (" + runningState.pID + ")" + "  CPU burst done");
					if (!runningState.isCPU()) {
						displayInfo = true;
						deviceList.add(runningState);
						if (readyQueue.size() != 0) {
							prevRunningState = runningState;
							runningState = readyQueue.poll();
						} else {
							prevRunningState = runningState;
							runningState = null;
						}
					} else {
						System.out.println("Ye line kabhi print nahi hogi! :D");
						readyQueue.add(runningState);
						prevRunningState = runningState;
						runningState = readyQueue.poll();
					}
				}
			}

			// CPU burst time ko kam karna (work progress)
			if (runningState != null)
				runningState.decrement();

			currentTime++;

			// Device list (I/O) ke liye kaam
			for (int i = 0; i < deviceList.size(); i++) {
				deviceList.get(i).decrementFCFS();
				if (deviceList.get(i).getCurrentBurst().timeNeeded == 0) {
					System.out.print("At time unit : " + currentTime);
					System.out.println("  ||   process (" + deviceList.get(i).pID + ")" + "  IO burst done");
					deviceList.get(i).currentBurst++;
					deviceList.get(i).arrivalTime = currentTime;
					readyQueue.add(deviceList.get(i));
					deviceList.remove(i);
					i--;
				}
			}

			// Info display (jab bhi process change hota hai)
			if (displayInfo) {
				System.out.println((prevTime) + "\t------>\t\t" + (currentTime - 1) + "\t::::\t"
						+ (prevRunningState != null ? "pID(" + prevRunningState.pID + ")" : "No process running"));
				prevTime = currentTime - 1;
				displayInfo = false;
			}

			// Simulation khatam karne ka condition
			if (runningState == null && deviceList.isEmpty() && readyQueue.isEmpty() && jobQueue.isEmpty()) {
				break;
			}
		}
	}
}
