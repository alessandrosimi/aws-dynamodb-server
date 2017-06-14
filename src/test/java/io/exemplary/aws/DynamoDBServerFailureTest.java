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
package io.exemplary.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.exceptions.AmazonServiceExceptionType;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DynamoDBServerFailureTest extends AbstractTest {

    @Test
    public void theServerShouldFailWithAResponseCode() {
        // Failing server
        server.failsWithResponseCode(501);
        // Fail to create a table
        AmazonServiceException error = null;
        try {
            client.createTable(createTableRequest("tableName", "key"));
        } catch (AmazonServiceException e) {
            error = e;
        }
        assertNotNull("The exception should have been thrown", error);
        assertEquals(501, error.getStatusCode());
    }

    @Test
    public void theServerShouldFailWithAnErrorCode() {
        // Failing server
        server.failsWithResponseCode(501)
            .withAwsErrorCode("errorCode");
        // Fail to create a table
        AmazonServiceException error = null;
        try {
            client.createTable(createTableRequest("tableName", "key"));
        } catch (AmazonServiceException e) {
            error = e;
        }
        assertNotNull("The exception should have been thrown", error);
        assertEquals("errorCode", error.getErrorCode());
    }

    @Test
    public void theServerShouldFailWithAnErrorMessage() {
        // Failing server
        server.failsWithResponseCode(501)
                .withAwsErrorMessage("errorMessage");
        // Fail to create a table
        AmazonServiceException error = null;
        try {
            client.createTable(createTableRequest("tableName", "key"));
        } catch (AmazonServiceException e) {
            error = e;
        }
        assertNotNull("The exception should have been thrown", error);
        assertEquals("errorMessage", error.getErrorMessage());
    }

    @Test
    public void theServerShouldFailWithAPredefinedError() {
        // Failing server
        server.failsWith(AmazonServiceExceptionType.THROTTLING_EXCEPTION);
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
    }

    @Test
    public void theServerShouldFailAndThenSucceed() {
        // Failing server
        server.failsWithResponseCode(501);
        // Fail to create a table
        AmazonServiceException error = null;
        try {
            client.createTable(createTableRequest("tableName", "key"));
        } catch (AmazonServiceException e) {
            error = e;
        }
        assertNotNull("The exception should have been thrown", error);
        // Server should not fail
        server.doesNotFail();
        // Succeed
        client.createTable(createTableRequest("tableName", "key"));
        ListTablesResult result = client.listTables();
        assertEquals(1, result.getTableNames().size());
        assertEquals("tableName", result.getTableNames().get(0));
    }

    @Test
    public void theServerShouldFailWithCondition() {
        // Failing server
        server.failsWithResponseCode(501)
                .withErrorCondition(new ErrorCondition<CreateTableRequest>() {
            @Override
            public boolean shouldFail(CreateTableRequest request) {
                return true;
            }
        });
        // Fail to create a table
        AmazonServiceException error = null;
        try {
            client.createTable(createTableRequest("tableName", "key"));
        } catch (AmazonServiceException e) {
            error = e;
        }
        assertNotNull("The exception should have been thrown", error);
        assertEquals(501, error.getStatusCode());
    }

    @Test
    public void shouldBePossibleTosStartTwoServers() {
        DynamoDBServer server1 = new DynamoDBServer();
        DynamoDBServer server2 = new DynamoDBServer();
        try {
            server1.start();
            server2.start();
        } finally {
            server1.stop();
            server2.stop();
        }
    }

}
