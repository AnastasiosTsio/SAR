# Design Document
## Brokers, Tasks, and BrokerManager
### Tasks and Broker Retrieval
A task in the system is associated with a broker. A task may contain runnables, and at any point, it can retrieve the broker it is connected to using a static method getBroker(). This method is designed based on the Task(Thread), enabling thread-safe access to the associated broker. Multiple brokers can coexist within the system.

### BrokerManager
To manage all brokers within the system, we introduce the BrokerManager. This component maintains a list of brokers and provides a thread-safe mechanism to access them through the function getBroker(BrokerName). Since brokers are thread-safe, BrokerManager must also ensure thread safety to maintain a consistent state across concurrent operations.

### Broker Variables
Each broker holds a reference to the BrokerManager to facilitate interaction with other brokers. The broker can thus find the appropriate location to place a RDV entry when establishing a connection.

## Connection Handling: Connect and Accept
### RDV Mechanism
The broker facilitates connection establishment using the connect and accept functions, which rely on a list of RDV objects. Each RDV contains the following elements:
- Two brokers: brokers (the local broker, server) and brokerc (the remote broker, client).
- Port: The communication port number.
- Connect and Accept Functions: These functions enable the formation of a connection between the local and remote brokers (brokers and brokerc).

The RDV creates channels that facilitate communication between the two brokers, handling both the data flow and thread safety required for concurrent operations.

### Connecting to a Broker
When a task attempts to establish a connection using connect, it uses BrokerManager to find the broker specified by the argument name. Then, it seeks out the designated port within that broker to initiate the RDV. If a matching accept exists, the RDV completes, and a channel is created for communication.

### Accepting a Connection
A task performing an accept on a broker will generate an RDV entry on that broker. This RDV then waits for an incoming connect request. A broker can maintain a list of RDVs but ensures that only one accept can exist for a particular port. However, it supports multiple connect requests on the same port.

## Channel Creation

The RDV is responsible for creating channels upon the successful execution of a connect or accept. During the creation of channels, the RDV also initializes two buffers:
- Buffer A: buffera
- Buffer B: bufferb

The RDV generates:
- Channel for brokerc: This channel uses bufferin = buffera and bufferout = bufferb.
- Channel for brokers: This channel uses bufferin = bufferb and bufferout = buffera.

This arrangement ensures that one broker reads from buffera and writes to bufferb, while the other broker reads from bufferb and writes to buffera. This setup prevents message conflicts and guarantees FIFO ordering and data integrity.

## Disconnecting Mechanism

The disconnecting process in this system is crucial to maintaining data consistency and handling in-transit bytes correctly. Here's how the disconnection protocol is implemented:

### Local Disconnection
When a channel disconnects on the local side:
- The disconnect method is invoked on the local channel.
- This method changes the internal state of the channel, marking it as disconnected. From this point on, any invocation of read or write on this channel will result in a DisconnectedException.
- If there are pending operations on the local side (e.g., a blocked read or write), they are immediately interrupted, throwing a DisconnectedException.
- The local broker informs the associated RDV that the channel has been disconnected.

### Remote Disconnection
When a disconnect occurs on the remote side:
- The system ensures that all bytes in transit are read locally before the channel is considered fully disconnected.
- The RDV tracks the in-transit bytes to ensure that the local channel reads any remaining data before marking the channel as disconnected.
- During this "half-disconnected" state, the channel on the local side will still allow reads but will silently drop any new writes. This approach creates the illusion of a still-connected channel on the local side, allowing it to finish processing any final messages from the remote side.
- Once all in-transit bytes have been read or the local channel requests a disconnect, the local channel's state changes to fully disconnected.

### How RDV Manages Disconnection
The RDV contains internal flags and status indicators for both brokerc and brokers. When a disconnect request is received (either locally or remotely), the RDV:
- Updates the status of the involved brokers to indicate a disconnection request.
- Monitors the associated buffers (buffera and bufferb) to check for any in-transit bytes that still need to be processed. 
- If there are bytes left in the buffer, the RDV maintains a "half-disconnected" state for the channel, allowing reading of the remaining bytes. 
- The RDV transitions to a fully disconnected state once both:
  1. All in-transit bytes have been read.
  2. The disconnect method has been called on the local side.
  
The RDV is thus able to seamlessly handle disconnection requests and manage the state of the buffers, ensuring no data loss and consistent communication behavior.

### Concurrent Disconnection
The protocol supports concurrent disconnection, meaning both the local and remote sides can request disconnection simultaneously. The RDV manages these concurrent requests to ensure a smooth transition to a disconnected state.

### Handling RDV During Disconnection
The RDV monitors the states of both brokers involved in the channel. If either side initiates a disconnect:
- The RDV removes itself from the broker's list, preventing new connections on that port.
- The RDV ensures that any further attempts to connect or accept on the involved brokers return an indication of the channel's disconnected state.

This disconnection mechanism ensures the proper closing of channels while preserving data integrity, handling in-transit bytes correctly, and providing an exception-based mechanism for managing blocked operations.
