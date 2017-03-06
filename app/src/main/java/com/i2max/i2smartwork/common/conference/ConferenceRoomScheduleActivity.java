package com.i2max.i2smartwork.common.conference;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.datetimepicker.date.DatePickerDialog;
import com.google.gson.internal.LinkedTreeMap;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.component.SimpleDividerItemDecoration;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.FormatUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by shlee on 15. 9. 18..
 */
public class ConferenceRoomScheduleActivity extends BaseAppCompatActivity implements DatePickerDialog.OnDateSetListener {
    static String TAG = ConferenceRoomScheduleActivity.class.getSimpleName();

    public final static String READ_ONLY = "read_only";

    private List<LinkedTreeMap<String, String>> mPlanList;
    public List<List<String>> mReservList;
    protected ConferenceRoomScheduleRecyclerViewAdapter mAdapter;
    protected LinearLayout mLlEditable;
    protected RecyclerView mRV;
    protected TextView mTvEmpty;
    protected Button mBtDt, mBtSave;
    protected Spinner mSpRoom, mSpStartTm, mSpEndTm;
    protected List<String> mRoomIdList;
    private boolean mReadOnly = false, mOnline = false;
    protected static final String TIME_PATTERN = "HH:mm";
    protected Calendar now;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conference_room_schedule);

        Bundle bundle = getIntent().getExtras();
        String title = "";
        if (bundle != null) {
            title= bundle.getString(CodeConstant.TITLE, getString(R.string.connect_community));
            mReadOnly = "Y".equals(bundle.getString(READ_ONLY, "N"));
            mOnline = "Y".equals(bundle.getString(ConferenceWriteActivity.CFRC_ONLINE, "N"));
            Log.e(TAG, "online = "+ mOnline);
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(title);

        mLlEditable = (LinearLayout) findViewById(R.id.ll_editable);
        mBtDt = (Button) findViewById(R.id.btn_date);
//        mBtStartTm = (Button) findViewById(R.id.btn_start_time);
//        mBtEndTm = (Button) findViewById(R.id.btn_end_time);
        mSpStartTm = (Spinner) findViewById(R.id.sp_start_time);
        mSpStartTm.setPrompt("시작시간을 선택하세요.");
        mSpEndTm = (Spinner) findViewById(R.id.sp_end_time);
        mSpEndTm.setPrompt("종료시간을 선택하세요.");
        mBtSave = (Button) findViewById(R.id.btn_save);
        mSpRoom = (Spinner) findViewById(R.id.btn_room);
        mSpRoom.setPrompt("회의실을 선택하세요.");

        mRV = (RecyclerView) findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mRV.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRV.setLayoutManager(layoutManager);
        mRV.addItemDecoration(new SimpleDividerItemDecoration(mRV.getContext()));

        mReservList = new ArrayList<>();
        mPlanList = new ArrayList<LinkedTreeMap<String, String>>();
        mAdapter = new ConferenceRoomScheduleRecyclerViewAdapter(ConferenceRoomScheduleActivity.this, mPlanList);
        mRV.setAdapter(mAdapter);

        mTvEmpty = (TextView) findViewById(R.id.empty_view);
        mTvEmpty.setText(getString(R.string.no_cfrc_room_available));

        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeViewsValues();
        loadRecyclerView(mBtDt.getText().toString().trim());
    }

    public void initView() {
        now = Calendar.getInstance();
        mBtDt.setText(FormatUtil.getSendableFormat3Today());
        setTimeSpinner(mSpStartTm, "시작시간");
        setTimeSpinner(mSpEndTm, "종료시간");

        mBtDt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.newInstance(ConferenceRoomScheduleActivity.this, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show(getFragmentManager(), "cfrc_dt");
            }
        });


        if(mReadOnly) {
            mLlEditable.setVisibility(View.GONE);
            mBtSave.setVisibility(View.GONE);
        } else {
            mSpStartTm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.e(TAG, "mStartTm="+position+ " / mSpEndTm="+mSpEndTm.getSelectedItemPosition());
                    if (position <= 1) {
//                    Toast.makeText(ConferenceRoomScheduleActivity.this, "시작시간을 선택해주세요.", Toast.LENGTH_LONG).show();
                        return;
                    } else if (mSpEndTm.getSelectedItemPosition() > 2 && mSpEndTm.getSelectedItemPosition() <= position) {
                        Toast.makeText(ConferenceRoomScheduleActivity.this, "시작시간을 종료시간 이후로 설정할 수 없습니다.", Toast.LENGTH_LONG).show();
                        mSpStartTm.setSelection(0);
                        return;
                    }

                    String date = mBtDt.getText().toString().replace("-", "");
                    String startTime = mSpStartTm.getSelectedItem().toString().replace(":", "");

                    if (isPreviousTime(date, startTime)) { //회의시간이 현재보다 이른지 확인 처리
                        Toast.makeText(ConferenceRoomScheduleActivity.this, "시작시간을 현재시간 이전으로 설정할 수 없습니다.", Toast.LENGTH_LONG).show();
                        mSpStartTm.setSelection(0);
                        return;
                    } else {
                        if(isDuplicatedReserve()) {
                            Toast.makeText(ConferenceRoomScheduleActivity.this, "이미 예약이 되어있습니다.", Toast.LENGTH_LONG).show();
                            mSpStartTm.setSelection(0);
                        } else setPlanImage(true, mSpRoom.getSelectedItemPosition()-1);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            mSpEndTm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position <= 1) {
//                    Toast.makeText(ConferenceRoomScheduleActivity.this, "종료시간을 선택해주세요.", Toast.LENGTH_LONG).show();
                        return;
                    } else if (mSpStartTm.getSelectedItemPosition() >= position) {
                        Toast.makeText(ConferenceRoomScheduleActivity.this, "종료시간을 시작시간 이전으로 설정할 수 없습니다.", Toast.LENGTH_LONG).show();
                        mSpEndTm.setSelection(0);
                        return;
                    }

                    String date = mBtDt.getText().toString().replace("-", "");
                    String endTime = mSpEndTm.getSelectedItem().toString().replace(":", "");

                    if (isPreviousTime(date, endTime)) { //회의시간이 현재보다 이른지 확인 처리
                        Toast.makeText(ConferenceRoomScheduleActivity.this, "종료시간을 현재시간 이전으로 설정할 수 없습니다.", Toast.LENGTH_LONG).show();
                        mSpEndTm.setSelection(0);
                        return;
                    } else {
                        Log.e(TAG, "selectedStartTmPos = " + mSpStartTm.getSelectedItemPosition() + " / endTimeIndex = " + (position - 1));
                        if(isDuplicatedReserve()) {
                            Toast.makeText(ConferenceRoomScheduleActivity.this, "이미 예약이 되어있습니다.", Toast.LENGTH_LONG).show();
                            mSpEndTm.setSelection(0);
                        } else setPlanImage(true, mSpRoom.getSelectedItemPosition()-1);


                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            mBtSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSpRoom.getSelectedItemPosition() < 1) {
                        Toast.makeText(ConferenceRoomScheduleActivity.this, "회의실 선택하시기 바랍니다.", Toast.LENGTH_LONG).show();
                        return;
                    } else if (mSpStartTm.getSelectedItemPosition() < 1) {
                        Toast.makeText(ConferenceRoomScheduleActivity.this, "시작시간을 선택하시기 바랍니다.", Toast.LENGTH_LONG).show();
                        return;
                    } else if (mSpEndTm.getSelectedItemPosition() < 2) {
                        Toast.makeText(ConferenceRoomScheduleActivity.this, "종료시간을 선택하시기 바랍니다.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent data = getIntent();
                    data.putExtra("cfrc_dt", mBtDt.getText().toString().trim());
                    data.putExtra("cfrc_start_tm", mSpStartTm.getSelectedItem().toString());
                    data.putExtra("cfrc_end_tm", mSpEndTm.getSelectedItem().toString());
                    Log.e(TAG,"selectedRoomPos = "+ mSpRoom.getSelectedItemPosition() + " / roomId = "+mRoomIdList.get(mSpRoom.getSelectedItemPosition()) + " / room_nm = "+ mSpRoom.getSelectedItem().toString());
                    data.putExtra("cfrc_room_id", mRoomIdList.get(mSpRoom.getSelectedItemPosition()));
                    data.putExtra("cfrc_room_nm", mSpRoom.getSelectedItem().toString());
                    setResult(RESULT_OK, data);
                    finish();
                }
            });
        }
    }

    int mPrevRoomPos = -1;
    public void setDataSpinner(List<LinkedTreeMap<String, String>> result) {
        List<String> roomNmList = new ArrayList<>();
        mRoomIdList = new ArrayList<>();
        mRoomIdList.add("");
        roomNmList.add("회의실");

        for (int i = 0; i < result.size(); i++) {
            mRoomIdList.add(result.get(i).get("cfrc_room_id"));
            roomNmList.add(result.get(i).get("cfrc_room_nm"));
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getSupportActionBar().getThemedContext(), android.R.layout.simple_spinner_item, roomNmList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpRoom.setAdapter(dataAdapter);
        mSpRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0) {
//                    Toast.makeText(ConferenceRoomScheduleActivity.this, "회의실을 선택해주세요.", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    if (isDuplicatedReserve()) {
                        Toast.makeText(ConferenceRoomScheduleActivity.this, "이미 예약이 되어있습니다.", Toast.LENGTH_LONG).show();
                        mSpRoom.setSelection(0);
                    } else {
                        //회의실 변경전 예약현황  삭제
                        if (mPrevRoomPos > -1) setPlanImage(false, mPrevRoomPos);
                        // 변경한 회의실 예약현황 그리기
                        setPlanImage(true, mSpRoom.getSelectedItemPosition() - 1);
                        mPrevRoomPos = mSpRoom.getSelectedItemPosition() - 1;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void setTimeSpinner(Spinner spView, String title) {
       String[] timeArray = getResources().getStringArray(R.array.time_select);
        timeArray[0] = title;

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getSupportActionBar().getThemedContext(), android.R.layout.simple_spinner_item, timeArray);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spView.setAdapter(dataAdapter);
    }

    protected void loadRecyclerView(String cfrcDt) {
        if(mOnline) {
            //온라인 처리 추가
            List<LinkedTreeMap<String, String>> planList = new ArrayList<LinkedTreeMap<String, String>>();
            LinkedTreeMap<String, String> onlineRoom = new LinkedTreeMap<String, String>();
            onlineRoom.put("cfrc_room_nm","온라인");
            planList.add(onlineRoom);
            setDataSpinner(planList);
            mPlanList.addAll(planList);
            mAdapter.notifyDataSetChanged();
        } else {
            I2ConnectApi.requestJSON2Map(ConferenceRoomScheduleActivity.this, I2UrlHelper.Cfrc.getListSnsConferencePlan(cfrcDt))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Map<String, Object>>() {
                        @Override
                        public void onCompleted() {
                            Log.d(TAG, "I2UrlHelper.Conference.getListSnsConferencePlan onCompleted");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, "I2UrlHelper.Conference.getListSnsConferencePlan onError");
                            //Error dialog 표시
                            DialogUtil.showErrorDialog(ConferenceRoomScheduleActivity.this, e.getMessage());
                            e.printStackTrace();
                            //Error dialog 표시
                            DialogUtil.showErrorDialogWithValidateSession(ConferenceRoomScheduleActivity.this, e);
                        }

                        @Override
                        public void onNext(Map<String, Object> result) {
                            Log.d(TAG, "I2UrlHelper.Conference.getListSnsConferencePlan onNext");

                            LinkedTreeMap<String, Object> statusInfo = (LinkedTreeMap<String, Object>) result.get("statusInfo");
                            List<LinkedTreeMap<String, String>> planList = (List<LinkedTreeMap<String, String>>) statusInfo.get("list_data");
                            if (planList != null && planList.size() > 0) {
                                setDataSpinner(planList);
                                mPlanList.addAll(planList);
                                mAdapter.notifyDataSetChanged();
                            }
                            //회의실 정보가 없습니다
                            setEmptyResult(mPlanList.size());
                        }
                    });
        }
    }

    public void setEmptyResult(int totalcnt) {
        if(totalcnt < 1) {
            mRV.setVisibility(View.GONE);
            mTvEmpty.setVisibility(View.VISIBLE);
        } else {
            mRV.setVisibility(View.VISIBLE);
            mTvEmpty.setVisibility(View.GONE);
            mTvEmpty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //리프레쉬
                }
            });
        }
    }

    @Override
    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        if (dialog.getTag().equals("cfrc_dt")) {
            now.set(year, monthOfYear, dayOfMonth);
            mBtDt.setText(FormatUtil.getFormattedDate3(now.getTime()));
            initializeViewsValues();
            loadRecyclerView(mBtDt.getText().toString().toString());
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


    public void initializeViewsValues() {
        mPlanList.clear();
        mRV.removeAllViews();
    }

    public void setPlanImage(boolean isReserve, int roomPosition) {
        Log.e(TAG, "roomIndex =" + mSpRoom.getSelectedItemPosition() +
                " /startTime = " + mSpStartTm.getSelectedItemPosition() +
                " / endTime = " + mSpEndTm.getSelectedItemPosition());
        if(mSpRoom.getSelectedItemPosition() > 0
                && mSpStartTm.getSelectedItemPosition() > 0
                && mSpEndTm.getSelectedItemPosition() > 0) {
            ConferenceRoomScheduleRecyclerViewAdapter.ViewHolder viewHolder =
                    (ConferenceRoomScheduleRecyclerViewAdapter.ViewHolder) mRV.getChildViewHolder(mRV.getChildAt(roomPosition));
            for (int i = mSpStartTm.getSelectedItemPosition()-1; i <= mSpEndTm.getSelectedItemPosition()-1; i++) {
                Log.e(TAG, "i = " + i);
                if (isReserve) setReserveImage(i, viewHolder.mListIvSchdule.get(i));
                else setDefaultImage(i, viewHolder.mListIvSchdule.get(i));
            }
        }

    }

    // 선택시 활성화 처리
    public void setReserveImage(int timeIndex, ImageView ivView) {
        if(timeIndex%2 == 0) { //1시간 단위 이미지처리
            ivView.setImageResource(R.drawable.ic_icon_noti);
        } else { //30분 단위 이미지처리
            ivView.setImageResource(R.drawable.ic_icon_noti2);
        }
    }
    // 디폴트 이미지로 변경
    public void setDefaultImage(int timeIndex, ImageView ivView) {
        if(timeIndex%2 == 0) { //1시간 단위 이미지처리
            ivView.setImageResource(R.drawable.ic_icon_non);
        } else { //30분 단위 이미지처리
            ivView.setImageResource(R.drawable.ic_icon_non2);
        }
    }

    public boolean isDuplicatedReserve() {
        boolean bool = false;

        if(mSpRoom.getSelectedItemPosition() <= 0 || mSpStartTm.getSelectedItemPosition() <= 0 || mSpEndTm.getSelectedItemPosition() <= 0)
            return bool;

        List<String> list = mReservList.get(mSpRoom.getSelectedItemPosition()-1);
        if((mSpRoom.getSelectedItemPosition()-1) <= 0 ||  (mSpStartTm.getSelectedItemPosition() - 1) == -1  ||  (mSpEndTm.getSelectedItemPosition() - 1) == -1) {
            bool = false;
        } else {
            for (int i = mSpStartTm.getSelectedItemPosition() - 1; i < mSpEndTm.getSelectedItemPosition() - 1; i++) {
                Log.e(TAG, mSpRoom.getSelectedItem().toString() + " check reservation = " + list.get(i) + " / pos = " + i);
                if ("1.0".equals(list.get(i))) {
                    bool = true;
                    break;
                }
            }
        }
        return bool;
    }

    public List<List<String>> getReservList() {
        return mReservList;
    }

    public boolean isPreviousTime(String date, String time) {
        boolean result = false;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");

        Date now = new Date();
        Date targetDate = null;
        String targetDttm = date.replace("-", "") + "" +time.replace(":", "");
        Log.e(TAG, "comfareDateTime = "+ targetDttm);
        try {
            targetDate = formatter.parse(targetDttm);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return isPreviousTime(now, targetDate);

    }

    public boolean isPreviousDateTime(String dttm1, String dttm2) {
        boolean result = false;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");

        Date date1 = null;
        Date date2 = null;
        Log.e(TAG, "dttm1 = "+ dttm1+" /dttm2 = "+ dttm2);
        try {
            date1 = formatter.parse(dttm1);
            date2 = formatter.parse(dttm2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return isPreviousTime(date1, date2);

    }

    public boolean isPreviousTime(Date date1, Date date2) {
        boolean result = false;

        result = date1.compareTo(date2) >= 0;

        return result;
    }
}

