package com.hrl.chaui.emoji;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hrl.chaui.R;

import java.util.List;

public class EmojiAdapter extends BaseQuickAdapter< EmojiBean,BaseViewHolder> {

    /**
     * 页数下标,从0开始(通俗讲第几页)
     */
    private int mIndex;

    /**
     * 每页显示最大条目个数
     */
    private int mPageSize;

    public EmojiAdapter( @Nullable List<EmojiBean> data, int index, int pageSize) {

        super(R.layout.item_emoji,  data);
        mPageSize = pageSize;
        mIndex = index;
    }

    @Override
    protected void convert(BaseViewHolder helper, EmojiBean item) {
        //判断是否为最后一个item
        if (item.getId()==0) {
            //iv_emotion.setImageResource(R.drawable.compose_emotion_delete);
            helper.setBackgroundRes(R.id.et_emoji,R.mipmap.rc_icon_emoji_delete );
        } else {
             helper.setText(R.id.et_emoji,item.getUnicodeInt() );
        }



    }


}
