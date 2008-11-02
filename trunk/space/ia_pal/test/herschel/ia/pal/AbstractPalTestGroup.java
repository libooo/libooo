package herschel.ia.pal;

public abstract class AbstractPalTestGroup extends AbstractPalTestCase {

	public AbstractPalTestGroup(String name, AbstractPalTestCase testcase) {
		super(name);
		_palTestCase = testcase;
	}

	private AbstractPalTestCase _palTestCase;
    
    protected ProductPool getPool(int index) {
        return _palTestCase.getPool(index);
    }
    
    protected ProductPool createPool(int index) {
        return _palTestCase.createPool(index);
    }

    protected void cleanPool(int index) {
        _palTestCase.cleanPool(index);
    }

    protected void setPalTestCase(AbstractPalTestCase test) {
	_palTestCase = test;
    }
    
    public String getPoolType() {
	return _palTestCase.getPoolType();
    }

}
