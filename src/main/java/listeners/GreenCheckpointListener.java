package listeners;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import budDetector.Cellobject;
import budDetector.Roiobject;
import ij.gui.OvalRoi;
import net.imglib2.Point;
import net.imglib2.RealPoint;
import pluginTools.ThreeDTimeCellFileChooser;

public class GreenCheckpointListener implements ActionListener {

	final ThreeDTimeCellFileChooser parent;
	
	public GreenCheckpointListener(final ThreeDTimeCellFileChooser parent) {
		
		this.parent = parent;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {

		JFileChooser csvfile = new JFileChooser();
		FileFilter csvfilter = new FileFilter() 
		{
		      //Override accept method
		      public boolean accept(File file) {
		              
		             //if the file extension is .log return true, else false
		             if (file.getName().endsWith(".csv")) {
		                return true;
		             }
		             return false;
		      }

			@Override
			public String getDescription() {
				
				return null;
			}
		};
        String line = "";
        String cvsSplitBy = ",";
		if (parent.impOrigGreen!=null)
		csvfile.setCurrentDirectory(new File(parent.impOrigGreen.getOriginalFileInfo().directory));
		else 
			csvfile.setCurrentDirectory(new java.io.File("."));
		csvfile.setDialogTitle("Green Cell CSV file");
		csvfile.setFileSelectionMode(JFileChooser.FILES_ONLY);
		csvfile.setFileFilter(csvfilter);
		int count = 0;
		
		if (csvfile.showOpenDialog(parent.Cardframe) == JFileChooser.APPROVE_OPTION) {
			
			File budfile = new File(csvfile.getSelectedFile().getPath());
			ArrayList<Cellobject> reloadcell = new ArrayList<Cellobject>(); 
			
	        try (BufferedReader br = new BufferedReader(new FileReader(budfile))) {

	            while ((line = br.readLine()) != null) {

	                // use comma as separator
	                String[] budpoints = line.split(cvsSplitBy);
                     

	                if(count > 0) {
	                	
		                int time = Integer.parseInt(budpoints[0]);
		                double X = Double.parseDouble(budpoints[1]);
		                double Y = Double.parseDouble(budpoints[2]);
		                double Z = Double.parseDouble(budpoints[3]);
		                int Label = Integer.parseInt(budpoints[4]);
		                double Perimeter = Double.parseDouble(budpoints[5]);
		                double Area = Double.parseDouble(budpoints[6]);
		                int Intensity = Integer.parseInt(budpoints[7]);
		                double sizeX =  Double.parseDouble(budpoints[8]);
		                double sizeY = Double.parseDouble(budpoints[9]);
		                double sizeZ = Double.parseDouble(budpoints[10]);
		                
		                double[] extents = new double[] {sizeX, sizeY, sizeZ};
		                Point point = new Point(new long[] {(long)X,(long)Y,(long)Z});
		                
		                
		                Cellobject currentcell = new Cellobject(point, time, Label, Perimeter, Area, Intensity, extents);
		                
                        if(parent.CSVGreen.get(time)==null) {
                        	reloadcell = new ArrayList<Cellobject>();
                     	    parent.CSVGreen.put(time, reloadcell);    
                        }
                        else
                     	   parent.CSVGreen.put(time, reloadcell);
		                
		                    reloadcell.add(currentcell);
		         
		            }
		                 count = count +  1;
		            }
	            }
	        
	        catch (IOException e) {
	            e.printStackTrace();
	        }
		}
		else
			csvfile = null;
	            
	            
	}  
	        
	
}
		
		
