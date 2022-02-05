package gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.Properties;

import geotransform.coords.Gdc_Coord_3d;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mil.navy.nps.dis.EntityID;
import mil.navy.nps.dis.EntityType;
import mil.navy.nps.dis.EulerAngle;
import mil.navy.nps.dis.WorldCoordinate;
import network.EntityStateComposer;
import network.PduProcessor;
import network.SetDataComposer;

public class PDUToolWindow extends VBox
{
	Stage stage;
	Scene scene;
	
    int disExerciseID;
    int disSiteID;
    int disHostID;
    
    int disRecPort, disSendPort;
    String disBroadcastAddress;
    
	Properties props;
    
	// TODO
	//
	// listen for DIS PDUs, for debugging / testing purposes
    
	@FXML
	Menu configAddrMenu;
    @FXML
    MenuItem configRecPortMenuItem, configSendPortMenuItem, configSiteMenuItem, configHostMenuItem, configExerciseMenuItem;
    @FXML
    CheckMenuItem connectedCheckMenuItem;
    
    @FXML
    ChoiceBox<String> forceChoice;
    @FXML
    TextField entityStateIDField, typeField, intervalField;
    @FXML
    TextField latField, lngField, elevField;
    @FXML
    TextField yawField, pitchField, rollField;
    @FXML
    ToggleButton sendEntityStateButton;
    
    @FXML
    TextField setDataIDField, datumIDField;
    @FXML
    ComboBox<String> scriptCombo;
    @FXML
    ListView<String> storedScriptListView;    


	public static void createPDUToolWindow(Stage _stage)
	{
        FXMLLoader fxmlLoader = new FXMLLoader(PDUToolWindow.class.getResource("PDUToolWindowFX.fxml"));
        PDUToolWindow pduTool = new PDUToolWindow();
        fxmlLoader.setRoot(pduTool);
        fxmlLoader.setController(pduTool);

        try 
        {
            Parent parent = fxmlLoader.load();
            
            pduTool.stage = _stage;
            pduTool.stage.setTitle("PDUTool");
    		pduTool.scene = new Scene(parent);
    		pduTool.stage.setScene(pduTool.scene);

//    		stage.getIcons().add(new Image(MainWindow.class.getResourceAsStream("../images/earth-icon.png")));
    		
    		pduTool.stage.setOnCloseRequest((event) -> ((PDUToolWindow) pduTool.scene.getRoot()).shutdown());

    		pduTool.stage.show();
        } 
        catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }
	}

	public PDUToolWindow()
	{
	}
	
	@FXML
	private void initialize()
	{
		// set force choice
		forceChoice.getItems().setAll("Other", "Friendly", "Opposing", "Neutral");
		forceChoice.getSelectionModel().select(1);
		
		disBroadcastAddress = getSubnet();
		configAddrMenu.getItems().get(0).setText("<this> " + disBroadcastAddress);
		
	    disSendPort = 3000;
	    disRecPort = 0;
		
	    disExerciseID = 1;
	    disSiteID = 99;
	    disHostID = 1;
	    
    	props = new Properties();
    	loadSettings();
	    
		updateSettings();	
	}
	
	public void shutdown()
	{
		saveSettings();
		disconnect();
	}
	
	public void disconnect()
	{
		connectedCheckMenuItem.setSelected(false);
		
		EntityStateComposer.killEntityStateThread();
		PduProcessor.getInstance().stopProcessing();
	}
	
