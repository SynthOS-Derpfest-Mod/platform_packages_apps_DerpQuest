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

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import com.derpquest.settings.preferences.SystemSettingSwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

@SearchIndexable
public class QSColorSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String USE_WALL = "qs_panel_bg_use_wall";
    private static final String USE_ACCENT = "qs_panel_bg_use_accent";
    private static final String PANEL_COLOR = "qs_panel_color";

    private static final int DEFAULT_QS_PANEL_COLOR = 0xffffffff;

    private SystemSettingSwitchPreference mUseWall;
    private SystemSettingSwitchPreference mUseAccent;
    private ColorPickerPreference mColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.derpquest_settings_qs_color);
        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mUseWall = (SystemSettingSwitchPreference) findPreference(USE_WALL);
        mUseAccent = (SystemSettingSwitchPreference) findPreference(USE_ACCENT);
        mColor = (ColorPickerPreference) findPreference(PANEL_COLOR);
        mColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getIntForUser(resolver,
                Settings.System.QS_PANEL_BG_COLOR, DEFAULT_QS_PANEL_COLOR, UserHandle.USER_CURRENT);
        String hexColor = String.format("#%08x", (0xffffffff & intColor));
        mColor.setSummary(hexColor);
        mColor.setNewPreviewColor(intColor);

        // NOTE: Reverse logic
        boolean isEnabled = Settings.System.getInt(resolver,
                Settings.System.QS_PANEL_BG_USE_FW, 1) == 0;
        mUseWall.setEnabled(isEnabled);
        mUseAccent.setEnabled(isEnabled);
        mColor.setEnabled(isEnabled);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = getContentResolver();
        if (preference == mColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putIntForUser(resolver,
                    Settings.System.QS_PANEL_BG_COLOR, intHex, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
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
                    sir.xmlResId = R.xml.derpquest_settings_qs_color;
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
