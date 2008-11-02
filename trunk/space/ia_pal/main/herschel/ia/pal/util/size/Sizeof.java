package herschel.ia.pal.util.size;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;


//----------------------------------------------------------------------------
/**
 * This class allows to measure the size of basic data types in your JVM.
 * 
 * The method is to create 10,000 instances of an object, and monitor the memory usage.
 * This works only if so many objects can be instantiated (if it fits into the memory, for example).
 * 
 * The class also provides methods to calculate the JVM dependent sizes of member fields of different types 
 * (primitive types and wrapper types, such as Integer).
 * 
 * Adapted <a href="mailto:paul.balm@sciops.esa.int">Paul Balm</a>, Jan 2008
 * Reference: <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip130.html">JavaWorld Tip 130</a>
 *
 * @author <a href="mailto:vlad@trilogy.com">Vladimir Roubtsov</a>
 * 
 * @deprecated
 */
public class Sizeof
{
    
    private final static int nFields = 8; // each of the classes below has 8 fields (somewhat optimal to deal with the problem of memory alignment)
    
    static final class Empty {
    }
    static final class ObjectRefFields {
	Object a = null;
	Object b = null;
	Object c = null;
	Object d = null;
	Object e = null;
	Object f = null;
	Object g = null;
	Object h = null;
    }
    static final class LongFields {
	long a = 0;
	long b = 1;
	long c = 2;
	long d = 3;
	long e = 4;
	long f = 5;
	long g = 6;
	long h = 7;
    }
    static final class IntFields {
	int a = 0;
	int b = 1;
	int c = 2;
	int d = 3;
	int e = 4;
	int f = 5;
	int g = 6;
	int h = 7;
    }
    static final class ShortFields {
	short a = 0;
	short b = 1;
	short c = 2;
	short d = 3;
	short e = 4;
	short f = 5;
	short g = 6;
	short h = 7;
    }
    static final class CharFields {
	char a = 0;
	char b = 1;
	char c = 2;
	char d = 3;
	char e = 4;
	char f = 5;
	char g = 6;
	char h = 7;
    }
    static final class ByteFields {
	byte a = 0;
	byte b = 1;
	byte c = 2;
	byte d = 3;
	byte e = 4;
	byte f = 5;
	byte g = 6;
	byte h = 7;
    }
    static final class BooleanFields {
	boolean a = true;
	boolean b = true;
	boolean c = true;
	boolean d = true;
	boolean e = true;
	boolean f = true;
	boolean g = true;
	boolean h = true;
    }
    static final class DoubleFields {
	double a = 0;
	double b = 1;
	double c = 2;
	double d = 3;
	double e = 4;
	double f = 5;
	double g = 6;
	double h = 7;
    }
    static final class FloatFields {
	float a = 0;
	float b = 1;
	float c = 2;
	float d = 3;
	float e = 4;
	float f = 5;
	float g = 6;
	float h = 7;
    }

    private static final Logger log = Logger.getLogger("Sizeof");
    private static final Runtime s_runtime = Runtime.getRuntime ();

    private static int sizeOfEmpty = calculate(Empty.class, null); 
    
    public static void main(String[]  args) throws Exception {
	
	System.out.println("An Integer object has a size of: "+Sizeof.IntegerSize()+" bytes");
	System.out.println("A Double object has a size of:   "+Sizeof.DoubleSize()+" bytes\n");

	System.out.println("A  plain Object has a size of:   "+Sizeof.objectShellSize()+" bytes");
	System.out.println("A  reference    has a size of:   "+Sizeof.objectRefSize()+" bytes\n");
	System.out.println("A  long         has a size of:   "+Sizeof.longSize()+" bytes");
	System.out.println("An int          has a size of:   "+Sizeof.intSize()+" bytes");
	System.out.println("A  short        has a size of:   "+Sizeof.shortSize()+" bytes");
	System.out.println("A  char         has a size of:   "+Sizeof.charSize()+" bytes");
	System.out.println("A  byte         has a size of:   "+Sizeof.byteSize()+" bytes");
	System.out.println("A  boolean      has a size of:   "+Sizeof.booleanSize()+" bytes");
	System.out.println("A  double       has a size of:   "+Sizeof.doubleSize()+" bytes");
	System.out.println("A  float        has a size of:   "+Sizeof.floatSize()+" bytes");
	 
    }

