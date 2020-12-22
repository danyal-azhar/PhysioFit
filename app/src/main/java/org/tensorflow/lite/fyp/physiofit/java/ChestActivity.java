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

public class ChestActivity extends AppCompatActivity {

    private int chestlevel = 0;
    private ImageView chesttopimage;
    private TextView chesttoptext, chesttopdetailstext , pushupvideo, dumbbellflyvideo , barbellbenchpressvideo;
    private LinearLayout startbutton ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chest);

        chestlevel=getIntent().getIntExtra("level",1);

        initializers();
        listners();
    }
    public void initializers(){
        chesttopimage = findViewById(R.id.chesttopimage);
        chesttoptext = findViewById(R.id.chesttoptext);
        chesttopdetailstext = findViewById(R.id.chesttopdetailstext);

        pushupvideo = findViewById(R.id.pushupswatchvideo);
        dumbbellflyvideo = findViewById(R.id.dumbblefylswatchvideo);
        barbellbenchpressvideo = findViewById(R.id.barbellbenchpresswatchvideo);
        startbutton = findViewById(R.id.cheststart);

        if(chestlevel == 1){
            chesttopimage.setImageResource(R.drawable.chestbeginner);
            chesttoptext.setText("CHEST BEGINNER");
            chesttopdetailstext.setText("In this session of CHEST BEGINNER, you will do following exercises, 3 sets each with 10, 8 and 6 reps respectively.\nLook at the details of given exercise and hit start at the bottom when you are ready, our voice assistant will guide you through your workout.");

        }
        else if(chestlevel == 2){
            chesttopimage.setImageResource(R.drawable.chestinter);
            chesttoptext.setText("CHEST INTERMEDIATE");
            chesttopdetailstext.setText("In this session of CHEST INTERMEDIATE, you will do following exercises, 3 sets each with 20, 15 and 10 reps respectively.\nLook at the details of given exercise and hit start at the bottom when you are ready, our voice assistant will guide you through your workout.");

        }
        else if(chestlevel == 3){
            chesttopimage.setImageResource(R.drawable.chestpro);
            chesttoptext.setText("CHEST PROFESSIONAL");
            chesttopdetailstext.setText("In this session of CHEST PROFESSIONAL, you will do following exercises, 3 sets each with 30, 20 and 10 reps respectively.\nLook at the details of given exercise and hit start at the bottom when you are ready, our voice assistant will guide you through your workout.");

        }

    }
    public void listners(){

        pushupvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=JyCG_5l3XLk")));

            }
        });
        dumbbellflyvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=ajdFwa-qM98")));

            }
        });
        barbellbenchpressvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=4aVy2Xj6wYs")));

            }
        });

        startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chestlevel == 1) {
                    Intent armsstartintent = new Intent(ChestActivity.this , CameraActivity.class);
                    armsstartintent.putExtra("type",7);
                    startActivity(armsstartintent);
                }
                else if (chestlevel == 2) {
                    Intent armsstartintent = new Intent(ChestActivity.this , CameraActivity.class);
                    armsstartintent.putExtra("type",8);
                    startActivity(armsstartintent);
                }
                else if (chestlevel == 3) {
                    Intent armsstartintent = new Intent(ChestActivity.this , CameraActivity.class);
                    armsstartintent.putExtra("type",9);
                    startActivity(armsstartintent);
                }
            }
        });

    }
}