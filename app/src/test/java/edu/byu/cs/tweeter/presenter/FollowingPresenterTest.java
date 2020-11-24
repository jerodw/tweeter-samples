package edu.byu.cs.tweeter.presenter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.service.FollowingService;
import edu.byu.cs.tweeter.model.service.request.FollowingRequest;
import edu.byu.cs.tweeter.model.service.response.FollowingResponse;

public class FollowingPresenterTest {

    private FollowingRequest request;
    private FollowingResponse response;
    private FollowingPresenter.View mockView;
    private FollowingServiceDouble followingServiceDouble;
    private FollowingPresenter presenter;

    @BeforeEach
    public void setup() {
        User currentUser = new User("FirstName", "LastName", null);

        User resultUser1 = new User("FirstName1", "LastName1",
                "https://faculty.cs.byu.edu/~jwilkerson/cs340/tweeter/images/donald_duck.png");
        User resultUser2 = new User("FirstName2", "LastName2",
                "https://faculty.cs.byu.edu/~jwilkerson/cs340/tweeter/images/daisy_duck.png");
        User resultUser3 = new User("FirstName3", "LastName3",
                "https://faculty.cs.byu.edu/~jwilkerson/cs340/tweeter/images/daisy_duck.png");

        request = new FollowingRequest(currentUser.getAlias(), 3, null);
        response = new FollowingResponse(Arrays.asList(resultUser1, resultUser2, resultUser3), false);

        // Create a mock following service view
        mockView = Mockito.mock(FollowingPresenter.View.class);

        // Wrap a FollowingPresenter in a spy that will use the following service double.
        presenter = Mockito.spy(new FollowingPresenter(mockView));

        // Create a FollowingService double that will return results to the presenter
        followingServiceDouble = new FollowingServiceDouble(presenter);

        // Make the presenter use the following service double as it's service
        Mockito.doReturn(followingServiceDouble).when(presenter).getFollowingService(Mockito.any());
    }

    /**
     * A double for {@link FollowingService} that synchronously simulates the behavior of a
     * FollowingService object by calling methods on it's Observer instance when it's
     * {@link #getFollowees(FollowingRequest)} method is called. Specify responses to return for
     * specific requests by calling {@link #setResponseForRequest(FollowingRequest, FollowingResponse)}.
     * Specify exceptions to return for specific requests by calling
     * {@link #setExceptionForRequest(FollowingRequest, Exception)}.
     * {@link #setResponseForRequest(FollowingRequest, FollowingResponse)}.
     */
    private static class FollowingServiceDouble extends FollowingService {

        private final Observer observer;
        private final Map<FollowingRequest, FollowingResponse> responsesByRequest = new HashMap<>();
        private final Map<FollowingRequest, Exception> exceptionsByRequest = new HashMap<>();

        /**
         * Creates an instance.
         *
         * @param observer the observer who wants to be notified when any asynchronous operations complete.
         */
        public FollowingServiceDouble(Observer observer) {
            super(observer);
            this.observer = observer;
        }

        private void setResponseForRequest(FollowingRequest request, FollowingResponse response) {
            responsesByRequest.put(request, response);
        }

        private void setExceptionForRequest(FollowingRequest request, Exception exception) {
            exceptionsByRequest.put(request, exception);
        }

        /**
         * Instead of creating an AsyncTask as the real service would do, just call either the
         * {@link FollowingPresenter.View#followeesRetrieved(FollowingResponse)} or
         * {@link FollowingPresenter.View#handleException(Exception)} method of the observer.
         *
         * @param request contains the data required to fulfill the request.
         */
        @Override
        public void getFollowees(FollowingRequest request) {
            if(!responsesByRequest.containsKey(request) && !exceptionsByRequest.containsKey(request)) {
                throw new IllegalStateException("Response or exception not set for request: " + request);
            }

            if(responsesByRequest.containsKey(request)) {
                observer.followeesRetrieved(responsesByRequest.get(request));
            } else {
                observer.handleException(exceptionsByRequest.get(request));
            }
        }
    }

    @Test
    public void testGetFollowing_viewNotifiedOfFollowees() {
        // Set the double to return the specified response when it gets the specified request
        followingServiceDouble.setResponseForRequest(request, response);

        presenter.getFollowing(request);
        Mockito.verify(mockView).followeesRetrieved(response);
    }

    @Test
    public void testGetFollowing_serviceThrowsIOException_viewNotifiedOfIOException() {
        // Set the double to return the specified exception when it gets the specified request
        IOException exception = new IOException();
        followingServiceDouble.setExceptionForRequest(request, exception);

        presenter.getFollowing(request);
        Mockito.verify(mockView).handleException(exception);
    }
}
