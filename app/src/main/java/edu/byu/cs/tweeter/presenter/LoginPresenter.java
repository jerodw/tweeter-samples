package edu.byu.cs.tweeter.presenter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.service.LoginService;
import edu.byu.cs.tweeter.model.service.request.LoginRequest;
import edu.byu.cs.tweeter.model.service.response.LoginResponse;
import edu.byu.cs.tweeter.presenter.backgroundtask.LoginTask;

/**
 * The presenter for the login functionality of the application.
 */
public class LoginPresenter {

    private static final String LOG_TAG = "LoginPresenter";

    private final View view;

    /**
     * The interface by which this presenter communicates with it's view.
     */
    public interface View {
        void loginSuccessful(User user, AuthToken authToken);
        void loginUnsuccessful(String message);
    }

    /**
     * Creates an instance.
     *
     * @param view the view for which this class is the presenter.
     */
    public LoginPresenter(View view) {
        this.view = view;
    }

    /**
     * Initiates the login process.
     *
     * @param username the user's username.
     * @param password the user's password.
     */
    public void initiateLogin(String username, String password) {
        LoginRequest loginRequest = new LoginRequest(username, password);
        MessageHandler messageHandler = new MessageHandler(Looper.getMainLooper(), this);
        LoginTask loginTask = new LoginTask(this, messageHandler, loginRequest);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(loginTask);
    }

    /**
     * Makes a login request. Invoked by the {@link LoginTask} on a background thread.
     *
     * @param loginRequest the request.
     */
    public LoginResponse login(LoginRequest loginRequest) throws IOException {
        LoginService loginService = new LoginService();
        return loginService.login(loginRequest);
    }

    /**
     * Invoked by the handler for a successful login after the background task completes.
     * Displays the MainActivity.
     *
     * @param loginResponse the response from the login request.
     */
    public void loginSuccessful(LoginResponse loginResponse) {
        view.loginSuccessful(loginResponse.getUser(), loginResponse.getAuthToken());
    }

    /**
     * Invoked by the handler for an unsuccessful login after the background task completes.
     * Displays a toast with a  message indicating why the login failed.
     *
     * @param loginResponse the response from the login request.
     */
    public void loginUnsuccessful(LoginResponse loginResponse) {
        view.loginUnsuccessful("Failed to login. " + loginResponse.getMessage());
    }

    /**
     * Invoked by the handler if the background task caught an exception. Displays a toast with the
     * exception message.
     *
     * @param exception the exception.
     */
    public void handleException(Exception exception) {
        Log.e(LOG_TAG, exception.getMessage(), exception);
        view.loginUnsuccessful("Failed to login because of exception: " + exception.getMessage());
    }

    /**
     * Handles the message from the background task indicating that the task is done, by invoking
     * methods on the presenter.
     */
    private static class MessageHandler extends Handler {

        private final LoginPresenter presenter;

        MessageHandler(Looper looper, LoginPresenter presenter) {
            super(looper);
            this.presenter = presenter;
        }

        @Override
        public void handleMessage(Message message) {
            Bundle bundle = message.getData();
            Exception exception = (Exception) bundle.getSerializable(LoginTask.EXCEPTION_KEY);

            if(exception == null) {
                LoginResponse response = (LoginResponse) bundle.getSerializable(LoginTask.LOGIN_RESPONSE_KEY);

                if(response.isSuccess()) {
                    presenter.loginSuccessful(response);
                } else {
                    presenter.loginUnsuccessful(response);
                }
            } else {
                presenter.handleException(exception);
            }
        }
    }
}
