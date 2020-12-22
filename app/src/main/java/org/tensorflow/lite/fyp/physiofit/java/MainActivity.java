package org.tensorflow.lite.fyp.physiofit.java;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import org.tensorflow.lite.examples.posenet.R;
import org.tensorflow.lite.fyp.physiofit.utils.SharedPrefManager;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static int customset1 = 0;
    public static int customset2 = 0;
    public static int customset3 = 0;
    public static int customexno = 0;

    private LottieAnimationView BMIlottie , WEIGHTlottie , HEIGHTlottie;
    private TextView bmitext, bmino , heighttext , weighttext;

    private CardView armsbeginner,legsbeginner,chestbeginner,shoulderbeginner,backbeginner;
    private CardView armsinter,legsinter,chestinter,shoulderinter,backinter;
    private CardView armspro,legspro,chestpro,shoulderpro,backpro;
    private CardView customarm , customleg, customchest, customshoulder, customback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        intializers();
        listners();
    }
    public void intializers(){
        BMIlottie = findViewById(R.id.BMIlottie);
        HEIGHTlottie = findViewById(R.id.HEIGHTlottie);
        WEIGHTlottie = findViewById(R.id.WEIGHTlottie);

        bmitext = findViewById(R.id.bmitext);
        bmino = findViewById(R.id.bmino);
        bmino.setText(SharedPrefManager.getBMI(MainActivity.this));
        if(Double.parseDouble(bmino.getText().toString()) <= 18.5){
            bmitext.setText("Underweight");
        }
        else if(Double.parseDouble(bmino.getText().toString()) <= 24.9 && Double.parseDouble(bmino.getText().toString()) > 18.5){
            bmitext.setText("Normal");
        }
        else if(Double.parseDouble(bmino.getText().toString()) <= 29.9 && Double.parseDouble(bmino.getText().toString()) > 24.9 ){
            bmitext.setText("Overweight");
        }
        else if(Double.parseDouble(bmino.getText().toString()) > 29.9){
            bmitext.setText("Obese");
        }
        heighttext = findViewById(R.id.heighttext);
        heighttext.setText(SharedPrefManager.getHEIGHT(MainActivity.this)+"cm");
        weighttext = findViewById(R.id.weighttext);
        weighttext.setText(SharedPrefManager.getWEIGHT(MainActivity.this)+"kg");


        customarm = findViewById(R.id.customarmcard);
        customleg = findViewById(R.id.customlegcard);
        customchest = findViewById(R.id.customchestcard);
        customshoulder = findViewById(R.id.customshouldercard);
        customback = findViewById(R.id.custombackcard);

        armsbeginner = findViewById(R.id.armsbeginnercard);
        armsinter = findViewById(R.id.armsintercard);
        armspro = findViewById(R.id.armsprocard);

        legsbeginner = findViewById(R.id.legsbeginnercard);
        legsinter = findViewById(R.id.legsintercard);
        legspro = findViewById(R.id.legsprocard);

        chestbeginner = findViewById(R.id.chestbeginnercard);
        chestinter = findViewById(R.id.chestintercard);
        chestpro = findViewById(R.id.chestprocard);

        shoulderbeginner = findViewById(R.id.shoulderbeginnercard);
        shoulderinter = findViewById(R.id.shoulderintercard);
        shoulderpro = findViewById(R.id.shoulderprocard);

        backbeginner = findViewById(R.id.backbeginnercard);
        backinter = findViewById(R.id.backintercard);
        backpro = findViewById(R.id.backprocard);

    }

    public void listners(){

        BMIlottie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showbmiDialog(MainActivity.this);

            }
        });
        HEIGHTlottie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showheightDialog(MainActivity.this);
            }
        });
        WEIGHTlottie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showweightDialog(MainActivity.this);

            }
        });


        customarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this , ArmsCustomActivity.class);
                startActivity(intent);
            }
        });

        customleg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this , LegsCustomActivity.class);
                startActivity(intent);
            }
        });

        customchest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this , ChestCustomActivity.class);
                startActivity(intent);
            }
        });

        customshoulder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this , ShoulderCustomActivity.class);
                startActivity(intent);
            }
        });

        customback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this , BackCustomActivity.class);
                startActivity(intent);
            }
        });


        armsbeginner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ArmsActivity.class);
                intent.putExtra("level",1);
                startActivity(intent);
            }
        });
        armsinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ArmsActivity.class);
                intent.putExtra("level",2);
                startActivity(intent);
            }
        });
        armspro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ArmsActivity.class);
                intent.putExtra("level",3);
                startActivity(intent);
            }
        });


        legsbeginner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LegsActivity.class);
                intent.putExtra("level",1);
                startActivity(intent);
            }
        });
        legsinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,LegsActivity.class);
                intent.putExtra("level",2);
                startActivity(intent);
            }
        });
        legspro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,LegsActivity.class);
                intent.putExtra("level",3);
                startActivity(intent);
            }
        });


        chestbeginner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChestActivity.class);
                intent.putExtra("level",1);
                startActivity(intent);
            }
        });
        chestinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ChestActivity.class);
                intent.putExtra("level",2);
                startActivity(intent);
            }
        });
        chestpro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ChestActivity.class);
                intent.putExtra("level",3);
                startActivity(intent);
            }
        });


        shoulderbeginner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShoulderActivity.class);
                intent.putExtra("level",1);
                startActivity(intent);
            }
        });
        shoulderinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ShoulderActivity.class);
                intent.putExtra("level",2);
                startActivity(intent);
            }
        });
        shoulderpro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ShoulderActivity.class);
                intent.putExtra("level",3);
                startActivity(intent);
            }
        });


        backbeginner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BackActivity.class);
                intent.putExtra("level",1);
                startActivity(intent);
            }
        });
        backinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,BackActivity.class);
                intent.putExtra("level",2);
                startActivity(intent);
            }
        });
        backpro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,BackActivity.class);
                intent.putExtra("level",3);
                startActivity(intent);
            }
        });

    }



    //Dialogs
    //POPUPS
    public void showbmiDialog(Context context) {

        View view = LayoutInflater.from(context).inflate(R.layout.bmi_popup, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(view);
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setWindowAnimations(R.style.PopupAnimation);
        dialog.show();
        dialog.setCancelable(true);

        EditText height, weight;
        height = view.findViewById(R.id.enterheight);
        weight = view.findViewById(R.id.enterweight);

        Button calculate;
        calculate = view.findViewById(R.id.calculatebmi);

        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(height.getText().equals("") || weight.getText().equals("")){
                    Toast.makeText(MainActivity.this,"Please enter both fields correctly",Toast.LENGTH_SHORT).show();
                }
                else {
                    double newbmi = 0;
                    double newweight = Double.parseDouble(weight.getText().toString());
                    double newHeight = Double.parseDouble(height.getText().toString());
                    newbmi = (((newweight)/(newHeight))/(newHeight))*10000;
                    SharedPrefManager.setBMI(MainActivity.this, String.valueOf(newbmi));
                    bmino.setText(String.valueOf(newbmi));
                    if(Double.parseDouble(bmino.getText().toString()) <= 18.5){
                        bmitext.setText("Underweight");
                    }
                    else if(Double.parseDouble(bmino.getText().toString()) <= 24.9 && Double.parseDouble(bmino.getText().toString()) > 18.5){
                        bmitext.setText("Normal");
                    }
                    else if(Double.parseDouble(bmino.getText().toString()) <= 29.9 && Double.parseDouble(bmino.getText().toString()) > 24.9 ){
                        bmitext.setText("Overweight");
                    }
                    else if(Double.parseDouble(bmino.getText().toString()) > 29.9){
                        bmitext.setText("Obese");
                    }
                    heighttext.setText(height.getText().toString()+"cm");
                    weighttext.setText(weight.getText().toString()+"kg");
                    dialog.dismiss();
                }
            }
        });
    }

    public void showheightDialog(Context context) {

        View view = LayoutInflater.from(context).inflate(R.layout.height_popup, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(view);
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setWindowAnimations(R.style.PopupAnimation);
        dialog.show();
        dialog.setCancelable(true);

        EditText height;
        height = view.findViewById(R.id.entergeightonly);

        Button save ;
        save = view.findViewById(R.id.saveheight);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(height.getText().equals("")){
                    Toast.makeText(MainActivity.this,"Please enter height",Toast.LENGTH_SHORT).show();
                }
                else {
                    SharedPrefManager.setHEIGHT(MainActivity.this, height.getText().toString());
                    double newbmi = 0;
                    double newweight = Double.parseDouble(SharedPrefManager.getWEIGHT(MainActivity.this));
                    double newHeight = Double.parseDouble(height.getText().toString());
                    newbmi = (((newweight)/(newHeight))/(newHeight))*10000;
                    SharedPrefManager.setBMI(MainActivity.this, String.valueOf(newbmi));
                    bmino.setText(String.valueOf(newbmi));
                    if(Double.parseDouble(bmino.getText().toString()) <= 18.5){
                        bmitext.setText("Underweight");
                    }
                    else if(Double.parseDouble(bmino.getText().toString()) <= 24.9 && Double.parseDouble(bmino.getText().toString()) > 18.5){
                        bmitext.setText("Normal");
                    }
                    else if(Double.parseDouble(bmino.getText().toString()) <= 29.9 && Double.parseDouble(bmino.getText().toString()) > 24.9 ){
                        bmitext.setText("Overweight");
                    }
                    else if(Double.parseDouble(bmino.getText().toString()) > 29.9){
                        bmitext.setText("Obese");
                    }
                    heighttext.setText(height.getText().toString());
                    weighttext.setText(SharedPrefManager.getWEIGHT(MainActivity.this)+"kg");
                    heighttext.setText(SharedPrefManager.getHEIGHT(MainActivity.this)+"cm");
                    dialog.dismiss();
                }
            }
        });

    }

    //POPUPS
    public void showweightDialog(Context context) {

        View view = LayoutInflater.from(context).inflate(R.layout.weight_popup, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setView(view);
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setWindowAnimations(R.style.PopupAnimation);
        dialog.show();
        dialog.setCancelable(true);
        EditText weight;
        weight = view.findViewById(R.id.enterweightonly);

        Button save ;
        save = view.findViewById(R.id.saveweight);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(weight.getText().equals("")){
                    Toast.makeText(MainActivity.this,"Please enter weight",Toast.LENGTH_SHORT).show();
                }
                else {
                    SharedPrefManager.setWEIGHT(MainActivity.this,weight.getText().toString());
                    double newbmi = 0;
                    double newweight = Double.parseDouble(weight.getText().toString());
                    double newHeight = Double.parseDouble(SharedPrefManager.getHEIGHT(MainActivity.this));
                    newbmi = (((newweight)/(newHeight))/(newHeight))*10000;
                    SharedPrefManager.setBMI(MainActivity.this, String.valueOf(newbmi));
                    bmino.setText(String.valueOf(newbmi));
                    if(Double.parseDouble(bmino.getText().toString()) <= 18.5){
                        bmitext.setText("Underweight");
                    }
                    else if(Double.parseDouble(bmino.getText().toString()) <= 24.9 && Double.parseDouble(bmino.getText().toString()) > 18.5){
                        bmitext.setText("Normal");
                    }
                    else if(Double.parseDouble(bmino.getText().toString()) <= 29.9 && Double.parseDouble(bmino.getText().toString()) > 24.9 ){
                        bmitext.setText("Overweight");
                    }
                    else if(Double.parseDouble(bmino.getText().toString()) > 29.9){
                        bmitext.setText("Obese");
                    }
                    heighttext.setText(SharedPrefManager.getHEIGHT(MainActivity.this)+"cm");
                    weighttext.setText(SharedPrefManager.getWEIGHT(MainActivity.this));
                    weighttext.setText(SharedPrefManager.getWEIGHT(MainActivity.this)+"kg");
                    dialog.dismiss();
                }
            }
        });

    }


}