package com.example.final_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_goReg;
    private Button btn_goLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 화면 최대화
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        btn_goLogin= findViewById(R.id.btn_goLogin);
        btn_goLogin.setOnClickListener(this);

        btn_goReg=findViewById(R.id.btn_goReg);
        btn_goReg.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        if(v==btn_goReg){
            Intent regIntet =new Intent(MainActivity.this, UserReg.class);
            startActivity(regIntet);
        }
        else if(v==btn_goLogin){
            Intent loginIntet =new Intent(MainActivity.this, UserLogin.class);
            startActivity(loginIntet);
        }

    }
}