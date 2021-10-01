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
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the debugger for an Intel 4004 CPU.
 * 
 * @author peter
 */
public class Intel4004Debugger extends Intel4004 implements Debugger {
    public static final String[] conditionNames = new String[]{"", "T", "C", "CT", "Z", "ZT", "ZC", "ZCT", "N", "NT", "NC", "NCT", "NZ", "NZT", "NZC", "NZCT" };
    List<String> registerNames = new ArrayList<>();
    
    public abstract static class Intel4004Mnemonic extends Debugger.Mnemonic {
        public boolean isRegisterNumber(String param) throws SyntaxErrorException {
            if((param.startsWith("R") || param.startsWith("r")) && isNumeric(param.substring(1))) {
                int regNum = getNumeric(param.substring(1));
                return (regNum >= 0) && (regNum < 16);
            }
            else if((param.startsWith("P") || param.startsWith("p")) && isNumeric(param.substring(1))) {
                int regNum = getNumeric(param.substring(1));
                return (regNum >= 0) && (regNum < 8);
            }
            else
                return false;
        }
        
        public int getRegisterNumber(String param) throws SyntaxErrorException {
            if(param.startsWith("R") || param.startsWith("r")) {
                return getNumeric(param.substring(1));
            }
            else if(param.startsWith("P") || param.startsWith("p")) {
                return getNumeric(param.substring(1))<<1;
            }
            else
                return -1;
        }
        
        public boolean isCondition(String condition) {
            for(int i=0; i<conditionNames.length; i++) {
                if(condition.equalsIgnoreCase(conditionNames[i]))
                    return true;
            }
            return false;
        }
        
        public int getConditionNumber(String condition) {
            for(int i=0; i<conditionNames.length; i++) {
                if(condition.equalsIgnoreCase(conditionNames[i]))
                    return i;
            }
            return -1;
        }
    }

