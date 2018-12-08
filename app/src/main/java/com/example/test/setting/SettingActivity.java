package com.example.test.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.myautocar.R;
import com.example.test.popupwindow.MyPopupWindow;

/**
 * Created by hefvcjm on 17-3-21.
 */

public class SettingActivity extends Activity
        implements View.OnTouchListener, View.OnFocusChangeListener, View.OnClickListener, View.OnKeyListener {

    public static final int CONTROL_BY_STEP = 0;
    public static final int CONTROL_BY_AUTO = 1;
    public static final int CONTROL_BY_TRACKING = 2;

    public static final String FILE_NAME = "data";
    public static final String DEFAULT_FILE_NAME = "data_default";

    private TextView tv_select_mode;
    private TextView tv_select_width_step;
    private TextView tv_select_length_step;
    private TextView tv_array_row;//行数
    private TextView tv_array_col;//列数
    private TextView tv_area_width;
    private TextView tv_area_length;

    private TextView tv_setting_info;

    private EditText et_select_mode;
    private EditText et_select_width_step;
    private EditText et_select_length_step;
    private EditText et_array_row;//行数
    private EditText et_array_col;//列数
    private EditText et_area_width;
    private EditText et_area_length;

    private View setting_view;
    private View scroll_view;
    private View info_bar;

    private Button bt_setting_default;
    private Button bt_setting_save;

    private int controlMode;//控制模式，步进或者自动
    private int colorFading;
    private int colorEditable;

    private boolean isEditable;
    private boolean isSelfDefineWidthStep;
    private boolean isSelfDefineLengthStep;

    private Intent intentForResult;

    Handler mHandler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String str = (String) msg.obj;
            switch (msg.what) {
                case MyPopupWindow.MODE_SELECTED:
                    et_select_mode.setText(str);
                    if (str.equals("自动步进")) {
                        controlMode = CONTROL_BY_STEP;
                        isEditable = true;
                    }else if (str.equals("实时遥控")) {
                        controlMode = CONTROL_BY_AUTO;
                        isEditable = false;
                    }
//                    else if (str.equals("自动循迹")){
//                        controlMode = CONTROL_BY_TRACKING;
//                        isEditable = true;
//                    }
                    else {
                        controlMode = CONTROL_BY_STEP;
                        isEditable = true;
                    }
                    break;
                case MyPopupWindow.WIDTH_STEP_SELECTED:
                    if (str.equals("自定义")) {
                        isSelfDefineWidthStep = true;
                        getInputStep(str, et_select_width_step);
                        break;
                    } else {
                        isSelfDefineWidthStep = false;
                    }
                    et_select_width_step.setText(str);
                    setInfoBarText();
                    break;
                case MyPopupWindow.LENGTH_STEP_SELECTED:
                    if (str.equals("自定义")) {
                        isSelfDefineLengthStep = true;
                        getInputStep(str, et_select_length_step);
                        break;
                    } else {
                        isSelfDefineLengthStep = false;
                    }
                    et_select_length_step.setText(str);
                    setInfoBarText();
                    break;
                default:
                    break;

            }
            setEditTextState();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_setting);
        //full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        init();

    }

    //初始化
    private void init() {

        controlMode = CONTROL_BY_STEP;
        isEditable = true;
        isSelfDefineWidthStep = false;
        isSelfDefineLengthStep = false;
        colorFading = getResources().getColor(R.color.font_gray);
        colorEditable = getResources().getColor(R.color.font_white);

        setting_view = findViewById(R.id.setting);
        scroll_view = findViewById(R.id.scroll_view);
        info_bar = findViewById(R.id.info_bar);

        et_select_mode = (EditText) findViewById(R.id.et_mode);
        et_select_width_step = (EditText) findViewById(R.id.et_width_step_space);
        et_select_length_step = (EditText) findViewById(R.id.et_length_step_space);
        et_array_row = (EditText) findViewById(R.id.et_array_row);
        et_array_col = (EditText) findViewById(R.id.et_array_col);
        et_area_width = (EditText) findViewById(R.id.et_area_width);
        et_area_length = (EditText) findViewById(R.id.et_area_length);

        tv_select_mode = (TextView) findViewById(R.id.tv_mode);
        tv_select_width_step = (TextView) findViewById(R.id.tv_width_step_space);
        tv_select_length_step = (TextView) findViewById(R.id.tv_length_step_space);
        tv_array_row = (TextView) findViewById(R.id.tv_array_row);
        tv_array_col = (TextView) findViewById(R.id.tv_array_col);
        tv_area_width = (TextView) findViewById(R.id.tv_area_width);
        tv_area_length = (TextView) findViewById(R.id.tv_area_length);
        tv_setting_info = (TextView) findViewById(R.id.tv_setting_info);

        bt_setting_default = (Button) findViewById(R.id.bt_default);
        bt_setting_save = (Button) findViewById(R.id.bt_save);

        intentForResult = new Intent();

        if (!isFileExist(DEFAULT_FILE_NAME)){
            saveSetting(DEFAULT_FILE_NAME);
        }
        if (isFileExist(FILE_NAME)) {
            loadSetting(FILE_NAME);
        }

        et_select_mode.setOnTouchListener(this);
        et_select_width_step.setOnTouchListener(this);
        et_select_length_step.setOnTouchListener(this);
        setting_view.setOnTouchListener(this);
        scroll_view.setOnTouchListener(this);

        et_area_width.setOnFocusChangeListener(this);
        et_area_length.setOnFocusChangeListener(this);
        et_array_row.setOnFocusChangeListener(this);
        et_array_col.setOnFocusChangeListener(this);

        et_area_width.setOnKeyListener(this);
        et_area_length.setOnKeyListener(this);
        et_array_row.setOnKeyListener(this);
        et_array_col.setOnKeyListener(this);

        bt_setting_save.setOnClickListener(this);
        bt_setting_default.setOnClickListener(this);

        //设置步进是否设置
        if (!isEditable) {
            tv_select_width_step.setTextColor(colorFading);
            tv_select_length_step.setTextColor(colorFading);
            et_select_width_step.setTextColor(colorFading);
            et_select_length_step.setTextColor(colorFading);
        }
        setInfoBarText();

        //监听软件盘状态
        setting_view.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect r = new Rect();
                        setting_view.getWindowVisibleDisplayFrame(r);
                        int screenHeight = setting_view.getRootView().getHeight();
                        int heightDifference = screenHeight - (r.bottom);
                        if (heightDifference > 200) {
                            //软键盘显示
                            Log.d("softInput", "open");
                        } else {
                            //软键盘隐藏
                            setting_view.requestFocus();
                            Log.d("softInput", "close");
                        }
                    }
                });
    }

    private void setEditTextState() {
        setInfoBarText();
        et_area_width.setEnabled(isEditable);
        et_area_length.setEnabled(isEditable);
        et_array_row.setEnabled(isEditable);
        et_array_col.setEnabled(isEditable);
        if (isEditable) {
            tv_select_width_step.setTextColor(colorEditable);
            tv_select_length_step.setTextColor(colorEditable);
            tv_array_row.setTextColor(colorEditable);
            tv_array_col.setTextColor(colorEditable);
            tv_area_width.setTextColor(colorEditable);
            tv_area_length.setTextColor(colorEditable);

            et_select_width_step.setTextColor(colorEditable);
            et_select_length_step.setTextColor(colorEditable);
            et_area_width.setTextColor(colorEditable);
            et_area_length.setTextColor(colorEditable);
            et_array_row.setTextColor(colorEditable);
            et_array_col.setTextColor(colorEditable);
        } else {
            tv_select_width_step.setTextColor(colorFading);
            tv_select_length_step.setTextColor(colorFading);
            tv_array_row.setTextColor(colorFading);
            tv_array_col.setTextColor(colorFading);
            tv_area_width.setTextColor(colorFading);
            tv_area_length.setTextColor(colorFading);

            et_select_width_step.setTextColor(colorFading);
            et_select_length_step.setTextColor(colorFading);
            et_area_width.setTextColor(colorFading);
            et_area_length.setTextColor(colorFading);
            et_array_row.setTextColor(colorFading);
            et_array_col.setTextColor(colorFading);
        }
    }

    private void setInfoBarText() {

        info_bar.setVisibility(ViewGroup.VISIBLE);

        boolean isDismatch = false;
        boolean isStepDismatch = false;

        double width = Double.parseDouble(et_area_width.getText().toString());
        double length = Double.parseDouble(et_area_length.getText().toString());
        int row = Integer.parseInt(et_array_row.getText().toString()) - 1;
        int col = Integer.parseInt(et_array_col.getText().toString()) - 1;

        double subWidth = Double.parseDouble(String.format("%.2f", width / row));
        double subLength = Double.parseDouble(String.format("%.2f", length / col));
        double selfDefineWidthStep = Double.parseDouble(et_select_width_step.getText().toString());
        double selfDefineLengthStep = Double.parseDouble(et_select_length_step.getText().toString());

        if (subWidth != subLength) {
            isDismatch = true;
        }
        if (selfDefineWidthStep != subWidth || selfDefineLengthStep != subLength) {
            isStepDismatch = true;
        }

        if (!isEditable) {
            tv_setting_info.setText(getResources().getString(R.string.str_info_uneditable));
        } else if (isDismatch && isStepDismatch) {
            tv_setting_info.setText("(1)" + getResources().getString(R.string.str_info_dismatch));
            tv_setting_info.append("\n(2)" + getResources().getString(R.string.str_info_step_dismatch));
        } else if (isDismatch) {
            tv_setting_info.setText(getResources().getString(R.string.str_info_dismatch));
        } else if (isStepDismatch) {
            tv_setting_info.setText(getResources().getString(R.string.str_info_step_dismatch));
        } else {
            info_bar.setVisibility(ViewGroup.GONE);
        }

    }

    //输入合法性检测
    private void checkAreaInput(EditText et) {

        String str = et.getText().toString();
        if (str.length() == 0) {
            et.setText("10.00");
            return;
        }
        if (Double.parseDouble(str) > 100) {
            et.setText("100.00");
            Toast.makeText(SettingActivity.this, "数值不能大于100.00！", Toast.LENGTH_SHORT).show();
        } else if (Double.parseDouble(str) < 0.1) {
            et.setText("0.10");
            Toast.makeText(SettingActivity.this, "数值不能小于0.10！", Toast.LENGTH_SHORT).show();
        } else {
            String text = String.format("%.2f", Double.parseDouble(str));
            et.setText(text);
        }

    }

    //输入合法性检测
    private void checkArrayInput(EditText et) {

        String toastFlag = null;

        String distance = "10.00";
        String num = null;

        double distanceDigit;
        int numDigit;

        num = et.getText().toString();

        if (et.equals(et_array_row)) {
            distance = et_area_width.getText().toString();
            toastFlag = "行数不能大于";
        }
        if (et.equals(et_array_col)) {
            distance = et_area_length.getText().toString();
            toastFlag = "列数不能大于";
        }

        distanceDigit = Double.parseDouble(distance);

        int maxNum = (int) (10 * distanceDigit) + 1;
        if (maxNum > 50) {
            maxNum = 50;
        }

        if (num.length() == 0) {
            if (10 > maxNum) {
                et.setText(maxNum + "");
            } else {
                et.setText("10");
            }
            return;
        }

        numDigit = Integer.parseInt(num);

        if (numDigit == 0 || numDigit == 1) {
            if (10 > maxNum) {
                et.setText("" + maxNum);
            } else {
                et.setText("10");
            }
        } else if (numDigit > maxNum) {
            et.setText("" + maxNum);
            Toast.makeText(SettingActivity.this, toastFlag + maxNum + "！",
                    Toast.LENGTH_SHORT).show();
        } else {
            et.setText("" + numDigit);
        }

    }

    String my_input_str = "";

    //自定义步进间距
    private void getInputStep(final String old, final EditText et) {

        final String oldString = old;

        final AlertDialog.Builder dialog = new AlertDialog.Builder(SettingActivity.this);
        final View dialogView = LayoutInflater.from(SettingActivity.this).
                inflate(R.layout.dialog_input_step, null);
        dialog.setView(dialogView);
        dialog.setCancelable(false);
        final AlertDialog dlg = dialog.show();
        Button ok = (Button) dialogView.findViewById(R.id.bt_dialog_ok);
        Button cancel = (Button) dialogView.findViewById(R.id.bt_dialog_cancel);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_inputStep = (EditText) dialogView.findViewById(R.id.et_dialog_input_step);
                my_input_str = et_inputStep.getText().toString();
                et.setText(checkForSelfDefineStep(my_input_str, et));
                dlg.dismiss();
                setInfoBarText();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                my_input_str = oldString;
                dlg.dismiss();
            }
        });
    }

    private String checkForSelfDefineStep(String input, EditText et) {
        String str = input;

        if (str.length() == 0) {
            str = et.getText().toString();
        } else if (Double.parseDouble(str) < 0.1) {
            str = "0.10";
            Toast.makeText(SettingActivity.this, "步进间距不能小于0.10！", Toast.LENGTH_SHORT).show();
        } else if (Double.parseDouble(str) > 10.0) {
            str = "10.00";
            Toast.makeText(SettingActivity.this, "步进间距不能大于10.00！", Toast.LENGTH_SHORT).show();
        } else {
            str = String.format("%.2f", Double.parseDouble(str));
        }

        return str;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int selectedID = MyPopupWindow.NONE_SELECTED;
        int resourceID = 0;
        int action = event.getAction();
        MyPopupWindow stepSelected = null;
        if (action == MotionEvent.ACTION_UP) {
            switch (v.getId()) {
                case R.id.et_mode:
                    selectedID = MyPopupWindow.MODE_SELECTED;
                    resourceID = R.array.value_mode;
                    stepSelected = new MyPopupWindow(SettingActivity.this, mHandler, resourceID, selectedID);
                    break;
                case R.id.et_width_step_space:
                    if (!isEditable) {
                        break;
                    }
                    selectedID = MyPopupWindow.WIDTH_STEP_SELECTED;
                    resourceID = R.array.value_step;
                    stepSelected = new MyPopupWindow(SettingActivity.this, mHandler, resourceID, selectedID);
                    stepSelected.addItem(setSubWidth());
                    break;
                case R.id.et_length_step_space:
                    if (!isEditable) {
                        break;
                    }
                    selectedID = MyPopupWindow.LENGTH_STEP_SELECTED;
                    resourceID = R.array.value_step;
                    stepSelected = new MyPopupWindow(SettingActivity.this, mHandler, resourceID, selectedID);
                    stepSelected.addItem(setSubLength());
                    break;
                default:
                    selectedID = MyPopupWindow.NONE_SELECTED;
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    break;
            }
            if (selectedID != MyPopupWindow.NONE_SELECTED) {
                stepSelected.show(v);
            }
            return false;
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.et_area_width:
                if (!hasFocus) {
                    checkAreaInput(et_area_width);
                    checkArrayInput(et_array_row);
                    et_select_width_step.setText(setSubWidth());
                    setInfoBarText();
                }
                break;
            case R.id.et_area_length:
                if (!hasFocus) {
                    checkAreaInput(et_area_length);
                    checkArrayInput(et_array_col);
                    et_select_length_step.setText(setSubLength());
                    setInfoBarText();
                }
                break;
            case R.id.et_array_row:
                if (!hasFocus) {
                    checkArrayInput(et_array_row);
                    et_select_width_step.setText(setSubWidth());
                    setInfoBarText();
                }
                break;
            case R.id.et_array_col:
                if (!hasFocus) {
                    checkArrayInput(et_array_col);
                    et_select_length_step.setText(setSubLength());
                    setInfoBarText();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_save:
                //保存设置
                saveSetting(FILE_NAME);
                break;
            case R.id.bt_default:
                //恢复默认设置
                loadSetting(DEFAULT_FILE_NAME);
                saveSetting(FILE_NAME);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            /*隐藏软键盘*/
            InputMethodManager inputMethodManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager.isActive()) {
                inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            }

            setting_view.requestFocus();

        }
        return false;
    }

    private String setSubWidth() {
        double width = Double.parseDouble(et_area_width.getText().toString());
        int row = Integer.parseInt(et_array_row.getText().toString()) - 1;
        String subWidth = String.format("%.2f", width / row);
        return subWidth;
    }

    private String setSubLength() {
        double length = Double.parseDouble(et_area_length.getText().toString());
        int col = Integer.parseInt(et_array_col.getText().toString()) - 1;
        String subLength = String.format("%.2f", length / col);
        return subLength;
    }

    private void loadSetting(String fileName) {
        SharedPreferences pref = getSharedPreferences(fileName, MODE_PRIVATE);

        et_select_mode.setText(pref.getString("et_select_mode", "自动步进"));
        et_select_width_step.setText(pref.getString("et_select_width_step", "1.00"));
        et_select_length_step.setText(pref.getString("et_select_length_step", "1.00"));
        et_array_row.setText(pref.getString("et_array_row", "10"));
        et_array_col.setText(pref.getString("et_array_col", "10"));
        et_area_width.setText(pref.getString("et_area_width", "10.00"));
        et_area_length.setText(pref.getString("et_area_length", "10.00"));

        Log.d("MyAutoCar","loadSetting width_step:"+et_select_width_step.getText().toString());


        controlMode = pref.getInt("controlMode", CONTROL_BY_STEP);
        colorFading = pref.getInt("colorFading", getResources().getColor(R.color.font_gray));
        colorEditable = pref.getInt("colorEditable", getResources().getColor(R.color.font_white));

        isEditable = pref.getBoolean("isEditable", false);
        isSelfDefineWidthStep = pref.getBoolean("isSelfDefineWidthStep", false);
        isSelfDefineLengthStep = pref.getBoolean("isSelfDefineLengthStep", false);

        checkAreaInput(et_area_width);
        checkAreaInput(et_area_length);
        checkArrayInput(et_array_row);
        checkArrayInput(et_array_col);
        setInfoBarText();
        setEditTextState();

    }

    private void saveSetting(String fileName) {

        intentForResult.putExtra("area_width",Float.parseFloat(et_area_width.getText().toString()));
        intentForResult.putExtra("area_length",Float.parseFloat(et_area_length.getText().toString()));
        intentForResult.putExtra("array_row",Integer.parseInt(et_array_row.getText().toString()));
        intentForResult.putExtra("array_col",Integer.parseInt(et_array_col.getText().toString()));
        intentForResult.putExtra("width_step",Float.parseFloat(et_select_width_step.getText().toString()));
        intentForResult.putExtra("length_step",Float.parseFloat(et_select_length_step.getText().toString()));
        intentForResult.putExtra("controlMode", controlMode);
        intentForResult.putExtra("mode_string", et_select_mode.getText().toString());
        setResult(2,intentForResult);

        SharedPreferences.Editor editor = getSharedPreferences(fileName, MODE_PRIVATE).edit();

        editor.putString(fileName, fileName);

        editor.putString("et_select_mode", et_select_mode.getText().toString());
        editor.putString("et_select_width_step", et_select_width_step.getText().toString());
        editor.putString("et_select_length_step", et_select_length_step.getText().toString());
        editor.putString("et_array_row", et_array_row.getText().toString());
        editor.putString("et_array_col", et_array_col.getText().toString());
        editor.putString("et_area_width", et_area_width.getText().toString());
        editor.putString("et_area_length", et_area_length.getText().toString());

        editor.putInt("controlMode", controlMode);
        editor.putInt("colorFading", colorFading);
        editor.putInt("colorEditable", colorEditable);

        editor.putBoolean("isEditable", isEditable);
        editor.putBoolean("isSelfDefineWidthStep", isSelfDefineWidthStep);
        editor.putBoolean("isSelfDefineLengthStep", isSelfDefineLengthStep);

        editor.commit();
    }

    private boolean isFileExist(String fileName) {
        SharedPreferences pref = getSharedPreferences(fileName, MODE_PRIVATE);
        if (pref.getString(fileName, "") == "") {
            return false;
        }
        return true;
    }
}
