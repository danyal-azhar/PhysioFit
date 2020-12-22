
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

public class BackCustomActivity extends AppCompatActivity {

    private ImageView backtopimage;
    private TextView backtoptext, backtopdetailstext , latpulldownvideo , chinupsvideo, seatedrowingvideo;
    private CardView latpulldown,chinups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backcustom);

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

        latpulldown = findViewById(R.id.latpulldowncard);
        chinups = findViewById(R.id.chinupscard);


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

        latpulldown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showcustomsetspopup(BackCustomActivity.this,1);
            }
        });
        chinups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showcustomsetspopup(BackCustomActivity.this,2);
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
                Intent armsstartintent = new Intent(BackCustomActivity.this , CameraActivity.class);
                armsstartintent.putExtra("type",20);
                startActivity(armsstartintent);
            }
        });

    }
}