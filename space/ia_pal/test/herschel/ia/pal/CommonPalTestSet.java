package herschel.ia.pal;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;

public abstract class CommonPalTestSet extends AbstractPalTestCase {

    @SuppressWarnings("unused")
    private static final Logger _LOGGER = Logger.getLogger("CommonPalTestSet");

    private boolean _runRemoveTests;

    private AttributePalTestGroup _attributePalTestGroup;
    private BasicPalTestGroup _basicPalTestGroup;
    private CloningPalTestGroup _cloningPalTestGroup;
    private ContextPalTestGroup _contextPalTestGroup;
    private ProductRefPalTestGroup _descriptorsPalTestGroup;
    private FullQueryPalTestGroup _fullQueryPalTestGroup;
    private GeneralQueryPalTestGroup _generalQueryPalTestGroup;
    private MetaPalTestGroup _metaPalTestGroup;
//    private PerformancePalTestGroup _performancePalTestGroup;
    private RemovePalTestGroup _removePalTestGroup;
    private VersionPalTestGroup _versionPalTestGroup;
    private WhiteBoxPalTestGroup _whiteBoxPalTestGroup;

    protected CommonPalTestSet(String name, boolean runRemoveTests) {
	super(name);
	_runRemoveTests = runRemoveTests;
	_attributePalTestGroup = new AttributePalTestGroup("AttribPalTestGroup", this);
	_basicPalTestGroup = new BasicPalTestGroup("BasicPalTestGroup", this);
	_cloningPalTestGroup = new CloningPalTestGroup("CloningPalTestGroup", this);
	_contextPalTestGroup = new ContextPalTestGroup("ContextPalTestGroup", this);
	_descriptorsPalTestGroup = new ProductRefPalTestGroup("DescriptorsPalTestGroup", this);
	_fullQueryPalTestGroup = new FullQueryPalTestGroup("FullQueryPalTestGroup", this);
	_generalQueryPalTestGroup = new GeneralQueryPalTestGroup("GeneralQueryPalTestGroup", this);
	_metaPalTestGroup = new MetaPalTestGroup("MetaPalTestGroup", this);
//	_performancePalTestGroup = new PerformancePalTestGroup("PerformancePalTestGroup", this);
	_removePalTestGroup = new RemovePalTestGroup("RemovePalTestGroup", this);
	_versionPalTestGroup = new VersionPalTestGroup("VersionPalTestGroup", this);
	_whiteBoxPalTestGroup = new WhiteBoxPalTestGroup("WhiteBoxPalTestGroup", this);
    }

    protected CommonPalTestSet(String name) {
	this(name, true);
    }

//  AttributePalTestGroup

    public void testAttQuerySimple() {
    runBefore(_attributePalTestGroup);
    _attributePalTestGroup.testAttQuerySimple();
    runAfter(_attributePalTestGroup);
    }

    public void testAttQueryComplex() {
    runBefore(_attributePalTestGroup);
    _attributePalTestGroup.testAttQueryComplex();
    runAfter(_attributePalTestGroup);
    }


//  BasicPalTestGroup

    public void testStorageCreation() {
	runBefore(_basicPalTestGroup);
	_basicPalTestGroup.testStorageCreation();
	runAfter(_basicPalTestGroup);
    }

    public void testPoolCreation() {
	runBefore(_basicPalTestGroup);
	_basicPalTestGroup.testPoolCreation();
	runAfter(_basicPalTestGroup);
    }

    public void testLoad() {
	runBefore(_basicPalTestGroup);
	_basicPalTestGroup.testLoad();
	runAfter(_basicPalTestGroup);
    }

    public void testMetaAccess() {
	runBefore(_basicPalTestGroup);
	_basicPalTestGroup.testMetaAccess();
	runAfter(_basicPalTestGroup);
    }


//  CloningPalTestGroup

//    public void testLeafProductClone()
//    throws IOException, GeneralSecurityException {
//        runBefore(_cloningPalTestGroup);
//        _cloningPalTestGroup.testLeafProductClone();
//        runAfter(_cloningPalTestGroup);
//    }

    public void testListContextComplexClone()
    throws IOException, GeneralSecurityException {
        runBefore(_cloningPalTestGroup);
        _cloningPalTestGroup.testListContextComplexClone();
        runAfter(_cloningPalTestGroup);
    }

