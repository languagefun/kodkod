/*
 * Expression.java
 * Created on May 4, 2005
 */
package kodkod.ast;



/** 
 * A type representing a relational expression.  Unless otherwise noted,
 * all methods in this class throw a NullPointerException when given
 * null arguments.
 * 
 * @specfield arity: int
 * @invariant arity > 0
 *
 * @author Emina Torlak 
 */
public abstract class Expression implements Node {
	
	/** universal relation */
	public static final Expression UNIV = ConstantExpression.UNIV;
	
	/** identity relation */
	public static final Expression IDEN = ConstantExpression.IDEN;
	
	/** empty relation */
	public static final Expression NONE = ConstantExpression.NONE;
	
    /**
     * Constructs a leaf expression
     * @effects no this.children'
     */
    Expression() { }

    /**
     * Returns the join of this and the specified expression.  The effect
     * of this method is the same as calling this.compose(BinaryExpression.Operator.JOIN, expr).
     * @return {e : Expression | e = this.expr}
     */
    public final Expression join(Expression expr) {
        return compose(BinaryExpression.Operator.JOIN,expr);
    }
    
    /**
     * Returns the product of this and the specified expression.  The effect
     * of this method is the same as calling this.compose(BinaryExpression.Operator.PRODUCT, expr).
     * @return {e : Expression | e = this->expr}
     */
    public final Expression product(Expression expr) {
        return compose(BinaryExpression.Operator.PRODUCT,expr);
    }
    
    /**
     * Returns the union of this and the specified expression.  The effect
     * of this method is the same as calling this.compose(BinaryExpression.Operator.UNION, expr).
     * @return {e : Expression | e = this + expr}
     */
    public final Expression union(Expression expr) {
        return compose(BinaryExpression.Operator.UNION,expr);
    }
    
    /**
     * Returns the difference of this and the specified expression.  The effect
     * of this method is the same as calling this.compose(BinaryExpression.Operator.DIFFERENCE, expr).
     * @return {e : Expression | e = this - expr}
     */
    public final Expression difference(Expression expr) {
        return compose(BinaryExpression.Operator.DIFFERENCE,expr);
    }
    
    /**
     * Returns the intersection of this and the specified expression.  The effect
     * of this method is the same as calling this.compose(BinaryExpression.Operator.INTERSECTION, expr).
     * @return {e : Expression | e = this & expr}
     */
    public final Expression intersection(Expression expr) {
        return compose(BinaryExpression.Operator.INTERSECTION,expr);
    }
    
    /**
     * Returns the relational override of this with the specified expression.  The effect
     * of this method is the same as calling this.compose(BinaryExpression.Operator.OVERRIDE, expr).
     * @return {e : Expression | e = this ++ expr}
     */
    public final Expression override(Expression expr) {
    		return compose(BinaryExpression.Operator.OVERRIDE,expr);
    }
    
    /**
     * Returns the composition of this and the specified expression, using the
     * given binary operator.
     * @return {e: Expression | e = this op expr }
     */
    public Expression compose(BinaryExpression.Operator op, Expression expr) {
    	return new BinaryExpression(this, op, expr);
    }
    
    /**
     * Returns the transpose of this.  The effect of this method is the same
     * as calling this.apply(UnaryExpression.Operator.TRANSPOSE).
     * @return {e : Expression | e = ~this}
     */
    public final Expression transpose() {
        return apply(UnaryExpression.Operator.TRANSPOSE);
    }
    
    /**
     * Returns the transitive closure of this.  The effect of this  method is the same
     * as calling this.apply(UnaryExpression.Operator.CLOSURE).
     * @return {e : Expression | e = ^this}
     * @throws IllegalArgumentException - this.arity != 2
     */
    public final Expression closure() {
        return apply(UnaryExpression.Operator.CLOSURE);
    }
    
    /**
     * Returns the reflexive transitive closure of this.  The effect of this 
     * method is the same
     * as calling this.apply(UnaryExpression.Operator.REFLEXIVE_CLOSURE).
     * @return {e : Expression | e = *this}
     * @throws IllegalArgumentException - this.arity != 2
     */
    public final Expression reflexiveClosure() {
    		return apply(UnaryExpression.Operator.REFLEXIVE_CLOSURE);
    }
    
    /**
     * Returns the expression that results from applying the given unary operator
     * to this.  
     * @return {e: Expression | e = op this }
     * @throws IllegalArgumentException - this.arity != 2
     */
    public Expression apply(UnaryExpression.Operator op) {
    	return new UnaryExpression(op, this);
    }
    
    /**
     * Returns the formula 'this = expr'. The effect of this method is the same 
     * as calling this.compose(ComparisonFormula.Operator.EQUALS, expr).
     * @return {f : Formula | f <=> this = expr}
     */
    public final Formula eq(Expression expr) {
    	return (this == expr) ? ConstantFormula.TRUE :
    		compose(ComparisonFormula.Operator.EQUALS, expr);
    }
    
    /**
     * Returns the formula 'this in expr'.  The effect of this method is the same 
     * as calling this.compose(ComparisonFormula.Operator.SUBSET, expr).
     * @return {f : Formula | f <=> this in expr}
     */
    public final Formula in(Expression expr) {
    	return (this == expr) ? ConstantFormula.TRUE :
    		compose(ComparisonFormula.Operator.SUBSET, expr);
    }
    
    /**
     * Returns the formula that represents the composition of this and the
     * given expression with the given comparison operator.
     * @return {f: Formula | f <=> this op expr }
     */
    public Formula compose(ComparisonFormula.Operator op, Expression expr) {
    	return new ComparisonFormula(this, op, expr);
    }
    
    /**
     * Returns the formula 'some this'.  The effect of this method is the same as calling
     * this.apply(MultiplicityFormula.Multiplicity.SOME).
     * @return {f : Formula | f <=> some this}
     */
    public final Formula some() {
        return apply(Multiplicity.SOME);
    }
    
    /**
     * Returns the formula 'no this'.  The effect of this method is the same as calling
     * this.apply(MultiplicityFormula.Multiplicity.NO).
     * @return {f : Formula | f <=> no this}
     */
    public final Formula no() {
        return apply(Multiplicity.NO);
    }
    
    /**
     * Returns the formula 'one this'.  The effect of this method is the same as calling
     * this.apply(MultiplicityFormula.Multiplicity.ONE).
     * @return {f : Formula | f <=> one this}
     */
    public final Formula one() {
        return apply(Multiplicity.ONE);
    }
    
    /**
     * Returns the formula 'lone this'.  The effect of this method is the same as calling
     * this.apply(MultiplicityFormula.Multiplicity.LONE).
     * @return {f : Formula | f <=> lone this}
     */
    public final Formula lone() {
        return apply(Multiplicity.LONE);
    }
    
    /**
     * Returns the formula that results from applying the specified multiplicity to
     * this expression.  The SET multiplicity is not allowed.
     * @return {f: Formula | f <=> mult this}
     * @throws IllegalArgumentException - mult = Multiplicity.SET
     */
    public Formula apply(Multiplicity mult) {
    	return new MultiplicityFormula(mult, this);
    }
    
    /**
     * Returns the arity of this expression.
     * @return this.arity
     */
    public abstract int arity();
    
    /**
     * Accepts the given visitor and returns the result.
     * @see kodkod.ast.Node#accept(kodkod.ast.Visitor)
     */
    public abstract <E, F, D> E accept(Visitor<E, F, D> visitor);
       
}
