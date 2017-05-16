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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Defines the failing condition over the DynamoDB request.
 * @param <R> DynamoDb request type.
 */
public abstract class ErrorCondition<R extends AmazonWebServiceRequest> {

    final Class<R> type;

    @SuppressWarnings("unchecked")
    protected ErrorCondition() {
        Type superclass = getClass().getGenericSuperclass();
        ParameterizedType parameterized = (ParameterizedType) superclass;
        type = (Class<R>) parameterized.getActualTypeArguments()[0];
    }

    /**
     * @param request DynamoDB request.
     * @return {@code true} if the request should fail, otherwise {@code false}.
     */
    abstract boolean shouldFail(R request);

}
