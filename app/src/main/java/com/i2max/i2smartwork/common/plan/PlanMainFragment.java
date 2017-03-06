package com.i2max.i2smartwork.common.plan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.i2max.i2smartwork.MainActivity;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.EventDecorator;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DateCalendarUtil;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.PreferenceUtil;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateChangedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class PlanMainFragment extends Fragment  {
    static String TAG = PlanMainFragment.class.getSimpleName();

    private AppCompatActivity acActivity;

    private MaterialCalendarView calendarView;

    private int mMonthTerm;
    private List<JSONObject> mEventList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=  inflater.inflate(R.layout.fragment_plan_main, container, false);

        acActivity = (AppCompatActivity)getActivity();
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        acActivity.setSupportActionBar(toolbar);

        ((MainActivity) acActivity).setVisibleFabButton(true);

        final ActionBar ab = acActivity.getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.plan);

        calendarView = (MaterialCalendarView) v.findViewById(R.id.calendarView);
        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView materialCalendarView, CalendarDay calendarDay) {
                Calendar curCal = Calendar.getInstance();
                int term = DateCalendarUtil.getMonthBetweenCalendar(curCal, calendarDay.getCalendar());
                loadListSnsPlan(term);
            }
        });
        calendarView.setOnDateChangedListener(new OnDateChangedListener() {
            @Override
            public void onDateChanged(MaterialCalendarView materialCalendarView, CalendarDay calendarDay) {
                dateChanged(calendarDay);
            }
        });

        mMonthTerm = 0;

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        loadListSnsPlan(mMonthTerm);
    }

    public void addCalandarEvent() {
        if (mEventList.size() > 0) {
            Log.d(TAG, "before mEventList.size() = " + mEventList.size());
            List<CalendarDay> eventDates = new ArrayList<>();
            for (int i=mEventList.size()-1; i>=0; i--) {
                Calendar cal = null;
                try {
                    String startDttm = mEventList.get(i).getString("start_dttm");
                    if (startDttm.length() == 12) {
                        cal = DateCalendarUtil.getCalenderFromYYYYMMDDHH(startDttm);
                    } else {
                        cal = DateCalendarUtil.getCalenderFromYYYYMMDDHHSS(startDttm);
                    }

                    //Log.d(TAG, calendarView.getCurrentDate().getYear() + " / " + calendarView.getCurrentDate().getMonth() + " / cal = " + cal.get(Calendar.YEAR) + " / " + cal.get(Calendar.MONTH) + " / " + cal.get(Calendar.DAY_OF_MONTH));
                    if (calendarView.getCurrentDate().getYear()!=cal.get(Calendar.YEAR) || calendarView.getCurrentDate().getMonth()!=cal.get(Calendar.MONTH)) {
                        mEventList.remove(i);
                    } else {
                        CalendarDay day = CalendarDay.from(cal);
                        eventDates.add(day);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            calendarView.addDecorator(new EventDecorator(getResources().getColor(R.color.colorPrimary), eventDates));
            Log.d(TAG, "after mEventList.size() = " + mEventList.size());
        }
    }

    public void dateChanged(CalendarDay calendarDay) {
        for (int i=0; i<mEventList.size(); i++) {
            try {
                Calendar cal = null;
                String startDttm = mEventList.get(i).getString("start_dttm");
                if (startDttm.length() == 12) {
                    cal = DateCalendarUtil.getCalenderFromYYYYMMDDHH(startDttm);
                } else {
                    cal = DateCalendarUtil.getCalenderFromYYYYMMDDHHSS(startDttm);
                }
                if (calendarDay.getYear()==cal.get(Calendar.YEAR) && calendarDay.getMonth()==cal.get(Calendar.MONTH) && calendarDay.getDay()==cal.get(Calendar.DAY_OF_MONTH)) {
                    // 일정 선택
                    Log.d(TAG, calendarDay.getDay() + " / " + cal.get(Calendar.DAY_OF_MONTH) + " dttm = " + startDttm);
                    Intent planIntent = new Intent(getActivity(), PlanListActivity.class);
                    planIntent.putExtra(CodeConstant.TAR_OBJ_TP, CodeConstant.TYPE_USER);
                    planIntent.putExtra(PlanListActivity.MONTH_TERM, mMonthTerm);
                    planIntent.putExtra(PlanListActivity.PLAN_ID, mEventList.get(i).getString("plan_id"));
                    planIntent.putExtra(PlanListActivity.PLAN_DATE, startDttm.substring(0,12));
                    getActivity().startActivity(planIntent);

                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadListSnsPlan(int term) {

        mMonthTerm = term;

        String monthTerm = String.format("%d", term);
        Log.d(TAG, "monthTerm = " + monthTerm);

        I2ConnectApi.requestJSON(getActivity(), I2UrlHelper.Plan.getListSnsPlan(CodeConstant.TYPE_USER, PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID), monthTerm))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsPlan onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsPlan onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(getActivity(), e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsPlan onNext");
                        JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                        List<JSONObject> statusInfoList = I2ResponseParser.getJsonArrayAsList(statusInfo, "list_data");
                        if (I2ResponseParser.checkReponseStatus(jsonObject) && statusInfoList != null) {
                            if(statusInfoList.size() <= 0) {
                                Toast.makeText(getActivity(), getString(R.string.no_plan_data_available), Toast.LENGTH_SHORT).show();
                            }
                            mEventList.clear();
                            mEventList.addAll(statusInfoList);
                            addCalandarEvent();
                        } else {
                            Toast.makeText(getActivity(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
