
package com.example.thesis.finalthesis;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.nfc.Tag;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.AppCompatActivity;

import nu.xom.Document;
import nu.xom.Serializer;
import uk.co.dolphin_com.sscore.Component;
import uk.co.dolphin_com.sscore.Header;
import uk.co.dolphin_com.sscore.LicenceKeyInstance;
import uk.co.dolphin_com.sscore.LoadOptions;
import uk.co.dolphin_com.sscore.RenderItem;
import uk.co.dolphin_com.sscore.SScore;
import uk.co.dolphin_com.sscore.Tempo;
import uk.co.dolphin_com.sscore.Version;
import uk.co.dolphin_com.sscore.ex.ScoreException;
import uk.co.dolphin_com.sscore.ex.XMLValidationException;
import com.example.thesis.finalthesis.ScoreView.ZoomNotification;
import uk.co.dolphin_com.sscore.playdata.Note;
import uk.co.dolphin_com.sscore.playdata.PlayData;
import uk.co.dolphin_com.sscore.playdata.UserTempo;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;
public class ScoreActivity extends AppCompatActivity {

    private static final boolean PlayUsingMediaPlayer = true;
    public static final String TAG = "ScoreActivity";
    private static final boolean UseNoteCursorIfPossible = true; // else bar cursor
    private static final boolean ColourPlayedNotes = false;
    private ImageButton plus;
    private ImageButton minus;

    private static final int kPlayLoopRepeats = 7;
    private String outputFileName = "";
    final Context context = this;

    private boolean isShowingSinglePart = false;
    private int singlePart = 0;
    private boolean playingLeft = true;
    private boolean playingRight = true;
    String  result;
    public String namefile;
    private ImageButton stop_score_layout;


    private ImageButton samplebutton;

    /**
     * set true to clear files in internal directory and reload from assets
     */
    private static final boolean reloadAssetsFiles = false;

    /**
     * the current file to preserve during a device rotation
     */
    private static final String CURRENT_FILE = "currentFile";

    /**
     * the index of the next file to load to preserve during device rotation
     */
    private static final String NEXT_FILE_INDEX = "nextFileIndex";

    /**
     * the magnification to preserve
     */
    private static final String MAGNIFICATION = "magnification";

    /**
     * the index of the next file to load from the internal directory
     */
    private int nextFileIndex = 0;

    /**
     * the current file which is displayed
     */
    private File currentFile;

    /**
     * the View which displays the score
     */
    private ScoreView scoreView;

    /**
     * the current viewed score.
     * <p>Preserved to avoid reload on rotate (which causes complete destruction and recreation of this Activity)
     */
    private SScore currentScore;
    /**
     * the current magnification.
     * <p>Preserved to avoid reload on rotate (which causes complete destruction and recreation of this Activity)
     */
    private float magnification;

    /**
     * set to prevent reentry during transpose
     */
    private boolean isTransposing;

    /**
     * the player plays the music using MediaPlayer and supports handlers for synchronised events on bar start, beat and note start
     */
    private Player player;

    /**
     * the current bar preserved on player stop so it can be restarted in the same place
     */
    private int currentBar;
    private int loopStart;
    private int loopEnd;



