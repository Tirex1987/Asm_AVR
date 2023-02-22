package com.tirex.avr_assembler;

import java.util.ArrayList;

public class Lexer {
    public static final int TOKEN_STRING = 1; // токен "строка" (выражение в кавычках)
    public static final int TOKEN_CHAR = 2; // токен "символ" (символ в апострофах)
    public static final int TOKEN_INSTRUCTION = 3; // токен "инструкция ассемблера"
    public static final int TOKEN_DIRECTIVE = 4; // токен "директива ассемблера"
    public static final int TOKEN_NUMBER = 5; //токен "число"
    public static final int TOKEN_USERWORD = 6; //токен "пользовательское слово"
    public static final int TOKEN_KEYWORD = 7; // токен "ключевое слово"
    public static final int TOKEN_FUNCTION = 8; // токен "функция"
    public static final int TOKEN_SYMBOL = 9; // токен включает в себя символы , ( ) : + - * & ! ~ = %
    public static final int TOKEN_ZAPYATAYA = 10; // токен "запятая"
    //    public static final int TOKEN_POINT = 11; // токен "точка"
    public static final int TOKEN_OPENED = 12; // токен "открывающая скобка" '('
    public static final int TOKEN_CLOSED = 13; // токен "закрывающая скобка" ')'
    public static final int TOKEN_OPERATION_PLUS = 14; // токен "плюс" '+'
    public static final int TOKEN_OPERATION_MINUS = 15; // токен "минус" '-'
    public static final int TOKEN_MULTI = 16; // токен "умножение" '*'
    public static final int TOKEN_BIT_AND = 17; // токен "логическое И" '&'
    public static final int TOKEN_TWO_POINT = 18; // токен "двоеточие" ':'
    public static final int TOKEN_LOGIC_NOT = 19; // токен "сравнение с нулем" '!'
    public static final int TOKEN_BIT_NOT = 20; // токен "логическое НЕ" '~'
    public static final int TOKEN_PERCENT = 21; // токен "проценты" '%'
    public static final int TOKEN_RAVNO = 22; // токег "равно" '='
    public static final int TOKEN_DIV = 23; // токен "деление" '/'
    public static final int TOKEN_USERWORD_WITH_POINT = 24; // токен "пользовательское слово с точкой"
    public static final int TOKEN_BIT_OR = 25; // токен "побитное ИЛИ" '|'
    public static final int TOKEN_BIT_OR_NOT = 26; // токен "исключающее ИЛИ" '^'
    public static final int TOKEN_BIG = 27; // токен "больше чем" '>'
    public static final int TOKEN_LESS = 28; // токен "меньше чем" <'

    private final int ERROR_EMPTY_CHAR = 1; // в апострофах нет символа
    private final int ERROR_MANY_CHAR = 2; // в апострофах более одного символа
    private final int ERROR_UNKNOWN_VALUE = 3; // неизвестное выражение, не соответствует ни одному токену
    private final int ERROR_NO_SPACE = 4; // нет разделителя между токенами (запятой или пробела)
    private final int ERROR_UNKNOWN_SYMBOL = 5; // неизвестный символ, не соответствует ни одному токену
    private final int ERROR_NOT_CLOSED_STRING = 6; // нет парной кавычки в строке
    private final int ERROR_NOT_CLOSED_CHAR = 7; // нет парного апострофа в строке

    private final String START_COMMENTMULTILINE = "/*";
    private final String END_COMMENTMULTILINE = "*/";
    // lexerText будет содержать результат выполнения лексера - список токенов
    public ArrayList <ArrayList <LexerWord>> lexerText = new ArrayList<ArrayList<LexerWord>>();
    // список будет содержать ошибки, возникшие при обработке строк лексером
    private ArrayList <LexerError> lexerErrors = new ArrayList<>();

    private boolean commentMultiLines; // для метода lineOfListingToLexer, содержит true если был открыт многострочный комментарий
    private ArrayList<LexerWord> lexerLine; // для метода lineOfListingToLexer, содержит результирующую строку, обработанную лексером
    private Avr_registers regs = new Avr_registers(); // для метода isKeyword, чтобы определить, является ли лексема ключевым словом (регистром)

    public Lexer(ArrayList<String> listing){
        textToLexer(listing);
    }

    public Lexer(){}

    // Объявляем класс LexerWord
    public class LexerWord{
        private int token;
        private String text;
        private int number;
        public void setToken (int token){
            this.token = token;
        }
        public int getToken (){
            return this.token;
        }
        public void setText (String text){
            this.text = text;
        }
        public String getText (){
            return this.text;
        }
        public void setNumber (int number){
            this.number = number;
        }
        public int getNumber (){
            return this.number;
        }
    }  // конец класса LexerWord

