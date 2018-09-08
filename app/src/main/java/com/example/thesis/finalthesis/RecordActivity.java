package com.example.thesis.finalthesis;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.VoiceInteractor;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.AlphabeticIndex;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.os.Bundle;

import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.content.Context;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.Inflater;


import nu.xom.Document;
import nu.xom.Serializer;
import nu.xom.Text;


public class RecordActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener {


    public static final String TAG = "recorderActivity";
    private SoundAnalyzer soundAnalyzer;
    private int recordFlag = 0;
    private ImageButton recordBtn;
    public ProgressDialog progressDialog;
    private double beatCtr = 0.0;
    private String outputFileName = "";
    private Timer timer;
    private TextView timerView;
    private TextView statusView;
    private int recorderSecondsElapsed = 0;
    private DrawerLayout drawer;
    private TextView tapButton;
    private TextView tapStop;
    String result;
    public String namefile;
    final Context context = this;
    private EditText musicsheet_title;
    private Button submit;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private Context mContext;

    public String title_result;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        try {
            Log.d(TAG, "onCreate()");
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_record);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

          /*  mContext = getApplicationContext();
            if(checkAndRequestPermissions()) {
                // carry on the normal flow, as the case of  permissions  granted.
            }*/

          //ActivityCompat.requestPermissions(this, permissions,REQUEST_ID_MULTIPLE_PERMISSIONS);
            musicsheet_title =(EditText) findViewById(R.id.title_musicsheet);
            tapButton = (TextView) findViewById(R.id.buttontap);
            tapStop = findViewById(R.id.buttontap_stop);
            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            //timer view for timer
            timerView = (TextView) findViewById(R.id.timer);
            statusView = (TextView) findViewById(R.id.status);
            /*
             * recordBtn
             * onTouchListeneronClickListener
             * */
            recordBtn = (ImageButton) findViewById(R.id.microphone);
            recordBtn.setImageResource(R.drawable.micro);

            //things to do:
            //ask permissiion if the applioction is API 23 up
           /* if (Build.VERSION.SDK_INT >= 23) {
                String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if (!hasPermissions(mContext, PERMISSIONS)) {
                    ActivityCompat.requestPermissions((Activity) mContext, PERMISSIONS, REQUEST );
                } else {
                    //do here
                }
            } else {
                //do here
            }*/
            // Click Listener
            recordBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    title_result = musicsheet_title.getText().toString();
                    musicsheet_title.setVisibility(EditText.INVISIBLE);
                    if (recordFlag == 0) {  //
                        // click record button
                        recordBtn.setImageResource(R.drawable.un_micro);
                        startRecord();
                        startTimer();
                    } else {
                        // click stop button
                        confirmStopRecord();
                        stopTimer();
                    }
                }
            });

            // new 一SoundAnalyze
            try {
                soundAnalyzer = new SoundAnalyzer();
            } catch (Exception e) {
                Log.e(TAG, "Exception when instantiating SoundAnalyzer: " + e.getMessage());
            }
        } catch (Exception ex) {
            Toast.makeText(this, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();

        }

    }
