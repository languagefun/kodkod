/**
 * 
 */
package kodkod.engine.ucore;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import kodkod.engine.satlab.Clause;
import kodkod.engine.satlab.ResolutionTrace;
import kodkod.engine.satlab.TraversalStrategy;
import kodkod.util.collections.Containers;

/**
 * A cnf-level CRR strategy that uses a distance
 * heuristic to pick the clauses to be excluded from the core.  
 * Instances of this strategy can be configured
 * to pick the clause that is either closest to or furthest from
 * the conflict close.  (A clause is considered to be furthest from
 * the conflict if its shortest path to the conflict is longer than
 * all other clauses' shortest paths to the conflict.)  
 * @specfield traces: [0..)->ResolutionTrace
 * @specfield nexts: [0..)->Set<Clause>
 * @invariant traces.ResolutionTrace = nexts.Set<Clause>
 * @invariant all i: [1..) | some traces[i] => some traces[i-1]
 * @invariant all i: [0..#nexts) | nexts[i] in traces[i].conflict.^antecedents
 * @invariant no disj i,j: [0..#nexts) | traces[i] = traces[j] && nexts[i] = nexts[j]
 * @author Emina Torlak
 */
public final class DistExtremumCRRStrategy extends CRRStrategy {
	private final boolean closest;
	/**
	 * Constructs a new instance of DistExtremumCRRStrategy that
	 * picks either the closest (closest = true) or furthest
	 * clause from the conflict (closest = false).
	 */
	public DistExtremumCRRStrategy(boolean closest) {
		this.closest = closest;
	}
	/**
	 * Returns an iterator that traverses the core clauses in the given trace
	 * according to their distance from the conflict clause.
	 * @return an iterator that traverses the core clauses in the given trace
	 * according to their distance from the conflict clause.
	 * @see kodkod.engine.ucore.CRRStrategy#order(kodkod.engine.satlab.ResolutionTrace)
	 */
	@Override
	protected Iterator<Clause> order(ResolutionTrace trace) {
		final Clause conflict = trace.conflict();
		final int maxIndex =  trace.maxIndex();
		final Clause[] core = new Clause[trace.core().size()];
		
		// traverse the dag in a topologically sorted order, and compute shortest paths
		final int[] dist = new int[maxIndex+1];
		java.util.Arrays.fill(dist, 0, maxIndex+1, Integer.MAX_VALUE);
		dist[conflict.index()] = 0;
		
		int coreIndex = 0;
		for(Iterator<Clause> itr = trace.iterator(TraversalStrategy.TOPOLOGICAL); itr.hasNext();) {
			Clause next = itr.next();
			if (next.learned()) {
				for(Clause ante : next.antecedents()) {
					if (dist[ante.index()] > dist[next.index()]+1) { // relax
						dist[ante.index()] = dist[next.index()]+1;
					}
				}
			} else { // core
				core[coreIndex++] = next;
			}
		}
		Arrays.sort(core, new Comparator<Clause>() {
			public int compare(Clause arg0, Clause arg1) {
				return dist[arg0.index()] - dist[arg1.index()];
			}});

		return closest ? Containers.iterate(core) : Containers.iterate(core.length-1, -1, core);
	}

}