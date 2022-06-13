package com.example.final_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.SoundPool;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

public class Game extends AppCompatActivity {

    // GLSurfaceView
    private GLSurfaceView mGLSurfaceView;
    // 액티비티 생성

    //효과음 설정
    private SoundPool mSoundPool;
    private int mSoundButton;
    private int mSoundFight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 화면 최대화
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        //효과음
//        mSoundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
        //Using the deprecated constructor to support versions before API 21
        //https://stackoverflow.com/questions/39184157/android-why-is-the-constructor-for-soundpool-deprecated
        mSoundPool=new SoundPool.Builder().setMaxStreams(10).build();
        try{
            AssetManager assetManager = getAssets();
            AssetFileDescriptor descriptorBtn = assetManager.openFd("buttoneffect.ogg");
            AssetFileDescriptor descriptorFight = assetManager.openFd("fight.ogg");
            mSoundButton = mSoundPool.load(descriptorBtn, 1);
            mSoundFight = mSoundPool.load(descriptorFight, 1);
        }catch(Exception ex){}


        // 서피스뷰 생성을 위한 매트릭스
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        mGLSurfaceView = new MainGLSurfaceView(this, width, height);
        setContentView(mGLSurfaceView);
    }

    //효과음
    public void soundButton(){
        try{
            mSoundPool.play(mSoundButton, 1.0f, 1.0f, 0, 0, 1.0f);
        }catch(Exception ex){}
    }
    public void soundFight(){
        try{
            mSoundPool.play(mSoundFight, 1.0f, 1.0f, 0, 0, 1.0f);
        }catch(Exception ex){}
    }
}