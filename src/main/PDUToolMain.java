package main;

import geotransform.ellipsoids.WE_Ellipsoid;
import geotransform.transforms.Gcc_To_Gdc_Converter;
import geotransform.transforms.Gdc_To_Gcc_Converter;

import gui.PDUToolWindow;
import javafx.application.Application;
import javafx.stage.Stage;
import utilities.UTMToGccConverter;

public class PDUToolMain extends Application
{
    public static final int disVersion = 6;

	public static void main(String[] args)
	{
		Gdc_To_Gcc_Converter.Init(new WE_Ellipsoid());
		Gcc_To_Gdc_Converter.Init(new WE_Ellipsoid());
		UTMToGccConverter.Init(new WE_Ellipsoid());
		
		Application.launch(args);
	}
	
	@Override
	public void start(Stage stage)
	{
		PDUToolWindow.createPDUToolWindow(stage);
	}
}