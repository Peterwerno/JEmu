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
import java.util.List;

/**
 * This class implements the behaviour of the Intel 4004 CPU.
 * 
 * @author peter
 */
public class Intel4004 implements CPU {
    Memory programMemory;
    Memory dataMemory;
    List<IO> IOBlocks;
    
    short regPC;
    short stacks[];
    byte regA;
    byte[] regIndex;
    boolean carry;
    byte regDataRamBank;
    short regRegisterControl;
    
    
    public Intel4004() {
        this.regPC = 0;
        this.stacks = new short[3];
        this.regA = 0;
        this.regIndex = new byte[16];
        this.carry = false;
        this.regDataRamBank = 0;
        this.regRegisterControl = 0;
    }
    
    public Intel4004(Memory programMemory, Memory dataMemory, IO IOBlock) {
        this();
        
        this.programMemory = programMemory;
        this.dataMemory = dataMemory;
        this.IOBlocks = new ArrayList<>();
        this.IOBlocks.add(IOBlock);
    }
    
    public Intel4004(Memory programMemory, Memory dataMemory, List<IO> IOBlocks) {
        this();
        
        this.programMemory = programMemory;
        this.dataMemory = dataMemory;
        this.IOBlocks = IOBlocks;
    }
    
    /**
     * Read a (4 bit) nibble from memory
     * 
     * @param address (long) the memory address
     * @param fromProgramMemory (boolean) whether to read from program or data memory
     * @return the memory content (byte)
     * @throws MemoryException if the address is not in the memory range
     */
    protected byte readMemory4(long address, boolean fromProgramMemory) throws MemoryException {
        Memory mem = fromProgramMemory ? this.programMemory : this.dataMemory;
        
        try {
            int value = Byte.toUnsignedInt(mem.getByte(address>>1));
            if((address & 0x1) == 0x1) {
                return (byte)((value >> 4) & 0x0F);
            }
            else {
                return (byte)(value & 0x0F);
            }
        }
        catch (MemoryException ex) {
            // Try next memory block
        }
        
        throw new MemoryException("No memory at address " + Long.toHexString(address));
    }
    
    /**
     * Read an (8 bit) byte from memory
     * 
     * @param address (long) the memory address
     * @param fromProgramMemory (boolean) whether to read from program or data memory
     * @return the memory content (byte)
     * @throws MemoryException if the address is not in the memory range
     */
    protected byte readMemory8(long address, boolean fromProgramMemory) throws MemoryException {
        int highNibble = Byte.toUnsignedInt(readMemory4(address, fromProgramMemory));
        int lowNibble = Byte.toUnsignedInt(readMemory4(address+1, fromProgramMemory));
        
        return (byte)((highNibble << 4) | lowNibble);
    }
    
    /**
     * Store a 4 bit nibble to memory
     * 
     * @param address (long) the memory address
     * @param value (byte) the value to store
     * @param toProgramMemory (boolean) whether to write to program or data memory
     * @throws MemoryException if the address is not in the memory range
     */
    protected void writeMemory4(long address, byte value, boolean toProgramMemory) throws MemoryException {
        Memory mem = toProgramMemory ? this.programMemory : this.dataMemory;
        
        mem.setContent(address, value);
    }
    
    /**
     * Push a value on the "stack"
     * 
     * @param value (short) the value to push
     */
    protected void push(short value) {
        for(int i=this.stacks.length-1; i>0; i--) {
            this.stacks[i] = this.stacks[i-1];
        }
        this.stacks[0] = value;
    }
    
    /**
     * Pop a value from the "stack"
     * 
     * @return the value from the stack (short)
     */
    protected short pop() {
        short retVal = this.stacks[0];
        
        for(int i=0; i<this.stacks.length-1; i++) {
            this.stacks[i] = this.stacks[i+1];
        }
        this.stacks[this.stacks.length-1] = 0;
        
        return retVal;
    }
    
    /**
     * Operation NOP - no operation
     * 
     * @param opCode
     * @return 
     */
    protected int opNOP(int opCode) {
        this.regPC += 2;
        return 2;
    }
    
