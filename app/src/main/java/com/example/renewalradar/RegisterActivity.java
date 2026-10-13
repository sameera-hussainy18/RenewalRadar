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
 * "Create account" screen. Saves a new user in the Room "users" table
 * and signs them in straight away.
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirm;
    private TextInputEditText etName, etEmail, etPassword, etConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirm = findViewById(R.id.tilConfirm);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        MaterialButton btnCreate = findViewById(R.id.btnCreateAccount);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnCreate.setOnClickListener(v -> createAccount());
        tvGoToLogin.setOnClickListener(v -> finish()); // back to the sign-in screen
    }

    private void createAccount() {
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirm.setError(null);

        String name = text(etName);
        String email = text(etEmail).toLowerCase();
        String password = text(etPassword);
        String confirm = text(etConfirm);

        if (name.isEmpty()) {
            tilName.setError("Enter your name");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email");
            return;
        }
        if (password.length() < 6) {
            tilPassword.setError("Use at least 6 characters");
            return;
        }
        if (!password.equals(confirm)) {
            tilConfirm.setError("Passwords do not match");
            return;
        }

        UserDao dao = AppDatabase.getInstance(this).userDao();
        if (dao.findByEmail(email) != null) {
            tilEmail.setError("An account with this email already exists");
            return;
        }

        User user = new User();
        user.name = name;
        user.email = email;
        user.passwordHash = SessionManager.hashPassword(password);
        user.id = dao.insert(user);

        new SessionManager(this).login(user);
        Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();

        // Open the home screen and clear the sign-in screens from the back stack
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private static String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
