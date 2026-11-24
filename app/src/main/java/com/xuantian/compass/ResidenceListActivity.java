package com.xuantian.compass;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class ResidenceListActivity extends Activity {

private ResidenceStorage storage;
private LinearLayout listContainer;

private final int gold = Color.rgb(220, 180, 105);
private final int background = Color.rgb(20, 17, 14);
private final int cardBackground = Color.rgb(34, 29, 24);
private final int textColor = Color.rgb(235, 225, 210);

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getWindow().setStatusBarColor(background);
    getWindow().setNavigationBarColor(background);

    storage = new ResidenceStorage(this);

    createUI();
    loadResidences();
}

private void createUI() {

    ScrollView scrollView = new ScrollView(this);
    scrollView.setBackgroundColor(background);
    scrollView.setFillViewport(true);

    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(dp(20), dp(18), dp(20), dp(25));

    scrollView.addView(root);

    TextView title = new TextView(this);
    title.setText("住宅档案管理");
    title.setTextColor(gold);
    title.setTextSize(25);
    title.setTypeface(null, Typeface.BOLD);
    title.setGravity(Gravity.CENTER);
    title.setIncludeFontPadding(true);

    root.addView(title, params(-1, 60));

    TextView subtitle = new TextView(this);
    subtitle.setText("本地保存的住宅资料");
    subtitle.setTextColor(Color.LTGRAY);
    subtitle.setTextSize(14);
    subtitle.setGravity(Gravity.CENTER);

    root.addView(subtitle, params(-1, 40));

    TextView countTitle = new TextView(this);
    countTitle.setText("已保存的住宅");
    countTitle.setTextColor(gold);
    countTitle.setTextSize(18);
    countTitle.setTypeface(null, Typeface.BOLD);
    countTitle.setPadding(0, dp(18), 0, dp(10));

    root.addView(countTitle);

    listContainer = new LinearLayout(this);
    listContainer.setOrientation(LinearLayout.VERTICAL);

    root.addView(listContainer);

    TextView refreshButton = createButton(
            "刷新住宅档案",
            gold,
            background
    );

    root.addView(refreshButton, params(-1, 54));

    refreshButton.setOnClickListener(v -> loadResidences());

    TextView backButton = createButton(
            "返回上一页",
            cardBackground,
            gold
    );

    root.addView(backButton, params(-1, 54));

    backButton.setOnClickListener(v -> finish());

    setContentView(scrollView);
}

/**
 * 读取数据库中的全部住宅
 */
private void loadResidences() {

    listContainer.removeAllViews();

    List<ResidenceData> residences = storage.getAll();

    if (residences == null || residences.isEmpty()) {

        TextView empty = new TextView(this);

        empty.setText(
                "目前还没有保存住宅资料。\n\n" +
                "请先返回住宅资料页面，填写资料并点击保存。"
        );

        empty.setTextColor(textColor);
        empty.setTextSize(15);
        empty.setGravity(Gravity.CENTER);
        empty.setLineSpacing(dp(6), 1f);
        empty.setPadding(dp(15), dp(30), dp(15), dp(30));

        listContainer.addView(
                empty,
                params(-1, -2)
        );

        return;
    }

    for (ResidenceData data : residences) {
        addResidenceCard(data);
    }
}

/**
 * 创建单套住宅档案卡片
 */
private void addResidenceCard(ResidenceData data) {

    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setPadding(dp(15), dp(12), dp(15), dp(12));

    GradientDrawable drawable = new GradientDrawable();
    drawable.setColor(cardBackground);
    drawable.setCornerRadius(dp(12));
    drawable.setStroke(dp(1), gold);

    card.setBackground(drawable);

    TextView name = new TextView(this);
    name.setText(data.name);
    name.setTextColor(gold);
    name.setTextSize(18);
    name.setTypeface(null, Typeface.BOLD);

    card.addView(name, params(-1, -2));

    TextView info = new TextView(this);

    info.setText(
            "建造年份：" + data.buildingYear +
            "\n所在楼层：" + data.floor +
            "\n住宅面积：" + data.area + " 平方米" +
            "\n档案编号：" + data.id
    );

    info.setTextColor(textColor);
    info.setTextSize(14);
    info.setLineSpacing(dp(5), 1f);
    info.setPadding(0, dp(10), 0, dp(5));

    card.addView(info, params(-1, -2));

    TextView detail = new TextView(this);
    detail.setText("查看档案详情");
    detail.setTextColor(gold);
    detail.setTextSize(14);
    detail.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
    detail.setPadding(0, dp(8), 0, dp(4));

    card.addView(detail, params(-1, 35));

    card.setClickable(true);
    card.setFocusable(true);

    card.setOnClickListener(v -> showDetails(data));

    listContainer.addView(card, params(-1, -2));
}

/**
 * 显示档案详细信息
 */
private void showDetails(ResidenceData data) {

    String message =
            "档案编号：" + data.id +
            "\n住宅名称：" + data.name +
            "\n建造年份：" + data.buildingYear +
            "\n所在楼层：" + data.floor +
            "\n住宅面积：" + data.area + " 平方米";

    new AlertDialog.Builder(this)
            .setTitle("住宅档案详情")
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show();
}

/**
 * 创建统一风格按钮
 */
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
    button.setIncludeFontPadding(true);
    button.setClickable(true);
    button.setFocusable(true);

    GradientDrawable drawable = new GradientDrawable();
    drawable.setColor(fillColor);
    drawable.setCornerRadius(dp(12));

    button.setBackground(drawable);

    return button;
}

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

    p.setMargins(0, dp(6), 0, dp(6));

    return p;
}

private int dp(float value) {

    return (int) (
            value * getResources()
                    .getDisplayMetrics().density + 0.5f
    );
}

@Override
protected void onDestroy() {
    super.onDestroy();

    if (storage != null) {
        storage.close();
    }
}

}
