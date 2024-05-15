# Remote Watch Service Library

The Remote Watch Service (RWS) API is a Java-based
solution for watching files on remote hosts. It provides a
client and server API to enable file monitoring and
notifications across remote systems. The library
includes a gRPC-based implementation for
communication between the client and server.

## Usage Example

### Server Side

```java
Server server = new GrpcServer(8082);
server.start();
server.awaitTermination();
```

### Client Side

```java
InetSocketAddress address = InetSocketAddress.createUnresolved("localhost", 8082);

Client client = GrpcClientAdapter.createClient(address);
var broker = new FileNotificationBroker(client);

Path file = Path.of("/home/nasser");
int eventTypes = FileEventType.CREATION_EVENT | FileEventType.DELETION_EVENT | FileEventType.MODIFICATION_EVENT;
FileSubscription sub = new FileSubscription(file, eventTypes);

broker.register(sub, notification -> System.out.println("consumer: " + notification));
broker.stream();

Thread.sleep(1000 * 60 * 5);

```
