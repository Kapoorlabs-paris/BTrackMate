import xml.etree.cElementTree as et
import os
import numpy as np
import pandas as pd
import csv
from skimage import measure
import napari
from qtpy.QtCore import Qt
from qtpy.QtWidgets import QSlider, QComboBox, QPushButton
from tqdm import tqdm
from matplotlib.backends.backend_qt5agg import FigureCanvasQTAgg as FigureCanvas
from matplotlib.figure import Figure
import math
import matplotlib.pyplot as plt
from btrack.dataio import  _PyTrackObjectFactory
from btrack.dataio import export_CSV
from btrack.dataio import export_LBEP
import btrack
from skimage.measure import label
from skimage.filters import sobel
from btrack.constants import BayesianUpdates
from tifffile import imread, imwrite
from btrack.dataio import import_CSV
from skimage.segmentation import find_boundaries
from PyQt5.QtCore import pyqtSlot
from scipy import spatial 
import pandas as pd
from .napari_animation import AnimationWidget
Boxname = 'TrackBox'
pd.options.display.float_format = '${:,.2f}'.format

ParentDistances = {}
ChildrenDistances = {}

AllStartParent = []
AllEndParent = []
AllID = []
AllStartChildren = []
AllEndChildren = []
   
        
def Velocity(Source, Target, XYcalibration, Zcalibration, Tcalibration):
    
    
    ts,zs,ys,xs = Source
    
    tt,zt,yt,xt = Target
    
  
    
    Velocity = (float(zs)* Zcalibration - float(zt)* Zcalibration) * (float(zs)* Zcalibration - float(zt)* Zcalibration) + (float(ys)* XYcalibration - float(yt)* XYcalibration) * (float(ys)* XYcalibration - float(yt)* XYcalibration) + (float(xs)* XYcalibration - float(xt)* XYcalibration) * (float(xs)* XYcalibration - float(xt)* XYcalibration)
    
    return math.sqrt(Velocity)/ max((float(tt)* Tcalibration-float(ts)* Tcalibration),1)
    


def GetBorderMask(Mask):
    
    ndim = len(Mask.shape)
    #YX shaped object
    if ndim == 2:
        Mask = label(Mask)
        Boundary = find_boundaries(Mask)
        
    #TYX shaped object    
    if ndim == 3:
        
        Boundary = np.zeros([Mask.shape[0], Mask.shape[1], Mask.shape[2]])
        for i in range(0, Mask.shape[0]):
            
            Mask[i,:] = label(Mask[i,:])   
            Boundary[i,:] = find_boundaries(Mask[i,:])
            
            
        #TZYX shaped object        
    if ndim == 4:

        Boundary = np.zeros([Mask.shape[0], Mask.shape[1], Mask.shape[2], Mask.shape[3]])
        
        #Loop over time
        for i in range(0, Mask.shape[0]):
            
            Mask[i,:] = label(Mask[i,:])   
            
            for j in range(0,Mask.shape[1]):
               
                          Boundary[i,j,:,:] = find_boundaries(Mask[i,j,:,:])    
        
    return Boundary  

     
        

"""
Convert an integer image into boundary points for 2,3 and 4D data

"""