    // объявляем класс LexerError
    private class LexerError{
        private int numLine; // хранит номер строки, в которой обнаружена ошибка
        private int numError; // хранит код ошибки
        private String errorWord; // хранит слово, в котором обнаружена ошибка
        public LexerError (int numLine, int numError, String errorWord){
            this.numLine = numLine;
            this.numError = numError;
            this.errorWord = errorWord;
        }
        public void setNumLine (int numLine){
            this.numLine = numLine;
        }
        public int getNumLine(){
            return numLine;
        }
        public void setNumError (int numError){
            this.numError = numError;
        }
        public int getNumError(){
            return numError;
        }
    } // конец класса LexerError

    // метод возвращает кол-во обнаруженных ошибок
    public int countErrors (){
        return lexerErrors.size();
    }

    public int getErrorNumLine (int number){ return lexerErrors.get(number).getNumLine(); }

    public int getErrorNumError (int number){ return lexerErrors.get(number).getNumError(); };

    public String gerErrorWord (int number){ return lexerErrors.get(number).errorWord; };

    // метод возвращает текст ошибки по номеру, 1 - на русском языке, 2 - на английском
    public String textOfError (int numError, int language){
        String res = "";
        switch(language){
            case 1: // русский язык
                switch(numError){
                    case ERROR_EMPTY_CHAR:
                        res = "Ожидается символ в апострофах";
                        break;
                    case ERROR_MANY_CHAR:
                        res = "В апострофах должно быть не более 1 символа";
                        break;
                    case ERROR_UNKNOWN_VALUE:
                        res = "Неизвестное выражение";
                        break;
                    case ERROR_NO_SPACE:
                        res = "Ожидается разделитель";
                        break;
                    case ERROR_UNKNOWN_SYMBOL:
                        res = "Недопустимый символ";
                        break;
                    case ERROR_NOT_CLOSED_STRING:
                        res = "Ожидается \"";
                        break;
                    case ERROR_NOT_CLOSED_CHAR:
                        res = "Ожидается '";
                        break;
                }
                break;
            case 2: //английский язык
                switch(numError){
                    case ERROR_EMPTY_CHAR:
                        res = "Expected the symbol in apostrophes";
                        break;
                    case ERROR_MANY_CHAR:
                        res = "Apostrophes must contain no more than 1 character";
                        break;
                    case ERROR_UNKNOWN_VALUE:
                        res = "Unknown expression";
                        break;
                    case ERROR_NO_SPACE:
                        res = "Separator expected";
                        break;
                    case ERROR_UNKNOWN_SYMBOL:
                        res = "Invalid character";
                        break;
                    case ERROR_NOT_CLOSED_STRING:
                        res = "Expected \"";
                        break;
                    case ERROR_NOT_CLOSED_CHAR:
                        res = "Expected '";
                        break;
                }
        }
        return res;
    } // конец метода textOfError

    public String getFullTextError(int numInLexerErrors, int language){
        int numError = lexerErrors.get(numInLexerErrors).getNumError();
        String res = textOfError(numError, language);
        switch (numError){
            case ERROR_MANY_CHAR:
            case ERROR_UNKNOWN_VALUE:
            case ERROR_NO_SPACE:
            case ERROR_UNKNOWN_SYMBOL:
                res += ": "+lexerErrors.get(numInLexerErrors).errorWord;
        }
        return res;
    }

    // вспомогательный метод удаления из строки count символов начиная с символа startPos
    private String deleteFromString(String line, int startPos, int count){
        String resultString;
        resultString = line.substring(0, startPos) +' '+ line.substring(startPos+count);
        return resultString;
    }

