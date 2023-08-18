package com.frank.word;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

public abstract class DialogView {
    private static InputMethodManager manager = null;
    public Dialog dialog;
    private final Window window;
    private final Context context;
    private final int layoutResID;
    private final DialogManager dialog_manager;
    private DisplayMetrics dm = null;

    public DialogView(Context context, int layoutResID) {
        dialog_manager = DialogManager.getInstance();
        this.context = context;
        dialog = new AlertDialog.Builder(context).create();
        dialog.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_UP) {
                closeDialog(false);
            }
            return false;
        });
        dialog.setOnCancelListener(dialog -> closeDialog(false));
        window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.color.transparent);
            window.setGravity(Gravity.CENTER);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        this.layoutResID = layoutResID;
    }

    protected void showBegin() {
        if (context instanceof Activity && ((Activity) context).isFinishing()) {
            return;
        }
        dialog.setCancelable(false);
        dialog.show();
        window.setContentView(layoutResID);
        setSettingWindowSize();
        showInput();
    }

    protected void showEnd() {
        dialog_manager.addDialog(this);
    }

    private void setSettingWindowSize() {
        if (dm == null) {
            dm = new DisplayMetrics();
        }
        if (context instanceof Activity) {
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        }
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        WindowManager.LayoutParams lp = window.getAttributes();
        if (width > height) {
            lp.width = height * 7 / 8;
        } else {
            lp.width = width * 7 / 8;
        }
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //lp.y = -300;
        window.setAttributes(lp);
    }

    /**
     * the sub-class must come true this method
     * the first line code must call the method show(boolean isNeedInputMethod)
     * and then, user can get the view case by super.dialog.findViewById(int resID);
     */
    protected void showDialog() {
    }

    /**
     * before create a new dialog,the previous dialog will call this method first
     */
    public void onPause() {
    }

    /**
     * the dialog will call this method when it get focus
     */
    public void onResume() {
    }

    /**
     * close all the dialogs that have opened
     */
    public void closeAllDialog() {
        dialog_manager.closeAllDialog();
    }

    /**
     * if you want to manage DialogManager isManagerCalling = true but at most
     * time, isManagerCalling = false
     */
    protected void closeDialog(boolean isNeedRemoveByManual) {
        hideInput(context);
        dialog.cancel();
        window.setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        if (!isNeedRemoveByManual) {
            dialog_manager.removeDialog(this);
        }
    }

    /**
     * show the soft input method
     */
    protected void showInput() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    /**
     * hide the soft input method
     */
    private void hideInput(Context context) {
        if (manager == null) {
            manager = ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE));
        }
        if (dialog.getCurrentFocus() != null)
            manager.hideSoftInputFromWindow(dialog.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