    /**
     * Operation JCN - Jump conditional
     * 
     * @param opCode
     * @return 
     * @throws MemoryException 
     */
    protected int opJCN(int opCode) throws MemoryException {
        int opCode2 = Byte.toUnsignedInt(readMemory8(Short.toUnsignedLong(this.regPC)+2, true));
        boolean condition = false;
        
        // TODO: Code the "test" signal later. In this implementation, it is assumed to always be true
        if((opCode & 0x08) == 0x08) {
            // negate everything
            if((opCode & 0x04) == 0x04) {
                // jump if accumulator is not zero
                if(this.regA != 0) condition = true;
            }
            if((opCode & 0x02) == 0x02) {
                // jump if carry is reset
                if(!this.carry) condition = true;
            }
        }
        else {
            if((opCode & 0x04) == 0x04) {
                // jump if accumulator is zero
                if(this.regA == 0) condition = true;
            }
            if((opCode & 0x02) == 0x02) {
                // jump if carry is set
                if(this.carry) condition = true;
            }
        }
        
        if(condition) {
            if((this.regPC & 0xFE) == 0xFE) this.regPC += (short)0x0100;
            this.regPC = (short)((this.regPC & 0x0F00) | opCode2);
            return 4;
        }
        this.regPC+=4;
        return 4;
    }
    
    /**
     * Operation FIM - Fetch immediate from ROM or
     *           SRC - Send Register Control
     * 
     * @param opCode
     * @return
     * @throws MemoryException 
     */
    protected int opFIMSRC(int opCode) throws MemoryException {
        int regGroup = (opCode & 0x0E);
        
        switch (opCode & 0x01) {
            case 0x00:  // FIM - Fetch immediate from ROM
                int opCode2 = Byte.toUnsignedInt(readMemory8(Short.toUnsignedLong(this.regPC)+2, true));
                this.regIndex[regGroup] = (byte)((opCode2 >> 4) & 0x0F);
                this.regIndex[regGroup+1] = (byte)(opCode2 & 0x0F);
                this.regPC+=4;
                return 4;
                
            case 0x01:  // SRC - Send Register Control
                this.regRegisterControl = (short)(((short)(this.regIndex[regGroup])<<4) | (short)(this.regIndex[regGroup+1]));
                this.regPC+=2;
                return 2;
        }
        
        // Should never get here!
        return 2;
    }
    
