**Latest release:** Pre Release<br/>
**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

# Aws DynamoDB Server

A simple wrapper of the DynamoDBLocal project to make the easy to test DynamoDB locally with unit test.

## Easy to use

Start the server is easy.

```java
DynamoDBServer server = new DynamoDBServer();
server.start();
server.stop();
```

## How to get it

The server would be soon available directly from maven.

```xml
<dependency>
    <groupId>com.as.aws</groupId>
    <artifactId>aws-dynamodb-server</artifactId>
    <version>${aws.version}</version>
</dependency>
```
