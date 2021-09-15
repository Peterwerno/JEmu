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
 * This interface defines a memory. Any concrete type of memory should
 * implement this interface and should define the required methods.
 * 
 * @author peter
 */
public interface Memory {
    /**
     * This method returns the lowest memory address that is supported by
     * this memory "chip". If the memory address covers the 16KB 
     * from 0x4000 to 0x7FFF, then this function should return 0x4000.
     * 
     * @return the memory starting address.
     */
    public long getLowAddress();
    
    /**
     * This method returns the top memory address (exclusive) that is supported
     * by this memory "chip". If the memory address coveres the 16KB
     * from 0x4000 to 0x7FFF, then this function should return 0x8000.
     * 
     * @return the memory ending address (excluding)
     */
    public long getHighAddress();
    
    /**
     * This method returns whether this memory is readable (most memory should
     * be)
     * 
     * @return whether the memory is readable
     */
    public boolean isReadable();
    
    /**
     * This method returns whether this memory is writeable, which would be
     * true for "regular" RAM, but false for ROM memory.
     * 
     * @return whether the memory is writeable
     */
    public boolean isWriteable();
    
    /**
     * This method returns whether this memory stores the data in little endian
     * mode. This is important when reading 16 bit or larger data at once.
     * 
     * @return whether the data is stores in little endian format (boolean)
     */
    public boolean isLittleEndian();
    
    /**
     * This method returns a single byte (8 bit) from the memory for a given
     * address
     * 
     * @param address (long) the memory address
     * @return the content of the memory at that address (byte)
     * @throws MemoryException if there was a problem
     */
    public byte getByte(long address) throws MemoryException;
    
    /**
     * This method returns a single word (16 bit) from the memory for a given
     * address
     * 
     * @param address (long) the memory address
     * @return the content of the 2 bytes starting at that address (short)
     * @throws MemoryException if there was a problem
     */
    public short getShort(long address) throws MemoryException;
    
    /**
     * This method returns a single int (32 bit) from the memory for a given
     * address
     * 
     * @param address (long) the memory address
     * @return the content of the 4 bytes starting at that address (int)
     * @throws MemoryException if there was a problem
     */
    public int getInt(long address) throws MemoryException;
    
    /**
     * This method returns a single long (64 bit) from the memory for a given
     * address
     * 
     * @param address (long) the memory address
     * @return the content of the 8 bytes starting at that address (long)
     * @throws MemoryException if there was a problem
     */
    public long getLong(long address) throws MemoryException;
    
    /**
     * This method stores a single byte (8 bit) in memory at the given address
     * 
     * @param address (long) the memory address
     * @param value (byte) the value to store
     * @throws MemoryException if there was a problem
     */
    public void setByte(long address, byte value) throws MemoryException;
    
    /**
     * This method stores a single word (16 bit) in memory at the given address
     * 
     * @param address (long) the memory address
     * @param value (byte) the value to store
     * @throws MemoryException if there was a problem
     */
    public void setShort(long address, short value) throws MemoryException;
    
    /**
     * This method stores a single int (32 bit) in memory at the given address
     * 
     * @param address (long) the memory address
     * @param value (byte) the value to store
     * @throws MemoryException if there was a problem
     */
    public void setInt(long address, int value) throws MemoryException;
    
    /**
     * This method stores a single long (64 bit) in memory at the given address
     * 
     * @param address (long) the memory address
     * @param value (byte) the value to store
     * @throws MemoryException if there was a problem
     */
    public void setLong(long address, long value) throws MemoryException;
}
