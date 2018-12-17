package com.hrl.chaui.bean;


public class ImageMsgBody extends FileMsgBody{
    //缩略图文件的本地路径
    private String thumbPath;
    //缩略图远程地址
    private String thumbUrl;
    //是否压缩(false:原图，true：压缩过)
    private boolean compress;
    //高度
    private int height;
    //宽度
    private int width;







    public String getThumbPath() {
        return thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
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



    public ImageMsgBody() {
    }


    /**
     * 生成ImageMessage对象。
     * @param thumbPath  缩略图地址。
     * @param localPath 大图地址。
     * @param compress 是否发送原图。
     * @return ImageMessage对象实例。
     */
    public ImageMsgBody(String thumbPath, String localPath, boolean compress) {
        this.thumbPath = thumbPath;
        this.thumbUrl = localPath;
        this.compress = compress;
    }

    public static ImageMsgBody obtain(String thumUri, String localUri, boolean isFull) {
        return new ImageMsgBody(thumUri.toString(), localUri.toString(), isFull);
    }

    @Override
    public String toString() {
        return "ImageMsgBody{" +
                "thumbPath='" + thumbPath + '\'' +
                ", thumbUrl='" + thumbUrl + '\'' +
                ", compress=" + compress +
                ", height=" + height +
                ", width=" + width +
                '}';
    }
}
