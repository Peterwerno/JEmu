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
 * This class implements a debugger for the Z80 processor
 * 
 * @author peter
 */
public class Z80Debugger extends Z80 implements Debugger {
    public abstract static class Z80Mnemonic extends Debugger.Mnemonic {
        public static byte getRegisterNum8(String registerName) throws SyntaxErrorException {
            switch (registerName) {
                case "B":
                case "B'":
                    return (byte)0x00;
                    
                case "C":
                case "C'":
                    return (byte)0x01;
                    
                case "D":
                case "D'":
                    return (byte)0x02;
                    
                case "E":
                case "E'":
                    return (byte)0x03;
                    
                case "H":
                case "H'":
                    return (byte)0x04;
                    
                case "L":
                case "L'":
                    return (byte)0x05;
                    
                case "A":
                case "A'":
                    return (byte)0x07;
                    
                default:
                    throw new SyntaxErrorException("Register " + registerName + " not available");
            }
        }
        
        public static byte getRegisterNum16(String registerName) throws SyntaxErrorException {
            switch (registerName) {
                case "BC":
                    return (byte)0x00;
                    
                case "DE":
                    return (byte)0x01;
                    
                case "HL":
                    return (byte)0x02;
                    
                case "SP":
                    return (byte)0x03;
                    
                default:
                    throw new SyntaxErrorException("Register " + registerName + " not available");
            }
        }
    }
    
    /**
     * class LD 
     * implements the "LD" operation
     */
    public static class LD extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("LD command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("LD command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            // LD r, r'
            if((isIn(param1, "A,B,C,D,E,H,L")) && (isIn(param2, "A',B',C',D',E',H',L'"))) {
                return new byte[]{(byte)(0x40 | (getRegisterNum8(param1) << 3) | (getRegisterNum8(param2)))};
            }
            
            // LD r, n
            if((isIn(param1, "A,B,C,D,E,H,L")) && (isNumeric(param2))) {
                int value = getNumeric(param2);
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Number " + param2 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)(0x06 + (getRegisterNum8(param1) << 3)), (byte)(value)};
            }
            
            // LD r, (HL)
            if((isIn(param1, "A,B,C,D,E,H,L")) && (param2.equals("(HL)"))) {
                return new byte[]{(byte)(0x46 | (getRegisterNum8(param1) << 3))};
            }
            
            // LD r, (IX+d)
            if((isIn(param1, "A,B,C,D,E,H,L")) && (param2.startsWith("(IX")) && (param2.endsWith(")"))) {
                int value = 0;
                if(param2.startsWith("(IX+"))
                    value = getNumeric(param2.substring(4, param2.length()-1));
                else if(param2.startsWith("(IX-"))
                    value = -getNumeric(param2.substring(4, param2.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 2nd parameter in LD: " + param2);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param2 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)(0x46 | (getRegisterNum8(param1) << 3)), (byte)(value)};
            }
            
            // LD r, (IY+d)
            if((isIn(param1, "A,B,C,D,E,H,L")) && (param2.startsWith("(IY")) && (param2.endsWith(")"))) {
                int value = 0;
                if(param2.startsWith("(IY+"))
                    value = getNumeric(param2.substring(4, param2.length()-1));
                else if(param2.startsWith("(IY-"))
                    value = -getNumeric(param2.substring(4, param2.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 2nd parameter in LD: " + param2);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param2 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)(0x46 | (getRegisterNum8(param1) << 3)), (byte)(value)};
            }
            
            // LD (HL), r
            if((param1.equals("(HL)")) && (isIn(param2, "A,B,C,D,E,H,L"))) {
                return new byte[]{(byte)(0x70 | (getRegisterNum8(param2)))};
            }
            
            // LD (IX+d), r
            if((param1.startsWith("(IX")) && (param1.endsWith(")")) && (isIn(param2, "A,B,C,D,E,H,L"))) {
                int value = 0;
                if(param1.startsWith("(IX+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IX-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in LD: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)(0x70 | (getRegisterNum8(param1))), (byte)(value)};
            }
            
            // LD (IY+d), r
            if((param1.startsWith("(IY")) && (param1.endsWith(")")) && (isIn(param2, "A,B,C,D,E,H,L"))) {
                int value = 0;
                if(param1.startsWith("(IY+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IY-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in LD: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)(0x70 | (getRegisterNum8(param1))), (byte)(value)};
            }
            
            // LD (HL), n
            if(param1.equals("(HL)") && isNumeric(param2)) {
                int value = getNumeric(param2);
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Value " + param2 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0x36, (byte)(value) };
            }
            
            // LD (IX+d), n
            if((param1.startsWith("(IX")) && (param1.endsWith(")")) && (isNumeric(param2))) {
                int value = 0;
                if(param1.startsWith("(IX+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IX-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in LD: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                int value2 = getNumeric(param2);
                if((value2 < -128) || (value2 > 255))
                    throw new SyntaxErrorException("Offset value " + param2 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)0x36, (byte)value, (byte)value2};
            }
            
            // LD (IY+d), n
            if((param1.startsWith("(IY")) && (param1.endsWith(")")) && (isNumeric(param2))) {
                int value = 0;
                if(param1.startsWith("(IY+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IY-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in LD: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                int value2 = getNumeric(param2);
                if((value2 < -128) || (value2 > 255))
                    throw new SyntaxErrorException("Offset value " + param2 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)0x36, (byte)value, (byte)value2};
            }
            
            // LD A, (BC)
            if(param1.equals("A") && param2.equals("(BC)"))
                return new byte[]{(byte)0x0A};
            
            // LD A, (DE)
            if(param1.equals("A") && param2.equals("(DE)"))
                return new byte[]{(byte)0x1A};
            
            // LD A, (nn)
            if(param1.equals("A") && param2.startsWith("(") && param2.endsWith(")") && isNumeric(param2.substring(1, param2.length()-1))) {
                int value = getNumeric(param2.substring(2, param2.length()-1));
                if((value < -32768) || (value > 65535))
                    throw new SyntaxErrorException("Value " + param2 + " too large to fit in 16 bit!");
                return new byte[]{(byte)0x3A, (byte)(value & 0xFF), (byte)((value & 0xFF00) >> 8)};
            }
            
            // LD (BC), A
            if(param1.equals("(BC)") && param2.equals("A"))
                return new byte[]{(byte)0x02};
            
            // LD (DE), A
            if(param1.equals("(DE)") && param2.equals("A"))
                return new byte[]{(byte)0x12};
            
            // LD (nn), A
            if(param1.startsWith("(") && param1.endsWith(")") && isNumeric(param1.substring(1, param1.length()-1)) && param2.equals("A")) {
                int value = getNumeric(param1.substring(2, param1.length()-1));
                if((value < -32768) || (value > 65535))
                    throw new SyntaxErrorException("Value " + param1 + " too large to fit in 16 bit!");
                return new byte[]{(byte)0x32, (byte)(value & 0xFF), (byte)((value & 0xFF00) >> 8)};
            }
            
            // LD A, I
            if(param1.equals("A") && param2.equals("I"))
                return new byte[]{(byte)0xED, (byte)0x57};
            
            // LD A, R
            if(param1.equals("A") && param2.equals("R"))
                return new byte[]{(byte)0xED, (byte)0x5F};
            
            // LD I, A
            if(param1.equals("I") && param2.equals("A"))
                return new byte[]{(byte)0xED, (byte)0x47};
            
            // LD R, A
            if(param1.equals("A") && param2.equals("R"))
                return new byte[]{(byte)0xED, (byte)0x4F};
            
            // 16 Bit lDs
            
            // LD dd, nn
            if(isIn(param1, "BC,DE,HL,SP") && isNumeric(param2)) {
                int value = getNumeric(param2);
                if((value < -32768) || (value > 65535))
                    throw new SyntaxErrorException("Value " + param1 + " too large to fit in 16 bit!");
                return new byte[]{(byte)(0x01 | (getRegisterNum16(param1) << 4)), (byte)(value & 0xFF), (byte)((value & 0xFF00) >> 8)};
            }
            
            // LD IX, nn
            if(param1.equals("IX") && isNumeric(param2)) {
                int value = getNumeric(param2);
                if((value < -32768) || (value > 65535))
                    throw new SyntaxErrorException("Value " + param1 + " too large to fit in 16 bit!");
                return new byte[]{(byte)0xDD, (byte)0x21, (byte)(value & 0xFF), (byte)((value & 0xFF00) >> 8)};
            }
            
            // LD IY, nn
            if(param1.equals("IY") && isNumeric(param2)) {
                int value = getNumeric(param2);
                if((value < -32768) || (value > 65535))
                    throw new SyntaxErrorException("Value " + param1 + " too large to fit in 16 bit!");
                return new byte[]{(byte)0xFD, (byte)0x21, (byte)(value & 0xFF), (byte)((value & 0xFF00) >> 8)};
            }
            
            // LD HL, (nn)
            if(param1.equals("HL") && param2.startsWith("(") && param2.endsWith(")") && isNumeric(param2.substring(1, param2.length()-1))) {
                int value = getNumeric(param2.substring(1, param2.length()-1));
                if((value < -32768) || (value > 65535))
                    throw new SyntaxErrorException("Value " + param1 + " too large to fit in 16 bit!");
                return new byte[]{(byte)0x2A, (byte)(value & 0xFF), (byte)((value & 0xFF00) >> 8)};
            }
            
            // LD dd, (nn)
            if(isIn(param1, "BC,DE,HL,SP") && param2.startsWith("(") && param2.endsWith(")") && isNumeric(param2.substring(1, param2.length()-1))) {
                int value = getNumeric(param2.substring(1, param2.length()-1));
                if((value < -32768) || (value > 65535))
                    throw new SyntaxErrorException("Value " + param1 + " too large to fit in 16 bit!");
                return new byte[]{(byte)0xED, (byte)(0x4B | (getRegisterNum16(param1) << 4)), (byte)(value & 0xFF), (byte)((value & 0xFF00) >> 8)};
            }
            
            // LD IX, (nn)
            if(param1.equals("IX") && param2.startsWith("(") && param2.endsWith(")") && isNumeric(param2.substring(1, param2.length()-1))) {
                int value = getNumeric(param2.substring(1, param2.length()-1));
                if((value < -32768) || (value > 65535))
                    throw new SyntaxErrorException("Value " + param1 + " too large to fit in 16 bit!");
                return new byte[]{(byte)0xDD, (byte)0x2A, (byte)(value & 0xFF), (byte)((value & 0xFF00) >> 8)};
            }
            
            // LD IY, (nn)
            if(param1.equals("IX") && param2.startsWith("(") && param2.endsWith(")") && isNumeric(param2.substring(1, param2.length()-1))) {
                int value = getNumeric(param2.substring(1, param2.length()-1));
                if((value < -32768) || (value > 65535))
                    throw new SyntaxErrorException("Value " + param1 + " too large to fit in 16 bit!");
                return new byte[]{(byte)0xFD, (byte)0x2A, (byte)(value & 0xFF), (byte)((value & 0xFF00) >> 8)};
            }
            
            // LD (nn), HL
            if(param1.startsWith("(") && param1.endsWith(")") && isNumeric(param1.substring(1, param1.length()-1)) && param2.equals("HL")) {
                int value = getNumeric(param2.substring(1, param2.length()-1));
                if((value < -32768) || (value > 65535))
                    throw new SyntaxErrorException("Value " + param1 + " too large to fit in 16 bit!");
                return new byte[]{(byte)0x22, (byte)(value & 0xFF), (byte)((value & 0xFF00) >> 8)};
            }
            
            // LD (nn), dd
            if(param1.startsWith("(") && param1.endsWith(")") && isNumeric(param1.substring(1, param1.length()-1)) && isIn(param2,"BC,DE,HL,SP")) {
                int value = getNumeric(param2.substring(1, param2.length()-1));
                if((value < -32768) || (value > 65535))
                    throw new SyntaxErrorException("Value " + param1 + " too large to fit in 16 bit!");
                return new byte[]{(byte)0xED, (byte)(0x43 | (getRegisterNum16(param2)<<4)), (byte)(value & 0xFF), (byte)((value & 0xFF00) >> 8)};
            }
            
            // LD (nn), IX
            if(param1.startsWith("(") && param1.endsWith(")") && isNumeric(param1.substring(1, param1.length()-1)) && param2.equals("IX")) {
                int value = getNumeric(param2.substring(1, param2.length()-1));
                if((value < -32768) || (value > 65535))
                    throw new SyntaxErrorException("Value " + param1 + " too large to fit in 16 bit!");
                return new byte[]{(byte)0xDD, (byte)0x22, (byte)(value & 0xFF), (byte)((value & 0xFF00) >> 8)};
            }
            
            // LD (nn), IY
            if(param1.startsWith("(") && param1.endsWith(")") && isNumeric(param1.substring(1, param1.length()-1)) && param2.equals("IY")) {
                int value = getNumeric(param2.substring(1, param2.length()-1));
                if((value < -32768) || (value > 65535))
                    throw new SyntaxErrorException("Value " + param1 + " too large to fit in 16 bit!");
                return new byte[]{(byte)0xFD, (byte)0x22, (byte)(value & 0xFF), (byte)((value & 0xFF00) >> 8)};
            }
            
            // LD SP, HL
            if(param1.equals("SP") && param2.equals("HL"))
                return new byte[]{(byte)0xF9};
            
            // LD SP, IX
            if(param1.equals("SP") && param2.equals("IX"))
                return new byte[]{(byte)0xDD, (byte)0xF9};
            
            // LD SP, HL
            if(param1.equals("SP") && param2.equals("IY"))
                return new byte[]{(byte)0xFD, (byte)0xF9};
            
            
            throw new SyntaxErrorException("Error in LD mnemonic");
        }
    }
    
    /**
     * Class PUSH
     * implements the assembler for PUSH commands
     */
    public static class PUSH extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("PUSH command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // PUSH qq
            if(isIn(param1, "BC,DE,HL"))
                return new byte[]{(byte)(0xC5 | (getRegisterNum16(param1)<<4))};
            
            // PUSH AF
            if(param1.equals("AF"))
                return new byte[]{(byte)0xF5};
            
            // PUSH IX
            if(param1.equals("IX"))
                return new byte[]{(byte)0xDD, (byte)0xE5};
            
            // PUSH IY
            if(param1.equals("IY"))
                return new byte[]{(byte)0xFD, (byte)0xE5};
            
            throw new SyntaxErrorException("Error in PUSH mnemonic");
        }
    }
    
    /**
     * Class POP
     * implements the assembler for PUSH commands
     */
    public static class POP extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("POP command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // POP qq
            if(isIn(param1, "BC,DE,HL"))
                return new byte[]{(byte)(0xC1 | (getRegisterNum16(param1)<<4))};
            
            // POP AF
            if(param1.equals("AF"))
                return new byte[]{(byte)0xF1};
            
            // POP IX
            if(param1.equals("IX"))
                return new byte[]{(byte)0xDD, (byte)0xE1};
            
            // POP IY
            if(param1.equals("IY"))
                return new byte[]{(byte)0xFD, (byte)0xE1};
            
            throw new SyntaxErrorException("Error in POP mnemonic");
        }
    }
    
    /**
     * class EX
     * implements the assembler for EX commands
     */
    public static class EX extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("EX command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("EX command requires 2 parameters");
            String param2 = parameters.nextToken();

            // EX DE, HL
            if(param1.equals("DE") && param2.equals("HL"))
                return new byte[]{(byte)0xEB};
            
            // EX AF, AF'
            if(param1.equals("AF") && param2.equals("AF'"))
                return new byte[]{(byte)0x08};
            
            // EX (SP),HL
            if(param1.equals("(SP)") && param2.equals("HL"))
                return new byte[]{(byte)0xE3};
            
            // EX (SP), IX
            if(param1.equals("(SP)") && param2.equals("IX"))
                return new byte[]{(byte)0xDD, (byte)0xE3};
            
            // EX (SP), IY
            if(param1.equals("(SP)") && param2.equals("IY"))
                return new byte[]{(byte)0xFD, (byte)0xE3};
            
            throw new SyntaxErrorException("Error in EX mnemonic");
        }
    }
    
    /**
     * class EXX
     * implements the assembler for EXX command
     */
    public static class EXX extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0xD9};
        }
    }
    
    /**
     * class LDI
     * implements the assembler for LDI commands
     */
    public static class LDI extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0xED, (byte)0xA0};
        }
    }
    