def BoundaryPoints(Mask, XYcalibration, Zcalibration):
    
    ndim = len(Mask.shape)
    
    TimedMask = {}
    #YX shaped object
    if ndim == 2:
        Mask = label(Mask)
        Label = []
        VolumeLabel = [] 
        tree = []
        properties = measure.regionprops(Mask, Mask)
        for prop in properties:
            
            LabelImage = prop.image
            regionlabel = prop.label
            sizeY = abs(prop.bbox[0] - prop.bbox[2]) * XYcalibration
            sizeX = abs(prop.bbox[1] - prop.bbox[3]) * XYcalibration
            Boundary = find_boundaries(LabelImage)
            Indices = np.where(Boundary > 0)
            Indices = np.transpose(np.asarray(Indices))
            RealIndices = Indices.copy()
            for j in range(0, len(RealIndices)):
                    
                    RealIndices[j][0] = RealIndices[j][0] * XYcalibration
                    RealIndices[j][1] = RealIndices[j][1] * XYcalibration
                    
                
            tree.append(spatial.cKDTree(RealIndices))
            
            if regionlabel not in Label:
                Label.append(regionlabel)
                VolumeLabel.append(math.sqrt(sizeX * sizeX + sizeY * sizeY)/4) 
        #This object contains list of all the points for all the labels in the Mask image with the label id and volume of each label    
        TimedMask[str(0)] = [tree, Indices, Label, VolumeLabel]
        
        
    #TYX shaped object    
    if ndim == 3:
        
        Boundary = np.zeros([Mask.shape[0], Mask.shape[1], Mask.shape[2]])
        for i in range(0, Mask.shape[0]):
            
            Mask[i,:] = label(Mask[i,:])
            properties = measure.regionprops(Mask[i,:], Mask[i,:])
            Label = []
            VolumeLabel = [] 
            tree = []
            for prop in properties:
                
                LabelImage = prop.image
                regionlabel = prop.label
                sizeY = abs(prop.bbox[0] - prop.bbox[2])* XYcalibration
                sizeX = abs(prop.bbox[1] - prop.bbox[3])* XYcalibration
                Boundary[i,:LabelImage.shape[0],:LabelImage.shape[1]] = find_boundaries(LabelImage)
                Indices = np.where(Boundary[i,:,:] > 0) 
                Indices = np.transpose(np.asarray(Indices))
                RealIndices = Indices.copy()
                for j in range(0, len(RealIndices)):
                    
                    RealIndices[j][0] = RealIndices[j][0] * XYcalibration
                    RealIndices[j][1] = RealIndices[j][1] * XYcalibration
                    
                
                tree.append(spatial.cKDTree(RealIndices))
                if regionlabel not in Label:
                    Label.append(regionlabel)
                    VolumeLabel.append(math.sqrt(sizeX * sizeX + sizeY * sizeY)/4) 
                
            TimedMask[str(i)] = [tree, Indices, Label, VolumeLabel]
            
            
    #TZYX shaped object        
    if ndim == 4:

        Boundary = np.zeros([Mask.shape[0], Mask.shape[1], Mask.shape[2], Mask.shape[3]])
        
        #Loop over time
        for i in range(0, Mask.shape[0]):
            
            Mask[i,:] = label(Mask[i,:])
            properties = measure.regionprops(Mask[i,:], Mask[i,:])
            Label = []
            VolumeLabel = []
            tree = []
            for prop in properties:
                
                LabelImage = prop.image
                regionlabel = prop.label
                sizeZ = abs(prop.bbox[0] - prop.bbox[3])* Zcalibration
                sizeY = abs(prop.bbox[1] - prop.bbox[4])* XYcalibration
                sizeX = abs(prop.bbox[2] - prop.bbox[5])* XYcalibration
                #Loop over Z
                if regionlabel > 1: 
                    for j in range(int(prop.bbox[0]),int(prop.bbox[3])):
               
                          Boundary[i,j,:LabelImage.shape[1],:LabelImage.shape[2]] = find_boundaries(LabelImage[j,:,:])
                else:
                    for j in range(int(prop.bbox[0]),int(prop.bbox[3])):
               
                          Boundary[i,j,:,:] = find_boundaries(Mask[i,j,:,:])
                
                Indices = np.where(Boundary[i,:] > 0)
                
                Indices = np.transpose(np.asarray(Indices))
                RealIndices = Indices.copy()
                for j in range(0, len(RealIndices)):
                    
                    RealIndices[j][0] = RealIndices[j][0] * Zcalibration
                    RealIndices[j][1] = RealIndices[j][1] * XYcalibration
                    RealIndices[j][2] = RealIndices[j][2] * XYcalibration
                    
                
                tree.append(spatial.cKDTree(RealIndices))
                if regionlabel not in Label:
                    Label.append(regionlabel)
                    VolumeLabel.append(math.sqrt(sizeX * sizeX + sizeY * sizeY)/4) 
                
            
            
            TimedMask[str(i)] = [tree, Indices, Label, VolumeLabel]    

    return TimedMask
    

def CreateTrackCheckpoint(Image, Label, Mask, Name, savedir):
    
    
    assert Image.shape == Label.shape
    
    TimeList = []
    
    XList = []
    YList = []
    ZList = []
    LabelList = []
    PerimeterList = []
    VolumeList = []
    IntensityList = []
    ExtentXList = []
    ExtentYList = []
    ExtentZList = []
    
    print('Image has shape:', Image.shape)
    print('Image Dimensions:', len(Image.shape))
    #Add Z to make TZYX image
    if len(Image.shape) <=3:
        
          Image4D = np.zeros([Image.shape[0], 2, Image.shape[1], Image.shape[2]])
          Label4D = np.zeros([Image.shape[0], 2, Image.shape[1], Image.shape[2]])
          
          for i in range(0,Image4D.shape[1]):
              
              Image4D[:,i,:] = Image
              Label4D[:,i,:] = Label

          Image = Image4D
          Label = Label4D
    if Mask is not None:      
            if len(Mask.shape) < len(Image.shape):
                # T Z Y X
                UpdateMask = np.zeros([Label.shape[0], Label.shape[1], Label.shape[2], Label.shape[3]])
                for i in range(0, UpdateMask.shape[0]):
                    for j in range(0, UpdateMask.shape[1]):
                        
                        UpdateMask[i,j,:,:] = Mask[i,:,:]
            else:
                UpdateMask = Mask
    for i in tqdm(range(0, Image.shape[0])):
        
        CurrentSegimage = Label[i,:].astype('uint16')
        Currentimage = Image[i,:]
        if Mask is not None:
            CurrentSegimage[UpdateMask[i,:] == 0] = 0
        properties = measure.regionprops(CurrentSegimage, Currentimage)
        for prop in properties:
            
            
            Z = prop.centroid[0]
            Y = prop.centroid[1]
            X = prop.centroid[2]
            regionlabel = prop.label
            intensity = np.sum(prop.image)
            sizeZ = abs(prop.bbox[0] - prop.bbox[3])
            sizeY = abs(prop.bbox[1] - prop.bbox[4])
            sizeX = abs(prop.bbox[2] - prop.bbox[5])
            volume = sizeZ * sizeX * sizeY
            perimeter = sizeZ * (sizeX + sizeY)
           
            TimeList.append(i)
            XList.append(int(X))
            YList.append(int(Y))
            ZList.append(int(Z))
            LabelList.append(regionlabel)
            VolumeList.append(volume)
            PerimeterList.append(perimeter)
            IntensityList.append(intensity)
            ExtentZList.append(sizeZ)
            ExtentXList.append(sizeX)
            ExtentYList.append(sizeY)
            
            
    df = pd.DataFrame(list(zip(TimeList,XList,YList,ZList,LabelList, PerimeterList, VolumeList, IntensityList,ExtentXList , ExtentYList, ExtentZList)), index = None, 
                                              columns =['T', 'X', 'Y', 'Z', 'Label', 'Perimeter', 'Volume', 'Intensity', 'ExtentX', 'ExtentY', 'ExtentZ'])

    df.to_csv(savedir + '/' + 'FijiBTrackMateCells' + Name +  '.csv', index = False)       
    
    
    PyCsvFile = savedir + '/' + 'PythonbTrackCells' + Name +  '.csv'
    pydf = pd.DataFrame(list(zip(TimeList,XList,YList,ZList)), index = None, 
                                              columns =['t', 'x', 'y', 'z'])

    pydf.to_csv(PyCsvFile) 
            
    return PyCsvFile


