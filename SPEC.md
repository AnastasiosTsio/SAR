# COMMUNICATION CHANNELS
## Specification
We have 3 main classes: Broker, Channel and Task

### Channel
The channel is how multiple tasks can communicate between them. It facilitates the transmission of data streams between tasks, ensuring orderly communication and preventing message overlap. It contains functions that allow a Task to read and write in the channel. It is also able to disconnect and give its status as well. It is important to note that channel is not multi-threaded. However, different threads can write/read in the channel, but one at a time. It is up to the developer to properly synchronize everything. Developers must use appropriate synchronization mechanisms (e.g., locks) to avoid race conditions during read and write operations. The channel does not provide internal synchronization.

#### Methods:
- int read(byte[] bytes, int offset, int length):
  - Reads data from the channel into the provided byte array. It is the responsibility of the developer to ensure that the bytes array has enough capacity to store the data being read.
  - **Parameters:**
    - bytes: The buffer into which the data will be read.
    - offset: The start position in the buffer at which to store the data.
    - length: The maximum number of bytes to read. If length exceeds the remaining capacity of the bytes array, only the bytes that fit will be read.
  - **Returns:**
    - the number of bytes actually read, or -1 if there was a problem. If an error occurs, appropriate error handling mechanisms (e.g., try-catch blocks) should be used to manage this scenario. 

- int write(byte[] bytes, int offset, int length):
  - Writes data from the provided byte array into the channel. The developer must ensure the integrity of data being written by checking the provided buffer size and offset validity.
  - **Parameters:**
    - bytes: The buffer containing the data to write.
    - offset: The start position in the buffer from which to read the data.
    - length: The number of bytes to write to the channel. If length exceeds the available data in the buffer, only the bytes that fit will be written.
  - **Returns:**
    - the number of bytes actually written, or -1 if there was a problem. Developers should handle the potential failure in writing data, implementing necessary retries or alternative logic as needed.

- void disconnect():
  - Closes the channel and terminates the connection. After calling this method, no further read or write operations should be performed. Attempting to read or write after disconnection may lead to undefined behavior.

- boolean disconnected():
  - Checks whether the channel is currently disconnected. Developers should use this method to verify the state of the channel before attempting read or write operations to avoid unnecessary errors.



### Broker
In order for a task to be connected to the channel, the broker is needed. It serves as a middle man, that accepts a port in order to see if it is available as well as connecting the Task to a specific channel in a specific port. The broker can be the same for multiple tasks. The Broker instance must be used responsibly to avoid connection issues, as improper handling can lead to resource exhaustion.

#### Constructor:
- Broker(String name): 
  - Initializes the Broker with the given name. Each name should be unique because we can have multiple brokers and we need a way to be able to differentiate the different brokers. Developers must ensure the uniqueness of the broker name to avoid conflicts in establishing connections.
  - **Parameters:**
    - name: The name of the broker (used to identify it for connection purposes).

#### Methods:
- Channel accept(int port): 
  - Waits for an incoming connection on the specified port. This method will block until a connection is established. Proper exception handling should be used to manage situations where the specified port is unavailable or an error occurs during the waiting process.
  - **Parameters:**
    - port: The port number to listen on for connections.
  - **Returns:**
    - A Channel object representing the established communication channel. Developers should ensure that the returned channel is correctly managed and eventually disconnected when no longer needed.

- Channel connect(String name, int port): 
  - Initiates a connection to another broker, with the given name, on the given port. This method may block until a connection is established. Ensure that the name and port correspond to a valid, listening broker to avoid connection failures. Use try-catch blocks to handle potential connection errors.
  - **Parameters:**
    - name: The name of the remote broker to connect to.
    - port: The port number on which the remote broker is listening.
  - **Returns:**
    - A Channel object representing the communication channel with the connected broker. Properly handle this channel's lifecycle to avoid memory leaks or unclosed connections.



### Task
It represents a task running in its own thread, and it interacts with a Broker and can have only one Broker. Each task performs an action provided as a Runnable. The developer must ensure that the action does not interfere with other tasks or channels, particularly when performing read/write operations on shared resources.

#### Constructor:
- Task(Broker b, Runnable r):
  - Initializes the Task with the specified Broker and the action to be performed. The developer is responsible for implementing the Runnable in a way that interacts correctly with the broker and channel, handling synchronization and error cases where necessary.
