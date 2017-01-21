package com.akash.android.uberlogin;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.uber.sdk.android.core.auth.AccessTokenManager;
import com.uber.sdk.android.core.auth.AuthenticationError;
import com.uber.sdk.android.core.auth.LoginButton;
import com.uber.sdk.android.core.auth.LoginCallback;
import com.uber.sdk.android.core.auth.LoginManager;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.SessionConfiguration;

import java.util.Arrays;

import static com.uber.sdk.android.core.utils.Preconditions.checkNotNull;
import static com.uber.sdk.android.core.utils.Preconditions.checkState;

public class MainActivity extends AppCompatActivity {
    public static final String CLIENT_ID = BuildConfig.CLIENT_ID;
    public static final String REDIRECT_URI = BuildConfig.REDIRECT_URI;

    private static final String LOG_TAG = "LoginSampleActivity";

    private static final int LOGIN_BUTTON_CUSTOM_REQUEST_CODE = 1112;
    private static final int CUSTOM_BUTTON_REQUEST_CODE = 1113;


    private LoginButton blackButton;
    private LoginButton whiteButton;
    private Button customButton;
    private AccessTokenManager accessTokenManager;
    private LoginManager loginManager;
    private SessionConfiguration configuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configuration = new SessionConfiguration.Builder()
                .setClientId(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .setRedirectUri("http://localhost")
                .setEnvironment(SessionConfiguration.Environment.SANDBOX)
                .setServerToken("7Dv5QZXK45jIm54cQgr2bWVj7UacOL4ak10Rnw5q")
                .setClientSecret("CQs3R3yN7j3Cbp2Fu6rLK-fPE1buYAMb_bV-jpmf")
                .setScopes(Arrays.asList(Scope.PROFILE,Scope.RIDE_WIDGETS,Scope.REQUEST))
                .build();
        validateConfiguration(configuration);

        accessTokenManager = new AccessTokenManager(this);

        //Create a button with a custom request code
        whiteButton = (LoginButton) findViewById(R.id.uber_button_white);
        whiteButton.setCallback(new SampleLoginCallback())
                .setSessionConfiguration(configuration);

        //Create a button using a custom AccessTokenManager
        //Custom Scopes are set using XML for this button as well in R.layout.activity_sample
        blackButton = (LoginButton) findViewById(R.id.uber_button_white);
        blackButton.setAccessTokenManager(accessTokenManager)
                .setCallback(new SampleLoginCallback())
                .setSessionConfiguration(configuration)
                .setRequestCode(LOGIN_BUTTON_CUSTOM_REQUEST_CODE);


        //Use a custom button with an onClickListener to call the LoginManager directly
        loginManager = new LoginManager(accessTokenManager,
                new SampleLoginCallback(),
                configuration,
                CUSTOM_BUTTON_REQUEST_CODE);

//        customButton = (Button) findViewById(R.id.custom_uber_button);
//        customButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                loginManager.login(MainActivity.this);
//            }
//        });
    }

    private void validateConfiguration(SessionConfiguration configuration) {
        String nullError = "%s must not be null";
        String sampleError = "Please update your %s in the gradle.properties of the project before " +
                "using the Uber SDK Sample app. For a more secure storage location, " +
                "please investigate storing in your user home gradle.properties ";

        checkNotNull(configuration, String.format(nullError, "SessionConfiguration"));
        checkNotNull(configuration.getClientId(), String.format(nullError, "Client ID"));
        checkNotNull(configuration.getRedirectUri(), String.format(nullError, "Redirect URI"));
        checkState(!configuration.getClientId().equals("insert_your_client_id_here"),
                String.format(sampleError, "Client ID"));
        checkState(!configuration.getRedirectUri().equals("insert_your_redirect_uri_here"),
                String.format(sampleError, "Redirect URI"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(LOG_TAG, String.format("onActivityResult requestCode:[%s] resultCode [%s]",
                requestCode, resultCode));

        //Allow each a chance to catch it.
        whiteButton.onActivityResult(requestCode, resultCode, data);

        blackButton.onActivityResult(requestCode, resultCode, data);

        loginManager.onActivityResult(this, requestCode, resultCode, data);
    }

    private class SampleLoginCallback implements LoginCallback {

        @Override
        public void onLoginCancel() {
            Toast.makeText(MainActivity.this, R.string.user_cancels_message, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onLoginError(@NonNull AuthenticationError error) {
            Toast.makeText(MainActivity.this,
                    getString(R.string.login_error_message, error.name()), Toast.LENGTH_LONG)
                    .show();
        }

        @Override
        public void onLoginSuccess(@NonNull AccessToken accessToken) {
            Log.d("uber app",accessToken.getTokenType());
            Log.d("uber app-refresh",accessToken.getRefreshToken());
            Log.d("uber app",""+accessToken.getScopes());
            Log.d("uber app",accessToken.getToken());
        }

        @Override
        public void onAuthorizationCodeReceived(@NonNull String authorizationCode) {
            Toast.makeText(MainActivity.this, getString(R.string.authorization_code_message, authorizationCode),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }
}