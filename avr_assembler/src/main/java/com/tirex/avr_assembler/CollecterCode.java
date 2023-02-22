package com.tirex.avr_assembler;

import java.util.ArrayList;

public class CollecterCode{

    // используются для указания текущего сегмента в currentSeg
    public static final int SEGMENT_CODE = 1; // сегмент cseg
    public static final int SEGMENT_DATA = 2; // сегмент dseg
    public static final int SEGMENT_EEPROM = 4; // сегмент eseg

    private int cseg; // счетчик cseg, содержит текущее положение в сегменте cseg (также содержит текущее значение РС - program countet)
    private int dseg = 32; // счетчик dseg, содержит текущее положение в сегменте dseg
    private int eseg; // счетчик eseg, содержит текущее положение в сегменте eseg
    private int currentSeg = SEGMENT_CODE; // указывает, какой сегмент используется в данный момент (по умолчанию cseg)
    // используемые в программе переменные:
    public Variables vars_def = new Variables(); // список переменных, объявленных через директиву def
    public Variables vars_equ = new Variables(); // список переменных, объявленных через директиву equ
    public Variables vars_set = new Variables(); // список переменных, объявленных через директиву set
    // используемые в программе метки
    public Variables labels = new Variables();
    // глубина стека
    private int stackDepth;
    // максимальная глубина стека
    private int maxStackDepth;
    // формируемый машинный код
    public MachineCode code = new MachineCode();
    // содержит список ошибок
    public ArrayList<CompilatorError> errors = new ArrayList();
    // содержимое eeprom
    public FlashEEPROM flashEeprom = new FlashEEPROM();
    // список отложенных ошибок, создается на случай, когда ссылка на метку встречается в листинге раньше, чем объявление этой метки. Сохраненные строки парсера перепроверяются после обработки всего листинга
    public ArrayList<DeferredError> deferredErrors = new ArrayList();
    private Avr_device device;

    public class MachineCode{
        private ArrayList<Short> words = new ArrayList(); // список двухбайтовых слов - машинный код
        private int counter = 0; // счетчик, указывает номер позиции в списке words, в которую нужно записать следующий байт кода (соотносится с cseg)
        public void setCounter (int valueCounter){
            counter = valueCounter;
        } // конец метода setCounter
        public void addWord (short word){
            setWord (word);
        } // конец метода addWord
        public void setWord (short word){
            if (counter>=words.size()){
                for (int i=words.size(); i<counter; i++){
                    words.add((short)0);
                }
                words.add (word);
            } else {
                words.set(counter, word);
            }
            counter++;
        } // конец метода setWord
        public void setHighByte (byte highByte){
            for (int i=words.size(); i<=counter; i++){
                words.add((short) 0);
            }
            words.set(counter, (short) (words.get(counter) & 255));
            words.set(counter, (short) (words.get(counter) | (highByte<<8)));
        } // конец метода setHighByte
        public void setLowByte (byte lowByte){
            for (int i=words.size(); i<=counter; i++){
                words.add((short) 0);
            }
            words.set(counter, (short) (words.get(counter) & (255<<8)));
            words.set(counter, (short) (words.get(counter) | (lowByte)));
        } // конец метода setLowByte
        public int countWords (){
            return words.size();
        } // конец метода countWords
        public short getWord (int num){
            short res = 0;
            if (num<words.size()){
                res = words.get(num);
            }
            return res;
        } // конец метода getWord
    } // конец класса MachineCode

    // класс содержит текст ошибки и номер строки с этой ошибкой
    public class CompilatorError{
        private int numLine; // номер строки с ошибкой
        private String textError; // полный текст ошибки
        public CompilatorError (String textError, int numLine){
            this.textError = textError;
            this.numLine = numLine;
        }
        public int getNumLine (){
            return numLine;
        }
        public String getTextError (){
            return textError;
        }
    } // конец класса CompilatorError

    public class FlashEEPROM{
        private ArrayList<Byte> bytes = new ArrayList();
        private int counter = 0; // счетчик, указывает номер позиции в списке bytes, в которую нужно записать следующий байт (соотносится с eseg)
        public void setCounter (int valueCounter){
            counter = valueCounter;
        } // конец метода setCounter
        public void setByte (byte byteFlash){
            if (counter>=bytes.size()){
                for (int i=bytes.size(); i<counter; i++){
                    bytes.add((byte) 0);
                }
                bytes.add (byteFlash);
            } else {
                bytes.set(counter, byteFlash);
            }
        } // конец метода setByte
        public int countBytes (){
            return bytes.size();
        } // конец метода countBytes
        public byte getByte (int num){
            byte res = 0;
            if (num<bytes.size()){
                res = bytes.get(num);
            }
            return res;
        } // конец метода getByte
    } // конец класса FlashEEPROM

