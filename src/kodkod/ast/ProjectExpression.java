/**
 * 
 */
package kodkod.ast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import kodkod.ast.visitor.ReturnVisitor;
import kodkod.ast.visitor.VoidVisitor;

/**
 * Represents a general projection expression.  For example,
 * let [[e]] = {&lt;a, b, c&gt;, &lt;d, e, f&gt;, &lt;d, g, f&gt;}.  Then, 
 * project(e, 1, 3) = {&lt;a, c&gt;, &lt;d, f&gt;} and project(e, 1, 1, 2) = {&lt;a, a, b&gt;, &lt;d, d, e&gt;, &lt;d, d, g&gt;}.
 * 
 * @specfield expression: Expression 
 * @specfield arity: [1..)
 * @specfield columns: [0..arity) -> one IntExpression
 * @invariant children = expression + columns[int]
 * @author Emina Torlak
 */
public final class ProjectExpression extends Expression {
	private final Expression expr;
	private final List<IntExpression> columns;

	/**
	 * Constructs a new projection expression using the given
	 * expr and columns.
	 * @effects this.expression' = expr && this.indices' = columns
	 */
	ProjectExpression(Expression expr, IntExpression... columns) {
		if (columns.length==0)
			throw new IllegalArgumentException("no columns specified for projection");
		this.expr = expr;
		final IntExpression[] temp = new IntExpression[columns.length];
		System.arraycopy(columns, 0, temp, 0, columns.length);
		this.columns = Collections.unmodifiableList(Arrays.asList(temp));
	}

	/**
	 * Returns this.arity.
	 * @return this.arity
	 */
	@Override
	public int arity() {
		return columns.size();
	}

	/**
	 * Returns this.expression.
	 * @return this.expressioin 
	 */
	public Expression expression() {
		return expr;
	}
	
	/**
	 * Returns this.columns.
	 * @return this.columns
	 */
	public List<IntExpression> columns() {
		return columns;
	}
	
	/**
	 * {@inheritDoc}
	 * @see kodkod.ast.Expression#accept(kodkod.ast.visitor.ReturnVisitor)
	 */
	@Override
	public <E, F, D, I> E accept(ReturnVisitor<E, F, D, I> visitor) {
		return visitor.visit(this);
	}

	/**
	 * {@inheritDoc}
	 * @see kodkod.ast.Node#accept(kodkod.ast.visitor.VoidVisitor)
	 */
	public void accept(VoidVisitor visitor) {
		visitor.visit(this);	
	}

	/**
	 * Returns the string representation of this expression.
	 * @return string representation of this expression
	 */
	public String toString() {
		return expr.toString() + columns.toString();
	}
		
}
