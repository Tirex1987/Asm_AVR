package com.tirex.avr_assembler;

import java.util.ArrayList;
import com.tirex.avr_assembler.ParserOperations.*;

public class Parser{

    private final int TOKEN_PARSER_LABEL = 50; // токен "метка"
    private final int TOKEN_PARSER_LOGIC_AND = 51; // токен "логическое И" '&&'
    private final int TOKEN_PARSER_LOGIC_OR = 52; // токен "логическое ИЛИ" '||'
    private final int TOKEN_PARSER_NOT_RAVNO = 53; //  токен "не равно" '!='
    private final int TOKEN_PARSER_IF_RAVNO = 54; // токен "если равно" '=='
    private final int TOKEN_PARSER_BIG_OR_RAVNO = 55; // токен "больше или равно" '>='
    private final int TOKEN_PARSER_LESS_OR_RAVNO = 56; // токен "меньше или равно" '<='
    private final int TOKEN_PARSER_SHIFT_RIGHT = 57; // токен "сдвиг вправо" '>>'
    private final int TOKEN_PARSER_SHIFT_LEFT = 58; // токен "сдвиг влево" '<<'
    private final int TOKEN_PARSER_MINUS = 59; // токен "унарный минус" '-'
    private final int TOKEN_PARSER_PLUS = 60; // токен "унарный плюс" '+' (для прединкремента регистров X, Y, Z)
    private final int TOKEN_PARSER_INCLUDE = 61; // токен "подгрузить файл" '<имяфайла.хх>'
    private final int TOKEN_PARSER_ELEMENT_OF_OPERATIONS = 62; // токен элемента, значение Number которого содержит номер в списке операций (для преобразования строки в список operations)

    public static final int ERROR_UNKNOWN_START = 1; // неизвестное выражение в начале строки (ожидается инструкция или директива)
    public static final int ERROR_OPERAND_CAN_NOT_BE_DIR_OR_INS = 2; // инструкции и директивы не могут использоваться в качестве операндов
    public static final int ERROR_OPERAND_NOT_OPENED = 3; // в операнде есть закрывающая скобка, которой не предшествует парная открывающая скобка
    public static final int ERROR_OPERAND_NOT_CLOSED = 4; // в операнде есть открывающая скобка, но нет парной закрывающей скобки
    public static final int ERROR_OPERAND_UNKNOWN_VALUE = 5; // недопустммое выражение в операнде
    public static final int ERROR_OPERAND_NO_OPERATIONS = 6; // в операнде подряд без разделителя идут несколько значений: ожидается запятая или знак операции
    public static final int ERROR_EXCEPTED_OPENED = 7; // ожидается открывающая скобка перед текущим значением после мнемоники функции в операнде
    public static final int ERROR_EXCEPTED_OPERATION = 8; // перед текущим значением в операнде ожидается запятая или знак бинарной операции
    public static final int ERROR_CAN_NOT_USE_KEYWORD = 9; // ключевое слово может быть только на первом месте в операнде или на втором после знака унарной операции
    public static final int ERROR_EXCEPTED_OPENED_BEFORE_UNAR = 10; // перед знаком унарной операции ожидается открывающая скобка
    public static final int ERROR_EXCEPTED_NUMBER_VALUE = 11; // перед текущей бинарной операцией ожидается числовое значение или пользовательское слово или ключевое слово
    public static final int ERROR_EXCEPTED_NUMBER_BEFORE_CLOSED = 12; // перед закрывающей скобкой ожидается число или пользовательское слово
    public static final int ERROR_COUNT_OPERANDS = 13; // превышено допустимое кол-во операндов в строке, заданное в константн MAX_OPERANDS класса ParserString
    public static final int ERROR_OPERAND_REGISTER_NO_PRIMER_OR_SECOND_WORD = 14; // имя регистра в операнде стоит не первым или вторым словом (нарушены конструкции постинкремента и прединкремента)
    public static final int ERROR_OPERAND_SIGN_REGISTER = 15; // в конструкции пост или пред инкремента/декремента стоит знак, отличный от допустимых - плюса и минуса
    public static final int ERROR_OPERAND_UNKNOWN_REGISTER = 16; // в конструкции пост или пред инкремента/декремента стоит регистр, недопустимый для этих операций (отличный от X, Y, Z)
    public static final int ERROR_OPERAND_UNKNOWN_ASSIGNMENT = 17; // недопустимый оператор присваивания (знак "=") в операнде
    public static final int ERROR_NO_OPERAND = 18; // в операнде есть запятая, но после нее нет никакого значения (строка заканчивается запятой)

    // parserText будет содержать весь набор элементов parserString
    public ArrayList<ParserString> parserText = new ArrayList<>();
    // список будет содержать ошибки, возникшие при обработке строк парсером
    private ArrayList<ErrorParserString> parserErrors = new ArrayList<>();

    // класс для хранения каждой обработанной парсером строки в листинге
    public class ParserString{
        public static final int MAX_OPERANDS = 9;
        public static final int TYPE_NO_COMMAND = 0;
        public static final int TYPE_COMMAND_INSTRUCTION = 1;
        public static final int TYPE_COMMAND_DIRECTIVE = 2;
        private int typeCommand; // тип команды: инструкция, директива
        private String label; // содержит название метки, если она есть в строке
        private String command; // команда (инструкция, директива)
        public ArrayList<ParserOperand> operands; // операнд или набор операндов для команды
        public void setTypeCommand(int typeCommand){
            this.typeCommand = typeCommand;
        }
        public int getTypeCommand(){
            return typeCommand;
        }
        public void setLabel(String labelName){
            this.label = labelName;
        }
        public String getLabel(){
            return label;
        }
        public void setCommand(String command){
            this.command = command;
        }
        public String getCommand(){
            return command;
        }
    } // конец класса ParserString

    // класс для хранения ошибки парсера
    public class ErrorParserString{
        private int numLine; // номер строки
        private int numError; // код ошибки
        private String errorWord; // слово, в котором ошибка
        public ErrorParserString (int numError, String errorWord, int numLine){
            this.numError = numError;
            this.errorWord = errorWord;
            this.numLine = numLine;
        } // конец конструктора ErrorParserString
        public int getNumError(){
            return numError;
        }
        public String getErrorWord(){
            return errorWord;
        }
        public int getNumLine(){
            return numLine;
        }
    } // конец класса ErrorParserString

    public int countErrors(){ return parserErrors.size(); };

    public int getErrorNumLine (int number){ return parserErrors.get(number).getNumLine(); }

