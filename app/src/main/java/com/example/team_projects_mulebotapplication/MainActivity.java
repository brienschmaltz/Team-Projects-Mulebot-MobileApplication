package com.example.team_projects_mulebotapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    String gps_example_text;
    TextView txtView;
    Button d_button, f_button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Essentially required code to instantiate app and its code
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Define var
        gps_example_text ="GPS Data: 55.01, 78.66";

        //Attaching GUI to code
        d_button = (Button)findViewById(R.id.d_button);
        txtView = (TextView)findViewById(R.id.gps_data_textview);

        //Button to change text box to gps_example_text
        d_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                txtView.setText(gps_example_text);
            }
        });
    }
}
