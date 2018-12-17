package com.hrl.chaui.bean;



public class VideoMsgBody extends FileMsgBody{
    //视频消息长度
    private long duration;
    //高度
    private int height;
    //宽度
    private int width;




    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }


}
