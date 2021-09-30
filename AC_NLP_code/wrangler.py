import pandas as pd
import os
import pyspark
import pandas as pd
from pyspark.sql import SparkSession, DataFrameWriter
from pyspark.sql.types import ArrayType, IntegerType, DataType
from pyspark.sql.functions import udf
from pyspark.sql.types import StructType,StructField, StringType
from pyspark.sql.functions import col
import findspark
import itertools
import nlp_arch as NLP
 
def dwrangler(data, spark, DOI):
    df = data.filter(data._c13 == DOI)
    organism = [x for x in df.select('_c4').collect()[1]._c4.split()]
    unique_prods = [x for x in df.select('_c8').collect()[1]._c8.replace(" + ", " ").replace("?", "").split()]
    unique_subs = [x for x in df.select('_c10').collect()[1]._c10.replace(" + ", " ").replace("?", "").split()]
    rdd = df.select('_c18').distinct().rdd
    rdd1 = rdd.flatMap(lambda x: x._c18.split())
    print("Subs, ", unique_subs, "\n")
    print("Prods, ", unique_prods, "\n")
    del rdd
    def labeller(word):
        if word in unique_prods:
            return 1
        if word in unique_subs:
            return -1
        if word in organism:
            return 2
        return 0
    
    rdd2 = rdd1.map(lambda x: [x, labeller(x)])
    print(rdd2.collect())
    df = rdd2.toDF(['Words', 'Labels'])
    loc_path = 'D:/NLPScibert/huggingface/AC_NLP_code/Wrangled_data/'
    path = loc_path + DOI.replace("/", " ") + '.csv'
    fd = os.open(path, os.O_RDWR|os.O_CREAT)
    f = os.fdopen(fd, "w+")
    i = 0
    for row in df.collect():
        if i == 0:
            s = "%s,%s\n" % ("Substrates " + str(unique_subs), "Products " + str(unique_prods))
            f.write(s)
        d = row.asDict()
        s = "%s,%s\n" % (d["Words"].replace(",", ""), d["Labels"])
        f.write(s)
        i = i + 1
    return [path, DOI, unique_prods, unique_subs]

def main():
    findspark.init()
    os.environ['JAVA_HOME'] = "C:\Program Files\Java\jdk1.8.0_111"
    os.environ['PYSPARK_DRIVER_PYTHON'] = os.environ['PYSPARK_PYTHON']
    data_path = 'D:/NLPScibert/data'
    files =  os.listdir(data_path)
    spark = SparkSession.builder.appName('Wrangler').getOrCreate()
    i = 0
    dirs = []
    failed = []
    for file in files:
        if i == 0:
            i = i + 1
            continue
        print('file ', file)
        df = spark.read.csv(data_path + '/' + file)
        DOI_array = df.select('_c13').distinct().collect()
        for DOI in DOI_array:
            try:
                result = dwrangler(df, spark, DOI._c13)
            except:
                failed.append(DOI._c13)
            dirs.append(result[0])
        i = i + 1
    return (dirs, failed)

if __name__ == "__main__":
    results = main()
    print(results[0])
    print(results[1])
    with open('~/Wrangled_data/Failed_DOI.tx') as f:
        for fail in results[1]:
            f.write(str(fail))




"""Test Cases"""
def basetest():
    findspark.init()
    try:
        print(os.environ['JAVA_HOME'])
    except:
        print("No JAVA_HOME")
    try:
        print(os.environ['SPARK_HOME'])
    except:
        print("No SPARK_HOME")
    try:
        print(os.environ['HADOOP_HOME'])
    except:
        print('No HADOOP_HOME')
    try:
        print(os.environ['PYSPARK_PYTHON'])
    except:
        print('No PYSPARK_PYTHON')
    try:
        print(os.environ['PYSPARK_DRIVER_PYTHON'])
    except:
        print("No PYTHON_DRIVER_VAR")
    print('Environ Var test terminated')
    print("\n")
    
def sparktest():
    spark = SparkSession.builder.appName('Test1').getOrCreate()
    spark.__exit__
    print('Spark Test Terminated')
    print("\n")