    public void testListContextSimpleClone()
    throws IOException, GeneralSecurityException {
        runBefore(_cloningPalTestGroup);
        _cloningPalTestGroup.testListContextSimpleClone();
        runAfter(_cloningPalTestGroup);
    }

    public void testListContextVersion()
    throws IOException, GeneralSecurityException {
        runBefore(_cloningPalTestGroup);
        _cloningPalTestGroup.testListContextVersion();
        runAfter(_cloningPalTestGroup);
    }

    public void testMapAndListContextComplexClone()
    throws IOException, GeneralSecurityException {
        runBefore(_cloningPalTestGroup);
        _cloningPalTestGroup.testMapAndListContextComplexClone();
        runAfter(_cloningPalTestGroup);
    }

    public void testMapContextComplexClone()
    throws IOException, GeneralSecurityException {
        runBefore(_cloningPalTestGroup);
        _cloningPalTestGroup.testMapContextComplexClone();
        runAfter(_cloningPalTestGroup);
    }

    public void testMapContextSimpleClone()
    throws IOException, GeneralSecurityException {
        runBefore(_cloningPalTestGroup);
        _cloningPalTestGroup.testMapContextSimpleClone();
        runAfter(_cloningPalTestGroup);
    }

    public void testMapContextVersion()
    throws IOException, GeneralSecurityException {
        runBefore(_cloningPalTestGroup);
        _cloningPalTestGroup.testMapContextVersion();
        runAfter(_cloningPalTestGroup);
    }


//  ProductRefPalTestGroup

    public void testGetProduct() throws IOException, GeneralSecurityException {
	runBefore(_descriptorsPalTestGroup);
	_descriptorsPalTestGroup.testGetProduct();
	runAfter(_descriptorsPalTestGroup);
    }

    public void testGetTotalSize() throws IOException, GeneralSecurityException {
	runBefore(_descriptorsPalTestGroup);
	_descriptorsPalTestGroup.testGetTotalSize();
	runAfter(_descriptorsPalTestGroup);
    }

    public void testDescriptorsSave() throws IOException, GeneralSecurityException {
	runBefore(_descriptorsPalTestGroup);
	_descriptorsPalTestGroup.testDescriptorsSave();
	runAfter(_descriptorsPalTestGroup);
    }

    public void testDescriptorsLoad() throws IOException, GeneralSecurityException {
	runBefore(_descriptorsPalTestGroup);
	_descriptorsPalTestGroup.testDescriptorsLoad();
	runAfter(_descriptorsPalTestGroup);
    }

//  MetaPalTestGroup

    public void testSingleMetaQuery() {
	runBefore(_metaPalTestGroup);
	_metaPalTestGroup.testSingleMetaQuery();
	runAfter(_metaPalTestGroup);
    }

    public void testSpr2290() {
	runBefore(_metaPalTestGroup);
	_metaPalTestGroup.testSpr2290();
	runAfter(_metaPalTestGroup);
    }

    public void testSpr5207() {
    	runBefore(_metaPalTestGroup);
    	_metaPalTestGroup.testSpr5207();
    	runAfter(_metaPalTestGroup);
        }

//  FullQueryPalTestGroup

    public void testFullQuery() {
	runBefore(_fullQueryPalTestGroup);
	_fullQueryPalTestGroup.testFullQuery();
	runAfter(_fullQueryPalTestGroup);
    }


//  GeneralQueryPalTestGroup

    public void testRefineQuery() {
	runBefore(_generalQueryPalTestGroup);
	_generalQueryPalTestGroup.testRefineQuery();
	runAfter(_generalQueryPalTestGroup);
    }

    public void testSpr2370() {
	runBefore(_generalQueryPalTestGroup);
	_generalQueryPalTestGroup.testSpr2370();
	runAfter(_generalQueryPalTestGroup);
    }

    public void testSpr2356ProductHandle() throws IOException, GeneralSecurityException {
	runBefore(_generalQueryPalTestGroup);
	_generalQueryPalTestGroup.testSpr2356ProductHandle();
	runAfter(_generalQueryPalTestGroup);
    }

    public void testSpr2356BracketsQuery() throws IOException, GeneralSecurityException {
	runBefore(_generalQueryPalTestGroup);
	_generalQueryPalTestGroup.testSpr2356BracketsQuery();
	runAfter(_generalQueryPalTestGroup);
    }


