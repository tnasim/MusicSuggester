#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
CSE535, group3, Project
Machine learning part
Created on Wed Nov 27 10:20:18 2019
"""
import numpy as np
from sklearn.tree import DecisionTreeClassifier
import pandas as pd
import pickle
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, log_loss, confusion_matrix, classification_report

data = pd.read_csv('proj.csv', index_col=None, header=0)
#print (data)

######shuffeling

data_shuf = pd.DataFrame(data.apply(np.random.permutation, axis=0), columns=data.columns.tolist())
#print(data_shuf)
collist = data.columns.tolist()
#print(collist)

######splite
X =data_shuf[['Location', 'Time', 'Movement']]
y = data_shuf['Predicted Activity']
#X = data_shuf.loc[:,0:-1]
#y = data_shuf.loc[:,-1]
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.05, random_state=42, shuffle=True)
print(X_train)

#######Decision Tree
#DTC = DecisionTreeClassifier(criterion='gini', splitter='best', max_depth=None, min_samples_split=10,
#            min_samples_leaf=3, min_weight_fraction_leaf=0.0, max_features=None, random_state=None, max_leaf_nodes=None,
#            min_impurity_decrease=0.0, min_impurity_split=None, class_weight=None, presort=False)
DTC = DecisionTreeClassifier()


DTC.fit(X, y)
y_pred = DTC.predict(X_test)
accuracy_scores=accuracy_score(y_test, y_pred)
precision_scores=precision_score(y_test, y_pred, average='micro')
recall_scores= recall_score(y_test, y_pred, average='micro')
f1_scores=f1_score(y_test, y_pred, average='micro')
print(accuracy_scores)
print(precision_scores)
print(recall_scores)
print(f1_scores)
pickle.dump(DTC, open("model.pkl", "wb"))