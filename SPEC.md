# COMMUNICATION CHANNELS
## Specification
We have 3 main classes: Broker, Channel and Task

### Channel
The channel is how multiple tasks can communicate between them.
It contains functions that allow a Task to read and write in the channel. It is also able to disconnect and give its status as well. A channel can have more than 1 task that writes to the channel. That's why write should be waited, because other wise the messages will be combined resulting in incomprensible messages.

#### Methods:
- int read(byte[] bytes, int offset, int length):
  - Reads data from the channel into the provided byte array.
  - **Parameters:**
    - bytes: The buffer into which the data will be read.
    - offset: The start position in the buffer at which to store the data.
    - length: The maximum number of bytes to read.

- int write(byte[] bytes, int offset, int length):
  - Writes data from the provided byte array into the channel.
  - **Parameters:**
    - bytes: The buffer containing the data to write.
    - offset: The start position in the buffer from which to read the data.
    - length: The number of bytes to write to the channel.

- void disconnect():
  - Closes the channel and terminates the connection.

- boolean disconnected():
  - Checks whether the channel is currently disconnected.



### Broker
In order for a task to be connected to the channel, the broker is needed. It serves as a middle man, that accepts a port in order to see if it is available as well as connecting the Task to a specific channel in a specific port. 

#### Constructor:
- Broker(String name): 
  - Initializes the Broker with the given name.
  - **Parameters:**
    - name: The name of the broker (used to identify it for connection purposes).

#### Methods:
- Channel accept(int port): 
  - Waits for an incoming connection on the specified port.
  - **Parameters:**
    - port: The port number to listen on for connections.
  - **Returns:**
    - A Channel object representing the established communication channel.

- Channel connect(String name, int port): 
  - Initiates a connection to another broker, with the given name, on the given port.
  - **Parameters:**
    - name: The name of the remote broker to connect to.
    - port: The port number on which the remote broker is listening.
  - **Returns:**
    - A Channel object representing the communication channel with the connected broker.



### Task
It represents a task running in its own thread, and it interacts with a Broker. Each task performs an action provided as a Runnable.

#### Constructor:
- Task(Broker b, Runnable r):
  - Initializes the Task with the specified Broker and the action to be performed.

