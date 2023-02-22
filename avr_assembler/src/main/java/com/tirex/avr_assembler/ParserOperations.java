package com.tirex.avr_assembler;

import java.util.ArrayList;

import com.tirex.avr_assembler.Variables.*;

public class ParserOperations{

    public static final int ERROR_CONVERT_NO_VAR = 1; // в списке переменных нет переменной с указанным именем
    public static final int ERROR_CONVERT_VAR_STR = 2; // переменная, указанная в арифметической или логической операции имеет строковый тип (имя регистра)
    public static final int ERROR_RUN_OPERATION = 3; // ошибка при выполнении операции (неизвестная операция)
    public static final int ERROR_DIV_ZERO = 4; // деление на ноль при выполнении
    public static final int ERROR_UNKNOWN_OPERATION = 5; // неизвестная операция
    public static final int ERROR_FUNC_BIG_OPERAND_EXP2 = 6; // ошибка выполнения функции EXP2, слишком большое значение операнда
    public static final int ERROR_FUNC_SMALL_OPERAND_EXP2 = 7; // ошибка выполнения функции EXP2, значение операнда не может быть меньше нуля
    public static final int ERROR_FUNC_SMALL_OPERAND_LOG2 = 8; // ошибка выполнения функции LOG2, значение операнда не может быть меньше или равно нуля
    public static final int ERROR_FUNC_UNKNOWN_FUNCTION = 9; // неизвестная функция
    public static final int ERROR_FUNC_UNKNOWN_ERROR = 10; // неизвестная ошибка при выполнении функции

    // перед вызовом runOperations в этот список заносятся имена и значения всех переменных, объявленных в программе
    private Variables programVars;
    // перед вызовом runOperations в этот список заносится список операций, которые необходимо посчитать
    private ArrayList<Operation> operations;
    // содержит результат после выполнения всех операций
    private int resultOperations;
    // содержит код ошибки, если она возникла при выполнении операций. Если ошибок нет, то содержит ноль
    private int errorStatus;
    // содержит слово или значение, в котором выявлена ошибка
    private String errorString;
    private CollecterCode collecterCode;
    private Avr_registers reg = new Avr_registers();

    // абстрактный класс для всех операций
    public static abstract class Operation{
        public static final int TYPE_OPERATION_1_OPERAND = 1; // операция с одним операндом
        public static final int TYPE_OPERATION_2_OPERANDS = 2; // операция с двумя операндами
        public static final int TYPE_OPERAND_WITHOUT_OPERATION= 3; // операция без операнда (число)
        public static final int TYPE_OPERATION_FUNCTION = 4; // функция
        private int typeOperation; // содержит тип операции: с одним операндом, с двумя операндами, без операндов или функция
        private int operationName; // содержит номер операции, который определяет, что это за операция
        private int result; // содержит результат после выполнения операции в методе runOperation()
        public int getResult(){
            return result;
        }
        private void setResult (int result){
            this.result = result;
        }
        public void setTypeOperation (int type_operation){
            typeOperation = type_operation;
        }
        public int getTypeOperation (){
            return typeOperation;
        }
        public void setOperationName (int operation_name){
            operationName = operation_name;
        }
        public int getOperationName (){
            return operationName;
        }
    } // конец абстрактного класса Operation