    public class StateCollecter{
        private int cseg;
        private int dseg;
        private int eseg;
        private int currentSeg;
        private int numError;
        private Variables vars_def = new Variables();
        private Variables vars_equ = new Variables();
        private Variables vars_set = new Variables();
        public StateCollecter (CollecterCode collecter){
            this.cseg = collecter.getCSEG();
            dseg = collecter.getDSEG();
            eseg = collecter.getESEG();
            currentSeg = collecter.getCurrentSeg();
            numError = errors.size();
            vars_def = collecter.vars_def.createCopy();
            vars_equ = collecter.vars_equ.createCopy();
            vars_set = collecter.vars_set.createCopy();
        }
        public int getCseg (){
            return this.cseg;
        }
        public int getDseg (){
            return dseg;
        }
        public int getEseg (){
            return eseg;
        }
        public int getCurrentSeg (){
            return currentSeg;
        }
        public int getNumError(){
            return numError;
        }
        public Variables getVarsDef (){
            return vars_def;
        }
        public Variables getVarsEqu (){
            return vars_equ;
        }
        public Variables getVarsSet (){
            return vars_set;
        }
    } // конец класса StateCollecter

    public class DeferredError {
        private Parser.ParserString parserString;
        private int numStr; // номер строки в листинге
        private StateCollecter state;
        public DeferredError (Parser.ParserString parserString, int numStr, CollecterCode collecterCode){
            this.parserString = parserString;
            this.numStr = numStr;
            this.state = collecterCode.getState();
        }
        public void setNumStr (int numStr){
            this.numStr = numStr;
        }
        public int getNumStr (){
            return numStr;
        }
        public Parser.ParserString getParserString (){
            return parserString;
        }
        public StateCollecter getState(){
            return state;
        }
    } // конец класса DeferredError

    // метод устанавливает значение счетчика cseg и счетчика counter машинного кода code
    public void setCSEG (int valueCSEG){
        if (valueCSEG>=0){
            cseg = valueCSEG;
            code.setCounter (cseg);
        }
    } // конец метода setCSEG

    // инкремент cseg
    public void incCSEG (){
        setCSEG (cseg++);
    } // конец метода incCSEG

    // декремент cseg
    public void decCSEG (){
        setCSEG (cseg--);
    } // конец метода decCSEG

    // метод возвращает значение cseg
    public int getCSEG (){
        return cseg;
    } // конец метода getCSEG

    // метод устанавливает значение счетчика dseg
    public void setDSEG (int valueDSEG){
        if (valueDSEG>=0){
            dseg = valueDSEG;
        }
    } // конец метода setDSEG

    // инкремент dseg
    public void incDSEG (){
        setDSEG (dseg++);
    } // конец метода incDSEG

    // декремент dseg
    public void decDSEG (){
        setDSEG (dseg--);
    } // конец метода decDSEG

    // метод возвращает значение dseg
    public int getDSEG (){
        return dseg;
    } // конец метода getDSEG

    // метод устанавливает значение счетчика eseg
    public void setESEG (int valueESEG){
        if (valueESEG>=0){
            eseg = valueESEG;
            this.flashEeprom.setCounter (eseg);
        }
    } // конец метода setESEG

    // инкремент eseg
    public void incESEG (){
        setESEG (eseg++);
    } // конец метода incESEG

    // декремент eseg
    public void decESEG (){
        setESEG (eseg--);
    } // конец метода decESEG

    // метод возвращает значение eseg
    public int getESEG (){
        return eseg;
    } // конец метода getESEG

    // метод для установки текущего сегмента
    public void setCurrentSeg (int segment){
        if ((segment>=1)&&(segment<=3)){
            currentSeg = segment;
        }
    } // конец метода setCurrentSeg

    // метод возвращает текущий активный сегмент
    public int getCurrentSeg (){
        return currentSeg;
    } // конец метода getCurrentSeg

    // метод устанавливает значение текущего сегмента
    public void setValueCurrentSeg (int valueSeg){
        switch (currentSeg){
            case SEGMENT_CODE:
                this.setCSEG (valueSeg);
                break;
            case SEGMENT_DATA:
                this.setDSEG (valueSeg);
                break;
            case SEGMENT_EEPROM:
                setESEG (valueSeg);
        } // конец switch (currentSeg)
    } // конец метода setValueCurrentSeg

    // метод увеличивает на 1 значение глубины стека
    public void incStackDepth(){
        stackDepth++;
        if (stackDepth>maxStackDepth){
            maxStackDepth = stackDepth;
        } // конец if (stackDepth>maxStackDepth)
    } // конец метода incStackDepth

