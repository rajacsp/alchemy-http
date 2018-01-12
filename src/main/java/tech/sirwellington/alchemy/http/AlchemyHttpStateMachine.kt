/*
 * Copyright © 2018. Sir Wellington.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.sirwellington.alchemy.http

import com.google.gson.Gson
import org.apache.http.client.HttpClient
import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.annotations.arguments.Required
import tech.sirwellington.alchemy.annotations.designs.StepMachineDesign
import tech.sirwellington.alchemy.annotations.designs.StepMachineDesign.Role.MACHINE
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.BUILDER
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern.Role
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.notNull
import tech.sirwellington.alchemy.http.AlchemyRequest.Step1
import tech.sirwellington.alchemy.http.AlchemyRequest.Step2
import tech.sirwellington.alchemy.http.AlchemyRequest.Step3
import tech.sirwellington.alchemy.http.AlchemyRequest.Step4
import tech.sirwellington.alchemy.http.AlchemyRequest.Step5
import tech.sirwellington.alchemy.http.AlchemyRequest.Step6
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException
import java.util.concurrent.Executor

/**
 * This is an internal state machine for managing the transitions of an Alchemy Http Request.
 *
 * @author SirWellington
 */
@Internal
@StepMachineDesign(role = MACHINE)
internal interface AlchemyHttpStateMachine
{

    fun begin(): Step1
    {
        val request = HttpRequest.Builder
                                 .newInstance()
                                 .build()

        return begin(request)
    }

    fun begin(initialRequest: HttpRequest): Step1


    @Throws(IllegalArgumentException::class)
    fun jumpToStep2(request: HttpRequest): Step2


    @Throws(IllegalArgumentException::class)
    fun jumpToStep3(request: HttpRequest): Step3


    @Throws(IllegalArgumentException::class)
    fun <ResponseType> jumpToStep4(request: HttpRequest,
                                   classOfResponseType: Class<ResponseType>): Step4<ResponseType>


    @Throws(IllegalArgumentException::class)
    fun <ResponseType> jumpToStep5(request: HttpRequest,
                                   classOfResponseType: Class<ResponseType>,
                                   successCallback: AlchemyRequest.OnSuccess<ResponseType>): Step5<ResponseType>


    fun <ResponseType> jumpToStep6(request: HttpRequest,
                                   classOfResponseType: Class<ResponseType>,
                                   successCallback: AlchemyRequest.OnSuccess<ResponseType>,
                                   failureCallback: AlchemyRequest.OnFailure): Step6<ResponseType>


    @Throws(AlchemyHttpException::class)
    fun executeSync(request: HttpRequest): HttpResponse
    {
        return executeSync(request, HttpResponse::class.java)
    }


    @Throws(AlchemyHttpException::class)
    fun <ResponseType> executeSync(request: HttpRequest,
                                   classOfResponseType: Class<ResponseType>): ResponseType


    fun <ResponseType> executeAsync(request: HttpRequest,
                                    classOfResponseType: Class<ResponseType>,
                                    successCallback: AlchemyRequest.OnSuccess<ResponseType>,
                                    failureCallback: AlchemyRequest.OnFailure)


    @BuilderPattern(role = BUILDER)
    @FactoryMethodPattern(role = Role.PRODUCT)
    class Builder
    {

        private var apacheHttpClient: HttpClient? = null
        private var executor: Executor = SynchronousExecutor.newInstance()
        private var gson = Constants.defaultGson

        @Throws(IllegalArgumentException::class)
        internal fun usingExecutorService(executor: Executor): Builder
        {
            checkThat(executor).`is`(notNull())

            this.executor = executor
            return this
        }

        @Throws(IllegalArgumentException::class)
        internal fun usingApacheHttpClient(apacheHttpClient: HttpClient): Builder
        {
            checkThat(apacheHttpClient).`is`(notNull())

            this.apacheHttpClient = apacheHttpClient
            return this
        }

        @Throws(IllegalArgumentException::class)
        internal fun usingGson(@Required gson: Gson): Builder
        {
            checkThat(gson).`is`(notNull())

            this.gson = gson
            return this
        }

        @Throws(IllegalStateException::class)
        internal fun build(): AlchemyHttpStateMachine
        {
            checkThat<HttpClient>(apacheHttpClient)
                    .throwing { ex -> IllegalStateException("missing Apache HTTP Client") }
                    .`is`(notNull())

            return AlchemyMachineImpl(apacheHttpClient!!, executor, gson)
        }

        companion object
        {

            @JvmStatic
            @FactoryMethodPattern(role = Role.FACTORY_METHOD)
            internal fun newInstance(): Builder
            {
                return Builder()
            }
        }
    }

}