    // метод возвращает текст ошибки по номеру, 1 - на русском языке, 2 - на английском
    public String textOfError (int numError, int language){
        String res = "";
        switch(language){
            case 1: // русский язык
                switch(numError){
                    case ERROR_UNKNOWN_START:
                        res = "Недопустимое выражение, ожидается инструкция или директива";
                        break;
                    case ERROR_OPERAND_CAN_NOT_BE_DIR_OR_INS:
                        res = "Недопустимое выражение, инструкции и директивы не могут использоваться в операнде";
                        break;
                    case ERROR_OPERAND_NOT_OPENED:
                        res = "Обнаружена ')', но нет парной открывающей скобки";
                        break;
                    case ERROR_OPERAND_NOT_CLOSED:
                        res = "Обнаружена '(', но нет парной закрывающей скобки";
                        break;
                    case ERROR_OPERAND_UNKNOWN_VALUE:
                        res = "Недопустимое выражение в операнде";
                        break;
                    case ERROR_OPERAND_NO_OPERATIONS:
                        res = "Недопустимое выражение, ожидается разделитель или знак арифметической операции";
                        break;
                    case ERROR_EXCEPTED_OPENED:
                        res = "Ожидается '('";
                        break;
                    case ERROR_EXCEPTED_OPERATION:
                        res = "Ожидается ',' или знак арифметической операции";
                        break;
                    case ERROR_CAN_NOT_USE_KEYWORD:
                        res = "Недопустимое использование ключевого слова";
                        break;
                    case ERROR_EXCEPTED_OPENED_BEFORE_UNAR:
                        res = "Перед знаком унарной операции ожидается '('";
                        break;
                    case ERROR_EXCEPTED_NUMBER_VALUE:
                        res = "Ожидается значение перед бинарной операцией";
                        break;
                    case ERROR_EXCEPTED_NUMBER_BEFORE_CLOSED:
                        res = "Ожидается значение перед закрывающей скобкой";
                        break;
                    case ERROR_COUNT_OPERANDS:
                        res = "Недопустимое количество операндов";
                        break;
                    case ERROR_OPERAND_REGISTER_NO_PRIMER_OR_SECOND_WORD:
                        res = "Недопустимое использование имени регистра";
                        break;
                    case ERROR_OPERAND_SIGN_REGISTER:
                        res = "Недопустимая операция с регистром";
                        break;
                    case ERROR_OPERAND_UNKNOWN_REGISTER:
                        res = "В конструкции не может быть использован этот регистр";
                        break;
                    case ERROR_OPERAND_UNKNOWN_ASSIGNMENT:
                        res = "Недопустимый оператор присваивания '=' в операнде";
                        break;
                    case ERROR_NO_OPERAND:
                        res = "Обнаружен разделитель ',', но после него нет операнда";
                        break;
                }
                break;
            case 2: //английский язык
                switch(numError){
                    case ERROR_UNKNOWN_START:
                        res = "Invalid expression, instruction or directive expected";
                        break;
                    case ERROR_OPERAND_CAN_NOT_BE_DIR_OR_INS:
                        res = "Invalid expression, instructions and directives cannot be used in the operand";
                        break;
                    case ERROR_OPERAND_NOT_OPENED:
                        res = "Detected ')' but no paired opening parenthesis";
                        break;
                    case ERROR_OPERAND_NOT_CLOSED:
                        res = "'(' Detected, but no pair closing parenthesis";
                        break;
                    case ERROR_OPERAND_UNKNOWN_VALUE:
                        res = "Invalid expression in operand";
                        break;
                    case ERROR_OPERAND_NO_OPERATIONS:
                        res = "Invalid expression, separator or arithmetic sign expected";
                        break;
                    case ERROR_EXCEPTED_OPENED:
                        res = "Expected '('";
                        break;
                    case ERROR_EXCEPTED_OPERATION:
                        res = "Expected ',' or sign of arithmetic operation";
                        break;
                    case ERROR_CAN_NOT_USE_KEYWORD:
                        res = "Invalid Keyword Use";
                        break;
                    case ERROR_EXCEPTED_OPENED_BEFORE_UNAR:
                        res = "Before the unary operation sign is expected '('";
                        break;
                    case ERROR_EXCEPTED_NUMBER_VALUE:
                        res = "Expected value before binary operation";
                        break;
                    case ERROR_EXCEPTED_NUMBER_BEFORE_CLOSED:
                        res = "Expected value before closing parenthesis";
                        break;
                    case ERROR_COUNT_OPERANDS:
                        res = "Invalid number of operands";
                        break;
                    case ERROR_OPERAND_REGISTER_NO_PRIMER_OR_SECOND_WORD:
                        res = "Invalid use of register name";
                        break;
                    case ERROR_OPERAND_SIGN_REGISTER:
                        res = "Invalid register operation";
                        break;
                    case ERROR_OPERAND_UNKNOWN_REGISTER:
                        res = "This register cannot be used in the construction";
                        break;
                    case ERROR_OPERAND_UNKNOWN_ASSIGNMENT:
                        res = "Invalid assignment operator '=' in operand";
                        break;
                    case ERROR_NO_OPERAND:
                        res = "Separator ',' detected, but no operand after it";
                        break;
                }
        }
        return res;
    } // конец метода textOfError

    public String getFullTextError(int numInParserErrors, int language){
        int numError = parserErrors.get(numInParserErrors).getNumError();
        String res = textOfError(numError, language);
        res += ": "+parserErrors.get(numInParserErrors).errorWord;
        return res;
    }

    // класс для хранения одного слова типа LexerWord из операнда с указанием приоритета операции
    private class OperandWord{
        public static final int PRIORITET_LOGIC_OR = 4; // логическое или ||
        public static final int PRIORITET_LOGIC_AND = 5; // логическое и &&
        public static final int PRIORITET_BIT_OR = 6; // побитное или |
        public static final int PRIORITET_BIT_OR_NOT = 7; // побитное исключающее или ^
        public static final int PRIORITET_BIT_AND = 8; // побитное и &
        public static final int PRIORITET_NOT_RAVNO = 9; // не равно !=
        public static final int PRIORITET_IF_RAVNO = 9; // равно ==
        public static final int PRIORITET_BIG_OR_RAVNO = 10; // больше или равно >=
        public static final int PRIORITET_BIG = 10; // больше чем >
        public static final int PRIORITET_LESS_OR_RAVNO = 10; // меньше или равно <=
        public static final int PRIORITET_LESS = 10; // меньше чем <
        public static final int PRIORITET_SHIFT_RIGHT = 11; // сдвиг вправо >>
        public static final int PRIORITET_SHIFT_LEFT = 11; // сдвиг влево <<
        public static final int PRIORITET_OPERATION_PLUS = 12; // приоритет операции сложение +
        public static final int PRIORITET_OPERATION_MINUS = 12; // приоритет операции вычитание -
        public static final int PRIORITET_DIV = 13; // деление /
        public static final int PRIORITET_MULTIPLE = 13; // умножение *
        public static final int PRIORITET_MINUS = 14; // унарный минус -
        public static final int PRIORITET_PLUS = 14; // унарный плюс +, для прединкремента регистров X, Y, Z
        public static final int PRIORITET_BIT_NOT = 14; // побитное отрицание ~
        public static final int PRIORITET_LOGIC_NOT = 14; // логическое отрицание !
        public static final int PRIORITET_FUNCTION = 15; // функция
        public static final int PRIORITET_CLOSED = 16; // закрывающая скобка
        public static final int PRIORITET_OPENED = 16; // открывающая скобка
        private Lexer.LexerWord lexerValue;
        private int prioritet;
        public void setLexerValue( Lexer.LexerWord lexerValue){
            this.lexerValue = lexerValue;
        }
        public Lexer.LexerWord getLexerValue(){
            return lexerValue;
        }
        private void setPrioritet(int prioritet){
            this.prioritet = prioritet;
        }
        public int getPrioritet(){
            return prioritet;
        }
        public void loadPrioritet(){ // метод устанавливает значение приоритета в зависимости от токена lexerValue
            switch (lexerValue.getToken()){
                case TOKEN_PARSER_LOGIC_AND:
                    prioritet = PRIORITET_LOGIC_AND;
                    break;
                case TOKEN_PARSER_LOGIC_OR:
                    prioritet = PRIORITET_LOGIC_OR;
                    break;
                case Lexer.TOKEN_BIT_OR:
                    prioritet = PRIORITET_BIT_OR;
                    break;
                case Lexer.TOKEN_BIT_OR_NOT:
                    prioritet = PRIORITET_BIT_OR_NOT;
                    break;
                case Lexer.TOKEN_BIT_AND:
                    prioritet = PRIORITET_BIT_AND;
                    break;
                case TOKEN_PARSER_NOT_RAVNO:
                    prioritet = PRIORITET_NOT_RAVNO;
                    break;
                case TOKEN_PARSER_IF_RAVNO:
                    prioritet = PRIORITET_IF_RAVNO;
                    break;
                case TOKEN_PARSER_BIG_OR_RAVNO:
                    prioritet = PRIORITET_BIG_OR_RAVNO;
                    break;
                case Lexer.TOKEN_BIG:
                    prioritet = PRIORITET_BIG;
                    break;
                case TOKEN_PARSER_LESS_OR_RAVNO:
                    prioritet = PRIORITET_LESS_OR_RAVNO;
                    break;
                case Lexer.TOKEN_LESS:
                    prioritet = PRIORITET_LESS;
                    break;
                case TOKEN_PARSER_SHIFT_RIGHT:
                    prioritet = PRIORITET_SHIFT_RIGHT;
                    break;
                case TOKEN_PARSER_SHIFT_LEFT:
                    prioritet = PRIORITET_SHIFT_LEFT;
                    break;
                case Lexer.TOKEN_OPERATION_PLUS:
                    prioritet = PRIORITET_OPERATION_PLUS;
                    break;
                case Lexer.TOKEN_OPERATION_MINUS:
                    prioritet = PRIORITET_OPERATION_MINUS;
                    break;
                case Lexer.TOKEN_DIV:
                    prioritet = PRIORITET_DIV;
                    break;
                case Lexer.TOKEN_MULTI:
                    prioritet = PRIORITET_MULTIPLE;
                    break;
                case TOKEN_PARSER_MINUS:
                    prioritet = PRIORITET_MINUS;
                    break;
                case TOKEN_PARSER_PLUS:
                    prioritet = PRIORITET_PLUS;
                    break;
                case Lexer.TOKEN_BIT_NOT:
                    prioritet = PRIORITET_BIT_NOT;
                    break;
                case Lexer.TOKEN_LOGIC_NOT:
                    prioritet = PRIORITET_LOGIC_NOT;
                    break;
                case Lexer.TOKEN_FUNCTION:
                    prioritet = PRIORITET_FUNCTION;
                    break;
                case Lexer.TOKEN_CLOSED:
                    prioritet = PRIORITET_CLOSED;
                    break;
                case Lexer.TOKEN_OPENED:
                    prioritet = PRIORITET_OPENED;
                    break;
                default:
                    prioritet = 0;
            } // конец switch (lexerValue.token)
        } // конец метода loadPrioritet
        public void setElementOfOperations (int numElementOfOperations){
            lexerValue.setToken(TOKEN_PARSER_ELEMENT_OF_OPERATIONS);
            setPrioritet(0);
            lexerValue.setNumber (numElementOfOperations); // ссылается на последнюю строку в списке operations
        } // конец метода setElementOfOperations
    } // конец класса operandWord

