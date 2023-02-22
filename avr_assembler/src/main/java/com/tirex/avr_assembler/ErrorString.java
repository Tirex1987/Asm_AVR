package com.tirex.avr_assembler;

public class ErrorString{
    private int numLine; // номер строки
    private int numError; // код ошибки
    private String errorWord; // слово, в котором ошибка
    private String textError; // текст ошибки

    public ErrorString (int numError, String errorWord, int numLine){
        this.numError = numError;
        this.errorWord = errorWord;
        this.numLine = numLine;
    } // конец конструктора ErrorString

    public int getNumError(){
        return numError;
    }

    public String getErrorWord(){
        return errorWord;
    }

    public int getNumLine(){
        return numLine;
    }

    public void setTextError (String textError){
        this.textError = textError;
    }

    public String getTextError(){
        return textError;
    }

} // конец класса ErrorString
