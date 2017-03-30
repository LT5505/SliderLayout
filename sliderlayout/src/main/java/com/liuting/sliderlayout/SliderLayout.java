package com.liuting.sliderlayout;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ViewSwitcher;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

/**
 * Package:com.liuting.imageswitcher
 * author:liuting
 * Date:2017/3/29
 * Desc:自定义自动无限轮播滚动View
 */

public class SliderLayout extends RelativeLayout implements View.OnTouchListener, ViewSwitcher.ViewFactory {
    private ImageSwitcher switcherImage;//图像切换
    private int pictureIndex = 0;//图像的index
    private float touchDownX;// 左右滑动时手指按下的X坐标
    private float touchUpX;// 左右滑动时手指松开的X坐标
    private static final int START_AUTO_PLAY = 0;
    private int autoPlayDuration = 4000;
    Drawable unSelectedDrawable;//没有选中时的图片
    Drawable selectedDrawable;//选中时的图片
    private LinearLayout indicatorContainer;//指示器布局
    private Context context;//context
    private List<Object> list = new ArrayList<>();//网络图片列表
    private int itemCount;//总数
    private Boolean isAutoPlay = true;//是否循环滚动
    private Dialog loadingDialog;//提示对话框
    private IndicatorShape indicatorShape = IndicatorShape.oval;//指示器默认图形
    private IndicatorPosition indicatorPosition = IndicatorPosition.centerBottom;//默认是指示器底部居中显示
    private int unSelectedIndicatorColor = 0xffffffff;//默认不选中指示器为白色
    private int selectedIndicatorColor = 0xff0000ff;//默认选中指示器为蓝色
    private float unSelectedIndicatorHeight = getResources().getDimension(R.dimen.sl_unselected_indicator_height);//默认不选中指示器高度
    private float unSelectedIndicatorWidth = getResources().getDimension(R.dimen.sl_unselected_indicator_width);//默认不选中指示器宽度
    private float selectedIndicatorHeight = getResources().getDimension(R.dimen.sl_selected_indicator_height);//默认选中指示器高度
    private float selectedIndicatorWidth = getResources().getDimension(R.dimen.sl_selected_indicator_width);//默认选中指示器宽度
    private float indicatorSpace = getResources().getDimension(R.dimen.sl_indicator_padding);//指示器padding
    private float indicatorMargin = getResources().getDimension(R.dimen.sl_indicator_margin);//指示器间距
    private Integer defaultImage = R.drawable.ic_default;//默认图片
    private Integer errorImage = R.drawable.ic_default;//默认图片
    private IOnClickListener listener;//事件监听

    //指示器形状
    private enum IndicatorShape {
        oval,rect
    }

    //指示器显示位置
    private enum IndicatorPosition {
        centerBottom,
        rightBottom,
        leftBottom,
        centerTop,
        rightTop,
        leftTop
    }

