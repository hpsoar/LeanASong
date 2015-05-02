package com.beacon.peng.leanasong;

import android.database.DataSetObserver;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;

import com.andrew.apolloMod.ui.widgets.VisualizerView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends ActionBarActivity {
    private MediaPlayer mPlayer;
    private VisualizerView mVisualizerView;

    private Button btnA;
    private Button btnB;

    private Button btnPlay;
    private Button btnSeekBack;
    private Button btnSeekForward;

    private SeekBar seekBar;

    private ListView clipListView;

    private ClipItem currentClip;

    private ArrayList<ClipItem> clipList = new ArrayList<>();

    private ArrayAdapter<ClipItem> clipListViewAdapter;

    private AudioSeeker audioSeeker;

    private Timer playerTimer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        // ui
        btnA = (Button)findViewById(R.id.btn_A);
        btnB = (Button)findViewById(R.id.btn_B);

        btnSeekBack = (Button)findViewById(R.id.btn_seek_back);
        btnSeekForward = (Button)findViewById(R.id.btn_seek_forward);
        btnPlay = (Button)findViewById(R.id.btn_play);

        seekBar = (SeekBar)findViewById(R.id.play_back_progress);

        clipListView = (ListView)findViewById(R.id.clip_list_view);

        mVisualizerView = (VisualizerView)findViewById(R.id.visualizer_view);

        // player
        mPlayer = MediaPlayer.create(this, R.raw.hua_nuo_suo);
        mPlayer.setLooping(false);

        // player seeker
        audioSeeker = new AudioSeeker(mPlayer, new AudioSeeker.AudioSeekerListener() {
            @Override
            public void playerDidSeek() {
                updateSeekBar();
            }
        });

        // progress
        seekBar.setMax(mPlayer.getDuration());

        // visualizer
        mVisualizerView.link(mPlayer);

        clipListViewAdapter = new ArrayAdapter<ClipItem>(this, android.R.layout.simple_expandable_list_item_1, clipList);

        clipListView.setAdapter(clipListViewAdapter);

        setupMediaPlayerListeners();

        setupClipListeners();

        setupPlayBackListeners();

        startPlay();
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
        else if (id == R.id.action_play) {
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupClipListeners() {
        // btn events
        btnA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentClip = new ClipItem();
                currentClip.start = mPlayer.getCurrentPosition();
                btnB.setEnabled(true);
            }
        });

        btnB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentClip.end = mPlayer.getCurrentPosition();
                clipList.add(currentClip);
                currentClip = null;

                btnB.setEnabled(false);

                clipListViewAdapter.notifyDataSetChanged();
            }
        });

        // clip selection
        clipListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ClipItem item = (ClipItem)clipList.get(position);

            }
        });
    }

    private void setupPlayBackListeners() {
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                    mPlayer.seekTo(0);
                }
                startPlay();
            }
        });

        // TODO: make this two btn exclusive
        btnSeekBack.setOnTouchListener(new SeekBtnOnTouchListner(audioSeeker, -1000));

        btnSeekForward.setOnTouchListener(new SeekBtnOnTouchListner(audioSeeker, 1000));

        // TODO: make the seekers exclusive
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPlayer.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setupMediaPlayerListeners() {
        // play completion
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mVisualizerView.setEnabled(false);
                btnA.setEnabled(false);
                btnB.setEnabled(false);
                playerTimer.cancel();
                playerTimer = null;
            }
        });
    }

    private void startPlay() {
        mVisualizerView.setEnabled(true);
        mPlayer.start();

        btnA.setEnabled(true);

        if (playerTimer == null) {
            playerTimer = new Timer();
            playerTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateSeekBar();
                }
            }, 0, 1000);
        }
    }

    private void updateSeekBar() {
        seekBar.setProgress(mPlayer.getCurrentPosition());
    }

    private static class ClipItem {
        int start;
        int end;

        @Override
        public String toString() {
            return "" + start + "-" + end;
        }
    }

    private static class AudioSeeker {
        private Timer timer;
        private MediaPlayer player;
        private AudioSeekerListener audioSeekerListener;

        public AudioSeeker(MediaPlayer player, AudioSeekerListener audioSeekerListener) {
            this.player = player;
            this.timer = new Timer();
            this.audioSeekerListener = audioSeekerListener;
        }

        public void finishSeek() {
            this.timer.cancel();
        }

        public void beginSeek(final int step) {
            if (step == 0) return;

            player.pause();
            this.timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    int pos =  player.getCurrentPosition();
                    if (step > 0) {
                        int nextPos = Math.min(pos + step, player.getDuration());
                        player.seekTo(nextPos);
                        if (nextPos == player.getDuration()) {
                            timer.cancel();
                        }
                        audioSeekerListener.playerDidSeek();
                    }
                    else {
                        int nextPos = Math.max(pos + step, 0);
                        player.seekTo(nextPos);
                        if (nextPos == 0) {
                            timer.cancel();
                        }
                        audioSeekerListener.playerDidSeek();
                    }
                }
            }, 0, 1000);
        }

        public interface AudioSeekerListener {
            public void playerDidSeek();
        }
    }

    private static class SeekBtnOnTouchListner implements View.OnTouchListener {
        private AudioSeeker audioSeeker;
        private int step;

        public SeekBtnOnTouchListner(AudioSeeker audioSeeker, int step) {
            this.audioSeeker = audioSeeker;
            this.step = step;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_OUTSIDE:
                case MotionEvent.ACTION_UP:
                    audioSeeker.finishSeek();
                    break;
                case MotionEvent.ACTION_DOWN:
                    audioSeeker.beginSeek(step);
                    break;
            }
            return false;
        }
    }
}
