package com.xuantian.compass;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;

import java.util.Locale;

public class ResidenceDirectionActivity extends Activity {

private EditText angleInput;
private TextView resultText;
private TextView directionText;
private CompassDialView compassView;

private final int gold = Color.rgb(215, 175, 95);
private final int background = Color.rgb(20, 17, 14);
private final int panel = Color.rgb(42, 35, 28);
private final int white = Color.WHITE;
private final int gray = Color.LTGRAY;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getWindow().setStatusBarColor(background);
    getWindow().setNavigationBarColor(background);

    createPage();
}

private void createPage() {

    ScrollView scrollView = new ScrollView(this);
    scrollView.setFillViewport(true);
    scrollView.setBackgroundColor(background);

    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(dp(18), dp(18), dp(18), dp(28));

    scrollView.addView(root);

    TextView title = new TextView(this);
    title.setText("玄天罗盘");
    title.setTextColor(gold);
    title.setTextSize(26);
    title.setTypeface(null, Typeface.BOLD);
    title.setGravity(Gravity.CENTER);

    root.addView(title, params(-1, 55));

    TextView subtitle = new TextView(this);
    subtitle.setText("传统二十四山 · 住宅坐向测量");
    subtitle.setTextColor(gray);
    subtitle.setTextSize(14);
    subtitle.setGravity(Gravity.CENTER);

    root.addView(subtitle, params(-1, 35));

    TextView status = new TextView(this);
    status.setText("当前模式：手动角度测试");
    status.setTextColor(gold);
    status.setTextSize(13);
    status.setGravity(Gravity.CENTER);
    status.setPadding(0, dp(8), 0, dp(8));

    root.addView(status, params(-1, 35));

    // 传统圆形二十四山罗盘
    compassView = new CompassDialView();

    LinearLayout.LayoutParams compassParams =
            params(-1, -2);

    compassParams.topMargin = dp(8);
    compassParams.bottomMargin = dp(18);

    root.addView(compassView, compassParams);

    TextView inputTitle = new TextView(this);
    inputTitle.setText("输入方位角度");
    inputTitle.setTextColor(gold);
    inputTitle.setTextSize(17);
    inputTitle.setTypeface(null, Typeface.BOLD);

    root.addView(inputTitle, params(-1, 38));

    angleInput = new EditText(this);
    angleInput.setSingleLine(true);
    angleInput.setInputType(
            InputType.TYPE_CLASS_NUMBER
                    | InputType.TYPE_NUMBER_FLAG_DECIMAL
                    | InputType.TYPE_NUMBER_FLAG_SIGNED
    );
    angleInput.setHint("例如：186.5");
    angleInput.setHintTextColor(Color.GRAY);
    angleInput.setTextColor(white);
    angleInput.setTextSize(19);
    angleInput.setGravity(Gravity.CENTER_VERTICAL);
    angleInput.setPadding(dp(14), dp(8), dp(14), dp(8));
    angleInput.setBackgroundColor(panel);

    root.addView(angleInput, params(-1, 54));

    resultText = new TextView(this);
    resultText.setText("等待输入角度");
    resultText.setTextColor(gold);
    resultText.setTextSize(23);
    resultText.setTypeface(null, Typeface.BOLD);
    resultText.setGravity(Gravity.CENTER);
    resultText.setPadding(dp(8), dp(18), dp(8), dp(18));

    root.addView(resultText, params(-1, -2));

    directionText = new TextView(this);
    directionText.setTextColor(white);
    directionText.setTextSize(15);
    directionText.setLineSpacing(dp(6), 1f);
    directionText.setPadding(dp(14), dp(15), dp(14), dp(15));
    directionText.setBackgroundColor(panel);

    root.addView(directionText, params(-1, -2));

    TextView note = new TextView(this);
    note.setText(
            "测量说明：\n\n" +
            "1. 0°代表正北，角度顺时针增加。\n" +
            "2. 每一山占15°，共二十四山。\n" +
            "3. 当前输入角度暂按住宅坐山方向处理。\n" +
            "4. 朝向取坐山相反方向180°。\n" +
            "5. 本页面目前尚未接入手机实时传感器。"
    );
    note.setTextColor(gray);
    note.setTextSize(13);
    note.setLineSpacing(dp(5), 1f);
    note.setPadding(dp(8), dp(18), dp(8), dp(18));

    root.addView(note, params(-1, -2));

    TextView backButton = new TextView(this);
    backButton.setText("返回住宅风水分析");
    backButton.setTextColor(Color.BLACK);
    backButton.setTextSize(16);
    backButton.setTypeface(null, Typeface.BOLD);
    backButton.setGravity(Gravity.CENTER);
    backButton.setBackgroundColor(gold);

    root.addView(backButton, params(-1, 52));

    backButton.setOnClickListener(v -> finish());

    angleInput.addTextChangedListener(new TextWatcher() {

        @Override
        public void beforeTextChanged(
                CharSequence s,
                int start,
                int count,
                int after) {
        }

        @Override
        public void onTextChanged(
                CharSequence s,
                int start,
                int before,
                int count) {

            calculateDirection(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    });

    setContentView(scrollView);
}

private void calculateDirection(String input) {

    if (input == null || input.trim().isEmpty()) {

        resultText.setText("等待输入角度");
        directionText.setText("请输入住宅坐山测量角度。");

        compassView.setSelectedAngle(0);

        return;
    }

    try {

        double angle = Double.parseDouble(input.trim());

        if (Double.isNaN(angle)
                || Double.isInfinite(angle)
                || angle < 0
                || angle >= 360) {

            resultText.setText("角度超出范围");
            directionText.setText(
                    "请输入大于或等于0°且小于360°的角度。"
            );

            return;
        }

        double sittingAngle =
                TwentyFourMountains.normalizeAngle(angle);

        double facingAngle =
                TwentyFourMountains.normalizeAngle(
                        sittingAngle + 180
                );

        String sittingMountain =
                TwentyFourMountains.getMountainName(
                        sittingAngle
                );

        String facingMountain =
                TwentyFourMountains.getMountainName(
                        facingAngle
                );

        resultText.setText(
                sittingMountain + "山坐 · "
                        + facingMountain + "山向"
        );

        directionText.setText(
                String.format(
                        Locale.CHINA,
                        "坐山角度：%.2f°\n" +
                        "坐山：%s山\n\n" +
                        "朝向角度：%.2f°\n" +
                        "朝向：%s山",
                        sittingAngle,
                        sittingMountain,
                        facingAngle,
                        facingMountain
                )
        );

        compassView.setSelectedAngle(sittingAngle);

    } catch (NumberFormatException e) {

        resultText.setText("输入格式不正确");
        directionText.setText("请输入正确的数字角度。");
    }
}

private LinearLayout.LayoutParams params(
        int width,
        int height) {

    return new LinearLayout.LayoutParams(
            width > 0 ? dp(width) : width,
            height > 0 ? dp(height) : height
    );
}

private int dp(float value) {

    return (int) (
            value * getResources()
                    .getDisplayMetrics().density + 0.5f
    );
}

/**
 * 传统二十四山圆形罗盘绘制
 */
private class CompassDialView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final String[] mountains = {
            "子", "癸", "丑",
            "艮", "寅", "甲",
            "卯", "乙", "辰",
            "巽", "巳", "丙",
            "午", "丁", "未",
            "坤", "申", "庚",
            "酉", "辛", "戌",
            "乾", "亥", "壬"
    };

    private final String[] directions = {
            "北", "东北", "东", "东南",
            "南", "西南", "西", "西北"
    };

    private double selectedAngle = 0;

    CompassDialView() {
        super(ResidenceDirectionActivity.this);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    void setSelectedAngle(double angle) {
        selectedAngle = angle;
        invalidate();
    }

    @Override
    protected void onMeasure(
            int widthMeasureSpec,
            int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);

        if (width == 0) {
            width = dp(320);
        }

        int height = width;

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        float cx = width / 2f;
        float cy = height / 2f;

        float radius = Math.min(width, height) * 0.46f;

        // 罗盘背景
        paint.setColor(Color.rgb(30, 25, 20));
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(cx, cy, radius, paint);

        // 外圈
        paint.setColor(gold);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));

        canvas.drawCircle(cx, cy, radius, paint);

        // 内圈
        canvas.drawCircle(
                cx,
                cy,
                radius * 0.84f,
                paint
        );

        canvas.drawCircle(
                cx,
                cy,
                radius * 0.53f,
                paint
        );

        canvas.drawCircle(
                cx,
                cy,
                radius * 0.31f,
                paint
        );

        // 中央圆形
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(52, 39, 25));

        canvas.drawCircle(
                cx,
                cy,
                radius * 0.29f,
                paint
        );

        // 绘制24山分隔线和文字
        for (int i = 0; i < 24; i++) {

            double angle =
                    Math.toRadians(i * 15.0 - 90);

            float startRadius = radius * 0.53f;
            float endRadius = radius * 0.84f;

            float x1 = cx
                    + (float) Math.cos(angle) * startRadius;

            float y1 = cy
                    + (float) Math.sin(angle) * startRadius;

            float x2 = cx
                    + (float) Math.cos(angle) * endRadius;

            float y2 = cy
                    + (float) Math.sin(angle) * endRadius;

            paint.setColor(gold);
            paint.setStrokeWidth(dp(1));

            canvas.drawLine(x1, y1, x2, y2, paint);

            float textRadius = radius * 0.68f;

            float tx = cx
                    + (float) Math.cos(angle) * textRadius;

            float ty = cy
                    + (float) Math.sin(angle) * textRadius;

            paint.setStyle(Paint.Style.FILL);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextSize(dp(14));
            paint.setTextAlign(Paint.Align.CENTER);

            paint.setColor(
                    mountains[i].equals(
                            TwentyFourMountains.getMountainName(
                                    selectedAngle
                            )
                    ) ? Color.YELLOW : gold
            );

            canvas.drawText(
                    mountains[i],
                    tx,
                    ty + dp(5),
                    paint
            );
        }

        // 八方方位
        for (int i = 0; i < 8; i++) {

            double angle =
                    Math.toRadians(i * 45.0 - 90);

            float textRadius = radius * 0.91f;

            float tx = cx
                    + (float) Math.cos(angle) * textRadius;

            float ty = cy
                    + (float) Math.sin(angle) * textRadius;

            paint.setColor(white);
            paint.setTextSize(dp(12));
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextAlign(Paint.Align.CENTER);

            canvas.drawText(
                    directions[i],
                    tx,
                    ty + dp(4),
                    paint
            );
        }

        // 正北标记
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);

        float northTop = cy - radius * 0.98f;

        PathHelper.drawTriangle(
                canvas,
                paint,
                cx,
                northTop,
                dp(9)
        );

        // 中央十字线
        paint.setColor(gold);
        paint.setStrokeWidth(dp(1.5f));

        canvas.drawLine(
                cx - radius * 0.23f,
                cy,
                cx + radius * 0.23f,
                cy,
                paint
        );

        canvas.drawLine(
                cx,
                cy - radius * 0.23f,
                cx,
                cy + radius * 0.23f,
                paint
        );

        // 中心文字
        paint.setColor(white);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(dp(14));
        paint.setTypeface(Typeface.DEFAULT_BOLD);

        canvas.drawText(
                "玄天",
                cx,
                cy - dp(5),
                paint
        );

        paint.setTextSize(dp(11));

        canvas.drawText(
                "罗盘",
                cx,
                cy + dp(15),
                paint
        );

        // 恢复绘制状态
        paint.setStyle(Paint.Style.FILL);
    }
}

/**
 * 三角形绘制辅助
 */
private static class PathHelper {

    static void drawTriangle(
            Canvas canvas,
            Paint paint,
            float centerX,
            float topY,
            float size) {

        android.graphics.Path path =
                new android.graphics.Path();

        path.moveTo(centerX, topY);
        path.lineTo(centerX - size, topY + size * 1.6f);
        path.lineTo(centerX + size, topY + size * 1.6f);
        path.close();

        canvas.drawPath(path, paint);
    }
}

}