    private Target mTarget = new Target() {

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            dismissDialog();
            //自适应高度时使用
//            int height = ((ImageView) switcherImage.getCurrentView()).getMeasuredHeight();
//            ((ImageView) switcherImage.getCurrentView()).setScaleType(ImageView.ScaleType.CENTER_CROP);
//            if (height > 0) {
//                ((ImageView) switcherImage.getCurrentView()).setLayoutParams(new ImageSwitcher.LayoutParams(ImageSwitcher.LayoutParams.MATCH_PARENT, height));
//            } else {
//                ((ImageView) switcherImage.getCurrentView()).setLayoutParams(new ImageSwitcher.LayoutParams(ImageSwitcher.LayoutParams.MATCH_PARENT, ImageSwitcher.LayoutParams.MATCH_PARENT));
//            }
            ((ImageView) switcherImage.getCurrentView()).setScaleType(ImageView.ScaleType.CENTER_CROP);
            ((ImageView) switcherImage.getCurrentView()).setLayoutParams(new ImageSwitcher.LayoutParams(ImageSwitcher.LayoutParams.MATCH_PARENT, ImageSwitcher.LayoutParams.MATCH_PARENT));
            ((ImageView) switcherImage.getCurrentView()).setImageBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            dismissDialog();
            ((ImageView) switcherImage.getCurrentView()).setImageDrawable(errorDrawable);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            showDialog();
        }
    };

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case START_AUTO_PLAY:
                    SliderRightToLeft();
                    handler.sendEmptyMessageDelayed(START_AUTO_PLAY, autoPlayDuration);
                    break;
            }
            return false;
        }
    });

    public SliderLayout(Context context) {
        super(context);
        this.context = context;
        initView(context, null, 0);
    }

    public SliderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView(context, attrs, 0);
    }

    public SliderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(context, attrs, defStyleAttr);
    }

    /**
     * 初始化
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SliderLayout, defStyleAttr, 0);
        if (array != null) {
            isAutoPlay = array.getBoolean(R.styleable.SliderLayout_sl_is_auto_play, isAutoPlay);
            //获取底部指示器的形状
            int intShape = array.getInt(R.styleable.SliderLayout_sl_indicator_shape, indicatorShape.ordinal());
            for (IndicatorShape shape : IndicatorShape.values()) {
                if (shape.ordinal() == intShape) {
                    indicatorShape = shape;
                    break;
                }
            }
            //获取底部指示器的位置
            int intPosition = array.getInt(R.styleable.SliderLayout_sl_indicator_position, IndicatorPosition.centerBottom.ordinal());
            for (IndicatorPosition position : IndicatorPosition.values()) {
                if (position.ordinal() == intPosition) {
                    indicatorPosition = position;
                    break;
                }
            }
            unSelectedIndicatorColor = array.getColor(R.styleable.SliderLayout_sl_unselected_indicator_color, unSelectedIndicatorColor);
            selectedIndicatorColor = array.getColor(R.styleable.SliderLayout_sl_selected_indicator_color, selectedIndicatorColor);
            unSelectedIndicatorHeight = array.getDimension(R.styleable.SliderLayout_sl_unselected_indicator_height, unSelectedIndicatorHeight);
            unSelectedIndicatorWidth = array.getDimension(R.styleable.SliderLayout_sl_unselected_indicator_width, unSelectedIndicatorWidth);
            selectedIndicatorHeight = array.getDimension(R.styleable.SliderLayout_sl_selected_indicator_height, selectedIndicatorHeight);
            selectedIndicatorWidth = array.getDimension(R.styleable.SliderLayout_sl_selected_indicator_width, selectedIndicatorWidth);
            indicatorSpace = array.getDimension(R.styleable.SliderLayout_sl_indicator_space, indicatorSpace);
            indicatorMargin = array.getDimension(R.styleable.SliderLayout_sl_indicator_margin, indicatorMargin);
            autoPlayDuration = array.getInt(R.styleable.SliderLayout_sl_auto_play_duration, autoPlayDuration);
            defaultImage = array.getResourceId(R.styleable.SliderLayout_sl_default_image, defaultImage);
            errorImage = array.getResourceId(R.styleable.SliderLayout_sl_error_image, errorImage);
        }

        //绘制未选中状态图形
        LayerDrawable unSelectedLayerDrawable;
        LayerDrawable selectedLayerDrawable;
        GradientDrawable unSelectedGradientDrawable;
        unSelectedGradientDrawable = new GradientDrawable();

        //绘制选中状态图形
        GradientDrawable selectedGradientDrawable;
        selectedGradientDrawable = new GradientDrawable();
        switch (indicatorShape) {
            case rect:
                unSelectedGradientDrawable.setShape(GradientDrawable.RECTANGLE);
                selectedGradientDrawable.setShape(GradientDrawable.RECTANGLE);
                break;
            case oval:
                unSelectedGradientDrawable.setShape(GradientDrawable.OVAL);
                selectedGradientDrawable.setShape(GradientDrawable.OVAL);
                break;
        }
        unSelectedGradientDrawable.setColor(unSelectedIndicatorColor);
        unSelectedGradientDrawable.setSize((int) unSelectedIndicatorWidth, (int) unSelectedIndicatorHeight);
        unSelectedLayerDrawable = new LayerDrawable(new Drawable[]{unSelectedGradientDrawable});
        unSelectedDrawable = unSelectedLayerDrawable;

        selectedGradientDrawable.setColor(selectedIndicatorColor);
        selectedGradientDrawable.setSize((int) selectedIndicatorWidth, (int) selectedIndicatorHeight);
        selectedLayerDrawable = new LayerDrawable(new Drawable[]{selectedGradientDrawable});
        selectedDrawable = selectedLayerDrawable;
    }

    /**
     * 设置图片列表
     *
     * @param list 图片列表
     */
    public void setList(List<Object> list) {
        this.list = list;
        itemCount = this.list.size();
        if (itemCount == 0) {
            throw new IllegalStateException("item count not equal zero");
        }
        initSlider();
    }

    public void setListener(IOnClickListener listener) {
        this.listener = listener;
    }

    /**
     * 设置网络图片加载时的提示框样式
     *
     * @param loadingDialog
     */
    public void setLoadingDialog(Dialog loadingDialog) {
        this.loadingDialog = loadingDialog;
    }

    /**
     * 初始化滑动
     */
    private void initSlider() {
        switcherImage = new ImageSwitcher(context);
        switcherImage.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        //设置视图工厂
        switcherImage.setFactory(this);
        switcherImage.setOnTouchListener(this);
        addView(switcherImage);
        indicatorContainer = new LinearLayout(context);
        indicatorContainer.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        switch (indicatorPosition) {
            case centerBottom:
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                break;
            case centerTop:
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                break;
            case leftBottom:
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                break;
            case leftTop:
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                break;
            case rightBottom:
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                break;
            case rightTop:
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                break;
        }

        //设置margin
        params.setMargins((int) (indicatorMargin), (int) (indicatorMargin), (int) (indicatorMargin), (int) (indicatorMargin));
//        //添加指示器容器布局到SliderLayout
        addView(indicatorContainer, params);
        //初始化指示器，并添加到指示器容器布局
        for (int i = 0; i < itemCount; i++) {
            ImageView indicator = new ImageView(context);
            indicator.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            indicator.setPadding((int) (indicatorSpace), (int) (indicatorSpace), (int) (indicatorSpace), (int) (indicatorSpace));
            indicator.setImageDrawable(unSelectedDrawable);
            indicatorContainer.addView(indicator);
            final int finalI = i;
            indicator.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    stopAutoPlay();
                    switchIndicator(finalI);
                    pictureIndex = finalI;
                    handler.sendEmptyMessageDelayed(START_AUTO_PLAY,autoPlayDuration);
                }
            });
        }
        switchIndicator(0);
        startAutoPlay();
    }

    /**
     * 开始滚动
     */
    private void startAutoPlay() {
        if (isAutoPlay) {
            handler.sendEmptyMessageDelayed(START_AUTO_PLAY, autoPlayDuration);
        }
    }

    /**
     * 停止滚动
     */
    public void stopAutoPlay() {
        if (isAutoPlay) {
            handler.removeMessages(START_AUTO_PLAY);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //如果图片需要自适应高度的话，可使用该方法重绘高度，使得图片高度为原始图片的高度
//        if (switcherImage != null) {
//            heightMeasureSpec = switcherImage.getMeasuredHeight();
//        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // 取得左右滑动时手指按下的X坐标
            touchDownX = event.getX();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            // 取得左右滑动时手指松开的X坐标
            touchUpX = event.getX();
            // 从左往右，看前一张
            if (touchUpX - touchDownX > 100) {
                stopAutoPlay();
                SliderLeftToRight();
                handler.sendEmptyMessageDelayed(START_AUTO_PLAY,autoPlayDuration);
                // 从右往左，看下一张
            } else if (touchDownX - touchUpX > 100) {
                stopAutoPlay();
                SliderRightToLeft();
                handler.sendEmptyMessageDelayed(START_AUTO_PLAY,autoPlayDuration);
                //相应点击事件
            } else if (0==(Math.abs(touchUpX - touchDownX))||(Math.abs(touchUpX - touchDownX))<50) {
                if (listener != null) {
                    stopAutoPlay();
                    listener.onItemClick(view, pictureIndex);
                    handler.sendEmptyMessageDelayed(START_AUTO_PLAY,autoPlayDuration);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 从左往右，看前一张
     */
    private void SliderLeftToRight() {
        // 取得当前要看的图片的index
        pictureIndex = pictureIndex == 0 ? itemCount - 1
                : pictureIndex - 1;
        // 设置图片切换的动画
        switcherImage.setInAnimation(AnimationUtils.loadAnimation(context,
                android.R.anim.slide_in_left));
        switcherImage.setOutAnimation(AnimationUtils.loadAnimation(context,
                android.R.anim.slide_out_right));
        // 设置当前要看的图片
//        switcherImage.setImageResource(image[pictureIndex]);
        switchIndicator(pictureIndex);
    }

    /**
     * 加载图片
     *
     * @param pictureIndex index
     */
    private void loadImage(int pictureIndex) {
        if (list.get(pictureIndex) instanceof String) {
            loadNetImage(pictureIndex);
        } else if (list.get(pictureIndex) instanceof Integer) {
            loadFileImage(pictureIndex);
        }
    }

    /**
     * 加载网络图片，图片自适应高度问题有待调整
     *
     * @param pictureIndex index
     */
    private void loadNetImage(int pictureIndex) {
        if (list != null && list.size() != 0) {
            Picasso.with(context)
                    .load((String) list.get(pictureIndex))
                    .placeholder(defaultImage)
                    .error(errorImage)
                    .tag(context)
                    .into(mTarget);
        }
    }

    /**
     * 加载本地图片，图片自适应高度问题有待调整
     *
     * @param pictureIndex index
     */
    private void loadFileImage(int pictureIndex) {
        if (list != null && list.size() != 0) {
            switcherImage.setImageResource((Integer) list.get(pictureIndex));
        }
    }

    /**
     * 从右往左，看下一张
     */
    private void SliderRightToLeft() {
        // 取得当前要看的图片的index
        pictureIndex = pictureIndex == itemCount - 1 ? 0
                : pictureIndex + 1;
        // 设置图片切换的动画
        // 由于Android没有提供slide_out_left和slide_in_right，所以仿照slide_in_left和slide_out_right编写了slide_out_left和slide_in_right
        switcherImage.setInAnimation(AnimationUtils.loadAnimation(context,
                R.anim.slide_in_right));
        switcherImage.setOutAnimation(AnimationUtils.loadAnimation(context,
                R.anim.slide_out_left));
//                switcherImage.setInAnimation(AnimationUtils.loadAnimation(this,android.R.anim.fade_in));
//                switcherImage.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
        // 设置当前要看的图片
//        switcherImage.setImageResource(image[pictureIndex]);
        switchIndicator(pictureIndex);
    }

    /**
     * 切换指示器状态
     *
     * @param index 当前位置
     */
    private void switchIndicator(int index) {
        for (int i = 0; i < indicatorContainer.getChildCount(); i++) {
            ((ImageView) indicatorContainer.getChildAt(i)).setImageDrawable(i == index ? selectedDrawable : unSelectedDrawable);
        }
        loadImage(index);
    }

    @Override
    public View makeView() {
        ImageView imageView = new ImageView(context);
        imageView.setBackgroundColor(0xFFFFFFFF);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new ImageSwitcher.LayoutParams(ImageSwitcher.LayoutParams.MATCH_PARENT, ImageSwitcher.LayoutParams.MATCH_PARENT));
//                if(list.get(pictureIndex) instanceof String){
//                    Picasso.with(context)
//                .load((String) list.get(pictureIndex))
//                .placeholder(R.mipmap.ic_default)
//                .error(R.mipmap.ic_default)
//                .tag(context)
//                .fit()
//                .into(imageView);
//                }else if(list.get(pictureIndex) instanceof Integer){
//                    imageView.setImageResource((Integer) list.get(pictureIndex));
//                }
        return imageView;
    }

    /**
     * show dialog
     */
    public void showDialog() {
        if (loadingDialog != null && !loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    /**
     * dismiss dialog
     */
    public void dismissDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    public interface IOnClickListener {
        void onItemClick(View view, int position);
    }

}
