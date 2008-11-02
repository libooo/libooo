/* $Id: JythonConfigReader.java,v 1.3 2008/04/30 16:38:40 jsaiz Exp $
 * Copyright (c) 2008 ESA, STFC
 */

package herschel.ia.pal.managers.jython;

import herschel.share.interpreter.InterpreterFactory;
import java.util.logging.Logger;
import org.python.util.PythonInterpreter;

abstract class JythonConfigReader {
    @SuppressWarnings("unused")
    private static Logger _LOGGER = Logger.getLogger (JythonConfigReader.class.getName());
    static PythonInterpreter _interp;

    static {
        _interp = InterpreterFactory.newInterpreter();
        _interp.exec ("from herschel.ia.pal.all import *");
    }
} 