package edu.byu.cs.tweeter.presenter.backgroundtask;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.service.request.LoginRequest;
import edu.byu.cs.tweeter.model.service.response.LoginResponse;
import edu.byu.cs.tweeter.presenter.LoginPresenter;
import edu.byu.cs.tweeter.util.ByteArrayUtils;

public class LoginTask extends BackgroundTask {

    public static final String EXCEPTION_KEY = "ExceptionKey";
    public static  final String LOGIN_RESPONSE_KEY = "LoginResponseKey";

    private final LoginPresenter presenter;
    private final LoginRequest request;

    /**
     * Creates an instance.
     *
     * @param presenter the presenter this task should use to login.
     * @param messageHandler the handler that wants to be notified when the task completes.
     * @param request the request to be processed on a background thread.
     */
    public LoginTask(LoginPresenter presenter, Handler messageHandler, LoginRequest request) {
        super(messageHandler);
        this.presenter = presenter;
        this.request = request;
    }

    /**
     * Invoked on a background thread to log the user in.
     */
    public void run() {
        try {
            LoginResponse loginResponse = presenter.login(request);

            if(loginResponse.isSuccess()) {
                loadImage(loginResponse.getUser());
            }
            sendMessage(LOGIN_RESPONSE_KEY, loginResponse);
        } catch (IOException ex) {
            sendMessage(EXCEPTION_KEY, ex);
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