    // вспомогательный метод определения находится ли открытие многострочного комментария в кавычках или апострофах
    // если комментарий открывается в кавычках или апострофах, то возвращает false
    private boolean noStringValue(String line, int endPos) {
        int pom1, pom2;
        int startPos = 0;
        boolean res = true;
        for (;;) {
            pom1 = line.indexOf('"', startPos);
            pom2 = line.indexOf('\'', startPos);
            if (pom1==-1 && pom2==-1 || startPos>=endPos){
                break;
            }
            if ((pom1 < endPos)&&(pom1 != -1) || (pom2 < endPos)&&(pom2 != -1)) {
                if ((pom2 == -1)|| pom1 < pom2 && pom1 != -1) { // если раньше в строке стоит кавычка, чем апостроф
                    pom2 = line.indexOf('"', pom1+1); // ищем в строке закрывающую кавычку
                    if (pom2 > endPos || pom2 == -1) { // если парная кавычка находится за пределами endPos или ее нет в строке
                        res = false;
                        break;
                    } else { // если парная кавычка в пределах endPos
                        startPos = pom2+1;
                    }
                }else{
                    if ((pom1 == -1)|| pom2 < pom1) { // если в строке апостроф стоит раньше, чем кавычка
                        pom1 = line.indexOf('\'', pom2 + 1); // ищем в строке закрывающий апостроф
                        if (pom1 > endPos || pom1 == -1) { // если парный апостроф за пределами endPos или его нет в строке
                            res = false;
                            break;
                        } else { // если парный апостроф в пределах endPos
                            startPos = pom1 + 1;
                        }
                    } // конец if ((pom1 == -1)|| pom2 < pom1 && pom2 !=-1)
                } // конец для if (pom1 < pom2 && pom1 != -1)
            }else{
                break;
            }// конец для if ((pom1 < endPos)&&(pom1 != -1) || (pom2 < endPos)&&(pom2 != -1))
        }
        return res;
    }

    // метод построчной обработки строк листинга лексером (строки листинга передаются в этот метод строго поочередно по одной). Результирующую строку записывает в lexerLine
    // При каждом вызове обнуляет список ошибок, поэтому после отработки список содержит ошибки только текущей строки
    public void lineOfListingToLexer (String lineInListing){
        int vspom, vspom1; // вспомогательная переменная
        lexerErrors = new ArrayList<>();
        if (commentMultiLines){ // проверяем не открыт ли многострочный комментарий
            vspom = lineInListing.indexOf(END_COMMENTMULTILINE);
            if (vspom>=0){
                lineInListing = deleteFromString(lineInListing, 0, vspom+2);
                commentMultiLines = false; // сбрасываем признак многострочного комментария
            } else{ // если в текущей строке комментарий не заканчивается
                lineInListing = ""; // устанавливаем исходную строку пустой, чтобы вернуть пустую строку
            }
        }
        for (;;){ // цикл для удаления всех пар символов многострочного комментария из строки
            vspom = lineInListing.indexOf(START_COMMENTMULTILINE);
            if (vspom>=0){
                vspom1 = lineInListing.indexOf(END_COMMENTMULTILINE, vspom);
                if (vspom1<0){ // если конец комментария в строке не найден
                    lineInListing = deleteFromString(lineInListing, vspom, lineInListing.length()-vspom); // удаляем из строки все символы от vspom до конца строки
                    commentMultiLines = true;
                    break; //выходим из цикла
                }else{
                    lineInListing = deleteFromString(lineInListing, vspom, vspom1-vspom+END_COMMENTMULTILINE.length());
                } // конец if (vspom1<0)
            }else{
                break;
            }// конец if (vspom>=0)
        } // конец цикла for(;;)
        lineInListing = lineInListing.trim(); // удаляем пробелы в начале и конце строки
        if (lineInListing.length()==0){ // если осталась пустая строка
            lexerLine = null; // возвращаем пустую строку
        } else {
            lexerLine = stringToLexer(lineInListing, 0);
        } //конец if (lineInString.length()==0)
    } // конец метода lineOfListingToLexer

    // метод возвращает значение lexerLine после вызова метода lineOfListingToLexer
    public ArrayList<LexerWord> getLexerLine (){
        return lexerLine;
    } // конец метода getLexerLine

