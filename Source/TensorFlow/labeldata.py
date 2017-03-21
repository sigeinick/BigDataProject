''' Extract labels out of data for Testing and Training data sets'''
file = open("Testing_data.txt", 'r')
Testlines = file.readlines()

fileTest = open("Testing_label.txt", 'w')
NewTest_data = open("NewTest_data.txt", 'w')
for TestData in Testlines:
    fileTest.write(TestData[0] + "\n")
    NewTest_data.write(TestData[2:])

file.close()
fileTest.close()
NewTest_data.close()

file2 = open("Training_data.txt", 'r')
Trainlines = file2.readlines()

fileTrain = open("Training_label.txt", 'w')
NewTrain_data = open("NewTraining_data.txt", 'w')
for TrainData in Trainlines:
    fileTrain.write(TrainData[0]+ "\n")
    NewTrain_data.write(TrainData[2:])

file2.close()
fileTrain.close()
NewTrain_data.close()
