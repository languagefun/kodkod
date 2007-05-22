/**
 * 
 */
package tests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import kodkod.ast.Formula;
import kodkod.engine.Proof;
import kodkod.engine.Solution;
import kodkod.engine.Solver;
import kodkod.engine.satlab.SATFactory;
import kodkod.engine.ucore.MinTopStrategy;
import kodkod.engine.ucore.StrategyUtils;
import kodkod.instance.Bounds;
import examples.CeilingsAndFloors;
import examples.Dijkstra;
import examples.Pigeonhole;
import examples.tptp.GEO158;

/**
 * Tests the unsat core functionality.
 * @author Emina Torlak
 */
public final class UCoreTest {
	
	
	/**
	 * Returns a pigeonhole problem with the given parameters.
	 * @return a pigeonhole problem with the given parameters.
	 */
	static Problem pigeonhole(List<String> params) {
		if (params.size() < 2)
			usage();
		final Pigeonhole model = new Pigeonhole();
		try {
			final int p = Integer.parseInt(params.get(0));
			final int h = Integer.parseInt(params.get(1));
			final Formula show = model.declarations().and(model.pigeonPerHole());
			final Problem problem = new Problem(show, model.bounds(p,h));
			problem.solver.options().setSymmetryBreaking(p);
			return problem;
		} catch (NumberFormatException nfe) {
			usage();
		}
		return null;
	}
	
	/**
	 * Returns a ceilingsAndFloors problem with the given parameters.
	 * @return a ceilingsAndFloors problem with the given parameters.
	 */
	static Problem ceilingsAndFloors(List<String> params) {
		if (params.size() < 2) usage();
		
		final CeilingsAndFloors model = new CeilingsAndFloors();
				
		try {
			final int m = Integer.parseInt(params.get(0));
			final int p = Integer.parseInt(params.get(1));
			final Formula show = model.belowTooDoublePrime();
			return new Problem(show, model.bounds(m,p));
			
			
		} catch (NumberFormatException nfe) {
			usage();
		}
		
		return null;
	}
	
	/**
	 * Returns a dijkstra problem with the given parameters.
	 * @return a dijkstra problem with the given parameters.
	 */
	static Problem dijkstra(List<String> params) {
		if (params.size() < 3)
			usage();
		
		final Dijkstra model = new Dijkstra();
		
		try {
			final Formula noDeadlocks = model.dijkstraPreventsDeadlocksAssertion();
			final int states = Integer.parseInt(params.get(0));
			final int processes = Integer.parseInt(params.get(1));
			final int mutexes = Integer.parseInt(params.get(2));
			final Bounds bounds = model.bounds(states, processes, mutexes);
			return new Problem(noDeadlocks, bounds);
			
		} catch (NumberFormatException nfe) {
			usage();
		}
		return null;
	}
	
	/**
	 * Returns a geo158 problem with the given parameters.
	 * @return a geo158 problem with the given parameters.
	 */
	static Problem geo158(List<String> params) {
		if (params.size() < 1)
			usage();
		
		try {
			final int n = Integer.parseInt(params.get(0));
	
			final GEO158 model = new GEO158();
			final Formula f = model.axioms().and(model.someCurve());
			final Bounds b = model.bounds(n,n);
			return new Problem(f,b);
		} catch (NumberFormatException nfe) {
			usage();
		}
		return null;
	}
	
	private static void usage() {
		System.out.println("Usage: java tests.UCoreTest <command>");
		System.out.println("Available commands:" );
		System.out.println(" pigeonhole #pigeons #holes");
		System.out.println(" ceilingsAndFloors #man #platform");
		System.out.println(" dijkstra #states #processes #mutexes");
		System.out.println(" geo158 #atoms");
		System.exit(1);
	}
	
	/**
	 * Checks that the the given proof of unsatisfiablity for the given problem is
	 * correct (i.e. the conjunction of its core formulas is unsat).
	 */
	private static void checkCorrect(Problem problem, Set<Formula> core) {
		System.out.print("checking correctness ... ");
		Formula coreFormula = Formula.TRUE;
		for(Formula f : core) {
			coreFormula = coreFormula.and(f);
		}
		if (problem.solver.solve(coreFormula, problem.bounds).instance()==null ) {
			System.out.println("correct");
		} else {
			System.out.println("incorrect.  The found core is satisfiable!");
		}
	}
	