    public void testSpr2357Quotes() throws IOException, GeneralSecurityException {
	runBefore(_generalQueryPalTestGroup);
	_generalQueryPalTestGroup.test2357Quotes();
	runAfter(_generalQueryPalTestGroup);
    }

    public void testSpr2357EmbeddedQuotes() throws IOException, GeneralSecurityException {
	runBefore(_generalQueryPalTestGroup);
	_generalQueryPalTestGroup.testSpr2357EmbeddedQuotes();
	runAfter(_generalQueryPalTestGroup);
    }


//  ContextPalTestGroup

    public void testContextCreatedWhollyInMemory() throws IOException, GeneralSecurityException {
	runBefore(_contextPalTestGroup);
	_contextPalTestGroup.testContextCreatedWhollyInMemory();
	runAfter(_contextPalTestGroup);
    }

    public void testContextModifiedInMemoryAfterSave() throws IOException, GeneralSecurityException {
	runBefore(_contextPalTestGroup);
	_contextPalTestGroup.testContextModifiedInMemoryAfterSave();
	runAfter(_contextPalTestGroup);
    }

    public void testContextSavedAfterEveryUpdate() throws IOException, GeneralSecurityException {
	runBefore(_contextPalTestGroup);
	_contextPalTestGroup.testContextSavedAfterEveryUpdate();
	runAfter(_contextPalTestGroup);
    }

    public void testProductCreatedInMemory() throws IOException, GeneralSecurityException {
	runBefore(_contextPalTestGroup);
	_contextPalTestGroup.testProductCreatedInMemory();
	runAfter(_contextPalTestGroup);
    }

    public void testListContext() throws IOException, GeneralSecurityException {
	runBefore(_contextPalTestGroup);
	_contextPalTestGroup.testListContext();
	runAfter(_contextPalTestGroup);
    }

    public void testSpr2487ListContextBasic() throws IOException, GeneralSecurityException {
	runBefore(_contextPalTestGroup);
	_contextPalTestGroup.testSpr2487ListContextBasic();
	runAfter(_contextPalTestGroup);
    }

    public void testSpr2487ListContextDetailed() throws IOException, GeneralSecurityException {
	runBefore(_contextPalTestGroup);
	_contextPalTestGroup.testSpr2487ListContextDetailed();
	runAfter(_contextPalTestGroup);
    }

    public void testSpr2487MapContextBasic() throws IOException, GeneralSecurityException {
	runBefore(_contextPalTestGroup);
	_contextPalTestGroup.testSpr2487MapContextBasic();
	runAfter(_contextPalTestGroup);
    }

    public void testSpr2487MapContextDetailed() throws IOException, GeneralSecurityException {
	runBefore(_contextPalTestGroup);
	_contextPalTestGroup.testSpr2487MapContextDetailed();
	runAfter(_contextPalTestGroup);
    }

    public void testSpr3547() {
	runBefore(_contextPalTestGroup);
	_contextPalTestGroup.testSpr3547();
	runAfter(_contextPalTestGroup);
    }
    
    public void testSpr5204RemoveContext() throws IOException, GeneralSecurityException {
    	runBefore(_contextPalTestGroup);
    	_contextPalTestGroup.testSpr5204RemoveContext();
    	runAfter(_contextPalTestGroup);
    }

//  public void testProductModifiedInMemoryAfterSave() throws IOException, GeneralSecurityException {
//  _contextPalTestGroup.testProductModifiedInMemoryAfterSave();
//  }


//  PerformancePalTestGroup

//    public void testPerformanceSave() throws GeneralSecurityException {
//	runBefore(_performancePalTestGroup);
//	_performancePalTestGroup.testSave();
//	runAfter(_performancePalTestGroup);
//    }
//
//    public void testPerformanceSaveAndLoad() throws GeneralSecurityException {
//	runBefore(_performancePalTestGroup);
//	_performancePalTestGroup.testSaveAndLoad();
//	runAfter(_performancePalTestGroup);
//    }
//
//    public void testPerformanceFullQuery() throws GeneralSecurityException {
//	runBefore(_performancePalTestGroup);
//	_performancePalTestGroup.testFullQuery();
//	runAfter(_performancePalTestGroup);
//    }
//
//    public void testPerformanceAttribQuery() throws GeneralSecurityException {
//	runBefore(_performancePalTestGroup);
//	_performancePalTestGroup.testAttribQuery();
//	runAfter(_performancePalTestGroup);
//    }
//
//    public void testPerformanceMetaQuery() throws GeneralSecurityException {
//	runBefore(_performancePalTestGroup);
//	_performancePalTestGroup.testMetaQuery();
//	runAfter(_performancePalTestGroup);
//    }


//  RemovePalTestGroup

