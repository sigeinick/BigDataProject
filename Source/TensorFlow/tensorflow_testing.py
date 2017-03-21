import tensorflow as tf
import data_handler
import numpy as np


tf.logging.set_verbosity(tf.logging.DEBUG)
sess = tf.Session()

# Import CIFAR-10 data using data_helpers.py to unpack
data_sets = data_handler.load_data()

# restore the saved model ( Import the data from the Training session)
new_saver = tf.train.import_meta_graph('data/mnist_model/00000001/export.meta')
new_saver.restore(sess, 'data/mnist_model/00000001/export')

# print to see the restored variables
for v in tf.get_collection('variables'):
    print(v.name)
print(sess.run(tf.global_variables()))

# Initialize weight and bias ; get saved weights ( using existing weights and bias from the trained model )
W = tf.get_collection('variables')[0]
b = tf.get_collection('variables')[1]

# placeholders for test images and labels
labels_placeholder = tf.placeholder(tf.int64, shape=[None])
images_placeholder = tf.placeholder(tf.float32, shape=[None, 3072])

# predict equation
y = tf.nn.softmax(tf.matmul(images_placeholder, W) + b, name='y')

# compare predicted label and actual label
correct_prediction = tf.equal(tf.argmax(y, 1), labels_placeholder)

# accuracy op
accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))
accu = sess.run(accuracy, feed_dict={images_placeholder: data_sets['images_test'],
                                     labels_placeholder: data_sets['labels_test']})

# Save summaries for visualization ( Histograms + Scalar )
with tf.name_scope('accuracy'):
  with tf.name_scope('correct_prediction'):
    correct_prediction = tf.equal(tf.argmax(y, 1), tf.argmax(labels_placeholder, 1))
  with tf.name_scope('accuracy'):
    accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))
tf.summary.scalar('accuracy', accuracy)


# Merge all summaries into one op
merged = tf.summary.merge_all()

# FileWriter for tensorboard graph log set-up
testwriter = tf.summary.FileWriter('data/mnist_model'+'/logs/test', sess.graph)
init = tf.global_variables_initializer()
sess.run(init)

#for i in range(1000):
    #indices = np.random.choice(data_sets['images_test'].shape[0], 100)
    #images_batch = data_sets['images_test'][indices]
    #labels_batch = data_sets['labels_test'][indices]
    #summary, acc = sess.run([merged, accuracy], feed_dict={
        #images_placeholder: data_sets['images_test'],
        #labels_placeholder: data_sets['labels_test']})
    #testwriter.add_summary(summary, i)



print("\n ---> Accuracy : " + str(accu))
