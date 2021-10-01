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
package org.jemu.micro;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class imlements an emulator for the Z80 CPU
 * 
 * @author peter
 */
public class Z80 implements CPU {
    public static byte FLAG_S = (byte)0x80;
    public static byte FLAG_Z = (byte)0x40;
    public static byte FLAG_H = (byte)0x10;
    public static byte FLAG_PV = (byte)0x04;
    public static byte FLAG_N = (byte)0x02;
    public static byte FLAG_C = (byte)0x01;
    
    public static byte INT_MODE_0 = (byte)0x00;
    public static byte INT_MODE_1 = (byte)0x01;
    public static byte INT_MODE_2 = (byte)0x02;
    
    byte regA, regF, regB, regC, regD, regE, regH, regL;
    byte regA2, regF2, regB2, regC2, regD2, regE2, regH2, regL2;
    byte regI, regR;
    short regIX, regIY, regSP, regPC;
    List<Memory> memoryBlocks;
    List<IO> IOBlocks;
    
    byte intMode;
    boolean intReq;
    boolean interruptsEnabled;
    byte intReqNumber;
    
    public Z80() {
        // Initialize the processor by setting all register to 0
        // 8 bit
        this.regA = 0;
        this.regB = 0;
        this.regC = 0;
        this.regD = 0;
        this.regE = 0;
        this.regF = 0;
        this.regH = 0;
        this.regL = 0;
        this.regA2 = 0;
        this.regB2 = 0;
        this.regC2 = 0;
        this.regD2 = 0;
        this.regE2 = 0;
        this.regF2 = 0;
        this.regH2 = 0;
        this.regL2 = 0;
        this.regI = 0;
        this.regR = 0;
        
        // 16 bit
        this.regIX = 0;
        this.regIY = 0;
        this.regSP = 0;
        this.regPC = 0;
        
        // interrupt stuff
        this.intReq = false;
        this.intReqNumber = 0;
        this.interruptsEnabled = true;
        this.intMode = INT_MODE_0;
    }
    
    public Z80(Memory memoryBlock, IO IOBlock) {
        this();
        
        this.memoryBlocks = new ArrayList<>();
        this.IOBlocks = new ArrayList<>();
        
        this.memoryBlocks.add(memoryBlock);
        this.IOBlocks.add(IOBlock);
    }
    
    public Z80(List<Memory> memoryBlocks, List<IO> IOBlocks) {
        this();
        this.memoryBlocks = memoryBlocks;
        this.IOBlocks = IOBlocks;
    }
    
    /**
     * Write an (8 bit) byte to memory
     * 
     * @param address (long) the memory address
     * @param value (byte) the new value
     * @throws MemoryException if the address is not in the memory range
     */
    protected void writeMemory8(long address, byte value) throws MemoryException {
        Iterator<Memory> memIterator = memoryBlocks.iterator();
        
        while(memIterator.hasNext()) {
            Memory mem = memIterator.next();
            
            try {
                mem.setByte(address, value);
                return;
            }
            catch (MemoryException ex) {
                // Try next memory block
            }
        }
        
        throw new MemoryException("No memory at address " + Long.toHexString(address));
    }
    
    /**
     * Write a (16 bit) short to memory
     * 
     * @param address (long) the memory address
     * @param value (short) the new value
     * @throws MemoryException if the address is not in the memory range
     */
    protected void writeMemory16(long address, short value) throws MemoryException {
        byte lowByte = (byte)(value & 0xFF);
        byte highByte = (byte)((value >> 8) & 0xFF);
        
        writeMemory8(address, lowByte);
        writeMemory8(address+1, highByte);
    }
    
    /**
     * Read an (8 bit) byte from memory
     * 
     * @param address (long) the memory address
     * @return the memory content (byte)
     * @throws MemoryException if the address is not in the memory range
     */
    protected byte readMemory8(long address) throws MemoryException {
        Iterator<Memory> memIterator = memoryBlocks.iterator();
        
        while(memIterator.hasNext()) {
            Memory mem = memIterator.next();
            
            try {
                return mem.getByte(address);
            }
            catch (MemoryException ex) {
                // Try next memory block
            }
        }
        
        throw new MemoryException("No memory at address " + Long.toHexString(address));
    }
    
    /**
     * Read a (16 bit) short from memory
     * 
     * @param address (long) the memory address
     * @return the memory content (short)
     * @throws MemoryException if the address is not in the memory range
     */
    protected short readMemory16(long address) throws MemoryException {
        byte lowByte = readMemory8(address);
        byte highByte = readMemory8(address + 1);
        
        return (short)(Byte.toUnsignedInt(lowByte) + Byte.toUnsignedInt(highByte) << 8);
    }
    
    /**
     * Write an (8 bit) byte to io
     * 
     * @param port (long) the port number
     * @param value (byte) the new value
     * @throws MemoryException if the address is not in the memory range
     */
    protected void writeIO8(long port, byte value) throws MemoryException {
        Iterator<IO> IOIterator = IOBlocks.iterator();
        
        while(IOIterator.hasNext()) {
            IO io = IOIterator.next();
            
            try {
                io.setByte(port, value);
                return;
            }
            catch (MemoryException ex) {
                // Try next memory block
            }
        }
        
        throw new MemoryException("No IO at port " + Long.toHexString(port));
    }
    
    /**
     * Read an (8 bit) byte from IO
     * 
     * @param port (long) the port number
     * @return the memory content (byte)
     * @throws MemoryException if the address is not in the memory range
     */
    protected byte readIO8(long port) throws MemoryException {
        Iterator<IO> IOIterator = IOBlocks.iterator();
        
        while(IOIterator.hasNext()) {
            IO io = IOIterator.next();
            
            try {
                return io.getByte(port);
            }
            catch (MemoryException ex) {
                // Try next memory block
            }
        }
        
        throw new MemoryException("No IO at port " + Long.toHexString(port));
    }
    
    /**
     * Sets a single bit in the flags register
     * 
     * @param flagNum (byte) the flag (see definition above)
     * @throws OpCodeException if there was a problem
     */
    protected void setFlag(byte flagNum) throws OpCodeException {
        this.regF |= flagNum;
    }
    
    /**
     * Clears a single bit in the flags register
     * 
     * @param flagNum (byte) the flag (see definition above)
     * @throws OpCodeException if there was a problem
     */
    protected void clearFlag(byte flagNum) throws OpCodeException {
        this.regF &= (0xFF - flagNum);
    }
    
    /**
     * Sets or clears a flag based on the set parameter
     * 
     * @param flagNum (byte) the flag (see definition above
     * @param set (boolean) whether to set or clear
     * @throws OpCodeException if there was a problem
     */
    protected void setFlag(byte flagNum, boolean set) throws OpCodeException {
        if(set)
            setFlag(flagNum);
        else
            clearFlag(flagNum);
    }
    
    /**
     * Returns whether or not a certain flag is set
     * 
     * @param flagNum (byte) the flag (see definition above)
     * @return whether or not this flag is set (boolean)
     * @throws OpCodeException if there was a problem
     */
    protected boolean checkFlag(byte flagNum) throws OpCodeException {
        return (this.regF & flagNum) == flagNum;
    }
    
    /**
     * Returns a value from a register identified by its register number that 
     * is 0 = B, 1 = C, ... 7 = A  and
     *    8 = B', 9 = C' ... 15 = A'
     * 
     * @param registerNum (byte) the register number
     * @return the value of the register
     * @throws OpCodeException if the register number does not exist
     */
    protected byte getRegister8(byte registerNum) throws OpCodeException {
        switch (registerNum) {
            case 0x00:
                return this.regB;
                
            case 0x01:
                return this.regC;
                
            case 0x02:
                return this.regD;
                
            case 0x03:
                return this.regE;
                
            case 0x04:
                return this.regH;
                
            case 0x05:
                return this.regL;
                
            case 0x07:
                return this.regA;
                
            case 0x08:
                return this.regB2;
                
            case 0x09:
                return this.regC2;
                
            case 0x0A:
                return this.regD2;
                
            case 0x0B:
                return this.regE2;
                
            case 0x0C:
                return this.regH2;
                
            case 0x0D:
                return this.regL2;
                
            case 0x0F:
                return this.regA2;
            default:
                throw new OpCodeException("Register number " + registerNum + " does not exist!");
        }
    }
    
    /**
     * Returns a value from a 16-bit register identified by its register number
     * that is 0 = BC, 1 = DE, 2 = HL, 3 = SP
     * 
     * @param registerNum (byte) the register number
     * @return the value of the register
     * @throws OpCodeException if the register number does not exist
     */
    protected short getRegister16(byte registerNum) throws OpCodeException {
        byte lowByte;
        byte highByte;
        
        switch (registerNum) {
            case 0x00:  // reg BC
                lowByte = this.regC;
                highByte = this.regB;
                break;
                
            case 0x01:  // reg DE
                lowByte = this.regE;
                highByte = this.regD;
                break;
                
            case 0x02:  // reg HL
                lowByte = this.regL;
                highByte = this.regH;
                break;
                
            case 0x03:  // reg SP
                return this.regSP;
                
            default:
                throw new OpCodeException("16-bit register number " + registerNum + " does not exist!");
        }
        
        return (short)(Byte.toUnsignedInt(lowByte) + (Byte.toUnsignedInt(highByte) << 8));
    }
    
    /**
     * Returns a value from a 16-bit register identified by its register number
     * that is 0 = BC, 1 = DE, 2 = IX, 3 = SP
     * 
     * @param registerNum (byte) the register number
     * @return the value of the register
     * @throws OpCodeException if the register number does not exist
     */
    protected short getRegister16_IX(byte registerNum) throws OpCodeException {
        byte lowByte;
        byte highByte;
        
        switch (registerNum) {
            case 0x00:  // reg BC
                lowByte = this.regC;
                highByte = this.regB;
                break;
                
            case 0x01:  // reg DE
                lowByte = this.regE;
                highByte = this.regD;
                break;
                
            case 0x02:  // reg IX
                return this.regIX;
                
            case 0x03:  // reg SP
                return this.regSP;
                
            default:
                throw new OpCodeException("16-bit register number " + registerNum + " does not exist!");
        }
        
        return (short)(Byte.toUnsignedInt(lowByte) + (Byte.toUnsignedInt(highByte) << 8));
    }
        
    /**
     * Returns a value from a 16-bit register identified by its register number
     * that is 0 = BC, 1 = DE, 2 = IY, 3 = SP
     * 
     * @param registerNum (byte) the register number
     * @return the value of the register
     * @throws OpCodeException if the register number does not exist
     */
    protected short getRegister16_IY(byte registerNum) throws OpCodeException {
        byte lowByte;
        byte highByte;
        
        switch (registerNum) {
            case 0x00:  // reg BC
                lowByte = this.regC;
                highByte = this.regB;
                break;
                
            case 0x01:  // reg DE
                lowByte = this.regE;
                highByte = this.regD;
                break;
                
            case 0x02:  // reg IY
                return this.regIY;
                
            case 0x03:  // reg SP
                return this.regSP;
                
            default:
                throw new OpCodeException("16-bit register number " + registerNum + " does not exist!");
        }
        
        return (short)(Byte.toUnsignedInt(lowByte) + (Byte.toUnsignedInt(highByte) << 8));
    }
    
    /**
     * Sets the value of a register identified by its regiser number that is
     * 0 = B, 1 = C, ... 7 = A
     * 
     * @param registerNum (byte) the register number
     * @param value (byte) the value
     * @throws OpCodeException if the register number is not supported
     */
    public void setRegister8(byte registerNum, byte value) throws OpCodeException {
        switch (registerNum) {
            case 0x00:
                this.regB = value;
                break;
                
            case 0x01:
                this.regC = value;
                break;
                
            case 0x02:
                this.regD = value;
                break;
                
            case 0x03:
                this.regE = value;
                break;
                
            case 0x04:
                this.regH = value;
                break;
                
            case 0x05:
                this.regL = value;
                break;
                
            case 0x07:
                this.regA = value;
                break;

            default:
                throw new OpCodeException("Register number " + registerNum + " does not exist!");
        }
    }
    
    public void setRegister16(byte registerNum, short value) throws OpCodeException {
        byte highByte = (byte)((Short.toUnsignedInt(value) >> 8) & 0xFF);
        byte lowByte = (byte)(value & 0xFF);
        
        switch (registerNum) {
            case 0x00:  // reg BC
                this.regB = highByte;
                this.regC = lowByte;
                break;
                
            case 0x01:  // reg DE
                this.regD = highByte;
                this.regE = lowByte;
                break;
                
            case 0x02:  // reg HL
                this.regH = highByte;
                this.regL = lowByte;
                break;
                
            case 0x03:  // reg SP
                this.regSP = value;
                break;
                
            default:
                throw new OpCodeException("16 bit register number " + registerNum + " does not exist!");
        }
    }
    
    /**
     * Operation "LD" on an 8 bit value.
     * 
     * @param input (byte) the input value
     * @return the output value
     */
    protected byte opLD8(byte input) {
        return input;
    }
    
    /**
     * Operation "LD" on a 16 bit value.
     * 
     * @param input (short) the input value
     * @return the output value
     */
    protected short opLD16(short input) {
        return input;
    }
    
    /**
     * Operation "INC" on an 8 bit value.
     * Increments the input value by 1 and sets the status flags appropriately.
     * 
     * @param input (byte) the input value
     * @return the output value
     * @throws OpCodeException if the flag does not exist
     */
    protected byte opINC8(byte input) throws OpCodeException{
        byte retVal = input++;
        
        
        this.setFlag(FLAG_S, retVal < 0);
        this.setFlag(FLAG_Z, retVal == 0);
        this.setFlag(FLAG_H, (retVal & 0x0F) == 0);
        this.setFlag(FLAG_PV, Byte.toUnsignedInt(input) == 0x7F);
        this.clearFlag(FLAG_N);
        
        return retVal;
    }
    
    /**
     * Operation "INC" on a 16 bit value.
     * This method increments a 16 bit value but doesn't seemm to affect any
     * flags.
     * 
     * @param input (short) the input value
     * @return the result
     */
    protected short opINC16(short input) {
        return (short)(input+1);
    }
    
    /**
     * Operation "DEC" on an 8 bit value.
     * This method decrements an 8 bit value and sets the status flags
     * appropriately.
     * 
     * @param input (byte) the input value
     * @return the output value
     * @throws OpCodeException 
     */
    protected byte opDEC8(byte input) throws OpCodeException {
        byte retVal = input--;
        
        this.setFlag(FLAG_S, retVal < 0);
        this.setFlag(FLAG_Z, retVal == 0);
        this.setFlag(FLAG_H, (retVal & 0x0F) == 0x0F);
        this.setFlag(FLAG_PV, Byte.toUnsignedInt(input) == 0x80);
        this.setFlag(FLAG_N);
        
        return retVal;
    }
    
    /**
     * Operation "DEC" on a 16 bit value.
     * This method decrements a 16 bit value but doesn't seem to affect any
     * flags.
     * 
     * @param input (short) the input value
     * @return the result
     * @throws OpCodeException 
     */
    protected short opDEC16(short input) throws OpCodeException {
        return (short)(input-1);
    }
    
    /**
     * Operation "RLCA" rotate left with carry A.
     * This method rotates the register A by 1 bit to the left and
     * sets the status flags accordingly.
     * 
     * @throws OpCodeException if there was a problem
     */
    protected void opRLCA() throws OpCodeException {
        int uregA = Byte.toUnsignedInt(this.regA);
        if((uregA & 0x80) == 0x80) {
            uregA = ((uregA << 1) | 0x01) & 0xFF;
            this.setFlag(FLAG_C);
        }
        else {
            uregA = (uregA << 1) & 0xFF;
            this.clearFlag(FLAG_C);
        }
        this.regA = (byte)uregA;
        this.clearFlag(FLAG_H);
        this.clearFlag(FLAG_N);
    }
    
