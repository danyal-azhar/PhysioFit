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

public class ShoulderActivity extends AppCompatActivity {


    private int shoulderlevel = 0;
    private ImageView shouldertopimage;
    private TextView shouldertoptext, shouldertopdetailstext , dumbbleshoulderpressvideo , overheadpressvideo;
    private LinearLayout startbutton ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoulders);
        shoulderlevel=getIntent().getIntExtra("level",1);

        initializers();
        listners();
    }
    public void initializers(){
        shouldertopimage = findViewById(R.id.shouldertopimage);
        shouldertoptext = findViewById(R.id.shouldertoptext);
        shouldertopdetailstext = findViewById(R.id.shouldertopdetailstext);

        dumbbleshoulderpressvideo = findViewById(R.id.dumbbleshoulderpresstachvideo);
        overheadpressvideo = findViewById(R.id.overheadpresswatchvideo);
        startbutton = findViewById(R.id.shoulderstart);


        if(shoulderlevel == 1){
            shouldertopimage.setImageResource(R.drawable.shoulderbeginner);
            shouldertoptext.setText("SHOULDER BEGINNER");
            shouldertopdetailstext.setText("In this session of SHOULDER BEGINNER, you will do following exercises, 3 sets each with 10, 8 and 6 reps respectively.\nLook at the details of given exercise and hit start at the bottom when you are ready, our voice assistant will guide you through your workout.");

        }
        else if(shoulderlevel == 2){
            shouldertopimage.setImageResource(R.drawable.shoulderinter);
            shouldertoptext.setText("SHOULDER INTERMEDIATE");
            shouldertopdetailstext.setText("In this session of SHOULDER INTERMEDIATE, you will do following exercises, 3 sets each with 20, 15 and 10 reps respectively.\nLook at the details of given exercise and hit start at the bottom when you are ready, our voice assistant will guide you through your workout.");

        }
        else if(shoulderlevel == 3){
            shouldertopimage.setImageResource(R.drawable.shoulderpro);
            shouldertoptext.setText("SHOULDER PROFESSIONAL");
            shouldertopdetailstext.setText("In this session of SHOULDER PROFESSIONAL, you will do following exercises, 3 sets each with 30, 20 and 10 reps respectively.\nLook at the details of given exercise and hit start at the bottom when you are ready, our voice assistant will guide you through your workout.");

        }
    }
    public void listners(){

        dumbbleshoulderpressvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=Z5g48LuHB9s")));

            }
        });
        overheadpressvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=_RlRDWO2jfg")));

            }
        });

        startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shoulderlevel == 1) {
                    Intent armsstartintent = new Intent(ShoulderActivity.this , CameraActivity.class);
                    armsstartintent.putExtra("type",10);
                    startActivity(armsstartintent);
                }
                else if (shoulderlevel == 2) {
                    Intent armsstartintent = new Intent(ShoulderActivity.this , CameraActivity.class);
                    armsstartintent.putExtra("type",11);
                    startActivity(armsstartintent);
                }
                else if (shoulderlevel == 3) {
                    Intent armsstartintent = new Intent(ShoulderActivity.this , CameraActivity.class);
                    armsstartintent.putExtra("type",12);
                    startActivity(armsstartintent);
                }
            }
        });
    }
}