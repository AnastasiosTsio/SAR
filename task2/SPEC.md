# Overview: QueueBroker / MessageQueue

A QueueBroker and MessageQueue system is designed to send and receive messages. A message is defined as a variable-sized payload of bytes. Each message is sent and received as a whole, preserving the order and integrity of the data. The QueueBroker establishes connections, while MessageQueue handles the transmission of messages. 

- A QueueBroker operates on a named broker and allows tasks to accept or connect on a specific port. It is responsible for maintaining the communication link and providing a message queue.
- A MessageQueue provides the abstraction to send and receive messages. It guarantees FIFO delivery, where the order of messages is maintained.
- A Task can be associated with either a Broker or a QueueBroker for performing concurrent operations.

The following rules apply:

- A MessageQueue can be used to send and receive messages at both ends, and the communication is fully duplex.
- Like channels, MessageQueues are thread-safe for reading and writing as long as one thread writes and one thread reads. Concurrent reading or writing by multiple threads is not allowed without additional synchronization.
- MessageQueue also supports closing operations, allowing a clean disconnection of the communication endpoints.

# Connecting

To establish communication, a QueueBroker either connects to a remote broker or accepts a connection on a specified port. This initiates a message queue for the tasks involved.

- accept(int port): The accept method binds to a specific port and waits for an incoming connection from a remote broker. It returns a fully operational MessageQueue when a connection is established.
- connect(String name, int port): The connect method initiates a connection to a remote broker identified by its name and port. This method blocks until a connection is successfully established, returning a usable MessageQueue for communication.

In both cases, communication is symmetric and occurs when both ends rendezvous. The methods are blocking until a matching connect or accept happens, resulting in the creation of a message queue.

# Sending

Signature: void send(byte[] bytes, int offset, int length)

- The send method is used to transmit a message from one endpoint to the other. The provided byte array contains the payload, starting at the specified offset for the given length. 
- The message is sent as a whole, and the operation ensures that all bytes within the specified range are transmitted in one go.
- The method does not return until the message has been fully transmitted, ensuring the integrity and order of delivery.
- If the MessageQueue is closed while a message is being sent, the method throws a QueueClosedException.

Here’s an example of sending a message:

void sendMessage(byte[] message) {
    queue.send(message, 0, message.length);
}

# Receiving

Signature: byte[] receive()

- The receive method retrieves the next available message from the MessageQueue.
- Messages are returned as an array of bytes, containing the entire payload sent by the other endpoint.
- If no message is available, the method blocks until a message is received or the MessageQueue is closed.
- If the MessageQueue is closed before receiving a message, the method throws a QueueClosedException.

Here’s an example of receiving a message:

byte[] message = queue.receive();

# Closing

Signature: void close()

- The close method gracefully closes the MessageQueue, ensuring no more messages can be sent or received. 
- Any ongoing operations will be interrupted and throw a QueueClosedException except the case when there are bytes that were sent by that remote side, before it was closed, and these bytes have not been received yet on a local side. 
Therefore, if we want the local side to be able to receive these last bytes, the local side should not be considered close until all these bytes have been received or if it is locally disconnected.
- After calling close, the MessageQueue is no longer usable. The closed() method returns true for a closed queue.

# Task and Broker Relationship

A Task is a thread-based execution unit. A Task can be associated with either a Broker or a QueueBroker, enabling communication handling through their respective mechanisms.

- Task(Broker b, Runnable r): Associates a task with a traditional Broker and a Runnable operation.
- Task(QueueBroker b, Runnable r): Associates a task with a QueueBroker, allowing message-based communication, and performs the Runnable operation.

# Exception Handling

### QueueClosedException
This exception is thrown when an operation is attempted on a closed MessageQueue. It indicates that the communication has been closed, either intentionally or due to an error condition.

### DisconnectedException
This exception is thrown when the connection is lost during a send or receive operation. It indicates that the other end of the communication has disconnected unexpectedly.
