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

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DynamoDBServerTest extends AbstractTest {

    @Test
    public void theServerShouldRespondToASimpleRequestsFlow() {
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
    }

}
