/*
 * ReturnVisitor.java
 * Created on May 20, 2005
 */
package kodkod.ast.visitor;

import kodkod.ast.BinaryExpression;
import kodkod.ast.BinaryFormula;
import kodkod.ast.ComparisonFormula;
import kodkod.ast.Comprehension;
import kodkod.ast.ConstantExpression;
import kodkod.ast.ConstantFormula;
import kodkod.ast.Decl;
import kodkod.ast.Decls;
import kodkod.ast.IfExpression;
import kodkod.ast.IntComparisonFormula;
import kodkod.ast.IntConstant;
import kodkod.ast.MultiplicityFormula;
import kodkod.ast.NotFormula;
import kodkod.ast.QuantifiedFormula;
import kodkod.ast.Relation;
import kodkod.ast.RelationPredicate;
import kodkod.ast.UnaryExpression;
import kodkod.ast.Cardinality;
import kodkod.ast.Variable;


/** 
 * A visitor that visits every node in the AST, returning some value for each.
 * The methods that visit an {@link kodkod.ast.Expression expression}, 
 * {@link kodkod.ast.Formula formula}, {@link kodkod.ast.Decls declarations},
 * and {@link kodkod.ast.IntExpression integer expression} and
 * return values of types E, F, D, and I respectively. 
 *
 * @author Emina Torlak  
 */
public interface ReturnVisitor<E, F, D, I> {
    
	/** 
	 * Visits the given sequence of declarations and returns the result.
	 * @return the result of visiting <code>decls</code> 
	 **/
    public D visit(Decls decls);
    /** 
	 * Visits the given declaration and returns the result.
	 * @return the result of visiting <code>decl</code> 
	 **/
    public D visit(Decl decl);
    
    /** 
	 * Visits the given relation and returns the result.
	 * @return the result of visiting <code>relation</code> 
	 **/
    public E visit(Relation relation);
    /** 
	 * Visits the given variable and returns the result.
	 * @return the result of visiting <code>variable</code> 
	 **/
    public E visit(Variable variable);
    /** 
	 * Visits the given constant expression and returns the result.
	 * @return the result of visiting <code>constExpr</code> 
	 **/
    public E visit(ConstantExpression constExpr);
    
    /** 
	 * Visits the given binary expression and returns the result.
	 * @return the result of visiting <code>binExpr</code> 
	 **/
    public E visit(BinaryExpression binExpr);
    /** 
	 * Visits the given unary expression and returns the result.
	 * @return the result of visiting <code>unaryExpr</code> 
	 **/
    public E visit(UnaryExpression unaryExpr);   
    /** 
	 * Visits the given comprehension and returns the result.
	 * @return the result of visiting <code>comprehension</code> 
	 **/
    public E visit(Comprehension comprehension);
    /** 
	 * Visits the given if-then expression and returns the result.
	 * @return the result of visiting <code>ifExpr</code> 
	 **/
    public E visit(IfExpression ifExpr); 
   
    /**
     * Visits the given integer constant and returns the result.
	 * @return the result of visiting <code>intconst</code> 
     */
    public I visit(IntConstant intConst);
    /**
     * Visits the given unary integer expression and returns the result.
	 * @return the result of visiting <code>intexpr</code> 
     */
    public I visit(Cardinality intExpr);
    /**
     * Visits the given integer comparison formula and returns the result.
	 * @return the result of visiting <code>intcomp</code> 
     */
    public F visit(IntComparisonFormula intComp);
    
    /** 
	 * Visits the given quantified formula and returns the result.
	 * @return the result of visiting <code>quantFormula</code> 
	 **/
    public F visit(QuantifiedFormula quantFormula);
    /** 
	 * Visits the given binary formula and returns the result.
	 * @return the result of visiting <code>binFormula</code> 
	 **/
    public F visit(BinaryFormula binFormula);
    /** 
	 * Visits the given negation and returns the result.
	 * @return the result of visiting <code>not</code> 
	 **/
    public F visit(NotFormula not);
    /** 
	 * Visits the given constant formula and returns the result.
	 * @return the result of visiting <code>constant</code> 
	 **/
    public F visit(ConstantFormula constant);
    
    /** 
	 * Visits the given comparison formula and returns the result.
	 * @return the result of visiting <code>compFormula</code> 
	 **/
    public F visit(ComparisonFormula compFormula);
    /** 
	 * Visits the given multiplicity formula and returns the result.
	 * @return the result of visiting <code>multFormula</code> 
	 **/
    public F visit(MultiplicityFormula multFormula);
    /**
     * Visits the given relation predicate and returns the result.
     * @return the result of visiting <code>predicate</code>
     */
    public F visit(RelationPredicate predicate);

    
}
