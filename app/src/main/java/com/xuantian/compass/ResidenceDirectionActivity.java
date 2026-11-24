package com.xuantian.compass;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Locale;

public class ResidenceDirectionActivity extends Activity {

private EditText angleInput;
private TextView resultText;
private TextView directionText;

private int gold = Color.rgb(210, 170, 85);
private int background = Color.rgb(18, 20, 27);
private int panel = Color.rgb(35, 38, 47);
private int white = Color.WHITE;
private int gray = Color.LTGRAY;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    createPage();
}

private void createPage() {

    ScrollView scrollView = new ScrollView(this);
    scrollView.setFillViewport(true);
    scrollView.setBackgroundColor(background);

    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(dp(20), dp(24), dp(20), dp(24));

    scrollView.addView(root);

    TextView title = new TextView(this);
    title.setText("住宅坐向测量");
    title.setTextColor(gold);
    title.setTextSize(25);
    title.setTypeface(null, Typeface.BOLD);
    title.setGravity(Gravity.CENTER);
    root.addView(title, params(-1, -2));

    TextView subtitle = new TextView(this);
    subtitle.setText("二十四山 · 坐山朝向计算");
    subtitle.setTextColor(gray);
    subtitle.setTextSize(14);
    subtitle.setGravity(Gravity.CENTER);
    subtitle.setPadding(0, dp(8), 0, dp(28));
    root.addView(subtitle, params(-1, -2));

    TextView inputLabel = new TextView(this);
    inputLabel.setText("请输入住宅坐向测量角度");
    inputLabel.setTextColor(white);
    inputLabel.setTextSize(16);
    root.addView(inputLabel, params(-1, -2));

    angleInput = new EditText(this);
    angleInput.setSingleLine(true);
    angleInput.setInputType(
            android.text.InputType.TYPE_CLASS_NUMBER
                    | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                    | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED
    );
    angleInput.setHint("例如：180");
    angleInput.setHintTextColor(Color.GRAY);
    angleInput.setTextColor(white);
    angleInput.setTextSize(20);
    angleInput.setPadding(dp(14), dp(10), dp(14), dp(10));
    angleInput.setBackgroundColor(panel);

    LinearLayout.LayoutParams inputParams = params(-1, dp(55));
    inputParams.topMargin = dp(12);
    root.addView(angleInput, inputParams);

    TextView hint = new TextView(this);
    hint.setText("角度范围：0° 至 359.99°。0°代表正北，角度顺时针增加。");
    hint.setTextColor(gray);
    hint.setTextSize(12);
    hint.setPadding(0, dp(10), 0, dp(20));
    root.addView(hint, params(-1, -2));

    TextView resultTitle = new TextView(this);
    resultTitle.setText("计算结果");
    resultTitle.setTextColor(gold);
    resultTitle.setTextSize(18);
    resultTitle.setTypeface(null, Typeface.BOLD);
    root.addView(resultTitle, params(-1, -2));

    resultText = new TextView(this);
    resultText.setText("等待输入测量角度");
    resultText.setTextColor(white);
    resultText.setTextSize(21);
    resultText.setGravity(Gravity.CENTER);
    resultText.setPadding(dp(10), dp(25), dp(10), dp(25));
    resultText.setBackgroundColor(panel);

    LinearLayout.LayoutParams resultParams = params(-1, -2);
    resultParams.topMargin = dp(12);
    root.addView(resultText, resultParams);

    directionText = new TextView(this);
    directionText.setText(
            "输入角度后，将显示住宅坐山、朝向及对应二十四山。"
    );
    directionText.setTextColor(gray);
    directionText.setTextSize(15);
    directionText.setLineSpacing(dp(6), 1.0f);
    directionText.setPadding(dp(12), dp(18), dp(12), dp(18));
    root.addView(directionText, params(-1, -2));

    TextView backButton = new TextView(this);
    backButton.setText("返回");
    backButton.setTextColor(Color.BLACK);
    backButton.setTextSize(16);
    backButton.setTypeface(null, Typeface.BOLD);
    backButton.setGravity(Gravity.CENTER);
    backButton.setBackgroundColor(gold);

    LinearLayout.LayoutParams buttonParams = params(-1, dp(50));
    buttonParams.topMargin = dp(24);
    root.addView(backButton, buttonParams);

    backButton.setOnClickListener(v -> finish());

    angleInput.addTextChangedListener(new TextWatcher() {

        @Override
        public void beforeTextChanged(
                CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(
                CharSequence s, int start, int before, int count) {

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
        resultText.setText("等待输入测量角度");
        directionText.setText("请输入有效的住宅测量角度。");
        return;
    }

    try {

        double angle = Double.parseDouble(input.trim());

        if (Double.isNaN(angle) || Double.isInfinite(angle)
                || angle < 0 || angle >= 360) {

            resultText.setText("角度超出范围");
            directionText.setText("请输入大于或等于0°且小于360°的角度。");
            return;
        }

        // 输入角度代表住宅坐山方向
        double sittingAngle =
                TwentyFourMountains.normalizeAngle(angle);

        // 朝向与坐山相差180度
        double facingAngle =
                TwentyFourMountains.normalizeAngle(
                        sittingAngle + 180.0
                );

        String sittingMountain =
                TwentyFourMountains.getMountainName(sittingAngle);

        String facingMountain =
                TwentyFourMountains.getMountainName(facingAngle);

        resultText.setText(
                "坐山：" + sittingMountain + "山\n"
                        + "朝向：" + facingMountain + "山"
        );

        directionText.setText(
                String.format(
                        Locale.CHINA,
                        "坐山角度：%.2f°\n"
                                + "朝向角度：%.2f°\n\n"
                                + "二十四山：%s山坐，%s山向\n\n"
                                + "说明：当前按输入角度为坐山方向计算，"
                                + "朝向取相反方向180°。",
                        sittingAngle,
                        facingAngle,
                        sittingMountain,
                        facingMountain
                )
        );

    } catch (NumberFormatException e) {

        resultText.setText("输入格式不正确");
        directionText.setText("请检查角度，只输入数字，例如：135.5");
    }
}

private LinearLayout.LayoutParams params(int width, int height) {

    return new LinearLayout.LayoutParams(
            width > 0 ? dp(width) : width,
            height > 0 ? dp(height) : height
    );
}

private int dp(float value) {

    return (int) (value * getResources()
            .getDisplayMetrics().density + 0.5f);
}

}
