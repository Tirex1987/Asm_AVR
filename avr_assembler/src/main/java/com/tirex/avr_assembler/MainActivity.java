package com.tirex.avr_assembler;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    private EditText errorsWindow;
    public static int language = 1; // 1- русский, 2 -английский
    public static String pathFolderIncludes;
    public static String pathFolderProjects;
    public static String lastOpenedFolder = null; //Путь к последней открытой папке в файловом менеджере
    public static String defaultPathFolderIncludes = "AsmAVR" + "/" + "Inc" + "/";
    public static String defaultPathFolderProjects = "AsmAVR" + "/" + "Projects" + "/";
    public static String expansionProjectFile = "prj";
    public static String expansionResFile = "avr";
    public static String expansionIncFile = "inc";
    private static final int STORAGE_PERMISSION_CODE = 100;
    public static final int REQUEST_CODE_OPEN_FILE = 1;
    //public static final int REQUEST_CODE_OPEN_FOLDER = 2;
    public static final int REQUEST_CODE_OPEN_PROJECT = 3;
    private static long back_pressed;
    private int lastSortForFileManager;
    private boolean permissionGranted = false;
    private CustomTabViewWithNumEditText customTabWithNET;
    private int colorButtons = 0xFFDFFFDF;
    private PopupMenu popupMenu;
    private ImageButton btnSave, btnUndo, btnRedo, btnCloseTab,
                        buttonFolderMenu, buttonEditorMenu, buttonPopupMenu;
    private ProjectAvr project;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        errorsWindow=findViewById(R.id.errorsWindow);

        loadElements ();
        LinearLayout layoutForTab = findViewById(R.id.layoutForTab);
        customTabWithNET = new CustomTabViewWithNumEditText(layoutForTab.getContext());
        layoutForTab.addView(customTabWithNET);
        customTabWithNET.addTab("Label123456789", "");
        customTabWithNET.addTab("Label123456789", "");
        customTabWithNET.addTab("Label123456789", "");

        errorsWindow.setHorizontallyScrolling(true);
        loadPreferences();
        loadMenu();
        drawPanelTools();
        //Определяем метод обработки нажатия на кнопку меню
        buttonPopupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickButtonPopupMenu();
            }
        });
        //Нажатие на кнопку меню редактирования
        buttonEditorMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickButtonEditorMenu();
            }
        });
        //Определяем метод обработки нажатия на кнопку закрытия вкладки
        btnCloseTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeTab();
            }
        });
        //Определяем метод обработки нажатия на кнопку меню папок
        buttonFolderMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickButtonFolderMenu();
            }
        });
    }


    private void loadMenu() {
        popupMenu = new PopupMenu(MainActivity.this, buttonPopupMenu);
        //включение отображения иконок в меню
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper
                            .getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        popupMenu.inflate(R.menu.menu);
        popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            //Обработка нажатий пунктов меню
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    //main menu
                    case R.id.menu_build:
                        proc_compile();
                        break;
                    case R.id.menu_new_project:
                        createNewProject ();
                        break;
                    case R.id.menu_open_project:
                        openProject();
                        break;
                    case R.id.menu_settings:
                        proc_settings();
                        break;
                    case R.id.menu_help:

                        break;
                    case R.id.menu_exit:
                        finish();
                        System.exit(0);
                        break;
                    //editor menu
                    case R.id.menu_undo:

                        break;
                    case R.id.menu_redo:

                        break;
                    case R.id.menu_select_all:

                        break;
                    case R.id.menu_find:

                        break;
                    case R.id.menu_find_and_replace:

                        break;
                    case R.id.menu_go_to_line:

                        break;
                    // folder menu
                    case R.id.menu_new_file:
                        createNewFile();
                        break;
                    case R.id.menu_open_file:
                        openFile();
                        break;
                    case R.id.menu_save:
                        saveCurrentFile();
                        break;
                    case R.id.menu_save_as:

                        break;
                    case R.id.menu_save_all:
                        saveAllFiles();
                        break;
                    case R.id.menu_close_tab:
                        closeTab();
                        break;
                }
                return false;
            }
        });
    }


    //Обработка нажатия на кнопку меню
    public void onClickButtonPopupMenu(){
        popupMenu.getMenu().setGroupVisible(R.id.menu_edit,false); //Visible определяет скрыть или показать группу меню с id равным group1
        popupMenu.getMenu().setGroupVisible(R.id.menu_folder,false); //Visible определяет скрыть или показать группу меню с id равным group1
        popupMenu.getMenu().setGroupVisible(R.id.menu_main,true); //Visible определяет скрыть или показать группу меню с id равным group1
        popupMenu.show();
    }

    //Обработка нажатия на кнопку редактирования
    public void onClickButtonEditorMenu(){
        popupMenu.getMenu().setGroupVisible(R.id.menu_main,false); //Visible определяет скрыть или показать группу меню с id равным group1
        popupMenu.getMenu().setGroupVisible(R.id.menu_folder,false); //Visible определяет скрыть или показать группу меню с id равным group1
        popupMenu.getMenu().setGroupVisible(R.id.menu_edit,true); //Visible определяет скрыть или показать группу меню с id равным group1
        popupMenu.show();
    }

    //Обработка нажатия на кнопку меню папок
    public void onClickButtonFolderMenu(){
        /*switch (language){
            case 1: //русский
                numMenu = R.menu.rusmainmenu;
        }*/
        popupMenu.getMenu().setGroupVisible(R.id.menu_edit,false); //Visible определяет скрыть или показать группу меню с id равным group1
        popupMenu.getMenu().setGroupVisible(R.id.menu_main,false); //Visible определяет скрыть или показать группу меню с id равным group1
        popupMenu.getMenu().setGroupVisible(R.id.menu_folder,true); //Visible определяет скрыть или показать группу меню с id равным group1
        popupMenu.show();
    }

    private void proc_compile(){
        CompileText asmProgramm=new CompileText(customTabWithNET.getNumberedEditText(0).getText().toString().split("\n"));
        asmProgramm.convertTextToMachineCode();
        errorsWindow.setText("");
        for (int i=0; i<asmProgramm.getCountErrors();i++){
            errorsWindow.append('\n' + asmProgramm.getErrors().get(i));
        }
//        errorsWindow.setText(asmProgramm.getMachineCode().get(0).toString());
    }

    private void proc_settings(){
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity (intent);
    }

    private void loadPreferences(){
        SharedPreferences sPref;
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
        FileExplorer fileExp = new FileExplorer();
        sPref = getSharedPreferences(SettingsActivity.FILE_SETTINGS,MODE_PRIVATE);
        language = sPref.getInt(SettingsActivity.NUM_LANGUAGE, 2);
        lastOpenedFolder = sPref.getString(SettingsActivity.LAST_OPENED_FOLDER, null);
        lastSortForFileManager = sPref.getInt(SettingsActivity.LAST_SORT_FILE_MANAGER, FileManagerActivity.SORT_BY_NAME);
        pathFolderIncludes = sPref.getString(SettingsActivity.PATH_FOLDER_INCLUDES, null);
        pathFolderProjects = sPref.getString(SettingsActivity.PATH_FOLDER_PROJECTS, null);
        if ((pathFolderIncludes == null)&& permissionGranted){
            pathFolderIncludes = fileExp.createExternalStorageFolder(defaultPathFolderIncludes);
            if (pathFolderIncludes==null){
                Toast.makeText(this, fileExp.getError().getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        if ((pathFolderProjects == null)&& permissionGranted){
            pathFolderProjects = fileExp.createExternalStorageFolder(defaultPathFolderProjects);
            if (pathFolderProjects==null){
                Toast.makeText(this, fileExp.getError().getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    } // конец метода loadPreferences

    public void checkPermission (String permission, int requestCode){
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {permission}, requestCode);
        } else {
            //Toast.makeText(this, "Permission is", Toast.LENGTH_SHORT).show();
            permissionGranted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.length>0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                String message="";
                switch (language){
                    case 1:
                        message = "Отказано в доступе, созданные файлы нельзя будет сохранить!";
                        break;
                    case 2:
                        message = "Access is denied, created files cannot be saved!";
                        break;
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }else{
                permissionGranted = true;
            }
        }
    }

    private void closeTab () {
        customTabWithNET.closeCurrentTab();
    }

    private void loadElements () {
        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentFile();
            }
        });
        btnUndo = findViewById(R.id.btnUndo);
        btnRedo = findViewById(R.id.btnRedo);
        btnCloseTab = findViewById(R.id.btnCloseTab);
        buttonFolderMenu = findViewById(R.id.buttonFolderMenu);
        buttonEditorMenu = findViewById(R.id.buttonEditorMenu);
        buttonPopupMenu = findViewById(R.id.buttonPopupMenu);
    }

    private void drawPanelTools () {
        int minWidthPanel = 330;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = (int) (dm.widthPixels/dm.density); // ширина экрана в dp
        //int height = (int) (dm.heightPixels/dm.density);
        if (width<minWidthPanel){
            btnCloseTab.setVisibility(View.GONE);
            if (width<(minWidthPanel-40)){
                btnSave.setVisibility(View.GONE);
            }
        }
    }


    private void createNewProject () {
        final CustomDialog dialog = new CustomDialog(this);
        dialog.setView(R.layout.dialog_create_new_project);
        dialog.setTitle(getString(R.string.dialog_create_new_project_window_name));
        dialog.addButton(getResources().getString(R.string.dialog_create_new_project_button_create), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etNameProject;
                String nameProject;
                etNameProject = dialog.getLayoutForContent().findViewById(R.id.editTextNameProject);
                nameProject = etNameProject.getText().toString();
                while ((nameProject.length()>0)&&(nameProject.charAt(nameProject.length()-1) == ' ')){
                    nameProject = nameProject.substring(0, nameProject.length()-1);
                }
                if (checkProjectName (nameProject)){
                    dialog.hide();
                    prepareProjectFiles(nameProject);
                }
            }
        });
        dialog.addButton(getResources().getString(R.string.dialog_create_new_project_button_cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.destroy();
            }
        });
        dialog.show();
    }

    private boolean checkProjectName (String projectName){
        if (projectName.length() == 0) {
            Toast.makeText(this, R.string.create_project_invalid_name, Toast.LENGTH_SHORT).show();
            return false;
        }
        FileExplorer fileExplorer = new FileExplorer();
        if (fileExplorer.isDir(pathFolderProjects + "/" + projectName)) {
            Toast.makeText(this, R.string.create_project_double_name, Toast.LENGTH_SHORT).show();
        } else {
            if (fileExplorer.checkFileName(pathFolderProjects, projectName)) {
                return true;
            } else {
                Toast.makeText(this, R.string.create_project_invalid_name, Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    private boolean prepareProjectFiles (String projectName){
        if (project != null) {
            if (! project.close()){
                return false;
            }
        }
        String mainFileName = projectName + "." + expansionResFile;
        customTabWithNET.addTab(mainFileName, pathFolderProjects + "/" + projectName + "/res");
        project = new ProjectAvr(projectName, mainFileName, customTabWithNET);
        project.createProjectFiles();
        TextView textViewProjectName = findViewById(R.id.textViewProjectName);
        textViewProjectName.setText(projectName);
        return true;
    }

    private void saveCurrentFile() {
        if (project != null){
            project.saveCurrentFile();
        }
    }

    private void saveAllFiles() {
        if (project != null){
            project.saveAllFiles();
        }
    }

    private void createNewFile() {
        if (project != null){
            project.createNewFile();
        }
    }

    private void openFile() {
        Intent intent = new Intent(MainActivity.this, FileManagerActivity.class);
        ArrayList <String> listExtensions= new ArrayList<>();
        listExtensions.add(expansionResFile);
        listExtensions.add(expansionIncFile);
        intent.putExtra(FileManagerActivity.LIST_EXTENSIONS_FILES, listExtensions);
        intent.putExtra(FileManagerActivity.TYPE_SORT, lastSortForFileManager);
        startActivityForResult (intent, REQUEST_CODE_OPEN_FILE);
    }

    private void openProject() {
        Intent intent = new Intent(MainActivity.this, FileManagerActivity.class);
        ArrayList <String> listExtensions= new ArrayList<>();
        listExtensions.add(expansionProjectFile);
        intent.putExtra(FileManagerActivity.LIST_EXTENSIONS_FILES, listExtensions);
        intent.putExtra(FileManagerActivity.TYPE_SORT, lastSortForFileManager);
        startActivityForResult (intent, REQUEST_CODE_OPEN_PROJECT);
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent data){
        String fileName;
        if (resultCode == RESULT_OK){
            switch (requestCode){
                case REQUEST_CODE_OPEN_FILE:
                    fileName = data.getStringExtra(FileManagerActivity.FULL_FILE_NAME);
                    if (project != null) {
                        if (project.openFile(fileName)){
                            lastOpenedFolder = new File(fileName).getParent();
                            saveSharedPreferencesLastFolder();
                        }
                    }
                    break;
                /*case REQUEST_CODE_OPEN_FOLDER:

                    break;*/
                case REQUEST_CODE_OPEN_PROJECT:
                    fileName = data.getStringExtra(FileManagerActivity.FULL_FILE_NAME);
                    if (project == null || project.close()) {
                        File file = new File (fileName);
                        String name = file.getName().substring(0, file.getName().indexOf('.'));
                        project = new ProjectAvr(name, name, customTabWithNET);
                        if (! project.loadProject(fileName)) {
                            // ошибка при загрузке файла проекта

                        }
                    }
                    break;
            }
        }
    }

    //сохраняем в настройки приложения Shared Preferences путь последней открытой папки через File Manager Activity (значение lastOpenedFolder)
    private void saveSharedPreferencesLastFolder (){
        SharedPreferences sPref;
        sPref = getSharedPreferences(SettingsActivity.FILE_SETTINGS,MODE_PRIVATE);
        SharedPreferences.Editor ed =sPref.edit();
        ed.putString(SettingsActivity.LAST_OPENED_FOLDER, lastOpenedFolder);
        ed.commit();
    }

    //Нажатие на кнопку Назад, двойное нажатие - выход из приложения
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis())
            super.onBackPressed();
        else
            Toast.makeText(this, R.string.exit_from_programm, Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
    }

}