    // переводит значение операнда из типа OperandOfOperation в тип int, результат записывает в operand.numberValue. Если ошибка - возвращает false
    // вызывается для каждого операнда перед выполнением операции
    private boolean convertOperandInInt (OperandOfOperation operand){
        boolean res = false;
        Variable var;
        switch (operand.getType()){
            case OperandOfOperation.TYPE_NUMBER: // ничего не делаем, operand.numberValue уже содержит число
                res = true;
                break;
            case OperandOfOperation.TYPE_USERWORD: // ищем числовое значение переменной
                var = programVars.getVariable (operand.getStringValue());
                if (var==null){
                    errorStatus = ERROR_CONVERT_NO_VAR;
                    errorString = operand.getStringValue();
                } else {
                    if (var.getType() == Variable.TYPE_STR){
                        errorStatus = ERROR_CONVERT_VAR_STR;
                        errorString = operand.getStringValue ();
                    } else {
                        operand.setNumberValue (var.getIntValue());
                        res = true;
                    } // конец if (var.getType() != TYPE_INT
                } // конец if (var==null)
                break;
            case OperandOfOperation.TYPE_OPERATION:
                operand.setNumberValue(operations.get(operand.getNumberOperation()).getResult());
                res = true;
                break;
            case OperandOfOperation.TYPE_KEYWORD:
                if (reg.getNumRegister(operand.getStringValue()) == reg.REG_PC){
                    operand.setNumberValue(collecterCode.getCSEG());
                    res = true;
                } else {
                    errorStatus = ERROR_CONVERT_VAR_STR;
                    errorString = operand.getStringValue ();
                } // конец if (reg.getNumRegister(operand.getStringValue()) == reg.REG_PC)
        } // конец switch (operand.getType())
        return res;
    } // конец метода convertOperandInInt

    // абстрактный  класс для операций с одним операндом - расширение класса Operation
    public static class Operation1operand extends Operation{
        public static final int OPERATION_UNAR_MINUS = 1; // операция унарный минус
        public static final int OPERATION_UNAR_PLUS = 2; // операция унарный плюс для прединкремента регистров X, Y, Z
        public static final int OPERATION_BIT_NOT = 3; // операция побитное отрицание ~
        public static final int OPERATION_LOGIC_NOT = 4; // операци логическое отрицание !
        private OperandOfOperation operand;
        public void setOperand (OperandOfOperation operand){
            this.operand = operand;
        }
        public OperandOfOperation getOperand (){
            return operand;
        }
		/*public void setOperand (int operandType, int numberValue){
			operand.setType (operandType);
		    operand.setNumberValue (numberValue);
		} // конец метода setOperand для числового значения
		public void setOperand (int operandType, String stringValue){
			operand.setType (operandType);
		    operand.setStringValue (stringValue);
		} // конец метода setOperand для строкового значения
		public void setOperand (int operandType, Operation operation){
			operand.setType (operandType);
			operand.setOperation (operation);
		}*/ // конец метода setOperand для значения, являющегося результатом более ранней операции
    } // конец абстрактного класса Operation1operand

    // абстрактный класс для бинарных операций - расширение класса Operation
    public static class Operation2operands extends Operation{
        public static final int OPERATION_LOGIC_OR = 1; // логическое или ||
        public static final int OPERATION_LOGIC_AND = 2; // логическое и &&
        public static final int OPERATION_BIT_OR = 3; // побитное или |
        public static final int OPERATION_BIT_OR_NOT = 4; // побитное исключающее или ^
        public static final int OPERATION_BIT_AND = 5; // побитное и &
        public static final int OPERATION_NOT_RAVNO = 6; // не равно !=
        public static final int OPERATION_IF_RAVNO = 7; // равно ==
        public static final int OPERATION_BIG_OR_RAVNO = 8; // больше или равно >=
        public static final int OPERATION_BIG = 9; // больше чем >
        public static final int OPERATION_LESS_OR_RAVNO = 10; // меньше или равно <=
        public static final int OPERATION_LESS = 11; // меньше чем <
        public static final int OPERATION_SHIFT_RIGHT = 12; // сдвиг вправо >>
        public static final int OPERATION_SHIFT_LEFT = 13; // сдвиг влево <<
        public static final int OPERATION_PLUS = 14; // сложение +
        public static final int OPERATION_MINUS = 15; // вычитание -
        public static final int OPERATION_DIV = 16; // деление /
        public static final int OPERATION_MULTIPLE = 17; // умножение *
        private OperandOfOperation operand1;
        private OperandOfOperation operand2;
        public void setOperand1 (OperandOfOperation operand1){
            this.operand1 = operand1;
        }
        public void setOperand2 (OperandOfOperation operand2){
            this.operand2 = operand2;
        }
        public OperandOfOperation getOperand1(){
            return operand1;
        }
        public OperandOfOperation getOperand2(){
            return operand2;
        }
		/*public void setOperand1 (int operandType, int numberValue){
			operand1.setType (operandType);
			operand1.setNumberValue (numberValue);
		} // конец метода setOperand1 для числового значения первого операнда
		public void setOperand1 (int operandType, String stringValue){
			operand1.setType (operandType);
		    operand1.setStringValue (stringValue);
		} // конец метода setOperand1 для строкового значения первого операнда
		public void setOperand1 (int operandType, Operation operation){
			operand1.setType (operandType);
			operand1.setOperation (operation);
		} // конец метода setOperand1 для значения, являющегося результатом более ранней операции
		public void setOperand2 (int operandType, int numberValue){
			operand2.setType (operandType);
			operand2.setNumberValue (numberValue);
		} // конец метода setOperand2 для числового значения второго операнда
		public void setOperand2 (int operandType, String stringValue){
			operand2.setType (operandType);
		    operand2.setStringValue (stringValue);
		} // конец метода setOperand2 для строкового значения второго операнда
		public void setOperand2 (int operandType, Operation operation){
			operand2.setType (operandType);
			operand2.setOperation (operation);
		} */// конец метода setOperand2 для значения, являющегося результатом более ранней операции
    } // конец абстрактного класса Operation2operands

