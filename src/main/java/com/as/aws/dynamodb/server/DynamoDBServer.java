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

import com.almworks.sqlite4java.SQLite;
import com.amazonaws.services.dynamodbv2.dataMembers.ResponseData;
import com.amazonaws.services.dynamodbv2.exceptions.AmazonServiceExceptionType;
import com.amazonaws.services.dynamodbv2.local.exceptions.ExceptionBean;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBRequestHandler;
import com.amazonaws.services.dynamodbv2.local.server.LocalDynamoDBRequestHandler;
import com.amazonaws.services.dynamodbv2.local.server.LocalDynamoDBServerHandler;
import org.eclipse.jetty.server.Request;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class DynamoDBServer {

    private final static Boolean RUN_IN_MEMORY = true;
    private final static String EMPTY_DB_PATH = null;
    private final static Boolean NonSharedDb = false;
    private final static Boolean NonDelayedTransientStatuses = false;
    private final static String EmptyCorsParams = null;

    private final int port;
    private final DynamoDBProxyServer server;

    public DynamoDBServer(int port) {
        this.port = port;
        LocalDynamoDBRequestHandler requestHandler = new LocalDynamoDBRequestHandler(0, RUN_IN_MEMORY, EMPTY_DB_PATH, NonSharedDb, NonDelayedTransientStatuses);
        LocalDynamoDBServerHandlerWithException serverHandler = new LocalDynamoDBServerHandlerWithException(requestHandler);
        server = new DynamoDBProxyServer(port, serverHandler);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                DynamoDBServer.this.stop();
            }
        });
    }

    public void start() {
        loadSqlLiteLibraries();
        try {
            server.start();
        } catch (Exception e) {
            throw new IllegalStateException("Impossible to start Dynamo DB Server", e);
        }
    }

    private void loadSqlLiteLibraries() {
        try {
            File sqlLiteJar = new File(SQLite.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File version = sqlLiteJar.getParentFile();
            File artifact = version.getParentFile();
            File group = artifact.getParentFile();
            for (File file : group.listFiles()) if (!file.getName().equals(artifact.getName())) {
                for (File ver : file.listFiles()) if (ver.getName().equals(version.getName())) {
                    try {
                        String path = ver.getPath();
                        SQLite.setLibraryPath(path);
                        SQLite.loadLibrary();
                    } catch (Exception e) {}
                }
            }
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Impossible to load sql lite libraries", e);
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new IllegalStateException("Impossible to stop Dynamo DB Server", e);

        }
    }

    public URI endpoint() {
        return URI.create("http://localhost:" + port);
    }

    private interface HandlerBehaviour {}
    private class NormalBehaviour implements HandlerBehaviour {}
    private final NormalBehaviour NORMAL = new NormalBehaviour();
    private class InjectedFailure implements HandlerBehaviour {
        final int responseCode;
        final String errorCode;
        final String errorMessage;

        private InjectedFailure(int responseCode, String errorCode, String errorMessage) {
            this.responseCode = responseCode;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }
    }

    private HandlerBehaviour handlerBehaviour = NORMAL;

    private class LocalDynamoDBServerHandlerWithException extends LocalDynamoDBServerHandler {

        private LocalDynamoDBServerHandlerWithException(DynamoDBRequestHandler handler) {
            super(handler, EmptyCorsParams);
        }

        @Override
        public void handle(String target,
                           Request baseRequest,
                           HttpServletRequest request,
                           HttpServletResponse response) throws ServletException, IOException {
            if (handlerBehaviour instanceof NormalBehaviour) {
                super.handle(target, baseRequest, request, response);
            } else if (handlerBehaviour instanceof InjectedFailure) {
                InjectedFailure failure = (InjectedFailure) handlerBehaviour;
                baseRequest.setHandled(true);
                ResponseData res = new ResponseData(response);
                res.getHttpServletResponse().setStatus(failure.responseCode);
                res.setResponseBody(this.jsonMapper.writeValueAsBytes(new ExceptionBean(failure.errorCode, failure.errorMessage)));
                response.setHeader("x-amzn-RequestId", UUID.randomUUID().toString());
                response.getOutputStream().write(res.getResponseBody());
            }
        }
    }

    public void forceFailureWith(AmazonServiceExceptionType exception) {
        handlerBehaviour = new InjectedFailure(
            exception.getResponseStatus(),
            exception.getErrorCode(),
            exception.getMessage()
        );
    }

    public void forceFailureWith(int responseCode) {
        forceFailureWith(responseCode, "");
    }

    public void forceFailureWith(int responseCode, String errorCode) {
        forceFailureWith(responseCode, errorCode, "");
    }

    public void forceFailureWith(int responseCode, String errorCode, String errorMessage) {
        handlerBehaviour = new InjectedFailure(responseCode, errorCode, errorMessage);
    }

    public void clearForcedFailure() {
        handlerBehaviour = NORMAL;
    }

}
