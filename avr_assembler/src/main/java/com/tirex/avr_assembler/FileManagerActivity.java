package com.tirex.avr_assembler;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.tirex.avr_assembler.res_for_extensions.ListResForExtensions;
import com.tirex.avr_assembler.res_for_extensions.ResForExtensions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileManagerActivity extends AppCompatActivity {

    // Режимы отображения системы папок и файлов (содержится в modeOfDisplayFiles):
    //public static final int DISPLAY_ALL_FILES = 1; // отображаются все файлы и папки
    //public static final int DISPLAY_FILES_OF_LIST_EXTENSIONS = 2; // отображаются файлы только с расширениями из списка listEnabledExtensions и все папки
    //public static final int DISPLAY_FOLDERS_ONLY = 3; // отображаются только папки (все файлы скрыты)
    // Режимы сортировки (содержится в typeSort):
    public static final int SORT_BY_NAME = 10; // по имени
    public static final int SORT_BY_DATE_CHANGE = 11; // по дате последнего изменения
    // Режимы отображения Activity (содержится в modeOfWork):
    public static final int MODE_NORMAL = 50; // Режим отображения по умолчанию
    public static final int MODE_REMOVE_FILE = 51; // Отображаются только папки, добавляется панель с кнопкой для подтверждения перемещения выбранного файла в текущую директорияю
    public static final int MODE_COPY_FILE = 52; // Отображаются только папки, добавляется панель с кнопкой для подтверждения копирования выбранного файла в текущую директорияю
    public static final String FULL_FILE_NAME = "file name";
    public static final String LIST_EXTENSIONS_FILES = "files extensions";
    //public static final String DISPLAY_MODE = "display mode";
    public static final String TYPE_SORT = "type sort";
    // Фильтр отображения файлов (содержится в filterFileTypes)
    public static final int FILTER_FILE_TYPE_LIST_EXTENSIONS = 80; // отображаются файлы только с расширениями из списка listEnabledExtensions и все папки
    public static final int FILTER_FILE_TYPE_ALL_FILES = 81; // отображаются все файлы
    private final int MAX_SIZE_LIST_FILES_WITHOUT_THREAD = 100;
    //public static long back_pressed;
    private int fileNameSize = 16;
    private int colorTextFileName = 0xFF202020;
    private int pathContainerTextSize = 16;
    private int pathContainerTextColor = 0xFF3C3838;
    private ArrayList <String> listEnabledExtensions = new ArrayList<>(); // Список отображаемых расширений файлов, если список пуст, то отображаются все файлы
    private LinearLayout filesContainer, pathContainer;
    private ScrollView fileManagerScrollView;
    private HorizontalScrollView horizontalScrollViewForPathContainer;
    private EditText editTextSearch;
    private ArrayList<FileInContainer> listFiles = new ArrayList<>();
    private List<String> targetDirectory = new ArrayList<>();
    private File currentDirectory; // текущая открытая директория
    private Exception error;
    private String parentDir = Environment.getExternalStorageDirectory().getAbsolutePath();
    private int typeSort; // содержит текущий тип сортировки: SORT_BY_NAME или SORT_BY_DATE_CHANGE
    private ArrayList <File> listLastDirectories = new ArrayList<>();
    private FileInContainer selectedFileWithContextMenu;
    private int modeOfWork = MODE_NORMAL;
    private LinearLayout searchPanel;
    private String lineForSearch = "";
    private String colorSearchSubString = "#FF930303";
    private Thread currentThread;
    private int filterFileTypes = FILTER_FILE_TYPE_ALL_FILES;
    private PopupMenu popupMenu;
    private ImageButton btnFilterFileTypes;
    //private int modeOfDisplayFiles = DISPLAY_ALL_FILES;
    // Ресурсы для разных типов файлов:
    private ListResForExtensions listResForExtensions = new ListResForExtensions(
                new ArrayList<ResForExtensions>(
                    Arrays.asList(
                        new ResForExtensions("avr", R.drawable.icon_file_avr),
                        new ResForExtensions("inc", R.drawable.icon_file_inc),
                        new ResForExtensions("prj", R.drawable.icon_file_prj)
                    )
                ),
                R.drawable.icon_file_2
            );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);
        fileManagerScrollView = findViewById(R.id.fileManagerScrollView);
        pathContainer = findViewById(R.id.fileManagerPathContainer);
        horizontalScrollViewForPathContainer = findViewById(R.id.fileManagerHorizontalScrollView);
        final ImageButton btnCancel = findViewById(R.id.fileManagerBtnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
        ImageButton btnSort = findViewById(R.id.fileManagerBtnSort);
        btnSort.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                clickBtnSort();
            }
        });
        ImageButton btnCreateFolder = findViewById(R.id.fileManagerBtnCreateFolder);
        btnCreateFolder.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                clickBtnCreateFolder();
            }
        });
        final ImageButton btnSearch = findViewById(R.id.fileManagerBtnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                clickBtnSearch();
            }
        });
        searchPanel = findViewById(R.id.fileManagerSearchPanel);
        editTextSearch = findViewById(R.id.fileManagerSearchEditText);
        ImageButton btnCloseSearchPanel = findViewById(R.id.fileManagerSearchPanelButtonClose);
        btnCloseSearchPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSearch.callOnClick();
            }
        });
        final ImageButton btnFind = findViewById(R.id.fileManagerSearchPanelButtonFind);
        btnFind.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                clickBtnFind();
            }
        });
        EditText editTextSearch = findViewById(R.id.fileManagerSearchEditText);
        editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    btnFind.callOnClick();
                }
                return false;
            }
        });
        ImageButton btnClearEditTextSearch = findViewById(R.id.fileManagerSearchPanelButtonClear);
        btnClearEditTextSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickBtnClearEditTextSearch();
            }
        });
        Intent intent = getIntent();
