package com.example.test.popupwindow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.test.myautocar.R;

/**
 * Created by hefvcjm on 17-3-24.
 */

public class MyPopupWindow extends PopupWindow {

    Context mContext;
    View contentView;
    ListView mListView;
    String[] item;
    MyAdapter myAdapter;
    Handler mHandler;
    int mSelectedID;
    int controlMode;//控制模式，步进或者自动

    //标记设置项目
    public static final int NONE_SELECTED = -1;
    public static final int MODE_SELECTED = 0;
    public static final int WIDTH_STEP_SELECTED = 1;
    public static final int LENGTH_STEP_SELECTED = 2;

    /**
     * @param context    上下文
     * @param handler    更新UI的Handler
     * @param resource   字符串数组资源ID
     * @param selectedID 标记设置项目
     */
    public MyPopupWindow(Context context, Handler handler, int resource, int selectedID) {

        mContext = context;
        contentView = LayoutInflater.from(mContext).inflate(R.layout.popupwindow, null);
        mListView = (ListView) contentView.findViewById(R.id.pop_list);
        item = mContext.getResources().getStringArray(resource);
        myAdapter = new MyAdapter(mContext, R.layout.layout_listview_item, item);
        mHandler = handler;
        mSelectedID = selectedID;

    }

    public void show(View v) {

        this.setContentView(contentView);
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), (Bitmap) null));
        this.setOutsideTouchable(true);

        mListView.setAdapter(myAdapter);
        this.showAsDropDown(v, v.getWidth() / 2, 0);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String select_item = item[position];
                Message msg = Message.obtain();
                msg.what = mSelectedID;
                msg.obj = select_item;
                mHandler.sendMessage(msg);
                Log.d("TAG_MODE", select_item);
                dismiss();
            }
        });

    }

    public void addItem(String str) {
        for(String s:item){
            if(s.equals(str)){
                return ;
            }
        }
        item[0] = str;
    }


    private class MyAdapter extends ArrayAdapter<String> {

        int mResource;

        public MyAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull String[] objects) {
            super(context, resource, objects);
            mResource = resource;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            String str = getItem(position);
            View view = LayoutInflater.from(getContext()).inflate(mResource, null);
            TextView tv = (TextView) view.findViewById(R.id.pop_list_item);
            tv.setText(str);
            return view;
        }
    }

}