    // класс для хранения набора элементов operandWord
    private class OperandWords{
        private ArrayList<OperandWord> listOperandWord = new ArrayList<>();
        public void addWord (Lexer.LexerWord lexerWord){
            OperandWord word = new OperandWord();
            word.setLexerValue (lexerWord);
            word.loadPrioritet();
            listOperandWord.add(word);
        }
        public int count(){
            return listOperandWord.size();
        }
        public OperandWord get(int index){
            return listOperandWord.get(index);
        }
        public void addCopyFromWord (OperandWord operandWord){
            listOperandWord.add (operandWord);
        }
        public void remove (int index){
            listOperandWord.remove (index);
        }
    } // конец класса operandWords

    // метод построчной обработки строк лексера парсером (строки лексера передаются в этот метод строго поочередно по одной). Возвращает полученную строку парсера
    // При каждом вызове обнуляет список ошибок, поэтому после отработки список содержит ошибки только текущей строки
    public ParserString lineOfLexerToParser (ArrayList<Lexer.LexerWord> lexerString){
        ParserString returnedParserString;
        parserErrors = new ArrayList<>();
        returnedParserString = lexerToParserString(lexerString, 0);
        if ((returnedParserString != null)&&((returnedParserString.getLabel() != null)||(returnedParserString.getTypeCommand() != ParserString.TYPE_NO_COMMAND))){ // если полученная строка типа ParserString не пустая
            return returnedParserString;
        }
        return null;
    } // конец метода lineOfLexerToParser

    // основной метод преобразования всех строк, состоящих из lexerWord, в строки ParserString
    public void lexerToParserText (ArrayList<ArrayList<Lexer.LexerWord>> lexerText){
        ParserString returnedParserString;
        for (int i=0; i<lexerText.size(); i++){ // цикл по всем строкам lexerText
            returnedParserString = lexerToParserString(lexerText.get(i), i);
            if ((returnedParserString != null)&&((returnedParserString.getLabel() != null)||(returnedParserString.getTypeCommand() != ParserString.TYPE_NO_COMMAND))){ // если полученная строка типа ParserString не пустая
                parserText.add (returnedParserString);
            }
        } // конец for (int i=0; i<lexerText.size(); i++)
    } // конец метода lexerToParserText

    // основной метод преобразования набора элементов LexerWord в строку ParserString
    public ParserString lexerToParserString (ArrayList<Lexer.LexerWord> lexerString, int numLine){
        ParserString resultParserString; // результирующая обработанная строка
        if (lexerString==null){
            return null;
        } // конец if (lexerString==null)
        if (lexerString.size()==0){
            return null;
        } // конец if(lexerString.size==0)
        resultParserString = new ParserString();
        findParserConstructions (lexerString);
        if (lexerString.get(0).getToken()==TOKEN_PARSER_LABEL){ // если первый элемент - метка
            resultParserString.setLabel(lexerString.get(0).getText()); // в поле label записываем имя метки
            lexerString.remove(0); // удаляем первый элемент из lexerString (имя метки)
            if (lexerString.size()==0){ // если после удаления элементов список lexerString пустой
                return resultParserString;
            } // конец if (lexerString.size()==0)
        } // конец if (lexerString.get(0).getToken==TOKEN_PARSER_LABEL)
        switch (lexerString.get(0).getToken()){ // проверяем значение токена первого элемента lexerString
            case Lexer.TOKEN_INSTRUCTION: // если первый элемент - инструкция
                resultParserString.setTypeCommand (ParserString.TYPE_COMMAND_INSTRUCTION);
                resultParserString.setCommand (lexerString.get(0).getText());
                break;
            case Lexer.TOKEN_DIRECTIVE: // если первый элемент - директива
                resultParserString.setTypeCommand (ParserString.TYPE_COMMAND_DIRECTIVE);
                resultParserString.setCommand (lexerString.get(0).getText());
                break;
            default:
                parserErrors.add (new ErrorParserString(ERROR_UNKNOWN_START, lexerString.get(0).getText(), numLine));
                return resultParserString;
        } // конец switch (lexerString.get(0).getToken())
        if (lexerString.size()==1){ // если lexerString не содержит операндов, а состоит только из одного слова - инструкции или директивы
            return resultParserString;
        }else{ // если в lexerString более одного слова, значит далее следуют операнды
            loadOperands (lexerString, resultParserString, numLine);
        } // конец if (lexerString.size()==1)
        return resultParserString;
    } // конец метода lexerToParsetString

