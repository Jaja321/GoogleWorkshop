package com.googleworkshop.taxipool;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

/**
 * Created by Jerafi on 1/2/2018.
 */

public class PreferencesUtils {
    protected static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    protected static int getNumOfSeconds(int pos){// move to auxiliary class?
        if(pos <= 3){//"15 min", "30 min" or "45 min" selected
            return (pos + 1) * 15 * 60;
        }
        return (pos - 2) * 60 * 60;//"1 hour", "2 hours" or "3 hours" selected
    }
}
