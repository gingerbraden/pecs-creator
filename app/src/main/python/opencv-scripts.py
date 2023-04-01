import cv2
import numpy as np
import os
from os.path import join
import io
import base64
from PIL import Image

def getSaliencyMapSpectralResidual(x):

    decoded_data = base64.b64decode(x)
    np_data = np.fromstring(decoded_data, np.uint8)
    img = cv2.imdecode(np_data, cv2.IMREAD_UNCHANGED)

    saliency = cv2.saliency.StaticSaliencySpectralResidual_create()
    (success, saliencyMap) = saliency.computeSaliency(img)
    saliencyMap = (saliencyMap * 255).astype("uint8")
    sal_img = Image.fromarray(saliencyMap)

    buff = io.BytesIO()
    sal_img.save(buff, format="PNG")
    img_str = base64.b64encode(buff.getvalue())
    return ""+str(img_str, 'utf-8')

def getSaliencyMapFineGrained(x):

   decoded_data = base64.b64decode(x)
   np_data = np.fromstring(decoded_data, np.uint8)
   img = cv2.imdecode(np_data, cv2.IMREAD_UNCHANGED)

   saliency = cv2.saliency.StaticSaliencyFineGrained_create()
   (success, saliencyMap) = saliency.computeSaliency(img)
   saliencyMap = (saliencyMap * 255).astype("uint8")
   sal_img = Image.fromarray(saliencyMap)

   buff = io.BytesIO()
   sal_img.save(buff, format="PNG")
   img_str = base64.b64encode(buff.getvalue())
   return ""+str(img_str, 'utf-8')

def returnCropped(obrazok, bulin):

    if not bulin:
        decoded_data = base64.b64decode(getSaliencyMapFineGrained(obrazok))
    else:
        decoded_data = base64.b64decode(getSaliencyMapSpectralResidual(obrazok))
    np_data = np.fromstring(decoded_data, np.uint8)
    image = cv2.imdecode(np_data, cv2.IMREAD_UNCHANGED)

    original = image.copy()
    gray = image.copy()
    blur = cv2.GaussianBlur(gray, (9,9), 0)
    thresh = cv2.threshold(gray,0,255,cv2.THRESH_OTSU + cv2.THRESH_BINARY)[1]

    # morphological transformations
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (2,2))
    opening = cv2.morphologyEx(thresh, cv2.MORPH_OPEN, kernel) # remove noise
    dilate_kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (9,9))
    dilate = cv2.dilate(opening, dilate_kernel, iterations=5)

    cnts = cv2.findContours(dilate, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    cnts = cnts[0] if len(cnts) == 2 else cnts[1]
    cnts = sorted(cnts, key=cv2.contourArea, reverse=True)

    for c in cnts:
        x,y,w,h = cv2.boundingRect(c)

        decoded_data = base64.b64decode(obrazok)
        np_data = np.fromstring(decoded_data, np.uint8)
        ogimage = cv2.imdecode(np_data, cv2.IMREAD_UNCHANGED)

        ROI = ogimage[y:y+h, x:x+w]
        ROI = cv2.cvtColor(ROI, cv2.COLOR_BGR2RGB)
        roi_img = Image.fromarray(ROI)
        break

    buff = io.BytesIO()
    roi_img.save(buff, format="PNG")
    img_str = base64.b64encode(buff.getvalue())
    return ""+str(img_str, 'utf-8')


def returnCoordinates(obrazok):
    decoded_data = base64.b64decode(getSaliencyMapFineGrained(obrazok))
    np_data = np.fromstring(decoded_data, np.uint8)
    image = cv2.imdecode(np_data, cv2.IMREAD_UNCHANGED)

    original = image.copy()
    gray = image.copy()
    blur = cv2.GaussianBlur(gray, (9,9), 0)
    thresh = cv2.threshold(gray,0,255,cv2.THRESH_OTSU + cv2.THRESH_BINARY)[1]

    # morphological transformations
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (2,2))
    opening = cv2.morphologyEx(thresh, cv2.MORPH_OPEN, kernel) # remove noise
    dilate_kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (9,9))
    dilate = cv2.dilate(opening, dilate_kernel, iterations=5)

    cnts = cv2.findContours(dilate, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    cnts = cnts[0] if len(cnts) == 2 else cnts[1]
    cnts = sorted(cnts, key=cv2.contourArea, reverse=True)

    x,y,w,h = cv2.boundingRect(cnts[0])

    decoded_data = base64.b64decode(obrazok)
    np_data = np.fromstring(decoded_data, np.uint8)
    image = cv2.imdecode(np_data, cv2.IMREAD_UNCHANGED)
    Hx,Hy,Hw,Hh = findFaceOutline(image)

    if x > Hx: x = Hx
    if y > Hy: y = Hy
    if x+Hw > x + w: w = Hw
    if y+Hh > y + h: h = Hh


    return str(x) + ":" + str(y) + ":" + str(w) + ":" + str(h) + ":" + str(Hx) + ":" + str(Hy) + ":" + str(Hw) + ":" + str(Hh)


def findFaceOutline(obrazok):
    face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')
    # Read the input image
    img = obrazok
    # Convert into grayscale
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    # Detect faces
    faces = face_cascade.detectMultiScale(gray, 1.1, 4)
    # Draw rectangle around the faces


    lowestx = img.shape[1]
    lowesty = img.shape[0]
    highestx = 0
    highesty = 0

    for (x, y, w, h) in faces:
        if x < lowestx: lowestx = x
        if y < lowesty: lowesty = y
        if x+w > highestx+x: highestx = w
        if y+h > highesty+y: highesty = h

    return [lowestx, lowesty, highestx, highesty]

