Koo and Toueg's algorithm is used for achieving checkpointing and recovery in distributed systems. All the features of the original algorithm were implemented. The algorithm is implemented in Java.

Major design decisions:

A process/node in the distributed system is treated as a thread. The program is designed to allow any node to initiate an instance of checkpointing or recovery. Then node can run any instance asynchronously. However, atmost one instance of checkpointing or recovery can be in progress at a given time. But the approach to ensure that only one instance of checkpointing/recovery protocol is in progress at any time, is implemented as defined in the problem statement.
Processes use sockets to communicate with each other. Stream Sockets are used. Each process listens for messages on an unique address and port. Each process knows the addresses of all other processes. These addresses are hard coded in the program. No Name server was implemented to do this.
The sequence of checkpointing and recovery operations is simulated using a configuration file. But the logic in the program guarantees that the set of last permanent checkpoints form a consistent global state the protocol rolls back
the system to a consistent global state.          

Compile Process:

javac checkpoint.java 

Run:

java checkpoint n    n being the server number in config file

There is also launcher scripts which can be used launcher_server.sh which can be used to automate the run process.

The sequence will be given by a list of tuples; the first entry in a tuple will denote a node identifier and the second entry will denote an operation type (checkpointing
or recovery. For e.g (2,c) then your program should execute an instance of checkpointing protocol initiated by node 2, followed by an instance of recovery protocol initiated by node 1, and so on.



