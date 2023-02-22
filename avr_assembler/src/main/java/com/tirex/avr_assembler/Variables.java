package com.tirex.avr_assembler;

import java.util.ArrayList;

// класс будет содержать соотношения имен переменных и их значений
public class Variables{

    private ArrayList<Variable> vars = new ArrayList<>();

    // класс для хранения одной переменной: имени и ее значения
    public class Variable {
        public static final int TYPE_INT = 1; // переменная целочисленного типа
        public static final int TYPE_STR = 2; // переменная строкового типа (имя регистра)
        private String nameVar; // имя переменной
        private int typeVar; // тип переменной - число или строка
        private int valueInt; // значение переменной с целочисленным значением
        private String valueStr; // значение переменной со строковым значением (имена регистров)
        public void initVariable (String nameVar, int valueVar){
            this.nameVar = nameVar;
            typeVar = TYPE_INT;
            valueInt = valueVar;
        }
        public void initVariable (String nameVar, String valueVar){
            this.nameVar = nameVar;
            typeVar = TYPE_STR;
            this.valueStr = valueVar;
        }
        public String getNameVar(){
            return nameVar;
        }
        public int getType(){
            return typeVar;
        }
        public int getIntValue (){
            return valueInt;
        }
        public String getStrValue(){
            return valueStr;
        }
        public Variable createCopy(){
            Variable var = new Variable();
            var.nameVar = nameVar;
            var.typeVar = getType();
            switch (getType()){
                case TYPE_INT:
                    var.valueInt = getIntValue();
                    break;
                case TYPE_STR:
                    var.valueStr = getStrValue();
            }
            return var;
        }
    } // конец класса Variable

    // метод добавления числовой переменной в список. Если переменная с именем nameVar уже существует, возвращает false
    public boolean addVariable (String nameVar, int valueVar){
        boolean res = false;
        Variable var = new Variable();
        if (! isNameVar(nameVar)){
            var.initVariable (nameVar, valueVar);
            vars.add (var);
            res = true;
        }
        return res;
    } // конец addVariable

    // метод добавления строковой переменной в список. Если переменная с именем nameVar уже существует, возвращает false
    public boolean addVariable (String nameVar, String valueVar){
        boolean res = false;
        Variable var = new Variable();
        if (! isNameVar(nameVar)){
            var.initVariable (nameVar, valueVar);
            vars.add (var);
            res = true;
        }
        return res;
    } // конец addVariable

    // метод возвращает номер переменной в списке по ее имени. Если переменной в списке нет, то возвращает -1
    private int getNumVar (String nameVar){
        int res = -1;
        for (int i=0; i<vars.size(); i++){
            if (vars.get(i).getNameVar().equalsIgnoreCase(nameVar)){
                res = i;
                break;
            } // конец if (vars.get(i).getNameVar().equalsIgnoreCase(nameVar))
        } // конец for (int i=0; i<vars.size(); i++)
        return res;
    } // конец метода getNumVar

    // метод проверяет, есть ли в списке переменная с именем nameVar. Если есть - возвращает true
    public boolean isNameVar (String nameVar){
        boolean res = false;
        if (getNumVar(nameVar) >=0){
            res = true;
        }
        return res;
    } // конец isNameVar

    // метод проверяет наличие в списке переменной с именем nameVar числового типа
    public boolean isIntVar (String nameVar){
        boolean res = false;
        int num = getNumVar (nameVar);
        if ((num >= 0)&&(vars.get(num).getType()==Variable.TYPE_INT)){
            res = true;
        }
        return res;
    } // конец метода isIntVar

    // метод возвращает значение числовой переменной. Перед использованием нужно вызвать метод isIntVar, чтобы убедиться что переменная числовая
    public int getIntValue (String nameVar){
        int res = 0;
        int num = getNumVar (nameVar);
        if ((num >= 0)&&(vars.get(num).getType()==Variable.TYPE_INT)){
            res = vars.get(num).getIntValue();
        }
        return res;
    } // конец метода getIntValue

    // метод проверяет наличие в списке переменной с именем nameVar строкового типа
    public boolean isStrVar (String nameVar){
        boolean res = false;
        int num = getNumVar (nameVar);
        if ((num >= 0)&&(vars.get(num).getType()==Variable.TYPE_STR)){
            res = true;
        }
        return res;
    } // конец метода isStrVar

    // метод возвращает значение строковой переменной. Перед использованием нужно вызвать метод isStrVar, чтобы убедиться что переменная строковая
    public String getStrValue (String nameVar){
        String res=null;
        int num = getNumVar (nameVar);
        if ((num >= 0)&&(vars.get(num).getType()==Variable.TYPE_STR)){
            res = vars.get(num).getStrValue();
        }
        return res;
    } // конец метода getIntValue

    // метод удаления списка всех переменных
    public void clear(){
        vars.clear();
    } // конец clear

    // метод возвращает кол-во переменных в списке
    public int count (){
        return vars.size();
    } // конец count

    // метод возвращает значение типа Variable по имени переменной
    public Variable getVariable (String varName){ // возвращает значение типа Variable по имени переменной. Если такой переменной - нет возвращает null
        Variable res=null;
        int num = getNumVar(varName);
        if (num >=0){
            res = vars.get(num);
        }
        return res;
    } // конец метода getVariable

    // метод удаляет из списка переменную по имени
    public boolean remove (String nameVar){
        boolean res = false;
        int num = getNumVar (nameVar);
        if (num >= 0){
            vars.remove(num);
            res = true;
        }
        return res;
    } // конец метода remove

    // метод возвращает копию текущего списка переменных
    public Variables createCopy (){
        Variables newVars = new Variables();
        for (int i=0; i<count(); i++){
            newVars.vars.add (vars.get(i).createCopy());
        } // конец for (int i=0; i<count(); i++)
        return newVars;
    } // конец метода createCopy

    public void addVars (Variables variables){
        vars.addAll(variables.vars);
    }

} // конец класса Variables
