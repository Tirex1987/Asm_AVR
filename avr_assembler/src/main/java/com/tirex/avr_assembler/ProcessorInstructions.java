package com.tirex.avr_assembler;

import java.util.ArrayList;

public class ProcessorInstructions{

    private CollecterCode collecterCode;
    private ErrorInstructions errors; // содержит ошибки, возникающие при вызове инструкции
    private Avr_registers regs = new Avr_registers();
    private Avr_instructions instruction;

    public static final int ERROR_NO_2_OPERANDS = 1; // для инструкции необходимо 2 операнда, но указано либо меньше, либо больше, либо вообще не указано
    public static final int ERROR_OPERAND_NO_REGISTER = 2; // недопустимое выражение, ожидается регистр
    public static final int ERROR_OPERAND_1_NO_REGISTER = 3; // недопустимое выражение в первом операнде инструкции, ожидается регистр
    public static final int ERROR_OPERAND_2_NO_REGISTER = 4; // недопустимое выражение во втором операнде инструкции, ожидается регистр
    public static final int ERROR_OPERAND_1_NO_PARE_REGISTERS = 5; // недопустимое выражение в первом операнде инструкции, ожидается регистровая пара
    public static final int ERROR_OPERAND_2_NO_INT = 6; // недопустимое выражение, ожидается числовое значение
    public static final int ERROR_OPERAND_NO_INT = 7; // Ожидается числовой операнд
    public static final int ERROR_OPERAND_DEF_MUST_INT = 8; // Ожидается числовое значение, но обнаружено наименование регистра
    public static final int ERROR_OPERAND_UNKNOWN_USERWORD = 9; // Недопустимое выражение, обнаружено не определенное ранее пользовательское слово
    public static final int ERROR_DIV_ZERO = 10; // Недопустимое выражение, деление на ноль
    public static final int ERROR_FUNC_BIG_OPERAND_EXP2 = 11; // Значение операнда функции EXP2 превышает допустимое значение
    public static final int ERROR_FUNC_SMALL_OPERAND_EXP2 = 12; // Значение операнда функции EXP2 не может быть меньше нуля
    public static final int ERROR_FUNC_SMALL_OPERAND_LOG2 = 13; // Значение операнда функции LOG2 не может быть меньше или равно нулю
    public static final int ERROR_FUNC_UNKNOWN_ERROR = 14; // Неизвестная функция
    public static final int ERROR_UNKNOWN = 15; // Unknown error when calculating operations
    //public static final int ERROR_OPERAND_2_NO_INT = 16; // недопустимое выражение в операнде 2, ожидается числовое значение
    public static final int ERROR_BIG_OR_SMALL_OPERAND_2 = 17; // числовое значение второго операнда выходит за допустимый диапазон
    public static final int ERROR_NO_1_OPERAND = 18; // // для инструкции необходим 1 операнд, но указано либо меньше, либо больше, либо вообще не указано
    public static final int ERROR_OPERAND_INVALID_VALUE = 19; //недопустимое выражение в операнде (единственном), ожидается числовое значение
    public static final int ERROR_LABEL_OUT_RANGE = 20; // метка, указанная в инструкции прыжка или вызова подпрограммы, вне диапазона
    public static final int ERROR_OPERAND_2_INVALID_VALUE = 21; // недопустимое выражение во втором операнде инструкции
    public static final int ERROR_OPERAND_1_INVALID_VALUE = 22; // недопустимое выражение в первом операнде инструкции
    public static final int ERROR_INT_OUT_RANGE = 23; // недопустимое значение операнда, вне диапазона
    public static final int ERROR_INVALID_OPERAND = 24; //недопустимое выражение в операнде (единственном)
    public static final int ERROR_BIG_OR_SMALL_OPERAND_1 = 25; // числовое значение первого операнда выходит за допустимый диапазон
    public static final int ERROR_IS_OPERANDS = 26; // обнаружен(ы) операнд(ы), но инструкция используется без операндов
    public static final int ERROR_NUM_REGISTER_IO = 27; // недопустимый номер регистра ввода-вывода
    public static final int ERROR_OPERAND_2_NO_PARE_REGISTERS = 28; // недопустимое выражение во втором операнде инструкции, ожидается регистровая пара
    public static final int ERROR_UNKNOWN_INSTRUCTION = 29; // неизвестная инструкция

    public ProcessorInstructions (CollecterCode collecterCode){
        this.collecterCode = collecterCode;
    } // конец конструктора

    private class ErrorInstructions{
        private ArrayList <ErrorString> error = new ArrayList();
        public void add (int numError, String errorWord){
            error.add (new ErrorString(numError, errorWord, 0));
            error.get(error.size()-1).setTextError(getErrorText(numError)+" : "+errorWord);
        }
        public ArrayList<ErrorString> getList(){
            return error;
        }
    } // конец класса ErrorInstructions

    // метод возвращает список ошибок
    public ArrayList<ErrorString> getErrors (){
        return errors.getList();
    } // конец метода getErrors

    // основной метод обработки инструкции
    public void runInstruction (Avr_instructions instruction, ArrayList<ParserOperand> operands){
        errors = new ErrorInstructions();
        this.instruction = instruction;
        switch (instruction){
            case ADC:
            case ADD:
            case AND:
            case CP:
            case CPC:
            case CPSE:
            case MOV:
            case MUL:
            case OR:
            case SBC:
            case SUB:
            case EOR:
                instructionsType_1 (operands);
                break;
            case ADIW:
            case SBIW:
                instructionsType_2 (operands);
                break;
            case RCALL:
                if (instructionsType_3 (operands)) { collecterCode.incStackDepthFromCall(); }
                break;
            case RJMP:
                instructionsType_3 (operands);
                break;
            case ASR:
            case COM:
            case DEC:
            case INC:
            case LSR:
            case NEG:
            case ROR:
            case SWAP:
                instructionsType_4 (operands);
                break;
            case PUSH:
                if (instructionsType_4 (operands)){ collecterCode.incStackDepth(); }
                break;
            case POP:
                if (instructionsType_4 (operands)){ collecterCode.decStackDepth(); }
                break;
            case LD:
                instructionsType_4_ld (operands);
                break;
            case ST:
                instructionsType_4_st (operands);
                break;
            case LPM:
                if (operands==null) { collecterCode.code.setWord ((short) 0x95C8); // для инструкции без операндов
                } else { instructionsType_4_lpm (operands); }
                break;
            case ELPM:
                if (operands==null) { collecterCode.code.setWord ((short) 0x95D8); // для инструкции без операндов
                } else { instructionsType_4_lpm (operands); }
                break;
            case BCLR:
            case BSET:
                instructionsType_5 (operands);
                break;
            case BLD:
            case BST:
            case SBRC:
            case SBRS:
                instructionsType_6 (operands);
                break;
            case BRBS:
            case BRBC:
                instructionsType_7 (operands);
                break;
            case ICALL:
                if (instructionsType_8 (operands)) { collecterCode.incStackDepthFromCall (); }
                break;
            case RET:
            case RETI:
                if (instructionsType_8 (operands)) { collecterCode.decStackDepthFromRet (); }
                break;
            case NOP:
            case IJMP:
            case SLEEP:
            case WDR:
                instructionsType_8 (operands);
                break;
            case SPM:
                if (operands==null) { collecterCode.code.setWord ((short) 0x95E8); // для инструкции без операндов
                } else { instructionsType_8_spm (operands); }
                break;
            case IN:
                instructionsType_9_in (operands);
                break;
            case OUT:
                instructionsType_9_out (operands);
                break;
            case MULSU:
            case FMUL:
            case FMULS:
            case FMULSU:
                instructionsType_10 (operands);
                break;
            case MOVW:
                instructionsType_12a (operands);
                break;
            case MULS:
                instructionsType_12b (operands);
                break;
            case CPI:
            case SBCI:
            case SUBI:
            case ORI:
            case ANDI:
            case LDI:
                instructionsType_13 (operands);
                break;
            case CBI:
            case SBIC:
            case SBI:
            case SBIS:
                instructionsType_15 (operands);
                break;
            case LDD:
            case STD:
                instructionsType_16 (operands);
                break;
            case CALL:
                if (instructionsType_11 (operands)) { collecterCode.incStackDepthFromCall(); }
                break;
            case JMP:
                instructionsType_11 (operands);
                break;
            case STS:
            case LDS:
                instructionsType_14 (operands);
                break;
            case BRCC:
            case BRSH:
            case BRNE:
            case BRPL:
            case BRVC:
            case BRGE:
            case BRHC:
            case BRTC:
            case BRID:
            case BRCS:
            case BRLO:
            case BREQ:
            case BRMI:
            case BRVS:
            case BRLT:
            case BRHS:
            case BRTS:
            case BRIE:
                instructionsType_BRBC_BRBS (operands);
                break;
            case CLC:
            case CLZ:
            case CLN:
            case CLV:
            case CLS:
            case CLH:
            case CLT:
            case CLI:
            case SEC:
            case SEZ:
            case SEN:
            case SEV:
            case SES:
            case SEH:
            case SET:
            case SEI:
                instructionsType_8 (operands); // для инструкций без операндов
                break;
            case TST:
            case CLR:
            case LSL:
            case ROL:
                instructionsType_1_subtype (operands);
                break;
            case CBR:
                instructionsType_13_subtype_CBR (operands);
                break;
            case SBR:
                instructionsType_13 (operands); // аналог инструкции ORI Rd, K
                break;
            case SER:
                instructionsType_13_subtype_SER(operands);
                break;
            case EIJMP:
            case EICALL:
            case BREAK:
                instructionsType_8 (operands); // инструкции без операндов
                break;
            case DES:
                instructionsType_DES (operands);
                break;
            case XCH:
            case LAS:
            case LAC:
            case LAT:
                instructionsType_XCH (operands);
                break;
            default:
                errors.add (ERROR_UNKNOWN_INSTRUCTION, instruction.toString());
        } // конец switch (instruction)
    } // конец метода runInstruction