    public
    void testRemoveFirstItem() throws IOException, GeneralSecurityException{
	if (_runRemoveTests) {
	    runBefore(_removePalTestGroup);
	    _removePalTestGroup.testRemoveFirstItem();
	    runAfter(_removePalTestGroup);
	}
    }

    public
    void testRemoveLastItem() throws IOException, GeneralSecurityException{
	if (_runRemoveTests) {
	    runBefore(_removePalTestGroup);
	    _removePalTestGroup.testRemoveLastItem();
	    runAfter(_removePalTestGroup);
	}
    }

    public
    void testRemoveMiddleItem() throws IOException, GeneralSecurityException {
	if (_runRemoveTests) {
	    runBefore(_removePalTestGroup);
	    _removePalTestGroup.testRemoveMiddleItem();
	    runAfter(_removePalTestGroup);
	}
    }

    public
    void testRemoveLast3Items() throws IOException, GeneralSecurityException {
	if (_runRemoveTests) {
	    runBefore(_removePalTestGroup);
	    _removePalTestGroup.testRemoveLast3Items();
	    runAfter(_removePalTestGroup);
	}
    }

    public
    void testRemoveFirst3Items() throws IOException, GeneralSecurityException {
	if (_runRemoveTests) {
	    runBefore(_removePalTestGroup);
	    _removePalTestGroup.testRemoveFirst3Items();
	    runAfter(_removePalTestGroup);
	}
    }

    public
    void testRemoveMiddle3Items() throws IOException, GeneralSecurityException {
	if (_runRemoveTests) {
	    runBefore(_removePalTestGroup);
	    _removePalTestGroup.testRemoveMiddle3Items();
	    runAfter(_removePalTestGroup);
	}
    }

    public
    void testRemoveSomeItems() throws IOException, GeneralSecurityException {
	if (_runRemoveTests) {
	    runBefore(_removePalTestGroup);
	    _removePalTestGroup.testRemoveSomeItems();
	    runAfter(_removePalTestGroup);
	}
    }

    public void testRemoveAllElements() {
	if (_runRemoveTests) {
	    runBefore(_removePalTestGroup);
	    _removePalTestGroup.testRemoveAllElements();
	    runAfter(_removePalTestGroup);
	}
    }

    public void testRemoveAllItemsReverse() {
	if (_runRemoveTests) {
	    runBefore(_removePalTestGroup);
	    _removePalTestGroup.testRemoveAllItemsReverse();
	    runAfter(_removePalTestGroup);
	}
    }

    public void testRemoveAttribItems()
    throws IOException, GeneralSecurityException {
	if (_runRemoveTests) {
	    runBefore(_removePalTestGroup);
	    _removePalTestGroup.testRemoveAttribItems();
	    runAfter(_removePalTestGroup);
	}
    }

    public void testRemoveGeneral()
    throws IOException, GeneralSecurityException {
	if (_runRemoveTests) {
	    runBefore(_removePalTestGroup);
	    _removePalTestGroup.testRemoveGeneral();
	    runAfter(_removePalTestGroup);
	}
    }

    public void testRemoveItemsAttribRefine()
    throws IOException, GeneralSecurityException {
	if (_runRemoveTests) {
	    runBefore(_removePalTestGroup);
	    _removePalTestGroup.testRemoveItemsAttribRefine();
	    runAfter(_removePalTestGroup);
	}
    }

    public void testRemoveItemsMetaRefine()
    throws IOException, GeneralSecurityException {
	if (_runRemoveTests) {
	    runBefore(_removePalTestGroup);
	    _removePalTestGroup.testRemoveItemsMetaRefine();
	    runAfter(_removePalTestGroup);
	}
    }

    public void testRemoveMetaItems()
    throws IOException, GeneralSecurityException {
	if (_runRemoveTests) {
	    runBefore(_removePalTestGroup);
	    _removePalTestGroup.testRemoveMetaItems();
	    runAfter(_removePalTestGroup);
	}
    }

    public void testRemoveNonExistant()
    throws IOException, GeneralSecurityException {
	if (_runRemoveTests) {
	    runBefore(_removePalTestGroup);
	    _removePalTestGroup.testRemoveNonExistant();
	    runAfter(_removePalTestGroup);
	}
    }

