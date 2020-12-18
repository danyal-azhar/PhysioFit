package fyp.physiofit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;

import  fyp.physiofit.R;

import java.util.Locale;

public class SplashActivity extends Activity {
    private Handler mWaitHandler = new Handler();
    private Button ahh;
    TextToSpeech ahhh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ahh=(Button)findViewById(R.id.ahh);
        ahh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ahhh.setLanguage(Locale.US);

                String toSpeak2 = "Welcome to your personal gym trainer";


                //Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();

                ahhh.setSpeechRate((float) 0.3);

                ahhh.speak(toSpeak2, TextToSpeech.QUEUE_FLUSH, null,null);

            }
        });
        ahhh=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {

                    ahhh.setLanguage(Locale.US);


                }

            }
        });

        mWaitHandler.postDelayed(new Runnable() {

            @Override
            public void run() {

                //The following code will execute after the 5 seconds.

                try {

                    //Go to next page i.e, start the next activity.
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);

                    //Let's Finish Splash Activity since we don't want to show this when user press back button.
                    finish();
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
        }, 3000);  // Give a 5 seconds delay.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Remove all the callbacks otherwise navigation will execute even after activity is killed or closed.
        mWaitHandler.removeCallbacksAndMessages(null);
    }
}



