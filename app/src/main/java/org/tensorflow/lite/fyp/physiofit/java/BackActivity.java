package org.tensorflow.lite.fyp.physiofit.java;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.tensorflow.lite.examples.posenet.R;
import org.tensorflow.lite.fyp.physiofit.CameraActivity;

public class BackActivity extends AppCompatActivity {


    private int backlevel = 0;
    private ImageView backtopimage;
    private TextView backtoptext, backtopdetailstext , latpulldownvideo , chinupsvideo, seatedrowingvideo;
    private LinearLayout startbutton ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back);

        backlevel=getIntent().getIntExtra("level",1);

        initializers();
        listners();
    }
    public void initializers(){
        backtopimage = findViewById(R.id.backtopimage);
        backtoptext = findViewById(R.id.backtoptext);
        backtopdetailstext = findViewById(R.id.backtopdetailstext);

        latpulldownvideo = findViewById(R.id.latpulldownwatchvideo);
        chinupsvideo = findViewById(R.id.chinupswatchvideo);
        seatedrowingvideo = findViewById(R.id.seatedrowingwatchvideo);
        startbutton = findViewById(R.id.backstart);

        if(backlevel == 1){
            backtopimage.setImageResource(R.drawable.backbeginner);
            backtoptext.setText("BACK BEGINNER");
            backtopdetailstext.setText("In this session of BACK BEGINNER, you will do following exercises, 3 sets each with 10, 8 and 6 reps respectively.\nLook at the details of given exercise and hit start at the bottom when you are ready, our voice assistant will guide you through your workout.");

        }
        else if(backlevel == 2){
            backtopimage.setImageResource(R.drawable.backinter);
            backtoptext.setText("BACK INTERMEDIATE");
            backtopdetailstext.setText("In this session of BACK INTERMEDIATE, you will do following exercises, 3 sets each with 20, 15 and 10 reps respectively.\nLook at the details of given exercise and hit start at the bottom when you are ready, our voice assistant will guide you through your workout.");

        }
        else if(backlevel == 3){
            backtopimage.setImageResource(R.drawable.backpro);
            backtoptext.setText("BACK PROFESSIONAL");
            backtopdetailstext.setText("In this session of BACK PROFESSIONAL, you will do following exercises, 3 sets each with 30, 20 and 10 reps respectively.\nLook at the details of given exercise and hit start at the bottom when you are ready, our voice assistant will guide you through your workout.");

        }

    }
    public void listners(){

        latpulldownvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=u3gQT2aMVaI")));

            }
        });

        chinupsvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=_71FpEaq-fQ")));

            }
        });

        seatedrowingvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=GZbfZ033f74")));

            }
        });

        startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (backlevel == 1) {
                    Intent armsstartintent = new Intent(BackActivity.this , CameraActivity.class);
                    armsstartintent.putExtra("type",13);
                    startActivity(armsstartintent);
                }
                else if (backlevel == 2) {
                    Intent armsstartintent = new Intent(BackActivity.this , CameraActivity.class);
                    armsstartintent.putExtra("type",14);
                    startActivity(armsstartintent);
                }
                else if (backlevel == 3) {
                    Intent armsstartintent = new Intent(BackActivity.this , CameraActivity.class);
                    armsstartintent.putExtra("type",15);
                    startActivity(armsstartintent);
                }
            }
        });

    }
}