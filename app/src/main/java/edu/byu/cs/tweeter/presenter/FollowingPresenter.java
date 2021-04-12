package edu.byu.cs.tweeter.presenter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.service.FollowingService;
import edu.byu.cs.tweeter.model.service.request.FollowingRequest;
import edu.byu.cs.tweeter.model.service.response.FollowingResponse;
import edu.byu.cs.tweeter.presenter.backgroundtask.GetFollowingTask;

/**
 * The presenter for the "following" functionality of the application.
 */
public class FollowingPresenter {

    private static final String LOG_TAG = "FollowingPresenter";
    private static final int PAGE_SIZE = 10;

    private final View view;
    private final User user;
    private final AuthToken authToken;

    private User lastFollowee;
    private boolean hasMorePages = true;
    private boolean isLoading = false;

    /**
     * The interface by which this presenter communicates with it's view.
     */
    public interface View {
        void setLoading(boolean value);
        void addItems(List<User> newUsers);
        void displayErrorMessage(String message);
    }

    /**
     * Creates an instance.
     *
     * @param view the view for which this class is the presenter.
     * @param user the user that is currently logged in.
     * @param authToken the auth token for the current session.
     */
    public FollowingPresenter(View view, User user, AuthToken authToken) {
        this.view = view;
        this.user = user;
        this.authToken = authToken;
    }

    User getLastFollowee() {
        return lastFollowee;
    }

    boolean isHasMorePages() {
        return hasMorePages;
    }

    boolean isLoading() {
        return isLoading;
    }

    /**
     * Called by the view to request that another page of "following" users be loaded.
     */
    public void loadMoreItems() {
        if (!isLoading && hasMorePages) {
            isLoading = true;
            view.setLoading(true);

            FollowingRequest request = new FollowingRequest(user.getAlias(), PAGE_SIZE, (lastFollowee == null ? null : lastFollowee.getAlias()));
            MessageHandler messageHandler = new MessageHandler(Looper.getMainLooper(), this);
            GetFollowingTask getFollowingTask = new GetFollowingTask(this, messageHandler, request);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(getFollowingTask);
        }
    }

    /**
     * Returns the users that the user specified in the request is following. Uses information in
     * the request object to limit the number of followees returned and to return the next set of
     * followees after any that were returned in a previous request.
     *
     * @param request contains the data required to fulfill the request.
     * @return the followees.
     */
    public FollowingResponse getFollowing(FollowingRequest request) throws IOException {
        FollowingService followingService = getFollowingService();
        return followingService.getFollowees(request);
    }

    /**
     * Returns an instance of {@link FollowingService}. Allows mocking of the FollowingService class
     * for testing purposes. All usages of FollowingService should get their FollowingService
     * instance from this method to allow for mocking of the instance.
     *
     * @return the instance.
     */
    FollowingService getFollowingService() {
        return new FollowingService();
    }

    /**
     * Handles new following data received from the background task. Loads the new followees
     * and removes the loading footer.
     *
     * @param followingResponse the asynchronous response to the request to load more items.
     */
    public void followeesRetrieved(FollowingResponse followingResponse) {
        List<User> followees = followingResponse.getFollowees();

        lastFollowee = (followees.size() > 0) ? followees.get(followees.size() -1) : null;
        hasMorePages = followingResponse.getHasMorePages();

        view.setLoading(false);
        view.addItems(followees);
        isLoading = false;
    }

    /**
     * Handles exceptions thrown by the GetFollowingTask.
     *
     * @param exception the exception.
     */
    public void handleException(Exception exception) {
        Log.e(LOG_TAG, exception.getMessage(), exception);
        
        view.setLoading(false);
        view.displayErrorMessage(exception.getMessage());
        isLoading = false;
    }

    /**
     * Handles the message from the background task indicating that the task is done, by invoking
     * methods on the presenter.
     */
    private static class MessageHandler extends Handler {

        private final FollowingPresenter presenter;

        MessageHandler(Looper looper, FollowingPresenter presenter) {
            super(looper);
            this.presenter = presenter;
        }

        @Override
        public void handleMessage(Message message) {
            Bundle bundle = message.getData();
            Exception exception = (Exception) bundle.getSerializable(GetFollowingTask.EXCEPTION_KEY);

            if(exception == null) {
                FollowingResponse followingResponse = (FollowingResponse) bundle.getSerializable(GetFollowingTask.FOLLOWING_RESPONSE_KEY);
                presenter.followeesRetrieved(followingResponse);
            } else {
                presenter.handleException(exception);
            }
        }
    }
}
