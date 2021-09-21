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
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author peter
 */
public class SeikoUC2000 implements CPU, IRQHandler {
    public static final byte FLAG_Z = (byte)0x01;
    public static final byte FLAG_C = (byte)0x02;
    
    public static final int IRQ_SET_BUTTON      = 0x0001;
    public static final int IRQ_MODE_BUTTON     = 0x0002;
    public static final int IRQ_TRANSMIT_BUTTON = 0x0004;
    public static final int IRQ_SELECT_BUTTON   = 0x0008;
    public static final int IRQ_SECOND_TIMER    = 0x0010;
    public static final int IRQ_REDRAW_SCREEN   = 0x0020;
    
    byte[][] registers = new byte[4][32];
    short regPC = 0x1800;
    short regSP = 0x2000;
    short regLA = 0x0000;
    short regSA = 0x0000;
    byte regFlags;
    byte regCurrentBank;
    byte regAdditionalBank;
    int irq = 0;
    boolean inIrq = false;
    
    List<Memory> memoryBlocks;
    List<IO> IOBlocks;
    
    public SeikoUC2000() {
        // TODO: Initialize
    }
    
    public SeikoUC2000(Memory memoryBlock, IO IOBlock) {
        this();
        
        this.memoryBlocks = new ArrayList<>();
        this.IOBlocks = new ArrayList<>();
        
        this.memoryBlocks.add(memoryBlock);
        this.IOBlocks.add(IOBlock);
    }
    
    public SeikoUC2000(List<Memory> memoryBlocks, List<IO> IOBlocks) {
        this();
        this.memoryBlocks = memoryBlocks;
        this.IOBlocks = IOBlocks;
    }
    
    /**
     * Set or reset a flag
     * 
     * @param flag (byte) the flag that should be manipulated
     * @param set (boolean) whether to set or reset the flag
     */
    public void setFlag(byte flag, boolean set) {
        if(set)
            this.regFlags |= flag;
        else
            this.regFlags &= (0xFF - flag);
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
        byte lowByte = readMemory8(address + 1);
        byte highByte = readMemory8(address);
        
        if(address < 0x1800) return (short)(0xB001);
        
        return (short)(Byte.toUnsignedInt(lowByte) + (Byte.toUnsignedInt(highByte) << 8));
    }
    
    /**
     * Write an (8 bit) byte to memory
     * 
     * @param address (long) the memory address
     * @param value (byte) the value
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
     * @param value (short) the value
     * @throws MemoryException if the address is not in the memory range
     */
    protected void writeMemory16(long address, short value) throws MemoryException {
        byte lowByte = (byte)(Short.toUnsignedInt(value) & 0x00FF);
        byte highByte = (byte)((Short.toUnsignedInt(value) & 0xFF00) >> 8);
        
        writeMemory8(address, highByte);
        writeMemory8(address+1, lowByte);
    }
    
    /**
     * Read an (8 bit) byte from IO
     * 
     * @param address (long) the memory address
     * @return the memory content (byte)
     * @throws MemoryException if the address is not in the memory range
     */
    protected byte readIO8(long address) throws MemoryException {
        Iterator<IO> IOIterator = IOBlocks.iterator();
        
        while(IOIterator.hasNext()) {
            IO io = IOIterator.next();
            
            try {
                return io.getByte(address);
            }
            catch (MemoryException ex) {
                // Try next memory block
            }
        }
        
        throw new MemoryException("No IO at port " + Long.toHexString(address));
    }
    
    /**
     * Write a (8 bit) byte to IO
     * 
     * @param address (long) the IO Address
     * @param value (byte) the value
     * @throws MemoryException if the IO address is not in the IO range
     */
    protected void writeIO8(long address, byte value) throws MemoryException {
        Iterator<IO> IOIterator = IOBlocks.iterator();
        
        while(IOIterator.hasNext()) {
            IO io = IOIterator.next();
            
            try {
                io.setByte(address, value);
                return;
            }
            catch (MemoryException ex) {
                // Try next memory block
            }
        }
        
        throw new MemoryException("No IO at port " + Long.toHexString(address));
    }
    
    /**
     * Operation ADD - add two registers
     * 
     * @param opCode
     * @return 
     */
    protected int opADD(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (opCode & 0x001F);
        
        this.registers[this.regCurrentBank][rd] += this.registers[this.regCurrentBank][rs];
        
        this.setFlag(FLAG_C, (this.registers[this.regCurrentBank][rd] & 0x10) == 0x10);
        this.registers[this.regCurrentBank][rd] &= 0x0F;
//        this.setFlag(FLAG_Z, this.registers[this.regCurrentBank][rd] == 0);
        
        this.regPC+=2;
        
        return 2;
    }
    
    /**
     * Operation ADB - add two BCD-Coded registers
     * 
     * @param opCode
     * @return 
     */
    protected int opADB(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (opCode & 0x001F);
        
        this.registers[this.regCurrentBank][rd] += this.registers[this.regCurrentBank][rs];
        
        if(this.registers[this.regCurrentBank][rd] > 9)
            this.registers[this.regCurrentBank][rd] += 6;
        
        this.setFlag(FLAG_C, (this.registers[this.regCurrentBank][rd] & 0x10) == 0x10);
        this.registers[this.regCurrentBank][rd] &= 0x0F;
//        this.setFlag(FLAG_Z, this.registers[this.regCurrentBank][rd] == 0);

        this.regPC+=2;
        
        return 2;
    }
    
