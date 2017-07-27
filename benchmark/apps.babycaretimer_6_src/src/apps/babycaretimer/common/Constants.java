package apps.babycaretimer.common;

/**
 * This class is a collection of all the constants used in this application.
 * 
 * @author Camille Sévigny
 */
public class Constants {
	
	public static final String BABY_CARE_TIMER_WAKELOCK = "BABY_CARE_TIMER_WAKELOCK";
	public static final String BABY_CARE_TIMER_KEYGUARD = "BABY_CARE_TIMER_KEYGUARD";
	public static final String BLUR_SCREEN_ENABLED_KEY = "blur_screen_background_enabled";
	public static final String DIM_SCREEN_ENABLED_KEY = "dim_screen_background_enabled";
	public static final String DIM_SCREEN_AMOUNT_KEY = "dim_screen_background_amount";	
	public static final String SNOOZE_AMOUNT_KEY = "snooze_amount";	
	public static final String SCREEN_ENABLED_KEY = "screen_enabled";
	public static final String SCREEN_DIM_ENABLED_KEY = "screen_dim_enabled";
	public static final String KEYGUARD_ENABLED_KEY = "keyguard_enabled";
	public static final String SCREEN_TIMEOUT_KEY = "screen_timeout_settings";
	public static final String ALARM_MAX_HOURS_KEY = "alarm_max_hours_settings";
	public static final int DIM_BACKGROUND_AMOUNT = 50;
	
	public static final String HAPTIC_FEEDBACK_ENABLED_KEY = "haptic_feedback_enabled";
	public static final String SECONDS_ENABLED_KEY = "display_seconds_enabled";
	public static final String BLINK_ENABLED_KEY = "blink_colon_enabled";
	public static final String LANDSCAPE_SCREEN_ENABLED_KEY = "landscape_screen_enabled";
	public static final String APP_THEME_KEY = "app_theme";
	
	public static final String SLEEP_TOTAL_ELAPSED_TIME_KEY = "total_elapsed_time_sleep";

	public static final String CONFIRM_RESET_COUNTERS_KEY = "confirm_reset_counters_enabled";
	
	public static final int DIAPER_DIALOG_RESET_COUNTER = 1;
	public static final int BOTTLE_DIALOG_RESET_COUNTER = 2;
	public static final int SLEEP_DIALOG_RESET_COUNTER = 3;
	public static final int CUSTOM_DIALOG_RESET_COUNTER = 4;
	
	public static final int TYPE_DIAPER = 0;
	public static final int TYPE_BOTTLE = 1;
	public static final int TYPE_SLEEP = 2;
	public static final int TYPE_CUSTOM = 3;
	
	public static final String DIAPER_BASE_TIME_KEY = "diaper_base_time";
	public static final String BOTTLE_BASE_TIME_KEY = "bottle_base_time";
	public static final String SLEEP_BASE_TIME_KEY = "sleep_base_time";
	public static final String CUSTOM_BASE_TIME_KEY = "custom_base_time";

	public static final String DIAPER_COUNT_KEY = "diaper_count";
	public static final String BOTTLE_COUNT_KEY = "bottle_count";
	public static final String SLEEP_COUNT_KEY = "sleep_count";
	public static final String CUSTOM_COUNT_KEY = "custom_count";
	
	public static final String DIAPER_ALARM_ACTIVE_KEY = "diaper_alarm_active";
	public static final String BOTTLE_ALARM_ACTIVE_KEY = "bottle_alarm_active";
	public static final String SLEEP_ALARM_ACTIVE_KEY = "sleep_alarm_active";
	public static final String CUSTOM_ALARM_ACTIVE_KEY = "custom_alarm_active";
	
	public static final String DIAPER_ALARM_TIME_KEY = "diaper_alarm_time";
	public static final String BOTTLE_ALARM_TIME_KEY = "bottle_alarm_time";
	public static final String SLEEP_ALARM_TIME_KEY = "sleep_alarm_time";
	public static final String CUSTOM_ALARM_TIME_KEY = "custom_alarm_time";

	public static final String DIAPER_ALARM_SNOOZE_KEY = "diaper_alarm_snooze";
	public static final String BOTTLE_ALARM_SNOOZE_KEY = "bottle_alarm_snooze";
	public static final String SLEEP_ALARM_SNOOZE_KEY = "sleep_alarm_snooze";
	public static final String CUSTOM_ALARM_SNOOZE_KEY = "custom_alarm_snooze";

	public static final String DIAPER_TIMER_ACTIVE_KEY = "diaper_timer_active";
	public static final String BOTTLE_TIMER_ACTIVE_KEY = "bottle_timer_active";
	public static final String SLEEP_TIMER_ACTIVE_KEY = "sleep_timer_active";
	public static final String CUSTOM_TIMER_ACTIVE_KEY = "custom_timer_active";
	
