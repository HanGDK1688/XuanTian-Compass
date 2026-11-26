package com.xuantian.compass;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.GridLayout;
import android.widget.ScrollView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class XuanKongActivity extends Activity {

    private int currentYear;
    private boolean showAnnual = false;
    private boolean dataReady = false;
    private boolean residenceMode = false;

    private int residenceId;
    private String residenceName = "";
    private String moveInDate = "";
    private String sittingMountain = "";
    private String facingMountain = "";
    private double facingAngle = -1;
    private double sittingAngle = -1;

    private LinearLayout root;
    private TextView titleText;
    private TextView infoText;
    private TextView centerText;
    private GridLayout grid;

    private final int BG = Color.rgb(20, 17, 14);
    private final int GOLD = Color.rgb(220, 180, 105);
    private final int WHITE = Color.WHITE;
    private final int GRAY = Color.rgb(150, 140, 125);

    private static final TimeZone BEIJING =
            TimeZone.getTimeZone("GMT+08:00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        readResidenceData();

        createUI();

        try {
            XuanKongCalculator.initialize(this);

            if (residenceMode) {
                currentYear = getEffectiveYearFromMoveInDate();
            } else {
                currentYear =
                        XuanKongCalculator.getCurrentEffectiveYear();
            }

            dataReady = true;

            if (residenceMode) {
                updateResidenceChart();
            } else {
                updateChart();
            }

        } catch (Exception e) {

            dataReady = false;

            infoText.setText(
                    "节气数据加载失败\n" + e.getMessage());

            centerText.setText("无法排盘");
            grid.removeAllViews();
        }
    }

    /**
     * 接收住宅档案传来的资料
     */
    private void readResidenceData() {

        if (!getIntent().hasExtra("residence_id")) {
            residenceMode = false;
            return;
        }

        residenceMode = true;

        residenceId = getIntent().getIntExtra(
                "residence_id", -1);

        residenceName = getIntent().getStringExtra(
                "residence_name");

        moveInDate = getIntent().getStringExtra(
                "move_in_date");

        facingAngle = getIntent().getDoubleExtra(
                "facing_angle", -1);

        sittingAngle = getIntent().getDoubleExtra(
                "sitting_angle", -1);

        facingMountain = getIntent().getStringExtra(
                "facing_mountain");

        sittingMountain = getIntent().getStringExtra(
                "sitting_mountain");

        if (residenceName == null) {
            residenceName = "未命名住宅";
        }

        if (moveInDate == null) {
            moveInDate = "";
        }

        if (facingMountain == null) {
            facingMountain = "";
        }

        if (sittingMountain == null) {
            sittingMountain = "";
        }
    }

    /**
     * 根据首次入住日期，计算立春节气年份
     */
    private int getEffectiveYearFromMoveInDate()
            throws Exception {

        if (!moveInDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException(
                    "住宅首次入住日期无效");
        }

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.US);

        format.setLenient(false);
        format.setTimeZone(BEIJING);

        Date date = format.parse(moveInDate + " 12:00:00");

        if (date == null) {
            throw new IllegalArgumentException(
                    "无法解析首次入住日期");
        }

        return SolarTermCalculator.getEffectiveYear(
                date.getTime());
    }

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private void createUI() {

        ScrollView scroll = new ScrollView(this);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(18), dp(16), dp(18), dp(16));

        scroll.addView(root);

        titleText = new TextView(this);
        titleText.setText("玄空飞星");
        titleText.setTextSize(27);
        titleText.setTextColor(GOLD);
        titleText.setTypeface(null, Typeface.BOLD);
        titleText.setGravity(Gravity.CENTER);

        root.addView(titleText,
                new LinearLayout.LayoutParams(-1, dp(55)));

        infoText = new TextView(this);
        infoText.setTextSize(16);
        infoText.setTextColor(WHITE);
        infoText.setGravity(Gravity.CENTER);
        infoText.setLineSpacing(dp(8), 1f);

        root.addView(infoText,
                new LinearLayout.LayoutParams(-1, dp(120)));

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
                new LinearLayout.LayoutParams(-1, dp(300));

        gridParams.setMargins(dp(5), dp(10), dp(5), dp(10));

        root.addView(grid, gridParams);

        if (!residenceMode) {
            createYearAndModeButtons();
            createTimeTestButton();
        }

        Button backButton = createButton(
                residenceMode ? "返回住宅档案" : "返回罗盘");

        backButton.setOnClickListener(v -> finish());

        root.addView(backButton,
                new LinearLayout.LayoutParams(-1, dp(55)));

        TextView disclaimer = new TextView(this);

        disclaimer.setText(
                "北京时间 · 立春换年 · 传统术数参考");

        disclaimer.setTextColor(GRAY);
        disclaimer.setTextSize(12);
        disclaimer.setGravity(Gravity.CENTER);

        root.addView(disclaimer,
                new LinearLayout.LayoutParams(-1, dp(30)));

        setContentView(scroll);
    }

    /**
     * 原有三元九运和流年飞星按钮
     */
    private void createYearAndModeButtons() {

        LinearLayout yearRow = new LinearLayout(this);
        yearRow.setOrientation(LinearLayout.HORIZONTAL);
        yearRow.setGravity(Gravity.CENTER);

        Button previous = createButton("◀ 上一年");

        previous.setOnClickListener(v -> {
            if (!dataReady) return;

            if (currentYear > 1900) {
                currentYear--;
                updateChart();
            }
        });

        Button next = createButton("下一年 ▶");

        next.setOnClickListener(v -> {
            if (!dataReady) return;

            if (currentYear < 2052) {
                currentYear++;
                updateChart();
            }
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
    }

    private void createTimeTestButton() {

        Button testButton = createButton("立春交接测试");

        testButton.setOnClickListener(v -> showTimeTestDialog());

        root.addView(testButton,
                new LinearLayout.LayoutParams(-1, dp(55)));
    }

    private Button createButton(String text) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextColor(GOLD);
        button.setTextSize(15);
        button.setBackgroundColor(Color.rgb(45, 36, 25));

        return button;
    }

    /**
     * 原有三元九运及流年飞星显示
     */
    private void updateChart() {

        if (!dataReady) return;

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

            chart =
                    XuanKongCalculator.getAnnualChart(currentYear);

            titleText.setText("流年飞星");

            infoText.setText(
                    "节气年份：" + currentYear
                    + "\n所属：" + period.name
                    + "\n时间：" + period.startYear
                    + "—" + period.endYear);

        } else {

            chart =
                    XuanKongCalculator.getPeriodChart(currentYear);

            titleText.setText("三元九运");

            infoText.setText(
                    "节气年份：" + currentYear
                    + "\n当前：" + period.name
                    + "\n时间：" + period.startYear
                    + "—" + period.endYear);
        }

        if (chart == null) {
            infoText.setText("排盘失败，请检查年份");
            centerText.setText("");
            grid.removeAllViews();
            return;
        }

        centerText.setText(
                "中宫：" + chart.centerStar
                + " · "
                + XuanKongCalculator.getStarName(
                        chart.centerStar));

        buildGrid(chart.palaceStars);
    }

    /**
     * 住宅专属玄空宅盘
     */
    private void updateResidenceChart() {

        if (!dataReady) return;

        if (sittingMountain.isEmpty()
                || facingMountain.isEmpty()) {

            titleText.setText("住宅宅盘");
            infoText.setText("住宅坐向资料不完整，无法排盘。");
            centerText.setText("");
            grid.removeAllViews();
            return;
        }

        XuanKongCalculator.ResidenceChart chart =
                XuanKongCalculator.getResidenceChart(
                        currentYear,
                        sittingMountain);

        if (chart == null) {

            titleText.setText("住宅宅盘");
            infoText.setText(
                    "住宅宅盘计算失败。\n"
                    + "请检查入住年份和坐山资料。");

            centerText.setText("");
            grid.removeAllViews();
            return;
        }

        titleText.setText("住宅玄空宅盘");

        infoText.setText(
                "住宅名称：" + residenceName
                + "\n首次入住：" + moveInDate
                + "\n节气年份：" + currentYear
                + "\n坐山：" + sittingMountain
                + " · 朝向：" + facingMountain
                + "\n朝向角度：" + formatAngle(facingAngle)
                + "° · 坐山角度：" + formatAngle(sittingAngle)
        );

        showResidenceCharts(chart);
    }

    /**
     * 显示运盘、山盘、向盘三张九宫图
     */
    private void showResidenceCharts(
            XuanKongCalculator.ResidenceChart chart) {

        root.removeView(grid);

        TextView periodTitle = createSectionTitle(
                "一、运盘 · 中宫入运星");

        root.addView(periodTitle);

        addResidenceGrid(chart.periodStars);

        TextView mountainTitle = createSectionTitle(
                "二、山盘 · 坐山：" + chart.sittingMountain);

        root.addView(mountainTitle);

        addResidenceGrid(chart.mountainStars);

        TextView facingTitle = createSectionTitle(
                "三、向盘 · 朝向：" + chart.facingMountain);

        root.addView(facingTitle);

        addResidenceGrid(chart.facingStars);

        TextView note = new TextView(this);

        note.setText(
                "说明：本页按当前选定的下卦法基础规则排盘，"
                + "暂未处理兼向、替卦。"
        );

        note.setTextColor(GRAY);
        note.setTextSize(13);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, dp(15), 0, dp(15));

        root.addView(note);
    }

    private TextView createSectionTitle(String text) {

        TextView title = new TextView(this);

        title.setText(text);
        title.setTextColor(GOLD);
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(18), 0, dp(8));

        return title;
    }

    /**
     * 住宅九宫盘使用相同的宫位排列
     */
    private void addResidenceGrid(int[] stars) {

        if (stars == null || stars.length != 9) {
            return;
        }

        GridLayout residenceGrid = new GridLayout(this);

        residenceGrid.setColumnCount(3);
        residenceGrid.setRowCount(3);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, dp(270));

        params.setMargins(dp(5), dp(5), dp(5), dp(10));

        root.addView(residenceGrid, params);

        int[][] order = {
                {1, 5, 3},
                {2, 0, 7},
                {6, 4, 8}
        };

        for (int row = 0; row < 3; row++) {

            for (int col = 0; col < 3; col++) {

                int index = order[row][col];

                addPalaceToGrid(
                        residenceGrid,
                        index,
                        stars[index]);
            }
        }
    }

    private void buildGrid(
            int[] stars) {

        grid.removeAllViews();

        int[][] order = {
                {1, 5, 3},
                {2, 0, 7},
                {6, 4, 8}
        };

        for (int row = 0; row < 3; row++) {

            for (int col = 0; col < 3; col++) {

                int index = order[row][col];

                addPalaceToGrid(
                        grid,
                        index,
                        stars[index]);
            }
        }
    }

    private void addPalaceToGrid(
            GridLayout target,
            int index,
            int star) {

        LinearLayout cell = new LinearLayout(this);

        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setBackgroundColor(Color.rgb(37, 30, 22));

        TextView palace = new TextView(this);

        palace.setText(
                XuanKongCalculator.getPalaceName(index));

        palace.setTextColor(GOLD);
        palace.setTextSize(13);
        palace.setGravity(Gravity.CENTER);

        TextView starNumber = new TextView(this);

        starNumber.setText(String.valueOf(star));
        starNumber.setTextColor(WHITE);
        starNumber.setTextSize(30);
        starNumber.setTypeface(Typeface.DEFAULT_BOLD);
        starNumber.setGravity(Gravity.CENTER);

        TextView starName = new TextView(this);

        starName.setText(
                XuanKongCalculator.getStarName(star));

        starName.setTextColor(GOLD);
        starName.setTextSize(12);
        starName.setGravity(Gravity.CENTER);

        TextView element = new TextView(this);

        element.setText(
                XuanKongCalculator.getElement(star) + "五行");

        element.setTextColor(GRAY);
        element.setTextSize(11);
        element.setGravity(Gravity.CENTER);

        cell.addView(palace);
        cell.addView(starNumber);
        cell.addView(starName);
        cell.addView(element);

        GridLayout.LayoutParams params =
                new GridLayout.LayoutParams();

        params.width = 0;
        params.height = 0;

        params.columnSpec = GridLayout.spec(
                GridLayout.UNDEFINED, 1f);

        params.rowSpec = GridLayout.spec(
                GridLayout.UNDEFINED, 1f);

        params.setMargins(dp(3), dp(3), dp(3), dp(3));

        target.addView(cell, params);
    }

    private String formatAngle(double angle) {

        if (angle < 0
                || Double.isNaN(angle)
                || Double.isInfinite(angle)) {
            return "未设置";
        }

        return String.format(
                Locale.CHINA,
                "%.2f",
                angle);
    }

    // ==============================
    // 指定北京时间测试
    // ==============================

    private void showTimeTestDialog() {

        if (!dataReady) return;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(20), dp(10), dp(20), dp(5));

        TextView hint = new TextView(this);

        hint.setText(
                "请输入北京时间\n格式：yyyy-MM-dd HH:mm:ss\n"
                + "例如：2024-02-04 16:00:00");

        hint.setTextColor(Color.BLACK);
        hint.setTextSize(15);

        layout.addView(hint);

        EditText input = new EditText(this);

        input.setSingleLine(true);
        input.setTextSize(16);
        input.setInputType(InputType.TYPE_CLASS_DATETIME);

        input.setHint("2024-02-04 16:00:00");

        layout.addView(input);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("立春交接测试")
                .setView(layout)
                .setNegativeButton("取消", null)
                .setPositiveButton("开始测试", null)
                .create();

        dialog.setOnShowListener(d -> {

            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        String value =
                                input.getText().toString().trim();

                        if (value.isEmpty()) {
                            input.setError("请输入北京时间");
                            return;
                        }

                        runTimeTest(value);
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    private void runTimeTest(String value) {

        try {

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.US);

            format.setLenient(false);
            format.setTimeZone(BEIJING);

            Date date = format.parse(value);

            if (date == null) {
                throw new IllegalArgumentException("日期解析失败");
            }

            long timestamp = date.getTime();

            int effectiveYear =
                    SolarTermCalculator.getEffectiveYear(
                            timestamp);

            XuanKongCalculator.PeriodInfo period =
                    XuanKongCalculator.getPeriodAt(
                            timestamp);

            int periodCenter =
                    XuanKongCalculator.getPeriodCenterStarAt(
                            timestamp);

            int annualCenter =
                    XuanKongCalculator.getAnnualCenterStarAt(
                            timestamp);

            if (period == null || periodCenter < 1) {
                throw new IllegalArgumentException(
                        "该时间不在当前支持的九运范围内");
            }

            String result =
                    "输入北京时间：\n"
                    + value
                    + "\n\n节气年份："
                    + effectiveYear
                    + "\n所属三元九运："
                    + period.name
                    + "\n\n运盘入中："
                    + periodCenter
                    + " · "
                    + XuanKongCalculator.getStarName(
                            periodCenter)
                    + "\n流年入中："
                    + annualCenter
                    + " · "
                    + XuanKongCalculator.getStarName(
                            annualCenter);

            new AlertDialog.Builder(this)
                    .setTitle("测试结果")
                    .setMessage(result)
                    .setPositiveButton("确定", null)
                    .show();

        } catch (Exception e) {

            new AlertDialog.Builder(this)
                    .setTitle("测试失败")
                    .setMessage(
                            "无法计算该时间：\n"
                            + e.getMessage())
                    .setPositiveButton("确定", null)
                    .show();
        }
    }
}