    private String fileName = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        Toolbar toolbar = (Toolbar) findViewById(R.id.scoretoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Bundle bundle = this.getIntent().getExtras();

        fileName = bundle.getString("fileName");
        hideBeat();

        stop_score_layout = findViewById(R.id.stop_score_layout);
        stop_score_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop_play();
            }
        });

        plus = (ImageButton) findViewById(R.id.plus);
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundTranspose(+1);
            }
        });

        minus = (ImageButton) findViewById(R.id.minus);
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundTranspose(-1);
            }
        });


        final CursorView cursorView = new CursorView(this, new CursorView.OffsetCalculator() {
            public float getScrollY() {
                final ScrollView sv = (ScrollView) findViewById(R.id.scrollView1);
                return sv.getScrollY();
            }
        });

        currentScore = null;
        currentBar = 0;
        loopStart = loopEnd = -1;
        magnification = 0.5F;
        isTransposing = false;

        if (reloadAssetsFiles)
            clearInternalDir();
        scoreView = new ScoreView(this, cursorView, getAssets(), new ZoomNotification(){
            public void zoom(float scale) {
                showZoom(scale);
                magnification = scale;
            }
        }, new ScoreView.TapNotification() {
            public void tap(int systemIndex, int partIndex, int barIndex, Component[] components) {

                boolean isPlaying = (player != null && player.state() == Player.State.Started);

                if (player != null) {
                    boolean isPaused = (player.state() == Player.State.Paused);
                    if (isPlaying || isPaused)
                        player.stop();
                }
                if (barIndex < loopStart)
                    barIndex = loopStart;
                else if (loopEnd > 0 && loopEnd > loopStart && barIndex > loopEnd)
                    barIndex = loopEnd;

                scoreView.setCursorAtBar(barIndex, (player != null) ? ScoreView.CursorType.line : ScoreView.CursorType.box, 200);

                if (isPlaying) {
                    player.startAt(barIndex, false/*no countIn*/);
                }
                currentBar = barIndex;
                updatePlayPauseButtonImage();
                for (Component comp : components)
                    System.out.println(comp);
            }

            public void longTap(int systemIndex, int partIndex, int barIndex, Component[] components) {

                isShowingSinglePart = !isShowingSinglePart;
                playingLeft = playingRight = true;
                clearLoop();
                invalidateOptionsMenu();
                if (isShowingSinglePart)
                    singlePart = partIndex;
                if (currentScore != null)
                    showScore(currentScore,null); // toggle single part or all parts on long tap
            }
        });


       final ScrollView sv = (ScrollView) findViewById(R.id.scrollView1);
        sv.addView(scoreView);

        //Add the overlaying cursor view
        final ScrollView cursorScrollView = (ScrollView)findViewById(R.id.scrollViewCursor);
        cursorScrollView.addView(cursorView);
        cursorScrollView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return sv.dispatchTouchEvent(event); // pass through the touch events to sv
            }
        });

        //Sets the cursorScrollView's height and width
        cursorScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                cursorView.measure(cursorScrollView.getWidth(), cursorScrollView.getHeight());
            }
        });
        sv.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                return scoreView.onTouchEvent(event);
            }

        });
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(2);


        if (savedInstanceState != null) // restore state on device rotation avoiding file reload
        {
            String filePath = savedInstanceState.getString(CURRENT_FILE);
            if (filePath != null && filePath.length() > 0)
                currentFile = new File(filePath);
            nextFileIndex = savedInstanceState.getInt(NEXT_FILE_INDEX);
            magnification = savedInstanceState.getFloat(MAGNIFICATION);


            Object o = getLastNonConfigurationInstance();
            if (o instanceof SScore) {
                currentScore = (SScore) o; // onResume updates the ui with this score
            }
        }


    }



    /**
     * called on app quit and device rotation.
     * <p>We save the state and the score so we can restore without reloading the file on device rotation
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (player != null) {
            player.reset();
        }
        if (currentFile != null)
            savedInstanceState.putString(CURRENT_FILE, currentFile.getAbsolutePath());
        savedInstanceState.putInt(NEXT_FILE_INDEX, nextFileIndex);
        savedInstanceState.putFloat(MAGNIFICATION, scoreView.getMagnification());
        savedInstanceState.putBoolean("playingLeft", playingLeft);
        savedInstanceState.putBoolean("playingRight", playingRight);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }






    private enum PlayPause { play, pause};


    private void setPlayButtonImage(PlayPause playPause) {
        ImageButton playButton = (ImageButton) findViewById(R.id.play_btn);
        if (playPause == PlayPause.pause)
            playButton.setImageDrawable(getResources().getDrawable(R.drawable.pause_pause));
        else
            playButton.setImageDrawable(getResources().getDrawable(R.drawable.play_play));
    }

    /** update the play-pause button image according to the player state */
    private void updatePlayPauseButtonImage() {
        if (player != null && player.state() == Player.State.Started)
            setPlayButtonImage(PlayPause.pause);
        else
            setPlayButtonImage(PlayPause.play);
    }

    /** Get all the .xml or .mxl filenames in the assets folder */
    private List<String> getXMLAssetsFilenames()
    {
        ArrayList<String> rval = new ArrayList<String>();
        // copy files from assets to internal directory where they can be opened as files (assets can only be opened as InputStreams)
        AssetManager am = getAssets();
        try {
            String[] files = am.list("");
            for (String filename : files)
                if (filename.endsWith(".mxl") || filename.endsWith(".xml"))
                    rval.add(filename);
        } catch (IOException e) {
        }
        return rval;
    }

    /** Get all the .xml/.mxl files in the internal dir */
    private List<File> getXMLFiles()
    {
        File internalDir = getFilesDir();
        String[] files = internalDir.list(new FilenameFilter() {

            @Override
            public boolean accept(File arg0, String filename) {
                return filename.endsWith(".xml") || filename.endsWith(".mxl");
            }
        });
        ArrayList<File> rval = new ArrayList<File>();
        for (String fname : files)
        {
            rval.add(new File(internalDir, fname));
        }
        return rval;
    }

    /** delete all xml/mxl files in internal directory so they are reloaded from assets */
    private void clearInternalDir()
    {
        File internalDir = getFilesDir();
        File[] files = internalDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File file, String filename) {
                return filename.endsWith(".xml") || filename.endsWith(".mxl");
            }

        });
        for (File file : files)
        {
            file.delete();
        }
    }

    /** copy all .xml/.mxl files from assets to the internal directory where they can be opened as files
     *  (assets can only be opened as InputStreams).  */
    private List<File> moveFilesToInternalStorage()
    {
        ArrayList<File> rval = new ArrayList<File>();
        AssetManager am = getAssets();
        try {
            String[] files = am.list("");
            File internalDir = getFilesDir();
            for (String filename : files)
            {
                if (filename.endsWith(".xml") || filename.endsWith(".mxl") )
                {
                    File outfile = new File(internalDir, filename);
                    InputStream is = am.open (filename);
                    OutputStream os = new FileOutputStream(outfile);
                    byte[] buffer = new byte[1024];
                    int read;
                    while((read = is.read(buffer)) != -1){
                        os.write(buffer, 0, read);
                    }
                    is.close();
                    os.close();
                    rval.add(outfile);
                }
            }
        } catch (IOException e) {
            Log.w("FileStorage", "Error copying asset files ", e);
        }
        return rval;
    }

    /**
     * get the list of .xml & .mxl files in the internal directory.
     *
     * @return the List of {@link File}.
     */
    private List<File> sourceXMLFiles()
    {
        List<File> files = getXMLFiles();
        List<String> assetsFiles = getXMLAssetsFilenames();
        if (files.size() >= assetsFiles.size())
        {
            return files;
        }
        else
            return moveFilesToInternalStorage();
    }



    /**
     * an implementation of the UserTempo interface used by the {@link PlayData}
     * to get a current user-defined tempo, or scaling for the score embedded tempo values
     * These read the position of the tempo slider and convert that to a suitable tempo value
     */
    private class UserTempoImpl implements UserTempo
    {
        /**
         * @return the user-defined tempo BPM (if not defined by the score)
         */
        public int getUserTempo() {
            return 80;
        }
        /**
         * @return the user-defined tempo scaling for score embedded tempo values (ie 1.0 => use standard tempo)
         */
        public float getUserTempoScaling() {
            return 1;
        }
    }

    /**
     * load a .mxl file and return a {@link SScore}
     * We use a ZipInputStream to decompress the .mxl data into a UTF-8 XML byte buffer
     *
     * @param file a file which can be opened with FileInputStream
     * @return a {@link SScore}
     */
    private SScore loadMXLFile(File file)
    {
        if (!file.getName().endsWith(".mxl"))
            return null;

        InputStream is;
        try {
            is = new FileInputStream(file);
            ZipInputStream zis = null;
            try
            {
                zis = new ZipInputStream(new BufferedInputStream(is));
                ZipEntry ze;
                while ((ze = zis.getNextEntry()) != null) {
                    if (!ze.getName().startsWith("META-INF") // ignore META-INF/ and container.xml
                            && ze.getName() != "container.xml")
                    {
                        // read from Zip into buffer and copy into ByteArrayOutputStream which is converted to byte array of whole file
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int count;
                        while ((count = zis.read(buffer)) != -1) { // load in 1K chunks
                            os.write(buffer, 0, count);
                        }
                        try
                        {
                            LoadOptions loadOptions = new LoadOptions(LicenceKeyInstance.SeeScoreLibKey, true);
                            return SScore.loadXMLData(os.toByteArray(), loadOptions);
                        }
                        catch (XMLValidationException e)
                        {
                            Log.w("sscore", "loadfile <" + file + "> xml validation error: " + e.getMessage());
                        }
                        catch (ScoreException e)
                        {
                            Log.w("sscore", "loadfile <" + file + "> error:" + e);
                        }
                    }
                }
            } catch (IOException e) {
                Log.w("Open", "file open error " + file, e);
                e.printStackTrace();
            }
            finally {
                if (zis != null)
                    zis.close();
            }
        } catch (FileNotFoundException e1) {
            Log.w("Open", "file not found error " + file, e1);
            e1.printStackTrace();
        } catch (IOException e) {
            Log.w("Open", "io exception " + file, e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Load the given xml file and return a SScore.
     *
     * @param file the file
     * @return the score
     */
    private SScore loadXMLFile(File file)
    {
        if (!file.getName().endsWith(".xml"))
            return null;
        try
        {
            LoadOptions loadOptions = new LoadOptions(LicenceKeyInstance.SeeScoreLibKey, true);
            return SScore.loadXMLFile(file, loadOptions);
        }
        catch (XMLValidationException e) {
            Log.w("sscore", "loadfile <" + file + "> xml validation error: " + e.getMessage());
        } catch (ScoreException e) {
            Log.w("sscore", "loadfile <" + file + "> error:" + e);
        }
        return null;
    }

    /**
     * Load the file of type .xml or .mxl
     *
     * @param file
     * @return the score
     */
    private SScore loadFile(File file)
    {
        if (player != null) {
            player.reset();
            player = null; // force a reload with the new score
            currentBar = 0;
        }
        isTransposing = false;
        if (file.getName().endsWith(".mxl"))
        {
            return loadMXLFile(file);
        }
        else if (file.getName().endsWith(".xml"))
        {
            return loadXMLFile(file);
        }
        else
            return null;
    }

    /**
     * Load the next .xml/.mxl file from the assets (copied via the internal dir)
     *
     * @return the score
     */
    private SScore loadNextFile()
    {
        List<File> files = sourceXMLFiles();
        int index = 0;
        for (File file : files)
        {
            if (index == nextFileIndex)
            {
                SScore sc = loadFile(file);
                nextFileIndex = (index + 1) % files.size();
                if (sc != null)
                {
                    currentFile = file;
                    currentScore = sc;
                    return sc;
                }
            }
            ++index;
        }
        return null;
    }


    /**
     * update the UI to show the score
     *
     * @param score the score
     */
    private void showScore(SScore score, Runnable completionHandler)
    {
        if (completionHandler != null) {
            scoreView.setLayoutCompletionHandler(completionHandler);
        } else {
            scoreView.setLayoutCompletionHandler(new Runnable() {
                public void run() {
                    // we could do something here when the score has finished loading
                }
            });
        }

        setPlayButtonImage(PlayPause.play); // show play in menu
        ArrayList parts = new ArrayList<Boolean>();
        if (isShowingSinglePart)
        {
            for (int i = 0; i < score.numParts(); ++i)
                parts.add(new Boolean(i == singlePart ? true : false));
        }
        scoreView.setScore(score, parts, magnification);

    }

    /** display the current zoom value in the TextView label */
    private void showZoom(float scale) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
    }


    /**
     * load the next file in the directory (transferred at startup from assets) in a background thread
     */
    void backgroundLoadNext()
    {
        magnification = scoreView.getMagnification(); // preserve the magnification
        new Thread(new Runnable() { // load file on background thread

            public void run() {

                scoreView.clear(new Runnable() {
                    @Override
                    public void run() {

                        final SScore score = loadNextFile();

                        new Handler(Looper.getMainLooper()).post(new Runnable() {

                            public void run() {
                                if (score != null) {
                                    showScore(score, null); // update score in SeeScoreView on foreground thread
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }
    /**
     * called on resuming the activity, including after device rotation
     */
    protected void onResume()
    {
        super.onResume();
        if (currentScore != null) // we can use the saved score if only rotating the display - we don't want the whole reload
        {
            showScore(currentScore, null);
        }
        else
        {
            new Thread(new Runnable(){ // load file on background thread

                public void run() {

                    final SScore score = (currentFile != null) ? loadFile(currentFile) : loadNextFile();

                    new Handler(Looper.getMainLooper()).post(new Runnable(){

                        public void run() {
                            if (score != null)
                            {
                                showScore(score, null); // update score in SeeScoreView on foreground thread
                            }
                        }
                    });
                }

            }).start();
        }
    }


    /** called by the system on opening the menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * transpose in a background thread
     *
     * @param transpose +1/-1 to transpose up/down one semitone from the current transpose setting
     */
    private void backgroundTranspose(final int transpose)
    {
        if (!isTransposing)
        {
            isTransposing = true;

            new Thread(new Runnable(){ // load file on background thread
                public void run()
                {
                    if (player != null) {
                        player.reset();
                        player = null;
                    }
                    currentBar = 0;
                    clearLoop();
                    if (currentScore != null)
                    {
                        try
                        {
                            currentScore.setTranspose(currentScore.getTranspose() + transpose);
                            new Handler(Looper.getMainLooper()).post(new Runnable(){
                                public void run()
                                {// relayout after transpose
                                    showScore(currentScore, new Runnable()
                                    {
                                        public void run() {
                                            isTransposing = false;

                                        }
                                    });
                                }
                            });
                        } catch(ScoreException e) {
                            System.out.println(" exception from setTranspose:" + e.toString());
                            isTransposing = false;

                        }
                    }
                    else
                    {
                        isTransposing = false;

                    }

                }
            }).start();
        }
    }
    private static RenderItem.Colour kOrange = new RenderItem.Colour(1, 0.5F, 0, 1);
    private static RenderItem.Colour kBlue =  new RenderItem.Colour(0, 0, 1, 1);
    /**
     * create and setup the Player with dispatch handlers
     * @return the new player
     */
    private Player setupPlayer()
    {
        try {
            final Player pl = new Player(currentScore, new UserTempoImpl(), this, PlayUsingMediaPlayer, new PlayData.PlayControls() {
                public boolean getPartEnabled(int partIndex) {
                    if (isShowingSinglePart)
                        return partIndex == singlePart; // play single part if showing single part
                    else
                        return true;
                }
                public boolean getPartStaffEnabled(int partIndex, int staffIndex) {
                    return staffIndex == 0 ? playingRight : playingLeft;
                }
                public int getPartMIDIInstrument(int partIndex) {
                    return 0; // 0 = use default. Return eg 41 for violin (see MIDI spec for standard program change values)
                }
                public boolean getMetronomeEnabled() {
                    final CheckBox metronomeCheck = (CheckBox)findViewById(R.id.metronomeSwitch);
                    return metronomeCheck.isChecked();
                }
                public int getMidiKeyForMetronome() {
                    // defines voice of metronome - see MIDI spec "Appendix 1.5 - General MIDI Percussion Key Map"
                    return 0; // use default voice
                }
                public float getPartVolume(int partIndex) {
                    return getMetronomeEnabled() ? 0.5F : 1.0F; // reduce volume of all parts if metronome is enabled
                }
                public float getMetronomeVolume() {
                    return 1.F;
                }
            }, loopStart, loopEnd, (loopStart >= 0 && loopEnd >= 0) ? kPlayLoopRepeats : 0);


            final int autoScrollAnimationTime = pl.bestScrollAnimationTime();
            pl.setBarStartHandler(new Dispatcher.EventHandler() {
                private int lastIndex  =-1;
                public void event(final int index, final boolean ci) {

                    // use bar cursor if bar time is short
                    final boolean useNoteCursor = UseNoteCursorIfPossible && !pl.needsFastCursor();
                    if (!useNoteCursor || ColourPlayedNotes) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {

                            public void run() {
                                if (!useNoteCursor) // use bar cursor
                                    scoreView.setCursorAtBar(index, ScoreView.CursorType.box, autoScrollAnimationTime);

                                if (ColourPlayedNotes) { // if this is a repeat section we clear the colouring from the previous repeat
                                    boolean startRepeat = index < lastIndex;
                                    if (startRepeat) {
                                        scoreView.clearColouringForBarRange(index, currentScore.numBars() - index);
                                    }
                                }
                                lastIndex = index;
                            }
                        });
                    }
                }
            }, -50); // anticipate so cursor arrives on time
            pl.setBeatHandler(new Dispatcher.EventHandler() {
                public void event(final int index, final boolean ci) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        final int beatNumber = index + 1;
                        final boolean countIn = ci;

                        public void run() {
                            if (countIn)
                                showBeat(beatNumber);
                            else
                                hideBeat();
                        }
                    });
                }
            }, 0);
            if (UseNoteCursorIfPossible || ColourPlayedNotes) {
                pl.setNoteHandler(new Dispatcher.NoteEventHandler() {
                    public void startNotes(final List<Note> notes) {

                        // disable note cursor if bar time is short
                        final boolean useNoteCursor = !pl.needsFastCursor();
                        if (useNoteCursor) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                final List<Note> localNotes = notes;

                                public void run() {

                                    scoreView.moveNoteCursor(localNotes, autoScrollAnimationTime);
                                }
                            });
                        }
                        if (ColourPlayedNotes)
                        {

                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                public void run() {
                                    for (Note note : notes)
                                    {
                                        scoreView.colourItem(note.partIndex, note.startBarIndex, note.item_h, (note.staffindex > 0) ? kOrange : kBlue, true); // different colours in different staves
                                    }
                                }
                            });
                        }
                    }
                }, -50);
            }
            pl.setEndHandler(new Dispatcher.EventHandler() {
                @Override
                public void event(int index, boolean countIn) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            setPlayButtonImage(PlayPause.play);
                            currentBar = Math.max(0, loopStart); // next play will be from start
                            if (ColourPlayedNotes)
                            {
                                scoreView.clearAllColouring();
                            }
                        }
                    });
                }
            }, 0);

            return pl;

        } catch (Player.PlayerException ex) {
            System.out.println("Player error: " + ex.getMessage());
        }
        return null;
    }


    /**
     * called on tapping play-pause button
     * @param button the button
     */
    public void play_pause(View button) {
        if (currentScore == null)
            return;

        if (player != null) {
            switch (player.state()) {
                case NotStarted:
                    // scroll to current bar ready for start
                    scoreView.setCursorAtBar(currentBar, ScoreView.CursorType.line, 0);
                    player.startAt(currentBar, true/*countIn*/);
                    break;

                case Started:
                    player.pause();
                    currentBar = player.currentBar();
                    break;

                case Paused:
                    currentBar = player.currentBar();
                    player.resume();
                    break;

                case Stopped:
                case Completed:
                    player.reset();
                    currentBar = Math.max(0, loopStart);
                    scoreView.setCursorAtBar(currentBar, ScoreView.CursorType.line, 0 );
                    player.startAt(currentBar, true/*countIn*/);
                    break;
            }

        } else { // player == null

            player = setupPlayer();
            scoreView.setCursorAtBar(currentBar, ScoreView.CursorType.line, 0);
            player.startAt(currentBar, true/*countIn*/);
        }
        updatePlayPauseButtonImage();
    }

    public void stop_play()
    {
        hideBeat();
        if (player != null) {
            switch (player.state()) {
                case Started:
                case Paused:
                case Stopped:
                case Completed:
                    player.reset();
                    currentBar = Math.max(0, loopStart);
                    break;
            }
        }
        player = null;
        updatePlayPauseButtonImage();
    }

    /**
     * called from the system to handle menu selection
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.save_menu:
                savefilenameprompt();
                return true;
            case R.id.print_picture:
                print_savefilenameprompt();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // method to confirm go back to record activity
    private void confirmGoBack(){

        AlertDialog.Builder dialog = new AlertDialog.Builder(ScoreActivity.this);
        dialog.setMessage("Finished?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent intent = new Intent();
            intent.setClass(ScoreActivity.this, RecordActivity.class);
            clearInternalDir();
            startActivity(intent);
            ScoreActivity.this.finish();
            stop_play();
        }

    });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {

            }

        });
        dialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            confirmGoBack();
            return true;
        }
        return false;
    }
    @Override
    public boolean onSupportNavigateUp() {
        confirmGoBack();
        return true;
    }


    //set output filename

    private void setOutputFileName(String s)
    {
        outputFileName = s;
    }

    private void showBeat(int beat) {
        TextView beatText = (TextView) findViewById(R.id.beatText);
        beatText.setText(""+beat);
        beatText.setVisibility(TextView.VISIBLE);
    }

    private void hideBeat() {
        TextView beatText = (TextView) findViewById(R.id.beatText);
        beatText.setVisibility(TextView.INVISIBLE);
    }
    void clearLoop() {
        loopStart = loopEnd = -1;
        new Handler(Looper.getMainLooper()).post(new Runnable() {

            public void run() {
                scoreView.hideLoopGraphics();
            }
        });
        player = null; // we need to recreate the player with new playdata
    }
    //the error is the cursor
    //which is the cursor is not working well

    public Bitmap getBitmapFromView(View view, int totalHeight, int totalWidth){
        Bitmap returnedBitmap = Bitmap.createBitmap(totalWidth,totalHeight,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if(bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }
    /**
     * convert scrollview content to bitmap image
     *
     */
    private void screen_shot(){
        View u = ((Activity) context).findViewById(R.id.scrollView1);
        ScrollView z = (ScrollView) ((Activity) context).findViewById(R.id.scrollView1);
        int totalHeight = z.getChildAt(0).getHeight();
        int totalwidth = z.getChildAt(0).getWidth();
        Bitmap b = getBitmapFromView(u,totalHeight,totalwidth);
        /**
         * Save Bitmap Image
         *
         */
        String extr = Environment.getExternalStorageDirectory()+"/Pictures/";
        String filename = namefile + ".jpg";
        File mypath = new File(extr, filename);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            MediaStore.Images.Media.insertImage(context.getContentResolver(), b, "Screen", "screen");
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }


    private void savefilenameprompt(){
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.savefilename, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        final EditText file = (EditText) promptsView
                .findViewById(R.id.EditText_Filename);

        // set dialog message

        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                namefile =(file.getText().toString());
                                if(namefile.equals("") || namefile.equals(null)){
                                    Toast.makeText(context, "Filename not valid!", Toast.LENGTH_SHORT).show();
                                }else{
                                    screen_shot();
                                    Toast.makeText(context, namefile +" has been saved!", Toast.LENGTH_SHORT).show();
              }
                            }
                        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    private void doprint(){
        View u = ((Activity) context).findViewById(R.id.scrollView1);
        ScrollView z = (ScrollView) ((Activity) context).findViewById(R.id.scrollView1);
        int totalHeight = z.getChildAt(0).getHeight();
        int totalwidth = z.getChildAt(0).getWidth();
        Bitmap b = getBitmapFromView(u,totalHeight,totalwidth);

        /**
         * PRINT BITMAP IMAGE
         *
         */

        PrintHelper printHelper = new PrintHelper(this);
        printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        printHelper.printBitmap(namefile, b);
    }

    private void print_savefilenameprompt(){
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.savefilename, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        final EditText file = (EditText) promptsView
                .findViewById(R.id.EditText_Filename);

        // set dialog message

        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                namefile =(file.getText().toString());
                                if(namefile.equals("") || namefile.equals(null)){
                                    Toast.makeText(context, "Filename not valid!", Toast.LENGTH_SHORT).show();
                                }else{
                                    doprint();
                                }
                            }
                        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    static {
        System.loadLibrary("stlport_shared");
        System.loadLibrary("SeeScoreLib");
    }
}