    // процедура обработки всех строк программы
    private void textToLexer (ArrayList<String> listing){
        boolean commentMultiLine = false; // признак наличия многострочного комментария
        String lineInListing;
        ArrayList<LexerWord> resultLexerString;
        int vspom, vspom1; // вспомогательная переменная
        int startPos;
        for (int i=0; i<listing.size(); i++){ // цикл по всем строкам в листинге
            lineInListing = listing.get(i);
            if (commentMultiLine){ // проверяем не открыт ли многострочный комментарий
                vspom = lineInListing.indexOf(END_COMMENTMULTILINE);
                if (vspom>=0){
                    lineInListing = deleteFromString(lineInListing, 0, vspom+2);
                    commentMultiLine = false; // сбрасываем признак многострочного комментария
                } else{ // если в текущей строке комментарий не заканчивается
                    lexerText.add(null); // добавляем в lexerText пустую строку
                    continue; // переходим к следующей строке
                }
            }
            startPos = 0;
            for (;;){ // цикл для удаления всех пар символов многострочного комментария из строки
                vspom = lineInListing.indexOf(START_COMMENTMULTILINE, startPos);
                if (vspom>=0){
                    if (! noStringValue(lineInListing, vspom)){
                        startPos = vspom+1;
                        continue;
                    }
                    vspom1 = lineInListing.indexOf(END_COMMENTMULTILINE, vspom);
                    if (vspom1<0){ // если конец комментария в строке не найден
                        lineInListing = deleteFromString(lineInListing, vspom, lineInListing.length()-vspom); // удаляем из строки все символы от vspom до конца строки
                        commentMultiLine = true;
                        break; //выходим из цикла
                    }else{ // конец if (vspom1<0)
                        lineInListing = deleteFromString(lineInListing, vspom, vspom1-vspom+END_COMMENTMULTILINE.length());
                    }
                }else{ // конец if (vspom>=0)
                    break;
                }
            } // конец цикла for(;;)
            lineInListing = lineInListing.trim(); // удаляем пробелы в начале и конце строки
            if (lineInListing.length()==0){ // если осталась пустая строка
                lexerText.add(null); // добавляем в lexerText пустую строку
                continue; // переходим к следующей строке в листинге
            } //конец if (lineInString.length()==0)
            resultLexerString = stringToLexer(lineInListing, i);
            lexerText.add(resultLexerString);
        } //конец цикла for (i=0; i<listing.size(); i++)
    } //конец метода textToLexer

