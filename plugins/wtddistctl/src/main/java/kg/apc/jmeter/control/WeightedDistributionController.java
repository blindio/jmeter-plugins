package kg.apc.jmeter.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.control.InterleaveControl;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.ThreadLocalRandom;

public class WeightedDistributionController
		extends InterleaveControl
{
	private static final long serialVersionUID = 8554248250211263894L;
	
	private static final String WEIGHTED_PROBABILITIES = "WeightedDistributionController.weightedProbabilities";
	
	private int maxCumulativeProbability;
	
	private transient Map<JMeterTreeNode, WeightedProbability> nodeToProbMap;
	
	//private Integer[] chldNodeIdxToSubCtlrIdxMap;
	
	private transient JMeterTreeNode node;

	public static final int DFLT_WEIGHT = 0;

	/** Weight of Test Element in probability as a short **/
	public static final String WEIGHT = "WeightedProbability.weight";
	
	public WeightedDistributionController() {
		setWeightedProbabilities(new ArrayList<JMeterProperty>());
		nodeToProbMap = null;
		node = null;
	}
	
	
	
	public JMeterTreeNode getNode() {
		return node;
	}
	
	public WeightedProbability getWeightedProbabilityForNode(JMeterTreeNode node, int childNodeIdx) {
		WeightedProbability prob = nodeToProbMap.get(node);
		if (prob == null) {
			prob = new WeightedProbability(node, childNodeIdx);
			addWeightedProbability(prob);
		} else {
			if (!prob.getName().equals(node.getName())) {
				prob.setName(node.getName());
			}
			
			if (!(prob.getChildNodeIndex() == childNodeIdx)) {
				prob.setChildNodeIndex(childNodeIdx);
			}
		}
		
		return prob;
	}
	
	public boolean buildNodeProbabilityMap() {
		JMeterTreeNode wdcNode = GuiPackage.getInstance().getNodeOf(this);
		if (node == null && wdcNode != null) {
			node = wdcNode;
			nodeToProbMap = new HashMap<JMeterTreeNode, WeightedProbability>();
			PropertyIterator iter = iterator();
			while(iter.hasNext()) {
				WeightedProbability currProb = (WeightedProbability)((TestElementProperty)iter.next()).getObjectValue();
				
				try {
					JMeterTreeNode currNode = (JMeterTreeNode)node.getChildAt(currProb.getChildNodeIndex());
					if (currNode.getName().equals(currProb.getName())) {
						currProb.setNode(currNode);
						nodeToProbMap.put(currNode, currProb);
					}
				} catch (IndexOutOfBoundsException e) {
					
				}
				
			}
		}
		return node != null;
	}
	
	public void setMaxCumulativeProbability(int value) {
		maxCumulativeProbability = value;
	}
	
	public void addToMaxCumulativeProbability(int value) {
		maxCumulativeProbability += value;
	}
	
	public void subtractFromMaxCumulativeProbability(int value) {
		maxCumulativeProbability -= value;
	}
	
	public void resetMaxCumulativeProbability() {
		setMaxCumulativeProbability(0);
	}
	
	public int getMaxCumulativeProbability() {
		return maxCumulativeProbability;
	}
	
	public void reset() {
		/*
		if (getNode() != null) {
			chldNodeIdxToSubCtlrIdxMap = new Integer[getNode().getChildCount()];
		}
		*/
		resetMaxCumulativeProbability();
		setWeightedProbabilities(new ArrayList<JMeterProperty>());
	}
	
	public CollectionProperty getWeightedProbabilities() {
		return (CollectionProperty)getProperty(WEIGHTED_PROBABILITIES);
	}
	
	public void setWeightedProbabilities(Collection<JMeterProperty> weightedProbs) {
		setProperty(new CollectionProperty(WEIGHTED_PROBABILITIES, weightedProbs));
	}
	
	public TestElementProperty getTestElementPropertyBySubControllerIdx(int subCtlrIdx) {
		return (TestElementProperty)getWeightedProbabilities().get(subCtlrIdx);
	}
	
	public WeightedProbability getWeightedProbabilityBySubControllerIdx(int subCtlrIdx) {
		return (WeightedProbability)getTestElementPropertyBySubControllerIdx(subCtlrIdx).getObjectValue();
	}	
	
	/*
	public TestElementProperty getTestElementPropertyByChildNodeIdx(int childNodeIdx) {
		if (chldNodeIdxToSubCtlrIdxMap != null && chldNodeIdxToSubCtlrIdxMap.length > 0) {
			Integer subCtlrIdx = chldNodeIdxToSubCtlrIdxMap[childNodeIdx];
			if (subCtlrIdx != null) {
				return (TestElementProperty)getWeightedProbabilities().get(subCtlrIdx);
			}
		}
		
		PropertyIterator iter = getWeightedProbabilities().iterator();
		while (iter.hasNext()) {
			TestElementProperty currProp = (TestElementProperty)iter.next();
			if (((WeightedProbability)currProp.getObjectValue()).getChildNodeIndex() == childNodeIdx) {
				return currProp;
			}
		}
		
		return null;
	}
	
	public TestElementProperty getTestElementPropertyByChildNodeIdx(int childNodeIdx, String propName) {
		TestElementProperty prop = getTestElementPropertyByChildNodeIdx(childNodeIdx);
		if (prop != null && prop.getName().equals(propName)) {
			return prop;
		}
		return null;
	}
	
	public WeightedProbability getWeightedProbabilityByChildNodeIdx(int childNodeIdx) {
		TestElementProperty prop = getTestElementPropertyByChildNodeIdx(childNodeIdx);
		return prop == null ? null : (WeightedProbability) prop.getObjectValue();
	}	

	public WeightedProbability getWeightedProbabilityByChildNodeIdx(int childNodeIdx, String propName) {
		TestElementProperty prop = getTestElementPropertyByChildNodeIdx(childNodeIdx, propName);
		return prop == null ? null : (WeightedProbability) prop.getObjectValue();
	}	
	*/
	
	public void addWeightedProbability(WeightedProbability weightedProb) {
		TestElementProperty prop = new TestElementProperty(weightedProb.getName(), weightedProb);

		if (isRunningVersion()) {
			setTemporary(prop);
		}
		
		if (weightedProb.isElementEnabled() == true) {
			addToMaxCumulativeProbability(weightedProb.getWeight());
		}
		
		/*
		if (chldNodeIdxToSubCtlrIdxMap != null) {
			chldNodeIdxToSubCtlrIdxMap[weightedProb.getChildNodeIndex()] = getWeightedProbabilities().size();
		}
		*/
		
		getWeightedProbabilities().addProperty(prop);
	}
	
	public PropertyIterator iterator() {
		CollectionProperty probs = getWeightedProbabilities();
		return probs == null ? null : probs.iterator();
	}
	
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        PropertyIterator iter = iterator();
        while (iter.hasNext()) {
            WeightedProbability prob = (WeightedProbability) iter.next().getObjectValue();
            str.append(String.format("%s=%d", prob.getName(), prob.getWeight()));
            if (iter.hasNext()) {
                str.append("&"); //$NON-NLS-1$
            }
        }
        return str.toString();
    }
    
    @Override
    protected void resetCurrent() {
        current = determineCurrentTestElement();
    }

    @Override
    protected void incrementCurrent() {
        super.incrementCurrent();
        current = determineCurrentTestElement();
    }
    
    private int determineCurrentTestElement () {
    	if (maxCumulativeProbability > 0) {
    		 int currentRandomizer = ThreadLocalRandom.current().nextInt(maxCumulativeProbability);
    		 List<TestElement> subControllers = getSubControllers();
    		 for (int elemIdx = 0; elemIdx < subControllers.size(); elemIdx++) {
    			 WeightedProbability prob = getWeightedProbabilityBySubControllerIdx(elemIdx);
    			 if (prob != null && prob.isEnabled()) {
    				 if (prob.getWeight() >= currentRandomizer) {
    					 return elemIdx;
    				 } else {
    					 currentRandomizer -= prob.getWeight();
    				 }
    			 }
    		 }
    	}
    	return 0;	
    }

}
