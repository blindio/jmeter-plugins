package kg.apc.jmeter.control;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import javax.swing.Box;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.collections.Data;
import org.apache.jorphan.gui.GuiUtils;

import kg.apc.jmeter.JMeterPluginsUtils;

public class WeightedDistributionControllerGui
		extends AbstractControllerGui
{
	private static final long serialVersionUID = 2245012323333943250L;

	public static final String WIKIPAGE = "WeightedDistributionController";

    private JTable table;

    private PowerTableModel tableModel;
	
    private static final String COLUMN_NAMES_0 = "Enable";

    private static final String COLUMN_NAMES_1 = "Element Name";
    
    private static final String COLUMN_NAMES_2 = "Weight";

    public WeightedDistributionControllerGui() {
    	super();
    	init();
    }
	
	@Override
	public TestElement createTestElement() {
        WeightedDistributionController wdc = new WeightedDistributionController();
        modifyTestElement(wdc);
        wdc.setComment(JMeterPluginsUtils.getWikiLinkText(WIKIPAGE));
        return wdc;
	}
    
    @Override
    public void modifyTestElement(TestElement el) {
        GuiUtils.stopTableEditing(table);
        Data model = tableModel.getData();
        model.reset();
        if (el instanceof WeightedDistributionController && model.size() > 0) {
        	Collection<JMeterProperty> newWeightedPropColl = new ArrayList<JMeterProperty>(model.size());
        	
	        while (model.next()) {
	        
	        	String newPropName = (String) model.getColumnValue(COLUMN_NAMES_1);
	        	TestElementProperty newProp = new TestElementProperty(newPropName, new WeightedProbability(
        				newPropName,
        				((Boolean)model.getColumnValue(COLUMN_NAMES_0)),
        				(Short) model.getColumnValue(COLUMN_NAMES_2)));
	        	newWeightedPropColl.add(newProp);
	        }
	        
	        ((WeightedDistributionController) el).setWeightedProbabilities(newWeightedPropColl);
        }
        this.configureTestElement(el);
    }
	
    @Override
    public String getLabelResource()
    {
       return getClass().getName();
    }
    
    @Override
    public String getStaticLabel()
    {
       return JMeterPluginsUtils.prefixLabel("Weighted Distribution Controller");
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        tableModel.clearData();
        if (el instanceof WeightedDistributionController && GuiPackage.getInstance().getCurrentElement() == el) {
        	Enumeration<JMeterTreeNode> elNodeEnum = GuiPackage.getInstance().getCurrentNode().children();
        	while(elNodeEnum.hasMoreElements()) {
        		TestElement testElement = elNodeEnum.nextElement().getTestElement();
        		WeightedProbability prob = ((WeightedDistributionController) el).getWeightedProbability(testElement.getName());
        		if (prob == null) {
        			prob = new WeightedProbability(testElement.getName());
        			((WeightedDistributionController) el).putWeightedProbability(prob);
        		}
        		tableModel.addRow(new Object[] { prob.isEnabled(), prob.getName(), prob.getWeight() });
        	}
        }
    }
	
    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        // Force the table to be at least 70 pixels high
        add(Box.createVerticalStrut(70), BorderLayout.WEST);
    }
    
    private Component createTablePanel() {
        tableModel = new PowerTableModel(
                new String[] { COLUMN_NAMES_0, COLUMN_NAMES_1, COLUMN_NAMES_2 },
                new Class[] { Boolean.class, String.class, Short.class });

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);;
        table.getColumn(COLUMN_NAMES_0).setPreferredWidth(100);
        table.getColumn(COLUMN_NAMES_0).setMaxWidth(100);
        table.getColumn(COLUMN_NAMES_0).setResizable(false);
        table.getColumn(COLUMN_NAMES_2).setPreferredWidth(100);
        table.getColumn(COLUMN_NAMES_2).setMaxWidth(100);
        table.getColumn(COLUMN_NAMES_2).setResizable(false);
        return makeScrollPane(table);
    }

}
