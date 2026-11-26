package com.xuantian.compass;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ResidenceListActivity extends Activity {

    private ResidenceStorage storage;
    private LinearLayout listContainer;
    private TextView countTitle;

    private final int gold = Color.rgb(220, 180, 105);
    private final int background = Color.rgb(20, 17, 14);
    private final int cardBackground = Color.rgb(34, 29, 24);
    private final int textColor = Color.rgb(235, 225, 210);
    private final int red = Color.rgb(190, 75, 65);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(background);

        storage = new ResidenceStorage(this);

        createUI();
        loadResidences();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (listContainer != null && storage != null) {
            loadResidences();
        }
    }

    private void createUI() {

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(background);
        scrollView.setFillViewport(true);
        scrollView.setVerticalScrollBarEnabled(false);

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
        subtitle.setText("本地保存的住宅资料 · 坐向档案");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);

        root.addView(subtitle, params(-1, 40));

        countTitle = new TextView(this);
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
     * 读取全部住宅档案
     */
    private void loadResidences() {

        listContainer.removeAllViews();

        List<ResidenceData> residences = storage.getAll();

        int total = residences == null ? 0 : residences.size();

        countTitle.setText("已保存的住宅（" + total + " 套）");

        if (total == 0) {

            TextView empty = new TextView(this);

            empty.setText(
                    "目前还没有保存住宅资料。\n\n"
                    + "请先返回住宅资料页面，填写资料并点击保存。"
            );

            empty.setTextColor(textColor);
            empty.setTextSize(15);
            empty.setGravity(Gravity.CENTER);
            empty.setLineSpacing(dp(6), 1f);
            empty.setPadding(dp(15), dp(30), dp(15), dp(30));

            listContainer.addView(empty, params(-1, -2));

            return;
        }

        for (ResidenceData data : residences) {
            addResidenceCard(data);
        }
    }

    /**
     * 获取有效的首次入住年份
     */
    private int getMoveInYear(ResidenceData data) {

        String date = getMoveInDate(data);

        if (!date.isEmpty()) {
            try {
                return Integer.parseInt(date.substring(0, 4));
            } catch (Exception ignored) {
            }
        }

        if (data.moveInYear >= 1900
                && data.moveInYear <= 2052) {
            return data.moveInYear;
        }

        return data.buildingYear;
    }

    /**
     * 获取真实首次入住日期。
     * 旧档案没有具体日期时返回空字符串，不虚构日期。
     */
    private String getMoveInDate(ResidenceData data) {

        if (data.moveInDate == null
                || data.moveInDate.trim().isEmpty()) {
            return "";
        }

        String value = data.moveInDate.trim();

        if (!value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return "";
        }

        try {
            int year = Integer.parseInt(value.substring(0, 4));
            int month = Integer.parseInt(value.substring(5, 7));
            int day = Integer.parseInt(value.substring(8, 10));

            Calendar calendar = Calendar.getInstance();
            calendar.setLenient(false);
            calendar.set(year, month - 1, day);
            calendar.getTime();

            if (year < 1900 || year > 2052) {
                return "";
            }

            return value;

        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 创建住宅档案卡片
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

        String moveInDate = getMoveInDate(data);

        String moveInDisplay = moveInDate.isEmpty()
                ? "尚未记录具体日期（年份：" + getMoveInYear(data) + "）"
                : moveInDate;

        TextView info = new TextView(this);

        info.setText(
                "建造年份：" + data.buildingYear
                + "\n首次入住日期：" + moveInDisplay
                + "\n首次入住年份：" + getMoveInYear(data)
                + "\n所在楼层：" + data.floor
                + "\n住宅面积：" + data.area + " 平方米"
                + "\n" + getDirectionSummary(data)
                + "\n档案编号：" + data.id
        );

        info.setTextColor(textColor);
        info.setTextSize(14);
        info.setLineSpacing(dp(5), 1f);
        info.setPadding(0, dp(10), 0, dp(8));

        card.addView(info, params(-1, -2));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);

        TextView detailButton = createSmallButton(
                "查看详情",
                cardBackground,
                gold
        );

        TextView editButton = createSmallButton(
                "编辑",
                gold,
                background
        );

        TextView deleteButton = createSmallButton(
                "删除",
                red,
                Color.WHITE
        );

        LinearLayout.LayoutParams actionParams =
                new LinearLayout.LayoutParams(
                        0,
                        dp(42),
                        1
                );

        actionParams.setMargins(dp(3), dp(5), dp(3), dp(5));

        actions.addView(detailButton, actionParams);
        actions.addView(editButton, actionParams);
        actions.addView(deleteButton, actionParams);

        card.addView(actions);

        detailButton.setOnClickListener(v -> showDetails(data));
        editButton.setOnClickListener(v -> showEditDialog(data));
        deleteButton.setOnClickListener(v -> confirmDelete(data));

        listContainer.addView(card, params(-1, -2));
    }

    /**
     * 生成住宅朝向和坐山文字
     */
    private String getDirectionSummary(ResidenceData data) {

        if (data.facingAngle < 0
                || data.facingAngle >= 360
                || Double.isNaN(data.facingAngle)
                || Double.isInfinite(data.facingAngle)) {

            return "住宅朝向：尚未设置\n坐山：尚未设置";
        }

        double facing =
                TwentyFourMountains.normalizeAngle(data.facingAngle);

        double sitting =
                TwentyFourMountains.normalizeAngle(facing + 180);

        String facingMountain =
                TwentyFourMountains.getMountainName(facing);

        String sittingMountain =
                TwentyFourMountains.getMountainName(sitting);

        return String.format(
                Locale.CHINA,
                "朝向：%.2f° · %s山\n坐山：%.2f° · %s山",
                facing,
                facingMountain,
                sitting,
                sittingMountain
        );
    }

    /**
     * 查看住宅详细资料
     */
    private void showDetails(ResidenceData data) {

        String moveInDate = getMoveInDate(data);

        String moveInDisplay = moveInDate.isEmpty()
                ? "尚未记录具体日期"
                : moveInDate;

        String message =
                "档案编号：" + data.id
                + "\n住宅名称：" + data.name
                + "\n建造年份：" + data.buildingYear
                + "\n首次入住日期：" + moveInDisplay
                + "\n首次入住年份：" + getMoveInYear(data)
                + "\n所在楼层：" + data.floor
                + "\n住宅面积：" + data.area + " 平方米"
                + "\n\n" + getDirectionSummary(data)
                + "\n\n元运起算依据：首次实际入住日期"
                + "\n年份交界：立春"
                + "\n\n说明：坐山根据朝向反方向计算，"
                + "尚未处理兼向及替卦。";

        new AlertDialog.Builder(this)
                .setTitle("住宅档案详情")
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }

    /**
     * 编辑住宅资料
     */
    private void showEditDialog(ResidenceData data) {

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(22), dp(8), dp(22), dp(5));

        EditText nameInput = createEditInput(
                "住宅名称",
                data.name,
                InputType.TYPE_CLASS_TEXT
        );

        EditText yearInput = createEditInput(
                "建造年份",
                String.valueOf(data.buildingYear),
                InputType.TYPE_CLASS_NUMBER
        );

        EditText floorInput = createEditInput(
                "所在楼层",
                data.floor,
                InputType.TYPE_CLASS_TEXT
        );

        EditText areaInput = createEditInput(
                "住宅面积（平方米）",
                String.valueOf(data.area),
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        EditText facingInput = createEditInput(
                "住宅朝向角度（0°—359.99°）",
                data.facingAngle >= 0
                        ? String.valueOf(data.facingAngle)
                        : "",
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        addFormField(form, "住宅名称", nameInput);
        addFormField(form, "建造年份", yearInput);

        TextView moveInDateButton = new TextView(this);
        moveInDateButton.setTextSize(16);
        moveInDateButton.setTextColor(Color.BLACK);
        moveInDateButton.setGravity(Gravity.CENTER_VERTICAL);
        moveInDateButton.setPadding(dp(10), 0, dp(10), 0);
        moveInDateButton.setBackgroundColor(Color.rgb(245, 240, 230));

        final String[] selectedMoveInDate = {
                getMoveInDate(data)
        };

        updateMoveInDateButton(
                moveInDateButton,
                selectedMoveInDate[0]
        );

        addDateFormField(
                form,
                "首次入住日期（选择年月日）",
                moveInDateButton
        );

        moveInDateButton.setOnClickListener(v -> {

            Calendar initial = Calendar.getInstance();

            if (!selectedMoveInDate[0].isEmpty()) {
                try {
                    int y = Integer.parseInt(
                            selectedMoveInDate[0].substring(0, 4));
                    int m = Integer.parseInt(
                            selectedMoveInDate[0].substring(5, 7));
                    int d = Integer.parseInt(
                            selectedMoveInDate[0].substring(8, 10));

                    initial.set(y, m - 1, d);

                } catch (Exception ignored) {
                    initial.set(getMoveInYear(data), 0, 1);
                }

            } else {
                initial.set(getMoveInYear(data), 0, 1);
            }

            DatePickerDialog picker = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {

                        selectedMoveInDate[0] = String.format(
                                Locale.CHINA,
                                "%04d-%02d-%02d",
                                year,
                                month + 1,
                                dayOfMonth
                        );

                        updateMoveInDateButton(
                                moveInDateButton,
                                selectedMoveInDate[0]
                        );
                    },
                    initial.get(Calendar.YEAR),
                    initial.get(Calendar.MONTH),
                    initial.get(Calendar.DAY_OF_MONTH)
            );

            Calendar min = Calendar.getInstance();
            min.clear();
            min.set(1900, Calendar.JANUARY, 1);

            Calendar max = Calendar.getInstance();
            max.clear();
            max.set(2052, Calendar.DECEMBER, 31);

            picker.getDatePicker().setMinDate(min.getTimeInMillis());
            picker.getDatePicker().setMaxDate(max.getTimeInMillis());

            picker.show();
        });

        addFormField(form, "所在楼层", floorInput);
        addFormField(form, "住宅面积（平方米）", areaInput);
        addFormField(form, "住宅朝向角度", facingInput);

        TextView directionPreview = new TextView(this);
        directionPreview.setTextColor(Color.DKGRAY);
        directionPreview.setTextSize(14);
        directionPreview.setPadding(
                dp(4), dp(8), dp(4), dp(8));

        form.addView(directionPreview, params(-1, -2));

        updateEditDirectionPreview(
                directionPreview,
                facingInput.getText().toString().trim()
        );

        facingInput.addTextChangedListener(new android.text.TextWatcher() {

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

                updateEditDirectionPreview(
                        directionPreview,
                        s.toString().trim()
                );
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(form);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("编辑住宅档案")
                .setView(scrollView)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存修改", null)
                .create();

        dialog.setOnShowListener(v -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(button -> {

                        String name = nameInput.getText()
                                .toString().trim();

                        String yearText = yearInput.getText()
                                .toString().trim();

                        String floor = floorInput.getText()
                                .toString().trim();

                        String areaText = areaInput.getText()
                                .toString().trim();

                        String facingText = facingInput.getText()
                                .toString().trim();

                        if (yearText.isEmpty()) {
                            yearInput.setError("请输入建造年份");
                            return;
                        }

                        int year;

                        try {
                            year = Integer.parseInt(yearText);
                        } catch (NumberFormatException e) {
                            yearInput.setError("年份必须是数字");
                            return;
                        }

                        if (year < 1900 || year > 2052) {
                            yearInput.setError("年份范围为1900—2052");
                            return;
                        }

                        String moveInDate = selectedMoveInDate[0];

                        if (moveInDate.isEmpty()) {
                            Toast.makeText(
                                    this,
                                    "请选择首次入住的具体年月日",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        int moveInYear;

                        try {
                            moveInYear = Integer.parseInt(
                                    moveInDate.substring(0, 4));
                        } catch (Exception e) {
                            Toast.makeText(
                                    this,
                                    "入住日期无效，请重新选择",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        if (moveInYear < year) {
                            Toast.makeText(
                                    this,
                                    "首次入住日期不能早于建造年份",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        if (floor.isEmpty()) {
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

                        if (Double.isNaN(area)
                                || Double.isInfinite(area)
                                || area <= 0) {

                            areaInput.setError("面积必须大于0");
                            return;
                        }

                        double facingAngle = -1;
                        String facingMountain = "";

                        if (!facingText.isEmpty()) {

                            try {
                                facingAngle = Double.parseDouble(facingText);
                            } catch (NumberFormatException e) {
                                facingInput.setError("角度必须是数字");
                                return;
                            }

                            if (Double.isNaN(facingAngle)
                                    || Double.isInfinite(facingAngle)
                                    || facingAngle < 0
                                    || facingAngle >= 360) {

                                facingInput.setError(
                                        "请输入0°至359.99°之间的角度");
                                return;
                            }

                            facingMountain =
                                    TwentyFourMountains.getMountainName(
                                            facingAngle);
                        }

                        if (name.isEmpty()) {
                            name = "未命名住宅";
                        }

                        // 保留原来的档案编号
                        data.name = name;
                        data.buildingYear = year;
                        data.moveInDate = moveInDate;
                        data.moveInYear = moveInYear;
                        data.floor = floor;
                        data.area = area;

                        // 更新坐向资料
                        data.facingAngle = facingAngle;
                        data.facingMountain = facingMountain;

                        int result = storage.update(data);

                        if (result > 0) {

                            Toast.makeText(
                                    this,
                                    "住宅资料修改成功",
                                    Toast.LENGTH_SHORT
                            ).show();

                            dialog.dismiss();
                            loadResidences();

                        } else {

                            Toast.makeText(
                                    this,
                                    "修改失败，请重试",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });

        dialog.show();
    }

    /**
     * 更新首次入住日期按钮文字
     */
    private void updateMoveInDateButton(
            TextView button,
            String date) {

        if (date == null || date.isEmpty()) {
            button.setText("点击选择首次入住日期");
        } else {
            button.setText(date + "   点击修改");
        }
    }

    /**
     * 添加日期选择字段
     */
    private void addDateFormField(
            LinearLayout parent,
            String label,
            TextView button) {

        TextView title = new TextView(this);

        title.setText(label);
        title.setTextSize(14);
        title.setTextColor(Color.DKGRAY);
        title.setPadding(0, dp(8), 0, dp(2));

        parent.addView(title, params(-1, -2));
        parent.addView(button, params(-1, 52));
    }

    /**
     * 编辑时实时预览朝向和坐山
     */
    private void updateEditDirectionPreview(
            TextView preview,
            String value) {

        if (value.isEmpty()) {
            preview.setText("朝向：尚未设置\n坐山：尚未设置");
            return;
        }

        try {
            double angle = Double.parseDouble(value);

            if (Double.isNaN(angle)
                    || Double.isInfinite(angle)
                    || angle < 0
                    || angle >= 360) {

                preview.setText("角度无效，请输入0°至359.99°");
                return;
            }

            double opposite =
                    TwentyFourMountains.normalizeAngle(angle + 180);

            preview.setText(
                    String.format(
                            Locale.CHINA,
                            "朝向：%.2f° · %s山\n坐山：%.2f° · %s山",
                            angle,
                            TwentyFourMountains.getMountainName(angle),
                            opposite,
                            TwentyFourMountains.getMountainName(opposite)
                    )
            );

        } catch (NumberFormatException e) {
            preview.setText("请输入有效的数字角度");
        }
    }

    /**
     * 创建编辑输入框
     */
    private EditText createEditInput(
            String hint,
            String value,
            int inputType) {

        EditText input = new EditText(this);

        input.setHint(hint);
        input.setText(value);
        input.setTextSize(16);
        input.setSingleLine(true);
        input.setInputType(inputType);
        input.setPadding(dp(10), dp(8), dp(10), dp(8));

        return input;
    }

    /**
     * 添加普通表单字段
     */
    private void addFormField(
            LinearLayout parent,
            String label,
            EditText input) {

        TextView title = new TextView(this);

        title.setText(label);
        title.setTextSize(14);
        title.setTextColor(Color.DKGRAY);
        title.setPadding(0, dp(8), 0, dp(2));

        parent.addView(title, params(-1, -2));
        parent.addView(input, params(-1, 52));
    }

    /**
     * 删除前确认
     */
    private void confirmDelete(ResidenceData data) {

        new AlertDialog.Builder(this)
                .setTitle("删除住宅档案")
                .setMessage(
                        "确定要删除这套住宅资料吗？\n\n"
                        + "住宅名称：" + data.name
                        + "\n档案编号：" + data.id
                        + "\n\n删除后无法通过本页面恢复。"
                )
                .setNegativeButton("取消", null)
                .setPositiveButton("确认删除", (dialog, which) -> {

                    int result = storage.delete(data.id);

                    if (result > 0) {

                        Toast.makeText(
                                this,
                                "住宅档案已删除",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadResidences();

                    } else {

                        Toast.makeText(
                                this,
                                "删除失败，请重试",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .show();
    }

    /**
     * 创建大按钮
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

    /**
     * 创建档案卡片小按钮
     */
    private TextView createSmallButton(
            String text,
            int fillColor,
            int fontColor) {

        TextView button = new TextView(this);

        button.setText(text);
        button.setTextColor(fontColor);
        button.setTextSize(13);
        button.setTypeface(null, Typeface.BOLD);
        button.setGravity(Gravity.CENTER);
        button.setIncludeFontPadding(true);
        button.setClickable(true);
        button.setFocusable(true);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(dp(8));

        button.setBackground(drawable);

        return button;
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
     * dp转换
     */
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
