package com.tirex.avr_assembler;

import java.util.ArrayList;

// класс для хранения операндов в строке
public class ParserOperand{
    public static final int TYPE_OPERAND_NUMBER = 1;
    public static final int TYPE_OPERAND_STRING = 2;
    public static final int TYPE_OPERAND_CHAR = 3;
    public static final int TYPE_OPERAND_USERWORD = 4;
    public static final int TYPE_OPERAND_KEYWORD = 5;
    public static final int TYPE_OPERAND_OPERATIONS = 6;
    public static final int TYPE_OPERAND_INCLUDE = 7;
    public static final int TYPE_OPERAND_POST_OR_PRED = 8;
    public static final int TYPE_REGISTER_MINUS_X = 1; // -X
    public static final int TYPE_REGISTER_PLUS_X = 2; // +X
    public static final int TYPE_REGISTER_X_MINUS = 3; // X-
    public static final int TYPE_REGISTER_X_PLUS = 4; // X+
    public static final int TYPE_REGISTER_MINUS_Y = 5; // -Y
    public static final int TYPE_REGISTER_PLUS_Y = 6; // +Y
    public static final int TYPE_REGISTER_Y_MINUS = 7; // Y-
    public static final int TYPE_REGISTER_Y_PLUS = 8; // Y+
    public static final int TYPE_REGISTER_MINUS_Z = 9; // -Z
    public static final int TYPE_REGISTER_PLUS_Z = 10; // +Z
    public static final int TYPE_REGISTER_Z_MINUS = 11; // Z-
    public static final int TYPE_REGISTER_Z_PLUS = 12; // Z+
    public static final int ERROR_OPERAND_NO_INT = 1; // в методе loadNumberValue ожидается числовое значение, но обнаружена строка или ключевое слово (регистр)
    public static final int ERROR_OPERAND_STR_MUST_INT = 2; // в методе loadNumberValue ожидается числовое значение операнда, но обнаружено пользовательское слово строкового типа
    public static final int ERROR_OPERAND_UNKNOWN_USERWORD = 3; // в методе loadNumberValue ожидается числовое значение операнда, но обнаружено не определенное ранее пользовательское слово
    public static final int ERROR_DIV_ZERO = 4; // деление на ноль при выполнении списка операций
    public static final int ERROR_FUNC_BIG_OPERAND_EXP2 = 5; // ошибка выполнения функции EXP2, слишком большое значение операнда
    public static final int ERROR_FUNC_SMALL_OPERAND_EXP2 = 6; // ошибка выполнения функции EXP2, значение операнда не может быть меньше нуля
    public static final int ERROR_FUNC_SMALL_OPERAND_LOG2 = 7; // ошибка выполнения функции LOG2, значение операнда не может быть меньше или равно нуля
    public static final int ERROR_FUNC_UNKNOWN_ERROR = 8; // неизвестная ошибка при выполнении функции
    public static final int ERROR_UNKNOWN = 9; // неизвестная ошибка при выполнении операций в методе loadNumberValue
    private int typeOperand; // тип операнда: число, пользовательское слово, ключевое слово, набор операций
    private boolean flagAssignment = false; // содержит true если операндом является операция присваивания '='. В этом случае nameVarAssignment содержит имя переменной
    private String nameVarAssignment; // имя переменной в операции присваивания
    private int typeRegister; // содержит код конструкции с регистром, если он есть в операнде: +X, -X, +Y, -Y, +Z, -Z. Если регитра нет содержит 0.
    private int numberValue; // числовое значение операнда
    private String stringValue; // строковое значение операнда если это пользовательское слово или ключевое слово
    public ArrayList<ParserOperations.Operation> operations; // список операций, если операндом выступает арифметическое или логическое выражение
    public int getTypeOperand(){
        return typeOperand;
    }
    public void setTypeOperand(int typeOperand){
        this.typeOperand = typeOperand;
    }
    public int getNumberValue(){
        return numberValue;
    }
    public void setNumberValue(int numberValue){
        this.numberValue = numberValue;
    }
    public String getStringValue(){
        return stringValue;
    }
    public void setStringValue(String stringValue){
        this.stringValue = stringValue;
    }
    public void setTypeRegister (int type){
        typeRegister = type;
    }
    public int getTypeRegister (){
        return typeRegister;
    }
    public void setFlagAssignment(){
        flagAssignment = true;
    }
    public boolean getFlagAssignment(){
        return flagAssignment;
    }
    public void setVarAssignment (String nameVar){
        nameVarAssignment = nameVar;
    }
    public String getVarAssignment(){
        return nameVarAssignment;
    }
    public ErrorString loadNumberValue (CollecterCode collecterCode){ // метод загружает значение числового типа в поле numberValue
        ErrorString error = null;
        Avr_registers reg = new Avr_registers();
        Variables vars = new Variables();
        vars.addVars(collecterCode.vars_def);
        vars.addVars (collecterCode.vars_equ);
        vars.addVars (collecterCode.vars_set);
        vars.addVars (collecterCode.labels);
        switch (getTypeOperand()){
            case TYPE_OPERAND_NUMBER:
            case TYPE_OPERAND_CHAR:
                break;
            case TYPE_OPERAND_USERWORD:
                if (vars.isIntVar(this.getStringValue())){
                    this.setNumberValue(vars.getIntValue(this.getStringValue()));
                } else {
                    if (vars.isStrVar(this.getStringValue())){
                        error = new ErrorString (ERROR_OPERAND_STR_MUST_INT,  this.getStringValue(), 0);
                    } else {
                        error = new ErrorString (ERROR_OPERAND_UNKNOWN_USERWORD,  this.getStringValue(), 0);
                    } // конец if (vars.isStrValue(this.getStringValue()))
                } // конец if (vars.isIntVar(operand.getStringValue()))
                break;
            case TYPE_OPERAND_KEYWORD:
                if (reg.getNumRegister(getStringValue()) == reg.REG_PC){
                    setNumberValue (collecterCode.getCSEG());
                } else {
                    error = new ErrorString (ERROR_OPERAND_NO_INT, this.getStringValue(), 0);
                } // конец if (reg.getNumRegister(getStringValue()) == reg.REG_PC)
                break;
            case TYPE_OPERAND_OPERATIONS:
                error = operationsRun (vars, collecterCode);
                break;
            default:
                error = new ErrorString (ERROR_OPERAND_NO_INT, this.getStringValue(), 0);
        } // конец switch (getTypeOperand())
        if (error != null){
            error.setTextError (this.getErrorText(error.getNumError()));
        } // конец if (error != null)
        return error;
    } // конец метода loadNumberValue
    private ErrorString operationsRun (Variables vars, CollecterCode collecterCode){ // вспомогательный метод для loadNumberValue, вычисляет результат операций в operations
        ErrorString error = null;
        ParserOperations parserOps = new ParserOperations();
        if (parserOps.runOperations (this.operations, vars, collecterCode)){
            setNumberValue (parserOps.getResult());
        } else {
            switch (parserOps.getErrorStatus()){
                case ParserOperations.ERROR_CONVERT_NO_VAR:
                    error = new ErrorString (ERROR_OPERAND_UNKNOWN_USERWORD,  parserOps.getErrorString(), 0);
                    break;
                case ParserOperations.ERROR_CONVERT_VAR_STR:
                    error = new ErrorString (ERROR_OPERAND_STR_MUST_INT,  parserOps.getErrorString(), 0);
                    break;
                case ParserOperations.ERROR_DIV_ZERO:
                    error = new ErrorString (ERROR_DIV_ZERO,  parserOps.getErrorString(), 0);
                    break;
                case ParserOperations.ERROR_FUNC_BIG_OPERAND_EXP2:
                    error = new ErrorString (ERROR_FUNC_BIG_OPERAND_EXP2, parserOps.getErrorString(), 0);
                    break;
                case ParserOperations.ERROR_FUNC_SMALL_OPERAND_EXP2:
                    error = new ErrorString (ERROR_FUNC_SMALL_OPERAND_EXP2, parserOps.getErrorString(), 0);
                    break;
                case ParserOperations.ERROR_FUNC_SMALL_OPERAND_LOG2:
                    error = new ErrorString (ERROR_FUNC_SMALL_OPERAND_LOG2, parserOps.getErrorString(), 0);
                    break;
                case ParserOperations.ERROR_FUNC_UNKNOWN_ERROR:
                    error = new ErrorString (ERROR_FUNC_UNKNOWN_ERROR, parserOps.getErrorString(), 0);
                    break;
                default:
                    error = new ErrorString (ERROR_UNKNOWN, parserOps.getErrorString(), 0);
            } // конец switch (parserOps.getErrorStatus())
        } // конец if (ParserOperations.runOperations (operations, vars))
        return error;
    } // конец метода operationsRun
    public String getErrorText (int numError){ // метод возвращает текст ошибки
        String res = null;
        switch (numError){
            case ERROR_OPERAND_NO_INT:
                switch (MainActivity.language){
                    case 1: // русский
                        res = "Ожидается числовое значение, но обнаружено недопустимое выражение";
                        break;
                    case 2: // английский
                        res = "A numeric value is expected, but an invalid expression was detected";
                } // конец switch (language)
                break;
            case ERROR_OPERAND_STR_MUST_INT:
                res = ParserOperations.getErrorText (ParserOperations.ERROR_CONVERT_VAR_STR);
                break;
            case ERROR_OPERAND_UNKNOWN_USERWORD:
                res = ParserOperations.getErrorText (ParserOperations.ERROR_CONVERT_NO_VAR);
                break;
            case ERROR_DIV_ZERO:
                res = ParserOperations.getErrorText (ParserOperations.ERROR_DIV_ZERO);
                break;
            case ERROR_FUNC_BIG_OPERAND_EXP2:
                res = ParserOperations.getErrorText (ParserOperations.ERROR_FUNC_BIG_OPERAND_EXP2);
                break;
            case ERROR_FUNC_SMALL_OPERAND_EXP2:
                res = ParserOperations.getErrorText (ParserOperations.ERROR_FUNC_SMALL_OPERAND_EXP2);
                break;
            case ERROR_FUNC_SMALL_OPERAND_LOG2:
                res = ParserOperations.getErrorText (ParserOperations.ERROR_FUNC_SMALL_OPERAND_LOG2);
                break;
            case ERROR_FUNC_UNKNOWN_ERROR:
                res = ParserOperations.getErrorText (ParserOperations.ERROR_FUNC_UNKNOWN_ERROR);
                break;
            case ERROR_UNKNOWN:
                switch (MainActivity.language){
                    case 1: // русский
                        res = "Неизвестная ошибка при вычислении операций";
                        break;
                    case 2: // английский
                        res = "Unknown error when calculating operations";
                } // конец switch (language)
        } // конец switch (numError)
        return res;
    } // конец getErrorText
} // конец класса ParserOperand
