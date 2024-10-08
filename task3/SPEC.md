# Specification: QueueBroker and MessageQueue

## Overview

This specification describes the structure and behavior of the QueueBroker and MessageQueue classes. These classes define the interface for a broker responsible for managing message queues, including binding to ports and connecting to queues. The MessageQueue class abstracts the functionality of a queue that can send and receive byte arrays, with support for listeners to handle incoming messages and closures.

### QueueBroker

QueueBroker is an abstract class that manages message queues by binding to a port and accepting connections. It provides two primary interfaces: AcceptListener for accepting incoming message queue connections and ConnectListener for managing connections to remote message queues.

#### Constructors
- QueueBroker(String name): Creates a new QueueBroker instance with the given name.

#### Inner Interfaces
- AcceptListener: 
  - void accepted(MessageQueue queue): Called when a new message queue is accepted and ready to be used.

- ConnectListener: 
  - void connected(MessageQueue queue): Called when the connection to a remote message queue is successfully established.
  - void refused(): Called when the connection attempt to a remote message queue is refused.

#### Methods
- boolean bind(int port, AcceptListener listener): 
  - Binds the broker to the specified port and registers an AcceptListener to handle incoming connections.
  - Parameters:
    - port: The port to bind to.
    - listener: The listener that will handle accepted message queues.
  - Returns: true if binding is successful, false otherwise.
  
- boolean unbind(int port): 
  - Unbinds the broker from the specified port, stopping it from accepting new connections.
  - Parameters: 
    - port: The port to unbind from.
  - Returns: true if unbinding is successful, false otherwise.

- boolean connect(String name, int port, ConnectListener listener): 
  - Attempts to connect to a remote message queue on the specified port with the provided name.
  - Parameters:
    - name: The name of the queue to connect to.
    - port: The port number to connect to.
    - listener: The listener that will handle the result of the connection attempt.
  - Returns: true if the connection attempt is initiated successfully, false otherwise.

### MessageQueue

MessageQueue is an abstract class that represents a queue capable of sending and receiving byte messages. It provides an interface to set listeners for incoming messages and to check whether the queue is closed.

#### Inner Interface
- Listener:
  - void received(byte[] msg): Called when a message is received by the queue.
  - void closed(): Called when the message queue is closed.

#### Methods
- void setListener(Listener l): 
  - Sets the listener for handling incoming messages and closures.
  - Parameters:
    - l: The listener instance.

- boolean send(byte[] bytes): 
  - Sends a message represented by the byte array.
  - Parameters:
    - bytes: The byte array representing the message to send.
  - Returns: true if the message is sent successfully, false otherwise.

- boolean send(byte[] bytes, int offset, int length): 
  - Sends a message using a subset of the provided byte array.
  - Parameters:
    - bytes: The byte array representing the message to send.
    - offset: The starting position in the byte array.
    - length: The number of bytes to send from the array.
  - Returns: true if the message is sent successfully, false otherwise.

- void close(): 
  - Closes the message queue, preventing any further messages from being sent or received.

- boolean closed(): 
  - Checks whether the message queue is closed.
  - Returns: true if the queue is closed, false otherwise.