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

    x,y,w,h = cv2.boundingRect(cnts[0])

    decoded_data = base64.b64decode(obrazok)
    np_data = np.fromstring(decoded_data, np.uint8)
    image = cv2.imdecode(np_data, cv2.IMREAD_UNCHANGED)

    Hx,Hy,Hw,Hh = findFaceOutline(image)
    if (findFaceOutline(image) != [0,0,0,0]):
        x = min(x, Hx)
        y = min(y, Hy)
        w = max(x + w, Hx + Hw) - x
        h = max(y  + h, Hy + Hh) - y
        return str(x) + ":" + str(y) + ":" + str(w) + ":" + str(h) + ":" + str(Hx) + ":" + str(Hy) + ":" + str(Hw) + ":" + str(Hh)
    else:
        return str(x) + ":" + str(y) + ":" + str(w) + ":" + str(h)



def findFaceOutline(obrazok):
    face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')
    # Read the input image
    img = obrazok
    # Convert into grayscale
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    # Detect faces
    faces = face_cascade.detectMultiScale(gray, 1.1, 4)
    # Draw rectangle around the faces


    if len(faces) >= 1:
        lowestx = min(faces, key=lambda r: r[0])[0]
        lowesty = min(faces, key=lambda r: r[1])[1]
        highestx = max(faces, key=lambda r: r[0]+r[2])[0] + faces[0][2] - lowestx
        highesty = max(faces, key=lambda r: r[1]+r[3])[1] + faces[0][3] - lowesty
        return [lowestx, lowesty, highestx, highesty]
    else:
        return [0,0,0,0]


