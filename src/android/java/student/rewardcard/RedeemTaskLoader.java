package com.learninga_z.onyourown.student.rewardcard;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

import com.learninga_z.lazlibrary.LazException;
import com.learninga_z.lazlibrary.net.JsonRequester;
import com.learninga_z.lazlibrary.task.TaskLoaderCallbacksInterface;
import com.learninga_z.lazlibrary.task.TaskLoaderInterface;
import com.learninga_z.onyourown.core.activity.KazActivity;
import com.learninga_z.onyourown.student.rewardcard.beans.RedeemBean;

import java.lang.ref.WeakReference;

public class RedeemTaskLoader implements TaskLoaderInterface<RedeemBean>, TaskLoaderCallbacksInterface<RedeemBean>
{
    public interface RedeemTaskListenerInterface
    {
        void onRedeemLoaded (RedeemBean result, Exception e);

        Activity getActivity ();
    }

    public static final String REWARD_CARD_CODE_ARG = "reward_card_code";
    private WeakReference<RedeemTaskListenerInterface> listenerRef;
    private int mRedeemUrlStringId;

    public RedeemTaskLoader (RedeemTaskListenerInterface listener, @StringRes int redeemUrlStringId)
    {
        this.listenerRef = new WeakReference<>(listener);
        this.mRedeemUrlStringId = redeemUrlStringId;
    }

    @Override
    public RedeemBean loadInBackground (Bundle args, AsyncTaskLoader loader) throws LazException.LazJsonException, LazException.LazMaintenanceException, LazException.LazIoException
    {
        RedeemBean redeemBean = null;
        if (args != null)
        {
            String postData = REWARD_CARD_CODE_ARG + "=" + args.getString (RedeemTaskLoader.REWARD_CARD_CODE_ARG);
            redeemBean = JsonRequester.makeJsonRequest (loader, mRedeemUrlStringId, RedeemBean.class, null, false, true, postData, 0);
        }
        return redeemBean;
    }

    @Override
    public void onLoadFinished (Loader loader, RedeemBean result, TaskLoaderInterface<RedeemBean> taskLoader)
    {
        RedeemTaskListenerInterface listener = listenerRef == null ? null : listenerRef.get ();
        if (listener != null)
        {
            listener.onRedeemLoaded (result, null);
        }
    }

    @Override
    public void onLoadFailed (Loader loader, Exception e, TaskLoaderInterface<RedeemBean> taskLoader)
    {
        RedeemTaskListenerInterface listener = listenerRef == null ? null : listenerRef.get ();
        if (listener != null)
        {
            KazActivity activity = (KazActivity) listener.getActivity ();
            if (activity != null)
            {
                activity.doDefaultExceptionHandling (e);
                listener.onRedeemLoaded (null, e);
            }
        }
    }

    @Override
    public void onLoadCanceled(Loader loader, TaskLoaderInterface<RedeemBean> taskLoader)
    {

    }
}