    // вспомогательный метод проверяет допустима ли последовательность значений и знаков операций в операнде
    // возвращает true если последовательность допустима, иначе возвращает false
    private boolean checkValue (OperandWords operandWords, int numLine){
        final int ELEMENT_FIRST = 0;
        final int ELEMENT_NUMBER_OR_USERWORD = 1;
        final int ELEMENT_UNAR_OPERATION = 2;
        final int ELEMENT_BINAR_OPERATION = 3;
        final int ELEMENT_FUNCTION = 4;
        final int ELEMENT_KEYWORD = 5;
        boolean res = true;
        int isValue = ELEMENT_FIRST;
        int numError;
        for (int i=0; i<operandWords.count(); i++){
            switch (operandWords.get(i).getLexerValue().getToken()){
                case Lexer.TOKEN_NUMBER:
                case Lexer.TOKEN_USERWORD:
                    if ((isValue==ELEMENT_FIRST) || (isValue==ELEMENT_UNAR_OPERATION) || (isValue ==ELEMENT_BINAR_OPERATION)){
                        isValue = ELEMENT_NUMBER_OR_USERWORD;
                    }else{
                        if (isValue==ELEMENT_FUNCTION){
                            numError = ERROR_EXCEPTED_OPENED;
                        }else{
                            numError = ERROR_EXCEPTED_OPERATION;
                        } // конец if (isValue==ELEMENT_FUNCTION)
                        parserErrors.add (new ErrorParserString(numError, operandWords.get(i).getLexerValue().getText(), numLine));
                        res = false;
                    } // конец if ((isValue==ELEMENT_FIRST) || (isValue==ELEMENT_UNAR_OPERATION) || (isValue ==ELEMENT_BINAR_OPERATION))
                    break;
                case Lexer.TOKEN_KEYWORD:
                    if ((isValue==ELEMENT_FIRST && i==0) || (isValue==ELEMENT_UNAR_OPERATION && i==1)){
                        isValue = ELEMENT_KEYWORD;
                    }else{
                        parserErrors.add (new ErrorParserString(ERROR_CAN_NOT_USE_KEYWORD, operandWords.get(i).getLexerValue().getText(), numLine));
                        res = false;
                    } // конец if ((isValue==ELEMENT_FIRST && i==0) || (isValue==ELEMENT_UNAR_OPERATION && i==1))
                    break;
                case Lexer.TOKEN_FUNCTION:
                    if ((isValue==ELEMENT_FIRST) || (isValue==ELEMENT_UNAR_OPERATION) || (isValue==ELEMENT_BINAR_OPERATION)){
                        isValue = ELEMENT_FUNCTION;
                    }else{
                        if (isValue==ELEMENT_FUNCTION){
                            numError = ERROR_EXCEPTED_OPENED;
                        }else{
                            numError = ERROR_EXCEPTED_OPERATION;
                        } // конец if (isValue==ELEMENT_FUNCTION)
                        parserErrors.add (new ErrorParserString(numError, operandWords.get(i).getLexerValue().getText(), numLine));
                        res = false;
                    } // конец if ((isValue==ELEMENT_FIRST) || (isValue==ELEMENT_UNAR_OPERATION) || (isValue==ELEMENT_BINAR_OPERATION))
                    break;
                case TOKEN_PARSER_MINUS: // унарный минус
                case TOKEN_PARSER_PLUS: // унарный плюс
                case Lexer.TOKEN_LOGIC_NOT:
                case Lexer.TOKEN_BIT_NOT:
                    if (isValue==ELEMENT_FIRST){
                        isValue = ELEMENT_UNAR_OPERATION;
                    }else{
                        parserErrors.add (new ErrorParserString(ERROR_EXCEPTED_OPENED_BEFORE_UNAR, operandWords.get(i).getLexerValue().getText(), numLine));
                        res = false;
                    } // конец if (isValue==ELEMENT_FIRST)
                    break;
                case TOKEN_PARSER_LOGIC_AND:
                case TOKEN_PARSER_LOGIC_OR:
                case TOKEN_PARSER_NOT_RAVNO:
                case TOKEN_PARSER_IF_RAVNO:
                case TOKEN_PARSER_BIG_OR_RAVNO:
                case TOKEN_PARSER_LESS_OR_RAVNO:
                case TOKEN_PARSER_SHIFT_RIGHT:
                case TOKEN_PARSER_SHIFT_LEFT:
                case Lexer.TOKEN_OPERATION_PLUS:
                case Lexer.TOKEN_OPERATION_MINUS:
                case Lexer.TOKEN_MULTI:
                case Lexer.TOKEN_BIT_AND:
                case Lexer.TOKEN_PERCENT:
                case Lexer.TOKEN_DIV:
                case Lexer.TOKEN_BIT_OR:
                case Lexer.TOKEN_BIT_OR_NOT:
                case Lexer.TOKEN_BIG:
                case Lexer.TOKEN_LESS:
                    if ((isValue==ELEMENT_NUMBER_OR_USERWORD) || (isValue==ELEMENT_KEYWORD)){
                        isValue = ELEMENT_BINAR_OPERATION;
                    }else{
                        if (isValue==ELEMENT_FUNCTION){
                            numError = ERROR_EXCEPTED_OPENED;
                        }else{
                            numError = ERROR_EXCEPTED_NUMBER_VALUE;
                        } // конец if (isValue==ELEMENT_FUNCTION)
                        parserErrors.add (new ErrorParserString(numError, operandWords.get(i).getLexerValue().getText(), numLine));
                        res = false;
                    } // конец if ((isValue==ELEMENT_NUMBER_OR_USERWORD) || (isValue==ELEMENT_KEYWORD))
                    break;
                case Lexer.TOKEN_OPENED:
                    if ((isValue==ELEMENT_FIRST) || (isValue==ELEMENT_UNAR_OPERATION) || (isValue==ELEMENT_BINAR_OPERATION) || (isValue==ELEMENT_FUNCTION)){
                        isValue = ELEMENT_FIRST;
                    }else{
                        parserErrors.add (new ErrorParserString(ERROR_EXCEPTED_OPERATION, operandWords.get(i).getLexerValue().getText(), numLine));
                        res = false;
                    } // конец if ((isValue==ELEMENT_FIRST) || (isValue==ELEMENT_UNAR_OPERATION) || (isValue==ELEMENT_BINAR_OPERATION) || (isValue==ELEMENT_FUNCTION))
                    break;
                case Lexer.TOKEN_CLOSED:
                    if (isValue==ELEMENT_NUMBER_OR_USERWORD){
                        isValue = ELEMENT_NUMBER_OR_USERWORD;
                    }else{
                        if (isValue==ELEMENT_FUNCTION){
                            numError = ERROR_EXCEPTED_OPENED;
                        }else{
                            numError = ERROR_EXCEPTED_NUMBER_BEFORE_CLOSED;
                        } // конец if (isValue==ELEMENT_FUNCTION)
                        parserErrors.add (new ErrorParserString(numError, operandWords.get(i).getLexerValue().getText(), numLine));
                        res = false;
                    } // конец if (isValue==ELEMENT_NUMBER_OR_USERWORD)
                    break;
                default:
                    parserErrors.add (new ErrorParserString(ERROR_OPERAND_UNKNOWN_VALUE, operandWords.get(i).getLexerValue().getText(), numLine));
                    res = false;
            } // конец switch (operandWords.get(i).getLexerValue().getToken())
            if (! res){
                break;
            }
        } // конец for (int i=0; i<operandWords.count(); i++)
        return res;
    } // конец checkValue

    // вспомогательный метод находит операцию с максимальным приоритетом в наборе OperandWords и возвращает ее приоритет
    private int findMaxPrioritet (OperandWords operandWords){
        int maxPrioritet = 0;
        for (int i=0; i<operandWords.count(); i++){
            if (operandWords.get(i).getPrioritet()>maxPrioritet){
                maxPrioritet = operandWords.get(i).getPrioritet();
            }
        } // конец for (int i=0; i<operandWords.count(); i++)
        return maxPrioritet;
    } // конец метода findMaxPrioritet

    // вспомогательный метод возвращает номер элемента в списке, который содержит операцию с указанным приоритетом
    private int findNumberElement (OperandWords operandWords, int prioritet){
        int i;
        for (i=0; i<operandWords.count(); i++){
            if (operandWords.get(i).getPrioritet()==prioritet){
                break;
            }
        } // конец for (int i=0; i<openWords.count(); i++)
        return i;
    } // конец метода findNumberElement

    // вспомогательный метод преобразовывает элемент operandWord в операнд типа OperandOfOperation
    private OperandOfOperation makeOperand (OperandWord operandWord){
        OperandOfOperation result = new OperandOfOperation();
        switch (operandWord.getLexerValue().getToken()){
            case Lexer.TOKEN_NUMBER:
            case Lexer.TOKEN_CHAR:
                result.setType (OperandOfOperation.TYPE_NUMBER);
                result.setNumberValue (operandWord.getLexerValue().getNumber());
                break;
            case Lexer.TOKEN_USERWORD:
                result.setType (OperandOfOperation.TYPE_USERWORD);
                result.setStringValue (operandWord.getLexerValue().getText());
                break;
            case TOKEN_PARSER_ELEMENT_OF_OPERATIONS:
                result.setType (OperandOfOperation.TYPE_OPERATION);
                result.setNumberOperation(operandWord.getLexerValue().getNumber());
                break;
            case Lexer.TOKEN_KEYWORD:
                result.setType (OperandOfOperation.TYPE_KEYWORD);
                result.setStringValue (operandWord.getLexerValue().getText());
                break;
            default:
                return null;
        } // конец switch (operandWords.get(numElement+1).getLexerValue().getToken)
        return result;
    } // конец метода makeOperand

    // вспомогательный метод возвращает операцию типа Operation1operand с заполненными полями типа операции, имени операции и операнда
    private Operation1operand createOperation1operand (int operationName, OperandWords operandWords, int numOperand){
        Operation1operand result = new Operation1operand();
        result.setTypeOperation (Operation.TYPE_OPERATION_1_OPERAND);
        result.setOperationName (operationName);
        result.setOperand (makeOperand(operandWords.get(numOperand)));
        operandWords.remove (numOperand);
        return result;
    } // конец метода createOperation1operand

    // вспомогательный метод возвращает операцию типа Operation2operands с заполненными полями типа операции, имени операции и операндов
    private Operation2operands createOperation2operands (int operationName, OperandWords operandWords, int numOperation){
        Operation2operands result = new Operation2operands();
        result.setTypeOperation (Operation.TYPE_OPERATION_2_OPERANDS);
        result.setOperationName (operationName);
        result.setOperand1 (makeOperand(operandWords.get(numOperation-1)));
        result.setOperand2 (makeOperand(operandWords.get(numOperation+1)));
        operandWords.remove (numOperation+1);
        operandWords.remove (numOperation-1);
        return result;
    } // конец метода createOperation1operand

