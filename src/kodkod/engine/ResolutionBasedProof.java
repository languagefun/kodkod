package kodkod.engine;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import kodkod.ast.Node;
import kodkod.ast.Variable;
import kodkod.ast.visitor.AbstractVoidVisitor;
import kodkod.engine.fol2sat.RecordFilter;
import kodkod.engine.fol2sat.TranslationLog;
import kodkod.engine.fol2sat.TranslationRecord;
import kodkod.engine.satlab.ReductionStrategy;
import kodkod.engine.satlab.ResolutionTrace;
import kodkod.engine.satlab.SATProver;
import kodkod.engine.ucore.StrategyUtils;
import kodkod.instance.TupleSet;
import kodkod.util.collections.IdentityHashSet;
import kodkod.util.ints.IntSet;

/**
 * A proof of unsatisfiability based on a {@linkplain ResolutionTrace resolution trace} produced
 * by a {@linkplain SATProver SATProver}.
 * 
 * @author Emina Torlak
 */
final class ResolutionBasedProof extends Proof {
	private SATProver solver;
	private RecordFilter coreFilter;
	/**
	 * Constructs a new ResolutionRefutation that will extract the 
	 * unsatisfiable core for log.formula from the given solver.  
	 * @requires solver.solve() has been called and it returned false.
	 * @requires log.formula is the formula whose translation
	 * resulted in the given SATProver
	 * @effects this.formula' = log.formula
	 */
	ResolutionBasedProof(SATProver solver, TranslationLog log) {
		super(log);
		this.solver = solver;
		this.coreFilter = null;
	}
	
	/**
	 * Returns the connected core based on the given set of 
	 * coreVariables.  
	 * @requires coreVariables = StrategyUtils.coreVars(solver.proof());
	 * @return let coreNodes = (this.log.records & literal.{i: int | abs(i) in coreVariables}).node | 
	 *  {n: coreNodes  | some s: set coreNodes | 
	 *   n + this.log.formula in s and (s - this.log.formula).~children in s }
	 */
	private Set<Node> connectedCore(final IntSet coreVars) {
		final Set<Node> coreNodes = new IdentityHashSet<Node>();
		final RecordFilter filter = new RecordFilter() {
			public boolean accept(Node node, int literal, Map<Variable,TupleSet> env) {
				return coreVars.contains(StrictMath.abs(literal));
			}
		};
		for(Iterator<TranslationRecord> iter = log().replay(filter); iter.hasNext(); ) {
			coreNodes.add(iter.next().node());
		}
		final Set<Node> connected = new IdentityHashSet<Node>();
		final AbstractVoidVisitor traverser = new AbstractVoidVisitor() {
			final Set<Node> visited = new IdentityHashSet<Node>();
			/**
			 * Returns true if the given node has been visited before or if 
			 * it is not contained in this.nodes set.  Otherwise adds 
			 * the node to the connected set and returns false.
			 * @effects this.visited' = this.visited + n
			 * @effects n !in this.visited && n in coreNodes => 
			 *  connected' = connected + n else connected' = connected
			 * @return n in visited || n !in coreNodes
			 */
			protected boolean visited(Node n) {
				if (visited.add(n) && coreNodes.contains(n)) {
					connected.add(n);
					return false;
				}
				return true;
			}
		};
		log().formula().accept(traverser);
		return connected;
	}
	
	/**
	 * {@inheritDoc}
	 * @see kodkod.engine.Proof#core()
	 */
	public final Iterator<TranslationRecord> core() { 
		if (coreFilter == null) {
			coreFilter = new RecordFilter() {
				final IntSet coreVariables = StrategyUtils.coreVars(solver.proof());
				final Set<Node> coreNodes = connectedCore(coreVariables);
				public boolean accept(Node node, int literal, Map<Variable,TupleSet> env) {
					return coreNodes.contains(node) && coreVariables.contains(StrictMath.abs(literal));
				}
			};
		}
		return log().replay(coreFilter); 
	}
	
	/**
	 * {@inheritDoc}
	 * @see kodkod.engine.Proof#minimize(kodkod.engine.satlab.ReductionStrategy)
	 */
	public void minimize(ReductionStrategy strategy) {
		solver.reduce(strategy);
		coreFilter = null;
	}
}