package kg.apc.jmeter.control;

import java.io.Serializable;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.StringProperty;

public class WeightedProbability
		extends AbstractTestElement
		implements Serializable
{
	private static final long serialVersionUID = -2798615433770808632L;

	/** Name of the Test Element **/
	public static final String WEIGHTED_PROBABILITY_NAME = "WeightedProbability.name";
	
	/** Weight of Test Element in probability as a short **/
	public static final String WEIGHTED_PROBABILITY_WEIGHT = "WeightedProbability.weight";
	
	public static final String WEIGHTED_PROBABILITY_CHILD_NODE_IDX = "WeightedProbability.childNodeIdx";
	
	public static final short DFLT_WEIGHT = (short)0;
	
	public static final int DFLT_IDX = -1;
		
	public WeightedProbability(String testElementName, short weight, int childNodeIdx) {
		if (testElementName != null) {
			setProperty(new StringProperty(WEIGHTED_PROBABILITY_NAME, testElementName));
		}
		
		setProperty(new IntegerProperty(WEIGHTED_PROBABILITY_WEIGHT, weight));
		setProperty(new IntegerProperty(WEIGHTED_PROBABILITY_CHILD_NODE_IDX, childNodeIdx));
	}
	
	public WeightedProbability(String testElementName, int childNodeIdx) {
		this(testElementName, DFLT_WEIGHT, childNodeIdx);
	}
	
	public WeightedProbability(String testElementName) {
		this(testElementName, DFLT_WEIGHT, DFLT_IDX);
	}
	
	public WeightedProbability() {
		this(null, DFLT_WEIGHT, DFLT_IDX);
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
	
	public int getChildNodeIndex() {
		return getPropertyAsInt(WEIGHTED_PROBABILITY_CHILD_NODE_IDX);
	}
	
	public void setChildNodeIndex(int idx) {
		setProperty(new IntegerProperty(WEIGHTED_PROBABILITY_CHILD_NODE_IDX, idx));
	}
	
	public boolean isElementEnabled() {
		JMeterTreeNode guiNode = (JMeterTreeNode)GuiPackage.getInstance().getCurrentNode();
		if (guiNode.getTestElement() instanceof WeightedDistributionController && getChildNodeIndex() > DFLT_IDX) {
			return ((JMeterTreeNode)guiNode.getChildAt(getChildNodeIndex())).isEnabled();
		}
		return false;
	}
}