    /**
     * Class NOP - No operation
     * implements the assembler for the NOP command
     */
    public static class NOP extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x00, (byte)0x00};
        }
    }
    
    /**
     * Class JCN - Jump conditional
     * implements the assembler for the ADD command
     */
    public static class JCN extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JCN command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JCN command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isCondition(param1) && isNumeric(param2)) {
                int cond = getConditionNumber(param1);
                int address = getNumeric(param2);
                
                if((cond < 0) || (cond > 15)) throw new SyntaxErrorException("Condition Number must be within [0..15]");
                if((address < 0) || (address > 255)) throw new SyntaxErrorException("Address offset must be within [0..255]");
                
                return new byte[]{(byte)0x01, (byte)cond, (byte)(address>>4), (byte)(address&0x0F)};
            }
            else
                throw new SyntaxErrorException("Syntax error in JCN command");
        }
    }
    
    /**
     * Class FIM - Fetch Immediate Direct
     * implements the assembler for the FIM command
     */
    public static class FIM extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("FIM command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("FIM command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isNumeric(param2)) {
                int reg = getRegisterNumber(param1);
                int address = getNumeric(param2);
                
                if((reg < 0) || (reg > 15)) throw new SyntaxErrorException("Register Number must be within [0..15]");
                if((reg & 0x01) == 0x01) throw new SyntaxErrorException("Register Number must be an even number");
                if((address < 0) || (address > 255)) throw new SyntaxErrorException("Address offset must be within [0..255]");
                
                return new byte[]{(byte)0x02, (byte)reg, (byte)(address>>4), (byte)(address&0x0F)};
            }
            else
                throw new SyntaxErrorException("Syntax error in FIM command");
        }
    }
    
    /**
     * Class FIN - Fetch indirect
     * implements the assembler for the FIN command
     */
    public static class FIN extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("FIN command requires 1 parameters");
            String param1 = parameters.nextToken(" ,");
            
            if(isRegisterNumber(param1)) {
                int reg = getConditionNumber(param1);
                
                if((reg < 0) || (reg > 15)) throw new SyntaxErrorException("Condition Number must be within [0..15]");
                if((reg & 0x01) == 0x01) throw new SyntaxErrorException("Register Number must be an even number");
                
                return new byte[]{(byte)0x03, (byte)reg};
            }
            else
                throw new SyntaxErrorException("Syntax error in JCN command");
        }
    }
    
    /**
     * Class JIN - Jump indirect
     * implements the assembler for the JIN command
     */
    public static class JIN extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JIN command requires 1 parameters");
            String param1 = parameters.nextToken(" ,");
            
            if(isRegisterNumber(param1)) {
                int reg = getRegisterNumber(param1);
                
                if((reg < 0) || (reg > 15)) throw new SyntaxErrorException("Register Number must be within [0..15]");
                if((reg & 0x01) == 0x01) throw new SyntaxErrorException("Register Number must be an even number");
                
                return new byte[]{(byte)0x03, (byte)(reg | 0x01)};
            }
            else
                throw new SyntaxErrorException("Syntax error in JIN command");
        }
    }
    
    /**
     * Class JUN - Jump unconditional
     * implements the assembler for the JUN command
     */
    public static class JUN extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JUN command requires 1 parameters");
            String param1 = parameters.nextToken(" ,");
            
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                
                if((address < 0) || (address > 4095)) throw new SyntaxErrorException("Address must be within [0..4095]");
                
                return new byte[]{(byte)0x04, (byte)((address >> 8) & 0x0F), (byte)((address >> 4) & 0x0F), (byte)(address & 0x0F)};
            }
            else
                throw new SyntaxErrorException("Syntax error in JUN command");
        }
    }
    
    /**
     * Class JMS - Jump unconditional
     * implements the assembler for the JMS command
     */
    public static class JMS extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JMS command requires 1 parameters");
            String param1 = parameters.nextToken(" ,");
            
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                
                if((address < 0) || (address > 4095)) throw new SyntaxErrorException("Address must be within [0..4095]");
                
                return new byte[]{(byte)0x05, (byte)((address >> 8) & 0x0F), (byte)((address >> 4) & 0x0F), (byte)(address & 0x0F)};
            }
            else
                throw new SyntaxErrorException("Syntax error in JMS command");
        }
    }
    
    /**
     * Class INC - Increment
     * implements the assembler for the INC command
     */
    public static class INC extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("INC command requires 1 parameters");
            String param1 = parameters.nextToken(" ,");
            
            if(isRegisterNumber(param1)) {
                int reg = getRegisterNumber(param1);
                
                if((reg < 0) || (reg > 15)) throw new SyntaxErrorException("Register Number must be within [0..15]");
                
                return new byte[]{(byte)0x06, (byte)(reg)};
            }
            else
                throw new SyntaxErrorException("Syntax error in INC command");
        }
    }
    
    /**
     * Class ISZ - Increment
     * implements the assembler for the ISZ command
     */
    public static class ISZ extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ISZ command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ISZ command requires 2 parameters");
            String param2 = parameters.nextToken(" ,");
            
            if(isRegisterNumber(param1) && isNumeric(param2)) {
                int reg = getRegisterNumber(param1);
                int address = getNumeric(param2);
                
                if((reg < 0) || (reg > 15)) throw new SyntaxErrorException("Register Number must be within [0..15]");
                if((address < 0) || (address > 255)) throw new SyntaxErrorException("Address must be within [0..255]");
                
                return new byte[]{(byte)0x07, (byte)(reg), (byte)((address >> 4) & 0x0F), (byte)(address & 0x0F)};
            }
            else
                throw new SyntaxErrorException("Syntax error in ISZ command");
        }
    }
    
    /**
     * Class ADD - Add to accumulator with carry
     * implements the assembler for the ADD command
     */
    public static class ADD extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ADD command requires 1 parameters");
            String param1 = parameters.nextToken(" ,");
            
            if(isRegisterNumber(param1)) {
                int reg = getRegisterNumber(param1);
                
                if((reg < 0) || (reg > 15)) throw new SyntaxErrorException("Register Number must be within [0..15]");
                
                return new byte[]{(byte)0x08, (byte)(reg)};
            }
            else
                throw new SyntaxErrorException("Syntax error in ADD command");
        }
    }
    
    /**
     * Class SUB - Subtract from accumulator with carry
     * implements the assembler for the SUB command
     */
    public static class SUB extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SUB command requires 1 parameters");
            String param1 = parameters.nextToken(" ,");
            
            if(isRegisterNumber(param1)) {
                int reg = getRegisterNumber(param1);
                
                if((reg < 0) || (reg > 15)) throw new SyntaxErrorException("Register Number must be within [0..15]");
                
                return new byte[]{(byte)0x09, (byte)(reg)};
            }
            else
                throw new SyntaxErrorException("Syntax error in SUB command");
        }
    }
    
    /**
     * Class LD - Load accumulator
     * implements the assembler for the LD command
     */
    public static class LD extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("LD command requires 1 parameters");
            String param1 = parameters.nextToken(" ,");
            
            if(isRegisterNumber(param1)) {
                int reg = getRegisterNumber(param1);
                
                if((reg < 0) || (reg > 15)) throw new SyntaxErrorException("Register Number must be within [0..15]");
                
                return new byte[]{(byte)0x0A, (byte)(reg)};
            }
            else
                throw new SyntaxErrorException("Syntax error in LD command");
        }
    }
    
    /**
     * Class XCH - Exchange with accumulator
     * implements the assembler for the XCH command
     */
    public static class XCH extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("XCH command requires 1 parameters");
            String param1 = parameters.nextToken(" ,");
            
            if(isRegisterNumber(param1)) {
                int reg = getRegisterNumber(param1);
                
                if((reg < 0) || (reg > 15)) throw new SyntaxErrorException("Register Number must be within [0..15]");
                
                return new byte[]{(byte)0x0B, (byte)(reg)};
            }
            else
                throw new SyntaxErrorException("Syntax error in XCH command");
        }
    }
    
    /**
     * Class BBL - Branch Back and Load
     * implements the assembler for the BBL command
     */
    public static class BBL extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("BBL command requires 1 parameters");
            String param1 = parameters.nextToken(" ,");
            
            if(isNumeric(param1)) {
                int value = getNumeric(param1);
                
                if((value < 0) || (value > 15)) throw new SyntaxErrorException("Value must be within [0..15]");
                
                return new byte[]{(byte)0x0C, (byte)(value)};
            }
            else
                throw new SyntaxErrorException("Syntax error in BBL command");
        }
    }
    
    /**
     * Class LDM - Load Data to Accumulator
     * implements the assembler for the LDM command
     */
    public static class LDM extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("LDM command requires 1 parameters");
            String param1 = parameters.nextToken(" ,");
            
            if(isNumeric(param1)) {
                int value = getNumeric(param1);
                
                if((value < 0) || (value > 15)) throw new SyntaxErrorException("Value must be within [0..15]");
                
                return new byte[]{(byte)0x0D, (byte)(value)};
            }
            else
                throw new SyntaxErrorException("Syntax error in LDM command");
        }
    }
    
    /**
     * Class CLB - Clear Both
     * implements the assembler for the CLB command
     */
    public static class CLB extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0F, (byte)(0x00)};
        }
    }
    
    /**
     * Class CLC - Clear Carry
     * implements the assembler for the CLC command
     */
    public static class CLC extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0F, (byte)(0x01)};
        }
    }
    
    /**
     * Class IAC - Increment Accumulator
     * implements the assembler for the IAC command
     */
    public static class IAC extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0F, (byte)(0x02)};
        }
    }
    
    /**
     * Class CMC - Complement Carry
     * implements the assembler for the CMC command
     */
    public static class CMC extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0F, (byte)(0x03)};
        }
    }
    
    /**
     * Class CMA - Complement Accumulator
     * implements the assembler for the CMA command
     */
    public static class CMA extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0F, (byte)(0x04)};
        }
    }
    
    /**
     * Class RAL - Rotate Left Accumulator through carry
     * implements the assembler for the RAL command
     */
    public static class RAL extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0F, (byte)(0x05)};
        }
    }
    
    /**
     * Class RAR - Rotate Right Accumuluator through carry
     * implements the assembler for the RAR command
     */
    public static class RAR extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0F, (byte)(0x06)};
        }
    }
    
    /**
     * Class TCC - Transmit Carry and Clear
     * implements the assembler for the TCC command
     */
    public static class TCC extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0F, (byte)(0x07)};
        }
    }
    
    /**
     * Class DAC - Decrement Accumulator
     * implements the assembler for the DAC command
     */
    public static class DAC extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0F, (byte)(0x08)};
        }
    }
    
    /**
     * Class TCS - Transfer Carry, Subtract & clear carry
     * implements the assembler for the TCS command
     */
    public static class TCS extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0F, (byte)(0x09)};
        }
    }
    
    /**
     * Class STC - Set Carry
     * implements the assembler for the STC command
     */
    public static class STC extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0F, (byte)(0x0A)};
        }
    }
    
    /**
     * Class DAA - Decimal Adjust Accumulator
     * implements the assembler for the DAA command
     */
    public static class DAA extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0F, (byte)(0x0B)};
        }
    }
    
    /**
     * Class KBP - Keyboard Process
     * implements the assembler for the KBP command
     */
    public static class KBP extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0F, (byte)(0x0C)};
        }
    }
    
    /**
     * Class DCL - Designate Command Line
     * implements the assembler for the DCL command
     */
    public static class DCL extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0F, (byte)(0x0D)};
        }
    }
    
    /**
     * Class SRC - Send Register Control
     * implements the assembler for the SRC command
     */
    public static class SRC extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SRC command requires 1 parameters");
            String param1 = parameters.nextToken(" ,");
            
            if(isRegisterNumber(param1)) {
                int reg = getRegisterNumber(param1);
                
                if((reg < 0) || (reg > 15)) throw new SyntaxErrorException("Register Number must be within [0..15]");
                if((reg & 0x01) == 0x01) throw new SyntaxErrorException("Register Number must be an even number");
                
                return new byte[]{(byte)0x02, (byte)(reg | 0x01)};
            }
            else
                throw new SyntaxErrorException("Syntax error in SRC command");
        }
    }
    
    /**
     * Class WRM - Write to RAM
     * implements the assembler for the WRM command
     */
    public static class WRM extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0E, (byte)(0x00)};
        }
    }
    
    /**
     * Class WMP - Write to RAM IO
     * implements the assembler for the WMP command
     */
    public static class WMP extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0E, (byte)(0x01)};
        }
    }
    
    /**
     * Class WRR - Write to ROM IO
     * implements the assembler for the WRR command
     */
    public static class WRR extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0E, (byte)(0x02)};
        }
    }
    
    /**
     * Class WPM - Write to Program Memory
     * implements the assembler for the WPM command
     */
    public static class WPM extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0E, (byte)(0x03)};
        }
    }
    
    /**
     * Class WR0 - Write to RAM status character 0
     * implements the assembler for the WR0 command
     */
    public static class WR0 extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0E, (byte)(0x04)};
        }
    }
    
    /**
     * Class WR1 - Write to RAM status character 0
     * implements the assembler for the WR1 command
     */
    public static class WR1 extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0E, (byte)(0x05)};
        }
    }
    
    /**
     * Class WR2 - Write to RAM status character 0
     * implements the assembler for the WR2 command
     */
    public static class WR2 extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0E, (byte)(0x06)};
        }
    }
    
    /**
     * Class WR3 - Write to RAM status character 0
     * implements the assembler for the WR3 command
     */
    public static class WR3 extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0E, (byte)(0x07)};
        }
    }
    
    /**
     * Class SBM - Subtract RAM from Accumulator with borrow
     * implements the assembler for the WR0 command
     */
    public static class SBM extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0E, (byte)(0x08)};
        }
    }
    
    /**
     * Class RDM - Read RAM to Accumulator
     * implements the assembler for the RDM command
     */
    public static class RDM extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0E, (byte)(0x09)};
        }
    }
    
    /**
     * Class RDR - Read ROM IO to Accumulator
     * implements the assembler for the RDR command
     */
    public static class RDR extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0E, (byte)(0x0A)};
        }
    }
    
    /**
     * Class ADM - Add RAM to Accumulator
     * implements the assembler for the ADM command
     */
    public static class ADM extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0E, (byte)(0x0B)};
        }
    }
    
    /**
     * Class RD0 - Read RAM status character 0 to Accumulator
     * implements the assembler for the RD0 command
     */
    public static class RD0 extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0E, (byte)(0x0C)};
        }
    }
    
    /**
     * Class RD1 - Read RAM status character 1 to Accumulator
     * implements the assembler for the RD1 command
     */
    public static class RD1 extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0E, (byte)(0x0D)};
        }
    }
    
    /**
     * Class RD2 - Read RAM status character 2 to Accumulator
     * implements the assembler for the RD2 command
     */
    public static class RD2 extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0E, (byte)(0x0E)};
        }
    }
    
    /**
     * Class RD3 - Read RAM status character 3 to Accumulator
     * implements the assembler for the RD3 command
     */
    public static class RD3 extends Intel4004Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0E, (byte)(0x0F)};
        }
    }
    
    /**
     * Creates a new instance of Intel4004Debugger with no RAM or ROM
     */
    public Intel4004Debugger() {
        super();
        this.registerNames.add("A");
        this.registerNames.add("C");
        for(int i=0; i<16; i++)
            this.registerNames.add("R"+i);
        for(int i=0; i<8; i++) 
            this.registerNames.add("P"+i);
        this.registerNames.add("PC");
        for(int i=0; i<3; i++)
            this.registerNames.add("Stack"+i);
        this.registerNames.add("DtRamBnk");
        this.registerNames.add("RegCtrl");
    }

    /**
     * Returns the code length at a given memory locatio
     * 
     * @param address (long) the memory location
     * @return the code length (long)
     * @throws MemoryException if there was a problem with the memory
     * @throws OpCodeException if the opcode was incorrect
     */
    @Override
    public long getCodeLength(long address) throws MemoryException, OpCodeException {
        return this.getCodeAndLength(address).getCodeLength();
    }

    /**
     * Returns the opcode mnemonic at a given memory locatio
     * 
     * @param address (long) the memory location
     * @return the mnemonic (String)
     * @throws MemoryException if there was a problem with the memory
     * @throws OpCodeException if the opcode was incorrect
     */
    @Override
    public String getCode(long address) throws MemoryException, OpCodeException {
        return this.getCodeAndLength(address).getCode();
    }

    /**
     * Returns the code length and opcode mnemonic for the operation at the
     * given memory address
     * 
     * @param address (long) the address
     * @return the code and length
     * @throws MemoryException if the memory address is incorrect
     * @throws OpCodeException if there is an error with the opcode
     */
    @Override
    public CodeAndLength getCodeAndLength(long address) throws MemoryException, OpCodeException {
        int opCode1 = Byte.toUnsignedInt(this.readMemory4(address, true));
        int opCode2 = Byte.toUnsignedInt(this.readMemory4(address+1, true));
        int opCode3 = Byte.toUnsignedInt(this.readMemory4(address+2, true));
        int opCode4 = Byte.toUnsignedInt(this.readMemory4(address+3, true));
        
        switch (opCode1) {
            case 0x00:  // NOP
                return new CodeAndLength(2, "NOP");
                
            case 0x01:  // JCN
                return new CodeAndLength(4, "JCN " + conditionNames[opCode2] + " $" + Integer.toHexString((opCode3<<4) | opCode4));
                
            case 0x02:  // FIM & SRC
                switch (opCode2 & 0x01) {
                    case 0x00:  // FIM
                        return new CodeAndLength(4, "FIM P" + (opCode2>>1) + ", $" + Integer.toHexString((opCode3<<4) | opCode4));

                    case 0x01:  // SRC
                        return new CodeAndLength(2, "SRC P" + (opCode2>>1));
                        
                    default:
                        throw new OpCodeException("OpCode " + opCode1 + "/" + opCode2 + " invalid");
                }
                
            case 0x03:  // FIN & JIN
                switch (opCode2 & 0x01) {
                    case 0x00:  // FIN
                        return new CodeAndLength(2, "FIN P" + (opCode2>>1));
                        
                    case 0x01:  // JIN
                        return new CodeAndLength(2, "JIN P" + (opCode2>>1));
                        
                    default:
                        throw new OpCodeException("OpCode " + opCode1 + "/" + opCode2 + " invalid");
                }
                
            case 0x04:  // JUN
                return new CodeAndLength(4, "JUN $" + Integer.toHexString((opCode2<<8) | (opCode3<<4) | opCode4));
                
            case 0x05:  // JMS
                return new CodeAndLength(4, "JMS $" + Integer.toHexString((opCode2<<8) | (opCode3<<4) | opCode4));
                
            case 0x06:  // INC
                return new CodeAndLength(2, "INC R" + opCode2);
                
            case 0x07:  // ISZ
                return new CodeAndLength(4, "ISZ R" + opCode2 + ", $" + Integer.toHexString((opCode3<<4) | opCode4));
                
            case 0x08:  // ADD
                return new CodeAndLength(2, "ADD R" + opCode2);
                
            case 0x09:  // SUB
                return new CodeAndLength(2, "SUB R" + opCode2);
                
            case 0x0A:  // LD
                return new CodeAndLength(2, "LD R" + opCode2);
                
            case 0x0B:  // XCHG
                return new CodeAndLength(2, "XCHG R" + opCode2);
                
            case 0x0C:  // BBL
                return new CodeAndLength(2, "BBL $" + Integer.toHexString(opCode2));
                
            case 0x0D:  // LDM
                return new CodeAndLength(2, "LDM $" + Integer.toHexString(opCode2));
                
            case 0x0E:  // Various ram/rom control commands
                switch (opCode2) {
                    case 0x00:  // WRM
                        return new CodeAndLength(2, "WRM");
                        
                    case 0x01:  // WMP
                        return new CodeAndLength(2, "WMP");
                        
                    case 0x02:  // WRR
                        return new CodeAndLength(2, "WRR");
                        
                    case 0x03:  // WPM
                        return new CodeAndLength(2, "WPM");
                        
                    case 0x04:  // WR0
                        return new CodeAndLength(2, "WR0");
                        
                    case 0x05:  // WR1
                        return new CodeAndLength(2, "WR1");
                        
                    case 0x06:  // WR2
                        return new CodeAndLength(2, "WR2");
                        
                    case 0x07:  // WR3
                        return new CodeAndLength(2, "WR3");
                        
                    case 0x08:  // SBM
                        return new CodeAndLength(2, "SBM");
                        
                    case 0x09:  // RDM
                        return new CodeAndLength(2, "RDM");
                        
                    case 0x0A:  // RDR
                        return new CodeAndLength(2, "RDR");
                        
                    case 0x0B:  // ADM
                        return new CodeAndLength(2, "ADM");
                        
                    case 0x0C:  // RD0
                        return new CodeAndLength(2, "RD0");
                        
                    case 0x0D:  // RD1
                        return new CodeAndLength(2, "RD1");
                        
                    case 0x0E:  // RD2
                        return new CodeAndLength(2, "RD2");
                        
                    case 0x0F:  // RD3
                        return new CodeAndLength(2, "RD3");
                        
                    default:
                        throw new OpCodeException("OpCode " + opCode1 + "/" + opCode2 + " invalid");
                }
                
            case 0x0F:  // Various accumulator commands
                switch (opCode2) {
                    case 0x00:  // CLB
                        return new CodeAndLength(2, "CLB");
                        
                    case 0x01:  // CLC
                        return new CodeAndLength(2, "CLC");
                        
                    case 0x02:  // IAC
                        return new CodeAndLength(2, "IAC");
                        
                    case 0x03:  // CMC
                        return new CodeAndLength(2, "CMC");
                        
                    case 0x04:  // CMA
                        return new CodeAndLength(2, "CMA");
                        
                    case 0x05:  // RAL
                        return new CodeAndLength(2, "RAL");
                        
                    case 0x06:  // RAR
                        return new CodeAndLength(2, "RAR");
                        
                    case 0x07:  // TCC
                        return new CodeAndLength(2, "TCC");
                        
                    case 0x08:  // DAC
                        return new CodeAndLength(2, "DAC");
                        
                    case 0x09:  // TCS
                        return new CodeAndLength(2, "TCS");
                        
                    case 0x0A:  // STC
                        return new CodeAndLength(2, "STC");
                        
                    case 0x0B:  // DAA
                        return new CodeAndLength(2, "DAA");
                        
                    case 0x0C:  // KBP
                        return new CodeAndLength(2, "KBP");
                        
                    case 0x0E:  // DCL
                        return new CodeAndLength(2, "DCL");
                        
                    default:
                        throw new OpCodeException("OpCode " + opCode1 + "/" + opCode2 + " invalid");
                }
                
            default:
                throw new OpCodeException("OpCode " + opCode1 + "/" + opCode2 + " invalid");
        }
    }

    /**
     * Returns the machine code for a given opcode Mnemonic
     * 
     * @param mnemonic (String) the opcode mnemonic
     * @return the machine code (byte[])
     * @throws SyntaxErrorException 
     */
    @Override
    public byte[] translate(String mnemonic) throws SyntaxErrorException {
        mnemonic = mnemonic.toUpperCase();
        StringTokenizer st = new StringTokenizer(mnemonic);
        
        String mnem1 = st.nextToken();
        
        try {
            String className = "org.ansic.micro.Intel4004Debugger$" + mnem1;
            Class theClass = Class.forName(className);
            Object theObj = theClass.newInstance();
            
            Z80Debugger.Mnemonic mnem = (Z80Debugger.Mnemonic)theObj;
            
            
            byte[] opCodes = mnem.getOpCodes(st);
            
            return opCodes;
        }
        catch (SyntaxErrorException sex) {
            throw sex;
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            Logger.getLogger(DebuggerGUI.class.getName()).log(Level.SEVERE, null, ex);
            throw new SyntaxErrorException("Syntax error in <" + mnemonic + ">");
        }
    }

    /**
     * Returns the list of all register names
     * 
     * @return the register names (List)
     */
    @Override
    public List<String> getRegisterNames() {
        return this.registerNames;
    }

    /**
     * Sets the value of a register identified by name
     * 
     * @param registerName (String) the register name
     * @param registerValue (long) the new value
     * @throws IllegalRegisterException if the register doesn't exist
     * @throws IllegalRegisterValueException if the value does not fit in the register
     */
    @Override
    public void setRegisterValue(String registerName, long registerValue) throws IllegalRegisterException, IllegalRegisterValueException {
        switch (registerName) {
            case "A":
                if((registerValue < 0) || (registerValue >= 16)) throw new IllegalRegisterValueException("Register A value must be within [0..15]");
                this.regA = (byte)registerValue;
                break;
                
            case "C":
                switch ((int)registerValue) {
                    case 0:
                        this.carry = false;
                        break;
                    case 1:
                        this.carry = true;
                        break;
                    default:
                        throw new IllegalRegisterValueException("Register C value must be within [0..1]");
                }
                break;
                
            case "PC":
                if((registerValue < 0) || (registerValue >= 4096)) throw new IllegalRegisterValueException("Register PC value must be within [0..4095]");
                this.regPC = (short)registerValue;
                break;
                
            case "DtRamBnk":
                if((registerValue < 0) || (registerValue >= 16)) throw new IllegalRegisterValueException("Register DtRamBnk value must be within [0..15]");;
                this.regDataRamBank = (byte)registerValue;
                break;
                
            case "RegCtrl":
                if((registerValue < 0) || (registerValue >= 256)) throw new IllegalRegisterValueException("Register RegCtrl value must be within [0..255]");
                this.regRegisterControl = (byte)registerValue;
                break;
                
            default:
                if(registerName.startsWith("Stack")) {
                    try {
                        int stackNum = Integer.parseInt(registerName.substring(5));
                        if((stackNum >= 0) && (stackNum <= 2)) {
                            if((registerValue < 0) || (registerValue >= 4096)) throw new IllegalRegisterValueException("Register " + registerName + " value must be within [0..4095]");
                            this.stacks[stackNum] = (short)registerValue;
                            return;
                        }
                    }
                    catch (NumberFormatException ex) {
                        // Don't do nothing here
                    }
                }
                else if(registerName.startsWith("R")) {
                    try {
                        int regNum = Integer.parseInt(registerName.substring(1));
                        if((regNum >= 0) && (regNum <= 15)) {
                            if((registerValue < 0) || (registerValue >= 16)) throw new IllegalRegisterValueException("Register " + registerName + " value must be within [0..15]");
                            this.regIndex[regNum] = (byte)registerValue;
                            return;
                        }
                    }
                    catch (NumberFormatException ex) {
                        // Don't do nothing here
                    }
                }
                else if(registerName.startsWith("P")) {
                    try {
                        int regNum = Integer.parseInt(registerName.substring(1));
                        if((regNum >= 0) && (regNum <= 7)) {
                            if((registerValue < 0) || (registerValue >= 256)) throw new IllegalRegisterValueException("Register " + registerName + " value must be within [0..255]");
                            this.regIndex[regNum * 2] = (byte)(registerValue >> 4);
                            this.regIndex[regNum * 2 + 1] = (byte)(registerValue & 0x0F);
                            return;
                        }
                    }
                    catch (NumberFormatException ex) {
                        // Don't do nothing here
                    }
                }
                throw new IllegalRegisterException("Register " + registerName + " does not exist");
        }
    }

    /**
     * Returns the value of a certain register identified by name
     * 
     * @param registerName (String) the register name
     * @return the content of the register (long)
     * @throws IllegalRegisterException if the register doesn't exist
     */
    @Override
    public long getRegisterValue(String registerName) throws IllegalRegisterException {
        switch (registerName) {
            case "A":
                return this.regA;
                
            case "C":
                return this.carry ? 1 : 0;
                
            case "PC":
                return this.regPC;
                
            case "DtRamBnk": 
                return this.regDataRamBank;
                
            case "RegCtrl":
                return this.regRegisterControl;
                
            default:
                if(registerName.startsWith("Stack")) {
                    try {
                        int stackNum = Integer.parseInt(registerName.substring(5));
                        if((stackNum >= 0) && (stackNum <= 2))
                            return this.stacks[stackNum];
                    }
                    catch (NumberFormatException ex) {
                        // Don't do nothing here
                    }
                }
                else if(registerName.startsWith("R")) {
                    try {
                        int regNum = Integer.parseInt(registerName.substring(1));
                        if((regNum >= 0) && (regNum <= 15))
                            return this.regIndex[regNum];
                    }
                    catch (NumberFormatException ex) {
                        // Don't do nothing here
                    }
                }
                else if(registerName.startsWith("P")) {
                    try {
                        int regNum = Integer.parseInt(registerName.substring(1));
                        if((regNum >= 0) && (regNum <= 7))
                            return (Byte.toUnsignedInt(this.regIndex[regNum*2]) << 4) | Byte.toUnsignedInt(this.regIndex[regNum*2 + 1]);
                    }
                    catch (NumberFormatException ex) {
                        // Don't do nothing here
                    }
                }
                throw new IllegalRegisterException("Register " + registerName + " does not exist");
        }
    }

    /**
     * Returns the program counter register
     * 
     * @return the program counter (long)
     */
    @Override
    public long getProgramCounter() {
        return this.regPC;
    }

    /**
     * Sets the program counter register
     * 
     * @param programCounter (long) the new pc
     * @throws IllegalRegisterValueException if the value is out of bounds
     */
    @Override
    public void setProgramCounter(long programCounter) throws IllegalRegisterValueException {
        if((programCounter < 0) || (programCounter >= 4096)) throw new IllegalRegisterValueException("Register PC value must be withing [0..4095]");
        this.regPC = (short)programCounter;
    }

    /**
     * Returns the register size in bits identified by name
     * 
     * @param registerName (String) the register name
     * @return the register size (int)
     * @throws IllegalRegisterException if the register does not exist 
     */
    @Override
    public int getRegisterSize(String registerName) throws IllegalRegisterException {
        switch (registerName) {
            case "A":
                return 4;
                
            case "C":
                return 1;
                
            case "PC":
                return 12;
                
            case "DtRamBnk": 
                return 4;
                
            case "RegCtrl":
                return 8;
                
            default:
                if(registerName.startsWith("Stack")) {
                    try {
                        int stackNum = Integer.parseInt(registerName.substring(5));
                        if((stackNum >= 0) && (stackNum <= 2))
                            return 12;
                    }
                    catch (NumberFormatException ex) {
                        // Don't do nothing here
                    }
                }
                else if(registerName.startsWith("R")) {
                    try {
                        int regNum = Integer.parseInt(registerName.substring(1));
                        if((regNum >= 0) && (regNum <= 15))
                            return 4;
                    }
                    catch (NumberFormatException ex) {
                        // Don't do nothing here
                    }
                }
                else if(registerName.startsWith("P")) {
                    try {
                        int regNum = Integer.parseInt(registerName.substring(1));
                        if((regNum >= 0) && (regNum <= 7))
                            return 8;
                    }
                    catch (NumberFormatException ex) {
                        // Don't do nothing here
                    }
                }
                throw new IllegalRegisterException("Register " + registerName + " does not exist");
        }
    }

    /**
     * Reads a byte (in this case 4-bit nibble) from the program memory at a 
     * given address
     * 
     * @param address (long) the address
     * @return the content at this address (byte)
     * @throws MemoryException 
     */
    @Override
    public byte readMemoryByte(long address) throws MemoryException {
        return this.readMemory4(address, true);
    }

    /**
     * Writes a byte (in this case 4-bit nibble) to the program memory at a
     * given address
     * 
     * @param address (long) the address
     * @param value the value to be stored (byte)
     * @throws MemoryException 
     */
    @Override
    public void writeMemoryByte(long address, byte value) throws MemoryException {
        this.writeMemory4(address, value, true);
    }
}
