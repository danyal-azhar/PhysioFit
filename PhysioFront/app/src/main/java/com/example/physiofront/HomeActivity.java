package com.example.physiofront;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {

    private Button reps;
    private Button back;
    private Button bmi;
    private Button challenges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

  /*      back=(Button)findViewById(R.id.back);
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
*/
        bmi=(Button)findViewById(R.id.bmi);
        bmi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myintent = new Intent(getApplicationContext(),BMIActivity.class);
                startActivity(myintent);
            }
        });
        
    }
}