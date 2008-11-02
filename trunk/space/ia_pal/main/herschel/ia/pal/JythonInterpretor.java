package herschel.ia.pal;

import herschel.ia.dataset.Product;
import herschel.ia.pal.query.StorageQuery;
import herschel.share.interpreter.InterpreterFactory;

import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.python.core.PyException;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 * Handles query requests that require a Jython interpreter to be invoked.
 * <p>
 * This is a utility class intended for {@link ProductPool} implementations.
 * </p>
 * 
 * <p>
 * TODO: (Bli 2008-04-01 ) To make pool implementations thread safe, the original getInstance() method is
 * modified to return multiple instances(the max number of instance is limited by MAX_NUM_OF_INSTANCE).
 * However the implementation is flawed, and can fail if the number of concurrent query threads is more
 * than NUM_OF_INSTANCE. On the other hand it might well work fine in practice, as it is expected no more
 * than 10 simultaneous threads. Ideally it would only create an interpreter when there were none free.
 * The inside implementation need to be improved in near future.
 * 
 * 
 * @author hsiddiqu
 * 
 */
public class JythonInterpretor {

    private static final Logger logger = Logger.getLogger(JythonInterpretor.class.getName());

    private static final String NEWLINE = System.getProperty("line.separator");

    private static final String META_MESSAGE = NEWLINE +
        "Are you sure the query syntax is correct, and you have added " +
        "meta.containsKey conditions to avoid this error?";

    private static final String ERROR_MESSAGE =
	"Jython query %s could not be evaluated correctly. Details as to why follow: " +
	NEWLINE + "%s";

    private static final Pattern META_ERROR_REGEX =
	Pattern.compile(".*IndexError: Parameter\\[\\w*\\] does not exist.*", Pattern.DOTALL);

//    private static final int MAX_NUM_OF_INSTANCE = Integer.parseInt(Configuration.getProperty("hcss.ia.pal.interpreter.pool.num",
//	                                                                                      "20"));
//    private static int counter = 0;
//    private static Vector<PythonInterpreter> _jythons = new Vector<PythonInterpreter>();

    private static ThreadLocal<JythonInterpretor> _instance = new ThreadLocal<JythonInterpretor>();
    private PythonInterpreter _jython;

    private JythonInterpretor() {
	_jython = InterpreterFactory.newInterpreter();
	_jython.exec("from herschel.ia.numeric.all import *");
    }

    /**
     * Returns an instance of a JythonInterpretor
     */
    public synchronized static JythonInterpretor getInstance() {
	long start = System.currentTimeMillis();
	if (_instance.get() == null) {
	    _instance.set(new JythonInterpretor());
	}
	long end = System.currentTimeMillis();
	logger.fine("JythonInterpretor.getInstance consumed: " + (end - start) + "ms.");
	return _instance.get();
    }

    /**
     * Checks if a {@link StorageQuery} matches the provided {@link Product}. This uses a Jython
     * interpretation operation.
     * 
     * <p>
     * Note cannot use Product as the type for the first argument, as inner class FakeProduct also
     * needs this method. Cannot easily extract FakeProduct, or convert it into a proper product.
     * </p>
     */
    public synchronized boolean matches(Object product, StorageQuery query) {
	synchronized (_jython) {
	    String variable = query.getVariable();
	    String where = query.getWhere();
	    PyObject previousValue = _jython.get(variable);
	    _jython.set(variable, product);

	    try {
		return ((PyInteger) _jython.eval(where)).getValue() != 0;
	    } catch (PyException e) {
		String reason = e.toString();
		String message = String.format(ERROR_MESSAGE, where, reason);
		if (META_ERROR_REGEX.matcher(reason).matches()) {
		    message = META_MESSAGE + NEWLINE + message;
		}
		throw new IllegalArgumentException(message, e);
	    } finally {
		_jython.set(variable, previousValue);
	    }
	}
    }
}
