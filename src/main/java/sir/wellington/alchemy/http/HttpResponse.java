/*
 * Copyright 2015 Wellington.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sir.wellington.alchemy.http;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.util.Collections;
import java.util.Map;
import jdk.nashorn.internal.ir.annotations.Immutable;
import sir.wellington.alchemy.annotations.arguments.Nullable;
import sir.wellington.alchemy.annotations.patterns.BuilderPattern;
import static sir.wellington.alchemy.annotations.patterns.BuilderPattern.Role.BUILDER;
import static sir.wellington.alchemy.annotations.patterns.BuilderPattern.Role.PRODUCT;
import static sir.wellington.alchemy.arguments.Arguments.checkThat;
import static sir.wellington.alchemy.arguments.assertions.Assertions.nonEmptyMap;
import static sir.wellington.alchemy.arguments.assertions.Assertions.notNull;
import static sir.wellington.alchemy.arguments.assertions.Assertions.positiveInteger;

/**
 *
 * @see HttpResponse.Builder
 *
 * @author SirWellington
 */
@Immutable
public interface HttpResponse
{

    int statusCode();

    @Nullable
    Map<String, String> responseHeaders();

    String asString();

    JsonElement asJSON() throws JsonParseException;

    <T> T as(Class<T> classOfT) throws JsonParseException;

    @BuilderPattern(role = BUILDER)
    static class Builder
    {

        private int statusCode = - 100;
        private Map<String, String> responseHeaders = Collections.EMPTY_MAP;
        private final Gson gson = new Gson();
        private JsonElement response;

        public static Builder newInstance()
        {
            return new Builder();
        }

        public Builder withStatusCode(int statusCode) throws IllegalArgumentException
        {
            checkThat(statusCode).is(positiveInteger());
            this.statusCode = statusCode;
            return this;
        }

        public Builder withResponseHeaders(Map<String, String> responseHeaders) throws IllegalArgumentException
        {
            checkThat(responseHeaders).is(nonEmptyMap());
            this.responseHeaders = responseHeaders;
            return this;
        }

        public Builder withResponse(JsonElement json) throws IllegalArgumentException
        {
            checkThat(json).is(notNull());
            this.response = json;
            return this;
        }

        public HttpResponse build() throws IllegalStateException
        {
            checkThat(statusCode)
                    .usingException(ex -> new IllegalStateException("No status code supplieda" + statusCode))
                    .is(positiveInteger());

            return new Impl(statusCode, responseHeaders, gson, response);
        }

        @Immutable
        @BuilderPattern(role = PRODUCT)
        private static class Impl implements HttpResponse
        {

            private final int statusCode;
            private final Map<String, String> responseHeaders;
            private final Gson gson;
            private final JsonElement response;

            private Impl(int statusCode,
                         Map<String, String> responseHeaders,
                         Gson gson,
                         JsonElement response)
            {
                this.statusCode = statusCode;
                this.responseHeaders = responseHeaders;
                this.gson = gson;
                this.response = response;
            }

            @Override
            public int statusCode()
            {
                return statusCode;
            }

            @Override
            public Map<String, String> responseHeaders()
            {
                return ImmutableMap.copyOf(responseHeaders);
            }

            @Override
            public String asString()
            {
                return response.toString();
            }

            @Override
            public JsonElement asJSON() throws JsonParseException
            {
                JsonElement copy = gson.toJsonTree(response);
                return copy;
            }

            @Override
            public <T> T as(Class<T> classOfT) throws JsonParseException
            {
                checkThat(classOfT).is(notNull());

                try
                {
                    T instance = gson.fromJson(response, classOfT);
                    return instance;
                }
                catch (RuntimeException ex)
                {
                    throw new JsonParseException("Failed to parse json to class: " + classOfT, ex);
                }
            }
        }
    }
}
