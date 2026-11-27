package com.xuantian.compass;

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

public class XuanKongActivity extends BaseActivity {

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

    // V1.6：默认采用沈氏规则
    private ResidenceReplacementEngine.Rule replacementRule =
            ResidenceReplacementEngine.Rule.SHEN;

    private LinearLayout root;
    private LinearLayout chartContainer;

    private TextView titleText;
    private TextView infoText;
    private TextView centerText;
    private GridLayout grid;

    private Button shenRuleButton;
    private Button zhongzhouRuleButton;

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

        // V1.6：住宅模式增加流派选择
        if (residenceMode) {
            createReplacementRuleButtons();
        }

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

    /**
     * V1.6：沈氏 / 中州规则切换。
     */
    private void createReplacementRuleButtons() {

        TextView ruleTitle = new TextView(this);

        ruleTitle.setText("替卦规则选择");
        ruleTitle.setTextColor(GOLD);
        ruleTitle.setTextSize(14);
        ruleTitle.setGravity(Gravity.CENTER);

        root.addView(ruleTitle,
                new LinearLayout.LayoutParams(-1, dp(28)));

        LinearLayout row = new LinearLayout(this);

        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        shenRuleButton = createButton("沈氏规则");

        zhongzhouRuleButton = createButton("中州规则");

        shenRuleButton.setOnClickListener(v -> {

            if (!dataReady) return;

            replacementRule =
                    ResidenceReplacementEngine.Rule.SHEN;

            updateRuleButtonState();
            updateResidenceChart();
        });

        zhongzhouRuleButton.setOnClickListener(v -> {

            if (!dataReady) return;

            replacementRule =
                    ResidenceReplacementEngine.Rule.ZHONGZHOU;

            updateRuleButtonState();
            updateResidenceChart();
        });

        row.addView(shenRuleButton,
                new LinearLayout.LayoutParams(0, dp(48), 1f));

        row.addView(zhongzhouRuleButton,
                new LinearLayout.LayoutParams(0, dp(48), 1f));

        root.addView(row,
                new LinearLayout.LayoutParams(-1, dp(54)));

        updateRuleButtonState();
    }

    /**
     * 更新流派按钮状态。
     */
    private void updateRuleButtonState() {

        if (shenRuleButton == null
                || zhongzhouRuleButton == null) {
            return;
        }

        if (replacementRule
                == ResidenceReplacementEngine.Rule.SHEN) {

            shenRuleButton.setText("✓ 沈氏规则");
            zhongzhouRuleButton.setText("中州规则");

        } else {

            shenRuleButton.setText("沈氏规则");
            zhongzhouRuleButton.setText("✓ 中州规则");
        }
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

        // 保留原有二参数接口，不改变基础宅盘算法
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
                + "\n当前替卦规则："
                + ResidenceReplacementEngine.getRuleName(
                        replacementRule)
        );

