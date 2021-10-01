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

/**
 * This is just a dummy implementation of the IO Interface
 * 
 * @author peter
 */
public class SimpleIO implements IO {
    long lowAddress;
    long highAddress;
    
    public SimpleIO(long addressSize) {
        this(0L, addressSize);
    }
    
    public SimpleIO(long lowAddress, long highAddress) {
        this.lowAddress = lowAddress;
        this.highAddress = highAddress;
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
        return false;
    }

    @Override
    public boolean isWriteable() {
        return false;
    }

    @Override
    public boolean isLittleEndian() {
        return false;
    }

    @Override
    public int getBitSize() {
        return 8;
    }

    @Override
    public int getContent(long address) throws MemoryException {
        return getByte(address);
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

    @Override
    public void setContent(long address, int value) throws MemoryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
