import numpy as np # linear algebra
import pandas as pd # data processing, CSV file I/O (e.g. pd.read_csv)
from keras.models import Sequential
from keras.layers import LSTM,Dense
from sklearn.preprocessing import MinMaxScaler
import matplotlib.pyplot as plt
from keras.models import model_from_json
# Input data files are available in the "../input/" directory.
# For example, running this (by clicking run or pressing Shift+Enter) will list the files in the input directory

from subprocess import check_output
#print(check_output(["ls", "../input"]).decode("utf8"))


data = pd.read_csv('../input/PETR4.csv')
cl = data.Close

scl = MinMaxScaler()
#Scale the data
cl = cl.values.reshape(cl.shape[0],1)
cl = scl.fit_transform(cl)

#print(cl)

#Create a function to process the data into 7 day look back slices
def processData(data,lb):
    X,Y = [],[]
    for i in range(len(data)-lb-1):
        X.append(data[i:(i+lb),0])
        Y.append(data[(i+lb),0])
    return np.array(X),np.array(Y)

X,y = processData(cl,7)
X_train,X_test = X[:int(X.shape[0]*0.80)],X[int(X.shape[0]*0.80):]
y_train,y_test = y[:int(y.shape[0]*0.80)],y[int(y.shape[0]*0.80):]

# load json and create model
json_file = open('data/model.json', 'r')
loaded_model_json = json_file.read()
json_file.close()
model = model_from_json(loaded_model_json)

# load weights into new model
model.load_weights("data/PETR4.h5")
print("Loaded model from disk")

#TODO precisa carregar um array do valor de fechamento dos ultimos 7 dias, essa eh a entrada para fazer a predicao

act = []
pred = []
#for i in range(250):
i=250
Xt = model.predict(X_test[i].reshape(1,7,1))
print('predicted:{0}, actual:{1}'.format(scl.inverse_transform(Xt),scl.inverse_transform(y_test[i].reshape(-1,1))))

#pred.append(scl.inverse_transform(Xt))
#act.append(scl.inverse_transform(y_test[i].reshape(-1,1)))

#result_df = pd.DataFrame({'pred':list(np.reshape(pred, (-1))),'act':list(np.reshape(act, (-1)))})

#result_df.plot(kind='line')

#Xt = model.predict(X_test)
#plt.plot(scl.inverse_transform(y_test.reshape(-1,1)))
#plt.plot(scl.inverse_transform(Xt))