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

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.*;
import org.junit.Before;

abstract class AbstractTest {

    final DynamoDBServer server;
    final AmazonDynamoDBClient client;

    AbstractTest() {
        server = new DynamoDBServer();
        server.start();
        client = new AmazonDynamoDBClient(new BasicAWSCredentials("accessKey", "secretKey"));
        client.setEndpoint(server.getEndpoint());
    }

    @Before
    public void cleanBeforeTest() {
        server.reset();
    }

    CreateTableRequest createTableRequest(String tableName, String hashKeyName) {
        AttributeDefinition attribute = new AttributeDefinition(hashKeyName, ScalarAttributeType.S);
        KeySchemaElement keySchema = new KeySchemaElement(hashKeyName, KeyType.HASH);
        ProvisionedThroughput throughput = new ProvisionedThroughput(10L, 10L);
        return new CreateTableRequest()
                .withTableName(tableName)
                .withAttributeDefinitions(attribute)
                .withKeySchema(keySchema)
                .withProvisionedThroughput(throughput);
    }

    UpdateTableRequest updateTableRequest(String tableName, long read, long write) {
        ProvisionedThroughput throughput = new ProvisionedThroughput(read, write);
        return new UpdateTableRequest()
                .withTableName(tableName)
                .withProvisionedThroughput(throughput);
    }


    DeleteTableRequest deleteTableRequest(String tableName) {
        return new DeleteTableRequest()
                .withTableName(tableName);
    }

}
