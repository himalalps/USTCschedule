package com.edu.ustc.ustcschedule.fragment;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.TimeZoneFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.edu.ustc.ustcschedule.R;
import com.edu.ustc.ustcschedule.SQL.BasicSchedule;
import com.edu.ustc.ustcschedule.SQL.MainDatabaseHelper;
import com.edu.ustc.ustcschedule.SQL.MyDeadLine;
import com.edu.ustc.ustcschedule.SQL.MySchedule;
import com.edu.ustc.ustcschedule.dialogs.DeleteDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DayListFragment extends Fragment {

    final SimpleDateFormat format_day = new SimpleDateFormat("yyyy/MM/dd",Locale.CHINA);
    final SimpleDateFormat format_time = new SimpleDateFormat("HH:mm",Locale.CHINA);
    Date date=new Date();

    Calendar ca=Calendar.getInstance(Locale.CHINA);
    long day_start=((date.getTime()+8*3600*1000)/(86400*1000))*(86400*1000)-8*3600*1000;//清除小时和分钟
    long day_end=day_start+86400*1000;
    double magnify_ratio=2.63;//default
    String day_start_str=Long.toString(day_start);
    String day_end_str=Long.toString(day_end);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ca.setTimeZone(TimeZone.getTimeZone("GMT+8"));



        View view= inflater.inflate(R.layout.fragment_day_list, container, false);
        ConstraintLayout layout=(ConstraintLayout)view.findViewById(R.id.day_list_layout);
        magnify_ratio=(float)(layout.getLayoutParams()).height/1226.0;

        MainDatabaseHelper db_helper=new MainDatabaseHelper(getContext());
        SQLiteDatabase db=db_helper.getReadableDatabase();
        Cursor cursor=db.query("SCHEDULE",new String[]{"_id","IS_FINISH","NAME" ,"START_TIME" ,"END_TIME","TIME_LENGTH",
                "IMPORTANCE" ,"IS_REPEAT" ,"PERIOD" , "PLACE" ,"DESCRIPTION"  } ,
                "IS_REPEAT=0 AND START_TIME>"+day_start_str+" AND START_TIME<"+day_end_str+" AND END_TIME<"+day_end_str+" AND END_TIME>"+day_start_str,
                null,null,null,"START_TIME ASC");
        cursor.moveToFirst();
        for(int i=0;i< cursor.getCount();i++) {
            MySchedule schedule=new MySchedule(cursor);
            add_schedule(layout, schedule, inflater, container);
            cursor.moveToNext();
        }


        Cursor cursor_repeat=db.query("SCHEDULE",new String[]{"_id","IS_FINISH","NAME" ,"START_TIME" ,"END_TIME","TIME_LENGTH",
                        "IMPORTANCE" ,"IS_REPEAT" ,"PERIOD" , "PLACE" ,"DESCRIPTION"  } ,
                "IS_REPEAT=1",
                null,null,null,"START_TIME ASC");
        cursor_repeat.moveToFirst();
        for(int i=0;i< cursor_repeat.getCount();i++) {
            MySchedule schedule=new MySchedule(cursor_repeat);
            boolean is_today=false;
            is_today=is_today_fun(schedule);
            if(is_today)
            {
                add_schedule(layout, schedule, inflater, container);
            }
            cursor_repeat.moveToNext();
        }

        Cursor ddl_cursor=db.query("DDL",new String[]{"_id","IS_FINISH","NAME" ,"START_TIME" ,"WORK_LOAD",
                        "IMPORTANCE" ,"IS_REPEAT" ,"PERIOD" , "PLACE" ,"DESCRIPTION"  } ,
                "IS_REPEAT=0 AND START_TIME>"+day_start_str+" AND START_TIME<"+day_end_str,
                null,null,null,"START_TIME ASC");
        ddl_cursor.moveToFirst();
        for(int i=0;i< ddl_cursor.getCount();i++) {
            MyDeadLine ddl=new MyDeadLine(ddl_cursor);
            add_DDL(layout, ddl, inflater, container);
            ddl_cursor.moveToNext();
        }

        Cursor ddl_cursor_repeat=db.query("DDL",new String[]{"_id","IS_FINISH","NAME" ,"START_TIME" ,"WORK_LOAD",
                        "IMPORTANCE" ,"IS_REPEAT" ,"PERIOD" , "PLACE" ,"DESCRIPTION"  } ,
                "IS_REPEAT=1",
                null,null,null,"START_TIME ASC");
        ddl_cursor_repeat.moveToFirst();
        for(int i=0;i< ddl_cursor_repeat.getCount();i++) {
            MyDeadLine ddl=new MyDeadLine(ddl_cursor_repeat);
            boolean is_today=false;
            is_today=is_today_fun(ddl);
            if(is_today)
            {
                add_DDL(layout, ddl, inflater, container);
            }
            ddl_cursor_repeat.moveToNext();
        }

        return view;
    }
    public void add_schedule(ConstraintLayout layout,MySchedule schedule,LayoutInflater inflater, ViewGroup container)
    {
        View schedule_view=inflater.inflate(R.layout.fragment_day_list_item, container, false);

        long starting_time=schedule.getStartingTime();
        long ending_time=schedule.getEndingTime();

        double height=1.01*(Math.abs(ending_time-starting_time))/72000;
        long day_start_temp=((starting_time+8*3600*1000)/(86400*1000))*(86400*1000)-8*3600*1000;
        double pos=1.01*(Math.min(starting_time,ending_time)-day_start_temp)/72000+6.5;//6是line到layout顶部的高度

        CardView card=(CardView)schedule_view.findViewById(R.id.lesson_card_day);
        card.setOnLongClickListener(new View.OnLongClickListener()
            {
                final int event_id= schedule.getId();
                final String table_name="SCHEDULE";

                @Override
                public boolean onLongClick(View v) {
                    DeleteDialog deleteDialog = new DeleteDialog();
                    deleteDialog.setEvent_id(event_id);
                    deleteDialog.setTable_name(table_name);
                    deleteDialog.show(getActivity().getSupportFragmentManager(), "delete");
                return false;
            }

        });
        ConstraintLayout.LayoutParams card_params = (ConstraintLayout.LayoutParams) card.getLayoutParams();
        ((TextView)card.findViewById(R.id.lesson_text_day)).setText(schedule.getName());
        ((TextView)card.findViewById(R.id.lesson_teacher)).setText(schedule.getDescription());
        ((TextView)card.findViewById(R.id.lesson_place)).setText(schedule.getPlace());
        //String starting_time_str=format_time.format(starting_time);
        //String ending_time_str=format_time.format(ending_time);
        ((TextView)schedule_view.findViewById(R.id.start_time_text)).setText(format_time.format(starting_time));
        ((TextView)schedule_view.findViewById(R.id.end_time_text)).setText(format_time.format(ending_time));

        switch(schedule.getImportance())
        {
            case 1:
            case 2:
                ((ImageView)schedule_view.findViewById(R.id.lesson_label_day)).setBackgroundResource(R.drawable.green_label);
                ((TextView)schedule_view.findViewById(R.id.lesson_text_day)).setTextColor(getResources().getColor(R.color.green_label_text));
                ((TextView)schedule_view.findViewById(R.id.lesson_place)).setBackgroundResource(R.drawable.green_label_small);

                break;

            case 3:
                ((ImageView)schedule_view.findViewById(R.id.lesson_label_day)).setBackgroundResource(R.drawable.yellow_label);
                ((TextView)schedule_view.findViewById(R.id.lesson_text_day)).setTextColor(getResources().getColor(R.color.yellow_label_text));
                ((TextView)schedule_view.findViewById(R.id.lesson_place)).setBackgroundResource(R.drawable.yellow_label_small);
                break;
        }

        //magnify_ratio=(float)card_params.height/100.0;
        card_params.height=(int)(magnify_ratio*height);//放大倍数乘值
        card_params.topMargin=(int)(magnify_ratio*pos);

        card.setLayoutParams(card_params);
        //schedule_view.layout(0,100,schedule_view.getRight()-schedule_view.getLeft(),170);
        schedule_view.setTag(R.id.Tag_id,schedule.getId());

        layout.addView(schedule_view);
        //schedule_view.setLayoutParams(card_params);
        //return view;
    }


    public void add_DDL(ConstraintLayout layout,MyDeadLine ddl,LayoutInflater inflater, ViewGroup container) {
        View ddl_view=inflater.inflate(R.layout.fragment_day_list_item_ddl, container, false);

        long starting_time=ddl.getStartingTime();
        long day_start_temp=((starting_time+8*3600*1000)/(86400*1000))*(86400*1000)-8*3600*1000;
        double pos=1.01*(starting_time-day_start_temp)/72000+6.5;//6是line到layout顶部的高度


        ConstraintLayout.LayoutParams ddl_params = (ConstraintLayout.LayoutParams) ddl_view.findViewById(R.id.day_deadline).getLayoutParams();
        //FrameLayout.LayoutParams ddl_params = (FrameLayout.LayoutParams) ddl_view.getLayoutParams();
        TextView ddl_text=((TextView)ddl_view.findViewById(R.id.day_deadline_label));
        ddl_text.setText(format_time.format(starting_time));
        ddl_text.setOnLongClickListener(new View.OnLongClickListener()
        {
            final int event_id= ddl.getId();
            final String table_name="DDL";

            @Override
            public boolean onLongClick(View v) {
                DeleteDialog deleteDialog = new DeleteDialog();
                deleteDialog.setEvent_id(event_id);
                deleteDialog.setTable_name(table_name);
                deleteDialog.show(getActivity().getSupportFragmentManager(), "delete");


                return false;
            }
        });

        ddl_params.topMargin=(int)(magnify_ratio*pos);
        ddl_view.findViewById(R.id.day_deadline).setLayoutParams(ddl_params);
        //ddl_view.layout(ddl_view.getLeft(),100,ddl_view.getRight(),170);
        layout.addView(ddl_view);
    }
    public boolean is_today_fun(BasicSchedule schedule)
    {
        boolean is_today=false;
        long starting_time=schedule.getStartingTime();
        Calendar temp_ca=Calendar.getInstance(Locale.CHINA);
        temp_ca.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        temp_ca.setTimeInMillis(starting_time);
        if(schedule.getPeriod()==1)
            is_today=true;
        if(schedule.getPeriod()==7&&(ca.get(Calendar.DAY_OF_WEEK) ==temp_ca.get(Calendar.DAY_OF_WEEK)))
            is_today=true;
        if(schedule.getPeriod()==30)
        {
            if(ca.get(Calendar.DAY_OF_MONTH) ==temp_ca.get(Calendar.DAY_OF_MONTH))
                is_today = true;
            if(ca.getActualMaximum(Calendar.DAY_OF_MONTH) <temp_ca.get(Calendar.DAY_OF_MONTH))//超过一个月最大天数
            {
                if(ca.getActualMaximum(Calendar.DAY_OF_MONTH)==ca.get(Calendar.DAY_OF_MONTH))
                    is_today=true;
            }
        }
        if(schedule.getPeriod()==365)
        {
            if((ca.get(Calendar.DAY_OF_MONTH) ==temp_ca.get(Calendar.DAY_OF_MONTH))&&
                    (ca.get(Calendar.MONTH)==temp_ca.get(Calendar.MONTH)))
            {
                is_today=true;
            }
            if(ca.getActualMaximum(Calendar.DAY_OF_MONTH) <temp_ca.get(Calendar.DAY_OF_MONTH)&&
                    (ca.get(Calendar.MONTH)==temp_ca.get(Calendar.MONTH)))//2月29日
            {
                is_today=true;
            }
        }
        return is_today;
    }
}
