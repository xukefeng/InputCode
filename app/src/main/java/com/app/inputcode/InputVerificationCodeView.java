package com.app.inputcode;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;

public class InputVerificationCodeView extends View {

    /**
     * 验证码的长度
     */
    private int codeLength;
    /**
     * 线之间的间隙
     */
    private int linePadding;

    /**
     * 画验证码的画笔
     */
    private Paint mCodePaint;
    /**
     * 画验证码下划线的画笔
     */
    private Paint mCodeLinePaint;
    /**
     * 控件的宽
     */
    private int width;
    /**
     * 控件的高
     */
    private int height;

    /**
     * 验证码字体颜色
     */
    private int textColor;
    /**
     * 验证码大小
     */
    private int textSize;
    /**
     * 未输入下划线的颜色
     */
    private int unLineColor;

    /**
     * 选择下滑线的颜色
     */
    private int lineColor;
    /**
     * 下划线的宽度
     */
    private int lineWidth;
    /**
     * 下划线的高度
     */
    private int lineHeight;

    /**
     * 输入到第几位验证码的长度
     */
    private int codePosition = 0;

    /**
     * 当输入完验证码后是否自动执行回调
     */
    private int autoExecution;

    /**
     * 保存验证码的容器
     */
    private ArrayList<String> mTextCodes = new ArrayList<>(codeLength);

    /**
     * 键盘管理器
     */
    private InputMethodManager inputManager;

    private OnInputCodeListener onInputCodeListener;

    public void setOnInputCodeListener(OnInputCodeListener onInputCodeListener) {
        this.onInputCodeListener = onInputCodeListener;
    }

    public InputVerificationCodeView(Context context) {
        this(context, null);
    }

    public InputVerificationCodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InputVerificationCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.InputVerificationCodeView);
        this.textColor = array.getColor(R.styleable.InputVerificationCodeView_textCodeColor, Color.BLACK);
        this.textSize = (int) array.getDimension(R.styleable.InputVerificationCodeView_textCodeSize, 30);
        this.linePadding = (int) array.getDimension(R.styleable.InputVerificationCodeView_linePadding, 40);
        this.lineHeight = (int) array.getDimension(R.styleable.InputVerificationCodeView_lineHeight, 8);
        this.lineColor = array.getColor(R.styleable.InputVerificationCodeView_lineCodeColor, Color.BLACK);
        this.unLineColor = array.getColor(R.styleable.InputVerificationCodeView_unlineCodeColor, Color.GRAY);
        this.codeLength = array.getInt(R.styleable.InputVerificationCodeView_textCodeLength, 4);
        this.autoExecution = array.getInt(R.styleable.InputVerificationCodeView_autoExecution, 0);
        array.recycle();
        init();
    }

    private void init() {
        mCodePaint = new Paint();
        mCodeLinePaint = new Paint();

        mCodePaint.setAntiAlias(true);
        mCodePaint.setColor(textColor);
        mCodePaint.setTextSize(textSize);
        mCodePaint.setStrokeWidth(8);

        mCodeLinePaint.setAntiAlias(true);
        mCodeLinePaint.setColor(unLineColor);
        mCodeLinePaint.setStyle(Paint.Style.FILL);

        setFocusableInTouchMode(true);
        setOnKeyListener(new KeyListener());
        inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER; //输入类型为数字
        return super.onCreateInputConnection(outAttrs);
    }

    /**
     * 保存状态
     *
     * @return
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putStringArrayList("password", mTextCodes);
        bundle.putInt("cursorPosition", codePosition);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mTextCodes = bundle.getStringArrayList("password");
            codePosition = bundle.getInt("cursorPosition");
            state = bundle.getParcelable("superState");
        }
        super.onRestoreInstanceState(state);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w - getPaddingLeft() - getPaddingRight();
        this.height = h;
        this.lineWidth = (this.width - linePadding * (codeLength - 1)) / codeLength;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawText(canvas);
        drawLine(canvas);
    }

    /**
     * 画下划线
     */
    private void drawLine(Canvas canvas) {
        int y = this.height - lineHeight;
        int x = 0;
        for (int i = 0; i < codeLength; i++) {
            if (i < codePosition && mTextCodes.size() > 0) {
                mCodeLinePaint.setColor(lineColor);
            } else {
                mCodeLinePaint.setColor(unLineColor);
            }
            RectF f = new RectF(x, y, x + lineWidth, this.height);
            canvas.drawRect(f, mCodeLinePaint);
            x = (int) (f.right + linePadding);
        }
    }

    /**
     * 画字体
     */
    private void drawText(Canvas canvas) {
        if (mTextCodes.size() <= 0)
            return;
        int y;
        int x;
        this.codePosition = mTextCodes.size();
        for (int i = 0; i < mTextCodes.size(); i++) {
            Rect rect = new Rect();
            mCodePaint.getTextBounds(mTextCodes.get(i), 0, mTextCodes.get(i).length(), rect);
            int textHeight = rect.height();
            int textWidth = rect.width();
            y = (this.height - lineHeight) / 2;
            x = (int) (((i + 0.5) * lineWidth - textWidth / 2) + i * linePadding);
            canvas.drawText(mTextCodes.get(i), x, y, mCodePaint);
        }
    }

    class KeyListener implements OnKeyListener {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            int action = event.getAction();
            if (action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {//删除操作
                    if (mTextCodes.size() > 0) {
                        mTextCodes.remove(mTextCodes.size() - 1);
                        codePosition = mTextCodes.size();
                        invalidate();
                        return true;
                    } else {
                        return true;
                    }

                }

                if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                    //只允许输入0-9的数字
                    if (mTextCodes.size() >= codeLength) {
                        return true;
                    }
                    mTextCodes.add((keyCode - 7) + "");
                    codePosition = mTextCodes.size();
                    if (mTextCodes.size() >= codeLength) {
                        if (autoExecution == 1) {
                            if (onInputCodeListener != null) {
                                StringBuffer sb = new StringBuffer();
                                for (int i = 0; i < mTextCodes.size(); i++) {
                                    sb.append(mTextCodes.get(i));
                                }
                                onInputCodeListener.onInputCode(sb.toString());
                            }
                        }
                    }
                    invalidate();

                }

                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    //确认按钮,点击关闭软键盘
                    inputManager.hideSoftInputFromWindow(getWindowToken(), 0);
                }
            }
            return false;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            requestFocus();
            inputManager.showSoftInput(this, InputMethodManager.SHOW_FORCED);
            return true;
        }
        return true;
    }

    /**
     * 获取验证码
     *
     * @return
     */
    public String getVerifyCode() {
        if (mTextCodes.size() < codeLength) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mTextCodes.size(); i++) {
            sb.append(mTextCodes.get(i));
        }

        return sb.toString();
    }

    /**
     * 重置
     */
    public void resetView() {
        if (mTextCodes != null)
            mTextCodes.clear();
        codePosition = 0;
        invalidate();
    }

    public interface OnInputCodeListener {
        void onInputCode(String code);
    }
}
