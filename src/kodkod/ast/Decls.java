/*
 * Declarations.java
 * Created on May 24, 2005
 */
package kodkod.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import kodkod.ast.visitor.ReturnVisitor;
import kodkod.ast.visitor.VoidVisitor;


/** 
 * Represents a sequence of declarations. 
 * 
 * @specfield size: int
 * @specfield declarations: [0..size) -> one Decl
 * @invariant size > 0
 * @invariant children = declarations[int]
 * @author Emina Torlak 
 */
public class Decls implements Node, Iterable<Decl> {
	private final List<Decl> declarations;
	
	/**
	 * Constructs a Decls object with itself as its sole
	 * declaration.  This constructor can only be called
	 * from inside the Decl constructor; otherwise it will
	 * throw a ClassCastException.
	 * @effects this.declarations' = 0->this
	 * @throws ClassCastException - this !in Decl
	 */
    Decls() {
    	Decl singleDecl = (Decl)this;
    	this.declarations = Collections.singletonList(singleDecl);
    }
    
    /**
	 * Constructs a new DeclSequence with the specified head and tail.
	 * @requires head.size > 0 && tail.size > 0
	 * @effects this.size' = head.size + tail.size &&
	 *          (all i: [0..head.size) | this.declarations[i] = head.declarations[i]) &&
	 *          (all i: [head.size..this.size') | this.declarations[i] = tail.declarations[i])
	 * @throws NullPointerException - head = null || tail is null 
	 */
	private Decls(Decls head, Decls tail) {
		List<Decl> temp = new ArrayList<Decl>(head.declarations.size() + tail.declarations.size());
		temp.addAll(head.declarations());
		temp.addAll(tail.declarations());
		this.declarations = Collections.unmodifiableList(temp);
	}
	
    /**
     * Returns an unmodifiable List view of this declaration sequence
     * @return {l: List | l.elems =  this.declarations }
     */
    public List<Decl> declarations() {
    	return declarations;
    }
    
    /**
     * Returns the number of declarations in this Decls object.
     * @return this.size
     */
    public int size() { return declarations.size(); }
    
    /**
     * Returns an unmodifiable iterator over the declarations in this Decls object.
     * @return this.declarations().iterator()
     */
    public Iterator<Decl> iterator() { return declarations.iterator(); }
    
    /**
     * Returns a sequence of this.size + decls.size declarations that has 
     * these declarations as the prefix and the given declarations as the suffix.
     * @return {ds: Decls | ds.size = this.size + decls.size && 
     *                      ds.declarations = this.declarations + 
     *                      {i: [this.size..this.size+decls.size), d: Decl | d = decls.declarations[i-this.size] }
     * @throws NullPointerException - decl = null
     */
    public final Decls and(Decls decls) {
    	return new Decls(this, decls);
    }
    
    /**
     * Accepts the given visitor and returns the result.
     * @see kodkod.ast.Node#accept(kodkod.ast.visitor.ReturnVisitor)
     */
    public <E, F, D, I> D accept(ReturnVisitor<E, F, D, I> visitor) {
        return visitor.visit(this);
    }
    
   
    
    /**
     * Accepts the given visitor.
     * @see kodkod.ast.Node#accept(kodkod.ast.visitor.VoidVisitor)
     */
    public void accept(VoidVisitor visitor) {
        visitor.visit(this);
    }
    
    /**
	 * Returns the string representation of these decls.
	 * @return string representation of these decls
	 */
    public String toString() {
        return declarations.toString();
    }
    
}
