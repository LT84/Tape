package com.project.tape.Activities;

import static com.project.tape.Activities.MainActivity.SORT_PREF;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.project.tape.R;


public class SortChoice extends AppCompatActivity {

    ImageButton exitBtnInSort;
    RadioGroup radioGroup;

    private int checkedIndex;

    public static boolean sortChoiceChanged, switchBetweenSorts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sort_choice);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        getSupportActionBar().hide();

        sortChoiceChanged = true;
        switchBetweenSorts = true;

        radioGroup = findViewById(R.id.radio_group);

        exitBtnInSort = findViewById(R.id.exit_button_in_sort);
        exitBtnInSort.setOnClickListener(btnL);

        //Setting check to last clicked button
        checkedIndex = getSharedPreferences("radioBtnIndex", Context.MODE_PRIVATE)
                .getInt("radioBtnIndex", checkedIndex);
        RadioButton savedCheckedRadioButton = (RadioButton) radioGroup.getChildAt(checkedIndex);
        savedCheckedRadioButton.setChecked(true);

    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        SharedPreferences.Editor editor = getSharedPreferences(SORT_PREF, MODE_PRIVATE).edit();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.sort_by_name:
                if (checked)
                    checkedIndex = 0;
                editor.putString("sort", "sortByName");
                editor.apply();
                getSharedPreferences("radioBtnIndex", Context.MODE_PRIVATE).edit()
                        .putInt("radioBtnIndex", 0).apply();
                finish();
                overridePendingTransition(0, R.anim.hold);
                break;
            case R.id.sort_by_date:
                if (checked)
                    checkedIndex = 1;
                editor.putString("sort", "sortByDate");
                editor.apply();
                getSharedPreferences("radioBtnIndex", Context.MODE_PRIVATE).edit()
                        .putInt("radioBtnIndex", 1).apply();
                finish();
                overridePendingTransition(0, R.anim.hold);
                break;
        }
    }

    View.OnClickListener btnL = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
            overridePendingTransition(0, R.anim.hold);
        }
    };


}
