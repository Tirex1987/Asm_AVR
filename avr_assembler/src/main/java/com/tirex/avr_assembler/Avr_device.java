package com.tirex.avr_assembler;

public class Avr_device{

    private int lengthPC; // разрядность счетчика команд PC (2, 3 или 4 байта)
    private int flashEnd; // значение верхнего адреса памяти программ (в 2-байтных словах)
    private int sramStart; // значение младшего адреса ОЗУ, доступного для хранения данных
    private int sramSize; // объем ОЗУ в байтах
    //private int sramEnd; // значение верхнего адреса внутреннего ОЗУ (рассчитывается как sramStart+sramSize-1 )
    private int eepromSize; // объем EEPROM в байтах
    private int countPorts; // кол-во портов ввода-вывода

    public void setLengthPC (int lengthPC){
        this.lengthPC = lengthPC;
    } // конец метода setLengthPC

    public int getLengthPC (){
        return lengthPC;
    } // конец метода getLengtgPC

    public void setFlashEnd (int flashEnd){
        this.flashEnd = flashEnd;
    } // конец метода setFlashEnd

    public int getFlashEnd (){
        return flashEnd;
    } // конец метода getFlashEnd

    public void setSramStart (int sramStart){
        this.sramStart = sramStart;
    } // конец метода setSramStart

    public int getSramStart (){
        return sramStart;
    } // конец метода getSramStart

    public void setSramSize (int sramSize){
        this.sramSize = sramSize;
    } // конец метода setSramSize

    public int getSramSize (){
        return sramSize;
    } // конец метода getSramSize

    public void setEepromSize (int eepromSize){
        this.eepromSize = eepromSize;
    } // конец метода setEepromSize

    public int getEepromSize (){
        return eepromSize;
    } // конец метода getEepromSize

    public void setCountPorts (int countPorts){
        this.countPorts = countPorts;
    } // конец метода setCountPorts

    public int getCountPorts (){
        return countPorts;
    } // конец метода getCountPorts

} // конец класса Avr_device