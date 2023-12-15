package kelompok7.msibfinalproject_4;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HalamanUtamaActivity extends AppCompatActivity {

    private TextView tvWelcome, tvUsername;
    private ImageView ivProfile;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halaman_utama);

        tvWelcome = findViewById(R.id.Hello);
        tvUsername = findViewById(R.id.Username);
        ivProfile = findViewById(R.id.personImageView);

        mAuth = FirebaseAuth.getInstance();

        // Ambil data pengguna dari Realtime Database
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String username = dataSnapshot.child("username").getValue(String.class);
                        tvUsername.setText(username);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        }

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfilePopup();
            }
        });
    }

    private void showProfilePopup() {
        if (userRef != null) {
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String username = dataSnapshot.child("username").getValue(String.class);
                        String phoneOrEmail = dataSnapshot.child("Phone/Email").getValue(String.class);
                        String password = dataSnapshot.child("password").getValue(String.class);

                        // Tampilkan data pengguna dalam popup
                        View popupView = getLayoutInflater().inflate(R.layout.activity_akun_user, null);

                        TextView usernameTextView = popupView.findViewById(R.id.usernamePopupTextView);
                        TextView phoneOrEmailTextView = popupView.findViewById(R.id.phoneOrEmailPopupTextView);
                        TextView passwordTextView = popupView.findViewById(R.id.passwordPopupTextView);
                        TextView cancelButton = popupView.findViewById(R.id.cancelButton);
                        TextView logoutButton = popupView.findViewById(R.id.logoutButton);

                        usernameTextView.setText("Username: " + username);
                        phoneOrEmailTextView.setText("Phone/Email: " + phoneOrEmail);
                        passwordTextView.setText("Password: " + password);

                        AlertDialog.Builder builder = new AlertDialog.Builder(HalamanUtamaActivity.this);
                        builder.setView(popupView);

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                        // Handle click event for buttons in the popup
                        cancelButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                            }
                        });

                        logoutButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Logout user dan kembali ke halaman login
                                mAuth.signOut();
                                alertDialog.dismiss();
                                finish();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
    }
}
