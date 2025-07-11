import java.util.LinkedList;
import java.util.Queue;

public class PriorityScheduling {

	Queue<Process> pendingProcesses; // Jobs waiting to be scheduled
	Queue<Process> readyProcesses;   // Jobs ready for CPU
	LinkedList<Process> ioDevices;   // IO queue (unlimited IOs assumed)
	int multiprogrammingLimit;

	LinkedList<Process> backupProcesses; // Copy of original jobs for safety

	public PriorityScheduling(Queue<Process> pendingProcesses, Queue<Process> readyProcesses,
			LinkedList<Process> ioDevices, int multiprogrammingLimit) {
		this.pendingProcesses = pendingProcesses;
		this.readyProcesses = readyProcesses;
		this.ioDevices = ioDevices;
		this.multiprogrammingLimit = multiprogrammingLimit;

		backupProcesses = new LinkedList<>();
		for (Process proc : pendingProcesses) {
			backupProcesses.add(proc);
		}
	}

	public void executePriorityScheduling() {
		int systemTime = 0;
		int activeCapacity = multiprogrammingLimit;

		Process activeProcess = null;

		int totalSystemTime = 0;
		int totalWaitingTime = 0;
		int idleTime = 0;
		int completedProcesses = 0;
		int previousTime = 0;

		boolean statusUpdateNeeded = false;
		Process lastActiveProcess = null;

		while (true) {
			// Admit new processes if their arrival time has come
			int newArrivals = 0;
			for (int i = 0; i < activeCapacity; i++) {
				if (!pendingProcesses.isEmpty() && pendingProcesses.peek().arrivalTime <= systemTime) {
					readyProcesses.add(pendingProcesses.poll());
					newArrivals++;
				}
			}
			activeCapacity -= newArrivals;

			// Time progresses for all queues
			for (Process proc : pendingProcesses) {
				proc.timeInTheSystem++;
				totalSystemTime++;
			}
			for (Process proc : readyProcesses) {
				proc.timeInTheSystem++;
				proc.timeInTheReadyQueue++;
				totalSystemTime++;
				totalWaitingTime++;
			}
			for (Process proc : ioDevices) {
				proc.timeInTheSystem++;
				totalSystemTime++;
			}
			if (activeProcess != null) {
				activeProcess.timeInTheSystem++;
				totalSystemTime++;
			}

			// Pick process if CPU is idle
			if (activeProcess == null && !readyProcesses.isEmpty()) {
				previousTime = systemTime;
				activeProcess = readyProcesses.poll();
			} else if (activeProcess == null) {
				idleTime++;
				System.out.println("System is idle or no processes have arrived yet.");
			}

			// CPU Burst Completion Check
			if (activeProcess != null && !activeProcess.isCPU()) {
				if (activeProcess.currentBurst >= activeProcess.content.length) {
					System.out.println("Time: " + systemTime + " | Process " + activeProcess.pID + " finished CPU burst.");
					statusUpdateNeeded = true;
					System.out.println("Process Completed: " + activeProcess.pID);
					completedProcesses++;
					activeCapacity++;
					if (!readyProcesses.isEmpty()) {
						lastActiveProcess = activeProcess;
						activeProcess = readyProcesses.poll();
					} else {
						lastActiveProcess = activeProcess;
						activeProcess = null;
					}
				} else {
					System.out.println("Time: " + systemTime + " | Process " + activeProcess.pID + " completed CPU burst.");
					statusUpdateNeeded = true;
					ioDevices.add(activeProcess);
					if (!readyProcesses.isEmpty()) {
						lastActiveProcess = activeProcess;
						activeProcess = readyProcesses.poll();
					} else {
						lastActiveProcess = activeProcess;
						activeProcess = null;
					}
				}
			}

			// Priority Preemption Check
			if (!readyProcesses.isEmpty() && activeProcess != null
					&& readyProcesses.peek().priority < activeProcess.priority) {
				System.out.println("Priority Preemption Happened! Process Switch at time " + systemTime);
				lastActiveProcess = activeProcess;
				statusUpdateNeeded = true;
				activeProcess.arrivalTime = systemTime;
				readyProcesses.add(activeProcess);
				activeProcess = readyProcesses.poll();
			}

			// Process executes
			if (activeProcess != null)
				activeProcess.decrement();

			systemTime++;

			// Handle IO Completion
			for (int i = 0; i < ioDevices.size(); i++) {
				ioDevices.get(i).decrementFCFS();
				if (ioDevices.get(i).getCurrentBurst().timeNeeded == 0) {
					System.out.println("Time: " + systemTime + " | IO completed for process "
							+ ioDevices.get(i).pID);
					ioDevices.get(i).currentBurst++;
					ioDevices.get(i).arrivalTime = systemTime;
					readyProcesses.add(ioDevices.get(i));
					ioDevices.remove(i);
					i--;
				}
			}

			// Show CPU usage status if required
			if (statusUpdateNeeded) {
				System.out.println(previousTime + " --> " + (systemTime - 1) + " | Process Run: "
						+ (lastActiveProcess != null ? "pID(" + lastActiveProcess.pID + ")" : "None"));
				previousTime = systemTime - 1;
				statusUpdateNeeded = false;
			}

			// Check for termination (no jobs anywhere)
			if (activeProcess == null && ioDevices.isEmpty() && readyProcesses.isEmpty()
					 && pendingProcesses.isEmpty()) {
				break;
			}
		}
	}
}
