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

public class LegsActivity extends AppCompatActivity {

    private int legslevel = 0;
    private ImageView legstopimage;
    private TextView legstoptext , legstopdetailsext, squatsvideo , lungesvideo, legpressvideo ;
    private LinearLayout startbutton ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legs);
        legslevel=getIntent().getIntExtra("level",1);

        initializers();
        listners();
    }
    public void initializers(){
        legstopimage = findViewById(R.id.legstopimage);
        legstoptext = findViewById(R.id.legstoptext);
        legstopdetailsext = findViewById(R.id.legstopdetailstext);

        squatsvideo = findViewById(R.id.squatwatchvideo);
        lungesvideo = findViewById(R.id.lungeswatchvideo);
        legpressvideo = findViewById(R.id.legpresswatchvideo);
        startbutton = findViewById(R.id.legsstart);

        if(legslevel == 1){
            legstopimage.setImageResource(R.drawable.legsbeginner);
            legstoptext.setText("LEGS BEGINNER");
            legstopdetailsext.setText("In this session of LEGS BEGINNER, you will do following exercises, 3 sets each with 10, 8 and 6 reps respectively.\n Look at the details of given exercise and hit start at the bottom when you are ready, our voice assistant will guide you through your workout.");

        }
        else if(legslevel == 2){
            legstopimage.setImageResource(R.drawable.legsinter);
            legstoptext.setText("LEGS INTERMEDIATE");
            legstopdetailsext.setText("In this session of LEGS INTERMEDIATE, you will do following exercises, 3 sets each with 20, 15 and 10 reps respectively.\n Look at the details of given exercise and hit start at the bottom when you are ready, our voice assistant will guide you through your workout.");

        }
        else if(legslevel == 3){
            legstopimage.setImageResource(R.drawable.legspro);
            legstoptext.setText("LEGS PROFESSIONAL");
            legstopdetailsext.setText("In this session of LEGS PROFESSIONAL, you will do following exercises, 3 sets each with 30, 20 and 10 reps respectively.\n Look at the details of given exercise and hit start at the bottom when you are ready, our voice assistant will guide you through your workout.");

        }

    }
    public void listners(){

        squatsvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=MVMNk0HiTMg")));

            }
        });
        lungesvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=COKYKgQ8KR0")));

            }
        });
        legpressvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=U9dnM3dguLc")));

            }
        });

        startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (legslevel == 1) {
                    Intent armsstartintent = new Intent(LegsActivity.this , CameraActivity.class);
                    armsstartintent.putExtra("type",4);
                    startActivity(armsstartintent);
                }
                else if (legslevel == 2) {
                    Intent armsstartintent = new Intent(LegsActivity.this , CameraActivity.class);
                    armsstartintent.putExtra("type",5);
                    startActivity(armsstartintent);
                }
                else if (legslevel == 3) {
                    Intent armsstartintent = new Intent(LegsActivity.this , CameraActivity.class);
                    armsstartintent.putExtra("type",6);
                    startActivity(armsstartintent);
                }
            }
        });

    }
}