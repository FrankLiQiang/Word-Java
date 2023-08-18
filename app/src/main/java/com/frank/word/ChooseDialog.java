package com.frank.word;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.TextView;

public class ChooseDialog extends DialogView implements OnClickListener {
    private final Context _context;
    private TextView tv_lesson_from;
    private TextView tv_lesson_to;
    private int _from;
    private int _to;
    private int _total;
    private SeekBar sb_form, sb_to;

    public ChooseDialog(Context context, int from, int to, int total) {
        super(context, R.layout.choose);
        this._context = context;
        this._from = from;
        this._to = to;
        this._total = total;
    }

    public void showDialog() {
        super.showBegin();
        View btnCancel = super.dialog.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(this);
        View btnSave = super.dialog.findViewById(R.id.btn_ok);
        btnSave.setOnClickListener(this);
        sb_form = super.dialog.findViewById(R.id.sb_from);
        sb_to = super.dialog.findViewById(R.id.sb_to);
        sb_form.setMin(0);
        sb_to.setMin(0);
        sb_form.setMax(_total - 1);
        sb_to.setMax(_total - 1);
        sb_form.setProgress(_from);
        sb_to.setProgress(_to);
        sb_form.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    _from = progress;
                    tv_lesson_from.setText("" + (_from + 1));
                    if (_from > _to) {
                        _to = _from;
                        sb_to.setProgress(_to);
                        tv_lesson_to.setText("" + (_to + 1));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        sb_to.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    _to = progress;
                    tv_lesson_to.setText("" + (_to + 1));
                    if (_from > _to) {
                        _from = _to;
                        sb_form.setProgress(_from);
                        tv_lesson_from.setText("" + (_from + 1));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        /*
        IntervalSeekBar isb = super.dialog.findViewById(R.id.seekBar);
        isb.setValue(_from, _to, _total);
        isb.setOnSeekBarChangeListener((seekBar, leftProgress, rightProgress) -> {
            tv_lesson_from.setText("" + (leftProgress + 1));
            tv_lesson_to.setText("" + (rightProgress + 1));
        });
        */
        tv_lesson_from = super.dialog.findViewById(R.id.tv_lesson_from);
        tv_lesson_to = super.dialog.findViewById(R.id.tv_lesson_to);
        tv_lesson_from.setText("" + (_from + 1));
        tv_lesson_to.setText("" + (_to + 1));
        super.showEnd();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_cancel) {
            super.closeDialog(false);
        } else if (v.getId() == R.id.btn_ok) {
            ((WordActivity) _context).setLessonInterval(_from, _to);
            this.closeAllDialog();
        }
    }
}
