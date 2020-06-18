package id.ac.stiki.doleno.digipub;

import android.annotation.TargetApi;

public class Constants {
    public interface ACTION {
        String STARTFOREGROUND_ACTION =
                "id.ac.stiki.doleno.digipub.action.startforeground";
        String STOPFOREGROUND_ACTION =
                "id.ac.stiki.doleno.digipub.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
        int TEMPERATURE_ALERT = 102;
        int ROTATION_ALERT = 103;
        int CAMERA_ALERT = 104;
    }

    @TargetApi(26)
    public interface CHANNEL_ID {
        String MAIN_CHANNEL =
                "id.ac.stiki.doleno.digipub.mainChannel";
        String WARNING_CHANNEL =
                "id.ac.stiki.doleno.digipub.warningChannel";
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
