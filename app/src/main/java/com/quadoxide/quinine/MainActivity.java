package com.quadoxide.quinine;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.quadoxide.quinine.Helpers.AppUtils;
import com.quadoxide.quinine.Models.User;

import java.util.Objects;
import java.util.logging.Logger;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.quadoxide.quinine.Helpers.AppUtils.isStringValid;
import static com.quadoxide.quinine.Helpers.AppUtils.showToast;
import static com.quadoxide.quinine.Helpers.Constants.FIRESTORE_COLLECTION_USERS;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView speech_text;
    private Button speech_button;
    private LinearLayout speech_loading_view;
    private LottieAnimationView speech_loading;
    private ScrollView scroll_view;
    private EditText query_input;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private CircleImageView profile_image;
    private static final int RC_SIGN_IN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Finding view by ID
        speech_text = findViewById(R.id.speech_text);
        speech_button = findViewById(R.id.speech_button);
        speech_loading_view = findViewById(R.id.speech_loading_view);
        speech_loading = findViewById(R.id.speech_loading);
        scroll_view = findViewById(R.id.scroll_view);
        query_input = findViewById(R.id.query_input);
        profile_image = findViewById(R.id.profile_image);

        //Initializing click listeners
        profile_image.setOnClickListener(this);

        //Initializing some objects
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //Showing welcome message
        showWelcomeMessage();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                showToast(this, getString(R.string.text_google_login_cancelled));
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.profile_image) {
            if (firebaseAuth.getCurrentUser() != null) {
                showLogoutOption();
            }
        }
    }

    private void loginWithGoogle(){
        //Showing loader dialog
        showSpeechLoading(true);

        //Getting google sign in options
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(MainActivity.this, googleSignInOptions);

        //Launching the login intent here
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        showWelcomeMessage();
                        saveUserDetails(task);
                    }else {
                        showToast(this, getString(R.string.text_google_login_failed));
                        showSpeechLoading(false);
                    }
                });
    }

    private void saveUserDetails(Task<AuthResult> task){
        ///Getting firebase user
        FirebaseUser user = task.getResult().getUser();

        //Initializing user object
        assert user != null;
        User userObject = new User(user.getUid(), user.getDisplayName(), user.getEmail(), Objects.requireNonNull(user.getPhotoUrl()).toString());

        //Saving user document in the users collection
        firebaseFirestore.collection(FIRESTORE_COLLECTION_USERS)
                .document(user.getUid())
                .set(userObject);
    }

    public void updateSpeechBubble(String message, String button_text, boolean animate, View.OnClickListener listener){
        if(animate){
            //Showing speech loader
            showSpeechLoading(true);

            new Handler().postDelayed(() -> {
                insertSpeechData(message, button_text, listener);

                //Hiding speech loader
                showSpeechLoading(false);

                /*Scrolling to the top to make sure that user is able to see the
                 * speech bubble message*/
                scroll_view.fullScroll(ScrollView.FOCUS_UP);
            }, 2000);
        }else {
            insertSpeechData(message, button_text, listener);

            //Hiding speech loader
            showSpeechLoading(false);
        }
    }

    private void insertSpeechData(String message, String button_text, View.OnClickListener listener){
        //Filling passed data to elements
        speech_text.setText(message);
        speech_button.setText(button_text);

        //Adding click listener to speech button
        speech_button.setOnClickListener(view -> {
            //Triggering the passed listener
            listener.onClick(speech_button);
        });
    }

    public void showWelcomeMessage(){
        //Getting current firebase user
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user==null){
            updateSpeechBubble(getString(R.string.text_howdy)+" "+getString(R.string.text_login_message), getString(R.string.text_login_google), true, view -> {loginWithGoogle();});
        }else {
            updateSpeechBubble(getString(R.string.text_howdy)+" "+firebaseAuth.getCurrentUser().getDisplayName()+". "+getString(R.string.text_login_message), "", true, null);

            //Loading user profile image
            Glide.with(this).load(user.getPhotoUrl()).into(profile_image);
        }

        //Resetting the number input
        query_input.setText("");
    }

    public void showSpeechLoading(boolean show){
        if(show){
            //Starting speech loading animation
            speech_loading.playAnimation();

            //Showing/Hiding view accordingly
            speech_text.setVisibility(View.GONE);
            speech_button.setVisibility(View.GONE);
            speech_loading_view.setVisibility(View.VISIBLE);
        }else {
            //Paused speech loading animation
            speech_loading.pauseAnimation();

            //Showing/Hiding view accordingly
            speech_loading_view.setVisibility(View.GONE);
            speech_text.setVisibility(View.VISIBLE);
            speech_button.setVisibility(View.VISIBLE);
        }

        //Hiding speech button if text is invalid
        if (!isStringValid(speech_button.getText().toString())){
            speech_button.setVisibility(View.GONE);
        }
    }

    private void showLogoutOption(){
        PopupMenu popupMenu = new PopupMenu(this, profile_image);
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.logout) {
                firebaseAuth.signOut();
            }

            //Restarting activity to refresh user login state
            finish();
            startActivity(new Intent(MainActivity.this, MainActivity.class));

            return false;
        });
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_main, popupMenu.getMenu());
        popupMenu.show();
    }
}