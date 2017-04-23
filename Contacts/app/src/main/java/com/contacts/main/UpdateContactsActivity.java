package com.contacts.main;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

public class UpdateContactsActivity extends AppCompatActivity {

    private EditText editName;
    private EditText editPhone;
    private String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_contacts);
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        String phone = intent.getStringExtra("phone");
        editName = (EditText) findViewById(R.id.update_contactName);
        editPhone = (EditText) findViewById(R.id.update_contactPhone);
        editName.setText(name);
       editPhone.setText(phone);

    }


    public void updateContact(View v){
        //进行修改联系人的操作
        /**
         * Update
         核心思想：
         (1)查出名字所对应的id

         (2)不需要更新raw_contacts，只需要更新data表；

         (3)uri=content://com.android.contacts/data 表示对data表进行操作；
         */

        //根据姓名求id
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");


        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Contacts.Data._ID},"display_name=?", new String[]{name}, null);
        //获取当前的联系人名称
        String  updateName = editName.getText().toString();
        String phone = editPhone.getText().toString();
        if(cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            Uri uri2 = Uri.parse("content://com.android.contacts/data");//对data表的所有数据操作
            ContentValues values = new ContentValues();
            values.put("data1", phone);
             resolver.update(uri2, values, "mimetype=? and raw_contact_id=?", new String[]{"vnd.android.cursor.item/phone_v2",id+""});
            values.clear();
            values.put("data1", updateName);
             resolver.update(uri2, values, "mimetype=? and raw_contact_id=?", new String[]{"vnd.android.cursor.item/name",id+""});

            Toast.makeText(this,"修改成功",Toast.LENGTH_SHORT).show();
            finish();
        }


    }
}
