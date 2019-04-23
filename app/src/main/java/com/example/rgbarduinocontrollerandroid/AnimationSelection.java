package com.example.rgbarduinocontrollerandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class AnimationSelection extends AppCompatActivity {

    private RadioGroup radioAnimationGroup;
    private RadioButton radioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation_selection);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

    }

    public void onRadioButtonClicked(View view) {

        radioAnimationGroup = findViewById(R.id.radioGroup);
        int selectedButtonID = radioAnimationGroup.getCheckedRadioButtonId();
        radioButton = findViewById(selectedButtonID);



        Intent intent = new Intent();
        intent.putExtra("selectedAnimation", radioButton.getText());
        setResult(RESULT_OK, intent);
        finish();
    }

}