	public static final String DIAPER_TIMER_START_KEY = "diaper_timer_start";
	public static final String BOTTLE_TIMER_START_KEY = "bottle_timer_start";
	public static final String SLEEP_TIMER_START_KEY = "sleep_timer_start";
	public static final String CUSTOM_TIMER_START_KEY = "custom_timer_start";

	public static final String DIAPER_TIMER_OFFSET_KEY = "diaper_timer_offset";
	public static final String BOTTLE_TIMER_OFFSET_KEY = "bottle_timer_offset";
	public static final String SLEEP_TIMER_OFFSET_KEY = "sleep_timer_offset";
	public static final String CUSTOM_TIMER_OFFSET_KEY = "custom_timer_offset";
	
	public static final String DIAPER_TIMER_STOP_KEY = "diaper_timer_stop";
	public static final String BOTTLE_TIMER_STOP_KEY = "bottle_timer_stop";
	public static final String SLEEP_TIMER_STOP_KEY = "sleep_timer_stop";
	public static final String CUSTOM_TIMER_STOP_KEY = "custom_timer_stop";

	public static final String DIAPER_COUNT_DATE_KEY = "diaper_count_date";
	public static final String BOTTLE_COUNT_DATE_KEY = "bottle_count_date";
	public static final String SLEEP_COUNT_DATE_KEY = "sleep_count_date";
	public static final String CUSTOM_COUNT_DATE_KEY = "custom_count_date";
	
	public static final String DIAPER_ALARM_RECURRING_KEY = "diaper_alarm_recurring";
	public static final String BOTTLE_ALARM_RECURRING_KEY = "bottle_alarm_recurring";
	public static final String SLEEP_ALARM_RECURRING_KEY = "sleep_alarm_recurring";
	public static final String CUSTOM_ALARM_RECURRING_KEY = "custom_alarm_recurring";
	
    public static final String DIAPER_START_TIME_KEY = "diaper_start_time";
    public static final String BOTTLE_START_TIME_KEY = "bottle_start_time";
    public static final String SLEEP_START_TIME_KEY = "sleep_start_time";
    public static final String CUSTOM_START_TIME_KEY = "custom_start_time";
	
	public static final String ALARM_TYPE = "alarm_type";
	public static final String ALARM_TIME = "alarm_time";
	public static final String ALARM_SNOOZE = "alarm_snooze";
	public static final String BREAST_FEEDING_SIDE_KEY = "breast_feeding_side";

	public static final String ALARM_NOTIFICATION_COUNT_KEY = "alarm_notification_count";
	
	public static final String ALARM_NOTIFICATIONS_ENABLED_KEY = "status_bar_notifications_enabled";
	
	public static final String ALARM_NOTIFICATION_SOUND_KEY = "notification_sound";
	
	public static final String ALARM_NOTIFICATION_VIBRATE_SETTING_KEY = "sms_notification_vibrate_setting";
	public static final String ALARM_NOTIFICATION_VIBRATE_ALWAYS_VALUE = "0";
	public static final String ALARM_NOTIFICATION_VIBRATE_NEVER_VALUE = "1";
	public static final String ALARM_NOTIFICATION_VIBRATE_WHEN_VIBRATE_MODE_VALUE = "2";
	public static final String ALARM_NOTIFICATION_VIBRATE_PATTERN_KEY = "notification_vibrate_pattern";
	public static final String ALARM_NOTIFICATION_VIBRATE_PATTERN_CUSTOM_VALUE_KEY = "custom";
	public static final String ALARM_NOTIFICATION_VIBRATE_PATTERN_CUSTOM_KEY = "notification_vibrate_pattern_custom";
	public static final String ALARM_NOTIFICATION_VIBRATE_DEFAULT = "0,800,200,800,200,800,200";
	
	public static final String ALARM_NOTIFICATION_LED_ENABLED_KEY = "notification_led_enabled";
	public static final String ALARM_NOTIFICATION_LED_PATTERN_KEY = "notification_led_pattern";	
	public static final String ALARM_NOTIFICATION_LED_PATTERN_CUSTOM_VALUE_KEY = "custom";
	public static final String ALARM_NOTIFICATION_LED_PATTERN_CUSTOM_KEY = "notification_led_pattern_custom";
	public static final String ALARM_NOTIFICATION_LED_PATTERN_DEFAULT = "1000,1000";

	public static final String ALARM_NOTIFICATION_LED_COLOR_KEY = "notification_led_color";	
	public static final String ALARM_NOTIFICATION_LED_COLOR_CUSTOM_VALUE_KEY = "custom";
	public static final String ALARM_NOTIFICATION_LED_COLOR_CUSTOM_KEY = "notification_led_color_custom";
	public static final String ALARM_NOTIFICATION_LED_COLOR_DEFAULT = "yellow";
	
	public static final String ALARM_NOTIFICATION_IN_CALL_SOUND_ENABLED_KEY = "notification_in_call_sound_enabled";
	public static final String ALARM_NOTIFICATION_IN_CALL_VIBRATE_ENABLED_KEY = "notification_in_call_vibrate_enabled";
	
}