package com.example.firebase_subida_archivos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = "loginactivity";
    private static final int GOOGLE_SIGN_IN = 1111;
    private Button btnSignInUp;
    private FirebaseAuth mAuthFB;
    private boolean createNewAccount = true;
    private GoogleSignInClient googleSignInClient;
    private SignInButton btnGoogle;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        setListners();
        initGoogleClient();
        mAuthFB = FirebaseAuth.getInstance();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        FirebaseAuth.getInstance().signOut();
    }

    private void initViews() {

        btnGoogle = findViewById(R.id.signInButton);

    }

    private void setListners() {
        btnGoogle.setOnClickListener(view -> {
            loginWithGoogle();
        });
    }

    // [START] logging with google

    private void initGoogleClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void loginWithGoogle() {
        googleSignInClient.signOut();
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                // Authenticate with Firebase
                loginfirebaseWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void loginfirebaseWithGoogle(String idToken) {
        progressBar.setVisibility(View.VISIBLE);
        btnGoogle.setVisibility(View.GONE);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuthFB.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {

                    progressBar.setVisibility(View.GONE);
                    btnGoogle.setVisibility(View.VISIBLE);

                    if (task.isSuccessful()) {// Sign in success
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuthFB.getCurrentUser();
                        updateUI(user);
                    } else { // sign in fails
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Error while logging with google account", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // [END] logging with google

    private void updateUI(FirebaseUser currentUser) { //send current user to next activity
        if (currentUser == null) return;
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuthFB.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onBackPressed() {
        // NO HACER NADA
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}