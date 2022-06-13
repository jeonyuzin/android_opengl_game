package com.example.final_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserLogin extends AppCompatActivity {

    private Button btn_login_game;
    private Button btn_back2;
    private EditText txt_Log_Id;
    private EditText txt_Log_Password;

    private ArrayList<String> arrayIndex = new ArrayList<>();
    private ArrayList<String> arrayData = new ArrayList<>();

    FirebaseDatabase myFirebase;
    DatabaseReference myDB_Reference=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        view_find_setClick_log();
        myFirebase=FirebaseDatabase.getInstance();
        myDB_Reference=myFirebase.getReference();
        getFirebaseDatabase();
    }
    private void view_find_setClick_log(){
        btn_login_game=findViewById(R.id.btn_login_game);
        btn_login_game.setOnClickListener(this::onClick);
        btn_back2 =findViewById(R.id.btn_Back2);
        btn_back2.setOnClickListener(this::onClick);

        txt_Log_Id=findViewById(R.id.txt_Log_Id);
        txt_Log_Password=findViewById(R.id.txt_Log_Password);
    }

    private void onClick(View view) {
        if(view==btn_back2){
            finish();
        }
        else if(view==btn_login_game){


            //맞냐 틀리냐만 검사함 보안상
            getFirebaseDatabase();
            if(!(arrayIndex.contains(txt_Log_Id.getText().toString()))){
                Toast.makeText(UserLogin.this,"아이디가 존재하지 않습니다.",Toast.LENGTH_LONG).show();
                return;
            }
            String user_id=txt_Log_Id.getText().toString();
            String user_pw=txt_Log_Password.getText().toString();

            int temp=arrayIndex.indexOf(user_id);//아이디에 맞는 비밃번호
            String[] string_temp=arrayData.get(temp).toString().split("/");
            String temp_pw=string_temp[2];
            Log.d("msg",temp_pw);
            if(user_pw.equals(temp_pw)){
                Intent gameintent=new Intent(UserLogin.this,Game.class);
                startActivity(gameintent);
                ConstMgr a=new ConstMgr();
                a.setUser(string_temp[0],string_temp[1]);
                finish();
            }
            else{
                Toast.makeText(UserLogin.this,"아이디 혹은 비밀번호가 틀립니다.",Toast.LENGTH_LONG).show();
            }


        }

    }


    ///
    public void getFirebaseDatabase(){
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("getFirebaseDatabase", "key: " + dataSnapshot.getChildrenCount());
                arrayIndex.clear();
                arrayData.clear();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    String key = postSnapshot.getKey();
                    FirebasePost get = postSnapshot.getValue(FirebasePost.class);
                    String[] info = {get.id, get.nickname ,get.pw};
                    String Result = info[0]+"/"+info[1]+ "/"+info[2];
                    arrayData.add(Result);
                    arrayIndex.add(key);
                    Log.d("getFirebaseDatabase", "key: " + key);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("getFirebaseDatabase","loadPost:onCancelled", databaseError.toException());
            }
        };
        Query sortbyAge = FirebaseDatabase.getInstance().getReference().child("user_list").orderByChild("id");
        sortbyAge.addListenerForSingleValueEvent(postListener);
    }
}