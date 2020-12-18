package fyp.physiofit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class Register extends AppCompatActivity {
    EditText emailId, password;
    Button btnSignUp;
    TextView tvSignIn;
    FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirebaseAuth = FirebaseAuth.getInstance();
        emailId = findViewById(R.id.editText);
        password = findViewById(R.id.editText2);
        btnSignUp = findViewById(R.id.button2);
        tvSignIn = findViewById(R.id.textView);


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = emailId.getText().toString();
                final String pwd = password.getText().toString();

                if(email.isEmpty()){
                    emailId.setError("Please enter email id");
                    emailId.requestFocus();
                }
                else  if(pwd.isEmpty()){
                    password.setError("Please enter your password");
                    password.requestFocus();
                }

                mFirebaseAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                // Sign in success, update UI with the signed-in user's information


                                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                                Toast.makeText(Register.this,"Registration done",Toast.LENGTH_SHORT).show();


                                ////////////////////////////////// Writing data to database as a child of user class///////////////////////////////////////////



                                ///////////////////////////////passsing attributes of user to add it to firebase//////////////
                                //writeNewUser(user.getUid(), email,pwd);

                                Intent intent = new Intent(Register.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                                //Toast.makeText(Register.this,"SignUp Unsuccessful, Please Try Again",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(Register.this,"Registration failed",Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

            }
        });

        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Register.this,LoginActivity.class);
                startActivity(i);
            }
        });

    }
    ///////////////////////////////writing data on database as a child of user class ignooree this
    // ///////////////////////////////////////
    private void writeNewUser(String userId, String email, String password) {
        User user = new User(email, password);

        mDatabase.child("users").child(userId).setValue(user);
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