    // метод уменьшает на 1 значение глубины стека. Если результат меньше нуля, то возвращает false
    public boolean decStackDepth (){
        boolean res = true;
        stackDepth--;
        if (stackDepth<0){
            res = false;
        }
        return res;
    } // конец метода decStackDepth

    // метод увеличивает значение глубины стека в зависимости от разрядности счетчика команд на 2, 3 или 4 байта.
    // вызывается при инструкциях call, rcall, ecall
    public void incStackDepthFromCall(){
        if ((device==null)||(device.getLengthPC()==0)){
            stackDepth = stackDepth + 2;
        } else {
            stackDepth = stackDepth + device.getLengthPC();
        } // конец if ((device==null)||(device.getLengthPC()==0))
        if (stackDepth>maxStackDepth){
            maxStackDepth = stackDepth;
        } // конец if (stackDepth>maxStackDepth)
    } // конец метода incStackDepthFromCall

    // метод уменьшает значение глубины стека в зависимости от разрядности счетчика команд на 2, 3 или 4 байта. Если результат меньше нуля, то возвращает false.
    // вызывается при инструкциях ret, reti
    public boolean decStackDepthFromRet (){
        boolean res = true;
        if ((device==null)||(device.getLengthPC()==0)){
            stackDepth = stackDepth - 2;
        } else {
            stackDepth = stackDepth - device.getLengthPC();
        } // конец if ((device==null)||(device.getLengthPC()==0))
        if (stackDepth<0){
            res = false;
        }
        return res;
    } // конец метода decStackDepthFromCall

    // метод возвращает максимальную глубину стека
    public int getMaxStackDepth(){
        return maxStackDepth;
    } // конец метода getMaxStackDepth

    // метод добавляет метку в список
    public boolean addLabelDeclaration (String labelName){
        int valueSeg;
        switch (currentSeg){
            case SEGMENT_DATA:
                valueSeg = dseg;
                break;
            case SEGMENT_EEPROM:
                valueSeg = eseg;
                break;
            default:
                valueSeg = cseg;
        } // конец switch (currentSeg)
        return labels.addVariable (labelName, valueSeg);
    } // конец метода addDeclarationLabel

    // метод проверяет, не была ли ранее заведена переменная (метка) с именем nameVar. Если переменна с таким именем есть - возвращает true
    private boolean isNameVar (String nameVar){
        if (vars_def.isNameVar(nameVar)){
            return true;
        }
        if (vars_equ.isNameVar(nameVar)){
            return true;
        }
        if (vars_set.isNameVar(nameVar)){
            return true;
        }
        if (labels.isNameVar(nameVar)){
            return true;
        }
        return false;
    } // конец checkNameVar

    // метод добавления переменной, определенной директивой def. Если переменная с именем nameVar уже существует, возвращает false
    public boolean addVarDef (String nameVar, String value){
        if (isNameVar (nameVar)){
            return false;
        }
        this.vars_def.addVariable (nameVar, value);
        return true;
    } // конец метода addVarDef

    // метод добавления переменной, определенной директивой equ. Если переменная с именем nameVar уже существует, возвращает false
    public boolean addVarEqu (String nameVar, int value){
        if (isNameVar (nameVar)){
            return false;
        }
        this.vars_equ.addVariable (nameVar, value);
        return true;
    } // конец метода addVarEqu

    // метод добавления переменной, определенной директивой set. Если переменная с именем nameVar уже определена директивами def, equ или является меткой, возвращает false
    public boolean addVarSet (String nameVar, int value){
        if (vars_def.isNameVar(nameVar)){
            return false;
        }
        if (vars_equ.isNameVar(nameVar)){
            return false;
        }
        if (labels.isNameVar(nameVar)){
            return false;
        }
        if (vars_set.isNameVar(nameVar)){
            vars_set.remove(nameVar);
        }
        vars_set.addVariable (nameVar, value);
        return true;
    } // конец метода addVarEqu

    // сохраняет текущее состояние основных переменных CollecterCode
    public StateCollecter getState (){
        return new StateCollecter (this);
    } // конец метода getState

    // метод загружает ранее сохраненное в state состояние основных переменных CollecterCode
    public void setState (StateCollecter state){
        setCSEG (state.getCseg());
        setDSEG (state.getDseg());
        setESEG (state.getEseg());
        setCurrentSeg (state.getCurrentSeg());
        vars_def = state.getVarsDef();
        vars_equ = state.getVarsEqu();
        vars_set = state.getVarsSet();
    } // конец метода setState

} // конец класса CollecterCode

