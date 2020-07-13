# -*- coding: utf-8 -*-
"""
Haralick

"""

import cv2, numpy as np
import os
import mahotas as mh
import pandas as pd
import sys, getopt

def gather_images_from_paths(jpg_path,start,count,img_rows,img_cols):
  print('Stats of Images Start:',start,' To:',(start+count),'All Images:',len(jpg_path))
  ima=np.zeros((count,img_rows,img_cols,3))
  for i in range(count):
      #print(jpg_path[start+i])
      img=cv2.imread(jpg_path[start+i])

      im = cv2.resize(img, (img_rows, img_cols)).astype(np.float32)
      im[:,:,0] -= 103.939
      im[:,:,1] -= 116.779
      im[:,:,2] -= 123.68
      ima[i]=im
  return ima

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

def extract_haralick_features(X,Y,haralick_features_path,counttest=0,img_rows=224,img_cols=224):
  count=1000
  i=0
  while i<counttest:
    total=count
    if(i+count>=counttest):
      total=counttest-i
    ima=gather_images_from_paths(X,i,total,img_rows,img_cols)
    hf=[mh.features.haralick(im.astype(np.uint8)).ravel() for im in ima]
    df = pd.DataFrame(data=hf)
    df = df.assign(Lbl=Y[i:i+total])
    with open(haralick_features_path, 'a') as f:
      df.to_csv(f,index=False,header=False)
    i+=count
  return


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
   count=np.sum([len(os.listdir(inputdir+f)) for f in os.listdir(inputdir)])
   X,Y1,_=gather_paths_all(inputdir)
   if(not os.path.exists(outputfile)):
       os.mkdir(outputfile)
   extract_haralick_features(X,Y1,inputdir,counttest=count,img_rows=224,img_cols=224)
