package com.xuantian.compass;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

public class ResidenceActivity extends Activity {

private EditText buildingYearInput;
private EditText floorInput;
private EditText houseNameInput;
private EditText areaInput;

private ResidenceStorage residenceStorage;

private final int gold = Color.rgb(220, 180, 105);
private final int background = Color.rgb(20, 17, 14);

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

    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(dp(20), dp(16), dp(20), dp(24));

    scrollView.addView(root);

    TextView title = new TextView(this);
    title.setText("住宅风水分析");
    title.setTextColor(gold);
    title.setTextSize(25);
    title.setGravity(Gravity.CENTER);
    title.setTypeface(null, Typeface.BOLD);

    root.addView(title, params(-1, 55));

    TextView subtitle = new TextView(this);
    subtitle.setText("住宅基本资料 · 玄空飞星宅盘");
    subtitle.setTextColor(Color.LTGRAY);
    subtitle.setTextSize(14);
    subtitle.setGravity(Gravity.CENTER);

    root.addView(subtitle, params(-1, 35));

    addSectionTitle(root, "一、住宅基本信息");

    houseNameInput = addInput(
            root, "住宅名称", "例如：自住房");

    buildingYearInput = addInput(
            root, "建造年份", "请输入公历年份，例如：2024");

    floorInput = addInput(
            root, "所在楼层", "例如：6");

    areaInput = addInput(
            root, "住宅面积（平方米）", "例如：120");

    addSectionTitle(root, "二、住宅坐向");

    TextView directionInfo = new TextView(this);
    directionInfo.setText(
            "后续通过二十四山罗盘测量住宅坐向。\n" +
            "坐向需要区分坐山与朝向，不能简单地把手机指南针读数直接当作住宅坐向。");

    directionInfo.setTextColor(Color.rgb(190, 180, 165));
    directionInfo.setTextSize(14);
    directionInfo.setLineSpacing(dp(5), 1f);
    directionInfo.setPadding(0, dp(8), 0, dp(12));

    root.addView(directionInfo);

    addSectionTitle(root, "三、排盘说明");

    TextView methodInfo = new TextView(this);
    methodInfo.setText(
            "当前采用的开发方案：\n\n" +
            "1. 玄空飞星下卦法。\n" +
            "2. 根据建造年份确定元运。\n" +
            "3. 根据坐山、朝向确定宅盘。\n" +
            "4. 暂不处理替卦及兼向。\n\n" +
            "注意：建造年份不一定等于实际入伙或换运年份，" +
            "后续需要进一步确定住宅的起运依据。");

    methodInfo.setTextColor(Color.rgb(190, 180, 165));
    methodInfo.setTextSize(14);
    methodInfo.setLineSpacing(dp(5), 1f);
    methodInfo.setPadding(0, dp(8), 0, dp(12));

    root.addView(methodInfo);

    // 检查资料按钮
    Button confirmButton = new Button(this);
    confirmButton.setText("检查住宅资料");
    confirmButton.setTextColor(background);
    confirmButton.setTextSize(16);
    confirmButton.setTypeface(null, Typeface.BOLD);
    confirmButton.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(gold));

    root.addView(confirmButton, params(-1, 52));

    confirmButton.setOnClickListener(v -> checkInformation());

    // 保存资料按钮
    Button saveButton = new Button(this);
    saveButton.setText("保存住宅资料");
    saveButton.setTextColor(background);
    saveButton.setTextSize(16);
    saveButton.setTypeface(null, Typeface.BOLD);
    saveButton.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(gold));

    root.addView(saveButton, params(-1, 52));

    saveButton.setOnClickListener(v -> saveInformation());

    // 返回首页按钮
    Button backButton = new Button(this);
    backButton.setText("返回罗盘首页");

    root.addView(backButton, params(-1, 48));

    backButton.setOnClickListener(v -> finish());

    setContentView(scrollView);
}

private void addSectionTitle(
        LinearLayout root, String text) {

    TextView title = new TextView(this);
    title.setText(text);
    title.setTextColor(gold);
    title.setTextSize(18);
    title.setTypeface(null, Typeface.BOLD);
    title.setPadding(0, dp(18), 0, dp(8));

    root.addView(title);
}

/**
 * 创建住宅资料输入框
 * 输入文字垂直居中，金色横线固定在底部。
 */
