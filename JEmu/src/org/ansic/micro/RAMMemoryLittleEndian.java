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

/**
 * This class implements a RAM memory which stores the data in little endian
 * mode.
 * 
 * @author peter
 */
public class RAMMemoryLittleEndian implements Memory {
    long lowAddress;
    long highAddress;
    byte[] content;
    
    /**
     * Creates a new instance of RAMMemoryLittleEndian starting at address 0
     * with a given size
     * 
     * @param size (long) the memory size
     * @throws MemoryException if there was a problem
     */
    public RAMMemoryLittleEndian(long size) throws MemoryException {
        this.lowAddress = 0L;
        this.highAddress = size;
        if(size > 0x00000000FFFFFFFFL)
            throw new MemoryException("Memory size too large");
        if(size < 0L)
            throw new MemoryException("Memory size cannot be negative!");
        
        this.content = new byte[(int)size];
    }
    
    /**
     * Creates a new instance of RAMMemoryLittleEndian with predefined address
     * range
     * 
     * @param lowAddress (long) the starting range of the memory
     * @param highAddress (long) the ending range of the memory
     * @throws MemoryException if there was a problem
     */
    public RAMMemoryLittleEndian(long lowAddress, long highAddress) throws MemoryException {
        this.lowAddress = lowAddress;
        this.highAddress = highAddress;
        
        if((highAddress - lowAddress) > 0x00000000FFFFFFFFL)
            throw new MemoryException("Memory size too large");
        if(lowAddress > highAddress)
            throw new MemoryException("high address must be greater than low address!");
        
        this.content = new byte[(int)(highAddress - lowAddress)];
    }

    @Override
    public long getLowAddress() {
        return this.lowAddress;
    }

    @Override
    public long getHighAddress() {
        return this.highAddress;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isWriteable() {
        return true;
    }

    @Override
    public boolean isLittleEndian() {
        return true;
    }

    @Override
    public int getBitSize() {
        return 8;
    }

    @Override
    public int getContent(long address) throws MemoryException {
        return Byte.toUnsignedInt(getByte(address));
    }
    
    @Override
    public byte getByte(long address) throws MemoryException {
        if((address < this.lowAddress) || (address >= this.highAddress))
            throw new MemoryException("address out of range");
        return this.content[(int)(address - this.lowAddress)];
    }

    @Override
    public short getShort(long address) throws MemoryException {
        int byte1 = ((int)getByte(address)) & 0xFF;
        int byte2 = ((int)getByte(address + 1)) & 0xFF;
        
        return (short)(byte2<<8 + byte1);
    }

    @Override
    public int getInt(long address) throws MemoryException {
        int byte1 = ((int)getByte(address)) & 0xFF;
        int byte2 = ((int)getByte(address + 1)) & 0xFF;
        int byte3 = ((int)getByte(address + 2)) & 0xFF;
        int byte4 = ((int)getByte(address + 3)) & 0xFF;
        
        return byte4<<24 + byte3<<16 + byte2<<8 + byte1;
    }

    @Override
    public long getLong(long address) throws MemoryException {
        long byte1 = ((long)getByte(address)) & 0xFF;
        long byte2 = ((long)getByte(address + 1)) & 0xFF;
        long byte3 = ((long)getByte(address + 2)) & 0xFF;
        long byte4 = ((long)getByte(address + 3)) & 0xFF;
        long byte5 = ((long)getByte(address + 4)) & 0xFF;
        long byte6 = ((long)getByte(address + 5)) & 0xFF;
        long byte7 = ((long)getByte(address + 6)) & 0xFF;
        long byte8 = ((long)getByte(address + 7)) & 0xFF;
        
        return byte8<<56 + byte7<<48 + byte6<<40 + byte5<<32 + byte4<<24 + byte3<<16 + byte2<<8 + byte1;
    }

    @Override
    public void setContent(long address, int value) throws MemoryException {
        if((address < this.lowAddress) || (address >= this.highAddress))
            throw new MemoryException("address out of range");
        content[(int)(address - this.lowAddress)] = (byte)value;
    }

    @Override
    public void setByte(long address, byte value) throws MemoryException {
        if((address < this.lowAddress) || (address >= this.highAddress))
            throw new MemoryException("address out of range");
        content[(int)(address - this.lowAddress)] = value;
    }

    @Override
    public void setShort(long address, short value) throws MemoryException {
        int byte1 = (int)value & 0xFF;
        int byte2 = (((int)value) >> 8) & 0xFF;
        
        setByte(address, (byte)byte1);
        setByte(address + 1, (byte)byte2);
    }

    @Override
    public void setInt(long address, int value) throws MemoryException {
        int byte1 = (int)value & 0xFF;
        int byte2 = (((int)value) >> 8) & 0xFF;
        int byte3 = (((int)value) >> 16) & 0xFF;
        int byte4 = (((int)value) >> 24) & 0xFF;
        
        setByte(address, (byte)byte1);
        setByte(address + 1, (byte)byte2);
        setByte(address + 2, (byte)byte3);
        setByte(address + 3, (byte)byte4);
    }

    @Override
    public void setLong(long address, long value) throws MemoryException {
        long byte1 = (long)value & 0xFF;
        long byte2 = (((long)value) >> 8) & 0xFF;
        long byte3 = (((long)value) >> 16) & 0xFF;
        long byte4 = (((long)value) >> 24) & 0xFF;
        long byte5 = (((long)value) >> 32) & 0xFF;
        long byte6 = (((long)value) >> 40) & 0xFF;
        long byte7 = (((long)value) >> 48) & 0xFF;
        long byte8 = (((long)value) >> 56) & 0xFF;
        
        setByte(address, (byte)byte1);
        setByte(address + 1, (byte)byte2);
        setByte(address + 2, (byte)byte3);
        setByte(address + 3, (byte)byte4);
        setByte(address + 4, (byte)byte5);
        setByte(address + 5, (byte)byte6);
        setByte(address + 6, (byte)byte7);
        setByte(address + 7, (byte)byte8);
    }

}
