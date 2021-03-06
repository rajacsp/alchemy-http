/*
 * Copyright © 2018. Sir Wellington.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.sirwellington.alchemy.http

import sir.wellington.alchemy.collections.maps.Maps
import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.PRODUCT
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.nonEmptyString

/**
 *
 * @author SirWellington
 */
@Internal
@BuilderPattern(role = PRODUCT)
@ThreadSafe
internal class AlchemyHttpImpl(defaultHeaders: Map<String, String>,
                               private val stateMachine: AlchemyHttpStateMachine) : AlchemyHttp
{

    override val defaultHeaders = Maps.immutableCopyOf(defaultHeaders)

    override fun usingDefaultHeader(key: String, value: String): AlchemyHttp
    {
        checkThat(key)
                .usingMessage("Key is empty")
                .isA(nonEmptyString())

        val copy = defaultHeaders.plus(Pair(key, value))

        return AlchemyHttpImpl(defaultHeaders = Maps.immutableCopyOf(copy), stateMachine = stateMachine)
    }

    override fun go(): AlchemyRequestSteps.Step1
    {
        val initialRequest = HttpRequest.Builder
                                        .newInstance()
                                        .usingRequestHeaders(defaultHeaders)
                                        .build()

        return stateMachine.begin(initialRequest)
    }

    override fun toString(): String
    {
        return "AlchemyHttp{defaultHeaders=$defaultHeaders, stateMachine=$stateMachine}"
    }

}
