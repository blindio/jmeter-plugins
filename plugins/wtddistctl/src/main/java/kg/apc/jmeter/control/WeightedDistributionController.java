package kg.apc.jmeter.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jmeter.control.InterleaveControl;
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
	
	private Integer[] chldNodeIdxToSubCtlrIdxMap;
	
	
	public WeightedDistributionController() {
		setWeightedProbabilities(new ArrayList<JMeterProperty>());
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
	
	public void reset(int childCnt) {
		chldNodeIdxToSubCtlrIdxMap = new Integer[childCnt];
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
	
	public void addWeightedProbability(WeightedProbability weightedProb) {
		TestElementProperty prop = new TestElementProperty(weightedProb.getName(), weightedProb);

		if (isRunningVersion()) {
			setTemporary(prop);
		}
		
		if (weightedProb.isElementEnabled()) {
			addToMaxCumulativeProbability(weightedProb.getWeight());
		}
		
		if (chldNodeIdxToSubCtlrIdxMap != null) {
			chldNodeIdxToSubCtlrIdxMap[weightedProb.getChildNodeIndex()] = getWeightedProbabilities().size();
		}
		
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