/*
    private  boolean checkAndRequestPermissions() {
        int permissionRecordAudio = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        int permissionReadExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWriteExternalStorage =  ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionRecordAudio != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (permissionReadExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionWriteExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }*/
   /* @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d(TAG, "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "Record Audio, Read and Write permission Granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)  || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                         || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                        {
                            showDialogOK("Record, Read and Write to storages are needed to your app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }*/

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }


   /* private boolean permissionToRecordAccepted = false;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }*/




    /*private static final int MY_PERMISSIONS_REQUEST_CODE = 123;
    protected void checkPermission(){
        if(ContextCompat.checkSelfPermission(RecordActivity.this,Manifest.permission.RECORD_AUDIO)
                + ContextCompat.checkSelfPermission(
                RecordActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(
                RecordActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){

            // Do something, when permissions not granted
            if(ActivityCompat.shouldShowRequestPermissionRationale(
                    RecordActivity.this,Manifest.permission.RECORD_AUDIO)
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    RecordActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    RecordActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                // If we should give explanation of requested permissions

                // Show an alert dialog here with request explanation
                AlertDialog.Builder builder = new AlertDialog.Builder(RecordActivity.this);
                builder.setMessage("Record Audio, Read and Write External" +
                        " Storage permissions are required to do the task.");
                builder.setTitle("Please grant those permissions");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(
                                RecordActivity.this,
                                new String[]{
                                        Manifest.permission.RECORD_AUDIO,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                },
                                MY_PERMISSIONS_REQUEST_CODE
                        );
                    }
                });
                builder.setNeutralButton("Cancel",null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }else{
                // Directly request for required permissions, without explanation
                ActivityCompat.requestPermissions(
                        RecordActivity.this,
                        new String[]{
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        MY_PERMISSIONS_REQUEST_CODE
                );
            }
        }else {
            // Do something, when permissions are already granted
            Toast.makeText(mContext,"Permissions already granted",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_CODE:{
                // When request is cancelled, the results array are empty
                if(
                        (grantResults.length >0) &&
                                (grantResults[0]
                                        + grantResults[1]
                                        + grantResults[2]
                                        == PackageManager.PERMISSION_GRANTED
                                )
                        ){
                    // Permissions are granted
                    Toast.makeText(mContext,"Permissions granted.",Toast.LENGTH_SHORT).show();
                }else {
                    // Permissions are denied
                    Toast.makeText(mContext,"Permissions denied.",Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }*/




    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

         if (id == R.id.nav_settings) {
            startActivity(new Intent(RecordActivity.this, Settings.class));
            drawer.closeDrawers();
            return true;

        } else if (id == R.id.nav_about) {
            startActivity(new Intent(RecordActivity.this, About.class));
            drawer.closeDrawers();
            return true;

        }


        else if(id==R.id.scores){
             startActivity(new Intent(RecordActivity.this, SavedScores.class));
             drawer.closeDrawers();
             return true;


         }
        else if (id == R.id.nav_help) {
            startActivity(new Intent(RecordActivity.this, Help.class));
            drawer.closeDrawers();
            return true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Start the sound Recorder
    private void startRecord() {
        statusView.setVisibility(View.VISIBLE);
        tapStop.setVisibility(View.VISIBLE);
        tapButton.setVisibility(View.INVISIBLE);
        recordFlag = 1;
        Log.d(TAG, "recorder onStart()");
        if (soundAnalyzer != null) {
            soundAnalyzer.start();
        } else {
            Log.d(TAG, "soundanalyzer not found");
        }
    }
    //showinputprompt


    // Stop the Recorder
    private void stopRecord() {


        progressDialog = new ProgressDialog(RecordActivity.this, R.style.Theme_MyDialog);
        progressDialog.setMessage("Converting Voice to Notes...");
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));
        progressDialog.setIndeterminate(false);
        progressDialog.show();
        recordFlag = 0;
        soundAnalyzer.stop();

        final Intent intent = new Intent();         //
        intent.setClass(RecordActivity.this, ScoreActivity.class);


        new Thread(new Runnable() {     //
            @Override
            public void run() {
                transformSoundData(intent);     // transformSoundData

                try {
                    Thread.sleep(1000);
                    progressDialog.dismiss();       //
                } catch (Exception e) {
                    Log.e(TAG, "Exception when after transforming data sleeping.");
                } finally {
                    try {
                        Bundle bundle = new Bundle();
                        bundle.putString("fileName", getOutputFileName());

                        intent.putExtras(bundle);

                        startActivity(intent); // intent
                        RecordActivity.this.finish();
                    } catch (Exception ex) {
                        Toast.makeText(RecordActivity.this, "Error:" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Error: " + ex.getMessage());
                    }
                }
            }
        }).start();

    }

    /*
     */

    private void transformSoundData(Intent it) {
        NoteAnalyzer noteAnalyzer = new NoteAnalyzer();     // NoteAnalyzer

        LinkedList frequencyList;
        frequencyList = soundAnalyzer.getFrequencyMessage();    // SoundAnalyzer

        LinkedList noteList = noteAnalyzer.analyzeNote(frequencyList); //noteAnalyzer

        TempleAnalyzer templeAnalyzer = new TempleAnalyzer();   // TempleAnalyzer

        LinkedList dataList;                                // TempleAnalyzer
        dataList = templeAnalyzer.analyzeTemple(noteList, 80);          //dataList

        int nodeNum = dataList.size();  //

        //Bundle bundle = new Bundle();   // new 一
        //bundle.putInt("nodeNum", nodeNum);  // Bundle

        //  musicXML
        MusicXmlRenderer mxlRender = new MusicXmlRenderer();
        mxlRender.newVoice();
        mxlRender.measureEvent();
        mxlRender.changeSystemEvent(false);

        int measureCtr = -1; //

        //  一note
        for (int num = 0; num < nodeNum; num++) {
            String data = dataList.pop().toString();
            String node[] = data.split(",");
            String value = node[0];
            int key = Integer.parseInt(node[1]);
            int beat = Integer.parseInt(node[2]);
            //bundle.putString("node"+num,data);  //Bundle

            countBeat(beat);

            // Doing Quantization
            // if next voice exceed the measure size then stuffing a rest
            if (beatCtr > 4.0) {
                double temp = 0;
                switch (beat) {
                    case 4:
                        temp = 1.0;
                        break;
                    case 8:
                        temp = 0.5;
                        break;
                    case 16:
                        temp = 0.25;
                        break;
                }
                double quantizeBeat = (4 - (beatCtr - temp));

                int quantizeBeat2;
                switch ("" + quantizeBeat) {
                    case "0.25":
                        quantizeBeat2 = 16;
                        mxlRender.noteEvent("rest" + "n\u0020", 4, quantizeBeat2);
                        break;
                    case "0.5":
                        quantizeBeat2 = 8;
                        mxlRender.noteEvent("rest" + "n\u0020", 4, quantizeBeat2);
                        break;
                    case "0.75":
                        quantizeBeat2 = 8;
                        mxlRender.noteEvent("rest" + "n\u0020", 4, quantizeBeat2);
                        quantizeBeat2 = 16;
                        mxlRender.noteEvent("rest" + "n\u0020", 4, quantizeBeat2);
                        break;
                }
                mxlRender.measureEvent();
                setBeatCtrZero();   // set beatCtr 0
                measureCtr++;
                if (measureCtr == 0) //
                {
                    mxlRender.changeSystemEvent(true);
                } else {
                    mxlRender.changeSystemEvent(false);
                    measureCtr = -1;
                }

                mxlRender.noteEvent(value, key, beat);
                countBeat(beat);
            } else if (beatCtr == 4.0)    //  the beat counter equal to 4 exactly
            {
                mxlRender.noteEvent(value, key, beat);
                mxlRender.measureEvent();
                setBeatCtrZero();   // set beatCtr 0
                measureCtr++;

                if (measureCtr == 0) //
                {
                    mxlRender.changeSystemEvent(true);
                } else {
                    mxlRender.changeSystemEvent(false);
                    measureCtr = -1;
                }
            } else {  // normal situation
                mxlRender.noteEvent(value, key, beat);
            }

            // etProgress
            double d = (100 * num) / nodeNum;
            int sendNum = (int) d;

            try {
                progressDialog.setProgress(sendNum);
                Thread.sleep(100);  // 0.05s
            } catch (Exception e) {
                Log.e(TAG, "Exception when running thread transformSoundData: " + e.getMessage());
            }
        }
        //it.putExtras(bundle);   // BundleIntent

        OutputFile(mxlRender);


        progressDialog.setProgress(100);    // 100%*/
    }

    private void setOutputFileName(String s) {
        outputFileName = s;
    }

    private String getOutputFileName() {
        return outputFileName;
    }

    /* Output the musicXML temp file to cellPhone internal storage as an asset
     *  This temp file will be process soon
     *
     */

    private void OutputFile(MusicXmlRenderer musicXmlRenderer) {
        // set the saving directory
        // default dir is /data/data/packageName/files/
        File savingDir = getFilesDir();
        String path = Environment.getExternalStorageDirectory()+"/Documents/";
        File filepath = new File(path,title_result+ ".xml");
        File fileXML = new File(savingDir, title_result + ".xml");
        setOutputFileName(title_result);
        FileOutputStream fosXML;
        FileOutputStream fosxmlpath;
        try {
            fosXML = new FileOutputStream(fileXML);
            fosxmlpath = new FileOutputStream(filepath);
            Document mxlDoc = musicXmlRenderer.getMusicXMLDoc();
            //	write the MusicXML file formatted
            Serializer ser = new Serializer(fosXML, "UTF-8");
            Serializer serpath = new Serializer(fosxmlpath, "UTF-8");
            serpath.setIndent(4);
            serpath.write(mxlDoc);
            fosxmlpath.close();
            ser.setIndent(4);
            ser.write(mxlDoc);
            fosXML.close();
        } catch (FileNotFoundException fileNotFoundException) {
            Log.e(TAG, "found fileNotFoundException when generate a file.");
        } catch (IOException ioException) {
            Log.e(TAG, "found ioException when serialize the musicXML.");
        }
    }

    // Output the musicXML file to Phone external storage

   /* private void OutputFile_To_External_Storage(MusicXmlRenderer musicXmlRenderer) {
        try {
            File savingDir = null;

            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File sdcard = Environment.getExternalStorageDirectory();
                String path = sdcard.getAbsolutePath() + "/VoiceSheetThesis";

                savingDir = new File(path);
                if (!savingDir.exists())   // if directory isn't exist
                {
                    savingDir.mkdir();    // make a directory
                }
            }

            // generate a file named by dateTime
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            File fileXML = new File(savingDir, result + ".xml");
            try {
                FileOutputStream fosXML = new FileOutputStream(fileXML, false);

                Document mxlDoc = musicXmlRenderer.getMusicXMLDoc();

                //	write the MusicXML file formatted
                Serializer ser = new Serializer(fosXML, "UTF-8");
                ser.setIndent(4);
                ser.write(mxlDoc);

                //Log.d(TAG,""+musicXmlRenderer.getMusicXMLString());

                fosXML.close();
            } catch (FileNotFoundException fileNotFoundException) {
                Log.e(TAG, "found fileNotFoundException when generate a file.");
            } catch (IOException ioException) {
                Log.e(TAG, "found ioException when serialize the musicXML.");
            }
        }catch (Exception ex){
            Toast.makeText(this, "Error: " + ex, Toast.LENGTH_SHORT).show();

        }

    }
*/
    //

    private static Boolean isExit = false;



    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {   //

            if(isExit == false)
            {

                isExit = true;
                Toast.makeText(RecordActivity.this, "Press the return key again to exit the program", Toast.LENGTH_SHORT).show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                            isExit = false;
                        }
                        catch (InterruptedException e)
                        {
                            Log.e(TAG,"InterruptedException Occurred when onKeyDown method");
                        }
                    }
                }).start();
            }
            else {
                android.os.Process.killProcess(android.os.Process.myPid());
                return true;
            }
        }
        return false;
    }

    private void confirmExit()
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(RecordActivity.this); //

        dialog.setTitle("Go away");
        dialog.setMessage("OK");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int i) {

                android.os.Process.killProcess(android.os.Process.myPid());
            }

        });

        dialog.setNegativeButton("No",new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int i) {

            }

        });

        dialog.show();//
    }   // end method confirmExit


    // confirm stop record
    private void confirmStopRecord()
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(RecordActivity.this);
        dialog.setTitle("Stop Recording");
        dialog.setMessage("Are you sure?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopRecord();
            }
        });
        dialog.show();//
    }

    // count the beat to make quantize
    private void countBeat(int beat)
    {
        switch (beat)
        {
            case 4:
                beatCtr += 1.0;
                break;
            case 8:
                beatCtr += 0.5;
                break;
            case 16:
                beatCtr += 0.25;
                break;
        }
    }

    // a set method that set the beat counter as zero
    private void setBeatCtrZero()
    {
        beatCtr = 0;
    }
    private void startTimer(){
        stopTimer();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTimer();
            }
        }, 0, 1000);
    }

    private void stopTimer(){
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }
    private static String getTwoDecimalsValue(int value) {
        if (value >= 0 && value <= 9) {
            return "0" + value;
        } else {
            return value + "";
        }
    }
    public static String formatSeconds(int seconds) {
        return  getTwoDecimalsValue(seconds / 60) + ":"
                + getTwoDecimalsValue(seconds % 60);
    }
    private void updateTimer() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recorderSecondsElapsed++;
                timerView.setText(formatSeconds(recorderSecondsElapsed));
            }

        });
    }


