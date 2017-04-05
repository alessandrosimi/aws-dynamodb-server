**Latest release:** Pre Release<br/>
**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

# Aws DynamoDB Server

A simple wrapper of the DynamoDBLocal project to make the easy to test DynamoDB locally with unit test.

## Easy to use

Start the server and use it is easy.

```java
DynamoDBServer server = new DynamoDBServer();
// Start the server
server.start();
// Create the client
String serverEndpoint = server.getEndpoint();
AmazonDynamoDBClient client = new AmazonDynamoDBClient();
client.setEndpoint(serverEndpoint);
// Use the client
...
// Stop the server
server.stop();
```

The server constructor takes the port as parameter, otherwise it would choose a free one (suggested behaviour).

## How to get it

The server would be soon available directly from maven.

```xml
<dependency>
    <groupId>io.exemplary.aws</groupId>
    <artifactId>aws-dynamodb-server</artifactId>
    <version>${aws.version}</version>
</dependency>
```
## Full example

This is a full example that can be use to start testing with `Aws DynamoDB Server`.

```java
import junit.Test;
import io.exemplary.aws.DynamoDBServer;

public class DynamoDBTest {

  @Test
  public void createATableAndStore() {
    DynamoDBServer server = new DynamoDBServer();
    server.start();
    String serverEndpoint = server.getEndpoint();
    server.stop();
  }

}
```
