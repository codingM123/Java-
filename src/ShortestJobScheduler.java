import java.util.LinkedList;
import java.util.Queue;

public class ShortestJobScheduler {

	Queue<Process> jobQueue;
	Queue<Process> readyQueue;
	LinkedList<Process> deviceQueue;
	int multiProgramDegree;
	LinkedList<Process> backupList;

	public ShortestJobScheduler(Queue<Process> jobQueue, Queue<Process> readyQueue, LinkedList<Process> deviceQueue,
			int multiProgramDegree) {
		this.jobQueue = jobQueue;
		this.readyQueue = readyQueue;
		this.deviceQueue = deviceQueue;
		this.multiProgramDegree = multiProgramDegree;

		backupList = new LinkedList<>();
		for (Process p : jobQueue) {
			backupList.add(p);
		}
	}

	public void runSJF() {
		int clock = 0;
		int availableSlots = multiProgramDegree;
		Process currentProcess = null;

		int totalSystemTime = 0;
		int totalWaitTime = 0;
		int cpuIdleTime = 0;
		int finishedProcessCount = 0;
		int lastRecordedTime = 0;

		boolean needToDisplay = false;
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
				totalSystemTime++;
			}
			for (Process p : readyQueue) {
				p.timeInTheSystem++;
				p.timeInTheReadyQueue++;
				totalSystemTime++;
				totalWaitTime++;
			}
			for (Process p : deviceQueue) {
				p.timeInTheSystem++;
				totalSystemTime++;
			}

			if (currentProcess != null) {
				currentProcess.timeInTheSystem++;
				totalSystemTime++;
			}

			if (currentProcess == null) {
				if (!readyQueue.isEmpty()) {
					lastRecordedTime = clock;
					currentProcess = readyQueue.poll();
				} else {
					cpuIdleTime++;
					System.out.println("CPU is waiting... No process available or all are in IO.");
				}
			}

			if (currentProcess != null && !currentProcess.isCPU()) {
				if (currentProcess.currentBurst >= currentProcess.content.length) {
					System.out.print("At time: " + clock);
					System.out.println(" || Process (" + currentProcess.pID + ") completed its CPU task");
					needToDisplay = true;
					System.out.println("Process " + currentProcess.pID + " fully completed.");
					finishedProcessCount++;
					availableSlots++;
					if (!readyQueue.isEmpty()) {
						previousProcess = currentProcess;
						currentProcess = readyQueue.poll();
					} else {
						previousProcess = currentProcess;
						currentProcess = null;
					}
				} else {
					System.out.print("At time: " + clock);
					System.out.println(" || Process (" + currentProcess.pID + ") completed its CPU task");
					if (!currentProcess.isCPU()) {
						needToDisplay = true;
						deviceQueue.add(currentProcess);
						if (!readyQueue.isEmpty()) {
							previousProcess = currentProcess;
							currentProcess = readyQueue.poll();
						} else {
							previousProcess = currentProcess;
							currentProcess = null;
						}
					} else {
						System.out.println("Unexpected case! Check logic.");
						readyQueue.add(currentProcess);
						previousProcess = currentProcess;
						currentProcess = readyQueue.poll();
					}
				}
			}

			if (currentProcess != null) {
				currentProcess.decrement();
			}

			clock++;

			for (int i = 0; i < deviceQueue.size(); i++) {
				deviceQueue.get(i).decrementFCFS();
				if (deviceQueue.get(i).getCurrentBurst().timeNeeded == 0) {
					System.out.print("At time: " + clock);
					System.out.println(" || Process (" + deviceQueue.get(i).pID + ") completed IO burst");
					deviceQueue.get(i).currentBurst++;
					deviceQueue.get(i).arrivalTime = clock;
					readyQueue.add(deviceQueue.get(i));
					deviceQueue.remove(i);
					i--;
				}
			}

			if (needToDisplay) {
				System.out.println(lastRecordedTime + " ---> " + (clock - 1) + " :: Process "
						+ (previousProcess != null ? "pID(" + previousProcess.pID + ")" : "None"));
				lastRecordedTime = clock - 1;
				needToDisplay = false;
			}

			if (currentProcess == null && deviceQueue.isEmpty() && readyQueue.isEmpty() && jobQueue.isEmpty()) {
				break;
			}
		}
	}
}
