# Design Document

## Event Queue Broker Mechanism

### Overview
The system utilizes event-oriented communication between brokers to facilitate message passing and connection management. Each broker can manage multiple queues for handling different types of messages. The goal of the broker is to establish connections, manage queues, and ensure asynchronous communication using an event-driven model.

### Binding and Accepting Connections
Each broker can bind to a specific port to listen for incoming connections. When a connection is accepted:
- A message queue is created for the connection.
- The broker listens for incoming messages on this queue.
- The listener for the broker is notified that a connection has been accepted, and the message queue is ready for use.
  
The broker continuously monitors the port and accepts multiple connections, ensuring that each connection is properly handled through the event system.

### Connecting to a Broker
When a broker connects to a remote broker:
- The system locates the appropriate port on the remote broker.
- A message queue is established between the two brokers, facilitating communication.
- The connection is asynchronous, and once the connection is established, the local broker is notified via an event.
- If the connection is refused, the system informs the broker of the failure.

### Connection Lifecycle
Once a connection is established between brokers, messages can flow through the message queue. The system ensures that both brokers can:
- Send messages to the remote broker via the message queue.
- Receive messages from the remote broker, which are automatically processed through the event handling mechanism.

The broker tracks the state of the connection, ensuring that all data is transmitted before closing the connection. Both sending and receiving of messages are event-driven and non-blocking, ensuring that brokers can handle multiple simultaneous connections.

### Event-Driven Communication
The entire system operates using an event-driven model, where each broker posts tasks to an event pump. This event pump:
- Processes tasks such as message sending, message receiving, and connection handling.
- Ensures that tasks are executed in the correct order, maintaining the integrity of the communication process.
- Handles asynchronous events without blocking the broker's ability to process other events.

### Message Passing Mechanism
Messages are passed between brokers using a queue mechanism. For each connection:
- A message queue is set up, allowing brokers to send and receive messages.
- Messages are passed in a first-in, first-out (FIFO) order to ensure data consistency.
- Both brokers maintain control over their respective message queues and are notified of incoming messages through event listeners.

### Handling Disconnection
Disconnections can occur either locally or remotely. When a broker decides to disconnect:
- The system ensures that any remaining messages in the queue are processed before fully closing the connection.
- The message queue is marked as closed, and no further messages can be sent or received.
- The broker's listener is notified of the disconnection, allowing it to take appropriate action.

The system supports both normal disconnections and forced disconnections, ensuring that even when the remote side disconnects unexpectedly, the local broker handles the disconnection gracefully.

### Managing Multiple Connections
A single broker can manage multiple connections simultaneously. Each connection is bound to a different port, and the broker keeps track of all active connections. The event-driven model ensures that each connection is handled independently without blocking other connections. This allows brokers to efficiently manage a large number of connections, each with its own message queue and event handler.

## Event Pump Mechanism

### Overview
The event pump is a central component that manages all event-driven tasks. It ensures that tasks are executed in a non-blocking manner, allowing brokers to handle multiple events concurrently.

### Task Execution
When an event (e.g., message sent, message received, connection established) occurs, a task is posted to the event pump. The event pump then:
- Queues the task for execution.
- Processes tasks in the order they were received, ensuring that the system remains consistent and maintains the proper flow of events.

Tasks are executed asynchronously, meaning that the broker can continue processing other events while waiting for a particular task (e.g., message sending) to complete.

### Synchronization and Event Handling
The event pump is responsible for ensuring that tasks are synchronized across the system. This ensures that message queues, connections, and other components are always in a consistent state. The event pump:
- Notifies the appropriate listeners when an event is completed.
- Ensures that tasks waiting on specific conditions (e.g., a message being received) are woken up and processed when those conditions are met.

This mechanism allows the system to handle a large volume of tasks without any one task blocking the entire system.

## Message Queue and Communication Flow

### Message Flow
When a broker sends a message to another broker:
- The message is placed in the message queue for the connection.
- The event pump processes the task of sending the message.
- The receiving broker is notified when a message arrives, and the message is processed in the message queue of the receiving broker.
  
Messages are processed asynchronously, ensuring that brokers can continue to send and receive messages without waiting for each individual message to be processed.

### Ensuring Data Integrity
The message queue mechanism ensures that data integrity is maintained throughout the communication process:
- Messages are sent and received in the order they were placed in the queue (FIFO).
- Any interruptions in the communication process, such as disconnections, are handled gracefully by the system, ensuring that no data is lost.

The system ensures that all messages are delivered to their intended recipients or, in the case of disconnection, properly managed to avoid data loss.
