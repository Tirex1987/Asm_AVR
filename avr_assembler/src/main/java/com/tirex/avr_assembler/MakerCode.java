package com.tirex.avr_assembler;

import java.util.ArrayList;

public class MakerCode {

    private CollecterCode collecterCode; // объект типа CollecterCode, в который будут вноситься все результаты обработки строк
    private String includeFileName; // если MakerCode вызван для вложенного файла, то содержит имя этого файла (для записи в текст при ошибках)
    private boolean exitDirective; // если встретилась директива exit, то устанавливается в true
    private ProcessorInstructions processorInstructions; // обработчик инструкций

    // конструктор
    public MakerCode (ArrayList<String> listing, CollecterCode collecterCode){
        this.collecterCode = collecterCode;
        processorInstructions = new ProcessorInstructions (collecterCode);
        compile (listing);
    } // конец конструктора

    // конструктор для обработки вложенного файла
    public MakerCode (ArrayList<String> listing, String includeFileName, CollecterCode collecterCode){
        this.includeFileName = includeFileName;
        this.collecterCode = collecterCode;
        processorInstructions = new ProcessorInstructions (collecterCode);
        compile (listing);
    } // конец конструктора для include файла

    // основной метод обработки листинга
    private void compile (ArrayList<String> listing){
        Lexer lexer = new Lexer();
        Parser parser = new Parser();
        Parser.ParserString parserString;
        for (int i=0; i<listing.size(); i++){ // цикл по всем строкам листинга
            lexer.lineOfListingToLexer (listing.get(i));
            lexerErrors (lexer, i);
            if ((lexer.getLexerLine() != null)&&(lexer.getLexerLine().size()>0)){ // если строка лексера не пустая
                parserString = parser.lineOfLexerToParser (lexer.getLexerLine());
                parserErrors (parser, i);
                if (parser.countErrors()==0){ // если в парсере ошибок не выявлено
                    processingParserString (parserString, i); // переходим к обработке полученной строки парсера
                    if (exitDirective){
                        break; // завершаем построчную обработку при встрече директивы exit
                    } // конец if (exitDirective)
                } // конец if (parser.countErrors()==0
            } // конец ((lexer.getLexerLine() != null)&&(lexer.getLexerLine().size()>0))
        } // конец for (int i=0; i<listing.size(); i++)
        if (includeFileName==null || includeFileName.length()==0){ // если метод выполняется не для подключенного файла, а для основного листинга программы
            for (int i=collecterCode.deferredErrors.size()-1; i>=0; i--){
                processingParserString (collecterCode.deferredErrors.get(i));
            } // конец for (int i=0; i<collecterCode.deferredErrors.size(); i++)
        } // конец if (includeFileName==null || includeFileName.length()==0)
    } // конец метода compile

    // вспомогательный метод возвращает начальный текст строки ошибки
    private String getStartText (int numStr){
        String res=null;
        switch (MainActivity.language){ // определяем язык
            case 1: // русский язык
                if (includeFileName==null || includeFileName.length()==0){
                    res = "Ошибка в строке "+Integer.toString(numStr)+" : ";
                } else {
                    res = "Ошибка в подключенном файле \""+includeFileName+"\""+" в строке "+Integer.toString(numStr)+" : ";
                }
                break;
            case 2: // английский язык
                if (includeFileName==null || includeFileName.length()==0){
                    res = "Error in line "+Integer.toString(numStr)+" : ";
                } else {
                    res = "Error in include file \""+includeFileName+"\""+" in line "+Integer.toString(numStr)+" : ";
                }
        } // конец switch (language)
        return res;
    } // конец метода getStartText

    // вспомогательный метод проверяет наличие ошибок лексера в строке
    private void lexerErrors (Lexer lexer, int numStr){
        String errorString;
        for (int j=0; j<lexer.countErrors(); j++){
            errorString = getStartText (numStr);
            errorString = errorString + lexer.getFullTextError(j, MainActivity.language); // формируем текст ошибки
            collecterCode.errors.add(collecterCode.new CompilatorError(errorString, numStr));
        } // конец for (int j=0; j<lexer.countErrors(); j++)
    } // конец метода lexerErrors

