package com.tirex.avr_assembler;

enum Avr_instructions{

    ADC ((short) 0x1C00),
    ADD ((short) 0x0C00),
    AND ((short) 0x2000),
    CP ((short) 0x1400),
    CPC ((short) 0x0400),
    CPSE ((short) 0x1000),
    MOV ((short) 0x2C00),
    MUL ((short) 0x9C00),
    OR ((short) 0x2800),
    SBC ((short) 0x0800),
    SUB ((short) 0x1800),
    EOR ((short) 0x2400),

    ADIW ((short) 0x9600),
    SBIW ((short) 0x9700),

    RJMP ((short) 0xC000),
    RCALL ((short) 0xD000),

    ASR ((short) 0x9405),
    COM ((short) 0x9400),
    DEC  ((short) 0x940A),
    INC ((short) 0x9403),
    LSR ((short) 0x9406),
    NEG ((short) 0x9401),
    POP ((short) 0x900F),
    PUSH ((short) 0x920F),
    ROR ((short) 0x9407),
    SWAP ((short) 0x9402),
    LD ((short) 0x8000),
    ST ((short) 0x8200),
    LPM ((short) 0x9004), // для инструкции с двумя аргументами, для инструкции без аргументов - 0x95C8
    ELPM ((short) 0x9006), // для инструкции с двумя аргументами, для инструкции без аргументов - 0x95D8

    BCLR ((short) 0x9488),
    BSET ((short) 0x9408),

    BLD ((short) 0xF800),
    BST ((short) 0xFA00),
    SBRC ((short) 0xFC00),
    SBRS ((short) 0xFE00),

    BRBS ((short) 0xF000),
    BRBC ((short) 0xF400),

    NOP ((short) 0x0000),
    IJMP ((short) 0x9409),
    RET ((short) 0x9508),
    RETI ((short) 0x9518),
    SLEEP ((short) 0x9588),
    WDR ((short) 0x95A8),
    // LPM ((short) 0x95C8), // имя дублируется
    SPM ((short) 0x95E8), // для инструкции без операндов, для ATxmega новый опкод 0x95F8 - SPM Z+
    ICALL ((short) 0x9509),
    // ELPM ((short) 0x95D8), // имя дублируется

    IN ((short) 0xB000),
    OUT ((short) 0xB800),

    MULSU ((short) 0x0300),
    FMUL ((short) 0x0308),
    FMULS ((short) 0x0380),
    FMULSU ((short) 0x0388),

    MOVW ((short) 0x0100),

    MULS ((short) 0x0200),

    CPI ((short) 0x3000),
    SBCI ((short) 0x4000),
    SUBI ((short) 0x5000),
    ORI ((short) 0x6000),
    ANDI ((short) 0x7000),
    LDI ((short) 0xE000),

    CBI ((short) 0x9800),
    SBIC ((short) 0x9900),
    SBI ((short) 0x9A00),
    SBIS ((short) 0x9B00),

    LDD ((short) 0x8000), // для (LDD Rd, Z+q), для инструкции (LDD Rd, Y+q) - опкод 0x8008
    STD ((short) 0x8200), // для (STD Rd, Z+q), для инструкции (STD Rd, Y+q) - опкод 0x8208

    JMP ((short) 0x940C), // инструкция 4 байта, младшее слово - 0x0000
    CALL ((short) 0x940E), // инструкция 4 байта, младшее слово - 0x0000

    STS ((short) 0x9200), // инструкция 4 байта, младшее слово - 0x0000
    LDS ((short) 0x9000), // инструкция 4 байта, младшее слово - 0x0000

    // мнемонические варианты команды BRBC
    BRCC ((short) 0xF400),
    BRSH ((short) 0xF400),
    BRNE ((short) 0xF401),
    BRPL ((short) 0xF402),
    BRVC ((short) 0xF403),
    BRGE ((short) 0xF404),
    BRHC ((short) 0xF405),
    BRTC ((short) 0xF406),
    BRID ((short) 0xF407),
    // мнемонические варианты команды BRBS
    BRCS ((short) 0xF000),
    BRLO ((short) 0xF000),
    BREQ ((short) 0xF001),
    BRMI ((short) 0xF002),
    BRVS ((short) 0xF003),
    BRLT ((short) 0xF004),
    BRHS ((short) 0xF005),
    BRTS ((short) 0xF006),
    BRIE ((short) 0xF007),
    // мнемонические варианты команды BCLR
    CLC ((short) 0x9488),
    CLZ ((short) 0x9498),
    CLN ((short) 0x94A8),
    CLV ((short) 0x94B8),
    CLS ((short) 0x94C8),
    CLH ((short) 0x94D8),
    CLT ((short) 0x94E8),
    CLI ((short) 0x94F8),
    // мнемонические варианты команды BSET
    SEC ((short) 0x9408),
    SEZ ((short) 0x9418),
    SEN ((short) 0x9428),
    SEV ((short) 0x9438),
    SES ((short) 0x9448),
    SEH ((short) 0x9458),
    SET ((short) 0x9468),
    SEI ((short) 0x9478),
    // мнемонический вариант команды AND
    TST ((short) 0x2000),    // tst Rd - аналог and Rd, Rd
    // мнемонический вариант команды ANDI
    CBR ((short) 0x7000),   // cbr Rd, k8 - аналог andi Rd, ($FF - k8)
    // мнемонический вариант команды ORI
    SBR ((short) 0x6000),    // sbr Rd, k8 - аналог ori Rd, k8
    // мнемонический вариант команды EOR
    CLR ((short) 0x2400),    // clr Rd - аналог eor Rd, Rd
    // мнемонический вариант команды ADD
    LSL ((short) 0x0C00),    // lsl Rd - аналог add Rd, Rd
    // мнемонический вариант команды ADC
    ROL ((short) 0x1C00),    // rol - аналог adc Rd, Rd

    SER ((short) 0xE000),    // ser Rd - аналог ldi Rd, $FF
    EIJMP ((short) 0x9419), // без операндов (поодерживают МК с минимум 128кб флеш памяти)
    EICALL ((short) 0x9519), // без операндов (поодерживают МК с минимум 128кб флеш памяти)
    BREAK ((short) 0x9598), // без операндов
    DES ((short) 0x940B), // опкод определен для ATxmega (операнд - число от 0 (940B) до 15 (94FB))
    XCH ((short) 0x9204), // xch Z, Rd (0<=d<=31)
    LAS ((short) 0x9205), // las Z, Rd (0<=d<=31)
    LAC ((short) 0x9206), // // lac Z, Rd (0<=d<=31)
    LAT ((short) 0x9207); // lat Z, Rd (0<=d<=31)

    private short opcode; // опкод инструкции

    Avr_instructions (short opcode){ // конструктор
        this.opcode = opcode;
    }

    public short getOpcode (){
        return opcode;
    }

    public static boolean isInstruction (String instructionName){
        boolean res = false;
        Avr_instructions [ ] arrayInstructions = Avr_instructions.values();
        for (Avr_instructions instruction : arrayInstructions){
            if (instructionName.equalsIgnoreCase(instruction.toString())){
                res = true;
                break;
            } // if (instructionName.equalsIgnoreCase(instruction.toString()))
        } // for (Avr_instructions instruction : arrayInstructions)
        return res;
    } // конец метода isInstruction

} // конец класса Avr_instructions
