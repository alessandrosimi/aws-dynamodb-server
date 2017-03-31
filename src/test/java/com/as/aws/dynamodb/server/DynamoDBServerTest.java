/**
 * Copyright 2017 Alessandro Simi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.as.aws.dynamodb.server;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.exceptions.AmazonServiceExceptionType;
import com.amazonaws.services.dynamodbv2.model.*;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DynamoDBServerTest {

    @Test
    public void theServerShouldRespondToASimpleRequestsFlow() {
        // Create and start the server
        DynamoDBServer server = new DynamoDBServer(8989);
        server.start();
        // Create the client
        AmazonDynamoDBClient client = new AmazonDynamoDBClient(new BasicAWSCredentials("accessKey", "secretKey"));
        client.setEndpoint(server.getEndpoint());
        // Create a table
        client.createTable(createTableRequest("tableName", "key"));
        // Get the tables name
        ListTablesResult result = client.listTables();
        assertEquals(1, result.getTableNames().size());
        assertEquals("tableName", result.getTableNames().get(0));
        // Put an item
        Map<String, AttributeValue> items = new HashMap<String, AttributeValue>();
        items.put("key", new AttributeValue("key1"));
        items.put("field", new AttributeValue("field1"));
        client.putItem("tableName", items);
        // Get items
        Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
        key.put("key", new AttributeValue("key1"));
        items = client.getItem("tableName", key).getItem();
        assertEquals("field1", items.get("field").getS());
        // Stop the server
        server.stop();
    }

    private CreateTableRequest createTableRequest(String tableName, String hashKeyName) {
        AttributeDefinition attribute = new AttributeDefinition(hashKeyName, ScalarAttributeType.S);
        KeySchemaElement keySchema = new KeySchemaElement(hashKeyName, KeyType.HASH);
        ProvisionedThroughput throughput = new ProvisionedThroughput(10L, 10L);
        return new CreateTableRequest()
                .withTableName(tableName)
                .withAttributeDefinitions(attribute)
                .withKeySchema(keySchema)
                .withProvisionedThroughput(throughput);
    }

    @Test
    public void theServerShouldRespondToARandomPort() {
        // Create and start the server
        DynamoDBServer server = new DynamoDBServer();
        server.start();
        // Create the client
        AmazonDynamoDBClient client = new AmazonDynamoDBClient(new BasicAWSCredentials("accessKey", "secretKey"));
        client.setEndpoint(server.getEndpoint());
        // Create a table
        client.createTable(createTableRequest("tableName", "key"));
        // Get the tables name
        ListTablesResult result = client.listTables();
        assertEquals(1, result.getTableNames().size());
        assertEquals("tableName", result.getTableNames().get(0));
        // Stop the server
        server.stop();
    }

    @Test
    public void theServerShouldBeForcedToFailWithACustomError() {
        // Create and start the server
        DynamoDBServer server = new DynamoDBServer(8989);
        server.start();
        // Create the client
        AmazonDynamoDBClient client = new AmazonDynamoDBClient(new BasicAWSCredentials("accessKey", "secretKey"));
        client.setEndpoint(server.getEndpoint());
        // Failing server
        server.forceFailureWith(501);
        // Fail to create a table
        AmazonServiceException error = null;
        try {
            client.createTable(createTableRequest("tableName", "key"));
        } catch (AmazonServiceException e) {
            error = e;
        }
        assertNotNull("The exception should have been thrown", error);
        assertEquals(501, error.getStatusCode());
        // Stop the server
        server.stop();
    }

    @Test
    public void theServerShouldBeForcedToFailWithAPreDefinedError() {
        // Create and start the server
        DynamoDBServer server = new DynamoDBServer(8989);
        server.start();
        // Create the client
        AmazonDynamoDBClient client = new AmazonDynamoDBClient(new BasicAWSCredentials("accessKey", "secretKey"));
        client.setEndpoint(server.getEndpoint());
        // Failing server
        server.forceFailureWith(AmazonServiceExceptionType.THROTTLING_EXCEPTION);
        // Fail to create a table
        AmazonServiceException error = null;
        try {
            client.createTable(createTableRequest("tableName", "key"));
        } catch (AmazonServiceException e) {
            error = e;
        }
        assertNotNull("The exception should have been thrown", error);
        assertEquals(AmazonServiceExceptionType.THROTTLING_EXCEPTION.getResponseStatus(), error.getStatusCode());
        assertEquals(AmazonServiceExceptionType.THROTTLING_EXCEPTION.getErrorCode(), error.getErrorCode());
        assertEquals(AmazonServiceExceptionType.THROTTLING_EXCEPTION.getMessage(), error.getErrorMessage());
        // Stop the server
        server.stop();
    }

    @Test
    public void theServerShouldBeForcedToFailAndShouldSucceed() {
        // Create and start the server
        DynamoDBServer server = new DynamoDBServer(8989);
        server.start();
        // Create the client
        AmazonDynamoDBClient client = new AmazonDynamoDBClient(new BasicAWSCredentials("accessKey", "secretKey"));
        client.setEndpoint(server.getEndpoint());
        // Failing server
        server.forceFailureWith(501);
        // Fail to create a table
        AmazonServiceException error = null;
        try {
            client.createTable(createTableRequest("tableName", "key"));
        } catch (AmazonServiceException e) {
            error = e;
        }
        assertNotNull("The exception should have been thrown", error);
        // Server should not fail
        server.clearForcedFailure();
        // Succeed
        client.createTable(createTableRequest("tableName", "key"));
        ListTablesResult result = client.listTables();
        assertEquals(1, result.getTableNames().size());
        assertEquals("tableName", result.getTableNames().get(0));
        // Stop the server
        server.stop();
    }

}
