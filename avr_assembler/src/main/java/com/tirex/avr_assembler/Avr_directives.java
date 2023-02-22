package com.tirex.avr_assembler;

import java.util.ArrayList;

enum Avr_directives{

    BYTE {
        public boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode){
            this.collecterCode = collecterCode;
            if (! checkSeg (CollecterCode.SEGMENT_DATA, ".BYTE")){
                return false;
            } // конец if (! checkSeg (CollecterCode.SEGMENT_DATA, ".BYTE"))
            if (! checkOperandInt(operands, 1, ".BYTE")){
                return false;
            } // конец if (! checkOperandInt(operands, 1, ".BYTE"))
            collecterCode.setDSEG (collecterCode.getDSEG()+operands.get(0).getNumberValue());
            return true;
        } // конец метода runDirective
    } , // конец BYTE
    CSEG {
        public boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode){
            errors.clear();
            this.collecterCode = collecterCode;
            if (! checkOperandInt(operands, 0, ".CSEG")){
                return false;
            } // конец if (! checkOperandInt(operands, 0, ".CSEG"))
            collecterCode.setCurrentSeg (CollecterCode.SEGMENT_CODE);
            return true;
        } // конец метода runDirective
    } , // конец CSEG
    DB {
        public boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode){
            this.collecterCode = collecterCode;
            if (! checkSeg (CollecterCode.SEGMENT_CODE | CollecterCode.SEGMENT_EEPROM, this.toString())){
                return false;
            } // конец if (! checkSeg (CollecterCode.SEGMENT_DATA, ".BYTE"))
            if ((operands==null)||(operands.size()==0)){ // если операндов нет
                errors.add (new ErrorString(ERROR_NO_OPERANDS_INT, this.toString(), 0));
                return false;
            } // конец if ((operands==null)||(operands.size()==0))
            convertStringOperands (operands);
            for (int i=0; i<operands.size(); i++){
                if (! checkIntValueOperand(operands.get(i), this.toString())){
                    return false;
                } // конец if (! checkIntValueOperand(operands.get(i))
                if (collecterCode.getCurrentSeg()==CollecterCode.SEGMENT_CODE){
                    if ((i & 1)==1){
                        collecterCode.code.setLowByte ((byte)(operands.get(i).getNumberValue() & 255));
                        collecterCode.incCSEG();
                    } else {
                        collecterCode.code.setHighByte ((byte)(operands.get(i).getNumberValue() & 255));
                    } // конец if ((i & 1)==1)
                } else {
                    collecterCode.flashEeprom.setByte ((byte) (operands.get(i).getNumberValue() & 255));
                    collecterCode.incESEG();
                } // конец if (collecterCode.getCurrentSeg()==CollecterCode.SEGMENT_CODE)
            } // конец for (int i=0; i<operands.size(); i++)
            if (collecterCode.getCurrentSeg()==CollecterCode.SEGMENT_CODE){ // если текущий сегмент cseg
                if ((operands.size() & 1)==1){ // если кол-во операндов не кратно двум
                    collecterCode.code.setLowByte ((byte) 0);
                    collecterCode.incCSEG();
                } // конец if ((operands.size() & 1)==1
            } // конец if (collecterCode.currentSeg()==CollecterCode.SEGMENT_CODE)
            return true;
        } // конец метода runDirective
    } , // конец DB
    DEF {
        public boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode){
            Avr_registers reg = new Avr_registers();
            errors.clear();
            this.collecterCode = collecterCode;
            if ((operands==null)||(operands.size()==0)){ // если операндов нет
                errors.add (new ErrorString(ERROR_NO_OPERAND_ASSIGN, this.toString(), 0));
                return false;
            } // конец if ((operands==null)||(operands.size()==0))
            if (operands.size() > 1){
                errors.add (new ErrorString(ERROR_MORE_1_OPERANDS, this.toString(), 0));
                return false;
            } // конец if (operands.size() > 1)
            if (operands.get(0).getTypeRegister()>0){
                errors.add (new ErrorString(ERROR_INVALID_OPERAND, this.toString(), 0));
                return false;
            } // конец if (operands.get(0).getTypeRegister()>0)
            if (! operands.get(0).getFlagAssignment()){
                errors.add (new ErrorString(ERROR_INVALID_OPERAND_ASSIGN, this.toString(), 0));
                return false;
            } // конец if (! operands.get(0).getFlagAssignment())
            if ((operands.get(0).getTypeOperand() != ParserOperand.TYPE_OPERAND_KEYWORD)||(! reg.isReg_r0_r31(operands.get(0).getStringValue()))){
                errors.add (new ErrorString(ERROR_INVALID_VALUE, this.toString(), 0));
                return false;
            } // конец if ((operands.get(0).getTypeOperand != Parser.ParserOperand.TYPE_OPERAND_KEYWORD)||(operands.get(0).getStringValue.equalsIgnoreCase("X"))||(operands.get(0).getStringValue.equalsIgnoreCase("Y"))||(operands.get(0).getStringValue.equalsIgnoreCase("Z")))
            if (! collecterCode.addVarDef (operands.get(0).getVarAssignment(), operands.get(0).getStringValue())){
                errors.add (new ErrorString(ERROR_NAME_VAR_NOT_UNIQUE, operands.get(0).getVarAssignment(), 0));
                return false;
            } // конец if (! collecterCode.addVarDef (operands.get(0).getVarAssignment(), operands.get(0).getStringValue()))
            return true;
        } // конец метода runDirective
    } , // конец DEF
    DEVICE { // заглушка, не реализовано
        public boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode){
            errors.clear();
            errors.add (new ErrorString(ERROR_NOT_REALISE, this.toString(), 0));
            return false;
        } // конец метода runDirective
    } , // конец DEVICE
    DSEG {
        public boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode){
            errors.clear();
            this.collecterCode = collecterCode;
            if (! checkOperandInt(operands, 0, ".DSEG")){
                return false;
            } // конец if (! checkOperandInt(operands, 0, ".ESEG"))
            collecterCode.setCurrentSeg (CollecterCode.SEGMENT_DATA);
            return true;
        } // конец метода runDirective
    } , // конец DSEG
    DW {
        public boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode){
            this.collecterCode = collecterCode;
            if (! checkSeg (CollecterCode.SEGMENT_CODE | CollecterCode.SEGMENT_EEPROM, this.toString())){
                return false;
            } // конец if (! checkSeg (CollecterCode.SEGMENT_DATA, ".BYTE"))
            if ((operands==null)||(operands.size()==0)){ // если операндов нет
                errors.add (new ErrorString(ERROR_NO_OPERANDS_INT, this.toString(), 0));
                return false;
            } // конец if ((operands==null)||(operands.size()==0))
            convertStringOperands (operands);
            for (int i=0; i<operands.size(); i++){
                if (! checkIntValueOperand(operands.get(i), this.toString())){
                    return false;
                } // конец if (! checkIntValueOperand(operands.get(i))
                if (collecterCode.getCurrentSeg()==CollecterCode.SEGMENT_CODE){
                    collecterCode.code.setWord ((short)(operands.get(i).getNumberValue() & ((255<<8)+255)));
                    collecterCode.incCSEG();
                } else {
                    collecterCode.flashEeprom.setByte ((byte) (operands.get(i).getNumberValue() & (255<<8)));
                    collecterCode.incESEG();
                    collecterCode.flashEeprom.setByte ((byte) (operands.get(i).getNumberValue() & 255));
                    collecterCode.incESEG();
                } // конец if (collecterCode.getCurrentSeg()==CollecterCode.SEGMENT_CODE)
            } // конец for (int i=0; i<operands.size(); i++)
            return true;
        } // конец метода runDirective
    } , // конец DW
    ENDM { // заглушка, не реализовано
        public boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode){
            errors.clear();
            errors.add (new ErrorString(ERROR_NOT_REALISE, this.toString(), 0));
            return false;
        } // конец метода runDirective
    } , // конец ENDM
    ENDMACRO { // заглушка, не реализовано
        public boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode){
            //return Avr_directives.ENDM.runDirective (operands, collecterCode);
            errors.clear();
            errors.add (new ErrorString(ERROR_NOT_REALISE, this.toString(), 0));
            return false;
        } // конец метода runDirective
    } , // конец ENDMACRO
    EQU {
        public boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode){
            errors.clear();
            this.collecterCode = collecterCode;
            if ((operands==null)||(operands.size()==0)){ // если операндов нет
                errors.add (new ErrorString(ERROR_NO_OPERAND_ASSIGN, this.toString(), 0));
                return false;
            } // конец if ((operands==null)||(operands.size()==0))
            if (operands.size() > 1){
                errors.add (new ErrorString(ERROR_MORE_1_OPERANDS, this.toString(), 0));
                return false;
            } // конец if (operands.size() > 1)
            if (operands.get(0).getTypeRegister()>0){
                errors.add (new ErrorString(ERROR_INVALID_OPERAND, this.toString(), 0));
                return false;
            } // конец if (operands.get(0).getTypeRegister()>0)
            if (! operands.get(0).getFlagAssignment()){
                errors.add (new ErrorString(ERROR_INVALID_OPERAND_ASSIGN, this.toString(), 0));
                return false;
            } // конец if (! operands.get(0).getFlagAssignment())
            if (! loadNumberValueForOperand(operands.get(0))){
                //errors.add (new ErrorString(ERROR_INVALID_VALUE, this.toString(), 0);
                return false;
            } // конец
            if (! collecterCode.addVarEqu (operands.get(0).getVarAssignment(), operands.get(0).getNumberValue())){
                errors.add (new ErrorString(ERROR_NAME_VAR_NOT_UNIQUE, operands.get(0).getVarAssignment(), 0));
                return false;
            } // конец if (! collecterCode.addVarDef (operands.get(0).getVarAssignment(), operands.get(0).getStringValue()))
            return true;
        } // конец метода runDirective
    } , // конец EQU
    ESEG {
        public boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode){
            errors.clear();
            this.collecterCode = collecterCode;
            if (! checkOperandInt(operands, 0, ".ESEG")){
                return false;
            } // конец if (! checkOperandInt(operands, 0, ".ESEG"))
            collecterCode.setCurrentSeg (CollecterCode.SEGMENT_EEPROM);
            return true;
        } // конец метода runDirective
    } , // конец ESEG
    EXIT {
        public boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode){
            errors.clear();
            this.collecterCode = collecterCode;
            if ((operands!=null)&&(operands.size()!=0)){ // если есть операнды
                errors.add (new ErrorString(ERROR_IS_OPERAND, this.toString(), 0));
                return true;
            } // конец if ((operands!=null)&(operands.size()!=0))
            errors.add (new ErrorString(DIR_EXIT, this.toString(), 0));
            return true;
        } // конец метода runDirective
    } , // конец EXIT
    INCLUDE {
        public boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode){
            errors.clear();
            this.collecterCode = collecterCode;
            if ((operands==null)||(operands.size()==0)){ // если операндов нет
                errors.add (new ErrorString(ERROR_NO_OPERAND_INCLUDE, this.toString(), 0));
                return false;
            } // конец if ((operands==null)||(operands.size()==0))
            if (operands.size() > 1){
                errors.add (new ErrorString(ERROR_MORE_1_OPERANDS, this.toString(), 0));
                return false;
            } // конец if (operands.size() > 1)
            if (operands.get(0).getTypeOperand() != ParserOperand.TYPE_OPERAND_INCLUDE){
                errors.add (new ErrorString(ERROR_INVALID_OPERAND, this.toString(), 0));
                return false;
            } // конец if (operands.get(0).getTypeOperand() != Parser.ParserOperand.TYPE_OPERAND_INCLUDE)
            // ЗДЕСЬ ЗАГРУЖАЕМ ФАЙЛ
            return true;
        } // конец метода runDirective
    } , // конец INCLUDE
    LIST { // заглушка, не реализовано
        public boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode){
            errors.clear();
            errors.add (new ErrorString(ERROR_NOT_REALISE, this.toString(), 0));
            return false;
        } // конец метода runDirective
    } , // конец LIST
    LISTMAC { // заглушка, не реализовано
        public boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode){
            errors.clear();
            errors.add (new ErrorString(ERROR_NOT_REALISE, this.toString(), 0));
            return false;
        } // конец метода runDirective
    } , // конец LISTMAC
    MACRO { // заглушка, не реализовано
        public boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode){
            errors.clear();
            errors.add (new ErrorString(ERROR_NOT_REALISE, this.toString(), 0));
            return false;
        } // конец метода runDirective
    } , // конец MACRO
    NOLIST { // заглушка, не реализовано
        public boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode){
            errors.clear();
            errors.add (new ErrorString(ERROR_NOT_REALISE, this.toString(), 0));
            return false;
        } // конец метода runDirective
    } , // конец NOLIST
    ORG {
        public boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode){
            errors.clear();
            this.collecterCode = collecterCode;
            if (! checkOperandInt(operands, 1, ".ORG")){
                if (errors.get(errors.size()-1).getNumError()==ERROR_OPERAND_UNKNOWN_USERWORD){
                    errors.remove(errors.size()-1);
                    errors.add (new ErrorString(ERROR_INVALID_OPERAND, this.toString(), 0));
                } // конец if (errors.get(errors.size()-1).getNumError()==ERROR_OPERAND_UNKNOWN_USERWORD)
                return false;
            } // конец if (! checkOperandInt(operands, 1, ".ORG"))
            collecterCode.setValueCurrentSeg (operands.get(0).getNumberValue());
            errors.add (new ErrorString(DIR_ORG, this.toString(), 0));
            return true;
        } // конец метода runDirective
    } , // конец ORG
    SET {
        public boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode){
            errors.clear();
            this.collecterCode = collecterCode;
            if ((operands==null)||(operands.size()==0)){ // если операндов нет
                errors.add (new ErrorString(ERROR_NO_OPERAND_ASSIGN, this.toString(), 0));
                return false;
            } // конец if ((operands==null)||(operands.size()==0))
            if (operands.size() > 1){
                errors.add (new ErrorString(ERROR_MORE_1_OPERANDS, this.toString(), 0));
                return false;
            } // конец if (operands.size() > 1)
            if (operands.get(0).getTypeRegister()>0){
                errors.add (new ErrorString(ERROR_INVALID_OPERAND, this.toString(), 0));
                return false;
            } // конец if (operands.get(0).getTypeRegister()>0)
            if (! operands.get(0).getFlagAssignment()){
                errors.add (new ErrorString(ERROR_INVALID_OPERAND_ASSIGN, this.toString(), 0));
                return false;
            } // конец if (! operands.get(0).getFlagAssignment())
            if (! loadNumberValueForOperand(operands.get(0))){
                //errors.add (new ErrorString(ERROR_INVALID_VALUE, this.toString(), 0);
                return false;
            } // конец
            if (! collecterCode.addVarSet (operands.get(0).getVarAssignment(), operands.get(0).getNumberValue())){
                errors.add (new ErrorString(ERROR_NAME_VAR_NOT_UNIQUE_SET, operands.get(0).getVarAssignment(), 0));
                return false;
            } // конец if (! collecterCode.addVarDef (operands.get(0).getVarAssignment(), operands.get(0).getStringValue()))
            return true;
        } // конец метода runDirective
    } , // конец SET
    UNDEF {
        public boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode){
            errors.clear();
            this.collecterCode = collecterCode;
            if ((operands==null)||(operands.size()==0)){ // если операндов нет
                errors.add (new ErrorString(ERROR_NO_OPERAND_NAME_VAR, this.toString(), 0));
                return false;
            } // конец if ((operands==null)||(operands.size()==0))
            if (operands.size() > 1){
                errors.add (new ErrorString(ERROR_MORE_1_OPERANDS, this.toString(), 0));
                return false;
            } // конец if (operands.size() > 1)
            if ((operands.get(0).getFlagAssignment())||(operands.get(0).getTypeRegister()>0)){
                errors.add (new ErrorString(ERROR_INVALID_OPERAND, this.toString(), 0));
                return false;
            } // конец if ((operands.get(0).getFlagAssognment())||(operands.get(0).getTypeRegister()>0))
            if (operands.get(0).getTypeOperand() != ParserOperand.TYPE_OPERAND_USERWORD){
                errors.add (new ErrorString(ERROR_INVALID_OPERAND, this.toString(), 0));
                return false;
            } // конец if (operands.get(0).getTypeOperand != Parser.ParserOperand.TYPE_OPERAND_USERWORD)
            if (! collecterCode.vars_def.remove(operands.get(0).getStringValue())){
                errors.add (new ErrorString(ERROR_INVALID_NAME_VAR, this.toString(), 0));
                return false;
            } // конец if (! collecterCode.vars_def.remove(operands.get(0).getStringValue())
            return true;
        } // конец метода runDirective
    } ; // конец UNDEF

    public static final int ERROR_NO_OPERAND_INT = 1; // отсутсвует операнд, ожидается числовой операнд
    public static final int ERROR_MORE_1_OPERANDS = 2; // для директивы нужен 1 операнд, а указано несколько
    public static final int ERROR_INVALID_OPERAND = 3; // недопустимый операнд
    public static final int ERROR_SEGMENT_NO_DSEG = 4; // директива может использоваться только в сегменте dseg
    public static final int ERROR_OPERAND_UNKNOWN_USERWORD = 5; // ожидается числовое значение операнда, но обнаружено не определенное ранее пользовательское слово
    public static final int ERROR_OPERAND_DEF_MUST_INT = 6; // ожидается числовое значение операнда, но обнаружено пользовательское слово, ранее определенное директивой def как регистр
    public static final int ERROR_OPERAND_NO_INT = 7; // ожидается числовое значение, но обнаружена строка или ключевое слово (регистр)
    public static final int ERROR_DIV_ZERO = 8; // деление на ноль при выполнении списка операций
    public static final int ERROR_FUNC_BIG_OPERAND_EXP2 = 9; // ошибка выполнения функции EXP2, слишком большое значение операнда
    public static final int ERROR_FUNC_SMALL_OPERAND_EXP2 = 10; // ошибка выполнения функции EXP2, значение операнда не может быть меньше нуля
    public static final int ERROR_FUNC_SMALL_OPERAND_LOG2 = 11; // ошибка выполнения функции LOG2, значение операнда не может быть меньше или равно нуля
    public static final int ERROR_FUNC_UNKNOWN_ERROR = 12; // неизвестная ошибка при выполнении функции
    public static final int ERROR_UNKNOWN = 13; // неизвестная ошибка при выполнении операций в методе loadNumberValue экземпляра ParserOperand
    public static final int ERROR_SEGMENT_NO_CSEG = 14; // директива может использоваться только в сегменте cseg
    public static final int ERROR_SEGMENT_NO_CSEG_OR_ESEG = 15; // директива может использоваться только в сегментах cseg или eseg
    public static final int ERROR_IS_OPERAND = 16; // директива должна использоваться без операнда, но обнаружен операнд
    public static final int ERROR_NOT_REALISE = 17; // директива не поддерживается, временно не реализована
    public static final int ERROR_NO_OPERANDS_INT = 18; // нет операнда, ожидается один или несколько операндов числового типа
    public static final int ERROR_NO_OPERAND_INCLUDE = 19; // после инструкции include ожидается операнд
    public static final int DIR_EXIT = 20; // директива exit - конец компиляции
    public static final int ERROR_NO_OPERAND_ASSIGN = 21; // нет операнда у директивы присваивания (def, equ, set)
    public static final int ERROR_INVALID_OPERAND_ASSIGN = 22; // недопустимый операнд, ожидается операция присваивания
    public static final int ERROR_INVALID_VALUE = 23; // недопустимое значение в операции присваивания у директивы
    public static final int ERROR_NAME_VAR_NOT_UNIQUE = 24; // пользовательское слово (имя переменной) в операции присваивания не уникально, определено ранее
    public static final int ERROR_NAME_VAR_NOT_UNIQUE_SET = 25; // пользовательское слово (имя переменной) в операции присваивания директивы set не уникально, определено ранее другой директивой (def, equ) или как метка
    public static final int ERROR_NO_OPERAND_NAME_VAR = 26; // нет операнда для  директивы undef - имя переменной
    public static final int ERROR_INVALID_NAME_VAR = 27; // в undef указано имя переменной, которое ранее не определено в def
    public static final int DIR_ORG = 28; // директива ORG (на случай, если в этой же строке есть метка)

    // содержит список ошибок после выполнения runDirective, если метод вернул false
    public static ArrayList<ErrorString> errors = new ArrayList();
    public static CollecterCode collecterCode;

    // абстрактный метод выполнения директивы, если директива выполнена успешно, то возвращант true. Иначе возвращает false, а errors содержит список ошибок
    public abstract boolean runDirective (ArrayList<ParserOperand> operands, CollecterCode collecterCode);

    // метод возвращает экземпляр Avr_directives, имя которого совпадает со строкой directiveName. Если такого экземпляра нет, то возвращает null
    private static Avr_directives getDirective (String directiveName){
        Avr_directives res = null;
        Avr_directives [ ] arrayDirectives = Avr_directives.values();
        for (Avr_directives directive : arrayDirectives){
            if (directiveName.equalsIgnoreCase(directive.toString())){
                res = directive;
                break;
            } // конец if (directiveName.equalsIgnoreCase(directive.toString()))
        } // конец for (Avr_directives directive : arrayDirectives)
        return res;
    } // конец метода getDirective

    // возвращает true если строка directiveName является директивой
    public static boolean isDirective (String directiveName){
        boolean res = false;
        if (getDirective(directiveName) != null){
            res = true;
        }
        return res;
    } // конец метода isDirective

    // метод загружает в поле numberValue операнда operand числовое значение. Если загрузить число невозможно, то записывает ошибку и возвращает false
    private static boolean loadNumberValueForOperand (ParserOperand operand){
        boolean res = false;
        ErrorString error;
        error = operand.loadNumberValue (collecterCode);
        if (error != null){
            switch (error.getNumError()){
                case ParserOperand.ERROR_OPERAND_NO_INT:
                    errors.add (new ErrorString(ERROR_OPERAND_NO_INT, error.getErrorWord(), 0));
                    errors.get(errors.size()-1).setTextError (error.getTextError());
                    break;
                case ParserOperand.ERROR_OPERAND_STR_MUST_INT:
                    errors.add (new ErrorString(ERROR_OPERAND_DEF_MUST_INT, error.getErrorWord(), 0));
                    errors.get(errors.size()-1).setTextError (error.getTextError());
                    break;
                case ParserOperand.ERROR_OPERAND_UNKNOWN_USERWORD:
                    errors.add (new ErrorString(ERROR_OPERAND_UNKNOWN_USERWORD, error.getErrorWord(), 0));
                    errors.get(errors.size()-1).setTextError (error.getTextError());
                    break;
                case ParserOperand.ERROR_DIV_ZERO:
                    errors.add (new ErrorString(ERROR_DIV_ZERO, error.getErrorWord(), 0));
                    errors.get(errors.size()-1).setTextError (error.getTextError());
                    break;
                case ParserOperand.ERROR_FUNC_BIG_OPERAND_EXP2:
                    errors.add (new ErrorString(ERROR_FUNC_BIG_OPERAND_EXP2, error.getErrorWord(), 0));
                    errors.get(errors.size()-1).setTextError (error.getTextError());
                    break;
                case ParserOperand.ERROR_FUNC_SMALL_OPERAND_EXP2:
                    errors.add (new ErrorString(ERROR_FUNC_SMALL_OPERAND_EXP2, error.getErrorWord(), 0));
                    errors.get(errors.size()-1).setTextError (error.getTextError());
                    break;
                case ParserOperand.ERROR_FUNC_SMALL_OPERAND_LOG2:
                    errors.add (new ErrorString(ERROR_FUNC_SMALL_OPERAND_LOG2, error.getErrorWord(), 0));
                    errors.get(errors.size()-1).setTextError (error.getTextError());
                    break;
                case ParserOperand.ERROR_FUNC_UNKNOWN_ERROR:
                    errors.add (new ErrorString(ERROR_FUNC_UNKNOWN_ERROR, error.getErrorWord(), 0));
                    errors.get(errors.size()-1).setTextError (error.getTextError());
                    break;
                case ParserOperand.ERROR_UNKNOWN:
                    errors.add (new ErrorString(ERROR_UNKNOWN, error.getErrorWord(), 0));
                    errors.get(errors.size()-1).setTextError (error.getTextError());
                    break;
            } // конец switch (error.getNumError())
        } else {
            res = true;
        } // конец if (error != null)
        return res;
    } // конец loadNumberValueForOperand

    // метод принимает на вход допустимый сегмент permissibleSeg, в котором возможно использование данной директивы (имя директивы передается в errorWord)
    // возвращает true если в текущем сегменте допустимо использование данной директивы, иначе возвращает false и записывает ошибку в errors.
    private static boolean checkSeg (int permissibleSeg, String errorWord){
        int numError=0;
        errors.clear();
        if ((collecterCode.getCurrentSeg() & permissibleSeg)==0){
            switch (permissibleSeg){
                case CollecterCode.SEGMENT_CODE:
                    numError = ERROR_SEGMENT_NO_CSEG;
                    break;
                case CollecterCode.SEGMENT_DATA:
                    numError = ERROR_SEGMENT_NO_DSEG;
                    break;
                case CollecterCode.SEGMENT_CODE | CollecterCode.SEGMENT_EEPROM:
                    numError = ERROR_SEGMENT_NO_CSEG_OR_ESEG;
            } // конец switch (permissibleSeg)
            errors.add (new ErrorString(numError, errorWord, 0));
            return false;
        } // конец if (!(collecterCode.getCurrentSeg() & permissibleSeg))
        return true;
    } // конец метода checkSeg

    // метод получает на вход кол-во операндов countOperands, которое должна содержать директива (используется 0 или 1). Имя директивы записывается в errorWord.
    // используется для директив с числовым операндом или без операндов. Проверяет кол-во операндов. Также проверяет отсутствие в операнде операции присваивания и пост или пред инкрементов/декркментов регистров.
    // если проверка выполнена успешно, то возвращает true, иначе возвращает false и записывает ошибку в errors
    private static boolean checkOperandInt (ArrayList<ParserOperand> operands, int countOperands, String errorWord){
        if ((operands==null)||(operands.size()==0)){ // если операндов нет
            if (countOperands != 0){
                errors.add (new ErrorString(ERROR_NO_OPERAND_INT, errorWord, 0));
                return false;
            } // конец if (countOperands != 0)
        } else { // если есть операнд один или несколько
            if (countOperands == 0){ // если у директивы не должно быть операндов
                errors.add (new ErrorString(ERROR_IS_OPERAND, errorWord, 0));
                return false;
            } else { // если у директивы должен быть один операнд
                if (operands.size() > 1){
                    errors.add (new ErrorString(ERROR_MORE_1_OPERANDS, errorWord, 0));
                    return false;
                } // конец if (operands.size() > 1)
            } // конец if (countOperands == 0)
            if (! checkIntValueOperand (operands.get(0), errorWord)){
                return false;
            } // конец if ((operands.get(0).getFlagAssognment())||(operands.get(0).getTypeRegister()>0))
        } // конец if ((operands==null)||(operands.size()==0))
        return true;
    } // конец метода checkOperandInt

    // метод проверяет отсутствие в операнде операции присваивания и пост или пред инкрементов/декркментов регистров. Также проверяет, чтобы значение операнда было числовое.
    // если ошибок нет, то возвращает true
    private static boolean checkIntValueOperand (ParserOperand parserOperand, String errorWord){
        if ((parserOperand.getFlagAssignment())||(parserOperand.getTypeRegister()>0)){
            errors.add (new ErrorString(ERROR_INVALID_OPERAND, errorWord, 0));
            return false;
        } // конец if ((operands.get(0).getFlagAssognment())||(operands.get(0).getTypeRegister()>0))
        if (! loadNumberValueForOperand(parserOperand)){
            return false;
        } // конец if (! loadNumberValueForOperand(operands.get(i))
        return true;
    } // конец chekIntValueOperand

    // метод проверяет наличие строковых операндов. Если находит, то заменяет строковый операнд на набор числовых операндов, где каждое значение - это код симола в строке
    private static void convertStringOperands (ArrayList<ParserOperand> operands){
        ParserOperand parserOperand;
        String stringValue;
        int numberValue;
        for (int i=0; i<operands.size(); i++){
            if (operands.get(i).getTypeOperand()==ParserOperand.TYPE_OPERAND_STRING){
                stringValue = operands.get(i).getStringValue();
                operands.remove(i);
                for (int j=0; j<stringValue.length(); j++){
                    numberValue = stringValue.charAt(j) & 255;
                    parserOperand = new ParserOperand();
                    parserOperand.setTypeOperand(ParserOperand.TYPE_OPERAND_NUMBER);
                    parserOperand.setNumberValue(numberValue);
                    operands.add (i+j, parserOperand);
                } // конец for (int j=0; j<stringValue.length(); j++)
            } // конец if (operands.get(i).getTypeOperand()==Parser.ParserOperand.TYPE_OPERAND_STRING)
        } // конец for (int i=0; i<operands.size(); i++)
    } // конец метода convertStringOperands

    public static String getErrorText (int numError){
        String res = null;
        ParserOperand parserOp = new ParserOperand();
        switch (MainActivity.language){
            case 1: // русский
                switch (numError){
                    case ERROR_NO_OPERAND_INT:
                        res = "Ожидается числовой операнд у директивы";
                        break;
                    case ERROR_MORE_1_OPERANDS:
                        res = "Ожидается только один операнд, но обнаружено несколько операндов у директивы";
                        break;
                    case ERROR_INVALID_OPERAND:
                        res = "Недопустимый операнд у директивы";
                        break;
                    case ERROR_SEGMENT_NO_DSEG:
                        res = "Директиву возможно использовать только в сегменте данных dseg";
                        break;
                    case ERROR_OPERAND_UNKNOWN_USERWORD:
                        res = "Недопустимое выражение, обнаружено не определенное ранее пользовательское слово";
                        break;
                    case ERROR_OPERAND_DEF_MUST_INT:
                        res = "Ожидается числовое значение, но обнаружено наименование регистра";
                        break;
                    case ERROR_OPERAND_NO_INT:
                        res = "Недопустимое выражение, ожидается числовое значение";
                        break;
                    case ERROR_DIV_ZERO:
                        res = parserOp.getErrorText (ParserOperand.ERROR_DIV_ZERO);
                        break;
                    case ERROR_FUNC_BIG_OPERAND_EXP2:
                        res = parserOp.getErrorText (ParserOperand.ERROR_FUNC_BIG_OPERAND_EXP2);
                        break;
                    case ERROR_FUNC_SMALL_OPERAND_EXP2:
                        res = parserOp.getErrorText (ParserOperand.ERROR_FUNC_SMALL_OPERAND_EXP2);
                        break;
                    case ERROR_FUNC_SMALL_OPERAND_LOG2:
                        res = parserOp.getErrorText (ParserOperand.ERROR_FUNC_SMALL_OPERAND_LOG2);
                        break;
                    case ERROR_FUNC_UNKNOWN_ERROR:
                        res = parserOp.getErrorText (ParserOperand.ERROR_FUNC_UNKNOWN_ERROR);
                        break;
                    case ERROR_UNKNOWN:
                        res = parserOp.getErrorText (ParserOperand.ERROR_UNKNOWN);
                        break;
                    case ERROR_SEGMENT_NO_CSEG:
                        res = "Директиву возможно использовать только в сегменте кода cseg";
                        break;
                    case ERROR_SEGMENT_NO_CSEG_OR_ESEG:
                        res = "Директиву возможно использовать только в сегментах cseg или eseg";
                        break;
                    case ERROR_IS_OPERAND:
                        res = "Обнаружен операнд, но директива должна использоваться без операнда";
                        break;
                    case ERROR_NOT_REALISE:
                        res = "Функционал директивы не реализован, недопустимо использовать";
                        break;
                    case ERROR_NO_OPERANDS_INT:
                        res = "Отсутствуют операнды, необходимо указать хотя бы один числовой операнд для директивы";
                        break;
                    case ERROR_NO_OPERAND_INCLUDE:
                        res = "Отсутствует операнд, ожидается имя подключаемого файла в угловых скобках после имени директивы";
                        break;
                    case ERROR_NO_OPERAND_ASSIGN:
                        res = "Отсутствует операнд, ожидается оператор присваивания";
                        break;
                    case ERROR_INVALID_OPERAND_ASSIGN:
                        res = "Недопустимый операнд, ожидается оператор присваивания";
                        break;
                    case ERROR_INVALID_VALUE:
                        res = "Недопустимое выражение в операторе присваивания у директивы";
                        break;
                    case ERROR_NAME_VAR_NOT_UNIQUE:
                        res = "Указанное пользовательское слово в операторе присваивания директивы не уникально, определено ранее";
                        break;
                    case ERROR_NAME_VAR_NOT_UNIQUE_SET:
                        res = "Недопустимо использовать пользовательское слово, определенное ранее не директивой set";
                        break;
                    case ERROR_NO_OPERAND_NAME_VAR:
                        res = "Отсутствует операнд, ожидается пользовательское слово, определенное ранее директивой def";
                        break;
                    case ERROR_INVALID_NAME_VAR:
                        res = "Недопустимый операнд, ожидается пользовательское слово, которое ранее было определено директивой def";
                        break;
                }
                break;
            case 2: // английский
                switch (numError) {
                    case ERROR_NO_OPERAND_INT:
                        res = "A numeric operand is expected for the directive";
                        break;
                    case ERROR_MORE_1_OPERANDS:
                        res = "Only one operand is expected, but several operands were detected for the directive";
                        break;
                    case ERROR_INVALID_OPERAND:
                        res = "Invalid operand to a directive";
                        break;
                    case ERROR_SEGMENT_NO_DSEG:
                        res = "The directive can only be used in the data segment dseg";
                        break;
                    case ERROR_OPERAND_UNKNOWN_USERWORD:
                        res = "An invalid expression is not detected you have already defined a custom word";
                        break;
                    case ERROR_OPERAND_DEF_MUST_INT:
                        res = "A numeric value is expected, but the register name is detected";
                        break;
                    case ERROR_OPERAND_NO_INT:
                        res = "Invalid expression, numeric value expected";
                        break;
                    case ERROR_DIV_ZERO:
                        res = parserOp.getErrorText(ParserOperand.ERROR_DIV_ZERO);
                        break;
                    case ERROR_FUNC_BIG_OPERAND_EXP2:
                        res = parserOp.getErrorText(ParserOperand.ERROR_FUNC_BIG_OPERAND_EXP2);
                        break;
                    case ERROR_FUNC_SMALL_OPERAND_EXP2:
                        res = parserOp.getErrorText(ParserOperand.ERROR_FUNC_SMALL_OPERAND_EXP2);
                        break;
                    case ERROR_FUNC_SMALL_OPERAND_LOG2:
                        res = parserOp.getErrorText(ParserOperand.ERROR_FUNC_SMALL_OPERAND_LOG2);
                        break;
                    case ERROR_FUNC_UNKNOWN_ERROR:
                        res = parserOp.getErrorText(ParserOperand.ERROR_FUNC_UNKNOWN_ERROR);
                        break;
                    case ERROR_UNKNOWN:
                        res = parserOp.getErrorText(ParserOperand.ERROR_UNKNOWN);
                        break;
                    case ERROR_SEGMENT_NO_CSEG:
                        res = "The directive can only be used in a code segment cseg";
                        break;
                    case ERROR_SEGMENT_NO_CSEG_OR_ESEG:
                        res = "The directive can only be used in segments cseg or eseg";
                        break;
                    case ERROR_IS_OPERAND:
                        res = "An operand is detected, but the directive must be used without an operand";
                        break;
                    case ERROR_NOT_REALISE:
                        res = "The functionality of the directive is not implemented and cannot be used";
                        break;
                    case ERROR_NO_OPERANDS_INT:
                        res = "There are no operands, you must specify at least one numeric operand for the directive";
                        break;
                    case ERROR_NO_OPERAND_INCLUDE:
                        res = "There is no operand, and the name of the file to be connected is expected in angle brackets after the directive name";
                        break;
                    case ERROR_NO_OPERAND_ASSIGN:
                        res = "An operand is missing, and an assignment operator is expected";
                        break;
                    case ERROR_INVALID_OPERAND_ASSIGN:
                        res = "Invalid operand, an assignment operator is expected";
                        break;
                    case ERROR_INVALID_VALUE:
                        res = "Invalid expression in the directive's assignment operator";
                        break;
                    case ERROR_NAME_VAR_NOT_UNIQUE:
                        res = "The specified user word in the directive assignment operator is not unique, it was defined earlier";
                        break;
                    case ERROR_NAME_VAR_NOT_UNIQUE_SET:
                        res = "It is not allowed to use a user word that was previously defined by a non-directive set";
                        break;
                    case ERROR_NO_OPERAND_NAME_VAR:
                        res = "An operand is missing, and a user word defined earlier by the Directive def is expected";
                        break;
                    case ERROR_INVALID_NAME_VAR:
                        res = "Invalid operand, a user word that was previously defined by the Directive def is expected";
                        break;
                }
        } // конец switch (language)
        return res;
    } // конец метода getErrorText

} // конец перечисления Avr_directives