    // вспомогательный метод, возвращает номер регистра по операнду parserOperand. Если это не регистр или регистр PC, то возвращает -1
    private short getKeywordNum (ParserOperand parserOperand){
        short res = (short) -1;
        if ((! parserOperand.getFlagAssignment())&&(parserOperand.getTypeRegister()==0)){
            switch (parserOperand.getTypeOperand()){
                case ParserOperand.TYPE_OPERAND_KEYWORD:
                    res = regs.getNumRegister (parserOperand.getStringValue());
                    break;
                case ParserOperand.TYPE_OPERAND_USERWORD:
                    if (collecterCode.vars_def.isStrVar(parserOperand.getStringValue())){
                        res = regs.getNumRegister (collecterCode.vars_def.getStrValue(parserOperand.getStringValue()));
                    } // конец if (collecterCode.vars_def.isStrValue(parserOperand.getStringValue()))
            } // конец switch (parserOperand.getTypeOperand())
            if (res == regs.REG_PC){
                res = (short) -1;
            } // конец if (res == regs.REG_PC)
        } // конец if ((! parserOperand.getFlagAssignment())&&(parserOperand.getTypeRegister()==0))
        return res;
    } // конец метода getKeywordNum

    // метод проверяет отсутствие в операнде операции присваивания и пост или пред инкрементов/декркментов регистров.
    // если ошибок нет, то возвращает true
    private boolean checkValueOperand (ParserOperand parserOperand){
        if ((parserOperand.getFlagAssignment())||(parserOperand.getTypeRegister()>0)){
            return false;
        } // конец if ((operands.get(0).getFlagAssognment())||(operands.get(0).getTypeRegister()>0))
        return true;
    } // конец chekValueOperand

    // вспомогательный метод проверяет кол-во операндов в operands. Если кол-во равно numOperands, то возвращает true, иначе false
    private boolean checkNumOperands (ArrayList<ParserOperand> operands, int numOperands){
        boolean res = true;
        switch (numOperands){
            case 1:
                if ((operands==null)||(operands.size() != 1)){
                    errors.add (ERROR_NO_1_OPERAND, instruction.toString());
                    res = false;
                } // конец if ((operands==null)||(operands.size() != 1))
                break;
            case 2:
                if ((operands==null)||(operands.size() != 2)){
                    errors.add (ERROR_NO_2_OPERANDS, instruction.toString());
                    res = false;
                } // конец if ((operands==null)||(operands.size() != 2))
                break;
            default: // если кол-во операндов должно быть ноль
                if (operands != null){
                    errors.add (ERROR_IS_OPERANDS, instruction.toString());
                    res = false;
                } // конец if (operands != null)
        } // конец switch (numOperands)
        return res;
    } // конец метода checkNumOperands

    // метод загружает в поле numberValue операнда operand числовое значение. Вызывается после метода checkValueOperand.
    // Если загрузить число невозможно, то записывает ошибку и возвращает false
    private boolean loadNumberValueForOperand (ParserOperand operand){
        boolean res = false;
        ErrorString error;
        error = operand.loadNumberValue (collecterCode);
        if (error != null){
            switch (error.getNumError()){
                case ParserOperand.ERROR_OPERAND_NO_INT:
                    errors.add (ERROR_OPERAND_NO_INT, error.getErrorWord());
                    break;
                case ParserOperand.ERROR_OPERAND_STR_MUST_INT:
                    errors.add (ERROR_OPERAND_DEF_MUST_INT, error.getErrorWord());
                    break;
                case ParserOperand.ERROR_OPERAND_UNKNOWN_USERWORD:
                    errors.add (ERROR_OPERAND_UNKNOWN_USERWORD, error.getErrorWord());
                    break;
                case ParserOperand.ERROR_DIV_ZERO:
                    errors.add (ERROR_DIV_ZERO, error.getErrorWord());
                    break;
                case ParserOperand.ERROR_FUNC_BIG_OPERAND_EXP2:
                    errors.add (ERROR_FUNC_BIG_OPERAND_EXP2, error.getErrorWord());
                    break;
                case ParserOperand.ERROR_FUNC_SMALL_OPERAND_EXP2:
                    errors.add (ERROR_FUNC_SMALL_OPERAND_EXP2, error.getErrorWord());
                    break;
                case ParserOperand.ERROR_FUNC_SMALL_OPERAND_LOG2:
                    errors.add (ERROR_FUNC_SMALL_OPERAND_LOG2, error.getErrorWord());
                    break;
                case ParserOperand.ERROR_FUNC_UNKNOWN_ERROR:
                    errors.add (ERROR_FUNC_UNKNOWN_ERROR, error.getErrorWord());
                    break;
                case ParserOperand.ERROR_UNKNOWN:
                    errors.add (ERROR_UNKNOWN, error.getErrorWord());
                    break;
            } // конец switch (error.getNumError())
        } else {
            res = true;
        } // конец if (error != null)
        return res;
    } // конец loadNumberValueForOperand

    // вспомогательный класс для передачи результата отработки вспомогательных методов
    // если обнаружена ошибка, то isError содержит true, иначе поле result содержит результат метода
    private class ResultForMethod{
        public boolean isError;
        public int result;
    } // конец класса resultForMethod

