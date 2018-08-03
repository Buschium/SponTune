package de.spontune.android.spontune.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import de.spontune.android.spontune.LoginActivity;
import de.spontune.android.spontune.MapsActivity;
import de.spontune.android.spontune.R;

public class SignupFragment extends Fragment {

    private Activity activity;
    private EditText inputUsername, inputEmail, inputPassword, inputConfirmPassword;
    private Button btnSignIn, btnSignUp;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_signup, container, false);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignIn = v.findViewById(R.id.btn_login);
        btnSignUp = v.findViewById(R.id.btn_signup);
        inputUsername = v.findViewById(R.id.username);
        inputEmail = v.findViewById(R.id.email);
        inputPassword = v.findViewById(R.id.password);
        inputConfirmPassword = v.findViewById(R.id.confirm_password);
        progressBar = v.findViewById(R.id.progressBar);
        setupButtons();

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    private void setupButtons(){
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LoginActivity) activity).setCurrentItem(1);
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = inputUsername.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String confirmPassword = inputConfirmPassword.getText().toString().trim();

                if(TextUtils.isEmpty(username)){
                    Toast.makeText(activity.getApplicationContext(), "Please enter a username.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(email)) {
                    Toast.makeText(activity.getApplicationContext(), "Please enter an email address.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(password)) {
                    Toast.makeText(activity.getApplicationContext(), "Please enter a password.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!password.equals(confirmPassword)){
                    Toast.makeText(activity.getApplicationContext(), "Your passwords do not match.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(password.length() < 6) {
                    Toast.makeText(activity.getApplicationContext(), "Password too short, please enter a minimum of 6 characters.", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                //create user
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                //Toast.makeText(activity, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(activity, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    startActivity(new Intent(activity, MapsActivity.class));
                                    activity.finish();
                                }
                            }
                        });

            }
        });
    }
}
