package kg.apc.jmeter.control;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

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
	
    private static final String COLUMN_NAMES_0 = "Enabled";

    private static final String COLUMN_NAMES_1 = "Element Name";
    
    private static final String COLUMN_NAMES_2 = "Weight (0-32767)";

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
        	Collection<JMeterProperty> newProps = new ArrayList<JMeterProperty>(model.size());
	        while (model.next()) {
	        	String newPropName = (String) model.getColumnValue(COLUMN_NAMES_1);
	        	short weight = (Short) model.getColumnValue(COLUMN_NAMES_2);
	        	if (weight < 0) {
	        		JMeterUtils.reportErrorToUser(String.format("Weight must be an integer value between 0 - 32767, %d is invalid", weight));
	        		newProps.add( ((WeightedDistributionController) el).getWeightedProbabilityProperty(newPropName));
	        	} else {
		        	TestElementProperty newProp = new TestElementProperty(newPropName, new WeightedProbability(newPropName, weight));
		        	newProps.add(newProp);
		        	WeightedDistributionControllerGui.updateEnabled(newPropName, (Boolean)model.getColumnValue(COLUMN_NAMES_0));
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
    }
    
    private Component createTablePanel() {
        tableModel = new PowerTableModel(
                new String[] { COLUMN_NAMES_0, COLUMN_NAMES_1, COLUMN_NAMES_2 },
                new Class[] { Boolean.class, String.class, Short.class })
		        {
		        	@Override
		        	public boolean isCellEditable(int row, int column) {
		        		return column != 1;
		        	}
		        };
		        
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);;
        table.getColumn(COLUMN_NAMES_0).setPreferredWidth(100);
        table.getColumn(COLUMN_NAMES_0).setMaxWidth(100);
        table.getColumn(COLUMN_NAMES_0).setResizable(false);
        table.getColumn(COLUMN_NAMES_2).setPreferredWidth(100);
        table.getColumn(COLUMN_NAMES_2).setMaxWidth(100);
        table.getColumn(COLUMN_NAMES_2).setResizable(false);
        table.getColumnModel().getColumn(0).setCellEditor(new CheckBoxEditor(new JCheckBox()));
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

class CheckBoxEditor extends DefaultCellEditor implements ItemListener {

	private static final long serialVersionUID = 1L;
	private JCheckBox checkBox;
	private JTable table;
	private int row;
	
	public CheckBoxEditor(JCheckBox checkBox) {
	    super(checkBox);
	    this.checkBox = checkBox;
	    this.checkBox.addItemListener(this);
	}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
	        boolean isSelected, int row, int column) {
		this.table = table;
	    this.row = row;
	    this.checkBox.setSelected((Boolean) value);
	    return super.getTableCellEditorComponent(table, value, isSelected, row, column);
	}
	
	public void itemStateChanged(ItemEvent e) {
	    this.fireEditingStopped();
	    PowerTableModel model = (PowerTableModel)this.table.getModel();
		Object[] rowData = model.getRowData(this.row);
		WeightedDistributionControllerGui.updateEnabled((String)rowData[1], (Boolean)rowData[0]);
	}
}