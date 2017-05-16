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

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.services.dynamodbv2.exceptions.DynamoDBLocalServiceException;
import com.amazonaws.services.dynamodbv2.local.server.LocalDynamoDBRequestHandler;
import com.amazonaws.services.dynamodbv2.model.*;

class RequestHandler extends LocalDynamoDBRequestHandler {

    private final static int HIGH_AUTHORITY_LEVEL = 0;
    private final static Boolean RUN_IN_MEMORY = true;
    private final static String EMPTY_DB_PATH = null;
    private final static Boolean NON_SHARED_DB = false;
    private final static Boolean NON_DELAYED_TRANSIENT_STATUSES = false;

    private DynamoDBServer.InjectedFailure injectedFailure = null;

    RequestHandler() throws DynamoDBLocalServiceException {
        super(HIGH_AUTHORITY_LEVEL, RUN_IN_MEMORY, EMPTY_DB_PATH, NON_SHARED_DB, NON_DELAYED_TRANSIENT_STATUSES);
    }

    void setInjectedFailure(DynamoDBServer.InjectedFailure injectedFailure) {
        this.injectedFailure = injectedFailure;
    }

    void doesNotFail() {
        this.injectedFailure = null;
    }

    @SuppressWarnings("unchecked")
    private void injectFailureFor(AmazonWebServiceRequest req) {
        if (injectedFailure != null) {
            ErrorCondition<AmazonWebServiceRequest> errorCondition = (ErrorCondition<AmazonWebServiceRequest>) injectedFailure.errorCondition;
            if (errorCondition == null) {
                failsWith(injectedFailure);
            } else if (errorCondition.type.equals(req.getClass())) {
                if (errorCondition.shouldFail(req)) {
                    failsWith(injectedFailure);
                }
            }
        }
    }

    private void failsWith(DynamoDBServer.InjectedFailure injectedFailure) {
        DynamoDBLocalServiceException exception = new DynamoDBLocalServiceException(injectedFailure.errorMessage);
        exception.setErrorCode(injectedFailure.errorCode);
        exception.setStatusCode(injectedFailure.responseCode);
        throw exception;
    }

    @Override
    public BatchGetItemResult batchGetItem(String accessKey, String region, BatchGetItemRequest req) {
        injectFailureFor(req);
        return super.batchGetItem(accessKey, region, req);
    }

    @Override
    public BatchWriteItemResult batchWriteItem(String accessKey, String region, BatchWriteItemRequest req) {
        injectFailureFor(req);
        return super.batchWriteItem(accessKey, region, req);
    }

    @Override
    public CreateTableResult createTable(String accessKey, String region, CreateTableRequest req) {
        injectFailureFor(req);
        return super.createTable(accessKey, region, req);
    }

    @Override
    public DeleteItemResult deleteItem(String accessKey, String region, DeleteItemRequest req) {
        injectFailureFor(req);
        return super.deleteItem(accessKey, region, req);
    }

    @Override
    public DeleteTableResult deleteTable(String accessKey, String region, DeleteTableRequest req) {
        injectFailureFor(req);
        return super.deleteTable(accessKey, region, req);
    }

    @Override
    public DescribeStreamResult describeStream(String accessKey, String region, DescribeStreamRequest req) {
        injectFailureFor(req);
        return super.describeStream(accessKey, region, req);
    }

    @Override
    public DescribeTableResult describeTable(String accessKey, String region, DescribeTableRequest req) {
        injectFailureFor(req);
        return super.describeTable(accessKey, region, req);
    }

    @Override
    public GetItemResult getItem(String accessKey, String region, GetItemRequest req) {
        injectFailureFor(req);
        return super.getItem(accessKey, region, req);
    }

    @Override
    public GetRecordsResult getRecords(String accessKey, String region, GetRecordsRequest req) {
        injectFailureFor(req);
        return super.getRecords(accessKey, region, req);
    }

    @Override
    public GetShardIteratorResult getShardIterator(String accessKey, String region, GetShardIteratorRequest req) {
        injectFailureFor(req);
        return super.getShardIterator(accessKey, region, req);
    }

    @Override
    public ListStreamsResult listStreams(String accessKey, String region, ListStreamsRequest req) {
        injectFailureFor(req);
        return super.listStreams(accessKey, region, req);
    }

    @Override
    public ListTablesResult listTables(String accessKey, String region, ListTablesRequest req) {
        injectFailureFor(req);
        return super.listTables(accessKey, region, req);
    }

    @Override
    public PutItemResult putItem(String accessKey, String region, PutItemRequest req) {
        injectFailureFor(req);
        return super.putItem(accessKey, region, req);
    }

    @Override
    public QueryResult query(String accessKey, String region, QueryRequest req) {
        injectFailureFor(req);
        return super.query(accessKey, region, req);
    }

    @Override
    public ScanResult scan(String accessKey, String region, ScanRequest req) {
        injectFailureFor(req);
        return super.scan(accessKey, region, req);
    }

    @Override
    public UpdateItemResult updateItem(String accessKey, String region, UpdateItemRequest req) {
        injectFailureFor(req);
        return super.updateItem(accessKey, region, req);
    }

    @Override
    public UpdateTableResult updateTable(String accessKey, String region, UpdateTableRequest req) {
        injectFailureFor(req);
        return super.updateTable(accessKey, region, req);
    }

}
