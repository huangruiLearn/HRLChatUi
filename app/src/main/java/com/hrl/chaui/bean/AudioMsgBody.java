package com.hrl.chaui.bean;



public class AudioMsgBody extends FileMsgBody{
    //语音消息长度,单位：秒。
    private long duration;
    //扩展信息，可以放置任意的数据内容，也可以去掉此属性


    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }


}
