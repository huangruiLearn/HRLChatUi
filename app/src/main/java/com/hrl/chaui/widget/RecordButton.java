package com.hrl.chaui.widget;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hrl.chaui.MyApplication;
import com.hrl.chaui.activity.ChatActivity;
import com.hrl.chaui.activity.SplashActivity;
import com.hrl.chaui.util.LogUtil;
import com.hrl.chaui.R;
import com.hrl.chaui.util.PermissionUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.IOException;

import io.reactivex.functions.Consumer;

public class RecordButton extends android.support.v7.widget.AppCompatButton {




    public RecordButton(Context context) {
        super(context);
        init();
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private String mFile =  getContext().getFilesDir()+ "/"+"voice_"+System.currentTimeMillis()+".mp3";


    private OnFinishedRecordListener finishedListener;
    /**
     * 最短录音时间
     **/
    private int MIN_INTERVAL_TIME = 1000;
    /**
     * 最长录音时间
     **/
    private int MAX_INTERVAL_TIME = 1000 * 60;




    private static  View view;

    private TextView mStateTV;

    private ImageView mStateIV;

    private MediaRecorder mRecorder;
    private ObtainDecibelThread mThread;
    private Handler volumeHandler;
/*    public static String File_Voice = Environment.getExternalStorageDirectory()
            .getPath() + "/acoe/demo/voice";// 录音全部放在这个目录下
    private final String SAVE_PATH = File_Voice;*/

    private float y ;





    public void setOnFinishedRecordListener(OnFinishedRecordListener listener) {
        finishedListener = listener;
    }


    private static long startTime;
    private Dialog recordDialog;
    private static int[] res = { R.drawable.ic_volume_0, R.drawable.ic_volume_1, R.drawable.ic_volume_2,
            R.drawable.ic_volume_3, R.drawable.ic_volume_4 , R.drawable.ic_volume_5 , R.drawable.ic_volume_6
            , R.drawable.ic_volume_7 , R.drawable.ic_volume_8 };



    @SuppressLint("HandlerLeak")
    private void init() {
        volumeHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == -100){
                    stopRecording();
                    recordDialog.dismiss();
                }else if(msg.what != -1){
                    mStateIV.setImageResource(res[msg.what]);
                }
            }
        };
    }

    private AnimationDrawable anim;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {






        int action = event.getAction();
        y = event.getY();
        LogUtil.d("y的值："+y);
        if(mStateTV!=null && mStateIV!=null &&y<0){

            mStateTV.setText("松开手指,取消发送");
            mStateIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_volume_cancel));
           // anim.stop();
        }else if(mStateTV != null){
            mStateTV.setText("手指上滑,取消发送");

        //    anim = (AnimationDrawable) mStateIV.getBackground();
         //   anim.start();
          //  view.setBackgroundResource(R.drawable.anim_mic);
         //   anim = (AnimationDrawable) view.getBackground();
         //   anim.start();
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setText("松开发送");
                initDialogAndStartRecord();
                //anim = (AnimationDrawable) mStateIV.getBackground();
               // anim.start();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                this.setText("按住录音");
                startTimer.cancel(); // 主动松开时取消计时
                recordTimer.cancel(); // 主动松开时取消计时
                if(y>=0 && (System.currentTimeMillis() - startTime <= MAX_INTERVAL_TIME)){
                    LogUtil.d("结束录音:");
                    finishRecord();

                }else if(y<0){  //当手指向上滑，会cancel
                    cancelRecord();
                }
                break;
       /*     case MotionEvent.ACTION_CANCEL: // 异常
                LogUtil.d("滑动取消");
                cancelRecord();
                break;*/
        }

        return true;
    }

    /**
     * 初始化录音对话框 并 开始录音
     */
    private void initDialogAndStartRecord() {
         startTime = System.currentTimeMillis();
         recordDialog = new Dialog(getContext(), R.style.like_toast_dialog_style);
       // view = new ImageView(getContext());
         view = View.inflate(getContext(), R.layout.dialog_record, null);
         mStateIV = (ImageView) view.findViewById(R.id.rc_audio_state_image);
         mStateTV = (TextView) view.findViewById(R.id.rc_audio_state_text);
         mStateIV.setImageDrawable(getResources().getDrawable(R.drawable.anim_mic));
         anim = (AnimationDrawable) mStateIV.getDrawable();
         anim.start();
         mStateIV.setVisibility(View.VISIBLE);
         //mStateIV.setImageResource(R.drawable.ic_volume_1);
         mStateTV.setVisibility(View.VISIBLE);
         mStateTV.setText("手指上滑,取消发送");
         recordDialog.setContentView(view, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        recordDialog.setOnDismissListener(onDismiss);
        WindowManager.LayoutParams lp = recordDialog.getWindow().getAttributes();
        lp.gravity = Gravity.CENTER;
        startRecording();
        recordDialog.show();
    }

    /**
     * 放开手指，结束录音处理
     */
    private void finishRecord() {
        long intervalTime = System.currentTimeMillis() - startTime;
        if (intervalTime < MIN_INTERVAL_TIME) {
            LogUtil.d("录音时间太短");
            volumeHandler.sendEmptyMessageDelayed(-100, 500);
             //view.setBackgroundResource(R.drawable.ic_voice_cancel);
            mStateIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_volume_wraning));
            mStateTV.setText("录音时间太短");
            anim.stop();
            File file = new File(mFile);
            file.delete();
        /*    stopRecording();
            recordDialog.dismiss();*/
            return;
        }else{
            LogUtil.d("取消录音111");
            stopRecording();
            recordDialog.dismiss();
        }
        LogUtil.d("录音完成的路径:"+mFile);
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(mFile);
            mediaPlayer.prepare();
            mediaPlayer.getDuration();
            LogUtil.d("获取到的时长:"+mediaPlayer.getDuration()/1000);
        }catch (Exception e){

        }

        if (finishedListener != null)
            finishedListener.onFinishedRecord(mFile,mediaPlayer.getDuration()/1000);

    }

    /**
     * 取消录音对话框和停止录音
     */
    public void cancelRecord() {
        LogUtil.d("取消录音222");
        stopRecording();
        recordDialog.dismiss();
        //MyToast.makeText(getContext(), "取消录音！", Toast.LENGTH_SHORT);
        File file = new File(mFile);
        file.delete();
    }

    //获取类的实例
   // ExtAudioRecorder extAudioRecorder; //压缩的录音（WAV）
    /**
     * 执行录音操作
     */
    //int num = 0 ;
    private void startRecording() {
          if (mRecorder != null) {
            mRecorder.reset();
        } else {
            mRecorder = new MediaRecorder();
        }
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        File file = new File(mFile);
        LogUtil.d("创建文件的路径:"+mFile);
        LogUtil.d("文件创建成功:"+file.exists());
        mRecorder.setOutputFile(mFile);

        try {
            mRecorder.prepare();

        } catch (IOException e) {
            LogUtil.d("prepare异常,重新开始录音:"+e.toString());
            e.printStackTrace();
        }

        try {
            mRecorder.start();
        }catch (Exception e){
            LogUtil.d("start异常,重新开始录音:"+e.toString());
            e.printStackTrace();
            mRecorder.release();
            mRecorder = null ;
            startRecording();
        }


       /* mThread = new  ObtainDecibelThread();
        mThread.start();*/

    }

    /**
     * 录音开始计时器，允许的最大时长倒数10秒时进入倒计时
     */
    private CountDownTimer startTimer = new CountDownTimer(20000 - 500 - 10000, 1000) { // 50秒后开始倒计时
        @Override
        public void onFinish() {
            recordTimer.start();
        }
        @Override
        public void onTick(long millisUntilFinished) {
        }};


    /**
     * 录音最后10秒倒计时器，倒计时结束发送录音
     */
    private CountDownTimer recordTimer = new CountDownTimer(10000, 1000){
        @Override
        public void onFinish() {
           // finishRecord();
        }
        @Override
        public void onTick(long millisUntilFinished) { // 显示倒计时动画
            switch ((int)millisUntilFinished / 1000) {
            /*   case 10:
                    view.setBackgroundResource(R.drawable.mic_count_10);
                    break;
                case 9:
                    view.setBackgroundResource(R.drawable.mic_count_9);
                    break;
                case 8:
                    view.setBackgroundResource(R.drawable.mic_count_8);
                    break;
                case 7:
                    view.setBackgroundResource(R.drawable.mic_count_7);
                    break;
                case 6:
                    view.setBackgroundResource(R.drawable.mic_count_6);
                    break;
                case 5:
                    view.setBackgroundResource(R.drawable.mic_count_5);
                    break;
                case 4:
                    view.setBackgroundResource(R.drawable.mic_count_4);
                    break;
                case 3:
                    view.setBackgroundResource(R.drawable.mic_count_3);
                    break;
                case 2:
                    view.setBackgroundResource(R.drawable.mic_count_2);
                    break;
                case 1:
                    view.setBackgroundResource(R.drawable.mic_count_1);
                    break; */
            }
        }};

    private void stopRecording() {

        if (mThread != null) {
            mThread.exit();
            mThread = null;
        }
        if (mRecorder != null) {
            try {
                mRecorder.stop();//停止时没有prepare，就会报stop failed
                mRecorder.reset();
                mRecorder.release();
                mRecorder = null;
            } catch (RuntimeException pE) {
                pE.printStackTrace();
            } finally {
                if (recordDialog.isShowing()) {
                    recordDialog.dismiss();
                }
            }
        }


        /*if(extAudioRecorder != null){
            extAudioRecorder.stop();
            extAudioRecorder.release();
        }*/
    }

    private class ObtainDecibelThread extends Thread {

        private volatile boolean running = true;

        public void exit() {
            running = false;
        }

        @Override
        public void run() {
            LogUtil.d("检测到的分贝001:");
            while (running) {
                if (mRecorder == null || !running) {
                    break;
                }
               // int x = recorder.getMaxAmplitude(); //振幅
                int db =   mRecorder.getMaxAmplitude() / 600;
                LogUtil.d("检测到的分贝002:"+mRecorder);
                if (db != 0 && y>=0) {


                   int f = (int) (db/ 5);
                    if (f ==0  )
                        volumeHandler.sendEmptyMessage(0);
                    else if (f  ==1)
                        volumeHandler.sendEmptyMessage(1);
                    else if (f  ==2)
                        volumeHandler.sendEmptyMessage(2);
                    else if (f  ==3)
                        volumeHandler.sendEmptyMessage(3);
                    else if (f  ==4)
                        volumeHandler.sendEmptyMessage(4);
                    else if (f  ==5)
                        volumeHandler.sendEmptyMessage(5);
                    else if (f  ==6)
                        volumeHandler.sendEmptyMessage(6);
                    else
                        volumeHandler.sendEmptyMessage(7);
                }

                volumeHandler.sendEmptyMessage(-1);
                if(System.currentTimeMillis() - startTime > 20000){
                    finishRecord();
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private DialogInterface.OnDismissListener onDismiss = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            stopRecording();
        }
    };

    public interface OnFinishedRecordListener {
        public void onFinishedRecord(String audioPath, int time);
    }

    class CountDown extends CountDownTimer {

        /**
         * @param millisInFuture
         * @param countDownInterval
         */
        public CountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onFinish() {
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }

    }
}
