package tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(BooleanMatrixTest.class);
		suite.addTestSuite(TranslatorTest.class);
		suite.addTestSuite(EvaluatorTest.class);
		suite.addTestSuite(BooleanCircuitTest.class);
		suite.addTestSuite(SymmetryBreakingTest.class);
		suite.addTestSuite(SkolemizationTest.class);
		suite.addTestSuite(BugTests.class);
		suite.addTestSuite(ReductionAndProofTest.class);
		suite.addTestSuite(ExamplesTest.class);
		suite.addTestSuite(CardinalityTest.class);
		//$JUnit-END$
		return suite;
	}

}