private EditText addInput(
        LinearLayout root,
        String label,
        String hint) {

    TextView title = new TextView(this);
    title.setText(label);
    title.setTextColor(Color.WHITE);
    title.setTextSize(14);
    title.setPadding(0, dp(10), 0, dp(4));

    root.addView(title);

    LinearLayout inputContainer = new LinearLayout(this);
    inputContainer.setOrientation(LinearLayout.VERTICAL);
    inputContainer.setGravity(Gravity.BOTTOM);

    EditText input = new EditText(this);
    input.setHint(hint);
    input.setHintTextColor(Color.GRAY);
    input.setTextColor(Color.WHITE);
    input.setTextSize(15);
    input.setSingleLine(true);
    input.setGravity(Gravity.CENTER_VERTICAL);
    input.setPadding(dp(12), 0, dp(12), 0);
    input.setBackgroundColor(Color.TRANSPARENT);

    inputContainer.addView(
            input,
            new LinearLayout.LayoutParams(-1, dp(64)));

    View underline = new View(this);
    underline.setBackgroundColor(gold);

    inputContainer.addView(
            underline,
            new LinearLayout.LayoutParams(-1, dp(1)));

    root.addView(
            inputContainer,
            params(-1, dp(65)));

    return input;
}

/**
 * 检查住宅资料
 */
private void checkInformation() {

    String name = houseNameInput.getText().toString().trim();
    String yearText = buildingYearInput.getText().toString().trim();
    String floorText = floorInput.getText().toString().trim();
    String areaText = areaInput.getText().toString().trim();

    if (yearText.isEmpty()) {
        buildingYearInput.setError("请输入建造年份");
        return;
    }

    int year;

    try {
        year = Integer.parseInt(yearText);
    } catch (NumberFormatException e) {
        buildingYearInput.setError("年份必须是数字");
        return;
    }

    if (year < 1900 || year > 2052) {
        buildingYearInput.setError(
                "目前支持的节气数据范围为1900—2052年");
        return;
    }

    if (floorText.isEmpty()) {
        floorInput.setError("请输入所在楼层");
        return;
    }

    if (areaText.isEmpty()) {
        areaInput.setError("请输入住宅面积");
        return;
    }

    double area;

    try {
        area = Double.parseDouble(areaText);
    } catch (NumberFormatException e) {
        areaInput.setError("面积必须是数字");
        return;
    }

    if (area <= 0) {
        areaInput.setError("住宅面积必须大于0");
        return;
    }

    String summary =
            "住宅名称：" + (name.isEmpty() ? "未填写" : name) +
            "\n建造年份：" + year +
            "\n所在楼层：" + floorText +
            "\n住宅面积：" + area + " 平方米" +
            "\n\n资料检查通过。";

    new AlertDialog.Builder(this)
            .setTitle("住宅资料确认")
            .setMessage(summary)
            .setPositiveButton("确定", null)
            .show();
}

/**
 * 保存住宅资料到手机本地数据库
 */
private void saveInformation() {

    String name = houseNameInput.getText().toString().trim();
    String yearText = buildingYearInput.getText().toString().trim();
    String floorText = floorInput.getText().toString().trim();
    String areaText = areaInput.getText().toString().trim();

    if (yearText.isEmpty()) {
        buildingYearInput.setError("请输入建造年份");
        buildingYearInput.requestFocus();
        return;
    }

    int year;

    try {
        year = Integer.parseInt(yearText);
    } catch (NumberFormatException e) {
        buildingYearInput.setError("年份必须是数字");
        buildingYearInput.requestFocus();
        return;
    }

    if (year < 1900 || year > 2052) {
        buildingYearInput.setError(
                "目前支持的节气数据范围为1900—2052年");
        buildingYearInput.requestFocus();
        return;
    }

    if (floorText.isEmpty()) {
        floorInput.setError("请输入所在楼层");
        floorInput.requestFocus();
        return;
    }

    if (areaText.isEmpty()) {
        areaInput.setError("请输入住宅面积");
        areaInput.requestFocus();
        return;
    }

    double area;

    try {
        area = Double.parseDouble(areaText);
    } catch (NumberFormatException e) {
        areaInput.setError("面积必须是数字");
        areaInput.requestFocus();
        return;
    }

    if (area <= 0) {
        areaInput.setError("住宅面积必须大于0");
        areaInput.requestFocus();
        return;
    }

    if (name.isEmpty()) {
        name = "未命名住宅";
    }

    ResidenceData data = new ResidenceData();

    data.name = name;
    data.buildingYear = year;
    data.floor = floorText;
    data.area = area;

    long result = residenceStorage.insert(data);

    if (result > 0) {

        new AlertDialog.Builder(this)
                .setTitle("保存成功")
                .setMessage(
                        "住宅资料已经保存到手机本地数据库。\n\n" +
                        "住宅名称：" + name +
                        "\n建造年份：" + year +
                        "\n所在楼层：" + floorText +
                        "\n住宅面积：" + area + " 平方米" +
                        "\n\n档案编号：" + result)
                .setPositiveButton("确定", null)
                .show();

    } else {

        Toast.makeText(
                this,
                "保存失败，请检查后重试",
                Toast.LENGTH_LONG
        ).show();
    }
}

private LinearLayout.LayoutParams params(
        int width, int height) {

    LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(width, height);

    p.setMargins(0, dp(5), 0, dp(5));

    return p;
}

private int dp(float value) {
    return (int) (value * getResources()
            .getDisplayMetrics().density + 0.5f);
}

}