    // вспомогательный метод проверяет наличие ошибок парсера в строке
    private void parserErrors (Parser parser, int numStr){
        String errorString;
        for (int j=0; j<parser.countErrors(); j++){
            errorString = getStartText (numStr);
            errorString = errorString + parser.getFullTextError(j, MainActivity.language); // формируем текст ошибки
            collecterCode.errors.add(collecterCode.new CompilatorError(errorString, numStr));
        } // конец for (int j=0; j<lexer.countErrors(); j++)
    } // конец метода parserErrors

    // вспомогательный метод добавляет ошибку, что метка с указанным именем была объявлена ранее в листинге
    private void addErrorLabelName (String labelName, int numStr){
        String errorString = getStartText (numStr);
        switch (MainActivity.language){
            case 1:
                errorString = errorString + "метка \""+labelName+"\" была ранее объявлена в программе.";
                break;
            case 2:
                errorString = errorString + "label name \""+labelName+"\" not uniqe in program.";
        }
        collecterCode.errors.add(collecterCode.new CompilatorError(errorString, numStr));
    } // конец метода addErrorLabelName

    // метод проверяет наличие метки в строке ParserString, и, если она обнаружена, добавляет ее в список меток CollecterCode
    public void checkLabelInParserString (Parser.ParserString parserString, int numStr){
        if (parserString.getLabel() != null){ // если в строке есть метка
            if (parserString.getLabel() != ""){ // если метка не пустая строка
                if (! collecterCode.addLabelDeclaration (parserString.getLabel())){ // если метка с таким именем уже есть в списке
                    addErrorLabelName (parserString.getLabel(), numStr); // добавляем ошибку в список ошибок
                } // конец if (! collecterCode.addLabelDeclaration (parserString.getLabel))
            } // конец if (parserString.getLabel() != "")
        } // конец if (parserString.getLabel() != null)к
    } // конец метода checkLabelInParserString

    // метод обработки строки ParserString
    public void processingParserString (Parser.ParserString parserString, int numStr){
        Avr_directives directive;
        Avr_instructions instruction;
        String textError;
        checkLabelInParserString (parserString, numStr);
        switch (parserString.getTypeCommand()){
            case Parser.ParserString.TYPE_COMMAND_INSTRUCTION:
                instruction = Avr_instructions.valueOf (parserString.getCommand().toUpperCase());
                processorInstructions.runInstruction (instruction, parserString.operands);
                if ((processorInstructions.getErrors() != null)&&(processorInstructions.getErrors().size() !=0)){
                    for (int i=0; i<processorInstructions.getErrors().size(); i++){
                        if (processorInstructions.getErrors().get(i).getNumError()==ProcessorInstructions.ERROR_OPERAND_UNKNOWN_USERWORD){
                            collecterCode.deferredErrors.add (collecterCode.new DeferredError(parserString, numStr, collecterCode));
                        } else {
                            textError = getStartText(numStr) + processorInstructions.getErrors().get(i).getTextError();
                            collecterCode.errors.add(collecterCode.new CompilatorError(textError, numStr));
                        } // конец if (processorInstructions.getErrors().get(i).getNumError()==Avr_instructions.ERROR_OPERAND_UNKNOWN_USERWORD)
                    } // конец for (int i=0; i<processorInstructions.getErrors().size(); i++)
                } // конец if ((processorInstructions.getErrors() != null)&&(processorInstructions.getErrors().size() !=0))
                break;
            case Parser.ParserString.TYPE_COMMAND_DIRECTIVE:
                directive = Avr_directives.valueOf (parserString.getCommand().toUpperCase());
                directive.runDirective(parserString.operands, collecterCode);
                if ((directive.errors != null)&&(directive.errors.size()!=0)){
                    for (int i=0; i<directive.errors.size(); i++){
                        if (directive.errors.get(i).getNumError()==Avr_directives.ERROR_OPERAND_UNKNOWN_USERWORD){
                            collecterCode.deferredErrors.add (collecterCode.new DeferredError(parserString, numStr, collecterCode));
                        } else {
                            if (directive.errors.get(i).getNumError()==Avr_directives.DIR_EXIT){
                                exitDirective = true;
                            } else {
                                if (directive.errors.get(i).getNumError()==Avr_directives.DIR_ORG){ // если в строке с директивой ORG стоит имя метки, значение это  метки должно соответствовать значению ORG
                                    if (parserString.getLabel() != null){ // если в строке есть метка
                                        if (parserString.getLabel() != ""){ // если метка не пустая строка
                                            collecterCode.labels.remove(parserString.getLabel());
                                            collecterCode.addLabelDeclaration (parserString.getLabel());
                                        } // конец if (parserString.getLabel() != "")
                                    } // конец if (parserString.getLabel() != null)к
                                } else {
                                    textError = getStartText(numStr) + Avr_directives.getErrorText(directive.errors.get(i).getNumError()) + " : " + directive.errors.get(i).getErrorWord();
                                    collecterCode.errors.add(collecterCode.new CompilatorError(textError, numStr));
                                } // конец if (directive.errors.get(i).getNumError()==Avr_directives.DIR_ORG)
                            } // конец if (directive.errors.get(i)==Avr_directives.EXIT)
                        } // конец if (directive.errors.get(i)==Avr_directives.ERROR_OPERAND_UNKNOWN_USERWORD)
                    } // конец for (int i=0; i<directive.errors.size(); i++)
                } // конец if ((directive.errors != null)&&(directive.errors.size()!=0))
        } // конец switch (parserString.getTypeComnand())
    } // конец метода processingParserString

