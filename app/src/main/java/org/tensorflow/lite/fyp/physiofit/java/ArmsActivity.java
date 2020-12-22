package org.tensorflow.lite.fyp.physiofit.java;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.tensorflow.lite.fyp.physiofit.CameraActivity;
import org.tensorflow.lite.examples.posenet.R;

public class ArmsActivity extends AppCompatActivity {

    private int armlevel = 0;
    private ImageView armtopimage;
    private TextView  armtoptext , armstopdetailstext, dumbblecurlswatchvideo, barbellcurlsswatchvideo, hammercurlsswatchvideo, seatedtricepswatchvideo;
    private LinearLayout startbutton ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arms);

        armlevel=getIntent().getIntExtra("level",1);

        initializers();
        listners();
    }
    public void initializers(){
        armtopimage = findViewById(R.id.armstopimage);
        armtoptext = findViewById(R.id.armstoptext);
        armstopdetailstext = findViewById(R.id.armstopdetailstext);
        dumbblecurlswatchvideo = findViewById(R.id.dumbblebicepwatchvideo);
        barbellcurlsswatchvideo = findViewById(R.id.barbellbicepwatchvideo);
        hammercurlsswatchvideo = findViewById(R.id.hammerbicepwatchvideo);
        seatedtricepswatchvideo = findViewById(R.id.seatedbicepwatchvideo);
        startbutton = findViewById(R.id.armsstart);

        if(armlevel == 1){
            armtopimage.setImageResource(R.drawable.armsbeginner);
            armtoptext.setText("ARMS BEGINNER");
            armstopdetailstext.setText("In this session of ARMS BEGINNER, you will do following exercises, 3 sets each with 10, 8 and 6 reps respectively.\nLook at the details of given exercise and hit start at the bottom when you are ready, our voice assistant will guide you through your workout.");
        }
        else if(armlevel == 2){
            armtopimage.setImageResource(R.drawable.armsintermediate);
            armtoptext.setText("ARMS INTERMEDIATE");
            armstopdetailstext.setText("In this session of ARMS INTERMEDIATE, you will do following exercises, 3 sets each with 20, 15 and 10 reps respectively.\nLook at the details of given exercise and hit start at the bottom when you are ready, our voice assistant will guide you through your workout.");

        }
        else if(armlevel == 3){
            armtopimage.setImageResource(R.drawable.armsprofessional);
            armtoptext.setText("ARMS PROFESSIONAL");
            armstopdetailstext.setText("In this session of ARMS PROFESSIONAL, you will do following exercises, 3 sets each with 30, 20 and 10 reps respectively.\nLook at the details of given exercise and hit start at the bottom when you are ready, our voice assistant will guide you through your workout.");

        }

    }
    public void listners(){
        dumbblecurlswatchvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=ykJmrZ5v0Oo")));
            }
        });
        barbellcurlsswatchvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=kwG2ipFRgfo")));
            }
        });
        hammercurlsswatchvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=TwD-YGVP4Bk")));
            }
        });
        seatedtricepswatchvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=kDbUNTX-Xts")));
            }
        });

        startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (armlevel == 1) {
                    Intent armsstartintent = new Intent(ArmsActivity.this , CameraActivity.class);
                    armsstartintent.putExtra("type",1);
                    startActivity(armsstartintent);
                }
                else if (armlevel == 2) {
                    Intent armsstartintent = new Intent(ArmsActivity.this , CameraActivity.class);
                    armsstartintent.putExtra("type",2);
                    startActivity(armsstartintent);
                }
                else if (armlevel == 3) {
                    Intent armsstartintent = new Intent(ArmsActivity.this , CameraActivity.class);
                    armsstartintent.putExtra("type",3);
                    startActivity(armsstartintent);
                }
            }
        });
    }
}