    // вспомогательный метод для обработки операнда-метки parserOperand. minValue и maxValue - крайние значения допустимого диапазона для прыжка
    // numOperand - номер операнда parserOperand в инструкции: 0 -единственный операнд, 1 -первый операнд, 2 -второй операнд
    private ResultForMethod loadLabelOperand (ParserOperand parserOperand, int numOperand, int minValue, int maxValue){
        ResultForMethod res = new ResultForMethod();
        res.isError = false;
        int numError, vspom;
        if (! checkValueOperand(parserOperand)){
            if (numOperand==1){
                numError = ERROR_OPERAND_1_INVALID_VALUE;
            } else {
                if (numOperand==2){
                    numError = ERROR_OPERAND_2_INVALID_VALUE;
                } else {
                    numError = ERROR_OPERAND_INVALID_VALUE;
                } // конец if (numOperand==2)
            } // конец if (numOperand==1)
            errors.add (numError, instruction.toString());
            res.isError = true;
        } else { // if (! checkValueOperand(parserOperand))
            if (! loadNumberValueForOperand(parserOperand)){
                res.isError = true;
                return res;
            } // конец if (! loadNumberValueForOperand(parserOperand))
            vspom = parserOperand.getNumberValue() - collecterCode.getCSEG() -1;
            if ((vspom<(minValue))||(vspom>maxValue)){
                errors.add (ERROR_LABEL_OUT_RANGE, instruction.toString());
                res.isError = true;
                return res;
            } else {
                res.result = vspom;
            } // конец if ((vspom<minValue)||(vspom>maxValue))
        } // конец if (! checkValueOperand(parserOperand))
        return res;
    } // конец метода loadLabelOperand

    // вспомогательный метод для обработки операнда-числа parserOperand. minValue и maxValue - крайние значения допустимого диапазона чисел.
    // numOperand - номер операнда parserOperand в инструкции: 0 -единственный операнд, 1 -первый операнд, 2 -второй операнд
    private ResultForMethod loadIntOperand (ParserOperand parserOperand, int numOperand, int minValue, int maxValue){
        ResultForMethod res = new ResultForMethod();
        int numError;
        res.isError = false;
        if (! checkValueOperand(parserOperand)){
            switch (numOperand){
                case 1:
                    numError = ERROR_OPERAND_1_INVALID_VALUE; break;
                case 2:
                    numError = ERROR_OPERAND_2_NO_INT; break;
                default:
                    numError = ERROR_OPERAND_INVALID_VALUE;
            } // конец switch (numOperand)
            errors.add (numError, instruction.toString());
            res.isError = true;
            return res;
        } // конец if (! checkValueOperand(parserOperand))
        if (! loadNumberValueForOperand(parserOperand)){
            res.isError = true;
            return res;
        } // конец if (! loadNumberValueForOperand(parserOperand))
        if ((parserOperand.getNumberValue()<minValue)||(parserOperand.getNumberValue()>maxValue)){
            switch (numOperand){
                case 1:
                    numError = ERROR_BIG_OR_SMALL_OPERAND_1; break;
                case 2:
                    numError = ERROR_BIG_OR_SMALL_OPERAND_2; break;
                default:
                    numError = ERROR_INT_OUT_RANGE;
            } // конец switch (numOperand)
            errors.add (numError, instruction.toString());
            res.isError = true;
            return res;
        } else {
            res.result = parserOperand.getNumberValue();
        } // конец if ((parserOperand.getNumberValue()<minValue)||(parserOperand.getNumberValue()>maxValue))
        return res;
    } // конец метода loadIntOperand

    // вспомогательный метод для обработки операнда-регистра parserOperand. minValue и maxValue - крайние значения допустимого диапазона регистров.
    // numOperand - номер операнда parserOperand в инструкции: 0 -единственный операнд, 1 -первый операнд, 2 -второй операнд
    private ResultForMethod loadKeywordOperand (ParserOperand parserOperand, int numOperand, int minValue, int maxValue){
        ResultForMethod res = new ResultForMethod();
        int numError;
        res.isError = false;
        res.result = getKeywordNum (parserOperand);
        if (res.result<0){
            switch (numOperand){
                case 1:
                    numError = ERROR_OPERAND_1_NO_REGISTER; break;
                case 2:
                    numError = ERROR_OPERAND_2_NO_REGISTER; break;
                default:
                    numError = ERROR_OPERAND_NO_REGISTER;
            } // конец switch (numOperand)
            errors.add (numError, instruction.toString());
            res.isError = true;
        } else {
            if ((res.result<minValue)||(res.result>maxValue)){
                switch (numOperand){
                    case 1:
                        numError = ERROR_OPERAND_1_INVALID_VALUE; break;
                    case 2:
                        numError = ERROR_OPERAND_2_INVALID_VALUE; break;
                    default:
                        numError = ERROR_INVALID_OPERAND;
                } // конец switch (numOperand)
                errors.add (numError, instruction.toString());
                res.result = -1;
                res.isError = true;
            } // конец if ((res.result<minValue)||(res.result>maxValue))
        } // конец if (res.result<0)
        return res;
    } // конец метода loadKeywordOperand

    // вспомогательный метод для обработки операнда-порта parserOperand.
    // numOperand - номер операнда parserOperand в инструкции: 0 -единственный операнд, 1 -первый операнд, 2 -второй операнд
    private ResultForMethod loadPortOperand (ParserOperand parserOperand, int numOperand, int minValue, int maxValue){
        ResultForMethod res = new ResultForMethod();
        int numError;
        res.isError = false;
        if (! checkValueOperand(parserOperand)){
            switch (numOperand){
                case 1:
                    numError = ERROR_OPERAND_1_INVALID_VALUE; break;
                case 2:
                    numError = ERROR_OPERAND_2_NO_INT; break;
                default:
                    numError = ERROR_OPERAND_INVALID_VALUE;
            } // конец switch (numOperand)
            errors.add (numError, instruction.toString());
            res.isError = true;
            res.result = (short) -1;
            return res;
        } // конец if (! checkValueOperand(parserOperand))
        if (! loadNumberValueForOperand(parserOperand)){
            res.isError = true;
            res.result = (short) -1;
            return res;
        } // конец if (! loadNumberValueForOperand(parserOperand))
        if ((parserOperand.getNumberValue()<minValue)||(parserOperand.getNumberValue()>maxValue)){
            errors.add (ERROR_NUM_REGISTER_IO, instruction.toString());
            res.isError = true;
            res.result = (short) -1;
            return res;
        } else {
            res.result = parserOperand.getNumberValue();
        } // конец if ((parserOperand.getNumberValue()<minValue)||(parserOperand.getNumberValue()>maxValue))
        return res;
    } // конец метода loadKeywordOperand

