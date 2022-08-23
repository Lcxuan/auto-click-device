package com.lcxuan.autoclickdevice.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.lcxuan.autoclickdevice.R;

public class TakeAimView extends FrameLayout {

    private TextView numId;
    private ImageView takeAim;

    public TakeAimView(@NonNull Context context) {
        super(context);

        // 加载布局
        View view = LayoutInflater.from(context).inflate(R.layout.layout_take_aim_layout, this);
        numId = view.findViewById(R.id.num_id);
        takeAim = view.findViewById(R.id.take_aim);
    }

    /**
     * 设置编号
     * @param id 编号
     * @return
     */
    public TakeAimView setNumId(Integer id){
        numId.setText(String.valueOf(id));
        return this;
    }

    /**
     * 获取编号
     * @return
     */
    public Integer getNumId(){
        return Integer.parseInt(numId.getText().toString());
    }

    /**
     * 设置图片大小
     * @param width 宽度
     * @param height 高度
     * @return
     */
    public TakeAimView setTakeAimSize(int width, int height){
        ViewGroup.LayoutParams params = takeAim.getLayoutParams();
        params.width = width;
        params.height = height;
        takeAim.setLayoutParams(params);
        return this;
    }

    /**
     * 设置图片样式
     * @param styleId 样式ID
     * @return
     */
    public TakeAimView setTakeAimStyle(int styleId){
        takeAim.setImageResource(styleId);
        return this;
    }
}
