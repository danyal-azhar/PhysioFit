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

public class ShoulderCustomActivity extends AppCompatActivity {

    private ImageView shouldertopimage;
    private TextView shouldertoptext, shouldertopdetailstext, dumbbleshoulderpressvideo, overheadpressvideo;
    private CardView shoulderpresscard, overheadpresscard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shouldercustom);

        initializers();
        listners();
    }

    public void initializers() {
        shouldertopimage = findViewById(R.id.shouldertopimage);
        shouldertoptext = findViewById(R.id.shouldertoptext);
        shouldertopdetailstext = findViewById(R.id.shouldertopdetailstext);

        dumbbleshoulderpressvideo = findViewById(R.id.dumbbleshoulderpresstachvideo);
        overheadpressvideo = findViewById(R.id.overheadpresswatchvideo);

        shoulderpresscard = findViewById(R.id.dumbbellshoulderpresscard);
        overheadpresscard = findViewById(R.id.overheadpresscard);
    }

    public void listners() {

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

        shoulderpresscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showcustomsetspopup(ShoulderCustomActivity.this,1);
            }
        });

        overheadpresscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showcustomsetspopup(ShoulderCustomActivity.this,1);
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
                Intent armsstartintent = new Intent(ShoulderCustomActivity.this , CameraActivity.class);
                armsstartintent.putExtra("type",19);
                startActivity(armsstartintent);
            }
        });

    }
}