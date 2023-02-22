package com.tirex.avr_assembler;

public class Avr_functions {

    public static final int NUM_FUNCTION_HIGH = 1; // номер функции HIGH, возвращает второй байт операнда
    public static final String FUNCTION_STRING_HIGH = "HIGH";
    public static final int NUM_FUNCTION_LOW = 2; // номер функции LOW, возвращает первый байт операнда
    public static final String FUNCTION_STRING_LOW = "LOW";
    public static final int NUM_FUNCTION_BYTE2 = NUM_FUNCTION_HIGH; // то же что и функция HIGH
    public static final String FUNCTION_STRING_BYTE2 = "BYTE2";
    public static final int NUM_FUNCTION_BYTE3 = 3; // номер функции BYTE3, возвращает третий байт операнда
    public static final String FUNCTION_STRING_BYTE3 = "BYTE3";
    public static final int NUM_FUNCTION_BYTE4 = 4; // номер функции BYTE4, возвращает четвертый байт операнда
    public static final String FUNCTION_STRING_BYTE4 = "BYTE4";
    public static final int NUM_FUNCTION_LWRD = 5; // номер функции LWRD, возвращает младшее слово операнда (биты 0-15)
    public static final String FUNCTION_STRING_LWRD = "LWRD";
    public static final int NUM_FUNCTION_HWRD = 6; // номер функции HWRD, возвращает старшее слово операнда (биты 16-31)
    public static final String FUNCTION_STRING_HWRD = "HWRD";
    public static final int NUM_FUNCTION_PAGE = 7; // номер функции PAGE, возвращает биты 16-21 операнда
    public static final String FUNCTION_STRING_PAGE = "PAGE";
    public static final int NUM_FUNCTION_EXP2 = 8; // номер функции EXP2, возвращает число, равное 2 в степени operand
    public static final String FUNCTION_STRING_EXP2 = "EXP2";
    public static final int NUM_FUNCTION_LOG2 = 9; // номер функции LOG2, возвращает целую часть степени, в которую надо возвести 2, чтобы получить число operand
    public static final String FUNCTION_STRING_LOG2 = "LOG2";

    public static final int ERROR_BIG_OPERAND_EXP2 = 1; // операнд функции EXP2 не может быть больше 31
    public static final int ERROR_SMALL_OPERAND_EXP2 = 2; // операнд функции EXP2 не может быть меньше нуля
    public static final int ERROR_SMALL_OPERAND_LOG2 = 3; // операнд функции LOG2 не может быть меньше или равен нулю
    public static final int ERROR_UNKNOWN_FUNCTION = 4; // неизвестная функция

    // результат выполнения функции
    private int resultFunction;
    // содержит код ошибки если в процессе выполнения функции обнаружена ошибка, иначе содержит 0
    private int errorStatus;

    // статический метод, если указанная строка functionName является функцией, то возвращает номер этой функции, иначе возвращает ноль
    public static int numFunction (String functionName){
        int result = 0;
        if (functionName.equalsIgnoreCase (FUNCTION_STRING_HIGH)){
            result = NUM_FUNCTION_HIGH;
        } else {
            if (functionName.equalsIgnoreCase (FUNCTION_STRING_LOW)){
                result = NUM_FUNCTION_LOW;
            } else {
                if (functionName.equalsIgnoreCase (FUNCTION_STRING_BYTE2)){
                    result = NUM_FUNCTION_BYTE2;
                } else {
                    if (functionName.equalsIgnoreCase (FUNCTION_STRING_BYTE3)){
                        result = NUM_FUNCTION_BYTE3;
                    } else {
                        if (functionName.equalsIgnoreCase (FUNCTION_STRING_BYTE4)){
                            result = NUM_FUNCTION_BYTE4;
                        } else {
                            if (functionName.equalsIgnoreCase (FUNCTION_STRING_LWRD)){
                                result = NUM_FUNCTION_LWRD;
                            } else {
                                if (functionName.equalsIgnoreCase (FUNCTION_STRING_HWRD)){
                                    result = NUM_FUNCTION_HWRD;
                                } else {
                                    if (functionName.equalsIgnoreCase (FUNCTION_STRING_PAGE)){
                                        result = NUM_FUNCTION_PAGE;
                                    } else {
                                        if (functionName.equalsIgnoreCase (FUNCTION_STRING_EXP2)){
                                            result = NUM_FUNCTION_EXP2;
                                        } else {
                                            if (functionName.equalsIgnoreCase (FUNCTION_STRING_LOG2)){
                                                result = NUM_FUNCTION_LOG2;
                                            } // конец if (functionName.equalsIgnoreCase (FUNCTION_STRING_LOG2)){
                                        } // конец if (functionName.equalsIgnoreCase (FUNCTION_STRING_EXP2){
                                    } // конец if (functionName.equalsIgnoreCase (FUNCTION_STRING_PAGE))
                                } // конец if (functionName.equalsIgnoreCase (FUNCTION_STRING_HWRD))
                            } // конец if (functionName.equalsIgnoreCase (FUNCTION_STRING_LWRD)){
                        } // конец if (functionName.equalsIgnoreCase (FUNCTION_STRING_BYTE4))
                    } // конец if (functionName.equalsIgnoreCase (FUNCTION_STRING_BYTE3))
                } // конец if (functionName.equalsIgnoreCase (FUNCTION_STRING_BYTE2))
            } // конец  if (functionName.equalsIgnoreCase (FUNCTION_STRING_LOW))
        } // конец if (functionName.equalsIgnoreCase (FUNCTION_STRING_HIGH))
        return result;
    } // конец метода numFunction

