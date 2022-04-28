package com.idarkwizard.calculatorapp.service;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

public class PermissionService {

    public static boolean hasPermissions(Context context, String... allPermissionNeeded) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && allPermissionNeeded != null)
            for (String permission : allPermissionNeeded)
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                    return false;
        return true;
    }

}
