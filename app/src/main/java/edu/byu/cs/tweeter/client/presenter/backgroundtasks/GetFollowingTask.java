package edu.byu.cs.tweeter.client.presenter.backgroundtasks;

import android.os.Handler;

import edu.byu.cs.tweeter.model.service.request.FollowingRequest;
import edu.byu.cs.tweeter.model.service.response.FollowingResponse;
import edu.byu.cs.tweeter.client.presenter.FollowingPresenter;

/**
 * A task for retrieving followees for a user.
 */
public class GetFollowingTask extends BackgroundTask {

    public static final String EXCEPTION_KEY = "ExceptionKey";
    public static  final String FOLLOWING_RESPONSE_KEY = "FollowingResponseKey";

    private final FollowingPresenter presenter;
    private final FollowingRequest request;

    /**
     * Creates an instance.
     *
     * @param presenter the presenter from whom this task should retrieve followees.
     * @param messageHandler the handler that wants to be notified when the task completes.
     * @param request the request to be processed on a background thread.
     */
    public GetFollowingTask(FollowingPresenter presenter, Handler messageHandler, FollowingRequest request) {
        super(messageHandler);

        this.presenter = presenter;
        this.request = request;
    }

    /**
     * Invoked on the background thread to retrieve followees.
     */
    @Override
    public void run() {
        try {
            FollowingResponse response = presenter.getFollowing(request);
            sendMessage(FOLLOWING_RESPONSE_KEY, response);
        } catch (Exception ex) {
            sendMessage(EXCEPTION_KEY, ex);
        }
    }
}