    // обработка инструкций типа 1
    // instr Rd, Rr //первый операнд регистр r0-r31, второй операнд тоже регистр r0-r31
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |            КОП             | R |  D     D   D  D   D  |   R  R   R   R |
    private boolean instructionsType_1 (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand1, operand2;
        ResultForMethod resultForMethod;
        if (! checkNumOperands(operands, 2)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 2)
        resultForMethod = loadKeywordOperand (operands.get(0), 1, 0, 31);
        operand1 = resultForMethod.result;
        resultForMethod = loadKeywordOperand (operands.get(1), 2, 0, 31);
        if ((operand1<0)|| resultForMethod.isError){
            collecterCode.incCSEG();
            return res;
        } // конец if ((operand1<0)|| resultForMethod.isError)
        operand2 = resultForMethod.result;
        operand1 = operand1<<4;
        if (operand2>15){
            operand2 = 0x0200+(operand2 & 0x000F);
        } // конец if (operand2>15)
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand1 | operand2));
        return true;
    } // конец метода instructionsType_1

    // обработка инструкций типа 2
    // instr Rd, K // первый операнд - пара регистров (R24:R25, X (R26:R27), Y (R28:R29), Z (R30:R31)), второй операнд - число 0..63.
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |                   КОП                   |  K   K | D   D  |  K   K   K   K |
    private boolean instructionsType_2 (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand1, operand2;
        ResultForMethod resultForMethod = new ResultForMethod();
        if (! checkNumOperands(operands, 2)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 2)
        operand1 = getKeywordNum (operands.get(0));
        switch (operand1){
            case 24: // регистровая пара r24:r25
                operand1 = 0x0000;
                break;
            case 26: // регистровая пара r26:r27
            case 32: // регистр X
                operand1 = 0x0010;
                break;
            case 28: // регистровая пара r28:r29
            case 33: // регистр Y
                operand1 = 0x0020;
                break;
            case 30: // регистровая пара r30:r31
            case 34: // регистр Z
                operand1 = 0x0030;
                break;
            default:
                operand1 = -1;
        } // конец switch (operand1)
        if (operand1<0){
            errors.add (ERROR_OPERAND_1_NO_PARE_REGISTERS, instruction.toString());
        } // конец if (operand1<0)
        resultForMethod = loadIntOperand (operands.get(1), 2, 0, 63);
        if ((operand1<0)|| resultForMethod.isError){
            collecterCode.incCSEG();
            return res;
        } else {
            operand2 = resultForMethod.result;
        } // конец if (operand1<0)
        operand1 = operand1<<4;
        operand2 = ((operand2 & 0x00F0)<<2)+(operand2 & 0x000F);
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand1 | operand2));
        return true;
    } // конец метода instructionsType_2

    // обработка инструкций типа 3
    // instr K // операнд - число от -2048 до 2047 (расчет: К (адрес label) - текущий адрес PC -1)
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |       КОП      |   K   K   K   K     K   K   K   K     K   K   K   K |
    private boolean instructionsType_3 (ArrayList<ParserOperand> operands){
        boolean res = false;
        short operand;
        ResultForMethod resultForMethod;
        if (! checkNumOperands(operands, 1)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 1)
        resultForMethod = loadLabelOperand (operands.get(0), 0, -2048, 2047);
        if (resultForMethod.isError){
            collecterCode.incCSEG();
            return res;
        } // конец if (resultForMethod.isError
        operand = (short) (resultForMethod.result & 0x0FFF);
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand));
        return true;
    } // конец метода instructionsType_3

    // обработка инструкций типа 4
    // instr Rd // операнд - регистр r0-r31
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |                КОП               | D     D   D  D   D   |       КОП      |
    private boolean instructionsType_4 (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand;
        ResultForMethod resultForMethod;
        if (! checkNumOperands(operands, 1)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 1)
        resultForMethod = loadKeywordOperand (operands.get(0), 0, 0, 31);
        if (resultForMethod.isError){
            collecterCode.incCSEG();
            return res;
        } // конец if (resultForMethod.isError)
        operand = resultForMethod.result<<4;
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand));
        return true;
    } // конец метода instructionsType_4

    // вспомогательный метод для определения у инструкций LD и ST операнда X, Y, Z, -X, -Y, -Z, X+, Y+, Z+
    // возвращает опкод найденного операнда или -1, если ошибка
    private int getOperandForLdSt (ParserOperand parserOperand){
        int operand;
        operand = getKeywordNum (parserOperand);
        switch (operand){
            case 32: // регистр X
                operand = 0x100C; break;
            case 33: // регистр Y
                operand = 0x0008; break;
            case 34: // регистр Z
                operand = 0x0000; break;
            default:
                if (parserOperand.getTypeOperand()==ParserOperand.TYPE_OPERAND_POST_OR_PRED){
                    switch (parserOperand.getTypeRegister()){
                        case ParserOperand.TYPE_REGISTER_MINUS_X:
                            operand = 0x100E; break;
                        case ParserOperand.TYPE_REGISTER_MINUS_Y:
                            operand = 0x100A; break;
                        case ParserOperand.TYPE_REGISTER_MINUS_Z:
                            operand = 0x1002; break;
                        case ParserOperand.TYPE_REGISTER_X_PLUS:
                            operand = 0x100D; break;
                        case ParserOperand.TYPE_REGISTER_Y_PLUS:
                            operand = 0x1009; break;
                        case ParserOperand.TYPE_REGISTER_Z_PLUS:
                            operand = 0x1019; break;
                        default:
                            operand = -1;
                    } // конец switch (operands.get(1).getTypeRegister())
                } else {
                    operand = -1;
                } // конец if (parserOperand.getType==ParserOperand.TYPE_OPERAND_POST_OR_PRED)
        } // конец switch (operand)
        return operand;
    } // конец метода getOperandForLdSt

    // обработка инструкций типа 4_ld для LD
    // LD Rd, (-XYZ+) //первый операнд регистр r0-r31, второй операнд - одна из конструкций: X, Y, Z, -X, -Y, -Z, X+, Y+, Z+
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |                КОП               | D     D   D  D   D   |       КОП      |
    private boolean instructionsType_4_ld (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand1, operand2;
        ResultForMethod resultForMethod;
        if (! checkNumOperands(operands, 2)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 2)
        resultForMethod = loadKeywordOperand (operands.get(0), 1, 0, 31);
        operand1 = resultForMethod.result;
        operand2 = getOperandForLdSt (operands.get(1));
        if (operand2<0){
            errors.add (ERROR_OPERAND_2_INVALID_VALUE, instruction.toString());
        } // конец if (operand2<0)
        if ((operand1<0)||(operand2<0)){
            collecterCode.incCSEG();
            return res;
        } // конец if ((operand1<0)||(operand2<0))
        operand1 = operand1<<4;
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand1 | operand2));
        return true;
    } // конец метода instructionsType_4_ld

    // обработка инструкций типа 4_st для ST
    // ST (-XYZ+),Rd // первый операнд - одна из конструкций: X, Y, Z, -X, -Y, -Z, X+, Y+, Z+, второй операнд регистр r0-r31
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |                КОП               | D     D   D  D   D   |       КОП      |
    private boolean instructionsType_4_st (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand1, operand2;
        ResultForMethod resultForMethod;
        if (! checkNumOperands(operands, 2)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 2)
        operand1 = getOperandForLdSt (operands.get(0));
        if (operand1<0){
            errors.add (ERROR_OPERAND_1_INVALID_VALUE, instruction.toString());
        } // конец if (operand1<0)
        resultForMethod = loadKeywordOperand (operands.get(1), 2, 0, 31);
        operand2 = resultForMethod.result;
        if ((operand1<0)||(operand2<0)){
            collecterCode.incCSEG();
            return res;
        } // конец if ((operand1<0)||(operand2<0))
        operand2 = operand2<<4;
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand1 | operand2));
        return true;
    } // конец метода instructionsType_4_st

    // обработка инструкций типа 4_lpm для LPM и ELPM
    // instr Rd, Z(+) // первый операнд - регистр r0-r31, второй операнд Z или Z+
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |                КОП               | D     D   D  D   D   |       КОП      |
    private boolean instructionsType_4_lpm (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand1, operand2;
        ResultForMethod resultForMethod;
        if (! checkNumOperands(operands, 2)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 2)
        resultForMethod = loadKeywordOperand (operands.get(0), 1, 0, 31);
        operand1 = resultForMethod.result;
        operand2 = getKeywordNum (operands.get(1));
        if (operand2==regs.REG_Z){ // если второй операнд Z
            operand2 = (short) 0;
        } else {
            if ((operands.get(1).getTypeOperand()==ParserOperand.TYPE_OPERAND_POST_OR_PRED)&&(operands.get(1).getTypeRegister()==ParserOperand.TYPE_REGISTER_Z_PLUS)){ // если второй операнд Z+
                operand2 = 1;
            } else {
                operand2 = -1;
            } // конец if ((operands.get(1).getType()==ParserOperand.TYPE_OPERAND_POST_OR_PRED)&&(operands.get(1).getTypeRegister()==ParserOperand.TYPE_REGISTER_Z_PLUS))
        } // конец if (operand2==regs.REG_Z
        if (operand2<0){
            errors.add (ERROR_OPERAND_2_INVALID_VALUE, instruction.toString());
        } // конец if (operand2<0)
        if ((operand1<0)||(operand2<0)){
            collecterCode.incCSEG();
            return res;
        } // конец if ((operand1<0)||(operand2<0))
        operand1 = operand1<<4;
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand1 | operand2));
        return true;
    } // конец метода instructionsType_4_lpm

    // обработка инструкций типа 5
    // instr s // операнд s - число от 0 до 7 (номер бита в регистре статуса)
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |                       КОП                      | s   s   s   |       КОП       |
    private boolean instructionsType_5 (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand;
        ResultForMethod resultForMethod = new ResultForMethod();
        if (! checkNumOperands(operands, 1)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 1)
        resultForMethod = loadIntOperand (operands.get(0), 0, 0, 7);
        if (resultForMethod.isError){
            collecterCode.incCSEG();
            return res;
        } else {
            operand = resultForMethod.result <<4;
        } // конец if (resultForMethod.isError)
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand));
        return true;
    } // конец метода instructionsType_5

    // обработка инструкций типа 6
    // instr Rd, b // первый операнд - регистр r0-r31, второй операнд - число 0..7 (номер бита).
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |               КОП                | D      D   D  D   D |КОП|b   b   b |
    private boolean instructionsType_6 (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand1, operand2;
        ResultForMethod resultForMethod = new ResultForMethod();
        if (! checkNumOperands(operands, 2)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 2)
        resultForMethod = loadKeywordOperand (operands.get(0), 1, 0, 31);
        operand1 = resultForMethod.result;
        resultForMethod = loadIntOperand (operands.get(1), 2, 0, 7);
        if ((operand1<0)|| resultForMethod.isError){
            collecterCode.incCSEG();
            return res;
        } else {
            operand2 = resultForMethod.result;
        } // конец if ((operand1<0)|| resultForMethod.isError)
        operand1 = operand1<<4;
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand1 | operand2));
        return true;
    } // конец метода instructionsType_6

    // обработка инструкций типа 7
    // instr s, K // первый операнд - число от 0 до 7 (номер бита), второй операнд - число от -64 до +63 (метка)
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |            КОП             |  K   K      K   K   K   K     K | s   s   s |
    private boolean instructionsType_7 (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand1, operand2;
        ResultForMethod resultForMethod;
        if (! checkNumOperands(operands, 2)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 2)
        resultForMethod = loadIntOperand (operands.get(0), 1, 0, 7);
        if (resultForMethod.isError){
            operand1 = -1;
        } else {
            operand1 = resultForMethod.result;
        } // конец if (resultForMethod.isError)
        resultForMethod = loadLabelOperand (operands.get(1), 2, -64, 63);
        if ((operand1<0)|| resultForMethod.isError){
            collecterCode.incCSEG();
            return res;
        } // конец if ((operand1<0)|| resultForMethod.isError)
        operand2 = resultForMethod.result <<3;
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand1 | operand2));
        return true;
    } // конец метода instructionsType_7

    // обработка инструкций типа 8
    // instr // без операндов
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |                                           КОП                                           |
    private boolean instructionsType_8 (ArrayList<ParserOperand> operands){
        boolean res = false;
        if (! checkNumOperands(operands, 0)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 0)
        collecterCode.code.setWord ((short) instruction.getOpcode());
        return true;
    } // конец метода instructionsType_8

    // обработка инструкций типа 8 для spm с операндом
    // spm Z+ // допустим только операнд Z+
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |                                           КОП                                           |
    private boolean instructionsType_8_spm (ArrayList<ParserOperand> operands){
        boolean res = false;
        if (! checkNumOperands(operands, 1)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 1)
        if ((operands.get(0).getTypeOperand()!=ParserOperand.TYPE_OPERAND_POST_OR_PRED)||(operands.get(0).getTypeRegister() != ParserOperand.TYPE_REGISTER_Z_PLUS)){
            errors.add (ERROR_INVALID_OPERAND, instruction.toString());
            collecterCode.incCSEG();
            return res;
        } // конец if ((operands.get(0).getType()!=ParserOperand.TYPE_OPERAND_POST_OR_PRED)||(operands.get(0).getTypeRegister() != ParserOperand.TYPE_REGISTER_Z_PLUS))
        collecterCode.code.setWord ((short) (instruction.getOpcode()+0x0010)); // опкод 0x95F8
        return true;
    } // конец метода instructionsType_8_spm

    // обработка инструкций типа 9 для in
    // in Rd, P // первый операнд - регистр r0-r31, второй операнд - номер порта ввода-вывода
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |           КОП         | P   P | D     D   D   D   D  |  P   P   P   P |
    private boolean instructionsType_9_in (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand1, operand2;
        ResultForMethod resultForMethod = new ResultForMethod();
        if (! checkNumOperands(operands, 2)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 2)
        resultForMethod = loadKeywordOperand (operands.get(0), 1, 0, 31);
        operand1 = resultForMethod.result;
        resultForMethod = loadPortOperand (operands.get(1), 2, 0, 63);
        if ((operand1<0)|| resultForMethod.isError){
            collecterCode.incCSEG();
            return res;
        } else {
            operand2 = ((resultForMethod.result & 0x0030)<<5)+(resultForMethod.result & 0x000F);
        } // конец if ((operand1<0)|| resultForMethod.isError)
        operand1 = operand1<<4;
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand1 | operand2));
        return true;
    } // конец метода instructionsType_9_in

    // обработка инструкций типа 9 для out
    // out P, Rd // первый операнд - номер порта ввода-вывода, второй операнд - регистр r0-r31
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |           КОП         | P   P | D     D   D   D   D  |  P   P   P   P |
    private boolean instructionsType_9_out (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand1, operand2;
        ResultForMethod resultForMethod = new ResultForMethod();
        if (! checkNumOperands(operands, 2)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 2)
        resultForMethod = loadPortOperand (operands.get(0), 1, 0, 63);
        operand1 = resultForMethod.result;
        resultForMethod = loadKeywordOperand (operands.get(1), 2, 0, 31);
        operand2 = resultForMethod.result;
        if ((operand1<0)|| (operand2<0)){
            collecterCode.incCSEG();
            return res;
        } // конец if ((operand1<0)|| (operand2<0))
        operand1 = ((operand1 & 0x0030)<<5)+(operand1 & 0x000F);
        operand2 = operand2<<4;
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand1 | operand2));
        return true;
    } // конец метода instructionsType_9_out

    // обработка инструкций типа 10
    // instr Rd, Rr //первый операнд регистр r16-r23, второй операнд тоже регистр r16-r23
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |                       КОП                      | D  D   D |КОП|R   R   R |
    private boolean instructionsType_10 (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand1, operand2;
        ResultForMethod resultForMethod;
        if (! checkNumOperands(operands, 2)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 2)
        resultForMethod = loadKeywordOperand (operands.get(0), 1, 16, 23);
        operand1 = resultForMethod.result;
        resultForMethod = loadKeywordOperand (operands.get(1), 2, 16, 23);
        if ((operand1<0)|| resultForMethod.isError){
            collecterCode.incCSEG();
            return res;
        } // конец if ((operand1<0)|| resultForMethod.isError)
        operand2 = resultForMethod.result - 16;
        operand1 = (operand1-16)<<4;
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand1 | operand2));
        return true;
    } // конец метода instructionsType_10

    // обработка инструкций типа 12a
    // instr Rd, Rr //первый операнд пара регистров r0:r1, r2:r3,..., r30:r31 (включая X, Y, Z), второй операнд тоже пара регистров r0:r1, r2:r3,..., r30:r31 (включая X, Y, Z)
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |                   КОП                   |  D   D   D  D  |   R  R   R   R |
    private boolean instructionsType_12a (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand1, operand2;
        ResultForMethod resultForMethod;
        if (! checkNumOperands(operands, 2)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 2)
        resultForMethod = loadKeywordOperand (operands.get(0), 1, 0, 34);
        if ((resultForMethod.result>=0)&&(resultForMethod.result<=31)){
            if ((resultForMethod.result & 0x00FE) != resultForMethod.result){
                errors.add (ERROR_OPERAND_1_NO_PARE_REGISTERS, instruction.toString());
                operand1 = -1;
            } else {
                operand1 = resultForMethod.result;
            } // конец if ((resultForMethod.result & 0x00FE) != resultForMethod.result)
        } else {
            switch (resultForMethod.result){
                case 32: // регистр X
                    operand1 = 26; break;
                case 33: // регистр Y
                    operand1 = 28; break;
                case 34: // регистр Z
                    operand1 = 30; break;
                default:
                    operand1 = -1;
            } // конец switch (resultForMethod.result)
        } // конец if ((resultForMethod.result>=0)&&(resultForMethod.result<=31))
        resultForMethod = loadKeywordOperand (operands.get(1), 2, 0, 34);
        if ((resultForMethod.result>=0)&&(resultForMethod.result<=31)){
            if ((resultForMethod.result & 0x00FE) != resultForMethod.result){
                errors.add (ERROR_OPERAND_2_NO_PARE_REGISTERS, instruction.toString());
                operand2 = -1;
            } else {
                operand2 = resultForMethod.result;
            } // конец if ((resultForMethod.result & 0x00FE) != resultForMethod.result)
        } else {
            switch (resultForMethod.result){
                case 32: // регистр X
                    operand2 = 26; break;
                case 33: // регистр Y
                    operand2 = 28; break;
                case 34: // регистр Z
                    operand2 = 30; break;
                default:
                    operand2 = -1;
            } // конец switch (resultForMethod.result)
        } // конец if ((resultForMethod.result>=0)&&(resultForMethod.result<=31))
        if ((operand1<0)||(operand2<0)){
            collecterCode.incCSEG();
            return res;
        } // конец if ((operand1<0)|| resultForMethod.isError)
        operand1 = operand1<<3; // регистровые пары нумеруются по порядку: r0 -0, r2 -1,..., r30 -15
        operand2 = operand2>>1; // также
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand1 | operand2));
        return true;
    } // конец метода instructionsType_12a

    // обработка инструкций типа 12b
    // instr Rd, Rr //первый операнд регистр r16-r31, второй операнд тоже регистр r16-r31
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |                   КОП                   |  D   D   D  D  |   R  R   R   R |
    private boolean instructionsType_12b (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand1, operand2;
        ResultForMethod resultForMethod;
        if (! checkNumOperands(operands, 2)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 2)
        resultForMethod = loadKeywordOperand (operands.get(0), 1, 16, 31);
        operand1 = resultForMethod.result;
        resultForMethod = loadKeywordOperand (operands.get(1), 2, 16, 31);
        if ((operand1<0)|| resultForMethod.isError){
            collecterCode.incCSEG();
            return res;
        } // конец if ((operand1<0)|| resultForMethod.isError)
        operand2 = (short) (resultForMethod.result - 16);
        operand1 = (short) ((operand1-16)<<4);
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand1 | operand2));
        return true;
    } // конец метода instructionsType_12b

    // обработка инструкций типа 13
    // instr Rd, K // первый операнд - регистр r16-r31, второй операнд - число -128..255 (0x00...0xFF).
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |       КОП      |   K   K  K   K   |  D   D  D   D  |  K   K   K   K |
    private boolean instructionsType_13 (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand1, operand2;
        ResultForMethod resultForMethod = new ResultForMethod();
        if (! checkNumOperands(operands, 2)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 2)
        resultForMethod = loadKeywordOperand (operands.get(0), 1, 16, 31);
        operand1 = resultForMethod.result;
        resultForMethod = loadIntOperand (operands.get(1), 2, -128, 255);
        if ((operand1<0)|| resultForMethod.isError){
            collecterCode.incCSEG();
            return res;
        } else {
            operand2 = resultForMethod.result;
        } // конец if ((operand1<0)|| resultForMethod.isError)
        operand1 = ((operand1-16)<<4);
        operand2 = ((operand2 & 0x00F0)<<4 + (operand2 & 0x000F));
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand1 | operand2));
        return true;
    } // конец метода instructionsType_13

    // обработка инструкций типа 15
    // instr P, b // первый операнд - номер регистра в/в (допустимы 0-31), второй операнд - число 0..7 (номер бита).
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |                  КОП                    |  P   P   P   P      P |  b   b   b |
    private boolean instructionsType_15 (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand1, operand2;
        ResultForMethod resultForMethod = new ResultForMethod();
        if (! checkNumOperands(operands, 2)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 2)
        resultForMethod = loadPortOperand (operands.get(0), 1, 0, 31);
        operand1 = resultForMethod.result;
        resultForMethod = loadIntOperand (operands.get(1), 2, 0, 7);
        if ((operand1<0)|| resultForMethod.isError){
            collecterCode.incCSEG();
            return res;
        } else {
            operand2 = resultForMethod.result;
        } // конец if ((operand1<0)|| resultForMethod.isError)
        operand1 = operand1<<3;
        collecterCode.code.setWord ((short)(instruction.getOpcode() | operand1 | operand2));
        return true;
    } // конец метода instructionsType_15

    // обработка инструкций типа 16
    // LD Rd, Z(Y) +q //первый операнд регистр r0-r31, второй операнд - одна из конструкций: Z+q, Y+q (q - смещение, число 0..63)
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // | коп  | q |коп|  q   q|коп|D     D   D   D  D  |коп| q   q   q |
    private boolean instructionsType_16 (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand1, operand2;
        ResultForMethod resultForMethod;
        int opcode = instruction.getOpcode();
        if (! checkNumOperands(operands, 2)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 2)
        resultForMethod = loadKeywordOperand (operands.get(0), 1, 0, 31);
        operand1 = resultForMethod.result;
        if ((! operands.get(1).getFlagAssignment())&&(operands.get(1).getTypeOperand()==ParserOperand.TYPE_OPERAND_POST_OR_PRED)){
            switch (operands.get(1).getTypeRegister()){
                case ParserOperand.TYPE_REGISTER_Y_PLUS:
                case ParserOperand.TYPE_REGISTER_Y_MINUS:
                    opcode = opcode+8; break;
                case ParserOperand.TYPE_REGISTER_Z_PLUS:
                case ParserOperand.TYPE_REGISTER_Z_MINUS:
                    break;
                default:
                    errors.add (ERROR_OPERAND_2_INVALID_VALUE, instruction.toString());
                    collecterCode.incCSEG();
                    return res;
            } // конец switch (operands.get(1).getTypeRegister())
            if (! loadNumberValueForOperand(operands.get(1))){
                collecterCode.incCSEG();
                return res;
            } // конец if (! loadNumberValueForOperand(parserOperand))
            if ((operands.get(1).getNumberValue()<0)||(operands.get(1).getNumberValue()>63)){
                errors.add (ERROR_BIG_OR_SMALL_OPERAND_2, instruction.toString());
                collecterCode.incCSEG();
                return res;
            } else {
                operand2 = operands.get(1).getNumberValue();
            } // конец if ((parserOperand.getNumberValue()<minValue)||(parserOperand.getNumberValue()>maxValue))
        } else {
            errors.add (ERROR_OPERAND_2_INVALID_VALUE, instruction.toString());
            operand2 = -1;
        } // конец if (operands.get(1).getType()==ParserOperand.TYPE_OPERAND_POST_OR_PRED){
        if ((operand1<0)||(operand2<0)){
            collecterCode.incCSEG();
            return res;
        } // конец if ((operand1<0)||(operand2<0))
        operand1 = operand1<<4;
        operand2 = (operand2 & 0x0020)<<8 + (operand2 & 0x0018)<<7 + (operand2 & 0x0007);
        collecterCode.code.setWord ((short) (opcode | operand1 | operand2));
        return true;
    } // конец метода instructionsType_16

    // обработка инструкций типа 11 (4 байта)
    // instr K // операнд - число от 0 до 3FFFFF (4 194 303) (абсолютный адрес, который загружается в регистр РС)
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00    15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |                КОП               | K      K   K   K  K   |     КОП   | K |   | K   K  K   K      K   K  K   K     K   K  K  K        K   K  K   K |
    private boolean instructionsType_11 (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand;
        ResultForMethod resultForMethod;
        if (! checkNumOperands(operands, 1)){
            collecterCode.incCSEG();
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 1)
        resultForMethod = loadLabelOperand (operands.get(0), 0, 0, 0x3FFFFF);
        if (resultForMethod.isError){
            collecterCode.incCSEG();
            collecterCode.incCSEG();
            return res;
        } // конец if (resultForMethod.isError)
        operand = resultForMethod.result;
        collecterCode.code.setWord ((short) (instruction.getOpcode() | ((operand>>17)<<4) | ((operand>>16)&0x00000001)));
        collecterCode.code.setWord ((short) (operand & 0x0000FFFF));
        return true;
    } // конец метода instructionsType_11

    // обработка инструкций типа 14 (4 байта)
    // instr Rd, K // первый операнд - регистр r0-r31, второй операнд - число от 0 до FFFF () (абсолютный адрес в SRAM, из которого происходит считывание или запись)
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00    15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |                КОП               | D      D   D   D  D   |       КОП      |   | K   K  K   K      K   K  K   K     K   K  K  K        K   K  K   K |
    private boolean instructionsType_14 (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand1, operand2;
        ResultForMethod resultForMethod;
        if (! checkNumOperands(operands, 2)){
            collecterCode.incCSEG();
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 2)
        resultForMethod = loadKeywordOperand (operands.get(0), 1, 0, 31);
        operand1 = resultForMethod.result;
        resultForMethod = loadIntOperand (operands.get(1), 2, 0, 0xFFFF);
        if ((operand1<0) || resultForMethod.isError){
            collecterCode.incCSEG();
            collecterCode.incCSEG();
            return res;
        } // конец if (resultForMethod.isError)
        operand1 = operand1<<4;
        operand2 = resultForMethod.result;
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand1));
        collecterCode.code.setWord ((short) operand2);
        return true;
    } // конец метода instructionsType_14

    // обработка мнемонических вариантов инструкции BRBC
    // instr K // операнд - число от -64 до +63 (метка)
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |            КОП             |  K   K      K   K   K   K     K |   КОП   |
    private boolean instructionsType_BRBC_BRBS (ArrayList<ParserOperand> operands){
        boolean res = false;
        short operand;
        ResultForMethod resultForMethod;
        if (! checkNumOperands(operands, 1)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 1)
        resultForMethod = loadLabelOperand (operands.get(0), 0, -64, 63);
        if (resultForMethod.isError){
            collecterCode.incCSEG();
            return res;
        } // конец if (resultForMethod.isError)
        operand = (short) (resultForMethod.result <<3);
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand));
        return true;
    } // конец метода instructionsType_BRBC

    // обработка мнемонических вариантов инструкций типа 1
    // instr Rd  //операнд - регистр r0-r31 (вариант инструкций типа 1: instr Rd, Rr   , где Rr=Rd)
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |            КОП             | R |  D     D   D  D   D  |   R  R   R   R |  (R = D)
    private boolean instructionsType_1_subtype (ArrayList<ParserOperand> operands){
        boolean res = false;
        short operand1, operand2;
        ResultForMethod resultForMethod;
        if (! checkNumOperands(operands, 1)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 1)
        resultForMethod = loadKeywordOperand (operands.get(0), 1, 0, 31);
        if (resultForMethod.isError){
            collecterCode.incCSEG();
            return res;
        } // конец if ( resultForMethod.isError)
        operand1 = (short) (resultForMethod.result << 4);
        operand2 = (short) resultForMethod.result;
        if (operand2>15){
            operand2 = (short) (0x0200+(operand2 & 0x000F));
        } // конец if (operand2>15)
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand1 | operand2));
        return true;
    } // конец метода instructionsType_1_subtype

    // обработка инструкции CBR (по типу 13)
    // CBR Rd, K8 // первый операнд - регистр r16-r31, второй операнд - число -128..255 (0x00...0xFF). Аналог инструкции ANDI Rd, (0xFF - K8)
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |       КОП      |   K   K  K   K   |  D   D  D   D  |  K   K   K   K |
    private boolean instructionsType_13_subtype_CBR (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand1, operand2;
        ResultForMethod resultForMethod = new ResultForMethod();
        if (! checkNumOperands(operands, 2)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 2)
        resultForMethod = loadKeywordOperand (operands.get(0), 1, 16, 31);
        operand1 = resultForMethod.result;
        resultForMethod = loadIntOperand (operands.get(1), 2, -128, 255);
        if ((operand1<0)|| resultForMethod.isError){
            collecterCode.incCSEG();
            return res;
        } else {
            operand2 = 0xFF - resultForMethod.result;
        } // конец if ((operand1<0)|| resultForMethod.isError)
        operand1 = ((operand1-16)<<4);
        operand2 = ((operand2 & 0x00F0)<<4 + (operand2 & 0x000F));
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand1 | operand2));
        return true;
    } // конец метода instructionsType_13_subtype_CBR

    // обработка инструкции SER (по типу 13)
    // SER Rd // операнд - регистр r16-r31. Аналог инструкции LDI Rd, K  , где K = 0xFF
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |       КОП      |   K   K  K   K   |  D   D  D   D  |  K   K   K   K |
    private boolean instructionsType_13_subtype_SER (ArrayList<ParserOperand> operands){
        boolean res = false;
        int operand1, operand2;
        ResultForMethod resultForMethod = new ResultForMethod();
        if (! checkNumOperands(operands, 1)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 1)
        resultForMethod = loadKeywordOperand (operands.get(0), 0, 16, 31);
        operand1 = resultForMethod.result;
        if (resultForMethod.isError){
            collecterCode.incCSEG();
            return res;
        } // конец if ((operand1<0)|| resultForMethod.isError)
        operand1 = ((operand1-16)<<4);
        operand2 = 0x0F0F;
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand1 | operand2));
        return true;
    } // конец метода instructionsType_13_subtype_SER

    // обработка инструкции DES
    // instr K // операнд K - число от 0 до 15
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |                       КОП               |  K   K   K   K  |       КОП       |
    private boolean instructionsType_DES (ArrayList<ParserOperand> operands){
        boolean res = false;
        short operand;
        ResultForMethod resultForMethod = new ResultForMethod();
        if (! checkNumOperands(operands, 1)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 1)
        resultForMethod = loadIntOperand (operands.get(0), 0, 0, 15);
        if (resultForMethod.isError){
            collecterCode.incCSEG();
            return res;
        } else {
            operand = (short) (resultForMethod.result <<4);
        } // конец if (resultForMethod.isError)
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand));
        return true;
    } // конец метода instructionsType_DES

    // обработка инструкций XCH, LAS, LAC, LAT
    // instr Z, Rd // первый операнд - регистр Z, второй операнд - регистр r0-r31
    // 15 14 13 12 * 11 10 09 08 * 07 06 05 04 * 03 02 01 00
    // |                КОП               | D     D   D  D   D   |       КОП      |
    private boolean instructionsType_XCH (ArrayList<ParserOperand> operands){
        boolean res = false;
        short operand1, operand2;
        ResultForMethod resultForMethod;
        if (! checkNumOperands(operands, 2)){
            collecterCode.incCSEG();
            return res;
        } // конец if (operands.size() != 2)
        operand1 = getKeywordNum (operands.get(0));
        if (operand1 != regs.REG_Z){ // если первый операнд не Z
            errors.add (ERROR_OPERAND_1_INVALID_VALUE, instruction.toString());
            operand1 = (short) -1;
        } // конец if (operand1 !=regs.REG_Z
        resultForMethod = loadKeywordOperand (operands.get(1), 2, 0, 31);
        operand2 = (short) resultForMethod.result;
        if ((operand1<0)||(operand2<0)){
            collecterCode.incCSEG();
            return res;
        } // конец if ((operand1<0)||(operand2<0))
        operand2 = (short) (operand2<<4);
        collecterCode.code.setWord ((short) (instruction.getOpcode() | operand2));
        return true;
    } // конец метода instructionsType_XCH

    public String getErrorText (int numError){
        String res = "";
        switch (MainActivity.language){
            case 1: // русский
                switch (numError){
                    case ERROR_NO_2_OPERANDS:
                        res = "Инструкция использует 2 операнда";
                        break;
                    case ERROR_OPERAND_NO_REGISTER:
                        res = "Недопустимое выражение, ожидается регистр";
                        break;
                    case ERROR_OPERAND_1_NO_REGISTER:
                        res = "Недопустимое выражение в первом операнде инструкции, ожидается регистр";
                        break;
                    case ERROR_OPERAND_2_NO_REGISTER:
                        res = "Недопустимое выражение во втором операнде инструкции, ожидается регистр";
                        break;
                    case ERROR_OPERAND_1_NO_PARE_REGISTERS:
                        res = "Недопустимое выражение в первом операнде инструкции, ожидается регистровая пара";
                        break;
                    case ERROR_OPERAND_2_NO_INT:
                        res = "Недопустимое выражение во втором операнде инструкции, ожидается числовое значение";
                        break;
                    case ERROR_OPERAND_NO_INT:
                        res = "Недопустимое выражение в операнде инструкции, ожидается числовое значение";
                        break;
                    case ERROR_OPERAND_DEF_MUST_INT:
                        res = "Недопустимое выражение - обнаружено наименование регистра, но ожидается числовое значение операнда инструкции";
                        break;
                    case ERROR_OPERAND_UNKNOWN_USERWORD:
                        res = "Недопустимое выражение, обнаружено не определенное ранее пользовательское слово";
                        break;
                    case ERROR_DIV_ZERO:
                        res = "Недопустимое выражение, деление на ноль";
                        break;
                    case ERROR_FUNC_BIG_OPERAND_EXP2:
                        res = "Значение операнда функции EXP2 превышает максимально допустимое значение";
                        break;
                    case ERROR_FUNC_SMALL_OPERAND_EXP2:
                        res = "Значение операнда функции EXP2 не может быть меньше нуля";
                        break;
                    case ERROR_FUNC_SMALL_OPERAND_LOG2:
                        res = "Значение операнда функции LOG2 не может быть меньше или равно нулю";
                        break;
                    case ERROR_FUNC_UNKNOWN_ERROR:
                        res = "Неизвестная функция";
                        break;
                    case ERROR_UNKNOWN:
                        res = "Неизвестная ошибка при вычислении операций";
                        break;
                    //case ERROR_OPERAND_2_NO_INT:
                    //    res = "Недопустимое выражение во втором операнде, ожидается числовое значение";
                    //    break;
                    case ERROR_BIG_OR_SMALL_OPERAND_2:
                        res = "Числовое значение второго операнда вне допустимого диапазона";
                        break;
                    case ERROR_NO_1_OPERAND:
                        res = "Для инструкции необходим один операнд";
                        break;
                    case ERROR_OPERAND_INVALID_VALUE:
                        res = "Недопустимое выражение в операнде, ожидается числовое значение";
                        break;
                    case ERROR_LABEL_OUT_RANGE:
                        res = "Метка находится вне допустимого для инструкции диапазона";
                        break;
                    case ERROR_OPERAND_2_INVALID_VALUE:
                        res = "Недопустимое выражение во втором операнде инструкции";
                        break;
                    case ERROR_OPERAND_1_INVALID_VALUE:
                        res = "Недопустимое выражение в первом операнде инструкции";
                        break;
                    case ERROR_INT_OUT_RANGE:
                        res = "Числовое значение операнда за пределами допустимого диапазона";
                        break;
                    case ERROR_INVALID_OPERAND:
                        res = "Недопустимое выражение в операнде";
                        break;
                    case ERROR_BIG_OR_SMALL_OPERAND_1:
                        res = "Числовое значение первого операнда вне допустимого диапазона";
                        break;
                    case ERROR_IS_OPERANDS:
                        res = "Инструкция используется без операндов";
                        break;
                    case ERROR_NUM_REGISTER_IO:
                        res = "Недопустимый номер регистра ввода-вывода";
                        break;
                    case ERROR_OPERAND_2_NO_PARE_REGISTERS:
                        res = "Недопустимое выражение во втором операнде инструкции, ожидается регистровая пара";
                        break;
                    case ERROR_UNKNOWN_INSTRUCTION:
                        res = "Неизвестная инструкция";
                        break;
                } // конец switch (numError)
                break;
            case 2: // английский
                switch (numError){
                    case ERROR_NO_2_OPERANDS:
                        res = "Instruction uses 2 operands";
                        break;
                    case ERROR_OPERAND_NO_REGISTER:
                        res = "Invalid expression, case expected";
                        break;
                    case ERROR_OPERAND_1_NO_REGISTER:
                        res = "Invalid expression in first operand of instruction, expected register";
                        break;
                    case ERROR_OPERAND_2_NO_REGISTER:
                        res = "Invalid expression in second operand of instruction, expected register";
                        break;
                    case ERROR_OPERAND_1_NO_PARE_REGISTERS:
                        res = "Invalid expression in first operand of instruction, register pair expected";
                        break;
                    //case ERROR_OPERAND_2_NO_INT:
                    //    res = "Недопустимое выражение во втором операнде инструкции, ожидается числовое значение";
                    //    break;
                    case ERROR_OPERAND_NO_INT:
                        res = "Invalid expression in operand of instruction, expected numeric value";
                        break;
                    case ERROR_OPERAND_DEF_MUST_INT:
                        res = "Invalid expression - register name found, but numeric value of instruction operand expected";
                        break;
                    case ERROR_OPERAND_UNKNOWN_USERWORD:
                        res = "Invalid expression, previously undefined user word found";
                        break;
                    case ERROR_DIV_ZERO:
                        res = "Invalid expression, division by zero";
                        break;
                    case ERROR_FUNC_BIG_OPERAND_EXP2:
                        res = "The operand value of function EXP2 exceeds the maximum value";
                        break;
                    case ERROR_FUNC_SMALL_OPERAND_EXP2:
                        res = "The operand value of function EXP2 cannot be less than zero";
                        break;
                    case ERROR_FUNC_SMALL_OPERAND_LOG2:
                        res = "The operand value of the function LOG2 cannot be less than or equal to zero";
                        break;
                    case ERROR_FUNC_UNKNOWN_ERROR:
                        res = "Unknown function";
                        break;
                    case ERROR_UNKNOWN:
                        res = "Unknown error while calculating operations";
                        break;
                    case ERROR_OPERAND_2_NO_INT:
                        res = "Invalid expression in second operand, numeric value expected";
                        break;
                    case ERROR_BIG_OR_SMALL_OPERAND_2:
                        res = "The numeric value of the second operand is out of range";
                        break;
                    case ERROR_NO_1_OPERAND:
                        res = "An instruction needs one operand";
                        break;
                    case ERROR_OPERAND_INVALID_VALUE:
                        res = "Invalid expression in operand, numeric value expected";
                        break;
                    case ERROR_LABEL_OUT_RANGE:
                        res = "Label is out of range";
                        break;
                    case ERROR_OPERAND_2_INVALID_VALUE:
                        res = "Invalid expression in second operand of instruction";
                        break;
                    case ERROR_OPERAND_1_INVALID_VALUE:
                        res = "Invalid expression in first operand of instruction";
                        break;
                    case ERROR_INT_OUT_RANGE:
                        res = "The numeric value of the operand is out of range";
                        break;
                    case ERROR_INVALID_OPERAND:
                        res = "Invalid expression in operand";
                        break;
                    case ERROR_BIG_OR_SMALL_OPERAND_1:
                        res = "The numeric value of the first operand is out of range";
                        break;
                    case ERROR_IS_OPERANDS:
                        res = "The instruction is used without operands";
                        break;
                    case ERROR_NUM_REGISTER_IO:
                        res = "Invalid I/O Register Number";
                        break;
                    case ERROR_OPERAND_2_NO_PARE_REGISTERS:
                        res = "Invalid expression in second operand of instruction, register pair expected";
                        break;
                    case ERROR_UNKNOWN_INSTRUCTION:
                        res = "Unknown instruction";
                        break;
                } // конец switch (numError)
        } // конец switch (MainActivity.language)
        return res;
    } // конец метода getErrorText

} // конец класса ProcessorInstructions
