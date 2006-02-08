/*
 * Node.java
 * Created on May 5, 2005
 */
package kodkod.ast;

import kodkod.ast.visitor.ReturnVisitor;
import kodkod.ast.visitor.VoidVisitor;


/**
 * Represents a node in the abstract syntax tree.  A node
 * can accept a ReturnVisitor and have zero or more children.
 * 
 * @specfield children: set Node
 * @author Emina Torlak
 */
public interface Node {
    
    /**
     * Accepts the given visitor and returns the result
     * of the visit (i.e. the result of the call visitor.visit(this))
     * @return the result of being visited by the given visitor
     * @throws NullPointerException visitor = null
     * @see kodkod.ast.Node#accept(kodkod.ast.visitor.ReturnVisitor)
     */
    public <E, F, D> Object accept(ReturnVisitor<E, F, D> visitor);
    
    /**
     * Accepts the given void visitor by calling visitor.visit(this).
     * @throws NullPointerException visitor = null
     * @see kodkod.ast.Node#accept(kodkod.ast.visitor.VoidVisitor)
     */
    public void accept(VoidVisitor visitor);
}