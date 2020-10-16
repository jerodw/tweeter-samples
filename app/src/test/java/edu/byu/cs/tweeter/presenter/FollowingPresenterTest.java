package edu.byu.cs.tweeter.presenter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.util.Arrays;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.service.FollowingService;
import edu.byu.cs.tweeter.model.service.request.FollowingRequest;
import edu.byu.cs.tweeter.model.service.response.FollowingResponse;

public class FollowingPresenterTest {

    private FollowingRequest request;
    private FollowingResponse response;
    private FollowingService mockFollowingService;
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

        request = new FollowingRequest(currentUser, 3, null);
        response = new FollowingResponse(Arrays.asList(resultUser1, resultUser2, resultUser3), false);

        // Create a mock FollowingService
        mockFollowingService = mock(FollowingService.class);

        // Wrap a FollowingPresenter in a spy that will use the mock service.
        presenter = spy(new FollowingPresenter());
        when(presenter.getFollowingService()).thenReturn(mockFollowingService);
    }

    @Test
    public void testGetFollowing_returnsServiceResult() throws IOException {
        when(mockFollowingService.getFollowees(request)).thenReturn(response);
        assertEquals(response, presenter.getFollowing(request));
    }

    @Test
    public void testGetFollowing_serviceThrowsIOException_presenterThrowsIOException() throws IOException {
        when(mockFollowingService.getFollowees(request)).thenThrow(new IOException());
        assertThrows(IOException.class, () -> {
            presenter.getFollowing(request);
        });
    }
}
