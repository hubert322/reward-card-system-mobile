package com.learninga_z.onyourown.student.rewardcard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.learninga_z.lazlibrary.analytics.AnalyticsTrackable;
import com.learninga_z.lazlibrary.task.TaskRunner;
import com.learninga_z.onyourown.R;
import com.learninga_z.onyourown.core.activity.KazActivity;
import com.learninga_z.onyourown.core.activity.OnBackPressListener;
import com.learninga_z.onyourown.core.beans.ActivityOptionBean;
import com.learninga_z.onyourown.core.beans.StudentBean;
import com.learninga_z.onyourown.student.KazStudentBaseFragment;
import com.learninga_z.onyourown.student.qrcodereader.BarcodeCaptureActivity;
import com.learninga_z.onyourown.student.rewardcard.beans.RedeemBean;

public class RedeemFragment extends KazStudentBaseFragment implements AnalyticsTrackable, OnBackPressListener, RedeemTaskLoader.RedeemTaskListenerInterface
{
    private RedeemBean mRedeemBean;
    private int mPaddingTop;
    private int mPaddingBottom;
    private int mPaddingLeft;
    private int mPaddingRight;

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "RedeemFragment";

    private Button scanButton;
    private Button redeemButton;
    private EditText rewardCardCodeEditText;
    private TextView messageTextView;

    public RedeemBean getRedeemBean ()
    {
        return mRedeemBean;
    }

    public static RedeemFragment newInstance (RedeemBean redeemBean)
    {
        return newInstance (redeemBean, 0, 0, 0, 0);
    }

    public static RedeemFragment newInstance (RedeemBean redeemBean, int paddingTop, int paddingBottom, int paddingLeft, int paddingRight)
    {
        RedeemFragment frag = new RedeemFragment ();
        final Bundle args = new Bundle ();
        args.putParcelable ("redeemBean", redeemBean);
        args.putInt ("paddingTop", paddingTop);
        args.putInt ("paddingBottom", paddingBottom);
        args.putInt ("paddingLeft", paddingLeft);
        args.putInt ("paddingRight", paddingRight);
        frag.setArguments (args);
        return frag;
    }

    @Override
    public void onSaveInstanceState (Bundle savedInstanceState)
    {
        super.onSaveInstanceState (savedInstanceState);
        savedInstanceState.putParcelable ("mRedeemBean", mRedeemBean);
        savedInstanceState.putInt("mPaddingTop", mPaddingTop);
        savedInstanceState.putInt("mPaddingBottom", mPaddingBottom);
        savedInstanceState.putInt("mPaddingLeft", mPaddingLeft);
        savedInstanceState.putInt("mPaddingRight", mPaddingRight);
    }

    @Override
    public boolean onBackPressed(boolean actualBackButton)
    {
        return false;
    }

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate (savedInstanceState);

        if (savedInstanceState != null)
        {
            mRedeemBean = savedInstanceState.getParcelable ("mRedeemBean");
            mPaddingTop = savedInstanceState.getInt("mPaddingTop");
            mPaddingBottom = savedInstanceState.getInt("mPaddingBottom");
            mPaddingLeft = savedInstanceState.getInt("mPaddingLeft");
            mPaddingRight = savedInstanceState.getInt("mPaddingRight");
        }
        else if (getArguments () != null)
        {
            mRedeemBean = getArguments ().getParcelable ("redeemBean");
            mPaddingTop = getArguments().getInt("paddingTop");
            mPaddingBottom = getArguments().getInt("paddingBottom");
            mPaddingLeft = getArguments().getInt("paddingLeft");
            mPaddingRight = getArguments().getInt("paddingRight");
        }
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate (R.layout.redeem_fragment, container, false);
        scanButton = v.findViewById (R.id.scan_button);
        redeemButton = v.findViewById (R.id.redeem_button);
        rewardCardCodeEditText = v.findViewById (R.id.reward_card_code);
        messageTextView = v.findViewById (R.id.message);

        return v;
    }

    @Override
    public void updateTitle(StudentBean studentBean)
    {
        String titleText = studentBean.getActivityTitle(ActivityOptionBean.ACTIVITY_OPTION_INCENTIVES, ActivityOptionBean.ACTIVITY_OPTION_INCENTIVES_REDEEM, getResources().getString(R.string.screen_title_redeem));
        ((KazActivity) getActivity()).setActionBarTitle(titleText, null, false, R.id.nav_redeem);
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState)
    {
        super.onViewCreated (view, savedInstanceState);

        view.setOnTouchListener (new View.OnTouchListener ()
        {
            @SuppressLint ("ClickableViewAccessibility")
            @Override
            public boolean onTouch (View v, MotionEvent event)
            {
                return true;
            }
        });

        scanButton.setOnClickListener (new View.OnClickListener ()
        {
            @Override
            public void onClick (View view)
            {
                Intent intent = new Intent (getContext (), BarcodeCaptureActivity.class);
                startActivityForResult (intent, RC_BARCODE_CAPTURE);
            }
        });

        rewardCardCodeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener ()
        {
            @Override
            public boolean onEditorAction (TextView textView, int i, KeyEvent keyEvent)
            {
                if (i == EditorInfo.IME_ACTION_GO)
                {
                    closeKeyboard ();
                    redeem (rewardCardCodeEditText.getText ().toString ());
                }
                return false;
            }
        });

        redeemButton.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick (View view)
            {
                closeKeyboard ();
                redeem (rewardCardCodeEditText.getText ().toString ());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (readQrCodeSuccessful (requestCode, resultCode, data))
        {
            Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);

            Log.d(TAG, "Status: " + R.string.barcode_success);
            Log.d(TAG, "Barcode read: " + barcode.displayValue);

            redeem (barcode.displayValue);
        }
    }

    @Override
    public void onRedeemLoaded(RedeemBean result, Exception e)
    {
        String message;
        switch (result.redeemStatus)
        {
            case "successful":
                message = "You've earned " + result.starAmount + " stars!";
                break;
            case "invalid":
                message = "Invalid code.";
                break;
            case "used":
                message = "This code has been redeemed.";
                break;
            default:
                message = "There was an error redeeming the code";
                break;
        }
        messageTextView.setText (message);
    }

    private void closeKeyboard ()
    {
        if (getContext () != null)
        {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext ().getSystemService (Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow (rewardCardCodeEditText.getWindowToken (), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
    }

    private boolean readQrCodeSuccessful (int requestCode, int resultCode, Intent data)
    {
        return requestCode == RC_BARCODE_CAPTURE && resultCode == CommonStatusCodes.SUCCESS && data != null;
    }

    private void redeem (String rewardCardCode)
    {
        if (getActivity () != null)
        {
            Bundle args = new Bundle (1);
            args.putString ("reward_card_code", rewardCardCode);
            RedeemTaskLoader taskLoader = new RedeemTaskLoader (RedeemFragment.this, R.string.url_reward_card_redeem);
            TaskRunner.execute (R.integer.task_post_redeem_reward_card_code, 0, getFragmentManager (), LoaderManager.getInstance (getActivity ()), taskLoader, taskLoader, true, args);
        }
    }
}
