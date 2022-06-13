package com.example.final_test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class UserReg extends AppCompatActivity {

    private Button btn_Reg_Id_Check;
    private Button btn_Reg_Complete;
    private Button btn_Back;
    private EditText txt_Reg_Id;
    private EditText txt_Reg_Password;
    private EditText txt_Reg_Password_Check;
    private EditText txt_Reg_NickName;
    private CheckBox jcb_Reg_Check;


    private ArrayList<String> arrayIndex = new ArrayList<>();

    FirebaseDatabase myFirebase;
    DatabaseReference myDB_Reference=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_reg);
        getFirebaseDatabase();
        myFirebase=FirebaseDatabase.getInstance();
        myDB_Reference=myFirebase.getReference();
        view_find_setClick();



    }

    private void view_find_setClick(){
        btn_Reg_Complete=findViewById(R.id.btn_Reg_Complete);
        btn_Reg_Complete.setOnClickListener(this::onClick);
        btn_Reg_Id_Check=findViewById(R.id.btn_Reg_Id_Check);
        btn_Reg_Id_Check.setOnClickListener(this::onClick);
        btn_Back=findViewById(R.id.btn_Back);
        btn_Back.setOnClickListener(this::onClick);

        txt_Reg_Id=findViewById(R.id.txt_Reg_Id);
        txt_Reg_Password=findViewById(R.id.txt_Reg_Password);
        txt_Reg_Password_Check=findViewById(R.id.txt_Reg_Password_Check);
        txt_Reg_NickName=findViewById(R.id.txt_Reg_NickName);
        jcb_Reg_Check=findViewById(R.id.jcb_Reg_Check);
        txt_Reg_Id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                jcb_Reg_Check.setChecked(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    public void onClick(View v){
        if(v==btn_Reg_Complete){//회원가입 완료
            postFirebaseDatabase(true);
        }
        else if(v==btn_Reg_Id_Check){
            String temp=txt_Reg_Id.getText().toString();

            if(isNullOrSpace(temp)){//(공백 및 빈문자열)x 처리
                Toast.makeText(UserReg.this,"띄어쓰기 및 공백은 사용 할 수 없습니다.",Toast.LENGTH_LONG).show();
                return;
            }

            if(isLength(temp,5,15)){ //자릿 수 처리 (문자열, 최소,최대)
                Toast.makeText(UserReg.this,"아이디는 5자이상 15자이하로 입력해주세요",Toast.LENGTH_LONG).show();
                return;
            }

            if(isUpper(temp)){//대문자X처리
                Toast.makeText(UserReg.this,"아이디에 대문자는 사용할 수 없습니다.",Toast.LENGTH_LONG).show();
                return;
            }

            if(isKorean(temp)){
                Toast.makeText(UserReg.this,"아이디에 한글은 사용할 수 없습니다",Toast.LENGTH_LONG).show();
                return;
            }

            ///-_특수문자만 가능하게함
            String temp_replace=temp.replace("-", "").replace("_", "").trim();//- _는 검증때 없다고 가정
            System.out.println(temp_replace);
            System.out.println(temp_replace.matches("^[a-z0-9]*$"));
            //나머지 경우는 특수문자 _ - 제외 문제 입력일 때//
            if(!(temp_replace.matches("^[a-z0-9]*$"))) {
                Toast.makeText(UserReg.this,"특수문자는 - _ 만 입력가능합니다.",Toast.LENGTH_LONG).show();
                return;
            }
            //db로 중복확인

            getFirebaseDatabase();
            if(arrayIndex.contains(txt_Reg_Id.getText().toString())){
                Toast.makeText(UserReg.this,"중복된 아이디 입니다.",Toast.LENGTH_LONG).show();
                jcb_Reg_Check.setChecked(false);
                return;
            }
            jcb_Reg_Check.setChecked(true);
            Toast.makeText(UserReg.this, "사용 가능한 아이디 입니다.",Toast.LENGTH_LONG).show();
            return;

        }
        else if(v==btn_Back){
            finish();

        }
    }
    public boolean isNullOrSpace(String str) {
        String str_replace=str.replace(" ", "");
        if(str.length()!=str_replace.length()){//공백을 제거 했을때와 아닐때 문자열길이가 달라지면 공백이있음
            return true;
        }
        else if(str.trim().equals(null)){//null값이면 정상데이터아님
            return true;
        }
        else if(str.trim().equals("")){//빈문자열
            return true;
        }
        return false;//정상값
    }
    public boolean isLength(String str,int a, int b) {
        if(str.length()<a || str.length()>b){//문자열길이비교
            return true;
        }
        return false;
    }
    public boolean isUpper(String str){
        for(int i=0;i<str.length();i++){//대문자비교
            char ch = str.charAt(i);//인덱스에 맞는 문자뽑아옴 0부터시작
            if(ch>='A' && ch<='Z'){//아스키코드 값으로 비교
                return true;
            }
        }
        return false;
    }
    public boolean isKorean(String str){
        if(str.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")) { //https://ooz.co.kr/254 표현식 공부 참고함
            // 한글이 포함된 문자열
            return true;
        }
        return false;
    }
    public void getFirebaseDatabase(){
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("getFirebaseDatabase", "key: " + dataSnapshot.getChildrenCount());
                arrayIndex.clear();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    String key = postSnapshot.getKey();
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

    public void postFirebaseDatabase(boolean add){
        String char_pw=txt_Reg_Password.getText().toString();
        String char_pw_c=txt_Reg_Password_Check.getText().toString();


        if(!(jcb_Reg_Check.isChecked())){//ID확인 체크여부
            Toast.makeText(UserReg.this,"ID확인 버튼을 눌러서 확인해주세요",Toast.LENGTH_LONG).show();
            return;
        }
        else if(isNullOrSpace(char_pw)){//비밀번호 공백 및 띄어쓰기 x
            Toast.makeText(UserReg.this,"비밀번호에 띄어쓰기 및 공백은 사용할 수 없습니다.",Toast.LENGTH_LONG).show();
            return;
        }

        else if(!(char_pw.equals(char_pw_c))){//비밀==비밀번호확인
            Toast.makeText(UserReg.this,"비밀번호와 비밀번호 확인이 다릅니다.",Toast.LENGTH_LONG).show();
            return;
        }

        else if(isLength(char_pw,5,15)){//비밀번호는 5~15자여야함
            Toast.makeText(UserReg.this,"비밀번호는 5자이상 15자이하로 입력해주세요",Toast.LENGTH_LONG).show();
        }
        else if(isNullOrSpace(txt_Reg_NickName.getText().toString())){ //닉네임은 공백 및 띄어쓰기 사용불가
            Toast.makeText(UserReg.this,"닉네임에 띄어쓰기 및 공백은 사용할 수 없습니다.",Toast.LENGTH_LONG).show();
            return;
        }
        else if(isLength(txt_Reg_NickName.getText().toString(),5,15)){//닉네임은 5~15자여야함
            Toast.makeText(UserReg.this,"닉네임은 5자이상 15자이하로 입력해주세요",Toast.LENGTH_LONG).show();
            return;
        }
        //DB열어서 INSERT
        myFirebase=FirebaseDatabase.getInstance();
        myDB_Reference = myFirebase.getReference();
        HashMap<String, Object> childUpdates = new HashMap<>();
        HashMap<String, Object> postValues = null;
        if(add){
            FirebasePost post = new FirebasePost(txt_Reg_Id.getText().toString(), txt_Reg_Password.getText().toString(), txt_Reg_NickName.getText().toString());
            postValues = post.toMap();
        }
        childUpdates.put("/user_list/" + txt_Reg_Id.getText().toString(), postValues);
        myDB_Reference.updateChildren(childUpdates);
        Toast.makeText(UserReg.this,"회원가입이 완료되었습니다.",Toast.LENGTH_LONG).show();
        finish();
    }


}