        showResidenceCharts(chart);
    }

    // 住宅三盘展示
    private void showResidenceCharts(
            XuanKongCalculator.ResidenceChart chart) {

        chartContainer.removeAllViews();

        addSectionTitle("一、三盘合参 · 九宫综合排盘");

        TextView guide = new TextView(this);
        guide.setText("每宫同时显示运星、山星、向星及五行关系");
        guide.setTextColor(GRAY);
        guide.setTextSize(12);
        guide.setGravity(Gravity.CENTER);
        guide.setPadding(0, dp(4), 0, dp(10));

        chartContainer.addView(guide);

        chartContainer.addView(
                createCombinedNinePalaceGrid(chart)
        );

        addSectionTitle("二、运盘 · 中宫入运星");

        chartContainer.addView(
                createNinePalaceGrid(chart.periodStars)
        );

        addSectionTitle(
                "三、山盘 · 坐山：" + chart.sittingMountain
        );

        chartContainer.addView(
                createNinePalaceGrid(chart.mountainStars)
        );

        addSectionTitle(
                "四、向盘 · 朝向：" + chart.facingMountain
        );

        chartContainer.addView(
                createNinePalaceGrid(chart.facingStars)
        );

        addSectionTitle(
                "五、兼向与替卦分析 · "
                + ResidenceReplacementEngine.getRuleName(
                        replacementRule)
        );

        addReplacementAnalysis(chart);

        addSectionTitle("六、替卦候选九宫盘");

        showReplacementCandidateCharts(chart);

        addSectionTitle("七、八宅分析");

        EightMansionsCalculator.Result eightResult =
                EightMansionsCalculator.calculate(
                        chart.sittingMountain);

        TextView eightText = new TextView(this);

        if (eightResult == null) {

            eightText.setText(
                    "坐山资料无效，暂时无法进行八宅分析。");

        } else {

            eightText.setText(
                    EightMansionsCalculator.format(eightResult));
        }

        eightText.setTextColor(WHITE);
        eightText.setTextSize(15);
        eightText.setLineSpacing(dp(7), 1f);
        eightText.setPadding(
                dp(16), dp(14), dp(16), dp(14));

        eightText.setBackgroundColor(CELL_BG);

        chartContainer.addView(eightText,
                new LinearLayout.LayoutParams(-1, -2));

        TextView note = new TextView(this);

        note.setText(
                "说明：基础三盘保留原下卦排盘；"
                + "替卦候选盘另列方案A、方案B，不覆盖基础盘。\n"
                + "坐山与朝向分别判断，互不影响。\n"
                + "档案原山向与角度识别山向分别传入所选流派规则。\n"
                + "中州规则以方法A为正式结果，方法B作为对照。\n"
                + "五行关系用于传统术数参考，不代表现实结果的确定预测。\n"
                + "八宅分析按住宅坐山所属宅卦计算，"
                + "不涉及户主命卦。");

        note.setTextColor(GRAY);
        note.setTextSize(13);
        note.setGravity(Gravity.CENTER);
        note.setLineSpacing(dp(4), 1f);
        note.setPadding(dp(8), dp(14), dp(8), dp(14));

        chartContainer.addView(note);
    }

    /**
     * V1.6：分别计算坐山、朝向的替卦分析。
     */
    private void addReplacementAnalysis(
            XuanKongCalculator.ResidenceChart chart) {

        if (chart == null) {
            addAnalysisText("住宅排盘数据无效，无法进行替卦分析。");
            return;
        }

        addAnalysisText(
                "当前规则：" +
                ResidenceReplacementEngine.getRuleName(
                        replacementRule)
                + "\n坐山和朝向分别计算，不共用替卦结果。");

        addOneReplacementAnalysis(
                "坐山",
                sittingAngle,
                sittingMountain,
                chart.mountainCenterStar
        );

        addOneReplacementAnalysis(
                "朝向",
                facingAngle,
                facingMountain,
                chart.facingCenterStar
        );
    }

    /**
     * 单独分析坐山或朝向。
     */
    private void addOneReplacementAnalysis(
            String label,
            double angle,
            String savedMountain,
            int originalStar) {

        StringBuilder result = new StringBuilder();

        result.append("【").append(label).append("】\n");

        result.append("档案原山向：")
                .append(savedMountain)
                .append("\n");

        result.append("原运星：")
                .append(originalStar)
                .append(" · ")
                .append(XuanKongCalculator.getStarName(originalStar))
                .append("\n");

        if (!isValidAngle(angle)) {

            result.append("测量角度：未设置\n");
            result.append("无法判断下卦或替卦范围。");

            addAnalysisText(result.toString());
            return;
        }

        XuanKongReplacementCalculator.AngleResult angleResult =
                XuanKongReplacementCalculator.analyzeBearing(
                        angle);

        if (angleResult == null) {

            result.append("角度分析失败。");
            addAnalysisText(result.toString());
            return;
        }

        result.append("测量角度：")
                .append(formatAngle(angle))
                .append("°\n");

        result.append("中心角度：")
                .append(formatAngle(angleResult.centerAngle))
                .append("°\n");

        result.append("偏离中心：")
                .append(formatSignedAngle(angleResult.deviation))
                .append("°\n");

        result.append("角度判定：")
                .append(angleResult.classification)
                .append("\n");

        result.append("实际识别山向：")
                .append(angleResult.mountain)
                .append("\n");

        result.append("兼向方向：")
                .append(angleResult.direction)
                .append("\n");

        if (angleResult.isBoundary) {

            result.append(
                    "当前角度处于边界区域，需要人工复核，"
                    + "暂不进行替卦计算。");

            addAnalysisText(result.toString());
            return;
        }

        if (angleResult.isDownGua) {

            result.append(
                    "当前角度属于下卦范围，不启动替卦计算。");

            addAnalysisText(result.toString());
            return;
        }

        if (!angleResult.isReplacementGua) {

            result.append(
                    "当前角度不属于已定义的替卦范围。");

            addAnalysisText(result.toString());
            return;
        }

        /*
         * 注意：
         * savedMountain 是档案原山向。
         * angleResult.mountain 是角度实际识别山向。
         *
         * 两者分别传入引擎，不再提前拦截。
         */
        ResidenceReplacementEngine.Result replacement =
                ResidenceReplacementEngine.calculate(
                        replacementRule,
                        originalStar,
                        savedMountain,
                        angleResult.mountain,
                        true
                );

        appendEngineResult(
                result,
                replacement
        );

        addAnalysisText(result.toString());
    }

    /**
     * 将引擎结果转换为界面文字。
     */
    private void appendEngineResult(
            StringBuilder result,
            ResidenceReplacementEngine.Result replacement) {

        if (replacement == null) {

            result.append("\n替卦引擎没有返回结果。");
            return;
        }

        result.append("\n规则：")
                .append(
                        ResidenceReplacementEngine.getRuleName(
                                replacement.rule))
                .append("\n");

        result.append("档案原山向：")
                .append(replacement.originalMountain)
                .append("\n");

        result.append("实际相邻山向：")
                .append(replacement.adjacentMountain)
                .append("\n");

        if (replacement.rule
                == ResidenceReplacementEngine.Rule.SHEN) {

            XuanKongReplacementCalculator.ReplacementResult shen =
                    replacement.shenResult;

            if (shen == null) {

                result.append(replacement.description);
                return;
            }

            result.append("沈氏规则结果：\n");

            result.append("原运星所在宫：")
                    .append(getPalaceNameByIndex(
                            shen.originalPalace))
                    .append("\n");

            result.append("三元龙：")
                    .append(getDragonNameByIndex(
                            shen.dragonIndex))
                    .append("\n");

            result.append("替卦山：")
                    .append(shen.replacementMountain)
                    .append("\n");

            result.append("替星：")
                    .append(shen.replacementStar)
                    .append(" · ")
                    .append(XuanKongCalculator.getStarName(
                            shen.replacementStar))
                    .append("\n");

            result.append("替星原宫：")
                    .append(getPalaceNameByIndex(
                            shen.replacementPalace))
                    .append("\n");

            result.append("方案A：")
                    .append(shen.methodA)
                    .append("\n");

            result.append("方案B：")
                    .append(shen.methodB)
                    .append("\n");

            result.append("说明：")
                    .append(replacement.description);

        } else {

            ZhongzhouReplacementRules.Result zhongzhou =
                    replacement.zhongzhouResult;

            if (zhongzhou == null) {

                result.append(replacement.description);
                return;
            }

            result.append("中州规则状态：")
                    .append(
                            ResidenceReplacementEngine
                                    .getZhongzhouStatusText(
                                            zhongzhou.status))
                    .append("\n");

            result.append("原山元龙：")
                    .append(getDragonNameByIndex(
                            zhongzhou.dragonIndex))
                    .append("\n");

            result.append("原山宫位：")
                    .append(getPalaceNameByIndex(
                            zhongzhou.originalPalace))
                    .append("\n");

            result.append("实际山宫位：")
                    .append(getPalaceNameByIndex(
                            zhongzhou.adjacentPalace))
                    .append("\n");

            if (zhongzhou.replacementMountain != null) {

                result.append("替卦山：")
                        .append(zhongzhou.replacementMountain)
                        .append("\n");
            }

            if (zhongzhou.replacementStar >= 1
                    && zhongzhou.replacementStar <= 9) {

                result.append("替星：")
                        .append(zhongzhou.replacementStar)
                        .append(" · ")
                        .append(XuanKongCalculator.getStarName(
                                zhongzhou.replacementStar))
                        .append("\n");

                result.append("替星原宫：")
                        .append(getPalaceNameByIndex(
                                zhongzhou.replacementPalace))
                        .append("\n");
            }

            result.append("方法A：")
                    .append(zhongzhou.methodA)
                    .append("（正式方法）\n");

            result.append("方法B：")
                    .append(zhongzhou.methodB)
                    .append("（对照方法）\n");

            result.append("说明：")
                    .append(replacement.description);
        }
    }

    /**
     * V1.6：替卦候选盘直接使用 ResidenceReplacementEngine。
     */
    private void showReplacementCandidateCharts(
            XuanKongCalculator.ResidenceChart chart) {

        if (chart == null) {
            addAnalysisText("住宅排盘数据无效，无法显示候选盘。");
            return;
        }

        TextView guide = new TextView(this);

        guide.setText(
                "候选盘由当前选定流派独立计算。\n"
                + "坐山和朝向分别展示。\n"
                + "中州方法A为正式结果，方法B为对照结果。\n"
                + "出卦、五黄特殊、无替星、下卦及边界状态不显示普通候选盘。");

        guide.setTextColor(GRAY);
        guide.setTextSize(12);
        guide.setGravity(Gravity.CENTER);
        guide.setLineSpacing(dp(4), 1f);
        guide.setPadding(dp(8), dp(4), dp(8), dp(12));

        chartContainer.addView(guide);

        showOneReplacementCandidate(
                "坐山",
                sittingAngle,
                sittingMountain,
                chart.mountainCenterStar
        );

        showOneReplacementCandidate(
                "朝向",
                facingAngle,
                facingMountain,
                chart.facingCenterStar
        );
    }

    /**
     * 单独展示坐山或朝向候选盘。
     */
    private void showOneReplacementCandidate(
            String label,
            double angle,
            String savedMountain,
            int originalStar) {

        TextView title = new TextView(this);

        title.setText("【" + label + "替卦候选盘】");
        title.setTextColor(GOLD);
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(dp(4), dp(10), dp(4), dp(8));

        chartContainer.addView(title);

        if (!isValidAngle(angle)) {

            addAnalysisText(
                    label + "角度尚未设置，暂不显示替卦候选盘。");

            return;
        }

        XuanKongReplacementCalculator.AngleResult angleResult =
                XuanKongReplacementCalculator.analyzeBearing(
                        angle);

        if (angleResult == null) {

            addAnalysisText(
                    label + "角度分析失败，暂不显示候选盘。");

            return;
        }

        if (angleResult.isBoundary) {

            addAnalysisText(
                    label + "当前角度处于边界区域，"
                    + "需要人工复核，暂不显示候选盘。");

            return;
        }

        if (angleResult.isDownGua) {

            addAnalysisText(
                    label + "当前角度属于下卦范围，不需要替卦候选盘。");

            return;
        }

        if (!angleResult.isReplacementGua) {

            addAnalysisText(
                    label + "当前角度不属于替卦范围，"
                    + "暂不显示候选盘。");

            return;
        }

        /*
         * 不再要求档案山向必须等于角度识别山向。
         * 两者作为独立参数进入规则引擎。
         */
        ResidenceReplacementEngine.Result replacement =
                ResidenceReplacementEngine.calculate(
                        replacementRule,
                        originalStar,
                        savedMountain,
                        angleResult.mountain,
                        true
                );

        if (replacement == null) {

            addAnalysisText(
                    label + "替卦引擎计算失败。");

            return;
        }

        if (replacement.rule
                == ResidenceReplacementEngine.Rule.ZHONGZHOU) {

            ZhongzhouReplacementRules.Result zhongzhou =
                    replacement.zhongzhouResult;

            if (zhongzhou != null
                    && zhongzhou.status
                    == ZhongzhouReplacementRules.Status.OUT_OF_PALACE) {

                addAnalysisText(
                        label + "："
                        + ResidenceReplacementEngine
                                .getZhongzhouStatusText(
                                        zhongzhou.status)
                        + "\n"
                        + "已保留替星分析信息，但不生成普通候选九宫盘。\n"
                        + replacement.description);

                return;
            }

            if (zhongzhou != null
                    && zhongzhou.status
                    == ZhongzhouReplacementRules.Status.FIVE_YELLOW_SPECIAL) {

                addAnalysisText(
                        label + "："
                        + ResidenceReplacementEngine
                                .getZhongzhouStatusText(
                                        zhongzhou.status)
                        + "\n"
                        + replacement.description);

                return;
            }
        }

        if (!replacement.hasReplacement) {

            addAnalysisText(
                    label + "：当前规则没有生成有效替卦候选盘。\n"
                    + replacement.description);

            return;
        }

        if (!replacement.availableA
                || !isValidCandidateChart(replacement.starsA)) {

            addAnalysisText(
                    label + "：方案A候选盘数据无效，不能显示。");

            return;
        }

        if (!replacement.availableB
                || !isValidCandidateChart(replacement.starsB)) {

            addAnalysisText(
                    label + "：方案B候选盘数据无效，不能显示。");

            return;
        }

        StringBuilder info = new StringBuilder();

        info.append("当前规则：")
                .append(
                        ResidenceReplacementEngine.getRuleName(
                                replacement.rule))
                .append("\n");

        info.append("档案原山向：")
                .append(savedMountain)
                .append("\n");

        info.append("角度实际识别山向：")
                .append(angleResult.mountain)
                .append("\n");

        info.append("角度：")
                .append(formatAngle(angle))
                .append("°\n");

        info.append("角度判定：")
                .append(angleResult.classification)
                .append("\n");

        info.append("原运星：")
                .append(originalStar)
                .append(" · ")
                .append(XuanKongCalculator.getStarName(
                        originalStar))
                .append("\n");

        if (replacement.rule
                == ResidenceReplacementEngine.Rule.SHEN
                && replacement.shenResult != null) {

            info.append("替卦山：")
                    .append(replacement.shenResult.replacementMountain)
                    .append("\n");

            info.append("替星：")
                    .append(replacement.shenResult.replacementStar)
                    .append("\n");

        } else if (replacement.zhongzhouResult != null) {

            info.append("中州状态：")
                    .append(
                            ResidenceReplacementEngine
                                    .getZhongzhouStatusText(
                                            replacement.zhongzhouResult.status))
                    .append("\n");

            info.append("替卦山：")
                    .append(replacement.zhongzhouResult.replacementMountain)
                    .append("\n");

            info.append("替星：")
                    .append(replacement.zhongzhouResult.replacementStar)
                    .append("\n");
        }

        addAnalysisText(info.toString());

        // 方案A
        TextView methodATitle = new TextView(this);

        String methodAName =
                replacement.rule
                == ResidenceReplacementEngine.Rule.ZHONGZHOU
                ? "方案 A · 正式方法"
                : "方案 A";

        methodATitle.setText(
                methodAName
                + " · "
                + getDirectionText(
                        replacement.rule,
                        replacement,
                        true));

        methodATitle.setTextColor(GOLD);
        methodATitle.setTextSize(16);
        methodATitle.setTypeface(null, Typeface.BOLD);
        methodATitle.setGravity(Gravity.CENTER);
        methodATitle.setPadding(
                dp(4), dp(12), dp(4), dp(4));

        chartContainer.addView(methodATitle);

        chartContainer.addView(
                createNinePalaceGrid(replacement.starsA));

        // 方案B
        TextView methodBTitle = new TextView(this);

        String methodBName =
                replacement.rule
                == ResidenceReplacementEngine.Rule.ZHONGZHOU
                ? "方案 B · 对照方法"
                : "方案 B";

        methodBTitle.setText(
                methodBName
                + " · "
                + getDirectionText(
                        replacement.rule,
                        replacement,
                        false));

        methodBTitle.setTextColor(GOLD);
        methodBTitle.setTextSize(16);
        methodBTitle.setTypeface(null, Typeface.BOLD);
        methodBTitle.setGravity(Gravity.CENTER);
        methodBTitle.setPadding(
                dp(4), dp(12), dp(4), dp(4));

        chartContainer.addView(methodBTitle);

        chartContainer.addView(
                createNinePalaceGrid(replacement.starsB));

        TextView comparison = new TextView(this);

        boolean sameDirection =
                getDirectionText(
                        replacement.rule,
                        replacement,
                        true).equals(
                        getDirectionText(
                                replacement.rule,
                                replacement,
                                false));

        comparison.setText(
                "方案A与方案B："
                + (sameDirection
                ? "顺逆方向一致"
                : "顺逆方向不同"));

        comparison.setTextColor(GRAY);
        comparison.setTextSize(12);
        comparison.setGravity(Gravity.CENTER);
        comparison.setPadding(
                dp(5), dp(4), dp(5), dp(10));

        chartContainer.addView(comparison);
    }

    /**
     * 获取当前规则的顺逆方向文字。
     */
    private String getDirectionText(
            ResidenceReplacementEngine.Rule rule,
            ResidenceReplacementEngine.Result result,
            boolean methodA) {

        boolean forward;

        if (rule == ResidenceReplacementEngine.Rule.SHEN) {

            if (result.shenResult == null) {
                return "方向未知";
            }

            forward = methodA
                    ? result.shenResult.methodAForward
                    : result.shenResult.methodBForward;

        } else {

            if (result.zhongzhouResult == null) {
                return "方向未知";
            }

            forward = methodA
                    ? result.zhongzhouResult.methodAForward
                    : result.zhongzhouResult.methodBForward;
        }

        return forward ? "顺飞" : "逆飞";
    }

    /**
     * 检查候选九宫盘是否有效。
     */
    private boolean isValidCandidateChart(int[] stars) {

        if (stars == null || stars.length != 9) {
            return false;
        }

        boolean[] found = new boolean[10];

        for (int star : stars) {

            if (star < 1 || star > 9) {
                return false;
            }

            if (found[star]) {
                return false;
            }

            found[star] = true;
        }

        for (int i = 1; i <= 9; i++) {

            if (!found[i]) {
                return false;
            }
        }

        return true;
    }

    private boolean isValidAngle(double angle) {

        return angle >= 0
                && !Double.isNaN(angle)
                && !Double.isInfinite(angle);
    }

    private void addAnalysisText(String message) {

        TextView text = new TextView(this);

        text.setText(message);
        text.setTextColor(WHITE);
        text.setTextSize(14);
        text.setLineSpacing(dp(5), 1f);

        text.setPadding(
                dp(15), dp(14), dp(15), dp(14));

        text.setBackgroundColor(CELL_BG);

        chartContainer.addView(
                text,
                new LinearLayout.LayoutParams(-1, -2)
        );
    }

    private String getPalaceNameByIndex(int palace) {

        switch (palace) {

            case 1:
                return "乾宫";

            case 2:
                return "兑宫";

            case 3:
                return "艮宫";

            case 4:
                return "离宫";

            case 5:
                return "坎宫";

            case 6:
                return "坤宫";

            case 7:
                return "震宫";

            case 8:
                return "巽宫";

            default:
                return "未知宫位";
        }
    }

    private String getDragonNameByIndex(int index) {

        switch (index) {

            case 0:
                return "地元龙";

            case 1:
                return "天元龙";

            case 2:
                return "人元龙";

            default:
                return "未知";
        }
    }

    private String formatSignedAngle(double angle) {

        return String.format(
                Locale.CHINA,
                "%+.2f",
                angle
        );
    }

    // 创建三盘合参九宫格
    private GridLayout createCombinedNinePalaceGrid(
            XuanKongCalculator.ResidenceChart chart) {

        GridLayout result = new GridLayout(this);

        result.setColumnCount(3);
        result.setRowCount(3);
        result.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        result.setUseDefaultMargins(false);

        LinearLayout.LayoutParams gridParams =
                new LinearLayout.LayoutParams(-1, dp(426));

        gridParams.setMargins(
                dp(2), dp(2), dp(2), dp(10));

        result.setLayoutParams(gridParams);

        if (chart == null
                || chart.periodStars == null
                || chart.mountainStars == null
                || chart.facingStars == null
                || chart.periodStars.length != 9
                || chart.mountainStars.length != 9
                || chart.facingStars.length != 9) {

            return result;
        }

        for (int row = 0; row < 3; row++) {

            for (int col = 0; col < 3; col++) {

                int index = PALACE_ORDER[row][col];

                LinearLayout cell =
                        createCombinedPalaceCell(
                                index,
                                chart.periodStars[index],
                                chart.mountainStars[index],
                                chart.facingStars[index]);

                GridLayout.LayoutParams params =
                        new GridLayout.LayoutParams();

                params.width = 0;
                params.height = dp(140);

                params.columnSpec =
                        GridLayout.spec(col, 1f);

                params.rowSpec =
                        GridLayout.spec(row);

                params.setMargins(
                        dp(2), dp(2), dp(2), dp(2));

                result.addView(cell, params);
            }
        }

        return result;
    }

    private LinearLayout createCombinedPalaceCell(
            int index,
            int periodStar,
            int mountainStar,
            int facingStar) {

        LinearLayout cell = new LinearLayout(this);

        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(
                dp(2), dp(3), dp(2), dp(3));

        cell.setBackgroundColor(CELL_BG);

        TextView palace = new TextView(this);

        palace.setText(
                XuanKongCalculator.getPalaceName(index));

        palace.setTextColor(GOLD);
        palace.setTextSize(12);
        palace.setTypeface(null, Typeface.BOLD);
        palace.setGravity(Gravity.CENTER);
        palace.setSingleLine(true);

        cell.addView(palace,
                new LinearLayout.LayoutParams(-1, dp(20)));

        addCombinedStarLine(cell, "运", periodStar);
        addCombinedStarLine(cell, "山", mountainStar);
        addCombinedStarLine(cell, "向", facingStar);

        String periodElement =
                getChartElement(periodStar);

        String mountainElement =
                getChartElement(mountainStar);

        String facingElement =
                getChartElement(facingStar);

        TextView elements = new TextView(this);

        elements.setText(
                "五行：" + periodElement
                + "/" + mountainElement
                + "/" + facingElement);

        elements.setTextColor(GRAY);
        elements.setTextSize(10);
        elements.setGravity(Gravity.CENTER);
        elements.setSingleLine(true);

        cell.addView(elements,
                new LinearLayout.LayoutParams(-1, dp(20)));

        TextView relations = new TextView(this);

        relations.setText(
                "运山：" + getFiveElementRelation(
                        periodElement, mountainElement)
                + "\n运向：" + getFiveElementRelation(
                        periodElement, facingElement)
                + "\n山向：" + getFiveElementRelation(
                        mountainElement, facingElement)
        );

        relations.setTextColor(WHITE);
        relations.setTextSize(9);
        relations.setGravity(Gravity.CENTER);
        relations.setLineSpacing(dp(1), 1f);

        cell.addView(relations,
                new LinearLayout.LayoutParams(-1, -2));

        return cell;
    }

    private void addCombinedStarLine(
            LinearLayout parent,
            String label,
            int star) {

        TextView text = new TextView(this);

        if (star <= 0 || star > 9) {

            text.setText(label + "星：—");

        } else {

            text.setText(
                    label + star + " "
                    + XuanKongCalculator.getStarName(star));
        }

        text.setTextColor(WHITE);
        text.setTextSize(10);
        text.setGravity(Gravity.CENTER);
        text.setSingleLine(true);

        parent.addView(text,
                new LinearLayout.LayoutParams(-1, dp(19)));
    }

    private String getChartElement(int star) {

        if (star <= 0 || star > 9) {
            return "未知";
        }

        return XuanKongCalculator.getElement(star);
    }

    private String getFiveElementRelation(
            String first,
            String second) {

        if (first == null || second == null
                || first.equals("未知")
                || second.equals("未知")) {

            return "未知";
        }

        if (first.equals(second)) {
            return "比和";
        }

        if (isGenerating(first, second)) {
            return "生";
        }

        if (isGenerating(second, first)) {
            return "受生";
        }

        if (isRestraining(first, second)) {
            return "克";
        }

        if (isRestraining(second, first)) {
            return "受克";
        }

        return "未知";
    }

    private boolean isGenerating(
            String first,
            String second) {

        return
                ("水".equals(first) && "木".equals(second))
                || ("木".equals(first) && "火".equals(second))
                || ("火".equals(first) && "土".equals(second))
                || ("土".equals(first) && "金".equals(second))
                || ("金".equals(first) && "水".equals(second));
    }

    private boolean isRestraining(
            String first,
            String second) {

        return
                ("水".equals(first) && "火".equals(second))
                || ("火".equals(first) && "金".equals(second))
                || ("金".equals(first) && "木".equals(second))
                || ("木".equals(first) && "土".equals(second))
                || ("土".equals(first) && "水".equals(second));
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