    // вспомогательный метод загружает в список operations операцию с указанным приоритетом и операндами
    // после этого заменяет в списке operandWords эту операцию с операндами на элемент с токеном TOKEN_PARSER_ELEMENT_OF_OPERATION
    public void loadOperationInList (OperandWords operandWords, int prioritet, ArrayList<Operation> operations){
        int numElement; // номер элемента, который содержит операцию с указанным приоритетом
        Operation operation=null; // добавляемая операция
        numElement = findNumberElement (operandWords, prioritet);
        switch (operandWords.get(numElement).getLexerValue().getToken()){
            case TOKEN_PARSER_MINUS:
                operation = createOperation1operand (Operation1operand.OPERATION_UNAR_MINUS, operandWords, numElement+1);
                operandWords.get(numElement).setElementOfOperations(operations.size());
                break;
            case TOKEN_PARSER_PLUS:
                operation = createOperation1operand (Operation1operand.OPERATION_UNAR_PLUS, operandWords, numElement+1);
                operandWords.get(numElement).setElementOfOperations(operations.size());break;
            case Lexer.TOKEN_BIT_NOT:
                operation = createOperation1operand (Operation1operand.OPERATION_BIT_NOT, operandWords, numElement+1);
                operandWords.get(numElement).setElementOfOperations(operations.size());
                break;
            case Lexer.TOKEN_LOGIC_NOT:
                operation = createOperation1operand (Operation1operand.OPERATION_LOGIC_NOT, operandWords, numElement+1);
                operandWords.get(numElement).setElementOfOperations(operations.size());
                break;
            case TOKEN_PARSER_LOGIC_OR:
                operation = createOperation2operands (Operation2operands.OPERATION_LOGIC_OR, operandWords, numElement);
                operandWords.get(numElement-1).setElementOfOperations(operations.size());
                break;
            case TOKEN_PARSER_LOGIC_AND:
                operation = createOperation2operands (Operation2operands.OPERATION_LOGIC_AND, operandWords, numElement);
                operandWords.get(numElement-1).setElementOfOperations(operations.size());
                break;
            case Lexer.TOKEN_BIT_OR:
                operation = createOperation2operands (Operation2operands.OPERATION_BIT_OR, operandWords, numElement);
                operandWords.get(numElement-1).setElementOfOperations(operations.size());
                break;
            case Lexer.TOKEN_BIT_OR_NOT:
                operation = createOperation2operands (Operation2operands.OPERATION_BIT_OR_NOT, operandWords, numElement);
                operandWords.get(numElement-1).setElementOfOperations(operations.size());
                break;
            case Lexer.TOKEN_BIT_AND:
                operation = createOperation2operands (Operation2operands.OPERATION_BIT_AND, operandWords, numElement);
                operandWords.get(numElement-1).setElementOfOperations(operations.size());
                break;
            case TOKEN_PARSER_NOT_RAVNO:
                operation = createOperation2operands (Operation2operands.OPERATION_NOT_RAVNO, operandWords, numElement);
                operandWords.get(numElement-1).setElementOfOperations(operations.size());
                break;
            case TOKEN_PARSER_IF_RAVNO:
                operation = createOperation2operands (Operation2operands.OPERATION_IF_RAVNO, operandWords, numElement);
                operandWords.get(numElement-1).setElementOfOperations(operations.size());
                break;
            case TOKEN_PARSER_BIG_OR_RAVNO:
                operation = createOperation2operands (Operation2operands.OPERATION_BIG_OR_RAVNO, operandWords, numElement);
                operandWords.get(numElement-1).setElementOfOperations(operations.size());
                break;
            case Lexer.TOKEN_BIG:
                operation = createOperation2operands (Operation2operands.OPERATION_BIG, operandWords, numElement);
                operandWords.get(numElement-1).setElementOfOperations(operations.size());
                break;
            case TOKEN_PARSER_LESS_OR_RAVNO:
                operation = createOperation2operands (Operation2operands.OPERATION_LESS_OR_RAVNO, operandWords, numElement);
                operandWords.get(numElement-1).setElementOfOperations(operations.size());
                break;
            case Lexer.TOKEN_LESS:
                operation = createOperation2operands (Operation2operands.OPERATION_LESS, operandWords, numElement);
                operandWords.get(numElement-1).setElementOfOperations(operations.size());
                break;
            case TOKEN_PARSER_SHIFT_RIGHT:
                operation = createOperation2operands (Operation2operands.OPERATION_SHIFT_RIGHT, operandWords, numElement);
                operandWords.get(numElement-1).setElementOfOperations(operations.size());
                break;
            case TOKEN_PARSER_SHIFT_LEFT:
                operation = createOperation2operands (Operation2operands.OPERATION_SHIFT_LEFT, operandWords, numElement);
                operandWords.get(numElement-1).setElementOfOperations(operations.size());
                break;
            case Lexer.TOKEN_OPERATION_PLUS:
                operation = createOperation2operands (Operation2operands.OPERATION_PLUS, operandWords, numElement);
                operandWords.get(numElement-1).setElementOfOperations(operations.size());
                break;
            case Lexer.TOKEN_OPERATION_MINUS:
                operation = createOperation2operands (Operation2operands.OPERATION_MINUS, operandWords, numElement);
                operandWords.get(numElement-1).setElementOfOperations(operations.size());
                break;
            case Lexer.TOKEN_DIV:
                operation = createOperation2operands (Operation2operands.OPERATION_DIV, operandWords, numElement);
                operandWords.get(numElement-1).setElementOfOperations(operations.size());
                break;
            case Lexer.TOKEN_MULTI:
                operation = createOperation2operands (Operation2operands.OPERATION_MULTIPLE, operandWords, numElement);
                operandWords.get(numElement-1).setElementOfOperations(operations.size());
                break;
            case Lexer.TOKEN_FUNCTION:
                operation = new OperationFunction();
                operation.setTypeOperation (Operation.TYPE_OPERATION_FUNCTION);
                ((OperationFunction) operation).determinateFunction(operandWords.get(numElement).getLexerValue().getText());
                ((OperationFunction) operation).setOperand (makeOperand(operandWords.get(numElement+1)));
                operandWords.remove (numElement+1);
                operandWords.get(numElement).setElementOfOperations(operations.size());
                break;
        } // конец switch (operandWords.get(numElement).getLexerValue().getToken())
        operations.add (operation);
    } // конец метода loadOperationInList

    // вспомогательный метод, переводит набор элементов OperandWords в список операций ArrayList<Operation>
    // вызывается только после метода checkValue
    private void getListOperations (OperandWords operandWords, ArrayList<Operation> operations, int numLine){
        int maxPrioritet; // максимальный приоритет операции в текущем списке operandWords
        int valueOpened=0, valueClosed; // номер элемента открывающей и соответствующей ему закрывающей скобки
        int skobki = 0; // значение переменной увеличивается на 1, когда встречается открывающая скобка, и уменьшается на 1, когда встречается закрывающая скобка
        int num; // вспомогательная переменная
        OperandWords rekursOperandWords;
        maxPrioritet = findMaxPrioritet(operandWords);
        while (maxPrioritet==OperandWord.PRIORITET_OPENED){ // повторяем, пока максимальный приоритет - это открывающая скобка
            valueClosed = 0;
            for (int i=0; i<operandWords.count(); i++){ // цикл по всем элементам operandWords
                switch (operandWords.get(i).getLexerValue().getToken()){
                    case Lexer.TOKEN_OPENED: // если текущий элемент - открывающая скобка
                        if (skobki==0){ // если значение skobki равно нулю, значит  это первая открывающая скобка в списке operandWords
                            valueOpened = i;
                        }
                        skobki++;
                        break;
                    case Lexer.TOKEN_CLOSED: // если текущий элемент - закрывающая скобка
                        skobki--;
                        if (skobki==0){ // если значение skobki равно нулю, значит это соответствующая закрывающая скобка
                            valueClosed = i;
                        }
                        break;
                } // конец switch (operandWords.get(i).getLexerValue().getToken())
                if (valueClosed>0){
                    break; // выходим из цикла
                }
            } // конец for (int i=0; i<operandWords.count(); i++)
            rekursOperandWords = new OperandWords();
            for (int i=valueOpened+1; i<valueClosed; i++){ // в цикле формируем новый список элементов, состоящий из элементов operandWords между открывающей и закрывающей скобками
                rekursOperandWords.addCopyFromWord (operandWords.get(i));
            }
            getListOperations (rekursOperandWords, operations, numLine); // рекурсивно передаем сформированный список элементов в метод getListOperations
            for (int i=valueClosed; i>valueOpened; i--){ // в цикле удаляем элементы, начиная с элемента, следующего за открывающей скобкой, и заканчивая закрывающей кобкой
                operandWords.remove (i);
            }
            operandWords.get(valueOpened).setElementOfOperations (operations.size()-1); // преобразуем элемент, который содержал открывающую скобку. (operations.size()-1) - ссылается на последнюю строку в списке operations
            maxPrioritet = findMaxPrioritet (operandWords);
        } // конец while (maxPrioritet==OperandWord.PRIORITET_OPENED)
        if (maxPrioritet==0){
            operations.add (new OperandWithoutOperation());
            switch (operandWords.get(0).getLexerValue().getToken()){
                case Lexer.TOKEN_NUMBER:
                case Lexer.TOKEN_CHAR:
                    ((OperandWithoutOperation) operations.get (operations.size()-1)).getOperand().setType(OperandOfOperation.TYPE_NUMBER);
                    ((OperandWithoutOperation) operations.get (operations.size()-1)).getOperand().setNumberValue(operandWords.get(0).getLexerValue().getNumber());
                    break;
                case Lexer.TOKEN_USERWORD:
                    ((OperandWithoutOperation) operations.get (operations.size()-1)).getOperand().setType(OperandOfOperation.TYPE_USERWORD);
                    ((OperandWithoutOperation) operations.get (operations.size()-1)).getOperand().setStringValue(operandWords.get(0).getLexerValue().getText());
                    break;
                case Lexer.TOKEN_KEYWORD:
                    ((OperandWithoutOperation) operations.get (operations.size()-1)).getOperand().setType(OperandOfOperation.TYPE_KEYWORD);
                    ((OperandWithoutOperation) operations.get (operations.size()-1)).getOperand().setStringValue(operandWords.get(0).getLexerValue().getText());
                    break;
                case TOKEN_PARSER_ELEMENT_OF_OPERATIONS:
                    ((OperandWithoutOperation) operations.get (operations.size()-1)).getOperand().setType(OperandOfOperation.TYPE_OPERATION);
                    ((OperandWithoutOperation) operations.get (operations.size()-1)).getOperand().setNumberOperation(operandWords.get(0).getLexerValue().getNumber());
                    break;
            } // конец switch (operandWords.get(0).getLexerValue().getToken()
        }else { // если maxPrioritet > 0
            do {
                loadOperationInList(operandWords, maxPrioritet, operations);
                maxPrioritet = findMaxPrioritet(operandWords);
            } while (maxPrioritet > 0);
        } // конец if (maxPrioritet==0)}=
    } // конец метода getListOperations

