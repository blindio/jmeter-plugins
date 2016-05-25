package kg.apc.jmeter.control;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
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
	
    protected static final int ENABLED_COLUMN = 0;
    protected static final int ELEMENT_NAME_COLUMN = 1;
    protected static final int WEIGHT_COLUMN = 2;
    
    private static final String[] COLUMN_NAMES = { "Enabled", "Element Name", "Weight (0-32767)" };

    public WeightedDistributionControllerGui() {
    	super();
    	init();
    }
	
    protected JTable getTable() {
    	return this.table;
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
        GuiUtils.stopTableEditing(getTable());
        Data model = tableModel.getData();
        model.reset();
        if (el instanceof WeightedDistributionController && model.size() > 0) {
        	Collection<JMeterProperty> newProps = new ArrayList<JMeterProperty>(model.size());
	        while (model.next()) {
	        	String newPropName = (String) model.getColumnValue(ELEMENT_NAME_COLUMN);
	        	short weight = (Short) model.getColumnValue(WEIGHT_COLUMN);
	        	if (weight < 0) {
	        		JMeterUtils.reportErrorToUser(String.format("Weight must be an integer value between 0 - 32767, %d is invalid", weight));
	        		newProps.add( ((WeightedDistributionController) el).getWeightedProbabilityProperty(newPropName));
	        	} else {
		        	TestElementProperty newProp = new TestElementProperty(newPropName, new WeightedProbability(newPropName, weight));
		        	newProps.add(newProp);
		        	WeightedDistributionControllerGui.updateEnabled(newPropName, (Boolean)model.getColumnValue(ENABLED_COLUMN));
	        	}
	        }
	        
	        ((WeightedDistributionController) el).setWeightedProbabilities(newProps);
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
        		if (testElement instanceof Controller || testElement instanceof Sampler) {
	        		WeightedProbability prob = ((WeightedDistributionController) el).getWeightedProbability(testElement.getName());
	        		if (prob == null) {
	        			prob = new WeightedProbability(testElement.getName());
	        			((WeightedDistributionController) el).addWeightedProbability(prob);
	        		}
	        		tableModel.addRow(new Object[] { testElement.isEnabled(), testElement.getName(), prob.getWeight() });
        		}
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
        add(createRandomSeedPanel(), BorderLayout.SOUTH);
    }
    
    private Component createRandomSeedPanel() {
    	Box seedPanel = Box.createHorizontalBox();
        JLabel seedLabel = new JLabel("Seed for Random function");//$NON-NLS-1$
        seedPanel.add(seedLabel);

        JTextField seedField = new JTextField(0);
        seedField.setName("seed field");
        seedPanel.add(seedField);
        
        return seedPanel;
    }
    
    @SuppressWarnings({ "serial" })
	private Component createTablePanel() {
        tableModel = new EventFiringPowerTableModel(
                COLUMN_NAMES,
                new Class[] { Boolean.class, String.class, Short.class })
		        {
		        	@Override
		        	public boolean isCellEditable(int row, int column) {
		        		return column != 1;
		        	}
		        };
		        
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);;
        table.getColumnModel().getColumn(ENABLED_COLUMN).setPreferredWidth(100);
        table.getColumnModel().getColumn(ENABLED_COLUMN).setMaxWidth(100);
        table.getColumnModel().getColumn(ENABLED_COLUMN).setResizable(false);
        table.getColumnModel().getColumn(WEIGHT_COLUMN).setPreferredWidth(100);
        table.getColumnModel().getColumn(WEIGHT_COLUMN).setMaxWidth(100);
        table.getColumnModel().getColumn(WEIGHT_COLUMN).setResizable(false);
        table.getModel().addTableModelListener(new WeightedDistributionTableModelListener());
        return makeScrollPane(table);
    }

    protected static void updateEnabled(String testElementName, boolean isEnabled) {
    	Enumeration<JMeterTreeNode> elNodeEnum = GuiPackage.getInstance().getCurrentNode().children();
    	while(elNodeEnum.hasMoreElements()) {
    		TestElement testElement = elNodeEnum.nextElement().getTestElement();
    		if (testElementName.equals(testElement.getName())) {
    			testElement.setEnabled(isEnabled);
    		}
    	}
    	GuiPackage.getInstance().getMainFrame().repaint();
    }
}

class WeightedDistributionTableModelListener implements TableModelListener {	

	@Override
	public void tableChanged(TableModelEvent e) {
		switch(e.getColumn()) {
			case WeightedDistributionControllerGui.ENABLED_COLUMN:
				handleEnabledChange(e);
				break;
			case WeightedDistributionControllerGui.ELEMENT_NAME_COLUMN:
				break;
			case WeightedDistributionControllerGui.WEIGHT_COLUMN:
				break;
			default:
				break;
		}	
	}
	
	private void handleEnabledChange(TableModelEvent e) {
		if (e.getSource() instanceof EventFiringPowerTableModel) {
		    EventFiringPowerTableModel firingModel = (EventFiringPowerTableModel) e.getSource();
			Object[] rowData = firingModel.getRowData(e.getFirstRow());
			WeightedDistributionControllerGui.updateEnabled((String)rowData[1], (Boolean)rowData[0]);
		}
	}
	
	
}

class EventFiringPowerTableModel extends PowerTableModel {
	private static final long serialVersionUID = -600418978315572279L;

	public EventFiringPowerTableModel(String[] headers, Class<?>[] classes) {
        super(headers, classes);
    }

    public EventFiringPowerTableModel() {
    	super();
    }
	
	@Override
	public void setValueAt(Object aValue, int row, int column) {
		super.setValueAt(aValue, row, column);
		fireTableCellUpdated(row, column);
	}
	
}
