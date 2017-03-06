package com.i2max.i2smartwork.common.plan;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.datetimepicker.date.DatePickerDialog;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DateCalendarUtil;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.PreferenceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PlanCreateActivity extends BaseAppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    protected static String TAG = PlanCreateActivity.class.getSimpleName();

    public static final String PLAN_ID = "plan_id";
    public static final String MODE = "mode";

    public static final int MODE_CREATE = 0;
    public static final int MODE_MODIFY = 1;

    protected int mMode;

    protected String mTarPlanID, mTarObjTp, mTarObjId, mTarObjTtl;
    protected EditText etTitle, etPlace, etDescript;
    protected Button btnStartDate, btnStartTime, btnEndDate, btnEndTime, btnAllDay, btnOpenYN;
    protected TextView tvTarNm;
    protected Spinner spGroup;
    protected List<JSONObject> mGroupArray = new ArrayList<>();
    protected int groupPage, selectedGroupPos;
    protected boolean isAllDay, isOpen;

    protected String seletedTimePicker;
    protected static final String TIME_PATTERN = "HH:mm";
    protected Calendar startCal, endCal;
    protected DateFormat dateFormat;
    protected SimpleDateFormat timeFormat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_create);

        Intent intent = getIntent();

        mMode = intent.getIntExtra(MODE, MODE_CREATE);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etTitle = (EditText) findViewById(R.id.et_title);
        etPlace = (EditText) findViewById(R.id.et_place);
        etDescript = (EditText) findViewById(R.id.et_descript);

        btnStartDate = (Button) findViewById(R.id.btn_start_date);
        btnStartTime = (Button) findViewById(R.id.btn_start_time);
        btnEndDate = (Button) findViewById(R.id.btn_end_date);
        btnEndTime = (Button) findViewById(R.id.btn_end_time);
        btnAllDay = (Button) findViewById(R.id.btn_all_day);
        btnOpenYN = (Button) findViewById(R.id.btn_open_yn);
        spGroup = (Spinner) findViewById(R.id.sp_group);
        tvTarNm = (TextView) findViewById(R.id.tv_tar_nm);

        isAllDay = false;
        isOpen = false;

        initBtns();

        if (mMode == MODE_CREATE) {
            spGroup.setVisibility(View.VISIBLE);
            tvTarNm.setVisibility(View.GONE);
            getSupportActionBar().setTitle("일정추가");
            mTarPlanID = "";
        } else {
            spGroup.setVisibility(View.GONE);
            tvTarNm.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("일정편집");
            mTarPlanID = intent.getStringExtra(PLAN_ID);
            loadPlanDetail();
        }

        selectedGroupPos = 0;
        groupPage = 1;
        loadGroups(groupPage);
    }

    public void initBtns() {
        startCal = Calendar.getInstance();
        endCal = Calendar.getInstance();
        dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        timeFormat = new SimpleDateFormat(TIME_PATTERN, Locale.getDefault());
        btnStartDate.setText(dateFormat.format(startCal.getTime()));
        btnStartTime.setText(timeFormat.format(startCal.getTime()));
        btnEndDate.setText(dateFormat.format(endCal.getTime()));
        btnEndTime.setText(timeFormat.format(endCal.getTime()));

        btnStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.newInstance(PlanCreateActivity.this, startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DAY_OF_MONTH)).show(getFragmentManager(), "start_date");
            }
        });
        btnStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seletedTimePicker = "start_time"; // tag에 저장이 안되서 따로 변수 만듬
                TimePickerDialog.newInstance(PlanCreateActivity.this, startCal.get(Calendar.HOUR_OF_DAY), startCal.get(Calendar.MINUTE), true).show(getFragmentManager(), "start_time");
            }
        });
        btnEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.newInstance(PlanCreateActivity.this, endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH), endCal.get(Calendar.DAY_OF_MONTH)).show(getFragmentManager(), "end_date");
            }
        });
        btnEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seletedTimePicker = "end_time"; // tag에 저장이 안되서 따로 변수 만듬
                TimePickerDialog.newInstance(PlanCreateActivity.this, endCal.get(Calendar.HOUR_OF_DAY), endCal.get(Calendar.MINUTE), true).show(getFragmentManager(), "end_time");
            }
        });
        btnAllDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAllDay = !isAllDay;
                toggleAllDay(isAllDay);
            }
        });
        btnOpenYN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isOpen = !isOpen;
                toggleOpenYN(isOpen);
            }
        });
        Button btnSave = (Button) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSnsPlan();
            }
        });
    }

    public void toggleAllDay(boolean allDay) {
        if (allDay) {
            btnAllDay.setBackground(ContextCompat.getDrawable(PlanCreateActivity.this, R.drawable.bg_check_on));
            btnAllDay.setTextColor(getResources().getColor(R.color.text_color_white));
        } else {
            btnAllDay.setBackground(ContextCompat.getDrawable(PlanCreateActivity.this, R.drawable.bg_check_off));
            btnAllDay.setTextColor(getResources().getColor(R.color.text_color_black));
        }
    }

    public void toggleOpenYN(boolean open) {
        if (open) {
            btnOpenYN.setBackground(ContextCompat.getDrawable(PlanCreateActivity.this, R.drawable.bg_check_on));
            btnOpenYN.setTextColor(getResources().getColor(R.color.text_color_white));
        } else {
            btnOpenYN.setBackground(ContextCompat.getDrawable(PlanCreateActivity.this, R.drawable.bg_check_off));
            btnOpenYN.setTextColor(getResources().getColor(R.color.text_color_black));
        }
    }

    public void loadPlanDetail() {

        I2ConnectApi.requestJSON(PlanCreateActivity.this, I2UrlHelper.Plan.getViewSnsPlan(mTarPlanID))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.getViewSnsPlan onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getViewSnsPlan onError");
                        //Error dialog 표시
                        DialogUtil.showErrorDialog(PlanCreateActivity.this, e.getMessage());
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getViewSnsPlan onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                            try {
                                final String userNm = statusInfo.getString("usr_nm");
                                mTarObjTp = statusInfo.getString("obj_tar_tp");
                                mTarObjId = statusInfo.getString("obj_tar_id");
                                tvTarNm.setText("대상 : "+mTarObjTtl);

                                startCal = DateCalendarUtil.getCalenderFromYYYYMMDDHHSS(statusInfo.getString("start_dttm"));
                                endCal = DateCalendarUtil.getCalenderFromYYYYMMDDHHSS(statusInfo.getString("end_dttm"));
                                btnStartDate.setText(dateFormat.format(startCal.getTime()));
                                btnStartTime.setText(timeFormat.format(startCal.getTime()));
                                btnEndDate.setText(dateFormat.format(endCal.getTime()));
                                btnEndTime.setText(timeFormat.format(endCal.getTime()));

                                if (!statusInfo.isNull("plan_ttl")) etTitle.setText(statusInfo.getString("plan_ttl"));
                                if (!statusInfo.isNull("place")) etPlace.setText(statusInfo.getString("place"));
                                isOpen = statusInfo.getString("plan_open_yn").equals("Y");
                                toggleOpenYN(isOpen);

                                isAllDay = !statusInfo.isNull("plan_tp") && statusInfo.getString("plan_tp").equals("ALDO");
                                toggleAllDay(isAllDay);

                                if (!statusInfo.isNull("plan_dtl_cntn")) etDescript.setText(statusInfo.getString("plan_dtl_cntn"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void saveSnsPlan() {

        if (etTitle.getText().toString().length() == 0) {
            DialogUtil.showInformationDialog(this, "제목을 입력해주십시오.");
            return;
        }

        DialogUtil.showConfirmDialog(PlanCreateActivity.this, "알림", "일정을 저장하시겠습니까?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String startDttm = DateCalendarUtil.getYMDHSFromCalendar(startCal);
                String endDttm = DateCalendarUtil.getYMDHSFromCalendar(endCal);

                if(spGroup.getVisibility() == View.VISIBLE) {
                    mTarObjTp = getOjbTp();
                    mTarObjId = getGrpID();
                }

                String planType=null;
                if (isAllDay) {
                    planType = "ALDD";
                } else {
                    planType = "";
                }
                String openYN=null;
                if (isOpen) {
                    openYN = "Y";
                } else {
                    openYN = "N";
                }

                I2ConnectApi.requestJSON(PlanCreateActivity.this,
                        I2UrlHelper.Plan.saveSnsPlan(mTarPlanID, etTitle.getText().toString(), startDttm, endDttm,
                        etPlace.getText().toString(), planType, openYN, etDescript.getText().toString(), mTarObjTp, mTarObjId))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<JSONObject>() {
                            @Override
                            public void onCompleted() {
                                Log.d(TAG, "I2UrlHelper.SNS.saveSnsPlan onCompleted");
                                DialogUtil.showInformationDialog(PlanCreateActivity.this, "일정이 저장되었습니다.", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, "I2UrlHelper.SNS.saveSnsPlan onError");
                                //Error dialog 표시
                                e.printStackTrace();
                                DialogUtil.showErrorDialogWithValidateSession(PlanCreateActivity.this, e);
                            }

                            @Override
                            public void onNext(JSONObject jsonObject) {
                                Log.d(TAG, "I2UrlHelper.SNS.saveSnsPlan onNext");
                                if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                                    JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                                } else {
                                    Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }

    @Override
    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        if (dialog.getTag().equals("start_date")) {
            startCal.set(year, monthOfYear, dayOfMonth);
            btnStartDate.setText(dateFormat.format(startCal.getTime()));
        } else {
            endCal.set(year, monthOfYear, dayOfMonth);
            btnEndDate.setText(dateFormat.format(endCal.getTime()));
        }
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        if (seletedTimePicker.equals("start_time")) {
            startCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            startCal.set(Calendar.MINUTE, minute);
            btnStartTime.setText(timeFormat.format(startCal.getTime()));
        } else {
            endCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            endCal.set(Calendar.MINUTE, minute);
            btnEndTime.setText(timeFormat.format(endCal.getTime()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void loadGroups(int page) {
        I2ConnectApi.requestJSON(PlanCreateActivity.this, I2UrlHelper.SNS.getListUserGroup(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID), String.format("%d", page), ""))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.getListUserGroup onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListUserGroup onError");
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(PlanCreateActivity.this, e);
                        List<String> list = new ArrayList<>();
                        list.add("개인일정");
                        setDataSpinner(list);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListUserGroup onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            List<JSONObject> statusInfoArrayAsList = I2ResponseParser.getStatusInfoArrayAsList(jsonObject);

                            if (statusInfoArrayAsList != null && statusInfoArrayAsList.size() > 0) {
                                mGroupArray.addAll(statusInfoArrayAsList);
                                groupPage++;
                                loadGroups(groupPage);
                            } else {
                                groupPage = 1;
                                List<String> list = new ArrayList<>();
                                list.add("개인일정");
                                for (int i = 0; i<mGroupArray.size(); i++) {
                                    try {
                                        list.add(mGroupArray.get(i).getString("grp_nm"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                setDataSpinner(list);
                            }

                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void setDataSpinner(List<String> list) {
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(PlanCreateActivity.this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGroup.setAdapter(dataAdapter);
        spGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroupPos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public String getOjbTp() {
        String objTp = "GROUP";
        if (selectedGroupPos == 0) {
            objTp = "USER";
        }
        return objTp;
    }

    public String getGrpID() {
        String grpID = "";
        if (selectedGroupPos != 0) {
            try {
                grpID = mGroupArray.get(selectedGroupPos - 1).getString("grp_id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return grpID;
    }
}
