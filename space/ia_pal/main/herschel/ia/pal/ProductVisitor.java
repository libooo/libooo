package herschel.ia.pal;

import herschel.ia.dataset.DatasetVisitor;

/**
 * ProductVisitor allows registration of an operation
 * on a product, using the visitor pattern.
 * 
 * Currently used by ProductStorage to peform an operation
 * on each product, immediately before committing it.
 * 
 * The visitor pattern allows automatic recovery of type
 * information: Iterating over products, call p.accept(visitor)
 * on each one, will result in the appropriate visit method
 * being executed, dependent on the type of *object* p, not
 * the type of reference (which is Product).
 * 
 * @author pbalm
 *
 */
public interface ProductVisitor extends DatasetVisitor {

    public void visit(Context node);
    public void visit(ListContext node);
    public void visit(MapContext node);
}