    // вспомогательный метод определения конструкций пост инкремента или декремента в операнде
    private boolean construction_post (String regText, int signToken, ParserOperand parserOperand){
        boolean res = false; // возвращаемый результат: true если конструкция занесена в parserOperand.typeRegister; если регистр не соответствует X, Y или Z, то возвращает false
        switch (signToken){
            case Lexer.TOKEN_OPERATION_MINUS:
                if (regText.equalsIgnoreCase("X")){ // конструкция "X-"
                    parserOperand.setTypeRegister (ParserOperand.TYPE_REGISTER_X_MINUS);
                    res = true;
                } else {
                    if (regText.equalsIgnoreCase ("Y")){ // конструкция "Y-"
                        parserOperand.setTypeRegister (ParserOperand.TYPE_REGISTER_Y_MINUS);
                        res = true;
                    } else {
                        if (regText.equalsIgnoreCase ("Z")){ // конструкция "Z-"
                            parserOperand.setTypeRegister (ParserOperand.TYPE_REGISTER_Z_MINUS);
                            res = true;
                        } // конец if (regText.equalsIgnoreCase ("Z"))
                    } // конец if (regText.equalsIgnoreCase ("Y"))
                } // конец if (regText.equalsIgnoreCase("X"))
                break;
            case Lexer.TOKEN_OPERATION_PLUS:
                if (regText.equalsIgnoreCase("X")){ // конструкция "X+"
                    parserOperand.setTypeRegister (ParserOperand.TYPE_REGISTER_X_PLUS);
                    res = true;
                } else {
                    if (regText.equalsIgnoreCase ("Y")){ // конструкция "Y+"
                        parserOperand.setTypeRegister (ParserOperand.TYPE_REGISTER_Y_PLUS);
                        res = true;
                    } else {
                        if (regText.equalsIgnoreCase ("Z")){ // конструкция "Z+"
                            parserOperand.setTypeRegister (ParserOperand.TYPE_REGISTER_Z_PLUS);
                            res = true;
                        } // конец if (regText.equalsIgnoreCase ("Z"))
                    } // конец if (regText.equalsIgnoreCase ("Y"))
                } // конец if (regText.equalsIgnoreCase("X"))
                break;
        } // конец switch (signToken)
        return res;
    } // конец метода construction_post

    // вспомогательный метод определения конструкций пред инкремента или декремента в операнде
    private boolean construction_pred (String regText, int signToken, ParserOperand parserOperand){
        boolean res = false; // возвращаемый результат: true если конструкция занесена в parserOperand.typeRegister; если регистр не соответствует X, Y или Z, то возвращает false
        switch (signToken){
            case TOKEN_PARSER_MINUS:
                if (regText.equalsIgnoreCase("X")){ // конструкция "-X"
                    parserOperand.setTypeRegister (ParserOperand.TYPE_REGISTER_MINUS_X);
                    res = true;
                } else {
                    if (regText.equalsIgnoreCase ("Y")){ // конструкция "-Y"
                        parserOperand.setTypeRegister (ParserOperand.TYPE_REGISTER_MINUS_Y);
                        res = true;
                    } else {
                        if (regText.equalsIgnoreCase ("Z")){ // конструкция "-Z"
                            parserOperand.setTypeRegister (ParserOperand.TYPE_REGISTER_MINUS_Z);
                            res = true;
                        } // конец if (regText.equalsIgnoreCase ("Z"))
                    } // конец if (regText.equalsIgnoreCase ("Y"))
                } // конец if (regText.equalsIgnoreCase("X"))
                break;
            case TOKEN_PARSER_PLUS:
                if (regText.equalsIgnoreCase("X")){ // конструкция "+X"
                    parserOperand.setTypeRegister (ParserOperand.TYPE_REGISTER_PLUS_X);
                    res = true;
                } else {
                    if (regText.equalsIgnoreCase ("Y")){ // конструкция "+Y"
                        parserOperand.setTypeRegister (ParserOperand.TYPE_REGISTER_PLUS_Y);
                        res = true;
                    } else {
                        if (regText.equalsIgnoreCase ("Z")){ // конструкция "+Z"
                            parserOperand.setTypeRegister (ParserOperand.TYPE_REGISTER_PLUS_Z);
                            res = true;
                        } // конец if (regText.equalsIgnoreCase ("Z"))
                    } // конец if (regText.equalsIgnoreCase ("Y"))
                } // конец if (regText.equalsIgnoreCase("X"))
                break;
        } // конец switch (signToken)
        return res;
    } // конец метода construction_pred

    // вспомогательный метод для определения конструкций пост и пред -инкремента и -декремента
    private boolean construction_post_or_pred (OperandWords operandWords, ParserOperand parserOperand, int numLine){
        boolean res = true;
        OperandWord reg; // сюда заносим имя регистра
        OperandWord sign; // сюда заносим знак
        boolean post; // содержит true если это конструкция пост инкремента или декремента (если регистр стоит первым, а не вторым словом)
        if (operandWords.get(0).getLexerValue().getToken()==Lexer.TOKEN_KEYWORD){ // если регистр стоит первым словом
            reg = operandWords.get(0);
            sign = operandWords.get(1);
            post = true;
        } else {
            reg = operandWords.get(1);
            sign = operandWords.get(0);
            post = false;
        } // конец if (operandWords.get(0).getLexerValue()==Lexer.TOKEN_KEYWORD)
        if (! (sign.getLexerValue().getText().equals("-") || sign.getLexerValue().getText().equals("+"))){ // допустимы только знаки + и -, если стоит другой, то выдаем ошибку
            parserErrors.add (new ErrorParserString(ERROR_OPERAND_SIGN_REGISTER, operandWords.get(0).getLexerValue().getText()+operandWords.get(1).getLexerValue().getText(), numLine));
            return false;
        } // конец if (! (sign.getLexerValue().getText().equals("-") || sign.getLexerValue().getText().equals("+"))
        if (post){
            res = construction_post (reg.getLexerValue().getText(), sign.getLexerValue().getToken(), parserOperand);
        } else {
            res = construction_pred (reg.getLexerValue().getText(), sign.getLexerValue().getToken(), parserOperand);
        } // конец if (post)
        if (res){ // если конструкция пост или пред инкремента или декремента  определена
            if ((! post)||(operandWords.count()==2)){ // если это пред-инкремент/декремент или пост-инкремент/декремент только с двумя операндами. Если это пост-инкремент/декремент более чем с двумя операндами, то удаляем только первый операнд и оставляем знак
                operandWords.remove (1); // удаляем второй элемент в списке
            }
            operandWords.remove(0); // удаляем первый элемент в списке
        } else { // иначе выдаем ошибку - недопустимый регистр
            parserErrors.add (new ErrorParserString(ERROR_OPERAND_UNKNOWN_REGISTER, operandWords.get(0).getLexerValue().getText()+operandWords.get(1).getLexerValue().getText(), numLine));
        } // конец if (res)
        return res;
    } // конец метода construction_post_or_pred