    /**
     * Operation SUB - subtract two registers
     * 
     * @param opCode
     * @return 
     */
    protected int opSUB(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (opCode & 0x001F);
        
        this.registers[this.regCurrentBank][rd] -= this.registers[this.regCurrentBank][rs];
        
        this.setFlag(FLAG_C, (this.registers[this.regCurrentBank][rd] & 0x10) == 0x10);
        this.registers[this.regCurrentBank][rd] &= 0x0F;
        this.setFlag(FLAG_Z, this.registers[this.regCurrentBank][rd] == 0);
        
        this.regPC+=2;
        
        return 2;
    }
    
    /**
     * Operation SBB - subtract two BCD-Coded registers
     * 
     * @param opCode
     * @return 
     */
    protected int opSBB(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (opCode & 0x001F);
        
        this.registers[this.regCurrentBank][rd] -= this.registers[this.regCurrentBank][rs];
        
        if(this.registers[this.regCurrentBank][rd] > 9)
            this.registers[this.regCurrentBank][rd] -= 6;
        
        this.setFlag(FLAG_C, (this.registers[this.regCurrentBank][rd] & 0x10) == 0x10);
        this.registers[this.regCurrentBank][rd] &= 0x0F;
        this.setFlag(FLAG_Z, this.registers[this.regCurrentBank][rd] == 0);

        this.regPC+=2;
        
        return 2;
    }
    
    /**
     * Operation ADI - add immediate to register
     * 
     * @param opCode
     * @return 
     */
    protected int opADI(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int i = (opCode & 0x001E) >> 1;
        
        this.registers[this.regCurrentBank][rd] += i;
        
        this.setFlag(FLAG_C, (this.registers[this.regCurrentBank][rd] & 0x10) == 0x10);
        this.registers[this.regCurrentBank][rd] &= 0x0F;
//        this.setFlag(FLAG_Z, this.registers[this.regCurrentBank][rd] == 0);
        
        this.regPC+=2;
        
        return 2;
    }
    
    /**
     * Operation ADBI - add immediate to BCD endoced register
     * 
     * @param opCode
     * @return 
     */
    protected int opADBI(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int i = (opCode & 0x001E) >> 1;
        
        this.registers[this.regCurrentBank][rd] += i;
        
        if(this.registers[this.regCurrentBank][rd] > 9)
            this.registers[this.regCurrentBank][rd] += 6;
        
        this.setFlag(FLAG_C, (this.registers[this.regCurrentBank][rd] & 0x10) == 0x10);
        this.registers[this.regCurrentBank][rd] &= 0x0F;
//        this.setFlag(FLAG_Z, this.registers[this.regCurrentBank][rd] == 0);

        this.regPC+=2;
        
        return 2;
    }
    
    /**
     * Operation SBI - subtract immediate from register
     * 
     * @param opCode
     * @return 
     */
    protected int opSBI(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int i = (opCode & 0x001F) >> 1;
        
        this.registers[this.regCurrentBank][rd] -= i;
        
        this.setFlag(FLAG_C, (this.registers[this.regCurrentBank][rd] & 0x10) == 0x10);
        this.registers[this.regCurrentBank][rd] &= 0x0F;
        this.setFlag(FLAG_Z, this.registers[this.regCurrentBank][rd] == 0);
        
        this.regPC+=2;
        
        return 2;
    }
    
    /**
     * Operation SBBI - subtract immediate from BCD encoded register
     * 
     * @param opCode
     * @return 
     */
    protected int opSBBI(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int i = (opCode & 0x001F) >> 1;
        
        this.registers[this.regCurrentBank][rd] -= i;
        
        if(this.registers[this.regCurrentBank][rd] > 9)
            this.registers[this.regCurrentBank][rd] -= 6;
        
        this.setFlag(FLAG_C, (this.registers[this.regCurrentBank][rd] & 0x10) == 0x10);
        this.registers[this.regCurrentBank][rd] &= 0x0F;
        this.setFlag(FLAG_Z, this.registers[this.regCurrentBank][rd] == 0);

        this.regPC+=2;
        
        return 2;
    }
    
    /**
     * Operation ADM - multiple add registers
     * 
     * @param opCode
     * @return 
     */
    protected int opADM(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (opCode & 0x001F);
        int k = ((rs-rd) % 8);
        boolean zero = true;
        boolean carry = false;

        for(int i=0; i<k+1; i++) {
            this.registers[this.regCurrentBank][rd+i] += this.registers[this.regCurrentBank][rs-k+i];
            if(carry) this.registers[this.regCurrentBank][rd+i]++;
            carry = ((this.registers[this.regCurrentBank][rd+i] & 0x10) == 0x10);
            this.registers[this.regCurrentBank][rd+i] &= 0x0F;
            if(this.registers[this.regCurrentBank][rd+i] != 0) zero = false;
        }
        
        this.setFlag(FLAG_C, carry);    // TODO: Check!! really??
//        this.setFlag(FLAG_Z, zero);
        
        this.regPC+=2;
        
        return 2;
    }
    
