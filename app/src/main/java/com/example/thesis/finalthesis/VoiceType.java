package com.example.thesis.finalthesis;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


public class VoiceType extends AppCompatActivity {


    public static final String TAG = "VoiceType";
    private SoundAnalyzer soundAnalyzer;
    private ImageButton record;
    final Context context = this;
    private RadioButton Male, Female;
    private int flag_record =0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_type);
        Toolbar toolbar = (Toolbar) findViewById(R.id.voicetoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        // new 一SoundAnalyze
        try {
            soundAnalyzer = new SoundAnalyzer();
        } catch (Exception e) {
            Log.e(TAG, "Exception when instantiating SoundAnalyzer: " + e.getMessage());
            Toast.makeText(this, "Error:" + e, Toast.LENGTH_SHORT).show();
        }


        GenderPrompt();

        /** if the gender' is selected then we assume that we need two output which is
         *
         * For Female
         *
         * Alto
         * Soprano
         *
         * For Male
         *
         * Tenor
         * Bass
         *
         */

        record = (ImageButton) findViewById(R.id.typeRecord);
        record.setImageResource(R.drawable.microphone);
        record.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                if(flag_record==0){
                    record.setImageResource(R.drawable.stop);
                    StartRecord();
                }

                /**if click stop, then the recorder will analyze the voice
                 * and pop up the result which is the resultLayout();
                 *
                */


                else{

                    /**
                     *Pop up the result and stop the soundAnalyzer
                     *
                     *
                     * But first things first we need to retrieved the data of the soundAnalyzer's result
                     *
                     * so that we can transfor it to the goal or specific output
                     *
                     *
                     */


                    StopRecord();
                }
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    //Hit the button to start
    private void StartRecord(){
        if (soundAnalyzer != null) {
            soundAnalyzer.start();
        }
        else {
            Log.d(TAG, "soundanalyzer not found");


        }

    }


    private void StopRecord(){
        soundAnalyzer.stop();

        //Output the voice type result
        try{
            //call the soundAnalyzer class to retrieved the frequency results

            //if soundanalyzer.getfrequency.equals(/*Specific Frequency for the type of voice)



            //Output the result



        }catch (Exception ex){
            Toast.makeText(this, "Error:" + ex, Toast.LENGTH_SHORT).show();

        }

    }
    private void GenderPrompt(){
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.voicesheet_gender, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

            Male =(RadioButton) promptsView.findViewById(R.id.radioMale);
            Female=(RadioButton) promptsView.findViewById(R.id.radioFemale);


        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                //edit text

                                if(Male.isChecked()){
                                    Toast.makeText(context, "Gender:" + Male.getText().toString(), Toast.LENGTH_SHORT).show();

                                    //for male mHz

                                }
                                else{
                                    Toast.makeText(context, "Gender: " + Female.getText().toString(), Toast.LENGTH_SHORT).show();
                                }

                            }
                            });



        //this will make some financial gain, because some matters it makes me mad, why?
        //because of some matter of time
        //keeping worries is also fine.

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }


//run permission on real time thread so that the API 23 and up can use the app
    //base on the given selection

}