    // вспомогательный метод проверяет наличие оператора присваивания (конструкция "пользовательское слово" + "=")
    public void checkAssignment (OperandWords operandWords, ParserOperand parserOperand){
        if (operandWords.get(0).getLexerValue().getToken()==Lexer.TOKEN_USERWORD){ // если первый элемент пользовательское слово
            if (operandWords.get(1).getLexerValue().getToken()==Lexer.TOKEN_RAVNO){ // если после пользовательского слова знак "="
                parserOperand.setFlagAssignment(); // устанавливаем в true флаг flagAssignment
                parserOperand.setVarAssignment (operandWords.get(0).getLexerValue().getText()); // сохраняем пользовательское слово (имя переменной) в nameVarAssignment
                operandWords.remove (1); // удаляем конструкцию присваивания
                operandWords.remove (0);
            }
        }
    } // конец метода checkAssignment

    // вспомогательный метод преобразования операнда из набора элементов OperandWord в ParserOperand
    // Если при выполнении обнаруживается ошибка в операнде, то возвращает null
    private ParserOperand loadOperand (OperandWords operandWords, int numLine){
        ParserOperand resultOperand = new ParserOperand();
        int skobki = 0; // переменная для проверки парности скобок
        int  max_prioritet = 0; // сюда сохраняем максимальное значение prioritet из всех элементов operandWord
        int i;
        if ((operandWords==null)||(operandWords.count()==0)){
            return null;
        }
        if (operandWords.count()>=3){
            checkAssignment (operandWords, resultOperand); // проверяем наличие оператора присваивания
        }
        for (i=0; i<operandWords.count(); i++){
            switch (operandWords.get(i).getLexerValue().getToken()){
                case Lexer.TOKEN_INSTRUCTION:
                case Lexer.TOKEN_DIRECTIVE:
                    parserErrors.add (new ErrorParserString(ERROR_OPERAND_CAN_NOT_BE_DIR_OR_INS, operandWords.get(i).getLexerValue().getText(), numLine));
                    break;
                case Lexer.TOKEN_RAVNO:
                    parserErrors.add (new ErrorParserString(ERROR_OPERAND_UNKNOWN_ASSIGNMENT, operandWords.get(i).getLexerValue().getText(), numLine));
                    break;
                case Lexer.TOKEN_OPENED:
                    skobki++;
                    break;
                case Lexer.TOKEN_CLOSED:
                    skobki--;
                    break;
                case Lexer.TOKEN_KEYWORD: // выявляем конструкции регистров с прединкрементом и постинкрементом
                    if (i>1){ // имя регистра может быть только первым или вторым словом в списке operandWords, иначе выдаем ошибку
                        parserErrors.add (new ErrorParserString(ERROR_OPERAND_REGISTER_NO_PRIMER_OR_SECOND_WORD, operandWords.get(i).getLexerValue().getText(), numLine));
                    } else {
                        if (operandWords.count()>1){
                            if (! construction_post_or_pred (operandWords, resultOperand, numLine)){
                                return null;
                            } else {
                                max_prioritet = 0;
                                if (operandWords.count()==0){ // если других операндов не осталось
                                    resultOperand.setTypeOperand (ParserOperand.TYPE_OPERAND_POST_OR_PRED);
                                    return resultOperand;
                                } // конец if (operandWords.count()==0)
                            } // конец if (! construction_post_or_pred (operandWords, resultOperand, numLine))
                        } // конец if (operandWords.count()>1)
                    } // конец if (i>1)
            } // конец switch (operandWords.get(i).getToken)
            if (skobki<0){ // встретилась непарная закрывающая скобка
                parserErrors.add (new ErrorParserString(ERROR_OPERAND_NOT_OPENED, operandWords.get(i).getLexerValue().getText(), numLine));
                break; // выходим из цикла
            } // конец if (skobki<0)
            if (operandWords.get(i).getPrioritet()>max_prioritet){ // если значение приоритета операции текущего элемента больше max_prioritet
                max_prioritet = operandWords.get(i).getPrioritet();
            } // конец if (operandWords.get(i).getPrioritet()>max_prioritet)
        } // конец for (int i=0; i<operandWords.count; i++)
        if (skobki>0){ // в  операнде нет парных закрывающих скобок
            parserErrors.add (new ErrorParserString(ERROR_OPERAND_NOT_CLOSED, operandWords.get(i-1).getLexerValue().getText(), numLine));
        } // конец if (skobki>0)
        if ((parserErrors.size()>0)&&(parserErrors.get(parserErrors.size()-1).getNumLine()==numLine)){ // если в текущей строке есть ошибки
            return null;
        }
        if (max_prioritet==0){ // если в операнде нет арифметических и логических операций, функций, скобок
            if (operandWords.count()==1){ // если операнд состоит только из одного элемента
                switch (operandWords.get(0).getLexerValue().getToken()){
                    case Lexer.TOKEN_NUMBER:
                        resultOperand.setTypeOperand (ParserOperand.TYPE_OPERAND_NUMBER);
                        resultOperand.setStringValue (operandWords.get(0).getLexerValue().getText());
                        resultOperand.setNumberValue (operandWords.get(0).getLexerValue().getNumber());
                        break;
                    case Lexer.TOKEN_STRING:
                        resultOperand.setTypeOperand (ParserOperand.TYPE_OPERAND_STRING);
                        resultOperand.setStringValue (operandWords.get(0).getLexerValue().getText());
                        break;
                    case Lexer.TOKEN_CHAR:
                        resultOperand.setTypeOperand (ParserOperand.TYPE_OPERAND_CHAR);
                        resultOperand.setStringValue (operandWords.get(0).getLexerValue().getText());
                        break;
                    case Lexer.TOKEN_USERWORD:
                        resultOperand.setTypeOperand (ParserOperand.TYPE_OPERAND_USERWORD);
                        resultOperand.setStringValue (operandWords.get(0).getLexerValue().getText());
                        break;
                    case Lexer.TOKEN_KEYWORD:
                        resultOperand.setTypeOperand (ParserOperand.TYPE_OPERAND_KEYWORD);
                        resultOperand.setStringValue (operandWords.get(0).getLexerValue().getText());
                        break;
                    case TOKEN_PARSER_INCLUDE:
                        resultOperand.setTypeOperand (ParserOperand.TYPE_OPERAND_INCLUDE);
                        resultOperand.setStringValue (operandWords.get(0).getLexerValue().getText());
                        break;
                    default:
                        parserErrors.add (new ErrorParserString(ERROR_OPERAND_UNKNOWN_VALUE, operandWords.get(0).getLexerValue().getText(), numLine));
                        return null;
                } // конец switch (operandWords.get(0).getLexerValue().getToken())
            }else{ // если max_prioritet=0, но операнд состоит из нескольких элементов, то выдаем ошибку
                parserErrors.add (new ErrorParserString(ERROR_OPERAND_NO_OPERATIONS, operandWords.get(0).getLexerValue().getText(), numLine));
                return null;
            } // конец if (operandWords.size()==1)
        }else{ // если max_prioritet не равен нулю
            if (operandWords.count()==1){ // max_prioritet не равен нулю, но в операнде только один элемент, значит выдаем ошибку
                parserErrors.add (new ErrorParserString(ERROR_OPERAND_UNKNOWN_VALUE, operandWords.get(0).getLexerValue().getText(), numLine));
                return null;
            } // конец if (operandWords.size()==1)
            if (! checkValue(operandWords, numLine)){ // если в операнде недопустимое выражение
                return null;
            }
            resultOperand.setTypeOperand (ParserOperand.TYPE_OPERAND_OPERATIONS);
            resultOperand.operations = new ArrayList<>();
            getListOperations (operandWords, resultOperand.operations, numLine);
           /* ParserOperations pars = new ParserOperations();
            pars.runOperations(resultOperand.operations, null);
            parserErrors.add(new ErrorParserString(0, "res " + Integer.toString(pars.getResult()), 21));*/
        } // конец if (max_prioritet==0)
        return resultOperand;
    } // конец метода loadOperand

