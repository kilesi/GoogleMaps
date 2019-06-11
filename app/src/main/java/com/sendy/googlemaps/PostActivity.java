package com.sendy.googlemaps;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class PostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        TextView textView = findViewById(R.id.post_view);

        Intent intent = getIntent();
        String orderNo = intent.getStringExtra("order_no");

       // Toast.makeText(this, orderNo, Toast.LENGTH_SHORT).show();

        textView.setText(orderNo);
    }

    public void getResponse(View view) {
    }
}
