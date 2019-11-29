from flask import Flask
from flask import Response
from flask import request
from flask import jsonify
import json


import pandas as pd
import numpy as np
import glob
import sys
import pickle
import scipy.signal as signal
import matplotlib.pyplot as plt
from scipy.fftpack import fft, fftshift
from numpy import linalg as LA
import copy
from matplotlib.colors import ListedColormap
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, log_loss, confusion_matrix, classification_report
from sklearn.model_selection import train_test_split
from sklearn.neural_network import MLPClassifier
from sklearn.neighbors import KNeighborsClassifier, RadiusNeighborsClassifier
from sklearn.gaussian_process import GaussianProcessClassifier
from sklearn.gaussian_process.kernels import RBF
from sklearn.tree import DecisionTreeClassifier
from sklearn.ensemble import RandomForestClassifier, AdaBoostClassifier, GradientBoostingClassifier
from sklearn.naive_bayes import GaussianNB, MultinomialNB, BernoulliNB
from sklearn.discriminant_analysis import QuadraticDiscriminantAnalysis, LinearDiscriminantAnalysis
from sklearn.svm import SVC, LinearSVC, NuSVC
from sklearn.model_selection import StratifiedShuffleSplit, StratifiedKFold
from sklearn.linear_model import LogisticRegression
from sklearn.preprocessing import StandardScaler
from operator import itemgetter

	
def predict_song(input):
	model = pickle.load(open('model.pkl', 'rb'))

	x_test = np.array([input['location'], input['time'], input['movement']]).reshape(1, -1)
	y_pred = model.predict(x_test)
	location_dict = {1: 'home', 2: 'school', 3: 'grocery', 4: 'gym', 5: 'library', 6: 'street', 7:'restaurant', 8:'park'}
	time_dict = {1: 'morning', 2: 'noon', 3: 'afternoon', 4: 'evening', 5: 'night', 6: 'midnight'}
	movement_dict = {1: 'stationary', 2: 'slow', 3: 'fast'}
	activity_dict = {1: 'rest', 2: 'walk', 3: 'workout', 4: 'study', 5: 'driving'}

	return {"result": activity_dict[y_pred[0]]}

app = Flask(__name__)

@app.route('/')
def root():
	html =		"<html>" \
			+ "<head><title>CSE535-Gesture Predictor</title></head>" \
			+ 	"<body>" \
			+ 		"<h1>Gesture Predictor - CSE 535 Project - Group 3</h1>" \
			+ 		"<h2>API Endpoints:</h2>" \
			+ 		"<ul>"\
			+ 			"<li>" + "<b>Gesture Prediction:</b> " + request.host_url + "api/predict_song" + " [POST] 'application/json'" + "</li>" \
			+ 		"</ul>" \
			+ 	"</body>" \
			+ "</html>"
	return html

@app.route('/api/predict_song', methods=[ 'POST' ])
def api_predict_song():
	location_info = request.get_json()
	if(location_info == None):
		return "ERROR reading json data from request"
	result = predict_song(location_info)
	print("result: ", result)
	return Response(json.dumps(result), mimetype='application/json')



if __name__ == '__main__':
	app.run(debug=True, host='0.0.0.0')