    /**
     * Operation ADBM - multiple add BCD encoded registers
     * 
     * @param opCode
     * @return 
     */
    protected int opADBM(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (opCode & 0x001F);
        int k = ((rs-rd) % 8);
        boolean zero = true;
        boolean carry = false;
        
        for(int i=0; i<k+1; i++) {
            this.registers[this.regCurrentBank][rd+i] += this.registers[this.regCurrentBank][rs-k+i];
            if(carry) this.registers[this.regCurrentBank][rd+i]++;
            if(this.registers[this.regCurrentBank][rd+i] > 9)
                this.registers[this.regCurrentBank][rd+i] += 6;
            carry = ((this.registers[this.regCurrentBank][rd+i] & 0x10) == 0x10);
            this.registers[this.regCurrentBank][rd+i] &= 0x0F;
            if(this.registers[this.regCurrentBank][rd+i] != 0) zero = false;
        }
        
        this.setFlag(FLAG_C, carry);    // TODO: Check!! really??
//        this.setFlag(FLAG_Z, zero);
        
        this.regPC+=2;
        
        return 2;
    }
    
    /**
     * Operation SBM - multiple subtract registers
     * 
     * @param opCode
     * @return 
     */
    protected int opSBM(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (opCode & 0x001F);
        int k = ((rs-rd) % 8);
        boolean zero = true;
        boolean carry = false;
        
        for(int i=0; i<k+1; i++) {
            this.registers[this.regCurrentBank][rd+i] -= this.registers[this.regCurrentBank][rs-k+i];
            if(carry) this.registers[this.regCurrentBank][rd+i]--;
            carry = ((this.registers[this.regCurrentBank][rd+i] & 0x10) == 0x10);
            this.registers[this.regCurrentBank][rd+i] &= 0x0F;
            if(this.registers[this.regCurrentBank][rd+i] != 0) zero = false;
        }
        
        this.setFlag(FLAG_C, carry);    // TODO: Check!! really??
//        this.setFlag(FLAG_Z, zero);
        
        this.regPC+=2;
        
        return 2;
    }
    
    /**
     * Operation SBBM - multiple subtract BCD encoded registers
     * 
     * @param opCode
     * @return 
     */
    protected int opSBBM(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (opCode & 0x001F);
        int k = ((rs-rd) % 8);
        boolean zero = true;
        boolean carry = false;
        
        for(int i=0; i<k+1; i++) {
            this.registers[this.regCurrentBank][rd+i] -= this.registers[this.regCurrentBank][rs-k+i];
            if(carry) this.registers[this.regCurrentBank][rd+i]--;
            if((this.registers[this.regCurrentBank][rd+i] & 0x0F) > 9)
                this.registers[this.regCurrentBank][rd+i] -= 6;
            carry = ((this.registers[this.regCurrentBank][rd+i] & 0x10) == 0x10);
            this.registers[this.regCurrentBank][rd+i] &= 0x0F;
            if(this.registers[this.regCurrentBank][rd+i] != 0) zero = false;

        }
        
        this.setFlag(FLAG_C, carry);    // TODO: Check!! really??
//        this.setFlag(FLAG_Z, zero);
        
        this.regPC+=2;
        
        return 2;
    }
    
