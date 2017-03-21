import tensorflow as tf
from tensorflow.contrib.session_bundle import exporter
import data_handler
import numpy as np

batch_size = 100
data_sets = data_handler.load_data()

# Connection between tensorflow & python
sess = tf.Session()
tf.logging.set_verbosity(tf.logging.INFO)

# Input Placeholders : Labels + Images
labels_placeholder = tf.placeholder(tf.int64, shape=[None])
images_placeholder = tf.placeholder(tf.float32, shape=[None, 3072])

# Weights and Bias
W = tf.Variable(tf.zeros([3072, 3]), name='W')
b = tf.Variable(tf.zeros([3]),name='b')

# Softmax
y = tf.nn.softmax(tf.matmul(images_placeholder, W) + b, name='y')

# Create different collections ( layers ) and add them together
tf.add_to_collection('variables', W)
tf.add_to_collection('variables', b)

# Cross Entropy
cross_entropy = tf.reduce_mean(tf.nn.sparse_softmax_cross_entropy_with_logits(logits=y, labels=labels_placeholder))

# Training using Gradient Descent Algorithm
train_step = tf.train.GradientDescentOptimizer(0.005).minimize(cross_entropy)

# Save summaries for visualization ( Histograms + Scalar )
tf.summary.histogram('weights', W)
tf.summary.histogram('max_weight', tf.reduce_max(W))
tf.summary.histogram('bias', b)
tf.summary.scalar('cross_entropy', cross_entropy)
tf.summary.histogram('cross_hist', cross_entropy)

# Merge all summaries into one op
merged = tf.summary.merge_all()

# FileWriter for tensorboard graph log set-up
trainwriter = tf.summary.FileWriter('data/mnist_model'+'/logs/train', sess.graph)

init = tf.global_variables_initializer()
sess.run(init)

for i in range(1000):  # Train 1000 steps
    indices = np.random.choice(data_sets['images_train'].shape[0], batch_size)
    images_batch = data_sets['images_train'][indices]
    labels_batch = data_sets['labels_train'][indices]
    summary, _ = sess.run([merged, train_step], feed_dict={images_placeholder: images_batch, labels_placeholder: labels_batch})
    trainwriter.add_summary(summary, i)

# model export path
export_path = 'data/mnist_model'
print('Exporting trained model to', export_path)


saver = tf.train.Saver(sharded=True)
model_exporter = exporter.Exporter(saver)
model_exporter.init(
    sess.graph.as_graph_def(),
    named_graph_signatures={
        'inputs': exporter.generic_signature({'images': images_placeholder}),
        'outputs': exporter.generic_signature({'scores': y})})

# Save the snapshot of the trained model -- > can be load for testing later
model_exporter.export(export_path, tf.constant(1), sess)