    /**
     * Operation "RRCA" rotate right with carry A.
     * this method rotates the register A by 1 bit to the right and
     * sets the status flags accordingly
     * 
     * @throws OpCodeException 
     */
    protected void opRRCA() throws OpCodeException {
        int uregA = Byte.toUnsignedInt(this.regA);
        if((uregA & 0x01) == 0x01) {
            uregA = ((uregA >> 1) | 0x80) & 0xFF;
            this.setFlag(FLAG_C);
        }
        else {
            uregA = (uregA >> 1) & 0xFF;
            this.clearFlag(FLAG_C);
        }
        this.regA = (byte)uregA;
        this.clearFlag(FLAG_H);
        this.clearFlag(FLAG_N);
    }
    
    /**
     * Operation "RLA" rotate left with carry A.
     * This method rotates the register A by 1 bit to the left and
     * sets the status flags accordingly
     * 
     * @throws OpCodeException 
     */
    protected void opRLA() throws OpCodeException {
        int uregA = Byte.toUnsignedInt(this.regA);
        int uC = 0;
        
        if(this.checkFlag(FLAG_C))
            uC = 1;
        
        uregA = ((uregA << 1) | uC) & 0xFF;
        
        this.setFlag(FLAG_C, (uregA & 0x0100) == 0x0100);
        this.regA = (byte)uregA;
        this.clearFlag(FLAG_H);
        this.clearFlag(FLAG_N);
    }
    
    /**
     * Operation "RRA" rotate right with carry A.
     * This method rotates the register A by 1 bit to the right and
     * sets the status flags accordingly
     * 
     * @throws OpCodeException 
     */
    protected void opRRA() throws OpCodeException {
        int uregA = Byte.toUnsignedInt(this.regA);
        int uC = 0;
        
        if(this.checkFlag(FLAG_C))
            uC = 0x80;
        
        this.setFlag(FLAG_C, (uregA & 0x01) == 0x01);
        
        uregA = ((uregA >> 1) | uC) & 0xFF;
        this.regA = (byte)uregA;
        this.clearFlag(FLAG_H);
        this.clearFlag(FLAG_N);
    }
    
    /**
     * Operation "EX AF, AF'" exchanges the registers A and F with their
     * related shadow registers.
     * 
     * @throws OpCodeException 
     */
    protected void opEX_AF_AF2() throws OpCodeException {
        byte buffer = this.regA;
        this.regA = this.regA2;
        this.regA2 = buffer;
        
        buffer = this.regF;
        this.regF = this.regF2;
        this.regF2 = buffer;
    }
    
    /**
     * Opertion "EXX" exchanges all but AF between live and shadow registers
     * 
     * @throws OpCodeException 
     */
    protected void opEXX() throws OpCodeException {
        byte buffer = this.regB;
        this.regB = this.regB2;
        this.regB2 = buffer;
        
        buffer = this.regC;
        this.regC = this.regC2;
        this.regC2 = buffer;

        buffer = this.regD;
        this.regD = this.regD2;
        this.regD2 = buffer;
        
        buffer = this.regE;
        this.regE = this.regE2;
        this.regE2 = buffer;
        
        buffer = this.regH;
        this.regH = this.regH2;
        this.regH2 = buffer;
        
        buffer = this.regL;
        this.regL = this.regL2;
        this.regL2 = buffer;
    }
    
    /**
     * Operation "EX (SP), HL" 
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opEX_SP_HL() throws OpCodeException, MemoryException {
        byte buffer = this.regH;
        this.regH = this.readMemory8(Short.toUnsignedLong(this.regSP)+1);
        this.writeMemory8(Short.toUnsignedLong(this.regSP)+1, buffer);
        
        buffer = this.regL;
        this.regL = this.readMemory8(Short.toUnsignedLong(this.regSP));
        this.writeMemory8(Short.toUnsignedLong(this.regSP), buffer);
    }
    
    /**
     * Operation "EX (SP), IX" 
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opEX_SP_IX() throws OpCodeException, MemoryException {
        short buffer = this.regIX;
        this.regIX = this.readMemory16(Short.toUnsignedLong(this.regSP));
        this.writeMemory16(Short.toUnsignedLong(this.regSP), buffer);
    }
    
    /**
     * Operation "EX (SP), IY" 
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opEX_SP_IY() throws OpCodeException, MemoryException {
        short buffer = this.regIY;
        this.regIY = this.readMemory16(Short.toUnsignedLong(this.regSP));
        this.writeMemory16(Short.toUnsignedLong(this.regSP), buffer);
    }
    
    /**
     * Operation "EX DE, HL" 
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opEX_DE_HL() throws OpCodeException, MemoryException {
        byte buffer = this.regD;
        this.regD = this.regH;
        this.regH = buffer;
        
        buffer = this.regE;
        this.regE = this.regL;
        this.regL = buffer;
    }
    
    /**
     * Operation "ADD" for two 8 bit values
     * 
     * @param input1 (byte) the first value
     * @param input2 (byte) the second value
     * @return the result
     * @throws OpCodeException if there was a problem
     */
    protected byte opADD8(byte input1, byte input2) throws OpCodeException {
        int u1 = Byte.toUnsignedInt(input1);
        int u2 = Byte.toUnsignedInt(input2);
        
        int retVal = u1+u2;
        
        this.clearFlag(FLAG_N);
        this.setFlag(FLAG_S, (retVal & 0x80) == 0x80);
        this.setFlag(FLAG_Z, retVal == 0);
        this.setFlag(FLAG_H, (((u1 & 0x0F) + (u2 & 0x0F)) & 0x10) == 0x10);
        this.setFlag(FLAG_PV, retVal > 127);
        this.setFlag(FLAG_C, (retVal & 0x0100) == 0x0100);
        
        return (byte)retVal;
    }
    
    /**
     * Operation "ADD" for two 16 bit values
     * 
     * @param input1 (short) the first value
     * @param input2 (short) the second value
     * @return the result
     * @throws OpCodeException if there was a problem
     */
    protected short opADD16(short input1, short input2) throws OpCodeException {
        int u1 = Short.toUnsignedInt(input1);
        int u2 = Short.toUnsignedInt(input2);
        
        int retVal = u1+u2;
        
        this.clearFlag(FLAG_N);
        this.setFlag(FLAG_S, (retVal & 0x8000) == 0x8000);
        this.setFlag(FLAG_Z, retVal == 0);
        this.setFlag(FLAG_H, (((u1 & 0x0FFF) + (u2 & 0x0FFF)) & 0x1000) == 0x1000);
        this.setFlag(FLAG_PV, retVal > 32767);
        this.setFlag(FLAG_C, (retVal & 0x010000) == 0x010000);
        
        return (short)retVal;
    }
    
    /**
     * Operation "ADC" for two 8 bit values
     * 
     * @param input1 (byte) the first value
     * @param input2 (byte) the second value
     * @return the result
     * @throws OpCodeException if there was a problem
     */
    protected byte opADC8(byte input1, byte input2) throws OpCodeException {
        int u1 = Byte.toUnsignedInt(input1);
        int u2 = Byte.toUnsignedInt(input2);
        
        int retVal = u1+u2;
        if(this.checkFlag(FLAG_C)) retVal++;
        
        this.clearFlag(FLAG_N);
        this.setFlag(FLAG_S, (retVal & 0x80) == 0x80);
        this.setFlag(FLAG_Z, retVal == 0);
        this.setFlag(FLAG_H, (((u1 & 0x0F) + (u2 & 0x0F) + (this.checkFlag(FLAG_C) ? 1 : 0)) & 0x10) == 0x10);
        this.setFlag(FLAG_PV, retVal > 127);
        this.setFlag(FLAG_C, (retVal & 0x0100) == 0x0100);
        
        return (byte)retVal;
    }
    
    /**
     * Operation "ADC" for two 16 bit values
     * 
     * @param input1 (short) the first value
     * @param input2 (short) the second value
     * @return the result
     * @throws OpCodeException if there was a problem
     */
    protected short opADC16(short input1, short input2) throws OpCodeException {
        int u1 = Short.toUnsignedInt(input1);
        int u2 = Short.toUnsignedInt(input2);
        
        int retVal = u1+u2;
        if(this.checkFlag(FLAG_C)) retVal++;
        
        this.clearFlag(FLAG_N);
        this.setFlag(FLAG_S, (retVal & 0x8000) == 0x8000);
        this.setFlag(FLAG_Z, retVal == 0);
        this.setFlag(FLAG_H, (((u1 & 0x0FFF) + (u2 & 0x0FFF) + (this.checkFlag(FLAG_C) ? 1 : 0)) & 0x1000) == 0x1000);
        this.setFlag(FLAG_PV, retVal > 32767);
        this.setFlag(FLAG_C, (retVal & 0x010000) == 0x010000);
        
        return (short)retVal;
    }
    
    /**
     * Operation "SUB" for two 8 bit values
     * 
     * @param input1 (byte) the first value
     * @param input2 (byte) the second value
     * @return the result
     * @throws OpCodeException if there was a problem
     */
    protected byte opSUB8(byte input1, byte input2) throws OpCodeException {
        int u1 = Byte.toUnsignedInt(input1);
        int u2 = Byte.toUnsignedInt(input2);
        
        int retVal = u1-u2;
        
        this.clearFlag(FLAG_N);
        this.setFlag(FLAG_S, (retVal & 0x80) == 0x80);
        this.setFlag(FLAG_Z, retVal == 0);
        this.setFlag(FLAG_H, (((u1 & 0x0F) - (u2 & 0x0F)) & 0x10) == 0x10);
        this.setFlag(FLAG_PV, retVal > 127);
        this.setFlag(FLAG_C, (retVal & 0x0100) == 0x0100);
        
        return (byte)retVal;
    }
    
    /**
     * Operation "SUB" for two 16 bit values
     * 
     * @param input1 (short) the first value
     * @param input2 (short) the second value
     * @return the result
     * @throws OpCodeException if there was a problem
     */
    protected short opSUB16(short input1, short input2) throws OpCodeException {
        int u1 = Short.toUnsignedInt(input1);
        int u2 = Short.toUnsignedInt(input2);
        
        int retVal = u1-u2;
        
        this.clearFlag(FLAG_N);
        this.setFlag(FLAG_S, (retVal & 0x8000) == 0x8000);
        this.setFlag(FLAG_Z, retVal == 0);
        this.setFlag(FLAG_H, (((u1 & 0x0FFF) - (u2 & 0x0FFF)) & 0x1000) == 0x1000);
        this.setFlag(FLAG_PV, retVal > 32767);
        this.setFlag(FLAG_C, (retVal & 0x010000) == 0x010000);
        
        return (short)retVal;
    }
    
    /**
     * Operation "SBC" for two 8 bit values
     * 
     * @param input1 (byte) the first value
     * @param input2 (byte) the second value
     * @return the result
     * @throws OpCodeException if there was a problem
     */
    protected byte opSBC8(byte input1, byte input2) throws OpCodeException {
        int u1 = Byte.toUnsignedInt(input1);
        int u2 = Byte.toUnsignedInt(input2);
        
        int retVal = u1-u2;
        if(this.checkFlag(FLAG_C)) retVal--;
        
        this.clearFlag(FLAG_N);
        this.setFlag(FLAG_S, (retVal & 0x80) == 0x80);
        this.setFlag(FLAG_Z, retVal == 0);
        this.setFlag(FLAG_H, (((u1 & 0x0F) - (u2 & 0x0F) - (this.checkFlag(FLAG_C) ? 1 : 0)) & 0x10) == 0x10);
        this.setFlag(FLAG_PV, retVal > 127);
        this.setFlag(FLAG_C, (retVal & 0x0100) == 0x0100);
        
        return (byte)retVal;
    }
    
    /**
     * Operation "SBC" for two 16 bit values
     * 
     * @param input1 (short) the first value
     * @param input2 (short) the second value
     * @return the result
     * @throws OpCodeException if there was a problem
     */
    protected short opSBC16(short input1, short input2) throws OpCodeException {
        int u1 = Short.toUnsignedInt(input1);
        int u2 = Short.toUnsignedInt(input2);
        
        int retVal = u1-u2;
        if(this.checkFlag(FLAG_C)) retVal--;
        
        this.clearFlag(FLAG_N);
        this.setFlag(FLAG_S, (retVal & 0x8000) == 0x8000);
        this.setFlag(FLAG_Z, retVal == 0);
        this.setFlag(FLAG_H, (((u1 & 0x0FFF) - (u2 & 0x0FFF) - (this.checkFlag(FLAG_C) ? 1 : 0)) & 0x1000) == 0x1000);
        this.setFlag(FLAG_PV, retVal > 32767);
        this.setFlag(FLAG_C, (retVal & 0x010000) == 0x010000);
        
        return (short)retVal;
    }
    
    /**
     * Operation "AND" for two 8 bit values
     * 
     * @param input1 (byte) the first value
     * @param input2 (byte) the second value
     * @return the result
     * @throws OpCodeException if there was a problem
     */
    protected byte opAND8(byte input1, byte input2) throws OpCodeException {
        int u1 = Byte.toUnsignedInt(input1);
        int u2 = Byte.toUnsignedInt(input2);
        
        int retVal = u1 & u2;
        
        this.clearFlag(FLAG_N);
        this.setFlag(FLAG_S, (retVal & 0x80) == 0x80);
        this.setFlag(FLAG_Z, retVal == 0);
        this.setFlag(FLAG_H);
        this.setFlag(FLAG_PV, (retVal > 127) && (input1 <= 127));
        this.clearFlag(FLAG_C);
        
        return (byte)retVal;
    }
    
    /**
     * Checks the parity of a byte (stored in an int)
     * 
     * @param input the input byte
     * @return whether or not the byte is in parity
     */
    protected boolean parityEven8(int input) {
        int bit = 0x01;
        int bitCount = 0;
        for(int i=0; i<8; i++) {
            if((input & bit) == bit) bitCount++;
            bit <<= 1;
        }
        
        return (bitCount & 0x01) == 0;
    }
    
    /**
     * Operation "XOR" for two 8 bit values
     * 
     * @param input1 (byte) the first value
     * @param input2 (byte) the second value
     * @return the result
     * @throws OpCodeException if there was a problem
     */
    protected byte opXOR8(byte input1, byte input2) throws OpCodeException {
        int u1 = Byte.toUnsignedInt(input1);
        int u2 = Byte.toUnsignedInt(input2);
        
        int retVal = u1 ^ u2;
        
        this.clearFlag(FLAG_N);
        this.setFlag(FLAG_S, (retVal & 0x80) == 0x80);
        this.setFlag(FLAG_Z, retVal == 0);
        this.setFlag(FLAG_H);
        this.setFlag(FLAG_PV, this.parityEven8(retVal));
        this.clearFlag(FLAG_C);
        
        return (byte)retVal;
    }
    
    /**
     * Operation "OR" for two 8 bit values
     * 
     * @param input1 (byte) the first value
     * @param input2 (byte) the second value
     * @return the result
     * @throws OpCodeException if there was a problem
     */
    protected byte opOR8(byte input1, byte input2) throws OpCodeException {
        int u1 = Byte.toUnsignedInt(input1);
        int u2 = Byte.toUnsignedInt(input2);
        
        int retVal = u1 | u2;
        
        this.clearFlag(FLAG_N);
        this.setFlag(FLAG_S, (retVal & 0x80) == 0x80);
        this.setFlag(FLAG_Z, retVal == 0);
        this.setFlag(FLAG_H);
        this.setFlag(FLAG_PV, (retVal > 127) && (input1 <= 127));
        this.clearFlag(FLAG_C);
        
        return (byte)retVal;
    }
    
