package com.example.thesis.finalthesis;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class WelcomeActivity extends Activity {

    final static String TAG = "WelcomeActivity";

    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_page);



                //addShortcut();
//        Jetomo: In case we need to have a shortcut in the homepage


        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(3000);
                    startActivity(new Intent().setClass(WelcomeActivity.this, RecordActivity.class));
                    WelcomeActivity.this.finish();
                }
                catch (Exception e)
                {
                    Log.e(TAG,"Exception when change intent at welcome page.");
                }
            }
        }).start();
    }

    // automatically build a shortcut on user's homepage
    private void addShortcut()
    {
        Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");


        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        shortcut.putExtra("duplicate", false);  // build

        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.setClassName(this, this.getClass().getName());
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);


        Intent.ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(this, R.drawable.app_logo);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);

        sendBroadcast(shortcut);
    }

}