    /**
     * Operation FIN - Fetch indirect from ROM
     * or JIN - Jump indirect
     * 
     * @param opCode
     * @return
     * @throws MemoryException 
     */
    protected int opFINJIN(int opCode) throws MemoryException {
        int regGroup = (opCode & 0x0E);
        int address = Byte.toUnsignedInt(this.regIndex[0]) | (Byte.toUnsignedInt(this.regIndex[1]) << 4) | (this.regPC & 0x0F00);
        
        switch (opCode & 0x01) {
            case 0x00:  // FIN
                // TODO: This is not correct yet! Find out what to do!
                this.regIndex[regGroup] = this.readMemory4(address, true);
                this.regIndex[regGroup+1] = this.readMemory4(address+1, true);
                break;
                
            case 0x01:  // JIN
                this.regPC = (short)((this.regPC & 0x0F00) | address);
                return 2;
                
            default:
                throw new AssertionError();
        }
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation JUN - Jump unconditional
     * 
     * @param opCode
     * @return 
     * @throws MemoryException 
     */
    protected int opJUN(int opCode) throws MemoryException {
        int opCode2 = Byte.toUnsignedInt(readMemory8(Short.toUnsignedLong(this.regPC)+2, true));
        
        opCode2 |= (opCode & 0x0F)<<8;
        
        this.regPC = (short)opCode2;
        return 4;
    }
    
    /**
     * Operation JMS - Jump to subroutine
     * 
     * @param opCode
     * @return 
     * @throws MemoryException 
     */
    protected int opJMS(int opCode) throws MemoryException {
        int opCode2 = Byte.toUnsignedInt(readMemory8(Short.toUnsignedLong(this.regPC)+2, true));
        
        opCode2 |= (opCode & 0x0F)<<8;
        this.regPC+=4;
        push(this.regPC);
        
        this.regPC = (short)opCode2;
        
        return 4;
    }
    
    /**
     * Operation INC - Increase register
     * 
     * @param opCode
     * @return 
     */
    protected int opINC(int opCode) {
        int reg = (opCode & 0x0F);
        
        this.regIndex[reg]++;
        if((this.regIndex[reg] & 0x10) == 0x10) {
            this.regIndex[reg] &= 0x0F;
        }
        
        // Carry flag is not influenced by "INC"!
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation ISZ - Increment and skip if zero
     * 
     * @param opCode
     * @return 
     * @throws MemoryException 
     */
    protected int opISZ(int opCode) throws MemoryException {
        int opCode2 = Byte.toUnsignedInt(readMemory8(Short.toUnsignedLong(this.regPC)+2, true));
        int reg = (opCode & 0x0F);
        
        this.regIndex[reg]++;
        if(this.regIndex[reg] == 0x10) {
            this.regIndex[reg] = 0;
            this.regPC+=2;
            return 2;
        }
        
        this.regPC = (short)((this.regPC & 0x0F00) | opCode2);
        return 2;
    }
    
    /**
     * Operaton ADD - add register to accumulator
     * 
     * @param opCode
     * @return 
     */
    protected int opADD(int opCode) {
        int reg = (opCode & 0x0F);
        
        this.regA += this.regIndex[reg];
        if(this.carry) regA++;
        this.carry = ((this.regA & 0x10) == 0x10);
        this.regA &= 0x0F;
        
        this.regPC+=2;
        return 2;
    }

    /**
     * Operaton SUB - subtract register from accumulator
     * 
     * @param opCode
     * @return 
     */
    protected int opSUB(int opCode) {
        int reg = (opCode & 0x0F);
        
        this.regA -= this.regIndex[reg];
        if(!this.carry) regA--;
        this.carry = ((this.regA & 0x10) == 0x10);
        this.regA &= 0x0F;
        
        this.regPC+=2;
        return 2;
    }

    /**
     * Operaton LD - load register to accumulator
     * 
     * @param opCode
     * @return 
     */
    protected int opLD(int opCode) {
        int reg = (opCode & 0x0F);
        
        this.regA = this.regIndex[reg];

        this.regPC+=2;
        return 2;
    }

    /**
     * Operaton XCH - exchange register with accumulator
     * 
     * @param opCode
     * @return 
     */
    protected int opXCH(int opCode) {
        int reg = (opCode & 0x0F);
        
        byte buffer = this.regA;
        this.regA = this.regIndex[reg];
        this.regIndex[reg] = buffer;
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation BBL - Branch back and load
     * 
     * @param opCode
     * @return 
     */
    protected int opBBL(int opCode) {
        this.regA = (byte)(opCode & 0x0F);
        this.regPC = pop();
        return 2;
    }
    
    /**
     * Operation LDM - Load Accumulator immediate
     * 
     * @param opCode
     * @return 
     */
    protected int opLDM(int opCode) {
        this.regA = (byte)(opCode & 0x0F);
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operations that reference IO or RAM
     * 
     * @param opCode
     * @return
     * @throws OpCodeException 
     * @throws MemoryException 
     */
    protected int opIOandRAMInstructions(int opCode) throws OpCodeException, MemoryException {
        int address;
        
        switch (opCode & 0x0F) {
            case 0x00:  // WRM - Write Data RAM Character
                address = (this.regDataRamBank << 8) | this.regRegisterControl;
                this.writeMemory4(address, this.regA, false);
                break;
                
            case 0x01:  // WMP - Write RAM Port
                // TODO: Code this
                break;
                
            case 0x02:  // WRR - Write ROM Port
                // TODO: Code this
                break;
                
            case 0x03:  // WPM - Write Program RAM 
                // TODO: Code this
                break;
                
            case 0x04:  // WR0 - Write Data RAM Status character
                // TODO: Code this
                break;
                
            case 0x05:  // WR1 - Write Data RAM Status character
                // TODO: Code this
                break;
                
            case 0x06:  // WR2 - Write Data RAM Status character
                // TODO: Code this
                break;
                
            case 0x07:  // WR3 - Write Data RAM Status character
                // TODO: Code this
                break;
                
            case 0x08:  // SBM - Subtract data RAM from memory with carry
                address = (this.regDataRamBank << 8) | this.regRegisterControl;
                this.regA += (this.readMemory4(address, false) ^ 0x0F);
                if(!this.carry) regA++;
                this.carry = (this.regA & 0x10) == 0x10;
                this.regA &= 0x0F;
                break;
                
            case 0x09:  // RDM
                this.regA = this.readMemory4((this.regDataRamBank << 8) | this.regRegisterControl, false);
                break;
                
            case 0x0A:  // RDR - Read ROM Port
                // TODO: Code this
                break;
                
            case 0x0B:  // ADM - Add Data RAM to Accumulator with carry
                address = (this.regDataRamBank << 8) | this.regRegisterControl;
                this.regA += this.readMemory4(address, false);
                if(this.carry) regA++;
                this.carry = (this.regA & 0x10) == 0x10;
                this.regA &= 0x0F;
                break;
                
            case 0x0C:  // RD0 - Read Data RAM Status Character
                // TODO: Code this
                break;
                
            case 0x0D:  // RD1 - Read Data RAM Status Character
                // TODO: Code this
                break;
                
            case 0x0E:  // RD2 - Read Data RAM Status Character
                // TODO: Code this
                break;
                
            case 0x0F:  // RD3 - Read Data RAM Status Character
                // TODO: Code this
                break;
                
            default:
                throw new OpCodeException("Illegal opcode " + opCode);
        }
        
        this.regPC += 2;
        return 2;
    }
    
    /**
     * Operations on the accumulator - this is a combination of various 
     * instructions indicated by the high nibble 0xF
     * 
     * @param opCode
     * @return
     * @throws OpCodeException 
     */
    protected int opAccumulator(int opCode) throws OpCodeException {
        switch (opCode & 0x0F) {
            case 0x00:  // CLB - Clear Both (clear Accumulator and carry)
                this.regA = 0;
                this.carry = false;
                break;
                
            case 0x01:  // CLC - Clear Carry 
                this.carry = false;
                break;
                
            case 0x02:  // IAC - Increment accumulator
                this.regA++;
                this.carry = (this.regA & 0x10) == 0x10;
                this.regA &= 0x0F;
                break;
                
            case 0x03:  // CMC - Complement carry
                this.carry = !this.carry;
                break;
                
            case 0x04:  // CMA - Complement accumualtor
                this.regA ^= 0x0F;
                break;
                
            case 0x05:  // RAL - Rotate Accumulator left through carry
                this.regA<<=1;
                if(this.carry) this.regA |= 0x01;
                this.carry = (this.regA & 0x10) == 0x10;
                this.regA &= 0x0F;
                break;
                
            case 0x06:  // RAR - Rotate Accumulator right through carry
                if(this.carry) this.regA |= 0x10;
                this.carry = (this.regA & 0x01) == 0x01;
                this.regA >>= 1;
                this.regA &= 0x0F;
                break;
                
            case 0x07:  // TCC - Transmit carry and clear
                this.regA = this.carry ? (byte)1 : (byte)0;
                this.carry = false;
                break;
                
            case 0x08:  // DAC - Decrement Accumulator
                this.regA += 0x0F;
                this.carry = (this.regA & 0x10) == 0x10;
                this.regA &= 0x0F;
                break;
                
            case 0x09:  // TCS - Transfer Carry Subtract
                this.regA = this.carry ? (byte)0x0A : (byte)0x09;
                this.carry = false;
                break;
                
            case 0x0A:  // STC - Set carry
                this.carry = true;
                break;
                
            case 0x0B:  // DAA - Decimal Adjust Accumulator
                if(this.carry || (this.regA > 0x09)) {
                    this.regA += 0x06;
                    this.carry = (this.regA & 0x10) == 0x10;
                    this.regA &= 0x0F;
                }
                break;
                
            case 0x0C:  // KBP - Keyboard Process
                switch (this.regA) {
                    case 0x00:
                    case 0x01:
                    case 0x02:
                        break;
                        
                    case 0x04:
                        this.regA = 3;
                        break;
                        
                    case 0x08:
                        this.regA = 4;
                        break;
                        
                    default:
                        this.regA = 0x0F;
                        break;
                }
                break;
                
            case 0x0D:  // DCL - Designate Command Line
                this.regDataRamBank = (byte)(this.regA & 0x07);
                break;
                
            default:
                throw new OpCodeException("Invalid opcode " + opCode);
        }
        
        this.regPC+=2;
        return 2;
    }

    @Override
    public int runNextOpCode() throws MemoryException, OpCodeException {
        int opCode = Byte.toUnsignedInt(readMemory8(Short.toUnsignedLong(this.regPC), true));
        
        switch (opCode & 0xF0) {
            case 0x00:
                return opNOP(opCode);
                
            case 0x10:
                return opJCN(opCode);
                
            case 0x20:
                return opFIMSRC(opCode);
                
            case 0x30:
                return opFINJIN(opCode);
                
            case 0x40:
                return opJUN(opCode);
                
            case 0x50:
                return opJMS(opCode);
                
            case 0x60:
                return opINC(opCode);
                
            case 0x70:
                return opISZ(opCode);
                
            case 0x80:
                return opADD(opCode);
                
            case 0x90:
                return opSUB(opCode);
                
            case 0xA0:
                return opLD(opCode);
                
            case 0xB0:
                return opXCH(opCode);
                
            case 0xC0:
                return opBBL(opCode);
                
            case 0xD0:
                return opLDM(opCode);
                
            case 0xF0:
                return opAccumulator(opCode);
                
            default:
                throw new OpCodeException("Illegal OpCode " + opCode);
        }
    }
    
}