    /**
     * Operation "ADD" for two 16 bit values
     * 
     * @param input1 (short) the first value
     * @param input2 (short) the second value
     * @return the result
     * @throws OpCodeException if there was a problem
     */
    protected short opADD16_Flags(short input1, short input2) throws OpCodeException {
        int u1 = Short.toUnsignedInt(input1);
        int u2 = Short.toUnsignedInt(input2);
        
        int retVal = u1 + u2;
        
        this.clearFlag(FLAG_N);
        this.setFlag(FLAG_C, (retVal & 0x010000) == 0x010000);
        this.setFlag(FLAG_H, (((u1 & 0x0FFF) + (u2 & 0x0FFF)) & 0x1000) == 0x1000);
        
        return (short)retVal;
    }
    
    /**
     * Operation "DJNZ" conditional jump on decrement.
     * This method decrements B and if B = 0 continues, otherwise jumps by
     * offset.
     * 
     * @param offset (byte) the offset
     * @throws OpCodeException 
     */
    protected void opDJNZ(byte offset) throws OpCodeException {
        this.regB--;
        if(this.regB == 0) return;
        this.regPC += (short)offset;
    }
    
    /**
     * Operation "JR" jump relative
     * 
     * @param offset (byte) the jump offset
     * @throws OpCodeException if there was a problem
     */
    protected void opJR(byte offset) throws OpCodeException {
        this.regPC += (short)offset;
    }
    
    /**
     * Operation "JR NZ" jump relative if not zero
     * 
     * @param offset (byte) the jump offset
     * @throws OpCodeException if there was a problem
     */
    protected void opJRNZ(byte offset) throws OpCodeException {
        if(!this.checkFlag(FLAG_Z))
            this.regPC += (short)offset;
    }
    
    /**
     * Operation "JR Z" jump relative if zero
     * 
     * @param offset (byte) the jump offset
     * @throws OpCodeException if there was a problem
     */
    protected void opJRZ(byte offset) throws OpCodeException {
        if(this.checkFlag(FLAG_Z))
            this.regPC += (short)offset;
    }
    
    /**
     * Operation "JR NC" jump relative if not carry
     * 
     * @param offset (byte) the jump offset
     * @throws OpCodeException if there was a problem
     */
    protected void opJRNC(byte offset) throws OpCodeException {
        if(!this.checkFlag(FLAG_C))
            this.regPC += (short)offset;
    }
    
    /**
     * Operation "JR C" jump relative if carry
     * 
     * @param offset (byte) the jump offset
     * @throws OpCodeException if there was a problem
     */
    protected void opJRC(byte offset) throws OpCodeException {
        if(this.checkFlag(FLAG_C))
            this.regPC += (short)offset;
    }
    
    /**
     * Operaton "RET" return from subroutine
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opRET() throws OpCodeException, MemoryException {
        int low = Byte.toUnsignedInt(this.readMemory8(Short.toUnsignedLong(this.regSP)));
        this.regSP++;
        int high = Byte.toUnsignedInt(this.readMemory8(Short.toUnsignedLong(this.regSP)));
        this.regSP++;

        this.regPC = (short)((high << 8) + low);
    }
    
    /**
     * Operation "RET NZ" return when not zero
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opRETNZ() throws OpCodeException, MemoryException {
        if(!this.checkFlag(FLAG_Z)) {
            int low = Byte.toUnsignedInt(this.readMemory8(Short.toUnsignedLong(this.regSP)));
            this.regSP++;
            int high = Byte.toUnsignedInt(this.readMemory8(Short.toUnsignedLong(this.regSP)));
            this.regSP++;
            
            this.regPC = (short)((high << 8) + low);
        }
        else {
            this.regPC++;
        }
    }
    
    /**
     * Operation "RET Z" return when zero
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opRETZ() throws OpCodeException, MemoryException {
        if(this.checkFlag(FLAG_Z)) {
            int low = Byte.toUnsignedInt(this.readMemory8(Short.toUnsignedLong(this.regSP)));
            this.regSP++;
            int high = Byte.toUnsignedInt(this.readMemory8(Short.toUnsignedLong(this.regSP)));
            this.regSP++;
            
            this.regPC = (short)((high << 8) + low);
        }
        else {
            this.regPC++;
        }
    }
    
    /**
     * Operation "RET NC" return when not carry
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opRETNC() throws OpCodeException, MemoryException {
        if(!this.checkFlag(FLAG_C)) {
            int low = Byte.toUnsignedInt(this.readMemory8(Short.toUnsignedLong(this.regSP)));
            this.regSP++;
            int high = Byte.toUnsignedInt(this.readMemory8(Short.toUnsignedLong(this.regSP)));
            this.regSP++;
            
            this.regPC = (short)((high << 8) + low);
        }
        else {
            this.regPC++;
        }
    }
    
    /**
     * Operation "RET C" return when carry
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opRETC() throws OpCodeException, MemoryException {
        if(this.checkFlag(FLAG_C)) {
            int low = Byte.toUnsignedInt(this.readMemory8(Short.toUnsignedLong(this.regSP)));
            this.regSP++;
            int high = Byte.toUnsignedInt(this.readMemory8(Short.toUnsignedLong(this.regSP)));
            this.regSP++;
            
            this.regPC = (short)((high << 8) + low);
        }
        else {
            this.regPC++;
        }
    }
    
    /**
     * Operation "RET PO" return when parity odd
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opRETPO() throws OpCodeException, MemoryException {
        if(!this.checkFlag(FLAG_PV)) {
            int low = Byte.toUnsignedInt(this.readMemory8(Short.toUnsignedLong(this.regSP)));
            this.regSP++;
            int high = Byte.toUnsignedInt(this.readMemory8(Short.toUnsignedLong(this.regSP)));
            this.regSP++;
            
            this.regPC = (short)((high << 8) + low);
        }
        else {
            this.regPC++;
        }
    }
    
    /**
     * Operation "RET PE" return when parity even
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opRETPE() throws OpCodeException, MemoryException {
        if(this.checkFlag(FLAG_PV)) {
            int low = Byte.toUnsignedInt(this.readMemory8(Short.toUnsignedLong(this.regSP)));
            this.regSP++;
            int high = Byte.toUnsignedInt(this.readMemory8(Short.toUnsignedLong(this.regSP)));
            this.regSP++;
            
            this.regPC = (short)((high << 8) + low);
        }
        else {
            this.regPC++;
        }
    }
    
    /**
     * Operation "RET NS" return when not sign
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opRETNS() throws OpCodeException, MemoryException {
        if(!this.checkFlag(FLAG_S)) {
            int low = Byte.toUnsignedInt(this.readMemory8(Short.toUnsignedLong(this.regSP)));
            this.regSP++;
            int high = Byte.toUnsignedInt(this.readMemory8(Short.toUnsignedLong(this.regSP)));
            this.regSP++;
            
            this.regPC = (short)((high << 8) + low);
        }
        else {
            this.regPC++;
        }
    }
    
    /**
     * Operation "RET S" return when sign
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opRETS() throws OpCodeException, MemoryException {
        if(this.checkFlag(FLAG_S)) {
            int low = Byte.toUnsignedInt(this.readMemory8(Short.toUnsignedLong(this.regSP)));
            this.regSP++;
            int high = Byte.toUnsignedInt(this.readMemory8(Short.toUnsignedLong(this.regSP)));
            this.regSP++;
            
            this.regPC = (short)((high << 8) + low);
        }
        else {
            this.regPC++;
        }
    }
    
    /**
     * Operation "CALL" call a subroutine
     * 
     * @param callAddress
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opCALL(short callAddress) throws OpCodeException, MemoryException {
        this.regSP-=2;
        this.writeMemory16(Short.toUnsignedLong(this.regSP), this.regPC);
        this.regPC = callAddress;
    }
    
    /**
     * Operation "CALL NZ nn" calls a subroutine if not zero
     * 
     * @param callAddress
     * @throws OpCodeException 
     * @throws MemoryException 
     */
    protected void opCALLNZ(short callAddress) throws OpCodeException, MemoryException {
        if(!this.checkFlag(FLAG_Z)) {
            this.regSP-=2;
            this.writeMemory16(Short.toUnsignedLong(this.regSP), this.regPC);
            this.regPC = callAddress;
        }
        else {
            this.regPC += 3;
        }
    }
    
    /**
     * Operation "CALL Z nn" calls a subroutine if zero
     * 
     * @param callAddress
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opCALLZ(short callAddress) throws OpCodeException, MemoryException {
        if(this.checkFlag(FLAG_Z)) {
            this.regSP-=2;
            this.writeMemory16(Short.toUnsignedLong(this.regSP), this.regPC);
            this.regPC = callAddress;
        }
        else {
            this.regPC += 3;
        }
    }
    
    /**
     * Operation "CALL NC nn" calls a subroutine if not carry
     * 
     * @param callAddress
     * @throws OpCodeException 
     * @throws MemoryException 
     */
    protected void opCALLNC(short callAddress) throws OpCodeException, MemoryException {
        if(!this.checkFlag(FLAG_C)) {
            this.regSP-=2;
            this.writeMemory16(Short.toUnsignedLong(this.regSP), this.regPC);
            this.regPC = callAddress;
        }
        else {
            this.regPC += 3;
        }
    }
    
    /**
     * Operation "CALL C nn" calls a subroutine if carry
     * 
     * @param callAddress
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opCALLC(short callAddress) throws OpCodeException, MemoryException {
        if(this.checkFlag(FLAG_C)) {
            this.regSP-=2;
            this.writeMemory16(Short.toUnsignedLong(this.regSP), this.regPC);
            this.regPC = callAddress;
        }
        else {
            this.regPC += 3;
        }
    }
    
    /**
     * Operation "CALL PO nn" calls a subroutine if parity odd
     * 
     * @param callAddress
     * @throws OpCodeException 
     * @throws MemoryException 
     */
    protected void opCALLPO(short callAddress) throws OpCodeException, MemoryException {
        if(!this.checkFlag(FLAG_PV)) {
            this.regSP-=2;
            this.writeMemory16(Short.toUnsignedLong(this.regSP), this.regPC);
            this.regPC = callAddress;
        }
        else {
            this.regPC += 3;
        }
    }
    
    /**
     * Operation "CALL PE nn" calls a subroutine if parity even
     * 
     * @param callAddress
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opCALLPE(short callAddress) throws OpCodeException, MemoryException {
        if(this.checkFlag(FLAG_PV)) {
            this.regSP-=2;
            this.writeMemory16(Short.toUnsignedLong(this.regSP), this.regPC);
            this.regPC = callAddress;
        }
        else {
            this.regPC += 3;
        }
    }
    
    /**
     * Operation "CALL NS nn" calls a subroutine if not sign
     * 
     * @param callAddress
     * @throws OpCodeException 
     * @throws MemoryException 
     */
    protected void opCALLNS(short callAddress) throws OpCodeException, MemoryException {
        if(!this.checkFlag(FLAG_S)) {
            this.regSP-=2;
            this.writeMemory16(Short.toUnsignedLong(this.regSP), this.regPC);
            this.regPC = callAddress;
        }
        else {
            this.regPC += 3;
        }
    }
    
    /**
     * Operation "CALL S nn" calls a subroutine if sign
     * 
     * @param callAddress
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opCALLS(short callAddress) throws OpCodeException, MemoryException {
        if(this.checkFlag(FLAG_Z)) {
            this.regSP-=2;
            this.writeMemory16(Short.toUnsignedLong(this.regSP), this.regPC);
            this.regPC = callAddress;
        }
        else {
            this.regPC += 3;
        }
    }
    
    /**
     * Operation "RTS nn" call a certain address in bank 0
     * 
     * @param low
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opRTS(byte low) throws OpCodeException, MemoryException {
        this.regSP-=2;
        this.writeMemory16(Short.toUnsignedLong(this.regSP), this.regPC);
        this.regPC = (short)((Byte.toUnsignedInt(low)));
    }
    
    /**
     * Operation "POP 16bit" pops a 16 bit value from the stack
     * 
     * @param registerNum
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opPOP16(byte registerNum) throws OpCodeException, MemoryException {
        byte low = this.readMemory8(Short.toUnsignedLong(this.regSP));
        this.regSP++;
        byte high = this.readMemory8(Short.toUnsignedLong(this.regSP));
        this.regSP++;
        
        switch (registerNum) {
            case 0x00:
                this.regB = high;
                this.regC = low;
                break;
                
            case 0x01:
                this.regD = high;
                this.regE = low;
                break;
                
            case 0x02:
                this.regH = high;
                this.regL = low;
                break;
                
            case 0x03:
                this.regA = high;
                this.regF = low;
                break;
                
            default:
                throw new OpCodeException("16 bit register number " + registerNum + " does not exist!");
        }
    }
    
    /**
     * Operation "POP IX" pops a 16 bit value from the stack
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opPOPIX() throws OpCodeException, MemoryException {
        this.regIX = this.readMemory16(Short.toUnsignedLong(this.regSP));
        this.regSP+=2;
    }
    
    /**
     * Operation "POP IY" pops a 16 bit value from the stack
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opPOPIY() throws OpCodeException, MemoryException {
        this.regIY = this.readMemory16(Short.toUnsignedLong(this.regSP));
        this.regSP+=2;
    }
    
    /**
     * Operation "PUSH 16bit" pushes a 16 bit value to the stack
     * 
     * @param registerNum
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opPUSH16(byte registerNum) throws OpCodeException, MemoryException {
        byte low;
        byte high;
        
        switch (registerNum) {
            case 0x00:
                high = this.regB;
                low = this.regC;
                break;
                
            case 0x01:
                high = this.regD;
                low = this.regE;
                break;
                
            case 0x02:
                high = this.regH;
                low = this.regL;
                break;
                
            case 0x03:
                high = this.regA;
                low = this.regF;
                break;
                
            default:
                throw new OpCodeException("16 bit register number " + registerNum + " does not exist!");
        }
        
        this.regSP--;
        this.writeMemory8(Short.toUnsignedLong(this.regSP), high);
        this.regSP--;
        this.writeMemory8(Short.toUnsignedLong(this.regSP), low);
    }
    
    /**
     * Operation "PUSH IX" pops a 16 bit value from the stack
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opPUSHIX() throws OpCodeException, MemoryException {
        this.regSP-=2;
        this.writeMemory16(Short.toUnsignedLong(this.regSP), this.regIX);
    }
    
    /**
     * Operation "PUSH IY" pops a 16 bit value from the stack
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opPUSHIY() throws OpCodeException, MemoryException {
        this.regSP-=2;
        this.writeMemory16(Short.toUnsignedLong(this.regSP), this.regIY);
    }
    
    /**
     * Operation "DAA" adjust accumulator after addition...
     * 
     * @throws OpCodeException 
     */
    protected void opDAA() throws OpCodeException {
        if((this.regA & 0x0F) > 0x09) {
            if(this.checkFlag(FLAG_H)) {
                this.regA += 0x0A;
                this.clearFlag(FLAG_H);
            }
            else {
                this.regA += 0x06;
                this.setFlag(FLAG_H);
            }
        }
        
        if((this.regA & 0xF0) > 0x09) {
            if(this.checkFlag(FLAG_C)) {
                this.regA += 0xA0;
                this.clearFlag(FLAG_C);
            }
            else {
                this.regA += 0x60;
                this.setFlag(FLAG_C);
            }
        }
    }
    
    /**
     * Operation "CPL" calculates the twos-complement of the accumulator
     * 
     * @throws OpCodeException 
     */
    protected void opCPL() throws OpCodeException {
        this.regA = (byte)(this.regA ^ 0xFF);
    }
    
    /**
     * Operation "SCF" - Set carry flag
     * 
     * @throws OpCodeException 
     */
    protected void opSCF() throws OpCodeException {
        this.setFlag(FLAG_C);
        this.clearFlag(FLAG_H);
        this.clearFlag(FLAG_N);
    }
    