    public void testRemoveAndAdd() throws IOException, GeneralSecurityException{
	if (_runRemoveTests) {
	    runBefore(_removePalTestGroup);
	    _removePalTestGroup.testRemoveAndAdd();
	    runAfter(_removePalTestGroup);
	}
    }
    
    public void testRemoveVersioning1() {
        if (_runRemoveTests) {
	    runBefore(_removePalTestGroup);
            _removePalTestGroup.testRemoveVersioning1();
	    runAfter(_removePalTestGroup);
        }
    }

    public void testRemoveVersioning2() {
        if (_runRemoveTests) {
	    runBefore(_removePalTestGroup);
            _removePalTestGroup.testRemoveVersioning2();
	    runAfter(_removePalTestGroup);
        }
    }


//  VersionPalTestGroup

    public void testGeneral() throws IOException, GeneralSecurityException {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testGeneral();
        runAfter(_versionPalTestGroup);
    }

    public void testAccessContext() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testAccessContext();
        runAfter(_versionPalTestGroup);
    }

    public void testAccessProduct() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testAccessProduct();
        runAfter(_versionPalTestGroup);
    }


    public void testAssignTag() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testAssignTag();
        runAfter(_versionPalTestGroup);
    }

    public void testCloningScatteredListChildLeaf() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testCloningScatteredListChildLeaf();
        runAfter(_versionPalTestGroup);
    }

    public void testCloningScatteredListChildListContext() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testCloningScatteredListChildListContext();
        runAfter(_versionPalTestGroup);
    }

    public void testCloningScatteredMapChildLeaf() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testCloningScatteredMapChildLeaf();
        runAfter(_versionPalTestGroup);
    }

    public void testCloningScatteredMapChildListContext() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testCloningScatteredMapChildListContext();
        runAfter(_versionPalTestGroup);
    }

    public void testDeepCloningListContext() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testDeepCloningListContext();
        runAfter(_versionPalTestGroup);
    }

    public void testDeepCloningMapContext() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testDeepCloningMapContext();
        runAfter(_versionPalTestGroup);
    }

    public void testDeepCloningMapListContext() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testDeepCloningMapListContext();
        runAfter(_versionPalTestGroup);
    }

    public void testFindTagsProducts() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testFindTagsProducts();
        runAfter(_versionPalTestGroup);
    }

//    public void testFindVersioningProduct() {
//        runBefore(_versionPalTestGroup);
//        _versionPalTestGroup.testFindVersioningProduct();
//        runAfter(_versionPalTestGroup);
//    }
//
//    public void testFindVersioningProductTwo() {
//        runBefore(_versionPalTestGroup);
//        _versionPalTestGroup.testFindVersioningProductTwo();
//        runAfter(_versionPalTestGroup);
//    }

    public void testImplicitVersioning() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testImplicitVersioning();
        runAfter(_versionPalTestGroup);
    }

    public void testImplicitVersioningTwoInstances() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testImplicitVersioningTwoInstances();
        runAfter(_versionPalTestGroup);
    }

    public void testProductSubclasses() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testProductSubclasses();
        runAfter(_versionPalTestGroup);
    }

    public void testRemoveTag() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testRemoveTag();
        runAfter(_versionPalTestGroup);
    }

    public void testSingleProductQuery() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSingleProductQuery();
        runAfter(_versionPalTestGroup);
    }

    public void testSaveAndModifyCleanlyLeafProduct() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSaveAndModifyCleanlyLeafProduct();
        runAfter(_versionPalTestGroup);
    }

    public void testSaveAndModifyCleanlyListContext() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSaveAndModifyCleanlyListContext();
        runAfter(_versionPalTestGroup);
    }

    public void testSaveAndModifyCleanlyMapContext() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSaveAndModifyCleanlyMapContext();
        runAfter(_versionPalTestGroup);
    }

    public void testSaveAndModifyLeafProduct() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSaveAndModifyLeafProduct();
        runAfter(_versionPalTestGroup);
    }

    public void testSaveAndModifyListContext() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSaveAndModifyListContext();
        runAfter(_versionPalTestGroup);
    }

    public void testSaveAndModifyMapContext() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSaveAndModifyMapContext();
        runAfter(_versionPalTestGroup);
    }

    public void testSaveAnotherStorageLeafProduct() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSaveAnotherStorageLeafProduct();
        runAfter(_versionPalTestGroup);
    }

    public void testSaveAnotherStorageListContext() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSaveAnotherStorageListContext();
        runAfter(_versionPalTestGroup);
    }

    public void testSaveAnotherStorageMapContext() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSaveAnotherStorageMapContext();
        runAfter(_versionPalTestGroup);
    }

    public void testSaveNoModifyLeafProduct() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSaveNoModifyLeafProduct();
        runAfter(_versionPalTestGroup);
    }

    public void testSaveNoModifyListContext() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSaveNoModifyListContext();
        runAfter(_versionPalTestGroup);
    }

    public void testSaveNoModifyMapContext() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSaveNoModifyMapContext();
        runAfter(_versionPalTestGroup);
    }

    public void testSimpleContextCloning() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSimpleContextCloning();
        runAfter(_versionPalTestGroup);
    }

    public void testSimpleContextSave() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSimpleContextSave();
        runAfter(_versionPalTestGroup);
    }

    public void testSimpleMerging() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSimpleMerging();
        runAfter(_versionPalTestGroup);
    }

    public void testSimpleProductCloning() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSimpleProductCloning();
        runAfter(_versionPalTestGroup);
    }

    public void testSimpleTagMerging() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSimpleTagMerging();
        runAfter(_versionPalTestGroup);
    }

    public void testSimpleVersionMerging() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSimpleVersionMerging();
        runAfter(_versionPalTestGroup);
    }

    public void testSaveHeadOnly() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSaveHeadOnly();
        runAfter(_versionPalTestGroup);
    }

    public void testSynchronization() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSynchronization();
        runAfter(_versionPalTestGroup);
    }

    public void testCountCalls() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testCountCalls();
        runAfter(_versionPalTestGroup);
    }

    public void testTagName() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testTagName();
        runAfter(_versionPalTestGroup);
    }

    // Doesn't make sense after the removal of versioning metadata
