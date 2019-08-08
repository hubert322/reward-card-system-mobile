package com.learninga_z.onyourown.student.rewardcard.beans;

import android.os.Parcel;
import android.os.Parcelable;

import com.learninga_z.lazlibrary.LazException;
import com.learninga_z.lazlibrary.data.LazJsonBean;

import org.json.JSONException;
import org.json.JSONObject;

public class RedeemBean implements Parcelable, LazJsonBean
{
    public String redeemStatus;
    public int starAmount;

    @Override
    public void populateFromJson (JSONObject json, Object... args) throws LazException.LazJsonException
    {
        try
        {
            redeemStatus = json.getString ("redeemStatus");
            starAmount = json.getInt ("starAmount");
        }
        catch (JSONException e)
        {
            //throw new LazException.LazJsonException (e);
            redeemStatus = "";
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeString (redeemStatus);
        out.writeInt (starAmount);
    }

    public static final Creator<RedeemBean> CREATOR = new Creator<RedeemBean>()
    {
        @Override
        public RedeemBean createFromParcel (Parcel in) {
            return new RedeemBean (in);
        }

        @Override
        public RedeemBean[] newArray (int size) {
            return new RedeemBean[size];
        }
    };

    private RedeemBean (Parcel in)
    {
        redeemStatus = in.readString ();
        starAmount = in.readInt ();
    }

    public RedeemBean ()
    {

    }
}
