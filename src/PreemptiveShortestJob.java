import java.util.LinkedList;
import java.util.Queue;

public class PreemptiveShortestJob {

	Queue<Process> jobQueue;
	Queue<Process> readyQueue;
	LinkedList<Process> ioQueue;
	int multiprogrammingLimit;
	LinkedList<Process> backupProcesses;

	public PreemptiveShortestJob(Queue<Process> jobQueue, Queue<Process> readyQueue,
			LinkedList<Process> ioQueue, int multiprogrammingLimit) {
		this.jobQueue = jobQueue;
		this.readyQueue = readyQueue;
		this.ioQueue = ioQueue;
		this.multiprogrammingLimit = multiprogrammingLimit;

		backupProcesses = new LinkedList<>();
		for (Process p : jobQueue) {
			backupProcesses.add(p);
		}
	}

	public void startSRTF() {
		int clock = 0;
		int availableSlots = multiprogrammingLimit;

		Process activeProcess = null;

		int totalTimeInSystem = 0;
		int totalWaitTime = 0;
		int cpuIdleTime = 0;
		int completedProcessCount = 0;
		int lastSwitchTime = 0;

		boolean needLog = false;

		Process previousProcess = null;

		while (true) {
			int admitted = 0;
			for (int i = 0; i < availableSlots; i++) {
				if (!jobQueue.isEmpty() && jobQueue.peek().arrivalTime <= clock) {
					readyQueue.add(jobQueue.poll());
					admitted++;
				}
			}
			availableSlots -= admitted;

			for (Process p : jobQueue) {
				p.timeInTheSystem++;
				totalTimeInSystem++;
			}
			for (Process p : readyQueue) {
				p.timeInTheSystem++;
				p.timeInTheReadyQueue++;
				totalTimeInSystem++;
				totalWaitTime++;
			}
			for (Process p : ioQueue) {
				p.timeInTheSystem++;
				totalTimeInSystem++;
			}

			if (activeProcess != null) {
				activeProcess.timeInTheSystem++;
				totalTimeInSystem++;
			}

			if (activeProcess == null) {
				if (!readyQueue.isEmpty()) {
					lastSwitchTime = clock;
					activeProcess = readyQueue.poll();
				} else {
					cpuIdleTime++;
					System.out.println("CPU Idle... No process ready or all are in IO.");
				}
			}

			if (activeProcess != null && !activeProcess.isCPU()) {
				if (activeProcess.currentBurst >= activeProcess.content.length) {
					System.out.print("At time " + clock);
					System.out.println(" || Process (" + activeProcess.pID + ") completed its CPU tasks");
					needLog = true;
					System.out.println("Process " + activeProcess.pID + " has finished execution.");
					completedProcessCount++;
					availableSlots++;
					if (!readyQueue.isEmpty()) {
						previousProcess = activeProcess;
						activeProcess = readyQueue.poll();
					} else {
						previousProcess = activeProcess;
						activeProcess = null;
					}
				} else {
					System.out.print("At time " + clock);
					System.out.println(" || Process (" + activeProcess.pID + ") completed current CPU burst");
					if (!activeProcess.isCPU()) {
						needLog = true;
						ioQueue.add(activeProcess);
						if (!readyQueue.isEmpty()) {
							previousProcess = activeProcess;
							activeProcess = readyQueue.poll();
						} else {
							previousProcess = activeProcess;
							activeProcess = null;
						}
					} else {
						System.out.println("Unexpected case! This should never print.");
						readyQueue.add(activeProcess);
						previousProcess = activeProcess;
						activeProcess = readyQueue.poll();
					}
				}
			}

			if (!readyQueue.isEmpty() && activeProcess != null
					&& readyQueue.peek().getTimeOfCurrentCPUBurst() < activeProcess.getTimeOfCurrentCPUBurst()) {
				System.out.println("Preemption Occurred! Shorter job arrived. Switching process.");
				previousProcess = activeProcess;
				needLog = true;
				activeProcess.arrivalTime = clock;
				readyQueue.add(activeProcess);
				activeProcess = readyQueue.poll();
			}

			if (activeProcess != null) {
				activeProcess.decrement();
			}

			clock++;

			for (int i = 0; i < ioQueue.size(); i++) {
				ioQueue.get(i).decrementFCFS();
				if (ioQueue.get(i).getCurrentBurst().timeNeeded == 0) {
					System.out.print("At time " + clock);
					System.out.println(" || Process (" + ioQueue.get(i).pID + ") completed IO burst");
					ioQueue.get(i).currentBurst++;
					ioQueue.get(i).arrivalTime = clock;
					readyQueue.add(ioQueue.get(i));
					ioQueue.remove(i);
					i--;
				}
			}

			if (needLog) {
				System.out.println(lastSwitchTime + " --> " + (clock - 1) + " :: Process "
						+ (previousProcess != null ? "pID(" + previousProcess.pID + ")" : "None running"));
				lastSwitchTime = clock - 1;
				needLog = false;
			}

			if (activeProcess == null && ioQueue.isEmpty() && readyQueue.isEmpty() && jobQueue.isEmpty()) {
				break;
			}
		}
	}
}
