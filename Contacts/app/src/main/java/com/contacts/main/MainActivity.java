package com.contacts.main;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.contacts.adapter.SortAdapter;
import com.contacts.domain.Contacts;
import com.contacts.handle.CharacterParser;
import com.contacts.handle.PinyinComparator;
import com.contacts.main.SideBar.OnTouchingLetterChangedListener;
import java.util.ArrayList;

import java.util.Collections;
import java.util.List;

import static android.R.attr.editable;
import static android.R.attr.type;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.contacts.main.R.id.sort_key;


public class MainActivity extends Activity {



    private Context context = this;

    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser characterParser;
    private List<Contacts>	SourceDateList;

    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;

    /**
     * 联系人列表适配器
     */
    private SortAdapter adapter;


    /**
     * 分组上显示的字母
     */
    private TextView title;

    /**
     * 联系人ListView
     */
    private  ListView contactsListView;




    /**
     * 存储所有手机中的联系人
     */
    private  List<Contacts> contacts = new ArrayList<Contacts>();



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
            initViews(0);
        }
    };




    @Override
    protected void onRestart() {
        super.onRestart();//防止报错 android.app.SuperNotCalledException: Activity
        contacts.clear();//清空数据
        initViews(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews(0);
        initSearchView();
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







    /**
     * 为ListView填充数据
     *
     * @param contacts
     * @return
     */
    private List<Contacts> filledData(List<Contacts> contacts) {
        List<Contacts> mSortList = new ArrayList<Contacts>();

        for (int i = 0; i < contacts.size(); i++) {
            Contacts sortModel = new Contacts();
            sortModel.setName(contacts.get(i).getName());
            sortModel.setPhone(contacts.get(i).getPhone());
            // 汉字转换成拼音
            String pinyin = characterParser.getSelling(contacts.get(i).getName());
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if(sortString.matches("[A-Z]")) {
                sortModel.setSortKey(sortString.toUpperCase());
            } else {
                sortModel.setSortKey("#");
            }

            mSortList.add(sortModel);
        }
        return mSortList;

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


                //携带数据跳转到修改联系人界面
                Intent intent = new Intent(this,UpdateContactsActivity.class);
                intent.putExtra("name", contact.getName());
                intent.putExtra("phone",contact.getPhone());
                startActivity(intent);

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


    public List<Contacts> initData(){
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
        return contacts;
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



    private void initViews(int chooseType) {
        // 实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();

        pinyinComparator = new PinyinComparator();

        sideBar = (SideBar) findViewById(R.id.sidrbar);
        dialog = (TextView) findViewById(R.id.dialog);
        sideBar.setTextView(dialog);

        // 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if(position != -1) {
                    contactsListView.setSelection(position);
                }

            }
        });

        contactsListView = (ListView) findViewById(R.id.contacts_list_view);
        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 这里要利用adapter.getItem(position)来获取当前position所对应的对象
                Toast.makeText(context, ((Contacts) adapter.getItem(position)).getName(), Toast.LENGTH_SHORT).show();
            }
        });

        if(chooseType==0) {
            //获取联系人数据
            SourceDateList = initData();
        }else{
            SourceDateList = contacts;
        }


        // 根据a-z进行排序源数据
        Collections.sort(SourceDateList, pinyinComparator);
        adapter = new SortAdapter(context, SourceDateList);
        contactsListView.setAdapter(adapter);
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




    public void initSearchView(){
        final EditText edit_search = (EditText) findViewById(R.id.et_search);
        edit_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void afterTextChanged(Editable editable) {

                String content = edit_search.getText().toString();
                if(!content.trim().equals("")) {
                    //内容改变的时候使用此方法
                    Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;//查询手机里所有的联系人


                    Cursor cursor = getContentResolver().query(uri,
                            new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, "sort_key"}, "display_name like '%"+content+"%' or "+
                                    ContactsContract.CommonDataKinds.Phone.NUMBER+" like'%"+content+"%'",null, "sort_key");
                    Log.e("12121",content+"!!");

                    Log.e("12121",cursor.getCount()+"!!");

                    query(cursor);
                    initViews(1);
                }else{
                    contacts.clear();//清除一遍数据
                    initViews(0);
                }
            }
        });
    }



    public List<Contacts> query(Cursor cursor){
        //先清除数据
        contacts.clear();
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
        return contacts;
    }



}
