package com.example.www.loginwithdifferentaccounts;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import com.facebook.*;
import com.facebook.model.*;
import com.facebook.widget.LoginButton.UserInfoChangedCallback;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private GoogleApiClient mGoogleApiClient;
    private LoginButton loginButton;
    //private TextView username;
    private UiLifecycleHelper uiLifecycleHelper;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "SignInActivity";
    private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;
    private TextView info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //username = (TextView) findViewById(R.id.username);
        loginButton = (LoginButton) findViewById(R.id.log_in_button);
        loginButton.setOnClickListener(this);
        /*loginButton.setReadPermissions(Arrays.asList("email"));
        loginButton.setUserInfoChangedCallback(new UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                if(user != null){
                    //updateFacebookUI(true);
                    username.setText("You are currently logged in as " + user.getName());
                }
                else{
                    // updateFacebookUI(false);
                    //findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                }
            }
        });*/

        Session.openActiveSession(this, true, new Session.StatusCallback(){
            @Override
            public void call(Session session, SessionState state, Exception exception){
                if(session.isOpened()){ //checks if session for Google sign in opened
                    Request.executeMeRequestAsync(session, new Request.GraphUserCallback(){
                        @Override
                        public void onCompleted(GraphUser user, Response response){
                            if(user != null){ //checks that user is logged in
                                //TextView welcome = (TextView) findViewById(R.id.welcome);
                               // welcome.setText(R.string.signed_in_fmt + user.getName()); //confirms that user logged in
                            }
                        }
                    });
                }
            }
        });

        // Views
        mStatusTextView = (TextView) findViewById(R.id.status);

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);


       if(isLoggedIn()){ //checks whether user is already logged into Facebook
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        } else{
           //creates new ui lifecycle helper
           uiLifecycleHelper = new UiLifecycleHelper(this, statusCallback);
           uiLifecycleHelper.onCreate(savedInstanceState);
       }

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                //.requestIdToken()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.www.kodejoy",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    //creates new status callback to determine whether Facebook session opened or not
    private Session.StatusCallback statusCallback = new Session.StatusCallback(){
        @Override
        public void call(Session session, SessionState state, Exception exception){
            if(state.isOpened()){
                Log.d("MainActivity", "Facebook session opened.");
            } else{
                Log.d("MainActivity", "Facebook session closed.");
            }
        }
    };

    public boolean isLoggedIn() {
        Session session = Session.getActiveSession();
        return (session != null && session.isOpened());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
            case R.id.disconnect_button:
                revokeAccess();
                break;
            case R.id.log_in_button:
                signInToFacebook();
                break;
        }
    }

   private void signInToFacebook(){
        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        loginButton.setReadPermissions(Arrays.asList("email"));
        loginButton.setUserInfoChangedCallback(new UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                if(user != null){
                    updateFacebookUI(true);
                    //username.setText("You are currently logged in as " + user.getName());
                }
                else{
                    //username.setTextColor(Color.WHITE);
                   updateFacebookUI(false);
                    findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        uiLifecycleHelper.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        uiLifecycleHelper.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        uiLifecycleHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedState){
        super.onSaveInstanceState(savedState);
        uiLifecycleHelper.onSaveInstanceState(savedState);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else{
            uiLifecycleHelper.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            if(acct.getDisplayName() != null){
                mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            }
            else{
                mStatusTextView.setText(getString(R.string.signed_in));
            }
            updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    // [START signOut]
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END revokeAccess]

    private void updateFacebookUI(boolean signedIn){
        if(signedIn) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        }
        else{
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.VISIBLE);
        }

    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
           loginButton.setVisibility(View.GONE);
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
            mStatusTextView.setTextColor(Color.BLACK);
        } else {

            mStatusTextView.setTextColor(Color.WHITE);
            mStatusTextView.setText(R.string.signed_out);

            loginButton.setVisibility(View.VISIBLE);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }
}
