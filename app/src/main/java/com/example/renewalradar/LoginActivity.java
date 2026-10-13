package com.example.renewalradar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Sign-in screen. This is the first screen the app opens.
 * If someone is already signed in, it jumps straight to MainActivity.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);
        if (session.isLoggedIn()) {
            openMain();
            return;
        }

        setContentView(R.layout.activity_login);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        MaterialButton btnSignIn = findViewById(R.id.btnSignIn);
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);

        btnSignIn.setOnClickListener(v -> signIn());
        tvGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void signIn() {
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email = text(etEmail).toLowerCase();
        String password = text(etPassword);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email");
            return;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Enter your password");
            return;
        }

        User user = AppDatabase.getInstance(this).userDao().findByEmail(email);
        if (user == null || !user.passwordHash.equals(SessionManager.hashPassword(password))) {
            tilPassword.setError("Wrong email or password");
            return;
        }

        session.login(user);
        Toast.makeText(this, "Welcome back, " + user.name, Toast.LENGTH_SHORT).show();
        openMain();
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish(); // so Back does not return to the sign-in screen
    }

    private static String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
