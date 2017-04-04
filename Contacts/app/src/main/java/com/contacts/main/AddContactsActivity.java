package com.contacts.main;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class AddContactsActivity extends AppCompatActivity {

    private EditText contactName;
    private EditText contactPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);
        initView();//初始化控件
    }



    public void initView(){
        contactName = (EditText) findViewById(R.id.contactName);//输入联系人的EditText
        contactPhone = (EditText) findViewById(R.id.contactPhone);//输入联系号码的EditText


    }


    public void addContact(View v){
        //先校验输入的数据
        String name = contactName.getText().toString();
        String phone = contactPhone.getText().toString();
        if("".equals(name.trim())){
            Toast.makeText(this,"输入的联系人姓名不能为空",Toast.LENGTH_SHORT).show();
        }else if("".equals(phone.trim())){
            Toast.makeText(this,"输入的联系号码不能为空",Toast.LENGTH_SHORT).show();
        }else {

            ContentResolver cr = getContentResolver();
            //先查询raw_contacts表，获取最新联系人的主键，然后主键+1，就是要插入的联系人的id
            Cursor cursorContactId = cr.query(Uri.parse("content://com.android.contacts/raw_contacts"), new String[]{"_id"}, null, null, null);
            //默认联系人id就是1
            int contact_id = 1;
            if (cursorContactId.moveToLast()) {
                //拿到主键
                int _id = cursorContactId.getInt(0);
                //主键+1，就是要插入的联系人id
                contact_id = ++_id;
            }
            ContentValues values = new ContentValues();
            values.put("contact_id", contact_id);
            //把联系人id插入raw_contacts数据库
            cr.insert(Uri.parse("content://com.android.contacts/raw_contacts"), values);
            //插入姓名
            values.clear();
            values.put("data1", name.trim());
            values.put("mimetype", "vnd.android.cursor.item/name");
            values.put("raw_contact_id", contact_id);
            cr.insert(Uri.parse("content://com.android.contacts/data"), values);

            //插入电话
            values.clear();
            values.put("data1", phone.trim());
            values.put("mimetype", "vnd.android.cursor.item/phone_v2");
            values.put("raw_contact_id", contact_id);
            cr.insert(Uri.parse("content://com.android.contacts/data"), values);
            Toast.makeText(this,"添加成功",Toast.LENGTH_SHORT).show();
            finish();
        }

    }
}
