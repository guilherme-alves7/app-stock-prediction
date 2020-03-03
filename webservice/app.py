# -*- coding: utf-8 -*-
#!flask/bin/python - https://medium.com/@umerfarooq_26378/web-services-in-python-ef81a9067aaf
import tensorflow as tf
import requests
import os
from numpy import array
from numpy import reshape
import sys
import numpy as np # linear algebra
import pandas as pd # data processing, CSV file I/O (e.g. pd.read_csv)
from keras.models import Sequential
from keras.layers import LSTM,Dense
from sklearn.preprocessing import MinMaxScaler
#import matplotlib.pyplot as plt
from keras.models import model_from_json
import json

from flask import Flask, jsonify, current_app
from flask import request
from flask import json, Response

app = Flask(__name__)
graph = []
model = None

@app.route('/', methods=['GET'])
def index():
	return current_app.send_static_file('index.html')


@app.route('/prediction/post', methods=['POST'])
def post():
	global graph
	with graph.as_default():
		if request.is_json:
			json_post = request.get_json()
			symbol = json_post['symbol']
			symbol = symbol.upper()

			closes = []
			for value in json_post['close']:
				#closes.append( np.float64(value) )
				closes.append( value )
			
			data = predict_by_closes_symbol(closes, symbol) #call function predict

		else: #not JSON
			data = { 'success':False, 'msg':'POST não contém um JSON', 'predicted':None }

		json_string = json.dumps(data, ensure_ascii = False)
		#creating a Response object to set the content type and the encoding
		response = Response(json_string, content_type="application/json; charset=utf-8")
		return response


@app.route('/prediction/post/days', methods=['POST'])
def post_days():
	global graph
	with graph.as_default():
		if request.is_json:
			json_post = request.get_json()
			symbol = json_post['symbol']
			symbol = symbol.upper()

			days = json_post['days']
			days = int(days)

			closes = []
			for value in json_post['close']:
				closes.append( value )
			
			data = predict_several_days(days, closes, symbol)

		else: #not JSON
			data = { 'success':False, 'msg':'POST não contém um JSON', 'predicted':None }

		json_string = json.dumps(data, ensure_ascii = False)
		#creating a Response object to set the content type and the encoding
		response = Response(json_string, content_type="application/json; charset=utf-8")
		return response


@app.route('/prediction/get/api/alpha', methods=['GET'])
def get_api_alpha():
	global graph
	with graph.as_default():
		symbol = request.args.get('symbol', '')
		symbol = symbol.upper()
		#print(symbol)

		#response = requests.get('https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol='+symbol+'&apikey=86N2VD9UOJ28QPXG')
		response = requests.get('https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol='+symbol+'&apikey=35N9FJM40DDWOY3V')
		if response.status_code == 200:		

			json_response = response.json().get("Time Series (Daily)")
			#print('---------- json_response ----------')
			#print(json_response)

			if json_response != None:
				i=0
				closes = []
				for day in json_response:
					if i < 8: # get stock price for the last 8 days
						i = i+1
						stock = json_response.get(day)
						closes.append(stock.get('4. close'))

				closes.reverse() #reverse order array
				data = predict_by_closes_symbol(closes, symbol) #call function predict

			else:
				data = { 'success':False, 'msg':'Limite de 5 requisições por minuto atingido', 'predicted':None, 'current':None }
		else:
			data = { 'success':False, 'msg':'Requisição inválida', 'predicted':None, 'current':None }

		json_string = json.dumps(data, ensure_ascii = False)
		#creating a Response object to set the content type and the encoding
		response = Response(json_string, content_type="application/json; charset=utf-8")
		return response


@app.route('/prediction/get/api/alpha/days', methods=['GET'])
def get_api_alpha_days():
	global graph
	with graph.as_default():
		symbol = request.args.get('symbol', '')
		symbol = symbol.upper()
		
		days = request.args.get('days', 0)
		days = int(days)

		#response = requests.get('https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol='+symbol+'&apikey=86N2VD9UOJ28QPXG')
		response = requests.get('https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol='+symbol+'&apikey=35N9FJM40DDWOY3V')
		if response.status_code == 200:		

			json_response = response.json().get("Time Series (Daily)")
			#print('---------- json_response ----------')
			#print(json_response)

			if json_response != None:
				i=0
				closes = []
				for day in json_response:
					if i < 8: # get stock price for the last 8 days
						i = i+1
						stock = json_response.get(day)
						closes.append(stock.get('4. close'))

				closes.reverse() #reverse order array
				data = predict_several_days(days, closes, symbol)

			else:
				data = { 'success':False, 'msg':'Limite de 5 requisições por minuto atingido', 'predicted':None, 'current':None }
		else:
			data = { 'success':False, 'msg':'Requisição inválida', 'predicted':None, 'current':None }

		json_string = json.dumps(data, ensure_ascii = False)
		#creating a Response object to set the content type and the encoding
		response = Response(json_string, content_type="application/json; charset=utf-8")
		return response