    // статический метод, возвращает true если указанная строка functionName является функцией
    public static boolean isFunction (String functionName){
        boolean res;
        if (numFunction(functionName) != 0){
            res = true;
        } else {
            res = false;
        }
        return res;
    } // конец метода isFunction

    // основной метод выполнения функции. На входе получает номер функции и числовой операнд.
    // если при выполнении произошла ошибка, то возаращает false. Результат расчета функции сохраняет в resultFunction.
    public boolean runFunction (int numFunction, int operand){
        boolean result = false; // возвращаемый методом результат
        switch (numFunction){
            case NUM_FUNCTION_HIGH:
                resultFunction = runFunctionHigh (operand);
                result = true;
                break;
            case NUM_FUNCTION_LOW:
                resultFunction = runFunctionLow (operand);
                result = true;
                break;
            case NUM_FUNCTION_BYTE3:
                resultFunction = runFunctionByte3 (operand);
                result = true;
                break;
            case NUM_FUNCTION_BYTE4:
                resultFunction = runFunctionByte4 (operand);
                result = true;
                break;
            case NUM_FUNCTION_LWRD:
                resultFunction = runFunctionLwrd (operand);
                result = true;
                break;
            case NUM_FUNCTION_HWRD:
                resultFunction = runFunctionHwrd (operand);
                result = true;
                break;
            case NUM_FUNCTION_PAGE:
                resultFunction = runFunctionPage (operand);
                result = true;
                break;
            case NUM_FUNCTION_EXP2:
                if (operand>31){ // если степень уеазана больше 31, то ошибка
                    errorStatus = ERROR_BIG_OPERAND_EXP2;
                    result = false;
                } else {
                    if (operand<0){ // если степень указана меньше 0, то ошибка
                        errorStatus = ERROR_SMALL_OPERAND_EXP2;
                        result = false;
                    } else { // если степень указана в пределах 0..31, то выполняем функцию
                        resultFunction = runFunctionExp2 (operand);
                        result = true;
                    } // конец if (operand<0)
                } // конец if (operand>31)
                break;
            case NUM_FUNCTION_LOG2:
                if (operand<=0){
                    errorStatus = ERROR_SMALL_OPERAND_LOG2;
                    result = false;
                } else {
                    resultFunction = runFunctionLog2 (operand);
                    result = true;
                }
                break;
            default:
                errorStatus = ERROR_UNKNOWN_FUNCTION;
                result = false;
        } // конец switch (numFunction)
        return result;
    } // конец метода runFunction (int)

    // одноименная функция, на вход получает строковое имя функции и передает в runFunction номер этой функции
    public boolean runFunction (String functionName, int operand){
        boolean res = false;
        int num = numFunction (functionName);
        if (num==0){
            errorStatus = ERROR_UNKNOWN_FUNCTION;
            res = false;
        } else {
            res = runFunction (num, operand);
        } // конец if (num==0)
        return res;
    } // конец метода runFunction (String)

    // вспомогательный метод рассчета функции HIGH или BYTE2
    private int runFunctionHigh (int operand){
        return (operand & (255<<8))>>8;
    } // конец метода runFunctionHigh

    // вспомогательный метод рассчета функции LOW
    private int runFunctionLow (int operand){
        return operand & 255;
    } // конец метода runFunctionLow

    // вспомогательный метод рассчета функции BYTE3
    private int runFunctionByte3 (int operand){
        return (operand & (255<<16))>>16;
    } // конец метода runFunctionByte3

    // вспомогательный метод рассчета функции BYTE4
    private int runFunctionByte4 (int operand){
        return (operand & (255<<24))>>24;
    } // конец метода runFunctionByte4

    // вспомогательный метод рассчета функции LWRD
    private int runFunctionLwrd (int operand){
        return operand & ((255<<8)|255);
    } // конец метода runFunctionLwrd

    // вспомогательный метод рассчета функции HWRD
    private int runFunctionHwrd (int operand){
        return (operand & ((255<<24)|(255<<16)))>>16;
    } // конец метода runFunctionHwrd

    // вспомогательный метод рассчета функции PAGE
    private int runFunctionPage (int operand){
        return (operand & (63<<16))>>16;
    } // конец метода runFunctionPage

    // вспомогательный метод рассчета функции EXP2
    private int runFunctionExp2 (int operand){
        return 1<<operand;
    } // конец метода runFunctionPage

    // вспомогательный метод рассчета функции LOG2
    private int runFunctionLog2 (int operand){
        int res;
        operand = operand >> 1;
        for (res=0; operand>0; res++){
            operand = operand >> 1;
        }
        return res;
    } // конец метода runFunctionPage

    // возвращает результат выполнения функции
    public int getResultFunction (){
        return resultFunction;
    } // конец метода getResult

    // возвращает значение errorStatus
    public int getErrorStatus (){
        return errorStatus;
    } // конец метода getErrorStatus

} // конец класса Avr_functions
