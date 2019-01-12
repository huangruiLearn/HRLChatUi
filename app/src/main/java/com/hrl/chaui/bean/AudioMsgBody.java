package com.hrl.chaui.bean;



public class AudioMsgBody extends FileMsgBody{
    //语音消息长度,单位：秒。
    private long duration;


    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }


}
