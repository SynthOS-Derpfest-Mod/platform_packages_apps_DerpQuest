/*
 * Copyright (C) 2017-2020 The PixelDust Project
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
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class VolumeSettings extends SettingsPreferenceFragment
         implements Preference.OnPreferenceChangeListener, Indexable {

    private ListPreference mVolumePanelTheme;
    private ListPreference mVolumeAlignment;
    private static final String SYNTHOS_VOLUME_PANEL_THEME = "synthos_volume_panel_theme";
    private static final String VOLUME_PANEL_ALIGNMENT = "volume_panel_alignment";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.derpquest_settings_volume);

        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        Resources resources = getResources();

        // set volume panel theme
        mVolumePanelTheme = (ListPreference) findPreference(SYNTHOS_VOLUME_PANEL_THEME);
        int style = Settings.System.getInt(resolver,
                Settings.System.SYNTHOS_VOLUME_PANEL_THEME, 0);
        mVolumePanelTheme.setValue(String.valueOf(style));
        mVolumePanelTheme.setSummary(mVolumePanelTheme.getEntry());
        mVolumePanelTheme.setOnPreferenceChangeListener(this);

        // set volume alignment
        mVolumeAlignment = (ListPreference) findPreference(VOLUME_PANEL_ALIGNMENT);
        int align = Settings.System.getInt(resolver,
                Settings.System.VOLUME_PANEL_ALIGNMENT, 1);
        mVolumeAlignment.setValue(String.valueOf(align));
        mVolumeAlignment.setSummary(mVolumeAlignment.getEntry());
        mVolumeAlignment.setOnPreferenceChangeListener(this);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.OWLSNEST;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mVolumePanelTheme) {
            int style = Integer.valueOf((String) newValue);
            int index = mVolumePanelTheme.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SYNTHOS_VOLUME_PANEL_THEME, style);
            mVolumePanelTheme.setSummary(mVolumePanelTheme.getEntries()[index]);
            return true;
        } else if (preference == mVolumeAlignment) {
            int align = Integer.valueOf((String) newValue);
            int index = mVolumeAlignment.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.VOLUME_PANEL_ALIGNMENT, align);
            mVolumeAlignment.setSummary(mVolumeAlignment.getEntries()[index]);
            return true;
        }
        return false;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                                                                            boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.derpquest_settings_volume;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
