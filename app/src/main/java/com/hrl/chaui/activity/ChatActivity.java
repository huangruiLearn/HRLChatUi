package com.hrl.chaui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hrl.chaui.adapter.ChatAdapter;
import com.hrl.chaui.bean.MsgType;
import com.hrl.chaui.util.LogUtil;
import com.hrl.chaui.bean.Message;
import com.hrl.chaui.R;
import com.hrl.chaui.bean.AudioMsgBody;
import com.hrl.chaui.bean.FileMsgBody;
import com.hrl.chaui.bean.ImageMsgBody;
import com.hrl.chaui.bean.MsgSendStatus;
import com.hrl.chaui.bean.TextMsgBody;
import com.hrl.chaui.bean.VideoMsgBody;
import com.hrl.chaui.util.ChatUiHelper;
import com.hrl.chaui.util.FileUtils;
import com.hrl.chaui.util.PictureFileUtil;
import com.hrl.chaui.widget.MediaManager;
import com.hrl.chaui.widget.RecordButton;
import com.hrl.chaui.widget.StateButton;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ChatActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.llContent)
    LinearLayout mLlContent;
    @BindView(R.id.rv_chat_list)
    RecyclerView mRvChat;
    @BindView(R.id.et_content)
    EditText mEtContent;
    @BindView(R.id.bottom_layout)
    RelativeLayout mRlBottomLayout;//表情,添加底部布局
    @BindView(R.id.ivAdd)
    ImageView mIvAdd;
    @BindView(R.id.ivEmo)
    ImageView mIvEmo;
    @BindView(R.id.btn_send)
    StateButton mBtnSend;//发送按钮
    @BindView(R.id.ivAudio)
    ImageView mIvAudio;//录音图片
    @BindView(R.id.btnAudio)
    RecordButton mBtnAudio;//录音按钮
    @BindView(R.id.rlEmotion)
    LinearLayout mLlEmotion;//表情布局
    @BindView(R.id.llAdd)
    LinearLayout mLlAdd;//添加布局
     @BindView(R.id.swipe_chat)
     SwipeRefreshLayout mSwipeRefresh;//下拉刷新
     private ChatAdapter mAdapter;
     public static final String 	  mSenderId="right";
     public static final String     mTargetId="left";
     public static final int       REQUEST_CODE_IMAGE=0000;
     public static final int       REQUEST_CODE_VEDIO=1111;
     public static final int       REQUEST_CODE_FILE=2222;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initContent();
    }


    private    ImageView  ivAudio;

    protected void initContent() {
        ButterKnife.bind(this) ;
        mAdapter=new ChatAdapter(this, new ArrayList<Message>());
        LinearLayoutManager mLinearLayout=new LinearLayoutManager(this);
        mRvChat.setLayoutManager(mLinearLayout);
        mRvChat.setAdapter(mAdapter);
        mSwipeRefresh.setOnRefreshListener(this);
        initChatUi();
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

                final  boolean isSend = mAdapter.getItem(position).getSenderId().equals(ChatActivity.mSenderId);
                 if (ivAudio != null) {
                    if (isSend){
                        ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_right_3);
                    }else {
                        ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_left_3);
                    }
                     ivAudio = null;
                    MediaManager.reset();
                }else{
                    ivAudio = view.findViewById(R.id.ivAudio);
                      MediaManager.reset();
                    if (isSend){
                        ivAudio.setBackgroundResource(R.drawable.audio_animation_right_list);
                    }else {
                        ivAudio.setBackgroundResource(R.drawable.audio_animation_left_list);
                    }
                      AnimationDrawable  drawable = (AnimationDrawable) ivAudio.getBackground();
                    drawable.start();
                     MediaManager.playSound(ChatActivity.this,((AudioMsgBody)mAdapter.getData().get(position).getBody()).getLocalPath(), new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                             if (isSend){
                                ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_right_3);
                            }else {
                                ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_left_3);
                            }

                            MediaManager.release();
                         }
                    });
                }
            }
        });

     }









    @Override
    public void onRefresh() {
          //下拉刷新模拟获取历史消息
          List<Message>  mReceiveMsgList=new ArrayList<Message>();
          //构建文本消息
          Message mMessgaeText=getBaseReceiveMessage(MsgType.TEXT);
          TextMsgBody mTextMsgBody=new TextMsgBody();
          mTextMsgBody.setMessage("收到的消息");
          mMessgaeText.setBody(mTextMsgBody);
          mReceiveMsgList.add(mMessgaeText);
          //构建图片消息
          Message mMessgaeImage=getBaseReceiveMessage(MsgType.IMAGE);
          ImageMsgBody mImageMsgBody=new ImageMsgBody();
          mImageMsgBody.setThumbUrl("https://c-ssl.duitang.com/uploads/item/201208/30/20120830173930_PBfJE.thumb.700_0.jpeg");
          mMessgaeImage.setBody(mImageMsgBody);
          mReceiveMsgList.add(mMessgaeImage);
          //构建文件消息
          Message mMessgaeFile=getBaseReceiveMessage(MsgType.FILE);
          FileMsgBody mFileMsgBody=new FileMsgBody();
          mFileMsgBody.setDisplayName("收到的文件");
          mFileMsgBody.setSize(12);
          mMessgaeFile.setBody(mFileMsgBody);
          mReceiveMsgList.add(mMessgaeFile);
          mAdapter.addData(0,mReceiveMsgList);
          mSwipeRefresh.setRefreshing(false);
    }




    private void initChatUi(){
        //mBtnAudio
        final ChatUiHelper mUiHelper= ChatUiHelper.with(this);
        mUiHelper.bindContentLayout(mLlContent)
                .bindttToSendButton(mBtnSend)
                .bindEditText(mEtContent)
                .bindBottomLayout(mRlBottomLayout)
                .bindEmojiLayout(mLlEmotion)
                .bindAddLayout(mLlAdd)
                .bindToAddButton(mIvAdd)
                .bindToEmojiButton(mIvEmo)
                .bindAudioBtn(mBtnAudio)
                .bindAudioIv(mIvAudio)
                .bindEmojiData();
        //底部布局弹出,聊天列表上滑
        mRvChat.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    mRvChat.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mAdapter.getItemCount() > 0) {
                                mRvChat.smoothScrollToPosition(mAdapter.getItemCount() - 1);
                            }
                        }
                    });
                }
            }
        });
        //点击空白区域关闭键盘
        mRvChat.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mUiHelper.hideBottomLayout(false);
                mUiHelper.hideSoftInput();
                mEtContent.clearFocus();
                mIvEmo.setImageResource(R.mipmap.ic_emoji);
                return false;
            }
        });
        //
        ((RecordButton) mBtnAudio).setOnFinishedRecordListener(new RecordButton.OnFinishedRecordListener() {
            @Override
            public void onFinishedRecord(String audioPath, int time) {
                LogUtil.d("录音结束回调");
                 File file = new File(audioPath);
                 if (file.exists()) {
                    sendAudioMessage(audioPath,time);
                }
            }
        });

    }

    @OnClick({R.id.btn_send,R.id.rlPhoto,R.id.rlVideo,R.id.rlLocation,R.id.rlFile})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                sendTextMsg(mEtContent.getText().toString());
                mEtContent.setText("");
                break;
            case R.id.rlPhoto:
                PictureFileUtil.openGalleryPic(ChatActivity.this,REQUEST_CODE_IMAGE);
                break;
            case R.id.rlVideo:
                PictureFileUtil.openGalleryAudio(ChatActivity.this,REQUEST_CODE_VEDIO);
                break;
            case R.id.rlFile:
                PictureFileUtil.openFile(ChatActivity.this,REQUEST_CODE_FILE);
                break;
            case R.id.rlLocation:
                break;
        }
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FILE:
                    String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                    LogUtil.d("获取到的文件路径:"+filePath);
                    sendFileMessage(mSenderId, mTargetId, filePath);
                    break;
                case REQUEST_CODE_IMAGE:
                    // 图片选择结果回调
                    List<LocalMedia> selectListPic = PictureSelector.obtainMultipleResult(data);
                    for (LocalMedia media : selectListPic) {
                        LogUtil.d("获取图片路径成功:"+  media.getPath());
                        sendImageMessage(media);
                    }
                    break;
                case REQUEST_CODE_VEDIO:
                    // 视频选择结果回调
                    List<LocalMedia> selectListVideo = PictureSelector.obtainMultipleResult(data);
                    for (LocalMedia media : selectListVideo) {
                        LogUtil.d("获取视频路径成功:"+  media.getPath());
                        sendVedioMessage(media);
                    }
                    break;
            }
        }
    }




    //文本消息
    private void sendTextMsg(String hello)  {
        final Message mMessgae=getBaseSendMessage(MsgType.TEXT);
        TextMsgBody mTextMsgBody=new TextMsgBody();
        mTextMsgBody.setMessage(hello);
        mMessgae.setBody(mTextMsgBody);
        //开始发送
        mAdapter.addData( mMessgae);
        //模拟两秒后发送成功
        updateMsg(mMessgae);
    }



    //图片消息
    private void sendImageMessage(final LocalMedia media) {
        final Message mMessgae=getBaseSendMessage(MsgType.IMAGE);
        ImageMsgBody mImageMsgBody=new ImageMsgBody();
        mImageMsgBody.setThumbUrl(media.getCompressPath());
        mMessgae.setBody(mImageMsgBody);
        //开始发送
        mAdapter.addData( mMessgae);
        //模拟两秒后发送成功
        updateMsg(mMessgae);
    }


    //视频消息
    private void sendVedioMessage(final LocalMedia media) {
        final Message mMessgae=getBaseSendMessage(MsgType.VIDEO);
        //生成缩略图路径
        String vedioPath=media.getPath();
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(vedioPath);
        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime();
        String imgname = System.currentTimeMillis() + ".jpg";
        String urlpath = Environment.getExternalStorageDirectory() + "/" + imgname;
        File f = new File(urlpath);
        try {
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        }catch ( Exception e) {
            LogUtil.d("视频缩略图路径获取失败："+e.toString());
            e.printStackTrace();
        }
        VideoMsgBody mImageMsgBody=new VideoMsgBody();
        mImageMsgBody.setExtra(urlpath);
        mMessgae.setBody(mImageMsgBody);
        //开始发送
        mAdapter.addData( mMessgae);
        //模拟两秒后发送成功
        updateMsg(mMessgae);

    }

    //文件消息
    private void sendFileMessage(String from, String to, final String path) {
        final Message mMessgae=getBaseSendMessage(MsgType.FILE);
        FileMsgBody mFileMsgBody=new FileMsgBody();
        mFileMsgBody.setLocalPath(path);
        mFileMsgBody.setDisplayName(FileUtils.getFileName(path));
        mFileMsgBody.setSize(FileUtils.getFileLength(path));
        mMessgae.setBody(mFileMsgBody);
        //开始发送
        mAdapter.addData( mMessgae);
        //模拟两秒后发送成功
        updateMsg(mMessgae);

    }

    //语音消息
    private void sendAudioMessage(  final String path,int time) {
        final Message mMessgae=getBaseSendMessage(MsgType.AUDIO);
        AudioMsgBody mFileMsgBody=new AudioMsgBody();
        mFileMsgBody.setLocalPath(path);
        mFileMsgBody.setDuration(time);
        mMessgae.setBody(mFileMsgBody);
        //开始发送
        mAdapter.addData( mMessgae);
        //模拟两秒后发送成功
        updateMsg(mMessgae);
    }


    private Message getBaseSendMessage(MsgType msgType){
        Message mMessgae=new Message();
        mMessgae.setUuid(UUID.randomUUID()+"");
        mMessgae.setSenderId(mSenderId);
        mMessgae.setTargetId(mTargetId);
        mMessgae.setSentTime(System.currentTimeMillis());
        mMessgae.setSentStatus(MsgSendStatus.SENDING);
        mMessgae.setMsgType(msgType);
        return mMessgae;
    }


    private Message getBaseReceiveMessage(MsgType msgType){
        Message mMessgae=new Message();
        mMessgae.setUuid(UUID.randomUUID()+"");
        mMessgae.setSenderId(mTargetId);
        mMessgae.setTargetId(mSenderId);
        mMessgae.setSentTime(System.currentTimeMillis());
        mMessgae.setSentStatus(MsgSendStatus.SENDING);
        mMessgae.setMsgType(msgType);
        return mMessgae;
    }


    private void updateMsg(final Message mMessgae) {
        mRvChat.scrollToPosition(mAdapter.getItemCount() - 1);
         //模拟2秒后发送成功
        new Handler().postDelayed(new Runnable() {
            public void run() {
                int position=0;
                mMessgae.setSentStatus(MsgSendStatus.SENT);
                //更新单个子条目
                for (int i=0;i<mAdapter.getData().size();i++){
                    Message mAdapterMessage=mAdapter.getData().get(i);
                    if (mMessgae.getUuid().equals(mAdapterMessage.getUuid())){
                        position=i;
                    }
                }
                mAdapter.notifyItemChanged(position);
            }
        }, 2000);

    }



}
