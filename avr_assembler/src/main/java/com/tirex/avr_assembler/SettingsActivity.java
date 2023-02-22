package com.tirex.avr_assembler;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    TextView textView_FolderIncludes, textView_FolderProjects, textView_Language;
    TextView textView_PathIncludes, textView_PathProjects, textView_ChosenLanguage;
    Button btn_PathIncludes, btn_PathProjects, btn_ChoseLanguage;
    final int DIALOG_LANGUAGE = 1;
    private String listLanguages[] = {"English", "Русский"};

    public static final String FILE_SETTINGS = "Settings";
    public static final String NUM_LANGUAGE = "num_language";
    public static final String PATH_FOLDER_INCLUDES = "path_folder_includes";
    public static final String PATH_FOLDER_PROJECTS = "path_folder_projects";
    public static final String LAST_OPENED_FOLDER = "last_opened_folder";
    public static final String LAST_SORT_FILE_MANAGER = "last_sort_file_manager";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        textView_FolderIncludes = (TextView) findViewById(R.id.textView_FolderIncludes);
        textView_FolderProjects = (TextView) findViewById(R.id.textView_FolderProjects);
        textView_Language = (TextView) findViewById(R.id.textView_Language);
        textView_PathIncludes = (TextView) findViewById(R.id.textView_PathIncludes);
        textView_PathProjects = (TextView) findViewById(R.id.textView_PathProjects);
        textView_ChosenLanguage = (TextView) findViewById(R.id.textView_chosenLanguage);
        btn_PathIncludes = (Button) findViewById(R.id.btn_PathIncludes);
        btn_PathProjects = (Button) findViewById(R.id.btn_PathProjects);
        btn_ChoseLanguage = (Button) findViewById(R.id.btn_ChoseLanguage);
        textView_PathIncludes.setText(MainActivity.pathFolderIncludes);
        textView_PathProjects.setText(MainActivity.pathFolderProjects);
        setTextForElements();
        btn_PathIncludes.setOnClickListener(this);
        btn_ChoseLanguage.setOnClickListener(this);
        btn_ChoseLanguage.setOnClickListener(this);
    }

    // Метод обработки нажатий кнопок
    @Override
    public void onClick (View v){
        switch (v.getId()){
            case R.id.btn_PathIncludes:

                break;
            case R.id.btn_PathProjects:

                break;
            case R.id.btn_ChoseLanguage:
                showDialog(DIALOG_LANGUAGE);
                break;
        }
    } // конец метода onClick

    // вспомогательный метод определения номера выбранной позиции в диалоге выбора языка
    private int getNumChecked (){
        int numChecked = -1;
        switch (MainActivity.language){
            case 1: //русский
                numChecked = 1;
                break;
            case 2: //английский
                numChecked = 0;
                break;
        }
        return numChecked;
    }

    protected Dialog onCreateDialog (int id){
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        switch (id){
            case DIALOG_LANGUAGE:
                adb.setTitle(textView_Language.getText());
                adb.setSingleChoiceItems(listLanguages, getNumChecked(), clickListenerForLanguage);
                break;
        }
        adb.setPositiveButton("Ok", clickListenerForLanguage);
        return adb.create();
    }

    protected void onPrepareDialog (int id, Dialog dialog){
        dialog.setTitle(textView_Language.getText());
        ((AlertDialog) dialog).getListView().setItemChecked(getNumChecked(), true);
    };

    OnClickListener clickListenerForLanguage = new OnClickListener(){
        public void onClick (DialogInterface dialog, int which){
            ListView lv = ((AlertDialog) dialog).getListView();
            if ((which == Dialog.BUTTON_POSITIVE)&&(getNumChecked() != lv.getCheckedItemPosition())){
                switch (lv.getCheckedItemPosition()){
                    case 0:
                        MainActivity.language = 2; // английский
                        break;
                    case 1:
                        MainActivity.language = 1; // русский
                        break;
                }
                setTextForElements();
                SharedPreferences sPref;
                sPref = getSharedPreferences(FILE_SETTINGS,MODE_PRIVATE);
                SharedPreferences.Editor ed =sPref.edit();
                ed.putInt(NUM_LANGUAGE, MainActivity.language);
                ed.commit();
            }
        }
    };

    private void setTextForElements(){
        switch (MainActivity.language){
            case 1: //русский
                textView_FolderIncludes.setText(R.string.rusTextFolderIncludes);
                textView_FolderProjects.setText(R.string.rusTextFolderProjects);
                textView_Language.setText(R.string.rusTextLanguage);
                textView_ChosenLanguage.setText(R.string.rusTextChosenLanguage);
                btn_PathIncludes.setText(R.string.rusTextButtonPathes);
                btn_PathProjects.setText(R.string.rusTextButtonPathes);
                btn_ChoseLanguage.setText(R.string.rusTextButtonLanguage);
                break;
            case 2: //английский
                textView_FolderIncludes.setText(R.string.enTextFolderIncludes);
                textView_FolderProjects.setText(R.string.enTextFolderProjects);
                textView_Language.setText(R.string.enTextLanguage);
                textView_ChosenLanguage.setText(R.string.enTextChosenLanguage);
                btn_PathIncludes.setText(R.string.enTextButtonPathes);
                btn_PathProjects.setText(R.string.enTextButtonPathes);
                btn_ChoseLanguage.setText(R.string.enTextButtonLanguage);
                break;
        }
    }

}
