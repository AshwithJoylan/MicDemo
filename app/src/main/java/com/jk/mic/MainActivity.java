package com.jk.mic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView textViewStatus;
    private AudioRecord record;
    private AudioTrack track;

    private int intBufferSize;
    private short[] shortAudioData;

    private Boolean isActive = false;

    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);
        textViewStatus = findViewById(R.id.textViewStatus);




    }

    private void threadLoop() {
        int intRecordSampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);

        intBufferSize = AudioRecord.getMinBufferSize(intRecordSampleRate, AudioFormat.CHANNEL_IN_MONO
                , AudioFormat.ENCODING_PCM_16BIT);

        shortAudioData = new short[intBufferSize];

        record = new AudioRecord(MediaRecorder.AudioSource.MIC
                , intRecordSampleRate
                , AudioFormat.CHANNEL_IN_STEREO
                , AudioFormat.ENCODING_PCM_16BIT
                , intBufferSize);

        track = new AudioTrack(AudioManager.STREAM_MUSIC
                , intRecordSampleRate
                , AudioFormat.CHANNEL_IN_STEREO
                , AudioFormat.ENCODING_PCM_16BIT
                , intBufferSize
                , AudioTrack.MODE_STREAM);

        track.setPlaybackRate(intRecordSampleRate);

        record.startRecording();
        track.play();

        while (isActive){
            record.read(shortAudioData, 0, shortAudioData.length);

            for (int i = 0; i < shortAudioData.length; i++){
                shortAudioData[i] = (short) Math.min (shortAudioData[i] * 2, Short.MAX_VALUE);
            }
            track.write(shortAudioData, 0, shortAudioData.length);
        }

    }

    @SuppressLint("SetTextI18n")
    public void onPressStart(View view) {
        isActive = true;
        textViewStatus.setText("Active");
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                threadLoop();
            }
        });
        thread.start();
    }

    @SuppressLint("SetTextI18n")
    public void onPressStop(View view) {
        if (null!=record) {
        isActive = false;
            track.stop();
            record.stop();
            record.release();
            record = null;
            thread = null;track = null;
        textViewStatus.setText("Stopped");}
    }
}