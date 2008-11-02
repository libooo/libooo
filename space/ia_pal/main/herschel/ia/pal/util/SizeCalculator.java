package herschel.ia.pal.util;

import herschel.ia.dataset.*;
import herschel.ia.numeric.*;
import herschel.share.unit.Unit;

/**
 * This class is used to calculate the size of a Product.
 * It visits all the metadata and dataset in a Product, and sum the size.
 * The size of simple types is hard-coded, which may be different on a 64bit computer.
 * 
 * Usage:
 * <pre>
 * 		SizeCalculator calc = new SizeCalcultor();
 * 		calc.visit(product);
 * 		long size = calc.getResult();
 * </pre>
 *
 */
public class SizeCalculator implements MetaDataVisitor, DatasetVisitor,
		ColumnVisitor, ParameterVisitor, ArrayDataVisitor {
	
	private transient long _result = 0;
	
	private transient final int SIZE_OF_BOOLEAN = 1;
	private transient final int SIZE_OF_BYTE = 1;
	private transient final int SIZE_OF_SHORT = 2;
	private transient final int SIZE_OF_INT = 4;
	private transient final int SIZE_OF_FLOAT = 4;
	private transient final int SIZE_OF_LONG = 8;
	private transient final int SIZE_OF_DOUBLE = 8;
	private transient final int SIZE_OF_COMPLEX = 16;
	private transient final int SIZE_OF_DATE = 8;	
	
	/**
	 * A short cut method to get Product size.
	 * @param p
	 * @return
	 */
	public static long sizeOf(Product p){
		SizeCalculator sc = new SizeCalculator();
		sc.visit(p);
		long result = sc.getResult();
		return result;
	}
	
	/**
	 * get the result of the size calculation
	 * @return the result of the size calculation
	 */
	public long getResult(){
		long result = _result;
		reset();
		return result;
	}
	
	/**
	 * set the result of the size calculation to zero
	 */
	public void reset(){
		_result = 0;
	}

	/* 
	 * Sum the size of meta keys and meta parameters
	 * 
	 * @see herschel.ia.dataset.MetaDataVisitor#visit(herschel.ia.dataset.MetaData)
	 */
	public void visit(MetaData meta) {
		for (String key : meta.keySet()){
			_result += sizeof(key);			
			Parameter p = meta.get(key);
			p.accept(this);	
		}
	}
	
	public void visit(BooleanParameter p) {
		_result += SIZE_OF_BOOLEAN + sizeof(p.getDescription());
	}

	public void visit(DateParameter p) {	
		_result += SIZE_OF_DATE + sizeof(p.getDescription());
	}

	public void visit(DoubleParameter p) {
		_result += SIZE_OF_DOUBLE + sizeof(p.getUnit()) + sizeof(p.getDescription());
	}

	public void visit(LongParameter p) {
		_result += SIZE_OF_LONG + sizeof(p.getUnit()) + sizeof(p.getDescription());
	}

	public void visit(StringParameter p) {
		_result += sizeof(p.getString()) + sizeof(p.getDescription());
	}

	/* 
	 * Sum the size of MetaData, Dataset keys and Dataset of the product
	 *  
	 * @see herschel.ia.dataset.DatasetVisitor#visit(herschel.ia.dataset.Product)
	 */
	public void visit(Product node) {
		MetaData meta = node.getMeta();
		visit(meta);
		
		for (String key : node.keySet()) {			
			_result += sizeof(key);			
			Dataset data = node.get(key);			
			data.accept(this);
		}
	}	

	/* 
	 * Sum the size of MetaData, Unit, Description and all of ArrayData of the ArrayDataset
	 *  
	 * @see herschel.ia.dataset.DatasetVisitor#visit(herschel.ia.dataset.ArrayDataset)
	 */
	public void visit(ArrayDataset node) {
		MetaData meta = node.getMeta();
		visit(meta);
		
		Unit unit = node.getUnit();
		_result += sizeof(unit);
		
		_result += sizeof(node.getDescription());
		
		ArrayData data = node.getData();
		data.accept(this);
	}

	/*
	 * Sum the size of MetaData, Description and all of Columns of the TableDataset
	 * 
	 * @see herschel.ia.dataset.DatasetVisitor#visit(herschel.ia.dataset.TableDataset)
	 */
	public void visit(TableDataset node) {
		MetaData meta = node.getMeta();
		visit(meta);		
		
		_result += sizeof(node.getDescription());
		
		for (int i = 0; i < node.getColumnCount(); i++){
			Column col = node.getColumn(i);
			visit(col);
		}		
	}

	/* 
	 * Sum the MetaData, Description and all of children Dataset of the CompositeDataset
	 * 
	 * @see herschel.ia.dataset.DatasetVisitor#visit(herschel.ia.dataset.CompositeDataset)
	 */
	public void visit(CompositeDataset node) {
		MetaData meta = node.getMeta();
		visit(meta);
		
		_result += sizeof(node.getDescription());		
	
		for (String key : node.keySet()){
			_result += sizeof(key);			
			Dataset data = node.get(key);
			data.accept(this);
		}
	}

	/* 
	 * Sum the size of description, Unit and ArrayData of the Column
	 *  
	 * @see herschel.ia.dataset.ColumnVisitor#visit(herschel.ia.dataset.Column)
	 */
	public void visit(Column column) {
		_result += sizeof(column.getDescription());
		_result += sizeof(column.getUnit());
		ArrayData data = column.getData();
		data.accept(this);
	}
	
	/**
	 * calculate the size of a String,
	 * which is 2*string length, or 2 if null.
	 * @param str
	 * @return the size of the string
	 */
	private long sizeof(String str){
		return (str == null) ? 2 : 2 * str.length();
	}
	
	/**
	 * calculate the size of a Unit Object, 
	 * which is the size of the unit name, or 2 if null. 
	 * @param unit
	 * @return the size of the Unit Object
	 */
	private long sizeof(Unit unit){
		return (unit == null) ? 2 : sizeof(unit.getName());
	}		
	
	/**
	 * Calculate the size of ArrayData.
	 * For StringArray, the size is 4 + the sum size of all strings.
	 * For other ArrayData, such as IntegerArray, FloatArray, etc, 
	 * the size is 4 * array dimensions + array size * numericTypeSize.
	 * @param data
	 * @return the size of the ArrayData
	 * @deprecated ArrayDataVisitor implemented
	 */
	private long sizeof (ArrayData data){
		long length = 0;		
		
		int numericTypeSize = 0;
		if (data instanceof StringArray){
			length += 4;
			for (String s : ((String1d)data).getArray()){
				length += sizeof(s);
			}
		} else { 
			length += 4 * data.getDimensions().length;
			if (data instanceof BooleanArray){
				numericTypeSize = SIZE_OF_BOOLEAN;
			} else if (data instanceof ByteArray){
				numericTypeSize = SIZE_OF_BYTE;
			} else if (data instanceof ShortArray){
				numericTypeSize = SIZE_OF_SHORT;
			} else if (data instanceof IntegerArray){
				numericTypeSize = SIZE_OF_INT;
			} else if (data instanceof LongArray){
				numericTypeSize = SIZE_OF_LONG;
			} else if (data instanceof FloatArray){
				numericTypeSize = SIZE_OF_FLOAT;
			} else if (data instanceof DoubleArray){
				numericTypeSize = SIZE_OF_DOUBLE;
			} else if (data instanceof ComplexArray){
				numericTypeSize = SIZE_OF_COMPLEX;
			}

			length += numericTypeSize * data.getSize();	
		}		
		
		return length;
	}

	public void visit(String1d node) {		
		_result += 4;
		for (String s : node.getArray()){
			_result += sizeof(s);
		}		
	}

	public void visit(Bool1d node) {
		_result += 4 + SIZE_OF_BOOLEAN * node.getSize();		
	}

	public void visit(Bool2d node) {
		_result += 4 * 2 + SIZE_OF_BOOLEAN * node.getSize();	
	}

	public void visit(Bool3d node) {
		_result += 4 * 3 + SIZE_OF_BOOLEAN * node.getSize();		
	}

	public void visit(Bool4d node) {
		_result += 4 * 4 + SIZE_OF_BOOLEAN * node.getSize();			
	}

	public void visit(Bool5d node) {
		_result += 4 * 5 + SIZE_OF_BOOLEAN * node.getSize();			
	}

	public void visit(Byte1d node) {
		_result += 4 + SIZE_OF_BYTE * node.getSize();			
	}

	public void visit(Byte2d node) {
		_result += 4 * 2 + SIZE_OF_BYTE * node.getSize();			
	}

	public void visit(Byte3d node) {
		_result += 4 * 3 + SIZE_OF_BYTE * node.getSize();		
	}

	public void visit(Byte4d node) {
		_result += 4 * 4 + SIZE_OF_BYTE * node.getSize();		
	}

	public void visit(Byte5d node) {
		_result += 4 * 5 + SIZE_OF_BYTE * node.getSize();
	}

	public void visit(Short1d node) {
		_result += 4 + SIZE_OF_SHORT * node.getSize();		
	}

	public void visit(Short2d node) {
		_result += 4 * 2 + SIZE_OF_SHORT * node.getSize();		
	}

	public void visit(Short3d node) {
		_result += 4 * 3 + SIZE_OF_SHORT * node.getSize();	
	}

	public void visit(Short4d node) {
		_result += 4 * 4 + SIZE_OF_SHORT * node.getSize();	
	}

	public void visit(Short5d node) {
		_result += 4 * 5 + SIZE_OF_SHORT * node.getSize();	
	}

	public void visit(Int1d node) {
		_result += 4 + SIZE_OF_INT * node.getSize();	
	}

	public void visit(Int2d node) {
		_result += 4 * 2 + SIZE_OF_INT * node.getSize();	
	}

	public void visit(Int3d node) {
		_result += 4 * 3 + SIZE_OF_INT * node.getSize();	
	}

	public void visit(Int4d node) {
		_result += 4 * 4 + SIZE_OF_INT * node.getSize();	
	}

	public void visit(Int5d node) {
		_result += 4 * 5 + SIZE_OF_INT * node.getSize();	
	}

	public void visit(Long1d node) {
		_result += 4 + SIZE_OF_LONG * node.getSize();	
	}

	public void visit(Long2d node) {
		_result += 4 * 2 + SIZE_OF_LONG * node.getSize();
	}

	public void visit(Long3d node) {
		_result += 4 * 3 + SIZE_OF_LONG * node.getSize();
	}

	public void visit(Long4d node) {
		_result += 4 * 4 + SIZE_OF_LONG * node.getSize();
	}

	public void visit(Long5d node) {
		_result += 4 * 5 + SIZE_OF_LONG * node.getSize();
	}

	public void visit(Float1d node) {
		_result += 4 + SIZE_OF_FLOAT * node.getSize();
	}

	public void visit(Float2d node) {
		_result += 4 * 2 + SIZE_OF_FLOAT * node.getSize();
	}

	public void visit(Float3d node) {
		_result += 4 * 3 + SIZE_OF_FLOAT * node.getSize();		
	}

	public void visit(Float4d node) {
		_result += 4 * 4 + SIZE_OF_FLOAT * node.getSize();		
	}

	public void visit(Float5d node) {
		_result += 4 * 5 + SIZE_OF_FLOAT * node.getSize();		
	}

	public void visit(Double1d node) {
		_result += 4 + SIZE_OF_DOUBLE * node.getSize();		
	}

	public void visit(Double2d node) {
		_result += 4 * 2 + SIZE_OF_DOUBLE * node.getSize();		
	}

	public void visit(Double3d node) {
		_result += 4 * 3 + SIZE_OF_DOUBLE * node.getSize();
	}

	public void visit(Double4d node) {
		_result += 4 * 4 + SIZE_OF_DOUBLE * node.getSize();
	}

	public void visit(Double5d node) {
		_result += 4 * 5 + SIZE_OF_DOUBLE * node.getSize();
	}

	public void visit(Complex1d node) {
		_result += 4 + SIZE_OF_COMPLEX * node.getSize();
	}

	public void visit(Complex2d node) {
		_result += 4 * 2 + SIZE_OF_COMPLEX * node.getSize();
	}

	public void visit(Complex3d node) {
		_result += 4 * 3 + SIZE_OF_COMPLEX * node.getSize();
	}

	public void visit(Complex4d node) {
		_result += 4 * 4 + SIZE_OF_COMPLEX * node.getSize();
	}

	public void visit(Complex5d node) {
		_result += 4 * 5 + SIZE_OF_COMPLEX * node.getSize();
	}

}
