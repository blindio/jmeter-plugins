package kg.apc.jmeter.control;

import java.io.Serializable;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.StringProperty;

public class WeightedProbability
		extends AbstractTestElement
		implements Serializable
{
	private static final long serialVersionUID = -2798615433770808632L;

	/** Name of the Test Element **/
	public static final String WEIGHTED_PROBABILITY_NAME = "WeightedTestElementProbability.name";
	
	/** Weight of Test Element in probability as a short **/
	public static final String WEIGHTED_PROBABILITY_WEIGHT = "WeightedTestElementProbability.weight";
	
	public static final short DFLT_WEIGHT = (short)0;
		
	public WeightedProbability(String testElementName, short weight) {
		if (testElementName != null) {
			setProperty(new StringProperty(WEIGHTED_PROBABILITY_NAME, testElementName));
		}
		
		setProperty(new IntegerProperty(WEIGHTED_PROBABILITY_WEIGHT, weight));
	}
	
	public WeightedProbability(String testElementName) {
		this(testElementName, DFLT_WEIGHT);
	}
	
	public WeightedProbability() {
		this(null, DFLT_WEIGHT);
	}

	@Override
	public String getName() {
		return getPropertyAsString(WEIGHTED_PROBABILITY_NAME);
	}
	
	@Override
	public void setName(String testElementName) {
		setProperty(new StringProperty(WEIGHTED_PROBABILITY_NAME, testElementName));
	}
	
	public short getWeight() {
		return (short)getPropertyAsInt(WEIGHTED_PROBABILITY_WEIGHT);
	}
	
	public void setWeight(short weight) {
		setProperty(new IntegerProperty(WEIGHTED_PROBABILITY_WEIGHT, weight));
	}
}
