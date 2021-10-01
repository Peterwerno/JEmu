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
 * This class implements a debugger for the Seiko UC 2000 CPU
 * 
 * @author peter
 */
public class SeikoUC2000Debugger extends SeikoUC2000 implements Debugger {
    public abstract static class UC2000Mnemonic extends Debugger.Mnemonic {
        public boolean isRegisterNumber(String param) {
            if(param.startsWith("R") || param.startsWith("r")) {
                return isNumeric(param.substring(1));
            }
            else
                return false;
        }
        
        public int getRegisterNumber(String param) throws SyntaxErrorException {
            if(param.startsWith("R") || param.startsWith("r")) {
                return getNumeric(param.substring(1));
            }
            else
                return -1;
        }
    }
    
    /**
     * Class ADD
     * implements the assembler for the ADD command
     */
    public static class ADD extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ADD command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ADD command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                
                return new byte[]{(byte)(reg1>>3), (byte)(((reg1 & 0x07)<<5) | (reg2))};
            }
            else
                throw new SyntaxErrorException("Syntax error in ADD command");
        }
    }
    
    /**
     * Class ADB
     * implements the assembler for the ADB command
     */
    public static class ADB extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ADB command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ADB command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                
                return new byte[]{(byte)(0x04 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2))};
            }
            else
                throw new SyntaxErrorException("Syntax error in ADB command");
        }
    }
    
    /**
     * Class SUB
     * implements the assembler for the SUB command
     */
    public static class SUB extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SUB command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SUB command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                
                return new byte[]{(byte)(0x08 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2))};
            }
            else
                throw new SyntaxErrorException("Syntax error in SUB command");
        }
    }
    
    /**
     * Class SBB
     * implements the assembler for the SBB command
     */
    public static class SBB extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SBB command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SBB command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                
                return new byte[]{(byte)(0x0C | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2))};
            }
            else
                throw new SyntaxErrorException("Syntax error in SBB command");
        }
    }
    
    /**
     * Class ADI
     * implements the assembler for the ADI command
     */
    public static class ADI extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ADI command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ADI command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isNumeric(param2)) {
                int reg1 = getRegisterNumber(param1);
                int value = getNumeric(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((value < 0) || (value > 15)) throw new SyntaxErrorException("Value must be within [0..15]");
                
                return new byte[]{(byte)(0x10 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (value << 1))};
            }
            else
                throw new SyntaxErrorException("Syntax error in ADI command");
        }
    }
    
    /**
     * Class ADBI
     * implements the assembler for the ADBI command
     */
    public static class ADBI extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ADBI command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ADBI command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isNumeric(param2)) {
                int reg1 = getRegisterNumber(param1);
                int value = getNumeric(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((value < 0) || (value > 15)) throw new SyntaxErrorException("Value must be within [0..15]");
                
                return new byte[]{(byte)(0x14 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (value << 1))};
            }
            else
                throw new SyntaxErrorException("Syntax error in ADBI command");
        }
    }
    
    /**
     * Class SBI
     * implements the assembler for the SBI command
     */
    public static class SBI extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SBI command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SBI command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isNumeric(param2)) {
                int reg1 = getRegisterNumber(param1);
                int value = getNumeric(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((value < 0) || (value > 15)) throw new SyntaxErrorException("Value must be within [0..15]");
                
                return new byte[]{(byte)(0x18 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (value << 1))};
            }
            else
                throw new SyntaxErrorException("Syntax error in SBI command");
        }
    }
    
    /**
     * Class SBBI
     * implements the assembler for the SBBI command
     */
    public static class SBBI extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SBBI command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SBBI command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isNumeric(param2)) {
                int reg1 = getRegisterNumber(param1);
                int value = getNumeric(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((value < 0) || (value > 15)) throw new SyntaxErrorException("Value must be within [0..15]");
                
                return new byte[]{(byte)(0x1C | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (value << 1))};
            }
            else
                throw new SyntaxErrorException("Syntax error in SBBI command");
        }
    }
    
    /**
     * Class ADM
     * implements the assembler for the ADM command
     */
    public static class ADM extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ADM command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ADM command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                
                return new byte[]{(byte)(0x20 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2))};
            }
            else
                throw new SyntaxErrorException("Syntax error in ADM command");
        }
    }
    
    /**
     * Class ADBM
     * implements the assembler for the ADBM command
     */
    public static class ADBM extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ADBM command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ADBM command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                
                return new byte[]{(byte)(0x24 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2))};
            }
            else
                throw new SyntaxErrorException("Syntax error in ADBM command");
        }
    }
    
    /**
     * Class SBM
     * implements the assembler for the SBM command
     */
    public static class SBM extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SBM command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SBM command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                
                return new byte[]{(byte)(0x28 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2))};
            }
            else
                throw new SyntaxErrorException("Syntax error in SBM command");
        }
    }
    
    /**
     * Class SBBM
     * implements the assembler for the SBBM command
     */
    public static class SBBM extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SBBM command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SBBM command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                
                return new byte[]{(byte)(0x2C | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2))};
            }
            else
                throw new SyntaxErrorException("Syntax error in SBBM command");
        }
    }
    
    /**
     * Class CMP
     * implements the assembler for the CMP command
     */
    public static class CMP extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CMP command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CMP command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                
                return new byte[]{(byte)(0x30 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2))};
            }
            else
                throw new SyntaxErrorException("Syntax error in CMP command");
        }
    }
    
    /**
     * Class CPM
     * implements the assembler for the CPM command
     */
    public static class CPM extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CPM command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CPM command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                
                return new byte[]{(byte)(0x34 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2))};
            }
            else
                throw new SyntaxErrorException("Syntax error in CPM command");
        }
    }
    
    /**
     * Class CPI
     * implements the assembler for the CPI command
     */
    public static class CPI extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CPI command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CPI command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isNumeric(param2)) {
                int reg1 = getRegisterNumber(param1);
                int value = getNumeric(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((value < 0) || (value > 15)) throw new SyntaxErrorException("Value must be within [0..15]");
                
                return new byte[]{(byte)(0x38 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (value << 1))};
            }
            else
                throw new SyntaxErrorException("Syntax error in CPI command");
        }
    }
    
    /**
     * Class LCRB
     * implements the assembler for the LCRB command
     */
    public static class LCRB extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("LCRB command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            if(isNumeric(param1)) {
                int value = getNumeric(param1);
                
                if((value < 0) || (value > 3)) throw new SyntaxErrorException("Bank Number must be within [0..3]");
                
                return new byte[]{(byte)(0x3C), (byte)(value << 3)};
            }
            else
                throw new SyntaxErrorException("Syntax error in LCRB command");
        }
    }
    
    /**
     * Class LARB
     * implements the assembler for the LARB command
     */
    public static class LARB extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("LARB command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            if(isNumeric(param1)) {
                int value = getNumeric(param1);
                
                if((value < 0) || (value > 3)) throw new SyntaxErrorException("Bank Number must be within [0..3]");
                
                return new byte[]{(byte)(0x3E), (byte)(value << 3)};
            }
            else
                throw new SyntaxErrorException("Syntax error in LARB command");
        }
    }
    
    /**
     * Class ANDI
     * implements the assembler for the ANDI command
     */
    public static class ANDI extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ANDI command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ANDI command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isNumeric(param2)) {
                int reg1 = getRegisterNumber(param1);
                int value = getNumeric(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((value < 0) || (value > 15)) throw new SyntaxErrorException("Value must be within [0..15]");
                
                return new byte[]{(byte)(0x40 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (value << 1))};
            }
            else
                throw new SyntaxErrorException("Syntax error in ANDI command");
        }
    }
    
    /**
     * Class ORI
     * implements the assembler for the ORI command
     */
    public static class ORI extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ORI command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ORI command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isNumeric(param2)) {
                int reg1 = getRegisterNumber(param1);
                int value = getNumeric(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((value < 0) || (value > 15)) throw new SyntaxErrorException("Value must be within [0..15]");
                
                return new byte[]{(byte)(0x44 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (value << 1))};
            }
            else
                throw new SyntaxErrorException("Syntax error in ORI command");
        }
    }
    
    /**
     * Class XORI
     * implements the assembler for the XORI command
     */
    public static class XORI extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("XORI command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("XORI command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isNumeric(param2)) {
                int reg1 = getRegisterNumber(param1);
                int value = getNumeric(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((value < 0) || (value > 15)) throw new SyntaxErrorException("Value must be within [0..15]");
                
                return new byte[]{(byte)(0x48 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (value << 1))};
            }
            else
                throw new SyntaxErrorException("Syntax error in XORI command");
        }
    }
    
    /**
     * Class INC
     * implements the assembler for the INC command
     */
    public static class INC extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("INC command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("INC command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg1 & 0x18) != (reg2 & 0x18)) throw new SyntaxErrorException("Registers must be from the same bank-tuple");
                
                return new byte[]{(byte)(0x4C | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2 & 0x07))};
            }
            else
                throw new SyntaxErrorException("Syntax error in INC command");
        }
    }
    
    /**
     * Class INCB
     * implements the assembler for the INCB command
     */
    public static class INCB extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("INCB command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("INCB command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg1 & 0x18) != (reg2 & 0x18)) throw new SyntaxErrorException("Registers must be from the same bank-tuple");
                
                return new byte[]{(byte)(0x4C | (reg1>>3)), (byte)(0x08 | ((reg1 & 0x07)<<5) | (reg2 & 0x07))};
            }
            else
                throw new SyntaxErrorException("Syntax error in INCB command");
        }
    }
    
    /**
     * Class DEC
     * implements the assembler for the DEC command
     */
    public static class DEC extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("DEC command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("DEC command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg1 & 0x18) != (reg2 & 0x18)) throw new SyntaxErrorException("Registers must be from the same bank-tuple");
                
                return new byte[]{(byte)(0x4C | (reg1>>3)), (byte)(0x10 | ((reg1 & 0x07)<<5) | (reg2 & 0x07))};
            }
            else
                throw new SyntaxErrorException("Syntax error in DEC command");
        }
    }
    
    /**
     * Class DECB
     * implements the assembler for the DECB command
     */
    public static class DECB extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("DECB command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("DECB command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg1 & 0x18) != (reg2 & 0x18)) throw new SyntaxErrorException("Registers must be from the same bank-tuple");
                
                return new byte[]{(byte)(0x4C | (reg1>>3)), (byte)(0x18 | ((reg1 & 0x07)<<5) | (reg2 & 0x07))};
            }
            else
                throw new SyntaxErrorException("Syntax error in DECB command");
        }
    }
    
    /**
     * Class RSHM
     * implements the assembler for the RSHM command
     */
    public static class RSHM extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("RSHM command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("RSHM command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg1 & 0x18) != (reg2 & 0x18)) throw new SyntaxErrorException("Registers must be from the same bank-tuple");
                
                return new byte[]{(byte)(0x50 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2 & 0x07))};
            }
            else
                throw new SyntaxErrorException("Syntax error in RSHM command");
        }
    }
    
    /**
     * Class LSHM
     * implements the assembler for the LSHM command
     */
    public static class LSHM extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("LSHM command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("LSHM command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg1 & 0x18) != (reg2 & 0x18)) throw new SyntaxErrorException("Registers must be from the same bank-tuple");
                
                return new byte[]{(byte)(0x50 | (reg1>>3)), (byte)(0x08 | ((reg1 & 0x07)<<5) | (reg2 & 0x07))};
            }
            else
                throw new SyntaxErrorException("Syntax error in LSHM command");
        }
    }
    
    /**
     * Class IN
     * implements the assembler for the IN command
     */
    public static class IN extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("IN command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("IN command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isNumeric(param2)) {
                int reg1 = getRegisterNumber(param1);
                int value = getNumeric(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((value < 0) || (value > 15)) throw new SyntaxErrorException("Port Number must be within [0..15]");
                
                return new byte[]{(byte)(0x54 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (value))};
            }
            else
                throw new SyntaxErrorException("Syntax error in IN command");
        }
    }
    
    /**
     * Class OUT
     * implements the assembler for the OUT command
     */
    public static class OUT extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("OUT command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("OUT command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param2) && isNumeric(param1)) {
                int value = getNumeric(param1);
                int reg1 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((value < 0) || (value > 15)) throw new SyntaxErrorException("Port Number must be within [0..15]");
                
                return new byte[]{(byte)(0x58 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (value))};
            }
            else
                throw new SyntaxErrorException("Syntax error in OUT command");
        }
    }
    
    /**
     * Class OUTI
     * implements the assembler for the OUTI command
     */
    public static class OUTI extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("OUTI command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("OUTI command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isNumeric(param1) && isNumeric(param2)) {
                int port = getNumeric(param1);
                int value = getNumeric(param2);
                
                if((port < 0) || (port > 15)) throw new SyntaxErrorException("Port Number must be within [0..15]");
                if((value < 0) || (value > 15)) throw new SyntaxErrorException("Value must be within [0..15]");
                
                return new byte[]{(byte)(0x5C | (value>>2)), (byte)(((value & 0x03)<<6) | (port))};
            }
            else
                throw new SyntaxErrorException("Syntax error in OUTI command");
        }
    }
    
    /**
     * Class PSAM
     * implements the assembler for the PSAM command
     */
    public static class PSAM extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("PSAM command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("PSAM command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg1 & 0x18) != (reg2 & 0x18)) throw new SyntaxErrorException("Registers must be from the same bank-tuple");
                
                return new byte[]{(byte)(0x60 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2 & 0x07))};
            }
            else
                throw new SyntaxErrorException("Syntax error in PSAM command");
        }
    }
    
    /**
     * Class PLAM
     * implements the assembler for the PLAM command
     */
    public static class PLAM extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("PLAM command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("PLAM command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg1 & 0x18) != (reg2 & 0x18)) throw new SyntaxErrorException("Registers must be from the same bank-tuple");
                
                return new byte[]{(byte)(0x60 | (reg1>>3)), (byte)(0x10 | ((reg1 & 0x07)<<5) | (reg2 & 0x07))};
            }
            else
                throw new SyntaxErrorException("Syntax error in PLAM command");
        }
    }
    
    /**
     * Class LDSM
     * implements the assembler for the LDSM command
     */
    public static class LDSM extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("LDSM command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("LDSM command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg1 & 0x18) != (reg2 & 0x18)) throw new SyntaxErrorException("Registers must be from the same bank-tuple");
                
                return new byte[]{(byte)(0x64 | (reg1>>3)), (byte)(0x08 | ((reg1 & 0x07)<<5) | (reg2 & 0x07))};
            }
            else
                throw new SyntaxErrorException("Syntax error in LDSM command");
        }
    }
    
    /**
     * Class STSM
     * implements the assembler for the STSM command
     */
    public static class STSM extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("STSM command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("STSM command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg1 & 0x18) != (reg2 & 0x18)) throw new SyntaxErrorException("Registers must be from the same bank-tuple");
                
                return new byte[]{(byte)(0x64 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2 & 0x07))};
            }
            else
                throw new SyntaxErrorException("Syntax error in STSM command");
        }
    }
    
    /**
     * Class STLM
     * implements the assembler for the STLM command
     */
    public static class STLM extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("STLM command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("STLM command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg1 & 0x18) != (reg2 & 0x18)) throw new SyntaxErrorException("Registers must be from the same bank-tuple");
                
                return new byte[]{(byte)(0x68 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2 & 0x07))};
            }
            else
                throw new SyntaxErrorException("Syntax error in STLM command");
        }
    }
    
    /**
     * Class STL
     * implements the assembler for the STL command
     */
    public static class STL extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("STL command requires 1 parameter1");
            String param1 = parameters.nextToken(" ,");
            
            if(isRegisterNumber(param1)) {
                int reg1 = getRegisterNumber(param1);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                
                return new byte[]{(byte)(0x6C | (reg1>>3)), (byte)((reg1 & 0x07)<<5)};
            }
            else
                throw new SyntaxErrorException("Syntax error in STL command");
        }
    }
    
    /**
     * Class PSAI
     * implements the assembler for the PSAI command
     */
    public static class PSAI extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("PSAI command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            if(isNumeric(param1)) {
                int value = getNumeric(param1);
                
                if((value < 0) || (value > 2047)) throw new SyntaxErrorException("Address must be within [0..2047]");
                
                return new byte[]{(byte)(0x70 | (value>>8)), (byte)(value & 0xFF)};
            }
            else
                throw new SyntaxErrorException("Syntax error in PSAI command");
        }
    }
    
    /**
     * Class PLAI
     * implements the assembler for the PLAI command
     */
    public static class PLAI extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("PLAI command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            if(isNumeric(param1)) {
                int value = getNumeric(param1);
                
                if((value < 0) || (value > 255)) throw new SyntaxErrorException("Value must be within [0..255]");
                
                return new byte[]{(byte)(0x78 | (value>>6)), (byte)(((value & 0x38)<<5) | (value & 0x07))};
            }
            else
                throw new SyntaxErrorException("Syntax error in PLAI command");
        }
    }
    
    /**
     * Class STLI
     * implements the assembler for the STLI command
     */
    public static class STLI extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("STLI command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            if(isNumeric(param1)) {
                int value = getNumeric(param1);
                
                if((value < 0) || (value > 255)) throw new SyntaxErrorException("Value must be within [0..255]");
                
                return new byte[]{(byte)(0x7C | (value>>6)), (byte)(0x10 | ((value & 0x38)<<5) | (value & 0x07))};
            }
            else
                throw new SyntaxErrorException("Syntax error in STLI command");
        }
    }
    
    /**
     * Class STLS
     * implements the assembler for the STLS command
     */
    public static class STLS extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x7C, (byte)0x00};
        }
    }
    
    /**
     * Class STLALI
     * implements the assembler for the STLALI command
     */
    public static class STLALI extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("STLALI command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            if(isNumeric(param1)) {
                int value = getNumeric(param1);
                
                if((value < 0) || (value > 255)) throw new SyntaxErrorException("Value must be within [0..255]");
                
                return new byte[]{(byte)(0x7C | (value>>6)), (byte)(0x18 | ((value & 0x38)<<5) | (value & 0x07))};
            }
            else
                throw new SyntaxErrorException("Syntax error in STLALI command");
        }
    }
    
    /**
     * Class MOV
     * implements the assembler for the MOV command
     */
    public static class MOV extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("MOV command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("MOV command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                
                return new byte[]{(byte)(0x80 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2))};
            }
            else
                throw new SyntaxErrorException("Syntax error in MOV command");
        }
    }
    
    /**
     * Class MOVM
     * implements the assembler for the MOVM command
     */
    public static class MOVM extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("MOVM command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("MOVM command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                
                return new byte[]{(byte)(0x84 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2))};
            }
            else
                throw new SyntaxErrorException("Syntax error in MOVM command");
        }
    }
    
    /**
     * Class LDI
     * implements the assembler for the LDI command
     */
    public static class LDI extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("LDI command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("LDI command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isNumeric(param2)) {
                int reg1 = getRegisterNumber(param1);
                int value = getNumeric(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((value < 0) || (value > 15)) throw new SyntaxErrorException("Value must be within [0..15]");
                
                return new byte[]{(byte)(0x88 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (value<<1))};
            }
            else
                throw new SyntaxErrorException("Syntax error in LDI command");
        }
    }
    
    /**
     * Class CLRM
     * implements the assembler for the CLRM command
     */
    public static class CLRM extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CLRM command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CLRM command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg1 & 0x18) != (reg2 & 0x18)) throw new SyntaxErrorException("Registers must be from the same bank-tuple");
                
                return new byte[]{(byte)(0x8C | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2 & 0x07))};
            }
            else
                throw new SyntaxErrorException("Syntax error in CLRM command");
        }
    }
    
    /**
     * Class MVAC
     * implements the assembler for the MVAC command
     */
    public static class MVAC extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("MVAC command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("MVAC command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                
                return new byte[]{(byte)(0x90 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2))};
            }
            else
                throw new SyntaxErrorException("Syntax error in MVAC command");
        }
    }
    
    /**
     * Class MVACM
     * implements the assembler for the MVACM command
     */
    public static class MVACM extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("MVACM command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("MVACM command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                
                return new byte[]{(byte)(0x94 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2))};
            }
            else
                throw new SyntaxErrorException("Syntax error in MVACM command");
        }
    }
    
    /**
     * Class MVCA
     * implements the assembler for the MVCA command
     */
    public static class MVCA extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("MVCA command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("MVCA command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                
                return new byte[]{(byte)(0x98 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2))};
            }
            else
                throw new SyntaxErrorException("Syntax error in MVCA command");
        }
    }
    
    /**
     * Class MVCAM
     * implements the assembler for the MVCAM command
     */
    public static class MVCAM extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("MVCAM command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("MVCAM command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isRegisterNumber(param2)) {
                int reg1 = getRegisterNumber(param1);
                int reg2 = getRegisterNumber(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((reg2 < 0) || (reg2 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                
                return new byte[]{(byte)(0x9C | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (reg2))};
            }
            else
                throw new SyntaxErrorException("Syntax error in MVCAM command");
        }
    }
    
    /**
     * Class CALL
     * implements the assembler for the CALL command
     */
    public static class CALL extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CALL command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            if(isNumeric(param1)) {
                int value = getNumeric(param1);
                
                if((value < 0) || (value > 6143)) throw new SyntaxErrorException("Value must be within [0..6143]");
                value >>= 1;
                
                return new byte[]{(byte)(0xA0 | (value>>8)), (byte)(value & 0xFF)};
            }
            else
                throw new SyntaxErrorException("Syntax error in CALL command");
        }
    }
    
    /**
     * Class RET
     * implements the assembler for the RET command
     */
    public static class RET extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0xB0, (byte)0x00};
        }
    }
    
    /**
     * Class CPFJR
     * implements the assembler for the CPFJI command
     */
    public static class CPFJR extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CPFJR command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CPFJR command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            if(isRegisterNumber(param1) && isNumeric(param2)) {
                int reg1 = getRegisterNumber(param1);
                int value = getNumeric(param2);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((value < 0) || (value > 31)) throw new SyntaxErrorException("Offset must be within [0..31]");
                
                return new byte[]{(byte)(0xB4 | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (value))};
            }
            else
                throw new SyntaxErrorException("Syntax error in CPFJR command");
        }
    }
    
    /**
     * Class IJMR
     * implements the assembler for the IJMR command
     */
    public static class IJMR extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("IJMR command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            if(isRegisterNumber(param1)) {
                int reg1 = getRegisterNumber(param1);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                
                return new byte[]{(byte)(0xB8 | (reg1>>3)), (byte)((reg1 & 0x07)<<5)};
            }
            else
                throw new SyntaxErrorException("Syntax error in IJMR command");
        }
    }
    
    /**
     * Class WFI
     * implements the assembler for the WFI command
     */
    public static class WFI extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0xBC, (byte)0x00};
        }
    }
    
    /**
     * Class JMP
     * implements the assembler for the JMP command
     */
    public static class JMP extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JMP command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            if(isNumeric(param1)) {
                int value = getNumeric(param1);
                
                if((value < 0) || (value > 6143)) throw new SyntaxErrorException("Value must be within [0..6143]");
                value >>= 1;
                
                return new byte[]{(byte)(0xC0 | (value>>8)), (byte)(value & 0xFF)};
            }
            else
                throw new SyntaxErrorException("Syntax error in JMP command");
        }
    }
    
    /**
     * Class JZ
     * implements the assembler for the JZ command
     */
    public static class JZ extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JZ command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            if(isNumeric(param1)) {
                int value = getNumeric(param1);
                
                if((value < 0x1800) || (value > 6143)) throw new SyntaxErrorException("Value must be within [4096..6143]");
                value >>= 1;
                value -= 0xC00;
                
                return new byte[]{(byte)(0xD0 | (value>>8)), (byte)(value & 0xFF)};
            }
            else
                throw new SyntaxErrorException("Syntax error in JZ command");
        }
    }
    
    /**
     * Class JNZ
     * implements the assembler for the JNZ command
     */
    public static class JNZ extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JNZ command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            if(isNumeric(param1)) {
                int value = getNumeric(param1);
                
                if((value < 0x1800) || (value > 6143)) throw new SyntaxErrorException("Value must be within [4096..6143]");
                value >>= 1;
                value -= 0xC00;
                
                return new byte[]{(byte)(0xD4 | (value>>8)), (byte)(value & 0xFF)};
            }
            else
                throw new SyntaxErrorException("Syntax error in JNZ command");
        }
    }
    
    /**
     * Class JC
     * implements the assembler for the JC command
     */
    public static class JC extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JC command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            if(isNumeric(param1)) {
                int value = getNumeric(param1);
                
                if((value < 0x1800) || (value > 6143)) throw new SyntaxErrorException("Value must be within [4096..6143]");
                value >>= 1;
                value -= 0xC00;
                
                return new byte[]{(byte)(0xD8 | (value>>8)), (byte)(value & 0xFF)};
            }
            else
                throw new SyntaxErrorException("Syntax error in JC command");
        }
    }
    
    /**
     * Class JNC
     * implements the assembler for the JNC command
     */
    public static class JNC extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JNC command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            if(isNumeric(param1)) {
                int value = getNumeric(param1);
                
                if((value < 0x1800) || (value > 6143)) throw new SyntaxErrorException("Value must be within [4096..6143]");
                value >>= 1;
                value -= 0xC00;
                
                return new byte[]{(byte)(0xDC | (value>>8)), (byte)(value & 0xFF)};
            }
            else
                throw new SyntaxErrorException("Syntax error in JNC command");
        }
    }
    
    /**
     * Class BTJR
     * implements the assembler for the BTJR command
     */
    public static class BTJR extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("BTJR command requires 3 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("BTJR command requires 3 parameters");
            String param2 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("BTJR command requires 3 parameters");
            String param3 = parameters.nextToken(" ,");
            
            if(isRegisterNumber(param1) && isNumeric(param2) && isNumeric(param3)) {
                int reg1 = getRegisterNumber(param1);
                int value = getNumeric(param2);
                int offset = getNumeric(param3);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((value < 0) || (value > 3)) throw new SyntaxErrorException("Bit number must be within [0..3]");
                if((offset < 0) || (offset > 31)) throw new SyntaxErrorException("Offset must be within [0..31]");
                
                return new byte[]{(byte)(0xE0 | (value<<4) | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (offset))};
            }
            else
                throw new SyntaxErrorException("Syntax error in BTJR command");
        }
    }
    
    /**
     * Class CPJR
     * implements the assembler for the CPJR command
     */
    public static class CPJR extends UC2000Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CPJR command requires 3 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CPJR command requires 3 parameters");
            String param2 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CPJR command requires 3 parameters");
            String param3 = parameters.nextToken(" ,");
            
            if(isRegisterNumber(param1) && isNumeric(param2) && isNumeric(param3)) {
                int reg1 = getRegisterNumber(param1);
                int value = getNumeric(param2);
                int offset = getNumeric(param3);
                
                if((reg1 < 0) || (reg1 > 31)) throw new SyntaxErrorException("Register Number must be within [0..31]");
                if((value < 0) || (value > 3)) throw new SyntaxErrorException("Bit number must be within [0..3]");
                if((offset < 0) || (offset > 31)) throw new SyntaxErrorException("Offset must be within [0..31]");
                
                return new byte[]{(byte)(0xF0 | (value<<4) | (reg1>>3)), (byte)(((reg1 & 0x07)<<5) | (offset))};
            }
            else
                throw new SyntaxErrorException("Syntax error in CPJR command");
        }
    }
    
    public SeikoUC2000Debugger() {
        super();
    }
    
    public SeikoUC2000Debugger(Memory memoryBlock, IO IOBlock) {
        super(memoryBlock, IOBlock);
    }
    
    public SeikoUC2000Debugger(List<Memory> memoryBlocks, List<IO> IOBlocks) {
        super(memoryBlocks, IOBlocks);
    }

    @Override
    public long getCodeLength(long address) throws MemoryException, OpCodeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCode(long address) throws MemoryException, OpCodeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CodeAndLength getCodeAndLength(long address) throws MemoryException, OpCodeException {
        int opCode = (Byte.toUnsignedInt(this.readMemory8(address)) << 8) + (Byte.toUnsignedInt(this.readMemory8(address + 1)));
        int k;
        int d = ((opCode & 0x03E0) >> 5);
        int s = ((opCode & 0x001F));
        
        switch (opCode & 0xFC00) {
            case 0x0000:    // ADD Rd, Rs
                return new CodeAndLength(2, "ADD R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x0400:    // ADB Rd, Rs
                return new CodeAndLength(2, "ADB R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x0800:    // SUB Rd, Rs
                return new CodeAndLength(2, "SUB R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x0C00:    // SBB Rd, Rs
                return new CodeAndLength(2, "SBB R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x1000:    // ADI R, I
                return new CodeAndLength(2, "ADI R" + Integer.toString(d) + ", $" + Integer.toHexString(s >> 1));
                
            case 0x1400:    // ADBI R, I
                return new CodeAndLength(2, "ADBI R" + Integer.toString(d) + ", $" + Integer.toHexString(s >> 1));
                
            case 0x1800:    // SBI R, I
                return new CodeAndLength(2, "SBI R" + Integer.toString(d) + ", $" + Integer.toHexString(s >> 1));
                
            case 0x1C00:    // SBBI R, I
                return new CodeAndLength(2, "SBBI R" + Integer.toString(d) + ", $" + Integer.toHexString(s >> 1));
                
            case 0x2000:    // ADM Rd-Rd*, Rs-Rs*
                k = (s - d) % 8;
                if(k != 0) {
                    return new CodeAndLength(2, "ADM R" + Integer.toString(d) + "-R" + Integer.toString(d+k) + ", R" + Integer.toString(s-k) + "-R" + Integer.toString(s));
                }
                else
                    return new CodeAndLength(2, "ADM R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x2400:    // ADBM Rd-Rd*, Rs-Rs*
                k = (s - d) % 8;
                if(k != 0) {
                    return new CodeAndLength(2, "ADBM R" + Integer.toString(d) + "-R" + Integer.toString(d+k) + ", R" + Integer.toString(s-k) + "-R" + Integer.toString(s));
                }
                else
                    return new CodeAndLength(2, "ADBM R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x2800:    // SBM Rd-Rd*, Rs-Rs*
                k = (s - d) % 8;
                if(k != 0) {
                    return new CodeAndLength(2, "SBM R" + Integer.toString(d) + "-R" + Integer.toString(d+k) + ", R" + Integer.toString(s-k) + "-R" + Integer.toString(s));
                }
                else
                    return new CodeAndLength(2, "SBM R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x2C00:    // SBBM Rd-Rd*, Rs-Rs*
                k = (s - d) % 8;
                if(k != 0) {
                    return new CodeAndLength(2, "SBBM R" + Integer.toString(d) + "-R" + Integer.toString(d+k) + ", R" + Integer.toString(s-k) + "-R" + Integer.toString(s));
                }
                else
                    return new CodeAndLength(2, "SBBM R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x3000:    // CMP Rd, Rs
                return new CodeAndLength(2, "CMP R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x3400:    // CMPM Rd-Rd*, Rs-Rs*
                k = (s - d) % 8;
                if(k != 0) {
                    return new CodeAndLength(2, "CMPM R" + Integer.toString(d) + "-R" + Integer.toString(d+k) + ", R" + Integer.toString(s-k) + "-R" + Integer.toString(s));
                }
                else
                    return new CodeAndLength(2, "CMPM R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x3800:    // CMPI Rd, I
                return new CodeAndLength(2, "CMPI R" + Integer.toString(d) + ", $" + Integer.toHexString(s >> 1));
                
            case 0x3C00:    // LCRB b, LARB b (load current bank, load additional bank)
                if((opCode & 0xFE00) == 0x3C00) {
                    return new CodeAndLength(2, "LCRB " + Integer.toString((opCode & 0x0018) >>3));
                }
                else if((opCode & 0xFE00) == 0x3E00) {
                    return new CodeAndLength(2, "LARB " + Integer.toString((opCode & 0x0018) >>3));
                }
                else
                    throw new OpCodeException("OpCode " + opCode + " not supported");
    
            case 0x4000:    // ANDI Rd, I
                return new CodeAndLength(2, "ANDI R" + Integer.toString(d) + ", $" + Integer.toHexString(s >> 1));
                
            case 0x4400:    // ORI Rd, I
                return new CodeAndLength(2, "ORI R" + Integer.toString(d) + ", $" + Integer.toHexString(s >> 1));
                
            case 0x4800:    // XORI Rd, I
                return new CodeAndLength(2, "XORI R" + Integer.toString(d) + ", $" + Integer.toHexString(s >> 1));
                
            case 0x4C00:    // INC, INCB, DEC, DECB R, r
                switch (opCode & 0x0018) {
                    case 0x0000:
                        return new CodeAndLength(2, "INC R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                        
                    case 0x0008:
                        return new CodeAndLength(2, "INCB R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                        
                    case 0x0010:
                        return new CodeAndLength(2, "DEC R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                        
                    case 0x0018:
                        return new CodeAndLength(2, "DECB R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                        
                    default:
                        throw new OpCodeException("OpCode " + opCode + " not supported");
                }
                
            case 0x5000:    // RSHM, LSHM
                switch (opCode & 0x0008) {
                    case 0x0000:
                        return new CodeAndLength(2, "RSHM R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                        
                    case 0x0008:
                        return new CodeAndLength(2, "LSHM R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                        
                    default:
                        throw new OpCodeException("OpCode " + opCode + " not supported");
                }
                
            case 0x5400:    // IN R, S
                return new CodeAndLength(2, "IN R" + Integer.toString(d) + ", ($" + Integer.toHexString(s & 0x000F) + ")");
                
                // 0x5800 + 0x5C00 not identified yet
                
            case 0x6000:    // PSAM, PLAM
                switch (opCode & 0x0010) {
                    case 0x0000:
                        return new CodeAndLength(2, "PSAM R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                        
                    case 0x0010:
                        return new CodeAndLength(2, "PLAM R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                        
                    default:
                        throw new OpCodeException("OpCode " + opCode + " not supported");
                }
                
            case 0x6400:    // LDSM, STSM
                switch (opCode & 0x0008) {
                    case 0x0000:
                        return new CodeAndLength(2, "STSM R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                        
                    case 0x0008:
                        return new CodeAndLength(2, "LDSM R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                        
                    default:
                        throw new OpCodeException("OpCode " + opCode + " not supported");
                }
                
            case 0x6800:    // STLM R, r
                return new CodeAndLength(2, "STLM R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                
            case 0x6C00:    // STL R
                return new CodeAndLength(2, "STL R" + Integer.toString(d));
                
            case 0x7000:    // PSAI I
            case 0x7400:
                return new CodeAndLength(2, "PSAI $" + Integer.toHexString((opCode & 0x07FF)));
                
            case 0x7800:    // PLAI I
                return new CodeAndLength(2, "PLAI $" + Integer.toHexString((opCode & 0x03C0) >> 6) + Integer.toHexString(((opCode & 0x0020) >> 2) | (opCode & 0x0007)));
                
            case 0x7C00:    // STLI I
                // TODO: Some varints are not yet documented here!!
                switch (opCode & 0x0018) {
                    case 0x0000:    // STLS
                        return new CodeAndLength(2, "STLS");
                        
                    case 0x0010:    // STLI
                        return new CodeAndLength(2, "STLI $" + Integer.toHexString((opCode & 0x03C0) >> 6) + Integer.toHexString(((opCode & 0x0020) >> 2) | (opCode & 0x0007)));

                    case 0x0018:    // STLALI                        
                        return new CodeAndLength(2, "STLALI $" + Integer.toHexString((opCode & 0x03C0) >> 6) + Integer.toHexString(((opCode & 0x0020) >> 2) | (opCode & 0x0007)));
                        
                    default:
                        throw new OpCodeException("Illegal opcode " + opCode);
                }
                
            case 0x8000:    // MOV Rd, Rs
                return new CodeAndLength(2, "MOV R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x8400:    // MOVM Rd-Rd*, Rs-Rs*
                k = (s - d) % 8;
                if(k != 0) {
                    return new CodeAndLength(2, "MOVM R" + Integer.toString(d) + "-R" + Integer.toString(d+k) + ", R" + Integer.toString(s-k) + "-R" + Integer.toString(s));
                }
                else
                    return new CodeAndLength(2, "MOVM R" + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x8800:    // LDI R, I
                return new CodeAndLength(2, "LDI R" + Integer.toString(d) + ", $" + Integer.toHexString(s >> 1));
                
            case 0x8C00:    // CLRM R, r
                return new CodeAndLength(2, "CLRM R" + Integer.toString(d) + ", $" + Integer.toHexString(s & 0x0007));
                
            case 0x9000:    // MVAC E, R
                return new CodeAndLength(2, "MVAC " + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x9400:    // MVACM E, R
                k = (s - d) % 8;
                if(k != 0) {
                    return new CodeAndLength(2, "MVACM " + Integer.toString(d) + "-" + Integer.toString(d+k) + ", R" + Integer.toString(s-k) + "-R" + Integer.toString(s));
                }
                else
                    return new CodeAndLength(2, "MOVACM " + Integer.toString(d) + ", R" + Integer.toString(s));
                
            case 0x9800:    // MVCA R, E
                return new CodeAndLength(2, "MVCA R" + Integer.toString(d) + ", " + Integer.toString(s));
                
            case 0x9C00:    // MVCAM R, E
                k = (s - d) % 8;
                if(k != 0) {
                    return new CodeAndLength(2, "MVACM R" + Integer.toString(d) + "-R" + Integer.toString(d+k) + ", " + Integer.toString(s-k) + "-" + Integer.toString(s));
                }
                else
                    return new CodeAndLength(2, "MOVACM R" + Integer.toString(d) + ", " + Integer.toString(s));
                
                
            case 0xA000:    // CALL A
            case 0xA400:
            case 0xA800:
            case 0xAC00:
                return new CodeAndLength(2, "CALL $" + Integer.toHexString((opCode & 0x0FFF) << 1));
                
            case 0xB000:    // RET
                // Not all opcodes are identified yet
                return new CodeAndLength(2, "RET");
                
            case 0xB400:    // CPFJR R, a
                return new CodeAndLength(2, "CPFJR R" + Integer.toString(d) + ", $" + Integer.toHexString(s << 1));
                
            case 0xB800:    // IJMR R
                return new CodeAndLength(2, "IJMR R" + Integer.toString(d));
                
            case 0xBC00:    // WFI
                // Not all opcodes are identified yet
                return new CodeAndLength(2, "WFI");
                
            case 0xC000:    // JMP A
            case 0xC400:
            case 0xC800:
            case 0xCC00:
                return new CodeAndLength(2, "JMP $" + Integer.toHexString((opCode & 0x0FFF) << 1));
                
            case 0xD000:    // JZ A
                return new CodeAndLength(2, "JZ $" + (Integer.toHexString(((opCode & 0x03FF) << 1) + 0x1800)));
                
            case 0xD400:    // JNZ A
                return new CodeAndLength(2, "JNZ $" + (Integer.toHexString(((opCode & 0x03FF) << 1) + 0x1800)));
                
            case 0xD800:    // JC A
                return new CodeAndLength(2, "JC $" + (Integer.toHexString(((opCode & 0x03FF) << 1) + 0x1800)));
                
            case 0xDC00:    // JNC A
                return new CodeAndLength(2, "JNC $" + (Integer.toHexString(((opCode & 0x03FF) << 1) + 0x1800)));
                
            case 0xE000:    // BTJR R, I, a
            case 0xE400:
            case 0xE800:
            case 0xEC00:
                return new CodeAndLength(2, "BTJR R" + Integer.toString(d) + ", $" + Integer.toHexString((opCode & 0x0C00) >> 10) + ", $" + Integer.toHexString(s << 1));
                
            case 0xF000:    // CPJR R, I, a
            case 0xF400:
            case 0xF800:
            case 0xFC00:
                return new CodeAndLength(2, "CPJR R" + Integer.toString(d) + ", $" + Integer.toHexString((opCode & 0x0C00) >> 10) + ", $" + Integer.toHexString(s << 1));
                
            default:
                throw new OpCodeException("OpCode " + opCode + " not supported");
        }
    }

    /**
     * Returns the bytecode for a given assembler mnemonic
     * 
     * @param mnemonic (String) the mnemonic
     * @return the byte code (byte[])
     * @throws SyntaxErrorException if there was a syntax error
     */
    @Override
    public byte[] translate(String mnemonic) throws SyntaxErrorException {
        mnemonic = mnemonic.toUpperCase();
        StringTokenizer st = new StringTokenizer(mnemonic);
        
        String mnem1 = st.nextToken();
        
        try {
            String className = "org.ansic.micro.SeikoUC2000Debugger$" + mnem1;
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
     * Returns a list of all register names
     * 
     * @return all registers (List)
     */
    @Override
    public List<String> getRegisterNames() {
        ArrayList<String> retVal = new ArrayList<>();
        
        for(int bank=0; bank<4; bank++) {
            for(int reg=0; reg<32; reg++) {
                String regName = "R" + (char)(bank + 'A') + Integer.toString(reg);
                retVal.add(regName);
            }
        }
        
        retVal.add("PC");   // Program Counter
        retVal.add("F");    // Flags
        retVal.add("CB");   // Current Bank
        retVal.add("AB");   // Additional Bank
        retVal.add("SA");   // Memory Address
        retVal.add("SP");   // Stack Pointer
        
        return retVal;
    }

    /**
     * Changes a register Value
     * 
     * @param registerName (String) the register Name
     * @param registerValue (long) the new register value
     * @throws IllegalRegisterException if the register does not exist
     * @throws IllegalRegisterValueException if the value does not fit
     */
    @Override
    public void setRegisterValue(String registerName, long registerValue) throws IllegalRegisterException, IllegalRegisterValueException {
        switch (registerName) {
            case "PC":
                if((registerValue < -32768) || (registerValue > 65535))
                    throw new IllegalRegisterValueException("Value " + registerValue + " does not fit in program counter");
                this.regPC = (short)registerValue;
                break;
                
            case "CB":
                if((registerValue < 0) || (registerValue > 0x000F))
                    throw new IllegalRegisterValueException("Value " + registerValue + " does not fit in bank select register");
                this.regCurrentBank = (byte)registerValue;
                break;
                
            case "AB":
                if((registerValue < 0) || (registerValue > 0x000F))
                    throw new IllegalRegisterValueException("Value " + registerValue + " does not fit in bank select register");
                this.regAdditionalBank = (byte)registerValue;
                break;
                
            case "SA":
                if((registerValue < 0) || (registerValue > 0xFFFF))
                    throw new IllegalRegisterValueException("Value " + registerValue + " does not fit in SA register");
                this.regSA = (short)registerValue;
                break;
                
            case "SP":
                if((registerValue < 0) || (registerValue > 0xFFFF))
                    throw new IllegalRegisterValueException("Value " + registerValue + " does not fit in SP register");
                this.regSP = (short)registerValue;
                break;
                
                
            case "F":
                if((registerValue < 0) || (registerValue > 3))
                    throw new IllegalRegisterValueException("Value " + registerValue + " does not fit in flags register");
                this.regFlags = (byte)registerValue;
                
            default:
                if(registerName.length() < 3)
                    throw new IllegalRegisterException("Register " + registerName + " does not exist");
                
                // Check for Rbx (b=bank, x=register number)
                
                // get the bank number
                int bank = 0;
                switch (registerName.charAt(1)) {
                    case 'A':
                        bank = 0;
                        break;
                        
                    case 'B':
                        bank = 1;
                        break;
                        
                    case 'C':
                        bank = 2;
                        break;
                        
                    case 'D':
                        bank = 3;
                        break;
                        
                    default:
                        throw new IllegalRegisterException("Register " + registerName + " does not exist");
                }
                
                // get the register number
                try {
                    int regNum = Integer.parseInt(registerName.substring(2));
                    if((regNum < 0) || (regNum > 31))
                        throw new IllegalRegisterException("Register " + registerName + " does not exist");
                    
                    if((registerValue < -8) || (registerValue > 15)) 
                        throw new IllegalRegisterValueException("Value " + registerValue + " does not fit in register " + registerName);
                    
                    this.registers[bank][regNum] = (byte)(registerValue & 0x0F);
                }
                catch (NumberFormatException ex) {
                    throw new IllegalRegisterException("Register " + registerName + " does not exist");
                }
        }
    }

    /**
     * Returns the value of a register
     * 
     * @param registerName (String) the register name
     * @return the value of the register (long)
     * @throws IllegalRegisterException 
     */
    @Override
    public long getRegisterValue(String registerName) throws IllegalRegisterException {
        switch (registerName) {
            case "PC":
                return this.regPC;
                
            case "F":
                return this.regFlags;
                
            case "CB":
                return this.regCurrentBank;
                
            case "AB":
                return this.regAdditionalBank;
                
            case "SA":
                return this.regSA;
                
            case "SP":
                return this.regSP;
                
            default:
                if(registerName.length() < 3)
                    throw new IllegalRegisterException("Register " + registerName + " does not exist");
                
                // Check for Rbx (b=bank, x=register number)
                
                // get the bank number
                int bank = 0;
                switch (registerName.charAt(1)) {
                    case 'A':
                        bank = 0;
                        break;
                        
                    case 'B':
                        bank = 1;
                        break;
                        
                    case 'C':
                        bank = 2;
                        break;
                        
                    case 'D':
                        bank = 3;
                        break;
                        
                    default:
                        throw new IllegalRegisterException("Register " + registerName + " does not exist");
                }
                
                // get the register number
                try {
                    int regNum = Integer.parseInt(registerName.substring(2));
                    if((regNum < 0) || (regNum > 31))
                        throw new IllegalRegisterException("Register " + registerName + " does not exist");
                    
                    return this.registers[bank][regNum];
                }
                catch (NumberFormatException ex) {
                    throw new IllegalRegisterException("Register " + registerName + " does not exist");
                }
                
        }
    }
    
    /**
     * Returns the program counter
     * 
     * @return the program counter register
     */
    @Override
    public long getProgramCounter() {
        return Short.toUnsignedLong(this.regPC);
    }

    /**
     * Returns the size of the register
     * 
     * @param registerName (String) the name of the register
     * @return the size in bits (int)
     * @throws IllegalRegisterException if the register does not exist 
     */
    @Override
    public int getRegisterSize(String registerName) throws IllegalRegisterException {
        switch (registerName) {
            case "PC":
            case "SA":
                return 16;
                
            case "F":
            case "CB":
            case "AB":
            case "SP":
                return 4;
                
            default:
                if(registerName.length() < 3)
                    throw new IllegalRegisterException("Register " + registerName + " does not exist");
                
                // bank between a and d?
                if((registerName.charAt(1) < 'A') || (registerName.charAt(1) > 'D'))
                    throw new IllegalRegisterException("Register " + registerName + " does not exist");
                
                // register number correct?
                try {
                    int regNum = Integer.parseInt(registerName.substring(2));
                    
                    if((regNum < 0) || (regNum > 31))
                        throw new IllegalRegisterException("Register " + registerName + " does not exist");
                    
                    return 4;
                }
                catch (NumberFormatException ex) {
                    throw new IllegalRegisterException("Register " + registerName + " does not exist");
                }
                    
        }
    }

    /**
     * Returns a single byte of memory
     * 
     * @param address (long) the address
     * @return the content (byte)
     * @throws MemoryException 
     */
    @Override
    public byte readMemoryByte(long address) throws MemoryException {
        return this.readMemory8(address);
    }
    
    /**
     * Updates a single byte of memory
     * 
     * @param address (long) the address
     * @param value (byte) the new content
     * @throws MemoryException 
     */
    @Override
    public void writeMemoryByte(long address, byte value) throws MemoryException {
        this.writeMemory8(address, value);
    }
    
    /**
     * Sets a new program counter value
     * 
     * @param programCounter (long) the new address
     * @throws IllegalRegisterValueException if the address is out of range
     */
    @Override
    public void setProgramCounter(long programCounter) throws IllegalRegisterValueException {
        if((programCounter < -32768) || (programCounter >= 65535))
            throw new IllegalRegisterValueException("Value " + programCounter + " too large to fit in program counter");
        
        this.regPC = (short)programCounter;
    }
}