def test2():
    print('Test 2')
    findspark.init()
    os.environ['JAVA_HOME'] = "C:\Program Files\Java\jdk1.8.0_111"
    os.environ['PYSPARK_DRIVER_PYTHON'] = os.environ['PYSPARK_PYTHON']
    model_path = "allenai/scibert_scivocab_uncased"
    #model_path = "roberta-base"
    model = NLP.NLPTokenClassification(model_path, 2)
    data_path = "D:/NLPScibert/data/sentence_annotations_elsevier_pmid_split1_brenda_data-004.csv"
    spark = SparkSession.builder.appName('Test1').getOrCreate()
    df = spark.read.csv(data_path)
    DOI_array = df.select('_c13').distinct().limit(20).collect()
    DOI = DOI_array[0].__getitem__("_c13")
    print('DOI: ', DOI)
    path = dwrangler(df, spark, DOI)
    print(path)
        
"""Run Test Cases"""
#basetest()
#os.environ['PYSPARK_DRIVER_PYTHON'] = os.environ['PYSPARK_PYTHON']
#sparktest()
#test2()



###ARCHIVED CODE BELOW

"""def wrangle(data_df, spark, tokenizer, name):
    bc_tokenizer = spark.sparkContext.broadcast(tokenizer)
    def labeller(words, unique_prods, unique_subs, organism):
        if words is None:
            return None
        labelling_tensor = []
        unique_prods = [x for x in unique_prods.replace(" + ", " ").replace("?", "").split()]
        unique_subs = [x for x in unique_subs.replace(" + ", " ").replace("?", "").split()]
        organism = [x for x in organism.split(" ")]
        for word in words.split():
            if word in unique_prods:
                labelling_tensor.append(1)
            if word in unique_subs:
                labelling_tensor.append(-1)
            if word in organism:
                labelling_tensor.append(2)
            else:
                labelling_tensor.append(0)
        return labelling_tensor
    
    
    def bc_tokenize(seq):
        if seq is None:
            return None
        return bc_tokenizer.value(seq)['input_ids']
    
    bc_tokenize_udf = udf(bc_tokenize, ArrayType(IntegerType())) 
    labeller_udf = udf(labeller, ArrayType(IntegerType()))        
    data_df = data_df.withColumn('Tokenized_Words', bc_tokenize_udf(data_df._c18))
    data_df = data_df.withColumn('Labels', labeller_udf(col("_c18"), col('_c8'), col('_c10'), col('_c4')))
    loc_path = 'D:/NLPScibert/huggingface/AC_NLP_code/Wrangled_data/'
    path = loc_path + 'data' + str(name)
    data_df.write.mode('overwrite').json(path)
    return path
"""   

"""
def test0():
    from transformers import AutoTokenizer
    data_path = "D:/NLPScibert/data/sentence_annotations_elsevier_pmid_split1_brenda_data-004.csv"
    spark = SparkSession.builder.appName('Test1').getOrCreate()
    df = spark.read.csv(data_path)
    model_type = "roberta-base"
    tokenizer = AutoTokenizer.from_pretrained(model_type)
    print("Tokenize single word", tokenizer("hello i am bob"))
    bc_tokenizer = spark.sparkContext.broadcast(tokenizer)
    #print(df.show())
    #DataFrameWriter.csv()
    df = df.select("_c18").distinct()
    rdd = df.rdd
    rdd2 = rdd.flatMap(lambda x: [x._c18.split(), x._c13])
    print(rdd2.collect())
    del rdd
    rdd3 = rdd2.map(lambda x: [x, 0])
    del rdd2
    print(rdd3)
    df = rdd3.toDF(["Words",'DOI', "Labels"])

    def bc_tokenize(seq):
        return bc_tokenizer.value(seq)['input_ids']
    
    
    bc_tokenize_udf = udf(bc_tokenize, ArrayType(IntegerType()))
    df = df.withColumn("bc_tokenized_value", bc_tokenize_udf("Words"))
    try:
        print(df.show())
    except:
        print("Error")
    
    
 
def test1():
    print('Test 1')
    model_path = "allenai/scibert_scivocab_uncased"
    #model_path = "roberta-base"
    model = NLP.NLPTokenClassification(model_path, 2)
    data_path = "D:/NLPScibert/data/sentence_annotations_elsevier_pmid_split1_brenda_data-008.csv"
    spark = SparkSession.builder.appName('Test1').getOrCreate()
    df = spark.read.csv(data_path)
    DOI_array = df.select('_c13').distinct().limit(20).collect()
    DOI = DOI_array[0].__getitem__("_c13")
    print('DOI: ', DOI)
    data = wrangle(df, spark, model.tokenizer, 8)
    print(type(data))
    print(data.show())
"""