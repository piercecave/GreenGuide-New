package com.guide.green.green_guide_master;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import static android.app.PendingIntent.getActivity;

public class ResultDetail extends Activity {
    private TextView reviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_detail);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        reviews =  findViewById(R.id. Reviews);
        if (!extras.isEmpty()) {
            String value = extras.getString("id");
            reviews.setText("Reviews"); 
        }
    }
}
