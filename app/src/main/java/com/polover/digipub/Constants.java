package com.polover.digipub;

import android.annotation.TargetApi;

public class Constants {
    public interface ACTION {
        String STARTFOREGROUND_ACTION =
                "com.polover.gurd.action.startforeground";
        String STOPFOREGROUND_ACTION =
                "com.polover.gurd.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
        int TEMPERATURE_ALERT = 102;
    }

    @TargetApi(26)
    public interface CHANNEL_ID {
        String MAIN_CHANNEL =
                "com.polover.gurd.mainChannel";
        String WARNING_CHANNEL =
                "com.polover.gurd.warningChannel";
    }

    public interface MEASURING_UNIT {
        int CELSIUS = 0;
        int FAHRENHEIT = 1;
    }

    public interface DEFAULT_VALUES {
        String MEASURING_UNIT = Model.UserInfo.getDefaultMeasuringUnit();
        String WARNING_TEMPERATURE = Model.UserInfo.getDefaultWarningTemperature();
    }
}
