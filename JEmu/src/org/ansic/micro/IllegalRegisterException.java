/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ansic.micro;

/**
 *
 * @author peter
 */
public class IllegalRegisterException extends Exception {

    /**
     * Creates a new instance of <code>IllegalRegisterException</code> without
     * detail message.
     */
    public IllegalRegisterException() {
    }

    /**
     * Constructs an instance of <code>IllegalRegisterException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public IllegalRegisterException(String msg) {
        super(msg);
    }
}
