package com.xuantian.compass;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.inputmethod.InputMethodManager;

import java.util.Locale;

public class ResidenceDirectionActivity extends Activity
        implements SensorEventListener {

    private EditText angleInput;
    private TextView resultText;
    private TextView directionText;
    private TextView sensorStatus;
    private CompassDialView compassView;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magneticSensor;

    private final float[] gravityValues = new float[3];
    private final float[] magneticValues = new float[3];

    private boolean hasGravity = false;
    private boolean hasMagnetic = false;
    private boolean sensorStarted = false;

    private double currentAngle = 0;
    private boolean manualMode = false;

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

        sensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);

        magneticSensor = sensorManager.getDefaultSensor(
                Sensor.TYPE_MAGNETIC_FIELD);

        createPage();
        checkSensors();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startSensors();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSensors();
    }

    private void checkSensors() {
        if (accelerometer == null || magneticSensor == null) {
            sensorStatus.setText("传感器不可用：无法进行实时指南针测量");
            sensorStatus.setTextColor(Color.RED);
        } else {
            sensorStatus.setText("传感器已就绪，等待方向数据……");
            sensorStatus.setTextColor(gold);
        }
    }

    private void startSensors() {
        if (sensorManager == null
                || accelerometer == null
                || magneticSensor == null) {
            return;
        }

        sensorManager.registerListener(
                this, accelerometer, SensorManager.SENSOR_DELAY_UI);

        sensorManager.registerListener(
                this, magneticSensor, SensorManager.SENSOR_DELAY_UI);

        sensorStarted = true;
    }

    private void stopSensors() {
        if (sensorManager != null && sensorStarted) {
            sensorManager.unregisterListener(this);
        }
        sensorStarted = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            lowPassFilter(event.values, gravityValues, !hasGravity);
            hasGravity = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            lowPassFilter(event.values, magneticValues, !hasMagnetic);
            hasMagnetic = true;
        }

        if (hasGravity && hasMagnetic && !manualMode) {
            calculateSensorAngle();
        }
    }

    private void lowPassFilter(
            float[] input,
            float[] output,
            boolean firstReading) {

        final float alpha = 0.15f;

        for (int i = 0; i < 3; i++) {
            if (firstReading) {
                output[i] = input[i];
            } else {
                output[i] = alpha * input[i]
                        + (1 - alpha) * output[i];
            }
        }
    }

    private void calculateSensorAngle() {
        float[] rotationMatrix = new float[9];
        float[] orientation = new float[3];

        boolean success = SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                gravityValues,
                magneticValues);

        if (!success) {
            sensorStatus.setText("方向计算失败，请远离磁性干扰并重新校准");
            sensorStatus.setTextColor(Color.RED);
            return;
        }

        SensorManager.getOrientation(rotationMatrix, orientation);

        double azimuth = Math.toDegrees(orientation[0]);
        azimuth = (azimuth + 360.0) % 360.0;

        currentAngle = azimuth;

        sensorStatus.setText("实时指南针：传感器工作正常");
        sensorStatus.setTextColor(gold);

        updateDirectionDisplay(currentAngle);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            sensorStatus.setText("磁场精度较低，请校准手机指南针");
            sensorStatus.setTextColor(Color.RED);
        } else if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
            sensorStatus.setText("指南针精度较低，建议进行校准");
            sensorStatus.setTextColor(Color.YELLOW);
        }
    }

    private void createPage() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(background);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(14), dp(12), dp(24));

        scrollView.addView(root);

        TextView title = new TextView(this);
        title.setText("玄天罗盘");
        title.setTextColor(gold);
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, params(-1, 52));

        TextView subtitle = new TextView(this);
        subtitle.setText("传统综合罗盘 · 九层方位盘");
        subtitle.setTextColor(gray);
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle, params(-1, 32));

        sensorStatus = new TextView(this);
        sensorStatus.setText("正在检查手机传感器……");
        sensorStatus.setTextColor(gold);
        sensorStatus.setTextSize(13);
        sensorStatus.setGravity(Gravity.CENTER);
        sensorStatus.setPadding(dp(4), dp(6), dp(4), dp(6));
        root.addView(sensorStatus, params(-1, 40));

        compassView = new CompassDialView();

        LinearLayout.LayoutParams compassParams = params(-1, -2);
        compassParams.topMargin = dp(4);
        compassParams.bottomMargin = dp(12);
        root.addView(compassView, compassParams);

        TextView inputTitle = new TextView(this);
        inputTitle.setText("手动输入角度（可选）");
        inputTitle.setTextColor(gold);
        inputTitle.setTextSize(17);
        inputTitle.setTypeface(null, Typeface.BOLD);
        root.addView(inputTitle, params(-1, 36));

        angleInput = new EditText(this);
        angleInput.setSingleLine(true);
        angleInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
                        | InputType.TYPE_NUMBER_FLAG_SIGNED);
        angleInput.setHint("例如：186.5");
        angleInput.setHintTextColor(Color.GRAY);
        angleInput.setTextColor(white);
        angleInput.setTextSize(18);
        angleInput.setGravity(Gravity.CENTER_VERTICAL);
        angleInput.setPadding(dp(12), dp(8), dp(12), dp(8));
        angleInput.setBackgroundColor(panel);
        root.addView(angleInput, params(-1, 52));

        resultText = new TextView(this);
        resultText.setText("等待传感器数据");
        resultText.setTextColor(gold);
        resultText.setTextSize(22);
        resultText.setTypeface(null, Typeface.BOLD);
        resultText.setGravity(Gravity.CENTER);
        resultText.setPadding(dp(6), dp(14), dp(6), dp(14));
        root.addView(resultText, params(-1, -2));

        directionText = new TextView(this);
        directionText.setTextColor(white);
        directionText.setTextSize(15);
        directionText.setLineSpacing(dp(5), 1f);
        directionText.setPadding(dp(12), dp(12), dp(12), dp(12));
        directionText.setBackgroundColor(panel);
        root.addView(directionText, params(-1, -2));

        TextView resetButton = createButton(
                "恢复实时指南针", panel, gold);
        root.addView(resetButton, params(-1, 48));

        resetButton.setOnClickListener(v -> {
            manualMode = false;
            angleInput.setText("");
            hideKeyboard();

            sensorStatus.setText("已恢复实时指南针模式");
            sensorStatus.setTextColor(gold);

            if (hasGravity && hasMagnetic) {
                calculateSensorAngle();
            }
        });

        TextView note = new TextView(this);
        note.setText(
                "罗盘使用说明：\n\n" +
                "1. 手机顶部所指方向为当前测量方向。\n" +
                "2. 0°代表磁北，角度顺时针增加。\n" +
                "3. 红色磁针端代表磁北，白色端代表磁南。\n" +
                "4. 二十四山每山15°，用于基础方位识别。\n" +
                "5. 六十甲子圈为分区展示，不代表完整择日盘式。\n" +
                "6. 十二长生、二十八宿及九星目前为基础展示，尚未完成流派精确排布。\n" +
                "7. 本页尚未校正磁偏角，不能直接代替专业住宅坐向测量。"
        );
        note.setTextColor(gray);
        note.setTextSize(13);
        note.setLineSpacing(dp(5), 1f);
        note.setPadding(dp(6), dp(14), dp(6), dp(14));
        root.addView(note, params(-1, -2));

        TextView backButton = createButton(
                "返回住宅风水分析", gold, Color.BLACK);
        root.addView(backButton, params(-1, 50));
        backButton.setOnClickListener(v -> finish());

        angleInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {

                String value = s.toString().trim();

                if (value.isEmpty()) {
                    manualMode = false;
                    return;
                }

                manualMode = true;

                try {
                    double angle = Double.parseDouble(value);

                    if (angle < 0 || angle >= 360
                            || Double.isNaN(angle)
                            || Double.isInfinite(angle)) {
                        resultText.setText("角度超出范围");
                        directionText.setText(
                                "请输入0°至359.99°之间的角度。");
                        return;
                    }

                    currentAngle = angle;
                    updateDirectionDisplay(angle);

                } catch (NumberFormatException e) {
                    resultText.setText("输入格式不正确");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        setContentView(scrollView);
    }

    private void updateDirectionDisplay(double angle) {
        double normalizedAngle =
                TwentyFourMountains.normalizeAngle(angle);

        double oppositeAngle =
                TwentyFourMountains.normalizeAngle(
                        normalizedAngle + 180);

        String mountain =
                TwentyFourMountains.getMountainName(normalizedAngle);

        String oppositeMountain =
                TwentyFourMountains.getMountainName(oppositeAngle);

        resultText.setText(
                String.format(
                        Locale.CHINA,
                        "%.2f°\n%s山",
                        normalizedAngle,
                        mountain));

        directionText.setText(
                String.format(
                        Locale.CHINA,
                        "当前方位角：%.2f°\n" +
                        "当前二十四山：%s山\n\n" +
                        "相反方向角度：%.2f°\n" +
                        "相反方向二十四山：%s山",
                        normalizedAngle,
                        mountain,
                        oppositeAngle,
                        oppositeMountain));

        compassView.setSelectedAngle(normalizedAngle);
    }

    private TextView createButton(
            String text,
            int fillColor,
            int fontColor) {

        TextView button = new TextView(this);
        button.setText(text);
        button.setTextColor(fontColor);
        button.setTextSize(16);
        button.setTypeface(null, Typeface.BOLD);
        button.setGravity(Gravity.CENTER);
        button.setBackgroundColor(fillColor);
        button.setClickable(true);
        return button;
    }

    private void hideKeyboard() {
        if (angleInput != null) {
            InputMethodManager manager =
                    (InputMethodManager)
                            getSystemService(INPUT_METHOD_SERVICE);

            if (manager != null) {
                manager.hideSoftInputFromWindow(
                        angleInput.getWindowToken(), 0);
            }
        }
    }

    private LinearLayout.LayoutParams params(
            int width, int height) {

        return new LinearLayout.LayoutParams(
                width > 0 ? dp(width) : width,
                height > 0 ? dp(height) : height);
    }

    private int dp(float value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics().density + 0.5f);
    }

    /**
     * 仿古九圈罗盘绘制区域
     */
    private class CompassDialView extends View {

        private final Paint paint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        private final int paper = Color.rgb(226, 207, 163);
        private final int paperLight = Color.rgb(239, 224, 190);
        private final int ink = Color.rgb(75, 48, 27);
        private final int bronze = Color.rgb(139, 91, 43);
        private final int redInk = Color.rgb(155, 43, 31);

        private final String[] baguaLater = {
                "坎", "艮", "震", "巽",
                "离", "坤", "兑", "乾"
        };

        private final String[] baguaEarlier = {
                "乾", "兑", "离", "震",
                "巽", "坎", "艮", "坤"
        };

        private final String[] mountains = {
                "子", "癸", "丑", "艮", "寅", "甲",
                "卯", "乙", "辰", "巽", "巳", "丙",
                "午", "丁", "未", "坤", "申", "庚",
                "酉", "辛", "戌", "乾", "亥", "壬"
        };

        private final String[] stems = {
                "甲", "乙", "丙", "丁", "戊",
                "己", "庚", "辛", "壬", "癸"
        };

        private final String[] branches = {
                "子", "丑", "寅", "卯", "辰", "巳",
                "午", "未", "申", "酉", "戌", "亥"
        };

        private final String[] sixtyJiaZi = {
                "甲子", "乙丑", "丙寅", "丁卯", "戊辰",
                "己巳", "庚午", "辛未", "壬申", "癸酉",
                "甲戌", "乙亥", "丙子", "丁丑", "戊寅",
                "己卯", "庚辰", "辛巳", "壬午", "癸未",
                "甲申", "乙酉", "丙戌", "丁亥", "戊子",
                "己丑", "庚寅", "辛卯", "壬辰", "癸巳",
                "甲午", "乙未", "丙申", "丁酉", "戊戌",
                "己亥", "庚子", "辛丑", "壬寅", "癸卯",
                "甲辰", "乙巳", "丙午", "丁未", "戊申",
                "己酉", "庚戌", "辛亥", "壬子", "癸丑",
                "甲寅", "乙卯", "丙辰", "丁巳", "戊午",
                "己未", "庚申", "辛酉", "壬戌", "癸亥"
        };

        private final String[] twelveStages = {
                "长生", "沐浴", "冠带", "临官",
                "帝旺", "衰", "病", "死",
                "墓", "绝", "胎", "养"
        };

        private final String[] twentyEightMansions = {
                "角", "亢", "氐", "房", "心", "尾", "箕",
                "斗", "牛", "女", "虚", "危", "室", "壁",
                "奎", "娄", "胃", "昴", "毕", "觜", "参",
                "井", "鬼", "柳", "星", "张", "翼", "轸"
        };

        private final String[] nineStars = {
                "一白", "二黑", "三碧",
                "四绿", "五黄", "六白",
                "七赤", "八白", "九紫"
        };

        private double selectedAngle = 0;

        CompassDialView() {
            super(ResidenceDirectionActivity.this);
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
                width = dp(340);
            }

            setMeasuredDimension(width, width);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float width = getWidth();
            float cx = width / 2f;
            float cy = getHeight() / 2f;
            float radius = Math.min(width, getHeight()) * 0.47f;

            // 仿古纸质底盘
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(paper);
            canvas.drawCircle(cx, cy, radius, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(bronze);
            paint.setStrokeWidth(dp(2));
            canvas.drawCircle(cx, cy, radius, paint);

            paint.setStrokeWidth(dp(1));
            canvas.drawCircle(cx, cy, radius * 0.975f, paint);

            canvas.save();
            canvas.rotate((float) -selectedAngle, cx, cy);

            // 第一圈：后天八卦
            drawRing(canvas, cx, cy, radius,
                    0.88f, 0.97f, baguaLater, 8, 11);

            // 第二圈：先天八卦
            drawRing(canvas, cx, cy, radius,
                    0.78f, 0.87f, baguaEarlier, 8, 11);

            // 第三圈：二十四山
            drawRing(canvas, cx, cy, radius,
                    0.67f, 0.77f, mountains, 24, 9);

            // 第四圈：十天干
            drawRing(canvas, cx, cy, radius,
                    0.57f, 0.66f, stems, 10, 10);

            // 第五圈：十二地支
            drawRing(canvas, cx, cy, radius,
                    0.47f, 0.56f, branches, 12, 10);

            // 第六圈：六十甲子，两个字沿半径竖排
            drawSixtyJiaZiRing(canvas, cx, cy, radius,
                    0.34f, 0.46f);

            // 第七圈：十二长生
            drawRing(canvas, cx, cy, radius,
                    0.24f, 0.33f, twelveStages, 12, 8);

            // 第八圈：二十八宿
            drawRing(canvas, cx, cy, radius,
                    0.14f, 0.23f, twentyEightMansions, 28, 7);

            // 第九圈：九星
            drawRing(canvas, cx, cy, radius,
                    0.055f, 0.13f, nineStars, 9, 7);

            canvas.restore();

            drawNorthSouthNeedle(canvas, cx, cy, radius);
            drawCenterAxis(canvas, cx, cy);
        }

        private void drawRing(
                Canvas canvas,
                float cx,
                float cy,
                float radius,
                float innerRatio,
                float outerRatio,
                String[] labels,
                int divisions,
                float textSizeDp) {

            float inner = radius * innerRatio;
            float outer = radius * outerRatio;
            float middle = (inner + outer) / 2f;

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(paperLight);

            canvas.drawCircle(cx, cy, outer, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(0.8f));
            paint.setColor(bronze);

            canvas.drawCircle(cx, cy, inner, paint);
            canvas.drawCircle(cx, cy, outer, paint);

            for (int i = 0; i < divisions; i++) {
                double angle = Math.toRadians(
                        i * 360.0 / divisions - 90);

                float x1 = cx
                        + (float) Math.cos(angle) * inner;
                float y1 = cy
                        + (float) Math.sin(angle) * inner;
                float x2 = cx
                        + (float) Math.cos(angle) * outer;
                float y2 = cy
                        + (float) Math.sin(angle) * outer;

                canvas.drawLine(x1, y1, x2, y2, paint);
            }

            paint.setStyle(Paint.Style.FILL);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(dp(textSizeDp));
            paint.setColor(ink);

            for (int i = 0; i < labels.length; i++) {
                double angle = Math.toRadians(
                        i * 360.0 / divisions - 90);

                float tx = cx
                        + (float) Math.cos(angle) * middle;
                float ty = cy
                        + (float) Math.sin(angle) * middle;

                if (i == 0 && labels.length == 24) {
                    paint.setColor(redInk);
                } else {
                    paint.setColor(ink);
                }

                canvas.drawText(
                        labels[i],
                        tx,
                        ty + dp(textSizeDp) * 0.35f,
                        paint);
            }
        }

        private void drawSixtyJiaZiRing(
                Canvas canvas,
                float cx,
                float cy,
                float radius,
                float innerRatio,
                float outerRatio) {

            float inner = radius * innerRatio;
            float outer = radius * outerRatio;
            float middle = (inner + outer) / 2f;

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(paperLight);
            canvas.drawCircle(cx, cy, outer, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(0.7f));
            paint.setColor(bronze);

            canvas.drawCircle(cx, cy, inner, paint);
            canvas.drawCircle(cx, cy, outer, paint);

            for (int i = 0; i < 60; i++) {
                double angle = Math.toRadians(
                        i * 6.0 - 90);

                float x1 = cx
                        + (float) Math.cos(angle) * inner;
                float y1 = cy
                        + (float) Math.sin(angle) * inner;
                float x2 = cx
                        + (float) Math.cos(angle) * outer;
                float y2 = cy
                        + (float) Math.sin(angle) * outer;

                canvas.drawLine(x1, y1, x2, y2, paint);
            }

            paint.setStyle(Paint.Style.FILL);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(dp(5.5f));
            paint.setColor(ink);

            for (int i = 0; i < sixtyJiaZi.length; i++) {
                double angle = Math.toRadians(
                        i * 6.0 - 90);

                float tx = cx
                        + (float) Math.cos(angle) * middle;
                float ty = cy
                        + (float) Math.sin(angle) * middle;

                String first = sixtyJiaZi[i].substring(0, 1);
                String second = sixtyJiaZi[i].substring(1, 2);

                canvas.save();
                canvas.rotate(
                        (float) Math.toDegrees(angle) + 90,
                        tx, ty);

                canvas.drawText(
                        first, tx, ty - dp(1), paint);
                canvas.drawText(
                        second, tx, ty + dp(5), paint);

                canvas.restore();
            }
        }

        private void drawNorthSouthNeedle(
                Canvas canvas,
                float cx,
                float cy,
                float radius) {

            float length = radius * 0.24f;
            float halfWidth = dp(6);

            Path north = new Path();
            north.moveTo(cx, cy - length);
            north.lineTo(cx - halfWidth, cy);
            north.lineTo(cx + halfWidth, cy);
            north.close();

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(176, 45, 32));
            canvas.drawPath(north, paint);

            Path south = new Path();
            south.moveTo(cx, cy + length);
            south.lineTo(cx - halfWidth, cy);
            south.lineTo(cx + halfWidth, cy);
            south.close();

            paint.setColor(Color.rgb(250, 246, 229));
            canvas.drawPath(south, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(bronze);

            canvas.drawLine(
                    cx, cy - length,
                    cx, cy + length, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextSize(dp(9));

            paint.setColor(redInk);
            canvas.drawText(
                    "N", cx, cy - length - dp(3), paint);

            paint.setColor(ink);
            canvas.drawText(
                    "S", cx, cy + length + dp(11), paint);
        }

        private void drawCenterAxis(
                Canvas canvas,
                float cx,
                float cy) {

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(112, 76, 39));
            canvas.drawCircle(cx, cy, dp(17), paint);

            paint.setColor(paperLight);
            canvas.drawCircle(cx, cy, dp(13), paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(bronze);
            canvas.drawCircle(cx, cy, dp(13), paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(ink);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(dp(8));
            paint.setTypeface(Typeface.DEFAULT_BOLD);

            canvas.drawText(
                    "玄天",
                    cx,
                    cy + dp(3),
                    paint);
        }
    }
}
