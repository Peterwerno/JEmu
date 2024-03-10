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

import java.util.List; 

/**
 * This interface defines the methods that must be implemented by a CPU
 * class. 
 * 
 * @author peter
 */
public interface CPU {
    
    /**
     * This method is used to execute a single op code.
     * It should perform the action behind the op code and return the number
     * of clock cycles this *should* have taken. That way, the caller can
     * then arrange for synchronisation
     * 
     * @return the number of clock cycles (int)
     * @throws MemoryException
     * @throws OpCodeException 
     */
    public int runNextOpCode() throws MemoryException, OpCodeException;
    
    
}
