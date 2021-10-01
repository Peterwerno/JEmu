/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jemu.micro;

/**
 *
 * @author peter
 */
public class IllegalOpCodeException extends Exception {

    /**
     * Creates a new instance of <code>IllegalOpCodeException</code> without
     * detail message.
     */
    public IllegalOpCodeException() {
    }

    /**
     * Constructs an instance of <code>IllegalOpCodeException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public IllegalOpCodeException(String msg) {
        super(msg);
    }
}