	/**
	 * Checks that the given proof of unsatisfiablity for the given problem is miminal.
	 * This method assumes that the given proof is correct.
	 */
	private static void checkMinimal(Problem problem, Set<Formula> core) {
		System.out.print("checking minimality ... ");
		final long start = System.currentTimeMillis();
		final Set<Formula> minCore = new LinkedHashSet<Formula>(core);
		for(Iterator<Formula> itr = minCore.iterator(); itr.hasNext();) {
			Formula f = itr.next();
			Formula noF = Formula.TRUE;
			for( Formula f1 : minCore ) {
				if (f!=f1)
					noF = noF.and(f1);
			}
			if (problem.solver.solve(noF, problem.bounds).instance()==null) {
				itr.remove();
			}			
		}
		final long end = System.currentTimeMillis();
		if (minCore.size()==core.size()) {
			System.out.println("minimal (" + (end-start) + " ms).");
		} else {
			System.out.println("not minimal (" + (end-start) + " ms). The minimal core has these " + minCore.size() + " formulas:");
			for(Formula f : minCore) {
				System.out.println(" " + f);
			}
		}
	}
	
	
	/**
	 * Usage:  java tests.UCoreTest <name of test> <scope parameters>
	 */
	public static void main(String[] args) {
		if (args.length < 1) usage();
		
		try {
			final Method method = UCoreTest.class.getDeclaredMethod(args[0], List.class);
			//System.out.println(method);
			final Problem problem = (Problem) method.invoke(null, Arrays.asList(args).subList(1, args.length));
			final Solution sol = problem.solve();
			
			System.out.println(sol);
			if (sol.outcome()==Solution.Outcome.UNSATISFIABLE) {
				final Proof proof = sol.proof();
				System.out.println("top-level formulas: " + StrategyUtils.topFormulas(problem.formula).size());
//				checkMinimal(problem,StrategyUtils.topFormulas(problem.formula));
				System.out.println("top-level formulas before min: " + proof.highLevelCore().size());
//				checkMinimal(problem,proof.highLevelCore());
//				minimize(problem,proof);
				long start = System.currentTimeMillis();
				proof.minimize(new MinTopStrategy(proof.log()));
//				proof.minimize(new HybridStrategy(proof.log()));
				
				long end = System.currentTimeMillis();
				final Set<Formula> topCore = proof.highLevelCore();
				System.out.println("top-level formulas after min: " + topCore.size());
				System.out.println("time: " + (end-start) + " ms");
				System.out.println("core: ");
				for(Formula f : topCore) {
					System.out.println(" "+f);
				}
				checkCorrect(problem, proof.highLevelCore());
				checkMinimal(problem, proof.highLevelCore());
				
//				System.out.print("low level min ... ");
//				start = System.currentTimeMillis();
//				proof.minimize(new CRRStrategy());
//				proof.minimize(new EmptyClauseConeStrategy());
//				proof.minimize(new NaiveStrategy());
//				end = System.currentTimeMillis();
//				System.out.println("(" +(end-start) + " ms)");
						
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			usage();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
	}
	
	private static final class Problem { 
		final Formula formula;
		final Bounds bounds;
		final Solver solver;
		
		Problem(Formula formula, Bounds bounds) {
			this.formula = formula; 
			this.bounds = bounds;
			this.solver = new Solver();
			solver.options().setLogTranslation(true);
			solver.options().setSolver(SATFactory.MiniSatProver);
//			solver.options().setSymmetryBreaking(0);
		}
		
		/**
		 * Solves this problem with translation logging on,
		 * using MiniSatProver.
		 * @return a solution to this problem generated with 
		 * with translation logging on, using MiniSatProver.
		 */
		Solution solve() {
			return solver.solve(formula, bounds);
		}
	}
	
}
