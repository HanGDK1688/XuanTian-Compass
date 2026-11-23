package com.xuantian.compass;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Path;
import android.view.View;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.Toast;
import java.util.Locale;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private float[] gravity = new float[3];
    private float[] magnetic = new float[3];

    private boolean hasGravity = false;
    private boolean hasMagnetic = false;

    private float azimuth = 0;
    private float pitch = 0;
    private float roll = 0;

    private float filteredAzimuth = 0;
    private boolean hasFilteredAzimuth = false;

    private static final float DEAD_ZONE = 0.6f;
    private static final float SLOW_ALPHA = 0.12f;
    private static final float FAST_ALPHA = 0.45f;
    private static final float FAST_THRESHOLD = 8.0f;

    private CompassView compassView;
    private TextView angleText;
    private TextView mountainText;
    private TextView trigramText;
    private TextView levelText;
    private TextView magneticText;

    private boolean sensorAvailable = false;

    private final String[] mountains = {
            "子", "癸", "丑", "艮", "寅", "甲",
            "卯", "乙", "辰", "巽", "巳", "丙",
            "午", "丁", "未", "坤", "申", "庚",
            "酉", "辛", "戌", "乾", "亥", "壬"
    };

    private final String[] trigrams = {
            "坎", "坎", "艮", "艮", "艮", "震",
            "震", "震", "巽", "巽", "巽", "离",
            "离", "离", "坤", "坤", "坤", "兑",
            "兑", "兑", "乾", "乾", "乾", "坎"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(20, 17, 14));
        getWindow().setNavigationBarColor(Color.rgb(20, 17, 14));

        sensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);

        magnetometer = sensorManager.getDefaultSensor(
                Sensor.TYPE_MAGNETIC_FIELD);

        sensorAvailable = accelerometer != null &&
                magnetometer != null;

        createUI();
    }

    private void createUI() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(20, 17, 14));
        root.setPadding(dp(16), dp(12), dp(16), dp(12));

        TextView title = new TextView(this);
        title.setText("玄天罗盘");
        title.setTextColor(Color.rgb(220, 180, 105));
        title.setTextSize(25);
        title.setGravity(android.view.Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        root.addView(title, new LinearLayout.LayoutParams(-1, dp(50)));

        TextView subtitle = new TextView(this);
        subtitle.setText("传统二十四山 · 八卦定位");
        subtitle.setTextColor(Color.rgb(150, 140, 125));
        subtitle.setTextSize(13);
        subtitle.setGravity(android.view.Gravity.CENTER);

        root.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(30)));

        angleText = new TextView(this);
        angleText.setText("0.0°");
        angleText.setTextColor(Color.rgb(230, 195, 125));
        angleText.setTextSize(36);
        angleText.setGravity(android.view.Gravity.CENTER);
        angleText.setTypeface(Typeface.MONOSPACE);

        root.addView(angleText, new LinearLayout.LayoutParams(-1, dp(65)));

        mountainText = new TextView(this);
        mountainText.setText("子山");
        mountainText.setTextColor(Color.WHITE);
        mountainText.setTextSize(26);
        mountainText.setGravity(android.view.Gravity.CENTER);

        root.addView(mountainText, new LinearLayout.LayoutParams(-1, dp(45)));

        trigramText = new TextView(this);
        trigramText.setText("坎卦 · 北方");
        trigramText.setTextColor(Color.rgb(220, 180, 105));
        trigramText.setTextSize(18);
        trigramText.setGravity(android.view.Gravity.CENTER);

        root.addView(trigramText, new LinearLayout.LayoutParams(-1, dp(35)));

        compassView = new CompassView(this);

        LinearLayout.LayoutParams compassParams =
                new LinearLayout.LayoutParams(-1, 0, 1f);
        compassParams.setMargins(0, dp(8), 0, dp(8));

        root.addView(compassView, compassParams);

        levelText = new TextView(this);
        levelText.setText("水平仪：等待传感器");
        levelText.setTextColor(Color.WHITE);
        levelText.setTextSize(15);
        levelText.setGravity(android.view.Gravity.CENTER);

        root.addView(levelText, new LinearLayout.LayoutParams(-1, dp(35)));

        magneticText = new TextView(this);
        magneticText.setText("磁场：等待检测");
        magneticText.setTextColor(Color.rgb(150, 140, 125));
        magneticText.setTextSize(13);
        magneticText.setGravity(android.view.Gravity.CENTER);

        root.addView(magneticText, new LinearLayout.LayoutParams(-1, dp(30)));

        TextView calibration = new TextView(this);
        calibration.setText("校准方法：手机呈8字形移动");
        calibration.setTextColor(Color.rgb(150, 140, 125));
        calibration.setTextSize(13);
        calibration.setGravity(android.view.Gravity.CENTER);

        root.addView(calibration, new LinearLayout.LayoutParams(-1, dp(30)));

        // ===== 新增：玄空飞星入口 =====

        Button xuanKongButton = new Button(this);
        xuanKongButton.setText("玄空飞星排盘");
        xuanKongButton.setTextColor(Color.rgb(20, 17, 14));
        xuanKongButton.setTextSize(16);
        xuanKongButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        xuanKongButton.setBackgroundColor(Color.rgb(220, 180, 105));

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(-1, dp(50));

        buttonParams.setMargins(0, dp(8), 0, dp(8));

        root.addView(xuanKongButton, buttonParams);

        xuanKongButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    XuanKongActivity.class);
            startActivity(intent);
        });

        // ===== 新增部分结束 =====

        if (!sensorAvailable) {
            Toast.makeText(this,
                    "设备缺少必要传感器，无法正常测量方向",
                    Toast.LENGTH_LONG).show();
        }

        setContentView(root);
    }

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (accelerometer != null) {
            sensorManager.registerListener(
                    this, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME);
        }

        if (magnetometer != null) {
            sensorManager.registerListener(
                    this, magnetometer,
                    SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            gravity = event.values.clone();
            hasGravity = true;

            pitch = (float) Math.toDegrees(
                    Math.atan2(gravity[0],
                            Math.sqrt(gravity[1] * gravity[1]
                                    + gravity[2] * gravity[2])));

            roll = (float) Math.toDegrees(
                    Math.atan2(gravity[1], gravity[2]));
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

            magnetic = event.values.clone();
            hasMagnetic = true;

            float strength = (float) Math.sqrt(
                    magnetic[0] * magnetic[0]
                            + magnetic[1] * magnetic[1]
                            + magnetic[2] * magnetic[2]);

            if (strength < 25 || strength > 65) {
                magneticText.setText(
                        String.format(Locale.CHINA,
                                "磁场异常：%.1f μT，请远离磁性物体",
                                strength));
                magneticText.setTextColor(Color.RED);
            } else {
                magneticText.setText(
                        String.format(Locale.CHINA,
                                "磁场正常：%.1f μT", strength));
                magneticText.setTextColor(Color.rgb(130, 190, 130));
            }
        }

        if (hasGravity && hasMagnetic) {

            float[] R = new float[9];
            float[] I = new float[9];
            float[] orientation = new float[3];

            boolean success = SensorManager.getRotationMatrix(
                    R, I, gravity, magnetic);

            if (success) {
                SensorManager.getOrientation(R, orientation);

                float newAzimuth = (float) Math.toDegrees(orientation[0]);
                newAzimuth = (newAzimuth + 360) % 360;

                updateFilteredAzimuth(newAzimuth);
            }
        }
    }

    private void updateFilteredAzimuth(float newAzimuth) {

        if (!hasFilteredAzimuth) {
            filteredAzimuth = newAzimuth;
            hasFilteredAzimuth = true;
        }

        float diff = newAzimuth - filteredAzimuth;

        if (diff > 180) {
            diff -= 360;
        } else if (diff < -180) {
            diff += 360;
        }

        if (Math.abs(diff) < DEAD_ZONE) {
            return;
        }

        float alpha;

        if (Math.abs(diff) > FAST_THRESHOLD) {
            alpha = FAST_ALPHA;
        } else {
            alpha = SLOW_ALPHA;
        }

        filteredAzimuth += diff * alpha;
        filteredAzimuth = (filteredAzimuth + 360) % 360;

        azimuth = filteredAzimuth;

        updateDisplay();
    }

    private void updateDisplay() {

        int index = Math.round(azimuth / 15f) % 24;

        angleText.setText(String.format(
                Locale.CHINA, "%.1f°", azimuth));

        mountainText.setText(mountains[index] + "山");

        trigramText.setText(
                trigrams[index] + "卦 · " + getDirection(azimuth));

        compassView.setAzimuth(azimuth);

        levelText.setText(String.format(
                Locale.CHINA,
                "水平仪  横倾：%.1f°  俯仰：%.1f°",
                roll, pitch));
    }

    private String getDirection(float degree) {

        if (degree >= 337.5 || degree < 22.5)
            return "北方";

        if (degree < 67.5)
            return "东北方";

        if (degree < 112.5)
            return "东方";

        if (degree < 157.5)
            return "东南方";

        if (degree < 202.5)
            return "南方";

        if (degree < 247.5)
            return "西南方";

        if (degree < 292.5)
            return "西方";

        return "西北方";
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    class CompassView extends View {

        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float rotation = 0;

        public CompassView(Context context) {
            super(context);
        }

        public void setAzimuth(float value) {
            rotation = value;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {

            super.onDraw(canvas);

            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;

            float radius = Math.min(cx, cy) - dp(12);

            canvas.save();

            paint.setColor(Color.rgb(220, 180, 105));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));

            canvas.drawCircle(cx, cy, radius, paint);

            paint.setStrokeWidth(dp(1));

            canvas.drawCircle(cx, cy, radius * 0.82f, paint);
            canvas.drawCircle(cx, cy, radius * 0.60f, paint);
            canvas.drawCircle(cx, cy, radius * 0.38f, paint);

            canvas.rotate(-rotation, cx, cy);

            for (int i = 0; i < 360; i += 5) {

                float angle = (float) Math.toRadians(i - 90);
                float startRadius = radius * 0.84f;
                float endRadius;

                if (i % 30 == 0)
                    endRadius = radius * 0.96f;
                else if (i % 10 == 0)
                    endRadius = radius * 0.91f;
                else
                    endRadius = radius * 0.87f;

                float x1 = cx + (float) Math.cos(angle) * startRadius;
                float y1 = cy + (float) Math.sin(angle) * startRadius;

                float x2 = cx + (float) Math.cos(angle) * endRadius;
                float y2 = cy + (float) Math.sin(angle) * endRadius;

                paint.setStrokeWidth(dp(1));
                canvas.drawLine(x1, y1, x2, y2, paint);
            }

            String[] labels = {
                    "子", "癸", "丑", "艮", "寅", "甲",
                    "卯", "乙", "辰", "巽", "巳", "丙",
                    "午", "丁", "未", "坤", "申", "庚",
                    "酉", "辛", "戌", "乾", "亥", "壬"
            };

            for (int i = 0; i < 24; i++) {

                float angle = (float) Math.toRadians(i * 15 - 90);
                float textRadius = radius * 0.72f;

                float x = cx + (float) Math.cos(angle) * textRadius;
                float y = cy + (float) Math.sin(angle) * textRadius;

                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(dp(17));
                paint.setTypeface(Typeface.DEFAULT_BOLD);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setColor(Color.rgb(220, 180, 105));

                canvas.drawText(labels[i], x, y + dp(6), paint);
            }

            canvas.restore();

            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);

            drawArrow(canvas, cx, cy, radius);

            paint.setColor(Color.WHITE);
            paint.setTextSize(dp(20));
            paint.setTextAlign(Paint.Align.CENTER);

            canvas.drawText("玄天", cx, cy + dp(8), paint);
        }

        private void drawArrow(
                Canvas canvas,
                float cx,
                float cy,
                float radius) {

            float top = cy - radius * 0.95f;
            float bottom = cy + radius * 0.95f;
            float width = radius * 0.08f;

            Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            arrowPaint.setColor(Color.RED);

            Path path = new Path();

            path.moveTo(cx, top);
            path.lineTo(cx - width, cy);
            path.lineTo(cx, bottom);
            path.lineTo(cx + width, cy);
            path.close();

            canvas.drawPath(path, arrowPaint);
        }
    }
}
