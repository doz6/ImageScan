package com.doz6.imagescan;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private MyImageTopView mTopView;

    private LinearLayout mBottomView;

    private int[] imgIds=new int[] {R.drawable.pic1,R.drawable.pic2,R.drawable.pic3,R.drawable.pic4,R.drawable.pic5,R.drawable.pic6,R.drawable.pic7};

    public ImageView[] imageViews=new ImageView[imgIds.length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBottomView=(LinearLayout)findViewById(R.id.mBottomView);
        mTopView=(MyImageTopView)findViewById(R.id.mTopView);
        initBottom();
        mTopView.initImages(imgIds);
    }

    public void initBottom(){
        for(int i=0;i<imageViews.length;i++){
            imageViews[i]=new ImageView(this);
            if(i==0){
                imageViews[i].setImageResource(R.drawable.choosed);
            }else {
                imageViews[i].setImageResource(R.drawable.unchoosed);
            }
            imageViews[i].setPadding(15,0,0,0);
            imageViews[i].setId(i);
            imageViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetImg();
                    ((ImageView)v).setImageResource(R.drawable.choosed);
                    mTopView.scrollToImage(v.getId());
                }
            });
            mBottomView.addView(imageViews[i]);
        }
    }

    public void resetImg(){
        for(int i=0;i<imageViews.length;i++){
            imageViews[i].setImageResource(R.drawable.unchoosed);
        }
    }

}
