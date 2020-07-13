# -*- coding: utf-8 -*-
"""
Local Binary Patterns

"""
import cv2,os,numpy as np, pandas as pd
from skimage import feature
import sys, getopt

def lbp_feature(img,radius=1,eps=1e-7):
    numPoints=4*radius
    image=cv2.imread(img)
    gray_image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    lbp = feature.local_binary_pattern(gray_image, numPoints,radius, method="uniform")
    (hist, _) = np.histogram(lbp.ravel(),bins=np.arange(0, numPoints + 3),range=(0, numPoints + 2))
    hist = hist.astype("float")
    hist /= (hist.sum() + eps)
    return lbp,hist

def lbp_features_each(data_path='/',storage_path='/',paths=None,labels=None,radius=1):
    points=radius*4
    count=len(paths)
    print(count,type(count),"Points:",points,"RAD",radius)
    f=np.zeros((count,points+2),float)
    count-=1
    while (count>=0):
        (_,f[count])=lbp_feature(paths[count],radius=radius)
        count-=1
    
    df=pd.DataFrame(f)
    df = df.assign(Lbl=labels[:f.shape[0]])
    #df['Y']=labels[:f.shape[0]]
    df.to_csv(storage_path)
    return df

def lbp_feature_extraction(data_path='/',storage_path='/',paths=None,labels=None,st=1,end=1,train=True):
    for radius in range(st,end+1):
      if(train==True):
        lbp_features_each(data_path=data_path,storage_path=storage_path+'lbp_'+str(radius)+'_train.csv',paths=paths,labels=labels,radius=radius)
      if(train==False):
        lbp_features_each(data_path=data_path,storage_path=storage_path+'lbp_'+str(radius)+'_test.csv',paths=paths,labels=labels,radius=radius)
    return

def gather_paths_all(jpg_path):
  label_map=['retroflex-rectum', 'out-of-patient', 'ulcerative-colitis', 'normal-cecum', 'normal-z-line', 'dyed-lifted-polyps', 'blurry-nothing', 'retroflex-stomach', 'instruments', 'dyed-resection-margins', 'stool-plenty', 'esophagitis', 'normal-pylorus', 'polyps', 'stool-inclusions', 'colon-clear']
  count=sum([len(os.listdir(jpg_path+f)) for f in os.listdir(jpg_path)])
  counta=count
  folder=os.listdir(jpg_path)
  ima=['' for x in range(count)]
  labels=np.zeros((count,len(folder)),dtype=float)
  label=[0 for x in range(count)]
  for fldr in folder:
      inner=1
      for f in os.listdir(jpg_path+fldr+"/"):
          im=jpg_path+fldr+"/"+f
          count-=1
          ima[count]=im
          label[count]=label_map.index(fldr)+1
          inner+=1
      if(count<=0):
          break
  for i in range(counta):
      labels[i][label[i]-1]=1
  return ima,label,labels




def main(argv):
   inputdir = ''
   outputfile = ''
   radius=1
   try:
      opts, args = getopt.getopt(argv,"hi:o:r:",["ifile=","ofile=","radius="])
   except getopt.GetoptError:
      print('file.py -i <inputdirectory> -o <outputfile> -r <radius>')
      sys.exit(2)
   for opt, arg in opts:
      if opt == '-h':
         print ('file.py -i <inputdirectory> -o <outputfile> -r <radius>')
         sys.exit()
      elif opt in ("-i", "--ifile"):
         inputdir = arg
      elif opt in ("-o", "--ofile"):
         outputfile = arg
      elif opt in ("-r", "--radius"):
         radius = int(arg)
   print ('Input file is "', inputdir)
   print ('Output file is "', outputfile)
   print ('Output file is "', radius)
   return inputdir,outputfile,radius

if __name__ == "__main__":
   inputdir,outputfile,radius=main(sys.argv[1:])
   paths_all,labels_all,_=gather_paths_all(inputdir)
   if(not os.path.exists(outputfile)):
       os.mkdir(outputfile)
   lbp_features_each(data_path=inputdir,storage_path=outputfile,paths=paths_all,labels=labels_all,radius=radius)
