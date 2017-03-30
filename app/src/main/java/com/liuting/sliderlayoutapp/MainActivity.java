package com.liuting.sliderlayoutapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.liuting.sliderlayout.SliderLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SliderLayout layoutImage;//自定义自动轮播图片View
    private List<Object> list;//图片列表
    private Dialog dialog;//提示框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layoutImage = (SliderLayout) findViewById(R.id.main_layout_image);
        dialog = new ProgressDialog(this);
        list = new ArrayList<>();
        list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1490860796691&di=ab022894a5d8ddae71c5e0284dc672a5&imgtype=0&src=http%3A%2F%2Fnpic7.edushi.com%2Fcn%2Fzixun%2Fzh-chs%2F2016-05%2F05%2Fe-2983726-s20160505114515873.jpg");
        list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1491455434&di=826f37e5f0c1357a9984a9a906a26f4c&imgtype=jpg&er=1&src=http%3A%2F%2Fpic1.win4000.com%2Fwallpaper%2F2%2F54607bc63e64b.jpg");
        list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1490860900979&di=ac742656402beb1b50fbf0cbff98a670&imgtype=0&src=http%3A%2F%2Fpic.nen.com.cn%2F0%2F16%2F53%2F57%2F16535747_697116.jpg");
        list.add(R.mipmap.pic1);
        list.add(R.mipmap.pic2);
        list.add(R.mipmap.pic3);
        layoutImage.setList(list);
        //不设置就不显示提示框
        layoutImage.setLoadingDialog(dialog);
        layoutImage.setListener(new SliderLayout.IOnClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(MainActivity.this,"第 "+(position+1)+" 张图片",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        layoutImage.stopAutoPlay();
        layoutImage.dismissDialog();
        super.onDestroy();
    }
}
