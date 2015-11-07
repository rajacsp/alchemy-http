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

import com.google.gson.Gson;
import java.util.concurrent.ExecutorService;
import org.apache.http.client.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import tech.sirwellington.alchemy.http.AlchemyRequest.OnFailure;
import tech.sirwellington.alchemy.http.AlchemyRequest.OnSuccess;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.http.exceptions.JsonException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class AlchemyMachineImplTest
{

    @Mock
    private HttpClient apacheClient;

    @Mock
    private ExecutorService executorService;

    @Captor
    private ArgumentCaptor<Runnable> taskCaptor;

    private Gson gson;

    private AlchemyHttpStateMachine instance;

    @Mock
    private HttpRequest mockRequest;

    private TestRequest request;

    @Mock
    private OnSuccess<TestPojo> onSuccess;

    @Mock
    private OnFailure onFailure;

    private final Class<TestPojo> responseClass = TestPojo.class;

    private TestPojo pojo;

    @Mock
    private HttpVerb verb;

    @Mock
    private HttpResponse response;

    @Before
    public void setUp() throws Exception
    {
        gson = Constants.getDefaultGson();
        request = new TestRequest();

        instance = new AlchemyMachineImpl(apacheClient, executorService, gson);
        verifyZeroInteractions(apacheClient, executorService);

        setupVerb();
        setupResponse();
    }

    private void setupResponse()
    {
        pojo = TestPojo.generate();

        when(response.isOk()).thenReturn(true);
        when(response.bodyAs(responseClass))
                .thenReturn(pojo);
    }

    private void setupVerb()
    {
        when(verb.execute(apacheClient, request))
                .thenReturn(response);

        request.verb = this.verb;

    }

    @Test
    public void testConstructor()
    {
        assertThrows(() -> new AlchemyMachineImpl(null, null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new AlchemyMachineImpl(apacheClient, null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new AlchemyMachineImpl(null, executorService, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new AlchemyMachineImpl(null, null, gson))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testBegin()
    {
        AlchemyRequest.Step1 step1 = instance.begin(mockRequest);
        assertThat(step1, notNullValue());

        //Edge cases
        assertThrows(() -> instance.begin(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testJumpToStep2()
    {
        AlchemyRequest.Step2 step2 = instance.jumpToStep2(mockRequest);
        assertThat(step2, notNullValue());

        //Edge cases
        assertThrows(() -> instance.jumpToStep2(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testJumpToStep3()
    {
        AlchemyRequest.Step3 step3 = instance.jumpToStep3(mockRequest);
        assertThat(step3, notNullValue());

        //Edge cases
        assertThrows(() -> instance.jumpToStep3(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testJumpToStep4()
    {
        AlchemyRequest.Step4<TestPojo> step4 = instance.jumpToStep4(mockRequest, responseClass);
        assertThat(step4, notNullValue());

        //Edge cases
        assertThrows(() -> instance.jumpToStep4(null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep4(mockRequest, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep4(mockRequest, Void.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testJumpToStep5()
    {
        AlchemyRequest.Step5<TestPojo> step5 = instance.jumpToStep5(mockRequest, responseClass, onSuccess);
        assertThat(step5, notNullValue());

        //Edge cases
        assertThrows(() -> instance.jumpToStep5(null, null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep5(mockRequest, null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep5(mockRequest, responseClass, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep5(mockRequest, null, onSuccess))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep5(null, responseClass, onSuccess))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep5(mockRequest, Void.class, mock(OnSuccess.class)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testJumpToStep6()
    {
        AlchemyRequest.Step6<TestPojo> step6 = instance.jumpToStep6(mockRequest, responseClass, onSuccess, onFailure);
        assertThat(step6, notNullValue());

        //Edge cases
        assertThrows(() -> instance.jumpToStep6(mockRequest, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep6(mockRequest, responseClass, null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep6(mockRequest, responseClass, onSuccess, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep6(null, responseClass, onSuccess, onFailure))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep6(null, null, onSuccess, onFailure))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep6(mockRequest, Void.class, mock(OnSuccess.class), onFailure))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Repeat(200)
    @Test
    public void testExecuteSync()
    {

        HttpResponse result = instance.executeSync(request);

        assertThat(result, is(response));
    }

    @Repeat(200)
    @Test
    public void testExecuteSyncWithCustomClass()
    {
        when(response.bodyAs(responseClass)).thenReturn(pojo);

        TestPojo result = instance.executeSync(request, responseClass);
        assertThat(result, is(pojo));
    }

    @Test
    public void testExecuteSyncWhenVerbFails()
    {

        when(verb.execute(apacheClient, request))
                .thenThrow(new RuntimeException());

        assertThrows(() -> instance.executeSync(request))
                .isInstanceOf(AlchemyHttpException.class);

        //Reset and do another assertion
        reset(verb);

        when(verb.execute(apacheClient, request))
                .thenThrow(new AlchemyHttpException(request));

        assertThrows(() -> instance.executeSync(request))
                .isInstanceOf(AlchemyHttpException.class);
    }

    @Test
    public void testExecuteSyncWithBadArguments()
    {
        assertThrows(() -> instance.executeSync(null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.executeSync(null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.executeSync(mockRequest, Void.class))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.executeSync(request, Void.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testExecuteWhenVerbReturnsNullResponse()
    {
        when(verb.execute(apacheClient, request))
                .thenReturn(null);

        assertThrows(() -> instance.executeSync(request, responseClass))
                .isInstanceOf(AlchemyHttpException.class);
    }

    @Test
    public void testExecuteSyncWhenClassOfResponseTypeIsString()
    {
        instance.executeSync(request, String.class);
        verify(response).bodyAsString();
    }

    @Repeat(200)
    @Test
    public void testExecuteWhenResponseNotOk()
    {
        when(response.isOk()).thenReturn(false);

        assertThrows(() -> instance.executeSync(request, responseClass))
                .isInstanceOf(AlchemyHttpException.class);
    }

    @Test
    public void testExecuteWhenCastingToResponseClassFails()
    {
        when(response.bodyAs(responseClass))
                .thenThrow(new JsonException());

        assertThrows(() -> instance.executeSync(request, responseClass))
                .isInstanceOf(AlchemyHttpException.class);
    }

    @Repeat(200)
    @Test
    public void testExecuteAsync() throws Exception
    {

        instance.executeAsync(request, responseClass, onSuccess, onFailure);

        verify(executorService).submit(taskCaptor.capture());

        Runnable task = taskCaptor.getValue();
        assertThat(task, notNullValue());

        task.run();
        verify(onSuccess).processResponse(pojo);
    }

    @Test
    public void testExecuteAsyncWhenFails()
    {
        AlchemyHttpException ex = new AlchemyHttpException();

        when(verb.execute(apacheClient, request))
                .thenThrow(ex);

        instance.executeAsync(request, responseClass, onSuccess, onFailure);

        verify(executorService).submit(taskCaptor.capture());

        Runnable task = taskCaptor.getValue();
        assertThat(task, notNullValue());

        task.run();
        verify(onFailure).handleError(ex);
    }

    @Test
    public void testExecuteAsyncWhenRuntimeExceptionHappens()
    {
        when(verb.execute(apacheClient, request))
                .thenThrow(new RuntimeException());

        instance.executeAsync(request, responseClass, onSuccess, onFailure);

        verify(executorService).submit(taskCaptor.capture());

        Runnable task = taskCaptor.getValue();
        assertThat(task, notNullValue());

        task.run();
        verify(onFailure).handleError(any());

    }

    @Test
    public void testExecuteAsyncWhenOnSuccessFails()
    {
        doThrow(new RuntimeException())
                .when(onSuccess)
                .processResponse(pojo);

        instance.executeAsync(request, responseClass, onSuccess, onFailure);

        verify(executorService).submit(taskCaptor.capture());

        Runnable task = taskCaptor.getValue();
        assertThat(task, notNullValue());
        task.run();
        verify(onFailure).handleError(any());
    }

    @Test
    public void testExecuteAsyncWithBadArgs()
    {

        assertThrows(() -> instance.executeAsync(mockRequest, responseClass, onSuccess, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.executeAsync(mockRequest, responseClass, null, onFailure))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.executeAsync(mockRequest, null, onSuccess, onFailure))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.executeAsync(null, responseClass, onSuccess, onFailure))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.executeAsync(mockRequest, Void.class, mock(OnSuccess.class), onFailure))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testToString()
    {
        String toString = instance.toString();
        assertThat(toString, not(isEmptyOrNullString()));
    }

}