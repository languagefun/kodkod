package kodkod.engine.fol2sat;

import static kodkod.ast.BinaryFormula.Operator.AND;
import static kodkod.ast.BinaryFormula.Operator.IMPLIES;
import static kodkod.ast.BinaryFormula.Operator.OR;
import static kodkod.ast.RelationPredicate.Name.ACYCLIC;
import static kodkod.ast.RelationPredicate.Name.FUNCTION;
import static kodkod.ast.RelationPredicate.Name.TOTAL_ORDERING;

import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import kodkod.ast.BinaryFormula;
import kodkod.ast.ComparisonFormula;
import kodkod.ast.Comprehension;
import kodkod.ast.ConstantExpression;
import kodkod.ast.Decl;
import kodkod.ast.Decls;
import kodkod.ast.ExprToIntCast;
import kodkod.ast.Expression;
import kodkod.ast.IfExpression;
import kodkod.ast.IfIntExpression;
import kodkod.ast.IntComparisonFormula;
import kodkod.ast.IntToExprCast;
import kodkod.ast.MultiplicityFormula;
import kodkod.ast.Node;
import kodkod.ast.NotFormula;
import kodkod.ast.QuantifiedFormula;
import kodkod.ast.Relation;
import kodkod.ast.RelationPredicate;
import kodkod.ast.SumExpression;
import kodkod.ast.Variable;
import kodkod.ast.visitor.AbstractDetector;
import kodkod.ast.visitor.AbstractVoidVisitor;
import kodkod.util.collections.ArrayStack;
import kodkod.util.collections.IdentityHashSet;
import kodkod.util.collections.Stack;

/**
 * A node annotated with information about
 * structural sharing in its ast/dag.  The class
 * also provides utility methods for collecting
 * various information about annotated nodes.
 * 
 * @specfield node: N // annotated node
 * @specfield source: N.*children ->one Node // maps the subnodes of this.node to nodes from 
 *                                           // which they were derived by some transformation process
 *                                           // (e.g. skolemization, predicate inlining)  
 * @author Emina Torlak
 */ 
final class AnnotatedNode<N extends Node> {
	private final N node;
	private final Set<Node> sharedNodes;
	private final Map<Node, Node> source;
	
	/**
	 * Constructs a new annotator for the given node.
	 * @effects this.node' = node && this.source' = ((node.*children -> node.*children) & iden)
	 */
	AnnotatedNode(N node) {
		this.node = node;
		final SharingDetector detector = new SharingDetector();
		node.accept(detector);
		this.sharedNodes = Collections.unmodifiableSet(detector.sharedNodes());
		this.source = Collections.emptyMap();
	}
	
	
	/**
	 * Constructs a new annotator for the given node and source map.
	 * @effects this.node' = node && this.source' = ((node.*children -> node.*children) & iden) ++ source
	 */
	AnnotatedNode(N node, Map<Node,Node> source) {
		this.node = node;
		final SharingDetector detector = new SharingDetector();
		node.accept(detector);
		this.sharedNodes = Collections.unmodifiableSet(detector.sharedNodes());
		this.source = source;
	}
	
	/**
	 * Returns this.node.
	 * @return this.node
	 */
	final N node() {
		return node;
	}
	
	/**
	 * Returns the source of the the given descendent
	 * of this.node.
	 * @requires n in this.node.*children
	 * @return this.source[n]
	 */
	final Node sourceOf(Node n) {
		final Node d = source.get(n);
		return d==null ? n : d;
	}
	
	/**
	 * Returns the set of all non-leaf descendents
	 * of this.node that have more than one parent.
	 * @return {n: Node | some n.children && #(n.~children & this.node.*children) > 1 }
	 */
	final Set<Node> sharedNodes() { 
		return sharedNodes;
	}
	
	/**
	 * Returns the set of all relations at the leaves of this annotated node.
	 * @return Relation & this.node.*children
	 */
	final Set<Relation> relations() {
		final Set<Relation> relations = new IdentityHashSet<Relation>();
		final AbstractVoidVisitor visitor = new AbstractVoidVisitor() {
			private final Set<Node> visited = new IdentityHashSet<Node>(sharedNodes.size());
			protected boolean visited(Node n) {
				return sharedNodes.contains(n) && !visited.add(n);
			}
			public void visit(Relation relation) {
				relations.add(relation);
			}
		};
		node.accept(visitor);
		return relations;
	}
	