@app.route('/prediction/get/api/iex', methods=['GET'])
def get_api_iex():
	global graph
	with graph.as_default():
		symbol = request.args.get('symbol', '')
		symbol = symbol.upper()

		response = requests.get('https://cloud.iexapis.com/stable/stock/'+symbol+'/chart/1m?token=pk_d5b21f257bc045239f67819e432d7fbd')
		if response.status_code == 200:

			text = response.text
			json_response = json.loads(text)

			j=0
			closes = []
			for i in range(len(json_response)-1, 0, -1):
				if j < 7: # get stock price for the last 7 days
					closes.append( json_response[i]['close'] )
					j = j+1

			closes.reverse() #reverse order array
			data = predict_by_closes_symbol(closes, symbol) #call function predict

		else:
			data = { 'success':False, 'msg':'Requisição inválida', 'predicted':None, 'current':None }

		json_string = json.dumps(data, ensure_ascii = False)
		#creating a Response object to set the content type and the encoding
		response = Response(json_string, content_type="application/json; charset=utf-8")
		return response


@app.route('/prediction/get/api/iex/days', methods=['GET'])
def get_api_iex_days():
	global graph
	with graph.as_default():
		symbol = request.args.get('symbol', '')
		symbol = symbol.upper()

		days = request.args.get('days', 0)
		days = int(days)

		response = requests.get('https://cloud.iexapis.com/stable/stock/'+symbol+'/chart/1m?token=pk_d5b21f257bc045239f67819e432d7fbd')
		if response.status_code == 200:

			text = response.text
			json_response = json.loads(text)

			j=0
			closes = []
			for i in range(len(json_response)-1, 0, -1):
				if j < 7: # get stock price for the last 7 days
					closes.append( json_response[i]['close'] )
					j = j+1

			closes.reverse() #reverse order array
			data = predict_several_days(days, closes, symbol)

		else:
			data = { 'success':False, 'msg':'Requisição inválida', 'predicted':None, 'current':None }

		json_string = json.dumps(data, ensure_ascii = False)
		#creating a Response object to set the content type and the encoding
		response = Response(json_string, content_type="application/json; charset=utf-8")
		return response


def predict_several_days(days, closes, symbol):
	data = predict_by_closes_symbol(closes, symbol) #call function predict	
	current_value = data['current']

	for day in range(0, days):
		if data['success']:
			closes.pop(0) #remove first close value
			closes.append( np.float64(data['predicted']) ) #add predicted value on closes

			data = predict_by_closes_symbol(closes, symbol)
			data['current'] = current_value

	return data


def predict_by_closes_symbol(closes, symbol):
	if len(closes) > 0 :
		if len(closes) > 7:
			current_value = closes.pop( len(closes)-1 )
		else:
			current_value = None

		# define array
		cl = array(closes)

		scl = MinMaxScaler()
		#Scale the data
		cl = cl.reshape(cl.shape[0],1)
		cl = scl.fit_transform(cl)

		if os.path.exists("../rede/data/"+symbol+".h5"):

			# load weights into new model
			global model
			model.load_weights("../rede/data/"+symbol+".h5")
			print("\n\n--- Loaded model from disk ---")
			#print( len(model.layers[0].get_weights()) )

			Xt = model.predict(cl.reshape(1,7,1))
			predicted_value = scl.inverse_transform(Xt)
			predicted_value = predicted_value[0][0]
			
			print('predicted: '+ str(predicted_value))
			#return ('predicted:{0}'.format(scl.inverse_transform(Xt)))
			data = { 'success':True, 'msg':'Previsão gerada com sucesso', 'predicted':str(predicted_value), 'current':str(current_value) }
		else:
			data = { 'success':False, 'msg':'Empresa ainda não treinada', 'predicted':None, 'current':None }
	
	else: #empty closes
		data = { 'success':False, 'msg':'Valores de fechamento vazio', 'predicted':None, 'current':None }

	return data


def load_model():
	# load json and create model
	json_file = open('../rede/data/model.json', 'r')
	loaded_model_json = json_file.read()
	json_file.close()

	global model
	model = model_from_json(loaded_model_json)	

	global graph
	graph = tf.get_default_graph()


if __name__ == "__main__":
	print("* Loading Keras model and Flask starting server...please wait until server has fully started")
	load_model()	

	app.run(host='192.168.0.101', port=5000)
	#app.run(host='0.0.0.0', port=80) #use on AWS
	#app.run(debug=True)
	#app.run(use_reloader=False, debug=True)
	#app.run()