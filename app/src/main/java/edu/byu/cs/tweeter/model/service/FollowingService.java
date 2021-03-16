package edu.byu.cs.tweeter.model.service;

import android.os.AsyncTask;

import java.io.IOException;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.service.request.FollowingRequest;
import edu.byu.cs.tweeter.model.service.response.FollowingResponse;
import edu.byu.cs.tweeter.util.ByteArrayUtils;

/**
 * Contains the business logic for getting the users a user is following.
 */
public class FollowingService {

    private final Observer observer;

    /**
     * An observer interface to be implemented by observers who want to be notified when
     * asynchronous operations complete.
     */
    public interface Observer {
        void followeesRetrieved(FollowingResponse followingResponse);
        void handleException(Exception exception);
    }

    /**
     * Creates an instance.
     *
     * @param observer the observer who wants to be notified when any asynchronous operations complete.
     */
    public FollowingService(Observer observer) {
        // An assertion would be better, but Android doesn't support Java assertions
        if(observer == null) {
            throw new NullPointerException();
        }

        this.observer = observer;
    }

    /**
     * Requests the users that the user specified in the request is following. Uses information in
     * the request object to limit the number of followees returned and to return the next set of
     * followees after any that were returned in a previous request. Uses the {@link ServerFacade}
     * to get the followees from the server. This is an asynchronous operation.
     *
     * @param request contains the data required to fulfill the request.
     */
    public void getFollowees(FollowingRequest request) {
        AsyncTask<FollowingRequest, Void, FollowingResponse> followingTask = getRetrieveFollowingAsyncTask();
        followingTask.execute(request);
    }

    /**
     * Returns an instance of {@link ServerFacade}. Allows mocking of the ServerFacade class for
     * testing purposes. All usages of ServerFacade should get their instance from this method to
     * allow for proper mocking.
     *
     * @return the instance.
     */
    public ServerFacade getServerFacade() {
        return new ServerFacade();
    }

    /**
     * Returns an instance of {@link RetrieveFollowingAsyncTask}. Allows mocking of the
     * RetrieveFollowingAsyncTask class for testing purposes. All usages of
     * RetrieveFollowingAsyncTask should get their instance from this method to allow for proper
     * mocking.
     *
     * @return the instance.
     */
    RetrieveFollowingAsyncTask getRetrieveFollowingAsyncTask() {
        return new RetrieveFollowingAsyncTask(observer);
    }

    /**
     * The AsyncTask that makes the request to retrieve followees on a background thread.
     */
    class RetrieveFollowingAsyncTask extends AsyncTask<FollowingRequest, Void, FollowingResponse> {

        private final Observer observer;
        private Exception exception;

        RetrieveFollowingAsyncTask(Observer observer) {
            this.observer = observer;
        }

        /**
         * The method that is invoked on the background thread to retrieve followees. This method is
         * invoked indirectly by calling {@link #execute(FollowingRequest...)}.
         *
         * @param followingRequests the request object (there will only be one).
         * @return the response.
         */
        @Override
        protected FollowingResponse doInBackground(FollowingRequest... followingRequests) {
            FollowingResponse response = getServerFacade().getFollowees(followingRequests[0]);

            if(response.isSuccess()) {
                try {
                    loadImages(response);
                } catch (IOException ex) {
                    exception = ex;
                }
            }

            return response;
        }

        /**
         * Loads the profile image for each followee included in the response.
         *
         * @param response the response from the followee request.
         */
        void loadImages(FollowingResponse response) throws IOException {
            for(User user : response.getFollowees()) {
                byte [] bytes = ByteArrayUtils.bytesFromUrl(user.getImageUrl());
                user.setImageBytes(bytes);
            }
        }

        /**
         * Notifies the observer (on the thread of the invoker of the
         * {@link #execute(FollowingRequest...)} method) when the task completes.
         *
         * @param followingResponse the response that was received by the task.
         */
        @Override
        protected void onPostExecute(FollowingResponse followingResponse) {
            if(exception != null) {
                observer.handleException(exception);
            } else {
                observer.followeesRetrieved(followingResponse);
            }
        }
    }
}