    // класс для хранения в списке operations чисел, пользовательских слов и ключевых слов
    public static class OperandWithoutOperation extends Operation{
        private OperandOfOperation operand = new OperandOfOperation();
        public OperandWithoutOperation(){
            setTypeOperation(TYPE_OPERAND_WITHOUT_OPERATION);
        }
        public void setOperand (OperandOfOperation operand){
            this.operand = operand;
        }
        public OperandOfOperation getOperand (){
            return operand;
        }
		/*public int run(){
			int result = 0;
			switch (operand.getType()){
                case TYPE_NUMBER:
				    result = operand.getNumberValue;
				    break;
				case TYPE_OPERATION:
				    result = operand.getOperationResult();
				    break;
				case TYPE_USERWORD:
				    break;
				case TYPE_KEYWORD:
				    break;
			}
			return result;
		}*/
    } // конец класса OperandWithoutOperations

    // класс для операции с токеном "функция"
    public static class OperationFunction extends Operation1operand {
        private String functionName; // хранит имя функции
        public void setFunctionName (String functionName){
            this.functionName = functionName;
        }
        public String getFunctionName (){
            return functionName;
        }
        public boolean determinateFunction (String functionName){ // метод для определения имени функции, возвращает true если строка functionName является функцией
            boolean result = false;
            int num_func = Avr_functions.numFunction(functionName);
            if (num_func != 0){
                setOperationName (num_func);
                setFunctionName (functionName);
                result = true;
            }// конец if (functionName.equalsIgnoreCase (FUNCTION_STRING_HIGH))
            return result;
        } // конец метода determinateFunction
    } // конец класса OperationFunction

    // класс для хранения операнда в классе Operation
    public static class OperandOfOperation{
        public static final int TYPE_NUMBER = 1;
        public static final int TYPE_USERWORD = 2;
        public static final int TYPE_OPERATION = 3;
        public static final int TYPE_KEYWORD = 4;
        private int type; // определяет тип операнда: 1 - число, 2 - строка (пользовательское слово), 3 - порядковый номер операции в списке operations в ParserWord
        private int numberValue; // числовое значение операнда
        private String stringValue; // строковое имя операнда если это пользовательское слово
        //private Operation operation; // содержит ссылку на операцию в списке операций operations класса ParserWord  если операндом является результат более ранней операции
        private int numberOperation; // содержит номер операции из списка операций если операндом является результат более ранней операции
        public void setType(int type){
            this.type = type;
        }
        public int getType(){
            return type;
        }
        public void setNumberValue(int numberValue){
            this.numberValue = numberValue;
        }
        public int getNumberValue(){
            return numberValue;
        }
        public void setStringValue(String stringValue){
            this.stringValue = stringValue;
        }
        public String getStringValue (){
            return stringValue;
        }
        /*public void setOperation(Operation operation){
            this.operation = operation;
        }
        public Operation getOperation(){
            return operation;
        }*/
     /*   public int getOperationResult (){ // через этот метод получаем результат более ранних операций, если тип операнда TYPE_OPERATION
            return operation.getResult;
        }*/
        public void setNumberOperation (int numOperation){
            numberOperation = numOperation;
        }
        public int getNumberOperation(){
            return numberOperation;
        }
    } // конец класса OperandOfOperation