    // вспомогательный метод выделяет и заполняет операнды из строки lexerString в список operands строки ParserString
    private void loadOperands (ArrayList<Lexer.LexerWord> lexerString, ParserString parserString, int numLine){
        parserString.operands = new ArrayList<>();
        OperandWords operandWords = new OperandWords();
        ParserOperand parserOperand;
        int countZPT = 0; // содержит кол-во запятых, разделяющих операнды
        for (int i=1; i<=lexerString.size(); i++){ // цикл по всем элементам lexerString, кроме первого (т.к. первый элемент это команда)
            if ((i==lexerString.size())||(lexerString.get(i).getToken()==Lexer.TOKEN_ZAPYATAYA)){ // если текущий элемент запятая, значит список operandWords содержит полный операнд
                countZPT++; // увеличиваем на 1 кол-во запятых
                if (countZPT >= ParserString.MAX_OPERANDS){ // если кол-во операндов больше предельно допустимого, заданного в MAX_OPERANDS
                    parserErrors.add (new ErrorParserString(ERROR_COUNT_OPERANDS, lexerString.get(i-1).getText(), numLine)); // записываем ошибку
                    parserString.setTypeCommand (ParserString.TYPE_NO_COMMAND); // удаляем команду из текущей строки, как будто она пустая, т.к. содержит ошибку
                    break;
                } // конец if (countZPT >= ParserString.MAX_OPERANDS)
                if (operandWords.count()==0){
                    parserErrors.add (new ErrorParserString(ERROR_NO_OPERAND, lexerString.get(i-1).getText(), numLine)); // записываем ошибку
                    parserString.setTypeCommand (ParserString.TYPE_NO_COMMAND); // удаляем команду из текущей строки, как будто она пустая, т.к. содержит ошибку
                    break;
                }
                parserOperand = loadOperand (operandWords, numLine);
                if (parserOperand==null){ // если обнаружена ошибка при выполнении loadOperand
                    parserString.setTypeCommand (ParserString.TYPE_NO_COMMAND); // удаляем команду из текущей строки, как будто она пустая, т.к. содержит ошибку
                    break;
                }
                parserString.operands.add (parserOperand);
                operandWords = new OperandWords();
            }else{ // если текущий элемент не запятая
                if (i<lexerString.size()){
                    operandWords.addWord (lexerString.get(i));
                }
            } // конец if (lexerString.get(i).getToken==Lexer.TOKEN_ZAPYATAYA)
        } // конец for (int i=1; i<lexerString.size(); i++)
    } // конец метода loadOperands

    // вспомогательный метод ищет в строке конструкции, состоящие из нескольких лексем, и заменяет их одной лексемой
    private void findParserConstructions (ArrayList<Lexer.LexerWord> lexerString){
        for (int i=0; i<lexerString.size()-1; i++){ // цикл по всем элементам, начиная с первого и заканчивая предпоследним
            switch (lexerString.get(i).getToken()){ // проверяем значение токена текущего элемента
                case Lexer.TOKEN_USERWORD: // если это пользовательское слово
                    if (lexerString.get(i+1).getToken()==Lexer.TOKEN_TWO_POINT){ // если после пользовательского слова следует двоеточие, значит это конструкция МЕТКА
                        lexerString.get(i).setToken (TOKEN_PARSER_LABEL);
                        lexerString.remove(i+1);
                    }
                    break;
                case Lexer.TOKEN_BIT_AND: // если это побитное И '&'
                    if (lexerString.get(i+1).getToken()==Lexer.TOKEN_BIT_AND){ // если токен следующего элемента тоже побитное И '&', значит это конструкция ЛОГИЧЕСКОЕ И
                        lexerString.get(i).setToken (TOKEN_PARSER_LOGIC_AND);
                        lexerString.get(i).setText(lexerString.get(i).getText() + lexerString.get(i+1).getText());
                        lexerString.remove(i+1);
                    }
                    break;
                case Lexer.TOKEN_BIT_OR: // если это побитное ИЛИ '|'
                    if (lexerString.get(i+1).getToken()==Lexer.TOKEN_BIT_OR){ // если токен следующего элемента тоже побитное ИЛИ '|', значит это конструкция ЛОГИЧЕСКОЕ ИЛИ
                        lexerString.get(i).setToken (TOKEN_PARSER_LOGIC_OR);
                        lexerString.get(i).setText(lexerString.get(i).getText() + lexerString.get(i+1).getText());
                        lexerString.remove(i+1);
                    }
                    break;
                case Lexer.TOKEN_LOGIC_NOT: // если это ЛОГИЧЕСКОЕ НЕ '!'
                    if (lexerString.get(i+1).getToken()==Lexer.TOKEN_RAVNO){ // если токен следующего элемента "знак равно" '=', значит это конструкция НЕ РАВНО
                        lexerString.get(i).setToken (TOKEN_PARSER_NOT_RAVNO);
                        lexerString.get(i).setText (lexerString.get(i).getText() + lexerString.get(i+1).getText());
                        lexerString.remove(i+1);
                    }
                    break;
                case Lexer.TOKEN_RAVNO: // если это знак равенства '="
                    if (lexerString.get(i+1).getToken()==Lexer.TOKEN_RAVNO){ // если токен следующего элемента тоже "равно" '=', значит это конструкция ЕСЛИ РАВНО
                        lexerString.get(i).setToken (TOKEN_PARSER_IF_RAVNO);
                        lexerString.get(i).setText (lexerString.get(i).getText() + lexerString.get(i+1).getText());
                        lexerString.remove(i+1);
                    }
                    break;
                case Lexer.TOKEN_BIG: // если это знак больше '>'
                    if (lexerString.get(i+1).getToken()==Lexer.TOKEN_RAVNO){ // если токен следующего элемента "знак равно" '=', значит это конструкция БОЛЬШЕ ИЛИ РАВНО
                        lexerString.get(i).setToken (TOKEN_PARSER_BIG_OR_RAVNO);
                        lexerString.get(i).setText (lexerString.get(i).getText() + lexerString.get(i+1).getText());
                        lexerString.remove(i+1);
                    }else{
                        if (lexerString.get(i+1).getToken()==Lexer.TOKEN_BIG){ // если токен следующего элемента "знак больше" '>', значит это конструкция СДВИГ ВПРАВО
                            lexerString.get(i).setToken (TOKEN_PARSER_SHIFT_RIGHT);
                            lexerString.get(i).setText (lexerString.get(i).getText() + lexerString.get(i+1).getText());
                            lexerString.remove(i+1);
                        }
                    }
                    break;
                case Lexer.TOKEN_LESS: // если это знак меньше '<'
                    if (lexerString.get(i+1).getToken()==Lexer.TOKEN_RAVNO){ // если токен следующего элемента "знак равно" '=', значит это конструкция МЕНЬШЕ ИЛИ РАВНО
                        lexerString.get(i).setToken (TOKEN_PARSER_LESS_OR_RAVNO);
                        lexerString.get(i).setText (lexerString.get(i).getText() + lexerString.get(i+1).getText());
                        lexerString.remove(i+1);
                    }else{
                        if (lexerString.get(i+1).getToken()==Lexer.TOKEN_LESS){ // если токен следующего элемента "знак меньше" '<', значит это конструкция СДВИГ ВЛЕВО
                            lexerString.get(i).setToken (TOKEN_PARSER_SHIFT_LEFT);
                            lexerString.get(i).setText (lexerString.get(i).getText() + lexerString.get(i+1).getText());
                            lexerString.remove(i+1);
                        }else{
                            if ((lexerString.size()>=3)&&(lexerString.get(i+1).getToken()==Lexer.TOKEN_USERWORD_WITH_POINT)&&(lexerString.get(i+2).getToken()==Lexer.TOKEN_BIG)){ // конструкция "<имяфайла.хх>"
                                lexerString.get(i).setToken (TOKEN_PARSER_INCLUDE);
                                lexerString.get(i).setText (lexerString.get(i+1).getText()); // в поле text записываем имя файла
                                lexerString.remove(i+2);
                                lexerString.remove(i+1);
                            }
                        }
                    }
                    break;
                case Lexer.TOKEN_OPERATION_PLUS: // если это знак плюс '+'
                    if ((i==1)||(lexerString.get(i-1).getToken()==Lexer.TOKEN_OPENED)){ // если это первый элемент в строке после команды или знаку '+' предшествует открытая скобка '('
                        lexerString.get(i).setToken (TOKEN_PARSER_PLUS); // меняем токен операции сложения на "унарный плюс"
                    }
                    break;
                case Lexer.TOKEN_OPERATION_MINUS: // если это знак минус '-'
                    if ((i==1)||(lexerString.get(i-1).getToken()==Lexer.TOKEN_OPENED)){ // если это первый элемент в строке после команды или знаку '-' предшествует открытая скобка '('
                        lexerString.get(i).setToken (TOKEN_PARSER_MINUS); // меняем токен операции вычитания на "унарный минус"
                    }
            } // конец switch (lexerString.get(i))
        } // конец for (int i=0; i<lexerString.size()-1; i++)
    } // конец метода findParserConstructions

} // конец класса Parser