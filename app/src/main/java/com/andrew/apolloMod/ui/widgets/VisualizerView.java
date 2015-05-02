/**
 * Copyright 2011, Felix Palmer
 *
 * Licensed under the MIT license:
 * http://creativecommons.org/licenses/MIT/
 */
package com.andrew.apolloMod.ui.widgets;

import com.andrew.apolloMod.helpers.visualizer.AudioData;
import com.andrew.apolloMod.helpers.visualizer.BarGraphRenderer;
import com.andrew.apolloMod.helpers.visualizer.SolidBarGraphRenderer;
import com.andrew.apolloMod.helpers.visualizer.FFTData;
import com.andrew.apolloMod.helpers.visualizer.WaveformRenderer;
import com.andrew.apolloMod.helpers.visualizer.Renderer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class VisualizerView extends View {
    private final static String TAG = "VisualizerView";

  public final static String VISUALIZATION_TYPE = "visualization_type";
  public final static String VISUALIZATION_TYPE_NONE = "visual_none";
  public final static String VISUALIZATION_TYPE_SOLID_BAR_GRAPH = "visual_solid_bar_graph";
  public final static String VISUALIZATION_TYPE_WAVEFORM = "visual_waveform";
  public final static String VISUALIZATION_TYPE_BAR_GRAPH = "visual_bar_graph";
  private byte[] mBytes;
  private byte[] mFFTBytes;
  private Rect mRect = new Rect();

    private Visualizer mVisualizer;

    private ByteArrayOutputStream mBAOS = new ByteArrayOutputStream();

  private Renderer mRenderer;
  String type = null;

  public VisualizerView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs);

    mBytes = null;
    mFFTBytes = null;

    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    type = sp.getString(VISUALIZATION_TYPE, VISUALIZATION_TYPE_BAR_GRAPH);
    
    if( type.equals(VISUALIZATION_TYPE_SOLID_BAR_GRAPH)) {
    	mRenderer = new SolidBarGraphRenderer(context);
    }
    else if ( type.equals(VISUALIZATION_TYPE_WAVEFORM)) {
        mRenderer = new WaveformRenderer(context);
    }
    else if ( type.equals(VISUALIZATION_TYPE_BAR_GRAPH)) {
    	mRenderer = new BarGraphRenderer(context);
    }
  }

  public VisualizerView(Context context, AttributeSet attrs)
  {
    this(context, attrs, 0);
  }

  public VisualizerView(Context context)
  {
    this(context, null, 0);
  }

  /**
   * Pass data to the visualizer. Typically this will be obtained from the
   * Android Visualizer.OnDataCaptureListener call back. See
   * {@link Visualizer.OnDataCaptureListener#onWaveFormDataCapture }
   * @param bytes
   */
  public void updateVisualizer(byte[] bytes) {
    mBytes = bytes;
    invalidate();
  }

  /**
   * Pass FFT data to the visualizer. Typically this will be obtained from the
   * Android Visualizer.OnDataCaptureListener call back. See
   * {@link Visualizer.OnDataCaptureListener#onFftDataCapture }
   * @param bytes
   */
  public void updateVisualizerFFT(byte[] bytes) {
    mFFTBytes = bytes;
    invalidate();
  }

  Bitmap mCanvasBitmap;
  Canvas mCanvas;


  @SuppressLint("DrawAllocation")
@Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if(type.equals("None"))
    	return;
    // Create canvas once we're ready to draw
    mRect.set(0, 0, getWidth(), getHeight());

    if(mCanvasBitmap == null)
    {
      mCanvasBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Config.ARGB_8888);
    }
    if(mCanvas == null)
    {
      mCanvas = new Canvas(mCanvasBitmap);
    }
    
    //Clear canvas
    mCanvas.drawColor(0, Mode.CLEAR);


    if (mBytes != null) {
      // Render all audio renderers
      AudioData audioData = new AudioData(mBytes);
      mRenderer.render(mCanvas, audioData, mRect);
    }

    if (mFFTBytes != null) {
      // Render all FFT renderers
      FFTData fftData = new FFTData(mFFTBytes);
      mRenderer.render(mCanvas, fftData, mRect);
    }

    canvas.drawBitmap(mCanvasBitmap, new Matrix(), null);
  }


    /**
     * Links the visualizer to a player
     * @param player - MediaPlayer instance to link to
     */
    public void link(final MediaPlayer player)
    {
        if(player == null)
        {
            throw new NullPointerException("Cannot link to null MediaPlayer");
        }

        try {
            // Create the Visualizer object and attach it to our media player.
            mVisualizer = new Visualizer(player.getAudioSessionId());
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

            // Pass through Visualizer data to VisualizerView
            Visualizer.OnDataCaptureListener captureListener = new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
                                                  int samplingRate) {
                    updateVisualizer(bytes);
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] bytes,
                                             int samplingRate)  {
                    updateVisualizerFFT(bytes);
                    try {
                        mBAOS.write(bytes);
                        Log.d(TAG, "" + mBAOS.toByteArray().length + ", " + player.getCurrentPosition());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            mVisualizer.setDataCaptureListener(captureListener,
                    Visualizer.getMaxCaptureRate() / 2, true, true);
        }
        catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }
    }

    public void setEnabled(boolean enabled) {
        mBAOS.reset();
        mVisualizer.setEnabled(enabled);
    }
}