//        modeOfDisplayFiles = intent.getIntExtra(DISPLAY_MODE, DISPLAY_FILES_OF_LIST_EXTENSIONS);
        if (intent.getStringArrayListExtra(LIST_EXTENSIONS_FILES) != null){
            listEnabledExtensions = intent.getStringArrayListExtra(LIST_EXTENSIONS_FILES);
            filterFileTypes = FILTER_FILE_TYPE_LIST_EXTENSIONS;
        } //else modeOfDisplayFiles = DISPLAY_ALL_FILES;
        btnFilterFileTypes = findViewById(R.id.fileManagerBtnFilterFileTypes);
        loadPopupMenu(btnFilterFileTypes);
        btnFilterFileTypes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterFileTypes == FILTER_FILE_TYPE_LIST_EXTENSIONS || listEnabledExtensions.size() == 0) {
                    popupMenu.getMenu().getItem(0).setChecked(true);
                } else {
                    popupMenu.getMenu().getItem(1).setChecked(true);
                }
                popupMenu.show();
            }
        });
        typeSort = intent.getIntExtra(TYPE_SORT, SORT_BY_NAME);
        filesContainer = findViewById(R.id.fileManagerFilesContainer);
        printFileTypes();
        String currentPath = getLastOpenedFolder();
        currentDirectory = getDirectory (currentPath);
        if (currentDirectory == null){
            String text = this.getString(R.string.file_manager_activity_error_load_files);
            Toast.makeText(this, text + " " + '\n' + error.getMessage(), Toast.LENGTH_SHORT).show();
        } else {
            browseTo (currentDirectory, false);
        }
    }


    private void addNewFileInContainer (String fileName){
        if (fileName.equals("..")){
            listFiles.add(new FileInContainer(this, fileName, R.drawable.icon_folder));
        } else {
            File file = new File(fileName);
            try {
                FileInContainer fileInContainer;
                if (file.exists()) {
                    if (file.isDirectory()) {
                        fileInContainer = new FileInContainer(this, fileName, R.drawable.icon_folder);
                        fileInContainer.isDirectory = true;
                    } else {
                        fileInContainer = new FileInContainer(this, fileName, loadResFromExtension(getFileExtansion(fileName)));
                        fileInContainer.isDirectory = false;
                    }
                    if (file.getName().charAt(0) == '.') fileInContainer.setTransparent();
                    listFiles.add(fileInContainer);
                }
            } catch (Exception e) {
                error = e;
            }
        }
    }

    private void loadFilesInContainer() {
        filesContainer.removeAllViews();
        for (int i=0; i<listFiles.size(); i++){
            filesContainer.addView(listFiles.get(i));
            if ((listFiles.get(i).isDirectory)||(! listFiles.get(i).fileName.equals(".."))) {
                registerForContextMenu(listFiles.get(i));
                if ((searchPanel.getVisibility() == View.VISIBLE)&&(lineForSearch.length()>0)) {
                    String text = listFiles.get(i).getTextViewForFileName().getText().toString();
                    listFiles.get(i).getTextViewForFileName().setText(coloreSubString(text), TextView.BufferType.SPANNABLE);
                }
            }
        }
        fileManagerScrollView.scrollTo(0,0);
    }

    private void loadPopupMenu(View v) {
        popupMenu = new PopupMenu(this, v);
        if (listEnabledExtensions.size() > 0) {
            popupMenu.getMenu().add(1, FILTER_FILE_TYPE_LIST_EXTENSIONS, 1, getFormatListExtensions());
        }
        popupMenu.getMenu().add(1, FILTER_FILE_TYPE_ALL_FILES, 2, "all files");
        popupMenu.getMenu().setGroupCheckable(1, true, true);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case FILTER_FILE_TYPE_LIST_EXTENSIONS:
                        if (filterFileTypes != FILTER_FILE_TYPE_LIST_EXTENSIONS) {
                            item.setChecked(true);
                            setFilterFileTypes (FILTER_FILE_TYPE_LIST_EXTENSIONS);
                        }
                        return true;
                    case FILTER_FILE_TYPE_ALL_FILES:
                        if (filterFileTypes != FILTER_FILE_TYPE_ALL_FILES) {
                            item.setChecked(true);
                            setFilterFileTypes (FILTER_FILE_TYPE_ALL_FILES);
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        //Toast.makeText(this, v.getClass().toString(), Toast.LENGTH_SHORT).show();
        if (v.getClass() == FileInContainer.class) {
            FileInContainer dir = (FileInContainer) v;
            selectedFileWithContextMenu = (FileInContainer) v;
            v.setSelected(true);
            if (dir.isDirectory) {
                inflater.inflate(R.menu.file_manager_dir_context_menu, menu);
            } else {
                inflater.inflate(R.menu.file_manager_file_context_menu, menu);
            }
        }
    }

    @Override
    public boolean onContextItemSelected (MenuItem item){
       // ContextMenu.ContextMenuInfo info = item.getMenuInfo();
        switch (item.getItemId()){
            case R.id.file_manager_context_menu_dir_open:
                selectedFileWithContextMenu.callOnClick();
                return true;
            case R.id.file_manager_context_menu_dir_rename:
                renameDir(selectedFileWithContextMenu.fileName);
                return true;
            case R.id.file_manager_context_menu_dir_delete:
                deleteDir(selectedFileWithContextMenu.fileName);
                return true;
            case R.id.file_manager_context_menu_file_open:
                selectedFileWithContextMenu.callOnClick();
                return true;
            case R.id.file_manager_context_menu_file_rename:
                renameFile(selectedFileWithContextMenu.fileName);
                return true;
            case R.id.file_manager_context_menu_file_copy:
                copyFile(selectedFileWithContextMenu.fileName);
                return true;
            case R.id.file_manager_context_menu_file_remove:
                removeFile(selectedFileWithContextMenu.fileName);
                return true;
            case R.id.file_manager_context_menu_file_delete:
                delFile(selectedFileWithContextMenu.fileName);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onContextMenuClosed (Menu menu){
        super.onContextMenuClosed(menu);
        selectedFileWithContextMenu.setSelected(false);
    }


    private class FileInContainer extends LinearLayout {
        private Context context;
        private ImageView imageViewForIcon;
        private TextView textViewForFileName;
        private int resource;
        private String fileName;
        private String littleFileName;
        private int height = 40;
        private int width = 40;
        private boolean isDirectory;
        public FileInContainer (Context context, String fileName, @DrawableRes int resource){
            super(context);
            this.context = context;
            this.fileName = fileName;
            if (fileName.equals("..")){
                littleFileName = fileName;
            }else{
                File file = new File(fileName);
                littleFileName = file.getName();
            }
            this.resource = resource;
            init();
        }
        public void setTransparent() { imageViewForIcon.setAlpha(0.4f);}
        public LinearLayout getLinearLayout (){
            return (LinearLayout) this;
        }
        public TextView getTextViewForFileName() { return textViewForFileName; }
        private void init(){
            height = getPx(height);
            width = height;
            imageViewForIcon = new ImageView(context);
            imageViewForIcon.setLayoutParams(new LayoutParams(width, height));
            imageViewForIcon.setImageResource(resource); //.setImageDrawable(View.inflate(context, resource, null).getBackground());
            textViewForFileName = new TextView(context);
            textViewForFileName.setText(littleFileName);
            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(getPx(10), 0, 0, 0);
            textViewForFileName.setLayoutParams(layoutParams);
            textViewForFileName.setTextSize(fileNameSize);
            textViewForFileName.setTextColor(colorTextFileName);
            this.setLayoutParams(new LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
            this.setVerticalGravity(Gravity.CENTER_VERTICAL);
            this.setPadding(getPx(10),getPx(3),getPx(10),getPx(3));
            this.setOrientation(LinearLayout.HORIZONTAL);
            this.addView(imageViewForIcon);
            this.addView(textViewForFileName);
            this.setBackgroundResource(R.drawable.file_manager_list_files_selector);
            this.setClickable(true);
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (! fileName.equals("..")){
                        File clickedFile = new File (fileName);
                        if (clickedFile != null){
                            browseTo(clickedFile, true);
                        }
                    }else{
                        if ((currentDirectory.getParent() != null)&&(! currentDirectory.getAbsolutePath().equals(parentDir))){
                            browseTo(currentDirectory.getParentFile(), true);
                        }
                    }
                }
            });
        }
    }

    public int getPx(int dp){
        float scale = getResources().getDisplayMetrics().density;
        return((int) (dp * scale + 0.5f));
    }

    //Возвращает путь к последней открытой папке, а если такой нет - то текущую папку проекта
    private String getLastOpenedFolder() {
        String path;
        if (MainActivity.lastOpenedFolder != null){
            path = MainActivity.lastOpenedFolder;
        }else{
            if (MainActivity.pathFolderProjects != null){
                path = MainActivity.pathFolderProjects;
            }else{
                path = MainActivity.defaultPathFolderProjects;
            }
        }
        return path;
    }

    private File getDirectory (String directory){
        File fDir = new File (directory);
        try {
            if (! fDir.exists()){
                fDir = new File(MainActivity.pathFolderProjects);
                if (! fDir.exists()){
                    fDir = new File (parentDir);
                }
            }
        }catch (Exception e){
            fDir = null;
            error = e;
        }
        return fDir;
    }

    private void browseTo (final File directory, boolean saveLastDirectory){
        if (directory.isDirectory()){
            if (saveLastDirectory) {
                listLastDirectories.add(currentDirectory);
            }
            currentDirectory = directory;
            createClickablePath(directory);
            measureDimension(horizontalScrollViewForPathContainer);
            if (directory.listFiles().length > MAX_SIZE_LIST_FILES_WITHOUT_THREAD) {
                fillInThread(directory.listFiles());
            } else {
                fill(directory.listFiles());
                loadFilesInContainer();
            }
        }else{
            sendResult (directory.getAbsolutePath());
        }
    }

    private void measureDimension (final View v) {
        v.post(new Runnable() {
            @Override
            public void run() {
                switch (v.getId()) {
                    case R.id.fileManagerHorizontalScrollView:
                        horizontalScrollViewForPathContainer.smoothScrollTo(horizontalScrollViewForPathContainer.getMeasuredWidth(), 0);
                        break;
                    case R.id.editTextForInputDialog:
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
                        break;
                }
            }
        });
    }

    // Запуск метода fill через отдельный поток в случае, если массив files содержит
    //более MAX_SIZE_LIST_FILES_WITHOUT_THREAD элементов
    private void fillInThread(final File[] files) {
        setVisibleProgressBar(true);
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                Boolean resultFill = bundle.getBoolean(FileExplorer.KEY_RESULT_OF_THREAD);
                if (resultFill) {
                    loadFilesInContainer();
                } else {
                    String text = getString(R.string.file_manager_activity_error_load_files);
                    Toast.makeText(fileManagerScrollView.getContext(), text + " " + '\n' + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
                setVisibleProgressBar(false);
                currentThread = null;
            }
        };
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                boolean res = fill(files);
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putBoolean(FileExplorer.KEY_RESULT_OF_THREAD, res);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        };
        Thread thread = new Thread(runnable);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
        currentThread = thread;
    }

    private boolean fill (File[] files){
        ArrayList <String> listOfDirs = new ArrayList<>();
        ArrayList <String> listOfFiles = new ArrayList<>();
        boolean isSearchActive = false;
        if (searchPanel.getVisibility() == View.VISIBLE) { // Проверяем, активирована ли панель поиска и введен ли в ней текст для поиска
            lineForSearch = editTextSearch.getText().toString().trim().toLowerCase();
            if (lineForSearch.length()>0) isSearchActive = true;
        }
        try {
            for (File file: files){
                if (isSearchActive) { // Проверяем, активирована ли панель поиска и введен ли в ней текст для поиска
                    if (file.getName().toLowerCase().indexOf(lineForSearch) < 0){
                        continue;
                    }
                }
                if (! file.isDirectory()){
                    if (modeOfWork != MODE_REMOVE_FILE && modeOfWork != MODE_COPY_FILE){
                        if (filterFileTypes == FILTER_FILE_TYPE_LIST_EXTENSIONS && listEnabledExtensions.size()>0) { //(listEnabledExtensions.size()>0){
                            String extension = getFileExtansion(file);
                            if (isExtensionInList(extension)){
                                listOfFiles.add(file.getAbsolutePath());
                            }
                        } else listOfFiles.add(file.getAbsolutePath());
                    }
                } else listOfDirs.add(file.getAbsolutePath());
            }
        } catch (Exception e){
            if (currentThread == null) { // если метод исполняется в основном потоке, а не в отдельном потоке (иначе Toast крашит приложение, т.к. в отдельном потоке обновлять или изменять View компоненты запрещено)
                String text = this.getString(R.string.file_manager_activity_error_load_files);
                Toast.makeText(this, text + " " + '\n' + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        listFiles.clear();
        sortList(listOfDirs);
        sortList(listOfFiles);
        targetDirectory.clear();
        targetDirectory = listOfDirs;
        targetDirectory.addAll(listOfFiles);
        if ((currentDirectory.getParent() != null)&&(! currentDirectory.getAbsolutePath().equals(parentDir))) targetDirectory.add(0, "..");
        for (String fileName : targetDirectory){
            addNewFileInContainer(fileName);
        }
        //loadFilesInContainer();
        return true;
    }

    private String getFileExtansion (File file){
        return getFileExtansion(file.getName());
    }

    private String getFileExtansion (String fileName){
        int lastIndexPoint = fileName.lastIndexOf(".");
        if (lastIndexPoint != -1 && lastIndexPoint != 0){
            return fileName.substring(lastIndexPoint + 1);
        }
        return "";
    }

    private void sendResult (String fullFileName){
        Intent intent = new Intent();
        intent.putExtra(FULL_FILE_NAME, fullFileName);
        setResult(RESULT_OK, intent);
        finish();
    }

    //проверяет, содержится ли расширение файла extension в списке расширений listEnabledExtensions
    private boolean isExtensionInList (String extension){
        if (extension.length() == 0) return false;
        boolean res = false;
        for (String line: listEnabledExtensions){
            if (line.equals(extension)){
                res = true;
                break;
            }
        }
        return res;
    }

    // Сортировка списка имен файлов (папок) по имени или дате последнего изменения
    private void sortList (ArrayList <String> list){
        switch (typeSort){
            case SORT_BY_NAME:
                Collections.sort(list);
                break;
            case SORT_BY_DATE_CHANGE:
                ArrayList <Long> lastModified = new ArrayList<>();
                for (String fileName: list){
                    File file = new File(fileName);
                    lastModified.add(file.lastModified());
                }
                for (int i = 0; i<list.size(); i++){
                    int indexMaxDateModified = i;
                    for (int j=0; j<i; j++){
                        if (lastModified.get(i)>lastModified.get(j)){
                            indexMaxDateModified = j;
                            break;
                        }
                    }
                    lastModified.add(indexMaxDateModified, lastModified.get(i));
                    lastModified.remove(i+1);
                    list.add(indexMaxDateModified, list.get(i));
                    list.remove(i+1);
                }
                break;
        }
    }

    private void clickBtnSort(){
        final CustomDialog dialog = new CustomDialog(this);
        dialog.setTitle(getString(R.string.file_manager_dialog_sort_window_name));
        dialog.setView(R.layout.file_manager_dialog_sort);
        final RadioButton btnSortByName = dialog.getLayoutForContent().findViewById(R.id.fileManagerRadioButtonSortByName);
        final RadioButton btnSortByDate = dialog.getLayoutForContent().findViewById(R.id.fileManagerRadioButtonSortByDate);
        if (typeSort==SORT_BY_DATE_CHANGE){
            btnSortByDate.setChecked(true);
        }
        dialog.addButton(getResources().getString(R.string.file_manager_dialog_sort_button_ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnSortByName.isChecked() && typeSort != SORT_BY_NAME){
                    changeTypeSort(SORT_BY_NAME);
                } else if (btnSortByDate.isChecked() && typeSort != SORT_BY_DATE_CHANGE){
                    changeTypeSort(SORT_BY_DATE_CHANGE);
                }
                dialog.destroy();
            }
        });
        dialog.addButton(getResources().getString(R.string.file_manager_dialog_sort_button_cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.destroy();
            }
        });
        dialog.show();
    }

    private void changeTypeSort (int newTypeSort){
        typeSort = newTypeSort;
        saveSharedPreferencesLastTypeSort();
        browseTo(currentDirectory, false);
    }

    private void saveSharedPreferencesLastTypeSort (){
        SharedPreferences sPref;
        sPref = getSharedPreferences(SettingsActivity.FILE_SETTINGS,MODE_PRIVATE);
        SharedPreferences.Editor ed =sPref.edit();
        ed.putInt(SettingsActivity.LAST_SORT_FILE_MANAGER, typeSort);
        ed.commit();
    }

    //Нажатие на кнопку создания папки
    private void clickBtnCreateFolder() {
        final CustomDialog dialog = new CustomDialog(this);
        /*dialog.setTitle(getString(R.string.file_manager_dialog_create_folder_window_name));
        dialog.setView(R.layout.input_dialog);*/
        dialog.setInputDialog(getString(R.string.file_manager_dialog_create_folder_window_name),
                getString(R.string.file_manager_dialog_create_folder_label_name),
                getString(R.string.file_manager_dialog_create_folder_edittext_hint));
        dialog.addButton(getResources().getString(R.string.file_manager_dialog_create_folder_button_create), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etNameFolder = dialog.getLayoutForContent().findViewById(R.id.editTextForInputDialog);
                String nameFolder = etNameFolder.getText().toString().trim();
                if (checkFolderName(nameFolder)) {
                    FileExplorer fileExplorer = new FileExplorer();
                    fileExplorer.createDir(currentDirectory.getAbsolutePath() + File.separator + nameFolder);
                    browseTo(currentDirectory, false);
                    dialog.destroy();
                }
            }
        });
        dialog.addButton(getResources().getString(R.string.file_manager_dialog_create_folder_button_cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.destroy();
            }
        });
        dialog.show();
    }

    //Проверяет введенное имя папки
    private boolean checkFolderName (String folderName) {
        if (folderName.length() == 0) {
            Toast.makeText(this, R.string.file_manager_invalid_folder_name, Toast.LENGTH_SHORT).show();
            return false;
        }
        return checkName(folderName, true);
    }

    //Проверяет введенное имя файла
    private boolean checkFileName (String fileName) {
        if (fileName.length() == 0) {
            Toast.makeText(this, R.string.file_manager_invalid_file_name, Toast.LENGTH_SHORT).show();
            return false;
        }
        return checkName(fileName, false);
    }

    //Проверяет введенное имя файла или папки. Вызов только через методы checkFolderName или checkFileName
    //Если проверяем имя для папки - isDir ставим true, иначе false
    private boolean checkName(String nameFileOrFolder, boolean isDir){
        String invalidName;
        String doubleName;
        if (isDir) {
            invalidName = getString(R.string.file_manager_invalid_folder_name);
            doubleName = getString(R.string.file_manager_folder_double_name);
        } else {
            invalidName = getString(R.string.file_manager_invalid_file_name);
            doubleName = getString(R.string.file_manager_file_double_name);
        }
        FileExplorer fileExplorer = new FileExplorer();
        if (fileExplorer.isDir(currentDirectory.getAbsolutePath() + File.separator + nameFileOrFolder)) {
            Toast.makeText(this, doubleName, Toast.LENGTH_SHORT).show();
        } else {
            if (fileExplorer.checkFileName(currentDirectory.getAbsolutePath(), nameFileOrFolder)) {
                return true;
            } else {
                Toast.makeText(this, invalidName, Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    //Разбивает путь на отдельные папки, выводит путь к текущей папке в окне менеджера файлов
    private void createClickablePath (File directory){
        pathContainer.removeAllViews();
        String dirName;
        if ((directory.getParent() != null)&&(! directory.getAbsolutePath().equals(parentDir))){
            createClickablePath(directory.getParentFile());
            dirName = directory.getName();
        } else {
            dirName = "***";
        }
        TextView textView = createTextViewForPath(dirName, true);
        textView.setTag(directory.getAbsoluteFile());
        pathContainer.addView(textView);
        if (! directory.getAbsolutePath().equals(currentDirectory.getAbsolutePath())){
            textView = createTextViewForPath(File.separator, false);
            pathContainer.addView(textView);
        }
    }

    private TextView createTextViewForPath (String dirName, boolean isClickable){
        TextView textView = new TextView(this);
        textView.setText(dirName);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(getPx(3), getPx(2), getPx(3), getPx(2));
        textView.setLayoutParams(layoutParams);
        textView.setTextSize(pathContainerTextSize);
        textView.setTextColor(pathContainerTextColor);
        textView.setMinWidth(getPx(10));
        if (isClickable){
            textView.setClickable(true);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (! currentDirectory.getAbsoluteFile().equals((File) v.getTag())) {
                        browseTo((File) v.getTag(), true);
                    }
                }
            });
        }
        return textView;
    }

    //Кнопка "Переименовать директорию" из контекстного меню
    private void renameDir(final String dirPath) {
        String folderName = new File(dirPath).getName();
        final CustomDialog dialog = new CustomDialog(this);
        dialog.setInputDialog(getString(R.string.file_manager_dialog_rename_folder_window_name),
                getString(R.string.file_manager_dialog_rename_folder_label_name),
                getString(R.string.file_manager_dialog_rename_folder_edittext_hint));
        final EditText etNameFolder = dialog.getLayoutForContent().findViewById(R.id.editTextForInputDialog);
        etNameFolder.setText(folderName);
        dialog.addButton(getResources().getString(R.string.file_manager_dialog_rename_folder_button_rename), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etNameFolder = dialog.getLayoutForContent().findViewById(R.id.editTextForInputDialog);
                String newNameFolder = etNameFolder.getText().toString().trim();
                if (checkFolderName(newNameFolder)) {
                    FileExplorer fileExplorer = new FileExplorer();
                    if (! fileExplorer.renameDir(dirPath, newNameFolder)) {
                        Toast.makeText(v.getContext(), fileExplorer.getError().getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.destroy();
                    } else {
                        browseTo(currentDirectory, false);
                        dialog.destroy();
                    }
                }
            }
        });
        dialog.addButton(getResources().getString(R.string.file_manager_dialog_create_folder_button_cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.destroy();
            }
        });
        etNameFolder.setSelection(0, folderName.length());
        etNameFolder.requestFocus();
        measureDimension(etNameFolder);
        /*etNameFolder.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(etNameFolder, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);*/
        dialog.show();
    }

    //Кнопка "Удалить директорию" из контекстного меню
    private void deleteDir(final String dirPath) {
        final CustomDialog dialog = new CustomDialog(this);
        dialog.setTextDialog(getString(R.string.file_manager_dialog_delete_folder_window_name),
                getString(R.string.file_manager_dialog_delete_folder_label_name_1)
                + new File(dirPath).getName()
                + getString(R.string.file_manager_dialog_delete_folder_label_name_2));
        dialog.addButton(getResources().getString(R.string.file_manager_dialog_delete_folder_button_delete), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibleProgressBar(true);
                FileExplorer fileExplorer = new FileExplorer();
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        Bundle bundle = msg.getData();
                        Boolean resultDel = bundle.getBoolean(FileExplorer.KEY_RESULT_OF_THREAD);
                        if (! resultDel) {
                            String errorMsg = bundle.getString(FileExplorer.KEY_THREAD_ERROR_MSG);
                            Toast.makeText(fileManagerScrollView.getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(fileManagerScrollView.getContext(), R.string.file_manager_dialog_delete_folder_deleted, Toast.LENGTH_SHORT).show();
                            browseTo(currentDirectory, false);
                        }
                        setVisibleProgressBar(false);
                        dialog.destroy();
                        currentThread = null;
                    }
                };
                currentThread = fileExplorer.deleteDirInThread(dirPath,
                        Thread.MAX_PRIORITY, handler);
                /*if (! fileExplorer.deleteDir(dirPath)) {
                    Toast.makeText(v.getContext(), fileExplorer.getError().getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.destroy();
                } else {
                    browseTo(currentDirectory, false);
                    dialog.destroy();
                }*/
            }
        });
        dialog.addButton(getResources().getString(R.string.file_manager_dialog_delete_folder_button_cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.destroy();
            }
        });
        dialog.show();
    }

    //Кнопка "Удалить файл" из контекстного меню
    private void delFile(final String filePath) {
        final CustomDialog dialog = new CustomDialog(this);
        File file = new File(filePath);
        dialog.setTextDialog(getString(R.string.file_manager_dialog_delete_file_window_name),
                getString(R.string.file_manager_dialog_delete_file_label_name) + file.getName()+ "\" ?");
        dialog.addButton(getResources().getString(R.string.file_manager_dialog_delete_file_button_delete), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileExplorer fileExplorer = new FileExplorer();
                if (! fileExplorer.deleteDir(filePath)){
                    Toast.makeText(v.getContext(), fileExplorer.getError().getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.destroy();
                } else {
                    browseTo(currentDirectory, false);
                    dialog.destroy();
                }
            }
        });
        dialog.addButton(getResources().getString(R.string.file_manager_dialog_delete_file_button_cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.destroy();
            }
        });
        dialog.show();
    }

    //Кнопка "Переименовать файл" из контекстного меню
    private void renameFile(final String filePath) {
        final CustomDialog dialog = new CustomDialog(this);
        String fileName = new File(filePath).getName();
        dialog.setInputDialog(getString(R.string.file_manager_dialog_rename_file_window_name),
                getString(R.string.file_manager_dialog_rename_file_label_name),
                getString(R.string.file_manager_dialog_rename_file_edittext_hint));
        final EditText etNameFile = dialog.getLayoutForContent().findViewById(R.id.editTextForInputDialog);
        etNameFile.setText(fileName);
        dialog.addButton(getResources().getString(R.string.file_manager_dialog_rename_file_button_rename), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etNameFolder = dialog.getLayoutForContent().findViewById(R.id.editTextForInputDialog);
                String newNameFile = etNameFolder.getText().toString().trim();
                if (checkFileName(newNameFile)) {
                    FileExplorer fileExplorer = new FileExplorer();
                    if (! fileExplorer.renameDir(filePath, newNameFile)) {
                        Toast.makeText(v.getContext(), fileExplorer.getError().getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.destroy();
                    } else {
                        browseTo(currentDirectory, false);
                        dialog.destroy();
                    }
                }
            }
        });
        dialog.addButton(getResources().getString(R.string.file_manager_dialog_create_folder_button_cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.destroy();
            }
        });
        int endIndexSelect = fileName.lastIndexOf('.');
        if (endIndexSelect < 0) endIndexSelect = fileName.length();
        etNameFile.setSelection(0, endIndexSelect);
        etNameFile.requestFocus();
        measureDimension(etNameFile);
        /*etNameFile.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(etNameFile, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);*/
        dialog.show();
    }

    // Кнопка "Переместить файл" из контекстного меню
    public void removeFile(final String filePath) {
        Button buttonOk = findViewById(R.id.fileManagerButtonOk);
        Button buttonCancel = findViewById(R.id.fileManagerButtonCancel);
        buttonOk.setText(getString(R.string.file_manager_bottom_panel_button_remove_file_ok));
        buttonCancel.setText(getString(R.string.file_manager_bottom_panel_button_remove_file_cancel));
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File sourceFile = new File(filePath);
                if (sourceFile.getParent().toString().equals(currentDirectory.toString())){
                    Toast.makeText(v.getContext(), getString(R.string.file_manager_remove_file_error_need_new_dir), Toast.LENGTH_SHORT).show();
                    return;
                }
                File destFile = new File(currentDirectory.toString() + File.separator + sourceFile.getName());
                if (destFile.exists()){
                    createReplaceFileDialog(filePath);
                } else {
                    runRemoveFile(filePath);
                }
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMode(MODE_NORMAL);
            }
        });
        setMode(MODE_REMOVE_FILE);
    }

    //Вспомогательный метод для запуска операции перемещения файла из метода removeFile
    private void runRemoveFile(String filePath) {
        FileExplorer fileExplorer = new FileExplorer();
        if (! fileExplorer.removeFile(filePath, currentDirectory.toString())) {
            String errorText = getString(R.string.file_manager_remove_file_error);
            Exception e = fileExplorer.getError();
            if (e != null) {
                errorText = errorText + "\n" + e.getMessage();
            }
            Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.file_manager_remove_file_done), Toast.LENGTH_SHORT).show();
        }
        setMode(MODE_NORMAL);
    }

    // Кнопка "Копировать файл" из контекстного меню
    public void copyFile(final String filePath) {
        Button buttonOk = findViewById(R.id.fileManagerButtonOk);
        Button buttonCancel = findViewById(R.id.fileManagerButtonCancel);
        buttonOk.setText(getString(R.string.file_manager_bottom_panel_button_copy_file_ok));
        buttonCancel.setText(getString(R.string.file_manager_bottom_panel_button_copy_file_cancel));
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File sourceFile = new File(filePath);
                if (sourceFile.getParent().toString().equals(currentDirectory.toString())){
                    Toast.makeText(v.getContext(), getString(R.string.file_manager_copy_file_error_need_new_dir), Toast.LENGTH_SHORT).show();
                    return;
                }
                File destFile = new File(currentDirectory.toString() + File.separator + sourceFile.getName());
                if (destFile.exists()){
                    createReplaceFileDialog(filePath);
                } else {
                    runCopyFile(filePath);
                }
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMode(MODE_NORMAL);
            }
        });
        setMode(MODE_COPY_FILE);
    }

    //Вспомогательный метод для запуска операции копирования файла из метода copyFile
    private void runCopyFile(String filePath) {
        setVisibleProgressBar(true);
        FileExplorer fileExplorer = new FileExplorer();
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                Boolean resultCopy = bundle.getBoolean(FileExplorer.KEY_RESULT_OF_THREAD);
                if (! resultCopy) {
                    String errorText = getString(R.string.file_manager_copy_file_error);
                    String errorMsg = bundle.getString(FileExplorer.KEY_THREAD_ERROR_MSG);
                    if (errorMsg.length()>0) {
                        errorText = errorText + "\n" + errorMsg;
                    }
                    Toast.makeText(fileManagerScrollView.getContext(), errorText, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(fileManagerScrollView.getContext(), getString(R.string.file_manager_copy_file_done), Toast.LENGTH_SHORT).show();
                }
                setVisibleProgressBar(false);
                setMode(MODE_NORMAL);
                currentThread = null;
            }
        };
        currentThread = fileExplorer.copyFileInThread(filePath,
                currentDirectory.toString(), Thread.MAX_PRIORITY, handler);
        /*if (! fileExplorer.copyFile(filePath, currentDirectory.toString())) {
            String errorText = getString(R.string.file_manager_copy_file_error);
            Exception e = fileExplorer.getError();
            if (e != null) {
                errorText = errorText + "\n" + e.getMessage();
            }
            Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.file_manager_copy_file_done), Toast.LENGTH_SHORT).show();
        }
        setMode(MODE_NORMAL);*/
    }

    //Открывает диалоговое окно для подтверждения замены существующего в директории файла на новый с тем же именем
    //В случае подтверждения замены файла вызывает метод runRemoveFile или runCopyFile в зависимости от значения modeOfWork
    private void createReplaceFileDialog(final String filePath) {
        final CustomDialog dialog = new CustomDialog(this);
        File file = new File(filePath);
        dialog.setTextDialog(getString(R.string.file_manager_dialog_delete_file_window_name),
                getString(R.string.file_manager_dialog_replace_file_label_name_1)+file.getName()+ getString(R.string.file_manager_dialog_replace_file_label_name_2));
        dialog.addButton(getResources().getString(R.string.file_manager_dialog_replace_file_button_replace), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (modeOfWork) {
                    case MODE_REMOVE_FILE:
                        runRemoveFile(filePath);
                        break;
                    case MODE_COPY_FILE:
                        runCopyFile(filePath);
                        break;
                }
                dialog.destroy();
            }
        });
        dialog.addButton(getResources().getString(R.string.file_manager_dialog_replace_file_button_cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.destroy();
            }
        });
        dialog.show();
    }

    //Установка режима работы Activity:
    //MODE_NORMAL - отображает папки, а также файлы, расширение которых передано при вызове Activity.
    // При этом fileManagerBottomPanelForButtons с кнопками скрыта
    //MODE_REMOVE_FILE - отображает только папки. При этом fileManagerBottomPanelForButtons отображается
    // с кнопками для подтверждения перемещения файла в текущую выбранную директорию.
    //MODE_COPY_FILE - отображает только папки. При этом fileManagerBottomPanelForButtons отображается
    // с кнопками для подтверждения копирования файла в текущую выбранную директорию.
    private void setMode(int mode) {
        LinearLayout bottomPanel = findViewById(R.id.fileManagerBottomPanelForButtons);
        //Button buttonOk = findViewById(R.id.fileManagerButtonOk);
        //Button buttonCancel = findViewById(R.id.fileManagerButtonCancel);
        switch (mode) {
            case MODE_NORMAL:
                modeOfWork = mode;
                btnFilterFileTypes.setEnabled(true);
                bottomPanel.setVisibility(View.GONE);
                break;
            case MODE_REMOVE_FILE:
            case MODE_COPY_FILE:
                modeOfWork = mode;
                btnFilterFileTypes.setEnabled(false);
                bottomPanel.setVisibility(View.VISIBLE);
                break;
            default:
                return;
        }
        printFileTypes();
        browseTo(currentDirectory, false);
    }

    // Изменение переключателя фильтра отображаемых типов файлов
    private void setFilterFileTypes (int filterFileTypes) {
        switch (filterFileTypes) {
            case FILTER_FILE_TYPE_LIST_EXTENSIONS:
                this.filterFileTypes = FILTER_FILE_TYPE_LIST_EXTENSIONS;
                break;
            case FILTER_FILE_TYPE_ALL_FILES:
                this.filterFileTypes = FILTER_FILE_TYPE_ALL_FILES;
                break;
            default:
                return;
        }
        printFileTypes();
        browseTo(currentDirectory, false);
    }

    //Нажатие на кнопку fileManagerBtnSearch в панели инструментов
    private void clickBtnSearch() {
        int visiblePanel = (searchPanel.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE;
        searchPanel.setVisibility(visiblePanel);
        if (editTextSearch.getText().toString().trim().length() > 0) {
            browseTo(currentDirectory, false);
        }
    }

    //Нажатие на кнопку fileManagerSearchPanelButtonFind в панели поиска fileManagerSearchPanel
    private void clickBtnFind() {
        String line = editTextSearch.getText().toString().trim().toLowerCase();
        if (! line.equals(lineForSearch)) {
            lineForSearch = line;
            browseTo(currentDirectory, false);
        }
    }

    private void clickBtnClearEditTextSearch() {
        editTextSearch.setText("");
        lineForSearch = "";
        browseTo(currentDirectory, false);
    }

    //Нажатие на кнопку Назад, двойное нажатие - выход из приложения
    public void onBackPressed() {
        if (findViewById(R.id.fileManagerProgressBar).getVisibility() == View.VISIBLE) {
            /*currentThread.interrupt();
            setVisibleProgressBar(false);
            LinearLayout bottomPanel = findViewById(R.id.fileManagerBottomPanelForButtons);
            if (bottomPanel.getVisibility() == View.VISIBLE)
                bottomPanel.setVisibility(View.GONE);*/
            Toast.makeText(this, R.string.file_manager_work_thread_no_exit, Toast.LENGTH_SHORT).show();
            return;
        }
        if (listLastDirectories.size()>0){
            File directory = listLastDirectories.get(listLastDirectories.size()-1);
            if (directory.exists()) {
                listLastDirectories.remove(listLastDirectories.size()-1);
                browseTo(directory, false);
            } else {
                listLastDirectories.clear();
            }
        }else if (modeOfWork != MODE_NORMAL) {
            try {
                Button buttonCancel = findViewById(R.id.fileManagerButtonCancel);
                buttonCancel.callOnClick();
            } catch(Exception e) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }


    private Spannable coloreSubString(final String text) {
        int argb = Color.parseColor(colorSearchSubString);
        String word = lineForSearch;
        final Spannable spannable = new SpannableString(text);
        int start = text.indexOf(word);
        if (start<0) start = 0;
        spannable.setSpan(
                new ForegroundColorSpan(argb),start,start+word.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        spannable.setSpan(
                new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                start, start+word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        /*int substringStart=0;
        int start;
        while((start=text.indexOf(word,substringStart))>=0){
            spannable.setSpan(
                    new ForegroundColorSpan(argb),start,start+word.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            substringStart = start+word.length();
        }*/
        return spannable;
    }

    private void setVisibleProgressBar(Boolean setVisible) {
        ProgressBar progressBar = findViewById(R.id.fileManagerProgressBar);
        LinearLayout layoutForProgressBar = findViewById(R.id.fileManagerLayoutForProgressBar);
        if (setVisible) {
            layoutForProgressBar.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            layoutForProgressBar.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void printFileTypes() {
        TextView textViewFileTypes = findViewById(R.id.fileManagerTextViewfileTypes);
        String text = "";
        if (modeOfWork != MODE_NORMAL) {
            text = "folders only";
        } else if (filterFileTypes == FILTER_FILE_TYPE_ALL_FILES) {
            text = "all files";
        } else {
            text = getFormatListExtensions();
        }
        textViewFileTypes.setText(text);
    }

    private String getFormatListExtensions () {
        String text = "";
        for (String s: listEnabledExtensions) {
            text = text + "*." + s + ", ";
        }
        text = text.substring(0, text.length()-2);
        return text;
    }

    // По расширению файла extensions возвращает иконку, которая будет отображаться для данного типа файлов
    private @DrawableRes int loadResFromExtension(String extension) {
        @DrawableRes int result = 0;
        boolean isExten = false;
        for (ResForExtensions resForExtensions: listResForExtensions.getList()) {
            for (String exten: resForExtensions.getListExtensions()) {
                if (extension.equals(exten)) {
                    isExten = true;
                    result = resForExtensions.getRes();
                    break;
                }
            }
            if (isExten) break;
        }
        if (! isExten) {
            result = listResForExtensions.getDefaultRes();
        }
        return result;
    }

}
