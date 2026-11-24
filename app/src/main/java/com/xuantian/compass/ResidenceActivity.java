package com.xuantian.compass;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ResidenceActivity extends Activity {

private EditText buildingYearInput;
private EditText floorInput;
private EditText houseNameInput;
private EditText areaInput;

private ResidenceStorage residenceStorage;

private final int gold = Color.rgb(220, 180, 105);
private final int background = Color.rgb(20, 17, 14);
private final int cardBackground = Color.rgb(34, 29, 24);
private final int textColor = Color.rgb(235, 225, 210);

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getWindow().setStatusBarColor(background);
    getWindow().setNavigationBarColor(background);

    residenceStorage = new ResidenceStorage(this);

    createUI();
}

@Override
protected void onDestroy() {
    super.onDestroy();

    if (residenceStorage != null) {
        residenceStorage.close();
    }
}

private void createUI() {

    ScrollView scrollView = new ScrollView(this);
    scrollView.setFillViewport(true);
    scrollView.setBackgroundColor(background);
    scrollView.setVerticalScrollBarEnabled(false);

    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(dp(20), dp(16), dp(20), dp(30));

    scrollView.addView(root);

    // 页面标题
    TextView title = new TextView(this);
    title.setText("住宅风水分析");
    title.setTextColor(gold);
    title.setTextSize(25);
    title.setGravity(Gravity.CENTER);
    title.setTypeface(null, Typeface.BOLD);
    title.setIncludeFontPadding(true);

    root.addView(title, params(-1, 60));

    TextView subtitle = new TextView(this);
    subtitle.setText("住宅基本资料 · 玄空飞星宅盘");
    subtitle.setTextColor(Color.LTGRAY);
    subtitle.setTextSize(14);
    subtitle.setGravity(Gravity.CENTER);

    root.addView(subtitle, params(-1, 40));

    // 一、住宅基本信息
    addSectionTitle(root, "一、住宅基本信息");

    houseNameInput = addInput(
            root, "住宅名称", "例如：自住房");

    buildingYearInput = addInput(
            root, "建造年份", "请输入公历年份，例如：2024");

    floorInput = addInput(
            root, "所在楼层", "例如：6");

    areaInput = addInput(
            root, "住宅面积（平方米）", "例如：120");

    // 二、住宅坐向
    addSectionTitle(root, "二、住宅坐向");

    TextView directionInfo = new TextView(this);

    directionInfo.setText(
            "住宅坐向需要区分坐山与朝向。\n\n" +
            "通过二十四山罗盘测量住宅坐向，" +
            "记录住宅的坐山、朝向及对应角度。\n\n" +
            "注意：不能简单地把手机指南针读数直接当作住宅坐向。"
    );

    styleInfoText(directionInfo);
    root.addView(directionInfo, params(-1, -2));

    // 住宅坐向测量入口
    TextView directionButton = createActionButton(
            "测量住宅坐向 · 二十四山罗盘",
            gold,
            background
    );

    root.addView(directionButton, params(-1, 56));

    directionButton.setOnClickListener(v -> {
        Intent intent = new Intent(
                ResidenceActivity.this,
                ResidenceDirectionActivity.class
        );
        startActivity(intent);
    });

    // 三、排盘说明
    addSectionTitle(root, "三、排盘说明");

    TextView methodInfo = new TextView(this);

    methodInfo.setText(
            "当前采用的开发方案：\n\n" +
            "1. 玄空飞星下卦法。\n" +
            "2. 根据住宅起运依据确定元运。\n" +
            "3. 根据坐山、朝向确定宅盘。\n" +
            "4. 暂不处理替卦及兼向。\n\n" +
            "注意：建造年份不一定等于实际入伙或换运年份，" +
            "后续需要进一步确定住宅的起运依据。"
    );

    styleInfoText(methodInfo);
    root.addView(methodInfo, params(-1, -2));

    // 四、住宅资料管理
    addSectionTitle(root, "四、住宅资料管理");

    TextView checkButton = createActionButton(
            "检查住宅资料",
            gold,
            background
    );

    root.addView(checkButton, params(-1, 56));
    checkButton.setOnClickListener(v -> checkInformation());

    TextView saveButton = createActionButton(
            "保存住宅资料",
            gold,
            background
    );

    root.addView(saveButton, params(-1, 56));
    saveButton.setOnClickListener(v -> saveInformation());

    // 查看住宅档案入口
    TextView listButton = createActionButton(
            "查看已保存的住宅档案",
            cardBackground,
            gold
    );

    root.addView(listButton, params(-1, 56));

    listButton.setOnClickListener(v -> {
        Intent intent = new Intent(
                ResidenceActivity.this,
                ResidenceListActivity.class
        );
        startActivity(intent);
    });

    // 返回首页
    TextView backButton = createActionButton(
            "返回罗盘首页",
            cardBackground,
            gold
    );

    root.addView(backButton, params(-1, 56));
    backButton.setOnClickListener(v -> finish());

    setContentView(scrollView);
}

