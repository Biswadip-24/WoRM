package com.example.memeland;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;


import androidx.appcompat.app.AppCompatActivity;

public class meme_layout_activity extends AppCompatActivity {

    ImageButton mLiked;
    int liked = R.drawable.liked_heart;
    int not_liked = R.drawable.heart;
    static int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meme_layout);
        mLiked = findViewById(R.id.imageButton2);
    }
    public void Onlike(View v){
        if(count==0) {
            count = 1;
            mLiked.setBackgroundResource(liked);
        }
        else {
            count = 0;
            mLiked.setBackgroundResource(not_liked);
        }

    }
}