    /**
     * Operation CMP - compare two registers
     * 
     * @param opCode
     * @return 
     */
    protected int opCMP(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (opCode & 0x001F);
        
        this.setFlag(FLAG_Z, this.registers[this.regCurrentBank][rd] == this.registers[this.regCurrentBank][rs]);
        this.setFlag(FLAG_C, this.registers[this.regCurrentBank][rd] < this.registers[this.regCurrentBank][rs]);
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation CPM - compare multiple registers
     * 
     * @param opCode
     * @return 
     */
    protected int opCPM(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (opCode & 0x001F);
        int k = ((rs-rd) % 8);
        
        this.setFlag(FLAG_Z, false);
        this.setFlag(FLAG_C, false);
        for(int i=k+1; i>=0; i--) {
            if(this.registers[this.regCurrentBank][rd+i] < this.registers[this.regCurrentBank][rs-k+i]) {
                this.setFlag(FLAG_C, true);
                this.regPC += 2;
                return 2;
            }
            else if(this.registers[this.regCurrentBank][rd+i] > this.registers[this.regCurrentBank][rs-k+i]) {
                this.setFlag(FLAG_C, false);
                this.regPC += 2;
                return 2;
            }
        }
        
        this.setFlag(FLAG_Z, true);
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation CPI - compare register with immediate value
     * 
     * @param opCode
     * @return 
     */
    protected int opCPI(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int i = (opCode & 0x001E) >> 1;
        
        this.setFlag(FLAG_Z, this.registers[this.regCurrentBank][rd] == i);
        this.setFlag(FLAG_C, this.registers[this.regCurrentBank][rd] < i);
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation LCRB - Load current bank
     * 
     * @param opCode
     * @return 
     */
    protected int opLCRB(int opCode) {
        int bank = (opCode & 0x0018) >> 3;
        
        this.regCurrentBank = (byte)bank;
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation LARB - Load additional bank
     * 
     * @param opCode
     * @return 
     */
    protected int opLARB(int opCode) {
        int bank = (opCode & 0x0018) >> 3;
        
        this.regAdditionalBank = (byte)bank;
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation ANDI - logical AND register with immediate value
     * 
     * @param opCode
     * @return 
     */
    protected int opANDI(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int i = (opCode & 0x001E) >> 1;
        
        this.registers[this.regCurrentBank][rd] &= i;
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation ORI - logical OR register with immediate value
     * 
     * @param opCode
     * @return 
     */
    protected int opORI(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int i = (opCode & 0x001E) >> 1;
        
        this.registers[this.regCurrentBank][rd] |= i;
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation XORI - logical XOR register with immediate value
     * 
     * @param opCode
     * @return 
     */
    protected int opXORI(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int i = (opCode & 0x001E) >> 1;
        
        this.registers[this.regCurrentBank][rd] ^= i;
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation INC - increment register range
     * 
     * @param opCode
     * @return 
     */
    protected int opINC(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (rd & 0x0018) | (opCode & 0x0007);
        boolean carry = true;
        
        for(int i=rs; i<=rd; i++) {
            if(carry) this.registers[this.regCurrentBank][i]++;
            carry = (this.registers[this.regCurrentBank][i] & 0x10) == 0x10;
            this.registers[this.regCurrentBank][i] &= 0x0F;
        }
        
        this.setFlag(FLAG_C, carry);
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation INCB - increment BCD encoded register range
     * 
     * @param opCode
     * @return 
     */
    protected int opINCB(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (rd & 0x0018) | (opCode & 0x0007);
        boolean carry = true;
        
        for(int i=rs; i<=rd; i++) {
            if(carry) this.registers[this.regCurrentBank][i]++;
            if(this.registers[this.regCurrentBank][i] > 9) this.registers[this.regCurrentBank][i] += 6;
            carry = (this.registers[this.regCurrentBank][i] & 0x10) == 0x10;
            this.registers[this.regCurrentBank][i] &= 0x0F;
        }
        
        this.setFlag(FLAG_C, carry);
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation DEC - decrement register range
     * 
     * @param opCode
     * @return 
     */
    protected int opDEC(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (rd & 0x0018) | (opCode & 0x0007);
        boolean carry = true;
        
        for(int i=rs; i<=rd; i++) {
            if(carry) this.registers[this.regCurrentBank][i]--;
            carry = (this.registers[this.regCurrentBank][i] & 0x10) == 0x10;
            this.registers[this.regCurrentBank][i] &= 0x0F;
        }
        
        this.setFlag(FLAG_C, carry);
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation DECB - decrement BCD encoded register range
     * 
     * @param opCode
     * @return 
     */
    protected int opDECB(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (rd & 0x0018) | (opCode & 0x0007);
        boolean carry = true;
        
        for(int i=rs; i<=rd; i++) {
            if(carry) this.registers[this.regCurrentBank][i]--;
            if(this.registers[this.regCurrentBank][i] < 0) this.registers[this.regCurrentBank][i] -= 6;
            carry = (this.registers[this.regCurrentBank][i] & 0x10) == 0x10;
            this.registers[this.regCurrentBank][i] &= 0x0F;
        }

        this.setFlag(FLAG_C, carry);
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation RSHM - Shift right register wide over register range
     * 
     * @param opCode
     * @return 
     */
    protected int opRSHM(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (rd & 0x0018) | (opCode & 0x0007);
        
        for(int i=rd; i>=rs; i--) {
            this.registers[this.regCurrentBank][i] = this.registers[this.regCurrentBank][i-1];
        }
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation LSHM - Shift left register wide over register range
     * 
     * @param opCode
     * @return 
     */
    protected int opLSHM(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (rd & 0x0018) | (opCode & 0x0007);
        
        for(int i=rs; i<=rd; i++) {
            this.registers[this.regCurrentBank][i] = this.registers[this.regCurrentBank][(i+1)%32];
        }
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation IN - read from IO port
     * 
     * @param opCode
     * @return 
     */
    protected int opIN(int opCode) throws MemoryException {
        int rd = (opCode & 0x03E0) >> 5;
        int port = (opCode & 0x000F);
        
        this.registers[this.regCurrentBank][rd] = this.readIO8(port);
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation PSAM - set SA from multiple registers
     * 
     * @param opCode
     * @return
     * @throws OpCodeException 
     */
    protected int opPSAM(int opCode) throws OpCodeException {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (rd & 0x0018) | (opCode & 0x0007);
        
        if(rs-rd < 2) throw new OpCodeException("PSAM rs-rd < 2!");
        int value = 0;
        for(int i=rs; i>=rd; i--) {
            value <<= 4;
            value |= this.registers[this.regCurrentBank][i];
        }
        
        this.regSA = (short)((value & 0x07FF) + 0x1800);
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation PLAM - set LA from multiple registers
     * 
     * @param opCode
     * @return
     * @throws OpCodeException 
     * @throws MemoryException 
     */
    protected int opPLAM(int opCode) throws OpCodeException, MemoryException {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (rd & 0x0018) | (opCode & 0x0007);
        
        if(rs-rd < 1) throw new OpCodeException("PLAM rs-rd < 1!");
        int value = 0;
        for(int i=rs; i>=rd; i--) {
            value <<= 4;
            value |= this.registers[this.regCurrentBank][i];
        }
        
//        this.regLA = (short)(value);
        this.writeIO8(0, (byte)(value));
       
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation LDSM - load memory from SA to registers
     * 
     * @param opCode
     * @return 
     * @throws MemoryException
     */
    protected int opLDSM(int opCode) throws MemoryException {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (rd & 0x0018) | (opCode & 0x0007);

        if(rd < rs) {
            boolean half = true;
            byte byteRead = this.readMemory8(this.regSA++);
            
            for(int i=rd; i<=rs; i++) {
                this.registers[this.regCurrentBank][i] = (byte)(byteRead & 0x0F);
                if(half) {
                    byteRead >>= 4;
                    half = false;
                }
                else {
                    if(i<rs) byteRead = this.readMemory8(this.regSA++);
                    half = true;
                }
            }
        }
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation STSM - store multiple registers to memory (SA)
     * 
     * @param opCode
     * @return 
     * @throws MemoryException
     */
    protected int opSTSM(int opCode) throws MemoryException {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (rd & 0x0018) | (opCode & 0x0007);

        if(rd <= rs) {
            boolean half = true;
            byte byteRead = 0;
            
            for(int i=rd; i<=rs; i++) {
                byteRead = this.registers[this.regCurrentBank][i];
                if(half) {
                    byteRead <<= 4;
                    half = false;
                }
                else {
                    this.writeMemory8(this.regSA++, byteRead);
                    half = true;
                }
            }
            if(!half)
                this.writeMemory8(this.regSA++, byteRead);
        }
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation STLM - Store multiple registers to LCD (LA)
     * 
     * @param opCode
     * @return 
     * @throws MemoryException
     */
    protected int opSTLM(int opCode) throws MemoryException {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (rd & 0x0018) | (opCode & 0x0007);

        if(rd > rs) {
            boolean half = true;
            byte byteRead = 0;
            
            for(int i=rs; i<=rd; i++) {
                byteRead = this.registers[this.regCurrentBank][i];
                if(half) {
                    byteRead <<= 4;
                    half = false;
                }
                else {
                    this.writeIO8(1, byteRead);
                    half = true;
                }
            }
            if(!half)
                this.writeIO8(1, byteRead);
        }
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation STL - store register in LCD
     * 
     * @param opCode
     * @return
     * @throws MemoryException 
     */
    protected int opSTL(int opCode) throws MemoryException {
        int r = (opCode & 0x03E0) >> 5;
        
        this.writeIO8(1, (byte)(this.registers[this.regCurrentBank][r] | 0x30));
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation PSAI - load AI with immediate
     * 
     * @param opCode
     * @return 
     */
    protected int opPSAI(int opCode) {
        int value = (opCode & 0x07FF);
        
        this.regSA = (short)(value + 0x1800);
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation PLAI - write byte to LCD address register
     * 
     * @param opCode
     * @return
     * @throws MemoryException 
     */
    protected int opPLAI(int opCode) throws MemoryException {
        int high = (opCode & 0x03E0)>>2;
        int val = (opCode & 0x0007) | high;

        this.writeIO8(0, (byte)(val));
        this.regPC+=2;
        
        return 2;
    }
    
    /**
     * Operation STLI - write byte to LCD data register
     * 
     * @param opCode
     * @return
     * @throws MemoryException 
     */
    protected int opSTLI(int opCode) throws MemoryException {
        int high = (opCode & 0x03E0)>>2;
        int val = (opCode & 0x0007) | high;
        
        this.writeIO8(1, (byte)(val));
        this.regPC+=2;
        
        return 2;
    }
    
    /**
     * Operation MOV - move register to register
     * 
     * @param opCode
     * @return 
     */
    protected int opMOV(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (opCode & 0x001F);
        
        this.registers[this.regCurrentBank][rd] = this.registers[this.regCurrentBank][rs];
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation MOVM - move multiple register to register
     * 
     * @param opCode
     * @return 
     */
    protected int opMOVM(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (opCode & 0x001F);
        int k = ((rs-rd) % 8);
        
        for(int i=0; i<k+1; i++) {
            this.registers[this.regCurrentBank][rd+i] = this.registers[this.regCurrentBank][rs-k-i];
        }
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation LDI - load immediate value to register
     * 
     * @param opCode
     * @return 
     */
    protected int opLDI(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int i = (opCode & 0x001F) >> 1;
        
        this.registers[this.regCurrentBank][rd] = (byte)i;
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation CLRM - clear multiple registers
     * 
     * @param opCode
     * @return 
     */
    protected int opCLRM(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (rd & 0x0018) | (opCode & 0x0007);
        
        for(int i=rs; i<=rd; i++) {
            this.registers[this.regCurrentBank][i] = 0;
        }
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation MVAC - Move register from current to additional bank
     * 
     * @param opCode
     * @return 
     */
    protected int opMVAC(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (opCode & 0x001F);
        
        this.registers[this.regAdditionalBank][rd] = this.registers[this.regCurrentBank][rs];
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation MVACM - Move multiple registers from current to additional bank
     * 
     * @param opCode
     * @return 
     */
    protected int opMVACM(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (opCode & 0x001F);
        int k = ((rs-rd) % 8);
        
//        for(int i=rd; i<rs; i++) {
//            this.registers[this.regAdditionalBank][i] = this.registers[this.regCurrentBank][i];
//        }
        for(int i=0; i<k+1; i++) {
            this.registers[this.regAdditionalBank][rd+i] = this.registers[this.regCurrentBank][rs-k+i];
        }
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation MVCA - Move register from additional to current bank
     * 
     * @param opCode
     * @return 
     */
    protected int opMVCA(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (opCode & 0x001F);
        
        this.registers[this.regCurrentBank][rd] = this.registers[this.regAdditionalBank][rs];
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation MVCAM - Move multiple registers from additional to current bank
     * 
     * @param opCode
     * @return 
     */
    protected int opMVCAM(int opCode) {
        int rd = (opCode & 0x03E0) >> 5;
        int rs = (opCode & 0x001F);
        int k = ((rs-rd) % 8);
        
//        for(int i=rd; i<rs; i++) {
//            this.registers[this.regCurrentBank][i] = this.registers[this.regAdditionalBank][i];
//        }
        for(int i=0; i<k+1; i++) {
            this.registers[this.regCurrentBank][rd+i] = this.registers[this.regAdditionalBank][rs-k+i];
        }
        
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation CALL - Call a subroutine
     * 
     * @param opCode
     * @return 
     * @throws MemoryException if something went wrong
     */
    protected int opCALL(int opCode) throws MemoryException {
        int address = (opCode & 0x0FFF) << 1;
        
        // Special handling for some OS routines
        switch (address) {
            case 0x178C:    // print0-10
            case 0x178E:    // print0-9
            case 0x1790:    // print0-8
            case 0x1792:    // print0-7
            case 0x1794:    // print0-6
            case 0x1796:    // print0-5
            case 0x1798:    // print0-4
            case 0x179A:    // print0-3
            case 0x179C:    // print0-2
            case 0x179E:    // print0-1
                for(int i=0; i<(0x17A0-address)/2; i++) {
                    
                    byte value = this.readMemory8(this.regSA);
                    this.writeIO8(1, value);
                    this.regSA++;
                }
                this.regPC+=2;
                break;
                
            case 0x11E4:    // Clear current bank
                for(int i=0; i<32; i++)
                    this.registers[this.regCurrentBank][i] = 0;
                this.regPC+=2;
                break;
                
            default:
                this.regPC+=2;
                this.regSP-=2;
                this.writeMemory16(Short.toUnsignedLong(this.regSP), this.regPC);
                this.regPC= (short)address;
                break;
        }
        
        
        return 2;
    }
    
    /**
     * Operation RET - return from subroutine
     * 
     * @param opCode
     * @return 
     * @throws MemoryException
     */
    protected int opRET(int opCode) throws MemoryException {
        this.regPC = this.readMemory16(Short.toUnsignedLong(this.regSP));
        this.regSP+=2;
        return 2;
    }
    
    protected int opHLT(int opCode) throws MemoryException {
        if(this.inIrq) {
            this.inIrq = false;
            this.opRET(0xB000);
        }
        return 2;
    }
    
    /**
     * Operation CPFJR - compare register and jump
     * 
     * @param opCode
     * @return 
     */
    protected int opCPFJR(int opCode) {
        int r = (opCode & 0x03E0) >> 5;
        int offset = (opCode & 0x001F);
        
        if(this.registers[this.regCurrentBank][r] == 4) {
            //if((offset & 0x0010) == 0x0010) offset = offset | 0xFFFFFFF0;
            this.regPC += (short)(offset);
        }
        
        this.regPC+=2;
        
        return 2;
    }
    
    /**
     * Operation IJMR - jump relative by register offset
     * 
     * @param opCode
     * @return 
     */
    protected int opIJMR(int opCode) {
        int r = (opCode & 0x03E0) >> 5;
        int offset = this.registers[this.regCurrentBank][r];
        
        if((offset & 0x0008) == 0x0008) offset |=0xFFFFFFF0;
        
        this.regPC += (short)(offset);
        return 2;
    }
    
    /**
     * Operation NOP - don't do anything
     * 
     * @param opCode
     * @return 
     */
    protected int opNOP(int opCode) {
        this.regPC+=2;
        return 2;
    }
    
    /**
     * Operation JMP - jump to address
     * 
     * @param opCode
     * @return 
     */
    protected int opJMP(int opCode) {
        this.regPC = (short)((opCode & 0x0FFF) << 1);
        return 2;
    }
    
    /**
     * Operation JZ - jump if zero
     * 
     * @param opCode
     * @return 
     */
    protected int opJZ(int opCode) {
        int offset = (opCode & 0x03FF) << 1;
        
        if((this.regFlags & FLAG_Z) == FLAG_Z) {
            this.regPC = (short)(0x1800 + offset);
        }
        else {
            this.regPC+=2;
        }
        return 2;
    }
    
    /**
     * Operation JNZ - jump if not zero
     * 
     * @param opCode
     * @return 
     */
    protected int opJNZ(int opCode) {
        int offset = (opCode & 0x03FF) << 1;
        
        if((this.regFlags & FLAG_Z) == 0) {
            this.regPC = (short)(0x1800 + offset);
        }
        else {
            this.regPC+=2;
        }
        return 2;
    }
    
    /**
     * Operation JC - jump if carry
     * 
     * @param opCode
     * @return 
     */
    protected int opJC(int opCode) {
        int offset = (opCode & 0x03FF) << 1;
        
        if((this.regFlags & FLAG_C) == FLAG_C) {
            this.regPC = (short)(0x1800 + offset);
        }
        else {
            this.regPC+=2;
        }
        return 2;
    }
    
    /**
     * Operation JNC - jump if not carry
     * 
     * @param opCode
     * @return 
     */
    protected int opJNC(int opCode) {
        int offset = (opCode & 0x03FF) << 1;
        
        if((this.regFlags & FLAG_C) == 0) {
            this.regPC = (short)(0x1800 + offset);
        }
        else {
            this.regPC+=2;
        }
        return 2;
    }
    
    /**
     * Operation BTJR - bit test and jump relative
     * 
     * @param opCode
     * @return 
     */
    protected int opBTJR(int opCode) {
        int bit = (opCode & 0x0C00) >> 10;
        int r = (opCode & 0x03E0) >> 5;
        int offset = (opCode & 0x001F);
        int bitVal = 1 << bit;
        
        //if((offset & 0x0010) == 0x0010) offset |= 0xFFFFFFF0;
        
        if((this.registers[this.regCurrentBank][r] & bitVal) == bitVal) {
            this.regPC += (offset << 1);
        }
        
        this.regPC+=2;
        
        return 2;
    }
    
    /**
     * Operation CPJR - compare register and jump relative
     * 
     * @param opCode
     * @return 
     */
    protected int opCPJR(int opCode) {
        int value = (opCode & 0x0C00) >> 10;
        int r = (opCode & 0x03E0) >> 5;
        int offset = (opCode & 0x001F);
        
        //if((offset & 0x0010) == 0x0010) offset |= 0xFFFFFFF0;
        
        if(this.registers[this.regCurrentBank][r] == value) {
            this.regPC += (offset << 1);
        }
        
        this.regPC+=2;
        
        return 2;
    }
    
    /**
     * Before each opcode, call this method to check if an IRQ is waiting
     * 
     * @throws MemoryException
     */
    public void checkIrq() throws MemoryException {
        if(this.inIrq) return;
        if(this.irq != 0) {
            // In order of precedence
            if((this.irq & IRQ_SECOND_TIMER) != 0) {
                this.inIrq = true;
                this.opCALL(0xAC01);
                this.irq -= IRQ_SECOND_TIMER;
            }
            else if((this.irq & IRQ_REDRAW_SCREEN) != 0) {
                this.inIrq = true;
                this.opCALL(0xAC0D);
                this.irq -= IRQ_REDRAW_SCREEN;
            }
            else if((this.irq & IRQ_SET_BUTTON) != 0) {
                this.inIrq = true;
                this.opCALL(0xAC08);
                this.irq -= IRQ_SET_BUTTON;
            }
            else if((this.irq & IRQ_MODE_BUTTON) != 0) {
                this.inIrq = true;
                this.opCALL(0xAC05);
                this.irq -= IRQ_MODE_BUTTON;
            }
            else if((this.irq & IRQ_TRANSMIT_BUTTON) != 0) {
                this.inIrq = true;
                this.opCALL(0xAC06);
                this.irq -= IRQ_TRANSMIT_BUTTON;
            }
            else if((this.irq & IRQ_SELECT_BUTTON) != 0) {
                this.inIrq = true;
                this.opCALL(0xAC07);
                this.irq -= IRQ_SELECT_BUTTON;
            }
        }
    }
    
    @Override
    public int runNextOpCode() throws MemoryException, OpCodeException {
        checkIrq();
        
        int opCode = Short.toUnsignedInt(readMemory16(Short.toUnsignedLong(this.regPC)));
        
        if((opCode >= 0x0000) && (opCode <= 0x03FF)) return opADD(opCode);
        if((opCode >= 0x0400) && (opCode <= 0x07FF)) return opADB(opCode);
        if((opCode >= 0x0800) && (opCode <= 0x0BFF)) return opSUB(opCode);
        if((opCode >= 0x0C00) && (opCode <= 0x0FFF)) return opSBB(opCode);
        if((opCode >= 0x1000) && (opCode <= 0x13FF)) return opADI(opCode);
        if((opCode >= 0x1400) && (opCode <= 0x17FF)) return opADBI(opCode);
        if((opCode >= 0x1800) && (opCode <= 0x1BFF)) return opSBI(opCode);
        if((opCode >= 0x1C00) && (opCode <= 0x1FFF)) return opSBBI(opCode);
        if((opCode >= 0x2000) && (opCode <= 0x23FF)) return opADM(opCode);
        if((opCode >= 0x2400) && (opCode <= 0x27FF)) return opADBM(opCode);
        if((opCode >= 0x2800) && (opCode <= 0x2BFF)) return opSBM(opCode);
        if((opCode >= 0x2C00) && (opCode <= 0x2FFF)) return opSBBM(opCode);
        if((opCode >= 0x3000) && (opCode <= 0x33FF)) return opCMP(opCode);
        if((opCode >= 0x3400) && (opCode <= 0x37FF)) return opCPM(opCode);
        if((opCode >= 0x3800) && (opCode <= 0x3BFF)) return opCPI(opCode);
        if((opCode >= 0x3C00) && (opCode <= 0x3C18)) return opLCRB(opCode);
        if((opCode >= 0x3E00) && (opCode <= 0x3E18)) return opLARB(opCode);
        if((opCode >= 0x4000) && (opCode <= 0x43FF)) return opANDI(opCode);
        if((opCode >= 0x4400) && (opCode <= 0x47FF)) return opORI(opCode);
        if((opCode >= 0x4800) && (opCode <= 0x4BFF)) return opXORI(opCode);
        // Hier gibt es Überschneidungen!!
        if((opCode >= 0x4C00) && (opCode <= 0x4FFF)) {
            if((opCode & 0x0018) == 0x0000) return opINC(opCode);
            if((opCode & 0x0018) == 0x0008) return opINCB(opCode);
            if((opCode & 0x0018) == 0x0010) return opDEC(opCode);
            if((opCode & 0x0018) == 0x0018) return opDECB(opCode);
        }
        if((opCode >= 0x5000) && (opCode <= 0x53FF)) {
            if((opCode & 0x0018) == 0x0000) return opRSHM(opCode);
            if((opCode & 0x0018) == 0x0008) return opLSHM(opCode);
        }
        if((opCode >= 0x5400) && (opCode <= 0x57FF)) return opIN(opCode);
        if((opCode >= 0x6000) && (opCode <= 0x63FF)) {
            if((opCode & 0x0018) == 0x0000) return opPSAM(opCode);
            if((opCode & 0x0018) == 0x0010) return opPLAM(opCode);
        }
        if((opCode >= 0x6400) && (opCode <= 0x67FF)) {
            if((opCode & 0x0008) == 0x0008) return opLDSM(opCode);
            if((opCode & 0x0008) == 0x0000) return opSTSM(opCode);
        }
        if((opCode >= 0x6800) && (opCode <= 0x6BFF)) return opSTLM(opCode);
        if((opCode >= 0x6C00) && (opCode <= 0x6FFF)) return opSTL(opCode);
        if((opCode >= 0x7000) && (opCode <= 0x77FF)) return opPSAI(opCode);
        if((opCode >= 0x7800) && (opCode <= 0x7BFF)) return opPLAI(opCode);
        if((opCode >= 0x7C00) && (opCode <= 0x7FFF)) return opSTLI(opCode);
        if((opCode >= 0x8000) && (opCode <= 0x83FF)) return opMOV(opCode);
        if((opCode >= 0x8400) && (opCode <= 0x87FF)) return opMOVM(opCode);
        if((opCode >= 0x8800) && (opCode <= 0x8BFF)) return opLDI(opCode);
        if((opCode >= 0x8C00) && (opCode <= 0x8FFF)) return opCLRM(opCode);
        if((opCode >= 0x9000) && (opCode <= 0x93FF)) return opMVAC(opCode);
        if((opCode >= 0x9400) && (opCode <= 0x97FF)) return opMVACM(opCode);
        if((opCode >= 0x9800) && (opCode <= 0x9BFF)) return opMVCA(opCode);
        if((opCode >= 0x9C00) && (opCode <= 0x9FFF)) return opMVCAM(opCode);
        if((opCode >= 0xA000) && (opCode <= 0xAFFF)) return opCALL(opCode);
        if(opCode == 0xB000) return opRET(opCode);
        if(opCode == 0xB001) return opHLT(opCode);
        if((opCode >= 0xB400) && (opCode <= 0xB7FF)) return opCPFJR(opCode);
        if((opCode >= 0xB800) && (opCode <= 0xBBFF)) return opIJMR(opCode);
        if((opCode >= 0xBC00) && (opCode <= 0xBFFF)) return opNOP(opCode);
        if((opCode >= 0xC000) && (opCode <= 0xCFFF)) return opJMP(opCode);
        if((opCode >= 0xD000) && (opCode <= 0xD3FF)) return opJZ(opCode);
        if((opCode >= 0xD400) && (opCode <= 0xD7FF)) return opJNZ(opCode);
        if((opCode >= 0xD800) && (opCode <= 0xDBFF)) return opJC(opCode);
        if((opCode >= 0xDC00) && (opCode <= 0xDFFF)) return opJNC(opCode);
        if((opCode >= 0xE000) && (opCode <= 0xEFFF)) return opBTJR(opCode);
        if((opCode >= 0xF000) && (opCode <= 0xFFFF)) return opCPJR(opCode);
        
        throw new OpCodeException("Illegal Opcode " + opCode);
    }

    @Override
    public void handleIRQ(int IRQNumber) {
        switch (IRQNumber) {
            case IRQ_SET_BUTTON:
                this.irq |= IRQNumber;
                break;
                
            case IRQ_MODE_BUTTON:
                this.irq |= IRQNumber;
                break;
                
            case IRQ_TRANSMIT_BUTTON:
                this.irq |= IRQNumber;
                break;
                
            case IRQ_SELECT_BUTTON:
                this.irq |= IRQNumber;
                break;
                
            case IRQ_SECOND_TIMER:
                this.irq |= IRQNumber;
                break;
                
            case IRQ_REDRAW_SCREEN:
                this.irq |= IRQNumber;
                break;
                
            default:
                throw new AssertionError();
        }
    }
    
}
