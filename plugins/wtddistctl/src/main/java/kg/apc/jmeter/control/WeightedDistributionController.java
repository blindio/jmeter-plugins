package kg.apc.jmeter.control;

import java.util.ArrayList;
import java.util.Collection;

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
	
	public WeightedDistributionController() {
		setWeightedProbabilities(new ArrayList<JMeterProperty>());
	}
	
	public CollectionProperty getWeightedProbabilities() {
		return (CollectionProperty)getProperty(WEIGHTED_PROBABILITIES);
	}
	
	public void setWeightedProbabilities(Collection<JMeterProperty> weightedProbs) {
		setProperty(new CollectionProperty(WEIGHTED_PROBABILITIES, weightedProbs));
	}
	
	public int getWeightedProbabilityPropertyIndex(String testElementName) {
        PropertyIterator iter = getWeightedProbabilities().iterator();
        int index = 0;
        CollectionProperty probs = getWeightedProbabilities();
        if (probs != null) {
        	while (iter.hasNext()) {
        		TestElementProperty currProp = (TestElementProperty) iter.next();;
        		if (testElementName.equals(currProp.getName())) {
        			return index;
        		}
        		index++;
        	}
        }
        return -1;
	}
	
	public TestElementProperty getWeightedProbabilityProperty(String testElementName) {
		int index = getWeightedProbabilityPropertyIndex(testElementName);
		return index == -1 ? null : (TestElementProperty)getWeightedProbabilities().get(index);
	}
	
	public WeightedProbability getWeightedProbability(String testElementName) {
		TestElementProperty prop = getWeightedProbabilityProperty(testElementName);
		return prop == null ? null : (WeightedProbability) prop.getObjectValue();
	}	

	public void putWeightedProbability(WeightedProbability weightedProb) {
		TestElementProperty newProp = new TestElementProperty(weightedProb.getName(), weightedProb);
		int propIndex = getWeightedProbabilityPropertyIndex(weightedProb.getName());
		if (propIndex != -1) {
			getWeightedProbabilities().set(propIndex, newProp);
		} else {
			if (isRunningVersion()) {
				setTemporary(newProp);
			}
			getWeightedProbabilities().addProperty(newProp);
		}
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
            str.append(String.format("%s=%hd(%s)", prob.getName(), prob.getWeight(),  prob.isEnabled() ? "enabled" : "disabled"));
            if (iter.hasNext()) {
                str.append("&"); //$NON-NLS-1$
            }
        }
        return str.toString();
    }
	
    public void removeWeightedProbability(WeightedProbability weightedProb) {
    	removeWeightedProbability(weightedProb.getName());
    }


    public void removeWeightedProbability(String weightedProbName) {
        PropertyIterator iter = getWeightedProbabilities().iterator();
        while (iter.hasNext()) {
            WeightedProbability currWeightedProb = (WeightedProbability) iter.next().getObjectValue();
            if (weightedProbName.equals(currWeightedProb.getName())) {
                iter.remove();
            }
        }
    }
    
    public void removeAllWeightedProbabilites() {
		CollectionProperty weightedProbs = getWeightedProbabilities();
		if (weightedProbs != null) {
			weightedProbs.clear();
		}
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
    		 int currentElementIndex = 0;
    		 for (TestElement currElement : this.getSubControllers()) {
    			 WeightedProbability elementProb = this.getWeightedProbability(currElement.getName());
    			 if (elementProb != null && elementProb.isEnabled()) {
    				 if (elementProb.getWeight() >= currentRandomizer) {
    					 return currentElementIndex;
    				 } else {
    					 currentRandomizer -= elementProb.getWeight();
    				 }
    			 }
    			 currentElementIndex++;
    		 }
    	}
    	return 0;	
    }

}