	/**
	 * Returns true if this.node contains a child whose meaning depends on 
	 * integer bounds (i.e. an ExprToIntCast node with SUM operator or an IntToExprCast node or Expression.INTS constant).
	 * @return true if this.node contains a child whose meaning depends on 
	 * integer bounds (i.e. an ExprToIntCast node with SUM operator or an IntToExprCast node or Expression.INTS constant).
	 */
	final boolean usesIntBounds() {
		final AbstractDetector detector = new AbstractDetector(sharedNodes) {
			public Boolean visit(IntToExprCast expr) {
				return cache(expr, Boolean.TRUE);
			}
			public Boolean visit(ExprToIntCast intExpr) {
				if (intExpr.op()==ExprToIntCast.Operator.CARDINALITY)
					super.visit(intExpr);
				return cache(intExpr, Boolean.TRUE);
			}
			public Boolean visit(ConstantExpression expr) {
				return expr==Expression.INTS ? Boolean.TRUE : Boolean.FALSE;
			}
		};
		return (Boolean)node.accept(detector);
	}
	
	/**
	 * Returns a map of RelationPredicate names to sets of top-level relation predicates with
	 * the corresponding names in this.node.  
	 * @return a map of RelationPredicate names to sets of top-level relation predicates with
	 * the corresponding names in this.node.  A predicate is considered 'top-level'  
	 * if it is a component of the top-level conjunction, if any, of this.node.  
	 */
	final Map<RelationPredicate.Name, Set<RelationPredicate>> predicates() {
		final PredicateCollector collector = new PredicateCollector(sharedNodes);
		node.accept(collector);
		return collector.preds;
	}
	
	/**
	 * Returns a Detector that will return TRUE when applied to a descendent
	 * of this.node iff the descendent contains a quantified formula.
	 * @return a Detector that will return TRUE when applied to a descendent
	 * of this.node iff the descendent contains a quantified formula.
	 */
	final AbstractDetector quantifiedFormulaDetector() {
		return new AbstractDetector(sharedNodes) {
			public Boolean visit(QuantifiedFormula quantFormula) {
				return cache(quantFormula, true);
			}
		};
	}
	
	/**
	 * Returns a Detector that will return TRUE when applied to a descendent
	 * of this.node iff the descendent contains a free variable.
	 * @return a Detector that will return TRUE when applied to a descendent
	 * of this.node iff the descendent contains a free variable.
	 */
	final AbstractDetector freeVariableDetector() {
		return new FreeVariableDetector(sharedNodes);
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final StringBuilder ret =  new StringBuilder();
		ret.append("node: ");
		ret.append(node);
		ret.append("\nshared nodes: ");
		ret.append(sharedNodes);
		ret.append("\nsources: ");
		ret.append(source);
		return ret.toString();
	}
	
	/**
	 * Detects shared non-leaf descendents of a given node.
	 * 
	 * @specfield node: Node // node to which the analyzer is applied
	 */
	private static final class SharingDetector extends AbstractVoidVisitor {
		/* maps each internal node with more than one parent to TRUE and all
		 * other internal nodes to FALSE */
		final IdentityHashMap<Node,Boolean> sharingStatus;
		/* @invariant numShareNodes = #sharingStatus.TRUE */
		int numSharedNodes;
		
		SharingDetector() {
			sharingStatus = new IdentityHashMap<Node,Boolean>();
		}
		
		/**
		 * Returns the shared internal nodes of this.node.  This method should
		 * be called only after this visitor has been applied to this.node.
		 * @return {n: Node | #(n.~children & node.*children) > 1 }
		 */
		IdentityHashSet<Node> sharedNodes() {
			final IdentityHashSet<Node> shared = new IdentityHashSet<Node>(numSharedNodes);
			for(Map.Entry<Node,Boolean> entry : sharingStatus.entrySet()) {
				if (entry.getValue()==Boolean.TRUE)
					shared.add(entry.getKey());
			}
			return shared;
		}
		