/**
 * 创建自定义圆角按钮
 */
private TextView createActionButton(
        String text,
        int fillColor,
        int fontColor) {

    TextView button = new TextView(this);

    button.setText(text);
    button.setTextColor(fontColor);
    button.setTextSize(16);
    button.setTypeface(null, Typeface.BOLD);
    button.setGravity(Gravity.CENTER);
    button.setIncludeFontPadding(true);
    button.setMinHeight(dp(56));
    button.setClickable(true);
    button.setFocusable(true);

    GradientDrawable drawable = new GradientDrawable();
    drawable.setColor(fillColor);
    drawable.setCornerRadius(dp(12));

    button.setBackground(drawable);

    return button;
}

/**
 * 创建分区标题
 */
private void addSectionTitle(
        LinearLayout root,
        String text) {

    TextView title = new TextView(this);

    title.setText(text);
    title.setTextColor(gold);
    title.setTextSize(18);
    title.setTypeface(null, Typeface.BOLD);
    title.setIncludeFontPadding(true);
    title.setGravity(Gravity.CENTER_VERTICAL);
    title.setPadding(0, dp(12), 0, dp(8));

    root.addView(title, params(-1, 58));
}

/**
 * 统一说明文字样式
 */
private void styleInfoText(TextView textView) {

    textView.setTextColor(textColor);
    textView.setTextSize(14);
    textView.setIncludeFontPadding(true);
    textView.setLineSpacing(dp(5), 1f);
    textView.setPadding(dp(12), dp(12), dp(12), dp(12));

    GradientDrawable drawable = new GradientDrawable();
    drawable.setColor(cardBackground);
    drawable.setCornerRadius(dp(10));

    textView.setBackground(drawable);
}

/**
 * 创建住宅资料输入框
 */
private EditText addInput(
        LinearLayout root,
        String label,
        String hint) {

    TextView title = new TextView(this);

    title.setText(label);
    title.setTextColor(Color.WHITE);
    title.setTextSize(14);
    title.setIncludeFontPadding(true);
    title.setGravity(Gravity.CENTER_VERTICAL);
    title.setPadding(0, dp(8), 0, dp(4));

    root.addView(title, params(-1, 38));

    LinearLayout container = new LinearLayout(this);
    container.setOrientation(LinearLayout.VERTICAL);

    EditText input = new EditText(this);

    input.setHint(hint);
    input.setHintTextColor(Color.GRAY);
    input.setTextColor(Color.WHITE);
    input.setTextSize(15);
    input.setSingleLine(true);
    input.setIncludeFontPadding(true);
    input.setGravity(Gravity.CENTER_VERTICAL);
    input.setPadding(dp(12), 0, dp(12), 0);
    input.setBackgroundColor(Color.TRANSPARENT);

    container.addView(
            input,
            new LinearLayout.LayoutParams(-1, dp(64))
    );

    View underline = new View(this);
    underline.setBackgroundColor(gold);

    container.addView(
            underline,
            new LinearLayout.LayoutParams(-1, dp(1))
    );

    root.addView(container, params(-1, 65));

    return input;
}