    // метод перевода одной строки листинга в набор токенов (список экземпляров LexerWord)
    private ArrayList<LexerWord> stringToLexer(String line, int numLine){
        String nextWord="";
        int  priznak = 0;
        char ch;
        LexerWord vspomLexer;
        ArrayList <LexerWord> resultLexer = new ArrayList<>();
        for (int i=0; i<line.length(); i++){ // цикл по всем символам строки
            ch = line.charAt(i);
            switch (priznak){
                case 1: //priznak =1// признак открытых кавычек
                    if (ch=='"'){ // если текущий символ кавычка
                        vspomLexer = wordToLexer(nextWord, TOKEN_STRING);
                        resultLexer.add(vspomLexer);
                        nextWord = "";
                        priznak = 0;
                        continue;
                    } else{// конец if (line.CharAt(i)=='"') , если символ не кавычка
                        nextWord += ch; // добавляем к nextWord текущий символ
                        continue;
                    } // конец case 1  (priznak =1)
                case 2: // priznak = 2, признак открытого апострофа
                    if (ch=='\''){ // если текущий символ апостроф
                        if (nextWord.length()==1){ // если в апострофах только один символ
                            vspomLexer = wordToLexer(nextWord, TOKEN_CHAR);
                            resultLexer.add(vspomLexer);
                            nextWord = "";
                            priznak = 0;
                            continue;
                        }else{ // иначе для if (nextWord.length()==1)
                            if (nextWord.length()==0){ // ессли в апострофах нет символа
                                lexerErrors.add(new LexerError(numLine, ERROR_EMPTY_CHAR, nextWord));
                                nextWord = "";
                                continue; //return resultLexer;
                            }else{ // если в апострофах больше одного символа
                                lexerErrors.add(new LexerError(numLine, ERROR_MANY_CHAR, nextWord));
                                nextWord = "";
                                continue; //return resultLexer;
                            }
                        } // конец if (nextWord.length()==1)
                    } else{ // иначе для if (ch=='''), если символ не апостроф
                        nextWord += ch; // добавляем к nextWord текущий символ
                        continue;
                    } // конец для if (ch==''')
            } // конец switch (priznak)
            switch (ch){
                case ' ': // если текущий символ пробел
                    if (nextWord.isEmpty()) { // если в nextWord пустая строка
                        continue;
                    } else{ // если nextWord не пустая строка
                        vspomLexer = wordToLexer(nextWord, 0);
                        if (vspomLexer.token==0){ // если токен не удалось определить
                            lexerErrors.add(new LexerError(numLine, ERROR_UNKNOWN_VALUE, nextWord));
                            nextWord = "";
                            continue; //return resultLexer;
                        }
                        resultLexer.add(vspomLexer);
                        nextWord = "";
                        continue;
                    } // конец if (nextWord.isEmpty())
                case '"': // если текущий символ кавычка
                    if (! nextWord.isEmpty()) { // если в nextWord не пустая строка
                        vspomLexer = wordToLexer(nextWord, 0);
                        if (vspomLexer.token == 0) { // если токен не удалось определить
                            lexerErrors.add(new LexerError(numLine, ERROR_UNKNOWN_VALUE, nextWord));
                        } else {
                            resultLexer.add(vspomLexer);
                        }
                        lexerErrors.add(new LexerError(numLine, ERROR_NO_SPACE, nextWord));
                        nextWord = "";
                    }
                    //return resultLexer; // переходим к следующей строке
                    //   }else{ // если в nextWord пустая строка
                    priznak = 1; // устанавливаем признак открытых кавычек
                    continue; // переходим к следующему символу
                    //} // конец для if (! nextWord.isEmpty())
                case '\'': // если текущий символ апостроф
                    if (! nextWord.isEmpty()) { // если nextWord не пустая строка
                        vspomLexer = wordToLexer(nextWord, 0);
                        if (vspomLexer.token == 0) { // если токен не удалось определить
                            lexerErrors.add(new LexerError(numLine, ERROR_UNKNOWN_VALUE, nextWord));
                        } else {
                            resultLexer.add(vspomLexer);
                        }
                        lexerErrors.add(new LexerError(numLine, ERROR_NO_SPACE, nextWord));
                        nextWord = "";
                    }
                    //return resultLexer; // переходим к следующей строке
                    //}else{ // если nextWord пустая строка
                    priznak = 2; // устанавливаем признак открытого апострофа
                    continue; // переходим к следующему символу
                    //  } // конец для if (! nextWord.isEmpty())
                case ',':
                case '(':
                case ')':
                case '+':
                case '-':
                case '*':
                case '&':
                case ':':
                case '!':
                case '~':
                case '%':
                case '|':
                case '^':
                case '>':
                case '<':
                case '=': // если текущий символ один из перечисленных
                    if (! nextWord.isEmpty()){ // если в nextWord не пустая строка
                        vspomLexer = wordToLexer(nextWord, 0);
                        if (vspomLexer.token==0){ // если токен не удалось определить
                            lexerErrors.add(new LexerError (numLine, ERROR_UNKNOWN_VALUE, nextWord));
                            nextWord = "";
                            continue; //return resultLexer;
                        } // конец if (vspomLexer==null)
                        resultLexer.add(vspomLexer);
                        nextWord = "";
                    } // конец if (! nextWord.isEmpty())
                    vspomLexer = wordToLexer(Character.toString(ch), TOKEN_SYMBOL);
                    if (vspomLexer.token != 0 && vspomLexer.token != TOKEN_SYMBOL){
                        resultLexer.add(vspomLexer);
                        continue;
                    }else{
                        lexerErrors.add(new LexerError(numLine, ERROR_UNKNOWN_SYMBOL, nextWord));
                        nextWord = "";
                        continue; //return resultLexer;
                    } // конец if (vspomLexer.token != 0 && vspomLexer.token != TOKEN_SYMBOL)
                case '.': // если текущий символ точка
                    nextWord += ch;
                    continue;
                case '/': // если текущий символ слэш
                    if (! nextWord.isEmpty()){ // если nextWord не пустая строка
                        vspomLexer = wordToLexer(nextWord, 0);
                        if (vspomLexer.token==0){
                            lexerErrors.add(new LexerError(numLine, ERROR_UNKNOWN_VALUE, nextWord));
                            nextWord = "";
                            continue; //return resultLexer;
                        }else{
                            resultLexer.add(vspomLexer);
                            nextWord = "";
                        } // конец if (vspomLexer.token==0)
                    } // конец if (! nextWord.isEmpty())
                    if (i==line.length()-1){ // если текущий символ последний в строке
                        vspomLexer = wordToLexer("/", TOKEN_DIV);
                        resultLexer.add (vspomLexer);
                        continue;
                    }else{ // если текущий символ не последний в строке
                        if (line.charAt(i+1)=='/'){ // проверяем значение следующего символа
                            return resultLexer; // далее следует комментарий, переходим к следующей строке
                        }else{ // следующий символ не '/'
                            vspomLexer = wordToLexer("/", TOKEN_DIV);
                            resultLexer.add(vspomLexer);
                            continue;
                        } // конец if (line.charAt(i+1)=='/')
                    } // конец if (i==line.length()-1)
                case ';': // текущий символ "точка с запятой" - начало комментария в строке
                    if (! nextWord.isEmpty()){ // если nextWord не пустая строка
                        vspomLexer = wordToLexer(nextWord, 0);
                        if (vspomLexer.token==0){
                            lexerErrors.add(new LexerError(numLine, ERROR_UNKNOWN_VALUE, nextWord));
                            //nextWord = "";
                            //return resultLexer;
                        }else{
                            resultLexer.add(vspomLexer);
                            //nextWord = "";
                        } // конец if (vspomLexer.token==0)
                    } // конец if (! nextWord.isEmpty())
                    return resultLexer; // переходим к следующей строке, т.к. далее следует комментарий
                default: // символ не соответствует указанным выше
                    if ((ch>='a' && ch<='z')||(ch>='A' && ch<='Z')||(ch>='0' && ch<='9')||(ch=='$')||(ch=='_')){ // если символ соответствует алфавиту
                        nextWord += ch;
                        //continue;
                    }else{
                        lexerErrors.add(new LexerError(numLine, ERROR_UNKNOWN_SYMBOL, Character.toString(ch)));
                        return resultLexer;
                    } // конец if ((ch>='a' && ch<='z')||(ch>='A' && ch<='Z')||(ch>='0' && ch<='9')||(ch=='$')||(ch=='_'))
            } // конец switch (ch)
        } // конец цикла for (i=0; i<line.length(); i++)
        if (priznak==1){
            lexerErrors.add(new LexerError(numLine, ERROR_NOT_CLOSED_STRING, nextWord));
        }else {
            if (priznak == 2) {
                lexerErrors.add(new LexerError(numLine, ERROR_NOT_CLOSED_CHAR, nextWord));
            } else {
                if (!nextWord.isEmpty()) {
                    vspomLexer = wordToLexer(nextWord, 0);
                    if (vspomLexer.token==0){
                        lexerErrors.add(new LexerError(numLine, ERROR_UNKNOWN_VALUE, nextWord));
                    }else{
                        resultLexer.add(vspomLexer);
                    } // конец if (vspomLexer.token==0)
                }
            }
        }
        return resultLexer;
    } // конец метода stringToLexer

