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

import com.almworks.sqlite4java.SQLite;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.exceptions.AmazonServiceExceptionType;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.local.server.LocalDynamoDBServerHandler;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;

import java.io.File;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * Local server that implements Aws DynamoDB API.
 */
public class DynamoDBServer {

    private static final Logger logger = Logger.getLogger(DynamoDBServer.class.getName());

    private final static String EMPTY_CORS_PARAMS = null;

    private final int port;
    private final RequestHandler requestHandler;
    private final DynamoDBProxyServer server;

    public DynamoDBServer(int port) {
        this.port = port;
        requestHandler = new RequestHandler();
        LocalDynamoDBServerHandler serverHandler = new LocalDynamoDBServerHandler(requestHandler, EMPTY_CORS_PARAMS);
        server = new DynamoDBProxyServer(port, serverHandler);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                DynamoDBServer.this.stop();
            }
        });
    }

    public DynamoDBServer() {
        this(findPort());
    }

    private static int findPort() {
        try {
            ServerSocket socket = null;
            try {
                socket = new ServerSocket(0);
                socket.setReuseAddress(true);
                return socket.getLocalPort();
            } finally {
                socket.close();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Impossible to find a free port");
        }
    }

    /**
     * Start the server.
     */
    public void start() {
        loadSqlLiteLibraries();
        try {
            logger.info("Start dynamo db server on port " + port);
            server.start();
        } catch (Exception e) {
            throw new IllegalStateException("Impossible to start Dynamo DB Server", e);
        }
    }

    private void loadSqlLiteLibraries() {
        SqliteLibrary.unload();
        File sqLiteJar = getSqLiteJar();
        File sqLiteRoot = getSqLiteRoot(sqLiteJar);
        boolean loadedLibraries = loadLibrariesFromSubDirectories(sqLiteRoot);
        if (!loadedLibraries) throw new IllegalStateException("Impossible to load sql lite libraries");
    }

    private File getSqLiteJar() {
        try {
            return new File(SQLite.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Impossible to load sql lite jar", e);
        }
    }

    private final static String SQLITE = "sqlite4java";

    private File getSqLiteRoot(File jar) {
        File parent = jar;
        while (parent.getParentFile().getPath().contains(SQLITE)) {
            parent = parent.getParentFile();
        }
        return parent;
    }

    private boolean loadLibrariesFromSubDirectories(File directory) {
        boolean result = loadLibrariesFromDirectory(directory);
        if (result) return true;
        else for(File file : directory.listFiles()) if (file.isDirectory()) {
            if (loadLibrariesFromSubDirectories(file)) return true;
        }
        return false;
    }

    private boolean loadLibrariesFromDirectory(File directory) {
        try {
            String path = directory.getPath();
            SQLite.setLibraryPath(path);
            SQLite.loadLibrary();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Stop the server.
     */
    public void stop() {
        try {
            logger.info("Stop dynamo db server");
            server.stop();
        } catch (Exception e) {
            throw new IllegalStateException("Impossible to stop Dynamo DB Server", e);
        }
    }

    /**
     * @return the server endpoint. The URI always point to localhost while
     * the port depends how the server has been created.
     */
    public String getEndpoint() {
        return "http://localhost:" + port;
    }

    /**
     * Reset the server status, bringing it back to the same position
     * after the creation. All the tables will be deleted, forced
     * errors cleaned and provisioned throughput reset.
     */
    public void reset() {
        doesNotFail();
        deleteAllTables();
    }

    private void deleteAllTables() {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient(new BasicAWSCredentials("accessKey", "secretKey"));
        client.setEndpoint(getEndpoint());
        ListTablesResult result = client.listTables(new ListTablesRequest());
        for (String tableName : result.getTableNames()) {
            client.deleteTable(new DeleteTableRequest(tableName));
        }
        client.shutdown();
    }

    /**
     * The server behaves normally without any forces failures.
     */
    public void doesNotFail() {
        requestHandler.doesNotFail();
    }

    /**
     * The server is forced to fail with a predefined exception.
     * @param exception of type {@link AmazonServiceExceptionType}.
     * @return the server injected failure.
     */
    public InjectedFailure failsWith(AmazonServiceExceptionType exception) {
        return new InjectedFailure(
            exception.getResponseStatus(),
            exception.getErrorCode(),
            exception.getMessage(),
            null
        );
    }

    /**
     * The server is forced to fail with a an response code.
     * @param responseCode the error response code.
     * @return the server injected failure.
     */
    public InjectedFailure failsWithResponseCode(int responseCode) {
        return new InjectedFailure(responseCode, "", "", null);
    }

    public class InjectedFailure {

        final int responseCode;
        final String errorCode;
        final String errorMessage;
        final ErrorCondition<? extends AmazonWebServiceRequest> errorCondition;

        private InjectedFailure(int responseCode, String errorCode, String errorMessage, ErrorCondition<? extends AmazonWebServiceRequest> errorCondition) {
            this.responseCode = responseCode;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.errorCondition = errorCondition;
            requestHandler.setInjectedFailure(this);
        }

        /**
         * The server is forced to fail with a an error code.
         * @param errorCode the string summary of the error code.
         * @return the server injected failure.
         */
        public InjectedFailure withAwsErrorCode(String errorCode) {
            return new InjectedFailure(responseCode, errorCode, errorMessage, errorCondition);
        }

        /**
         * The server is forced to fail with a an error message.
         * @param errorMessage the string summary of the error message.
         * @return the server injected failure.
         */
        public InjectedFailure withAwsErrorMessage(String errorMessage) {
            return new InjectedFailure(responseCode, errorCode, errorMessage, errorCondition);
        }

        /**
         * The server is forced to fail for a specific request depending on the condition.
         * @param errorCondition the failing condition.
         * @return the server injected failure.
         */
        public InjectedFailure withErrorCondition(ErrorCondition<? extends AmazonWebServiceRequest> errorCondition) {
            return new InjectedFailure(responseCode, errorCode, errorMessage, errorCondition);
        }

    }

}
