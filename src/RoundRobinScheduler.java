import java.util.LinkedList;
import java.util.Queue;

public class RoundRobinScheduler {

	Queue<Process> jobQueue;
	Queue<Process> readyQueue;
	LinkedList<Process> deviceQueue;
	int multiProgramDegree;
	LinkedList<Process> backupList;

	public RoundRobinScheduler(Queue<Process> jobQueue, Queue<Process> readyQueue, LinkedList<Process> deviceQueue,
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

	public void runRoundRobin(int timeSlice) {
		int clock = 0;
		int availableSlots = multiProgramDegree;
		Process currentProcess = null;

		int totalSystemTime = 0;
		int totalWaitTime = 0;
		int idleCpuTime = 0;
		int completedProcesses = 0;
		int previousTime = 0;

		int quantumCounter = 0;
		boolean logExecution = false;
		Process lastProcess = null;

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
			System.out.println("____");
			for (Process p : readyQueue) {
				p.timeInTheSystem++;
				p.timeInTheReadyQueue++;
				totalSystemTime++;
				totalWaitTime++;
				System.out.println(p.arrivalTime + "::: PID->" + p.pID);
			}
			System.out.println("____");
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
					quantumCounter = 0;
					previousTime = clock;
					currentProcess = readyQueue.poll();
				} else {
					idleCpuTime++;
					System.out.println("No processes available to run. CPU idle.");
				}
			}

			if (currentProcess != null && currentProcess.getCurrentBurst().timeNeeded == 0) {
				currentProcess.currentBurst++;
				quantumCounter = 0;
				if (currentProcess.currentBurst >= currentProcess.content.length) {
					System.out.print("At time unit : " + clock);
					System.out.println("  ||   Process (" + currentProcess.pID + ") finished CPU burst");
					logExecution = true;
					System.out.println("Process " + currentProcess.pID + " has completed all tasks.");
					completedProcesses++;
					availableSlots++;
					if (!readyQueue.isEmpty()) {
						lastProcess = currentProcess;
						currentProcess = readyQueue.poll();
					} else {
						lastProcess = currentProcess;
						currentProcess = null;
					}
				} else {
					System.out.print("At time unit : " + clock);
					System.out.println("  ||   Process (" + currentProcess.pID + ") finished CPU burst");
					if (!currentProcess.isCPU()) {
						logExecution = true;
						deviceQueue.add(currentProcess);
						if (!readyQueue.isEmpty()) {
							lastProcess = currentProcess;
							currentProcess = readyQueue.poll();
						} else {
							lastProcess = currentProcess;
							currentProcess = null;
						}
					} else {
						lastProcess = currentProcess;
						logExecution = true;
						currentProcess.arrivalTime = clock;
						readyQueue.add(currentProcess);
						currentProcess = readyQueue.poll();
					}
				}
			} else if (currentProcess != null && quantumCounter % timeSlice == 0 && quantumCounter != 0) {
				System.out.println("Time quantum expired! Process switching now.");
				lastProcess = currentProcess;
				logExecution = true;

				currentProcess.arrivalTime = clock;
				readyQueue.add(currentProcess);
				currentProcess = readyQueue.poll();
			}

			if (currentProcess != null && currentProcess.getCurrentBurst().timeNeeded != 0) {
				currentProcess.decrementFCFS();
			}

			clock++;
			quantumCounter++;

			for (int i = 0; i < deviceQueue.size(); i++) {
				deviceQueue.get(i).decrementFCFS();
				if (deviceQueue.get(i).getCurrentBurst().timeNeeded == 0) {
					System.out.print("At time unit : " + clock);
					System.out.println("  ||   Process (" + deviceQueue.get(i).pID + ") finished IO burst");
					deviceQueue.get(i).currentBurst++;
					deviceQueue.get(i).arrivalTime = clock;
					readyQueue.add(deviceQueue.get(i));
					deviceQueue.remove(i);
					i--;
				}
			}

			if (logExecution) {
				System.out.println((previousTime) + " -----> " + (clock - 1) + " :: Process "
						+ (lastProcess != null ? "pID(" + lastProcess.pID + ")" : "None"));
				previousTime = clock - 1;
				logExecution = false;
			}

			if (currentProcess == null && deviceQueue.isEmpty() && readyQueue.isEmpty() && jobQueue.isEmpty()) {
				break;
			}
		}
	}
}