    // метод перевода одного слова или символа в экземпляр LexerWord. token содержит предполагаемый токен.
    // если token=0, то определяет тип токена.
    // если токен не удалось определить, то возвращает result равное null
    private LexerWord wordToLexer (String line, int token){
        LexerWord result = new LexerWord();
        switch (token){
            case TOKEN_STRING:
                result.setToken(token);
                result.setText(line);
                break;
            case TOKEN_CHAR:
                result.setToken(token);
                result.setText(line);
                result.setNumber((int) line.charAt(0)); // записываем в number код символа
                break;
            case TOKEN_DIV:
                result.setToken(token);
                result.setText(line);
                break;
            case TOKEN_SYMBOL:
                switch (line.charAt(0)){
                    case ',':
                        token = TOKEN_ZAPYATAYA;
                        break;
                    case '(':
                        token = TOKEN_OPENED;
                        break;
                    case ')':
                        token = TOKEN_CLOSED;
                        break;
                    case '+':
                        token = TOKEN_OPERATION_PLUS;
                        break;
                    case '-':
                        token = TOKEN_OPERATION_MINUS;
                        break;
                    case '*':
                        token = TOKEN_MULTI;
                        break;
                    case '&':
                        token = TOKEN_BIT_AND;
                        break;
                    case ':':
                        token = TOKEN_TWO_POINT;
                        break;
                    case '!':
                        token = TOKEN_LOGIC_NOT;
                        break;
                    case '~':
                        token = TOKEN_BIT_NOT;
                        break;
                    case '%':
                        token = TOKEN_PERCENT;
                        break;
                    case '=':
                        token = TOKEN_RAVNO;
                        break;
                    case '|':
                        token = TOKEN_BIT_OR;
                        break;
                    case '^':
                        token = TOKEN_BIT_OR_NOT;
                        break;
                    case '>':
                        token = TOKEN_BIG;
                        break;
                    case '<':
                        token = TOKEN_LESS;
                        break;
                }
                result.setToken(token);
                result.setText(line);
                break;
            default : // если токен не определен или равен нулю
                if (isInstruction(line)){ // проверяем является ли line инструкцией
                    result.setToken(TOKEN_INSTRUCTION);
                    result.setText(line);
                } else{ // если line не инструкция
                    if (isDirective(line)){ // проверяем является ли line директивой
                        line = line.substring(1);
                        result.setToken(TOKEN_DIRECTIVE);
                        result.setText(line);
                    } else{ // если line не директива
                        if (isKeyWord(line)){ // проверяем является ли line ключевым словом
                            result.setToken(TOKEN_KEYWORD);
                            result.setText(line);
                        }else{ // если line не ключевое слово
                            if (isNumber(line)>0){ // проверяем является ли line числом
                                result.setToken(TOKEN_NUMBER);
                                result.setText(line);
                                result.setNumber(stringToNumber(line));
                            }else{ // если line не число
                                if (isFunction(line)){ // проверяем является ли line функцией
                                    result.setToken(TOKEN_FUNCTION);
                                    result.setText(line);
                                }else{ // если line не функция
                                    if (isUserWord(line)){ // если line соответствует требованиям пользовательского слова
                                        result.setToken(TOKEN_USERWORD);
                                        result.setText(line);
                                    }else{ // если line не соответствует пользовательскому слову
                                        if (isUserWordWithPoint(line)){
                                            result.setToken(TOKEN_USERWORD_WITH_POINT);
                                            result.setText(line);
                                        } // конец if (isUserWordWithPoint(line))
                                    } // конец if (isUserWord(line))
                                } // конец if (isFunction(line))
                            }// конец if (isNumber(line)>0)
                        } // конец if (isKeyWord(line))
                    } // конец if(isDirective(line))
                } // конец if (isInstruction(line))
        } // конец switch (token)
        return result;
    } // конец метода wordToLexer

