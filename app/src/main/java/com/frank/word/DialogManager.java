package com.frank.word;

import java.util.ArrayList;

public class DialogManager {
    private static final ArrayList<DialogView> dialog_array = new ArrayList<>();
    private static DialogManager dialog_manager;

    private DialogManager() {
    }

    public static DialogManager getInstance() {
        if (dialog_manager == null) {
            dialog_manager = new DialogManager();
        }
        return dialog_manager;
    }

    private static int getDialogCount() {
        return dialog_array.size();
    }

    public void addDialog(DialogView dialog) {
        if (dialog_array.size() > 0) {
            dialog_array.get(dialog_array.size() - 1).onPause();
        }
        dialog_array.add(dialog);
        dialog_array.get(dialog_array.size() - 1).onResume();
    }

    public void removeDialog(DialogView dialog) {
        if (dialog_array.contains(dialog)) {
            dialog.onPause();
            dialog_array.remove(dialog);
            if (dialog_array.size() > 0) {
                dialog_array.get(dialog_array.size() - 1).onResume();
            }
        }
    }

    public void closeAllDialog() {
        for (int i = getDialogCount() - 1; i > -1; i--) {
            dialog_array.get(i).closeDialog(true);
            dialog_array.remove(i);
        }
    }
}
