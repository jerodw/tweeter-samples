package edu.byu.cs.tweeter.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.byu.cs.tweeter.R;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.presenter.LoginPresenter;
import edu.byu.cs.tweeter.view.main.MainActivity;

/**
 * Contains the minimum UI required to allow the user to login with a hard-coded user. Most or all
 * of this should be replaced when the back-end is implemented.
 */
public class LoginActivity extends AppCompatActivity implements LoginPresenter.View {

    private static final String LOG_TAG = "LoginActivity";

    private LoginPresenter presenter;
    private Toast loginInToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        presenter = new LoginPresenter(this);

        Button loginButton = findViewById(R.id.LoginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {

            /**
             * Makes a login request. The user is hard-coded, so it doesn't matter what data we put
             * in the LoginRequest object.
             *
             * @param view the view object that was clicked.
             */
            @Override
            public void onClick(View view) {
                loginInToast = Toast.makeText(LoginActivity.this, "Logging In", Toast.LENGTH_LONG);
                loginInToast.show();

                // It doesn't matter what values we put here. We will be logged in with a hard-coded dummy user.
                presenter.initiateLogin("dummyUserName", "dummyPassword");
            }
        });
    }

    /**
     * Called to notify view when a login completed successfully.
     *
     * @param user the user who logged in.
     * @param authToken the auth token for this session.
     */
    @Override
    public void loginSuccessful(User user, AuthToken authToken) {
        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra(MainActivity.CURRENT_USER_KEY, user);
        intent.putExtra(MainActivity.AUTH_TOKEN_KEY, authToken);

        loginInToast.cancel();
        startActivity(intent);
    }

    /**
     * Called to notify view when a login completes unsuccessfully.
     *
     * @param message error message describing the failed login.
     */
    @Override
    public void loginUnsuccessful(String message) {
        loginInToast.cancel();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
