package com.contacts.adapter;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.contacts.domain.Contacts;
import com.contacts.main.R;

import java.util.List;

/**
 * Created by c on 2017/3/15.
 */

public class ContactAdapter  extends ArrayAdapter<Contacts> {

    /**
     * 需要渲染的item布局文件
     */
    private int resource;

    /**
     * 字母表分组工具
     */
    private SectionIndexer mIndexer;

    public ContactAdapter(Context context, int textViewResourceId, List<Contacts> objects) {
        super(context, textViewResourceId, objects);
        resource = textViewResourceId;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Contacts contact = getItem(position);
        LinearLayout layout = null;
        if (convertView == null) {
            layout = (LinearLayout) LayoutInflater.from(getContext()).inflate(resource, null);
        } else {
            layout = (LinearLayout) convertView;
        }
        TextView name = (TextView) layout.findViewById(R.id.name);  //获取姓名的TextView
        TextView phone = (TextView) layout.findViewById(R.id.phone);//获取电话的TextView
        LinearLayout sortKeyLayout = (LinearLayout) layout.findViewById(R.id.sort_key_layout);
        TextView sortKey = (TextView) layout.findViewById(R.id.sort_key);
        name.setText(contact.getName());//设置姓名TextView的内容
        phone.setText(contact.getPhone());//设置电话TextView的内容
        int section = mIndexer.getSectionForPosition(position);
        if (position == mIndexer.getPositionForSection(section)) {
            sortKey.setText(contact.getSortKey());
            sortKeyLayout.setVisibility(View.VISIBLE);
        } else {
            sortKeyLayout.setVisibility(View.GONE);
        }
        return layout;
    }

    /**
     * 给当前适配器传入一个分组工具。
     *
     * @param indexer
     */
    public void setIndexer(SectionIndexer indexer) {
        mIndexer = indexer;
    }
}
