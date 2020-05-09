package com.example.memoria.signup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.example.memoria.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.regEmail) TextInputEditText regEmail;
    @BindView(R.id.regPass) TextInputEditText regPass;
    @BindView(R.id.regConfPass) TextInputEditText regConfPass;
    @BindView(R.id.signUpBtn) Button signUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                String email = regEmail.getText().toString();
                String pass = regPass.getText().toString();
                String passConf = regConfPass.getText().toString();

                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(passConf)){
                    Snackbar.make(v, "Please fill all entries", Snackbar.LENGTH_LONG).show();
                }else if (!pass.equals(passConf)){
                    Snackbar.make(v, "Passwords do not match", Snackbar.LENGTH_LONG).show();
                }else{
                    mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.i("RegisterActivity", "createUserWithEmail : success");
                                Intent intent = new Intent(RegisterActivity.this, SettingsActivity.class);
                                intent.putExtra("firstTime", true);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.e("RegisterActivity", "createUserWithEmail : failure", task.getException());
                                Snackbar.make(v, "Authentication failed. \n" + task.getException(), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
