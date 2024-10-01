# Design Document

## QueueBroker, Tasks, and BrokerManager

### Tasks and Broker Retrieval
A task in the system can be associated with either a QueueBroker or a traditional Broker. Tasks are represented as threads and are responsible for handling message-based communication. A task retrieves its associated broker or QueueBroker using the methods getBroker() or getQueueBroker. Each task runs a Runnable object concurrently, ensuring efficient communication handling.

### QueueBroker
The QueueBroker is responsible for managing message-based communication between tasks. It allows tasks to connect and accept messages over specific ports. QueueBroker abstracts the concept of brokers from traditional channels to message-based queues and provides the following key functionalities:

- **accept(int port)**: Binds to a specific port and waits for incoming connections. When a connection is established, it returns a MessageQueue for handling communication.
- **connect(String name, int port)**: Initiates a connection to a remote broker using the specified name and port. It returns a MessageQueue when the connection is successful.

Both accept and connect block until a connection is established, forming a MessageQueue that facilitates message transfer between endpoints.

## MessageQueue Mechanism

### Message Queue Creation
Once a connection is established via QueueBroker, a MessageQueue is created. A MessageQueue is used for sending and receiving messages, which are guaranteed to follow a FIFO (First In, First Out) order.

The MessageQueue offers the following methods for message handling:
- **send(byte[] bytes, int offset, int length)**: Sends a message from one end to another, transmitting the specified range of bytes.
- **receive()**: Receives the next message from the queue, returning it as a byte array.
- **close()**: Closes the MessageQueue, terminating any further communication.
- **closed()**: Checks if the MessageQueue has been closed.

The message queues are fully duplex and support sending and receiving of messages concurrently between tasks.

### Message Transmission and Thread Safety
Message transmission via send ensures that the entire message is delivered as a whole. The integrity and order of the messages are preserved, with the send method blocking until all bytes are successfully transmitted.

For receiving, the receive method blocks until a message is available, ensuring that no partial data is retrieved. This blocking mechanism helps prevent active polling and guarantees efficient use of resources.

Thread safety is ensured as long as only one thread reads and another writes to the same MessageQueue. Concurrent reads or writes by multiple threads require additional synchronization mechanisms outside the provided queue structure.

## Disconnecting and Closing Mechanism

### Local Closing
When a task closes the MessageQueue:
- The close() method is invoked, transitioning the queue into a closed state.
- Any pending operations, such as blocked send or receive, will throw a QueueClosedException.
- Once the queue is closed, no further messages can be sent or received.

### Remote Closing
In cases where the remote side closes the connection:
- The MessageQueue allows any remaining in-transit messages to be received before transitioning to a closed state.
- The closed() method will return true only after all pending messages have been processed.

This approach ensures that message integrity is preserved, even in cases of asynchronous closure from the remote side.

### Exception Handling

- **QueueClosedException**: This exception is thrown when a message operation is attempted on a closed MessageQueue.
- **DisconnectedException**: Thrown when the connection between brokers is unexpectedly lost during a message transfer operation. This indicates a failure in maintaining the connection, either due to network issues or an intentional remote disconnect.

## BrokerManager

### BrokerManager and Thread Safety
The BrokerManager handles the registration and management of all brokers, including QueueBroker and traditional brokers. It ensures that brokers can be retrieved by name and port. Thread safety is critical within the BrokerManager, which must allow multiple tasks to access and manage brokers concurrently.

Tasks use BrokerManager to look up or register brokers, ensuring that each broker can serve multiple tasks efficiently.