    // основной метод поочередно выполняет все операции в operations
    // возвращает true если операции выполнены без ошибок, результат записывает в переменную resultOperations
    public boolean runOperations (ArrayList<Operation> operationsList, Variables vars, CollecterCode collecterCode){
        operations = operationsList;
        programVars = vars;
        this.collecterCode = collecterCode;
        boolean res = true;
        for (int i = 0; i < operations.size(); i++){
            if (! runOperation (operations.get(i))){
                res = false;
                break;
            } // конец if (! runOperation (operations.get(i)))
        } // конец for (int i = 0; i < operations.size(); i++)
        if (res){
            setResult (operations.get(operations.size()-1).getResult());
        }
        return res;
    } // конец метода runOperations

    // вспомогательный метод выполняет одну операцию из списка, результат сохраняет в переменной operation.result
    // если в процессе выполнения произошла ошибка - возвращает false, иначе true
    private boolean runOperation (Operation operation){
        boolean res; // результат, возвращаемый методом
        switch (operation.getTypeOperation()){
            case Operation.TYPE_OPERATION_1_OPERAND:
                res = runOperation1operand (operation);
                break;
            case Operation.TYPE_OPERATION_2_OPERANDS:
                res = runOperation2operands (operation);
                break;
            case Operation.TYPE_OPERAND_WITHOUT_OPERATION:
                res = runOperandWithoutOperation (operation);
                break;
            case Operation.TYPE_OPERATION_FUNCTION:
                res = runOperationFunction (operation);
                break;
            default:
                res = false;
                errorStatus = ERROR_UNKNOWN_OPERATION;
        } // конец switch (operation.getTypeOperation())
        return res;
    } // конец метода runOperation

    // вспомогательный метод выполнения операции с одним операндом
    private boolean runOperation1operand (Operation operation){
        boolean res = true; // результат, возвращаемый методом
        Operation1operand op1 = (Operation1operand) operation;
        if (! convertOperandInInt (op1.getOperand())){
            res = false;
        } else {
            switch (operation.getOperationName()){
                case Operation1operand.OPERATION_UNAR_MINUS:
                    operation.setResult (- op1.getOperand().getNumberValue());
                    break;
                case Operation1operand.OPERATION_UNAR_PLUS:
                    operation.setResult (op1.getOperand().getNumberValue());
                    break;
                case Operation1operand.OPERATION_BIT_NOT: // побитовое отрицание
                    operation.setResult (~ op1.getOperand().getNumberValue());
                    break;
                case Operation1operand.OPERATION_LOGIC_NOT: // если операнд равен нулю, то возвращает 1, иначе 0
                    if (op1.getOperand().getNumberValue()==0){
                        operation.setResult (1);
                    } else {
                        operation.setResult (0);
                    } // конец if (op1.getOperand().getNumberValue()==0)
                default:
                    errorStatus = ERROR_RUN_OPERATION;
                    res = false;
            } // конец switch (operation.getOperationName())
        } // конец if (! convertOperandInInt (op1.getOperand))
        return res;
    } // конец метода runOperation1operand

