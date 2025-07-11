The main objective of this project is to simulate and analyze the behavior of classic CPU scheduling algorithms under different operating conditions. It allows users to dynamically generate workloads, select scheduling policies, and observe their impact on system performance metrics.

This simulator is built with an intuitive user interface to make it suitable for both academic learning and experimental analysis.

This project is a simulation of OS process scheduling techniques, implemented with key DSA structures like Queues, LinkedLists, and Comparators. It covers CPU Scheduling Algorithms (RR, Priority, SJF, SRTF), Process Handling, and IO Burst Simulation. It allowed me to practically apply OS concepts along with core Java and DSA.”



 ##Key Features
 1. Job Stream Generator:
Randomized, configurable job stream generator allowing fine-grained control over:

Number of processes

Degree of multi-programming

CPU and I/O burst patterns

Arrival times, priorities, context switch durations, Tau, and Alpha values.

2. Algorithm Selection:
Supports six widely used scheduling algorithms:

First-Come, First-Served (FCFS)

Shortest Job First (SJF)

Shortest Remaining Time First (SRTF)

Priority Scheduling

Round Robin (RR) with customizable time quantum

Exponential Average-based SJF (SRJF with prediction)

Process Execution Visualization:

Time unit by time unit simulation for full transparency

Real-time updates on Job Queue, Ready Queue, and Device Queue

4. Two progress bars:

One showing the current burst of the active process

One representing total process progress

Full control over simulation flow:

Next Step: Progresses to the next time unit

Next Change: Jumps to the next system event (e.g., queue state change)

Animated Playback: Adjustable speed for automatic simulation

5. Detailed System Monitoring:

Live tracking of:

CPU Utilization

System Throughput

Turnaround Time

Waiting Time

Job-specific performance metrics available at every time unit.

6. Comprehensive Logging:

Change Log Window: Logs every system event per time unit

Burst Log Window: Tracks all process-specific changes during bursts

7. Educational Focus:

Designed to serve as a teaching and learning tool

Helps visualize underlying scheduling mechanics clearly


⚙️ Tech Stack
Programming Language: Java

User Interface: JavaFX or Swing (depending on actual codebase)

explains the concept of operating System ,  process managment and cover funadamental of Data Structure and algorithms
Also incudes the concept like Multi Threading and Multi programming