    // метод обработки строки ParserString для отложенных ошибок DeferredError
    public void processingParserString (CollecterCode.DeferredError deferredError){
        Avr_directives directive;
        Avr_instructions instruction;
        Parser.ParserString parserString = deferredError.getParserString();
        int numStr = deferredError.getNumStr();
        String textError;
        collecterCode.setState (deferredError.getState());
        switch (parserString.getTypeCommand()){
            case Parser.ParserString.TYPE_COMMAND_INSTRUCTION:
                instruction = Avr_instructions.valueOf (parserString.getCommand().toUpperCase());
                processorInstructions.runInstruction (instruction, parserString.operands);
                if ((processorInstructions.getErrors() != null)&&(processorInstructions.getErrors().size() != 0)){
                    for (int i=0; i<processorInstructions.getErrors().size(); i++){
                        textError = getStartText (numStr) + processorInstructions.getErrors().get(i).getTextError();
                        collecterCode.errors.add (deferredError.getState().getNumError()+i, collecterCode.new CompilatorError(textError, numStr));
                    } // конец for (int i=0; i<directive.errors.size(); i++)
                } // конец if ((processorInstructions.getErrors() != null)&&(processorInstructions.getErrors().size != 0))
                break;
            case Parser.ParserString.TYPE_COMMAND_DIRECTIVE:
                directive = Avr_directives.valueOf (parserString.getCommand().toUpperCase());
                directive.runDirective(parserString.operands, collecterCode);
                if ((directive.errors != null)&&(directive.errors.size()!=0)){
                    for (int i=0; i<directive.errors.size(); i++){
                        textError = getStartText (numStr) + Avr_directives.getErrorText (directive.errors.get(i).getNumError())+" : "+directive.errors.get(i).getErrorWord();
                        collecterCode.errors.add (deferredError.getState().getNumError()+i, collecterCode.new CompilatorError(textError, numStr));
                    } // конец for (int i=0; i<directive.errors.size(); i++)
                } // конец if ((directive.errors != null)&&(directive.errors.size()!=0))
        } // конец switch (parserString.getTypeComnand())
    } // конец метода processingParserString

} // конец класса MakerCode