    /**
     * class LDIR
     * implements the assembler for LDIR commands
     */
    public static class LDIR extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0xED, (byte)0xB0};
        }
    }
    
   /**
     * class LDD
     * implements the assembler for LDD commands
     */
    public static class LDD extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0xED, (byte)0xA8};
        }
    }
    
   /**
     * class LDDR
     * implements the assembler for LDDR commands
     */
    public static class LDDR extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0xED, (byte)0xB8};
        }
    }
    
   /**
     * class CPI
     * implements the assembler for CPI commands
     */
    public static class CPI extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0xED, (byte)0xA1};
        }
    }
    
   /**
     * class CPIR
     * implements the assembler for CPIR commands
     */
    public static class CPIR extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0xED, (byte)0xB1};
        }
    }
    
   /**
     * class CPD
     * implements the assembler for CPD commands
     */
    public static class CPD extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0xED, (byte)0xA9};
        }
    }
    
   /**
     * class CPDR
     * implements the assembler for CPDR commands
     */
    public static class CPDR extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0xED, (byte)0xB9};
        }
    }
    
    /**
     * class ADD
     * implements the assembler for ADD commands
     */
    public static class ADD extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ADD command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ADD command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            // ADD A, r
            if(param1.equals("A") && isIn(param2, "A,B,C,D,E,H,L"))
                return new byte[]{(byte)(0x80 | (getRegisterNum8(param2)))};
            
            // ADD A, n
            if(param1.equals("A") && isNumeric(param2)) {
                int value = getNumeric(param2);
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xC6, (byte)value};
            }
            
            // ADD A, (HL)
            if(param1.equals("A") && param2.equals("(HL)"))
                return new byte[]{(byte)0x86};
            
            // ADD A, (IX+d)
            if(param1.equals("A") && (param2.startsWith("(IX")) && (param2.endsWith(")"))) {
                int value = 0;
                if(param2.startsWith("(IX+"))
                    value = getNumeric(param2.substring(4, param2.length()-1));
                else if(param2.startsWith("(IX-"))
                    value = -getNumeric(param2.substring(4, param2.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 2nd parameter in LD: " + param2);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param2 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)0x86, (byte)(value)};
            }
            
            // ADD A, (IY+d)
            if(param1.equals("A") && (param2.startsWith("(IY")) && (param2.endsWith(")"))) {
                int value = 0;
                if(param2.startsWith("(IY+"))
                    value = getNumeric(param2.substring(4, param2.length()-1));
                else if(param2.startsWith("(IY-"))
                    value = -getNumeric(param2.substring(4, param2.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 2nd parameter in LD: " + param2);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param2 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)0x86, (byte)(value)};
            }
            
            // 16 bit
            // ADD HL, ss
            if(param1.equals("HL") && isIn(param2, "BC,DE,HL,SP"))
                return new byte[]{(byte)(0x09 | (getRegisterNum16(param2)<<4))};
            
            // ADD IX, ss
            if(param1.equals("IX") && isIn(param2, "BC,DE,SP"))
                return new byte[]{(byte)0xDD, (byte)(0x09 | (getRegisterNum16(param2)<<4))};
            
            // ADD IX, IX
            if(param1.equals("IX") && param2.equals("IX"))
                return new byte[]{(byte)0xDD, (byte)0x29};
            
            // ADD IY, ss
            if(param1.equals("IX") && isIn(param2, "BC,DE,SP"))
                return new byte[]{(byte)0xFD, (byte)(0x09 | (getRegisterNum16(param2)<<4))};
            
            // ADD IY, IY
            if(param1.equals("IY") && param2.equals("IY"))
                return new byte[]{(byte)0xFD, (byte)0x29};
            
            throw new SyntaxErrorException("Error in ADD mnemonic");
        }        
    }
    
    /**
     * Class ADC
     * implements the assembler for ADC commands
     */
    public static class ADC extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ADC command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("ADC command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            switch(param1) {
                case "A":
                    switch (param2) {
                        case "B":
                            return new byte[]{(byte)0x88};
                        case "C":
                            return new byte[]{(byte)0x89};
                        case "D":
                            return new byte[]{(byte)0x8A};
                        case "E":
                            return new byte[]{(byte)0x8B};
                        case "H":
                            return new byte[]{(byte)0x8C};
                        case "L":
                            return new byte[]{(byte)0x8D};
                        case "(HL)":
                            return new byte[]{(byte)0x8E};
                        case "A":
                            return new byte[]{(byte)0x8F};
                        default:
                            // Three different possibilities here:
                            // 1: (IX+d)
                            if(param2.startsWith("(IX+") && param2.endsWith(")")) {
                                String offset = param2.substring(4, param2.length()-1);
                                int value;
                                if(offset.startsWith("$")) {
                                    value = Integer.parseInt(offset.substring(1), 16);
                                }
                                else {
                                    value = Integer.parseInt(offset, 10);
                                }

                                if((value > 255) || (value < -128))
                                    throw new SyntaxErrorException("Numeric value must fit in 8 bits!");

                                return new byte[]{(byte)0xDD, (byte)0x8E, (byte)value};
                            }
                            // 2: (IY+d)
                            else if(param2.startsWith("(IY+") && param2.endsWith(")")) {
                                String offset = param2.substring(4, param2.length()-1);
                                int value;
                                if(offset.startsWith("$")) {
                                    value = Integer.parseInt(offset.substring(1), 16);
                                }
                                else {
                                    value = Integer.parseInt(offset, 10);
                                }

                                if((value > 255) || (value < -128))
                                    throw new SyntaxErrorException("Numeric value must fit in 8 bits!");

                                return new byte[]{(byte)0xFD, (byte)0x8E, (byte)value};
                            }
                            // 3: numeric constant
                            else {
                                // Check for hex ($)
                                int value;
                                if(param2.startsWith("$")) {
                                    value = Integer.parseInt(param2.substring(1), 16);
                                }
                                else {
                                    value = Integer.parseInt(param2, 10);
                                }

                                if((value > 255) || (value < -128))
                                    throw new SyntaxErrorException("Numeric value must fit in 8 bits!");

                                return new byte[]{(byte)0xCE, (byte)value};
                            }
                    }
                    
                case "HL":
                    switch (param2) {
                        case "BC":
                            return new byte[]{(byte)0xED, (byte)0x4A};
                        case "DE":
                            return new byte[]{(byte)0xED, (byte)0x5A};
                        case "HL":
                            return new byte[]{(byte)0xED, (byte)0x6A};
                        case "SP":
                            return new byte[]{(byte)0xED, (byte)0x7A};
                        default:
                            throw new SyntaxErrorException("Error in ADC mnemonic");
                    }
                    
                
                default:
                    throw new SyntaxErrorException("Error in ADC mnemonic");
            }
        }
    }
    
    /**
     * class SUB
     * implements the assembler for SUB commands
     */
    public static class SUB extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SUB command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SUB command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            // SUB A, r
            if(param1.equals("A") && isIn(param2, "A,B,C,D,E,H,L"))
                return new byte[]{(byte)(0x90 | (getRegisterNum8(param2)))};
            
            // SUB A, n
            if(param1.equals("A") && isNumeric(param2)) {
                int value = getNumeric(param2);
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xD6, (byte)value};
            }
            
            // SUB A, (HL)
            if(param1.equals("A") && param2.equals("(HL)"))
                return new byte[]{(byte)0x96};
            
            // SUB A, (IX+d)
            if(param1.equals("A") && (param2.startsWith("(IX")) && (param2.endsWith(")"))) {
                int value = 0;
                if(param2.startsWith("(IX+"))
                    value = getNumeric(param2.substring(4, param2.length()-1));
                else if(param2.startsWith("(IX-"))
                    value = -getNumeric(param2.substring(4, param2.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 2nd parameter in LD: " + param2);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param2 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)0x96, (byte)(value)};
            }
            
            // SUB A, (IY+d)
            if(param1.equals("A") && (param2.startsWith("(IY")) && (param2.endsWith(")"))) {
                int value = 0;
                if(param2.startsWith("(IY+"))
                    value = getNumeric(param2.substring(4, param2.length()-1));
                else if(param2.startsWith("(IY-"))
                    value = -getNumeric(param2.substring(4, param2.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 2nd parameter in LD: " + param2);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param2 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)0x96, (byte)(value)};
            }
            
            throw new SyntaxErrorException("Error in SUB mnemonic");
        }        
    }
    
    /**
     * class SBC
     * implements the assembler for SBC commands
     */
    public static class SBC extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SBC command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SBC command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            // SBC A, r
            if(param1.equals("A") && isIn(param2, "A,B,C,D,E,H,L"))
                return new byte[]{(byte)(0x98 | (getRegisterNum8(param2)))};
            
            // SBC A, n
            if(param1.equals("A") && isNumeric(param2)) {
                int value = getNumeric(param2);
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDE, (byte)value};
            }
            
            // SBC A, (HL)
            if(param1.equals("A") && param2.equals("(HL)"))
                return new byte[]{(byte)0x9E};
            
            // SBC A, (IX+d)
            if(param1.equals("A") && (param2.startsWith("(IX")) && (param2.endsWith(")"))) {
                int value = 0;
                if(param2.startsWith("(IX+"))
                    value = getNumeric(param2.substring(4, param2.length()-1));
                else if(param2.startsWith("(IX-"))
                    value = -getNumeric(param2.substring(4, param2.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 2nd parameter in LD: " + param2);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param2 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)0x9E, (byte)(value)};
            }
            
            // SBC A, (IY+d)
            if(param1.equals("A") && (param2.startsWith("(IY")) && (param2.endsWith(")"))) {
                int value = 0;
                if(param2.startsWith("(IY+"))
                    value = getNumeric(param2.substring(4, param2.length()-1));
                else if(param2.startsWith("(IY-"))
                    value = -getNumeric(param2.substring(4, param2.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 2nd parameter in LD: " + param2);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param2 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)0x9E, (byte)(value)};
            }
            
            // 16 bit
            // SBC HL, ss
            if(param1.equals("HL") & isIn(param2, "BC,DE,HL,SP"))
                return new byte[]{(byte)0xED, (byte)(0x42 | (getRegisterNum16(param2)<<4))};
            
            throw new SyntaxErrorException("Error in SBC mnemonic");
        }        
    }
    
    /**
     * class AND
     * implements the assembler for AND commands
     */
    public static class AND extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("AND command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("AND command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            // AND A, r
            if(param1.equals("A") && isIn(param2, "A,B,C,D,E,H,L"))
                return new byte[]{(byte)(0xA0 | (getRegisterNum8(param2)))};
            
            // AND A, n
            if(param1.equals("A") && isNumeric(param2)) {
                int value = getNumeric(param2);
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xE6, (byte)value};
            }
            
            // AND A, (HL)
            if(param1.equals("A") && param2.equals("(HL)"))
                return new byte[]{(byte)0xA6};
            
            // AND A, (IX+d)
            if(param1.equals("A") && (param2.startsWith("(IX")) && (param2.endsWith(")"))) {
                int value = 0;
                if(param2.startsWith("(IX+"))
                    value = getNumeric(param2.substring(4, param2.length()-1));
                else if(param2.startsWith("(IX-"))
                    value = -getNumeric(param2.substring(4, param2.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 2nd parameter in LD: " + param2);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param2 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)0xA6, (byte)(value)};
            }
            
            // AND A, (IY+d)
            if(param1.equals("A") && (param2.startsWith("(IY")) && (param2.endsWith(")"))) {
                int value = 0;
                if(param2.startsWith("(IY+"))
                    value = getNumeric(param2.substring(4, param2.length()-1));
                else if(param2.startsWith("(IY-"))
                    value = -getNumeric(param2.substring(4, param2.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 2nd parameter in LD: " + param2);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param2 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)0xA6, (byte)(value)};
            }
            
            throw new SyntaxErrorException("Error in AND mnemonic");
        }        
    }
    
    /**
     * class OR
     * implements the assembler for OR commands
     */
    public static class OR extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("OR command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("OR command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            // OR A, r
            if(param1.equals("A") && isIn(param2, "A,B,C,D,E,H,L"))
                return new byte[]{(byte)(0xB0 | (getRegisterNum8(param2)))};
            
            // OR A, n
            if(param1.equals("A") && isNumeric(param2)) {
                int value = getNumeric(param2);
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xF6, (byte)value};
            }
            
            // OR A, (HL)
            if(param1.equals("A") && param2.equals("(HL)"))
                return new byte[]{(byte)0xB6};
            
            // OR A, (IX+d)
            if(param1.equals("A") && (param2.startsWith("(IX")) && (param2.endsWith(")"))) {
                int value = 0;
                if(param2.startsWith("(IX+"))
                    value = getNumeric(param2.substring(4, param2.length()-1));
                else if(param2.startsWith("(IX-"))
                    value = -getNumeric(param2.substring(4, param2.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 2nd parameter in LD: " + param2);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param2 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)0xB6, (byte)(value)};
            }
            
            // OR A, (IY+d)
            if(param1.equals("A") && (param2.startsWith("(IY")) && (param2.endsWith(")"))) {
                int value = 0;
                if(param2.startsWith("(IY+"))
                    value = getNumeric(param2.substring(4, param2.length()-1));
                else if(param2.startsWith("(IY-"))
                    value = -getNumeric(param2.substring(4, param2.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 2nd parameter in LD: " + param2);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param2 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)0xB6, (byte)(value)};
            }
            
            throw new SyntaxErrorException("Error in OR mnemonic");
        }        
    }
    
    /**
     * class XOR
     * implements the assembler for XOR commands
     */
    public static class XOR extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("XOR command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("XOR command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            // XOR A, r
            if(param1.equals("A") && isIn(param2, "A,B,C,D,E,H,L"))
                return new byte[]{(byte)(0xA8 | (getRegisterNum8(param2)))};
            
            // XOR A, n
            if(param1.equals("A") && isNumeric(param2)) {
                int value = getNumeric(param2);
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xEE, (byte)value};
            }
            
            // XOR A, (HL)
            if(param1.equals("A") && param2.equals("(HL)"))
                return new byte[]{(byte)0xAE};
            
            // XOR A, (IX+d)
            if(param1.equals("A") && (param2.startsWith("(IX")) && (param2.endsWith(")"))) {
                int value = 0;
                if(param2.startsWith("(IX+"))
                    value = getNumeric(param2.substring(4, param2.length()-1));
                else if(param2.startsWith("(IX-"))
                    value = -getNumeric(param2.substring(4, param2.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 2nd parameter in LD: " + param2);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param2 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)0xAE, (byte)(value)};
            }
            
            // XOR A, (IY+d)
            if(param1.equals("A") && (param2.startsWith("(IY")) && (param2.endsWith(")"))) {
                int value = 0;
                if(param2.startsWith("(IY+"))
                    value = getNumeric(param2.substring(4, param2.length()-1));
                else if(param2.startsWith("(IY-"))
                    value = -getNumeric(param2.substring(4, param2.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 2nd parameter in LD: " + param2);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param2 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)0xAE, (byte)(value)};
            }
            
            throw new SyntaxErrorException("Error in XOR mnemonic");
        }        
    }
    
    /**
     * class CP
     * implements the assembler for CP commands
     */
    public static class CP extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CP command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CP command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            // CP A, r
            if(param1.equals("A") && isIn(param2, "A,B,C,D,E,H,L"))
                return new byte[]{(byte)(0xB8 | (getRegisterNum8(param2)))};
            
            // CP A, n
            if(param1.equals("A") && isNumeric(param2)) {
                int value = getNumeric(param2);
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFE, (byte)value};
            }
            
            // CP A, (HL)
            if(param1.equals("A") && param2.equals("(HL)"))
                return new byte[]{(byte)0xBE};
            
            // CP A, (IX+d)
            if(param1.equals("A") && (param2.startsWith("(IX")) && (param2.endsWith(")"))) {
                int value = 0;
                if(param2.startsWith("(IX+"))
                    value = getNumeric(param2.substring(4, param2.length()-1));
                else if(param2.startsWith("(IX-"))
                    value = -getNumeric(param2.substring(4, param2.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 2nd parameter in LD: " + param2);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param2 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)0xBE, (byte)(value)};
            }
            
            // CP A, (IY+d)
            if(param1.equals("A") && (param2.startsWith("(IY")) && (param2.endsWith(")"))) {
                int value = 0;
                if(param2.startsWith("(IY+"))
                    value = getNumeric(param2.substring(4, param2.length()-1));
                else if(param2.startsWith("(IY-"))
                    value = -getNumeric(param2.substring(4, param2.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 2nd parameter in LD: " + param2);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param2 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)0xBE, (byte)(value)};
            }
            
            throw new SyntaxErrorException("Error in CP mnemonic");
        }        
    }
    
    /**
     * class INC
     * impements the assembler of the INC commands
     */
    public static class INC extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("INC command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // INC r
            if(isIn(param1, "A,B,C,D,E,H,L"))
                return new byte[]{(byte)(0x04 | (getRegisterNum8(param1)<<3)) };
            
            // INC (HL)
            if(param1.equals("(HL)"))
                return new byte[]{(byte)0x34};
            
            // INC (IX+d)
            if(param1.startsWith("(IX") && param1.endsWith(")")) {
                int value = 0;
                if(param1.startsWith("(IX+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IX-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in INC: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)0x34, (byte)(value)};
            }
            
            // INC (IY+d)
            if(param1.startsWith("(IY") && param1.endsWith(")")) {
                int value = 0;
                if(param1.startsWith("(IY+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IY-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in INC: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)0x34, (byte)(value)};
            }
            
            // 16 Bit
            // INC ss
            if(isIn(param1, "BC,DE,HL,SP"))
                return new byte[]{(byte)(0x03 | (getRegisterNum16(param1) << 4))};
            
            // INC IX
            if(param1.equals("IX"))
                return new byte[]{(byte)0xDD, (byte)0x23};
            
            // INC IY
            if(param1.equals("IY"))
                return new byte[]{(byte)0xFD, (byte)0x23};
            
            throw new SyntaxErrorException("Error in INC mnemonic");
        }
    }

    /**
     * class DEC
     * impements the assembler of the DEC commands
     */
    public static class DEC extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("DEC command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // DEC r
            if(isIn(param1, "A,B,C,D,E,H,L"))
                return new byte[]{(byte)(0x05 | (getRegisterNum8(param1)<<3)) };
            
            // INC (HL)
            if(param1.equals("(HL)"))
                return new byte[]{(byte)0x35};
            
            // INC (IX+d)
            if(param1.startsWith("(IX") && param1.endsWith(")")) {
                int value = 0;
                if(param1.startsWith("(IX+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IX-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in DEC: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)0x35, (byte)(value)};
            }
            
            // INC (IY+d)
            if(param1.startsWith("(IY") && param1.endsWith(")")) {
                int value = 0;
                if(param1.startsWith("(IY+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IY-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in DEC: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)0x35, (byte)(value)};
            }
            
            // 16 Bit
            // DEC ss
            if(isIn(param1, "BC,DE,HL,SP"))
                return new byte[]{(byte)(0x0B | (getRegisterNum16(param1) << 4))};
            
            // DEC IX
            if(param1.equals("IX"))
                return new byte[]{(byte)0xDD, (byte)0x2B};
            
            // DEC IY
            if(param1.equals("IY"))
                return new byte[]{(byte)0xFD, (byte)0x2B};
            
            throw new SyntaxErrorException("Error in DEC mnemonic");
        }
    }
    
    /**
     * Class DAA
     * implement the assembler for the DAA command
     */
    public static class DAA extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x27};
        }
    }

    /**
     * Class CPL
     * implement the assembler for the CPL command
     */
    public static class CPL extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x2F};
        }
    }
    
    /**
     * Class NEG
     * implements the assembler for the NEG commands
     */
    public static class NEG extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("NEG command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // NEG A
            if(param1.equals("A"))
                return new byte[]{(byte)0xED, (byte)0x44};
            
            throw new SyntaxErrorException("Error in NEG mnemonic");
        }
    }

    /**
     * Class CCF
     * implement the assembler for the CCF command
     */
    public static class CCF extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x3F};
        }
    }
    
    /**
     * Class SCF
     * implement the assembler for the SCF command
     */
    public static class SCF extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x37};
        }
    }
    
    /**
     * Class NOP
     * implement the assembler for the NOP command
     */
    public static class NOP extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x00};
        }
    }
    
    /**
     * Class HALT
     * implement the assembler for the HALT command
     */
    public static class HALT extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x76};
        }
    }
    
    /**
     * Class DI
     * implement the assembler for the DI command
     */
    public static class DI extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0xF3};
        }
    }
    
    /**
     * Class EI
     * implement the assembler for the EI command
     */
    public static class EI extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0xFB};
        }
    }
    
    /**
     * Class EI
     * implement the assembler for the IM commands
     */
    public static class IM extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("IM command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // IM 0
            if(param1.equals("0"))
                return new byte[]{(byte)0xED, (byte)0x46};
            
            // IM 1
            if(param1.equals("1"))
                return new byte[]{(byte)0xED, (byte)0x56};
            
            // IM 2
            if(param1.equals("0"))
                return new byte[]{(byte)0xED, (byte)0x5E};
            
            throw new SyntaxErrorException("Error in IM mnemonic");
        }
    }
    
    /**
     * Class RLCA
     * implements the assembler for the RLCA command
     */
    public static class RLCA extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x07};
        }
    }
    
    /**
     * Class RLA
     * implements the assembler for the RLA command
     */
    public static class RLA extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x17};
        }
    }
    
    /**
     * Class RRCA
     * implements the assembler for the RRCA command
     */
    public static class RRCA extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x0F};
        }
    }
    
    /**
     * Class RRA
     * implements the assembler for the RRA command
     */
    public static class RRA extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0x1F};
        }
    }
    
    /**
     * Class RLC
     * implements the assembler for the RLC command
     */
    public static class RLC extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("RLC command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // RLC r
            if(isIn(param1, "A,B,C,D,E,H,L"))
                return new byte[]{(byte)0xCB, (byte)(getRegisterNum8(param1))};
            
            // RLC (HL)
            if(param1.equals("(HL)"))
                return new byte[]{(byte)0xCB, (byte)0x06};
            
            // RLC (IX+d)
            if(param1.startsWith("(IX") && param1.endsWith(")")) {
                int value = 0;
                if(param1.startsWith("(IX+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IX-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in RLC: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)0xCB, (byte)(value), (byte)0x06};
            }
            
            // RLC (IY+d)
            if(param1.startsWith("(IY") && param1.endsWith(")")) {
                int value = 0;
                if(param1.startsWith("(IY+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IY-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in RLC: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)0xCB, (byte)(value), (byte)0x06};
            }
            
            throw new SyntaxErrorException("Error in RLC mnemonic");
        }
    }
    
    /**
     * Class RL
     * implements the assembler for the RL command
     */
    public static class RL extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("RL command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // RL r
            if(isIn(param1, "A,B,C,D,E,H,L"))
                return new byte[]{(byte)0xCB, (byte)(0x10 | (getRegisterNum8(param1)))};
            
            // RL (HL)
            if(param1.equals("(HL)"))
                return new byte[]{(byte)0xCB, (byte)0x16};
            
            // RL (IX+d)
            if(param1.startsWith("(IX") && param1.endsWith(")")) {
                int value = 0;
                if(param1.startsWith("(IX+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IX-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in RL: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)0xCB, (byte)(value), (byte)0x16};
            }
            
            // RL (IY+d)
            if(param1.startsWith("(IY") && param1.endsWith(")")) {
                int value = 0;
                if(param1.startsWith("(IY+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IY-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in RL: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)0xCB, (byte)(value), (byte)0x16};
            }
            
            throw new SyntaxErrorException("Error in RL mnemonic");
        }
    }
    
    /**
     * Class RRC
     * implements the assembler for the RRC command
     */
    public static class RRC extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("RRC command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // RRC r
            if(isIn(param1, "A,B,C,D,E,H,L"))
                return new byte[]{(byte)0xCB, (byte)(0x08 | (getRegisterNum8(param1)))};
            
            // RRC (HL)
            if(param1.equals("(HL)"))
                return new byte[]{(byte)0xCB, (byte)0x0E};
            
            // RRC (IX+d)
            if(param1.startsWith("(IX") && param1.endsWith(")")) {
                int value = 0;
                if(param1.startsWith("(IX+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IX-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in RRC: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)0xCB, (byte)(value), (byte)0x0E};
            }
            
            // RRC (IY+d)
            if(param1.startsWith("(IY") && param1.endsWith(")")) {
                int value = 0;
                if(param1.startsWith("(IY+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IY-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in RRC: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)0xCB, (byte)(value), (byte)0x0E};
            }
            
            throw new SyntaxErrorException("Error in RRC mnemonic");
        }
    }
    
    /**
     * Class RR
     * implements the assembler for the RR command
     */
    public static class RR extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("RR command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // RR r
            if(isIn(param1, "A,B,C,D,E,H,L"))
                return new byte[]{(byte)0xCB, (byte)(0x18 | (getRegisterNum8(param1)))};
            
            // RR (HL)
            if(param1.equals("(HL)"))
                return new byte[]{(byte)0xCB, (byte)0x1E};
            
            // RR (IX+d)
            if(param1.startsWith("(IX") && param1.endsWith(")")) {
                int value = 0;
                if(param1.startsWith("(IX+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IX-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in RR: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)0xCB, (byte)(value), (byte)0x1E};
            }
            
            // RL (IY+d)
            if(param1.startsWith("(IY") && param1.endsWith(")")) {
                int value = 0;
                if(param1.startsWith("(IY+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IY-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in RR: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)0xCB, (byte)(value), (byte)0x1E};
            }
            
            throw new SyntaxErrorException("Error in RR mnemonic");
        }
    }
    
    /**
     * Class SLA
     * implements the assembler for the SLA command
     */
    public static class SLA extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SLA command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // SLA r
            if(isIn(param1, "A,B,C,D,E,H,L"))
                return new byte[]{(byte)0xCB, (byte)(0x20 | (getRegisterNum8(param1)))};
            
            // SLA (HL)
            if(param1.equals("(HL)"))
                return new byte[]{(byte)0xCB, (byte)0x26};
            
            // SLA (IX+d)
            if(param1.startsWith("(IX") && param1.endsWith(")")) {
                int value = 0;
                if(param1.startsWith("(IX+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IX-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in SLA: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)0xCB, (byte)(value), (byte)0x26};
            }
            
            // SLA (IY+d)
            if(param1.startsWith("(IY") && param1.endsWith(")")) {
                int value = 0;
                if(param1.startsWith("(IY+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IY-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in SLA: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)0xCB, (byte)(value), (byte)0x26};
            }
            
            throw new SyntaxErrorException("Error in SLA mnemonic");
        }
    }
    
    /**
     * Class SRA
     * implements the assembler for the SRA command
     */
    public static class SRA extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SRA command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // SRA r
            if(isIn(param1, "A,B,C,D,E,H,L"))
                return new byte[]{(byte)0xCB, (byte)(0x28 | (getRegisterNum8(param1)))};
            
            // SRA (HL)
            if(param1.equals("(HL)"))
                return new byte[]{(byte)0xCB, (byte)0x2E};
            
            // SRA (IX+d)
            if(param1.startsWith("(IX") && param1.endsWith(")")) {
                int value = 0;
                if(param1.startsWith("(IX+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IX-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in SRA: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)0xCB, (byte)(value), (byte)0x2E};
            }
            
            // SRA (IY+d)
            if(param1.startsWith("(IY") && param1.endsWith(")")) {
                int value = 0;
                if(param1.startsWith("(IY+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IY-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in SRA: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)0xCB, (byte)(value), (byte)0x2E};
            }
            
            throw new SyntaxErrorException("Error in SRA mnemonic");
        }
    }
    
    /**
     * Class SRL
     * implements the assembler for the SRL command
     */
    public static class SRL extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SRL command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // SRL r
            if(isIn(param1, "A,B,C,D,E,H,L"))
                return new byte[]{(byte)0xCB, (byte)(0x38 | (getRegisterNum8(param1)))};
            
            // SRL (HL)
            if(param1.equals("(HL)"))
                return new byte[]{(byte)0xCB, (byte)0x3E};
            
            // SRL (IX+d)
            if(param1.startsWith("(IX") && param1.endsWith(")")) {
                int value = 0;
                if(param1.startsWith("(IX+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IX-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in SRL: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)0xCB, (byte)(value), (byte)0x3E};
            }
            
            // SRL (IY+d)
            if(param1.startsWith("(IY") && param1.endsWith(")")) {
                int value = 0;
                if(param1.startsWith("(IY+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IY-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in SRL: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)0xCB, (byte)(value), (byte)0x3E};
            }
            
            throw new SyntaxErrorException("Error in SRL mnemonic");
        }
    }
    
    /**
     * Class RLD
     * implements the assembler for the RLD command
     */
    public static class RLD extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0xED, (byte)0x6F};
        }
    }
    
    /**
     * Class RRD
     * implements the assembler for the RRD command
     */
    public static class RRD extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            return new byte[]{(byte)0xED, (byte)0x67};
        }
    }
    
    /**
     * Class BIT
     * implements the assembler for the BIT command
     */
    public static class BIT extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("BIT command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("BIT command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            // BIT b, r
            if(isNumeric(param1) && isIn(param2, "A,B,C,D,E,H,L")) {
                int bit = getNumeric(param1);
                if((bit < 0) || (bit > 7))
                    throw new SyntaxErrorException("Bit number must be between 0 and 7 " + param1);
                return new byte[]{(byte)0xCB, (byte)(0x40 | (bit << 6) | (getRegisterNum8(param2)))};
            }
            
            // BIT b, (HL)
            if(isNumeric(param1) && param2.equals("(HL)")) {
                int bit = getNumeric(param1);
                if((bit < 0) || (bit > 7))
                    throw new SyntaxErrorException("Bit number must be between 0 and 7 " + param1);
                return new byte[]{(byte)0xCB, (byte)(0x46 | (bit << 6))};
            }
            
            // BIT b, (IX+d)
            if(isNumeric(param1) && param2.startsWith("(IX") && param2.endsWith(")")) {
                int value = 0;
                int bit = getNumeric(param1);
                
                if((bit < 0) || (bit > 7))
                    throw new SyntaxErrorException("Bit number must be between 0 and 7 " + param1);
                
                if(param1.startsWith("(IX+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IX-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in BIT: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)0xCB, (byte)(value), (byte)(0x46 | (bit << 3))};
            }
            
            // BIT b, (IY+d)
            if(isNumeric(param1) && param2.startsWith("(IY") && param2.endsWith(")")) {
                int value = 0;
                int bit = getNumeric(param1);
                
                if((bit < 0) || (bit > 7))
                    throw new SyntaxErrorException("Bit number must be between 0 and 7 " + param1);
                
                if(param1.startsWith("(IY+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IY-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in BIT: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)0xCB, (byte)(value), (byte)(0x46 | (bit << 3))};
            }
            
            throw new SyntaxErrorException("Error in BIT mnemonic");
        }
    }
    
    /**
     * Class SET
     * implements the assembler for the SET command
     */
    public static class SET extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SET command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("SET command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            // SET b, r
            if(isNumeric(param1) && isIn(param2, "A,B,C,D,E,H,L")) {
                int bit = getNumeric(param1);
                if((bit < 0) || (bit > 7))
                    throw new SyntaxErrorException("Bit number must be between 0 and 7 " + param1);
                return new byte[]{(byte)0xCB, (byte)(0xC0 | (bit << 6) | (getRegisterNum8(param2)))};
            }
            
            // SET b, (HL)
            if(isNumeric(param1) && param2.equals("(HL)")) {
                int bit = getNumeric(param1);
                if((bit < 0) || (bit > 7))
                    throw new SyntaxErrorException("Bit number must be between 0 and 7 " + param1);
                return new byte[]{(byte)0xCB, (byte)(0xC6 | (bit << 6))};
            }
            
            // SET b, (IX+d)
            if(isNumeric(param1) && param2.startsWith("(IX") && param2.endsWith(")")) {
                int value = 0;
                int bit = getNumeric(param1);
                
                if((bit < 0) || (bit > 7))
                    throw new SyntaxErrorException("Bit number must be between 0 and 7 " + param1);
                
                if(param1.startsWith("(IX+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IX-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in SET: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)0xCB, (byte)(value), (byte)(0xC6 | (bit << 3))};
            }
            
            // SET b, (IY+d)
            if(isNumeric(param1) && param2.startsWith("(IY") && param2.endsWith(")")) {
                int value = 0;
                int bit = getNumeric(param1);
                
                if((bit < 0) || (bit > 7))
                    throw new SyntaxErrorException("Bit number must be between 0 and 7 " + param1);
                
                if(param1.startsWith("(IY+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IY-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in SET: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)0xCB, (byte)(value), (byte)(0xC6 | (bit << 3))};
            }
            
            throw new SyntaxErrorException("Error in SET mnemonic");
        }
    }
    
    /**
     * Class RES
     * implements the assembler for the RES command
     */
    public static class RES extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("RES command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("RES command requires 2 parameters");
            String param2 = parameters.nextToken();
            
            // RES b, r
            if(isNumeric(param1) && isIn(param2, "A,B,C,D,E,H,L")) {
                int bit = getNumeric(param1);
                if((bit < 0) || (bit > 7))
                    throw new SyntaxErrorException("Bit number must be between 0 and 7 " + param1);
                return new byte[]{(byte)0xCB, (byte)(0x80 | (bit << 6) | (getRegisterNum8(param2)))};
            }
            
            // RES b, (HL)
            if(isNumeric(param1) && param2.equals("(HL)")) {
                int bit = getNumeric(param1);
                if((bit < 0) || (bit > 7))
                    throw new SyntaxErrorException("Bit number must be between 0 and 7 " + param1);
                return new byte[]{(byte)0xCB, (byte)(0x86 | (bit << 6))};
            }
            
            // RES b, (IX+d)
            if(isNumeric(param1) && param2.startsWith("(IX") && param2.endsWith(")")) {
                int value = 0;
                int bit = getNumeric(param1);
                
                if((bit < 0) || (bit > 7))
                    throw new SyntaxErrorException("Bit number must be between 0 and 7 " + param1);
                
                if(param1.startsWith("(IX+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IX-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in RES: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDD, (byte)0xCB, (byte)(value), (byte)(0x86 | (bit << 3))};
            }
            
            // RES b, (IY+d)
            if(isNumeric(param1) && param2.startsWith("(IY") && param2.endsWith(")")) {
                int value = 0;
                int bit = getNumeric(param1);
                
                if((bit < 0) || (bit > 7))
                    throw new SyntaxErrorException("Bit number must be between 0 and 7 " + param1);
                
                if(param1.startsWith("(IY+"))
                    value = getNumeric(param1.substring(4, param1.length()-1));
                else if(param1.startsWith("(IY-"))
                    value = -getNumeric(param1.substring(4, param1.length()-1));
                else
                    throw new SyntaxErrorException("Incorrect 1st parameter in RES: " + param1);
                
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xFD, (byte)0xCB, (byte)(value), (byte)(0x86 | (bit << 3))};
            }
            
            throw new SyntaxErrorException("Error in RES mnemonic");
        }
    }
    
    /**
     * Class JP
     * implements the assembler for the JP command
     */
    public static class JP extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JP command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            if(parameters.hasMoreElements()) return getOpCodesConditional(param1, parameters.nextToken(" ,"));
            
            // JP nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xC3, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            // JP (HL)
            if(param1.equals("(HC)"))
                return new byte[]{(byte)0xE9};
            
            // JP (IX)
            if(param1.equals("(IX)"))
                return new byte[]{(byte)0xDD, (byte)0xE9};
            
            // JP (IY)
            if(param1.equals("(IY)"))
                return new byte[]{(byte)0xFD, (byte)0xE9};
            
            throw new SyntaxErrorException("Error in JP mnemonic");
        }
        
        protected int getConditionNum(String condition) throws SyntaxErrorException {
            switch (condition) {
                case "NZ":
                    return 0x00;
                    
                case "Z":
                    return 0x01;
                    
                case "NC":
                    return 0x02;
                    
                case "C":
                    return 0x03;
                    
                case "PO":
                    return 0x04;
                    
                case "PE":
                    return 0x05;
                    
                case "P":
                case "NS":
                    return 0x06;
                    
                case "M":
                case "S":
                    return 0x07;
                    
                default:
                    throw new SyntaxErrorException("Condition " + condition + " unknown");
            }
        }
        
        protected byte[] getOpCodesConditional(String param1, String param2) throws SyntaxErrorException {
            // JP cc, nn
            
            if(isIn(param1, "NZ,Z,NC,C,PO,PE,P,NS,M,S") && isNumeric(param2)) {
                int address = getNumeric(param2);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param2 + " too large to fit in 16 bit");
                return new byte[]{(byte)(0xC2 | (getConditionNum(param1) << 3)), (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in JP mnemonic");
        }
    }
    
    /**
     * Class JPNZ
     * implements the assembler for conditional jump JPNZ / JP NZ
     */
    public static class JPNZ extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JPNZ command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // JPNZ nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xC2, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in JPNZ mnemonic");
        }
    }
    
    /**
     * Class JPZ
     * implements the assembler for conditional jump JPZ / JP Z
     */
    public static class JPZ extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JPZ command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // JPZ nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xCA, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in JPZ mnemonic");
        }
    }
    
    /**
     * Class JPNC
     * implements the assembler for conditional jump JPNC / JP NC
     */
    public static class JPNC extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JPNC command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // JPNC nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xD2, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in JPNC mnemonic");
        }
    }
    
    /**
     * Class JPC
     * implements the assembler for conditional jump JPC / JP C
     */
    public static class JPC extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JPC command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // JPC nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xDA, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in JPC mnemonic");
        }
    }
    
    /**
     * Class JPPO
     * implements the assembler for conditional jump JPPO / JP PO
     */
    public static class JPPO extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JPPO command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // JPPO nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xE2, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in JPPO mnemonic");
        }
    }
    
    /**
     * Class JPPE
     * implements the assembler for conditional jump JPPE / JP PE
     */
    public static class JPPE extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JPPE command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // JPPE nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xEA, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in JPPE mnemonic");
        }
    }
    
    /**
     * Class JPNS
     * implements the assembler for conditional jump JPNS / JP NS
     */
    public static class JPNS extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JPNS command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // JPNS nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xF2, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in JPNS mnemonic");
        }
    }
    
    /**
     * Class JPS
     * implements the assembler for conditional jump JPS / JP S
     */
    public static class JPS extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JPS command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // JPS nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xF2, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in JPS mnemonic");
        }
    }
    
    /**
     * Class JPP
     * implements the assembler for conditional jump JPP / JP P
     */
    public static class JPP extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JPP command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // JPP nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xF2, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in JPP mnemonic");
        }
    }
    
    /**
     * Class JPM
     * implements the assembler for conditional jump JPM / JP M
     */
    public static class JPM extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JPM command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // JPM nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xF2, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in JPM mnemonic");
        }
    }
    
    /**
     * Class JR
     * implements the assembler for the JR command
     */
    public static class JR extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JR command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            if(parameters.hasMoreElements())
                return getOpCodesConditional(param1, parameters.nextToken(" ,"));
            
            // JR e
            if(isNumeric(param1)) {
                int offset = getNumeric(param1);
                if((offset < -128) || (offset > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                return new byte[]{(byte)0x18, (byte)(offset)};
            }
            
            throw new SyntaxErrorException("Error in JR mnemonic");
        }
        
        protected byte[] getOpCodesConditional(String param1, String param2) throws SyntaxErrorException {
            int offset = getNumeric(param2);
                if((offset < -128) || (offset > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
            
            // JR C, e
            if(param1.equals("C"))
                return new byte[]{(byte)0x38, (byte)offset};
            
            // JR NC, e
            if(param1.equals("NC"))
                return new byte[]{(byte)0x30, (byte)offset};
            
            // JR Z, e
            if(param1.equals("Z"))
                return new byte[]{(byte)0x28, (byte)offset};
            
            // JR NZ, e
            if(param1.equals("NZ"))
                return new byte[]{(byte)0x20, (byte)offset};
            
            // JR Z, e
            if(param1.equals("Z"))
                return new byte[]{(byte)0x28, (byte)offset};
            
            throw new SyntaxErrorException("Error in JR mnemonic");
        }
    }
    
    /**
     * Class JRC
     * implements the assembler for the JRC command
     */
    public static class JRC extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JRC command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // JRC e
            if(isNumeric(param1)) {
                int offset = getNumeric(param1);
                if((offset < -128) || (offset > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                return new byte[]{(byte)0x38, (byte)(offset)};
            }
            
            throw new SyntaxErrorException("Error in JRC mnemonic");
        }
    }
    
    /**
     * Class JRNC
     * implements the assembler for the JRNC command
     */
    public static class JRNC extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JRNC command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // JRC e
            if(isNumeric(param1)) {
                int offset = getNumeric(param1);
                if((offset < -128) || (offset > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                return new byte[]{(byte)0x30, (byte)(offset)};
            }
            
            throw new SyntaxErrorException("Error in JRNC mnemonic");
        }
    }
    
    /**
     * Class JRZ
     * implements the assembler for the JRZ command
     */
    public static class JRZ extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JRZ command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // JRZ e
            if(isNumeric(param1)) {
                int offset = getNumeric(param1);
                if((offset < -128) || (offset > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                return new byte[]{(byte)0x28, (byte)(offset)};
            }
            
            throw new SyntaxErrorException("Error in JRZ mnemonic");
        }
    }
    
    /**
     * Class JRNZ
     * implements the assembler for the JRNZ command
     */
    public static class JRNZ extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("JRNZ command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // JRNZ e
            if(isNumeric(param1)) {
                int offset = getNumeric(param1);
                if((offset < -128) || (offset > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                return new byte[]{(byte)0x20, (byte)(offset)};
            }
            
            throw new SyntaxErrorException("Error in JRNZ mnemonic");
        }
    }
    
    /**
     * Class DJNZ
     * implements the assembler for the DJNZ command
     */
    public static class DJNZ extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("DJNZ command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // DJNZ e
            if(isNumeric(param1)) {
                int offset = getNumeric(param1);
                if((offset < -128) || (offset > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                return new byte[]{(byte)0x10, (byte)(offset)};
            }
            
            throw new SyntaxErrorException("Error in DJNZ mnemonic");
        }
    }
    
    /**
     * Class CALL
     * implements the assembler for the CALL command
     */
    public static class CALL extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CALL command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            if(parameters.hasMoreElements()) return getOpCodesConditional(param1, parameters.nextToken(" ,"));
            
            // CALL nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xCD, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in CALL mnemonic");
        }
        
        protected int getConditionNum(String condition) throws SyntaxErrorException {
            switch (condition) {
                case "NZ":
                    return 0x00;
                    
                case "Z":
                    return 0x01;
                    
                case "NC":
                    return 0x02;
                    
                case "C":
                    return 0x03;
                    
                case "PO":
                    return 0x04;
                    
                case "PE":
                    return 0x05;
                    
                case "P":
                case "NS":
                    return 0x06;
                    
                case "M":
                case "S":
                    return 0x07;
                    
                default:
                    throw new SyntaxErrorException("Condition " + condition + " unknown");
            }
        }
        
        protected byte[] getOpCodesConditional(String param1, String param2) throws SyntaxErrorException {
            // CALL cc, nn
            
            if(isIn(param1, "NZ,Z,NC,C,PO,PE,P,NS,M,S") && isNumeric(param2)) {
                int address = getNumeric(param2);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param2 + " too large to fit in 16 bit");
                return new byte[]{(byte)(0xC4 | (getConditionNum(param1) << 3)), (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in CALL mnemonic");
        }
    }
    
    /**
     * Class CALLNZ
     * implements the assembler for conditional jump CALLNZ / CALL NZ
     */
    public static class CALLNZ extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CALLNZ command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // JPNZ nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xC4, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in CALLNZ mnemonic");
        }
    }
    
    /**
     * Class CALLZ
     * implements the assembler for conditional jump CALLZ / CALL Z
     */
    public static class CALLZ extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CALLZ command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // CALLZ nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xCC, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in CALLZ mnemonic");
        }
    }
    
    /**
     * Class CALLNC
     * implements the assembler for conditional jump CALLNC / CALL NC
     */
    public static class CALLNC extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CALLNC command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // JPNC nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xD4, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in CALLNC mnemonic");
        }
    }
    
    /**
     * Class CALLC
     * implements the assembler for conditional jump CALLC / CALL C
     */
    public static class CALLC extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CALLC command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // CALLC nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xDC, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in CALLC mnemonic");
        }
    }
    
    /**
     * Class CALLPO
     * implements the assembler for conditional jump CALLPO / CALL PO
     */
    public static class CALLPO extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CALLPO command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // CALLPO nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xE4, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in CALLPO mnemonic");
        }
    }
    
    /**
     * Class CALLPE
     * implements the assembler for conditional jump CALLPE / CAL PE
     */
    public static class CALLPE extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CALLPE command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // CALLPE nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xEC, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in CALLPE mnemonic");
        }
    }
    
    /**
     * Class CALLNS
     * implements the assembler for conditional jump CALLNS / CALL NS
     */
    public static class CALLNS extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CALLNS command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // CALLNS nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xF4, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in CALLNS mnemonic");
        }
    }
    
    /**
     * Class CALLS
     * implements the assembler for conditional jump CALLS / CALL S
     */
    public static class CALLS extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CALLS command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // JPS nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xFC, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in CALLS mnemonic");
        }
    }
    
    /**
     * Class CALLP
     * implements the assembler for conditional jump CALLP / CALL P
     */
    public static class CALLP extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CALLP command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // JPP nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xF4, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in CALLP mnemonic");
        }
    }
    
    /**
     * Class CALLM
     * implements the assembler for conditional jump CALLM / CALL M
     */
    public static class CALLM extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("CALLM command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // JPM nn
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address < -32768) || (address > 65535))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 16 bit");
                return new byte[]{(byte)0xFC, (byte)(address & 0xFF), (byte)((address >> 8) & 0xFF)};
            }
            
            throw new SyntaxErrorException("Error in CALLM mnemonic");
        }
    }
    
    /**
     * Class RET
     * implements the assembler for the RET command
     */
    public static class RET extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(parameters.hasMoreElements()) return getOpCodesConditional(parameters.nextToken(" ,"));
            
            // RET
            return new byte[]{(byte)0xC9};
        }
        
        protected int getConditionNum(String condition) throws SyntaxErrorException {
            switch (condition) {
                case "NZ":
                    return 0x00;
                    
                case "Z":
                    return 0x01;
                    
                case "NC":
                    return 0x02;
                    
                case "C":
                    return 0x03;
                    
                case "PO":
                    return 0x04;
                    
                case "PE":
                    return 0x05;
                    
                case "P":
                case "NS":
                    return 0x06;
                    
                case "M":
                case "S":
                    return 0x07;
                    
                default:
                    throw new SyntaxErrorException("Condition " + condition + " unknown");
            }
        }
        
        public byte[] getOpCodesConditional(String param) throws SyntaxErrorException {
            if(isIn(param, "NZ,Z,NC,C,PO,PE,P,NS,M,S")) {
                return new byte[]{(byte)(0xC0 | (getConditionNum(param) << 3))};
            }
            
            throw new SyntaxErrorException("Error in RET mnemonic");
        }
    }
    
    /**
     * Class RETI
     * implements the assembler for the RETI command
     */
    public static class RETI extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            // RETI
            return new byte[]{(byte)0xED, (byte)0x4D};
        }
    }
    
    /**
     * Class RETN
     * implements the assembler for the RETN command
     */
    public static class RETN extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            // RETN
            return new byte[]{(byte)0xED, (byte)0x45};
        }
    }
    
    /**
     * Class RST
     * implements the assembler for the RST command
     */
    public static class RST extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("RST command requires 1 parameter");
            String param1 = parameters.nextToken(" ,");
            
            // RST p
            if(isNumeric(param1)) {
                int address = getNumeric(param1);
                if((address & 0xC7) != 0)
                    throw new SyntaxErrorException("numeric value in RST command must be a multiple of $08 (between $00 and $38) - " + param1);
                
                return new byte[]{(byte)(0xC7 | address)};
            }
            
            throw new SyntaxErrorException("Error in RST mnemonic");
        }
    }
    
    /**
     * Class IN
     * implements the assembler for the IN command
     */
    public static class IN extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("IN command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("IN command requires 2 parameters");
            String param2 = parameters.nextToken(" ,");
            
            // IN A, (n)
            if(param1.equals("A") && param2.startsWith("(") && param2.endsWith(")") && isNumeric(param2.substring(1, param2.length()-1))) {
                int value = getNumeric(param2.substring(1, param2.length()-1));
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xDB, (byte)(value)};
            }
            
            // IN r, (C)
            if(isIn(param1, "A,B,C,D,E,H,L") && param2.equals("(C)"))
                return new byte[]{(byte)0xED, (byte)(0x40 | (getRegisterNum8(param1) << 3))};
            
            throw new SyntaxErrorException("Error in IN mnemonic");
        }
    }
    
    /**
     * Class INI
     * implements the assembler for the INI command
     */
    public static class INI extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            // INI
            return new byte[]{(byte)0xED, (byte)0xA2};
        }
    }
    
    /**
     * Class INIR
     * implements the assembler for the INIR command
     */
    public static class INIR extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            // INI
            return new byte[]{(byte)0xED, (byte)0xB2};
        }
    }
    
    /**
     * Class IND
     * implements the assembler for the IND command
     */
    public static class IND extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            // INI
            return new byte[]{(byte)0xED, (byte)0xAA};
        }
    }
    
    /**
     * Class INDR
     * implements the assembler for the INDR command
     */
    public static class INDR extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            // INI
            return new byte[]{(byte)0xED, (byte)0xBA};
        }
    }
    
    /**
     * Class OUT
     * implements the assembler for the OUT command
     */
    public static class OUT extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("OUT command requires 2 parameters");
            String param1 = parameters.nextToken(" ,");
            if(!parameters.hasMoreElements()) throw new SyntaxErrorException("OUT command requires 2 parameters");
            String param2 = parameters.nextToken(" ,");
            
            // OUT (n), A
            if(param1.startsWith("(") && param1.endsWith(")") && isNumeric(param1.substring(1, param1.length()-1)) && param2.equals("A")) {
                int value = getNumeric(param2.substring(1, param2.length()-1));
                if((value < -128) || (value > 255))
                    throw new SyntaxErrorException("Offset value " + param1 + " too large to fit in 8 bit");
                
                return new byte[]{(byte)0xD3, (byte)(value)};
            }
            
            // OUT (C), r
            if(param1.equals("(C)") && isIn(param2, "A,B,C,D,E,H,L"))
                return new byte[]{(byte)0xED, (byte)(0x41 | (getRegisterNum8(param1) << 3))};
            
            throw new SyntaxErrorException("Error in OUT mnemonic");
        }
    }
    
    /**
     * Class OUTI
     * implements the assembler for the OUTI command
     */
    public static class OUTI extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            // OUTI
            return new byte[]{(byte)0xED, (byte)0xA3};
        }
    }
    
    /**
     * Class OTIR
     * implements the assembler for the OTIR command
     */
    public static class OTIR extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            // OTIR
            return new byte[]{(byte)0xED, (byte)0xB3};
        }
    }
    
    /**
     * Class OUTD
     * implements the assembler for the OUTD command
     */
    public static class OUTD extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            // INI
            return new byte[]{(byte)0xED, (byte)0xAB};
        }
    }
    
    /**
     * Class OTDR
     * implements the assembler for the OTDR command
     */
    public static class OTDR extends Z80Mnemonic {
        @Override
        public byte[] getOpCodes(StringTokenizer parameters) throws SyntaxErrorException {
            // INI
            return new byte[]{(byte)0xED, (byte)0xBB};
        }
    }
    
