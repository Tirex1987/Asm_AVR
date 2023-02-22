package com.tirex.avr_assembler;

public class Avr_registers{

    public final int REG_X = 32;
    public final int REG_Y = 33;
    public final int REG_Z = 34;
    public final int REG_PC = 35;

    // метод возвращает номер регистра по его имени. Для r0-r31 - номера 0-31, для X возвращает 32, Y - 33, Z - 34, PC - 35.
    public short getNumRegister (String registerName){
        short res = -1;
        for (short i=0; i<=31; i++){
            if (registerName.equalsIgnoreCase ("r"+String.valueOf(i))){
                res = i;
                break;
            } // конец if (registerName.equalsIgnoreCase ("r"+String.valueOf(i)))
        } // конец for (short i=0; i<=31; i++)
        if (res<0){
            if (registerName.equalsIgnoreCase("x")){
                res = (short) 32;
            } else {
                if (registerName.equalsIgnoreCase("y")){
                    res = (short) 33;
                } else {
                    if (registerName.equalsIgnoreCase("z")){
                        res = (short) 34;
                    } else {
                        if (registerName.equalsIgnoreCase("pc")){
                            res = (short) 35;
                        } // конец if (registerName.equalsIgnoreCase("pc"))
                    } // конец if (registerName.equalsIgnoreCase("z")){
                } // конец if (registerName.equalsIgnoreCase("y"))
            } // конец if (registerName.equalsIgnoreCase("x"))
        } // конец if (res<0)
        return res;
    } // конец метода getNumRegister

    // возвращает true, если строка registerName является регистром
    public boolean isRegister (String registerName){
        boolean res = false;
        if (getNumRegister(registerName)>=0){
            res = true;
        } // конец if (getNumRegister(registerName)>=0)
        return res;
    } // конец метода isRegister

    // метод возвращает true, если registerName является регистром из диапазона r0-r31
    public boolean isReg_r0_r31 (String registerName){
        boolean res = false;
        short num = getNumRegister(registerName);
        if ((num>=0)&&(num<=31)){
            res = true;
        } // конец if ((num>=0)&&(num<=31))
        return res;
    } // конец метода isReg_r0_r31

} // конец класса Avr_registers