//    public void testSpr3620() throws IOException, GeneralSecurityException {
//        runBefore(_versionPalTestGroup);
//        _versionPalTestGroup.testSpr3620();
//        runAfter(_versionPalTestGroup);
//    }

    public void testPoolWithoutVersioning() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testPoolWithoutVersioning();
        runAfter(_versionPalTestGroup);
    }

    public void testProblemBuild1413() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testProblemBuild1413();
        runAfter(_versionPalTestGroup);
    }

    public void testSpr4151() {
        runBefore(_versionPalTestGroup);
        _versionPalTestGroup.testSpr4151();
        runAfter(_versionPalTestGroup);
    }

//     WhiteBoxPalTestGroup

    public void testSpr3458() throws IOException, GeneralSecurityException {
        runBefore(_whiteBoxPalTestGroup);
        _whiteBoxPalTestGroup.testSpr3458();
        runAfter(_whiteBoxPalTestGroup);
    }
    
    public void testSpr4590() throws IOException, GeneralSecurityException {
        runBefore(_whiteBoxPalTestGroup);
        _whiteBoxPalTestGroup.testSpr4590();
        runAfter(_whiteBoxPalTestGroup);
    }
    // PoolSchemaEvolver
    /*
     * Taking these tests out -- they don't work for cached or serial pools!
     * (only for db, simple and lstore)
    public void testPoolSchemaEvolverA() throws Exception {
	new CommonPoolSchemaEvolverTest("PoolSchemaEvolverTest - A", this).testA();
    }
    public void testPoolSchemaEvolverB() throws Exception {
	new CommonPoolSchemaEvolverTest("PoolSchemaEvolverTest - B", this).testB();
    }
     */

    // Some test infrastructure
    public void runBefore(AbstractPalTestGroup testGroup) {
        runWithAnnotation(testGroup, Before.class);
    }

    public void runAfter(AbstractPalTestGroup testGroup) {
        runWithAnnotation(testGroup, After.class);
    }

    public void runWithAnnotation(AbstractPalTestGroup testGroup,
                                  Class<? extends Annotation> annotation) {
        for (Method m : testGroup.getClass().getMethods()) {
            if (m.isAnnotationPresent(annotation)) {
                try {
                    m.invoke(testGroup);
                } catch (Exception e) {
                    _LOGGER.severe("Error running methods annotated with "
                            + annotation.getCanonicalName() + ": "
                            + e.getMessage());
                    fail(e.getMessage());
                }
            }
        }
    }
}