    // вспомогательный метод выполнения операции с двумя операндами
    private boolean runOperation2operands (Operation operation){
        boolean res = true; // результат, возвращаемый методом
        Operation2operands op2 = (Operation2operands) operation;
        if ((! convertOperandInInt(op2.getOperand1())) || (! convertOperandInInt(op2.getOperand2()))){
            res = false;
        } else {
            switch (operation.getOperationName()){
                case Operation2operands.OPERATION_LOGIC_OR:
                    operation.setResult (runLogicOr(op2));
                    break;
                case Operation2operands.OPERATION_LOGIC_AND:
                    operation.setResult (runLogicAnd(op2));
                    break;
                case Operation2operands.OPERATION_BIT_OR:
                    operation.setResult (op2.getOperand1().getNumberValue() | op2.getOperand2().getNumberValue());
                    break;
                case Operation2operands.OPERATION_BIT_OR_NOT:
                    operation.setResult (op2.getOperand1().getNumberValue() ^ op2.getOperand2().getNumberValue());
                    break;
                case Operation2operands.OPERATION_BIT_AND:
                    operation.setResult (op2.getOperand1().getNumberValue() & op2.getOperand2().getNumberValue());
                    break;
                case Operation2operands.OPERATION_NOT_RAVNO:
                    operation.setResult (runNoRavno(op2));
                    break;
                case Operation2operands.OPERATION_IF_RAVNO:
                    operation.setResult (runIfRavno(op2));
                    break;
                case Operation2operands.OPERATION_BIG_OR_RAVNO:
                    operation.setResult (runBigOrRavno(op2));
                    break;
                case Operation2operands.OPERATION_BIG:
                    operation.setResult (runBig(op2));
                    break;
                case Operation2operands.OPERATION_LESS_OR_RAVNO:
                    operation.setResult (runLessOrRavno(op2));
                    break;
                case Operation2operands.OPERATION_LESS:
                    operation.setResult (runLess(op2));
                    break;
                case Operation2operands.OPERATION_SHIFT_RIGHT:
                    operation.setResult (runShiftRight(op2));
                    break;
                case Operation2operands.OPERATION_SHIFT_LEFT:
                    operation.setResult (runShiftLeft(op2));
                    break;
                case Operation2operands.OPERATION_PLUS:
                    operation.setResult (op2.getOperand1().getNumberValue() + op2.getOperand2().getNumberValue());
                    break;
                case Operation2operands.OPERATION_MINUS:
                    operation.setResult (op2.getOperand1().getNumberValue() - op2.getOperand2().getNumberValue());
                    break;
                case Operation2operands.OPERATION_DIV:
                    if (op2.getOperand2().getNumberValue() != 0){
                        operation.setResult (op2.getOperand1().getNumberValue() / op2.getOperand2().getNumberValue());
                    } else {
                        errorStatus = ERROR_DIV_ZERO;
                        res = false;
                    }
                    break;
                case Operation2operands.OPERATION_MULTIPLE:
                    operation.setResult (op2.getOperand1().getNumberValue() * op2.getOperand2().getNumberValue());
                    break;
                default:
                    errorStatus = ERROR_RUN_OPERATION;
                    res = false;
            } // конец switch (operation.getOperationName())
        } // конец if ((! convertOperandInInt(op2.getOperand1())) || (! convertOperandInInt(op2.getOperand2())))
        return res;
    } // конец метода runOperation2operands

    // вспомогательный метод для операнда без операции
    private boolean runOperandWithoutOperation (Operation operation){
        boolean res;
        OperandWithoutOperation op = (OperandWithoutOperation) operation;
        if (! convertOperandInInt(op.getOperand())){
            res = false;
        } else {
            operation.setResult(op.getOperand().getNumberValue());
            res = true;
        }
        return res;
    } // конец метода runOperandWithoutOperation

