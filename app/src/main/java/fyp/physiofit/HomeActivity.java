package fyp.physiofit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import androidx.constraintlayout.widget.ConstraintLayout;

import fyp.physiofit.R;

public class HomeActivity extends AppCompatActivity {


        private Button reps;
        private Button back;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_home);

            back=(Button)findViewById(R.id.back);
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent back=new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(back);
                }
            });

            reps=(Button)findViewById(R.id.reps);
            reps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myintent = new Intent(getApplicationContext(),fyp.physiofit.CameraActivity.class);
                    startActivity(myintent);
                }
            });

        }

    }


