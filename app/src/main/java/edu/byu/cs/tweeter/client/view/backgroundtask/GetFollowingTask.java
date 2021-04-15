package edu.byu.cs.tweeter.client.view.backgroundtask;

import android.os.Handler;

import java.io.IOException;

import edu.byu.cs.tweeter.model.net.TweeterRemoteException;
import edu.byu.cs.tweeter.model.service.request.FollowingRequest;
import edu.byu.cs.tweeter.model.service.response.FollowingResponse;
import edu.byu.cs.tweeter.client.presenter.FollowingPresenter;
import edu.byu.cs.tweeter.client.view.main.following.FollowingFragment;

/**
 * A task for retrieving followees for a user. The task is intended to run on a background thread.
 */
public class GetFollowingTask extends BackgroundTask {

    private final FollowingRequest request;
    private final FollowingPresenter presenter;

    /**
     * Creates an instance.
     *
     * @param request the request.
     * @param presenter the presenter from whom this task should retrieve followees.
     * @param messageHandler the messageHandler that handles the result of this task.
     */
    public GetFollowingTask(FollowingRequest request, FollowingPresenter presenter, Handler messageHandler) {
        super(messageHandler);
        this.request = request;
        this.presenter = presenter;
    }

    /**
     * Invoked on the background thread to retrieve followees.
     */
    @Override
    public void run() {
        try {
            FollowingResponse response = presenter.getFollowing(request);
            sendMessage(FollowingFragment.FOLLOWING_RESPONSE_KEY, response);
        } catch (IOException | TweeterRemoteException ex) {
            sendMessage(FollowingFragment.EXCEPTION_KEY ,ex);
        }
    }
}
