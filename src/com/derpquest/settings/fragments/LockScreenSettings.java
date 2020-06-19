/*
 * Copyright (C) 2020 DerpFest ROM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.derpquest.settings.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import com.derpquest.settings.preferences.CustomSeekBarPreference;
import com.derpquest.settings.preferences.SecureSettingMasterSwitchPreference;
import com.derpquest.settings.preferences.SystemSettingListPreference;
import com.derpquest.settings.preferences.SystemSettingMasterSwitchPreference;
import com.derpquest.settings.preferences.SystemSettingEditTextPreference;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

@SearchIndexable
public class LockScreenSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String FINGERPRINT_VIB = "fingerprint_success_vib";
    private static final String FOD_ICON_PICKER_CATEGORY = "fod_icon_picker";
    private static final String FOD_PRESSED_STATE = "fod_pressed_state";
    private static final String FOD_SOLID_COLOR = "fod_solid_color";
    private static final String KEY_PULSE_BRIGHTNESS = "ambient_pulse_brightness";
    private static final String KEY_DOZE_BRIGHTNESS = "ambient_doze_brightness";

    private static final String LOCKSCREEN_BATTERY_INFO = "lockscreen_battery_info";
    private static final String BATTERY_BAR = "sysui_keyguard_show_battery_bar";
    private static final String LOCKSCREEN_CLOCK = "lockscreen_clock";
    private static final String LOCKSCREEN_INFO = "lockscreen_info";
    private static final String MEDIA_ART = "lockscreen_media_metadata";
    private static final String LOCKSCREEN_VISUALIZER_ENABLED = "lockscreen_visualizer_enabled";
    private static final String LOCKSCREEN_MAX_NOTIF_CONFIG = "lockscreen_max_notif_cofig";

    private static final String SYNTHOS_AMBIENT_TEXT_STRING = "synthos_ambient_text_string";
    private static final String SYNTHOS_AMBIENT_TEXT_ALIGNMENT = "synthos_ambient_text_alignment";
    private static final String SYNTHOS_AMBIENT_TEXT_FONT = "synthos_ambient_text_font";
    private static final String SYNTHOS_AMBIENT_TEXT_TYPE_COLOR = "synthos_ambient_text_type_color";
    private static final String SYNTHOS_AMBIENT_TEXT_COLOR = "synthos_ambient_text_color";
    private static final String FILE_AMBIENT_SELECT = "file_ambient_select";
    private static final String FILE_AMBIENT_VIDEO_SELECT = "file_ambient_video_select";

    private static final int REQUEST_PICK_IMAGE = 0;
    private static final int REQUEST_PICK_VIDEO = 1;

    private CustomSeekBarPreference mPulseBrightness;
    private CustomSeekBarPreference mDozeBrightness;
    private CustomSeekBarPreference mMaxKeyguardNotifConfig;

    private SystemSettingMasterSwitchPreference mBatteryInfo;
    private SystemSettingMasterSwitchPreference mBatteryBar;
    private SystemSettingMasterSwitchPreference mClockEnabled;
    private SystemSettingMasterSwitchPreference mInfoEnabled;
    private SystemSettingMasterSwitchPreference mMediaArt;
    private SecureSettingMasterSwitchPreference mVisualizerEnabled;

    private SystemSettingEditTextPreference mAmbientText;
    private ListPreference mAmbientTextAlign;
    private ListPreference mAmbientTextFonts;
    private ListPreference mAmbientTextTypeColor;
    private ColorPickerPreference mAmbientTextColor;
    private Preference mAmbientImage;
    private Preference mAmbientVideo;

    private FingerprintManager mFingerprintManager;
    private PreferenceCategory mFODIconPickerCategory;
    private SystemSettingListPreference mFODPressedState;
    private ColorPickerPreference mFODIconColor;
    private SwitchPreference mFingerprintVib;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.derpquest_settings_lockscreen);
        final PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        Resources resources = getResources();

        mBatteryInfo = (SystemSettingMasterSwitchPreference)
                findPreference(LOCKSCREEN_BATTERY_INFO);
        mBatteryInfo.setOnPreferenceChangeListener(this);
        boolean enabled = Settings.System.getInt(resolver,
                LOCKSCREEN_BATTERY_INFO, 1) == 1;
        mBatteryInfo.setChecked(enabled);

        mBatteryBar = (SystemSettingMasterSwitchPreference) findPreference(BATTERY_BAR);
        mBatteryBar.setOnPreferenceChangeListener(this);
        enabled = Settings.System.getInt(resolver,
                BATTERY_BAR, 0) == 1;
        mBatteryBar.setChecked(enabled);

        mClockEnabled = (SystemSettingMasterSwitchPreference) findPreference(LOCKSCREEN_CLOCK);
        mClockEnabled.setOnPreferenceChangeListener(this);
        int clockEnabled = Settings.System.getInt(resolver,
                LOCKSCREEN_CLOCK, 1);
        mClockEnabled.setChecked(clockEnabled != 0);

        mInfoEnabled = (SystemSettingMasterSwitchPreference) findPreference(LOCKSCREEN_INFO);
        mInfoEnabled.setOnPreferenceChangeListener(this);
        enabled = Settings.System.getInt(resolver,
                LOCKSCREEN_INFO, 1) != 0;
        mInfoEnabled.setChecked(enabled);
        mInfoEnabled.setEnabled(clockEnabled != 0);

        mMediaArt = (SystemSettingMasterSwitchPreference) findPreference(MEDIA_ART);
        mMediaArt.setOnPreferenceChangeListener(this);
        enabled = Settings.System.getInt(resolver,
                MEDIA_ART, 1) == 1;
        mMediaArt.setChecked(enabled);

        mVisualizerEnabled = (SecureSettingMasterSwitchPreference) findPreference(LOCKSCREEN_VISUALIZER_ENABLED);
        mVisualizerEnabled.setOnPreferenceChangeListener(this);
        enabled = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_VISUALIZER_ENABLED, 0) == 1;
        mVisualizerEnabled.setChecked(enabled);

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintVib = (SwitchPreference) findPreference(FINGERPRINT_VIB);
        if (mFingerprintManager == null) {
            prefScreen.removePreference(mFingerprintVib);
        } else {
            mFingerprintVib.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.FINGERPRINT_SUCCESS_VIB, 1) == 1));
            mFingerprintVib.setOnPreferenceChangeListener(this);
        }

        int defaultDoze = resources.getInteger(
                com.android.internal.R.integer.config_screenBrightnessDoze);
        int defaultPulse = resources.getInteger(
                com.android.internal.R.integer.config_screenBrightnessPulse);
        if (defaultPulse == -1) {
            defaultPulse = defaultDoze;
        }

        mPulseBrightness = (CustomSeekBarPreference) findPreference(KEY_PULSE_BRIGHTNESS);
        int value = Settings.System.getInt(getContentResolver(),
                Settings.System.PULSE_BRIGHTNESS, defaultPulse);
        mPulseBrightness.setValue(value);
        mPulseBrightness.setOnPreferenceChangeListener(this);

        mDozeBrightness = (CustomSeekBarPreference) findPreference(KEY_DOZE_BRIGHTNESS);
        value = Settings.System.getInt(getContentResolver(),
                Settings.System.DOZE_BRIGHTNESS, defaultDoze);
        mDozeBrightness.setValue(value);
        mDozeBrightness.setOnPreferenceChangeListener(this);

        mFODPressedState = (SystemSettingListPreference) findPreference(FOD_PRESSED_STATE);
        value = Settings.System.getInt(getContentResolver(),
                Settings.System.FOD_PRESSED_STATE, 0);
        mFODPressedState.setValue(String.valueOf(value));
        mFODPressedState.setOnPreferenceChangeListener(this);

        mFODIconColor = (ColorPickerPreference) findPreference(FOD_SOLID_COLOR);
        mFODIconColor.setEnabled(value == 2); // if mFODPressedState index == 2
        try {
            Resources res = getContext().getPackageManager().getResourcesForApplication(
                    "com.android.systemui");
            int defaultColor = res.getColor(res.getIdentifier(
                    "com.android.systemui:color/config_fodColor", null, null));
            mFODIconColor.setDefaultColor(defaultColor);
            value = Settings.System.getInt(getContentResolver(),
                    Settings.System.FOD_SOLID_COLOR, defaultColor);
            mFODIconColor.setNewPreviewColor(value);
            mFODIconColor.setOnPreferenceChangeListener(this);
        } catch (Exception e) {
            e.printStackTrace();
            mFODIconColor.setEnabled(false);
        }

        mFODIconPickerCategory = (PreferenceCategory)
                findPreference(FOD_ICON_PICKER_CATEGORY);
        if (mFODIconPickerCategory != null
                && !getResources().getBoolean(com.android.internal.R.bool.config_needCustomFODView))
            prefScreen.removePreference(mFODIconPickerCategory);

        mAmbientText = (SystemSettingEditTextPreference) findPreference(SYNTHOS_AMBIENT_TEXT_STRING);
        mAmbientText.setOnPreferenceChangeListener(this);

        // set ambient text alignment
        mAmbientTextAlign = (ListPreference) findPreference(SYNTHOS_AMBIENT_TEXT_ALIGNMENT);
        int align = Settings.System.getInt(resolver,
                Settings.System.SYNTHOS_AMBIENT_TEXT_ALIGNMENT, 3);
        mAmbientTextAlign.setValue(String.valueOf(align));
        mAmbientTextAlign.setSummary(mAmbientTextAlign.getEntry());
        mAmbientTextAlign.setOnPreferenceChangeListener(this);

        // ambient text Fonts
        mAmbientTextFonts = (ListPreference) findPreference(SYNTHOS_AMBIENT_TEXT_FONT);
        mAmbientTextFonts.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.SYNTHOS_AMBIENT_TEXT_FONT, 8)));
        mAmbientTextFonts.setSummary(mAmbientTextFonts.getEntry());
        mAmbientTextFonts.setOnPreferenceChangeListener(this);

        // ambient text color type
        mAmbientTextTypeColor = (ListPreference) findPreference(SYNTHOS_AMBIENT_TEXT_TYPE_COLOR);
        mAmbientTextTypeColor.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.SYNTHOS_AMBIENT_TEXT_TYPE_COLOR, 0)));
        mAmbientTextTypeColor.setSummary(mAmbientTextTypeColor.getEntry());
        mAmbientTextTypeColor.setOnPreferenceChangeListener(this);

        mAmbientTextColor = (ColorPickerPreference) findPreference(SYNTHOS_AMBIENT_TEXT_COLOR);
        mAmbientTextColor.setOnPreferenceChangeListener(this);
        int ambientTextColor = Settings.System.getInt(getContentResolver(),
                Settings.System.SYNTHOS_AMBIENT_TEXT_COLOR, 0xFF3980FF);
        String ambientTextColorHex = String.format("#%08x", (0xFF3980FF & ambientTextColor));
        if (ambientTextColorHex.equals("#ff3980ff")) {
            mAmbientTextColor.setSummary(R.string.default_string);
        } else {
            mAmbientTextColor.setSummary(ambientTextColorHex);
        }
        mAmbientTextColor.setNewPreviewColor(ambientTextColor);

        mMaxKeyguardNotifConfig = (CustomSeekBarPreference) findPreference(LOCKSCREEN_MAX_NOTIF_CONFIG);
        int kgconf = Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_MAX_NOTIF_CONFIG, 3);
        mMaxKeyguardNotifConfig.setValue(kgconf);
        mMaxKeyguardNotifConfig.setOnPreferenceChangeListener(this);

        mAmbientImage = findPreference(FILE_AMBIENT_SELECT);
        mAmbientVideo = findPreference(FILE_AMBIENT_VIDEO_SELECT);

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mAmbientImage) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
            return true;
        } else if (preference == mAmbientVideo) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("video/*");
            startActivityForResult(intent, REQUEST_PICK_VIDEO);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mBatteryInfo) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
		            LOCKSCREEN_BATTERY_INFO, value ? 1 : 0);
            return true;
        } else if (preference == mBatteryBar) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
		            BATTERY_BAR, value ? 1 : 0);
            return true;
        } else if (preference == mClockEnabled) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
		            LOCKSCREEN_CLOCK, value ? 1 : 0);
            mInfoEnabled.setEnabled(value);
            return true;
        } else if (preference == mInfoEnabled) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
		            LOCKSCREEN_INFO, value ? 1 : 0);
            return true;
        } else if (preference == mMediaArt) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
                MEDIA_ART, value ? 1 : 0);
            return true;
        } else if (preference == mFingerprintVib) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.FINGERPRINT_SUCCESS_VIB, value ? 1 : 0);
            return true;
        } else if (preference == mVisualizerEnabled) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(resolver,
                    LOCKSCREEN_VISUALIZER_ENABLED, value ? 1 : 0);
            return true;
        } else if (preference == mPulseBrightness) {
            int value = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PULSE_BRIGHTNESS, value);
            return true;
        } else if (preference == mDozeBrightness) {
            int value = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.DOZE_BRIGHTNESS, value);
            return true;
        } else if (preference == mFODPressedState) {
            int value = Integer.valueOf((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.FOD_PRESSED_STATE, value);
            mFODIconColor.setEnabled(value == 2);
            return true;
        } else if (preference == mFODIconColor) {
            int value = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.FOD_SOLID_COLOR, value);
            return true;
        } else if (preference == mAmbientText) {
            String value = (String) newValue;
            Settings.System.putString(resolver,
                    Settings.System.SYNTHOS_AMBIENT_TEXT_STRING, value);
            return true;
        } else if (preference == mAmbientTextAlign) {
            int align = Integer.valueOf((String) newValue);
            int index = mAmbientTextAlign.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SYNTHOS_AMBIENT_TEXT_ALIGNMENT, align);
            mAmbientTextAlign.setSummary(mAmbientTextAlign.getEntries()[index]);
            return true;
        } else if (preference == mAmbientTextFonts) {
            Settings.System.putInt(getContentResolver(), Settings.System.SYNTHOS_AMBIENT_TEXT_FONT,
                    Integer.valueOf((String) newValue));
            mAmbientTextFonts.setValue(String.valueOf(newValue));
            mAmbientTextFonts.setSummary(mAmbientTextFonts.getEntry());
            return true;
        } else if (preference == mAmbientTextTypeColor) {
            Settings.System.putInt(getContentResolver(), Settings.System.SYNTHOS_AMBIENT_TEXT_TYPE_COLOR,
                    Integer.valueOf((String) newValue));
            mAmbientTextTypeColor.setValue(String.valueOf(newValue));
            mAmbientTextTypeColor.setSummary(mAmbientTextTypeColor.getEntry());
            return true;
        } else if (preference == mAmbientTextColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ff3980ff")) {
                preference.setSummary(R.string.default_string);
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SYNTHOS_AMBIENT_TEXT_COLOR, intHex);
            return true;
        } else if (preference == mMaxKeyguardNotifConfig) {
            int kgconf = (Integer) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_MAX_NOTIF_CONFIG, kgconf);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == REQUEST_PICK_IMAGE) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }
            final Uri imageUri = result.getData();
            Settings.System.putString(getContentResolver(), Settings.System.SYNTHOS_AMBIENT_CUSTOM_IMAGE, imageUri.toString());
        } else if (requestCode == REQUEST_PICK_VIDEO) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }
            final Uri videoUri = result.getData();
            Settings.System.putString(getContentResolver(), Settings.System.SYNTHOS_AMBIENT_CUSTOM_VIDEO, videoUri.toString());
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.OWLSNEST;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                     SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.derpquest_settings_lockscreen;
                    result.add(sir);
                    return result;
                }
                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
    };
}
