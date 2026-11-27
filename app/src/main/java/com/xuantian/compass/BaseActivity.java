package com.xuantian.compass;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends Activity {

    private final Handler handler = new Handler(Looper.getMainLooper());

    private float downX;
    private float downY;

    private int touchSlop;
    private boolean longPressHandled;
    private boolean longPressCancelled;

    private View touchedView;
    private PopupWindow copyPopup;

    private static final long LONG_PRESS_TIME = 600L;

    private final Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {

            if (longPressCancelled || longPressHandled) {
                return;
            }

            View root = getWindow().getDecorView();

            touchedView = findViewAt(
                    root,
                    (int) downX,
                    (int) downY
            );

            if (isInsideEditText(touchedView)) {
                return;
            }

            longPressHandled = true;
            showCopyMenu(downX, downY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        touchSlop = ViewConfiguration
                .get(this)
                .getScaledTouchSlop();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:

                handler.removeCallbacks(longPressRunnable);

                downX = event.getRawX();
                downY = event.getRawY();

                longPressHandled = false;
                longPressCancelled = false;

                handler.postDelayed(
                        longPressRunnable,
                        LONG_PRESS_TIME
                );

                break;

            case MotionEvent.ACTION_MOVE:

                float dx = Math.abs(event.getRawX() - downX);
                float dy = Math.abs(event.getRawY() - downY);

                if (dx > touchSlop || dy > touchSlop) {
                    cancelLongPress();
                }

                if (longPressHandled) {
                    return true;
                }

                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                handler.removeCallbacks(longPressRunnable);

                if (longPressHandled) {
                    longPressHandled = false;
                    return true;
                }

                break;
        }

        return super.dispatchTouchEvent(event);
    }

    private void cancelLongPress() {

        longPressCancelled = true;
        handler.removeCallbacks(longPressRunnable);
    }

    private View findViewAt(View view, int rawX, int rawY) {

        if (view == null || !view.isShown()) {
            return null;
        }

        Rect rect = new Rect();

        if (!view.getGlobalVisibleRect(rect)
                || !rect.contains(rawX, rawY)) {
            return null;
        }

        if (view instanceof ViewGroup) {

            ViewGroup group = (ViewGroup) view;

            for (int i = group.getChildCount() - 1; i >= 0; i--) {

                View child = findViewAt(
                        group.getChildAt(i),
                        rawX,
                        rawY
                );

                if (child != null) {
                    return child;
                }
            }
        }

        return view;
    }

    private boolean isInsideEditText(View view) {

        View current = view;

        while (current != null) {

            if (current instanceof EditText) {
                return true;
            }

            if (!(current.getParent() instanceof View)) {
                break;
            }

            current = (View) current.getParent();
        }

        return false;
    }

    private void showCopyMenu(float rawX, float rawY) {

        if (isFinishing() || isDestroyed()) {
            return;
        }

        if (copyPopup != null && copyPopup.isShowing()) {
            copyPopup.dismiss();
        }

        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setPadding(8, 8, 8, 8);
        menu.setBackgroundColor(0xFF3A2B20);

        String selectedText = getSelectedText(touchedView);

        if (selectedText != null
                && !selectedText.trim().isEmpty()) {

            addMenuItem(menu, "复制", () -> {
                CopyHelper.copyText(this, selectedText);
            });
        }

        addMenuItem(menu, "全部复制", () -> {

            String allText = collectVisibleText(
                    getWindow().getDecorView()
            );

            CopyHelper.copyText(this, allText);
        });

        addMenuItem(menu, "取消", () -> {

            if (copyPopup != null) {
                copyPopup.dismiss();
            }
        });

        copyPopup = new PopupWindow(
                menu,
                dp(116),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        copyPopup.setOutsideTouchable(true);
        copyPopup.setElevation(dp(8));

        View decor = getWindow().getDecorView();

        int[] location = new int[2];
        decor.getLocationOnScreen(location);

        int x = (int) rawX - location[0];
        int y = (int) rawY - location[1];

        copyPopup.showAtLocation(
                decor,
                Gravity.TOP | Gravity.LEFT,
                Math.max(0, x),
                Math.max(0, y)
        );
    }

    private void addMenuItem(
            LinearLayout menu,
            String title,
            Runnable action
    ) {

        TextView item = new TextView(this);

        item.setText(title);
        item.setTextSize(15);
        item.setTextColor(0xFFFFFFFF);
        item.setGravity(Gravity.CENTER);
        item.setPadding(
                dp(12),
                dp(12),
                dp(12),
                dp(12)
        );

        item.setOnClickListener(v -> {

            if (copyPopup != null) {
                copyPopup.dismiss();
            }

            action.run();
        });

        menu.addView(
                item,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );
    }

    private String getSelectedText(View view) {

        if (view instanceof TextView
                && !(view instanceof EditText)) {

            CharSequence text = ((TextView) view).getText();

            if (text != null) {
                return text.toString();
            }
        }

        return null;
   
