/**
 */
package myfsm.tests;

import junit.framework.TestCase;

import junit.textui.TestRunner;

import myfsm.MyfsmFactory;
import myfsm.Trans;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Trans</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class TransTest extends TestCase {

	/**
	 * The fixture for this Trans test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Trans fixture = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(TransTest.class);
	}

	/**
	 * Constructs a new Trans test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TransTest(String name) {
		super(name);
	}

	/**
	 * Sets the fixture for this Trans test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void setFixture(Trans fixture) {
		this.fixture = fixture;
	}

	/**
	 * Returns the fixture for this Trans test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Trans getFixture() {
		return fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	@Override
	protected void setUp() throws Exception {
		setFixture(MyfsmFactory.eINSTANCE.createTrans());
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#tearDown()
	 * @generated
	 */
	@Override
	protected void tearDown() throws Exception {
		setFixture(null);
	}

} //TransTest