    // вспомогательный метод определения является ли слово line инструкцией
    private boolean isInstruction(String line){
        boolean res=false;
        //определяем, входит ли слово line в список инструкций, если да - возвращаем true
        if (Avr_instructions.isInstruction(line)){
            res = true;
        }
        return res;
    } // конец метода isInstruction

    // вспомогательный метод определения является ли слово line директивой
    private boolean isDirective(String line){
        boolean res=false;
        // определяем, входит ли слово line в список директив, если да - возвращаем true
        if ((line.charAt(0)=='.')&&(line.length()>1)) {
            res = Avr_directives.isDirective(line.substring(1));
        }
        return res;
    } // конец метода isDirective

    // вспомогательный метод, проверяет состоит ли строка из символов шестнадцатеричной системы
    private boolean is16number (String line){
        boolean is16 = true;
        char ch;
        if (line.isEmpty() || line.length()>4){
            is16 = false;
        }
        for (int i=0; i<line.length(); i++){
            ch = line.charAt(i);
            if (!((ch>='0' && ch<='9')||(ch>='A' && ch<='F')||(ch>='a' && ch<='f'))){
                is16=false;
                break;
            }
        }
        return is16;
    } // конец метода is16number

    // вспомогательный метод, проверяет состоит ли строка из цифр двоичной системы
    private boolean is2number (String  line){
        boolean is2 = true;
        char ch;
        if (line.length() != 8){
            is2 = false;
        }
        for (int i=0; i<line.length(); i++){
            ch = line.charAt(i);
            if (!(ch=='0' || ch=='1')){
                is2 = false;
                break;
            }
        }
        return is2;
    } // конец метода is2number

    // вспомогательный метод, проверяет состоит ли строка из цифр восьмеричной системы
    private boolean is8number(String line){
        boolean is8 = true;
        char ch;
        if (line.isEmpty() || line.length()>8){
            is8 = false;
        }
        for (int i=0; i<line.length(); i++){
            ch = line.charAt(i);
            if (!(ch>='0' && ch<='7')){
                is8 = false;
                break;
            }
        }
        return is8;
    } // конец метода is8number

    // вспомогательный метод, проверяет соответствуют ли символы строки цифрам десятеричной системы
    private boolean is10number(String line){
        boolean is10 = true;
        char ch;
        if (line.isEmpty() || line.length()>5){
            is10 = false;
        }
        for (int i=0; i<line.length(); i++){
            ch = line.charAt(i);
            if (!(ch>='0' && ch<='9')){
                is10 = false;
                break;
            }
        }
        return is10;
    } // конец метода is10number

    // вспомогательный метод определения, является ли line числом
    // возвращаемые значения:
    // 0 - строка не является числом
    // 2 - число в двоичной системе
    // 8 - в восьмеричной системе
    // 10 - в десятеричной системе
    // 16 - в шестнадцатеричной системе
    private int isNumber(String line){
        int isNum = 0;
        if (line.charAt(0)=='$'){ // число в шестнадцатеричной системе
            if (is16number(line.substring(1))){
                isNum = 16;
            }
        } // конец if (line.charAt(0)=='$')
        if (line.charAt(0)=='0'){
            if (line.length()>2){
                if (line.charAt(1)=='x' || line.charAt(1)=='X'){ // число в шестнадцатеричной системе
                    if (is16number(line.substring(2))){
                        isNum = 16;
                    }
                } // конец if (line.charAt(1)=='x' || line.charAt(1)=='X')
                if (line.charAt(1)=='b' || line.charAt(1)=='B'){ // число в двоичной системе
                    if (is2number(line.substring(2))){
                        isNum = 2;
                    }
                } // конец if (line.charAt(1)=='b' || line.charAt(1)=='B')
            } // конец if (line.length()>2)
            if (isNum==0){ // если число не двоичное и не шестнадцатеричное
                if (is8number(line.substring(1))){ // число в восьмеричной системе
                    isNum = 8;
                }
            }
        } // конец if (line.charAt(0)=='0')
        if (isNum==0){ // если система счисления еще не определена
            if (is10number(line)){ // проверяем соответствует ли число десятеричной системе
                isNum = 10;
            }
        } // конец if (isNum==0)
        return isNum;
    }// конец метода isNumber

