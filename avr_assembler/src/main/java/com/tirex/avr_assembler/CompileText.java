package com.tirex.avr_assembler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class CompileText {
    private ArrayList<String> lines;
    private ArrayList<Short> machineCode=new ArrayList<>();
    private String errorsText="";
    private ArrayList<String> listErrors = new ArrayList<>();
    private int countErrors;

    public CompileText( String[] lines){
        this.lines=new ArrayList<String>();
        Collections.addAll(this.lines, lines);
    }

    public String getLine(int index){
        if (index>=0 && index<this.lines.size()){
            return this.lines.get(index);
        }else{
            return null;
        }
    }

    public int getCountLines(){
        return this.lines.size();
    }

    public int sizeMachineCodeInWords(){
        return machineCode.size();
    }

    public String getErrorsText(){
        return errorsText;
    }

    private void addToMachineCode(Short code){
        this.machineCode.add(code);
    }

    public ArrayList<Short> getMachineCode() {
        return machineCode;
    }

    public ArrayList<String> getErrors(){ return listErrors; }

    public int getCountErrors(){  return listErrors.size(); }

    private void convertLineToMachineCode(int index){

    }

    public void convertTextToMachineCode(){
        /*
        Lexer lexerListing;
        String text;
        Parser parserListing = new Parser();

        lexerListing = new Lexer(lines);
        if (lexerListing.countErrors()>0){
            for (int i=0; i<lexerListing.countErrors(); i++){
                text = "";
                switch(MainActivity.language){
                    case 1:
                        text="Ошибка в строке "+Integer.toString(lexerListing.getErrorNumLine(i)+1) + ": ";
                        break;
                    case 2:
                        text="Error in line "+Integer.toString(lexerListing.getErrorNumLine(i)+1) + ": ";
                        break;
                }
                text += lexerListing.getFullTextError(i, MainActivity.language);
                listErrors.add(text);
            }
        }else{ // если в лексере ошибок нет, то переходим к работе парсера
            listErrors.add("Парсер");
            parserListing.lexerToParserText(lexerListing.lexerText);
            if (parserListing.countErrors()>0) {
                for (int i = 0; i < parserListing.countErrors(); i++) {
                    text = "";
                    switch (MainActivity.language) {
                        case 1:
                            text = "Ошибка в строке " + Integer.toString(parserListing.getErrorNumLine(i) + 1) + ": ";
                            break;
                        case 2:
                            text = "Error in line " + Integer.toString(lexerListing.getErrorNumLine(i) + 1) + ": ";
                            break;
                    }
                    text += parserListing.getFullTextError(i, MainActivity.language);
                    listErrors.add(text);
                }
            }  // конец  if (parserListing.countErrors()>0)
        }

//        for (int i=0; i<this.lines.length; i++){
//            convertLineToMachineCode(i);
//        }*/
        CollecterCode collecterCode = new CollecterCode();
        MakerCode maker = new MakerCode(lines, collecterCode);
        for (int i=0; i<collecterCode.errors.size(); i++){
            listErrors.add (collecterCode.errors.get(i).getTextError());
        }
        listErrors.add ("Count deferred "+ collecterCode.deferredErrors.size());

        String s="", s1;
        for (int i=0; i<collecterCode.code.countWords(); i++) {
            s1 =Integer.toHexString(collecterCode.code.getWord(i) & 0x0000FFFF).toUpperCase();
            for (int j = s1.length(); j < 4; j++) {
                s1 = "0" + s1;
            }
            s = s + s1 + " ";
        }
        listErrors.add(s);
    }
}
