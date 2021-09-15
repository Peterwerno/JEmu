/*
 * Copyright (C) 2021 peter.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ansic.micro;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author peter
 */
public class SeikoUC2000Debugger extends SeikoUC2000 implements Debugger {
    
    public SeikoUC2000Debugger() {
        super();
    }
    
    public SeikoUC2000Debugger(Memory memoryBlock, IO IOBlock) {
        super(memoryBlock, IOBlock);
    }
    
    public SeikoUC2000Debugger(List<Memory> memoryBlocks, List<IO> IOBlocks) {
        super(memoryBlocks, IOBlocks);
    }

    @Override
    public long getCodeLength(long address) throws MemoryException, OpCodeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCode(long address) throws MemoryException, OpCodeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CodeAndLength getCodeAndLength(long address) throws MemoryException, OpCodeException {
        int opCode = (Byte.toUnsignedInt(this.readMemory8(address)) << 8) + (Byte.toUnsignedInt(this.readMemory8(address + 1)));
        int k;
        int d = ((opCode & 0x03E0) >> 5);
        int s = ((opCode & 0x001F));
        
        switch (opCode & 0xFC00) {
            case 0x0000:    // ADD Rd, Rs
                return new CodeAndLength(2, "ADD R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x0400:    // ADB Rd, Rs
                return new CodeAndLength(2, "ADB R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x0800:    // SUB Rd, Rs
                return new CodeAndLength(2, "SUB R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x0C00:    // SBB Rd, Rs
                return new CodeAndLength(2, "SBB R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x1000:    // ADI R, I
                return new CodeAndLength(2, "ADI R" + Integer.toString(d) + ", $" + Integer.toHexString(s >> 1));
                
            case 0x1400:    // ADBI R, I
                return new CodeAndLength(2, "ADBI R" + Integer.toString(d) + ", $" + Integer.toHexString(s >> 1));
                
            case 0x1800:    // SBI R, I
                return new CodeAndLength(2, "SBI R" + Integer.toString(d) + ", $" + Integer.toHexString(s >> 1));
                
            case 0x1C00:    // SBBI R, I
                return new CodeAndLength(2, "SBBI R" + Integer.toString(d) + ", $" + Integer.toHexString(s >> 1));
                
            case 0x2000:    // ADM Rd-Rd*, Rs-Rs*
                k = (s - d) % 8;
                if(k != 0) {
                    return new CodeAndLength(2, "ADM R" + Integer.toString(d) + "-R" + Integer.toString(d+k) + ", R" + Integer.toString(s-k) + "-R" + Integer.toString(s));
                }
                else
                    return new CodeAndLength(2, "ADM R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x2400:    // ADBM Rd-Rd*, Rs-Rs*
                k = (s - d) % 8;
                if(k != 0) {
                    return new CodeAndLength(2, "ADBM R" + Integer.toString(d) + "-R" + Integer.toString(d+k) + ", R" + Integer.toString(s-k) + "-R" + Integer.toString(s));
                }
                else
                    return new CodeAndLength(2, "ADBM R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x2800:    // SBM Rd-Rd*, Rs-Rs*
                k = (s - d) % 8;
                if(k != 0) {
                    return new CodeAndLength(2, "SBM R" + Integer.toString(d) + "-R" + Integer.toString(d+k) + ", R" + Integer.toString(s-k) + "-R" + Integer.toString(s));
                }
                else
                    return new CodeAndLength(2, "SBM R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x2C00:    // SBBM Rd-Rd*, Rs-Rs*
                k = (s - d) % 8;
                if(k != 0) {
                    return new CodeAndLength(2, "SBBM R" + Integer.toString(d) + "-R" + Integer.toString(d+k) + ", R" + Integer.toString(s-k) + "-R" + Integer.toString(s));
                }
                else
                    return new CodeAndLength(2, "SBBM R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x3000:    // CMP Rd, Rs
                return new CodeAndLength(2, "CMP R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x3400:    // CMPM Rd-Rd*, Rs-Rs*
                k = (s - d) % 8;
                if(k != 0) {
                    return new CodeAndLength(2, "CMPM R" + Integer.toString(d) + "-R" + Integer.toString(d+k) + ", R" + Integer.toString(s-k) + "-R" + Integer.toString(s));
                }
                else
                    return new CodeAndLength(2, "CMPM R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x3800:    // CMPI Rd, I
                return new CodeAndLength(2, "CMPI R" + Integer.toString(d) + ", $" + Integer.toHexString(s >> 1));
                
            case 0x3C00:    // LCRB b, LARB b (load current bank, load additional bank)
                if((opCode & 0xFE00) == 0x3C00) {
                    return new CodeAndLength(2, "LCRB " + Integer.toString((opCode & 0x0018) >>3));
                }
                else if((opCode & 0xFE00) == 0x3E00) {
                    return new CodeAndLength(2, "LARB " + Integer.toString((opCode & 0x0018) >>3));
                }
                else
                    throw new OpCodeException("OpCode " + opCode + " not supported");
    
            case 0x4000:    // ANDI Rd, I
                return new CodeAndLength(2, "ANDI R" + Integer.toString(d) + ", $" + Integer.toHexString(s >> 1));
                
            case 0x4400:    // ORI Rd, I
                return new CodeAndLength(2, "ORI R" + Integer.toString(d) + ", $" + Integer.toHexString(s >> 1));
                
            case 0x4800:    // XORI Rd, I
                return new CodeAndLength(2, "XORI R" + Integer.toString(d) + ", $" + Integer.toHexString(s >> 1));
                
            case 0x4C00:    // INC, INCB, DEC, DECB R, r
                switch (opCode & 0x0018) {
                    case 0x0000:
                        return new CodeAndLength(2, "INC R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                        
                    case 0x0008:
                        return new CodeAndLength(2, "INCB R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                        
                    case 0x0010:
                        return new CodeAndLength(2, "DEC R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                        
                    case 0x0018:
                        return new CodeAndLength(2, "DECB R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                        
                    default:
                        throw new OpCodeException("OpCode " + opCode + " not supported");
                }
                
            case 0x5000:    // RSHM, LSHM
                switch (opCode & 0x0008) {
                    case 0x0000:
                        return new CodeAndLength(2, "RSHM R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                        
                    case 0x0008:
                        return new CodeAndLength(2, "LSHM R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                        
                    default:
                        throw new OpCodeException("OpCode " + opCode + " not supported");
                }
                
            case 0x5400:    // IN R, S
                return new CodeAndLength(2, "IN R" + Integer.toString(d) + ", ($" + Integer.toHexString(s & 0x000F) + ")");
                
                // 0x5800 + 0x5C00 not identified yet
                
            case 0x6000:    // PSAM, PLAM
                switch (opCode & 0x0010) {
                    case 0x0000:
                        return new CodeAndLength(2, "PSAM R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                        
                    case 0x0010:
                        return new CodeAndLength(2, "PLAM R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                        
                    default:
                        throw new OpCodeException("OpCode " + opCode + " not supported");
                }
                
            case 0x6400:    // LDSM, STSM
                switch (opCode & 0x0008) {
                    case 0x0000:
                        return new CodeAndLength(2, "STSM R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                        
                    case 0x0008:
                        return new CodeAndLength(2, "LDSM R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                        
                    default:
                        throw new OpCodeException("OpCode " + opCode + " not supported");
                }
                
            case 0x6800:    // STLM R, r
                return new CodeAndLength(2, "STLM R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                
            case 0x6C00:    // STL R
                return new CodeAndLength(2, "STL R" + Integer.toString(d));
                
            case 0x7000:    // PSAI I
            case 0x7400:
                return new CodeAndLength(2, "PSAI $" + Integer.toHexString((opCode & 0x07FF)));
                
            case 0x7800:    // PLAI I
                return new CodeAndLength(2, "PLAI $" + Integer.toHexString((opCode & 0x03C0) >> 6) + Integer.toHexString(((opCode & 0x0020) >> 2) | (opCode & 0x0007)));
                
            case 0x7C00:    // STLI I
                // TODO: Some varints are not yet documented here!!
                return new CodeAndLength(2, "STLI $" + Integer.toHexString((opCode & 0x03C0) >> 6) + Integer.toHexString(((opCode & 0x0020) >> 2) | (opCode & 0x0007)));
                
            case 0x8000:    // MOV Rd, Rs
                return new CodeAndLength(2, "MOV R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x8400:    // MOVM Rd-Rd*, Rs-Rs*
                k = (s - d) % 8;
                if(k != 0) {
                    return new CodeAndLength(2, "MOVM R" + Integer.toString(d) + "-R" + Integer.toString(d+k) + ", R" + Integer.toString(s-k) + "-R" + Integer.toString(s));
                }
                else
                    return new CodeAndLength(2, "MOVM R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x8800:    // LDI R, I
                return new CodeAndLength(2, "LDI R" + Integer.toString(d) + ", $" + Integer.toHexString(s >> 1));
                
            case 0x8C00:    // CLRM R, r
                return new CodeAndLength(2, "CLRM R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                
            case 0x9000:    // MVAC E, R
                return new CodeAndLength(2, "MVAC " + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x9400:    // MVACM E, R
                k = (s - d) % 8;
                if(k != 0) {
                    return new CodeAndLength(2, "MVACM " + Integer.toString(d) + "-" + Integer.toString(d+k) + ", R" + Integer.toString(s-k) + "-R" + Integer.toString(s));
                }
                else
                    return new CodeAndLength(2, "MOVACM " + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x9800:    // MVCA R, E
                return new CodeAndLength(2, "MVCA R" + Integer.toString(d) + ", " + Integer.toString(s));
                
            case 0x9C00:    // MVCAM R, E
                k = (s - d) % 8;
                if(k != 0) {
                    return new CodeAndLength(2, "MVACM R" + Integer.toString(d) + "-R" + Integer.toString(d+k) + ", " + Integer.toString(s-k) + "-" + Integer.toString(s));
                }
                else
                    return new CodeAndLength(2, "MOVACM R" + Integer.toString(d) + ", " + Integer.toString(s));
                
                
            case 0xA000:    // CALL A
            case 0xA400:
            case 0xA800:
            case 0xAC00:
                return new CodeAndLength(2, "CALL $" + Integer.toHexString((opCode & 0x0FFF) << 1));
                
            case 0xB000:    // RET
                // Not all opcodes are identified yet
                return new CodeAndLength(2, "RET");
                
            case 0xB400:    // CPFJR R, a
                return new CodeAndLength(2, "CPFJR R" + Integer.toString(d) + ", $" + Integer.toHexString(s << 1));
                
            case 0xB800:    // IJMR R
                return new CodeAndLength(2, "IJMR R" + Integer.toString(d));
                
            case 0xBC00:    // NOP
                // Not all opcodes are identified yet
                return new CodeAndLength(2, "NOP");
                
            case 0xC000:    // JMP A
            case 0xC400:
            case 0xC800:
            case 0xCC00:
                return new CodeAndLength(2, "JMP $" + Integer.toHexString((opCode & 0x0FFF) << 1));
                
            case 0xD000:    // JZ A
                return new CodeAndLength(2, "JZ $" + (Integer.toHexString(((opCode & 0x03FF) << 1) + 0x1800)));
                
            case 0xD400:    // JNZ A
                return new CodeAndLength(2, "JNZ $" + (Integer.toHexString(((opCode & 0x03FF) << 1) + 0x1800)));
                
            case 0xD800:    // JC A
                return new CodeAndLength(2, "JC $" + (Integer.toHexString(((opCode & 0x03FF) << 1) + 0x1800)));
                
            case 0xDC00:    // JNC A
                return new CodeAndLength(2, "JNC $" + (Integer.toHexString(((opCode & 0x03FF) << 1) + 0x1800)));
                
            case 0xE000:    // BTJR R, I, a
            case 0xE400:
            case 0xE800:
            case 0xEC00:
                return new CodeAndLength(2, "BTJR R" + Integer.toString(d) + ", $" + Integer.toHexString((opCode & 0x0C00) >> 10) + ", $" + Integer.toHexString(s << 1));
                
            case 0xF000:    // CPJR R, I, a
            case 0xF400:
            case 0xF800:
            case 0xFC00:
                return new CodeAndLength(2, "BTJR R" + Integer.toString(d) + ", $" + Integer.toHexString((opCode & 0x0C00) >> 10) + ", $" + Integer.toHexString(s << 1));
                
            default:
                throw new OpCodeException("OpCode " + opCode + " not supported");
        }
    }

    @Override
    public byte[] translate(String mnemonic) throws SyntaxErrorException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns a list of all register names
     * 
     * @return all registers (List)
     */
    @Override
    public List<String> getRegisterNames() {
        ArrayList<String> retVal = new ArrayList<>();
        
        for(int bank=0; bank<4; bank++) {
            for(int reg=0; reg<32; reg++) {
                String regName = "R" + (char)(bank + 'A') + Integer.toString(reg);
                retVal.add(regName);
            }
        }
        
        retVal.add("PC");   // Program Counter
        retVal.add("F");    // Flags
        retVal.add("CB");   // Current Bank
        retVal.add("AB");   // Additional Bank
        retVal.add("SA");   // Memory Address
        retVal.add("SP");   // Stack Pointer
        
        return retVal;
    }

    /**
     * Changes a register Value
     * 
     * @param registerName (String) the register Name
     * @param registerValue (long) the new register value
     * @throws IllegalRegisterException if the register does not exist
     * @throws IllegalRegisterValueException if the value does not fit
     */
    @Override
    public void setRegisterValue(String registerName, long registerValue) throws IllegalRegisterException, IllegalRegisterValueException {
        switch (registerName) {
            case "PC":
                if((registerValue < -32768) || (registerValue > 65535))
                    throw new IllegalRegisterValueException("Value " + registerValue + " does not fit in program counter");
                this.regPC = (short)registerValue;
                break;
                
            case "CB":
                if((registerValue < 0) || (registerValue > 0x000F))
                    throw new IllegalRegisterValueException("Value " + registerValue + " does not fit in bank select register");
                this.regCurrentBank = (byte)registerValue;
                break;
                
            case "AB":
                if((registerValue < 0) || (registerValue > 0x000F))
                    throw new IllegalRegisterValueException("Value " + registerValue + " does not fit in bank select register");
                this.regAdditionalBank = (byte)registerValue;
                break;
                
            case "SA":
                if((registerValue < 0) || (registerValue > 0xFFFF))
                    throw new IllegalRegisterValueException("Value " + registerValue + " does not fit in SA register");
                this.regSA = (short)registerValue;
                break;
                
            case "SP":
                if((registerValue < 0) || (registerValue > 0xFFFF))
                    throw new IllegalRegisterValueException("Value " + registerValue + " does not fit in SP register");
                this.regSP = (short)registerValue;
                break;
                
                
            case "F":
                if((registerValue < 0) || (registerValue > 3))
                    throw new IllegalRegisterValueException("Value " + registerValue + " does not fit in flags register");
                this.regFlags = (byte)registerValue;
                
            default:
                if(registerName.length() < 3)
                    throw new IllegalRegisterException("Register " + registerName + " does not exist");
                
                // Check for Rbx (b=bank, x=register number)
                
                // get the bank number
                int bank = 0;
                switch (registerName.charAt(1)) {
                    case 'A':
                        bank = 0;
                        break;
                        
                    case 'B':
                        bank = 1;
                        break;
                        
                    case 'C':
                        bank = 2;
                        break;
                        
                    case 'D':
                        bank = 3;
                        break;
                        
                    default:
                        throw new IllegalRegisterException("Register " + registerName + " does not exist");
                }
                
                // get the register number
                try {
                    int regNum = Integer.parseInt(registerName.substring(2));
                    if((regNum < 0) || (regNum > 31))
                        throw new IllegalRegisterException("Register " + registerName + " does not exist");
                    
                    if((registerValue < -8) || (registerValue > 15)) 
                        throw new IllegalRegisterValueException("Value " + registerValue + " does not fit in register " + registerName);
                    
                    this.registers[bank][regNum] = (byte)(registerValue & 0x0F);
                }
                catch (NumberFormatException ex) {
                    throw new IllegalRegisterException("Register " + registerName + " does not exist");
                }
        }
    }

    /**
     * Returns the value of a register
     * 
     * @param registerName (String) the register name
     * @return the value of the register (long)
     * @throws IllegalRegisterException 
     */
    @Override
    public long getRegisterValue(String registerName) throws IllegalRegisterException {
        switch (registerName) {
            case "PC":
                return this.regPC;
                
            case "F":
                return this.regFlags;
                
            case "CB":
                return this.regCurrentBank;
                
            case "AB":
                return this.regAdditionalBank;
                
            case "SA":
                return this.regSA;
                
            case "SP":
                return this.regSP;
                
            default:
                if(registerName.length() < 3)
                    throw new IllegalRegisterException("Register " + registerName + " does not exist");
                
                // Check for Rbx (b=bank, x=register number)
                
                // get the bank number
                int bank = 0;
                switch (registerName.charAt(1)) {
                    case 'A':
                        bank = 0;
                        break;
                        
                    case 'B':
                        bank = 1;
                        break;
                        
                    case 'C':
                        bank = 2;
                        break;
                        
                    case 'D':
                        bank = 3;
                        break;
                        
                    default:
                        throw new IllegalRegisterException("Register " + registerName + " does not exist");
                }
                
                // get the register number
                try {
                    int regNum = Integer.parseInt(registerName.substring(2));
                    if((regNum < 0) || (regNum > 31))
                        throw new IllegalRegisterException("Register " + registerName + " does not exist");
                    
                    return this.registers[bank][regNum];
                }
                catch (NumberFormatException ex) {
                    throw new IllegalRegisterException("Register " + registerName + " does not exist");
                }
                
        }
    }
    
    /**
     * Returns the program counter
     * 
     * @return the program counter register
     */
    @Override
    public long getProgramCounter() {
        return Short.toUnsignedLong(this.regPC);
    }

    /**
     * Returns the size of the register
     * 
     * @param registerName (String) the name of the register
     * @return the size in bits (int)
     * @throws IllegalRegisterException if the register does not exist 
     */
    @Override
    public int getRegisterSize(String registerName) throws IllegalRegisterException {
        switch (registerName) {
            case "PC":
            case "SA":
                return 16;
                
            case "F":
            case "CB":
            case "AB":
            case "SP":
                return 4;
                
            default:
                if(registerName.length() < 3)
                    throw new IllegalRegisterException("Register " + registerName + " does not exist");
                
                // bank between a and d?
                if((registerName.charAt(1) < 'A') || (registerName.charAt(1) > 'D'))
                    throw new IllegalRegisterException("Register " + registerName + " does not exist");
                
                // register number correct?
                try {
                    int regNum = Integer.parseInt(registerName.substring(2));
                    
                    if((regNum < 0) || (regNum > 31))
                        throw new IllegalRegisterException("Register " + registerName + " does not exist");
                    
                    return 4;
                }
                catch (NumberFormatException ex) {
                    throw new IllegalRegisterException("Register " + registerName + " does not exist");
                }
                    
        }
    }

    /**
     * Returns a single byte of memory
     * 
     * @param address (long) the address
     * @return the content (byte)
     * @throws MemoryException 
     */
    @Override
    public byte readMemoryByte(long address) throws MemoryException {
        return this.readMemory8(address);
    }
    
    /**
     * Sets a new program counter value
     * 
     * @param programCounter (long) the new address
     * @throws IllegalRegisterValueException if the address is out of range
     */
    @Override
    public void setProgramCounter(long programCounter) throws IllegalRegisterValueException {
        if((programCounter < -32768) || (programCounter >= 65535))
            throw new IllegalRegisterValueException("Value " + programCounter + " too large to fit in program counter");
        
        this.regPC = (short)programCounter;
    }
}
