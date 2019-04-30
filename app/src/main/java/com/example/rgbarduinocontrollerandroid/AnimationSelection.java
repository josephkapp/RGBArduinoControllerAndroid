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

        boolean checked = ((RadioButton) view).isChecked();
        String buttonText = "Off";
        int selAnimationVal = 0;

        switch(view.getId()) {
            case R.id.staticColorRadioButton:
                if (checked)
                {
                    buttonText = (String) ((RadioButton) view).getText();
                    selAnimationVal = 0;
                }
                    break;
            case R.id.fadeColorRadioButton:
                if (checked)
                {
                    buttonText = (String) ((RadioButton) view).getText();
                    selAnimationVal = 1;
                }
                    break;
            case R.id.rainbowCycleRadioButton:
                if (checked)
                {
                    buttonText = (String) ((RadioButton) view).getText();
                    selAnimationVal = 2;
                }
                break;
            case R.id.rainbowChaseRadioButton:
                if (checked)
                {
                    buttonText = (String) ((RadioButton) view).getText();
                    selAnimationVal = 3;
                }
                break;
            case R.id.theaterChaseRadioButton:
                if (checked)
                {
                    buttonText = (String) ((RadioButton) view).getText();
                    selAnimationVal = 4;
                }
                break;
            case R.id.randomColorChaseRadioButton:
                if (checked)
                {
                    buttonText = (String) ((RadioButton) view).getText();
                    selAnimationVal = 5;
                }
                break;
            case R.id.auroraGlowRadioButton:
                if (checked)
                {
                    buttonText = (String) ((RadioButton) view).getText();
                    selAnimationVal = 6;
                }
                break;
        }



        Intent intent = new Intent();
        intent.putExtra("selectedAnimationText", buttonText);
        intent.putExtra("selectedAnimationVal", selAnimationVal);
        setResult(RESULT_OK, intent);
        finish();
    }

}
