package kelompok7.msibfinalproject_4;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText etLoginUsername, etLoginPassword;
    private Button btnLogin;
    TextView btnToRegister;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLoginUsername = findViewById(R.id.ETUsernameLoginwithphone);
        etLoginPassword = findViewById(R.id.ETPasswordLoginwithphone);
        btnLogin = findViewById(R.id.btn_loginwithphone);
        btnToRegister = findViewById(R.id.kemenuregistrasi);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        btnToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect ke halaman registrasi
                Intent intent = new Intent(LoginActivity.this, RegistrasiActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String username = etLoginUsername.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Isi username dan password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ambil referensi user dari Realtime Database berdasarkan username
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        userRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User dengan username tersebut ditemukan
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String storedPassword = userSnapshot.child("hashedPassword").getValue(String.class);

                        // Verifikasi password
                        if (storedPassword.equals(password)) {
                            // Login berhasil
                            Toast.makeText(LoginActivity.this, "Login berhasil", Toast.LENGTH_SHORT).show();

                            // Redirect ke halaman beranda atau halaman lainnya
                            Intent intent = new Intent(LoginActivity.this, HalamanUtamaActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Password tidak cocok
                            Toast.makeText(LoginActivity.this, "Password salah", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    // User dengan username tersebut tidak ditemukan
                    Toast.makeText(LoginActivity.this, "Username tidak terdaftar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Terjadi kesalahan, coba lagi nanti", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
