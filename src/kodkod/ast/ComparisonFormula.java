/*
 * ComparisonFormula.java
 * Created on Jul 1, 2005
 */
package kodkod.ast;

import kodkod.ast.visitor.ReturnVisitor;
import kodkod.ast.visitor.VoidVisitor;



/** 
 * Represents a comparison formula, e.g. x = y
 * 
 * @specfield left: Expression
 * @specfield right: Expression
 * @specfield op: Operator
 * @invariant children = left + right
 * @author Emina Torlak 
 */
public final class ComparisonFormula extends Formula {
    private final Expression left;
    private final Expression right;
    private final Operator op;
    private final int hashCode;
    
    /**  
     * Constructs a new comparison formula: left op  right
     * 
     * @effects this.left' = left && this.right' = right && this.op' = op
     * * @throws NullPointerException - left = null || right = null || op = null
     * @throws IllegalArgumentException - left.arity != right.arity
     */
    ComparisonFormula(Expression left, Operator op, Expression right) {
        if (!op.applicable(left.arity(), right.arity())) {
            throw new IllegalArgumentException(
            		"Arity mismatch: " + left + "::" + left.arity() + 
                    " and " + right + "::" + right.arity());
        }
        this.left = left;
        this.right = right;
        this.op = op;
        this.hashCode = op.hashCode() + left.hashCode() + right.hashCode();
    }

    /**
     * Returns the left child of this.
     * @return this.left
     */
    public Expression left() {return left;}
    
    /**
     * Returns the right child of this.
     * @return this.right
     */
    public Expression right() {return right;}
    
    /**
     * Returns the operator of this.
     * @return this.op
     */
    public Operator op() {return op;}
    
    /**
     * Accepts the given visitor and returns the result.
     * @see kodkod.ast.Node#accept(kodkod.ast.visitor.ReturnVisitor)
     */
    public <E, F, D> F accept(ReturnVisitor<E, F, D> visitor) {
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
     * Returns true of o is a ComparisonFormula with the
     * same tree structure as this.
     * @return o.op.equals(this.op) && o.left.equals(this.left) && o.right.equals(this.right) 
     */
    public boolean equals(Object o) {
    	if (this == o) return true;
    	if (!(o instanceof ComparisonFormula)) return false;
    	ComparisonFormula that = (ComparisonFormula)o;
    	return op.equals(that.op) &&
    		left.equals(that.left) &&
    		right.equals(that.right);
    }
    
    public int hashCode() {
    	return hashCode;
    }

    public String toString() {
        return "(" + left + " " + op + " " + right + ")";
    }
    
    /**
     * Represents a comparison operator; e.g. "in" or "=".
     */
    public static enum Operator {
        SUBSET { public String toString() { return "in"; }},
        EQUALS { public String toString() { return "="; }};
        
        /**
         * @return true if two expressions with the given arities
         * can be compared using  this operator; otherwise returns false.  This
         * method assumes that leftArity and rightArity are positive integers.
         */
        boolean applicable(int leftArity, int rightArity) { return leftArity==rightArity; }
    }

}