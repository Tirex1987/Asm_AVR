package com.tirex.avr_assembler;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class FileExplorer {

    public static final String KEY_RESULT_OF_THREAD = "Result";
    public static final String KEY_THREAD_ERROR_MSG = "Error";
    private Exception error;

    // создает папку во внутренней памяти телефона
    public String createExternalStorageFolder(String pathFolder){
        String createdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + pathFolder;
        if (createDir(createdPath)){
            return createdPath;
        }
        return null;
    }

    // метод проверяет, доступно ли External Storage для чтения и записи
    public boolean isExternalStorageWritable(){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)){
            return true;
        }
        return false;
    }

    public boolean createDir (String dirPath){
        File fdir = new File (dirPath);
        if (isExternalStorageWritable()) {
            try {
                if (!fdir.exists()) {
                    fdir.mkdirs();
                }
            } catch (Exception e) {
                error = e;
                return false;
            }
            return true;
        }
        return false;
    }

    // возвращает true, если директория dirPath уже существует
    public boolean isDir (String dirPath){
        File fdir = new File (dirPath);
        try {
            if (fdir.exists()) {
                return true;
            }
        } catch (Exception e) {
            error = e;
        }
        return false;
    }

    public void saveFile (String filePath, ArrayList<String> fileContent){
        File fhandle = new File (filePath);
        try{
            if (! fhandle.getParentFile().exists()) // Если нет директорий в пути, то они будут созданы
                fhandle.getParentFile().mkdirs();
            fhandle.createNewFile(); // Если файл существует, то он будет перезаписан
            FileOutputStream fOut = new FileOutputStream(fhandle);
            OutputStreamWriter outWriter = new OutputStreamWriter(fOut);
            for (int i=0; i<fileContent.size(); i++){
                outWriter.write(fileContent.get(i)+"\n");
            }
            outWriter.close();
            fOut.close();
        }catch(IOException e){
            error = e;
        }
    }

    public void saveFile (String filePath, String[] fileContent){
        File fhandle = new File (filePath);
        try{
            if (! fhandle.getParentFile().exists()) // Если нет директорий в пути, то они будут созданы
                fhandle.getParentFile().mkdirs();
            fhandle.createNewFile(); // Если файл существует, то он будет перезаписан
            FileOutputStream fOut = new FileOutputStream(fhandle);
            OutputStreamWriter outWriter = new OutputStreamWriter(fOut);
            for (int i=0; i<fileContent.length; i++){
                outWriter.write(fileContent[i]+"\n");
            }
            outWriter.close();
            fOut.close();
        }catch(IOException e){
            error = e;
        }
    }

    public void createEmptyFile (String filePath){
        File fhandle = new File (filePath);
        try{
            if (! fhandle.getParentFile().exists()) // Если нет директорий в пути, то они будут созданы
                fhandle.getParentFile().mkdirs();
            fhandle.createNewFile(); // Если файл существует, то он будет перезаписан
        }catch(IOException e){
            error = e;
        }
    }

    public ArrayList<String> readFile(String fullFileName){
        File file = new File (fullFileName);
        ArrayList<String> text = new ArrayList<>();
        Collections.addAll(text, readFile(file.getParent(), file.getName()).toString().split("\n"));
        return text;
        /*ArrayList<String> text = new ArrayList<>();
        File file = new File(fullFileName);
        try {
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while (true){
            if (!((line = br.readLine()) != null)) break;
            text.add(line);
        }
        br.close();
        } catch (FileNotFoundException e) {
            Log.e("FileNotFoundException", e.getMessage());
        } catch (IOException e) {
            Log.e("IOException", e.getMessage());
        }*/
        /*String content = "";
        ArrayList<String> text = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                content = new String(Files.readAllBytes(Paths.get(fullFileName)));
            } catch (IOException e) {
                Log.e("IOException", e.getMessage());
            }
        }
        Collections.addAll(text, content.split("\n"));
        return text;*/
    }

    public CharSequence readFile(String filePath, String fileName){
       /* ArrayList<String> text = new ArrayList<>();
        List<String> lines;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                return Files.readAllLines(Paths.get(filePath + File.separator + fileName));
            } catch (IOException e) {
                error = e;
            }
        }
        return null;*/
        /*File file = new File (filePath, fileName);
        ArrayList<String> text = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.add(line);
            }
        }catch(IOException e){
            error = e;
        }
        return text;*/
        //ArrayList<String> text = new ArrayList<>();
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                text = (ArrayList<String>) Files.readAllLines(Paths.get(filePath + File.separator +fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        /*String text = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                text = new String(Files.readAllBytes(Paths.get(filePath + File.separator +fileName)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return text;*/
        FileInputStream fin = null;
        CharSequence text = new StringBuilder();
        try {
            fin = new FileInputStream(filePath + File.separator +fileName);
            byte[] bytes = new byte[fin.available()];
            fin.read(bytes);
            text = new String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (IOException e) {

            }
         }
        return text;
        /*Stream<String> stream;
        String text = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                stream = Files.lines(Paths.get(filePath + File.separator +fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return text;*/
    }

    //Проверяет, допустимое ли имя файла fullFileName
    public boolean checkFileName (String path, String fileName){
        final char[] illegal_charcters = {
                '/', '\n', '\r', '\t', '\f', '\'', '?',
                '*', '\\', '<', '>', '|', '"', ':'
        };
        String fullFileName = path + "/" + fileName;
        if (fileName.isEmpty()) return false;
        if (fileName.charAt(0) == ' ' || fileName.charAt(0) == '.') return false;
        for (int i=0; i<illegal_charcters.length; i++){
            if (fileName.indexOf(illegal_charcters[i]) > -1) return false;
        }
        File file = new File (fullFileName);
        if (file.exists()){
            return false;
        }
        createEmptyFile(fullFileName);
        if (error == null){
            file.delete();
            return true;
        }
        return false;
    }

    //Переименовать директорию. dirPath - абсолютный путь к текущей директории, newNameDir - новое имя директории
    public boolean renameDir (String dirPath, String newNameDir){
        File dir = new File (dirPath);
        File newDir = new File (dir.getParent().toString() + File.separator + newNameDir);
        try {
            dir.renameTo(newDir);
        } catch (Exception e) {
            error = e;
            return false;
        }
        return true;
    }

    //Запуск удаления папки в отдельном потоке, приоритет потока задается через priority
    public Thread deleteDirInThread(final String pathDir, int priority, final Handler handler) {
        if (priority < Thread.MIN_PRIORITY) priority = Thread.MIN_PRIORITY;
        if (priority > Thread.MAX_PRIORITY) priority = Thread.MAX_PRIORITY;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                boolean res = deleteDir(pathDir);
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putBoolean(KEY_RESULT_OF_THREAD, res);
                if (! res) {
                    String errorMsg = (error != null) ? getError().getMessage() : "";
                    bundle.putString(KEY_THREAD_ERROR_MSG, errorMsg);
                }
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        };
        Thread thread = new Thread(runnable);
        thread.setPriority(priority);
        thread.start();
        return thread;
    }

    public boolean deleteDir (String pathDir) {
        error = null;
        return deleteDir(new File(pathDir));
    }

    public boolean deleteDir(File file) {
        File[] contents = file.listFiles();
        try {
            if (contents != null) {
                for (File f : contents) {
                    if ((! f.getName().equals(".") && (! f.getName().equals("..")))){
                        deleteDir(f);
                        if (error != null) return false;
                    }
                }
            }
            file.delete();
        } catch(Exception e) {
            error = e;
            return false;
        }
        return true;
    }

    //Перемещает файл removedFile в директорию targetDir
    public boolean removeFile(String removedFile, String targetDir) {
        File fileForRemove = new File(removedFile);
        String fileName = fileForRemove.getName();
        File newName = new File(targetDir + File.separator + fileName);
        try {
            return fileForRemove.renameTo(newName);
        } catch (Exception e) {
            error = e;
            return false;
        }
    }

    //Запуск копирования файла в отдельном потоке, приоритет потока задается через priority
    public Thread copyFileInThread(final String copiedFile, final String dirForFileCopy, int priority, final Handler handler) {
        if (priority < Thread.MIN_PRIORITY) priority = Thread.MIN_PRIORITY;
        if (priority > Thread.MAX_PRIORITY) priority = Thread.MAX_PRIORITY;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                boolean res = copyFile(copiedFile, dirForFileCopy);
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putBoolean(KEY_RESULT_OF_THREAD, res);
                if (! res) {
                    String errorMsg = (error != null) ? getError().getMessage() : "";
                    bundle.putString(KEY_THREAD_ERROR_MSG, errorMsg);
                }
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        };
        Thread thread = new Thread(runnable);
        thread.setPriority(priority);
        thread.start();
        return thread;
    }

    //Копирует файл copiedFile в директорию dirForFileCopy
    public boolean copyFile(String copiedFile, String dirForFileCopy) {
        boolean result = true;
        //boolean closeThread = false;
        File source = new File(copiedFile);
        String fileName = source.getName();
        File dest = new File(dirForFileCopy + File.separator + fileName);
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
                /*if (Thread.currentThread().isInterrupted()) {
                    closeThread = true;
                    break;
                }*/
            }
        } catch (Exception e) {
            error =e;
            result = false;
        } finally {
            try {
                is.close();
                os.close();
                /*if (closeThread) {
                    result = false;
                    dest.delete();
                }*/
            } catch (Exception e) {
                error = e;
                result = false;
            }
        }
        return result;
    }

    public Exception getError(){
        Exception er=error;
        error = null;
        return er;
    }

}
