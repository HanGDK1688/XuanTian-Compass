文件名： "ResidenceDirectionActivity.java"
文件路径： "app/src/main/java/com/xuantian/compass/ResidenceDirectionActivity.java"

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
                Sensor.TYPE_ACCELEROMETER
        );

        magneticSensor = sensorManager.getDefaultSensor(
                Sensor.TYPE_MAGNETIC_FIELD
        );

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

            sensorStatus.setText(
                    "传感器不可用：无法进行实时指南针测量"
            );

            sensorStatus.setTextColor(Color.RED);

        } else {

            sensorStatus.setText(
                    "传感器已就绪，等待方向数据……"
            );

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
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI
        );

        sensorManager.registerListener(
                this,
                magneticSensor,
                SensorManager.SENSOR_DELAY_UI
        );

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

        if (event.sensor.getType()
                == Sensor.TYPE_ACCELEROMETER) {

            lowPassFilter(
                    event.values,
                    gravityValues,
                    !hasGravity
            );

            hasGravity = true;

        } else if (event.sensor.getType()
                == Sensor.TYPE_MAGNETIC_FIELD) {

            lowPassFilter(
                    event.values,
                    magneticValues,
                    !hasMagnetic
            );

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
                magneticValues
        );

        if (!success) {

            sensorStatus.setText(
                    "方向计算失败，请远离磁性干扰并重新校准"
            );

            sensorStatus.setTextColor(Color.RED);
            return;
        }

        SensorManager.getOrientation(
                rotationMatrix,
                orientation
        );

        double azimuth = Math.toDegrees(orientation[0]);

        azimuth = (azimuth + 360.0) % 360.0;

        currentAngle = azimuth;

        sensorStatus.setText(
                "实时指南针：传感器工作正常"
        );

        sensorStatus.setTextColor(gold);

        updateDirectionDisplay(currentAngle);
    }

    @Override
    public void onAccuracyChanged(
            Sensor sensor,
            int accuracy) {

        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {

            sensorStatus.setText(
                    "磁场精度较低，请校准手机指南针"
            );

            sensorStatus.setTextColor(Color.RED);

        } else if (accuracy
                == SensorManager.SENSOR_STATUS_ACCURACY_LOW) {

            sensorStatus.setText(
                    "指南针精度较低，建议进行校准"
            );

            sensorStatus.setTextColor(Color.YELLOW);
        }
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
        subtitle.setText("传统二十四山 · 动态旋转罗盘");
        subtitle.setTextColor(gray);
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);

        root.addView(subtitle, params(-1, 35));

        sensorStatus = new TextView(this);
        sensorStatus.setText("正在检查手机传感器……");
        sensorStatus.setTextColor(gold);
        sensorStatus.setTextSize(13);
        sensorStatus.setGravity(Gravity.CENTER);
        sensorStatus.setPadding(dp(5), dp(8), dp(5), dp(8));

        root.addView(sensorStatus, params(-1, 42));

        compassView = new CompassDialView();

        LinearLayout.LayoutParams compassParams =
                params(-1, -2);

        compassParams.topMargin = dp(8);
        compassParams.bottomMargin = dp(18);

        root.addView(compassView, compassParams);

        TextView inputTitle = new TextView(this);
        inputTitle.setText("手动输入角度（可选）");
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
        resultText.setText("等待传感器数据");
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

        TextView resetButton = createButton(
                "恢复实时指南针",
                panel,
                gold
        );

        root.addView(resetButton, params(-1, 50));

        resetButton.setOnClickListener(v -> {

            manualMode = false;

            angleInput.setText("");

            hideKeyboard();

            sensorStatus.setText(
                    "已恢复实时指南针模式"
            );

            sensorStatus.setTextColor(gold);

            if (hasGravity && hasMagnetic) {
                calculateSensorAngle();
            }
        });

        TextView note = new TextView(this);
        note.setText(
                "使用说明：\n\n" +
                "1. 手机顶部所指方向为当前测量方向。\n" +
                "2. 0°代表磁北，角度顺时针增加。\n" +
                "3. 每一山占15°，共二十四山。\n" +
                "4. 手机磁力计容易受到金属及电器干扰。\n" +
                "5. 手机方向不等于住宅实际坐向。\n" +
                "6. 本功能当前显示磁北参考方位，尚未校正磁偏角。"
        );

        note.setTextColor(gray);
        note.setTextSize(13);
        note.setLineSpacing(dp(5), 1f);
        note.setPadding(dp(8), dp(18), dp(8), dp(18));

        root.addView(note, params(-1, -2));

        TextView backButton = createButton(
                "返回住宅风水分析",
                gold,
                Color.BLACK
        );

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
                                "请输入0°至359.99°之间的角度。"
                        );

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
                        normalizedAngle + 180
                );

        String mountain =
                TwentyFourMountains.getMountainName(
                        normalizedAngle
                );

        String oppositeMountain =
                TwentyFourMountains.getMountainName(
                        oppositeAngle
                );

        resultText.setText(
                String.format(
                        Locale.CHINA,
                        "%.2f°\n%s山",
                        normalizedAngle,
                        mountain
                )
        );

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
                        oppositeMountain
                )
        );

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
                        angleInput.getWindowToken(),
                        0
                );
            }
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
     * 动态二十四山罗盘
     *
     * 盘面根据当前方位反向旋转；
     * 红色指针根据当前手机朝向转动。
     */
    private class CompassDialView extends View {

        private final Paint paint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

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

            setMeasuredDimension(width, width);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float width = getWidth();
            float cx = width / 2f;
            float cy = getHeight() / 2f;

            float radius = Math.min(width, getHeight()) * 0.46f;

            // 固定外圈背景
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(30, 25, 20));
            canvas.drawCircle(cx, cy, radius, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(gold);
            paint.setStrokeWidth(dp(2));

            canvas.drawCircle(cx, cy, radius, paint);

            // 盘面整体旋转
            canvas.save();
            canvas.rotate(
                    (float) -selectedAngle,
                    cx,
                    cy
            );

            drawDialFace(canvas, cx, cy, radius);

            canvas.restore();

            // 中心固定标记
            drawCenter(canvas, cx, cy, radius);

            // 固定外圈方向指针：显示手机当前朝向
            drawHeadingPointer(
                    canvas,
                    cx,
                    cy,
                    radius,
                    selectedAngle
            );
        }

        private void drawDialFace(
                Canvas canvas,
                float cx,
                float cy,
                float radius) {

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(gold);
            paint.setStrokeWidth(dp(1.5f));

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

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(52, 39, 25));

            canvas.drawCircle(
                    cx,
                    cy,
                    radius * 0.29f,
                    paint
            );

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

            paint.setStyle(Paint.Style.FILL);
        }

        private void drawCenter(
                Canvas canvas,
                float cx,
                float cy,
                float radius) {

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(52, 39, 25));

            canvas.drawCircle(
                    cx,
                    cy,
                    radius * 0.27f,
                    paint
            );

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
        }

        private void drawHeadingPointer(
                Canvas canvas,
                float cx,
                float cy,
                float radius,
                double angle) {

            canvas.save();

            canvas.rotate(
                    (float) angle,
                    cx,
                    cy
            );

            float tipY = cy - radius * 0.98f;
            float baseY = cy - radius * 0.78f;
            float size = dp(10);

            Path path = new Path();

            path.moveTo(cx, tipY);
            path.lineTo(cx - size, baseY);
            path.lineTo(cx + size, baseY);
            path.close();

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.RED);

            canvas.drawPath(path, paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(dp(10));
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextAlign(Paint.Align.CENTER);

            canvas.drawText(
                    "朝向",
                    cx,
                    baseY + dp(15),
                    paint
            );

            canvas.restore();
        }
    }
}