    /**
     * Get the size of a plain, empty object
     * @return size in bytes
     */
    public static int objectShellSize() {
	return sizeOfEmpty;
    }

    /**
     * Get the size of an object reference
     * @return size in bytes
     */
    public static int objectRefSize() {
	return (calculate(ObjectRefFields.class, null) - sizeOfEmpty) / nFields;
    }

    /**
     * Get the size of a long member field in a class
     * @return size in bytes
     */
    public static int longSize() {
	return (calculate(LongFields.class, null) - sizeOfEmpty) / nFields;
    }

    /**
     * Get the size of a int member field in a class
     * @return size in bytes
     */
    public static int intSize() {
	return (calculate(IntFields.class, null) - sizeOfEmpty) / nFields;
    }

    /**
     * Get the size of a short member field in a class
     * @return size in bytes
     */
    public static int shortSize() {
	return (calculate(ShortFields.class, null) - sizeOfEmpty) / nFields;
    }

    /**
     * Get the size of a char member field in a class
     * @return size in bytes
     */
    public static int charSize() {
	return (calculate(CharFields.class, null) - sizeOfEmpty) / nFields;
    }

    /**
     * Get the size of a btyte member field in a class
     * @return size in bytes
     */
    public static int byteSize() {
	return (calculate(ByteFields.class, null) - sizeOfEmpty) / nFields;
    }

    /**
     * Get the size of a boolean member field in a class
     * @return size in bytes
     */
    public static int booleanSize() {
	return (calculate(BooleanFields.class, null) - sizeOfEmpty) / nFields;
    }

    /**
     * Get the size of a double member field in a class
     * @return size in bytes
     */
    public static int doubleSize() {
	return (calculate(DoubleFields.class, null) - sizeOfEmpty) / nFields;
    }

    /**
     * Get the size of a float member field in a class
     * @return size in bytes
     */
    public static int floatSize() {
	return (calculate(FloatFields.class, null) - sizeOfEmpty) / nFields;
    }
    

    /**
     * Get the size of a Integer member field in a class
     * @return size in bytes
     */
    public static int IntegerSize() {
	return calculate(Integer.class, new Class<?>[] {int.class});
    }

    /**
     * Get the size of a Double member field in a class
     * @return size in bytes
     */
    public static int DoubleSize() {
	return calculate(Double.class, new Class<?>[] {double.class});
    }

    /**
     * Calculate the size of a newly constructed object of class c, when the constructed by
     * the constructor that has the specified list of parameter types.
     * 
     * @param c: the class of the objects to be sized
     * @param parameterTypes: the parameter types of the constructor to be used to instantiate the object
     * @return size in bytes
     */
    public static int calculate (Class<?> c, Class<?>[] parameterTypes)
    {
	// "warm up" all classes/methods that we are going to use:
	runGC ();
	usedMemory ();

	// array to keep strong references to allocated objects:
	final int count = 10000; // 10000 or so is enough for small objects
	Object [] objects = new Object [count];


	long heap1 = 0;

	// allocate count+1 objects, discard the first one:
	for (int i = -1; i < count; ++ i)
	{

	    Object object;
	    try {
		// INSTANTIATE YOUR DATA HERE AND ASSIGN IT TO 'object':

		// Dynamic:
		if(parameterTypes==null) {
		    object = c.newInstance();
		}
		else {
		    Constructor<?> ctor = c.getConstructor(parameterTypes);
		    Object[] initargs = createArguments(parameterTypes);
		    object = ctor.newInstance(initargs);
		}
		
		//object = i;
		
		// Hard-coded options from JavaWorld Tip:
		//object = new Object (); // 8 bytes
		//object = new Integer (i); // 16 bytes
		//object = new Long (i); // same size as Integer?
		//object = createString (10); // 56 bytes? fine...
		//object = createString (9)+' '; // 72 bytes? the article explains why
		//object = new char [10]; // 32 bytes
		//object = new byte [32][1]; // 656 bytes?!
		
	    } catch (Exception e) {
		String msg = e.getClass().getName()+" creating object: Can't create object of  type "
		    +c.getName()+" with this parameter list. Msg was: "+e.getMessage(); 
		log.severe(msg);
		throw new RuntimeException(msg);
	    }

	    if (i >= 0)
		objects [i] = object;
	    else
	    {
		object = null; // discard the "warmup" object
		runGC ();
		heap1 = usedMemory (); // take a "before" heap snapshot
	    }
	}

	runGC ();
	long heap2 = usedMemory (); // take an "after" heap snapshot:

	final int size = Math.round (((float)(heap2 - heap1))/count);

	log.fine("'before' heap: " + heap1 +", 'after' heap: " + heap2);
	log.fine("heap delta: " + (heap2 - heap1) +", {" + objects [0].getClass () + "} size = " + size + " bytes");

	return size;
    }