def BTracker(TrackConfig, CellCSV, image, savedir, Name, search_radius = 100,step_size = 100):
  # initialise a tracker session using a context manager
  
  objects = import_CSV(CellCSV)
  with btrack.BayesianTracker() as tracker:
                
                # configure the tracker using a config file
                tracker.configure_from_file(TrackConfig)
                # set the update method and maximum search radius (both optional)
                tracker.update_method = BayesianUpdates.EXACT
                tracker.max_search_radius = 100
                # append the objects to be tracked
                tracker.append(objects)
                # track them (in interactive mode)
                tracker.track_interactive(step_size=step_size)
                # generate hypotheses and run the global optimiser
                tracker.optimize()
                # get the tracks as a python list
                tracks = tracker.tracks
                data, properties, graph = tracker.to_napari()
                
  ExportName = savedir + '/' + 'PythonbTrackTracks' + Name
  export_CSV(ExportName +  '.csv', tracks)

  LBEPExportName = savedir + '/' + 'PythonbTrackLBEP' + Name
  # export the LBEP table of lineage information
  export_LBEP( LBEPExportName +  '.txt', tracks)  
  
  
  
  
  return tracks 


'''
In this method we purge the short tracklets to exclude them for further tracking process
'''
def PurgeTracklets(RootLeaf, SplitPoints, BCellobjectSourceTarget, DividingTrajectory, mintracklength = 2):
    
    #RootLeaf object contains root in the begining and leafes after that, we remove the split point ID and leaf ID the corresponds to short tracklets

    CuratedRootLeaf = []
    CuratedSplitPoints = SplitPoints.copy()
    Root = RootLeaf[0]
    CuratedRootLeaf = RootLeaf.copy()
    if DividingTrajectory == True:
        Visited = []
        for i in range(1, len(RootLeaf)):
                                Leaf = RootLeaf[i]
                                tracklength = 0
                                while(Leaf not in SplitPoints and Leaf != Root):
                                    for sourceID, targetID, EdgeTime in BCellobjectSourceTarget:
                                        # Search for the target id corresponding to leaf                        
                                        if Leaf == targetID:
                                              # Include the split points here
                                              #Once we find the leaf we move a step back to its source to find its source
                                              Leaf = sourceID
                                              tracklength = tracklength + 1
                                              RootSplitPoint = Leaf
                                              if Leaf in SplitPoints:
                                                  break
                                              if Leaf in Visited:
                                                break
                                              Visited.append(sourceID)
                                if tracklength < mintracklength:  
                                    try:
                                       CuratedSplitPoints.remove(RootSplitPoint)
                                       CuratedRootLeaf.remove(Leaf)
                                    except:
                                        pass
                                    
                             
    return CuratedSplitPoints, CuratedRootLeaf                                          


