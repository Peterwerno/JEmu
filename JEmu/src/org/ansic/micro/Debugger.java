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

import java.util.List;
import java.util.StringTokenizer;

/**
 * This interface defines methods that a debugger interface must provide for a
 * given CPU.
 * 
 * @author peter
 */
public interface Debugger extends CPU {
    public abstract static class Mnemonic {
        /**
         * Checks if a string "param" is within a list "optList" (comma 
         * separated list of strings)
         * 
         * @param param
         * @param optList
         * @return 
         */
        protected boolean isIn(String param, String optList) {
            StringTokenizer st = new StringTokenizer(optList, ",");
            while(st.hasMoreTokens()) {
                if(param.equals(st.nextToken()))
                    return true;
            }
            return false;
        }
        
        protected boolean isNumeric(String param) {
            int value = 0;
            
            // $ indicates hex value
            if(param.startsWith("$")) {
                try {
                    value = Integer.parseInt(param.substring(1), 16);
                    return true;
                }
                catch (NumberFormatException ex) {
                    
                }
            }
            // b indicates binary value
            else if(param.startsWith("b")) {
                try {
                    value = Integer.parseInt(param.substring(1), 2);
                    return true;
                }
                catch (NumberFormatException ex) {
                    
                }
            }
            // otherwise, decimal
            else {
                try {
                    value = Integer.parseInt(param);
                    return true;
                }
                catch (NumberFormatException ex) {
                    
                }
            }
            
            return false;
        }
        
        /**
         * Returns the numeric value of a parameter
         * 
         * @param param
         * @return
         * @throws SyntaxErrorException 
         */
        protected int getNumeric(String param) throws SyntaxErrorException {
            int value;
            
            // $ indicates hex value
            if(param.startsWith("$")) {
                try {
                    value = Integer.parseInt(param.substring(1), 16);
                    return value;
                }
                catch (NumberFormatException ex) {
                    // exception thrown below
                }
            }
            // b indicates binary value
            else if(param.startsWith("b")) {
                try {
                    value = Integer.parseInt(param.substring(1), 2);
                    return value;
                }
                catch (NumberFormatException ex) {
                    // exception thrown below
                }
            }
            // otherwise, decimal
            else {
                try {
                    value = Integer.parseInt(param);
                    return value;
                }
                catch (NumberFormatException ex) {
                    // exception thrown below
                }
            }
            
            throw new SyntaxErrorException(param + " is not numeric value");
        }
        
        public abstract byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException;
    }
    
    /**
     * Subclass combinig code length and opcode as string which is used as a 
     * return type in getCodeAndLength
     */
    public static class CodeAndLength {
        long codeLength;
        String code;
        
        public CodeAndLength() {
            this.codeLength = 0L;
            this.code = "";
        }
        
        public CodeAndLength(long codeLength, String code) {
            this.codeLength = codeLength;
            this.code = code;
        }
        
        public void setCodeLength(long codeLength) {
            this.codeLength = codeLength;
        }
        
        public long getCodeLength() {
            return this.codeLength;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return this.code;
        }
    }
    
    /**
     * Return the code length of the opcode that the given address points to.
     * 
     * @param address (long) the address
     * @return the code length of the opcode at the given address (long)
     * @throws MemoryException if the address is not in memory
     * @throws OpCodeException if the opcode at this address is not valid
     */
    public long getCodeLength(long address) throws MemoryException, OpCodeException;
    
    /**
     * Return the opcode as string at a given address
     * 
     * @param address (long) the address
     * @return the opcode at the given address (String)
     * @throws MemoryException if the address is not in memory
     * @throws OpCodeException if the opcode at this address is not valid
     */
    public String getCode(long address) throws MemoryException, OpCodeException;
    
    /**
     * Return a combination of opcode length and opcode (as string) at the
     * given address.
     * 
     * @param address (long) the address
     * @return the opcode and length at the given address (CodeAndLength)
     * @throws MemoryException if the address is not in memory
     * @throws OpCodeException if the opcode at this address is not valid
     */
    public CodeAndLength getCodeAndLength(long address) throws MemoryException, OpCodeException;
    
    /**
     * Translate a given mnemonic/command to machine code
     * 
     * @param mnemonic (String) the command in assembly language
     * @return the machine code (byte[])
     * @throws SyntaxErrorException if the mnemonic is not syntactically correct
     */
    public byte[] translate(String mnemonic) throws SyntaxErrorException;
    
    /**
     * Returns a list containing all the register names that are supported
     * by this CPU.
     * 
     * @return the list of register names (List&lt;String&gt;)
     */
    public List<String> getRegisterNames();
    
    /**
     * Sets the register to a different value
     * 
     * @param registerName (String) the register name
     * @param registerValue (long) the new value of the register
     * @throws IllegalRegisterException if the register does not exist
     * @throws IllegalRegisterValueException if the value does not fit in the register
     */
    public void setRegisterValue(String registerName, long registerValue) throws IllegalRegisterException, IllegalRegisterValueException;
    
    /**
     * Returns the content of a register
     * 
     * @param registerName (String) the register name
     * @return the register content (long)
     * @throws IllegalRegisterException if the register does not exist
     */
    public long getRegisterValue(String registerName) throws IllegalRegisterException;
    
    /**
     * Returns the value of the program counter
     * 
     * @return the pc (long)
     */
    public long getProgramCounter();
    
    /**
     * Sets the value of the program counter
     * 
     * @param programCounter (long) the new program counter
     * @throws IllegalRegisterValueException 
     */
    public void setProgramCounter(long programCounter) throws IllegalRegisterValueException;
    
    /**
     * Returns the size (in bits) of a register
     * 
     * @param registerName (String) the register name
     * @return the size (in bits) of the register (int)
     * @throws IllegalRegisterException if the register does not exist
     */
    public int getRegisterSize(String registerName) throws IllegalRegisterException;
    
    /**
     * Returns a byte from memory
     * 
     * @param address
     * @return
     * @throws MemoryException 
     */
    public byte readMemoryByte(long address) throws MemoryException;
    
    /**
     * Writes a byte to memory
     * 
     * @param address
     * @param value
     * @throws MemoryException 
     */
    public void writeMemoryByte(long address, byte value) throws MemoryException;
}