//inflate the filename
    private void file_name_prompt(){
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.filenameprompt, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        final EditText file = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text

                                namefile =(file.getText().toString());
                                if(namefile.equals("") || namefile.equals(null)){
                                    Toast.makeText(context, "Filename not valid!", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(context, namefile +" has been saved!", Toast.LENGTH_SHORT).show();
                                }
                            }


                        });


        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();


    }


    //ask the user if they should save the musicxml file or not

    private void output_file(MusicXmlRenderer musicXmlRenderer){
        String extr = Environment.getExternalStorageDirectory()+"/Documents/";
        File fileXML = new File(extr, namefile + ".xml");
        setOutputFileName(result);
        FileOutputStream fosXML;
        try {
            fosXML = new FileOutputStream(fileXML);
            Document mxlDoc = musicXmlRenderer.getMusicXMLDoc();
            //	write the MusicXML file formatted
            Serializer ser = new Serializer(fosXML, "UTF-8");
            ser.setIndent(4);
            ser.write(mxlDoc);
            fosXML.close();
        } catch (FileNotFoundException fileNotFoundException) {
            Log.e(TAG, "found fileNotFoundException when generate a file.");
        } catch (IOException ioException) {
            Log.e(TAG, "found ioException when serialize the musicXML.");
        }
    }

}