    // вспомогательный метод вычисления функции
    public boolean runOperationFunction (Operation operation){
        boolean res = false;
        OperationFunction opfunc = (OperationFunction) operation;
        Avr_functions func = new Avr_functions(); // переменная для вычисления функции
        if (! convertOperandInInt (opfunc.getOperand())){
            res = false;
        } else {
            res = func.runFunction (operation.getOperationName(), opfunc.getOperand().getNumberValue());
            if (res){ // если функция выполнена без ошибок
                operation.setResult (func.getResultFunction());
            } else { // обрабатываем ошибки
                switch (func.getErrorStatus()){
                    case Avr_functions.ERROR_BIG_OPERAND_EXP2:
                        errorStatus = ERROR_FUNC_BIG_OPERAND_EXP2;
                        errorString = Integer.toString(opfunc.getOperand().getNumberValue());
                        break;
                    case Avr_functions.ERROR_SMALL_OPERAND_EXP2:
                        errorStatus = ERROR_FUNC_SMALL_OPERAND_EXP2;
                        errorString = Integer.toString(opfunc.getOperand().getNumberValue());
                        break;
                    case Avr_functions.ERROR_SMALL_OPERAND_LOG2:
                        errorStatus = ERROR_FUNC_SMALL_OPERAND_LOG2;
                        errorString = Integer.toString(opfunc.getOperand().getNumberValue());
                        break;
                    case Avr_functions.ERROR_UNKNOWN_FUNCTION:
                        errorStatus = ERROR_FUNC_UNKNOWN_FUNCTION;
                        errorString = opfunc.getFunctionName();
                        break;
                    default:
                        errorStatus = ERROR_FUNC_UNKNOWN_ERROR;
                        errorString = opfunc.getFunctionName();
                } // конец switch (func.getErrorStatus())
            } // конец if (res)
        } // конец if (! convertOperandInInt (opfunc.getOperand()))
        return res;
    } // конец метода runOperationFunction

    // вспомогательный метод заносит в переменную resultOperations конечный результат выполнения всех операций
    private void setResult (int res){
        resultOperations = res;
    } // конец метода setResult

    // метод возвращает значение переменной resultOperations
    public int getResult (){
        return resultOperations;
    } // конец метода getResult

    // возвращает значение errorStatus: код ошибки или ноль
    public int getErrorStatus (){
        return errorStatus;
    }// конец метода getErrorStatus

    // возвращает значение errorString: строка (слово), в котором обнаружена ошибка при выполнении
    public String getErrorString (){
        return errorString;
    }

    // вспомогательный метод, если хоть один из операндов не равен нулю, то возвращает 1, иначе 0
    private int runLogicOr (Operation2operands op2){
        int res;
        if ((op2.getOperand1().getNumberValue() != 0)||(op2.getOperand2().getNumberValue() != 0)){
            res = 1;
        } else {
            res = 0;
        }
        return res;
    } // конец метода runLogicOr

    // вспомогательный метод, если оба операнда не равны нулю, то возвращает 1, иначе 0
    private int runLogicAnd (Operation2operands op2){
        int res;
        if ((op2.getOperand1().getNumberValue() !=0)&&(op2.getOperand2().getNumberValue() !=0)){
            res = 1;
        } else {
            res = 0;
        }
        return res;
    } // конец метода runLogicAnd

    // вспомогательный метод, если операнды не равны, то возвращает 1, иначе 0
    private int runNoRavno (Operation2operands op2){
        int res;
        if (op2.getOperand1().getNumberValue() != op2.getOperand2().getNumberValue()){
            res = 1;
        } else {
            res = 0;
        }
        return res;
    } // конец метода runNoRavno

    // вспомогательный метод, если операнды равны, то возвращает 1, иначе 0
    private int runIfRavno (Operation2operands op2){
        int res;
        if (op2.getOperand1().getNumberValue() == op2.getOperand2().getNumberValue()){
            res = 1;
        } else {
            res = 0;
        }
        return res;
    } // конец метода runIfRavno

    // вспомогательный метод, если первый операнд больше или равен второму, то возвращает 1, иначе 0
    private int runBigOrRavno (Operation2operands op2){
        int res;
        if (op2.getOperand1().getNumberValue() >= op2.getOperand2().getNumberValue()){
            res = 1;
        } else {
            res = 0;
        }
        return res;
    } // конец метода runBigOrRavno

    // вспомогательный метод, если первый операнд больше второго, то возвращает 1, иначе 0
    private int runBig (Operation2operands op2){
        int res;
        if (op2.getOperand1().getNumberValue() > op2.getOperand2().getNumberValue()){
            res = 1;
        } else {
            res = 0;
        }
        return res;
    } // конец метода runBig

    // вспомогательный метод, если первый операнд меньше или равен второму, то возвращает 1, иначе 0
    private int runLessOrRavno (Operation2operands op2){
        int res;
        if (op2.getOperand1().getNumberValue() <= op2.getOperand2().getNumberValue()){
            res = 1;
        } else {
            res = 0;
        }
        return res;
    } // конец метода runLessOrRavno