    /**
     * Operation "CCF" - invert carry flag
     * 
     * @throws OpCodeException 
     */
    protected void opCCF() throws OpCodeException {
        if(this.checkFlag(FLAG_C)) {
            this.clearFlag(FLAG_C);
            this.setFlag(FLAG_H);
        }
        else {
            this.setFlag(FLAG_C);
            this.clearFlag(FLAG_H);
        }
        this.clearFlag(FLAG_N);
    }
    
    /**
     * Operation "HALT" - wait for interrupt
     * 
     * @throws OpCodeException 
     */
    protected void opHALT() throws OpCodeException {
        // TODO: Code this!
    }
    
    /**
     * Operation "OUT" send value to port
     * 
     * @param port
     * @param value
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opOUT8(byte port, byte value) throws OpCodeException, MemoryException {
        this.writeIO8(Byte.toUnsignedLong(port), value);
    }
    
    /**
     * Operation "IN" receive input from port
     * 
     * @param port
     * @return
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected byte opIN8(byte port) throws OpCodeException, MemoryException {
        return this.readIO8(port);
    }
    
    /**
     * Operation "DI" - deactivate interrupt
     * 
     * @throws OpCodeException 
     */
    protected void opDI() throws OpCodeException {
        this.interruptsEnabled = false;
    }
    
    /**
     * Operation "EI" - enable interrupt
     * 
     * @throws OpCodeException 
     */
    protected void opEI() throws OpCodeException {
        this.interruptsEnabled = true;
    }
    
    /**
     * Operation "RLC" Rotate left with carry and set flags accordingly
     * 
     * @param input (byte) the input value
     * @return the rotated value
     * @throws OpCodeException 
     */
    protected byte opRLC(byte input) throws OpCodeException {
        int val = Byte.toUnsignedInt(input) << 1;
        
        if((val & 0x0100) == 0x0100) {
            this.setFlag(FLAG_C);
            val |= 0x01;
        }
        else {
            this.clearFlag(FLAG_C);
        }
        
        val &= 0xFF;
        
        this.setFlag(FLAG_S, ((byte)val) < 0);
        this.setFlag(FLAG_Z, val == 0);
        this.clearFlag(FLAG_H);
        this.setFlag(FLAG_PV, this.parityEven8(val));
        this.clearFlag(FLAG_N);
        return (byte)val;
    }
    
    /**
     * Operation "RRC" Rotate right with carry and set flags accordingly
     * 
     * @param input (byte) the input value
     * @return the rotated value
     * @throws OpCodeException 
     */
    protected byte opRRC(byte input) throws OpCodeException {
        int val = Byte.toUnsignedInt(input);
        
        if((val & 0x01) == 0x01) {
            this.setFlag(FLAG_C);
            val |= 0x0100;
        }
        else {
            this.clearFlag(FLAG_C);
        }
        
        val = (val>>1) & 0xFF;
        
        this.setFlag(FLAG_S, ((byte)val) < 0);
        this.setFlag(FLAG_Z, val == 0);
        this.clearFlag(FLAG_H);
        this.setFlag(FLAG_PV, this.parityEven8(val));
        this.clearFlag(FLAG_N);
        return (byte)val;
    }
    
    /**
     * Operation "RL" Rotate left and set flags accordingly
     * 
     * @param input (byte) the input value
     * @return the rotated value
     * @throws OpCodeException 
     */
    protected byte opRL(byte input) throws OpCodeException {
        int val = Byte.toUnsignedInt(input) << 1;

        if(this.checkFlag(FLAG_C))
            val |= 0x01;
        
        this.setFlag(FLAG_C, (val & 0x0100) == 0x0100);
        val &= 0xFF;
        this.setFlag(FLAG_S, ((byte)val) < 0);
        this.setFlag(FLAG_Z, val == 0);
        this.clearFlag(FLAG_H);
        this.setFlag(FLAG_PV, this.parityEven8(val));
        this.clearFlag(FLAG_N);
        return (byte)val;
    }
    
    /**
     * Operation "RR" Rotate right and set flags accordingly
     * 
     * @param input (byte) the input value
     * @return the rotated value
     * @throws OpCodeException 
     */
    protected byte opRR(byte input) throws OpCodeException {
        int val = Byte.toUnsignedInt(input);
        
        if(this.checkFlag(FLAG_C))
            val |= 0x0100;
        
        this.setFlag(FLAG_C, (val & 0x01) == 0x01);
        val = (val>>1) & 0xFF;
        
        this.setFlag(FLAG_S, ((byte)val) < 0);
        this.setFlag(FLAG_Z, val == 0);
        this.clearFlag(FLAG_H);
        this.setFlag(FLAG_PV, this.parityEven8(val));
        this.clearFlag(FLAG_N);
        return (byte)val;
    }
    
    /**
     * Operation "SLA" Shift left and set flags accordingly
     * 
     * @param input (byte) the input value
     * @return the rotated value
     * @throws OpCodeException 
     */
    protected byte opSLA(byte input) throws OpCodeException {
        int val = Byte.toUnsignedInt(input) << 1;

        this.setFlag(FLAG_C, (val & 0x0100) == 0x0100);
        val &= 0xFF;
        this.setFlag(FLAG_S, ((byte)val) < 0);
        this.setFlag(FLAG_Z, val == 0);
        this.clearFlag(FLAG_H);
        this.setFlag(FLAG_PV, this.parityEven8(val));
        this.clearFlag(FLAG_N);
        return (byte)val;
    }
    
    /**
     * Operation "SRA" Shift right and set flags accordingly
     * 
     * @param input (byte) the input value
     * @return the rotated value
     * @throws OpCodeException 
     */
    protected byte opSRA(byte input) throws OpCodeException {
        int val = Byte.toUnsignedInt(input);
        
        if((val & 0x80) == 0x80)
            val |= 0x0100;
        
        this.setFlag(FLAG_C, (val & 0x01) == 0x01);
        val = (val>>1) & 0xFF;
        
        this.setFlag(FLAG_S, ((byte)val) < 0);
        this.setFlag(FLAG_Z, val == 0);
        this.clearFlag(FLAG_H);
        this.setFlag(FLAG_PV, this.parityEven8(val));
        this.clearFlag(FLAG_N);
        return (byte)val;
    }
    
    /**
     * Operation "SRL" Shift right and set flags accordingly
     * 
     * @param input (byte) the input value
     * @return the rotated value
     * @throws OpCodeException 
     */
    protected byte opSRL(byte input) throws OpCodeException {
        int val = Byte.toUnsignedInt(input);
        
        this.setFlag(FLAG_C, (val & 0x01) == 0x01);
        val = (val>>1) & 0xFF;
        
        this.setFlag(FLAG_S, ((byte)val) < 0);
        this.setFlag(FLAG_Z, val == 0);
        this.clearFlag(FLAG_H);
        this.setFlag(FLAG_PV, this.parityEven8(val));
        this.clearFlag(FLAG_N);
        return (byte)val;
    }
    
    /**
     * Operation "BIT" - Bit Test
     * 
     * @param bitNumber
     * @param value
     * @throws OpCodeException 
     */
    protected void opBIT(byte bitNumber, byte value) throws OpCodeException {
        int uValue = Byte.toUnsignedInt(value);
        int bit = 0x01 << bitNumber;
        
        this.setFlag(FLAG_Z, (uValue & bit) == 0);
        this.setFlag(FLAG_H);
        this.clearFlag(FLAG_N);
    }
    
    /**
     * Operation "RES" - Reset (clear) bit
     * 
     * @param bitNumber
     * @param value
     * @return
     * @throws OpCodeException 
     */
    protected byte opRES(byte bitNumber, byte value) throws OpCodeException {
        int uValue = Byte.toUnsignedInt(value);
        int bit = 0x01 << bitNumber;
        
        uValue &= (0xFF - bit);
        
        return (byte)uValue;
    }
    
    /**
     * Operation "SET" - Set bit
     * 
     * @param bitNumber
     * @param value
     * @return
     * @throws OpCodeException 
     */
    protected byte opSET(byte bitNumber, byte value) throws OpCodeException {
        int uValue = Byte.toUnsignedInt(value);
        int bit = 0x01 << bitNumber;
        
        uValue |= bit;
        
        return (byte)uValue;
    }
    
