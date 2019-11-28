package com.app.inputcode;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private InputVerificationCodeView inputVerificationCodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputVerificationCodeView = findViewById(R.id.inputCodeView);
        inputVerificationCodeView.setOnInputCodeListener(new InputVerificationCodeView.OnInputCodeListener() {
            @Override
            public void onInputCode(String code) {
                Toast.makeText(MainActivity.this, code, Toast.LENGTH_LONG).show();
                inputVerificationCodeView.resetView();
            }
        });
    }
}
