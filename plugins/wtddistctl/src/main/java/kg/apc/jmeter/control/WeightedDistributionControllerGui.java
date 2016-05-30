package kg.apc.jmeter.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.Data;
import org.apache.jorphan.gui.GuiUtils;

public class WeightedDistributionControllerGui
		extends AbstractControllerGui
{
	private static final long serialVersionUID = 2245012323333943250L;
	
    protected static final int ENABLED_COLUMN = 0;
    protected static final int ELEMENT_NAME_COLUMN = 1;
    protected static final int WEIGHT_COLUMN = 2;
    protected static final int PERCENT_COLUMN = 3;
    protected static final int HIDDEN_CHILD_NODE_IDX_COLUMN = 4;
    
    private static final String[] COLUMN_NAMES = { "Enabled", "Element Name", "Weight (0-32767)", "Percentage", null };
    
    private JTable table;
    
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
        return wdc;
	}
    
    @Override
    public String getLabelResource()
    {
       return getClass().getName();
    }
    
    @Override
    public String getStaticLabel()
    {
       return "Weighted Distribution Controller";
    }
	
    @Override
    public void modifyTestElement(TestElement el) {
        GuiUtils.stopTableEditing(getTable());
        Data model = ((PowerTableModel)getTable().getModel()).getData();
        model.reset();
        if (el instanceof WeightedDistributionController && model.size() > 0) {
        	WeightedDistributionController wdc = (WeightedDistributionController) el;
        	if (wdc.buildNodeProbabilityMap()) {
	        	wdc.reset();
		        while (model.next()) {
		        	int childNodeIdx = (int) model.getColumnValue(HIDDEN_CHILD_NODE_IDX_COLUMN);
		        	TestElement currTestElement = ((JMeterTreeNode)wdc.getNode().getChildAt(childNodeIdx)).getTestElement();
		        	currTestElement.setProperty(WeightedDistributionController.WEIGHT, (int)model.getColumnValue(WEIGHT_COLUMN));
		        	currTestElement.setName((String)model.getColumnValue(ELEMENT_NAME_COLUMN));
		        	currTestElement.setEnabled((boolean)model.getColumnValue(ENABLED_COLUMN));
		        	//wdc.getWeightedProbabilityForNode((JMeterTreeNode)wdc.getNode().getChildAt(childNodeIdx), childNodeIdx);
		        	
		        	/*
		        	WeightedProbability prob = new WeightedProbability((JMeterTreeNode)wdc.getNode().getChildAt(childNodeIdx),
		        					childNodeIdx,
		        					(String) model.getColumnValue(ELEMENT_NAME_COLUMN),
		        					(short) model.getColumnValue(WEIGHT_COLUMN));
		        	wdc.addWeightedProbability(prob);
		        	*/
		        }
        	}
        }
        this.configureTestElement(el);
    }
	

    


    @Override
    public void configure(TestElement el) {
        super.configure(el);
        ((PowerTableModel)getTable().getModel()).clearData();
        if (el instanceof WeightedDistributionController) {
        	WeightedDistributionController wdc = (WeightedDistributionController) el;
        	if (wdc.buildNodeProbabilityMap()) {
	        	for (int childNodeIdx = 0; childNodeIdx < wdc.getNode().getChildCount(); childNodeIdx++) {
	        		JMeterTreeNode currNode = (JMeterTreeNode)wdc.getNode().getChildAt(childNodeIdx);
	        		TestElement currTestElement = currNode.getTestElement();
	        		if (currTestElement instanceof Controller || currTestElement instanceof Sampler) {
	        			((PowerTableModel)getTable().getModel()).addRow(
		        				new Object[] {
		        						currTestElement.isEnabled(),
		        						currTestElement.getName(),
		        						currTestElement.getPropertyAsInt(WeightedDistributionController.WEIGHT, WeightedDistributionController.DFLT_WEIGHT),
		        						0.0,
		        						childNodeIdx 
		        				});
	        			//WeightedProbability currProb = wdc.getWeightedProbabilityForNode(currChildNode, childNodeIdx);
	        			/*
		        		WeightedProbability currProb = wdc.getWeightedProbabilityByChildNodeIdx(childNodeIdx, currChildNodeTestElement.getName());
		        		if (currProb == null) {
		        			currProb = new WeightedProbability(currChildNode, childNodeIdx, currChildNodeTestElement.getName());
		        			wdc.addWeightedProbability(currProb);
		        		}
		        		
		        		((PowerTableModel)getTable().getModel()).addRow(
		        				new Object[] { 
		        						currChildNodeTestElement.isEnabled(),
		        						currChildNodeTestElement.getName(),
		        						currProb.getWeight(),
		        						getPercentage(currProb, wdc),
		        						childNodeIdx 
		        				});
		        				*/
	        		}
	        	}
	        }
        }
    }
 
    public void updatePercentageColumn() {
    	
    }
    
    private float getPercentage(WeightedProbability prob, WeightedDistributionController wdc) {
    	if (prob.isElementEnabled() == true  && wdc.getMaxCumulativeProbability() > 0) {
    		 return ((float)prob.getWeight()) / wdc.getMaxCumulativeProbability();
    	}
    	return 0.0f;
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
        TableModel tableModel = new EventFiringPowerTableModel(
                COLUMN_NAMES,
                new Class[] { Boolean.class, String.class, Integer.class, Float.class, Integer.class })
		        {
		        	@Override
		        	public boolean isCellEditable(int row, int column) {
		        		return column != PERCENT_COLUMN && column != HIDDEN_CHILD_NODE_IDX_COLUMN;
		        	}
		        };
		        
        table = new JTable(tableModel);
        Font defaultFont = table.getTableHeader().getFont();
        table.getTableHeader().setFont(new Font("Bold", Font.BOLD, defaultFont.getSize()));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);;
        table.getColumnModel().getColumn(ENABLED_COLUMN).setPreferredWidth(100);
        table.getColumnModel().getColumn(ENABLED_COLUMN).setMaxWidth(100);
        table.getColumnModel().getColumn(ENABLED_COLUMN).setResizable(false);
        table.getColumnModel().getColumn(WEIGHT_COLUMN).setPreferredWidth(100);
        table.getColumnModel().getColumn(WEIGHT_COLUMN).setMaxWidth(100);
        table.getColumnModel().getColumn(WEIGHT_COLUMN).setResizable(false);
        table.getColumnModel().getColumn(WEIGHT_COLUMN).setCellEditor(new UnsignedShortEditor());
        table.getColumnModel().getColumn(PERCENT_COLUMN).setPreferredWidth(100);
        table.getColumnModel().getColumn(PERCENT_COLUMN).setMaxWidth(100);
        table.getColumnModel().getColumn(PERCENT_COLUMN).setResizable(false);
        table.getColumnModel().getColumn(PERCENT_COLUMN).setCellRenderer(new IneditablePercentageRenderer());
        table.getColumnModel().getColumn(HIDDEN_CHILD_NODE_IDX_COLUMN).setMinWidth(0);
        table.getColumnModel().getColumn(HIDDEN_CHILD_NODE_IDX_COLUMN).setMaxWidth(0);
        table.getColumnModel().getColumn(HIDDEN_CHILD_NODE_IDX_COLUMN).setResizable(false);
        table.getModel().addTableModelListener(new WeightedDistributionTableModelListener());
        return makeScrollPane(table);
    }
    
    public static boolean isCurrentNodeWeightedDistributionNode() {
    	return GuiPackage.getInstance().getCurrentElement() instanceof WeightedDistributionController;
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
				handleElementNameChange(e);
				break;
			case WeightedDistributionControllerGui.WEIGHT_COLUMN:
				handleWeightChange(e);
				break;
			default:
				break;
		}	
	}
	
	private void handleEnabledChange(TableModelEvent e) {
		if (e.getSource() instanceof EventFiringPowerTableModel && e.getType() == TableModelEvent.UPDATE) {
		    EventFiringPowerTableModel firingModel = (EventFiringPowerTableModel) e.getSource();
			Object[] rowData = firingModel.getRowData(e.getFirstRow());
			boolean isEnabled = (boolean)rowData[WeightedDistributionControllerGui.ENABLED_COLUMN];
			if (WeightedDistributionControllerGui.isCurrentNodeWeightedDistributionNode()) {
				WeightedDistributionController wdc = (WeightedDistributionController)GuiPackage.getInstance().getCurrentElement();
				wdc.buildNodeProbabilityMap();
				((JMeterTreeNode)wdc.getNode().getChildAt((int)rowData[WeightedDistributionControllerGui.HIDDEN_CHILD_NODE_IDX_COLUMN]))
							.setEnabled(isEnabled);
				GuiPackage.getInstance().getMainFrame().repaint();
			}
		}
	}
	
	private void handleElementNameChange(TableModelEvent e) {
		if (e.getSource() instanceof EventFiringPowerTableModel && e.getType() == TableModelEvent.UPDATE) {
		    EventFiringPowerTableModel firingModel = (EventFiringPowerTableModel) e.getSource();
			Object[] rowData = firingModel.getRowData(e.getFirstRow());
			((JMeterTreeNode)GuiPackage.getInstance().getCurrentNode()
					.getChildAt((int)rowData[WeightedDistributionControllerGui.HIDDEN_CHILD_NODE_IDX_COLUMN]))
						.setName((String)rowData[WeightedDistributionControllerGui.ELEMENT_NAME_COLUMN]);
			GuiPackage.getInstance().getMainFrame().repaint();
		}
	}
	
	private void handleWeightChange(TableModelEvent e) {
		if (e.getSource() instanceof EventFiringPowerTableModel && e.getType() == TableModelEvent.UPDATE) {
			
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

@SuppressWarnings("serial")
class IneditablePercentageRenderer extends DefaultTableCellRenderer {
	private static final String FORMAT = "###.####%";
    private final DecimalFormat formatter = new DecimalFormat(FORMAT);
    
    public IneditablePercentageRenderer() { 
    	super();
    	setBackground(Color.LIGHT_GRAY);
    	setHorizontalAlignment(RIGHT);
    }

    public void setValue(Object value) {
        setText((value == null) ? "" : formatter.format(value));
    }
}

@SuppressWarnings("serial")
class UnsignedShortEditor extends DefaultCellEditor {

	JFormattedTextField ftf;
	NumberFormat intFormat = NumberFormat.getIntegerInstance();
	private boolean DEBUG = false;
	private static final String ERRMSG = "Valid values are integers between 0 - 32767";
	
	public UnsignedShortEditor() {
		super(new JFormattedTextField());
		ftf = (JFormattedTextField)getComponent();
		NumberFormatter ushortFormatter = new NumberFormatter(intFormat);
		ushortFormatter.setMinimum(0);
		ushortFormatter.setMaximum(9999);
		ftf.setFormatterFactory(new DefaultFormatterFactory(ushortFormatter));
		ftf.setValue(0);
		ftf.setHorizontalAlignment(JTextField.TRAILING);
		ftf.setFocusLostBehavior(JFormattedTextField.PERSIST);
		
        //React when the user presses Enter while the editor is
        //active.  (Tab is handled as specified by
        //JFormattedTextField's focusLostBehavior property.)
        ftf.getInputMap().put(KeyStroke.getKeyStroke(
                                        KeyEvent.VK_ENTER, 0),
                                        "check");
        ftf.getActionMap().put("check", new AbstractAction() {
        	
            public void actionPerformed(ActionEvent e) {
            	if (!ftf.isEditValid()) { //The text is invalid.
                	JMeterUtils.reportErrorToUser(ERRMSG);
                	ftf.postActionEvent();
            		/*
                    if (userSaysRevert()) { //reverted
                    	ftf.postActionEvent(); //inform the editor
                    }
                    */
                } else try {              //The text is valid,
                    ftf.commitEdit();     //so use it.
                    ftf.postActionEvent(); //stop editing
                } catch (java.text.ParseException exc) { }
            }
        });
	}
	
    //Override to invoke setValue on the formatted text field.
    public Component getTableCellEditorComponent(JTable table,
            Object value, boolean isSelected,
            int row, int column) {
        JFormattedTextField ftf =
            (JFormattedTextField)super.getTableCellEditorComponent(
                table, value, isSelected, row, column);
        ftf.setValue(value);
        return ftf;
    }

    //Override to ensure that the value remains an Integer.
    public Object getCellEditorValue() {
        JFormattedTextField ftf = (JFormattedTextField)getComponent();
        Object o = ftf.getValue();
        if (o instanceof Integer) {
            return o;
        } else if (o instanceof Number) {
            return new Integer(((Number)o).intValue());
        } else {
            if (DEBUG) {
                System.out.println("getCellEditorValue: o isn't a Number");
            }
            try {
                return intFormat.parseObject(o.toString());
            } catch (ParseException exc) {
                System.err.println("getCellEditorValue: can't parse o: " + o);
                return null;
            }
        }
    }

    //Override to check whether the edit is valid,
    //setting the value if it is and complaining if
    //it isn't.  If it's OK for the editor to go
    //away, we need to invoke the superclass's version 
    //of this method so that everything gets cleaned up.
    public boolean stopCellEditing() {
        JFormattedTextField ftf = (JFormattedTextField)getComponent();
        if (ftf.isEditValid()) {
            try {
                ftf.commitEdit();
            } catch (java.text.ParseException exc) { }
	    
        } else { //text is invalid
        	JMeterUtils.reportErrorToUser(ERRMSG);
        	return false;
        	/*
            if (!userSaysRevert()) { //user wants to edit
            	return false; //don't let the editor go away
            } 
            */
        }
        return super.stopCellEditing();
    }
}


