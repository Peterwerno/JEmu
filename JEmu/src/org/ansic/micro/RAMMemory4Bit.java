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
 * This class implements a 4 bit RAM memory
 * 
 * @author peter
 */
public class RAMMemory4Bit implements Memory {
    byte[] memoryContent;
    long lowAddress;
    long highAddress;
    boolean littleEndian;

    /**
     * Creates a new instance ot RAMMemory4Bit with a given size (in # of
     * nibbles)
     * 
     * @param size (long) the memory size
     */
    public RAMMemory4Bit(long size) {
        this.lowAddress = 0L;
        this.highAddress = size;
        this.memoryContent = new byte[(int)size];
        this.littleEndian = false;
    }

    /**
     * Creates a new instance of RAMMemory4Bit with a given low and high
     * address.
     * 
     * @param lowAddress (long) the low memory address
     * @param highAddress (long) the high memory address
     */
    public RAMMemory4Bit(long lowAddress, long highAddress) {
        this.lowAddress = lowAddress;
        this.highAddress = highAddress;
        this.memoryContent = new byte[(int)(highAddress - lowAddress)];
        this.littleEndian = false;
    }

    /**
     * Returns the low address number
     * 
     * @return the low address (long)
     */
    @Override
    public long getLowAddress() {
        return this.lowAddress;
    }

    /**
     * Returns the high address number
     * 
     * @return the high address (long)
     */
    @Override
    public long getHighAddress() {
        return this.highAddress;
    }

    /**
     * Returns wether the memory is readable
     * 
     * @return true
     */
    @Override
    public boolean isReadable() {
        return true;
    }

    /**
     * Returns wether the memory is writeable
     * 
     * @return true
     */
    @Override
    public boolean isWriteable() {
        return true;
    }

    /**
     * Returns wether the memory is little endian (TODO: Check what this means
     * for 4 bit!)
     * 
     * @return the little endian flag
     */
    @Override
    public boolean isLittleEndian() {
        return this.littleEndian;
    }

    @Override
    public int getBitSize() {
        return 4;
    }

    /**
     * Returns one nibble (4 bit) from the memory
     * 
     * @param address (long) the address
     * @return the memory content
     * @throws MemoryException 
     */
    @Override
    public int getContent(long address) throws MemoryException {
        if((address < this.lowAddress) || (address >= this.highAddress)) 
            throw new MemoryException("No memory at address " + address);
        return Byte.toUnsignedInt(this.memoryContent[(int)(address - this.lowAddress)]) & 0x0F;
    }
    
    @Override
    public byte getByte(long address) throws MemoryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public short getShort(long address) throws MemoryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getInt(long address) throws MemoryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getLong(long address) throws MemoryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Writes a nibble (4 bit) to memory
     * 
     * @param address (long) the address
     * @param value (int) the new value
     * @throws MemoryException 
     */
    @Override
    public void setContent(long address, int value) throws MemoryException {
        if((address < this.lowAddress) || (address >= this.highAddress)) 
            throw new MemoryException("No memory at address " + address);
        if((value < 0) || (value > 15))
            throw new MemoryException("Memory can only store 4 bit data");
        this.memoryContent[(int)(address - this.lowAddress)] = (byte)value;
    }

    @Override
    public void setByte(long address, byte value) throws MemoryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setShort(long address, short value) throws MemoryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setInt(long address, int value) throws MemoryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLong(long address, long value) throws MemoryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
