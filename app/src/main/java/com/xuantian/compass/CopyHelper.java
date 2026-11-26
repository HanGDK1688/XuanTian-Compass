package com.xuantian.compass;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

/**
 * 玄天罗盘统一文字复制工具。
 *
 * 供各个页面调用，统一复制文字到系统剪贴板。
 */
public class CopyHelper {

    private CopyHelper() {
        // 不创建实例
    }

    /**
     * 复制文字到系统剪贴板。
     *
     * @param context 页面上下文
     * @param text 要复制的文字
     */
    public static void copyText(Context context, String text) {

        if (context == null) {
            return;
        }

        if (text == null || text.trim().isEmpty()) {
            Toast.makeText(
                    context,
                    "没有可复制的内容",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        ClipboardManager clipboard =
                (ClipboardManager) context.getSystemService(
                        Context.CLIPBOARD_SERVICE
                );

        if (clipboard == null) {
            Toast.makeText(
                    context,
                    "无法使用系统剪贴板",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        ClipData clip = ClipData.newPlainText(
                "玄天罗盘",
                text
        );

        clipboard.setPrimaryClip(clip);

        Toast.makeText(
                context,
                "内容已复制",
                Toast.LENGTH_SHORT
        ).show();
    }
}