    // вспомогательный метод, если первый операнд меньше второго, то возвращает 1, иначе 0
    private int runLess (Operation2operands op2){
        int res;
        if (op2.getOperand1().getNumberValue() < op2.getOperand2().getNumberValue()){
            res = 1;
        } else {
            res = 0;
        }
        return res;
    } // конец метода runLess

    // вспомогательный метод, операция сдвиг вправо
    private int runShiftRight (Operation2operands op2){
        int res;
        if (op2.getOperand2().getNumberValue()<16){
            res = op2.getOperand1().getNumberValue() >> op2.getOperand2().getNumberValue();
        } else {
            res = 0;
        }
        return res;
    } // конец метода runShiftRight

    // вспомогательный метод, операция сдвиг влево
    private int runShiftLeft (Operation2operands op2){
        int res;
        if (op2.getOperand2().getNumberValue()<16){
            res = op2.getOperand1().getNumberValue() << op2.getOperand2().getNumberValue();
        } else {
            res = 0;
        }
        return res;
    } // конец метода runShiftLeft

    public static String getErrorText (int numError){ // метод возвращает текст ошибки
        String res = null;
        switch (MainActivity.language) {
            case 1: // русский
                switch (numError) {
                    case ERROR_CONVERT_NO_VAR:
                        res = "Обнаружено неизвестное выражение";
                        break;
                    case ERROR_CONVERT_VAR_STR:
                        res = "Недопустимое выражение, ожидается числовое значение";
                        break;
                    case ERROR_RUN_OPERATION:
                        res = "Ошибка при вычислении, неизвестная операция";
                        break;
                    case ERROR_DIV_ZERO:
                        res = "Недопустимое выражение, деление на ноль";
                        break;
                    case ERROR_UNKNOWN_OPERATION:
                        res = "Неизвестная операция";
                        break;
                    case ERROR_FUNC_BIG_OPERAND_EXP2:
                        res = "Значение операнда функции EXP2 превышает допустимое значение";
                        break;
                    case ERROR_FUNC_SMALL_OPERAND_EXP2:
                        res = "Значение операнда функции EXP2 не может быть меньше нуля";
                        break;
                    case ERROR_FUNC_SMALL_OPERAND_LOG2:
                        res = "Значение операнда функции LOG2 не может быть меньше или равно нулю";
                        break;
                    case ERROR_FUNC_UNKNOWN_FUNCTION:
                        res = "Неизвестная функция";
                        break;
                    case ERROR_FUNC_UNKNOWN_ERROR:
                        res = "Неизвестная ошибка при вычислении функции";
                        break;
                }
                break;
            case 2: // английский
                switch (numError) {
                    case ERROR_CONVERT_NO_VAR:
                        res = "An unknown expression was detected";
                        break;
                    case ERROR_CONVERT_VAR_STR:
                        res = "Invalid expression, numeric value expected";
                        break;
                    case ERROR_RUN_OPERATION:
                        res = "An error occurred while evaluating, the unknown operation";
                        break;
                    case ERROR_DIV_ZERO:
                        res = "Invalid expression, division by zero";
                        break;
                    case ERROR_UNKNOWN_OPERATION:
                        res = "Unknown operation";
                        break;
                    case ERROR_FUNC_BIG_OPERAND_EXP2:
                        res = "The value of the operand of the function EXP2 exceeds the allowed value";
                        break;
                    case ERROR_FUNC_SMALL_OPERAND_EXP2:
                        res = "The value of the operand of the function EXP2 cannot be less than zero";
                        break;
                    case ERROR_FUNC_SMALL_OPERAND_LOG2:
                        res = "The value of the LOG2 operand cannot be less than or equal to zero";
                        break;
                    case ERROR_FUNC_UNKNOWN_FUNCTION:
                        res = "Unknown function";
                        break;
                    case ERROR_FUNC_UNKNOWN_ERROR:
                        res = "Unknown error when calculating the function";
                        break;
                }
        }
        return res;
    } // конец getErrorText

} // конец класса ParserOperations