		/**
		 * Records the visit to the given node in the status map.
		 * If the node has not been visited before, it is mapped
		 * to Boolean.FALSE and false is returned.  Otherwise, 
		 * it is mapped to Boolean.TRUE and true is returned.
		 * The first time a Node is mapped to true, numSharedNodes
		 * is incremented by one.
		 * @effects no this.shared[node] => this.shared' = this.shared + node->FALSE,
		 *          this.shared[node] = FALSE => this.shared' = this.shared + node->TRUE,
		 *          this.shared' = this.shared
		 * @return this.shared'[node]
		 */
		protected final boolean visited(Node node) {
			Boolean status = sharingStatus.get(node);
			if (!Boolean.TRUE.equals(status)) {
				if (status==null) {
					status = Boolean.FALSE;
				} else { // status == Boolean.FALSE
					status = Boolean.TRUE;
					numSharedNodes++;
				}
				sharingStatus.put(node,status);
			}
			return status;
		}
	}

	/**
	 * A visitor that detects free variables of a node.
	 * @author Emina Torlak
	 */
	private static final class FreeVariableDetector extends AbstractDetector {
		/* Holds the variables that are currently in scope, with the
		 * variable at the top of the stack being the last declared variable. */
		
		private final Stack<Variable> varsInScope = new ArrayStack<Variable>();
		
		/**
		 * Constructs a new free variable detector.
		 */
		FreeVariableDetector(Set<Node> sharedNodes) {
			super(sharedNodes);
		}
		
		/**
		 * Visits the given comprehension, quantified formula, or sum expression.  
		 * The method returns TRUE if the creator body contains any 
		 * variable not bound by the decls; otherwise returns FALSE.  
		 */
		@SuppressWarnings("unchecked")
		private Boolean visit(Node creator, Decls decls, Node body) {
			Boolean ret = lookup(creator);
			if (ret!=null) return ret;
			boolean retVal = false;
			for(Decl decl : decls) {
				retVal = decl.expression().accept(this) || retVal;
				varsInScope.push(decl.variable());
			}
			retVal = ((Boolean)body.accept(this)) || retVal;
			for(int i = decls.size(); i > 0; i--) {
				varsInScope.pop();
			}
			return cache(creator, retVal);
		}
		/**
		 * Returns TRUE if the given variable is free in its parent, otherwise returns FALSE.
		 * @return TRUE if the given variable is free in its parent, otherwise returns FALSE.
		 */
		public Boolean visit(Variable variable) {
			return Boolean.valueOf(varsInScope.search(variable)<0);
		}	
		public Boolean visit(Decl decl) {
			Boolean ret = lookup(decl);
			if (ret!=null) return ret;
			return cache(decl, decl.expression().accept(this));
		}	
		public Boolean visit(Comprehension comprehension) {
			return visit(comprehension, comprehension.declarations(), comprehension.formula());
		}		
		public Boolean visit(SumExpression intExpr) {
			return visit(intExpr, intExpr.declarations(), intExpr.intExpr());
		}
		public Boolean visit(QuantifiedFormula qformula) {
			return visit(qformula, qformula.declarations(), qformula.formula());
		}
	}
	