/**
 * 验证住宅资料
 */
private boolean validateInformation() {

    String yearText =
            buildingYearInput.getText().toString().trim();

    String floorText =
            floorInput.getText().toString().trim();

    String areaText =
            areaInput.getText().toString().trim();

    if (yearText.isEmpty()) {
        buildingYearInput.setError("请输入建造年份");
        buildingYearInput.requestFocus();
        return false;
    }

    int year;

    try {
        year = Integer.parseInt(yearText);
    } catch (NumberFormatException e) {
        buildingYearInput.setError("年份必须是数字");
        buildingYearInput.requestFocus();
        return false;
    }

    if (year < 1900 || year > 2052) {
        buildingYearInput.setError(
                "目前支持的节气数据范围为1900—2052年"
        );
        buildingYearInput.requestFocus();
        return false;
    }

    if (floorText.isEmpty()) {
        floorInput.setError("请输入所在楼层");
        floorInput.requestFocus();
        return false;
    }

    if (areaText.isEmpty()) {
        areaInput.setError("请输入住宅面积");
        areaInput.requestFocus();
        return false;
    }

    double area;

    try {
        area = Double.parseDouble(areaText);
    } catch (NumberFormatException e) {
        areaInput.setError("面积必须是数字");
        areaInput.requestFocus();
        return false;
    }

    if (area <= 0) {
        areaInput.setError("住宅面积必须大于0");
        areaInput.requestFocus();
        return false;
    }

    return true;
}

/**
 * 检查住宅资料
 */
private void checkInformation() {

    if (!validateInformation()) {
        return;
    }

    String name =
            houseNameInput.getText().toString().trim();

    String year =
            buildingYearInput.getText().toString().trim();

    String floor =
            floorInput.getText().toString().trim();

    String area =
            areaInput.getText().toString().trim();

    String summary =
            "住宅名称：" + (name.isEmpty() ? "未填写" : name) +
            "\n建造年份：" + year +
            "\n所在楼层：" + floor +
            "\n住宅面积：" + area + " 平方米" +
            "\n\n资料检查通过。";

    new AlertDialog.Builder(this)
            .setTitle("住宅资料确认")
            .setMessage(summary)
            .setPositiveButton("确定", null)
            .show();
}

/**
 * 保存住宅资料
 */
private void saveInformation() {

    if (!validateInformation()) {
        return;
    }

    String name =
            houseNameInput.getText().toString().trim();

    int year = Integer.parseInt(
            buildingYearInput.getText().toString().trim()
    );

    String floor =
            floorInput.getText().toString().trim();

    double area = Double.parseDouble(
            areaInput.getText().toString().trim()
    );

    if (name.isEmpty()) {
        name = "未命名住宅";
    }

    ResidenceData data = new ResidenceData();

    data.name = name;
    data.buildingYear = year;
    data.floor = floor;
    data.area = area;

    long result = residenceStorage.insert(data);

    if (result > 0) {

        new AlertDialog.Builder(this)
                .setTitle("保存成功")
                .setMessage(
                        "住宅资料已经保存到手机本地数据库。\n\n" +
                        "住宅名称：" + name +
                        "\n建造年份：" + year +
                        "\n所在楼层：" + floor +
                        "\n住宅面积：" + area + " 平方米" +
                        "\n\n档案编号：" + result
                )
                .setPositiveButton("确定", null)
                .show();

    } else {

        Toast.makeText(
                this,
                "保存失败，请稍后重试",
                Toast.LENGTH_LONG
        ).show();
    }
}

/**
 * 统一布局参数
 */
private LinearLayout.LayoutParams params(
        int width,
        int height) {

    int actualWidth = width < 0 ? width : dp(width);
    int actualHeight = height < 0 ? height : dp(height);

    LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(
                    actualWidth,
                    actualHeight
            );

    p.setMargins(0, dp(5), 0, dp(5));

    return p;
}

/**
 * dp 转换
 */
private int dp(float value) {

    return (int) (
            value * getResources()
                    .getDisplayMetrics().density + 0.5f
    );
}

}
