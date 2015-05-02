package com.beacon.peng.leanasong;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.andrew.apolloMod.ui.widgets.VisualizerView;


public class MainActivity extends ActionBarActivity {
    private MediaPlayer mPlayer;
    private VisualizerView mVisualizerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mPlayer = MediaPlayer.create(this, R.raw.hua_nuo_suo);
        mPlayer.setLooping(false);
        mPlayer.start();

        // We need to link the visualizer view to the media player so that
        // it displays something
        mVisualizerView = new VisualizerView(this);
        //mVisualizerView.setBackgroundColor(Color.BLUE);
        mVisualizerView.link(mPlayer);
        setContentView(mVisualizerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