	/**
	 * A visitor that detects and collects
	 * top-level relation predicates; i.e. predicates that
	 * are components in the top-level conjunction, if any, on ANY
	 * path starting at the root.
	 */
	private static final class PredicateCollector extends AbstractVoidVisitor {
		protected boolean negated;
		private final Set<Node> sharedNodes;
		/* if a given node is not mapped at all, it means that it has not been visited;
		 * if it is mapped to FALSE, it has been visited with negated=FALSE, 
		 * if it is mapped to TRUE, it has been visited with negated=TRUE,
		 * if it is mapped to null, it has been visited with both values of negated. */
		private final Map<Node,Boolean> visited;	
		/* holds the top level predicates at the the end of the visit*/
		final EnumMap<RelationPredicate.Name, Set<RelationPredicate>> preds;
		/**
		 * Constructs a new collector.
		 * @effects this.negated' = false
		 */
		PredicateCollector(Set<Node> sharedNodes) {
			this.sharedNodes = sharedNodes;
			this.visited = new IdentityHashMap<Node,Boolean>();
			this.negated = false;
			preds = new EnumMap<RelationPredicate.Name, Set<RelationPredicate>>(RelationPredicate.Name.class);	
			preds.put(ACYCLIC, new IdentityHashSet<RelationPredicate>(4));
			preds.put(TOTAL_ORDERING, new IdentityHashSet<RelationPredicate>(4));
			preds.put(FUNCTION, new IdentityHashSet<RelationPredicate>(8));
		}
		/**
		 * Returns true if n has already been visited with the current value of the
		 * negated flag; otherwise returns false.
		 * @effects records that n is being visited with the current value of the negated flag
		 * @return true if n has already been visited with the current value of the
		 * negated flag; otherwise returns false.
		 */
		@Override
		protected final boolean visited(Node n) {
			if (sharedNodes.contains(n)) {
				if (!visited.containsKey(n)) { // first visit
					visited.put(n, Boolean.valueOf(negated));
					return false;
				} else {
					final Boolean visit = visited.get(n);
					if (visit==null || visit==negated) { // already visited with same negated value
						return true; 
					} else { // already visited with different negated value
						visited.put(n, null);
						return false;
					}
				}
			}
			return false;
		}
		/**
		 * Calls visited(comp); comp's children are not top-level formulas
		 * so they are not visited.
		 */
		public void visit(Comprehension comp) {
			visited(comp);
		}
		/**
		 * Calls visited(ifexpr); ifexpr's children are not top-level formulas
		 * so they are not visited.
		 */
		public void visit(IfExpression ifexpr) {
			visited(ifexpr);
		}
		/**
		 * Calls visited(ifexpr); ifexpr's children are not top-level formulas
		 * so they are not visited.
		 */
		public void visit(IfIntExpression ifexpr) {
			visited(ifexpr);
		}
		/**
		 * Calls visited(intComp); intComp's children are not top-level formulas
		 * so they are not visited.
		 */
		public void visit(IntComparisonFormula intComp) {
			visited(intComp);
		}
		/**
		 * Calls visited(quantFormula); quantFormula's children are not top-level formulas
		 * so they are not visited.
		 */
		public void visit(QuantifiedFormula quantFormula) {
			visited(quantFormula);
		}
		/**
		 * Visits the children of the given formula if it has not been visited already with
		 * the given value of the negated flag and if binFormula.op==IMPLIES && negated or
		 * binFormula.op==AND && !negated or binFormula.op==OR && negated.  Otherwise does nothing.
		 * @see kodkod.ast.visitor.AbstractVoidVisitor#visit(kodkod.ast.BinaryFormula)
		 */
		public void visit(BinaryFormula binFormula) {
			if (!visited(binFormula)) {
				final BinaryFormula.Operator op = binFormula.op();
			
				if ((!negated && op==AND) || (negated && op==OR)) { // op==AND || op==OR
					binFormula.left().accept(this);
					binFormula.right().accept(this);
				} else if (negated && op==IMPLIES) { // !(a => b) = !(!a || b) = a && !b
					negated = !negated;
					binFormula.left().accept(this);
					negated = !negated;
					binFormula.right().accept(this);
				} 
			}
		}
		/**
		 * Visits the children of the child of the child formula, with
		 * the negation of the current value of the negated flag, 
		 * if it has not already been visited 
		 * with the current value of this.negated; otherwise does nothing.
		 */
		public void visit(NotFormula not) {
			if (!visited(not)) {
				negated = !negated;
				not.formula().accept(this);
				negated = !negated;
			}
			
		}
		/**
		 * Calls visited(compFormula); compFormula's children are not top-level formulas
		 * so they are not visited.
		 */
		public void visit(ComparisonFormula compFormula) {
			visited(compFormula);
		}
		/**
		 * Calls visited(multFormula); multFormula's child is not top-level formulas
		 * so it is not visited.
		 */
		public void visit(MultiplicityFormula multFormula) {
			visited(multFormula);
		}
		/**
		 * Records the visit to this predicate if it is not negated.
		 */
		public void visit(RelationPredicate pred) {
			if (!visited(pred)) {
				if (!negated) {
					preds.get(pred.name()).add(pred);
				}
			}
		}
	}
}
