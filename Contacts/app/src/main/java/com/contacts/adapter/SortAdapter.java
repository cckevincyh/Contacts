package com.contacts.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.contacts.domain.Contacts;
import com.contacts.main.R;


/**
 * 
 * @author Mr.Z
 */
public class SortAdapter extends BaseAdapter implements SectionIndexer {
	private List<Contacts>	contactsList	= null;
	private Context	mContext;

	public SortAdapter(Context mContext, List<Contacts> contactsList) {
		this.mContext = mContext;
		this.contactsList = contactsList;
	}

	/**
	 * 当ListView数据发生变化时,调用此方法来更新ListView
	 * 
	 * @param contactsList
	 */
	public void updateListView(List<Contacts> contactsList) {
		this.contactsList = contactsList;
		notifyDataSetChanged();
	}

	public int getCount() {
		return this.contactsList.size();
	}

	public Object getItem(int position) {
		return contactsList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View view, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		final Contacts mContent = contactsList.get(position);
		if(view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(R.layout.contact_item, null);
			viewHolder.tvPhone = (TextView) view.findViewById(R.id.phone);
			viewHolder.tvName = (TextView) view.findViewById(R.id.name);
			viewHolder.sort_key = (TextView) view.findViewById(R.id.sort_key);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		// 根据position获取分类的首字母的Char ascii值
		int section = getSectionForPosition(position);

		// 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
		if(position == getPositionForSection(section)) {
			viewHolder.sort_key.setVisibility(View.VISIBLE);
			viewHolder.sort_key.setText(mContent.getSortKey());
		} else {
			viewHolder.sort_key.setVisibility(View.GONE);
		}



		viewHolder.tvPhone.setText(this.contactsList.get(position).getPhone());
		viewHolder.tvName.setText(this.contactsList.get(position).getName());
		// viewHolder.tvTitle.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//下划线

		return view;

	}

	final static class ViewHolder {
		TextView	tvName;
		TextView	tvPhone;
		TextView sort_key;
	}

	/**
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	public int getSectionForPosition(int position) {
		return contactsList.get(position).getSortKey().charAt(0);
	}

	/**
	 * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
	 */
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = contactsList.get(i).getSortKey();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if(firstChar == section) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * 提取英文的首字母，非英文字母用#代替。
	 * 
	 * @param str
	 * @return
	 */
	private String getAlpha(String str) {
		String sortStr = str.trim().substring(0, 1).toUpperCase();
		// 正则表达式，判断首字母是否是英文字母
		if(sortStr.matches("[A-Z]")) {
			return sortStr;
		} else {
			return "#";
		}
	}

	@Override
	public Object[] getSections() {
		return null;
	}
}
