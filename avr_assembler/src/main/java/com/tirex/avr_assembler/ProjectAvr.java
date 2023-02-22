package com.tirex.avr_assembler;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewDebug;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ProjectAvr {
    private String name; //Имя проекта
    private String nameMainFile; // Имя главного файла проекта (совпадает с именем проекта)
    private CustomTabViewWithNumEditText customTabWithNET;
    private final String PRJ_LINE_NAME_MAIN_FILE = "Main file name:";
    private final String PRJ_LINE_OPENED_FILES = "Opened files:";
    private final String PRJ_LINE_CURRENT_TAB = "Current tab:";
    private final String PRJ_LINE_FILE_NAME = "File name:";
    private final String PRJ_LINE_FILE_PATH = "File path:";
    private final String PRJ_LINE_FILE_CURSOR_POSITION = "Position:";
    private String path; //Путь к файлу проекта

    public ProjectAvr (String nameProject, String nameMainFile, CustomTabViewWithNumEditText customTabWithNET){
        name = nameProject;
        this.nameMainFile = nameMainFile;
        this.customTabWithNET = customTabWithNET;
        //createProjectFiles();
    }

    public boolean loadProject (String fullFileName) {
        File file = new File(fullFileName);
        if (! verifyExistsFile(file)) return false;
        path = file.getParent();//.getPath();
        FileExplorer fileExplorer = new FileExplorer();
        Exception error;
        ArrayList<String> projectFileList = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            projectFileList = fileExplorer.readFile(fullFileName);
        }
        error = fileExplorer.getError();
        if (error != null){
            String text = customTabWithNET.getContext().getString(R.string.project_avr_open_file_error);
            Toast.makeText(customTabWithNET.getContext(), text + '\n' + error.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
        String[] projectFileContent = (String[]) projectFileList.toArray(new String[projectFileList.size()]);
        projectFileList = null;
        int index = indexOfLine(projectFileContent, 0, PRJ_LINE_NAME_MAIN_FILE);
        if (index < 0) {
            if (new File(path + File.separator + "res" + File.separator + file.getName()).exists()) {
                this.nameMainFile = file.getName();
            } else {
                Toast.makeText(customTabWithNET.getContext(), R.string.project_avr_read_file_project_error, Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            this.nameMainFile = projectFileContent[index].substring(
                    PRJ_LINE_NAME_MAIN_FILE.length());
        }
        index = indexOfLine(projectFileContent,  0, PRJ_LINE_OPENED_FILES);
        if (index < 0 || indexOfLine(projectFileContent, index, PRJ_LINE_FILE_NAME) < 0) {
            if (! openFile(path + File.separator + "res" + File.separator + nameMainFile)) {
                Toast.makeText(customTabWithNET.getContext(), R.string.project_avr_read_file_project_error, Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            index = indexOfLine(projectFileContent, index, PRJ_LINE_FILE_NAME);
            if (! loadFilesFromProjectFile (projectFileContent, index)) {
                return false;
            } else {
                index = indexOfLine(projectFileContent, index+1, PRJ_LINE_FILE_NAME) ;
                while (index > 0) {
                    loadFilesFromProjectFile (projectFileContent, index);
                    index = indexOfLine(projectFileContent, index+1, PRJ_LINE_FILE_NAME) ;
                }
            }
        }
        index = indexOfLine(projectFileContent,  0, PRJ_LINE_CURRENT_TAB);
        int numFocusedLine = 0;
        try {
            numFocusedLine = Integer.parseInt(projectFileContent[index].substring(
                    PRJ_LINE_CURRENT_TAB.length()));
        } catch (Exception e) {
        }
        if (numFocusedLine < 0 || numFocusedLine > customTabWithNET.countTabs()) {
            customTabWithNET.setFocus(0);
        } else {
            customTabWithNET.setFocus(numFocusedLine);
        }
        return true;
    }

    private int indexOfLine (String[] arrayLine, int startIndex, String line) {
        int result = -1;
        for (int i = startIndex; i<arrayLine.length; i++) {
            if (arrayLine[i].indexOf(line) >= 0) {
                result = i;
                break;
            }
        }
        return result;
    }

    private boolean loadFilesFromProjectFile (String[] projectFileContent, int startIndex) {
        int index = startIndex;
        //index = indexOfLine(projectFileContent, index, PRJ_LINE_FILE_NAME);
        String fileName = projectFileContent[index].substring(
                    PRJ_LINE_FILE_NAME.length());
        index = indexOfLine(projectFileContent, index, PRJ_LINE_FILE_PATH);
        if (index < 0) {
            index = startIndex;
            if (!openFile(path + File.separator + "res" + File.separator + fileName)) {
                Toast.makeText(customTabWithNET.getContext(), R.string.project_avr_read_file_project_error, Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            String filePath = projectFileContent[index].substring(
                    PRJ_LINE_FILE_PATH.length());
            if (!openFile(filePath + File.separator + fileName)) {
                return false;
            }
        }
        index = indexOfLine(projectFileContent, index, PRJ_LINE_FILE_CURSOR_POSITION);
        if (index >= 0) {
            try {
                int cursorPosition = Integer.parseInt(projectFileContent[index].substring(
                        PRJ_LINE_FILE_CURSOR_POSITION.length()));
                if (cursorPosition <= customTabWithNET.getNumberedEditText(customTabWithNET.countTabs() - 1).getText().length()) {
                    customTabWithNET.getNumberedEditText(customTabWithNET.countTabs() - 1).setCursorPosition(cursorPosition);
                }
            } catch (Exception e) {
            }
        }
        return true;
    }

    public void createProjectFiles () {
        FileExplorer file = new FileExplorer();
        path = getFullPathProjectFolder();
        file.createExternalStorageFolder(path);
        file.createExternalStorageFolder(getFullPathFolderForFiles());
        //file.createEmptyFile(getFullPathFolderForFiles() + "/" + nameMainFile + "." + MainActivity.expansionResFile);
        saveCurrentFile();
    }

    private String getFullPathProjectFolder (){
        return MainActivity.pathFolderProjects + "/" + name;
    }

    private String getFullPathFolderForFiles () {
        return getFullPathProjectFolder() + "/res";
    }

    public boolean close (){
        while (customTabWithNET.countTabs()>0){
            customTabWithNET.closeTab(customTabWithNET.countTabs()-1);
        }
        return true;
    }

    //Проверяет, есть ли в текущем проекте вкладки, изменения в которых не сохранены
    private boolean checkUnsavedFilesProject (){
        return true;
    }

    //Проверяет, есть ли несохраненные изменения во вкладке с номером numTab
    private boolean checkUnsavedDataInFile (int numTab){
        return true;
    }

    public void saveCurrentFile () {
        int numCurrentTab = customTabWithNET.getCurrentTabIndex();
        if (checkUnsavedDataInFile (numCurrentTab)){
            if (saveFile (numCurrentTab)){
                Toast.makeText(customTabWithNET.getContext(), R.string.project_avr_file_saved, Toast.LENGTH_SHORT).show();
            }
        }
        saveProjectFile();
    }

    public void saveAllFiles () {
        boolean savedAll = true;
        boolean res;
        for (int i = 0; i<customTabWithNET.countTabs(); i++){
            if (checkUnsavedDataInFile (i)){
                res = saveFile (i);
                savedAll = savedAll && res;
            }
        }
        if (savedAll){
            Toast.makeText(customTabWithNET.getContext(), R.string.project_avr_file_saved, Toast.LENGTH_SHORT).show();
        }
        saveProjectFile();
    }

    //Сохраняет содержимое EditText из вкладки с номером numTab в файл
    private boolean saveFile (int numTab){
        String fileName = customTabWithNET.getFileName(numTab);
        String filePath = customTabWithNET.getFilePath(numTab);
        if ((fileName != null)&&(filePath != null)){
            FileExplorer fileExplorer = new FileExplorer();
            String fullFileName = filePath + "/" + fileName;// + "." + MainActivity.expansionResFile;
            String [] fileContent;
            fileContent = customTabWithNET.getNumberedEditText(numTab).getText().toString().split("\n");
            fileExplorer.saveFile(fullFileName, fileContent);
            Exception e = fileExplorer.getError();
            if (e != null){
                String text = customTabWithNET.getContext().getString(R.string.project_avr_saving_error);
                Toast.makeText(customTabWithNET.getContext(), text + " " + fileName + '\n' + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
        return false;
    }

    //Сохраняет файл проекта
    public void saveProjectFile (){
        //String filePath = getFullPathProjectFolder();
        FileExplorer fileExplorer = new FileExplorer();
        String fullFileName = path + "/" + name + "." + MainActivity.expansionProjectFile;
        ArrayList <String> fileContent = makeContentForProjectFile();
        fileExplorer.saveFile(fullFileName, fileContent);
        Exception e = fileExplorer.getError();
        if (e != null){
            String text = customTabWithNET.getContext().getString(R.string.project_avr_saving_prj_error);
            Toast.makeText(customTabWithNET.getContext(), text + '\n' + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList <String> makeContentForProjectFile (){
        ArrayList <String> fileContent = new ArrayList<>();
        fileContent.add(PRJ_LINE_NAME_MAIN_FILE + nameMainFile);
        //fileContent.add(nameMainFile);
        fileContent.add(PRJ_LINE_CURRENT_TAB + customTabWithNET.getCurrentTabIndex());
        //fileContent.add(String.valueOf(customTabWithNET.getCurrentTabIndex()));
        fileContent.add(PRJ_LINE_OPENED_FILES);
        for (int i=0; i<customTabWithNET.countTabs(); i++){
            fileContent.add(PRJ_LINE_FILE_NAME + customTabWithNET.getFileName(i));
            fileContent.add(PRJ_LINE_FILE_PATH + customTabWithNET.getFilePath(i));
            fileContent.add(PRJ_LINE_FILE_CURSOR_POSITION + String.valueOf(customTabWithNET.getNumberedEditText(i).getCursorPosition()));
        }
        return fileContent;
    }

    public void createNewFile() {
        final CustomDialog dialog = new CustomDialog(customTabWithNET.getContext());
        dialog.setView(R.layout.dialog_create_new_file);
        dialog.addButton(customTabWithNET.getContext().getString(R.string.dialog_create_new_file_button_create), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etNameFile;
                String nameFile, pathFile;
                etNameFile = dialog.getLayoutForContent().findViewById(R.id.editTextNameFile);
                nameFile = etNameFile.getText().toString();
                while ((nameFile.length()>0)&&(nameFile.charAt(nameFile.length()-1) == ' ')){
                    nameFile = nameFile.substring(0, nameFile.length()-1);
                }
                RadioButton radioIncludeFolder = dialog.getLayoutForContent().findViewById(R.id.radioButtonIncludeFolder);
                if (radioIncludeFolder.isChecked()){
                    pathFile = MainActivity.pathFolderIncludes;
                } else {
                    pathFile = path + "/res";
                }
                if (checkFileName (nameFile, pathFile)){
                    dialog.hide();
                    prepareNewFile(nameFile + "." + MainActivity.expansionIncFile, pathFile);
                }
            }
        });
        dialog.addButton(customTabWithNET.getContext().getString(R.string.dialog_create_new_file_button_cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.destroy();
            }
        });
        dialog.show();
    }

    private boolean checkFileName (String fileName, String filePath){
        if (fileName.length() == 0) {
            Toast.makeText(customTabWithNET.getContext(), R.string.create_file_invalid_name, Toast.LENGTH_SHORT).show();
            return false;
        }
        FileExplorer fileExplorer = new FileExplorer();
        if (fileExplorer.isDir(filePath + "/" + fileName)) {
            Toast.makeText(customTabWithNET.getContext(), R.string.create_file_double_name, Toast.LENGTH_SHORT).show();
        } else {
            if (fileExplorer.checkFileName(filePath, fileName)) {
                return true;
            } else {
                Toast.makeText(customTabWithNET.getContext(), R.string.create_file_invalid_name, Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    private void prepareNewFile(String fileName, String filePath) {
        customTabWithNET.addTab(fileName, filePath);
        saveFile(customTabWithNET.getCurrentTabIndex());
    }

    public boolean openFile (String fullFileName){
        String filePath;
        String fileName;
        File file = new File(fullFileName);
        if (! verifyExistsFile(file)) return false;
        try {
            filePath = file.getParent();
            fileName = file.getName();
        } catch (Exception e) {
            String text = customTabWithNET.getContext().getString(R.string.project_avr_open_file_error);
            Toast.makeText(customTabWithNET.getContext(), text + '\n' + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
        FileExplorer fileExplorer = new FileExplorer();
        Exception error;
        CharSequence fileContent = "";
        //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            fileContent = fileExplorer.readFile(filePath, fileName);
        //}
        error = fileExplorer.getError();
        if (error != null){
            String text = customTabWithNET.getContext().getString(R.string.project_avr_read_file_error);
            Toast.makeText(customTabWithNET.getContext(), text + " " + fileName + "\n" + error.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
        customTabWithNET.addTab(fileName, filePath);
        customTabWithNET.getNumberedEditText(customTabWithNET.countTabs()-1).setText(fileContent);
        customTabWithNET.getNumberedEditText(customTabWithNET.countTabs()-1).setCursorPosition(0);
        return true;
    }

    private boolean verifyExistsFile (File file) {
        if (! file.exists()){
            String text = customTabWithNET.getContext().getString(R.string.project_avr_no_file_error);
            Toast.makeText(customTabWithNET.getContext(), text + " " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
