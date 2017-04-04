package com.contacts.main;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;

import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.contacts.adapter.ContactAdapter;
import com.contacts.domain.Contacts;
import com.contacts.main.SideBar.OnTouchingLetterChangedListener;
import java.util.ArrayList;

import java.util.List;



public class MainActivity extends Activity {

    /**
     * 分组的布局
     */
    private LinearLayout titleLayout;

    /**
     * 分组上显示的字母
     */
    private TextView title;

    /**
     * 联系人ListView
     */
    private  ListView contactsListView;

    /**
     * 联系人列表适配器
     */
    private  ContactAdapter adapter;

    /**
     * 用于进行字母表分组
     */
    private AlphabetIndexer indexer;

    /**
     * 存储所有手机中的联系人
     */
    private  List<Contacts> contacts = new ArrayList<Contacts>();

    /**
     * 定义字母表的排序规则
     */
    private String alphabet = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 上次第一个可见元素，用于滚动时记录标识。
     */
    private int lastFirstVisibleItem = -1;


    /**
     * 侧边滑动栏
     */
    private SideBar	sideBar;

    /**
     * 弹出的字母
     */
    private TextView dialog;


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            contacts.clear();//清空数据
            initData();//加载联系人数据
        }
    };




    @Override
    protected void onRestart() {
        super.onRestart();//防止报错 android.app.SuperNotCalledException: Activity
        contacts.clear();//清空数据
        initData();//加载联系人数据
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adapter = new ContactAdapter(this, R.layout.contact_item, contacts);
        titleLayout = (LinearLayout) findViewById(R.id.title_layout);
        title = (TextView) findViewById(R.id.title);
        contactsListView = (ListView) findViewById(R.id.contacts_list_view);
        initBar();//侧边字母栏
        initData();//加载联系人数据

        contactsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                return false;
            }
        });

        // 添加长按点击弹出选择菜单
        contactsListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.setHeaderTitle("选择操作");
                menu.add(0, 1, 0, "修改该联系人");
                menu.add(0, 2, 0, "删除该联系人");
            }
        });




    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //info.id得到listview中选择的条目绑定的id
        String id = String.valueOf(info.id);//得到选中的索引
        Contacts contact = this.contacts.get(Integer.parseInt(id));//得到选中的Contacts对象
      //  Log.e("contact",contact.toString());

        switch (item.getItemId()) {
            case 1:

                Toast.makeText(this, "修改",Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(this, "删除", Toast.LENGTH_SHORT).show();
                /**
                 * Delete

                     核心思想：
                     (1)先在raw_contacts表根据姓名(此处的姓名为name记录的data2的数据而不是data1的数据)查出id；
                     (2)在data表中只要raw_contact_id匹配的都删除；
                 */
                delete(contact);//删除联系人
                //通知更新
                handler.sendEmptyMessage(1);
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }


    public void initData(){
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;//查询手机里所有的联系人



        //ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME 姓名
        //ContactsContract.CommonDataKinds.Phone.NUMBER 电话
        Cursor cursor = getContentResolver().query(uri,
                new String[] { ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER,"sort_key" }, null, null, "sort_key");
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                String phone = cursor.getString(1);
                String sortKey = getSortKey(cursor.getString(2));
                Contacts contact = new Contacts();
                contact.setName(name);
                contact.setPhone(phone);
                contact.setSortKey(sortKey);
                contacts.add(contact);
            } while (cursor.moveToNext());
        }
        startManagingCursor(cursor);
        indexer = new AlphabetIndexer(cursor, 2, alphabet);
        adapter.setIndexer(indexer);
        if (contacts.size() > 0) {
            setupContactsListView();
        }
    }



    /**
     * 为联系人ListView设置监听事件，根据当前的滑动状态来改变分组的显示位置，从而实现挤压动画的效果。
     */
    private void setupContactsListView() {
        contactsListView.setAdapter(adapter);
        contactsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                int section = indexer.getSectionForPosition(firstVisibleItem);
                int nextSecPosition = indexer.getPositionForSection(section + 1);
                if (firstVisibleItem != lastFirstVisibleItem) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) titleLayout.getLayoutParams();
                    params.topMargin = 0;
                    titleLayout.setLayoutParams(params);
                    title.setText(String.valueOf(alphabet.charAt(section)));
                }
                if (nextSecPosition == firstVisibleItem + 1) {
                    View childView = view.getChildAt(0);
                    if (childView != null) {
                        int titleHeight = titleLayout.getHeight();
                        int bottom = childView.getBottom();
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) titleLayout
                                .getLayoutParams();
                        if (bottom < titleHeight) {
                            float pushedDistance = bottom - titleHeight;
                            params.topMargin = (int) pushedDistance;
                            titleLayout.setLayoutParams(params);
                        } else {
                            if (params.topMargin != 0) {
                                params.topMargin = 0;
                                titleLayout.setLayoutParams(params);
                            }
                        }
                    }
                }
                lastFirstVisibleItem = firstVisibleItem;
            }
        });

    }




    /**
     * 获取sort key的首个字符，如果是英文字母就直接返回，否则返回#。
     *
     * @param sortKeyString
     *            数据库中读取出的sort key
     * @return 英文字母或者#
     */
    private String getSortKey(String sortKeyString) {
        String key = sortKeyString.substring(0, 1).toUpperCase();
        if (key.matches("[A-Z]")) {
            return key;
        }
        return "#";
    }


    private void initBar() {

        sideBar = (SideBar) findViewById(R.id.sidrbar);
        dialog = (TextView) findViewById(R.id.dialog);
        sideBar.setTextView(dialog);

        // 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {


                int position = alphabet.indexOf(s);//s为侧边栏点击中的字母，得出s在整个字母位子中的索引

                contactsListView.setSelection(position);//设置ListView的位置

            }
        });


    }

        //重写onCreateOptionMenu(Menu menu)方法，当菜单第一次被加载时调用
         @Override
        public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
            //填充选项菜单（读取XML文件、解析、加载到Menu组件上）
             getMenuInflater().inflate(R.menu.main, menu);
             return true;
         }

        //重写OptionsItemSelected(MenuItem item)来响应菜单项(MenuItem)的点击事件（根据id来区分是哪个item）
       @Override
       public boolean onOptionsItemSelected(MenuItem item) {

           switch (item.getItemId()) {
               case R.id.add:
                   Toast.makeText(this, "添加联系人", Toast.LENGTH_SHORT).show();
                   //添加新联系人
                   Intent intent = new Intent(this,AddContactsActivity.class);
                   this.startActivity(intent);
                   break;
               case R.id.group:
                   Toast.makeText(this, "查看分组", Toast.LENGTH_SHORT).show();
                   break;

               default:
                   break;
           }
           return super.onOptionsItemSelected(item);
       }


    /**
     * 删除联系人
     * @param contact
     * @throws Exception
     */
    public void delete(Contacts contact){
        //根据姓名求id
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");


        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Contacts.Data._ID},"display_name=?", new String[]{contact.getName()}, null);
        if(cursor.moveToFirst()){
            int id = cursor.getInt(0);
            //根据id删除data中的相应数据
            resolver.delete(uri, "display_name=?", new String[]{contact.getName()});
            uri = Uri.parse("content://com.android.contacts/data");
            resolver.delete(uri, "raw_contact_id=?", new String[]{id+""});
        }
    }





}
