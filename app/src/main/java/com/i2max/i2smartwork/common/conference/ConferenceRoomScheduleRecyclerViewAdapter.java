package com.i2max.i2smartwork.common.conference;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.internal.LinkedTreeMap;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.utils.FormatUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shlee on 15. 9. 11..
 */
public class ConferenceRoomScheduleRecyclerViewAdapter
        extends RecyclerView.Adapter<ConferenceRoomScheduleRecyclerViewAdapter.ViewHolder> {

    protected Context mContext;
    private List<LinkedTreeMap<String, String>>  mValues;
    protected List<List<String>> mReservList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public String mCfrcRoomId;
        protected TextView mTvRoomNm;
        protected LinearLayout llSchdule0800, llSchdule0830, llSchdule0900, llSchdule0930, llSchdule1000, llSchdule1030, llSchdule1100, llSchdule1130,  llSchdule1200,
                llSchdule1230, llSchdule1300, llSchdule1330, llSchdule1400, llSchdule1430, llSchdule1500, llSchdule1530, llSchdule1600, llSchdule1630, llSchdule1700,
                llSchdule1730, llSchdule1800, llSchdule1830, llSchdule1900, llSchdule1930, llSchdule2000;
        protected ImageView ivSchdule0800, ivSchdule0830, ivSchdule0900, ivSchdule0930, ivSchdule1000, ivSchdule1030, ivSchdule1100, ivSchdule1130,  ivSchdule1200,
                ivSchdule1230, ivSchdule1300, ivSchdule1330, ivSchdule1400, ivSchdule1430, ivSchdule1500, ivSchdule1530, ivSchdule1600, ivSchdule1630, ivSchdule1700,
                ivSchdule1730, ivSchdule1800, ivSchdule1830, ivSchdule1900, ivSchdule1930, ivSchdule2000;
        protected List<LinearLayout> mListLlSchdule;
        protected List<ImageView> mListIvSchdule;

        public ViewHolder(View view) {
            super(view);

            mView = view;
            mTvRoomNm = (TextView) view.findViewById(R.id.tv_cfrc_room_nm);
            llSchdule0800 = (LinearLayout) view.findViewById(R.id.ll_schedule_0800);
            llSchdule0830 = (LinearLayout) view.findViewById(R.id.ll_schedule_0830);
            llSchdule0900 = (LinearLayout) view.findViewById(R.id.ll_schedule_0900);
            llSchdule0930 = (LinearLayout) view.findViewById(R.id.ll_schedule_0930);
            llSchdule1000 = (LinearLayout) view.findViewById(R.id.ll_schedule_1800);
            llSchdule1030 = (LinearLayout) view.findViewById(R.id.ll_schedule_1830);
            llSchdule1100 = (LinearLayout) view.findViewById(R.id.ll_schedule_1100);
            llSchdule1130 = (LinearLayout) view.findViewById(R.id.ll_schedule_1130);
            llSchdule1200 = (LinearLayout) view.findViewById(R.id.ll_schedule_1200);
            llSchdule1230 = (LinearLayout) view.findViewById(R.id.ll_schedule_1230);
            llSchdule1300 = (LinearLayout) view.findViewById(R.id.ll_schedule_1300);
            llSchdule1330 = (LinearLayout) view.findViewById(R.id.ll_schedule_1330);
            llSchdule1400 = (LinearLayout) view.findViewById(R.id.ll_schedule_1400);
            llSchdule1430 = (LinearLayout) view.findViewById(R.id.ll_schedule_1430);
            llSchdule1500 = (LinearLayout) view.findViewById(R.id.ll_schedule_1500);
            llSchdule1530 = (LinearLayout) view.findViewById(R.id.ll_schedule_1530);
            llSchdule1600 = (LinearLayout) view.findViewById(R.id.ll_schedule_1600);
            llSchdule1630 = (LinearLayout) view.findViewById(R.id.ll_schedule_1630);
            llSchdule1700 = (LinearLayout) view.findViewById(R.id.ll_schedule_1700);
            llSchdule1730 = (LinearLayout) view.findViewById(R.id.ll_schedule_1730);
            llSchdule1800 = (LinearLayout) view.findViewById(R.id.ll_schedule_1800);
            llSchdule1830 = (LinearLayout) view.findViewById(R.id.ll_schedule_1830);
            llSchdule1900 = (LinearLayout) view.findViewById(R.id.ll_schedule_1900);
            llSchdule1930 = (LinearLayout) view.findViewById(R.id.ll_schedule_1930);
            llSchdule2000 = (LinearLayout) view.findViewById(R.id.ll_schedule_2000);
            mListLlSchdule = new ArrayList<>();
            mListLlSchdule.add(llSchdule0800);
            mListLlSchdule.add(llSchdule0830);
            mListLlSchdule.add(llSchdule0900);
            mListLlSchdule.add(llSchdule0930);
            mListLlSchdule.add(llSchdule1000);
            mListLlSchdule.add(llSchdule1030);
            mListLlSchdule.add(llSchdule1100);
            mListLlSchdule.add(llSchdule1130);
            mListLlSchdule.add(llSchdule1200);
            mListLlSchdule.add(llSchdule1230);
            mListLlSchdule.add(llSchdule1300);
            mListLlSchdule.add(llSchdule1330);
            mListLlSchdule.add(llSchdule1400);
            mListLlSchdule.add(llSchdule1430);
            mListLlSchdule.add(llSchdule1500);
            mListLlSchdule.add(llSchdule1530);
            mListLlSchdule.add(llSchdule1600);
            mListLlSchdule.add(llSchdule1630);
            mListLlSchdule.add(llSchdule1700);
            mListLlSchdule.add(llSchdule1730);
            mListLlSchdule.add(llSchdule1800);
            mListLlSchdule.add(llSchdule1830);
            mListLlSchdule.add(llSchdule1900);
            mListLlSchdule.add(llSchdule1930);
            mListLlSchdule.add(llSchdule2000);
            for (int i = 0; i < mListLlSchdule.size() ; i++) {
                mListLlSchdule.get(i).setTag(i);
            }

            ivSchdule0800 = (ImageView) view.findViewById(R.id.iv_schedule_0800);
            ivSchdule0830 = (ImageView) view.findViewById(R.id.iv_schedule_0830);
            ivSchdule0900 = (ImageView) view.findViewById(R.id.iv_schedule_0900);
            ivSchdule0930 = (ImageView) view.findViewById(R.id.iv_schedule_0930);
            ivSchdule1000 = (ImageView) view.findViewById(R.id.iv_schedule_1000);
            ivSchdule1030 = (ImageView) view.findViewById(R.id.iv_schedule_1030);
            ivSchdule1100 = (ImageView) view.findViewById(R.id.iv_schedule_1100);
            ivSchdule1130 = (ImageView) view.findViewById(R.id.iv_schedule_1130);
            ivSchdule1200 = (ImageView) view.findViewById(R.id.iv_schedule_1200);
            ivSchdule1230 = (ImageView) view.findViewById(R.id.iv_schedule_1230);
            ivSchdule1300 = (ImageView) view.findViewById(R.id.iv_schedule_1300);
            ivSchdule1330 = (ImageView) view.findViewById(R.id.iv_schedule_1330);
            ivSchdule1400 = (ImageView) view.findViewById(R.id.iv_schedule_1400);
            ivSchdule1430 = (ImageView) view.findViewById(R.id.iv_schedule_1430);
            ivSchdule1500 = (ImageView) view.findViewById(R.id.iv_schedule_1500);
            ivSchdule1530 = (ImageView) view.findViewById(R.id.iv_schedule_1530);
            ivSchdule1600 = (ImageView) view.findViewById(R.id.iv_schedule_1600);
            ivSchdule1630 = (ImageView) view.findViewById(R.id.iv_schedule_1630);
            ivSchdule1700 = (ImageView) view.findViewById(R.id.iv_schedule_1700);
            ivSchdule1730 = (ImageView) view.findViewById(R.id.iv_schedule_1730);
            ivSchdule1800 = (ImageView) view.findViewById(R.id.iv_schedule_1800);
            ivSchdule1830 = (ImageView) view.findViewById(R.id.iv_schedule_1830);
            ivSchdule1900 = (ImageView) view.findViewById(R.id.iv_schedule_1900);
            ivSchdule1930 = (ImageView) view.findViewById(R.id.iv_schedule_1930);
            ivSchdule2000 = (ImageView) view.findViewById(R.id.iv_schedule_2000);
            mListIvSchdule = new ArrayList<>();
            mListIvSchdule.add(ivSchdule0800);
            mListIvSchdule.add(ivSchdule0830);
            mListIvSchdule.add(ivSchdule0900);
            mListIvSchdule.add(ivSchdule0930);
            mListIvSchdule.add(ivSchdule1000);
            mListIvSchdule.add(ivSchdule1030);
            mListIvSchdule.add(ivSchdule1100);
            mListIvSchdule.add(ivSchdule1130);
            mListIvSchdule.add(ivSchdule1200);
            mListIvSchdule.add(ivSchdule1230);
            mListIvSchdule.add(ivSchdule1300);
            mListIvSchdule.add(ivSchdule1330);
            mListIvSchdule.add(ivSchdule1400);
            mListIvSchdule.add(ivSchdule1430);
            mListIvSchdule.add(ivSchdule1500);
            mListIvSchdule.add(ivSchdule1530);
            mListIvSchdule.add(ivSchdule1600);
            mListIvSchdule.add(ivSchdule1630);
            mListIvSchdule.add(ivSchdule1700);
            mListIvSchdule.add(ivSchdule1730);
            mListIvSchdule.add(ivSchdule1800);
            mListIvSchdule.add(ivSchdule1830);
            mListIvSchdule.add(ivSchdule1900);
            mListIvSchdule.add(ivSchdule1930);
            mListIvSchdule.add(ivSchdule2000);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public LinkedTreeMap<String, String> getValueAt(int position) {
        return mValues.get(position);
    }

    public ConferenceRoomScheduleRecyclerViewAdapter(Context context, List<LinkedTreeMap<String, String>> items) {
        mContext = context;
        mValues = items;
        mReservList = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_cfrc_room_schedule, parent, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            TypedValue outValue = new TypedValue();
            mContext.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            view.setBackgroundResource(outValue.resourceId);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final LinkedTreeMap<String, String> item = mValues.get(position);

        holder.mCfrcRoomId = FormatUtil.getStringValidate(item.get("cfrc_room_id"));
        holder.mTvRoomNm.setText(FormatUtil.getStringValidate(item.get("cfrc_room_nm")));

        final List<String> list = new ArrayList<>();
        list.add(FormatUtil.getStringValidate(item.get("p0800")));
        list.add(FormatUtil.getStringValidate(item.get("p0830")));
        list.add(FormatUtil.getStringValidate(item.get("p0900")));
        list.add(FormatUtil.getStringValidate(item.get("p0930")));
        list.add(FormatUtil.getStringValidate(item.get("p1000")));
        list.add(FormatUtil.getStringValidate(item.get("p1030")));
        list.add(FormatUtil.getStringValidate(item.get("p1100")));
        list.add(FormatUtil.getStringValidate(item.get("p1130")));
        list.add(FormatUtil.getStringValidate(item.get("p1200")));
        list.add(FormatUtil.getStringValidate(item.get("p1230")));
        list.add(FormatUtil.getStringValidate(item.get("p1300")));
        list.add(FormatUtil.getStringValidate(item.get("p1330")));
        list.add(FormatUtil.getStringValidate(item.get("p1400")));
        list.add(FormatUtil.getStringValidate(item.get("p1430")));
        list.add(FormatUtil.getStringValidate(item.get("p1500")));
        list.add(FormatUtil.getStringValidate(item.get("p1530")));
        list.add(FormatUtil.getStringValidate(item.get("p1600")));
        list.add(FormatUtil.getStringValidate(item.get("p1630")));
        list.add(FormatUtil.getStringValidate(item.get("p1700")));
        list.add(FormatUtil.getStringValidate(item.get("p1730")));
        list.add(FormatUtil.getStringValidate(item.get("p1800")));
        list.add(FormatUtil.getStringValidate(item.get("p1830")));
        list.add(FormatUtil.getStringValidate(item.get("p1900")));
        list.add(FormatUtil.getStringValidate(item.get("p1930")));
        list.add(FormatUtil.getStringValidate(item.get("p2000")));
        ((ConferenceRoomScheduleActivity)mContext).getReservList().add(list);
//        final int roomIndex = position;
        for (int i = 0; i < holder.mListLlSchdule.size(); i++) {
            setIvScheduled(i, holder.mListIvSchdule.get(i), list.get(i));
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    //예약 현황 표시
    public void setIvScheduled(int pos, ImageView ivView, String value) {
        if(pos%2 == 0) { //1시간 단위 이미지처리
            if ("1.0".equals(value)) { //예약이 있을경우
                ivView.setImageResource(R.drawable.ic_icon_nonnoti);
                ivView.setTag(R.drawable.ic_icon_nonnoti);
            } else { //예약이 없을경우
                ivView.setTag(R.drawable.ic_icon_non);
            }
        } else { //30분 단위 이미지처리
            if("1.0".equals(value)) { //예약이 있을경우
                ivView.setImageResource(R.drawable.ic_icon_nonnoti2);
                ivView.setTag(R.drawable.ic_icon_nonnoti2);
            } else { //예약이 없을경우
                ivView.setTag(R.drawable.ic_icon_non2);
            }
        }
    }

    //활성화 > 비활성화 처리
    public void setIvDefault(int pos, ImageView ivView) {
        if(pos%2 == 0) { //1시간 단위 이미지처리
            if ((int)ivView.getTag() == R.drawable.ic_icon_nonnoti) { //활성화 되었을 경우
                ivView.setTag(R.drawable.ic_icon_non);
            }
        } else { //30분 단위 이미지처리
            if ((int)ivView.getTag() == R.drawable.ic_icon_nonnoti2) { //활성화 되었을 경우
                ivView.setTag(R.drawable.ic_icon_non2);
            }
        }
    }

    // 선택시 활성화 처리
    public void setReserved(int timeIndex, ImageView ivView) {
        if(timeIndex%2 == 0) { //1시간 단위 이미지처리
            ivView.setImageResource(R.drawable.ic_icon_noti);
        } else { //30분 단위 이미지처리
            ivView.setImageResource(R.drawable.ic_icon_noti2);
        }
    }
    int firstRoomIndex = -1, firstTimeIndex = -1;
    int secondRoomIndex = -1, secondTimeIndex = -1;
    ImageView ivFirstTime, ivSecondTime;
    public void setTime(int roomIndex, int timeIndex, ImageView ivTime, String value) {
        if("1.0".equals(value)) { // 예약상태 확인
            Toast.makeText(mContext, "이미 예약되어있습니다.", Toast.LENGTH_LONG).show();
            return; //예약됨
        }

        // 첫번째 선택임?
        if(firstRoomIndex == -1 && firstTimeIndex == -1) {
            //첫 선택 일때 셋팅
            firstRoomIndex = roomIndex;
            firstTimeIndex = timeIndex;
            ivFirstTime = ivTime;
            setReserved(firstTimeIndex, ivFirstTime);
            return;
        } else if(firstRoomIndex != roomIndex) {
            //첫 선택은 아닌데 방이 다름? 첫번째 삭제하고, 설정
            setIvDefault(firstTimeIndex, ivFirstTime);
            firstRoomIndex = roomIndex;
            firstTimeIndex = timeIndex;
            ivFirstTime = ivTime;
            setReserved(firstTimeIndex, ivFirstTime);
            return;
        } else if(secondRoomIndex == -1 && secondTimeIndex == -1 ) { //두번째 선택임?
            if(firstRoomIndex == timeIndex) { //첫번째랑 같음?
                return;
            } else { //두번째 선택일때 셋팅
                secondRoomIndex = roomIndex;
                secondTimeIndex = timeIndex;
                ivSecondTime = ivTime;
                setReserved(secondRoomIndex, ivSecondTime);
                return;
            }
        }  else if(secondRoomIndex != roomIndex) { // 두번째 값있네? 두번째 방다름?
            if(firstRoomIndex != roomIndex) { //혹시 첫번째 방도 다름? 헐 다 초기화임
                setIvDefault(firstTimeIndex, ivFirstTime);
                setIvDefault(secondTimeIndex, ivSecondTime);
                firstRoomIndex = roomIndex;
                firstTimeIndex = timeIndex;
                ivFirstTime = ivTime;
                setReserved(firstTimeIndex, ivFirstTime);
                secondRoomIndex = -1;
                secondTimeIndex = -1;
                ivSecondTime = null;
                return;
            } else { // 첫번째 ㅂ두번째 방만 다를떄
                setIvDefault(secondTimeIndex, ivSecondTime);
                secondRoomIndex = roomIndex;
                secondTimeIndex = timeIndex;
                ivSecondTime = ivTime;
                setReserved(secondRoomIndex, ivSecondTime);
            }
        }
    }


}