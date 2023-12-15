package kelompok7.msibfinalproject_4;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegistrasiActivity extends AppCompatActivity {

    private EditText etLoginIdentifier, etLoginUsername, etLoginPassword;
    private Button btnRegister;

    private FirebaseAuth mAuth;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrasi);

        etLoginIdentifier = findViewById(R.id.et_loginidentifier);
        etLoginUsername = findViewById(R.id.et_loginusername);
        etLoginPassword = findViewById(R.id.et_loginpassword);
        btnRegister = findViewById(R.id.btn_register);

        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String phoneNumberOrEmail = etLoginIdentifier.getText().toString().trim();
        String username = etLoginUsername.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        // Validasi input
        if (phoneNumberOrEmail.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Isi semua kolom registrasi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lakukan proses registrasi sesuai dengan jenis data yang dimasukkan
        if (phoneNumberOrEmail.contains("@")) {
            // Registrasi dengan email, tidak perlu verifikasi nomor telepon
            registerWithEmail(phoneNumberOrEmail, password, username);
        } else {
            // Registrasi dengan nomor telepon, lakukan verifikasi nomor telepon
            startPhoneVerification(phoneNumberOrEmail, password, username);
        }
    }

    private void registerWithEmail(String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            saveUserDataToDatabase(username, email, password);
                            Toast.makeText(RegistrasiActivity.this, "Registrasi berhasil", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegistrasiActivity.this, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(RegistrasiActivity.this, "Registrasi gagal. Coba lagi.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void startPhoneVerification(String phoneNumber, String password, String username) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                java.util.concurrent.TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        signInWithPhoneAuthCredential(phoneAuthCredential, username, phoneNumber, password);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(RegistrasiActivity.this, "Verifikasi nomor telepon gagal. Coba lagi.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationId = s;
                        showOtpPopup(username, password);
                    }
                }
        );
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential, String username, String phone, String password) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Simpan informasi pengguna ke Realtime Database
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                            userRef.child("username").setValue(username);
                            userRef.child("phoneNumber").setValue(phone);
                            userRef.child("hashedPassword").setValue(password);
                            userRef.child("registrationMethod").setValue("phone");
                        }

                        Toast.makeText(RegistrasiActivity.this, "Registrasi Berhasil. Silahkan Login Untuk Melanjutkan", Toast.LENGTH_SHORT).show();

                        // Redirect ke halaman login
                        Intent intent = new Intent(RegistrasiActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegistrasiActivity.this, "Registrasi Gagal!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void showOtpPopup(String username, String password) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_dialog_otp, null);
        builder.setView(dialogView);

        final EditText etVerificationCode = dialogView.findViewById(R.id.et_verification_code);
        Button btnVerifyCode = dialogView.findViewById(R.id.btn_verify_code);

        btnVerifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = etVerificationCode.getText().toString().trim();
                if (!TextUtils.isEmpty(code)) {
                    // Verifikasi nomor telepon dengan kode OTP yang dimasukkan
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                    signInWithPhoneAuthCredential(credential, username, etLoginIdentifier.getText().toString().trim(), password);
                } else {
                    Toast.makeText(RegistrasiActivity.this, "Masukkan kode OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }
    private void saveUserDataToDatabase(String username, String email, String password) {
        String uid = mAuth.getCurrentUser().getUid();
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(uid);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("hashedPassword", password);
        userMap.put("registrationMethod", "email"); // Defaultnya registrasi menggunakan email

        userReference.setValue(userMap);
    }
}
