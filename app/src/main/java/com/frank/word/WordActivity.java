package com.frank.word;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.media.session.MediaButtonReceiver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordActivity extends AppCompatActivity implements View.OnClickListener {

    private static MusicService.MusicControl musicControl;
    private final static int SHOW_RANGE_NORMAL = 0;
    private final static int SHOW_FAVORITE = 1;
    private final static int SHOW_DEL = 2;
    private final static int SHOW_CHOSEN = 3;
    private final static int SHOW_RANGE_ALL = 4;
    private final static int SHOW_RANGE_CLASS = 5;

    private int iStart = 0;
    private int iEnd = 0;
    private int duration = 0;
    private int wordIndex = 0;
    private int normalIndex = 0;
    private int allIndex = 0;
    private int delIndex = 0;
    private int favoriteIndex = 0;
    private int chosenIndex = 0;

    private int fileBeginIndex = 0;
    private int fileEndIndex = 0;
    private int fileIndex = 0;
    private int loopIndex = 0;
    private int loopNumber = 1;
    private int all_num = 0;
    private int chosen_num = 0;
    private int removed_num = 0;
    private int normal_num = 0;
    private int favorite_num = 0;
    private long _exitTime = 0;
    private boolean isPlay = true;
    private boolean isReadOnly = true;
    private boolean isLRC_Time_OK = false;
    private boolean isLRC_Format_OK = false;
    private boolean isAdjust = false;
    private boolean isPlayFolder = false;
    private boolean isMute = false;
    private boolean isBlack = false;
    private boolean isNextLesson = true;
    private int iShowRange = SHOW_RANGE_ALL;
    private DocumentFile[] files;
    private int sortType = 0;
    private static final int SHOW_ALL = 0;
    private static int SHOW_FOREIGN = 1;
    private static int SHOW_PRONUNCIATION = 2;
    private static int SHOW_NATIVE = 3;
    private static int SHOW_NONE = 4;
    private int CurrentWordClass = 0;

    private int showWordType = SHOW_ALL;

    private Uri _uri;
    private String fileName, folderName = "";
    private TextView tv_word_class;
    private TextView lrcTV, sentence1, sentence2;
    private EditText _info;
    private EditText _dict;
    private EditText _new_lrc;
    private MyServiceConn conn;
    private ActivityResultLauncher<Intent> launcher;
    private ActivityResultLauncher<Intent> launcherFolder;
    private MenuItem pauseItem;
    private MenuItem saveItem, saveLRCItem;
    private MenuItem quickItem;
    private MenuItem rangeItem;
    private MenuItem chooseItem;

    private MenuItem mi_black;
    private MenuItem loop_one;
    private MenuItem mi_random;
    private MenuItem mi_favorite;
    private MenuItem menu_word_class;

    private ProgressBar progressBar;
    private SeekBar _seekBar;
    private final int[] numberArray = new int[22];
    private final int[] ClassColor = new int[16];
    private final MenuItem[] menuWordClass = new MenuItem[16];
    private int[] wordIndexArray;
    private final ArrayList<String> list_native = new ArrayList<>();
    private final ArrayList<String> list_foreign = new ArrayList<>();
    private final ArrayList<String> list_pronunciation = new ArrayList<>();
    private final ArrayList<String> list_sentence1 = new ArrayList<>();
    private final ArrayList<String> list_sentence2 = new ArrayList<>();
    private final ArrayList<String> listTime = new ArrayList<>();
    private final ArrayList<Integer> iListTime = new ArrayList<>();
    private final ArrayList<Integer> iFavorite = new ArrayList<>();
    private final ArrayList<Integer> iWordClass = new ArrayList<>();

    private MediaSessionCompat mMediaSession;
    private File lrcFile;
    private String[] wClass = new String[]{
            "名词"
            , "代词"
            , "数词"
            , "动1"
            , "动2"
            , "动3"
            , "形1"
            , "形2"
            , "连体词"
            , "副词"
            , "接续词"
            , "叹词"
            , "助动词"
            , "助词"
            , "专有名词"
            , "动词"
            , ""
    };

    private String error = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        _info = findViewById(R.id.info);
        _dict = findViewById(R.id.dict);
        _new_lrc = findViewById(R.id.new_lrc);
        lrcTV = findViewById(R.id.lrc);
        sentence1 = findViewById(R.id.sentence1);
        sentence2 = findViewById(R.id.sentence2);
        tv_word_class = findViewById(R.id.word_class);
        progressBar = findViewById(R.id.progressBar);
        _seekBar = findViewById(R.id.sb_progressBar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setListener();
        setBluetoothButton();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        _seekBar.setProgress((int) lrcTV.getTextSize() / 2);

        _seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    _info.setTextSize(TypedValue.COMPLEX_UNIT_PX, progress * 2);
                    _dict.setTextSize(TypedValue.COMPLEX_UNIT_PX, progress * 2);
                    lrcTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, progress * 2);
                    sentence1.setTextSize(TypedValue.COMPLEX_UNIT_PX, progress * 2);
                    sentence2.setTextSize(TypedValue.COMPLEX_UNIT_PX, progress * 2);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        pauseItem = menu.findItem(R.id.item_pause);
        saveItem = menu.findItem(R.id.item_save);
        saveLRCItem = menu.findItem(R.id.item_save_lrc);
        quickItem = menu.findItem(R.id.item_quick);
        rangeItem = menu.findItem(R.id.folder_range);
        chooseItem = menu.findItem(R.id.choose_lesson);
        mi_black = menu.findItem(R.id.background);
        loop_one = menu.findItem(R.id.single_menu_01);
        mi_random = menu.findItem(R.id.play_in_random);
        mi_favorite = menu.findItem(R.id.play_favorite);
        menu_word_class = menu.findItem(R.id.menu_word_class);

        menuWordClass[0] = menu.findItem(R.id.class_noun);
        menuWordClass[1] = menu.findItem(R.id.class_daici);
        menuWordClass[2] = menu.findItem(R.id.class_shuci);
        menuWordClass[3] = menu.findItem(R.id.class_verb_1);
        menuWordClass[4] = menu.findItem(R.id.class_verb_2);
        menuWordClass[5] = menu.findItem(R.id.class_verb_3);
        menuWordClass[6] = menu.findItem(R.id.class_adj_1);
        menuWordClass[7] = menu.findItem(R.id.class_adj_2);
        menuWordClass[8] = menu.findItem(R.id.class_liantici);
        menuWordClass[9] = menu.findItem(R.id.class_fuci);
        menuWordClass[10] = menu.findItem(R.id.class_jiexuci);
        menuWordClass[11] = menu.findItem(R.id.class_tanci);
        menuWordClass[12] = menu.findItem(R.id.class_zhudongci);
        menuWordClass[13] = menu.findItem(R.id.class_zhuci);
        menuWordClass[14] = menu.findItem(R.id.class_zhuanyou);
        menuWordClass[15] = menu.findItem(R.id.class_duanyu);

        numberArray[0] = R.id.single_menu_00;
        numberArray[1] = R.id.single_menu_01;
        numberArray[2] = R.id.single_menu_02;
        numberArray[3] = R.id.single_menu_03;
        numberArray[4] = R.id.single_menu_04;
        numberArray[5] = R.id.single_menu_05;

        numberArray[6] = R.id.class_noun;
        numberArray[7] = R.id.class_daici;
        numberArray[8] = R.id.class_shuci;
        numberArray[9] = R.id.class_verb_1;
        numberArray[10] = R.id.class_verb_2;
        numberArray[11] = R.id.class_verb_3;
        numberArray[12] = R.id.class_adj_1;
        numberArray[13] = R.id.class_adj_2;
        numberArray[14] = R.id.class_liantici;
        numberArray[15] = R.id.class_fuci;
        numberArray[16] = R.id.class_jiexuci;
        numberArray[17] = R.id.class_tanci;
        numberArray[18] = R.id.class_zhudongci;
        numberArray[19] = R.id.class_zhuci;
        numberArray[20] = R.id.class_zhuanyou;
        numberArray[21] = R.id.class_duanyu;

        ClassColor[0] = R.color.mingci;
        ClassColor[1] = R.color.daici;
        ClassColor[2] = R.color.shuci;
        ClassColor[3] = R.color.dong1;
        ClassColor[4] = R.color.dong2;
        ClassColor[5] = R.color.dong3;
        ClassColor[6] = R.color.adj1;
        ClassColor[7] = R.color.adj2;
        ClassColor[8] = R.color.liantici;
        ClassColor[9] = R.color.fuci;
        ClassColor[10] = R.color.jiexuci;
        ClassColor[11] = R.color.tanci;
        ClassColor[12] = R.color.zhudongci;
        ClassColor[13] = R.color.zhuci;
        ClassColor[14] = R.color.zhuanyou;
        ClassColor[15] = R.color.duanyu;
        return true;
    }

    private String checkNewLRC() {
        String str = _new_lrc.getText().toString();
        str = str.replace("　", " ");
        if (str.length() < 50) {
            return "请输入单词及解释。";
        }
        if (!str.endsWith("完了\n") && !str.endsWith("结束\n")) {
            return "请输入[完了]解释标志。";
        }
        String[] strArray = str.split("\n");
        list_foreign.clear();
        list_pronunciation.clear();
        list_native.clear();
        list_sentence1.clear();
        list_sentence2.clear();
        int len = 0;
        for (int i = 0; i < strArray.length; i++) {
            String[] tmp = strArray[i].split(" ");
            len += strArray[i].length() + 1;
            if (tmp.length == 1) {
                if (strArray[i].equals("完了") || strArray[i].equals("结束") || strArray[i].equals("3")) {

                    if (list_foreign.size() == list_sentence1.size()) {
                        list_sentence1.add(tmp[0]);
                        list_sentence2.add(tmp[0]);
                    }

                    list_foreign.add(tmp[0]);
                    list_pronunciation.add(tmp[0]);
                    list_native.add(tmp[0]);
                    continue;
                } else {
                    _new_lrc.setSelection(len - 1);
                    return "[ " + strArray[i] + " ] 格式错误！";
                }
            }
            if (tmp.length == 2) {
                list_foreign.add(tmp[0]);
                list_pronunciation.add(tmp[0]);
                list_native.add(tmp[1]);
                list_sentence1.add("");
                list_sentence2.add("");
                continue;
            }
            if (tmp.length < 3 || strArray[i].trim().isEmpty()) {
                _new_lrc.setSelection(len - 1);
                return "[ " + strArray[i] + " ] 格式错误！";
            }
            list_foreign.add(tmp[0]);
            list_pronunciation.add(tmp[1]);
            list_native.add(tmp[2]);
            if (tmp.length >= 5) {
                list_sentence1.add(tmp[3]);
                list_sentence2.add(tmp[4]);
            }
        }

        return "";
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if (findViewById(R.id.iv_all).getVisibility() == View.VISIBLE) {
                if (!isPlay) {
                    musicControl.continuePlay();
                }
                reset();
            } else {
                _dict.setText("");
            }
        } else if (id == R.id.open) {
            duration = 0;
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setDataAndType(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "audio/*");
            launcher.launch(intent);
            return true;
        } else if (id == R.id.edit_word) {
            if (lrcFile != null) {
                isLRC_Time_OK = false;
                String str = readRawTxtFile(lrcFile);
                editWords(str);
            }
        } else if (id == R.id.read_only) {
            isReadOnly = !isReadOnly;
            item.setChecked(isReadOnly);
            _info.setEnabled(!isReadOnly);
            _info.setText("");
        } else if (id == R.id.background) {
            isBlack = !isBlack;
            item.setChecked(isBlack);
            if (findViewById(R.id.iv_all).getVisibility() == View.VISIBLE) {
                if (isBlack) {
                    findViewById(R.id.iv_all).setBackgroundColor(getResources().getColor(R.color.black));
                } else {
                    findViewById(R.id.iv_all).setBackgroundDrawable(getResources().getDrawable(R.drawable.background_main));
                }
            } else {
                if (isBlack) {
                    findViewById(R.id.all_bg).setBackgroundColor(getResources().getColor(R.color.black));
                } else {
                    findViewById(R.id.all_bg).setBackgroundDrawable(getResources().getDrawable(R.drawable.background_main));
                }
            }
        } else if (id == R.id.folder) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            launcherFolder.launch(intent);
        } else if (id == R.id.one_key) {
            isBlack = true;
            loopNumber = 1;
            sortType = 1;
            iShowRange = SHOW_FAVORITE;
            mi_black.setChecked(true);
            loop_one.setChecked(true);
            mi_random.setChecked(true);
            mi_favorite.setChecked(true);
            findViewById(R.id.iv_all).setBackgroundColor(getResources().getColor(R.color.black));
        } else if (id == R.id.in_order) {
            sortType = 0;
            item.setChecked(true);
            sortFiles();
            sortWords();
        } else if (id == R.id.play_in_random) {
            sortType = 1;
            item.setChecked(true);
            sortFiles();
            sortWords();
        } else if (id == R.id.play_all_random) {
            sortType = 2;
            item.setChecked(true);
            sortFiles();
            sortWords();
        } else if (id == R.id.folder_range) {
            ChooseDialog dialog1 = new ChooseDialog(this, fileBeginIndex, fileEndIndex, files.length);
            dialog1.showDialog();
        } else if (id == R.id.choose_lesson) {
            myPopupMenu();
        } else if (id == R.id.item_quick) {
            if (!isPlay) {
                musicControl.continuePlay();
            }
            musicControl.seekTo(0);
            musicControl.speedTo(2.0f);
        } else if (id == R.id.item_save) {
            String str = checkNewLRC();
            if (!str.isEmpty()) {
                saveFile(str + "  -- ");
                return true;
            }
            saveFile("");
        } else if (id == R.id.item_save_lrc) {
            if (isAdjust) {
                saveFile("");
                return super.onOptionsItemSelected(item);
            }
        } else if (id == R.id.item_pause) {
            if (isPlay) {
                lrcTV.setTextColor(getColor(R.color.dong1));
                musicControl.pausePlay();
                item.setIcon(R.drawable.end_play_new);
                item.setTitle(R.string.play_img);
            } else {
                lrcTV.setTextColor(getColor(R.color.white));
                musicControl.continuePlay();
                item.setIcon(R.drawable.start_play_new);
                item.setTitle(R.string.pause_img);
            }
            isPlay = !isPlay;
        } else if (id == R.id.show_word_all) {
            item.setChecked(true);
            item.setCheckable(true);
            showWordType = SHOW_ALL;
            showWord();
        } else if (id == R.id.show_word_foreign) {
            item.setChecked(true);
            item.setCheckable(true);
            showWordType = SHOW_FOREIGN;
            showWord();
        } else if (id == R.id.show_word_pronunciation) {
            item.setChecked(true);
            item.setCheckable(true);
            showWordType = SHOW_PRONUNCIATION;
            showWord();
        } else if (id == R.id.show_word_native) {
            item.setChecked(true);
            item.setCheckable(true);
            showWordType = SHOW_NATIVE;
            showWord();
        } else if (id == R.id.show_none) {
            item.setChecked(true);
            item.setCheckable(true);
            showWordType = SHOW_NONE;
            showWord();
        } else if (id == R.id.mode_normal) {
            isMute = false;
            item.setChecked(true);
            item.setCheckable(true);
            musicControl.Volume();
        } else if (id == R.id.mode_mute) {
            isMute = true;
            item.setChecked(true);
            item.setCheckable(true);
            musicControl.mute();
        } else if (id == R.id.show_range_all) {
            iShowRange = SHOW_RANGE_ALL;
            sortFiles();
            sortWords();
            showTitle();
            item.setChecked(true);
            item.setCheckable(true);
        } else if (id == R.id.show_range_chosen) {
            if (chosen_num == 0 && !isPlayFolder) {
                Toast.makeText(this, "没有相应单词", Toast.LENGTH_LONG).show();
                return super.onOptionsItemSelected(item);
            }
            iShowRange = SHOW_CHOSEN;
            item.setChecked(true);
            item.setCheckable(true);
            sortFiles();
            sortWords();
            if (iFavorite.get(wordIndexArray[wordIndex]) == SHOW_DEL) {
                reset();
            } else {
                showTitle();
            }
        } else if (id == R.id.play_del) {
            if (removed_num == 0 && !isPlayFolder) {
                Toast.makeText(this, "没有相应单词", Toast.LENGTH_LONG).show();
                return super.onOptionsItemSelected(item);
            }
            iShowRange = SHOW_DEL;
            item.setChecked(true);
            sortFiles();
            sortWords();
            if (iFavorite.get(wordIndexArray[wordIndex]) == SHOW_DEL) {
                showTitle();
            } else {
                reset();
            }
        } else if (id == R.id.play_normal) {
            if (normal_num == 0 && !isPlayFolder) {
                Toast.makeText(this, "没有相应单词", Toast.LENGTH_LONG).show();
                return super.onOptionsItemSelected(item);
            }
            iShowRange = SHOW_RANGE_NORMAL;
            item.setChecked(true);
            sortFiles();
            sortWords();
            if (iFavorite.get(wordIndexArray[wordIndex]) == SHOW_RANGE_NORMAL) {
                showTitle();
            } else {
                reset();
            }
        } else if (id == R.id.play_favorite) {
            if (favorite_num == 0 && !isPlayFolder) {
                Toast.makeText(this, "没有相应单词", Toast.LENGTH_LONG).show();
                return super.onOptionsItemSelected(item);
            }
            iShowRange = SHOW_FAVORITE;
            item.setChecked(true);
            sortFiles();
            sortWords();
            if (iFavorite.get(wordIndexArray[wordIndex]) == SHOW_FAVORITE) {
                showTitle();
            } else {
                reset();
            }
        } else if (id == R.id.play_class) {
            iShowRange = SHOW_RANGE_CLASS;
            item.setChecked(true);
        } else if (id == R.id.class_vt) {
            CurrentWordClass = 16;
            if (iShowRange == SHOW_RANGE_CLASS) {
                showClassNext();
            } else {
                int cls = iWordClass.get(wordIndexArray[wordIndex]) % 20;
                if (cls < 3 || cls > 5) {
                    Toast.makeText(this, "请选择动词", Toast.LENGTH_LONG).show();
                    return super.onOptionsItemSelected(item);
                }
                iWordClass.set(wordIndexArray[wordIndex], 20 + cls);
                saveFile("");
                showCurrentWord();
            }
        } else if (id == R.id.class_vi) {
            CurrentWordClass = 17;
            if (iShowRange == SHOW_RANGE_CLASS) {
                showClassNext();
            } else {
                int cls = iWordClass.get(wordIndexArray[wordIndex]) % 20;
                if (cls < 3 || cls > 5) {
                    Toast.makeText(this, "请选择动词", Toast.LENGTH_LONG).show();
                    return super.onOptionsItemSelected(item);
                }
                iWordClass.set(wordIndexArray[wordIndex], 40 + cls);
                saveFile("");
                showCurrentWord();
            }
        } else if (id == R.id.help) {
            showHelp();
        } else {
            for (int i = 0; i < 6; i++) {
                if (id == numberArray[i]) {
                    loopNumber = i;
                    item.setChecked(true);
                    item.setCheckable(true);
                    loopIndex = 0;
                    if (loopNumber != 1 && wordIndexArray != null
                            && wordIndex < wordIndexArray.length
                            && iListTime.size() > wordIndexArray[wordIndex] + 1) {
                        iEnd = iListTime.get(wordIndexArray[wordIndex] + 1);
                    }
                    return super.onOptionsItemSelected(item);
                }
            }
            for (int i = 6; i < 22; i++) {
                if (id == numberArray[i]) {
                    item.setChecked(true);
                    item.setCheckable(true);
                    CurrentWordClass = i - 6;
                    if (iShowRange == SHOW_RANGE_CLASS) {
                        showClassNext();
                    } else {
                        iWordClass.set(wordIndexArray[wordIndex], CurrentWordClass);
                        saveFile("");
                        showCurrentWord();
                    }
                    return super.onOptionsItemSelected(item);
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    void showHelp() {
        String help = "文字末尾打入字符时的隐藏功能\n\n" +

                "换行：\n" +
                "  有文字时检查正确性\n" +
                "     正确时清空, 错误时显示正确单词\n" +
                "  无文字时：\n" +
                "     下一个单词\n\n" +

                "<: 上一个单词\n" +
                "空格或点击单词：清空\n\n" +

                "调整时刻模式,顺序,全部播放，才可以的:\n" +
                "  +：插入新单词\n" +
                "  -：删除当前单词(删文字 留读音)(慎用！！)\n" +
                "  !；设置当前单词为<非单词>属性(文字读音全删)\n\n" +

                "#：更新当前单词\n" +
                "&：查找并移动到指定单词位置\n" +
                "*：在初中级4本书中，查找单词\n" +
                "@：调整单词播放时刻 切换\n" +
                "?：显示本帮助信息。";

        if (findViewById(R.id.iv_all).getVisibility() == View.VISIBLE) {
            _info.setText(help);
        } else {
            _dict.setText(help);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.lrc) {
            onOptionsItemSelected(pauseItem);
            _info.setTextSize(TypedValue.COMPLEX_UNIT_PX, 80);
            _info.setText("");
        } else if (v.getId() == R.id.left) {
            if (iStart > 100) {
                DecimalFormat decimalFormat = new DecimalFormat("0000000");
                iStart -= 100;
                iListTime.set(wordIndexArray[wordIndex], iStart);
                listTime.set(wordIndexArray[wordIndex], decimalFormat.format(iStart));
            }
        } else if (v.getId() == R.id.right) {
            if (iStart < duration - 200) {
                DecimalFormat decimalFormat = new DecimalFormat("0000000");
                iStart += 100;
                iListTime.set(wordIndexArray[wordIndex], iStart);
                listTime.set(wordIndexArray[wordIndex], decimalFormat.format(iStart));
            }
        } else if (v.getId() == R.id.favorite) {
            if (iFavorite.get(wordIndexArray[wordIndex]) == SHOW_FAVORITE) {
                return;
            }
            if (iFavorite.get(wordIndexArray[wordIndex]) == SHOW_DEL) {
                removed_num--;
            }
            if (iFavorite.get(wordIndexArray[wordIndex]) == SHOW_RANGE_NORMAL) {
                normal_num--;
            }
            favorite_num++;

            chosen_num = normal_num + favorite_num;
            iFavorite.set(wordIndexArray[wordIndex], SHOW_FAVORITE);
            lrcTV.setBackgroundColor(getResources().getColor(R.color.purple_700));
            saveFile("");
        } else if (v.getId() == R.id.show_choose) {
            if (iFavorite.get(wordIndexArray[wordIndex]) == SHOW_DEL) {
                return;
            }
            if (iFavorite.get(wordIndexArray[wordIndex]) == SHOW_FAVORITE) {
                favorite_num--;
            }
            if (iFavorite.get(wordIndexArray[wordIndex]) == SHOW_RANGE_NORMAL) {
                normal_num--;
            }
            removed_num++;

            chosen_num = normal_num + favorite_num;
            iFavorite.set(wordIndexArray[wordIndex], SHOW_DEL);
            saveFile("");
            if (iShowRange == SHOW_RANGE_ALL) {
                lrcTV.setBackgroundColor(getResources().getColor(R.color.gray));
            } else {
                showNext();
            }
        } else if (v.getId() == R.id.show_all) {
            if (iFavorite.get(wordIndexArray[wordIndex]) == SHOW_RANGE_NORMAL) {
                return;
            }
            if (iFavorite.get(wordIndexArray[wordIndex]) == SHOW_FAVORITE) {
                favorite_num--;
            }
            if (iFavorite.get(wordIndexArray[wordIndex]) == SHOW_DEL) {
                removed_num--;
            }
            normal_num++;
            chosen_num = normal_num + favorite_num;
            iFavorite.set(wordIndexArray[wordIndex], SHOW_RANGE_NORMAL);
            lrcTV.setBackgroundColor(getResources().getColor(R.color.transparent));
            saveFile("");
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    private void showWord() {
        if (showCurrentWord()) {
            showSeekTo();
        }
    }

    private boolean showCurrentWord() {
        if (wordIndex >= iListTime.size() || wordIndexArray == null || wordIndex >= wordIndexArray.length) {
            return false;
        }
        String str = "", strSentence1 = "", strSentence2 = "";
        if (showWordType == SHOW_ALL) {
            if (list_foreign.get(wordIndexArray[wordIndex])
                    .equals(list_pronunciation.get(wordIndexArray[wordIndex]))) {
                str = list_foreign.get(wordIndexArray[wordIndex]) + "\n" +
                        list_native.get(wordIndexArray[wordIndex]);
            } else {
                str = list_foreign.get(wordIndexArray[wordIndex]) + "\n" +
                        list_pronunciation.get(wordIndexArray[wordIndex]) + "\n" +
                        list_native.get(wordIndexArray[wordIndex]);
            }
        } else if (showWordType == SHOW_FOREIGN) {
            str = list_foreign.get(wordIndexArray[wordIndex]);
        } else if (showWordType == SHOW_PRONUNCIATION) {
            str = list_pronunciation.get(wordIndexArray[wordIndex]);
        } else if (showWordType == SHOW_NATIVE) {
            str = list_native.get(wordIndexArray[wordIndex]);
        }
        lrcTV.setText(str);
        sentence1.setVisibility(View.GONE);
        sentence2.setVisibility(View.GONE);
        if (wordIndexArray[wordIndex] < list_sentence1.size()) {
            sentence1.setVisibility(View.VISIBLE);
            sentence2.setVisibility(View.VISIBLE);
            strSentence1 = list_sentence1.get(wordIndexArray[wordIndex]);
            strSentence2 = list_sentence2.get(wordIndexArray[wordIndex]);
        }
        sentence1.setText(strSentence1);
        sentence2.setText(strSentence2);
        if (iFavorite.get(wordIndexArray[wordIndex]) == SHOW_FAVORITE) {
            lrcTV.setBackgroundColor(getResources().getColor(R.color.purple_700));
        } else if (iFavorite.get(wordIndexArray[wordIndex]) == SHOW_DEL) {
            lrcTV.setBackgroundColor(getResources().getColor(R.color.gray));
        } else {
            lrcTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
        showTitle();
        showWordClass();
        return true;
    }

    private void showSeekTo() {
        if (wordIndex < wordIndexArray.length && wordIndexArray[wordIndex] < iListTime.size()) {
            iStart = iListTime.get(wordIndexArray[wordIndex]);
            if (wordIndexArray[wordIndex] + 1 < iListTime.size()) {
                iEnd = iListTime.get(wordIndexArray[wordIndex] + 1);
            }
            if (Math.abs(iStart - musicControl.getCurrentPosition()) > 1000) {
                musicControl.seekTo(iStart);
            }
        }
    }

    private void showFirstWord() {
        if (sortType == 1 || sortType == 2) {
            sortWords();
        }
        boolean isFound = false, isPrev = wordIndex == -1;
        if (iShowRange == SHOW_RANGE_ALL) {
            if (wordIndex == -1) {
                allIndex = -1;
                for (int i = iListTime.size() - 2; i >= 0; i--) {
                    if (iFavorite.get(wordIndexArray[i]) != 3) {
                        allIndex++;
                    }
                }
                wordIndex = iListTime.size() - 2;
            } else {
                allIndex = 0;
                wordIndex = 0;
            }
        } else if (iShowRange == SHOW_RANGE_CLASS) {
            all_num = iListTime.size() - 1;
            if (wordIndex == -1) {
                for (int i = iListTime.size() - 2; i >= 0; i--) {
                    int iwClass = iWordClass.get(wordIndexArray[i]);
                    if (CurrentWordClass == 16) {
                        if (iwClass >= 23 && iwClass <= 25) {
                            wordIndex = i;
                            isFound = true;
                            break;
                        }
                    } else if (CurrentWordClass == 17) {
                        if (iwClass >= 43 && iwClass <= 45) {
                            wordIndex = i;
                            isFound = true;
                            break;
                        }
                    } else {
                        if (iwClass % 20 == CurrentWordClass) {
                            wordIndex = i;
                            isFound = true;
                            break;
                        }
                    }
                }
            } else {
                for (int i = 0; i < iListTime.size(); i++) {
                    int iwClass = iWordClass.get(wordIndexArray[i]);
                    if (CurrentWordClass == 16) {
                        if (iwClass >= 23 && iwClass <= 25) {
                            wordIndex = i;
                            isFound = true;
                            break;
                        }
                    } else if (CurrentWordClass == 17) {
                        if (iwClass >= 43 && iwClass <= 45) {
                            wordIndex = i;
                            isFound = true;
                            break;
                        }
                    } else {
                        if (iwClass % 20 == CurrentWordClass) {
                            wordIndex = i;
                            isFound = true;
                            break;
                        }
                    }
                }
            }
            if (isFound) {
                showWord();
            } else {
                if (isNextLesson) {
                    showNextLesson();
                } else {
                    showPrevLesson();
                }
            }
            return;
        } else if (iShowRange == SHOW_CHOSEN) {
            if (wordIndex == -1) {
                for (int i = iListTime.size() - 2; i >= 0; i--) {
                    if (iFavorite.get(wordIndexArray[i]) == SHOW_RANGE_NORMAL
                            || iFavorite.get(wordIndexArray[i]) == SHOW_FAVORITE) {
                        wordIndex = i;
                        break;
                    }
                }
            } else {
                for (int i = 0; i < iListTime.size(); i++) {
                    if (iFavorite.get(wordIndexArray[i]) == SHOW_RANGE_NORMAL
                            || iFavorite.get(wordIndexArray[i]) == SHOW_FAVORITE) {
                        wordIndex = i;
                        break;
                    }
                }
            }
        } else {
            if (wordIndex == -1) {
                for (int i = iListTime.size() - 2; i >= 0; i--) {
                    if (iFavorite.get(wordIndexArray[i]) == iShowRange) {
                        wordIndex = i;
                        break;
                    }
                }
            } else {
                for (int i = 0; i < iListTime.size(); i++) {
                    if (iFavorite.get(wordIndexArray[i]) == iShowRange) {
                        wordIndex = i;
                        break;
                    }
                }
            }
        }
        int offset = isAdjust ? 0 : 1;
        removed_num = 0;
        normal_num = 0;
        favorite_num = 0;

        for (int i = 0; i < iFavorite.size() - offset; i++) {
            if (iFavorite.get(wordIndexArray[i]) == SHOW_RANGE_NORMAL) {
                normal_num++;
            } else if (iFavorite.get(wordIndexArray[i]) == SHOW_FAVORITE) {
                favorite_num++;
            } else if (iFavorite.get(wordIndexArray[i]) == SHOW_DEL) {
                removed_num++;
            }
        }
        chosen_num = normal_num + favorite_num;
        all_num = chosen_num + removed_num;
        if (isPrev) {
            normalIndex = normal_num - 1;
            delIndex = removed_num - 1;
            favoriteIndex = favorite_num - 1;
            chosenIndex = chosen_num - 1;
        } else {
            normalIndex = 0;
            delIndex = 0;
            favoriteIndex = 0;
            chosenIndex = 0;
        }

        if (isPlayFolder) {
            if (iShowRange == SHOW_CHOSEN && chosen_num == 0) {
                if (isNextLesson) {
                    showNextLesson();
                } else {
                    showPrevLesson();
                }
                return;
            }
            if (iShowRange == SHOW_DEL && removed_num == 0) {
                if (isNextLesson) {
                    showNextLesson();
                } else {
                    showPrevLesson();
                }
                return;
            }
            if (iShowRange == SHOW_FAVORITE && favorite_num == 0) {
                if (isNextLesson) {
                    showNextLesson();
                } else {
                    showPrevLesson();
                }
                return;
            }
        }
        if (wordIndex == -1) {
            wordIndex = 0;
        }
        showWord();
    }

    private void reset() {
        reset(0);
    }

    private void showWordsNormal() {
        showFirstWord();
        findViewById(R.id.ll_buttons).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.info).setVisibility(View.VISIBLE);
        findViewById(R.id.add_lrc).setVisibility(View.INVISIBLE);
        menu_word_class.setVisible(true);
    }

    private void addTime() {
        wordIndexArray = new int[iListTime.size()];
        sortType = 0;
        showWordType = SHOW_ALL;
        sortWords();
        progressBar.setMax(iListTime.size() - 1);
        progressBar.setProgress(1);
        setTitle(fileName + "(/" + (iListTime.size() - 1) + ")");
        findViewById(R.id.ll_buttons).setVisibility(View.INVISIBLE);
        findViewById(R.id.ll_progressBar).setVisibility(View.INVISIBLE);
        findViewById(R.id.info).setVisibility(View.INVISIBLE);
        findViewById(R.id.add_lrc).setVisibility(View.VISIBLE);
        iStart = 0;
    }

    private void showTitle() {
        int show_num = 0, show_index = wordIndex;
        if (iShowRange == SHOW_RANGE_NORMAL) {
            show_index = normalIndex;
            show_num = normal_num;
        } else if (iShowRange == SHOW_FAVORITE) {
            show_index = favoriteIndex;
            show_num = favorite_num;
        } else if (iShowRange == SHOW_DEL) {
            show_index = delIndex;
            show_num = removed_num;
        } else if (iShowRange == SHOW_CHOSEN) {
            show_index = chosenIndex;
            show_num = chosen_num;
        } else if (iShowRange == SHOW_RANGE_ALL) {
            show_index = allIndex;
            show_num = all_num;
        } else if (iShowRange == SHOW_RANGE_CLASS) {
            show_index = wordIndexArray[wordIndex];
            show_num = all_num;
        }
        if (isAdjust) {
            setTitle(fileName + "(" + (wordIndex + 1) + "/" + listTime.size() + ")");
        } else {
            setTitle(fileName + "(" + (show_index + 1) + "/" + show_num + ")");
        }
        progressBar.setMax(show_num);
        progressBar.setProgress(show_index + 1);
    }

    private void showWordClass() {
        int classId0 = iWordClass.get(wordIndexArray[wordIndex]);
        int classId = classId0 % 20;
        if (classId0 >= 43 && classId0 <= 45) {
            tv_word_class.setText("自" + wClass[classId]);
        } else if (classId0 >= 23 && classId0 <= 25) {
            tv_word_class.setText("他" + wClass[classId]);
        } else {
            tv_word_class.setText(wClass[classId]);
        }
        tv_word_class.setBackgroundColor(getResources().getColor(ClassColor[classId]));
        menuWordClass[classId].setChecked(true);
    }

    private void editWords(String str) {
        quickItem.setVisible(true);
        if (str.isEmpty()) {
            Toast.makeText(this, "单词文件不存在！", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "单词文件格式错误！\r\n" + error, Toast.LENGTH_LONG).show();
        }
        saveItem.setVisible(true);
        findViewById(R.id.sv_new_lrc).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.new_lrc)).setText(str);
        iStart = 0;
        musicControl.play(handler, _uri, isMute);
        musicControl.speedTo(1.0f);
    }


    private String searchWord(String search_str) {
        StringBuilder ret = new StringBuilder();
        String lrcPath = Objects.requireNonNull(getExternalFilesDir(null)).getAbsolutePath();
        File lrcFiles = getExternalFilesDir(null);
        String folder[] = lrcFiles.list();
        MyStringComparator cmp = new MyStringComparator();
        Arrays.sort(folder, cmp);

        for (int i = 0; i < folder.length; i++) {
            String pathName = lrcPath + "/" + folder[i];
            File lrcFile = new File(pathName);
            if (!pathName.contains("双语") && lrcFile.isDirectory()) {
                String fileNames[] = lrcFile.list();
                String fileName, tmp = "";
                Arrays.sort(fileNames, cmp);
                for (int j = 0; j < fileNames.length; j++) {
                    fileName = pathName + "/" + fileNames[j];
                    tmp = searchWordFromFile(fileName, folder[i], fileNames[j], search_str);
                    ret.append(tmp);
                }
            }
        }
        String rt = ret.toString();
        if (rt.isEmpty()) {
            return "";
        } else {
            return rt.substring(2);
        }
    }

    private void add00() {
        String lrcPath = Objects.requireNonNull(getExternalFilesDir(null)).getAbsolutePath();
        File lrcFiles = getExternalFilesDir(null);
        String folder[] = lrcFiles.list();
        MyStringComparator cmp = new MyStringComparator();
        Arrays.sort(folder, cmp);

        for (int i = 0; i < folder.length; i++) {
            String pathName = lrcPath + "/" + folder[i];
            File lrcFile = new File(pathName);
            if (!pathName.contains("双语") && lrcFile.isDirectory()) {
                String fileNames[] = lrcFile.list();
                String fileName, tmp = "";
                Arrays.sort(fileNames, cmp);
                for (int j = 0; j < fileNames.length; j++) {
                    fileName = pathName + "/" + fileNames[j];
                    File subFile = new File(fileName);
                    tmp = getNewStringFromFile(subFile);
                    FileOutputStream out = null;
                    BufferedWriter writer = null;
                    try {
                        out = new FileOutputStream(subFile);
                        writer = new BufferedWriter(new OutputStreamWriter(out));
                        writer.write(tmp);
                    } catch (Exception e) {
                        String a = e.toString();
                        a += "";
                    } finally {
                        if (writer != null) {
                            try {
                                writer.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private String searchWordFromFile(String findFileName, String folder,
                                      String fileName, String search_str) {
        File lrcFile = new File(findFileName);
        String tmp = searchWordFromTxtFile(lrcFile, search_str);
        if (tmp.isEmpty()) {
            return "";
        } else {
            return "\n\n  " + folder + " / " + fileName.substring(0, fileName.length() - 4) + tmp;
        }
    }

    public String searchWordFromTxtFile(File file, String str) {
        StringBuilder ret = new StringBuilder();
        String tmp = "";
        InputStreamReader read = null;
        BufferedReader bufferedReader = null;
        try {
            read = new InputStreamReader(new FileInputStream(file));
            bufferedReader = new BufferedReader(read);
            String lineTxt;
            int num = 0;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                num++;
                if (lineTxt.contains(str)) {
                    tmp = "  【" + num + "】" + lineTxt.substring(10);
                    tmp = tmp.replace(" ", "\n");
                    ret.append(tmp);
                }
            }
            bufferedReader.close();
            read.close();
            return ret.toString();
        } catch (Exception e) {
            try {
                bufferedReader.close();
                read.close();
            } catch (Exception e1) {
                //TODO
            }
            e.printStackTrace();
        }
        return ret.toString();
    }

    public String getNewStringFromFile(File file) {
        StringBuilder ret = new StringBuilder();
        InputStreamReader read = null;
        BufferedReader bufferedReader = null;
        try {
            read = new InputStreamReader(new FileInputStream(file));
            bufferedReader = new BufferedReader(read);
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                ret.append(lineTxt.substring(0, 8));
//                ret.append("00");
//                ret.append(lineTxt.substring(8));

                if (lineTxt.startsWith("08", 8)) {
                    ret.append("09");
                } else if (lineTxt.startsWith("09", 8)) {
                    ret.append("08");
                } else {
                    ret.append(lineTxt.substring(8, 10));
                }
                ret.append(lineTxt.substring(10));

                ret.append("\n");
            }
            bufferedReader.close();
            read.close();
            return ret.toString();
        } catch (Exception e) {
            try {
                bufferedReader.close();
                read.close();
            } catch (Exception e1) {
                //TODO
            }
            e.printStackTrace();
        }
        return ret.toString();
    }

    private void reset(int index) {
        isPlay = true;
        isLRC_Format_OK = false;
        isLRC_Time_OK = false;
        iStart = 0;
        wordIndex = index;
        loopIndex = 0;
        isAdjust = false;
        list_foreign.clear();
        list_pronunciation.clear();
        list_native.clear();
        list_sentence1.clear();
        list_sentence2.clear();
        listTime.clear();
        iListTime.clear();
        iFavorite.clear();
        iWordClass.clear();
        //hideKeyboard();

        normalIndex = 0;
        delIndex = 0;
        favoriteIndex = 0;
        chosenIndex = 0;

        findViewById(R.id.show_all).setVisibility(View.VISIBLE);
        findViewById(R.id.show_choose).setVisibility(View.VISIBLE);
        findViewById(R.id.favorite).setVisibility(View.VISIBLE);
        findViewById(R.id.left).setVisibility(View.GONE);
        findViewById(R.id.right).setVisibility(View.GONE);
        findViewById(R.id.iv_all).setVisibility(View.VISIBLE);
        findViewById(R.id.sv_new_lrc).setVisibility(View.INVISIBLE);
        saveItem.setVisible(false);
        quickItem.setVisible(false);
        findViewById(R.id.left).setVisibility(View.GONE);
        findViewById(R.id.right).setVisibility(View.GONE);
        pauseItem.setVisible(true);
        lrcTV.setText("");
        sentence1.setText("");
        sentence2.setText("");
        _new_lrc.setText("");

        String pathAndName = _uri.getPath();
        int start = pathAndName.lastIndexOf("/");
        int end = pathAndName.lastIndexOf(".");
        if (start != -1 && end != -1) {
            fileName = pathAndName.substring(start + 1, end);
            setTitle(fileName);
            String tmp = pathAndName.substring(0, start);
            end = start;
            start = tmp.lastIndexOf("/");
            folderName = pathAndName.substring(start + 1, end);
        }
        String lrcPath = Objects.requireNonNull(getExternalFilesDir(null)).getAbsolutePath();
        String pathName = lrcPath + "/" + folderName + "/" + fileName + ".txt";

        try {
            wordIndexArray = null;
            lrcFile = new File(pathName);
            if (lrcFile.exists()) {
                isLRC_Time_OK = true;
                readTxtFileIntoStringArrList(lrcFile, false);
                wordIndexArray = new int[iListTime.size()];
                sortWords();
                if (isLRC_Time_OK) {
                    if (isLRC_Format_OK) {
                        showWordsNormal();
                    } else {
                        String str = readRawTxtFile(lrcFile);
                        editWords(str);
                    }
                } else {
                    readTxtFileIntoStringArrList(lrcFile, true);
                    if (isLRC_Format_OK) {
                        loopNumber = 0;
                        addTime();
                    } else {
                        String str = readRawTxtFile(lrcFile);
                        editWords(str);
                    }
                }
                musicControl.play(handler, _uri, isMute);
                musicControl.speedTo(1.0f);
                musicControl.seekTo(iStart);
            } else {
                editWords("");
            }
        } catch (Exception e) {
            //TODO
        }
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.show();
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }
    }

    private void showPrevLesson() {
        if (fileIndex > fileBeginIndex) {
            fileIndex--;
        } else {
            fileIndex = fileEndIndex;
        }
        DocumentFile f = files[fileIndex];
        _uri = f.getUri();
        isNextLesson = false;
        reset(-1);
        isNextLesson = true;
    }

    private void showNextLesson() {
        if (fileIndex < fileEndIndex) {
            fileIndex++;
        } else {
            fileIndex = fileBeginIndex;
        }
        DocumentFile f = files[fileIndex];
        _uri = f.getUri();
        isNextLesson = true;
        reset(1);
    }

    private void showPrev() {
        if (iShowRange == SHOW_RANGE_CLASS) {
            showClassPrev();
            return;
        }
        int offset = isAdjust ? 1 : 2;
        if (wordIndex > 0) {
            if (iShowRange == SHOW_RANGE_ALL) {
                wordIndex--;
                allIndex--;
                if (!isAdjust && iFavorite.get(wordIndexArray[wordIndex]) == 3) {
                    wordIndex--;
                }
            } else if (iShowRange == SHOW_CHOSEN) {
                boolean isFound = false;
                for (int i = wordIndex - 1; i >= 0; i--) {
                    if (iFavorite.get(wordIndexArray[i]) == SHOW_RANGE_NORMAL
                            || iFavorite.get(wordIndexArray[i]) == SHOW_FAVORITE) {
                        wordIndex = i;
                        chosenIndex--;
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) {
                    if (isPlayFolder) {
                        showPrevLesson();
                    } else {
                        for (int i = iFavorite.size() - offset; i > wordIndex; i--) {
                            if (iFavorite.get(wordIndexArray[i]) == SHOW_RANGE_NORMAL
                                    || iFavorite.get(wordIndexArray[i]) == SHOW_FAVORITE) {
                                wordIndex = i;
                                chosenIndex = chosen_num - 1;
                                break;
                            }
                        }
                    }
                }
            } else {
                boolean isFound = false;
                for (int i = wordIndex - 1; i >= 0; i--) {
                    if (iFavorite.get(wordIndexArray[i]) == iShowRange) {
                        wordIndex = i;
                        if (iShowRange == SHOW_FAVORITE) {
                            favoriteIndex--;
                        } else if (iShowRange == SHOW_DEL) {
                            delIndex--;
                        } else if (iShowRange == SHOW_RANGE_NORMAL) {
                            normalIndex--;
                        }
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) {
                    if (isPlayFolder) {
                        showPrevLesson();
                    } else {
                        for (int i = iFavorite.size() - offset; i > wordIndex; i--) {
                            if (iFavorite.get(wordIndexArray[i]) == iShowRange) {
                                wordIndex = i;
                                if (iShowRange == SHOW_FAVORITE) {
                                    favoriteIndex = favorite_num - 1;
                                } else if (iShowRange == SHOW_DEL) {
                                    delIndex = removed_num - 1;
                                } else if (iShowRange == SHOW_RANGE_NORMAL) {
                                    normalIndex = normal_num - 1;
                                }
                                break;
                            }
                        }
                    }
                }
            }
            showWord();
        } else {
            if (isPlayFolder) {
                showPrevLesson();
            } else {
                wordIndex = iListTime.size() - offset + 1;
                allIndex = all_num;
                favoriteIndex = favorite_num;
                delIndex = removed_num;
                normalIndex = normal_num;
                showPrev();
            }
        }
    }

    private void showClassNext() {
        int offset = isAdjust ? 1 : 2;
        if (wordIndex < iListTime.size() - offset) {
            boolean isFound = false;
            for (int i = wordIndex + 1; i <= iListTime.size() - offset; i++) {
                int wClass = iWordClass.get(wordIndexArray[i]);
                if (CurrentWordClass == 16) {
                    if (wClass >= 23 && wClass <= 25) {
                        wordIndex = i;
                        isFound = true;
                        break;
                    }
                } else if (CurrentWordClass == 17) {
                    if (wClass >= 43 && wClass <= 45) {
                        wordIndex = i;
                        isFound = true;
                        break;
                    }
                } else {
                    if (wClass % 20 == CurrentWordClass) {
                        wordIndex = i;
                        isFound = true;
                        break;
                    }
                }
            }
            if (!isFound) {
                if (isPlayFolder) {
                    showNextLesson();
                    return;
                } else {
                    for (int i = 0; i < wordIndex; i++) {
                        int wClass = iWordClass.get(wordIndexArray[i]);
                        if (CurrentWordClass == 16) {
                            if (wClass >= 23 && wClass <= 25) {
                                wordIndex = i;
                                break;
                            }
                        } else if (CurrentWordClass == 17) {
                            if (wClass >= 43 && wClass <= 45) {
                                wordIndex = i;
                                break;
                            }
                        } else {
                            if (wClass % 20 == CurrentWordClass) {
                                wordIndex = i;
                                break;
                            }
                        }
                    }
                }
            }
            showWord();
        } else {
            if (isPlayFolder) {
                showNextLesson();
            } else {
                wordIndex = -1;
                sortWords();
                showClassNext();
            }
        }
    }

    private void showClassPrev() {
        int offset = isAdjust ? 1 : 2;
        if (wordIndex > 0) {
            boolean isFound = false;
            for (int i = wordIndex - 1; i >= 0; i--) {
                int wClass = iWordClass.get(wordIndexArray[i]);
                if (CurrentWordClass == 16) {
                    if (wClass >= 23 && wClass <= 25) {
                        wordIndex = i;
                        isFound = true;
                        break;
                    }
                } else if (CurrentWordClass == 17) {
                    if (wClass >= 43 && wClass <= 45) {
                        wordIndex = i;
                        isFound = true;
                        break;
                    }
                } else {
                    if (wClass % 20 == CurrentWordClass) {
                        wordIndex = i;
                        isFound = true;
                        break;
                    }
                }
            }
            if (!isFound) {
                if (isPlayFolder) {
                    showPrevLesson();
                } else {
                    for (int i = iFavorite.size() - offset; i > wordIndex; i--) {
                        int wClass = iWordClass.get(wordIndexArray[i]);
                        if (CurrentWordClass == 16) {
                            if (wClass >= 23 && wClass <= 25) {
                                wordIndex = i;
                                break;
                            }
                        } else if (CurrentWordClass == 17) {
                            if (wClass >= 43 && wClass <= 45) {
                                wordIndex = i;
                                break;
                            }
                        } else {
                            if (wClass % 20 == CurrentWordClass) {
                                wordIndex = i;
                                break;
                            }
                        }
                    }
                }
            }
            showWord();
        } else {
            if (isPlayFolder) {
                showPrevLesson();
            } else {
                wordIndex = iListTime.size() - offset + 1;
                showClassPrev();
            }
        }
    }

    private void showNext() {
        if (iShowRange == SHOW_RANGE_CLASS) {
            showClassNext();
            return;
        }
        int offset = isAdjust ? 1 : 2;
        if (wordIndex < iListTime.size() - offset) {
            if (iShowRange == SHOW_RANGE_ALL) {
                wordIndex++;
                allIndex++;
                if (!isAdjust && iFavorite.get(wordIndexArray[wordIndex]) == 3) {
                    wordIndex++;
                }
            } else if (iShowRange == SHOW_CHOSEN) {
                boolean isFound = false;
                for (int i = wordIndex + 1; i <= iListTime.size() - offset; i++) {
                    if (iFavorite.get(wordIndexArray[i]) == SHOW_RANGE_NORMAL
                            || iFavorite.get(wordIndexArray[i]) == SHOW_FAVORITE) {
                        wordIndex = i;
                        chosenIndex++;
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) {
                    if (isPlayFolder) {
                        showNextLesson();
                    } else {
                        for (int i = 0; i < wordIndex; i++) {
                            if (iFavorite.get(wordIndexArray[i]) == SHOW_RANGE_NORMAL
                                    || iFavorite.get(wordIndexArray[i]) == SHOW_FAVORITE) {
                                wordIndex = i;
                                chosenIndex = 0;
                                break;
                            }
                        }
                    }
                }
            } else {
                boolean isFound = false;
                for (int i = wordIndex + 1; i <= iListTime.size() - offset; i++) {
                    if (iFavorite.get(wordIndexArray[i]) == iShowRange) {
                        wordIndex = i;
                        if (iShowRange == SHOW_FAVORITE) {
                            favoriteIndex++;
                        } else if (iShowRange == SHOW_DEL) {
                            delIndex++;
                        } else if (iShowRange == SHOW_RANGE_NORMAL) {
                            normalIndex++;
                        }
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) {
                    if (isPlayFolder) {
                        showNextLesson();
                        return;
                    } else {
                        for (int i = 0; i < wordIndex; i++) {
                            if (iFavorite.get(wordIndexArray[i]) == iShowRange) {
                                wordIndex = i;
                                if (iShowRange == SHOW_FAVORITE) {
                                    favoriteIndex = 0;
                                } else if (iShowRange == SHOW_DEL) {
                                    delIndex = 0;
                                } else if (iShowRange == SHOW_RANGE_NORMAL) {
                                    normalIndex = 0;
                                }
                                break;
                            }
                        }
                    }
                }
            }
            showWord();
        } else {
            if (isPlayFolder) {
                showNextLesson();
            } else {
                wordIndex = -1;
                allIndex = -1;
                favoriteIndex = -1;
                delIndex = -1;
                normalIndex = -1;
                chosenIndex = -1;
                sortWords();
                showNext();
            }
        }
    }

    private void sortFiles() {
        if (files == null) return;

        Comparator cmp = new MyComparator();
        Arrays.sort(files, cmp);

        if (sortType == 2) {
            shuffleFiles();
        }
    }

    private void sortStringArray(String str[]) {
        Comparator cmp = new MyComparator();
        Arrays.sort(files, cmp);

        if (sortType == 2) {
            shuffleFiles();
        }
    }

    private void shuffleFiles() {
        int length = fileEndIndex + 1;
        if (fileBeginIndex == 0) {
            for (int i = 0; i < length; i++) {
                int iRandNum = (int) (Math.random() * length);
                DocumentFile temp = files[iRandNum];
                files[iRandNum] = files[i];
                files[i] = temp;
            }
        } else {
            for (int i = length - 1; i >= fileBeginIndex; i--) {
                int iRandNum = (int) (Math.random() * (length - fileBeginIndex));
                iRandNum = fileBeginIndex + iRandNum;
                DocumentFile temp = files[iRandNum];
                files[iRandNum] = files[i];
                files[i] = temp;
            }
        }
    }

    private void sortWords() {
        if (wordIndexArray == null) return;

        for (int i = 0; i < wordIndexArray.length; i++) {
            wordIndexArray[i] = i;
        }
        if (sortType == 1 || sortType == 2) {
            shuffleWords();
        }
    }

    private void shuffleWords() {
        int length = wordIndexArray.length;
        for (int i = 0; i < length - 1; i++) {
            int iRandNum = (int) (Math.random() * (length - 1));
            int temp = wordIndexArray[iRandNum];
            wordIndexArray[iRandNum] = wordIndexArray[i];
            wordIndexArray[i] = temp;
        }
    }


    class MyComparator implements Comparator<DocumentFile> {
        @Override
        public int compare(DocumentFile o1, DocumentFile o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    class MyStringComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }

    private void myPopupMenu() {
        if (files == null || fileBeginIndex == fileEndIndex) return;

        Comparator cmp = new MyComparator();
        Arrays.sort(files, cmp);
        PopupMenu popupMenu = new PopupMenu(WordActivity.this, findViewById(R.id.progressBar));
        Menu thisPop = popupMenu.getMenu();
        for (int i = 0; i < files.length; i++) {
            thisPop.add(0, i, i, files[i].getName());
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            fileIndex = item.getItemId();
            if (fileIndex < fileBeginIndex) {
                fileBeginIndex = fileIndex;
            }
            if (fileIndex > fileEndIndex) {
                fileEndIndex = fileIndex;
            }
            sortFiles();
            DocumentFile f = files[item.getItemId()];
            _uri = f.getUri();
            reset();
            return true;
        });
        popupMenu.show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListener() {
        findViewById(R.id.lrc).setOnClickListener(this);
        findViewById(R.id.left).setOnClickListener(this);
        findViewById(R.id.right).setOnClickListener(this);
        findViewById(R.id.favorite).setOnClickListener(this);
        findViewById(R.id.show_all).setOnClickListener(this);
        findViewById(R.id.show_choose).setOnClickListener(this);

        LongClickButton buttonPrev = findViewById(R.id.prev);
        LongClickButton buttonNext = findViewById(R.id.next);
        buttonPrev.setLongClickRepeatListener(this::showPrev, 50);
        buttonPrev.setOnClickListener(v -> showPrev());
        buttonNext.setLongClickRepeatListener(this::showNext, 50);
        buttonNext.setOnClickListener(v -> showNext());

        Intent intent2 = new Intent(this, MusicService.class);
        conn = new MyServiceConn();
        bindService(intent2, conn, BIND_AUTO_CREATE);
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();

            if (data == null) {
                return;
            }

            _uri = data.getData();
            isPlayFolder = false;
            reset();
        });
        launcherFolder = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();

            if (data == null) {
                return;
            }

            _uri = data.getData();

            DocumentFile documentFile = DocumentFile.fromTreeUri(this, _uri);
            files = documentFile.listFiles();
            if (files.length == 0) {
                return;
            }
            fileBeginIndex = 0;
            fileEndIndex = files.length - 1;
            sortFiles();
            isPlayFolder = true;
            rangeItem.setVisible(true);
            chooseItem.setVisible(true);
            DocumentFile f = files[0];
            _uri = f.getUri();
            reset();
        });
        _dict.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().endsWith("*")) {
                    String str = s.toString().substring(0, s.toString().length() - 1);
                    String found = searchWord(str);
                    _dict.setTextSize(TypedValue.COMPLEX_UNIT_PX, 60);
                    _dict.setText(found);
                    _dict.setFocusable(true);
                    _dict.setFocusableInTouchMode(true);
                    _dict.requestFocus();
                    _dict.setSelection(_dict.getText().toString().length());
                } else if (s.toString().endsWith("\n")) {
                    _dict.setText("");
                } else if (s.toString().endsWith(" ")) {
                    _dict.setText("");
                    //add00();
                }
            }
        });
        _info.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().endsWith("\n")) {
                    if (s.toString().equals("\n")) {
                        _info.setText("");
                        showNext();
                        return;
                    }
                    String str = s.toString().substring(0, s.toString().length() - 1);
                    if (!str.equals(list_foreign.get(wordIndexArray[wordIndex]))
                            && !str.equals(list_pronunciation.get(wordIndexArray[wordIndex]))
                            && !list_native.get(wordIndexArray[wordIndex]).contains(str)) {
                        _info.setText(list_foreign.get(wordIndexArray[wordIndex])
                                + "\n" + list_pronunciation.get(wordIndexArray[wordIndex])
                                + "\n" + list_native.get(wordIndexArray[wordIndex]));

                        _info.setFocusable(true);
                        _info.setFocusableInTouchMode(true);
                        _info.requestFocus();
                        _info.setSelection(_info.getText().toString().length());
                        return;
                    }
                    _info.setText("");

                } else if (s.toString().endsWith("＜") || s.toString().endsWith("<")) {
                    showPrev();
                    _info.setText("");
                } else if (s.toString().endsWith(" ")) {
                    _info.setText("");
                } else if (s.toString().endsWith("?")) {
                    showHelp();
                } else if (s.toString().endsWith("+")) {
                    if (!isAdjust || sortType != 0 || iShowRange != SHOW_RANGE_ALL || wordIndex == listTime.size() - 1) {
                        Toast.makeText(WordActivity.this, "要：调时，顺序，全部播放，才可以。", Toast.LENGTH_LONG).show();
                        return;
                    }
                    int time = iListTime.get(wordIndex) + iListTime.get(wordIndex + 1);
                    time /= 2;
                    iFavorite.add(wordIndex + 1, 0);
                    iWordClass.add(wordIndex + 1, 0);
                    iListTime.add(wordIndex + 1, time);
                    DecimalFormat decimalFormat = new DecimalFormat("0000000");
                    listTime.add(wordIndex + 1, decimalFormat.format(time));

                    String[] strArray = s.toString().substring(0, s.toString().length() - 1).split("\n");
                    if (strArray.length == 3) {
                        list_foreign.add(wordIndex + 1, strArray[0]);
                        list_pronunciation.add(wordIndex + 1, strArray[1]);
                        list_native.add(wordIndex + 1, strArray[2]);
                        list_sentence1.add("");
                        list_sentence2.add("");
                        _info.setText("");
                        saveFile("");
                        showWord();
                    } else if (strArray.length == 2) {
                        list_foreign.add(wordIndex + 1, strArray[0]);
                        list_pronunciation.add(wordIndex + 1, strArray[0]);
                        list_native.add(wordIndex + 1, strArray[1]);
                        list_sentence1.add("");
                        list_sentence2.add("");
                        _info.setText("");
                        saveFile("");
                        showWord();
                    } else {
                        Toast.makeText(WordActivity.this, "格式错误。", Toast.LENGTH_LONG).show();
                    }
                } else if (s.toString().endsWith("-")) {
                    if (!isAdjust || sortType != 0 || iShowRange != SHOW_RANGE_ALL || wordIndex == listTime.size() - 1) {
                        Toast.makeText(WordActivity.this, "要：调整时刻模式，顺序，全部播放，才可以。", Toast.LENGTH_LONG).show();
                        return;
                    }
                    iFavorite.remove(wordIndex);
                    iWordClass.remove(wordIndex);
                    iListTime.remove(wordIndex);
                    listTime.remove(wordIndex);
                    list_foreign.remove(wordIndex);
                    list_pronunciation.remove(wordIndex);
                    list_native.remove(wordIndex);
                    _info.setText("");
                    saveFile("");
                    showNext();
                } else if (s.toString().equals("!")) {
                    if (iFavorite.get(wordIndexArray[wordIndex]) == 3) {
                        _info.setText("");
                        return;
                    }
                    if (!isAdjust || sortType != 0 || iShowRange != SHOW_RANGE_ALL || wordIndex == listTime.size() - 1) {
                        Toast.makeText(WordActivity.this, "要：调整时刻模式，顺序，全部播放，才可以。", Toast.LENGTH_LONG).show();
                        _info.setText("");
                        return;
                    }
                    if (iFavorite.get(wordIndexArray[wordIndex]) == SHOW_FAVORITE) {
                        favorite_num--;
                    }
                    if (iFavorite.get(wordIndexArray[wordIndex]) == SHOW_DEL) {
                        removed_num--;
                    }
                    if (iFavorite.get(wordIndexArray[wordIndex]) == SHOW_RANGE_NORMAL) {
                        normal_num--;
                    }
                    chosen_num = normal_num + favorite_num;
                    iFavorite.set(wordIndexArray[wordIndex], 3);
                    lrcTV.setBackgroundColor(getResources().getColor(R.color.transparent));
                    saveFile("");
                    _info.setText("");
                } else if (s.toString().endsWith("#")) {
                    String[] strArray = s.toString().substring(0, s.toString().length() - 1).split("\n");
                    if (strArray.length == 3) {
                        if (strArray[0].contains(" ") || strArray[1].contains(" ") || strArray[2].contains(" ")) {
                            Toast.makeText(WordActivity.this, "格式错误。", Toast.LENGTH_LONG).show();
                            return;
                        }
                        list_foreign.set(wordIndexArray[wordIndex], strArray[0]);
                        list_pronunciation.set(wordIndexArray[wordIndex], strArray[1]);
                        list_native.set(wordIndexArray[wordIndex], strArray[2]);
                        _info.setText("");
                        saveFile("");
                        showWord();
                    } else if (strArray.length == 2) {
                        if (strArray[0].contains(" ") || strArray[1].contains(" ")) {
                            Toast.makeText(WordActivity.this, "格式错误。", Toast.LENGTH_LONG).show();
                            return;
                        }
                        list_foreign.set(wordIndexArray[wordIndex], strArray[0]);
                        list_pronunciation.set(wordIndexArray[wordIndex], strArray[0]);
                        list_native.set(wordIndexArray[wordIndex], strArray[1]);
                        _info.setText("");
                        saveFile("");
                        showWord();
                    } else {
                        Toast.makeText(WordActivity.this, "格式错误。", Toast.LENGTH_LONG).show();
                    }
                } else if (s.toString().endsWith("&")) {
                    if (iShowRange != SHOW_RANGE_ALL) {
                        _info.setText("");
                        Toast.makeText(WordActivity.this, "请选择显示所有单词模式。", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (sortType != 0) {
                        _info.setText("");
                        Toast.makeText(WordActivity.this, "请选择顺序显示模式。", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String str = s.toString().substring(0, s.toString().length() - 1);
                    int foundIndex = -1;
                    for (int i = 0; i < list_foreign.size(); i++) {
                        if (list_foreign.get(i).contains(str)) {
                            foundIndex = i;
                            break;
                        }
                        if (list_pronunciation.get(i).contains(str)) {
                            foundIndex = i;
                            break;
                        }
                        if (list_native.get(i).contains(str)) {
                            foundIndex = i;
                            break;
                        }
                    }
                    if (foundIndex == -1) {
                        _info.setText("Not Found!");
                        _info.setFocusable(true);
                        _info.setFocusableInTouchMode(true);
                        _info.requestFocus();
                        _info.setSelection(_info.getText().toString().length());
                    } else {
                        wordIndex = foundIndex;
                        _info.setText("");
                        allIndex = foundIndex;
                        showWord();
                    }
                } else if (s.toString().endsWith("*")) {
                    String str = s.toString().substring(0, s.toString().length() - 1);
                    String found = searchWord(str);
                    _info.setTextSize(TypedValue.COMPLEX_UNIT_PX, 60);
                    _info.setText(found);
                    _info.setFocusable(true);
                    _info.setFocusableInTouchMode(true);
                    _info.requestFocus();
                    _info.setSelection(_info.getText().toString().length());
                } else if (s.toString().endsWith("@")) {
                    if (iShowRange != SHOW_RANGE_ALL) {
                        _info.setText("");
                        Toast.makeText(WordActivity.this, "请选择显示所有单词模式。", Toast.LENGTH_LONG).show();
                        return;
                    }
                    isAdjust = !isAdjust;
                    if (isAdjust) {
                        loopNumber = 1;
                        findViewById(R.id.left).setVisibility(View.VISIBLE);
                        findViewById(R.id.right).setVisibility(View.VISIBLE);
                        findViewById(R.id.show_all).setVisibility(View.GONE);
                        findViewById(R.id.show_choose).setVisibility(View.GONE);
                        findViewById(R.id.favorite).setVisibility(View.GONE);
                        saveLRCItem.setVisible(true);
                    } else {
                        findViewById(R.id.show_all).setVisibility(View.VISIBLE);
                        findViewById(R.id.show_choose).setVisibility(View.VISIBLE);
                        findViewById(R.id.favorite).setVisibility(View.VISIBLE);
                        findViewById(R.id.left).setVisibility(View.GONE);
                        findViewById(R.id.right).setVisibility(View.GONE);
                        saveLRCItem.setVisible(false);
                    }
                    showTitle();
                    _info.setText("");
                }
            }

        });
        findViewById(R.id.add_lrc).setOnTouchListener((v, event) -> {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN: {
                    if (wordIndex < iListTime.size()) {
                        v.setBackground(getResources().getDrawable(R.drawable.background_add));
                        DecimalFormat decimalFormat = new DecimalFormat("0000000");
                        iStart = musicControl.getCurrentPosition();
                        showCurrentWord();
                        iListTime.set(wordIndex, iStart);
                        listTime.set(wordIndex, decimalFormat.format(iStart));
                        if (list_foreign.get(wordIndex).equals("3")) {
                            iFavorite.set(wordIndex, 3);
                        }
                        wordIndex++;

                        setTitle(fileName + "(" + (wordIndex + 1) + "/" + iListTime.size() + ")");
                        progressBar.setMax(iListTime.size());
                        progressBar.setProgress(wordIndex + 1);
                    } else {
                        isLRC_Time_OK = true;
                        saveFile("");
                    }

                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP: {
                    v.setBackground(getResources().getDrawable(R.drawable.background_main));
                    break;
                }
            }
            return true;
        });
    }

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            if (duration == 0 && bundle.getInt("duration") > 0) {
                duration = bundle.getInt("duration");
            }
            if (!isLRC_Time_OK || !isLRC_Format_OK) {
                iEnd = duration;
                if (!!isLRC_Time_OK || !isLRC_Format_OK) {
                    return;
                }
            }
            if (loopNumber == 0) {
                if (musicControl.getCurrentPosition() >= iEnd) {
                    musicControl.seekTo(iStart);
                }
                return;
            } else if (loopNumber == 1) {
                if (wordIndexArray[wordIndex] + 1 < iListTime.size()) {
                    if (musicControl.getCurrentPosition() > iListTime.get(wordIndexArray[wordIndex] + 1)) {
                        showNext();
                    }
                } else {
                    showNext();
                }
            } else {
                if (musicControl.getCurrentPosition() < iEnd) {
                    return;
                }
                if (loopIndex < loopNumber - 1) {
                    loopIndex++;
                    musicControl.seekTo(iStart);
                    return;
                }
                loopIndex = 0;
                showNext();
            }
        }
    };

    static class MyServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicControl = (MusicService.MusicControl) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    public boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public boolean readTxtFileIntoStringArrList(File file, boolean isMakeLRC) {
        InputStreamReader read = null;
        BufferedReader bufferedReader = null;
        try {
            read = new InputStreamReader(new FileInputStream(file));
            bufferedReader = new BufferedReader(read);
            String lineTxt;
            String[] strArray;
            isLRC_Time_OK = true;
            isLRC_Format_OK = true;
            while ((lineTxt = bufferedReader.readLine()) != null && !lineTxt.isEmpty()) {
                if (lineTxt.length() < 10 || isNumeric(lineTxt.substring(0, 10)) == false) {
                    isLRC_Time_OK = false;
                }
                if (isMakeLRC) {
                    iFavorite.add(0);
                    iWordClass.add(0);
                    iListTime.add(0);
                    listTime.add("");
                    strArray = lineTxt.split(" ");
                } else {
                    if (lineTxt.length() < 10 || isNumeric(lineTxt.substring(0, 10)) == false) {
                        isLRC_Time_OK = false;
                        bufferedReader.close();
                        read.close();
                        return false;
                    }
                    String flag = lineTxt.substring(0, 1);
                    iFavorite.add(Integer.parseInt(flag));
                    String time = lineTxt.substring(1, 8);
                    iListTime.add(Integer.parseInt(time));
                    listTime.add(time);
                    String wCls = lineTxt.substring(8, 10);
                    iWordClass.add(Integer.parseInt(wCls));
                    strArray = lineTxt.substring(10).split(" ");
                }
                if (strArray.length == 1) {
                    if (strArray[0].equals("完了") || strArray[0].equals("结束") || strArray[0].equals("3")) {
                        if (list_foreign.size() == list_sentence1.size()) {
                            list_sentence1.add(strArray[0]);
                            list_sentence2.add(strArray[0]);
                        }
                        list_foreign.add(strArray[0]);
                        list_pronunciation.add(strArray[0]);
                        list_native.add(strArray[0]);
                    } else {
                        isLRC_Format_OK = false;
                        error = "1";
                    }
                    continue;
                }
                if (strArray.length == 2) {
                    list_foreign.add(strArray[0]);
                    list_pronunciation.add(strArray[0]);
                    list_native.add(strArray[1]);
                    list_sentence1.add("");
                    list_sentence2.add("");
                    continue;
                }
                if (strArray.length < 3 || lineTxt.trim().isEmpty()) {
                    isLRC_Format_OK = false;
                    error = "2";
                    continue;
                }

                list_foreign.add(strArray[0]);
                list_pronunciation.add(strArray[1]);
                list_native.add(strArray[2]);
                if (strArray.length >= 5) {
                    list_sentence1.add(strArray[3]);
                    list_sentence2.add(strArray[4]);
                }
            }
            bufferedReader.close();
            read.close();
            return true;
        } catch (Exception e) {
            isLRC_Time_OK = false;
            isLRC_Format_OK = false;
            error = e.toString();
            try {
                bufferedReader.close();
                read.close();
            } catch (Exception e1) {
                //TODO
            }
            e.printStackTrace();
        }
        return false;
    }

    private String readRawTxtFile(File file) {
        InputStreamReader read;
        BufferedReader bufferedReader;
        StringBuilder stringBuffer = new StringBuilder();
        try {
            read = new InputStreamReader(new FileInputStream(file));
            bufferedReader = new BufferedReader(read);
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                if (isLRC_Time_OK) {
                    stringBuffer.append(lineTxt.substring(10));
                } else {
                    stringBuffer.append(lineTxt);
                }
                stringBuffer.append("\n");
            }
            bufferedReader.close();
            read.close();
            return stringBuffer.toString();
        } catch (Exception e) {
            e.toString();
        }
        return "";
    }

    public String readTxtFile(File file) {
        InputStreamReader read;
        BufferedReader bufferedReader;
        StringBuilder stringBuffer = new StringBuilder();
        try {
            list_foreign.clear();
            list_pronunciation.clear();
            list_native.clear();
            list_sentence1.clear();
            list_sentence2.clear();
            listTime.clear();
            iListTime.clear();
            iFavorite.clear();
            iWordClass.clear();
            read = new InputStreamReader(new FileInputStream(file));
            bufferedReader = new BufferedReader(read);
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                String flag = lineTxt.substring(0, 1);
                iFavorite.add(Integer.parseInt(flag));
                String time = lineTxt.substring(1, 8);
                iListTime.add(Integer.parseInt(time));
                listTime.add(time);
                String wCls = lineTxt.substring(8, 10);
                iWordClass.add(Integer.parseInt(wCls));

                stringBuffer.append(lineTxt.substring(10));
                stringBuffer.append("\n");
            }
            bufferedReader.close();
            read.close();
            return stringBuffer.toString();
        } catch (Exception e) {
            e.toString();
        }
        return "";
    }

    private int newClass(int c) {
//        if (c == 3) {
//            return 23;
//        }
//        if (c == 4) {
//            return 24;
//        }
//        if (c == 5) {
//            return 25;
//        }
//        if (c == 6) {
//            return 43;
//        }
//        if (c == 7) {
//            return 44;
//        }
//        if (c == 8) {
//            return 45;
//        }

        if (c > 8) {
            c -= 3;
        }
        if (c == 8) {
            c = 9;
        } else if (c == 9) {
            c = 8;
        }
        return c;
    }

    public void saveFile(String msg) {
        StringBuilder stringBuffer = null;
        DecimalFormat decimalFormat = new DecimalFormat("00");
        if (isLRC_Time_OK) {
            if (msg.isEmpty()) {
                stringBuffer = new StringBuilder();
                for (int i = 0; i < iListTime.size(); i++) {
                    stringBuffer.append(iFavorite.get(i));
                    stringBuffer.append(listTime.get(i));
                    //int a = newClass(iWordClass.get(i));
                    stringBuffer.append(decimalFormat.format(iWordClass.get(i)));
                    stringBuffer.append(list_foreign.get(i));
                    stringBuffer.append(" ");
                    stringBuffer.append(list_pronunciation.get(i));
                    stringBuffer.append(" ");
                    stringBuffer.append(list_native.get(i));
                    if (i < list_sentence1.size()) {
                        stringBuffer.append(" ");
                        stringBuffer.append(list_sentence1.get(i));
                        stringBuffer.append(" ");
                        stringBuffer.append(list_sentence2.get(i));
                    }
                    stringBuffer.append("\n");
                }
            } else {
                String str = _new_lrc.getText().toString();
                String[] strArray = str.split("\n");
                if (strArray.length == iListTime.size()) {
                    stringBuffer = new StringBuilder();
                    for (int i = 0; i < iListTime.size(); i++) {
                        stringBuffer.append(iFavorite.get(i));
                        stringBuffer.append(decimalFormat.format(iWordClass.get(i)));
                        stringBuffer.append(listTime.get(i));
                        stringBuffer.append(strArray[i]);
                        stringBuffer.append("\n");
                    }
                }
            }
        }
        String lrcPath = Objects.requireNonNull(getExternalFilesDir(null)).getAbsolutePath();

        String pathName = lrcPath + "/" + folderName + "/" + fileName + ".txt";

        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            File fileImage = new File(pathName);
            File parentFile = fileImage.getParentFile();
            if (parentFile == null) {
                Toast.makeText(WordActivity.this, "保存失败", Toast.LENGTH_LONG).show();
                return;
            }
            if (!parentFile.exists()) {
                boolean dirFile = parentFile.mkdirs();
                if (!dirFile) {
                    Toast.makeText(WordActivity.this, "创建目录失败！", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            if (!fileImage.exists()) {
                fileImage.createNewFile();
            }

            out = new FileOutputStream(fileImage);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            if (stringBuffer == null) {
                String str = _new_lrc.getText().toString();
                writer.write(str);
            } else {
                writer.write(stringBuffer.toString());
            }
            Toast.makeText(this, msg + "保存成功", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void unbind() {
        musicControl.pausePlay();
        unbindService(conn);
    }

    @Override
    public void onBackPressed() {

        if ((System.currentTimeMillis() - _exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), R.string.once_more, Toast.LENGTH_SHORT).show();
            _exitTime = System.currentTimeMillis();
        } else {
            try {
                unbind();
                mMediaSession.release();
                finish();
                System.exit(0);
            } catch (Exception e) {
                e.toString();
            }
        }
    }

    public void setLessonInterval(int from, int to) {
        fileBeginIndex = from;
        fileEndIndex = to;
        if (fileIndex >= fileBeginIndex && fileIndex <= fileEndIndex) {
            return;
        }

        fileIndex = fileBeginIndex;

        sortFiles();

        DocumentFile f = files[fileIndex];
        _uri = f.getUri();
        reset();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        boolean bRet = true;
        //bRet = super.onKeyDown(keyCode, event);

        if (KeyEvent.KEYCODE_HEADSETHOOK == keyCode) {
            onOptionsItemSelected(pauseItem);
            bRet = true;
        } else if (KeyEvent.KEYCODE_MEDIA_NEXT == keyCode) {

        } else if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == keyCode) {
//        } else if (24 == keyCode) {
//            showPrev();
//        } else if (25 == keyCode) {
//            showNext();
        } else {
            //Toast.makeText(getApplicationContext(), "" + keyCode, Toast.LENGTH_SHORT).show();
            bRet = super.onKeyDown(keyCode, event);
        }

        return bRet;
    }

    private void setBluetoothButton() {
        ComponentName mbr = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());
        mMediaSession = new MediaSessionCompat(this, "mbr", mbr, null);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        if (!mMediaSession.isActive()) {
            mMediaSession.setActive(true);
        }
        mMediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    if (TextUtils.equals(action, Intent.ACTION_MEDIA_BUTTON)) {
                        KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                        if (keyEvent != null) {
                            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                                int keyCode = keyEvent.getKeyCode();
                                switch (keyCode) {
                                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                                        onOptionsItemSelected(pauseItem);
                                        break;
                                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                                        showNext();
                                        break;
                                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                                        showPrev();
                                        break;
//                                    case KeyEvent.KEYCODE_MEDIA:
//                                        showPrev();
//                                        break;
                                }
                            }
                        }
                    }
                }

                return super.onMediaButtonEvent(intent);
            }
        });
    }
}
