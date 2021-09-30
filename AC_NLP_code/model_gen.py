import nlp_arch as nlp
import wrangler as wgl
import pyspark
import os
from pyspark.sql import SparkSession
from sklearn.metrics import f1_score, balanced_accuracy_score, accuracy_score

def main_v1(data_path, epochs, BATCH_SIZE, DOI_limit = None):
    os.environ['PYSPARK_DRIVER_PYTHON'] = os.environ['PYSPARK_PYTHON']
    model_save_dir = ""
    loss_history_to_DOI = {}
    spark = SparkSession.builder.appName('Model_gen').getOrCreate()
    model_path = "allenai/scibert_scivocab_uncased"
    model = nlp.NLPTokenClassification(model_path, 2)
    data_path = "D:/NLPScibert/data/sentence_annotations_elsevier_pmid_split1_brenda_data-004.csv"
    df = spark.read.csv(data_path)
    if DOI_limit == None:
        DOI_array = df.select('_c13').distinct().collect()
    else:
        DOI_array = df.select('_c13').distinct().limit(DOI_limit).collect()
        
    for DOI in DOI_array:
        data_wrangler = wgl.DataWrangler(spark, model.tokenizer, DOI)
        data = data_wrangler.wrangle(df)
        train_data = data.select(['Tokenized_Words', 'Labels'])
        loss_history = model.train(train_data, epochs, BATCH_SIZE, model_save_dir)
        loss_history_to_DOI.update({DOI: loss_history})
        accuracy = ...
        f1_score = ...
        balanced_accuracy = ...