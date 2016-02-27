package com.doz6.imagescan;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Scroller;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/2/26.
 */
public class MyImageTopView extends ViewGroup{//自定义控件从ViewGroup继承而来

    private GestureDetector gestureDetector;//手势检测器

    private Scroller scroller;//滚动对象

    private int currentImageIndex=0;//记录当前显示的图片的序号

    private boolean fling=false;//添加标志，防止底层的onTouch事件重复处理UP事件

    private Handler handler;//handler对象，用于发送、接受和处理消息

    private Context context;//上下文对象

    public MyImageTopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        init();//执行初始化操作
        this.setOnTouchListener(new MyOnTouchListener());//添加触摸事件处理
    }

    public void init(){
        scroller=new Scroller(context);//创建滚动条
        handler=new Handler(){
            public void handleMessage(Message msg){
                if(msg.what==0x11){
                    scrollToImage((currentImageIndex+1)%getChildCount());
                }
            }
        };
        gestureDetector=new GestureDetector(context, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {//手指刚刚接触到触摸屏的那一刹那，就是接触的那一下
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {//手指按在触摸屏上，它的时间范围在按下起效，在长按之前

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {//手指离开触摸屏的那一刹调用该方法
                return false;
            }

            //手指在触摸屏上滑动，如果滑动范围在第一页和最后一页之间，distanceX>0表示向右滑动，distance<0表示向左滑动，如果超出了这两个范围，则不做任何操作
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if((distanceX>0 && getScaleX()<getWidth()*(getChildCount()-1)) || (distanceX<0 && getScrollX()>0)){
                    scrollBy((int) distanceX, 0);//滚动的距离，在此只需要水平滚动，垂直方向滚动为0
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {//手指按在屏幕上持续一段时间，并且没有松开

            }

            //手指在触摸屏上迅速移动并松开
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if(Math.abs(velocityX)> ViewConfiguration.get(context).getScaledMinimumFlingVelocity()){//判断是否达到最小滑动速度，取绝对值
                    if(velocityX>0 && currentImageIndex>=0){//如果速度超过最小速度
                        fling=true;//velocityX>0表示向左滑动
                        scrollToImage((currentImageIndex-1+getChildCount()%getChildCount()));
                    }else if(velocityY<0 && currentImageIndex<=getChildCount()-1){
                        fling=true;//velocityY<0表示向右滑动
                        scrollToImage((currentImageIndex+1)%getChildCount());
                    }
                }
                return true;
            }
        });
        Timer timer=new Timer();//创建定时器对象
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0x11);
            }
        }, 0, 2000);
    }

    public void scrollToImage(int targetIndex){//跳转到目标图片
        if(targetIndex!=currentImageIndex && getFocusedChild()!=null && getFocusedChild()==getChildAt(currentImageIndex)){
            getFocusedChild().clearFocus();//当前图片清楚焦点
        }
        final int delta=targetIndex*getWidth()-getScrollX();//需要滑动的距离
        int time=Math.abs(delta)*5;//time表示滑动的时间，单位为毫秒，滑动的时间是滑动距离的5倍
        scroller.startScroll(getScrollX(),0,delta,0);
        invalidate();//刷新页面
        currentImageIndex=targetIndex;//改变当前图片的索引
        ((MainActivity)context).resetImg();
        //改变下方圆圈的状态
        ((MainActivity)context).imageViews[currentImageIndex].setImageResource(R.drawable.choosed);
    }

    public void computeScroll(){//重写父类的方法，记录滚动条的新位置
        super.computeScroll();
        if(scroller.computeScrollOffset()){
            scrollTo(scroller.getCurrX(),0);
            postInvalidate();
        }
    }

    public class MyOnTouchListener implements OnTouchListener{//触摸事件监听器
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            gestureDetector.onTouchEvent(event);//将触摸事件交由GestureDetector处理
            if(event.getAction()==MotionEvent.ACTION_UP){
                if(!fling){//当用户停止拖动时
                    snapToDestination();
                }
                fling=false;
            }
            return true;
        }
    }

    //该方法从ViewGroup中继承而来，是它的一个抽象方法，该方法用于指定容器里的控件该如何摆放，当控件大小发送变化时会回调该方法
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for(int i=00;i<getChildCount();i++){//设置布局，将子视图按顺序横向排列
            View child=getChildAt(i);//获取到每一个子控件
            child.setVisibility(View.VISIBLE);
            child.measure(r - l, b - t);
            child.layout(i*getWidth(),0,(i+1)*getWidth(),getHeight());
        }
    }

    private void snapToDestination(){//滑动到指定图片
        scrollToImage((getScrollX()+(getWidth()/2))/getWidth());//四舍五入，若超过一半进入下一张图片
    }

    public void initImages(int[] imgIds){//初始化显示的图片
        int num=imgIds.length;
        this.removeAllViews();//清空所有的控件
        for(int i=0;i<num;i++){
            ImageView imageView=new ImageView(getContext());
            imageView.setImageResource(imgIds[i]);//设置每个图片控件的图片
            this.addView(imageView);//将图片添加到自定义的控件中
        }
    }
}
