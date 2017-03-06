package com.i2max.i2smartwork;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.Tools;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;

import java.util.ArrayList;

public class I2SearchActivity extends BaseAppCompatActivity {
    static String TAG = I2SearchActivity.class.getSimpleName();

    public static final int REQUEST_SEARCH = 1111;
    public static final String START_POS = "start_pos";
    public static final String SEARCH_STR = "search_str";
    public static final String EXTRA_SEARCH_STR = "search_str";

    public static final int RIGHT_1 = 27;
    public static final int RIGHT_2 = 70;
    protected int mStartPos;

    protected final int REQUEST_VOICE = 1201;

    // 검색바 관련
    private CardView cvSearch;
    private ImageView ivSearchBack, ivClearSearch;
    private EditText etSearch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_i2_search);

        mStartPos = getIntent().getExtras().getInt(START_POS);
        String searchStr = getIntent().getExtras().getString(SEARCH_STR, "");

        cvSearch = (CardView) findViewById(R.id.card_search);
        ivSearchBack = (ImageView) findViewById(R.id.iv_search_back);
        ivClearSearch = (ImageView) findViewById(R.id.iv_clear_search);
        etSearch = (EditText) findViewById(R.id.et_search);
        etSearch.setText(searchStr);

        initSearchView();

        RelativeLayout rlBody = (RelativeLayout)findViewById(R.id.main_content);
        rlBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (cvSearch.getVisibility() != View.VISIBLE) {
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    showHideSearchBar();
                }
            }, 100);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        showHideSearchBar();
    }


    public void initSearchView() {
        ivSearchBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                showHideSearchBar();
            }
        });
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etSearch.getText().toString().length() == 0) {
                    ivClearSearch.setImageResource(R.drawable.ic_keyboard_voice_black_24dp);
                } else {
                    ivClearSearch.setImageResource(R.drawable.ic_close_black_24dp);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    setResult(RESULT_OK, getIntent().putExtra(EXTRA_SEARCH_STR, etSearch.getText().toString()));
                    showHideSearchBar();

//                    if (etSearch.getText().toString().trim().length() > 0) {
//                        setResult(RESULT_OK, getIntent().putExtra(EXTRA_SEARCH_STR, etSearch.getText().toString()));
//                        showHideSearchBar();
//                    } else {
//                        Toast.makeText(I2SearchActivity.this, "검색어를 입력해주세요.", Toast.LENGTH_LONG).show();
//                    }
                    return true;
                }
                return false;
            }
        });
        ivClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etSearch.getText().toString().length() == 0) {
                    Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-KR");
                    try {
                        startActivityForResult(i, REQUEST_VOICE);
                    } catch (Exception e) {
                        Toast.makeText(I2SearchActivity.this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    etSearch.setText("");
                    showHideSearchKeyboard(etSearch, true);
                }
            }
        });
    }

    public void showHideSearchBar() {
        if (cvSearch.getVisibility() == View.VISIBLE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final Animator animatorHide = ViewAnimationUtils.createCircularReveal(cvSearch,
                        cvSearch.getWidth() - (int) Tools.fromDpToPx(mStartPos), (int) Tools.fromDpToPx(26),
                        (float) Math.hypot(cvSearch.getWidth(), cvSearch.getHeight()), 0);
                animatorHide.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        cvSearch.setVisibility(View.GONE);
                        showHideSearchKeyboard(etSearch, false);
                        finish();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animatorHide.setDuration(300);
                animatorHide.start();
            } else {
                showHideSearchKeyboard(etSearch, false);
                cvSearch.setVisibility(View.GONE);
                finish();
            }
            etSearch.setText("");
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final Animator animator = ViewAnimationUtils.createCircularReveal(cvSearch,
                        cvSearch.getWidth() - (int) Tools.fromDpToPx(mStartPos), (int) Tools.fromDpToPx(26), 0,
                        (float) Math.hypot(cvSearch.getWidth(), cvSearch.getHeight()));

                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        showHideSearchKeyboard(etSearch, true);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                cvSearch.setVisibility(View.VISIBLE);
                if (cvSearch.getVisibility() == View.VISIBLE) {
                    animator.setDuration(300);
                    animator.start();
                }
            } else {
                cvSearch.setVisibility(View.VISIBLE);
                showHideSearchKeyboard(etSearch, true);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_VOICE  && resultCode==RESULT_OK) {
            ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            etSearch.setText(thingsYouSaid.get(0));
            showHideSearchKeyboard(etSearch, true);
            //mStrSearch = etSearch.getText().toString();

        }
    }

    protected void showHideSearchKeyboard(final EditText view, boolean doShow) {
        if (view == null) {
            return;
        }

        try {
            if (doShow) {
                (new Handler()).postDelayed(new Runnable() {
                    public void run() {
                        view.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                        view.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                        etSearch.setSelection(etSearch.getText().length());
                    }
                }, 100);
            } else {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

        } catch (Exception e) {
            Log.e(TAG, "decideFocus. Exception", e);
        }

    }
}
