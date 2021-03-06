package edu.gatech.schoolmark.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.gatech.schoolmark.R;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mAuth = FirebaseAuth.getInstance();

        // Append @gatech.edu to login email
        EditText emailField = (EditText)findViewById(R.id.enterEmailLogin);
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

    @Override
    public void onResume() {
        super.onResume();
        // Get the current user info check if it it's a verified user
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.isEmailVerified()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void logInAttempt(View view) {
        EditText emailEdit = findViewById(R.id.enterEmailLogin);
        EditText passwordEdit = findViewById(R.id.enterPasswordLogin);
        String email = emailEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        if (email.matches("") || password.matches("")) {
            Toast.makeText(this, "There shouldn't be any empty fields!", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (!task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Incorrect Email Address or Password!",
                                        Toast.LENGTH_SHORT).show();
                            } else if (user != null && user.isEmailVerified()) {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Email not verified. We'll resend a verification email now.",
                                        Toast.LENGTH_SHORT).show();
                                sendEmail();
                            }
                        }
                    });

        }

    }

    public void register(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    public void forgotPassword(View view) {
        // Goto different screen to send in the password to?
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendEmail() {
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) { return; }
        user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this,
                            "Account verification email sent to " + user.getEmail(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Failed to send account verification email",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
