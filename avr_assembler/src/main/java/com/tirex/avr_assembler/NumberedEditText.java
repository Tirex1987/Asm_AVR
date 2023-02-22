package com.tirex.avr_assembler;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class NumberedEditText extends ScrollView {

    private final int startIndentForNumbers = getPx(4);
    private final int endIndentForNumbers = getPx(4);
    private final int minCountDigitsInNumbers = 2;
    private Paint mPaint;
    private int colorNumber = 0xFFAAAAAA;//Color.GRAY;
    private int colorNumberBackground = 0xFFD8EFCF; //0xFFE8FFDF;//0xFFDFFFDF;//0xFFADFAAD; //0xFFEAEAEA
    private int colorTextBackground = 0xFFF8F8F8; //0xFFFAFAFA;
    private float textSize = 18;
    private CustomTextView textView;
    //private DrawView drawView;
    private EditText editText;
    private LinearLayout linearLayout;
    private HorizontalScrollView horizontalScrollView;
    private View verticalDivider;
    private static final String indent = "     "; // Отступ в начале каждой строки editText
    private boolean markEnter = false;
    private boolean runCheckPressEnter = false;
    private float indentForNumbers;
    private int currentDigitCount;

    public NumberedEditText (Context context){
        super(context);
        init(context);
    }

    private void init(Context context){
        /*mPaint = new Paint();
        mPaint.setColor(0xFF000000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTextSize(textSizeNumber);*/

        editText = new EditText(context);
        settingsEditText();
        horizontalScrollView = new HorizontalScrollView(context);
        settingsHorizontalScrollView();
        horizontalScrollView.addView(editText);
        textView = new CustomTextView(context);
        settingsTextView();
        //drawView = new DrawView(context);
        //settingsDrawView();
        verticalDivider = new View(context);
        settingsVerticalDivider();
        linearLayout = new LinearLayout(context);
        settingsLinearLayout ();
        linearLayout.addView(textView);
        //linearLayout.addView(drawView);
        linearLayout.addView(verticalDivider);
        linearLayout.addView(horizontalScrollView);
        settingsScrollView (this);
        this.addView(linearLayout);

    }

    /*private class DrawView extends View {
        public DrawView (Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int lineHeight = editText.getLineHeight();
            int lineCount = editText.getLineCount();
            int gutterDigitCount = String.valueOf(lineCount).length();
            if (gutterDigitCount > 2) {
                int widthDrawView = (int) ((gutterDigitCount +1) * textSize);
                if (drawView.getWidth() != widthDrawView) {
                    drawView.setLayoutParams(new LinearLayout.LayoutParams(getPx(30), ActionBar.LayoutParams.MATCH_PARENT));
                }
            }
            int topVisibleLine = getTopVisibleLine();
            topVisibleLine = (topVisibleLine > 2) ?
                    topVisibleLine - 2: 0;

            for (int i = topVisibleLine; i <= getBottomVisibleLine(); i++) {
                canvas.drawText(" " + String.valueOf(i + 1), 0, i * lineHeight + lineHeight,     mPaint);
            }

            super.onDraw(canvas);
        }
    }*/

    /*@Override
    protected void onDraw (Canvas canvas){
    //    textView.setBackgroundResource(R.drawable.border_textview);
    //    editText.setBackgroundResource(R.drawable.border_textview);
        mPaint.setColor(0xFF000000);
        canvas.drawLine(textView.getWidth(), 0, textView.getWidth()*2, textView.getHeight(), mPaint);
        super.onDraw(canvas);
    }*/

    private void settingsTextView(){
        textView.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.MATCH_PARENT));
        textView.setTextSize(textSize);
        textView.setTextColor(colorNumberBackground);
        textView.setBackgroundColor(colorNumberBackground);
        //textView.setGravity(Gravity.RIGHT);
        textView.setPadding(0,0, textView.indent * 2,0);
        CharSequence lineDigits = buildWidestDigitsString(minCountDigitsInNumbers);
        textView.setText(lineDigits);

        mPaint = new Paint();
        mPaint.setColor(colorNumber);
        mPaint.setTextAlign(Paint.Align.RIGHT);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(textView.getTextSize());//getPx((int) textSize));
    }

    /*private void settingsDrawView() {
        drawView.setLayoutParams(new LinearLayout.LayoutParams(getPx((int) textSize * 2), ActionBar.LayoutParams.MATCH_PARENT));
        drawView.setBackgroundColor(colorNumberBackground);
        mPaint = new Paint();
        mPaint.setColor(colorNumber);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTextSize(getPx((int) textSize));
    }*/

    /*public class customEditText extends android.support.v7.widget.AppCompatEditText {

        public customEditText (Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (editText.getLayout() != null) {
                int lineHeight = editText.getLineHeight();
                int lineCount = editText.getLineCount();
                int digitCountInLineCount = String.valueOf(lineCount).length();
                if (digitCountInLineCount != currentDigitCount) {
                    indentForNumbers = calcIndentForNumbers();
                    currentDigitCount = digitCountInLineCount;

                    editText.setPadding(startIndentForNumbers + (int) indentForNumbers + endIndentForNumbers,
                            5, 5, 5);
                }
                int topVisibleLine = getTopVisibleLine();
                topVisibleLine = (topVisibleLine > 2) ?
                        topVisibleLine - 2 : 0;

                for (int i = topVisibleLine; i <= getBottomVisibleLine(); i++) {
                    canvas.drawText(" " + String.valueOf(i + 1), 0, i * lineHeight + lineHeight, mPaint);
                }
            }
            super.onDraw(canvas);
        }

    }*/

    @SuppressLint("AppCompatCustomView")
    public class CustomTextView extends TextView {
        private int maxLines;
        protected int indent;

        public CustomTextView(Context context) {
            super(context);
            this.indent = (int) this.getPaint().measureText("6") / 2;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            //super.onDraw(canvas);
            if (this.getLayout() != null) {
                int lineHeight = editText.getLineHeight();
                int lineCount = editText.getLineCount();
                if (this.maxLines != lineCount) {
                    this.maxLines = lineCount;
                    this.setLines(this.maxLines);
                }
                int digitCountInLineCount = String.valueOf(lineCount).length();
                if (digitCountInLineCount != currentDigitCount && digitCountInLineCount >= minCountDigitsInNumbers) {
                    CharSequence lineDigits = buildWidestDigitsString(digitCountInLineCount);
                    textView.setText(lineDigits);
                    currentDigitCount = digitCountInLineCount;
                }
                int topVisibleLine = getTopVisibleLine();
                topVisibleLine = (topVisibleLine > 2) ?
                        topVisibleLine - 2 : 0;

                for (int i = topVisibleLine; i <= getBottomVisibleLine(); i++) {
                    canvas.drawText(String.valueOf(i + 1), getWidth() - this.indent, i * lineHeight + lineHeight, mPaint);
                }
            }
            super.onDraw(canvas);
        }
    }

    private void settingsVerticalDivider (){
        verticalDivider.setLayoutParams(new LinearLayout.LayoutParams(1, ActionBar.LayoutParams.MATCH_PARENT));
        verticalDivider.setBackgroundColor(0xFFB0B0B0);
    }

    private void settingsLinearLayout (){
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
    }

    private void settingsEditText (){
        editText.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
        editText.setTextSize(textSize);
        editText.setCursorVisible(true);
        editText.setText(indent);
        editText.setSelection(indent.length());
        editText.setGravity(Gravity.START);
        editText.setPadding(5,//startIndentForNumbers + (int) calcIndentForNumbers() + endIndentForNumbers,
                5,5,5);
        editText.setBackgroundColor(colorTextBackground);

        /*mPaint = new Paint();
        mPaint.setColor(Color.BLACK);//colorNumber);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTextSize(getPx((int) textSize));*/

        editText.setHorizontalScrollBarEnabled(true);

        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setInputType(editText.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setHorizontallyScrolling(true);

        editText.addTextChangedListener(new TextWatcher() {
            /*char beforeSymbol = '1';
            int startSymbol;
            boolean isProbel;*/
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                /*if (start>1){
                    beforeSymbol = s.charAt(start-2);
                } else beforeSymbol = '1';*/
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ((s.length()>start)&&(count>0)&&(s.charAt(start+count-1)=='\n')){
                    if (editText.getLayout() == null) return; // При загрузке содержимого файла editText только создается, поэтому getLayout возвращает null
                    int line = editText.getLayout().getLineForOffset(editText.getSelectionStart());
                    int column = editText.getSelectionStart() - editText.getLayout().getLineStart(line-1);
                    if (column>0){
                        markEnter = true;
                    }
                }
              /*  if ((s.length()>start)&&(s.charAt(start+count-1)==' ')&&(start>0)){
                    startSymbol = start;
                    isProbel = true;
                }*/
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (! runCheckPressEnter) {
                    textView.invalidate();
                    //writeTextView();
                    //writeView();
                    //editText.requestLayout();
                    checkPressEnter();
                }
               /* if (isProbel){
                    Toast.makeText(getContext(), "probel", Toast.LENGTH_SHORT).show();
                    if ((beforeSymbol==' ')&&(s.charAt(startSymbol)!=' ')){
                        isProbel = false;
                        /*char[] probel = {' '};
                        editText.setText(probel,start+count-2, 1);*/
                      /*  Toast.makeText(getContext(), "probel2", Toast.LENGTH_SHORT).show();
                    }
                }*/
            }
        });
    }

    private void settingsScrollView(ScrollView scrollView){
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
        scrollView.setFillViewport(true);
    }

    private void settingsHorizontalScrollView(){
        horizontalScrollView.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
        horizontalScrollView.setFillViewport(true);
    }

    public Editable getText(){
        return editText.getText();
    }

  /*  public void setText (List<String> text) {
        StringBuilder line = new StringBuilder();
        for (int i=0; i<text.size(); i++){
            line.append(text.get(i));
            if (i != text.size()-1) {  line.append("\n");  }
        }
        editText.setText(line);
        measureDimension(editText);
    }*/

    public void setText (CharSequence text) {
        if (text != null) {
            editText.setText(text);
        }
        measureDimension(editText);
    }

    private void measureDimension (View v) {
        v.post(new Runnable() {
            @Override
            public void run() {
                //writeTextView();
            }
        });
    }

    public void setCursorPosition(int cursorPosition) {
        if (cursorPosition<0 || cursorPosition>editText.getText().length()){
            cursorPosition = 0;
        }
        editText.setSelection(cursorPosition);
    }

    public int getCursorPosition() {
        return editText.getSelectionStart();
    }

    private void writeTextView(){
        /*String str=" 1";
        int lineCount = editText.getLineCount();
        for (int i=1; i<lineCount; i++)
            str=str+"\n"+(i+1);
        textView.setText(str);*/
        int line = editText.getLineCount() - textView.getLineCount();
        if (line == 0) return;
        StringBuilder str = new StringBuilder();
        textView.setLines(editText.getLineCount());
        if (line > 0) {
            /*str.append(textView.getText());
            textView.getEditableText().delete(
                    str.indexOf(String.valueOf(textView.getLineCount() - line)),
                    textView.length()
            );*/
            for (int i=textView.getLineCount(); i<editText.getLineCount(); i++) {
                str.append("\n"+(i+1));
            }
            textView.append(str);
        }// else if (line<0) {
            /*str.append(textView.getText());
            String lineCount = String.valueOf(editText.getLineCount());
            int index = str.indexOf(lineCount);
            textView.setText(str.substring(0, index + lineCount.length()));*/
          //  textView.setLines(editText.getLineCount());
        //}
    }

    //private void writeView() {
        //drawView.invalidate();
    //}

    private void checkPressEnter(){
        if (markEnter){
            markEnter = false;
            runCheckPressEnter = true;
            int positionEndSubStr, cursorPosition;
            StringBuilder indentStr = new StringBuilder();
            cursorPosition = editText.getSelectionStart();
            if (cursorPosition + indent.length() > editText.getText().length()){
                positionEndSubStr =  editText.getText().length();
            } else {
                positionEndSubStr = cursorPosition + indent.length();
            }
            CharSequence subStr = editText.getText().subSequence(cursorPosition, positionEndSubStr).toString();
            if (subStr.length()==0){
                indentStr.append(indent);
            } else {
                for (int i = 0; i < subStr.length() && i<indent.length(); i++) {
                    if (subStr.charAt(i) != ' ') {
                        indentStr.append(indent.toCharArray(), i, indent.length() - i); //= String.copyValueOf(indent.toCharArray(), i, indent.length() - i);
                        break;
                    }
                }
            }
            if (indentStr.length()>0) {
                editText.getText().insert(cursorPosition, indentStr);
            }
            runCheckPressEnter = false;
        }
    }

    public int getPx(int dp){
        float scale = getResources().getDisplayMetrics().density;
        return((int) (dp * scale + 0.5f));
    }

    //Возвращает номер верхней видимой строки editText
    private int getTopVisibleLine() {
        int line = editText.getLayout().getLineForVertical(editText.getScrollY());
        if (line < 0) return 0;
        int lineCount = editText.getLineCount();
        return (line >= lineCount) ? --lineCount : line;
    }

    //Возвращает номер нижней видимой строки editText
    private int getBottomVisibleLine() {
        int line = editText.getLayout().getLineForVertical(editText.getScrollY() + editText.getHeight());
        if (line < 0) return 0;
        int lineCount = editText.getLineCount();
        return (line >= lineCount) ? lineCount -1 : line;
    }

    private float calcIndentForNumbers() {
        float indentWidth;
        int lineCount = editText.getLineCount();
        int indentDigitsCount = String.valueOf(lineCount).length();
        if (indentDigitsCount < minCountDigitsInNumbers)
                indentDigitsCount = minCountDigitsInNumbers;

        float widestWidthDigit = 0;
        int widestDigit = 0;
        for (int i = 0; i <= 9; i++) {
            float width = editText.getPaint().measureText(String.valueOf(i));
            if (width > widestWidthDigit) {
                widestWidthDigit = width;
                widestDigit = i;
            }
        }

        StringBuilder lineBuilder = new StringBuilder();
        for (int i = 0; i < indentDigitsCount; i++) {
            lineBuilder.append(String.valueOf(widestDigit));
        }
        indentWidth = editText.getPaint().measureText(lineBuilder.toString());
        return indentWidth;
    }

    private CharSequence buildWidestDigitsString(int count) {
        float widestWidthDigit = 0;
        int widestDigit = 0;
        for (int i = 0; i <= 9; i++) {
            float width = textView.getPaint().measureText(String.valueOf(i));
            if (width > widestWidthDigit) {
                widestWidthDigit = width;
                widestDigit = i;
            }
        }
        StringBuilder lineBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            lineBuilder.append(String.valueOf(widestDigit));
        }
        return lineBuilder;
    }
}