    private static Object[] createArguments(Class<?>[] parameterTypes) {
	
	if(parameterTypes.length==0) {
	    return null;
	}
	
	Object[] args = new Object[parameterTypes.length];
	int i=0;
	for(Class<?> c : parameterTypes) {

	    try {
		
		if(c.isPrimitive()) {
		    args[i] = 0;
		}
		else {
		    args[i] = c.newInstance();
		}
	    } catch (InstantiationException e) {
		String msg = "InstantiationException creating constructor argument list: Can't create object of  type "
		    +c.getName()+": "+e.getMessage(); 
		log.severe(msg);
		throw new RuntimeException(msg);
	    } catch (IllegalAccessException e) {
		String msg = "IllegalAccessException creating constructor argument list: Can't create object of  type "
		    +c.getName()+": "+e.getMessage(); 
		log.severe(msg);
		throw new RuntimeException(msg);
	    }
	}
	return args;
    }
    
    /*
    public static Object getPrimitive(Class<?> primitiveClass) {
	Object obj;
	if(primitiveClass == java.lang.Long.TYPE) {
	    obj = 0l;
	}
	else if(primitiveClass == java.lang.Integer.TYPE) {
	    obj = 0;
	}
	else if(primitiveClass == java.lang.Short.TYPE) {
	    obj = (short) 0;
	}
	else if(primitiveClass == java.lang.Character.TYPE) {
	    obj = '0';
	}
	else if(primitiveClass == java.lang.Byte.TYPE) {
	    obj = (byte) 0;
	}
	else if(primitiveClass == java.lang.Boolean.TYPE) {
	    obj = true;
	}
	else if(primitiveClass == java.lang.Double.TYPE) {
	    obj = 0d;
	}
	else if(primitiveClass == java.lang.Float.TYPE) {
	    obj = 0f;
	}
	else {
	    throw new RuntimeException("Sizeof.getPrimitive lacks implementation for argument of type: "+primitiveClass.getName());
	}
	
	if(obj.getClass() != primitiveClass) {
	    throw new RuntimeException("Coding error. Returning object of type "+obj.getClass().getName()+" when asked for object of type "+primitiveClass.getName());
	}
    
	return obj;
    }
     */
    
    // a helper method for creating Strings of desired length
    // and avoiding getting tricked by String interning:
    /*
    private static String createString (final int length)
    {
	final char [] result = new char [length];
	for (int i = 0; i < length; ++ i) result [i] = (char) i;

	return new String (result);
    }*/

    // this is our way of requesting garbage collection to be run:
    // [how aggressive it is depends on the JVM to a large degree, but
    // it is almost always better than a single Runtime.gc() call]
    private static void runGC ()
    {
	// for whatever reason it helps to call Runtime.gc()
	// using several method calls:
	for (int r = 0; r < 4; ++ r) _runGC ();
    }

    private static void _runGC ()
    {
	long usedMem1 = usedMemory (), usedMem2 = Long.MAX_VALUE;

	for (int i = 0; (usedMem1 < usedMem2) && (i < 1000); ++ i)
	{
	    s_runtime.runFinalization ();
	    s_runtime.gc ();
	    Thread.yield ();

	    usedMem2 = usedMem1;
	    usedMem1 = usedMemory ();
	}
    }

    private static long usedMemory ()
    {
	return s_runtime.totalMemory () - s_runtime.freeMemory ();
    }


} // end of class
//----------------------------------------------------------------------------