def ImportBTrackmateXML(xml_path, Segimage, XYcalibration = 1, Zcalibration = 1, Tcalibration = 1, image = None, Mask = None, mintracklength = 2):
    
        Name = os.path.basename(os.path.splitext(xml_path)[0])
        savedir = os.path.dirname(xml_path)
        root = et.fromstring(open(xml_path).read())
          
        filtered_track_ids = [int(track.get('TRACK_ID')) for track in root.find('Model').find('FilteredTracks').findall('TrackID')]
        
        #Extract the tracks from xml
        tracks = root.find('Model').find('AllTracks')
        #Extract the cell objects from xml
        BCellobjects = root.find('Model').find('AllBCellobjects') 
        
        #Make a dictionary of the unique cell objects with their properties        
        Uniqueobjects = {}
        Uniqueproperties = {}
        
        
        if Mask is not None:
            if len(Mask.shape) < len(Segimage.shape):
                # T Z Y X
                UpdateMask = np.zeros([Segimage.shape[0], Segimage.shape[1], Segimage.shape[2], Segimage.shape[3]])
                for i in range(0, UpdateMask.shape[0]):
                    for j in range(0, UpdateMask.shape[1]):
                        
                        UpdateMask[i,j,:,:] = Mask[i,:,:]
            else:
                UpdateMask = Mask
            Mask = UpdateMask.astype('uint16')
            TimedMask = BoundaryPoints(Mask, XYcalibration, Zcalibration)
        
        for frame in BCellobjects.findall('BCellobjectsInFrame'):
            
            for BCellobject in frame.findall('BCellobject'):
                #Create object with unique cell ID
                cell_id = int(BCellobject.get("ID"))
                Uniqueobjects[cell_id] = [cell_id]
                Uniqueproperties[cell_id] = [cell_id]
                #Get the TZYX location of the cells in that frame
                Uniqueobjects[cell_id].append([BCellobject.get('POSITION_T'),BCellobject.get('POSITION_Z'), BCellobject.get('POSITION_Y'), BCellobject.get('POSITION_X') ])
                #Get other properties associated with the BCellobject
                Uniqueproperties[cell_id].append([BCellobject.get('INTENSITY')
                                                ,BCellobject.get('Radi_X'), BCellobject.get('Radi_Y'), BCellobject.get('Radi_Z')]) 
                
                
        Tracks = []
        for track in tracks.findall('Track'):

            track_id = int(track.get("TRACK_ID"))
            BCellobjectSourceTarget = []
            if track_id in filtered_track_ids:
                print('Creating Tracklets of TrackID', track_id)
                for edge in track.findall('Edge'):
                   
                   sourceID = edge.get('BCellobject_SOURCE_ID')
                   targetID = edge.get('BCellobject_TARGET_ID')
                   sourceTime = edge.get('EDGE_TIME')
                  
                   BCellobjectSourceTarget.append([sourceID,targetID, sourceTime])
                
                #Sort the tracks by edge time  
                BCellobjectSourceTarget = sorted(BCellobjectSourceTarget, key = sortTracks , reverse = False)
                
                # Get all the IDs, uniquesource, targets attached, leaf, root, splitpoint IDs
                Sources, MultiTargets, RootLeaf, SplitPoints = Multiplicity(BCellobjectSourceTarget)
                
                if len(SplitPoints) > 0:
                    SplitPoints = SplitPoints[::-1]
                    DividingTrajectory = True
                    
                else:
                    
                    DividingTrajectory = False
                    
                # Remove dqngling tracklets    
                SplitPoints, RootLeaf = PurgeTracklets(RootLeaf, SplitPoints, BCellobjectSourceTarget, DividingTrajectory, mintracklength = mintracklength)     
                    
                tstart = 0    
                for sourceID, targetID, EdgeTime in BCellobjectSourceTarget:
                     if RootLeaf[0] == sourceID:    
                             Source = Uniqueobjects[int(sourceID)][1]
                             tstart = int(float(Source[0]))
                             break
                    
                Tracklets = []        
                if DividingTrajectory == True:
                            print("Dividing Trajectory")
                            #Make tracklets
                            Root = RootLeaf[0]
                            
                            Visited = []
                            #For the root we need to go forward
                            tracklet = []
                            tracklet.append(Root)
                            trackletid = 0
                            RootCopy = Root
                            Visited.append(Root)
                            while(RootCopy not in SplitPoints and RootCopy not in RootLeaf[1:]):
                                for sourceID, targetID, EdgeTime in BCellobjectSourceTarget:
                                        # Search for the target id corresponding to leaf                        
                                        if RootCopy == sourceID:
                                              
                                              #Once we find the leaf we move a step fwd to its target to find its target
                                              RootCopy = targetID
                                              if RootCopy in SplitPoints:
                                                  break
                                              if RootCopy in Visited:
                                                break
                                              Visited.append(targetID)
                                              tracklet.append(targetID)
                                              
                            Tracklets.append([trackletid, tracklet])
                            
                            trackletid = 1       
                            for i in range(1, len(RootLeaf)):
                                Leaf = RootLeaf[i]
                                #For leaf we need to go backward
                                tracklet = []
                                tracklet.append(Leaf)
                                while(Leaf not in SplitPoints and Leaf != Root):
                                    for sourceID, targetID, EdgeTime in BCellobjectSourceTarget:
                                        # Search for the target id corresponding to leaf                        
                                        if Leaf == targetID:
                                              # Include the split points here
                                              
                                              #Once we find the leaf we move a step back to its source to find its source
                                              Leaf = sourceID
                                              if Leaf in SplitPoints:
                                                  break
                                              if Leaf in Visited:
                                                break
                                              tracklet.append(sourceID)
                                              Visited.append(sourceID)
                                Tracklets.append([trackletid, tracklet]) 
                                trackletid = trackletid + 1
                            
                            
                            # Exclude the split point near root    
                            for i in range(0, len(SplitPoints) -1):
                                Start = SplitPoints[i]
                                tracklet = []
                                tracklet.append(Start)
                                OtherSplitPoints = SplitPoints.copy()
                                OtherSplitPoints.pop(i)
                                while(Start is not Root):
                                    for sourceID, targetID, EdgeTime in BCellobjectSourceTarget:
                                        
                                        if Start == targetID:
                                            
                                            Start = sourceID
                                            if Start in Visited:
                                                break
                                            tracklet.append(sourceID)
                                            Visited.append(sourceID)
                                            if Start in OtherSplitPoints:
                                                break
                                            
                                Tracklets.append([trackletid, tracklet]) 
                                trackletid = trackletid + 1
                              
                            
                if DividingTrajectory == False:
                        print('Not Dividing Trajectory')
                        if len(RootLeaf) > 0:
                             Root = RootLeaf[0]
                             Leaf = RootLeaf[-1]
                             tracklet = []
                             trackletid = 0
                             tracklet.append(Root)
                             #For non dividing trajectories iterate from Root to the only Leaf
                             while(Root != Leaf):
                                        for sourceID, targetID, EdgeTime in BCellobjectSourceTarget:
                                                if Root == sourceID:
                                                      tracklet.append(sourceID)
                                                      Root = targetID
                                                      if Root==Leaf:
                                                          break
                                                else:
                                                    break
                             Tracklets.append([trackletid, tracklet])               
                             
                # Sort the Tracklets in time
                
                SortedTracklets = TrackletSorter(Tracklets, BCellobjectSourceTarget)
                # Create object trackID, T, Z, Y, X, speed, generationID, trackletID
                
                
                #For each tracklet create Track and Speed objects
                DictTrackobjects, DictSpeedobjects, Trackobjects, Trackletobjects = TrackobjectCreator(SortedTracklets, Uniqueobjects, XYcalibration, Zcalibration, Tcalibration)
                Tracks.append([track_id,DictTrackobjects, DictSpeedobjects, Trackobjects, Trackletobjects, SortedTracklets, tstart])
                
                
        #Sort tracks by their ID
        Tracks = sorted(Tracks, key = sortID, reverse = False)
               
        # Write all tracks to csv file as ID, T, Z, Y, X
        ID = []
        StartID = {}
        
        RegionID = {}
        VolumeID = {}
        locationID = {}    
        

        
        for trackid, DictTrackobjects, DictSpeedobjects, Trackobjects, Trackletobjects, SortedTracklets, tstart in Tracks:
            
             print('Computing Tracklets for TrackID:', trackid)  
             RegionID[trackid] = [trackid]
             VolumeID[trackid] = [trackid]
             locationID[trackid] = [trackid]
             StartID[trackid] = [trackid]
             ID.append(trackid)
             TrackletRegionID = {}
             TrackletVolumeID = {}
             TrackletlocationID = {}
             
             StartID[trackid].append(tstart)
             
             Tloc = []
             Zloc = []
             Yloc = []
             Xloc = []
             Speedloc = []
             DistanceBoundary = []
             ProbabilityInside = []
             SlocZ = []
             SlocY = []
             SlocX = []
             Vloc = []
             Iloc = []
             for j in tqdm(range(0, len(Trackletobjects))):
                         
                                    BCelltrackletid = Trackletobjects[j]
                                    TrackletRegionID[BCelltrackletid] = [BCelltrackletid]
                                    TrackletVolumeID[BCelltrackletid] = [BCelltrackletid]
                                    TrackletlocationID[BCelltrackletid] = [BCelltrackletid]
                                    TrackletLocation = []
                                    TrackletRegion = []
                                    TrackletVolume = []
                                    
                                    DictBCellobject = DictTrackobjects[BCelltrackletid][1]
                                    DictVelocityBcellobject = DictSpeedobjects[BCelltrackletid][1]
                                    
                                    for i in range(0, len(DictBCellobject)): 
                                           
                                           BCellobject = DictBCellobject[i]
                                           VelocityBcellobject = DictVelocityBcellobject[i]
                                           t = int(float(BCellobject[0]))
                                           z = int(float(BCellobject[1]))
                                           y = int(float(BCellobject[2]))
                                           x = int(float(BCellobject[3]))
                                           
                                          
                                           speed = (float(VelocityBcellobject))
                                           Tloc.append(t)
                                           Zloc.append(z)
                                           Yloc.append(y)
                                           Xloc.append(x)
                                           Speedloc.append(speed)
                                           if t < Segimage.shape[0]:
                                                   CurrentSegimage = Segimage[t,:]
                                                   if image is not None:
                                                           Currentimage = image[t,:]
                                                           properties = measure.regionprops(CurrentSegimage, Currentimage)
                                                   if image is None:
                                                           properties = measure.regionprops(CurrentSegimage, CurrentSegimage)
                                                           
                                                   TwoDCoordinates = [(prop.centroid[1] , prop.centroid[2]) for prop in properties]
                                                   TwoDtree = spatial.cKDTree(TwoDCoordinates)
                                                   TwoDLocation = (y ,x)
                                                   closestpoint = TwoDtree.query(TwoDLocation)
                                                   for prop in properties:
                                                       
                                                       
                                                       if int(prop.centroid[1]) == int(TwoDCoordinates[closestpoint[1]][0]) and int(prop.centroid[2]) == int(TwoDCoordinates[closestpoint[1]][1]):
                                                           
                                                           
                                                            sizeZ = abs(prop.bbox[0] - prop.bbox[3]) * Zcalibration
                                                            sizeY = abs(prop.bbox[1] - prop.bbox[4]) * XYcalibration
                                                            sizeX = abs(prop.bbox[2] - prop.bbox[5]) * XYcalibration
                                                            Area = prop.area
                                                            intensity = np.sum(prop.image)
                                                            Vloc.append(Area)
                                                            SlocZ.append(sizeZ)
                                                            SlocY.append(sizeY)
                                                            SlocX.append(sizeX)
                                                            Iloc.append(intensity)
                                                            TrackletRegion.append([1,sizeZ, sizeY,sizeX])
                                                            
                                                            
                                                            
                                                            # Compute distance to the boundary
                                                            if Mask is not None:
                                                                
                                                                testlocation = (z * Zcalibration ,y * XYcalibration ,x * XYcalibration)
                                                                tree, indices, masklabel, masklabelvolume = TimedMask[str(int(t))]
                                                                
                                                                
                                                                cellradius = math.sqrt( sizeX * sizeX + sizeY * sizeY)/4
                                                               
                                                                Regionlabel = Mask[int(t), int(z), int(y) , int(x)] 
                                                                for k in range(0, len(masklabel)):
                                                                    currentlabel = masklabel[k]
                                                                    currentvolume = masklabelvolume[k]
                                                                    currenttree = tree[k]
                                                                    #Get the location and distance to the nearest boundary point
                                                                    distance, location = currenttree.query(testlocation)
                                                                    distance = max(0,distance  - cellradius)
                                                                    if currentlabel == Regionlabel and Regionlabel > 0:
                                                                            probabilityInside = max(0,(distance) / currentvolume)
                                                                    else:
                                                                        
                                                                            probabilityInside = 0 
                                                            else:
                                                                distance = 0
                                                                probabilityInside = 0
                                                            
                                                            DistanceBoundary.append(distance)
                                                            ProbabilityInside.append(probabilityInside)
                                                            TrackletVolume.append([Area, intensity, speed, distance , probabilityInside])
                                                            TrackletLocation.append([t, z, y, x])
                           
                                           TrackletlocationID[BCelltrackletid].append(TrackletLocation)
                                           TrackletVolumeID[BCelltrackletid].append(TrackletVolume)
                                           TrackletRegionID[BCelltrackletid].append(TrackletRegion)
                           
             locationID[trackid].append(TrackletlocationID)
             RegionID[trackid].append(TrackletRegionID)
             VolumeID[trackid].append(TrackletVolumeID)
                 
        df = pd.DataFrame(list(zip(ID,Tloc,Zloc,Yloc,Xloc, DistanceBoundary, ProbabilityInside, SlocZ, SlocY, SlocX, Vloc, Iloc, Speedloc)), index = None, 
                                              columns =['ID', 't', 'z', 'y', 'x', 'distBoundary', 'probInside', 'sizeZ', 'sizeY', 'sizeX', 'volume', 'intensity', 'speed'])

        df.to_csv(savedir + '/' + 'Extra' + Name +  '.csv')  
        df     
        
        # create the final data array: track_id, T, Z, Y, X
        
        df = pd.DataFrame(list(zip(ID,Tloc,Zloc,Yloc,Xloc)), index = None, 
                                              columns =['ID', 't', 'z', 'y', 'x'])

        df.to_csv(savedir + '/' + 'BTrackMate' +  Name +  '.csv')  
        df

        return RegionID, VolumeID, locationID, Tracks, ID, StartID
    
 
    
 
