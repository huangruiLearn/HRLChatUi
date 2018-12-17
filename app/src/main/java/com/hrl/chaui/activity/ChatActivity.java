package com.hrl.chaui.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
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
import com.hrl.chaui.adapter.ChatAdapter;
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
import com.hrl.chaui.widget.RecordButton;
import com.hrl.chaui.widget.StateButton;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;


import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;


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
    SwipeRefreshLayout mSwipeLayout;
    private ChatAdapter mAdapter;
     public static String 	mSenderId="right";
    private static String     mTargetId="left";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initContent();
    }

     protected void initContent() {
        ButterKnife.bind(this) ;

        mAdapter=new ChatAdapter(this, new ArrayList<Message>());
        LinearLayoutManager mLinearLayout=new LinearLayoutManager(this);
        mRvChat.setLayoutManager(mLinearLayout);
        mRvChat.setAdapter(mAdapter);
        mSwipeLayout.setOnRefreshListener(this);
        initChatUi();

     }







    private void updateMsgStatus(Message message) {
        for (int i = 0; i < mAdapter.getData().size(); i++) {
            Message msg = mAdapter.getData().get(i);
            if (msg.getUuid().equals(message.getUuid())) {
                mAdapter.setData(i,message);
                mAdapter.notifyItemChanged(i);
                break;
            }
        }
    }


    @Override
    public void onRefresh() {



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
                LogUtil.d("发送消息");
               sendTextMsg(mEtContent.getText().toString());
                break;
            case R.id.rlPhoto:
                PictureFileUtil.openGalleryPic(ChatActivity.this);
                break;
            case R.id.rlVideo:
                PictureFileUtil.openGalleryAudio(ChatActivity.this);
                break;
            case R.id.rlLocation:

                break;
            case R.id.rlFile:
                PictureFileUtil.openFile(ChatActivity.this,1110);
                break;
        }
    }



 /*   //收到消息监听
    @Subscriber(tag = Constant.EB_MSG_RECEIVE, mode= ThreadMode.MAIN)
    public void getEventMessage(Message message) {
        LogUtils.d("ConMsgFragment收到消息,更新底部未读数量");
        // refresh();
        if (!UserDbUtil.getId().equals(message.getTargetId()))
            return;
        LogUtil.d("收到消息后更新列表:"+message);
        mAdapter.addData(message);
        mRvChat.scrollToPosition(mAdapter.getItemCount() - 1);
        //更新会话信息
        ConversationDbUtil.updateConversationLastMsg(message);


    }*/



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1110:
                    String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                    LogUtil.d("获取到的文件路径:"+filePath);
                    sendFileMessage(mSenderId, mTargetId, filePath);
                    break;
                case PictureConfig.TYPE_IMAGE:
                    // 图片选择结果回调
                    List<LocalMedia> selectListPic = PictureSelector.obtainMultipleResult(data);
                    for (LocalMedia media : selectListPic) {
                        LogUtil.d("获取图片或者视频路径成功:"+  media.getPath());
                        sendImageMessage(media);
                    }
                    break;
                case PictureConfig.TYPE_VIDEO:
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



    private void sendTextMsg(String hello)  {
        //文本消息
        final Message mMessgae=new Message();
        mMessgae.setSenderId(mSenderId);
        mMessgae.setTargetId(mTargetId);
        mMessgae.setSentTime(System.currentTimeMillis());
        mMessgae.setSentStatus(MsgSendStatus.SENDING);
        TextMsgBody mTextMsgBody=new TextMsgBody();
        mTextMsgBody.setMessage(hello);
        mMessgae.setBody(mTextMsgBody);
        //开始发送
        mAdapter.addData( mMessgae);
        rvMoveToBottom();
        //2秒后发送成功
        new Handler().postDelayed(new Runnable() {
            public void run() {
                mMessgae.setSentStatus(MsgSendStatus.SENT);
                mAdapter.notifyDataSetChanged();
            }
        }, 2000);
    }


    private void sendImageMessage( final LocalMedia media) {
        //图片消息
        final Message mMessgae=new Message();
        mMessgae.setSenderId(mSenderId);
        mMessgae.setTargetId(mTargetId);
        mMessgae.setSentTime(System.currentTimeMillis());
        mMessgae.setSentStatus(MsgSendStatus.SENDING);
        ImageMsgBody mImageMsgBody=new ImageMsgBody();
        mImageMsgBody.setThumbUrl(media.getCompressPath());
        mMessgae.setBody(mImageMsgBody);
        //开始发送
        mAdapter.addData( mMessgae);
        rvMoveToBottom();
        //2秒后发送成功
        new Handler().postDelayed(new Runnable() {
            public void run() {
                mMessgae.setSentStatus(MsgSendStatus.SENT);
                mAdapter.notifyDataSetChanged();
            }
        }, 2000);
    }



    private void sendVedioMessage(final LocalMedia media) {







        //视频消息
        final Message mMessgae=new Message();
        mMessgae.setSenderId(mSenderId);
        mMessgae.setTargetId(mTargetId);
        mMessgae.setSentTime(System.currentTimeMillis());
        mMessgae.setSentStatus(MsgSendStatus.SENDING);

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
            LogUtil.d("视频路径获取失败："+e.toString());
            e.printStackTrace();
        }

        LogUtil.d("视频的路径:"+vedioPath);
        LogUtil.d("视频图片的路径:"+urlpath);

        VideoMsgBody mImageMsgBody=new VideoMsgBody();
        mImageMsgBody.setExtra(urlpath);
        mMessgae.setBody(mImageMsgBody);
        //开始发送
        mAdapter.addData( mMessgae);
        rvMoveToBottom();
        //2秒后发送成功
        new Handler().postDelayed(new Runnable() {
            public void run() {
                mMessgae.setSentStatus(MsgSendStatus.SENT);
                mAdapter.notifyDataSetChanged();
            }
        }, 2000);

    }


    private void sendFileMessage(String from, String to, final String path) {
        //视频消息
        final Message mMessgae=new Message();
        mMessgae.setSenderId(mSenderId);
        mMessgae.setTargetId(mTargetId);
        mMessgae.setSentTime(System.currentTimeMillis());
        mMessgae.setSentStatus(MsgSendStatus.SENDING);

        FileMsgBody mFileMsgBody=new FileMsgBody();
        mFileMsgBody.setLocalPath(path);

        mFileMsgBody.setDisplayName(FileUtils.getFileName(path));
        mFileMsgBody.setSize(FileUtils.getFileLength(path));
       mMessgae.setBody(mFileMsgBody);
        //开始发送
        mAdapter.addData( mMessgae);
        rvMoveToBottom();
        //2秒后发送成功
        new Handler().postDelayed(new Runnable() {
            public void run() {
                mMessgae.setSentStatus(MsgSendStatus.SENT);
                mAdapter.notifyDataSetChanged();
            }
        }, 2000);

    }


    private void sendAudioMessage(  final String path,int time) {
        //视频消息
        final Message mMessgae=new Message();
        mMessgae.setSenderId(mSenderId);
        mMessgae.setTargetId(mTargetId);
        mMessgae.setSentTime(System.currentTimeMillis());
        mMessgae.setSentStatus(MsgSendStatus.SENDING);

        AudioMsgBody mFileMsgBody=new AudioMsgBody();
        mFileMsgBody.setLocalPath(path);
        mFileMsgBody.setDuration(time);
        mMessgae.setBody(mFileMsgBody);

        //开始发送
        mAdapter.addData( mMessgae);
        rvMoveToBottom();
        //2秒后发送成功
        new Handler().postDelayed(new Runnable() {
            public void run() {
                mMessgae.setSentStatus(MsgSendStatus.SENT);
                mAdapter.notifyDataSetChanged();
            }
        }, 2000);
    }



    private void  rvMoveToBottom(){
        mRvChat.scrollToPosition(mAdapter.getItemCount() - 1);
    }






}
