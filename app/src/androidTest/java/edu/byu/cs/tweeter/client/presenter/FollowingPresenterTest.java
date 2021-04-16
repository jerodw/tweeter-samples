package edu.byu.cs.tweeter.client.presenter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.client.model.service.FollowingServiceProxy;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.TweeterRemoteException;
import edu.byu.cs.tweeter.model.service.response.FollowingResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FollowingPresenterTest {

    private static final String MALE_IMAGE_URL = "https://faculty.cs.byu.edu/~jwilkerson/cs340/tweeter/images/donald_duck.png";
    private static final String FEMALE_IMAGE_URL = "https://faculty.cs.byu.edu/~jwilkerson/cs340/tweeter/images/daisy_duck.png";

    private final User user1 = new User("Allen", "Anderson", MALE_IMAGE_URL);
    private final User user2 = new User("Amy", "Ames", FEMALE_IMAGE_URL);
    private final User user3 = new User("Bob", "Bobson", MALE_IMAGE_URL);
    private final User user4 = new User("Bonnie", "Beatty", FEMALE_IMAGE_URL);
    private final User user5 = new User("Chris", "Colston", MALE_IMAGE_URL);
    private final User user6 = new User("Cindy", "Coats", FEMALE_IMAGE_URL);
    private final User user7 = new User("Dan", "Donaldson", MALE_IMAGE_URL);
    private final User user8 = new User("Dee", "Dempsey", FEMALE_IMAGE_URL);
    private final User user9 = new User("Elliott", "Enderson", MALE_IMAGE_URL);
    private final User user10 = new User("Elizabeth", "Engle", FEMALE_IMAGE_URL);
    private final User user11 = new User("Frank", "Frandson", MALE_IMAGE_URL);
    private final User user12 = new User("Fran", "Franklin", FEMALE_IMAGE_URL);
    private final User user13 = new User("Gary", "Gilbert", MALE_IMAGE_URL);
    private final User user14 = new User("Giovanna", "Giles", FEMALE_IMAGE_URL);
    private final User user15 = new User("Henry", "Henderson", MALE_IMAGE_URL);
    private final User user16 = new User("Helen", "Hopwell", FEMALE_IMAGE_URL);
    private final User user17 = new User("Igor", "Isaacson", MALE_IMAGE_URL);
    private final User user18 = new User("Isabel", "Isaacson", FEMALE_IMAGE_URL);
    private final User user19 = new User("Justin", "Jones", MALE_IMAGE_URL);
    private final User user20 = new User("Jill", "Johnson", FEMALE_IMAGE_URL);
    private final User user21 = new User("John", "Brown", MALE_IMAGE_URL);

    private ServerFacade serverFacadeMock;
    private FollowingServiceProxy followingServiceProxySpy;
    private FollowingPresenter followingPresenterSpy;
    private FollowingPresenter.View followingViewMock;
    private CountDownLatch latch;

    @Before
    public void setup() {
        serverFacadeMock = Mockito.mock(ServerFacade.class);

        followingServiceProxySpy = Mockito.spy(new FollowingServiceProxy());

        followingViewMock = Mockito.mock(FollowingPresenter.View.class);

        followingPresenterSpy = Mockito.spy(new FollowingPresenter(followingViewMock,
                new User("", "", ""), new AuthToken()));

        Mockito.when(followingServiceProxySpy.getServerFacade()).thenReturn(serverFacadeMock);
        Mockito.when(followingPresenterSpy.getFollowingService()).thenReturn(followingServiceProxySpy);

        resetLatch();
        Answer<Void> threadSyncAnswer = invocation -> {
            latch.countDown();
            return null;
        };
        Mockito.doAnswer(threadSyncAnswer).when(followingViewMock).addItems(Mockito.anyList());
        Mockito.doAnswer(threadSyncAnswer).when(followingViewMock).displayErrorMessage(Mockito.anyString());
    }

    private void resetLatch() {
        latch = new CountDownLatch(1);
    }

    @Test
    public void testInitialPresenterState() {
        assertNull(followingPresenterSpy.getLastFollowee());
        assertTrue(followingPresenterSpy.isHasMorePages());
        assertFalse(followingPresenterSpy.isLoading());
    }

    @Test
    public void testLoadMoreItems_noFolloweesForUser()
            throws InterruptedException, IOException, TweeterRemoteException {

        FollowingResponse response1 = new FollowingResponse(Collections.emptyList(), false);
        Mockito.doReturn(response1).when(serverFacadeMock).getFollowees(Mockito.any(), Mockito.any());

        followingPresenterSpy.loadMoreItems();
        latch.await();

        assertNull(followingPresenterSpy.getLastFollowee());
        assertFalse(followingPresenterSpy.isHasMorePages());
        assertFalse(followingPresenterSpy.isLoading());

        Mockito.verify(followingViewMock).setLoading(true);
        Mockito.verify(followingViewMock).setLoading(false);
        Mockito.verify(followingViewMock).addItems(Collections.emptyList());
    }

    @Test
    public void testLoadMoreItems_oneFollowerForUser_limitGreaterThanUsers()
            throws InterruptedException, IOException, TweeterRemoteException {

        FollowingResponse response1 = new FollowingResponse(Collections.singletonList(user2), false);
        Mockito.doReturn(response1).when(serverFacadeMock).getFollowees(Mockito.any(), Mockito.any());

        followingPresenterSpy.loadMoreItems();
        latch.await();

        assertEquals(user2, followingPresenterSpy.getLastFollowee());
        assertFalse(followingPresenterSpy.isHasMorePages());
        assertFalse(followingPresenterSpy.isLoading());

        Mockito.verify(followingViewMock).setLoading(true);
        Mockito.verify(followingViewMock).setLoading(false);
        Mockito.verify(followingViewMock).addItems(Collections.singletonList(user2));
    }

    @Test
    public void testLoadMoreItems_twoFollowersForUser_limitEqualsUsers()
            throws InterruptedException, IOException, TweeterRemoteException {

        FollowingResponse response1 = new FollowingResponse(Arrays.asList(user2, user3), false);
        Mockito.doReturn(response1).when(serverFacadeMock).getFollowees(Mockito.any(), Mockito.any());

        followingPresenterSpy.loadMoreItems();
        latch.await();

        assertEquals(user3, followingPresenterSpy.getLastFollowee());
        assertFalse(followingPresenterSpy.isHasMorePages());
        assertFalse(followingPresenterSpy.isLoading());

        Mockito.verify(followingViewMock).setLoading(true);
        Mockito.verify(followingViewMock).setLoading(false);
        Mockito.verify(followingViewMock).addItems(Arrays.asList(user2, user3));
    }

    @Test
    public void testLoadMoreItems_limitLessThanUsers_endsOnPageBoundary()
            throws InterruptedException, IOException, TweeterRemoteException {

        FollowingResponse response1 = new FollowingResponse(Arrays.asList(user1, user2, user3,
                user4, user5, user6, user7, user8, user9, user10), true);
        FollowingResponse response2 = new FollowingResponse(Arrays.asList(user11, user12, user13,
                user14, user15, user16, user17, user18, user19, user20), false);
        Mockito.doReturn(response1, response2).when(serverFacadeMock).getFollowees(Mockito.any(), Mockito.any());

        followingPresenterSpy.loadMoreItems();
        latch.await();

        assertEquals(user10, followingPresenterSpy.getLastFollowee());
        assertTrue(followingPresenterSpy.isHasMorePages());
        assertFalse(followingPresenterSpy.isLoading());

        resetLatch();
        followingPresenterSpy.loadMoreItems();
        latch.await();

        assertEquals(user20, followingPresenterSpy.getLastFollowee());
        assertFalse(followingPresenterSpy.isHasMorePages());
        assertFalse(followingPresenterSpy.isLoading());

        Mockito.verify(followingViewMock, Mockito.times(2)).setLoading(true);
        Mockito.verify(followingViewMock, Mockito.times(2)).setLoading(false);
        Mockito.verify(followingViewMock).addItems(Arrays.asList(user1, user2, user3, user4,
                user5, user6, user7, user8, user9, user10));
        Mockito.verify(followingViewMock).addItems(Arrays.asList(user11, user12, user13, user14,
                user15, user16, user17, user18, user19, user20));
    }

    @Test
    public void testLoadMoreItems_limitLessThanUsers_notEndsOnPageBoundary()
            throws InterruptedException, IOException, TweeterRemoteException {

        FollowingResponse response1 = new FollowingResponse(Arrays.asList(user1, user2, user3,
                user4, user5, user6, user7, user8, user9, user10), true);
        FollowingResponse response2 = new FollowingResponse(Arrays.asList(user11, user12, user13,
                user14, user15, user16, user17, user18, user19, user20), true);
        FollowingResponse response3 = new FollowingResponse(Collections.singletonList(user21), false);
        Mockito.doReturn(response1, response2, response3).when(serverFacadeMock).getFollowees(Mockito.any(), Mockito.any());

        followingPresenterSpy.loadMoreItems();
        latch.await();

        assertEquals(user10, followingPresenterSpy.getLastFollowee());
        assertTrue(followingPresenterSpy.isHasMorePages());
        assertFalse(followingPresenterSpy.isLoading());

        resetLatch();
        followingPresenterSpy.loadMoreItems();
        latch.await();

        assertEquals(user20, followingPresenterSpy.getLastFollowee());
        assertTrue(followingPresenterSpy.isHasMorePages());
        assertFalse(followingPresenterSpy.isLoading());

        resetLatch();
        followingPresenterSpy.loadMoreItems();
        latch.await();

        assertEquals(user21, followingPresenterSpy.getLastFollowee());
        assertFalse(followingPresenterSpy.isHasMorePages());
        assertFalse(followingPresenterSpy.isLoading());

        Mockito.verify(followingViewMock, Mockito.times(3)).setLoading(true);
        Mockito.verify(followingViewMock, Mockito.times(3)).setLoading(false);
        Mockito.verify(followingViewMock).addItems(Arrays.asList(user1, user2, user3, user4,
                user5, user6, user7, user8, user9, user10));
        Mockito.verify(followingViewMock).addItems(Arrays.asList(user11, user12, user13, user14,
                user15, user16, user17, user18, user19, user20));
        Mockito.verify(followingViewMock).addItems(Collections.singletonList(user21));
    }
}