def TrackobjectCreator(OrderedTracklets, Uniqueobjects, XYcalibration, Zcalibration, Tcalibration):

                DictTrackobjects = {}
                DictSpeedobjects = {} 
                Trackletobjects = []
                for k in range(0, len(OrderedTracklets)):
                    
                        trackletid, tracklet = OrderedTracklets[k]
                        Trackletobjects.append(trackletid)
                        Trackobjects = []
                        Speedobjects = []
                        DictTrackobjects[trackletid] = [trackletid]
                        DictSpeedobjects[trackletid] = [trackletid]
                        for i in range(0, len(tracklet)):
                            sourceID, timeID = tracklet[i]
                            if i < len(tracklet) - 1:
                              targetID, targettimeID = tracklet[i+1]
                            else:
                              targetID = sourceID
                            #All tracks
                            Source = Uniqueobjects[int(sourceID)][1]
                            Target = Uniqueobjects[int(targetID)][1]
                            speed = Velocity(Source, Target, XYcalibration, Zcalibration, Tcalibration)
                            if Target not in Trackobjects:
                               Trackobjects.append(Target)
                            Speedobjects.append(speed)
                        DictTrackobjects[trackletid].append(Trackobjects)    
                        DictSpeedobjects[trackletid].append(Speedobjects)    
                return DictTrackobjects, DictSpeedobjects, Trackobjects, Trackletobjects
            
            