/* ==========================================================================
   =
   =
   =
   =
   =
   ========================================================================== */
    
    
    public Z80Debugger() {
        super();
    }
    
    public Z80Debugger(Memory memoryBlock, IO IOBlock) {
        super(memoryBlock, IOBlock);
    }
    
    public Z80Debugger(List<Memory> memoryBlocks, List<IO> IOBlocks) {
        super(memoryBlocks, IOBlocks);
    }
    
    /**
     * Returns a list with all register names
     * 
     * @return the name list (List&lt;String&gt;)
     */
    @Override
    public List<String> getRegisterNames() {
        ArrayList<String> retVal = new ArrayList<>();
        
        retVal.add("A");
        retVal.add("F");
        retVal.add("B");
        retVal.add("C");
        retVal.add("D");
        retVal.add("E");
        retVal.add("H");
        retVal.add("L");
        retVal.add("A'");
        retVal.add("F'");
        retVal.add("B'");
        retVal.add("C'");
        retVal.add("D'");
        retVal.add("E'");
        retVal.add("H'");
        retVal.add("L'");
        retVal.add("PC");
        retVal.add("SP");
        retVal.add("IX");
        retVal.add("IY");
        retVal.add("I");
        retVal.add("R");
        
        return retVal;
    }
    
    protected void checkValueSize(long value, int bitSize) throws IllegalRegisterValueException {
        switch (bitSize) {
            case 1:
                if((value < 0) || (value > 1))
                    throw new IllegalRegisterValueException("Value " + value + " too large to fit in " + bitSize + " bit(s)");
                break;
                
            case 8:
                if((value < -128) || (value > 255))
                    throw new IllegalRegisterValueException("Value " + value + " too large to fit in " + bitSize + " bit(s)");
                break;
                
            case 16:
                if((value < -32768) || (value > 65535))
                    throw new IllegalRegisterValueException("Value " + value + " too large to fit in " + bitSize + " bit(s)");
                break;
        }
    }
    
    /**
     * Sets a register value
     * 
     * @param registerName (String) the register name
     * @param registerValue (long) the new register value
     * @throws IllegalRegisterException if the register does not exist
     * @throws IllegalRegisterValueException if the value does not fit
     */
    @Override
    public void setRegisterValue(String registerName, long registerValue) throws IllegalRegisterException, IllegalRegisterValueException {
        switch (registerName) {
            case "A":
                checkValueSize(registerValue , 8);
                this.regA = (byte)registerValue;
                break;
                
            case "F":
                checkValueSize(registerValue , 8);
                this.regF = (byte)registerValue;
                break;
                
            case "B":
                checkValueSize(registerValue , 8);
                this.regB = (byte)registerValue;
                break;
                
            case "C":
                checkValueSize(registerValue , 8);
                this.regC = (byte)registerValue;
                break;
                
            case "D":
                checkValueSize(registerValue , 8);
                this.regD = (byte)registerValue;
                break;
                
            case "E":
                checkValueSize(registerValue , 8);
                this.regE = (byte)registerValue;
                break;
                
            case "H":
                checkValueSize(registerValue , 8);
                this.regH = (byte)registerValue;
                break;
                
            case "L":
                checkValueSize(registerValue , 8);
                this.regL = (byte)registerValue;
                break;
                
            case "A'":
                checkValueSize(registerValue , 8);
                this.regA2 = (byte)registerValue;
                break;
                
            case "F'":
                checkValueSize(registerValue , 8);
                this.regF2 = (byte)registerValue;
                break;
                
            case "B'":
                checkValueSize(registerValue , 8);
                this.regB2 = (byte)registerValue;
                break;
                
            case "C'":
                checkValueSize(registerValue , 8);
                this.regC2 = (byte)registerValue;
                break;
                
            case "D'":
                checkValueSize(registerValue , 8);
                this.regD2 = (byte)registerValue;
                break;
                
            case "E'":
                checkValueSize(registerValue , 8);
                this.regE2 = (byte)registerValue;
                break;
                
            case "H'":
                checkValueSize(registerValue , 8);
                this.regH2 = (byte)registerValue;
                break;
                
            case "L'":
                checkValueSize(registerValue , 8);
                this.regL2 = (byte)registerValue;
                break;
                
            case "PC":
                checkValueSize(registerValue, 16);
                this.regPC = (short)registerValue;
                break;
                
            case "SP":
                checkValueSize(registerValue, 16);
                this.regSP = (short)registerValue;
                break;
                
            case "IX":
                checkValueSize(registerValue, 16);
                this.regIX = (short)registerValue;
                break;
                
            case "IY":
                checkValueSize(registerValue, 16);
                this.regIY = (short)registerValue;
                break;
                
            case "BC":
                checkValueSize(registerValue, 16);
                try {
                    this.setRegister16((byte)0x00, (short)registerValue);
                }
                catch (OpCodeException ex) {
                    throw new IllegalRegisterException("Error setting register BC");
                }
                
            case "DE":
                checkValueSize(registerValue, 16);
                try {
                    this.setRegister16((byte)0x01, (short)registerValue);
                }
                catch (OpCodeException ex) {
                    throw new IllegalRegisterException("Error setting register BC");
                }
                
            case "HL":
                checkValueSize(registerValue, 16);
                try {
                    this.setRegister16((byte)0x02, (short)registerValue);
                }
                catch (OpCodeException ex) {
                    throw new IllegalRegisterException("Error setting register BC");
                }
                
            default:
                throw new IllegalRegisterException("Register " + registerName + " does not exist");
        }
    }
    
    /**
     * Returns a register value
     * 
     * @param registerName (String) the register name
     * @throws IllegalRegisterException if the register does not exist
     */
    @Override
    public long getRegisterValue(String registerName) throws IllegalRegisterException {
        switch (registerName) {
            case "A":
                return Byte.toUnsignedLong(this.regA);
                
            case "F":
                return Byte.toUnsignedLong(this.regF);
                
            case "B":
                return Byte.toUnsignedLong(this.regB);
                
            case "C":
                return Byte.toUnsignedLong(this.regC);
                
            case "D":
                return Byte.toUnsignedLong(this.regD);
                
            case "E":
                return Byte.toUnsignedLong(this.regE);
                
            case "H":
                return Byte.toUnsignedLong(this.regH);
                
            case "L":
                return Byte.toUnsignedLong(this.regL);
                
            case "A'":
                return Byte.toUnsignedLong(this.regA2);
                
            case "F'":
                return Byte.toUnsignedLong(this.regF2);
                
            case "B'":
                return Byte.toUnsignedLong(this.regB2);
                
            case "C'":
                return Byte.toUnsignedLong(this.regC2);
                
            case "D'":
                return Byte.toUnsignedLong(this.regD2);
                
            case "E'":
                return Byte.toUnsignedLong(this.regE2);
                
            case "H'":
                return Byte.toUnsignedLong(this.regH2);
                
            case "L'":
                return Byte.toUnsignedLong(this.regL2);
                
            case "I":
                return Byte.toUnsignedInt(this.regI);
                
            case "R":
                return Byte.toUnsignedInt(this.regR);
                
            case "PC":
                return Short.toUnsignedLong(this.regPC);
                
            case "SP":
                return Short.toUnsignedLong(this.regSP);
                
            case "IX":
                return Short.toUnsignedLong(this.regIX);
                
            case "IY":
                return Short.toUnsignedLong(this.regIY);
                
            case "BC":
                try {
                    return Short.toUnsignedLong(this.getRegister16((byte)0x00));
                }
                catch (OpCodeException ex) {
                    throw new IllegalRegisterException(ex.getMessage());
                }
                
            case "DE":
                try {
                    return Short.toUnsignedLong(this.getRegister16((byte)0x01));
                }
                catch (OpCodeException ex) {
                    throw new IllegalRegisterException(ex.getMessage());
                }
                
                
            case "HL":
                try {
                    return Short.toUnsignedLong(this.getRegister16((byte)0x02));
                }
                catch (OpCodeException ex) {
                    throw new IllegalRegisterException(ex.getMessage());
                }
                
            default:
                throw new IllegalRegisterException("Register " + registerName + " does not exist");
        }
    }
    
    @Override
    public long getProgramCounter() {
        return Short.toUnsignedLong(this.regPC);
    }
    
    @Override
    public void setProgramCounter(long programCounter) throws IllegalRegisterValueException {
        if((programCounter < -32768) || (programCounter >= 65535))
            throw new IllegalRegisterValueException("Value " + programCounter + " too large to fit in program counter");
        
        this.regPC = (short)programCounter;
    }

    @Override
    public int getRegisterSize(String registerName) throws IllegalRegisterException {
        switch (registerName) {
            case "A":
            case "F":
            case "B":
            case "C":
            case "D":
            case "E":
            case "H":
            case "L":
            case "A'":
            case "F'":
            case "B'":
            case "C'":
            case "D'":
            case "E'":
            case "H'":
            case "L'":
            case "I":
            case "R":
                return 8;
                
            case "PC":
            case "SP":
            case "IX":
            case "IY":
            case "BC":
            case "DE":
            case "HL":
                return 16;
                
            default:
                throw new IllegalRegisterException("Register " + registerName + " does not exist");
        }
    }
    /**
     * Return the code length 
     * 
     * @param address
     * @return
     * @throws MemoryException
     * @throws OpCodeException 
     */
    @Override
    public long getCodeLength(long address) throws MemoryException, OpCodeException {
        return getCodeAndLength(address).getCodeLength();
    }

    /**
     * Return the op code
     * 
     * @param address
     * @return
     * @throws MemoryException
     * @throws OpCodeException 
     */
    @Override
    public String getCode(long address) throws MemoryException, OpCodeException {
        return getCodeAndLength(address).getCode();
    }

    @Override
    public byte readMemoryByte(long address) throws MemoryException {
        return this.readMemory8(address);
    }
    
    @Override
    public void writeMemoryByte(long address, byte value) throws MemoryException {
        this.writeMemory8(address, value);
    }
    
    protected String getRegisterName8(int registerNum) throws OpCodeException {
        switch (registerNum) {
            case 0x00:
                return "B";
                
            case 0x01:
                return "C";
                
            case 0x02:
                return "D";
                
            case 0x03:
                return "E";
                
            case 0x04:
                return "H";
                
            case 0x05:
                return "L";
                
            case 0x07:
                return "A";

            default:
                throw new OpCodeException("Register number " + registerNum + " does not exist");
        }
    }
    
    protected String getRegisterName16(int registerNum) throws OpCodeException {
        switch (registerNum) {
            case 0x00:
                return "BC";
                
            case 0x01:
                return "DE";
                
            case 0x02:
                return "HL";
                
            case 0x03:
                return "SP";
                
            default:
                throw new OpCodeException("16 bit Register number " + registerNum + " does not exist");
        }
    }
    
    protected String getConditionName(int condition) throws OpCodeException {
        switch (condition) {
            case 0x00:
                return "NZ";
                
            case 0x01:
                return "Z";
                
            case 0x02:
                return "NC";
                
            case 0x03:
                return "C";
                
            case 0x04:
                return "PO";
                
            case 0x05:
                return "PE";
                
            case 0x06:
                return "NS";
                
            case 0x07:
                return "S";
                
            default:
                throw new OpCodeException("Condition code " + condition + " unknown");
        }
    }
    
    protected CodeAndLength getCodeAndLengthCB(long address) throws MemoryException, OpCodeException {
        byte byte2 = this.readMemory8(address);
        
        switch (Byte.toUnsignedInt(byte2)) {
            case 0x00:  // RLC B
            case 0x01:  // RLC C
            case 0x02:  // RLC D
            case 0x03:  // RLC E
            case 0x04:  // RLC H
            case 0x05:  // RLC L
            case 0x07:  // RLC A
                return new CodeAndLength(2, "RLC " + getRegisterName8((byte2 & 0x07)));
                
            case 0x06:  // RLC (HL)
                return new CodeAndLength(2, "RLC (HL)");
                
            case 0x08:  // RRC B
            case 0x09:  // RRC C
            case 0x0A:  // RRC D
            case 0x0B:  // RRC E
            case 0x0C:  // RRC H
            case 0x0D:  // RRC L
            case 0x0F:  // RRC A
                return new CodeAndLength(2, "RRC " + getRegisterName8((byte2 & 0x07)));
                
            case 0x0E:  // RRC (HL)
                return new CodeAndLength(2, "RRC (HL)");
                
            case 0x10:  // RL B
            case 0x11:  // RL C
            case 0x12:  // RL D
            case 0x13:  // RL E
            case 0x14:  // RL H
            case 0x15:  // RL L
            case 0x17:  // RL A
                return new CodeAndLength(2, "RL " + getRegisterName8((byte2 & 0x07)));
                
            case 0x16:  // RL (HL)
                return new CodeAndLength(2, "RL (HL)");
                
            case 0x18:  // RR B
            case 0x19:  // RR C
            case 0x1A:  // RR D
            case 0x1B:  // RR E
            case 0x1C:  // RR H
            case 0x1D:  // RR L
            case 0x1F:  // RR A
                return new CodeAndLength(2, "RR " + getRegisterName8((byte2 & 0x07)));
                
            case 0x1E:  // RR (HL)
                return new CodeAndLength(2, "RR (HL)");
                
            case 0x20:  // SLA B
            case 0x21:  // SLA C
            case 0x22:  // SLA D
            case 0x23:  // SLA E
            case 0x24:  // SLA H
            case 0x25:  // SLA L
            case 0x27:  // SLA A
                return new CodeAndLength(2, "SLA " + getRegisterName8((byte2 & 0x07)));
                
            case 0x26:  // SLA (HL)
                return new CodeAndLength(2, "SLA (HL)");
                
            case 0x28:  // SRA B
            case 0x29:  // SRA C
            case 0x2A:  // SRA D
            case 0x2B:  // SRA E
            case 0x2C:  // SRA H
            case 0x2D:  // SRA L
            case 0x2F:  // SRA A
                return new CodeAndLength(2, "SRA " + getRegisterName8((byte2 & 0x07)));
                
            case 0x2E:  // SRA (HL)
                return new CodeAndLength(2, "SRA (HL)");
                
            case 0x38:  // SRL B
            case 0x39:  // SRL C
            case 0x3A:  // SRL D
            case 0x3B:  // SRL E
            case 0x3C:  // SRL H
            case 0x3D:  // SRL L
            case 0x3F:  // SRL A
                return new CodeAndLength(2, "SRL " + getRegisterName8((byte2 & 0x07)));
                
            case 0x3E:  // SRL (HL)
                return new CodeAndLength(2, "SRL (HL)");
                
            case 0x40:  // BIT 0, B
            case 0x41:  // BIT 0, C
            case 0x42:  // BIT 0, D
            case 0x43:  // BIT 0, E
            case 0x44:  // BIT 0, H
            case 0x45:  // BIT 0, L
            case 0x47:  // BIT 0, A
            case 0x48:  // BIT 1, B
            case 0x49:  // BIT 1, C
            case 0x4A:  // BIT 1, D
            case 0x4B:  // BIT 1, E
            case 0x4C:  // BIT 1, H
            case 0x4D:  // BIT 1, L
            case 0x4F:  // BIT 1, A
            case 0x50:  // BIT 2, B
            case 0x51:  // BIT 2, C
            case 0x52:  // BIT 2, D
            case 0x53:  // BIT 2, E
            case 0x54:  // BIT 2, H
            case 0x55:  // BIT 2, L
            case 0x57:  // BIT 2, A
            case 0x58:  // BIT 3, B
            case 0x59:  // BIT 3, C
            case 0x5A:  // BIT 3, D
            case 0x5B:  // BIT 3, E
            case 0x5C:  // BIT 3, H
            case 0x5D:  // BIT 3, L
            case 0x5F:  // BIT 3, A
            case 0x60:  // BIT 4, B
            case 0x61:  // BIT 4, C
            case 0x62:  // BIT 4, D
            case 0x63:  // BIT 4, E
            case 0x64:  // BIT 4, H
            case 0x65:  // BIT 4, L
            case 0x67:  // BIT 4, A
            case 0x68:  // BIT 5, B
            case 0x69:  // BIT 5, C
            case 0x6A:  // BIT 5, D
            case 0x6B:  // BIT 5, E
            case 0x6C:  // BIT 5, H
            case 0x6D:  // BIT 5, L
            case 0x6F:  // BIT 5, A
            case 0x70:  // BIT 6, B
            case 0x71:  // BIT 6, C
            case 0x72:  // BIT 6, D
            case 0x73:  // BIT 6, E
            case 0x74:  // BIT 6, H
            case 0x75:  // BIT 6, L
            case 0x77:  // BIT 6, A
            case 0x78:  // BIT 7, B
            case 0x79:  // BIT 7, C
            case 0x7A:  // BIT 7, D
            case 0x7B:  // BIT 7, E
            case 0x7C:  // BIT 7, H
            case 0x7D:  // BIT 7, L
            case 0x7F:  // BIT 7, A
                return new CodeAndLength(2, "BIT " + Integer.toString((byte2 & 0x38) >> 3) + ", " + getRegisterName8((byte2 & 0x07)));
                
            case 0x46:  // BIT 0, (HL)
            case 0x4E:  // BIT 1, (HL)
            case 0x56:  // BIT 2, (HL)
            case 0x5E:  // BIT 3, (HL)
            case 0x66:  // BIT 4, (HL)
            case 0x6E:  // BIT 5, (HL)
            case 0x76:  // BIT 6, (HL)
            case 0x7E:  // BIT 7, (HL)
                return new CodeAndLength(2, "BIT " + Integer.toString((byte2 & 0x38) >> 3) + ", (HL)");
                
            case 0x80:  // RES 0, B
            case 0x81:  // RES 0, C
            case 0x82:  // RES 0, D
            case 0x83:  // RES 0, E
            case 0x84:  // RES 0, H
            case 0x85:  // RES 0, L
            case 0x87:  // RES 0, A
            case 0x88:  // RES 1, B
            case 0x89:  // RES 1, C
            case 0x8A:  // RES 1, D
            case 0x8B:  // RES 1, E
            case 0x8C:  // RES 1, H
            case 0x8D:  // RES 1, L
            case 0x8F:  // RES 1, A
            case 0x90:  // RES 2, B
            case 0x91:  // RES 2, C
            case 0x92:  // RES 2, D
            case 0x93:  // RES 2, E
            case 0x94:  // RES 2, H
            case 0x95:  // RES 2, L
            case 0x97:  // RES 2, A
            case 0x98:  // RES 3, B
            case 0x99:  // RES 3, C
            case 0x9A:  // RES 3, D
            case 0x9B:  // RES 3, E
            case 0x9C:  // RES 3, H
            case 0x9D:  // RES 3, L
            case 0x9F:  // RES 3, A
            case 0xA0:  // RES 4, B
            case 0xA1:  // RES 4, C
            case 0xA2:  // RES 4, D
            case 0xA3:  // RES 4, E
            case 0xA4:  // RES 4, H
            case 0xA5:  // RES 4, L
            case 0xA7:  // RES 4, A
            case 0xA8:  // RES 5, B
            case 0xA9:  // RES 5, C
            case 0xAA:  // RES 5, D
            case 0xAB:  // RES 5, E
            case 0xAC:  // RES 5, H
            case 0xAD:  // RES 5, L
            case 0xAF:  // RES 5, A
            case 0xB0:  // RES 6, B
            case 0xB1:  // RES 6, C
            case 0xB2:  // RES 6, D
            case 0xB3:  // RES 6, E
            case 0xB4:  // RES 6, H
            case 0xB5:  // RES 6, L
            case 0xB7:  // RES 6, A
            case 0xB8:  // RES 7, B
            case 0xB9:  // RES 7, C
            case 0xBA:  // RES 7, D
            case 0xBB:  // RES 7, E
            case 0xBC:  // RES 7, H
            case 0xBD:  // RES 7, L
            case 0xBF:  // RES 7, A
                return new CodeAndLength(2, "RES " + Integer.toString((byte2 & 0x38) >> 3) + ", " + getRegisterName8((byte2 & 0x07)));
                
            case 0x86:  // RES 0, (HL)
            case 0x8E:  // RES 1, (HL)
            case 0x96:  // RES 2, (HL)
            case 0x9E:  // RES 3, (HL)
            case 0xA6:  // RES 4, (HL)
            case 0xAE:  // RES 5, (HL)
            case 0xB6:  // RES 6, (HL)
            case 0xBE:  // RES 7, (HL)
                return new CodeAndLength(2, "RES " + Integer.toString((byte2 & 0x38) >> 3) + ", (HL)");
                
            case 0xC0:  // SET 0, B
            case 0xC1:  // SET 0, C
            case 0xC2:  // SET 0, D
            case 0xC3:  // SET 0, E
            case 0xC4:  // SET 0, H
            case 0xC5:  // SET 0, L
            case 0xC7:  // SET 0, A
            case 0xC8:  // SET 1, B
            case 0xC9:  // SET 1, C
            case 0xCA:  // SET 1, D
            case 0xCB:  // SET 1, E
            case 0xCC:  // SET 1, H
            case 0xCD:  // SET 1, L
            case 0xCF:  // SET 1, A
            case 0xD0:  // SET 2, B
            case 0xD1:  // SET 2, C
            case 0xD2:  // SET 2, D
            case 0xD3:  // SET 2, E
            case 0xD4:  // SET 2, H
            case 0xD5:  // SET 2, L
            case 0xD7:  // SET 2, A
            case 0xD8:  // SET 3, B
            case 0xD9:  // SET 3, C
            case 0xDA:  // SET 3, D
            case 0xDB:  // SET 3, E
            case 0xDC:  // SET 3, H
            case 0xDD:  // SET 3, L
            case 0xDF:  // SET 3, A
            case 0xE0:  // SET 4, B
            case 0xE1:  // SET 4, C
            case 0xE2:  // SET 4, D
            case 0xE3:  // SET 4, E
            case 0xE4:  // SET 4, H
            case 0xE5:  // SET 4, L
            case 0xE7:  // SET 4, A
            case 0xE8:  // SET 5, B
            case 0xE9:  // SET 5, C
            case 0xEA:  // SET 5, D
            case 0xEB:  // SET 5, E
            case 0xEC:  // SET 5, H
            case 0xED:  // SET 5, L
            case 0xEF:  // SET 5, A
            case 0xF0:  // SET 6, B
            case 0xF1:  // SET 6, C
            case 0xF2:  // SET 6, D
            case 0xF3:  // SET 6, E
            case 0xF4:  // SET 6, H
            case 0xF5:  // SET 6, L
            case 0xF7:  // SET 6, A
            case 0xF8:  // SET 7, B
            case 0xF9:  // SET 7, C
            case 0xFA:  // SET 7, D
            case 0xFB:  // SET 7, E
            case 0xFC:  // SET 7, H
            case 0xFD:  // SET 7, L
            case 0xFF:  // SET 7, A
                return new CodeAndLength(2, "SET " + Integer.toString((byte2 & 0x38) >> 3) + ", " + getRegisterName8((byte2 & 0x07)));
                
            case 0xC6:  // SET 0, (HL)
            case 0xCE:  // SET 1, (HL)
            case 0xD6:  // SET 2, (HL)
            case 0xDE:  // SET 3, (HL)
            case 0xE6:  // SET 4, (HL)
            case 0xEE:  // SET 5, (HL)
            case 0xF6:  // SET 6, (HL)
            case 0xFE:  // SET 7, (HL)
                return new CodeAndLength(2, "SET " + Integer.toString((byte2 & 0x38) >> 3) + ", (HL)");
                
            default:
                throw new OpCodeException("Op Code CB " + byte2 + " invalid");
        }
    }
    
    protected CodeAndLength getCodeAndLengthDDCB(long address, String IXIY) throws MemoryException, OpCodeException {
        int dd = Byte.toUnsignedInt(this.readMemory8(address));
        int byte3 = Byte.toUnsignedInt(this.readMemory8(address+1));
        
        switch (byte3) {
            case 0x06:  // RLC (In+d)
                return new CodeAndLength(4, "RLC (" + IXIY + "+$" + Integer.toHexString(dd) + ")");
                
            case 0x0E:  // RRC (In+d)
                return new CodeAndLength(4, "RRC (" + IXIY + "+$" + Integer.toHexString(dd) + ")");
                
            case 0x16:  // RL (In+d)
                return new CodeAndLength(4, "RL (" + IXIY + "+$" + Integer.toHexString(dd) + ")");
                
            case 0x1E:  // RR (In+d)
                return new CodeAndLength(4, "RR (" + IXIY + "+$" + Integer.toHexString(dd) + ")");
                
            case 0x26:  // SLA (In+d)
                return new CodeAndLength(4, "SLA (" + IXIY + "+$" + Integer.toHexString(dd) + ")");
                
            case 0x2E:  // SRA (In+d)
                return new CodeAndLength(4, "SRA (" + IXIY + "+$" + Integer.toHexString(dd) + ")");
                
            case 0x3E:  // SRL (In+d)
                return new CodeAndLength(4, "SRL (" + IXIY + "+$" + Integer.toHexString(dd) + ")");
                
            case 0x46:  // BIT 0, (In+d)
            case 0x4E:  // BIT 1, (In+d)
            case 0x56:  // BIT 2, (In+d)
            case 0x5E:  // BIT 3, (In+d)
            case 0x66:  // BIT 4, (In+d)
            case 0x6E:  // BIT 5, (In+d)
            case 0x76:  // BIT 6, (In+d)
            case 0x7E:  // BIT 7, (In+d)
                return new CodeAndLength(4, "BIT " + Integer.toString((byte3 & 0x38) >> 3) + ", (" + IXIY + "+$" + Integer.toHexString(dd) + ")");
                
            case 0x86:  // BIT 0, (In+d)
            case 0x8E:  // BIT 1, (In+d)
            case 0x96:  // BIT 2, (In+d)
            case 0x9E:  // BIT 3, (In+d)
            case 0xA6:  // BIT 4, (In+d)
            case 0xAE:  // BIT 5, (In+d)
            case 0xB6:  // BIT 6, (In+d)
            case 0xBE:  // BIT 7, (In+d)
                return new CodeAndLength(4, "RES " + Integer.toString((byte3 & 0x38) >> 3) + ", (" + IXIY + "+$" + Integer.toHexString(dd) + ")");
                
            case 0xC6:  // BIT 0, (In+d)
            case 0xCE:  // BIT 1, (In+d)
            case 0xD6:  // BIT 2, (In+d)
            case 0xDE:  // BIT 3, (In+d)
            case 0xE6:  // BIT 4, (In+d)
            case 0xEE:  // BIT 5, (In+d)
            case 0xF6:  // BIT 6, (In+d)
            case 0xFE:  // BIT 7, (In+d)
                return new CodeAndLength(4, "SET " + Integer.toString((byte3 & 0x38) >> 3) + ", (" + IXIY + "+$" + Integer.toHexString(dd) + ")");
                
            default:
                throw new OpCodeException("Op code DD, CB, d, " + byte3 + "invalid");
        }
    }
    
    protected CodeAndLength getCodeAndLengthDD(long address, String IXIY) throws MemoryException, OpCodeException {
        byte byte2 = this.readMemory8(address);
        int nn;
        int dd;
        
        switch (Byte.toUnsignedInt(byte2)) {
            case 0x09:  // ADD In, BC
                return new CodeAndLength(2, "ADD " + IXIY + ", BC");
                
            case 0x19:  // ADD In, DE
                return new CodeAndLength(2, "ADD " + IXIY + ", DE");
                
            case 0x21:  // LD In, nn
                nn = Short.toUnsignedInt(this.readMemory16(address+1));
                return new CodeAndLength(4, "LD " + IXIY + ", $" + Integer.toHexString(nn));
                
            case 0x22:  // LD (nn), In
                nn = Short.toUnsignedInt(this.readMemory16(address+1));
                return new CodeAndLength(4, "LD ($" + Integer.toHexString(nn) + "), " + IXIY);
                
            case 0x23:  // INC In
                return new CodeAndLength(2, "INC " + IXIY);
                
            case 0x29:  // ADD In, In
                return new CodeAndLength(2, "ADD " + IXIY + ", " + IXIY);
                
            case 0x2A:  // LD In, (nn)
                nn = Short.toUnsignedInt(this.readMemory16(address+1));
                return new CodeAndLength(4, "LD " + IXIY + ", ($" + Integer.toHexString(nn) + ")");
                
            case 0x2B:  // DEC In
                return new CodeAndLength(2, "DEC " + IXIY);
                
            case 0x34:  // INC (In+d)
                nn = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(3, "INC (" + IXIY + "+$" + Integer.toHexString(nn) + ")");
                
            case 0x35:  // DEC (In+d)
                nn = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(3, "DEC (" + IXIY + "+$" + Integer.toHexString(nn) + ")");
                
            case 0x36:  // LD (In+d), n
                dd = Byte.toUnsignedInt(this.readMemory8(address+1));
                nn = Byte.toUnsignedInt(this.readMemory8(address+2));
                return new CodeAndLength(4, "LD (" + IXIY + "+$" + Integer.toHexString(dd) + "), $" + Integer.toHexString(nn));
                
            case 0x39:  // ADD In, SP
                return new CodeAndLength(2, "ADD " + IXIY + ", SP");
                
            case 0x46:  // LD B, (In+d)
            case 0x4E:  // LD C, (In+d)
            case 0x56:  // LD D, (In+d)
            case 0x5E:  // LD E, (In+d)
            case 0x66:  // LD H, (In+d)
            case 0x6E:  // LD L, (In+d)
            case 0x7E:  // LD A, (In+d)
                dd = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(3, "LD " + getRegisterName8((byte2 & 0x38) >> 3) + ", (" + IXIY + "+$" + Integer.toHexString(dd) + ")");
                
            case 0x70:  // LD (In+d), B
            case 0x71:  // LD (In+d), C
            case 0x72:  // LD (In+d), D
            case 0x73:  // LD (In+d), E
            case 0x74:  // LD (In+d), H
            case 0x75:  // LD (In+d), L
            case 0x77:  // LD (In+d), A
                dd = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(3, "LD (" + IXIY + "+$" + Integer.toHexString(dd) + "), " + getRegisterName8((byte2 & 0x07)));
                
            case 0x86:  // ADD A, (In+d)
                dd = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(3, "ADD A, (" + IXIY + "+$" + Integer.toHexString(dd) + ")");
                
            case 0x8E:  // ADC A, (In+d)                
                dd = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(3, "ADC A, (" + IXIY + "+$" + Integer.toHexString(dd) + ")");
                
            case 0x96:  // SUB A, (In+d)
                dd = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(3, "SUB A, (" + IXIY + "+$" + Integer.toHexString(dd) + ")");
                
            case 0x9E:  // SBC A, (In+d)
                dd = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(3, "SBC A, (" + IXIY + "+$" + Integer.toHexString(dd) + ")");
                
            case 0xA6:  // AND A, (In+d)
                dd = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(3, "AND A, (" + IXIY + "+$" + Integer.toHexString(dd) + ")");
                
            case 0xAE:  // XOR A, (In+d)
                dd = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(3, "XOR A, (" + IXIY + "+$" + Integer.toHexString(dd) + ")");
                
            case 0xB6:  // OR A, (In+d)
                dd = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(3, "OR A, (" + IXIY + "+$" + Integer.toHexString(dd) + ")");
                
            case 0xBE:  // CP A, (In+d)
                dd = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(3, "CP A, (" + IXIY + "+$" + Integer.toHexString(dd) + ")");
                
            case 0xCB:  // sub Opcode CB
                return getCodeAndLengthDDCB(address+1, IXIY);
                
            case 0xE1:  // POP In
                return new CodeAndLength(2, "POP " + IXIY);
                
            case 0xE3:  // EX (SP), In
                return new CodeAndLength(2, "EX (SP), " + IXIY);
                
            case 0xE5:  // PUSH In
                return new CodeAndLength(2, "PUSH " + IXIY);
                
            case 0xE9:  // JP (In)
                return new CodeAndLength(2, "JP (" + IXIY + ")");
                
            case 0xF9:  // LD SP, In
                return new CodeAndLength(2, "LD SP, " + IXIY);
                
            default:
                throw new OpCodeException("Op Code DD " + byte2 + " invalid");
        }
    }
    
    protected CodeAndLength getCodeAndLengthED(long address) throws MemoryException, OpCodeException {
        int byte2 = Byte.toUnsignedInt(this.readMemory8(address));
        int nn;
        
        switch (byte2) {
            case 0x40:  // IN B, (C)
            case 0x48:  // IN C, (C)
            case 0x50:  // IN D, (C)
            case 0x58:  // IN E, (C)
            case 0x60:  // IN H, (C)
            case 0x68:  // IN L, (C)
            case 0x78:  // IN A, (C)
                return new CodeAndLength(2, "IN " + getRegisterName8((byte2 & 0x38) >> 3) + ", (C)");
                
            case 0x41:  // OUT (C), B
            case 0x49:  // OUT (C), C
            case 0x51:  // OUT (C), D
            case 0x59:  // OUT (C), E
            case 0x61:  // OUT (C), H
            case 0x69:  // OUT (C), L
            case 0x79:  // OUT (C), A
                return new CodeAndLength(2, "OUT (C), " + getRegisterName8((byte2 & 0x38) >> 3));
                
            case 0x42:  // SBC HL, BC
            case 0x52:  // SBC HL, DE
            case 0x62:  // SBC HL, HL
            case 0x72:  // SBC HL, SP
                return new CodeAndLength(2, "SBC HL, " + getRegisterName16((byte2 & 0x30) >> 4));
                
            case 0x43:  // LD (nn), BC
            case 0x53:  // LD (nn), DE
            case 0x63:  // LD (nn), HL
            case 0x73:  // LD (nn), SP
                nn = Short.toUnsignedInt(this.readMemory16(address+1));
                return new CodeAndLength(4, "LD ($" + Integer.toHexString(nn) + "), " + getRegisterName16((byte2 & 0x30) >> 4));
                
            case 0x44:  // NEG
                return new CodeAndLength(2, "NEG");
                
            case 0x45:  // RETN
                return new CodeAndLength(2, "RETN");
                
            case 0x46:  // IM 0
                return new CodeAndLength(2, "IM 0");
                
            case 0x47:  // LD I, A
                return new CodeAndLength(2, "LD I, A");
                
            case 0x4A:  // ADC HL, BC
            case 0x5A:  // ADC HL, DE
            case 0x6A:  // ADC HL, HL
            case 0x7A:  // ADC HL, SP
                return new CodeAndLength(2, "ADC HL, " + getRegisterName16((byte2 & 0x30) >> 4));
                
            case 0x4B:  // LD BC, (nn)
            case 0x5B:  // LD DE, (nn)
            case 0x6B:  // LD HL, (nn)
            case 0x7B:  // LD SP, (nn)
                nn = Short.toUnsignedInt(this.readMemory16(address+1));
                return new CodeAndLength(4, "LD " + getRegisterName16((byte2 & 0x30) >> 4) + ", ($" + Integer.toHexString(nn) + ")");
                
            case 0x4D:  // RETI
                return new CodeAndLength(2, "RETI");
                
            case 0x4F:  // LD R, A
                return new CodeAndLength(2, "LD R, A");
                
            case 0x56:  // IM 1
                return new CodeAndLength(2, "IM 1");
                
            case 0x57:  // LD A, I
                return new CodeAndLength(2, "LD A, I");
                
            case 0x5E:  // IM 2
                return new CodeAndLength(2, "IM 2");
                
            case 0x5F:  // LD A, R
                return new CodeAndLength(2, "LD A, R");
                
            case 0x67:  // RRD
                return new CodeAndLength(2, "RRD");
                
            case 0x6F:  // RLD
                return new CodeAndLength(2, "RLD");
                
            case 0xA0:  // LDI
                return new CodeAndLength(2, "LDI");
                
            case 0xA1:  // CPI
                return new CodeAndLength(2, "CPI");
                
            case 0xA2:  // INI (HL), (BC)
                return new CodeAndLength(2, "INI (HL), (BC)");
                
            case 0xA3:  // OUTI (BC), (HL)
                return new CodeAndLength(2, "OUTI (BC), (HL)");
                
            case 0xA8:  // LDD
                return new CodeAndLength(2, "LDD");
                
            case 0xA9:  // CPD
                return new CodeAndLength(2, "CPD");
                
            case 0xAA:  // IND (HL), (BC)
                return new CodeAndLength(2, "IND (HL), (BC)");
                
            case 0xAB:  // OUTD (BC), (HL)
                return new CodeAndLength(2, "OUTD (BC), (HL)");
                
            case 0xB0:  // LDIR
                return new CodeAndLength(2, "LDIR");
                
            case 0xB1:  // CPIR
                return new CodeAndLength(2, "CPIR");
                
            case 0xB2:  // INIR (HL), (BC)
                return new CodeAndLength(2, "INIR (HL), (BC)");
                
            case 0xB3:  // OTIR (BC), (HL)
                return new CodeAndLength(2, "OTIR (BC), (HL)");
                
            case 0xB8:  // LDDR
                return new CodeAndLength(2, "LDDR");
                
            case 0xB9:  // CPDR
                return new CodeAndLength(2, "CPDR");
                
            case 0xBA:  // INDR (HL), (BC)
                return new CodeAndLength(2, "INDR (HL), (BC)");
                
            case 0xBB:  // OTDR (BC), (HL)
                return new CodeAndLength(2, "OTDR (BC), (HL)");
                
            default:
                throw new OpCodeException("Op Code ED " + byte2 + " invalid");
        }
    }
    
    @Override
    public CodeAndLength getCodeAndLength(long address) throws MemoryException, OpCodeException {
        byte byte1 = this.readMemory8(address);
        int nn;
        
        switch (Byte.toUnsignedInt(byte1)) {
            case 0x00:  // NOP
                return new CodeAndLength(1, "NOP");
                
            case 0x01:  // LD BC, nn
            case 0x11:  // LD DE, nn
            case 0x21:  // LD HL, nn
            case 0x31:  // LD SP, nn
                nn = Short.toUnsignedInt(this.readMemory16(address + 1));
                return new CodeAndLength(3, "LD " + getRegisterName16((byte1 & 0x30) >> 4) + ", $" + Integer.toHexString(nn));
                
            case 0x02:  // LD (BC), A
            case 0x12:  // LD (DE), A
                return new CodeAndLength(1, "LD (" + getRegisterName16((byte1 & 0x30) >> 4) + "), A");
                
            case 0x03:  // INC BC
            case 0x13:  // INC DE
            case 0x23:  // INC HL
            case 0x33:  // INC SP
                return new CodeAndLength(1, "INC " + getRegisterName16((byte1 & 0x30) >> 4));
                
            case 0x04:  // INC B
            case 0x0C:  // INC C
            case 0x14:  // INC D
            case 0x1C:  // INC E
            case 0x24:  // INC H
            case 0x2C:  // INC L
            case 0x3C:  // INC A
                return new CodeAndLength(1, "INC " + getRegisterName8((byte1 & 38)>>3));
                
            case 0x05:  // DEC B
            case 0x0D:  // DEC C
            case 0x15:  // DEC D
            case 0x1D:  // DEC E
            case 0x25:  // DEC H
            case 0x2D:  // DEC L
            case 0x3D:  // DEC A
                return new CodeAndLength(1, "DEC " + getRegisterName8((byte1 & 38)>>3));
                
            case 0x06:  // LD B, n
            case 0x0E:  // LD C, n
            case 0x16:  // LD D, n
            case 0x1E:  // LD E, n
            case 0x26:  // LD H, n
            case 0x2E:  // LD L, n
            case 0x3E:  // LD A, n
                nn = Byte.toUnsignedInt(this.readMemory8(address + 1));
                return new CodeAndLength(2, "LD " + getRegisterName8((byte1 & 0x38)>>3) + ", $" + Integer.toHexString(nn));
                
            case 0x07:  // RLCA
                return new CodeAndLength(1, "RLCA");
                
            case 0x08:  // EX AF, AF'
                return new CodeAndLength(1, "EX AF, AF'");
                
            case 0x09:  // ADD HL, BC
            case 0x19:  // ADD HL, DE
            case 0x29:  // ADD HL, HL
            case 0x39:  // ADD HL, SP
                return new CodeAndLength(1, "ADD HL, " + getRegisterName16((byte1 & 0x30) >> 4));
                
            case 0x0A:  // LD A, (BC)
                return new CodeAndLength(1, "LD A, (BC)");
                
            case 0x0B:  // DEC BC
            case 0x1B:  // DEC DE
            case 0x2B:  // DEC HL
            case 0x3B:  // DEC SP
                return new CodeAndLength(1, "DEC " + getRegisterName16((byte1 & 0x30) >> 4));
                
            case 0x0F:  // RRCA
                return new CodeAndLength(1, "RRCA");
                
            case 0x10:  // DJNZ n
                nn = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(2, "DJNZ $" + Integer.toHexString(nn));
                
            case 0x17:  // RLA
                return new CodeAndLength(1, "RLA");
                
            case 0x18:  // JR n
                nn = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(2, "JR $" + Integer.toHexString(nn));
                
            case 0x1A:  // LD A, (DE)
                return new CodeAndLength(1, "LD A, (DE)");
                
            case 0x1F:  // RRA
                return new CodeAndLength(1, "RRA");
                
            case 0x20:  // JRNZ n
            case 0x28:  // JRZ n
            case 0x30:  // JRNC n
            case 0x38:  // JRC n
                nn = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(2, "JR" + getConditionName((byte1 & 0x18) >> 3) + " $" + Integer.toHexString(nn));
                
            case 0x22:  // LD (nn), HL
                nn = Short.toUnsignedInt(this.readMemory16(address+1));
                return new CodeAndLength(3, "LD ($" + Integer.toHexString(nn) + "), HL");
                
            case 0x27:  // DAA
                return new CodeAndLength(1, "DAA");
                
            case 0x2A:  // LD HL, (nn)
                nn = Short.toUnsignedInt(this.readMemory16(address+1));
                return new CodeAndLength(3, "LD HL, ($" + Integer.toHexString(nn) + ")");
                
            case 0x2F:  // CPL
                return new CodeAndLength(1, "CPL");
                
            case 0x32:  // LD (nn), A
                nn = Short.toUnsignedInt(this.readMemory16(address+1));
                return new CodeAndLength(3, "LN ($" + Integer.toHexString(nn) + "), A");
                
            case 0x34:  // INC (HL)
                return new CodeAndLength(1, "INC (HL)");
                
            case 0x35:  // DEC (HL)
                return new CodeAndLength(1, "DEC (HL)");
                
            case 0x36:  // LD (HL), n
                nn = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(2, "LD (HL), $" + Integer.toHexString(nn));
                
            case 0x37:  // SCF
                return new CodeAndLength(1, "SCF");
                
            case 0x3A:  // LD A, (nn)
                nn = Short.toUnsignedInt(this.readMemory16(address+1));
                return new CodeAndLength(3, "LN A, ($" + Integer.toHexString(nn) + ")");
                
            case 0x3F:  // CCF
                return new CodeAndLength(1, "CCF");
                
            case 0x40:  // LD B, B'
            case 0x41:  // LD B, C'
            case 0x42:  // LD B, D'
            case 0x43:  // LD B, E'
            case 0x44:  // LD B, H'
            case 0x45:  // LD B, L'
            case 0x47:  // LD B, A'
            case 0x48:  // LD C, B'
            case 0x49:  // LD C, C'
            case 0x4A:  // LD C, D'
            case 0x4B:  // LD C, E'
            case 0x4C:  // LD C, H'
            case 0x4D:  // LD C, L'
            case 0x4F:  // LD C, A'
            case 0x50:  // LD D, B'
            case 0x51:  // LD D, C'
            case 0x52:  // LD D, D'
            case 0x53:  // LD D, E'
            case 0x54:  // LD D, H'
            case 0x55:  // LD D, L'
            case 0x57:  // LD D, A'
            case 0x58:  // LD E, B'
            case 0x59:  // LD E, C'
            case 0x5A:  // LD E, D'
            case 0x5B:  // LD E, E'
            case 0x5C:  // LD E, H'
            case 0x5D:  // LD E, L'
            case 0x5F:  // LD E, A'
            case 0x60:  // LD H, B'
            case 0x61:  // LD H, C'
            case 0x62:  // LD H, D'
            case 0x63:  // LD H, E'
            case 0x64:  // LD H, H'
            case 0x65:  // LD H, L'
            case 0x67:  // LD H, A'
            case 0x68:  // LD L, B'
            case 0x69:  // LD L, C'
            case 0x6A:  // LD L, D'
            case 0x6B:  // LD L, E'
            case 0x6C:  // LD L, H'
            case 0x6D:  // LD L, L'
            case 0x6F:  // LD L, A'
            case 0x78:  // LD A, B'
            case 0x79:  // LD A, C'
            case 0x7A:  // LD A, D'
            case 0x7B:  // LD A, E'
            case 0x7C:  // LD A, H'
            case 0x7D:  // LD A, L'
            case 0x7F:  // LD A, A'
                return new CodeAndLength(1, "LD " + getRegisterName8((byte1 & 0x38)>>3) + ", " + getRegisterName8(byte1 & 0x07) + "'");
                
            case 0x46:  // LD B, (HL)
            case 0x4E:  // LD C, (HL)
            case 0x56:  // LD D, (HL)
            case 0x5E:  // LD E, (HL)
            case 0x66:  // LD H, (HL)
            case 0x6E:  // LD L, (HL)
            case 0x7E:  // LD A, (HL)
                return new CodeAndLength(1, "LD " + getRegisterName8((byte1 & 0x38)>>3) + ", (HL)");
                
            case 0x70:  // LD (HL), B
            case 0x71:  // LD (HL), C
            case 0x72:  // LD (HL), D
            case 0x73:  // LD (HL), E
            case 0x74:  // LD (HL), H
            case 0x75:  // LD (HL), L
            case 0x77:  // LD (HL), A
                return new CodeAndLength(1, "LD (HL), " + getRegisterName8((byte1 & 0x07)));
                
            case 0x76:  // HALT
                return new CodeAndLength(1, "HALT");
                
            case 0x80:  // ADD A, B
            case 0x81:  // ADD A, C
            case 0x82:  // ADD A, D
            case 0x83:  // ADD A, E
            case 0x84:  // ADD A, H
            case 0x85:  // ADD A, L
            case 0x87:  // ADD A, A
                return new CodeAndLength(1, "ADD A, " + getRegisterName8((byte1 & 0x07)));
                
            case 0x86:  // ADD A, (HL)
                return new CodeAndLength(1, "ADD A, (HL)");
                
            case 0x88:  // ADC A, B
            case 0x89:  // ADC A, C
            case 0x8A:  // ADC A, D
            case 0x8B:  // ADC A, E
            case 0x8C:  // ADC A, H
            case 0x8D:  // ADC A, L
            case 0x8F:  // ADC A, A
                return new CodeAndLength(1, "ADC A, " + getRegisterName8((byte1 & 0x07)));
                
            case 0x8E:  // ADC A, (HL)
                return new CodeAndLength(1, "ADC A, (HL)");
                
            case 0x90:  // SUB A, B
            case 0x91:  // SUB A, C
            case 0x92:  // SUB A, D
            case 0x93:  // SUB A, E
            case 0x94:  // SUB A, H
            case 0x95:  // SUB A, L
            case 0x97:  // SUB A, A
                return new CodeAndLength(1, "SUB A, " + getRegisterName8((byte1 & 0x07)));
                
            case 0x96:  // SUB A, (HL)
                return new CodeAndLength(1, "SUB A, (HL)");
                
            case 0x98:  // SBC A, B
            case 0x99:  // SBC A, C
            case 0x9A:  // SBC A, D
            case 0x9B:  // SBC A, E
            case 0x9C:  // SBC A, H
            case 0x9D:  // SBC A, L
            case 0x9F:  // SBC A, A
                return new CodeAndLength(1, "SBC A, " + getRegisterName8((byte1 & 0x07)));
                
            case 0x9E:  // SBC A, (HL)
                return new CodeAndLength(1, "SBC A, (HL)");
                
            case 0xA0:  // AND A, B
            case 0xA1:  // AND A, C
            case 0xA2:  // AND A, D
            case 0xA3:  // AND A, E
            case 0xA4:  // AND A, H
            case 0xA5:  // AND A, L
            case 0xA7:  // AND A, A
                return new CodeAndLength(1, "AND A, " + getRegisterName8((byte1 & 0x07)));
                
            case 0xA6:  // AND A, (HL)
                return new CodeAndLength(1, "AND A, (HL)");
                
            case 0xA8:  // XOR A, B
            case 0xA9:  // XOR A, C
            case 0xAA:  // XOR A, D
            case 0xAB:  // XOR A, E
            case 0xAC:  // XOR A, H
            case 0xAD:  // XOR A, L
            case 0xAF:  // XOR A, A
                return new CodeAndLength(1, "XOR A, " + getRegisterName8((byte1 & 0x07)));
                
            case 0xAE:  // XOR A, (HL)
                return new CodeAndLength(1, "XOR A, (HL)");
                
            case 0xB0:  // OR A, B
            case 0xB1:  // OR A, C
            case 0xB2:  // OR A, D
            case 0xB3:  // OR A, E
            case 0xB4:  // OR A, H
            case 0xB5:  // OR A, L
            case 0xB7:  // OR A, A
                return new CodeAndLength(1, "OR A, " + getRegisterName8((byte1 & 0x07)));
                
            case 0xB6:  // OR A, (HL)
                return new CodeAndLength(1, "OR A, (HL)");
                
            case 0xB8:  // CP A, B
            case 0xB9:  // CP A, C
            case 0xBA:  // CP A, D
            case 0xBB:  // CP A, E
            case 0xBC:  // CP A, H
            case 0xBD:  // CP A, L
            case 0xBF:  // CP A, A
                return new CodeAndLength(1, "CP A, " + getRegisterName8((byte1 & 0x07)));
                
            case 0xBE:  // CP A, (HL)
                return new CodeAndLength(1, "CP A, (HL)");
                
            case 0xC0:  // RETNZ
                return new CodeAndLength(1, "RETNZ");
                
            case 0xC1:  // POP BC
            case 0xD1:  // POP DE
            case 0xE1:  // POP HL
                return new CodeAndLength(1, "POP " + getRegisterName16((byte1 & 0x30) >> 4));
                
            case 0xF1:  // POP AF
                return new CodeAndLength(1, "POP AF");
                
            case 0xC2:  // JNZ nn
            case 0xCA:  // JZ nn
            case 0xD2:  // JNC nn
            case 0xDA:  // JC nn
            case 0xE2:  // JPO nn
            case 0xEA:  // JPE nn
            case 0xF2:  // JNS nn
            case 0xFA:  // JS nn
                nn = Short.toUnsignedInt(this.readMemory16(address+1));
                return new CodeAndLength(3, "J" + getConditionName((byte1 & 0x38) >> 3) + " $" + Integer.toHexString(nn));
                
            case 0xC3:  // JP nn
                nn = Short.toUnsignedInt(this.readMemory16(address+1));
                return new CodeAndLength(3, "JP $" + Integer.toHexString(nn));
                
            case 0xC4:  // CALLNZ nn
            case 0xCC:  // CALLZ nn
            case 0xD4:  // CALLNC nn
            case 0xDC:  // CALLC nn
            case 0xE4:  // CALLPO nn
            case 0xEC:  // CALLPE nn
            case 0xF4:  // CALLNS nn
            case 0xFC:  // CALLS nn
                nn = Short.toUnsignedInt(this.readMemory16(address+1));
                return new CodeAndLength(3, "CALL" + getConditionName((byte1 & 0x38) >> 3) + " $" + Integer.toHexString(nn));
                
            case 0xC5:  // PUSH BC
            case 0xD5:  // PUSH DE
            case 0xE5:  // PUSH HL
                return new CodeAndLength(1, "PUSH " + getRegisterName16((byte1 & 0x30) >> 4));
                
            case 0xF5:  // PUSH AF
                return new CodeAndLength(1, "PUSH AF");
                
            case 0xC6:  // ADD A, n
                nn = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(2, "ADD A, $" + Integer.toHexString(nn));
                
            case 0xC7:  // RST 00h
            case 0xCF:  // RST 08h
            case 0xD7:  // RST 10h
            case 0xDF:  // RST 18h
            case 0xE7:  // RST 20h
            case 0xEF:  // RST 28h
            case 0xF7:  // RST 30h
            case 0xFF:  // RST 38h
                return new CodeAndLength(1, "RST $" + Integer.toHexString((byte1 & 0x38)));
                
            case 0xC8:  // RETZ
                return new CodeAndLength(1, "RETZ");
                
            case 0xC9:  // RET
                return new CodeAndLength(1, "RET");
                
            case 0xCB:  // OpCodeCB
                return getCodeAndLengthCB(address+1);
                
            case 0xCD:  // CALL nn
                nn = Short.toUnsignedInt(this.readMemory16(address+1));
                return new CodeAndLength(3, "CALL $" + Integer.toHexString(nn));
                
            case 0xCE:  // ADC A, n
                nn = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(2, "ADC A, $" + Integer.toHexString(nn));
                
            case 0xD0:  // RETNC
                return new CodeAndLength(1, "RETNC");
                
            case 0xD3:  // OUT (n), A
                nn = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(2, "OUT ($" + Integer.toHexString(nn) + "), A");
                
            case 0xD6:  // SUB A, n
                nn = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(2, "SUB A, $" + Integer.toHexString(nn));
                
            case 0xD8:  // RETC
                return new CodeAndLength(1, "RETC");
                
            case 0xD9:  // EXX
                return new CodeAndLength(1, "EXX");
                
            case 0xDB:  // IN A, (n)
                nn = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(2, "IN A, ($" + Integer.toHexString(nn) + ")");
                
            case 0xDD:  // OpCodeDD
                return getCodeAndLengthDD(address+1, "IX");
                
            case 0xDE:  // SBC A, n
                nn = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(2, "SBC A, $" + Integer.toHexString(nn));
                
            case 0xE0:  // RETPO
                return new CodeAndLength(1, "RETPO");
                
            case 0xE3:  // EX (SP), HL
                return new CodeAndLength(1, "EX (SP), HL");
                
            case 0xE6:  // AND A, n
                nn = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(2, "AND A, $" + Integer.toHexString(nn));
                
            case 0xE8:  // RETPE
                return new CodeAndLength(1, "RETPE");
                
            case 0xE9:  // JP (HL)
                return new CodeAndLength(1, "JP (HL)");
                
            case 0xEB:  // EX DE, HL
                return new CodeAndLength(1, "EX DE, HL");
                
            case 0xED:  // OpCodeED
                return getCodeAndLengthED(address+1);
                
            case 0xEE:  // XOR A, n
                nn = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(2, "XOR A, $" + Integer.toHexString(nn));
                
            case 0xF0:  // RETNS
                return new CodeAndLength(1, "RETNS");
                
            case 0xF3:  // DI
                return new CodeAndLength(1, "DI");
                
            case 0xF6:  // OR A, n
                nn = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(2, "OR A, $" + Integer.toHexString(nn));
                
            case 0xF8:  // RETS
                return new CodeAndLength(1, "RETS");
                
            case 0xF9:  // LD SP, HL
                return new CodeAndLength(1, "LD SP, HL");
                
            case 0xFB:  // EI
                return new CodeAndLength(1, "EI");
                
            case 0xFD:  // OpCodeFD
                return getCodeAndLengthDD(address+1, "IY");
                
            case 0xFE:  // CP A, n
                nn = Byte.toUnsignedInt(this.readMemory8(address+1));
                return new CodeAndLength(2, "CP A, $" + Integer.toHexString(nn));
                
            default:
                throw new OpCodeException("Op Code " + byte1 + " unknown");
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
            String className = "org.ansic.micro.Z80Debugger$" + mnem1;
            Class theClass = Class.forName(className);
            Object theObj = theClass.newInstance();
            
            Mnemonic mnem = (Mnemonic)theObj;
            
            
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
    
    public static void main(String[] args) throws Exception {
        Z80Debugger deb = new Z80Debugger();
        
        deb.translate("ADC A, (IX+$12)");
        deb.translate("RETI");
    }
}
