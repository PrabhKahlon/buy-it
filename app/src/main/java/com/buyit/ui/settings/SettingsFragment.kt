package com.buyit.ui.settings

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.buyit.R
import java.util.*


class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLanguageButton()
        setupCurrencyButton()
        setupThemeButton()

    }

    fun setupLanguageButton(){
        var pref = findPreference<Preference>("language")


        pref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val sharedPrefs = context?.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
            var editSharedPrefs = sharedPrefs?.edit();


            var builder = AlertDialog.Builder(context);
            builder.setTitle(R.string.settings_language_prompt);
            var units = arrayOf("English", "French")

            var previouschecked = sharedPrefs?.getInt("languageUnit", -1)
            if(previouschecked == -1) {
                when(Locale.getDefault().language) {
                    "en" -> previouschecked = 0
                    "fr" -> previouschecked = 1
                }
                editSharedPrefs?.putInt("languageUnit", previouschecked);
                editSharedPrefs?.commit();
                activity?.recreate()
            }
            builder.setSingleChoiceItems(units,
                previouschecked!!, DialogInterface.OnClickListener { dialogInterface, position ->
                    editSharedPrefs?.putInt("languageUnitNotSaved", position);
                    editSharedPrefs?.commit();
                })

            builder.setPositiveButton(R.string.confirm, DialogInterface.OnClickListener{ dialog, which ->
                var selectedUnit = sharedPrefs?.getInt("languageUnitNotSaved", -1)
                editSharedPrefs?.putInt("languageUnit", selectedUnit!!);
                val locale = when(selectedUnit) {
                    0 -> "en"
                    1 -> "fr"
                    else -> "en"
                }
                editSharedPrefs?.putString("languageLocale", locale);
                editSharedPrefs?.commit();
                activity?.recreate()
            })
            builder.setNegativeButton(R.string.cancel, DialogInterface.OnClickListener{ dialog, which -> dialog.cancel()})
            builder.show()

            true;
        }

    }

    fun setupCurrencyButton(){
        var pref = findPreference<Preference>("currency")

        pref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val sharedPrefs = context?.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
            var editSharedPrefs = sharedPrefs?.edit();


            var builder = AlertDialog.Builder(context);
            builder.setTitle(R.string.settings_currency_prompt);
            var units = arrayOf("USD", "CAD", "EUR", "GBP", "AUD")

            var previouschecked = sharedPrefs?.getInt("currencyUnit", -1)
            if(previouschecked == -1) {
                previouschecked = 0
                editSharedPrefs?.putInt("currencyUnit", previouschecked);
                editSharedPrefs?.putString("currencyUnitString", units[previouschecked]);
                editSharedPrefs?.commit();
            }
            builder.setSingleChoiceItems(units,
                previouschecked!!, DialogInterface.OnClickListener { dialogInterface, position ->
                    editSharedPrefs?.putInt("currencyUnitNotSaved", position);
                    editSharedPrefs?.commit();
                })

            builder.setPositiveButton(R.string.confirm, DialogInterface.OnClickListener{ dialog, which ->
                var selectedUnit = sharedPrefs?.getInt("currencyUnitNotSaved", -1)
                editSharedPrefs?.putInt("currencyUnit", selectedUnit!!);
                editSharedPrefs?.putString("currencyUnitString", units[selectedUnit!!]);
                editSharedPrefs?.commit();
            })
            builder.setNegativeButton(R.string.cancel, DialogInterface.OnClickListener{ dialog, which -> dialog.cancel()})
            builder.show()

            true;
        }


    }

    fun setupThemeButton(){
        var pref = findPreference<Preference>("theme")

        pref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val sharedPrefs = context?.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
            var editSharedPrefs = sharedPrefs?.edit();


            var builder = AlertDialog.Builder(context);
            builder.setTitle(R.string.settings_theme_prompt);
            var units = resources.getStringArray(R.array.theme_list)

            var previouschecked = sharedPrefs?.getInt("themeUnit", -1)?.toInt()
            if(previouschecked == -1) {
                when (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_NO -> previouschecked = 0
                    Configuration.UI_MODE_NIGHT_YES -> previouschecked = 1
                }

                editSharedPrefs?.putInt("themeUnit", previouschecked);
                editSharedPrefs?.commit();
                activity?.recreate()
            }
            builder.setSingleChoiceItems(units,
                previouschecked!!, DialogInterface.OnClickListener { dialogInterface, position ->
                    editSharedPrefs?.putInt("themeUnitNotSaved", position);
                    editSharedPrefs?.commit();
                })

            builder.setPositiveButton(R.string.confirm, DialogInterface.OnClickListener{ dialog, which ->
                var selectedUnit = sharedPrefs?.getInt("themeUnitNotSaved", -1)
                editSharedPrefs?.putInt("themeUnit", selectedUnit!!);
                editSharedPrefs?.commit();
                activity?.recreate()
            })
            builder.setNegativeButton(R.string.cancel, DialogInterface.OnClickListener{ dialog, which -> dialog.cancel()})
            builder.show()

            true;
        }


    }
}