def TrackletSorter(Tracklets, BCellobjectSourceTarget):

    
     OrderedTracklets = []  
     
     for trackletid , tracklet in Tracklets:
         TimeTracklet = []
         Visited = []
         for cellsourceid in tracklet:
             
              for sourceID, targetID,  EdgeTime in BCellobjectSourceTarget:
                  
                  if cellsourceid == sourceID or cellsourceid == targetID:
                                if cellsourceid not in Visited:          
                                   TimeTracklet.append([cellsourceid, EdgeTime])
                                   Visited.append(cellsourceid)
         otracklet = sorted(TimeTracklet, key = sortTracklet, reverse = False)
         if len(otracklet) > 0:
            OrderedTracklets.append([trackletid,otracklet])
     
     return OrderedTracklets

    
def Multiplicity(BCellobjectSourceTarget):

     Sources = []    
     
     MultiTargets = {}

     RootLeaf = []
     
     SplitPoints = []
     
     for sourceID, targetID, EdgeTime in BCellobjectSourceTarget:
                    
                             
                             if sourceID not in Sources:
                                  #List all the sources including the dividing ones only once
                                  Sources.append(sourceID)
     
     #The source list contains only unique sources, now we look for targets connected to it + Root and leaf nodes                           
     
     
     for i in range(0, len(Sources)):
         
           ID = Sources[i]
           Targets = []
           for sourceID, targetID, EdgeTime in BCellobjectSourceTarget:
               
               
               
               if ID == sourceID:
                   
                   Targets.append(targetID)
                   MultiTargets[str(sourceID)] = Targets
     #Append the leaf first              
     RootLeaf.append(Sources[0])
     for sourceID, targetID, EdgeTime in BCellobjectSourceTarget:
         
         TestTargets = MultiTargets[str(sourceID)]
         
         if len(TestTargets) > 1 and sourceID not in SplitPoints:
             
             SplitPoints.append(sourceID)
         
         if targetID not in Sources:
             
             RootLeaf.append(targetID)
     
             
        
     return Sources, MultiTargets, RootLeaf, SplitPoints

         

           
    
