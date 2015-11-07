/*
 * Copyright 2015 SirWellington Tech.
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
package tech.sirwellington.alchemy.http;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.junit.Assert.*;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class HttpVerbTest
{

    @Before
    public void setUp()
    {
    }

    @Test
    public void testGet()
    {
        assertNotNull(HttpVerb.get());
    }

    @Test
    public void testPost()
    {
        assertNotNull(HttpVerb.post());
    }

    @Test
    public void testPut()
    {
        assertNotNull(HttpVerb.put());
    }

    @Test
    public void testDelete()
    {
        assertNotNull(HttpVerb.delete());
    }

}