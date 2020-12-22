package org.tensorflow.lite.fyp.physiofit.java;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.tensorflow.lite.fyp.physiofit.CameraActivity;
import org.tensorflow.lite.examples.posenet.R;

import java.util.Objects;

import static org.tensorflow.lite.fyp.physiofit.java.MainActivity.customexno;
import static org.tensorflow.lite.fyp.physiofit.java.MainActivity.customset1;
import static org.tensorflow.lite.fyp.physiofit.java.MainActivity.customset2;
import static org.tensorflow.lite.fyp.physiofit.java.MainActivity.customset3;

public class LegsCustomActivity extends AppCompatActivity {

    private ImageView legstopimage;
    private TextView legstoptext, legstopdetailsext, squatsvideo, lungesvideo, legpressvideo;
    private CardView squatscard, lungescard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legscustom);

        initializers();
        listners();
    }

    public void initializers() {
        legstopimage = findViewById(R.id.legstopimage);
        legstoptext = findViewById(R.id.legstoptext);
        legstopdetailsext = findViewById(R.id.legstopdetailstext);

        squatsvideo = findViewById(R.id.squatwatchvideo);
        lungesvideo = findViewById(R.id.lungeswatchvideo);
        legpressvideo = findViewById(R.id.legpresswatchvideo);

        squatscard = findViewById(R.id.squatscard);
        lungescard = findViewById(R.id.lungescard);

    }

    public void listners() {

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

        squatscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showcustomsetspopup(LegsCustomActivity.this,1);
            }
        });
        lungescard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showcustomsetspopup(LegsCustomActivity.this,2);
            }
        });

    }


    //POPUPS
    public void showcustomsetspopup(Context context, int exno) {

        View view = LayoutInflater.from(context).inflate(R.layout.customsets_popup, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setView(view);
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setWindowAnimations(R.style.PopupAnimation);
        dialog.show();
        dialog.setCancelable(true);

        EditText set1,set2,set3;
        set1 = view.findViewById(R.id.set1reps);
        set2 = view.findViewById(R.id.set2reps);
        set3 = view.findViewById(R.id.set3reps);

        Button start ;
        start = view.findViewById(R.id.startcustom);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                customset1 = Integer.parseInt(set1.getText().toString());
                customset2 = Integer.parseInt(set2.getText().toString());
                customset3 = Integer.parseInt(set3.getText().toString());
                customexno = exno;
                dialog.dismiss();
                Intent armsstartintent = new Intent(LegsCustomActivity.this , CameraActivity.class);
                armsstartintent.putExtra("type",17);
                startActivity(armsstartintent);
            }
        });

    }
}