class TrackViewer(object):
    
    
    def __init__(self, originalviewer, Raw, Seg, Mask, locationID, RegionID, VolumeID,  scale, ID, startID, canvas, ax, figure, savedir, saveplot, Tcalibration):
        
        
        self.trackviewer = originalviewer
        self.Raw = Raw
        self.Seg = Seg
        self.Mask = Mask
        self.locationID = locationID
        self.RegionID = RegionID
        self.VolumeID = VolumeID
        self.scale = scale
        self.ID = ID
        self.startID = startID
        self.Tcalibration = Tcalibration
        self.saveplot = saveplot
        self.savedir = savedir
        self.layername = 'Trackpoints'
        self.layernamedot = 'Trackdot'
        self.tracklines = 'Tracklets'
        #Side plots
        self.canvas = canvas
        self.figure = figure
        self.ax = ax 
        self.AllLocations = {}
        self.AllRegions = {}

        self.LocationTracklets = []
        self.plot() 
    def plot(self):
        
            for i in range(self.ax.shape[0]):
                 for j in range(self.ax.shape[1]):
                                   self.ax[i,j].cla()
            if self.ID!=Boxname:
                
                        self.ax[0,0].set_title("CellSize")
                        self.ax[0,0].set_xlabel("minutes")
                        self.ax[0,0].set_ylabel("um")
                        
                        self.ax[1,0].set_title("Distance to Boundary")
                        self.ax[1,0].set_xlabel("minutes")
                        self.ax[1,0].set_ylabel("um")
                        
                        self.ax[0,1].set_title("Expectation Inner cell")
                        self.ax[0,1].set_xlabel("minutes")
                        self.ax[0,1].set_ylabel("Probability")
                        
                        self.ax[1,1].set_title("CellVelocity")
                        self.ax[1,1].set_xlabel("minutes")
                        self.ax[1,1].set_ylabel("um")
                        
                        self.ax[0,2].set_title("CellIntensity")
                        self.ax[0,2].set_xlabel("minutes")
                        self.ax[0,2].set_ylabel("Arb. units")
                        
                        self.ax[1,2].set_title("CellFate")
                        self.ax[1,2].set_xlabel("Start Distance")
                        self.ax[1,2].set_ylabel("End Distance")
                        
                        #Execute the function    
                        
                        Location = self.locationID[int(float(self.ID))][1]
                        Volume =  self.VolumeID[int(float(self.ID))][1]
                        Region =  self.RegionID[int(float(self.ID))][1]
                        self.AllLocations[self.ID] = [self.ID]
                        self.AllRegions[self.ID] = [self.ID]
                        
                        ParentDistances[self.ID] = [self.ID]
                        ChildrenDistances[self.ID] = [self.ID] 
                        
                        IDLocations = []
                        IDRegions = []
                        for (trackletid, tracklet) in Location.items():
                            
                            #print('Trackletid', trackletid)
                            self.AllT = []
                            self.AllIntensity = []
                            self.AllArea = []
                            self.AllSpeed = []
                            self.AllSize = []
                            self.AllDistance = []
                            self.AllProbability = []
                            Volumetracklet = Volume[trackletid][1]
                            Regiontracklet = Region[trackletid][1]
                            Locationtracklet = tracklet[1]
                            TrackLayerTracklets = []
                            #print('Locationtracklet', Locationtracklet)
                            for i in range(0, len(Locationtracklet)):
                                        t, z, y, x = Locationtracklet[i]
                                        TrackLayerTracklets.append([trackletid, t, z, y, x])
                                        area, intensity, speed, distance, probability = Volumetracklet[i]
                                        #print('Track ID:', self.ID, trackletid, 'Timepoint', t)
                                        
                                        sizeT, sizeZ, sizeY, sizeX = Regiontracklet[i]
                                        
                                        IDLocations.append([t,z,y,x])
                                        IDRegions.append([sizeT, sizeZ, sizeY, sizeX])
                                        
                                        self.AllT.append(t * self.Tcalibration)
                                        self.AllArea.append(area)
                                        self.AllIntensity.append(intensity)
                                        self.AllSpeed.append(speed)
                                        self.AllDistance.append(distance)
                                        self.AllProbability.append(probability)
                                        self.AllSize.append(math.sqrt(sizeY * sizeY + sizeX * sizeX)/4)
                                 
                            if str(self.ID) + str(trackletid) not in AllID:
                                      AllID.append(str(self.ID) + str(trackletid))
                                      if trackletid == 0: 
                                          AllStartParent.append(self.AllDistance[0])
                                          AllEndParent.append(self.AllDistance[-1])
                                          
                                      else:
                                          AllStartChildren.append(self.AllDistance[0])
                                          AllEndChildren.append(self.AllDistance[-1])
                                       
                                
                            self.ax[0,0].plot(self.AllT, self.AllSize)
                            self.ax[1,0].plot(self.AllT, self.AllDistance)
                            self.ax[0,1].plot(self.AllT, self.AllProbability)
                            self.ax[1,1].plot(self.AllT, self.AllSpeed)
                            self.ax[0,2].plot(self.AllT, self.AllIntensity)
                            self.ax[1,2].plot(AllStartParent, AllEndParent, 'og')
                            self.ax[1,2].plot(AllStartChildren, AllEndChildren, 'or')
                            self.LocationTracklets.append(TrackLayerTracklets)
                            if self.saveplot:
                                    df = pd.DataFrame(list(zip(self.AllT,self.AllSize,self.AllDistance,self.AllProbability,self.AllSpeed,self.AllIntensity)),  
                                                      columns =['Time', 'Cell Size', 'Distance to Border', 'Inner Cell Probability', 'Cell Speed', 'Cell Intensity'])
                                    df.to_csv(self.savedir + '/' + 'Track' +  str(self.ID) + 'tracklet' + str(trackletid) +  '.csv',index = False)  
                                    df
                                    
                                    df = pd.DataFrame(list(zip(AllStartParent,AllEndParent)),  
                                                      columns =['StartDistance', 'EndDistance'])
                                    df.to_csv(self.savedir + '/'  + 'ParentFate'  +  '.csv',index = False)  
                                    df
                                    
                                    df = pd.DataFrame(list(zip(AllStartChildren,AllEndChildren)),  
                                                      columns =['StartDistance', 'EndDistance'])
                                    df.to_csv(self.savedir + '/' + 'ChildrenFate'  +  '.csv',index = False)  
                                    df
                                    
                                    
                                    
                                    
                            
                        self.AllLocations[self.ID].append(IDLocations)
                        self.AllRegions[self.ID].append(IDRegions)
            self.canvas.draw()            
            self.UpdateTrack()   
            
            
    def SaveFig(self):
        
        if self.saveplot:
           self.figure.savefig(self.savedir + '/' + 'Track' +  str(self.ID) +  '.png', transparent = True )
           
           
            
                    
    def UpdateTrack(self):
        
        
        
                if self.ID != Boxname:
                    
                        
                    
                        for layer in list(self.trackviewer.layers):
                           
                                 if self.layername == layer.name:
                                     self.trackviewer.layers.remove(layer)
                                 if self.layernamedot == layer.name:
                                     self.trackviewer.layers.remove(layer)
                                     
                                 if self.tracklines in layer.name or layer.name in self.tracklines:
                                     self.trackviewer.layers.remove(layer)
        
                
                        tstart = self.startID[int(float(self.ID))][1]
                        self.trackviewer.dims.set_point(0, tstart)
                        self.trackviewer.status = str(self.ID)
                        for i in range(0, len(self.LocationTracklets)):
                            self.trackviewer.add_tracks(np.asarray(self.LocationTracklets[i]), scale = self.scale, name= self.tracklines + str(i))
                            
                            
                        self.trackviewer.theme = 'light'
                        self.trackviewer.dims.ndisplay = 3
                        self.SaveFig()
                        
                        T = self.Seg.shape[0]
                        animation_widget = AnimationWidget(self.trackviewer, self.savedir,self.ID, T)
                        self.trackviewer.window.add_dock_widget(animation_widget, area='right')
                        self.trackviewer.update_console({'animation': animation_widget.animation})

                    
            

                
