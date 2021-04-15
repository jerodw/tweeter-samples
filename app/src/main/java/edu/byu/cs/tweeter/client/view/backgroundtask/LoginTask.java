package edu.byu.cs.tweeter.client.view.backgroundtask;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.TweeterRemoteException;
import edu.byu.cs.tweeter.model.service.request.LoginRequest;
import edu.byu.cs.tweeter.model.service.response.LoginResponse;
import edu.byu.cs.tweeter.client.presenter.LoginPresenter;
import edu.byu.cs.tweeter.client.util.ByteArrayUtils;
import edu.byu.cs.tweeter.client.view.LoginActivity;

public class LoginTask extends BackgroundTask {

    private final LoginRequest request;
    private final LoginPresenter presenter;

    /**
     * Creates an instance.
     *
     * @param request the request.
     * @param presenter the presenter this task should use to login.
     * @param messageHandler the messageHandler that handles the result of this task.
     */
    public LoginTask(LoginRequest request, LoginPresenter presenter, Handler messageHandler) {
        super(messageHandler);
        this.request = request;
        this.presenter = presenter;
    }

    /**
     * Invoked on a background thread to log the user in.
     */
    @Override
    public void run() {
        try {
            LoginResponse loginResponse = presenter.login(request);

            if(loginResponse.isSuccess()) {
                loadImage(loginResponse.getUser());
            }
            sendMessage(LoginActivity.LOGIN_RESPONSE_KEY, loginResponse);
        } catch (IOException | TweeterRemoteException ex) {
            sendMessage(LoginActivity.EXCEPTION_KEY, ex);
        }
    }

    /**
     * Loads the profile image for the user.
     *
     * @param user the user whose profile image is to be loaded.
     */
    private void loadImage(User user) {
        try {
            byte [] bytes = ByteArrayUtils.bytesFromUrl(user.getImageUrl());
            user.setImageBytes(bytes);
        } catch (IOException e) {
            Log.e(this.getClass().getName(), e.toString(), e);
        }
    }
}
