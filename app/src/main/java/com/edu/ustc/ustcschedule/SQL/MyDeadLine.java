package com.edu.ustc.ustcschedule.SQL;

import android.content.ContentValues;
import android.database.Cursor;

import java.sql.Time;
import java.util.Date;

public class MyDeadLine extends BasicSchedule{

    private long WorkLoad;

    //默认不添加备注,创建时必须填写编号,时间和内容
    public MyDeadLine (String name,long starting_time,int importance,int is_repeat,int period,String place,String description,long work_load){
        super(name,starting_time,importance,is_repeat,period,place,description);
        this.WorkLoad=work_load;


    }

    public ContentValues getContentValues(){
        ContentValues info=super.getContentValues();
        info.put("WORK_LOAD",WorkLoad);

        return info;
    }

    public void setFromCursor(Cursor cursor)
    {
        super.setFromCursor(cursor);

        WorkLoad=cursor.getInt(cursor.getColumnIndexOrThrow("WORK_LOAD"));
    }


}