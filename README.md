**Latest release:** Pre Release<br/>
**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

# Aws DynamoDB Server

A simple wrapper of the AWS DynamoDBLocal project to make easy to test DynamoDB applications.

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

## Inject errors

The server behaviour can be controlled specifying the error.
The error can be one of the Amazon service exceptions or an http error code.

```java
DynamoDBServer server = new DynamoDBServer();
// Amazon exception
server.failsWith(AmazonServiceExceptionType.THROTTLING_EXCEPTION);
// Error code
server.failsWithResponseCode(501);
```

The error would apply to all the server request until the normal behaviour is restored.

```java
server.doesNotFail();
```

Also is possible to specify the request that triggers the error with a condition.

```java
server.failsWith(AmazonServiceExceptionType.THROTTLING_EXCEPTION)
      .withErrorCondition(new ErrorCondition<CreateTableRequest>() {
    @Override
    public boolean shouldFail(CreateTableRequest request) {
        return request.getTableName().equals("myTable");
    }
});
```

In this example the request fails with a throttling exception only for the table "myTable" creation.