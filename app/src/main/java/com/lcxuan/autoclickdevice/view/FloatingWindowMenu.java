package com.lcxuan.autoclickdevice.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lcxuan.autoclickdevice.R;

public class FloatingWindowMenu extends LinearLayout implements View.OnClickListener {

    private ImageView start;
    private ImageView add;
    private ImageView remove;
    private ImageView close;

    private FloatingMenuInterface floatingMenuInterface;

    public FloatingWindowMenu(Context context) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_floating_window_menu, this);

        initView(view);
        initListener();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start:
                if (floatingMenuInterface != null){
                    floatingMenuInterface.start();
                }
                break;
            case R.id.add:
                if (floatingMenuInterface != null){
                    floatingMenuInterface.add();
                }
                break;
            case R.id.remove:
                if (floatingMenuInterface != null){
                    floatingMenuInterface.remove();
                }
                break;
            case R.id.close:
                if (floatingMenuInterface != null){
                    floatingMenuInterface.close();
                }
                break;
        }
    }

    public void setOnChangeListener(FloatingMenuInterface menuInterface) {
        this.floatingMenuInterface = menuInterface;
    }

    private void initListener() {
        start.setOnClickListener(this);
        add.setOnClickListener(this);
        remove.setOnClickListener(this);
        close.setOnClickListener(this);
    }

    private void initView(View view) {
        start = view.findViewById(R.id.start);
        close = view.findViewById(R.id.close);
        add = view.findViewById(R.id.add);
        remove = view.findViewById(R.id.remove);
    }

    public interface FloatingMenuInterface{
        void start();
        void add();
        void remove();
        void close();
    }
}
