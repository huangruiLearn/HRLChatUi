package com.hrl.chaui.bean;



public  class   MsgBody implements java.io.Serializable {

    //所在消息的消息类型(增加这个字段用于在从数据库中获取消息时,直接把转换为sgbody的子类对象)
    private MsgType localMsgType;


    public MsgType getLocalMsgType() {
        return localMsgType;
    }

    public void setLocalMsgType(MsgType localMsgType) {
        this.localMsgType = localMsgType;
    }
}
