package com.xuantian.compass;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.GridLayout;

import java.util.Calendar;

public class XuanKongActivity extends Activity {

    private int currentYear;
    private boolean showAnnual = false;

    private LinearLayout root;
    private TextView titleText;
    private TextView infoText;
    private TextView centerText;
    private GridLayout grid;

    private final int BG = Color.rgb(20, 17, 14);
    private final int GOLD = Color.rgb(220, 180, 105);
    private final int WHITE = Color.WHITE;
    private final int GRAY = Color.rgb(150, 140, 125);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        currentYear = Calendar.getInstance()
                .get(Calendar.YEAR);

        createUI();
        updateChart();
    }

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private void createUI() {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(18), dp(16), dp(18), dp(16));

        titleText = new TextView(this);
        titleText.setText("玄空飞星");
        titleText.setTextSize(27);
        titleText.setTextColor(GOLD);
        titleText.setTypeface(null, Typeface.BOLD);
        titleText.setGravity(Gravity.CENTER);

        root.addView(titleText,
                new LinearLayout.LayoutParams(-1, dp(55)));

        infoText = new TextView(this);
        infoText.setTextSize(17);
        infoText.setTextColor(WHITE);
        infoText.setGravity(Gravity.CENTER);
        infoText.setLineSpacing(dp(8), 1f);

        root.addView(infoText,
                new LinearLayout.LayoutParams(-1, dp(100)));

        centerText = new TextView(this);
        centerText.setTextSize(20);
        centerText.setTextColor(GOLD);
        centerText.setGravity(Gravity.CENTER);

        root.addView(centerText,
                new LinearLayout.LayoutParams(-1, dp(50)));

        grid = new GridLayout(this);
        grid.setColumnCount(3);
        grid.setRowCount(3);

        LinearLayout.LayoutParams gridParams =
                new LinearLayout.LayoutParams(-1, 0, 1f);

        gridParams.setMargins(dp(5), dp(10), dp(5), dp(10));

        root.addView(grid, gridParams);

        LinearLayout yearRow = new LinearLayout(this);
        yearRow.setOrientation(LinearLayout.HORIZONTAL);
        yearRow.setGravity(Gravity.CENTER);

        Button previous = createButton("◀ 上一年");

        previous.setOnClickListener(v -> {
            currentYear--;
            updateChart();
        });

        Button next = createButton("下一年 ▶");

        next.setOnClickListener(v -> {
            currentYear++;
            updateChart();
        });

        yearRow.addView(previous,
                new LinearLayout.LayoutParams(0, dp(50), 1f));

        yearRow.addView(next,
                new LinearLayout.LayoutParams(0, dp(50), 1f));

        root.addView(yearRow,
                new LinearLayout.LayoutParams(-1, dp(55)));

        LinearLayout modeRow = new LinearLayout(this);
        modeRow.setOrientation(LinearLayout.HORIZONTAL);
        modeRow.setGravity(Gravity.CENTER);

        Button periodButton = createButton("三元九运");

        periodButton.setOnClickListener(v -> {
            showAnnual = false;
            updateChart();
        });

        Button annualButton = createButton("流年飞星");

        annualButton.setOnClickListener(v -> {
            showAnnual = true;
            updateChart();
        });

        modeRow.addView(periodButton,
                new LinearLayout.LayoutParams(0, dp(55), 1f));

        modeRow.addView(annualButton,
                new LinearLayout.LayoutParams(0, dp(55), 1f));

        root.addView(modeRow,
                new LinearLayout.LayoutParams(-1, dp(60)));

        Button backButton = createButton("返回罗盘");

        backButton.setOnClickListener(v -> finish());

        root.addView(backButton,
                new LinearLayout.LayoutParams(-1, dp(55)));

        TextView disclaimer = new TextView(this);

        disclaimer.setText("玄空飞星基础计算 · 传统术数参考");
        disclaimer.setTextColor(GRAY);
        disclaimer.setTextSize(12);
        disclaimer.setGravity(Gravity.CENTER);

        root.addView(disclaimer,
                new LinearLayout.LayoutParams(-1, dp(30)));

        setContentView(root);
    }

    private Button createButton(String text) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextColor(GOLD);
        button.setTextSize(15);

        button.setBackgroundColor(Color.rgb(45, 36, 25));

        return button;
    }

    private void updateChart() {

        XuanKongCalculator.PeriodInfo period =
                XuanKongCalculator.getPeriod(currentYear);

        if (period == null) {

            infoText.setText("暂不支持该年份的三元九运计算");
            centerText.setText("");
            grid.removeAllViews();

            return;
        }

        XuanKongCalculator.FlyingChart chart;

        if (showAnnual) {

            chart = XuanKongCalculator.getAnnualChart(currentYear);

            titleText.setText("流年飞星");

            infoText.setText(
                    "公历年份：" + currentYear
                    + "\n所属：" + period.name
                    + "\n时间：" + period.startYear
                    + "—" + period.endYear);

        } else {

            chart = XuanKongCalculator.getPeriodChart(currentYear);

            titleText.setText("三元九运");

            infoText.setText(
                    "当前：" + period.name
                    + "\n时间：" + period.startYear
                    + "—" + period.endYear
                    + "\n年份：" + currentYear);
        }

        centerText.setText(
                "中宫：" + chart.centerStar
                + " · "
                + XuanKongCalculator.getStarName(chart.centerStar));

        buildGrid(chart);
    }

    private void buildGrid(XuanKongCalculator.FlyingChart chart) {

        grid.removeAllViews();

        int[][] order = {
                {1, 5, 3},
                {2, 0, 7},
                {6, 4, 8}
        };

        for (int row = 0; row < 3; row++) {

            for (int col = 0; col < 3; col++) {

                int index = order[row][col];
                int star = chart.palaceStars[index];

                addPalace(index, star);
            }
        }
    }

    private void addPalace(int index, int star) {

        LinearLayout cell = new LinearLayout(this);

        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setBackgroundColor(Color.rgb(37, 30, 22));

        TextView palace = new TextView(this);

        palace.setText(XuanKongCalculator.getPalaceName(index));
        palace.setTextColor(GOLD);
        palace.setTextSize(13);
        palace.setGravity(Gravity.CENTER);

        TextView starNumber = new TextView(this);

        starNumber.setText(String.valueOf(star));
        starNumber.setTextColor(WHITE);
        starNumber.setTextSize(34);
        starNumber.setTypeface(Typeface.DEFAULT_BOLD);
        starNumber.setGravity(Gravity.CENTER);

        TextView starName = new TextView(this);

        starName.setText(XuanKongCalculator.getStarName(star));
        starName.setTextColor(GOLD);
        starName.setTextSize(12);
        starName.setGravity(Gravity.CENTER);

        TextView element = new TextView(this);

        element.setText(XuanKongCalculator.getElement(star) + "五行");
        element.setTextColor(GRAY);
        element.setTextSize(12);
        element.setGravity(Gravity.CENTER);

        cell.addView(palace);
        cell.addView(starNumber);
        cell.addView(starName);
        cell.addView(element);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();

        params.width = 0;
        params.height = 0;

        params.columnSpec = GridLayout.spec(
                GridLayout.UNDEFINED, 1f);

        params.rowSpec = GridLayout.spec(
                GridLayout.UNDEFINED, 1f);

        params.setMargins(dp(3), dp(3), dp(3), dp(3));

        grid.addView(cell, params);
    }
}
