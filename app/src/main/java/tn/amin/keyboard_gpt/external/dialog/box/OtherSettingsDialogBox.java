package tn.amin.keyboard_gpt.external.dialog.box;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.Arrays;

import tn.amin.keyboard_gpt.R;
import tn.amin.keyboard_gpt.external.ConfigContainer;
import tn.amin.keyboard_gpt.external.dialog.DialogBoxManager;
import tn.amin.keyboard_gpt.external.dialog.DialogType;
import tn.amin.keyboard_gpt.settings.OtherSettingsType;
import tn.amin.keyboard_gpt.ui.UiInteractor;

public class OtherSettingsDialogBox extends DialogBox {
    private final Bundle mOtherSettingsInput;
    private Adapter mAdapter;

    public OtherSettingsDialogBox(DialogBoxManager dialogManager, Activity parent,
                                  Bundle inputBundle, ConfigContainer configContainer) {
        super(dialogManager, parent, inputBundle, configContainer);
        mOtherSettingsInput = getInput().getBundle(UiInteractor.EXTRA_OTHER_SETTINGS);
    }

    @Override
    protected Dialog build() {
        mAdapter = new Adapter();
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Other Settings")
                .setAdapter(mAdapter, (d, which) -> {

                })
                .setNegativeButton("Cancel", (d, which) -> {
                    switchToDialog(DialogType.Settings);
                })
                .setPositiveButton("Ok", (d, which) -> {
                    getDialog().dismiss();
                })
                .create();
        alertDialog.getListView().setOnItemClickListener((parent, view, position, id) -> {});
        return alertDialog;
    }

    private boolean getBoolean(OtherSettingsType type) {

        if (mOtherSettingsInput.containsKey(type.name())) {
            return mOtherSettingsInput.getBoolean(type.name());
        } else {
            return (Boolean) type.defaultValue;
        }
    }

    private class Adapter implements ListAdapter {
        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public int getCount() {
            return OtherSettingsType.values().length;
        }

        @Override
        public Object getItem(int position) {
            return OtherSettingsType.values()[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return ((OtherSettingsType) getItem(position)).nature.ordinal();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            OtherSettingsType type = (OtherSettingsType) getItem(position);
            View view;
            switch (type.nature) {
                case Boolean:
                    view = getParent().getLayoutInflater().inflate(
                            R.layout.listview_item_checkbox, parent, false);
                    TextView titleView = view.findViewById(R.id.text_title);
                    TextView descView = view.findViewById(R.id.text_desc);
                    CheckBox checkBox = view.findViewById(R.id.checkbox);
                    titleView.setText(type.title);
                    descView.setText(type.description);
                    checkBox.setChecked(getBoolean(type));
                    view.setOnClickListener(v -> {
                        checkBox.setChecked(!checkBox.isChecked());
                        getConfig().otherExtras.putBoolean(type.name(), checkBox.isChecked());
                    });
                    break;

                default:
                    view = new TextView(getContext());
                    ((TextView) view).setText("Unknown");
                    break;
            }
            return view;
        }

        @Override
        public int getViewTypeCount() {
            return OtherSettingsType.Nature.values().length;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {

        }
    }
}