//	public void configAddr()
//	{
//		FXDialogs.createTextInputDialog("Network Config", "Configure Destination", "Address", stage, disBroadcastAddress).showAndWait().ifPresent(val -> {
//			disBroadcastAddress = val;
//		});
//		
//		updateSettings();
//	}

	@FXML
	public void addressChange(ActionEvent event)
	{
		MenuItem item = (MenuItem) event.getSource();
		String text = item.getText().trim();
		
		if (text.startsWith("<this>"))
		{
			disBroadcastAddress = getSubnet();
		}
		else if (text.equals("<new>"))
		{
			FXDialogs.createTextInputDialog("Network Config", "Configure Destination", "Address", stage, disBroadcastAddress).showAndWait().ifPresent(val ->
			{
				try
				{
					// validate ip format
					if (InetAddress.getByName(val) != null)
					{
						disBroadcastAddress = val;

						// if this is a new address, add to the list 
						if (!configAddrMenu.getItems().stream().map(i -> i.getText()).anyMatch(s -> s.equals(disBroadcastAddress)))
						{
							MenuItem i = new MenuItem(disBroadcastAddress);
							i.setOnAction(e -> addressChange(e));
							configAddrMenu.getItems().add(i);
						}							
					}
				}
				catch (UnknownHostException e)
				{
					FXDialogs.createAlert(AlertType.ERROR, stage, "Parameter Set Failure", null, "Invalid address.", (ButtonType[]) null).showAndWait();
				}
			});
		}
		else if (!text.isEmpty())
		{
			try
			{
				if (InetAddress.getByName(text) != null)
					disBroadcastAddress = text;
			}
			catch (UnknownHostException e)
			{
				FXDialogs.createAlert(AlertType.ERROR, stage, "Parameter Set Failure", null, "Invalid address.", (ButtonType[]) null).showAndWait();
			}
		}
		
		updateSettings();
	}
	
	public Integer configNumber(String title, String headerText, String contentText, int initial)
	{
		Integer intVal = initial;
		
		Optional<String> result = FXDialogs.createTextInputDialog(title, headerText, contentText, stage, Integer.toString(initial)).showAndWait();
		if (result.isPresent())
		{
			try
			{
				intVal = Integer.valueOf(result.get());
			}
			catch (Exception e)
			{
				FXDialogs.createAlert(AlertType.ERROR, stage, "Parameter Set Failure", null, "Invalid number.", (ButtonType[]) null).showAndWait();
			}
		}
		
		return intVal;
	}

	@FXML
	public void configRecPort()
	{
		disRecPort = configNumber("Network Config", "Configure Receive Port", "Receive Port", disRecPort);
		updateSettings();
	}

	@FXML
	public void configSendPort()
	{
		disSendPort = configNumber("Network Config", "Configure Send Port", "Dest Port", disSendPort);
		updateSettings();
	}

	@FXML
	public void configSite()
	{
		disSiteID = configNumber("Network Config", "Configure Site", "Site", disSiteID);
		updateSettings();
	}

	@FXML
	public void configHost()
	{
		disHostID = configNumber("Network Config", "Configure Host", "Host", disHostID);
		updateSettings();
	}

	@FXML
	public void configExercise()
	{
		disExerciseID = configNumber("Network Config", "Configure Exercise", "Exercise", disExerciseID);
		updateSettings();
	}
	
	@FXML
	public void connect()
	{
		if (connectedCheckMenuItem.isSelected())
		{
			PduProcessor.getInstance().initialize(disExerciseID, disBroadcastAddress, disRecPort, disSendPort);
			PduProcessor.getInstance().startProcessing();
		}
		else
		{
			disconnect();
		}
	}
	
	public void updateSettings()
	{
		disconnect();
		
//		configAddrMenuItem.setText("Address: " + disBroadcastAddress);
		configAddrMenu.setText("Address: " + disBroadcastAddress);
		configRecPortMenuItem.setText("Receive Port: " + disRecPort);
		configSendPortMenuItem.setText("Send Port: " + disSendPort);
		configSiteMenuItem.setText("Site: " + disSiteID);
		configHostMenuItem.setText("Host: " + disHostID);
		configExerciseMenuItem.setText("Exercise: " + disExerciseID);
	}
	
    private String getSubnet()
    {
    	String address = disBroadcastAddress;
        boolean discoverIP = true; // pref.getPreference("configuration.discoverIP", true);
        if (discoverIP)
        {
            try 
            {
                InetAddress addr = InetAddress.getLocalHost();
            
                // Get IP Address
                byte[] ipAddr = addr.getAddress();
                address = Byte.toUnsignedInt(ipAddr[0]) + "." + Byte.toUnsignedInt(ipAddr[1]) + "." + Byte.toUnsignedInt(ipAddr[2]) + ".255";
            } 
            catch (UnknownHostException e)
            {
            	e.printStackTrace();
            }
        }
        
        return address;
    }


	@FXML
    public void sendEntityStatePDUs()
    {
		// don't send if we're not connected
		if (!connectedCheckMenuItem.isSelected())
		{
			sendEntityStateButton.setSelected(false);
			return;
		}
		
		// toggle sending of entity state PDUs
		if (sendEntityStateButton.isSelected())
		{
			EntityID entityID = EntityStateComposer.getEntityID(disSiteID + "." + disHostID + "." + entityStateIDField.getText());
			EntityType entityType = EntityStateComposer.getEntityType(typeField.getText());
			int force = forceChoice.getSelectionModel().getSelectedIndex();
			long interval = Long.valueOf(intervalField.getText());

			WorldCoordinate coords = EntityStateComposer.getCoords(
					Double.valueOf(latField.getText()),
					Double.valueOf(lngField.getText()),
					Double.valueOf(elevField.getText()));
			EulerAngle orientation = EntityStateComposer.getOrientation(
					new Gdc_Coord_3d(Double.valueOf(lngField.getText()), Double.valueOf(latField.getText()), Double.valueOf(elevField.getText())),
					Double.valueOf(yawField.getText()),
					Double.valueOf(pitchField.getText()),
					Double.valueOf(rollField.getText()));

			EntityStateComposer.startEntityStatePDUs(disExerciseID, entityID, entityType, force, interval, coords, orientation);
		}
		else
		{
			EntityStateComposer.killEntityStateThread();
		}
    }

    @FXML
    public void sendSetDataPDU()
    {
		// don't send if we're not connected
		if (!connectedCheckMenuItem.isSelected())
		{
			return;
		}

//    	EntityID entityID = EntityStateComposer.getEntityID(disSiteID + "." + disHostID + "." + setDataIDField.getText());
//    	int datumID = Integer.valueOf(datumIDField.getText());
    	
    	// store history of script strings in combobox
    	String text= "";
    	if (!scriptCombo.getEditor().getText().trim().isEmpty())
		{
        	text = scriptCombo.getEditor().getText().trim();
//    		scriptCombo.getEditor().clear();
    		
    		if (!text.isEmpty())
    		{
    			if (!scriptCombo.getItems().contains(text))
    				scriptCombo.getItems().add(0, text);

    			scriptCombo.getSelectionModel().select(text);
    		}
		}
    	else if (scriptCombo.getSelectionModel().getSelectedIndex() >= 0)
    	{
        	text = scriptCombo.getSelectionModel().getSelectedItem();
    	}
    	else
    	{
    		return;
    	}
    	
    	text = scriptCombo.getSelectionModel().getSelectedItem();
    	if (!text.trim().isEmpty())
    	{
    		sendSetDataPDU(text);
//    		SetDataComposer.sendSetDataPDU(entityID, datumID, text, text, SetDataComposer.generateMessageID());
    	}
    }

    @FXML
    public void sendSetDataPDU(String text)
    {
		// don't send if we're not connected
		if (!connectedCheckMenuItem.isSelected())
		{
			return;
		}

    	if (text != null && !text.trim().isEmpty())
    	{
        	EntityID entityID = EntityStateComposer.getEntityID(disSiteID + "." + disHostID + "." + setDataIDField.getText());
        	int datumID = Integer.valueOf(datumIDField.getText());
    		SetDataComposer.sendSetDataPDU(entityID, datumID, text, text, SetDataComposer.generateMessageID());
    	}
    }

    public int getExerciseID()
    {
        return disExerciseID;
    }

    public int getSiteID()
    {
        return disSiteID;
    }

    public int getHostID()
    {
        return disHostID;
    }
    
    public String getBroadcastAddress()
    {
        return disBroadcastAddress;
    }
    
    public void saveSettings()
    {
//    	props.setProperty("disBroadcastAddress", disBroadcastAddress);
    	for (int i=2; i < configAddrMenu.getItems().size(); i++)
    	{
        	props.setProperty("addressHistory_" + (i-2), configAddrMenu.getItems().get(i).getText());
    	}
    	
    	props.setProperty("disReceivePort", Integer.toString(disRecPort));
    	props.setProperty("disSendPort", Integer.toString(disSendPort));

    	props.setProperty("disSiteID", Integer.toString(disSiteID));
    	props.setProperty("disHostID", Integer.toString(disHostID));
    	props.setProperty("disExerciseID", Integer.toString(disExerciseID));

    	props.setProperty("force", forceChoice.getValue());

    	props.setProperty("entityStateID", entityStateIDField.getText());
    	props.setProperty("entityStateType", typeField.getText());
    	props.setProperty("entityStateInterval", intervalField.getText());

    	props.setProperty("entityStateLat", latField.getText());
    	props.setProperty("entityStateLng", lngField.getText());
    	props.setProperty("entityStateElev", elevField.getText());

    	props.setProperty("entityStateYaw", yawField.getText());
    	props.setProperty("entityStatePitch", pitchField.getText());
    	props.setProperty("entityStateRoll", rollField.getText());

    	props.setProperty("setDataEntityID", setDataIDField.getText());
    	props.setProperty("setDataDatumID", datumIDField.getText());

    	// set data script history
    	for (int i=0; i < scriptCombo.getItems().size(); i++)
    	{
        	props.setProperty("setDataScriptHistory_" + i, scriptCombo.getItems().get(i));
    	}

    	// stored scripts
    	for (int i=0; i < storedScriptListView.getItems().size(); i++)
    	{
        	props.setProperty("setDataStoredScripts_" + i, storedScriptListView.getItems().get(i));
    	}
    	
    	try (FileOutputStream os = new FileOutputStream(new File("PDUTool.properties")))
		{
	    	props.store(os, "");
		}
    	catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
    	catch (IOException e)
		{
			e.printStackTrace();
		}
    }
    
    public void loadSettings()
    {
    	File propfile = new File("PDUTool.properties");
    	if (propfile.exists())
    	{
        	try (FileInputStream is = new FileInputStream(propfile))
    		{
    	    	props.load(is);
    		}
        	catch (FileNotFoundException e)
    		{
    			e.printStackTrace();
    		}
        	catch (IOException e)
    		{
    			e.printStackTrace();
    		}
    	}
    	
//    	disBroadcastAddress = getProperty("disBroadcastAddress", getSubnet());
    	configAddrMenu.getItems().removeIf(i -> !i.getText().startsWith("<"));
		int i=0;
    	while (true)
    	{
    		String s = props.getProperty("addressHistory_" + i++);
    		if (s != null)
    		{
				MenuItem mi = new MenuItem(s);
				mi.setOnAction(e -> addressChange(e));
				configAddrMenu.getItems().add(mi);
    		}
    		else
    		{
    			break;
    		}
    	}

    	disRecPort = Integer.valueOf(getProperty("disReceivePort", "0"));
    	disSendPort = Integer.valueOf(getProperty("disSendPort", "3000"));
    	
    	disSiteID = Integer.valueOf(getProperty("disSiteID", "99"));
    	disHostID = Integer.valueOf(getProperty("disHostID", "1"));
    	disExerciseID = Integer.valueOf(getProperty("disExerciseID", "1"));

		forceChoice.getSelectionModel().select(getProperty("force", "Friendly"));

		entityStateIDField.setText(getProperty("entityStateID", "1"));
		typeField.setText(getProperty("entityStateType", "1.2.225.50.8.0.0"));
		intervalField.setText(getProperty("entityStateInterval", "1000"));

		latField.setText(getProperty("entityStateLat", "0.0"));
		lngField.setText(getProperty("entityStateLng", "0.0"));
		elevField.setText(getProperty("entityStateElev", "0.0"));

		yawField.setText(getProperty("entityStateYaw", "0.0"));
		pitchField.setText(getProperty("entityStatePitch", "0.0"));
		rollField.setText(getProperty("entityStateRoll", "0.0"));

		setDataIDField.setText(getProperty("setDataEntityID", "1"));
		datumIDField.setText(getProperty("setDataDatumID", "1"));

    	// set data script history
		scriptCombo.getItems().clear();
		i=0;
    	while (true)
    	{
    		String s = props.getProperty("setDataScriptHistory_" + i++);
    		if (s != null)
        		scriptCombo.getItems().add(s);
    		else
    			break;
    	}
    	
    	// stored scripts
    	storedScriptListView.getItems().clear();
		i=0;
    	while (true)
    	{
    		String s = props.getProperty("setDataStoredScripts_" + i++);
    		if (s != null)
    			storedScriptListView.getItems().add(s);
    		else
    			break;
    	}
    }
    
    public String getProperty(String property, String defaultValue)
    {
    	String s = props.getProperty(property);
    	if (s == null)
    		s = defaultValue;
    	
    	return s;
    }
    
    @FXML
    public void sendStoredScript()
    {
    	String text = storedScriptListView.getSelectionModel().getSelectedItem();
    	
    	if (text != null)
    	{
    		sendSetDataPDU(text);
    	}
    }
    
    @FXML
    public void newStoredScript()
    {
    	int index = storedScriptListView.getSelectionModel().getSelectedIndex()+1;
    	
    	if (index < 0)
    		index = storedScriptListView.getItems().size();
    	
    	final int useIndex = index;
    	
		FXDialogs.createTextInputDialog("Stored Script", "New Script", "Script Text", stage, "").showAndWait().ifPresent(val ->
		{
			if (!val.trim().isEmpty())
				storedScriptListView.getItems().add(useIndex, val.trim());
		});
    }

    @FXML
    public void editStoredScript()
    {
    	int index = storedScriptListView.getSelectionModel().getSelectedIndex();
    	
    	if (index >= 0)
    	{
        	String text = storedScriptListView.getSelectionModel().getSelectedItem();
        	
    		FXDialogs.createTextInputDialog("Stored Script", "New Script", "Script Text", stage, text).showAndWait().ifPresent(val ->
    		{
    			storedScriptListView.getItems().remove(index);
    			storedScriptListView.getItems().add(index, val);
    		});
    	}
    }

    @FXML
    public void shiftUpStoredScript()
    {
    	shiftStoredScript(-1);
    }
    
    @FXML
    public void shiftDownStoredScript()
    {
    	shiftStoredScript(1);
    }

    public void shiftStoredScript(int mod)
    {
    	if (!storedScriptListView.getSelectionModel().isEmpty())
    	{
        	int index = storedScriptListView.getSelectionModel().getSelectedIndex();
        	
        	if (index+mod >= 0 && index+mod <= storedScriptListView.getItems().size()-1)
        	{
        		String str = storedScriptListView.getItems().remove(index);
        		storedScriptListView.getItems().add(index + mod, str);
        		storedScriptListView.getSelectionModel().select(index+mod);
        	}
    	}
    }

    @FXML
    public void removeStoredScript()
    {
    	int index = storedScriptListView.getSelectionModel().getSelectedIndex();
    	if (index >= 0)
    		storedScriptListView.getItems().remove(index);
    }
}
