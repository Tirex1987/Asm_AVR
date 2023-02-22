package com.tirex.avr_assembler;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class CustomDialog {
    private Dialog ad;
    private AlertDialog.Builder adb;
    private LinearLayout layoutForContent;
    private LinearLayout layoutForButtons;
    private Context context;
    private TextView textViewTitle;
    //private Drawable buttonBackground;
    //private Drawable buttonForeground;
    private ViewGroup.LayoutParams buttonLayoutParams;

    public CustomDialog (Context context){
        this.context = context;
        View view;
        Button button;
        adb = new AlertDialog.Builder(context);
        View customTitle = View.inflate(adb.getContext(), R.layout.custom_dialog_title, null);
        adb.setCustomTitle(customTitle);
        textViewTitle = customTitle.findViewById(R.id.customDialogTitle);
        view = View.inflate(adb.getContext(), R.layout.custom_dialog, null);
        adb.setView(view);
        layoutForContent = view.findViewById(R.id.layoutForContent);
        layoutForButtons = view.findViewById(R.id.layoutForButtons);
        button = View.inflate(adb.getContext(), R.layout.custom_dialog, null).findViewById(R.id.buttonInDialog);
        /*buttonBackground = button.getBackground();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            buttonForeground = button.getForeground();
        }*/
        buttonLayoutParams = button.getLayoutParams();
        //button.setVisibility(View.GONE);
        ad = adb.create();
    }

    public void setView (@LayoutRes int resource){
        View view = View.inflate(adb.getContext(), resource, null);
        layoutForContent.addView(view);
        ad = adb.create();
    }

    public void setTitle (String title){
        textViewTitle.setText(title);
        ad = adb.create();
    }

    public void show (){
        ad.show();
    }

    public void addButton (String textButton, View.OnClickListener onClickListener){
        Resources resources = context.getResources();
        //Button button = new Button(adb.getContext());
        //FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        //layoutParams.setMargins(0, 0, 0, 0);
       // button.setLayoutParams(buttonLayoutParams);
     /*   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            button.setForeground(buttonForeground);
            button.setBackgroundColor(resources.getColor(R.color.colorPanelButtons));
        } else {*/
       //     button.getBackground().setColorFilter(resources.getColor(R.color.colorPanelButtons), PorterDuff.Mode.MULTIPLY);
       // }
        //button.setBackgroundResource(R.style.StyleButtonsInPanel);
        int buttonStyle = R.style.StyleButtonsInDialog;
        Button button = new Button(new ContextThemeWrapper(adb.getContext(), buttonStyle), null, buttonStyle);
        //FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        //layoutParams.setMargins(0, 0, 0, 0);
        button.setLayoutParams(buttonLayoutParams);
        button.setText(textButton);
        button.setOnClickListener(onClickListener);
        layoutForButtons.addView(button);
        ad = adb.create();
    }

    public void destroy (){
        ad.dismiss();
    }

    public void hide () { ad.hide(); }

    public View getLayoutForContent (){
        return layoutForContent;
    }

    public void setTextDialog(String title, String text) {
        setView(R.layout.text_dialog);
        setTitle(title);
        TextView textView = getLayoutForContent().findViewById(R.id.textForTextDialog);
        textView.setText(text);
    }

    public void setInputDialog(String title, String text, String hint) {
        setView(R.layout.input_dialog);
        setTitle(title);
        TextView textView = getLayoutForContent().findViewById(R.id.textForInputDialog);
        textView.setText(text);
        EditText editText = getLayoutForContent().findViewById(R.id.editTextForInputDialog);
        editText.setHint(hint);
    }

}
