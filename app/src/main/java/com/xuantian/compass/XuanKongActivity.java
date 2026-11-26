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
    private LinearLayout chartContainer;

    private TextView titleText;
    private TextView infoText;
    private TextView centerText;
    private GridLayout grid;

    private final int BG = Color.rgb(20, 17, 14);
    private final int GOLD = Color.rgb(220, 180, 105);
    private final int WHITE = Color.WHITE;
    private final int GRAY = Color.rgb(150, 140, 125);
    private final int CELL_BG = Color.rgb(37, 30, 22);

    private static final TimeZone BEIJING =
            TimeZone.getTimeZone("GMT+08:00");

    private static final int[][] PALACE_ORDER = {
            {1, 5, 3},
            {2, 0, 7},
            {6, 4, 8}
    };

    /*
     * 八运子山午向固定校验数据。
     * 数组索引：
     * 0中宫、1乾、2兑、3艮、4离、
     * 5坎、6坤、7震、8巽。
     */
    private static final int[] CHECK_PERIOD = {
            8, 9, 1, 2, 3, 4, 5, 6, 7
    };

    private static final int[] CHECK_MOUNTAIN = {
            4, 5, 6, 7, 8, 9, 1, 2, 3
    };

    private static final int[] CHECK_FACING = {
            3, 2, 1, 9, 8, 7, 6, 5, 4
    };

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

            if (chartContainer != null) {
                chartContainer.removeAllViews();
            }
        }
    }

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
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(14), dp(12), dp(14), dp(20));

        scroll.addView(root);

        titleText = new TextView(this);
        titleText.setText("玄空飞星");
        titleText.setTextSize(25);
        titleText.setTextColor(GOLD);
        titleText.setTypeface(null, Typeface.BOLD);
        titleText.setGravity(Gravity.CENTER);

        root.addView(titleText,
                new LinearLayout.LayoutParams(-1, dp(58)));

        infoText = new TextView(this);
        infoText.setTextSize(15);
        infoText.setTextColor(WHITE);
        infoText.setGravity(Gravity.CENTER);
        infoText.setLineSpacing(dp(5), 1f);
        infoText.setPadding(dp(5), dp(8), dp(5), dp(8));

        root.addView(infoText,
                new LinearLayout.LayoutParams(-1, -2));

        centerText = new TextView(this);
        centerText.setTextSize(18);
        centerText.setTextColor(GOLD);
        centerText.setGravity(Gravity.CENTER);

        root.addView(centerText,
                new LinearLayout.LayoutParams(-1, dp(45)));

        chartContainer = new LinearLayout(this);
        chartContainer.setOrientation(LinearLayout.VERTICAL);

        root.addView(chartContainer,
                new LinearLayout.LayoutParams(-1, -2));

        if (!residenceMode) {
            createYearAndModeButtons();
            createTimeTestButton();
            createChartCheckButton();
        }

        Button backButton = createButton(
                residenceMode ? "返回住宅档案" : "返回罗盘");

        backButton.setOnClickListener(v -> finish());

        LinearLayout.LayoutParams backParams =
                new LinearLayout.LayoutParams(-1, dp(52));

        backParams.setMargins(0, dp(16), 0, dp(8));

        root.addView(backButton, backParams);

        TextView disclaimer = new TextView(this);

        disclaimer.setText(
                "北京时间 · 立春换年 · 传统术数参考");

        disclaimer.setTextColor(GRAY);
        disclaimer.setTextSize(12);
        disclaimer.setGravity(Gravity.CENTER);

        root.addView(disclaimer,
                new LinearLayout.LayoutParams(-1, dp(35)));

        setContentView(scroll);
    }

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

    private void createChartCheckButton() {

        Button checkButton = createButton("玄空排盘算法校验");

        checkButton.setOnClickListener(v -> runChartCheck());

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, dp(55));

        params.setMargins(0, dp(6), 0, dp(4));

        root.addView(checkButton, params);
    }

    private Button createButton(String text) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextColor(GOLD);
        button.setTextSize(15);
        button.setBackgroundColor(CELL_BG);

        return button;
    }

    private void updateChart() {

        if (!dataReady) return;

        XuanKongCalculator.PeriodInfo period =
                XuanKongCalculator.getPeriod(currentYear);

        if (period == null) {

            infoText.setText("暂不支持该年份的三元九运计算");
            centerText.setText("");
            chartContainer.removeAllViews();

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
            chartContainer.removeAllViews();
            return;
        }

        centerText.setText(
                "中宫：" + chart.centerStar
                + " · "
                + XuanKongCalculator.getStarName(
                        chart.centerStar));

        chartContainer.removeAllViews();

        grid = createNinePalaceGrid(chart.palaceStars);

        chartContainer.addView(grid);
    }

    private void updateResidenceChart() {

        if (!dataReady) return;

        if (sittingMountain.isEmpty()
                || facingMountain.isEmpty()) {

            titleText.setText("住宅宅盘");
            infoText.setText("住宅坐向资料不完整，无法排盘。");
            centerText.setText("");
            chartContainer.removeAllViews();

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
            chartContainer.removeAllViews();

            return;
        }

        titleText.setText("住宅玄空宅盘");
        centerText.setText("");

        infoText.setText(
                "住宅名称：" + residenceName
                + "\n首次入住：" + moveInDate
                + "\n节气年份：" + currentYear
                + "\n坐山：" + sittingMountain
                + "    朝向：" + facingMountain
                + "\n朝向角度：" + formatAngle(facingAngle)
                + "°    坐山角度：" + formatAngle(sittingAngle)
                + "°"
        );

        showResidenceCharts(chart);
    }

    private void showResidenceCharts(
            XuanKongCalculator.ResidenceChart chart) {

        chartContainer.removeAllViews();

        addSectionTitle("一、运盘 · 中宫入运星");
        chartContainer.addView(
                createNinePalaceGrid(chart.periodStars));

        addSectionTitle(
                "二、山盘 · 坐山：" + chart.sittingMountain);

        chartContainer.addView(
                createNinePalaceGrid(chart.mountainStars));

        addSectionTitle(
                "三、向盘 · 朝向：" + chart.facingMountain);

        chartContainer.addView(
                createNinePalaceGrid(chart.facingStars));

        TextView note = new TextView(this);

        note.setText(
                "说明：本页按当前选定的下卦法基础规则排盘，"
                + "暂未处理兼向、替卦。");

        note.setTextColor(GRAY);
        note.setTextSize(13);
        note.setGravity(Gravity.CENTER);
        note.setLineSpacing(dp(4), 1f);
        note.setPadding(dp(8), dp(14), dp(8), dp(14));

        chartContainer.addView(note);
    }

    private void addSectionTitle(String text) {

        TextView title = new TextView(this);

        title.setText(text);
        title.setTextColor(GOLD);
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(dp(4), dp(18), dp(4), dp(12));

        chartContainer.addView(title,
                new LinearLayout.LayoutParams(-1, -2));
    }

    private GridLayout createNinePalaceGrid(int[] stars) {

        GridLayout result = new GridLayout(this);

        result.setColumnCount(3);
        result.setRowCount(3);
        result.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        result.setUseDefaultMargins(false);

        LinearLayout.LayoutParams gridParams =
                new LinearLayout.LayoutParams(-1, dp(306));

        gridParams.setMargins(dp(2), dp(2), dp(2), dp(10));

        result.setLayoutParams(gridParams);

        if (stars == null || stars.length != 9) {
            return result;
        }

        for (int row = 0; row < 3; row++) {

            for (int col = 0; col < 3; col++) {

                int index = PALACE_ORDER[row][col];

                LinearLayout cell =
                        createPalaceCell(index, stars[index]);

                GridLayout.LayoutParams params =
                        new GridLayout.LayoutParams();

                params.width = 0;
                params.height = dp(98);

                params.columnSpec = GridLayout.spec(
                        col, 1f);

                params.rowSpec = GridLayout.spec(row);

                params.setMargins(
                        dp(3), dp(3), dp(3), dp(3));

                result.addView(cell, params);
            }
        }

        return result;
    }

    private LinearLayout createPalaceCell(
            int index,
            int star) {

        LinearLayout cell = new LinearLayout(this);

        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setBackgroundColor(CELL_BG);
        cell.setPadding(dp(2), dp(3), dp(2), dp(3));

        TextView palace = new TextView(this);

        palace.setText(
                XuanKongCalculator.getPalaceName(index));

        palace.setTextColor(GOLD);
        palace.setTextSize(13);
        palace.setGravity(Gravity.CENTER);
        palace.setSingleLine(true);
        palace.setIncludeFontPadding(false);

        cell.addView(palace,
                new LinearLayout.LayoutParams(-1, dp(23)));

        TextView starNumber = new TextView(this);

        starNumber.setText(String.valueOf(star));
        starNumber.setTextColor(WHITE);
        starNumber.setTextSize(29);
        starNumber.setTypeface(Typeface.DEFAULT_BOLD);
        starNumber.setGravity(Gravity.CENTER);
        starNumber.setIncludeFontPadding(false);

        cell.addView(starNumber,
                new LinearLayout.LayoutParams(-1, dp(38)));

        TextView starName = new TextView(this);

        starName.setText(
                XuanKongCalculator.getStarName(star));

        starName.setTextColor(GOLD);
        starName.setTextSize(12);
        starName.setGravity(Gravity.CENTER);
        starName.setSingleLine(true);
        starName.setIncludeFontPadding(false);

        cell.addView(starName,
                new LinearLayout.LayoutParams(-1, dp(21)));

        TextView element = new TextView(this);

        element.setText(
                XuanKongCalculator.getElement(star) + "五行");

        element.setTextColor(GRAY);
        element.setTextSize(11);
        element.setGravity(Gravity.CENTER);
        element.setSingleLine(true);
        element.setIncludeFontPadding(false);

        cell.addView(element,
                new LinearLayout.LayoutParams(-1, dp(19)));

        return cell;
    }

    /*
     * 独立校验：
     * 使用固定案例八运子山午向，
     * 将当前正式计算器输出与预设九宫数据逐项比较。
     * 不修改正式算法。
     */
    private void runChartCheck() {

        if (!dataReady) {
            showCheckResult("校验无法开始：节气数据尚未加载。");
            return;
        }

        XuanKongCalculator.ResidenceChart chart =
                XuanKongCalculator.getResidenceChart(
                        2020, "子");

        if (chart == null) {
            showCheckResult(
                    "校验失败：计算器没有返回排盘结果。");
            return;
        }

        int passed = 0;
        int failed = 0;

        StringBuilder result = new StringBuilder();

        result.append("固定案例：八运子山午向\n");
        result.append("校验年份：2020\n\n");

        result.append("中宫入中星：\n");
        result.append("运盘：").append(chart.periodStars[0])
                .append("（预期8）\n");
        result.append("山盘：").append(chart.mountainCenterStar)
                .append("（预期4）\n");
        result.append("向盘：").append(chart.facingCenterStar)
                .append("（预期3）\n\n");

        result.append("顺逆飞方向：\n");
        result.append("山盘：").append(chart.mountainDirection)
                .append("（预期顺飞）\n");
        result.append("向盘：").append(chart.facingDirection)
                .append("（预期逆飞）\n\n");

        result.append("逐宫校验：\n");

        for (int i = 0; i < 9; i++) {

            boolean periodOK =
                    chart.periodStars[i] == CHECK_PERIOD[i];

            boolean mountainOK =
                    chart.mountainStars[i] == CHECK_MOUNTAIN[i];

            boolean facingOK =
                    chart.facingStars[i] == CHECK_FACING[i];

            if (periodOK) passed++;
            else failed++;

            if (mountainOK) passed++;
            else failed++;

            if (facingOK) passed++;
            else failed++;

            result.append(XuanKongCalculator.getPalaceName(i))
                    .append("：");

            result.append("运")
                    .append(chart.periodStars[i])
                    .append(periodOK ? "✓ " : "× ");

            result.append("山")
                    .append(chart.mountainStars[i])
                    .append(mountainOK ? "✓ " : "× ");

            result.append("向")
                    .append(chart.facingStars[i])
                    .append(facingOK ? "✓" : "×");

            result.append("\n");
        }

        boolean centerOK =
                chart.periodStars[0] == 8
                && chart.mountainCenterStar == 4
                && chart.facingCenterStar == 3;

        boolean directionOK =
                chart.mountainForward
                && !chart.facingForward;

        result.append("\n九宫数据：")
                .append(passed)
                .append("项通过，")
                .append(failed)
                .append("项不通过。\n");

        result.append("中宫检查：")
                .append(centerOK ? "通过" : "不通过")
                .append("\n");

        result.append("顺逆检查：")
                .append(directionOK ? "通过" : "不通过")
                .append("\n\n");

        boolean allOK =
                failed == 0 && centerOK && directionOK;

        result.append("最终结果：")
                .append(allOK ? "校验通过" : "校验未通过");

        result.append(
                "\n\n注意：本测试只核对当前程序与固定案例数据是否一致，"
                + "不代表所有玄空流派规则均已得到验证。");

        showCheckResult(result.toString());
    }

    private void showCheckResult(String message) {

        TextView text = new TextView(this);

        text.setText(message);
        text.setTextColor(Color.BLACK);
        text.setTextSize(15);
        text.setLineSpacing(dp(4), 1f);
        text.setPadding(dp(18), dp(12), dp(18), dp(12));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(text);

        new AlertDialog.Builder(this)
                .setTitle("玄空排盘算法校验")
                .setView(scroll)
                .setPositiveButton("确定", null)
                .show();
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