def LiveTracks(Raw, Seg, Mask, savedir, scale, locationID, RegionID, VolumeID, ID, StartID, Tcalibration):

    if Mask is not None and len(Mask.shape) < len(Seg.shape):
        # T Z Y X
        UpdateMask = np.zeros_like(Seg)
        for i in range(0, UpdateMask.shape[0]):
            for j in range(0, UpdateMask.shape[1]):
                
                UpdateMask[i,j,:,:] = Mask[i,:,:]
    else:
        UpdateMask = Mask
    
    Boundary = GetBorderMask(UpdateMask.copy())
    
    with napari.gui_qt():
            if Raw is not None:
                          
                          viewer = napari.view_image(Raw, scale = scale, name='Image')
                          Labels = viewer.add_labels(Seg, scale = scale, name = 'SegImage')
            else:
                          viewer = napari.view_image(Seg, scale = scale, name='SegImage')
                          
            if Mask is not None:
                
                          LabelsMask = viewer.add_labels(Boundary, scale = scale, name='Mask')
            
            trackbox = QComboBox()
            trackbox.addItem(Boxname)
            
            tracksavebutton = QPushButton('Save Track')
            saveplot = tracksavebutton.clicked.connect(on_click)
            
        
            for i in range(0, len(ID)):
                trackbox.addItem(str(ID[i]))
            try:
               figure = plt.figure(figsize = (5, 5))    
               multiplot_widget = FigureCanvas(figure)
               ax = multiplot_widget.figure.subplots(2,3)
            except:
                pass
            viewer.window.add_dock_widget(multiplot_widget, name = "TrackStats", area = 'right')
            multiplot_widget.figure.tight_layout()
            trackbox.currentIndexChanged.connect(lambda trackid = trackbox : TrackViewer(viewer, Raw, Seg, Mask, locationID, RegionID,
                                                                                         VolumeID, scale, trackbox.currentText(), StartID,multiplot_widget, ax, figure, savedir, saveplot = False, Tcalibration = Tcalibration))
            
            if saveplot:
                tracksavebutton.clicked.connect(lambda trackid = tracksavebutton : TrackViewer(viewer, Raw, Seg, Mask, locationID, RegionID,
                                                                                         VolumeID, scale, trackbox.currentText(), StartID,multiplot_widget, ax, figure, savedir, True, Tcalibration))
                
            viewer.window.add_dock_widget(trackbox, name = "TrackID", area = 'left')
            viewer.window.add_dock_widget(tracksavebutton, name = "Save TrackID", area = 'left')
            
            
    
def DistancePlotter():
    
         
                 
             
         plt.plot(AllStartParent, AllEndParent, 'g')
         plt.title('Parent Start and End Distance')
         plt.xlabel('End Distance')
         plt.ylabel('Start Distance')
         plt.show()
         
         plt.plot(AllStartChildren, AllEndChildren, 'r')
         plt.title('Children Start and End Distance')
         plt.xlabel('End Distance')
         plt.ylabel('Start Distance')
         plt.show()
  
@pyqtSlot()            
def on_click():
        
         
         return True         
            
def sortTracks(List):
    
    return int(float(List[2]))

def sortID(List):
    
    return int(float(List[0]))


def sortTracklet(List):
    
    return int(float(List[1]))

def sortX(List):
    
    return int(float(List[-1]))

def sortY(List):
    
    return int(float(List[-2]))
    

