package edu.gatech.schoolmark.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import edu.gatech.schoolmark.R;

public class RegistrationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "EmailPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mAuth = FirebaseAuth.getInstance();

        // Append @gatech.edu to registration email
        EditText emailField = (EditText)findViewById(R.id.enterEmailRegister);
        emailField.setText("@gatech.edu");
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    public void registerAttempt(View view) {
        EditText nameEdit = (EditText) findViewById(R.id.enterNameRegister);
        EditText emailEdit = (EditText) findViewById(R.id.enterEmailRegister);
        EditText passwordEdit = (EditText) findViewById(R.id.enterPasswordRegister);

        String name = nameEdit.getText().toString();
        String email = emailEdit.getText().toString();
        String password = passwordEdit.getText().toString();


        if (name.matches("") || email.matches("") || password.matches("")) {
            Toast.makeText(this, "There shouldn't be any empty fields!", Toast.LENGTH_SHORT).show();
        } else if (!(email.substring(email.lastIndexOf('@') + 1).equals("gatech.edu"))) {
            Toast.makeText(this, "You need to register with a valid GaTech email!", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 6) {
            Toast.makeText(this, "Your password must be at least 6 characters long!", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Successful registration");
                                sendEmail();
                            } else {
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(RegistrationActivity.this, "This email account is already registered!", Toast.LENGTH_SHORT).show();
                                }
                                Toast.makeText(RegistrationActivity.this, "Firebase Authentification failed"
                                        , Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
    }

    public void cancelRegister(View view) {
        Intent intent = new Intent(this, WelcomeScreenActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * sends the email once a user is created
     */
    private void sendEmail() {
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) { return; }
        user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RegistrationActivity.this,
                            "Verification email sent to " + user.getEmail(),
                            Toast.LENGTH_SHORT).show();

                } else {
                    Log.e(TAG, "sendEmailVerification", task.getException());
                    Toast.makeText(RegistrationActivity.this,
                            "Failed to send verification email",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(RegistrationActivity.this, WelcomeScreenActivity.class));
        finish();

    }
}