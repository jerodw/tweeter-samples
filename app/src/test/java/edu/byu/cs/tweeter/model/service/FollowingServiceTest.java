package edu.byu.cs.tweeter.model.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.service.request.FollowingRequest;
import edu.byu.cs.tweeter.model.service.response.FollowingResponse;

public class FollowingServiceTest {

    private FollowingRequest validRequest;
    private FollowingRequest invalidRequest;

    private FollowingResponse successResponse;
    private FollowingResponse failureResponse;

    private FollowingService.Observer mockObserver;
    private FollowingService followingServiceSpy;
    private FollowingService.RetrieveFollowingAsyncTask retrieveFollowingAsyncTaskSpy;

    /**
     * Create a FollowingService spy that uses a mock ServerFacade to return known responses to
     * requests.
     */
    @BeforeEach
    public void setup() {
        User currentUser = new User("FirstName", "LastName", null);

        User resultUser1 = new User("FirstName1", "LastName1",
                "https://faculty.cs.byu.edu/~jwilkerson/cs340/tweeter/images/donald_duck.png");
        User resultUser2 = new User("FirstName2", "LastName2",
                "https://faculty.cs.byu.edu/~jwilkerson/cs340/tweeter/images/daisy_duck.png");
        User resultUser3 = new User("FirstName3", "LastName3",
                "https://faculty.cs.byu.edu/~jwilkerson/cs340/tweeter/images/daisy_duck.png");

        // Setup request objects to use in the tests
        validRequest = new FollowingRequest(currentUser.getAlias(), 3, null);
        invalidRequest = new FollowingRequest(null, 0, null);

        // Setup a mock ServerFacade that will return known responses
        successResponse = new FollowingResponse(Arrays.asList(resultUser1, resultUser2, resultUser3), false);
        ServerFacade mockServerFacade = Mockito.mock(ServerFacade.class);
        Mockito.when(mockServerFacade.getFollowees(validRequest)).thenReturn(successResponse);

        failureResponse = new FollowingResponse("An exception occurred");
        Mockito.when(mockServerFacade.getFollowees(invalidRequest)).thenReturn(failureResponse);

        // Setup a mock observer for the FollowingService and GetFolloweesAsyncTask
        mockObserver = Mockito.mock(FollowingService.Observer.class);

        // Create a FollowingService instance and wrap it with a spy that will use the mock server facade
        FollowingService followingService = new FollowingService(mockObserver);
        followingServiceSpy = Mockito.spy(followingService);
        Mockito.when(followingServiceSpy.getServerFacade()).thenReturn(mockServerFacade);

        FollowingService.RetrieveFollowingAsyncTask retrieveFollowingAsyncTask = followingServiceSpy.getRetrieveFollowingAsyncTask();
        retrieveFollowingAsyncTaskSpy = Mockito.spy(retrieveFollowingAsyncTask);
    }

    //
    // Tests that verify that the AsyncTask is invoked by the server.
    //

    /**
     * Verify that the execute method of the {@link edu.byu.cs.tweeter.model.service.FollowingService.RetrieveFollowingAsyncTask}
     * method is called when the {@link FollowingService#getFollowees(FollowingRequest)} method is
     * called.
     */
    @Test
    public void testGetFollowees_callsExecuteOnRetrieveFolloweesAsyncTask() {
        FollowingService.RetrieveFollowingAsyncTask mockRetrieveFollowingAsyncTask = Mockito.mock(FollowingService.RetrieveFollowingAsyncTask.class);
        Mockito.when(followingServiceSpy.getRetrieveFollowingAsyncTask()).thenReturn(mockRetrieveFollowingAsyncTask);

        followingServiceSpy.getFollowees(validRequest);

        Mockito.verify(mockRetrieveFollowingAsyncTask).execute(validRequest);
    }

    //
    // Tests that verify that the AsyncTask functions correctly when invoked. We don't need to test
    // that Google's implementation of AsyncTask works (i.e. no need to verify that calling execute
    // calls doInBackground or onPostExecute).
    //

    /**
     * Verify that for successful requests, the {@link edu.byu.cs.tweeter.model.service.FollowingService.RetrieveFollowingAsyncTask#doInBackground(FollowingRequest...)}
     * method returns the same result as the {@link ServerFacade}.
     */
    @Test
    public void testDoInBackground_validRequest_correctResponse() {
        FollowingResponse response = retrieveFollowingAsyncTaskSpy.doInBackground(validRequest);
        Assertions.assertEquals(successResponse, response);
    }

    /**
     * Verify that for successful requests, the {@link edu.byu.cs.tweeter.model.service.FollowingService.RetrieveFollowingAsyncTask#doInBackground(FollowingRequest...)}
     * method loads the profile image of each user included in the result.
     */
    @Test
    public void testDoInBackground_validRequest_loadsProfileImages() {
        FollowingResponse response = retrieveFollowingAsyncTaskSpy.doInBackground(validRequest);

        for(User user : response.getFollowees()) {
            Assertions.assertNotNull(user.getImageBytes());
        }
    }

    /**
     * Verify that for unsuccessful requests, the {@link edu.byu.cs.tweeter.model.service.FollowingService.RetrieveFollowingAsyncTask#doInBackground(FollowingRequest...)}
     * method returns the same result as the {@link ServerFacade}.
     */
    @Test
    public void testDoInBackground_invalidRequest_returnsNoFollowees() {
        FollowingResponse response = retrieveFollowingAsyncTaskSpy.doInBackground(invalidRequest);
        Assertions.assertEquals(failureResponse, response);
    }

    /**
     * Verify that when an IOException occurs while loading an image, the {@link edu.byu.cs.tweeter.model.service.FollowingService.Observer#handleException(Exception)}
     * method of the Service's observer is called and the {@link edu.byu.cs.tweeter.model.service.FollowingService.Observer#getFollowees(FollowingRequest)}
     * method is not called.
     */
    @Test
    public void testOnPostExecute_exceptionThrownLoadingImages_observerHandleExceptionMethodCalled() throws IOException {
        IOException exception = new IOException();
        Mockito.doThrow(exception).when(retrieveFollowingAsyncTaskSpy).loadImages(successResponse);

        FollowingResponse response = retrieveFollowingAsyncTaskSpy.doInBackground(validRequest);
        retrieveFollowingAsyncTaskSpy.onPostExecute(response);

        Mockito.verify(mockObserver).handleException(exception);
        Mockito.verify(mockObserver, Mockito.times(0)).followeesRetrieved(response);
    }

    /**
     * Verify that the {@link edu.byu.cs.tweeter.model.service.FollowingService.Observer#getFollowees(FollowingRequest)}
     * method of the Service's observer is called for successful results and the
     * {@link edu.byu.cs.tweeter.model.service.FollowingService.Observer#handleException(Exception)}
     * method is not called.
     */
    @Test
    public void testOnPostExecute_noExceptionThrown_observerFolloweesRetrievedMethodCalled() {
        FollowingResponse response = retrieveFollowingAsyncTaskSpy.doInBackground(validRequest);
        retrieveFollowingAsyncTaskSpy.onPostExecute(response);

        Mockito.verify(mockObserver).followeesRetrieved(response);
        Mockito.verify(mockObserver, Mockito.times(0)).handleException(Mockito.any(IOException.class));
    }
}
