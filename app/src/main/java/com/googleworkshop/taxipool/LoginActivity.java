package com.googleworkshop.taxipool;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {
    GoogleSignInClient mGoogleSignInClient;
    public static final int RC_SIGN_IN = 1;
    private FirebaseAuth mAuth;
    CallbackManager callbackManager;
    public static final String EXTRA_USER = "com.googleworkshop.taxipool.USER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getHashKey();
        callbackManager = CallbackManager.Factory.create();

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
        setContentView(R.layout.activity_login);
        LoginButton fbloginButton = findViewById(R.id.fb_login_button);
        fbloginButton.setReadPermissions("email", "public_profile");

        // Add listener to google sign in button
        SignInButton googleSignIn = findViewById(R.id.sign_in_button);
        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle(view);
            }
        });

        // Callback registration
        fbloginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });


        if (currentUser != null)
            loggedIn(currentUser);


    }

    private void loggedIn(FirebaseUser user) {
        Log.d("LoggedIn","I am here now");
        Toast.makeText(getApplicationContext(), "Welcome, " + user.getDisplayName(), Toast.LENGTH_SHORT).show();

        //TODO extract real User instead of mock user:
        User appUser = new User("MyMockUser","Ran Nachmany",(byte)1,31,"MyProfilePicture.png");
        Intent intent = new Intent(this,PreferencesActivity.class);
        intent.putExtra("User",appUser);

        startActivity(intent);
        finish();
    }

    public void signInWithGoogle(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        Log.d("handleSignIn","I am here now");

        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            Log.d("Exception: handleSignIn",e.getLocalizedMessage());

            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            //updateUI(null);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        Log.d("firebaseAuth","I am here now");

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = mAuth.getCurrentUser();
                            loggedIn(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("handleSignInResult","Authentication failed");
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    public void next(View view) {
        startActivity(new Intent(this, PreferencesActivity.class));
    }

    private void handleFacebookAccessToken(AccessToken token) {


        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            loggedIn(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void getHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.googleworkshop.taxipool", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));

            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }
}
