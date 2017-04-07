package com.liuting.sliderlayoutapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.liuting.sliderlayout.SliderLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SliderLayout layoutJingDong;//自定义自动轮播图片View
    private SliderLayout layoutTaobao;
    private List<Object> listJingDong;//图片列表
    private List<Object> listTaobao;//图片列表
    private Dialog dialog;//提示框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layoutTaobao = (SliderLayout) findViewById(R.id.main_layout_taobao);
        layoutJingDong = (SliderLayout) findViewById(R.id.main_layout_jingdong);
        dialog = new ProgressDialog(this);
        initJingDong();
    }

    /**
     * 初始化
     */
    private void initJingDong() {
        layoutJingDong.setVisibility(View.VISIBLE);
        layoutTaobao.setVisibility(View.GONE);
        listJingDong = new ArrayList<>();
        listJingDong.add("http://file02.16sucai.com/d/file/2014/0607/1902b774a2af86eb84f4bc502a71180e.jpg");
        listJingDong.add("http://img.sccnn.com/bimg/337/42282.jpg");
        listJingDong.add("http://pic.58pic.com/58pic/12/13/33/80358PIC9xp.jpg");
        listJingDong.add(R.mipmap.pic1);
        listJingDong.add(R.mipmap.pic2);
        listJingDong.add(R.mipmap.pic3);
        layoutJingDong.setList(listJingDong);
        //不设置就不显示提示框
        layoutJingDong.setLoadingDialog(dialog);
        layoutJingDong.setListener(new SliderLayout.IOnClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(MainActivity.this,"第 "+(position+1)+" 张图片",Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 初始化
     */
    private void initTaoBao(){
        layoutJingDong.setVisibility(View.GONE);
        layoutTaobao.setVisibility(View.VISIBLE);
        listTaobao = new ArrayList<>();
        listTaobao.add(R.mipmap.pic4);
        listTaobao.add(R.mipmap.pic5);
        listTaobao.add(R.mipmap.pic6);
        listTaobao.add(R.mipmap.pic7);
        listTaobao.add(R.mipmap.pic8);
        listTaobao.add(R.mipmap.pic9);
        layoutTaobao.setList(listTaobao);
        layoutTaobao.setListener(new SliderLayout.IOnClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(MainActivity.this,"第 "+(position+1)+" 张图片",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_item_jingdong://京东商城广告栏
                initJingDong();
                break;
            case R.id.menu_item_taobao://淘宝广告栏
                initTaoBao();
                break;
        }
        return true;
    }

    @Override
    protected void onStop() {
        layoutTaobao.stopAutoPlay();
        layoutJingDong.stopAutoPlay();
        layoutJingDong.dismissDialog();
        super.onStop();
    }
}