    /**
     * Operation "RRD" - do some weird stuff with A and (HL)
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    private void opRRD() throws OpCodeException, MemoryException {
        int highA = Byte.toUnsignedInt(this.regA) & 0xF0;
        int lowA  = Byte.toUnsignedInt(this.regA) & 0x0F;
        int value = Short.toUnsignedInt(this.readMemory16(Short.toUnsignedLong(this.getRegister16((byte)0x02))));
        int highValue = value & 0xF0;
        int lowValue = value & 0x0F;
        
        this.regA = (byte)(highA | lowValue & 0xFF); 
        value = (lowA << 4) | (highValue >> 4);
        
        this.writeMemory16(Short.toUnsignedLong(this.getRegister16((byte)0x02)), (byte)(value & 0xFF));
        
        this.setFlag(FLAG_S, (highA & 0x80) == 0x80);
        this.setFlag(FLAG_Z, this.regA == 0);
        this.clearFlag(FLAG_H);
        this.setFlag(FLAG_PV, this.parityEven8(this.regA));
        this.clearFlag(FLAG_N);
    }
    
    /**
     * Operation "RLD" - do some other weird stuff with A and (HL)
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    private void opRLD() throws OpCodeException, MemoryException {
        int highA = Byte.toUnsignedInt(this.regA) & 0xF0;
        int lowA  = Byte.toUnsignedInt(this.regA) & 0x0F;
        int value = Short.toUnsignedInt(this.readMemory16(Short.toUnsignedLong(this.getRegister16((byte)0x02))));
        int highValue = value & 0xF0;
        int lowValue = value & 0x0F;

        this.regA = (byte)(highA | (highValue >> 4));
        value = lowA | (lowValue << 4);
        
        this.writeMemory16(Short.toUnsignedLong(this.getRegister16((byte)0x02)), (byte)(value & 0xFF));
        
        this.setFlag(FLAG_S, (highA & 0x80) == 0x80);
        this.setFlag(FLAG_Z, this.regA == 0);
        this.clearFlag(FLAG_H);
        this.setFlag(FLAG_PV, this.parityEven8(this.regA));
        this.clearFlag(FLAG_N);
    }
    
    /**
     * Operation "LDI" - copy byte from (HL) to (DE) and decrease (BC)
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opLDI() throws OpCodeException, MemoryException {
        this.writeMemory8(this.getRegister16((byte)0x01), this.readMemory8(this.getRegister16((byte)0x02)));
        short bc = this.getRegister16((byte)0x00);
        bc--;
        this.setRegister16((byte)0x00, bc);
        
        this.clearFlag(FLAG_H);
        this.setFlag(FLAG_PV, bc != 0);
        this.clearFlag(FLAG_N);
    }
    
    /**
     * Operation "CPI" compare byte at (HL) and modify BC and HL
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opCPI() throws OpCodeException, MemoryException {
        short hl = this.getRegister16((byte)0x02);
        short bc = this.getRegister16((byte)0x00);
        this.opSUB8(this.regA, this.readMemory8(hl));
        hl++;
        bc--;
        
        this.setRegister16((byte)0x00, bc);
        this.setRegister16((byte)0x02, hl);
        
        this.setFlag(FLAG_PV, bc != 0);
    }
    
    /**
     * Operation "LDD" copy data from (hl) to (de) and decrease hl, bc and de
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opLDD() throws OpCodeException, MemoryException {
        short bc = this.getRegister16((byte)0x00);
        short de = this.getRegister16((byte)0x01);
        short hl = this.getRegister16((byte)0x02);
        
        this.writeMemory8(de, this.readMemory8(hl));
        de--;
        hl--;
        bc--;
        this.setRegister16((byte)0x00, bc);
        this.setRegister16((byte)0x01, de);
        this.setRegister16((byte)0x02, hl);
        
        this.clearFlag(FLAG_H);
        this.clearFlag(FLAG_N);
        this.setFlag(FLAG_PV, bc != 0);
    }
    
    /**
     * Operation "CPD" compare and then decrement
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void opCPD() throws OpCodeException, MemoryException {
        short hl = this.getRegister16((byte)0x02);
        short bc = this.getRegister16((byte)0x00);
        this.opSUB8(this.regA, this.readMemory8(hl));
        hl--;
        bc--;
        
        this.setRegister16((byte)0x00, bc);
        this.setRegister16((byte)0x02, hl);
        
        this.setFlag(FLAG_PV, bc != 0);
    }
        
    /**
     * subOpCB - this method handles the opcodes that start with 0xCB - this is
     * a large set of opcodes!
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void subOpCB() throws OpCodeException, MemoryException {
        byte nextOpCode = this.readMemory8(Short.toUnsignedLong(this.regPC));
        
        switch (Byte.toUnsignedInt(nextOpCode)) {
            case 0x00:  // RLC B
            case 0x01:  // RLC C
            case 0x02:  // RLC D
            case 0x03:  // RLC E
            case 0x04:  // RLC H
            case 0x05:  // RLC L
            case 0x07:  // RLC A
                this.setRegister8((byte)((nextOpCode & 0x07)), this.opRLC(this.getRegister8((byte)(nextOpCode & 0x07))));
                this.regPC++;
                break;
                
            case 0x06:  // RLC (HL)
                this.writeMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)), this.opRLC(this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)))));
                this.regPC++;
                break;
                
            case 0x08:  // RRC B
            case 0x09:  // RRC C
            case 0x0A:  // RRC D
            case 0x0B:  // RRC E
            case 0x0C:  // RRC H
            case 0x0D:  // RRC L
            case 0x0F:  // RRC A
                this.setRegister8((byte)((nextOpCode & 0x07)), this.opRRC(this.getRegister8((byte)(nextOpCode & 0x07))));
                this.regPC++;
                break;
                
            case 0x0E:  // RRC (HL)
                this.writeMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)), this.opRRC(this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)))));
                this.regPC++;
                break;
                
            case 0x10:  // RL B
            case 0x11:  // RL C
            case 0x12:  // RL D
            case 0x13:  // RL E
            case 0x14:  // RL H
            case 0x15:  // RL L
            case 0x17:  // RL A
                this.setRegister8((byte)((nextOpCode & 0x07)), this.opRL(this.getRegister8((byte)(nextOpCode & 0x07))));
                this.regPC++;
                break;
                
            case 0x16:  // RL (HL)
                this.writeMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)), this.opRL(this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)))));
                this.regPC++;
                break;
                
            case 0x18:  // RR B
            case 0x19:  // RR C
            case 0x1A:  // RR D
            case 0x1B:  // RR E
            case 0x1C:  // RR H
            case 0x1D:  // RR L
            case 0x1F:  // RR A
                this.setRegister8((byte)((nextOpCode & 0x07)), this.opRR(this.getRegister8((byte)(nextOpCode & 0x07))));
                this.regPC++;
                break;
                
            case 0x1E:  // RR (HL)
                this.writeMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)), this.opRR(this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)))));
                this.regPC++;
                break;
                
            case 0x20:  // SLA B
            case 0x21:  // SLA C
            case 0x22:  // SLA D
            case 0x23:  // SLA E
            case 0x24:  // SLA H
            case 0x25:  // SLA L
            case 0x27:  // SLA A
                this.setRegister8((byte)((nextOpCode & 0x07)), this.opSLA(this.getRegister8((byte)(nextOpCode & 0x07))));
                this.regPC++;
                break;
                
            case 0x26:  // SLA (HL)
                this.writeMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)), this.opSLA(this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)))));
                this.regPC++;
                break;
                
            case 0x28:  // SRA B
            case 0x29:  // SRA C
            case 0x2A:  // SRA D
            case 0x2B:  // SRA E
            case 0x2C:  // SRA H
            case 0x2D:  // SRA L
            case 0x2F:  // SRA A
                this.setRegister8((byte)((nextOpCode & 0x07)), this.opSRA(this.getRegister8((byte)(nextOpCode & 0x07))));
                this.regPC++;
                break;
                
            case 0x2E:  // SRA (HL)
                this.writeMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)), this.opSRA(this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)))));
                this.regPC++;
                break;
                
            case 0x38:  // SRL B
            case 0x39:  // SRL C
            case 0x3A:  // SRL D
            case 0x3B:  // SRL E
            case 0x3C:  // SRL H
            case 0x3D:  // SRL L
            case 0x3F:  // SRL A
                this.setRegister8((byte)((nextOpCode & 0x07)), this.opSRL(this.getRegister8((byte)(nextOpCode & 0x07))));
                this.regPC++;
                break;
                
            case 0x3E:  // SRL (HL)
                this.writeMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)), this.opSRL(this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)))));
                this.regPC++;
                break;
                
            case 0x40:  // BIT 0, B
            case 0x41:  // BIT 0, C
            case 0x42:  // BIT 0, D
            case 0x43:  // BIT 0, E
            case 0x44:  // BIT 0, H
            case 0x45:  // BIT 0, L
            case 0x47:  // BIT 0, A
            case 0x48:  // BIT 1, B
            case 0x49:  // BIT 1, C
            case 0x4A:  // BIT 1, D
            case 0x4B:  // BIT 1, E
            case 0x4C:  // BIT 1, H
            case 0x4D:  // BIT 1, L
            case 0x4F:  // BIT 1, A
            case 0x50:  // BIT 2, B
            case 0x51:  // BIT 2, C
            case 0x52:  // BIT 2, D
            case 0x53:  // BIT 2, E
            case 0x54:  // BIT 2, H
            case 0x55:  // BIT 2, L
            case 0x57:  // BIT 2, A
            case 0x58:  // BIT 3, B
            case 0x59:  // BIT 3, C
            case 0x5A:  // BIT 3, D
            case 0x5B:  // BIT 3, E
            case 0x5C:  // BIT 3, H
            case 0x5D:  // BIT 3, L
            case 0x5F:  // BIT 3, A
            case 0x60:  // BIT 4, B
            case 0x61:  // BIT 4, C
            case 0x62:  // BIT 4, D
            case 0x63:  // BIT 4, E
            case 0x64:  // BIT 4, H
            case 0x65:  // BIT 4, L
            case 0x67:  // BIT 4, A
            case 0x68:  // BIT 5, B
            case 0x69:  // BIT 5, C
            case 0x6A:  // BIT 5, D
            case 0x6B:  // BIT 5, E
            case 0x6C:  // BIT 5, H
            case 0x6D:  // BIT 5, L
            case 0x6F:  // BIT 5, A
            case 0x70:  // BIT 6, B
            case 0x71:  // BIT 6, C
            case 0x72:  // BIT 6, D
            case 0x73:  // BIT 6, E
            case 0x74:  // BIT 6, H
            case 0x75:  // BIT 6, L
            case 0x77:  // BIT 6, A
            case 0x78:  // BIT 7, B
            case 0x79:  // BIT 7, C
            case 0x7A:  // BIT 7, D
            case 0x7B:  // BIT 7, E
            case 0x7C:  // BIT 7, H
            case 0x7D:  // BIT 7, L
            case 0x7F:  // BIT 7, A
                this.opBIT((byte)((nextOpCode & 0x38) >> 3), this.getRegister8((byte)(nextOpCode & 0x07)));
                this.regPC++;
                break;
                
            case 0x46:  // BIT 0, (HL)
            case 0x4E:  // BIT 1, (HL)
            case 0x56:  // BIT 2, (HL)
            case 0x5E:  // BIT 3, (HL)
            case 0x66:  // BIT 4, (HL)
            case 0x6E:  // BIT 5, (HL)
            case 0x76:  // BIT 6, (HL)
            case 0x7E:  // BIT 7, (HL)
                this.opBIT((byte)((nextOpCode & 0x38) >> 3), this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02))));
                this.regPC++;
                break;
                
            case 0x80:  // RES 0, B
            case 0x81:  // RES 0, C
            case 0x82:  // RES 0, D
            case 0x83:  // RES 0, E
            case 0x84:  // RES 0, H
            case 0x85:  // RES 0, L
            case 0x87:  // RES 0, A
            case 0x88:  // RES 1, B
            case 0x89:  // RES 1, C
            case 0x8A:  // RES 1, D
            case 0x8B:  // RES 1, E
            case 0x8C:  // RES 1, H
            case 0x8D:  // RES 1, L
            case 0x8F:  // RES 1, A
            case 0x90:  // RES 2, B
            case 0x91:  // RES 2, C
            case 0x92:  // RES 2, D
            case 0x93:  // RES 2, E
            case 0x94:  // RES 2, H
            case 0x95:  // RES 2, L
            case 0x97:  // RES 2, A
            case 0x98:  // RES 3, B
            case 0x99:  // RES 3, C
            case 0x9A:  // RES 3, D
            case 0x9B:  // RES 3, E
            case 0x9C:  // RES 3, H
            case 0x9D:  // RES 3, L
            case 0x9F:  // RES 3, A
            case 0xA0:  // RES 4, B
            case 0xA1:  // RES 4, C
            case 0xA2:  // RES 4, D
            case 0xA3:  // RES 4, E
            case 0xA4:  // RES 4, H
            case 0xA5:  // RES 4, L
            case 0xA7:  // RES 4, A
            case 0xA8:  // RES 5, B
            case 0xA9:  // RES 5, C
            case 0xAA:  // RES 5, D
            case 0xAB:  // RES 5, E
            case 0xAC:  // RES 5, H
            case 0xAD:  // RES 5, L
            case 0xAF:  // RES 5, A
            case 0xB0:  // RES 6, B
            case 0xB1:  // RES 6, C
            case 0xB2:  // RES 6, D
            case 0xB3:  // RES 6, E
            case 0xB4:  // RES 6, H
            case 0xB5:  // RES 6, L
            case 0xB7:  // RES 6, A
            case 0xB8:  // RES 7, B
            case 0xB9:  // RES 7, C
            case 0xBA:  // RES 7, D
            case 0xBB:  // RES 7, E
            case 0xBC:  // RES 7, H
            case 0xBD:  // RES 7, L
            case 0xBF:  // RES 7, A
                this.setRegister8((byte)((nextOpCode & 0x07)), this.opRES((byte)((nextOpCode & 0x38) >> 3), this.getRegister8((byte)(nextOpCode & 0x07))));
                this.regPC++;
                break;
                
            case 0x86:  // RES 0, (HL)
            case 0x8E:  // RES 1, (HL)
            case 0x96:  // RES 2, (HL)
            case 0x9E:  // RES 3, (HL)
            case 0xA6:  // RES 4, (HL)
            case 0xAE:  // RES 5, (HL)
            case 0xB6:  // RES 6, (HL)
            case 0xBE:  // RES 7, (HL)
                this.writeMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)), this.opRES((byte)((nextOpCode & 0x38) >> 3), this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)))));
                this.regPC++;
                break;
                
            case 0xC0:  // SET 0, B
            case 0xC1:  // SET 0, C
            case 0xC2:  // SET 0, D
            case 0xC3:  // SET 0, E
            case 0xC4:  // SET 0, H
            case 0xC5:  // SET 0, L
            case 0xC7:  // SET 0, A
            case 0xC8:  // SET 1, B
            case 0xC9:  // SET 1, C
            case 0xCA:  // SET 1, D
            case 0xCB:  // SET 1, E
            case 0xCC:  // SET 1, H
            case 0xCD:  // SET 1, L
            case 0xCF:  // SET 1, A
            case 0xD0:  // SET 2, B
            case 0xD1:  // SET 2, C
            case 0xD2:  // SET 2, D
            case 0xD3:  // SET 2, E
            case 0xD4:  // SET 2, H
            case 0xD5:  // SET 2, L
            case 0xD7:  // SET 2, A
            case 0xD8:  // SET 3, B
            case 0xD9:  // SET 3, C
            case 0xDA:  // SET 3, D
            case 0xDB:  // SET 3, E
            case 0xDC:  // SET 3, H
            case 0xDD:  // SET 3, L
            case 0xDF:  // SET 3, A
            case 0xE0:  // SET 4, B
            case 0xE1:  // SET 4, C
            case 0xE2:  // SET 4, D
            case 0xE3:  // SET 4, E
            case 0xE4:  // SET 4, H
            case 0xE5:  // SET 4, L
            case 0xE7:  // SET 4, A
            case 0xE8:  // SET 5, B
            case 0xE9:  // SET 5, C
            case 0xEA:  // SET 5, D
            case 0xEB:  // SET 5, E
            case 0xEC:  // SET 5, H
            case 0xED:  // SET 5, L
            case 0xEF:  // SET 5, A
            case 0xF0:  // SET 6, B
            case 0xF1:  // SET 6, C
            case 0xF2:  // SET 6, D
            case 0xF3:  // SET 6, E
            case 0xF4:  // SET 6, H
            case 0xF5:  // SET 6, L
            case 0xF7:  // SET 6, A
            case 0xF8:  // SET 7, B
            case 0xF9:  // SET 7, C
            case 0xFA:  // SET 7, D
            case 0xFB:  // SET 7, E
            case 0xFC:  // SET 7, H
            case 0xFD:  // SET 7, L
            case 0xFF:  // SET 7, A
                this.setRegister8((byte)((nextOpCode & 0x07)), this.opRES((byte)((nextOpCode & 0x38) >> 3), this.getRegister8((byte)(nextOpCode & 0x07))));
                this.regPC++;
                break;
                
            case 0xC6:  // SET 0, (HL)
            case 0xCE:  // SET 1, (HL)
            case 0xD6:  // SET 2, (HL)
            case 0xDE:  // SET 3, (HL)
            case 0xE6:  // SET 4, (HL)
            case 0xEE:  // SET 5, (HL)
            case 0xF6:  // SET 6, (HL)
            case 0xFE:  // SET 7, (HL)
                this.writeMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)), this.opRES((byte)((nextOpCode & 0x38) >> 3), this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)))));
                this.regPC++;
                break;
                
            default:
                throw new OpCodeException("Opcode 0xCB, " + nextOpCode + " is not supported!");
        }
    }
    
    /**
     * subsubOpDDCB - this method handles the opcodes that start with 0xDD, 0xCB
     * 
     * @param d (byte) the displacement
     * @param subOpCode (byte) the opcode
     * @throws OpCodeException 
     * @throws MemoryException 
     */
    protected void subSubOpDDCB(byte d, byte subOpCode) throws OpCodeException, MemoryException {
        switch (Byte.toUnsignedInt(subOpCode)) {
            case 0x06:  // RLC (IX+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIX + (short)d)), this.opRLC(this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)d)))));
                break;
                
            case 0x0E:  // RRC (IX+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIX + (short)d)), this.opRRC(this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)d)))));
                break;
                
            case 0x16:  // RL (IX+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIX + (short)d)), this.opRL(this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)d)))));
                break;
                
            case 0x1E:  // RR (IX+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIX + (short)d)), this.opRR(this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)d)))));
                break;
                
            case 0x26:  // SLA (IX+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIX + (short)d)), this.opSLA(this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)d)))));
                break;
                
            case 0x2E:  // SRA (IX+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIX + (short)d)), this.opSRA(this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)d)))));
                break;
                
            case 0x3E:  // SRL (IX+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIX + (short)d)), this.opSRL(this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)d)))));
                break;
                
            case 0x46:  // BIT 0, (IX+d)
            case 0x4E:  // BIT 1, (IX+d)
            case 0x56:  // BIT 2, (IX+d)
            case 0x5E:  // BIT 3, (IX+d)
            case 0x66:  // BIT 4, (IX+d)
            case 0x6E:  // BIT 5, (IX+d)
            case 0x76:  // BIT 6, (IX+d)
            case 0x7E:  // BIT 7, (IX+d)
                this.opBIT((byte)((subOpCode & 0x38) >> 3), this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)d))));
                break;
                
            case 0x86:  // RES 0, (IX+d)
            case 0x8E:  // RES 1, (IX+d)
            case 0x96:  // RES 2, (IX+d)
            case 0x9E:  // RES 3, (IX+d)
            case 0xA6:  // RES 4, (IX+d)
            case 0xAE:  // RES 5, (IX+d)
            case 0xB6:  // RES 6, (IX+d)
            case 0xBE:  // RES 7, (IX+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIX + (short)d)), this.opRES((byte)((subOpCode & 0x38) >> 3), this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)d)))));
                break;
                
            case 0xC6:  // SET 0, (IX+d)
            case 0xCE:  // SET 1, (IX+d)
            case 0xD6:  // SET 2, (IX+d)
            case 0xDE:  // SET 3, (IX+d)
            case 0xE6:  // SET 4, (IX+d)
            case 0xEE:  // SET 5, (IX+d)
            case 0xF6:  // SET 6, (IX+d)
            case 0xFE:  // SET 7, (IX+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIX + (short)d)), this.opSET((byte)((subOpCode & 0x38) >> 3), this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)d)))));
                break;
                
            default:
                throw new OpCodeException("Opcode 0xDD, 0xCB, " + subOpCode + " is not supported!");
        }
    }
    
    /**
     * subOpDD - this method handles the opcodes that start with 0xDD - this is
     * a large set of opcodes!
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void subOpDD() throws OpCodeException, MemoryException {
        byte nextOpCode = this.readMemory8(Short.toUnsignedLong(this.regPC));
        
        switch (Byte.toUnsignedInt(nextOpCode)) {
            case 0x09:  // ADD IX, BC
            case 0x19:  // ADD IX, DE
            case 0x29:  // ADD IX, IX
            case 0x39:  // ADD IX, SP
                this.regIX = this.opADD16(this.getRegister16((byte)0x02), this.getRegister16_IX((byte)((nextOpCode & 0x30) >> 4)));
                this.regPC++;
                break;
                
            case 0x21:  // LD IX, nn
                this.regIX = this.opLD16(this.readMemory16(Short.toUnsignedLong(this.regPC)+1));
                this.regPC+=3;
                break;
                
            case 0x22:  // LD (nn), IX
                this.writeMemory16(Short.toUnsignedLong(this.readMemory16(Short.toUnsignedLong(this.regPC)+1)), this.regIX);
                this.regPC+=3;
                break;
                
            case 0x23:  // INC IX
                this.regIX = this.opINC16(this.regIX);
                this.regPC++;
                break;
                
            case 0x2A:  // LD IX, (nn)
                this.regIX = this.readMemory16(Short.toUnsignedLong(this.readMemory16(Short.toUnsignedLong(this.regPC)+1)));
                this.regPC++;
                break;
                
            case 0x2B:  // DEC IX
                this.regIX = this.opDEC16(this.regIX);
                this.regPC++;
                break;
                
            case 0x34:  // INC (IX+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIX + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))), this.opINC8(this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))))));
                this.regPC+=2;
                break;
                
            case 0x35:  // DEC (IX+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIX + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))), this.opDEC8(this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))))));
                this.regPC+=2;
                break;
                
            case 0x36:  // LD (IX+d), n
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIX + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))), this.readMemory8(Short.toUnsignedLong(this.regPC)+2));
                this.regPC+=3;
                break;
                
            case 0x46:  // LD B, (IX+d)
            case 0x4E:  // LD C, (IX+d)
            case 0x56:  // LD D, (IX+d)
            case 0x5E:  // LD E, (IX+d)
            case 0x66:  // LD H, (IX+d)
            case 0x6E:  // LD L, (IX+d)
            case 0x7E:  // LD A, (IX+d)
                this.setRegister8((byte)((nextOpCode & 0x38) >> 3), this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1))))));
                this.regPC+=2;
                break;
                
            case 0x70:  // LD (IX+d), B
            case 0x71:  // LD (IX+d), C
            case 0x72:  // LD (IX+d), D
            case 0x73:  // LD (IX+d), E
            case 0x74:  // LD (IX+d), H
            case 0x75:  // LD (IX+d), L
            case 0x77:  // LD (IX+d), A
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIX + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))), this.getRegister8((byte)((nextOpCode & 0x07))));
                this.regPC+=2;
                break;
                
            case 0x86:  // ADD A, (IX+d)
                this.setRegister8((byte)0x07, this.opADD8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))))));
                this.regPC+=2;
                break;
                
            case 0x8E:  // ADC A, (IX+d)
                this.setRegister8((byte)0x07, this.opADC8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))))));
                this.regPC+=2;
                break;
                
            case 0x96:  // SUB A, (IX+d)
                this.setRegister8((byte)0x07, this.opSUB8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))))));
                this.regPC+=2;
                break;
                
            case 0x9E:  // SBC A, (IX+d)
                this.setRegister8((byte)0x07, this.opSBC8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))))));
                this.regPC+=2;
                break;
                
            case 0xA6:  // AND A, (IX+d)
                this.setRegister8((byte)0x07, this.opAND8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))))));
                this.regPC+=2;
                break;
                
            case 0xAE:  // XOR A, (IX+d)
                this.setRegister8((byte)0x07, this.opXOR8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))))));
                this.regPC+=2;
                break;
                
            case 0xB6:  // OR A, (IX+d)
                this.setRegister8((byte)0x07, this.opOR8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))))));
                this.regPC+=2;
                break;
                
            case 0xBE:  // CP A, (IX+d)
                this.opSUB8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong((short)(this.regIX + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1))))));
                this.regPC+=2;
                break;
                
            case 0xCB:  // Special handling of DD CB          
                this.subSubOpDDCB(this.readMemory8(Short.toUnsignedLong(this.regPC)+1), this.readMemory8(Short.toUnsignedLong(this.regPC)+2));
                this.regPC+=3;
                break;
                
            case 0xE1:  // POP IX
                this.opPOPIX();
                this.regPC++;
                break;
                
            case 0xE3:  // EX (SP), IX
                this.opEX_SP_IX();
                this.regPC++;
                break;
                
            case 0xE5:  // PUSH IX
                this.opPUSHIX();
                this.regPC++;
                break;
                
            case 0xE9:  // JP (IX)
                this.regPC = this.regIX;
                break;
                
            case 0xF9:  // LD SP, IX
                this.regSP = this.regIX;
                this.regPC++;
                break;
                
            default:
                throw new OpCodeException("Opcode 0xDD, " + nextOpCode + " is not supported!");
        }
    }
    
    /**
     * subOpED - this method handles the opcodes that start with 0xED - this is
     * a large set of opcodes!
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void subOpED() throws OpCodeException, MemoryException {
        byte nextOpCode = this.readMemory8(Short.toUnsignedLong(this.regPC));
        
        switch (Byte.toUnsignedInt(nextOpCode)) {
            case 0x40:  // IN B, (C)
            case 0x48:  // IN C, (C)
            case 0x50:  // IN D, (C)
            case 0x58:  // IN E, (C)
            case 0x60:  // IN H, (C)
            case 0x68:  // IN L, (C)
            case 0x78:  // IN A, (C)
                this.setRegister8((byte)((nextOpCode & 0x38) >> 3), this.opIN8(this.getRegister8((byte)0x01)));
                this.regPC++;
                break;
                
            case 0x41:  // OUT (C), B
            case 0x49:  // OUT (C), C
            case 0x51:  // OUT (C), D
            case 0x59:  // OUT (C), E
            case 0x61:  // OUT (C), H
            case 0x69:  // OUT (C), L
            case 0x79:  // OUT (C), A
                this.opOUT8(this.getRegister8((byte)0x01), this.getRegister8((byte)((nextOpCode & 0x38) >> 3)));
                this.regPC++;
                break;
                
            case 0x42:  // SBC HL, BC
            case 0x52:  // SBC HL, DE
            case 0x62:  // SBC HL, HL
            case 0x72:  // SBC HL, SP
                this.setRegister16((byte)0x02, this.opSBC16(this.getRegister16((byte)0x02), this.getRegister16((byte)((nextOpCode & 0x30) >> 4))));
                this.regPC++;
                break;
                
            case 0x43:  // LD (nn), BC
            case 0x53:  // LD (nn), DE
            case 0x63:  // LD (nn), HL
            case 0x73:  // LD (nn), SP
                this.writeMemory16(Short.toUnsignedLong(this.readMemory16((Short.toUnsignedLong(this.regPC)+1))), this.opLD16(this.getRegister16((byte)((nextOpCode & 0x30) >> 4))));
                this.regPC++;
                break;
                
            case 0x44:  // NEG A
                this.setRegister8((byte)0x07, this.opSUB8((byte)0, this.getRegister8((byte)0x07)));
                this.regPC++;
                break;
                
            case 0x45:  // RETN
                // TODO: Code Interupt masking!
                this.regPC = (short)(this.readMemory16(Short.toUnsignedLong(this.regSP)) - 1);   // -1 because this it gets added by 1 in the main routine!
                this.regSP+=2;
                break;
                
            case 0x46:  // IM 0
                // TODO: Code Interupt masking!
                this.regPC++;
                break;
                
            case 0x47:  // LD I, A
                this.regI = this.regA;
                this.regPC++;
                break;
                
            case 0x4A:  // ADC HL, BC
            case 0x5A:  // ADC HL, DE
            case 0x6A:  // ADC HL, HL
            case 0x7A:  // ADC HL, SP
                this.setRegister16((byte)0x02, this.opADC16(this.getRegister16((byte)0x02), this.getRegister16((byte)((nextOpCode & 0x30) >> 4))));
                this.regPC++;
                break;
                
            case 0x4B:  // LD BC, (nn)
            case 0x5B:  // LD DE, (nn)
            case 0x6B:  // LD HL, (nn)
            case 0x7B:  // LD SP, (nn)
                this.setRegister16((byte)((nextOpCode & 0x30) >> 4), this.readMemory16(Short.toUnsignedLong(this.readMemory16(Short.toUnsignedLong(this.regPC)+1))));
                this.regPC+=3;
                break;
                
            case 0x4D:  // RETI
                // TODO: Code Interupt masking!
                this.regPC = (short)(this.readMemory16(Short.toUnsignedLong(this.regSP)) - 1);   // -1 because this it gets added by 1 in the main routine!
                this.regSP+=2;
                break;
                
            case 0x4F:  // LD R, A
                this.regR = this.regA;
                this.regPC++;
                break;
                
            case 0x56:  // IM 1
                // TODO: Code Interupt masking!
                this.regPC++;
                break;
                
            case 0x57:  // LD A, I
                this.regA = this.regI;
                this.regPC++;
                break;
                
            case 0x5E:  // IM 2
                // TODO: Code Interupt masking!
                this.regPC++;
                break;
                
            case 0x5F:  // LD A, R
                this.regA = this.regR;
                this.regPC++;
                break;
                
            case 0x67:  // RRD
                this.opRRD();
                this.regPC++;
                break;
                
            case 0x6F:  // RLD
                this.opRLD();
                this.regPC++;
                break;
                
            case 0xA0:  // LDI
                this.opLDI();
                this.regPC++;
                break;
                
            case 0xA1:  // CPI
                this.opCPI();
                this.regPC++;
                break;
                
            case 0xA2:  // INI (HL), (BC)
                this.writeMemory8(this.getRegister16((byte)0x02), this.opIN8(this.getRegister8((byte)0x01)));
                this.regB--;
                this.setRegister16((byte)0x02, (short)(this.getRegister16((byte)0x02) + 1));
                this.setFlag(FLAG_N);
                this.setFlag(FLAG_Z, this.regB == 0);
                break;
                
            case 0xA3:  // OUTI (BC), (HL)
                this.opOUT8(this.getRegister8((byte)0x01), this.readMemory8(this.getRegister16((byte)0x02)));
                this.regB--;
                this.setRegister16((byte)0x02, (short)(this.getRegister16((byte)0x02) + 1));
                this.setFlag(FLAG_N);
                this.setFlag(FLAG_Z, this.regB == 0);
                break;
                
            case 0xA8:  // LDD
                this.opLDD();
                this.regPC++;
                break;
                
            case 0xA9:  // CPD
                this.opCPD();
                this.regPC++;
                break;
                
            case 0xAA:  // IND (HL), (BC)
                this.writeMemory8(this.getRegister16((byte)0x02), this.opIN8(this.getRegister8((byte)0x01)));
                this.regB--;
                this.setRegister16((byte)0x02, (short)(this.getRegister16((byte)0x02) - 1));
                this.setFlag(FLAG_N);
                this.setFlag(FLAG_Z, this.regB == 0);
                break;
                
            case 0xAB:  // OUTD (BC), (HL)
                this.opOUT8(this.getRegister8((byte)0x01), this.readMemory8(this.getRegister16((byte)0x02)));
                this.regB--;
                this.setRegister16((byte)0x02, (short)(this.getRegister16((byte)0x02) - 1));
                this.setFlag(FLAG_N);
                this.setFlag(FLAG_Z, this.regB == 0);
                break;
                
            case 0xB0:  // LDIR
                this.opLDI();
                if(this.checkFlag(FLAG_PV))
                    this.regPC--;
                else
                    this.regPC++;
                break;
                
            case 0xB1:  // CPIR
                this.opCPI();
                if(this.checkFlag(FLAG_PV))
                    this.regPC--;
                else
                    this.regPC++;
                break;
                
            case 0xB2:  // INIR (HL), (BC)
                this.writeMemory8(this.getRegister16((byte)0x02), this.opIN8(this.getRegister8((byte)0x01)));
                this.regB--;
                this.setRegister16((byte)0x02, (short)(this.getRegister16((byte)0x02) + 1));
                this.setFlag(FLAG_N);
                this.setFlag(FLAG_Z, this.regB == 0);
                if(regB == 0)
                    this.regPC++;
                else
                    this.regPC--;
                break;
                
            case 0xB3:  // OTIR (BC), (HL)
                this.opOUT8(this.getRegister8((byte)0x01), this.readMemory8(this.getRegister16((byte)0x02)));
                this.regB--;
                this.setRegister16((byte)0x02, (short)(this.getRegister16((byte)0x02) + 1));
                this.setFlag(FLAG_N);
                this.setFlag(FLAG_Z, this.regB == 0);
                if(regB == 0)
                    this.regPC++;
                else
                    this.regPC--;
                break;
                
            case 0xB8:  // LDDR
                this.opLDD();
                if(this.checkFlag(FLAG_PV))
                    this.regPC--;
                else
                    this.regPC++;
                break;
                
            case 0xB9:  // CPDR
                this.opCPD();
                if(this.checkFlag(FLAG_PV))
                    this.regPC--;
                else
                    this.regPC++;
                break;
                
            case 0xBA:  // INDR (HL), (BC)
                this.writeMemory8(this.getRegister16((byte)0x02), this.opIN8(this.getRegister8((byte)0x01)));
                this.regB--;
                this.setRegister16((byte)0x02, (short)(this.getRegister16((byte)0x02) - 1));
                this.setFlag(FLAG_N);
                this.setFlag(FLAG_Z, this.regB == 0);
                if(regB == 0)
                    this.regPC++;
                else
                    this.regPC--;
                break;
                
            case 0xBB:  // OTDR (BC), (HL)
                this.opOUT8(this.getRegister8((byte)0x01), this.readMemory8(this.getRegister16((byte)0x02)));
                this.regB--;
                this.setRegister16((byte)0x02, (short)(this.getRegister16((byte)0x02) - 1));
                this.setFlag(FLAG_N);
                this.setFlag(FLAG_Z, this.regB == 0);
                if(regB == 0)
                    this.regPC++;
                else
                    this.regPC--;
                break;
                
            default:
                throw new OpCodeException("Opcode 0xED, " + nextOpCode + " is not supported!");
        }
    }
    
    /**
     * subsubOpFDCB - this method handles the opcodes that start with 0xFD, 0xCB
     * 
     * @param d (byte) the displacement
     * @param subOpCode (byte) the opcode
     * @throws OpCodeException 
     * @throws MemoryException 
     */
    protected void subSubOpFDCB(byte d, byte subOpCode) throws OpCodeException, MemoryException {
        switch (Byte.toUnsignedInt(subOpCode)) {
            case 0x06:  // RLC (IY+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIY + (short)d)), this.opRLC(this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)d)))));
                break;
                
            case 0x0E:  // RRC (IY+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIY + (short)d)), this.opRRC(this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)d)))));
                break;
                
            case 0x16:  // RL (IY+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIY + (short)d)), this.opRL(this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)d)))));
                break;
                
            case 0x1E:  // RR (IY+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIY + (short)d)), this.opRR(this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)d)))));
                break;
                
            case 0x26:  // SLA (IY+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIY + (short)d)), this.opSLA(this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)d)))));
                break;
                
            case 0x2E:  // SRA (IY+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIY + (short)d)), this.opSRA(this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)d)))));
                break;
                
            case 0x3E:  // SRL (IY+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIY + (short)d)), this.opSRL(this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)d)))));
                break;
                
            case 0x46:  // BIT 0, (IY+d)
            case 0x4E:  // BIT 1, (IY+d)
            case 0x56:  // BIT 2, (IY+d)
            case 0x5E:  // BIT 3, (IY+d)
            case 0x66:  // BIT 4, (IY+d)
            case 0x6E:  // BIT 5, (IY+d)
            case 0x76:  // BIT 6, (IY+d)
            case 0x7E:  // BIT 7, (IY+d)
                this.opBIT((byte)((subOpCode & 0x38) >> 3), this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)d))));
                break;
                
            case 0x86:  // RES 0, (IY+d)
            case 0x8E:  // RES 1, (IY+d)
            case 0x96:  // RES 2, (IY+d)
            case 0x9E:  // RES 3, (IY+d)
            case 0xA6:  // RES 4, (IY+d)
            case 0xAE:  // RES 5, (IY+d)
            case 0xB6:  // RES 6, (IY+d)
            case 0xBE:  // RES 7, (IY+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIY + (short)d)), this.opRES((byte)((subOpCode & 0x38) >> 3), this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)d)))));
                break;
                
            case 0xC6:  // SET 0, (IY+d)
            case 0xCE:  // SET 1, (IY+d)
            case 0xD6:  // SET 2, (IY+d)
            case 0xDE:  // SET 3, (IY+d)
            case 0xE6:  // SET 4, (IY+d)
            case 0xEE:  // SET 5, (IY+d)
            case 0xF6:  // SET 6, (IY+d)
            case 0xFE:  // SET 7, (IY+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIY + (short)d)), this.opSET((byte)((subOpCode & 0x38) >> 3), this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)d)))));
                break;
                
            default:
                throw new OpCodeException("Opcode 0xFD, 0xCB, " + subOpCode + " is not supported!");
        }
    }
    
    /**
     * subOpFD - this method handles the opcodes that start with 0xFE - this is
     * a large set of opcodes!
     * 
     * @throws OpCodeException
     * @throws MemoryException 
     */
    protected void subOpFD() throws OpCodeException, MemoryException {
        byte nextOpCode = this.readMemory8(Short.toUnsignedLong(this.regPC));
        
        switch (Byte.toUnsignedInt(nextOpCode)) {
            case 0x09:  // ADD IY, BC
            case 0x19:  // ADD IY, DE
            case 0x29:  // ADD IY, IX
            case 0x39:  // ADD IY, SP
                this.regIY = this.opADD16(this.getRegister16((byte)0x02), this.getRegister16_IY((byte)((nextOpCode & 0x30) >> 4)));
                this.regPC++;
                break;
                
            case 0x21:  // LD IY, nn
                this.regIY = this.opLD16(this.readMemory16(Short.toUnsignedLong(this.regPC)+1));
                this.regPC+=3;
                break;
                
            case 0x22:  // LD (nn), IY
                this.writeMemory16(Short.toUnsignedLong(this.readMemory16(Short.toUnsignedLong(this.regPC)+1)), this.regIY);
                this.regPC+=3;
                break;
                
            case 0x23:  // INC IY
                this.regIY = this.opINC16(this.regIY);
                this.regPC++;
                break;
                
            case 0x2A:  // LD IY, (nn)
                this.regIY = this.readMemory16(Short.toUnsignedLong(this.readMemory16(Short.toUnsignedLong(this.regPC)+1)));
                this.regPC++;
                break;
                
            case 0x2B:  // DEC IY
                this.regIY = this.opDEC16(this.regIY);
                this.regPC++;
                break;
                
            case 0x34:  // INC (IY+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIY + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))), this.opINC8(this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))))));
                this.regPC+=2;
                break;
                
            case 0x35:  // DEC (IY+d)
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIY + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))), this.opDEC8(this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))))));
                this.regPC+=2;
                break;
                
            case 0x36:  // LD (IY+d), n
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIY + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))), this.readMemory8(Short.toUnsignedLong(this.regPC)+2));
                this.regPC+=3;
                break;
                
            case 0x46:  // LD B, (IY+d)
            case 0x4E:  // LD C, (IY+d)
            case 0x56:  // LD D, (IY+d)
            case 0x5E:  // LD E, (IY+d)
            case 0x66:  // LD H, (IY+d)
            case 0x6E:  // LD L, (IY+d)
            case 0x7E:  // LD A, (IY+d)
                this.setRegister8((byte)((nextOpCode & 0x38) >> 3), this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1))))));
                this.regPC+=2;
                break;
                
            case 0x70:  // LD (IY+d), B
            case 0x71:  // LD (IY+d), C
            case 0x72:  // LD (IY+d), D
            case 0x73:  // LD (IY+d), E
            case 0x74:  // LD (IY+d), H
            case 0x75:  // LD (IY+d), L
            case 0x77:  // LD (IY+d), A
                this.writeMemory8(Short.toUnsignedLong((short)(this.regIY + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))), this.getRegister8((byte)((nextOpCode & 0x07))));
                this.regPC+=2;
                break;
                
            case 0x86:  // ADD A, (IY+d)
                this.setRegister8((byte)0x07, this.opADD8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))))));
                this.regPC+=2;
                break;
                
            case 0x8E:  // ADC A, (IY+d)
                this.setRegister8((byte)0x07, this.opADC8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))))));
                this.regPC+=2;
                break;
                
            case 0x96:  // SUB A, (IY+d)
                this.setRegister8((byte)0x07, this.opSUB8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))))));
                this.regPC+=2;
                break;
                
            case 0x9E:  // SBC A, (IY+d)
                this.setRegister8((byte)0x07, this.opSBC8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))))));
                this.regPC+=2;
                break;
                
            case 0xA6:  // AND A, (IY+d)
                this.setRegister8((byte)0x07, this.opAND8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))))));
                this.regPC+=2;
                break;
                
            case 0xAE:  // XOR A, (IY+d)
                this.setRegister8((byte)0x07, this.opXOR8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))))));
                this.regPC+=2;
                break;
                
            case 0xB6:  // OR A, (IY+d)
                this.setRegister8((byte)0x07, this.opOR8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)))))));
                this.regPC+=2;
                break;
                
            case 0xBE:  // CP A, (IY+d)
                this.opSUB8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong((short)(this.regIY + (short)(this.readMemory8(Short.toUnsignedLong(this.regPC)+1))))));
                this.regPC+=2;
                break;
                
            case 0xCB:  // Special handling of DD CB          
                this.subSubOpFDCB(this.readMemory8(Short.toUnsignedLong(this.regPC)+1), this.readMemory8(Short.toUnsignedLong(this.regPC)+2));
                this.regPC+=3;
                break;
                
            case 0xE1:  // POP IY
                this.opPOPIY();
                this.regPC++;
                break;
                
            case 0xE3:  // EX (SP), IY
                this.opEX_SP_IY();
                this.regPC++;
                break;
                
            case 0xE5:  // PUSH IY
                this.opPUSHIY();
                this.regPC++;
                break;
                
            case 0xE9:  // JP (IY)
                this.regPC = this.regIY;
                break;
                
            case 0xF9:  // LD SP, IY
                this.regSP = this.regIY;
                this.regPC++;
                break;
                
            default:
                throw new OpCodeException("Opcode 0xFD, " + nextOpCode + " is not supported!");
        }
    }
    
    /**
     * runNextOpCode - this method runs a single operation at the current 
     * program counter address.
     * 
     * @throws MemoryException
     * @throws OpCodeException 
     */
    @Override
    public int runNextOpCode() throws MemoryException, OpCodeException {
        Iterator<Memory> memIterator = memoryBlocks.iterator();
        
        while(memIterator.hasNext()) {
            Memory mem = memIterator.next();
            
            try {
                byte opCode = mem.getByte(this.regPC);
                byte opCode2, opCode3, opCode4;
                short nn;
                int address;
                
                switch (Byte.toUnsignedInt(opCode)) {
                    case 0x00: // NOP
                        this.regPC++;
                        break;
                        
                    case 0x01:  // LD BC, nn
                    case 0x11:  // LD DE, nn
                    case 0x21:  // LD HL, nn
                    case 0x31:  // LD SP, nn
                        this.setRegister16((byte)((opCode & 0x30) >> 4),this.readMemory16(this.regPC + 1));
                        this.regPC += 3;
                        break;
                        
                    case 0x02:  // LD (BC), A
                        this.writeMemory8((long)(Short.toUnsignedInt(this.getRegister16((byte)0))), this.opLD8(this.getRegister8((byte)0)));
                        this.regPC++;
                        break;
                        
                    case 0x03:  // INC BC
                    case 0x13:  // INC DE
                    case 0x23:  // INC HL
                    case 0x33:  // INC SP
                        this.setRegister16((byte)((opCode & 0x30) >> 4), this.opINC16(this.getRegister16((byte)((opCode & 0x30) >> 4))));
                        this.regPC++;
                        break;
                        
                    case 0x04:  // INC B,   Flags changed: SZHPN
                    case 0x0C:  // INC C,   Flags changed: SZHPN
                    case 0x14:  // INC D,   Flags changed: SZHPN
                    case 0x1C:  // INC E,   Flags changed: SZHPN
                    case 0x24:  // INC H,   Flags changed: SZHPN
                    case 0x2C:  // INC L,   Flags changed: SZHPN
                    case 0x3C:  // INC A,   Flags changed: SZHPN
                        this.setRegister8((byte)((opCode & 0x38) >> 3), opINC8(this.getRegister8((byte)((opCode & 0x38) >> 3))));
                        this.regPC++;
                        break;
                        
                    case 0x05:  // DEC B,   Flags changed: SZHPN
                    case 0x0D:  // DEC C,   Flags changed: SZHPN
                    case 0x15:  // DEC D,   Flags changed: SZHPN
                    case 0x1D:  // DEC E,   Flags changed: SZHPN
                    case 0x25:  // DEC H,   Flags changed: SZHPN
                    case 0x2D:  // DEC L,   Flags changed: SZHPN
                    case 0x3D:  // DEC A,   Flags changed: SZHPN
                        this.setRegister8((byte)((opCode & 0x38) >> 3), opDEC8(this.getRegister8((byte)((opCode & 0x38) >> 3))));
                        this.regPC++;
                        break;
                        
                    case 0x06:  // LD B, n
                    case 0x0E:  // LD C, n
                    case 0x16:  // LD D, n
                    case 0x1E:  // LD E, n
                    case 0x26:  // LD H, n
                    case 0x2E:  // LD L, n
                    case 0x3E:  // LD A, n
                        this.setRegister8((byte)((opCode & 0x38) >> 3), opLD8(this.readMemory8(this.regPC+1)));
                        this.regPC+=2;
                        break;
                        
                    case 0x07:  // RLCA
                        this.opRLCA();
                        this.regPC++;
                        break;
                        
                    case 0x08:  // EX AF, AF'
                        this.opEX_AF_AF2();
                        this.regPC++;
                        break;
                        
                    case 0x09:  // ADD HL, BC,  Flags changed: HNC
                    case 0x19:  // ADD HL, DE,  Flags changed: HNC
                    case 0x29:  // ADD HL, HL,  Flags changed: HNC
                    case 0x39:  // ADD HL, SP,  Flags changed: HNC
                        this.setRegister16((byte)2, this.opADD16(this.getRegister16((byte)2), this.getRegister16((byte)((opCode & 0x30) >> 4))));
                        this.regPC++;
                        break;
                        
                    case 0x0A:  // LD A, (BC)
                    case 0x1A:  // LD A, (DE)
                        this.setRegister8((byte)7, this.opLD8(this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)((opCode & 0x30) >> 4))))));
                        this.regPC++;
                        break;
                        
                    case 0x0B:  // DEC BC
                    case 0x1B:  // DEC DE
                    case 0x2B:  // DEC HL
                    case 0x3B:  // DEC SP
                        this.setRegister16((byte)((opCode & 0x30) >> 4), this.opDEC16(this.getRegister16((byte)((opCode & 0x30) >> 4))));
                        this.regPC++;
                        break;
                        
                    case 0x0F:  // RRCA
                        this.opRRCA();
                        this.regPC++;
                        break;
                        
                    case 0x10:  // DJNZ n
                        this.opDJNZ(this.readMemory8(this.regPC+1));
                        this.regPC+=2;
                        break;
                        
                    case 0x12:  // LD (DE), A
                        this.writeMemory8(this.getRegister16((byte)1), this.opLD8(this.getRegister8((byte)7)));
                        this.regPC++;
                        break;
                        
                    case 0x17:  // RLA
                        this.opRLA();
                        this.regPC++;
                        break;
                        
                    case 0x18:  // JR n
                        this.opJR(this.readMemory8(this.regPC+1));
                        this.regPC+=2;
                        break;
                        
                    case 0x1F:  // RRA
                        this.opRRA();
                        this.regPC++;
                        break;
                        
                    case 0x20:  // JRNZ n
                        this.opJRNZ(this.readMemory8(this.regPC+1));
                        this.regPC+=2;
                        break;
                        
                    case 0x22:  // LD (nn), HL
                        this.writeMemory16(Short.toUnsignedLong(this.readMemory16(this.regPC+1)), this.getRegister16((byte)0x02));
                        this.regPC+=3;
                        break;
                        
                    case 0x27:  // DAA
                        this.opDAA();
                        this.regPC++;
                        break;
                        
                    case 0x28:  // JRZ n
                        this.opJRZ(this.readMemory8(this.regPC+1));
                        this.regPC+=2;
                        break;
                        
                    case 0x2A:  // LD HL, (nn)
                        this.setRegister16((byte)0x02, this.opLD16(this.readMemory16(this.readMemory16(this.regPC+1))));
                        this.regPC+=3;
                        break;
                        
                    case 0x2F:  // CPL
                        this.opCPL();
                        this.regPC++;
                        break;
                        
                    case 0x30:  // JRNC n
                        this.opJRNC(this.readMemory8(this.regPC+1));
                        this.regPC+=2;
                        break;
                        
                    case 0x32:  // LD (nn), A
                        this.writeMemory8(Short.toUnsignedLong(this.readMemory16(this.regPC+1)), this.getRegister8((byte)0x07));
                        this.regPC+=3;
                        break;
                        
                    case 0x34:  // INC (HL)
                        this.writeMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)), this.opINC8(this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)))));
                        this.regPC++;
                        break;
                        
                    case 0x35:  // DEC (HL)
                        this.writeMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)), this.opDEC8(this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)))));
                        this.regPC++;
                        break;
                        
                    case 0x36:  // LD (HL), n
                        this.writeMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)), this.opLD8(this.readMemory8(this.regPC+1)));
                        this.regPC+=2;
                        break;
                        
                    case 0x37:  // SCF
                        this.opSCF();
                        this.regPC++;
                        break;
                        
                    case 0x38:  // JRC n
                        this.opJRC(this.readMemory8(this.regPC+1));
                        this.regPC+=2;
                        break;
                        
                    case 0x3A:  // LD A, (nn)
                        this.setRegister8((byte)0x07, this.opLD8(this.readMemory8(Short.toUnsignedLong(this.readMemory16(this.regPC+1)))));
                        this.regPC+=3;
                        break;
                        
                    case 0x3F:  // CCF
                        this.opCCF();
                        this.regPC++;
                        break;
                        
                    case 0x40:  // LD B, B'
                    case 0x41:  // LD B, C'
                    case 0x42:  // LD B, D'
                    case 0x43:  // LD B, E'
                    case 0x44:  // LD B, H'
                    case 0x45:  // LD B, L'
                    case 0x47:  // LD B, A'
                    case 0x48:  // LD C, B'
                    case 0x49:  // LD C, C'
                    case 0x4A:  // LD C, D'
                    case 0x4B:  // LD C, E'
                    case 0x4C:  // LD C, H'
                    case 0x4D:  // LD C, L'
                    case 0x4F:  // LD C, A'
                    case 0x50:  // LD D, B'
                    case 0x51:  // LD D, C'
                    case 0x52:  // LD D, D'
                    case 0x53:  // LD D, E'
                    case 0x54:  // LD D, H'
                    case 0x55:  // LD D, L'
                    case 0x57:  // LD D, A'
                    case 0x58:  // LD E, B'
                    case 0x59:  // LD E, C'
                    case 0x5A:  // LD E, D'
                    case 0x5B:  // LD E, E'
                    case 0x5C:  // LD E, H'
                    case 0x5D:  // LD E, L'
                    case 0x5F:  // LD E, A'
                    case 0x60:  // LD H, B'
                    case 0x61:  // LD H, C'
                    case 0x62:  // LD H, D'
                    case 0x63:  // LD H, E'
                    case 0x64:  // LD H, H'
                    case 0x65:  // LD H, L'
                    case 0x67:  // LD H, A'
                    case 0x68:  // LD L, B'
                    case 0x69:  // LD L, C'
                    case 0x6A:  // LD L, D'
                    case 0x6B:  // LD L, E'
                    case 0x6C:  // LD L, H'
                    case 0x6D:  // LD L, L'
                    case 0x6F:  // LD L, A'
                    case 0x78:  // LD A, B'
                    case 0x79:  // LD A, C'
                    case 0x7A:  // LD A, D'
                    case 0x7B:  // LD A, E'
                    case 0x7C:  // LD A, H'
                    case 0x7D:  // LD A, L'
                    case 0x7F:  // LD A, A'
                        this.setRegister8((byte)((opCode & 0x38) >> 3), this.opLD8(this.getRegister8((byte)((opCode & 0x07) | 0x08))));
                        this.regPC++;
                        break;
                        
                    case 0x70:  // LD (HL), B
                    case 0x71:  // LD (HL), C
                    case 0x72:  // LD (HL), D
                    case 0x73:  // LD (HL), E
                    case 0x74:  // LD (HL), H
                    case 0x75:  // LD (HL), L
                    case 0x77:  // LD (HL), A
                        this.writeMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)), this.getRegister8((byte)(opCode & 0x07)));
                        
                    case 0x46:  // LD B, (HL)
                    case 0x4E:  // LD C, (HL)
                    case 0x56:  // LD D, (HL)
                    case 0x5E:  // LD E, (HL)
                    case 0x66:  // LD H, (HL)
                    case 0x6E:  // LD L, (HL)
                    case 0x7E:  // LD A, (HL)
                        this.setRegister8((byte)((opCode & 0x38) >> 3), this.opLD8(this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)))));
                        this.regPC++;
                        break;
                        
                    case 0x76:  // HALT
                        this.opHALT();
                        this.regPC++;
                        break;
                        
                    case 0x80:  // ADD A, B
                    case 0x81:  // ADD A, C
                    case 0x82:  // ADD A, D
                    case 0x83:  // ADD A, E
                    case 0x84:  // ADD A, H
                    case 0x85:  // ADD A, L
                    case 0x87:  // ADD A, A
                        this.setRegister8((byte)0x07, this.opADD8(this.getRegister8((byte)0x07), this.getRegister8((byte)((opCode & 0x07)))));
                        this.regPC++;
                        break;
                        
                    case 0x86:  // ADD A, (HL)
                        this.setRegister8((byte)0x07, this.opADD8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)))));
                        this.regPC++;
                        break;
                        
                    case 0x88:  // ADC A, B
                    case 0x89:  // ADC A, C
                    case 0x8A:  // ADC A, D
                    case 0x8B:  // ADC A, E
                    case 0x8C:  // ADC A, H
                    case 0x8D:  // ADC A, L
                    case 0x8F:  // ADX A, A
                        this.setRegister8((byte)0x07, this.opADC8(this.getRegister8((byte)0x07), this.getRegister8((byte)((opCode & 0x07)))));
                        this.regPC++;
                        break;
                        
                    case 0x8E:  // ADC A, (HL)
                        this.setRegister8((byte)0x07, this.opADC8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)))));
                        this.regPC++;
                        break;
                        
                    case 0x90:  // SUB A, B
                    case 0x91:  // SUB A, C
                    case 0x92:  // SUB A, D
                    case 0x93:  // SUB A, E
                    case 0x94:  // SUB A, H
                    case 0x95:  // SUB A, L
                    case 0x97:  // SUB A, A
                        this.setRegister8((byte)0x07, this.opSUB8(this.getRegister8((byte)0x07), this.getRegister8((byte)((opCode & 0x07)))));
                        this.regPC++;
                        break;
                        
                    case 0x96:  // SUB A, (HL)
                        this.setRegister8((byte)0x07, this.opSUB8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)))));
                        this.regPC++;
                        break;
                        
                    case 0x98:  // SBC A, B
                    case 0x99:  // SBC A, C
                    case 0x9A:  // SBC A, D
                    case 0x9B:  // SBC A, E
                    case 0x9C:  // SBC A, H
                    case 0x9D:  // SBC A, L
                    case 0x9F:  // SBC A, A
                        this.setRegister8((byte)0x07, this.opSBC8(this.getRegister8((byte)0x07), this.getRegister8((byte)((opCode & 0x07)))));
                        this.regPC++;
                        break;
                        
                    case 0x9E:  // SBC A, (HL)
                        this.setRegister8((byte)0x07, this.opSBC8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)))));
                        this.regPC++;
                        break;
                        
                    case 0xA0:  // AND A, B
                    case 0xA1:  // AND A, C
                    case 0xA2:  // AND A, D
                    case 0xA3:  // AND A, E
                    case 0xA4:  // AND A, H
                    case 0xA5:  // AND A, L
                    case 0xA7:  // AND A, A
                        this.setRegister8((byte)0x07, this.opAND8(this.getRegister8((byte)0x07), this.getRegister8((byte)((opCode & 0x07)))));
                        this.regPC++;
                        break;
                        
                    case 0xA6:  // AND A, (HL)
                        this.setRegister8((byte)0x07, this.opAND8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)))));
                        this.regPC++;
                        break;
                        
                    case 0xA8:  // XOR A, B
                    case 0xA9:  // XOR A, C
                    case 0xAA:  // XOR A, D
                    case 0xAB:  // XOR A, E
                    case 0xAC:  // XOR A, H
                    case 0xAD:  // XOR A, L
                    case 0xAF:  // XOR A, A
                        this.setRegister8((byte)0x07, this.opXOR8(this.getRegister8((byte)0x07), this.getRegister8((byte)((opCode & 0x07)))));
                        this.regPC++;
                        break;
                        
                    case 0xAE:  // XOR A, (HL)
                        this.setRegister8((byte)0x07, this.opXOR8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)))));
                        this.regPC++;
                        break;
                        
                    case 0xB0:  // OR A, B
                    case 0xB1:  // OR A, C
                    case 0xB2:  // OR A, D
                    case 0xB3:  // OR A, E
                    case 0xB4:  // OR A, H
                    case 0xB5:  // OR A, L
                    case 0xB7:  // OR A, A
                        this.setRegister8((byte)0x07, this.opOR8(this.getRegister8((byte)0x07), this.getRegister8((byte)((opCode & 0x07)))));
                        this.regPC++;
                        break;
                        
                    case 0xB6:  // OR A, (HL)
                        this.setRegister8((byte)0x07, this.opOR8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02)))));
                        this.regPC++;
                        break;
                        
                    case 0xB8:  // CP A, B
                    case 0xB9:  // CP A, C
                    case 0xBA:  // CP A, D
                    case 0xBB:  // CP A, E
                    case 0xBC:  // CP A, H
                    case 0xBD:  // CP A, L
                    case 0xBF:  // CP A, A
                        this.opSUB8(this.getRegister8((byte)0x07), this.getRegister8((byte)((opCode & 0x07))));
                        this.regPC++;
                        break;
                        
                    case 0xBE:  // CP A, (HL)
                        this.opSUB8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong(this.getRegister16((byte)0x02))));
                        this.regPC++;
                        break;
                        
                    case 0xC0:  // RETNZ
                        // here, do not change the PC!!
                        this.opRETNZ();
                        break;
                        
                    case 0xC1:  // POP BC
                    case 0xD1:  // POP DE
                    case 0xE1:  // POP HL
                    case 0xF1:  // POP AF
                        this.opPOP16((byte)((opCode & 0x30) >> 4));
                        this.regPC++;
                        break;
                        
                    case 0xC2:  // JNZ nn
                        if(!this.checkFlag(FLAG_Z)) {
                            this.regPC = this.readMemory16(Short.toUnsignedLong(this.regPC)+1);
                        }
                        else {
                            this.regPC+=3;
                        }
                        break;
                        
                    case 0xC3:  // JP nn
                        this.regPC = this.readMemory16(Short.toUnsignedLong(this.regPC)+1);
                        break;
                        
                    case 0xC4:  // CALLNZ nn
                        // here, do not change the PC!!
                        this.opCALLNZ(this.readMemory16(Short.toUnsignedLong(this.regPC)+1));
                        break;
                        
                    case 0xC5:  // PUSH BC
                    case 0xD5:  // PUSH DE
                    case 0xE5:  // PUSH HL
                    case 0xF5:  // PUSH AF
                        this.opPUSH16((byte)((opCode & 0x30) >> 4));
                        this.regPC++;
                        break;
                        
                    case 0xC6:  // ADD A, n
                        this.setRegister8((byte)0x07, this.opADD8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong(this.regPC)+1)));
                        this.regPC+=2;
                        break;
                        
                    case 0xC7:  // RTS 00h
                        this.opRTS((byte)0x00);
                        break;
                        
                    case 0xC8:  // RETZ
                        // here, do not change the PC!!
                        this.opRETZ();
                        break;
                        
                    case 0xC9:  // RET
                        // here, do not change the PC!!
                        this.opRET();
                        break;
                        
                    case 0xCA:  // JZ nn
                        if(this.checkFlag(FLAG_Z)) {
                            this.regPC = this.readMemory16(Short.toUnsignedLong(this.regPC)+1);
                        }
                        else {
                            this.regPC+=3;
                        }
                        break;
                        
                    case 0xCB:  // Special 'CB' sub-thingys..
                        this.regPC++;
                        this.subOpCB();
                        break;
                        
                    case 0xCC:  // CALLZ nn
                        // here, do not change the PC!!
                        this.opCALLZ(this.readMemory16(Short.toUnsignedLong(this.regPC)+1));
                        break;
                        
                    case 0xCD:  // CALL nn
                        // here, do not change the PC!!
                        this.opCALL(this.readMemory16(Short.toUnsignedLong(this.regPC)+1));
                        break;
                        
                    case 0xCE:  // ADC A, n
                        this.setRegister8((byte)0x07, this.opADC8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong(this.regPC)+1)));
                        this.regPC+=2;
                        break;
                        
                    case 0xCF:  // RTS 08h
                        this.opRTS((byte)0x08);
                        break;
                        
                    case 0xD0:  // RETNC
                        // here, do not change the PC!!
                        this.opRETNC();
                        break;
                        
                    case 0xD2:  // JNC nn
                        if(!this.checkFlag(FLAG_C)) {
                            this.regPC = this.readMemory16(Short.toUnsignedLong(this.regPC)+1);
                        }
                        else {
                            this.regPC+=3;
                        }
                        break;
                        
                    case 0xD3:  // OUT (n), A
                        this.opOUT8(this.readMemory8(Short.toUnsignedLong(this.regPC)+1), this.getRegister8((byte)0x07));
                        this.regPC+=2;
                        break;
                        
                    case 0xD4:  // CALLNC nn
                        // here, do not change the PC!!
                        this.opCALLNC(this.readMemory16(Short.toUnsignedLong(this.regPC)+1));
                        break;
                        
                    case 0xD6:  // SUB A, n
                        this.setRegister8((byte)0x07, this.opSUB8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong(this.regPC)+1)));
                        this.regPC+=2;
                        break;
                        
                    case 0xD7:  // RST 10h
                        this.opRTS((byte)0x10);
                        break;
                        
                    case 0xD8:  // RETC
                        // here, do not change the PC!!
                        this.opRETC();
                        break;
                        
                    case 0xD9:  // EXX
                        this.opEXX();
                        this.regPC++;
                        break;
                        
                    case 0xDA:  // JC nn
                        if(this.checkFlag(FLAG_C)) {
                            this.regPC = this.readMemory16(Short.toUnsignedLong(this.regPC)+1);
                        }
                        else {
                            this.regPC+=3;
                        }
                        break;
                        
                    case 0xDB:  // IN A, (n)
                        this.setRegister8((byte)0x07, this.opIN8(this.readMemory8(Short.toUnsignedLong(this.regPC)+1)));
                        this.regPC+=2;
                        break;
                        
                    case 0xDC:  // CALLC nn
                        // here, do not change the PC!!
                        this.opCALLC(this.readMemory16(Short.toUnsignedLong(this.regPC)+1));
                        break;
                        
                    case 0xDD:  // Special 'DD' sub-thingys..
                        this.regPC++;
                        this.subOpDD();
                        break;
                        
                    case 0xDE:  // SBC A, n
                        this.setRegister8((byte)0x07, this.opSBC8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong(this.regPC)+1)));
                        this.regPC+=2;
                        break;
                        
                    case 0xDF:  // RST 18h
                        this.opRTS((byte)0x18);
                        break;
                        
                    case 0xE0:  // RETPO
                        // here, do not change the PC!!
                        this.opRETPO();
                        break;
                        
                    case 0xE2:  // JPO nn
                        if(!this.checkFlag(FLAG_PV)) {
                            this.regPC = this.readMemory16(Short.toUnsignedLong(this.regPC)+1);
                        }
                        else {
                            this.regPC+=3;
                        }
                        break;
                        
                    case 0xE3:  // EX (SP), HL
                        this.opEX_SP_HL();
                        this.regPC++;
                        break;
                        
                    case 0xE4:  // CALLPO nn
                        // here, do not change the PC!!
                        this.opCALLPO(this.readMemory16(Short.toUnsignedLong(this.regPC)+1));
                        break;
                        
                    case 0xE6:  // AND A, n
                        this.setRegister8((byte)0x07, this.opAND8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong(this.regPC)+1)));
                        this.regPC+=2;
                        break;
                        
                    case 0xE7:  // RST 20h
                        this.opRTS((byte)0x20);
                        break;
                        
                    case 0xE8:  // RETPE
                        // here, do not change the PC!!
                        this.opRETPE();
                        break;
                        
                    case 0xE9:  // JP (HL)
                        this.regPC = (short)((Byte.toUnsignedInt(this.regH) << 8) + Byte.toUnsignedInt(this.regL));
                        break;
                        
                    case 0xEA:  // JPE nn
                        if(this.checkFlag(FLAG_PV)) {
                            this.regPC = this.readMemory16(Short.toUnsignedLong(this.regPC)+1);
                        }
                        else {
                            this.regPC+=3;
                        }
                        break;
                        
                    case 0xEB:  // EX DE, HL
                        this.opEX_DE_HL();
                        this.regPC++;
                        break;
                        
                    case 0xEC:  // CALLPE nn
                        // here, do not change the PC!!
                        this.opCALLPE(this.readMemory16(Short.toUnsignedLong(this.regPC)+1));
                        break;
                        
                    case 0xED:  // Special 'ED' sub-thingys...
                        this.regPC++;
                        this.subOpED();
                        break;
                        
                    case 0xEE:  // XOR A, n
                        this.setRegister8((byte)0x07, this.opXOR8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong(this.regPC)+1)));
                        this.regPC+=2;
                        break;
                        
                    case 0xEF:  // RST 28h
                        this.opRTS((byte)0x28);
                        break;
                        
                    case 0xF0:  // RETNS
                        // here, do not change the PC!!
                        this.opRETNS();
                        break;
                        
                    case 0xF2:  // JNS nn
                        if(!this.checkFlag(FLAG_S)) {
                            this.regPC = this.readMemory16(Short.toUnsignedLong(this.regPC)+1);
                        }
                        else {
                            this.regPC+=3;
                        }
                        break;
                        
                    case 0xF3:  // DI
                        opDI();
                        this.regPC++;
                        break;
                        
                    case 0xF4:  // CALLNS nn
                        // here, do not change the PC!!
                        this.opCALLNS(this.readMemory16(Short.toUnsignedLong(this.regPC)+1));
                        break;
                        
                    case 0xF6:  // OR A, n
                        this.setRegister8((byte)0x07, this.opOR8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong(this.regPC)+1)));
                        this.regPC+=2;
                        break;
                        
                    case 0xF7:  // RST 30h
                        this.opRTS((byte)0x30);
                        break;
                        
                    case 0xF8:  // RETS
                        // here, do not change the PC!!
                        this.opRETS();
                        break;
                        
                    case 0xF9:  // LD SP, HL
                        this.regSP = (short)((Byte.toUnsignedInt(this.regH) << 8) + Byte.toUnsignedInt(this.regL));
                        this.regPC++;
                        break;
                        
                    case 0xFA:  // JS nn
                        if(this.checkFlag(FLAG_S)) {
                            this.regPC = this.readMemory16(Short.toUnsignedLong(this.regPC)+1);
                        }
                        else {
                            this.regPC+=3;
                        }
                        break;
                        
                    case 0xFB:  // EI
                        this.opEI();
                        this.regPC++;
                        break;
                        
                    case 0xFC:  // CALLS nn
                        // here, do not change the PC!!
                        this.opCALLS(this.readMemory16(Short.toUnsignedLong(this.regPC)+1));
                        break;
                        
                    case 0xFD:  // Special "FE" sub.thingys...
                        this.regPC++;
                        this.subOpFD();
                        break;
                        
                    case 0xFE:  // CP A, n
                        this.opSUB8(this.getRegister8((byte)0x07), this.readMemory8(Short.toUnsignedLong(this.regPC)+1));
                        this.regPC+=2;
                        break;
                        
                    case 0xFF:  // RST 38h
                        this.opRTS((byte)0x38);
                        break;
                        
                    default:
                        throw new OpCodeException("Opcode " + opCode + " not supported!");
                }
                
                return 0;
            }
            catch (MemoryException ex) {
                // Do nothing, try next block
            }
        }
        
        throw new MemoryException("No memory at location of program counter!");
    }
    
}
