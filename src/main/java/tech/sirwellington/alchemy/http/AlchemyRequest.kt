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

import tech.sirwellington.alchemy.annotations.arguments.NonEmpty
import tech.sirwellington.alchemy.annotations.arguments.Required
import tech.sirwellington.alchemy.annotations.designs.FluidAPIDesign
import tech.sirwellington.alchemy.annotations.designs.StepMachineDesign
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.nonEmptyString
import tech.sirwellington.alchemy.arguments.assertions.validURL
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException
import java.net.MalformedURLException
import java.net.URL
import java.util.LinkedHashSet

/**
 *
 *
 * @author SirWellington
 */
@FluidAPIDesign
@StepMachineDesign
interface AlchemyRequest
{

    interface Step1
    {

        /**
         * Directly download the content served by this URL.
         *
         * @param url The URL to download
         *
         * @return The raw binary served at the URL.
         * @throws IllegalArgumentException
         * @throws AlchemyHttpException
         */
        @Throws(IllegalArgumentException::class, AlchemyHttpException::class)
        fun download(@Required url: URL): ByteArray
        {
            try
            {
                return url.readBytes()
            }
            catch (ex: Exception)
            {
                throw AlchemyHttpException("Could not download from URL" + url, ex)
            }
        }

        /**
         * Begins a GET Request.
         * @return
         */
        fun get(): Step3

        /**
         * Begins a POST Request.
         * @return
         */
        fun post(): Step2

        /**
         * Begins a PUT Request.
         * @return
         */
        fun put(): Step2

        /**
         * Begins a DELETE Request.
         * @return
         */
        fun delete(): Step2

    }

    interface Step2
    {

        /**
         * No body will be included in the Request.
         */
        fun nothing(): Step3

        /**
         * Includes a JSON String as the body.
         *
         * @param jsonString
         * @throws IllegalArgumentException
         */
        @Throws(IllegalArgumentException::class)
        fun body(@NonEmpty jsonString: String): Step3

        /**
         * Includes a regular Java Value Object (or POJO) as the JSON Request Body.
         *
         * @param pojo
         *
         * @return
         * @throws IllegalArgumentException
         */
        @Throws(IllegalArgumentException::class)
        fun body(@Required pojo: Any): Step3
    }

    interface Step3
    {

        @Throws(IllegalArgumentException::class)
        fun usingHeader(key: String, value: String): Step3

        @Throws(IllegalArgumentException::class)
        fun usingQueryParam(name: String, value: String): Step3

        /**
         * Adds the HTTP 'Accept' Header with multiple values.
         *
         * @param mediaType
         * @param others
         *
         * @return
         * @throws IllegalArgumentException
         */
        @Throws(IllegalArgumentException::class)
        fun accept(mediaType: String, vararg others: String): Step3
        {
            checkThat(mediaType).isA(nonEmptyString())

            val contentTypes = LinkedHashSet<String>()
            contentTypes += mediaType
            others.forEach { contentTypes += it }

            val accepts = contentTypes.joinToString(",")

            return usingHeader("Accept", accepts)
        }

        @Throws(IllegalArgumentException::class)
        fun usingQueryParam(name: String, value: Number): Step3
        {
            return usingQueryParam(name, value.toString())
        }

        @Throws(IllegalArgumentException::class)
        fun usingQueryParam(name: String, value: Boolean): Step3
        {
            return usingQueryParam(name, value.toString())
        }

        @Throws(IllegalArgumentException::class)
        fun followRedirects(maxNumberOfTimes: Int): Step3

        fun followRedirects(): Step3
        {
            return followRedirects(5)
        }

        @Throws(AlchemyHttpException::class)
        fun at(url: URL): HttpResponse

        @Throws(IllegalArgumentException::class, AlchemyHttpException::class, MalformedURLException::class)
        fun at(url: String): HttpResponse
        {
            checkThat(url).isA(validURL())

            return at(URL(url))
        }

        fun onSuccess(onSuccessCallback: OnSuccess<HttpResponse>): Step5<HttpResponse>

        @Throws(IllegalArgumentException::class)
        fun <ResponseType> expecting(classOfResponseType: Class<ResponseType>): Step4<ResponseType>
    }

    interface Step4<ResponseType>
    {

        @Throws(IllegalArgumentException::class, AlchemyHttpException::class)
        fun at(url: URL): ResponseType

        @Throws(AlchemyHttpException::class, MalformedURLException::class)
        fun at(url: String): ResponseType
        {
            checkThat(url).isA(validURL())

            return at(URL(url))
        }

        /**
         * Calling this makes the Http Request Asynchronous. A corresponding
         * [Failure Callback][Step5.onFailure] is
         * required.
         *
         * @param onSuccessCallback Called when the response successfully completes.
         *
         * @return
         */
        fun onSuccess(onSuccessCallback: OnSuccess<ResponseType>): Step5<ResponseType>
    }

    interface Step5<ResponseType>
    {

        /**
         * @param onFailureCallback Called when the request could not be completed successfully.
         *
         * @return
         */
        fun onFailure(onFailureCallback: OnFailure): Step6<ResponseType>
    }

    interface Step6<ResponseType>
    {

        fun at(url: URL)

        @Throws(IllegalArgumentException::class, MalformedURLException::class)
        fun at(url: String)
        {
            checkThat(url).isA(validURL())

            at(URL(url))
        }

    }

    @FunctionalInterface
    interface OnSuccess<ResponseType>
    {

        fun processResponse(response: ResponseType)

        companion object
        {

            val NO_OP = object : OnSuccess<Any>
            {
                override fun processResponse(response: Any)
                {

                }
            }
        }
    }

    @FunctionalInterface
    interface OnFailure
    {

        fun handleError(ex: AlchemyHttpException)

        companion object
        {

            val NO_OP = object : OnFailure
            {
                override fun handleError(ex: AlchemyHttpException)
                {

                }
            }
        }
    }
}