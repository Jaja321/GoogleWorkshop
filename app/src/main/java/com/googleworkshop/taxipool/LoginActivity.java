package com.googleworkshop.taxipool;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.Login;
import com.facebook.login.LoginManager;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    GoogleSignInClient mGoogleSignInClient;
    public static final int RC_SIGN_IN = 1;
    private FirebaseAuth mAuth;
    CallbackManager callbackManager;
    public static final String EXTRA_USER = "com.googleworkshop.taxipool.USER";
    private static DatabaseReference database= FirebaseDatabase.getInstance().getReference();
    public final String lastRequest = "lastRequest";
    protected SharedPreferences lastRequestSharedPref;
    protected String currentRequestOrigin = null;
    protected String currentRequestDestination = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getHashKey();
        callbackManager = CallbackManager.Factory.create();

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            mNotificationManager.cancelAll();//delete all previously sent notifications
        }catch (NullPointerException e){
            Log.i("null pointer", "NullPointerException in cancelAll()");
        }


        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Login");

        if (currentUser != null)
            loggedIn(currentUser);
    }
    public void fbLogin(View view)
    {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList( "email", "public_profile"));
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>()
                {
                    @Override
                    public void onSuccess(LoginResult loginResult)
                    {
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel()
                    {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception)
                    {
                        // App code
                    }
                });
    }

    private void loggedIn(final FirebaseUser firebaseUser) {
        Log.d("LoggedIn","I am here now");
        Toast.makeText(getApplicationContext(), "Welcome, " + firebaseUser.getDisplayName(), Toast.LENGTH_SHORT).show();
        final DatabaseReference userReference=database.child("users").child(firebaseUser.getUid());

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){ //User already exist
                    Log.d("User already exists","I am here now");
                    final User user=dataSnapshot.getValue(User.class);
                    //if (!user.isBlocked())
                        if (user != null && (user.getReportedIDs() == null || user.getReportedIDs().size() < 3)) {
                            gotoPreferences(user);

                        } else { //user is  blocked from using this app due to too many reports
                            if(user == null){
                                Log.d("User doesn't exist","I am here now");
                                gotoPreferences(ServerUtils.createNewUser(firebaseUser));
                                return;
                            }
                            AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(LoginActivity.this);
                            alertDialogBuilder2.setTitle("Alert");
                            alertDialogBuilder2.setMessage("You are blocked for being reported too many times");
                            alertDialogBuilder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    finish();
                                }
                            });
                            AlertDialog alertDialog2 = alertDialogBuilder2.create();

                            // show it
                            alertDialog2.show();
                        }
                }else{
                    Log.d("User doesn't exist","I am here now");
                    User user=ServerUtils.createNewUser(firebaseUser);
                    gotoPreferences(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.d("onCancelled","I am here now");
            }
        });
    }

    private void gotoPreferences(final User user){
        Intent intent;
        SharedPreferences sharedPreferences=this.getSharedPreferences("requestId", Context.MODE_PRIVATE);
        final String requestId=sharedPreferences.getString("requestId",null);
        final long timeLeft = getTimeLeftForRequest();
        if(requestId==null) {
            //If user is in a group that is not closed but more than an hour has passed
            //he will be redirected to preferences
            String token = FirebaseInstanceId.getInstance().getToken();
            ServerUtils.updateToken(token,user.getUserId());
            intent = new Intent(this, PreferencesActivity.class);
            intent.putExtra("User", user);
            startActivity(intent);
            finish();
        }
        else{
            //There is an active request
            currentRequestOrigin = sharedPreferences.getString("origin", null);
            currentRequestDestination = sharedPreferences.getString("destination", null);

            if(currentRequestDestination != null) {//just to be safe

                DatabaseReference ref = database.child("requests").child(requestId);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final Request currRequest = dataSnapshot.getValue(Request.class);
                        if (currRequest != null && currRequest.getGroupId() != null) {

                            database.child("groups").child(currRequest.getGroupId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.exists()){
                                        return;
                                    }
                                    int numOfUsers = 1;
                                    try {
                                        numOfUsers = dataSnapshot.child("numOfUsers").getValue(int.class);
                                    }
                                    catch(NullPointerException e){
                                        final Intent intent = new Intent(LoginActivity.this, PreferencesActivity.class);
                                        intent.putExtra("User", user);
                                        startActivity(intent);
                                        finish();
                                    }

                                    if(numOfUsers > 1){//User is in a group
                                        final Intent intent = new Intent(LoginActivity.this, MatchScreenActivity.class);
                                        ClientUtils.saveRequest(currRequest, getApplicationContext());
                                        startActivity(intent);
                                        finish();
                                    }
                                    else if(getTimeLeftForRequest() > 0){
                                        final Intent intent = new Intent(LoginActivity.this, SearchingActivity2.class);
                                        intent.putExtra("origin", currentRequestOrigin);
                                        intent.putExtra("destination", currentRequestDestination);
                                        intent.putExtra("requestId", requestId);
                                        intent.putExtra("numOfSeconds", timeLeft);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else{
                                        String token = FirebaseInstanceId.getInstance().getToken();
                                        ServerUtils.updateToken(token,user.getUserId());
                                        Intent intent = new Intent(LoginActivity.this, PreferencesActivity.class);
                                        intent.putExtra("User", user);
                                        startActivity(intent);
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                        else{
                            if(currRequest == null || getTimeLeftForRequest() <= 0){
                                String token = FirebaseInstanceId.getInstance().getToken();
                                ServerUtils.updateToken(token,user.getUserId());
                                final Intent intent = new Intent(LoginActivity.this, PreferencesActivity.class);
                                intent.putExtra("User", user);
                                startActivity(intent);
                                finish();
                            }
                            final Intent intent = new Intent(LoginActivity.this, SearchingActivity2.class);
                            intent.putExtra("origin", currentRequestOrigin);
                            intent.putExtra("destination", currentRequestDestination);
                            intent.putExtra("requestId", requestId);
                            intent.putExtra("numOfSeconds", timeLeft);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            else if(getTimeLeftForRequest() <= 0){
                String token = FirebaseInstanceId.getInstance().getToken();
                ServerUtils.updateToken(token,user.getUserId());
                intent = new Intent(LoginActivity.this, PreferencesActivity.class);
                intent.putExtra("User", user);
                startActivity(intent);
                finish();
            }
            else {
                intent = new Intent(this, SearchingActivity2.class);
                intent.putExtra("origin", sharedPreferences.getString("origin", null));
                intent.putExtra("destination", sharedPreferences.getString("destination", null));
                intent.putExtra("requestId", requestId);
                intent.putExtra("numOfSeconds", timeLeft);
                startActivity(intent);
                finish();
            }
        }
    }

    public void signInWithGoogle(View view) {
        View progressBar=findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
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
                        }

                    }
                });
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
                        }

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

    protected long getTimeLeftForRequest(){//In seconds
        lastRequestSharedPref = getSharedPreferences(lastRequest, 0);

        if(!lastRequestSharedPref.contains("lastRequestTimeStamp") || !lastRequestSharedPref.contains("lastRequestDuration")){
            //no valid last request exists
            return -1;
        }
        long timeStamp = lastRequestSharedPref.getLong("lastRequestTimeStamp", 0);
        long duration = lastRequestSharedPref.getLong("lastRequestDuration", 0);
        long currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        return duration - (currentTime - timeStamp);
    }


}
