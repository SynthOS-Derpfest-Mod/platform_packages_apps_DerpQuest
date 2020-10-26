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

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class VolumeSettings extends SettingsPreferenceFragment
         implements Preference.OnPreferenceChangeListener, Indexable {

    private ListPreference mVolumeAlignment;
    private ColorPickerPreference mColor;
    private Preference mImage;
    private ListPreference mBackgroundTypeList;
    private static final String SYSTEMUI_PLUGIN_VOLUME = "systemui_plugin_volume";
    private static final String VOLUME_PANEL_ALIGNMENT = "volume_panel_alignment";
    private static final String SYNTHOS_VOLUME_PANEL_BACKGROUND_COLOR = "synthos_volume_panel_background_color";
    private static final String SYNTHOS_VOLUME_PANEL_BACKGROUND_TYPE = "synthos_volume_panel_background_type";
    private static final String FILE_VOLUME_SELECT = "file_volume_select";
    private static final int REQUEST_PICK_IMAGE = 0;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.derpquest_settings_volume);

        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        Resources resources = getResources();

        // set volume alignment
        mVolumeAlignment = (ListPreference) findPreference(VOLUME_PANEL_ALIGNMENT);
        int align = Settings.System.getInt(resolver,
                Settings.System.VOLUME_PANEL_ALIGNMENT, 1);
        mVolumeAlignment.setValue(String.valueOf(align));
        mVolumeAlignment.setSummary(mVolumeAlignment.getEntry());
        mVolumeAlignment.setOnPreferenceChangeListener(this);

        // set volume background color
        mColor = (ColorPickerPreference) findPreference(SYNTHOS_VOLUME_PANEL_BACKGROUND_COLOR);
        mColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getIntForUser(resolver,
                Settings.System.SYNTHOS_VOLUME_PANEL_BACKGROUND_COLOR, 0xffffffff, UserHandle.USER_CURRENT);
        String hexColor = String.format("#%08x", (0xffffffff & intColor));
        mColor.setSummary(hexColor);
        mColor.setNewPreviewColor(intColor);

        mBackgroundTypeList = (ListPreference) findPreference(SYNTHOS_VOLUME_PANEL_BACKGROUND_TYPE);
        int bgType = Settings.System.getInt(resolver,
                Settings.System.SYNTHOS_VOLUME_PANEL_BACKGROUND_TYPE, 1);
        mBackgroundTypeList.setValue(String.valueOf(bgType));
        mBackgroundTypeList.setSummary(mBackgroundTypeList.getEntry());
        mBackgroundTypeList.setOnPreferenceChangeListener(this);

        mImage = findPreference(FILE_VOLUME_SELECT);

        setEnabled();

    }

    private void setEnabled() {
        final ContentResolver resolver = getContentResolver();
        int backgroundType = Settings.System.getInt(resolver,
                Settings.System.SYNTHOS_VOLUME_PANEL_BACKGROUND_TYPE, 0);
        mColor.setEnabled(backgroundType == 2);
        mImage.setEnabled(backgroundType == 3);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mImage) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.OWLSNEST;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mVolumeAlignment) {
            int align = Integer.valueOf((String) newValue);
            int index = mVolumeAlignment.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.VOLUME_PANEL_ALIGNMENT, align);
            mVolumeAlignment.setSummary(mVolumeAlignment.getEntries()[index]);
            return true;
        } else if (preference == mColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putIntForUser(resolver,
                    Settings.System.SYNTHOS_VOLUME_PANEL_BACKGROUND_COLOR, intHex, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mBackgroundTypeList) {
            int type = Integer.valueOf((String) newValue);
            int index = mBackgroundTypeList.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SYNTHOS_VOLUME_PANEL_BACKGROUND_TYPE, type);
            mBackgroundTypeList.setSummary(mBackgroundTypeList.getEntries()[index]);
            setEnabled();
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
            Settings.System.putString(getContentResolver(), Settings.System.SYNTHOS_VOLUME_PANEL_BACKGROUND_IMAGE, imageUri.toString());
        }
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
