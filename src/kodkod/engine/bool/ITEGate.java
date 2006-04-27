/**
 * 
 */
package kodkod.engine.bool;

import java.util.Iterator;
import java.util.Set;

import kodkod.util.Ints;
import kodkod.util.Iterators;

/**
 * Represents an if-then-else gate.
 * 
 * @specfield ifFormula: BooleanFormula
 * @specfield thenFormula: BooleanFormula
 * @specfield elseFormula: BooleanFormula
 * @invariant inputs = 0->ifFormula + 1->thenFormula + 2->elseFormula
 * @invariant this.label > 0
 * @invariant this.op = Operator.ITE
 * @specfield all input: inputs | this.label > |input.label|
 * @author Emina Torlak
 */
public final class ITEGate extends BooleanFormula {
	private final BooleanFormula[] inputs;
	private final int label, hashcode, labelhash;
	
	/**
	 * Constructs a new ITEGate from the given formulas and label.
	 * @requires label >= 0 && null !in ifFormula + thenFormula + elseFormula
	 * @requires hashcode = ITE.hash(ifFormula, thenFormula, elseFormula)
	 * @effects this.label' = label && this.ifFormula' = ifFormula &&
	 * this.thenFormula' = thenFormula && this.elseFormula' = elseFormula
	 * @throws NullPointerException - owner = null
	 */
	ITEGate(int label, int hashcode, BooleanFormula ifFormula, BooleanFormula thenFormula, BooleanFormula elseFormula) {
		super(null);
		assert label >= 0;
		this.label = label;
		this.labelhash = Ints.superFastHash(label);
		this.hashcode = hashcode;
		this.inputs = new BooleanFormula[3];
		inputs[0] = ifFormula;
		inputs[1] = thenFormula;
		inputs[2] = elseFormula;
	}

	/**
	 * Returns a hash of this.label
	 * @return a hash of this.label
	 * @see kodkod.engine.bool.BooleanFormula#hash(kodkod.engine.bool.MultiGate.Operator)
	 */
	@Override
	int hash(Operator op) {
		return labelhash;
	}
	
	/**
	 * Returns an iterator over this.inputs
	 * @return returns an iterator over this.inputs
	 * @see kodkod.engine.bool.BooleanFormula#iterator()
	 */
	@Override
	public Iterator<BooleanFormula> iterator() {
		return Iterators.iterate(inputs);
	}
	
	/** 
	 * Returns 3.
	 * @return 2
	 * @see kodkod.engine.bool.BooleanFormula#size()
	 */
	@Override
	public int size() {
		return 3;
	}

	/**
	 * Returns this.label
	 * @return this.label
	 * @see kodkod.engine.bool.BooleanValue#label()
	 */
	@Override
	public int label() {
		return label;
	}

	/**
	 * Passes this value and the given
	 * argument value to the visitor, and returns the resulting value.
	 * @return the value produced by the visitor when visiting this node
	 * with the given argument.
	 * @see kodkod.engine.bool.BooleanValue#accept(kodkod.engine.bool.BooleanVisitor, A)
	 */
	@Override
	public <T, A> T accept(BooleanVisitor<T, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

	/**
	 * Returns a string representation of this ITE gate.
	 * @return a string representation of this ITE gate.
	 */
	public String toString() {
		return "(" + inputs[0] + "?" + inputs[1] + ":" + inputs[2] + ")";
	}

	/**
	 * Returns the hashcode for this if-then-else gate.
	 * @return the hashcode for this gate.
	 */
	public int hashCode() {
		return hashcode;
	}
	/**
	 * Returns Operator.ITE.
	 * @return Operator.ITE
	 */
	@Override
	public kodkod.engine.bool.Operator op() {
		return kodkod.engine.bool.Operator.ITE;
	}

	/**
	 * Returns this.inputs[i].
	 * @return this.inputs[i]
	 * @throws IndexOutOfBoundsException - 0 < i || i > 2
	 */
	@Override
	public BooleanFormula input(int i) {
		if (i < 0 || i > 2)
			throw new IndexOutOfBoundsException();
		return inputs[i];
	}
	
	/**
	 * Returns an integer k' such that 0 < |k'| < k and |k'| is the number of flattening
	 * steps that need to be taken to determine that f is (not) an input to this circuit.
	 * A positive k' indicates that f is found to be an input to this circuit in k' steps.
	 * A negative k' indicatets that f is not an input to this circuit, when it is flattened
	 * using at most k steps.  
	 * @requires k > 0
	 * @return this=f => 1, k>2 && f in this.inputs[int] => 3, -3
	 */
	@Override
	int contains(Operator op, BooleanFormula f, int k) {
		assert k > 0;
		if (f==this) return 1;
		else if (op != Operator.ITE || k < 3) return -1;
		else 
			return (inputs[0]==f || inputs[1]==f || inputs[2]==f) ? 3 : -3;
	}
		
	/**
	 * Flattens this circuit with respect to the given operator into 
	 * the provided set.  
	 * @requires k > 0
	 * @effects op = Operator.ITE && k> 2 => flat.elts' = flat.elts + this.inputs[ints], 
	 *          flat.elts' = flat.elts + this
	 */
	@Override
	void flatten(Operator op, Set<BooleanFormula> flat, int k) {
		assert k > 0;
		if (op==Operator.ITE && k > 2) {
			flat.add(inputs[0]);
			flat.add(inputs[1]);
			flat.add(inputs[2]);
		} else {
			flat.add(this);
		}
	}	
}