    // вспомогательный метод перевода строки в число. Если строка не число, то возвращает ноль
    private int stringToNumber (String line){
        int res = 0;
        try {
            switch (isNumber(line)) {
                case 2:
                    res = Integer.parseInt(line.substring(2), 2);
                    break;
                case 8:
                    res = Integer.parseInt(line, 8);
                    break;
                case 10:
                    res = Integer.parseInt(line);
                    break;
                case 16:
                    if (line.charAt(0) == '$') {
                        res = Integer.parseInt(line.substring(1), 16);
                    } else {
                        res = Integer.parseInt(line.substring(2), 16);
                    }
                    break;
                default:
                    res = 0;
            }
        }catch(Exception e){
            res = 0;
        }
        return res;
    } // конец метода stringToNumber

    // вспомогательный метод, проверяет соответствует ли строка line требованиям пользовательского слова
    private boolean isUserWord(String line){
        boolean res = true;
        boolean znak_ = false;
        char ch;
        if (line.isEmpty()){
            res = false;
        }else{ // если строка не пустая, то проверяем первый символ
            ch = line.charAt(0);
            if (ch=='_'){
                znak_ = true;
            }else{
                if (!((ch>='a' && ch<='z')||(ch>='A' && ch<='Z'))){
                    res = false;
                }
            } // конец if (ch=='_')
        } // конец if (line.isEmpty())
        if (res){ // если строка не пустая и первый символ '_' или буква латинского алфавита
            for (int i=1; i<line.length(); i++){ // проверяем каждый символ строки
                ch = line.charAt(i);
                if (znak_ && ch != '_' && !(ch>='0' && ch<='9')){ // проверяем, чтобы слово состояло не только из символов '_'
                    znak_ = false;
                }
                if (!((ch>='a' && ch<='z')||(ch>='A' && ch<='Z')||(ch>='0' && ch<='9')||(ch=='_'))){
                    res = false;
                    break;
                }
            } // конец for
        } // конец if (res)
        if (znak_){ // если в слове только символы нижнего подчеркивания
            res = false;
        }
        return res;
    } // конец метода isUserWord

    // вспомогательный метод определяет является ли line ключевым словом
    private boolean isKeyWord  (String line){
        boolean res = false;
        // определяет есть ли строка line в списке ключевых слов, если да - возвращает true
        if (regs.isRegister(line)){
            res = true;
        }
        return res;
    } // конец метода isKeyWord

    // вспомогательный метод определения является ли line функцией
    private boolean isFunction(String line){
        boolean res;
        // определяет есть ли строка line в списке функций, если да - возвращает true
        res = Avr_functions.isFunction(line);
        return res;
    } // конец метода isFunction

    // вспомогательный метод определения является ли line пользовательским словом с точкой
    private boolean isUserWordWithPoint(String line){
        boolean res = true;
        boolean znak_ = false;
        char ch;
        if (line.isEmpty()){
            res = false;
        }else{ // если строка не пустая, то проверяем первый символ
            ch = line.charAt(0);
            if (ch=='_' || ch=='.'){
                znak_ = true;
            }else{
                if (!((ch>='a' && ch<='z')||(ch>='A' && ch<='Z'))){
                    res = false;
                }
            } // конец if (ch=='_')
        } // конец if (line.isEmpty())
        if (res){ // если строка не пустая и первый символ '_' или буква латинского алфавита
            for (int i=1; i<line.length(); i++){ // проверяем каждый символ строки
                ch = line.charAt(i);
                if (znak_ && ch != '_' && !(ch>='0' && ch<='9')&& !(ch=='.')){ // проверяем, чтобы слово состояло не только из символов '_'
                    znak_ = false;
                }
                if (!((ch>='a' && ch<='z')||(ch>='A' && ch<='Z')||(ch>='0' && ch<='9')||(ch=='_')||(ch=='.'))){
                    res = false;
                    break;
                }
            } // конец for
        } // конец if (res)
        if (znak_){ // если в слове только символы нижнего подчеркивания
            res = false;
        }
        return res;
    } // конец метода isUserWord

}
