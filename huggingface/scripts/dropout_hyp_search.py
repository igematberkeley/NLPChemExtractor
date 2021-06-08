import pandas as pd
import os
import numpy as np
import torch
import pandas as pd
import csv
import re
from collections import OrderedDict
import transformers
from transformers import EarlyStoppingCallback



tokenizer = transformers.AutoTokenizer.from_pretrained('allenai/scibert_scivocab_uncased')
# model = transformers.AutoModelForTokenClassification.from_pretrained('allenai/scibert_scivocab_uncased', num_labels=len(label_list))


# Input/Output Args
DATA_DIR: str = "../data/ner_chemprot/"
DATA_FILES: dict = {

    "train": DATA_DIR + 'train.txt', 
    "test": DATA_DIR + 'test.txt', 
    "val": DATA_DIR + 'dev.txt'
}
label_list = ['O',
          'B-enzyme',
          'B-SUBSTRATE',
          'I-SUBSTRATE',
          'B-PRODUCT-OF',
          'I-enzyme',
          'I-PRODUCT-OF'
         ]
         
def txt2primary(fname) -> OrderedDict:
    # initialize primary data dict
    primary_data = OrderedDict()
    primary_data['id'] =  []
    primary_data['tokens'] = []
    primary_data['ner_tags'] = []
    
#     fname = DATA_DIR + fi #'head.txt' # to test with 2 sentences only.
    
    sentence_id = 0
    with open(fname, "r") as f:
        rd = csv.reader(f, delimiter='\t')
        
        is_blank_after_docstart = False
        tmp_words = []
        tmp_ners = []
        for row in rd:
            if is_blank_after_docstart:
                is_blank_after_docstart = False
                continue
            elif not row:
                continue
            elif re.findall('DOCSTART', row[0]):
                is_blank_after_docstart = True
                continue
            elif row[0] == '.' and row[1] == '.': # currently doesn't include periods.

                primary_data['id'].extend([sentence_id])
                primary_data['tokens'].extend([tmp_words])
                primary_data['ner_tags'].extend([tmp_ners])

                
                sentence_id += 1
                tmp_words = []
                tmp_ners = []
                continue
                
            tmp_words += [row[0]]
            tmp_ners += [label_list.index(row[3])]
    return primary_data
    
    
    
train_primary: OrderedDict = txt2primary(DATA_FILES['train'])
val_primary: OrderedDict = txt2primary(DATA_FILES['val'])
test_primary: OrderedDict = txt2primary(DATA_FILES['test'])

def get_entry(i, primary_data: OrderedDict) -> dict:
    out = {
        'id': primary_data['id'][i],
        'tokens': primary_data['tokens'][i],
        'ner_tags': primary_data['ner_tags'][i]
    }
    return out

example = get_entry(0, train_primary)
example

tokenized_input = tokenizer(example["tokens"], is_split_into_words=True)
tokens = tokenizer.convert_ids_to_tokens(tokenized_input["input_ids"])
print(tokens, flush=True)

word_ids = tokenized_input.word_ids()
aligned_labels = [-100 if i is None else example[f"ner_tags"][i] for i in word_ids]
print(len(aligned_labels), len(tokenized_input["input_ids"]), flush=True)

label_all_tokens = True
def tokenize_and_align_labels(primary) -> transformers.tokenization_utils_base.BatchEncoding: # basically dict
    tokenized_inputs = tokenizer(primary["tokens"], 
                                 padding=True, 
                                 truncation=True, 
                                 is_split_into_words=True,
                                 return_token_type_ids=False
                                )

    labels = []
    for i, label in enumerate(primary[f"ner_tags"]):
        word_ids = tokenized_inputs.word_ids(batch_index=i)
        previous_word_idx = None
        label_ids = []
        for word_idx in word_ids:
            # Special tokens have a word id that is None. We set the label to -100 so they are automatically
            # ignored in the loss function.
            if word_idx is None:
                label_ids.append(-100)
            # We set the label for the first token of each word.
            elif word_idx != previous_word_idx:
                label_ids.append(label[word_idx])
            # For the other tokens in a word, we set the label to either the current label or -100, depending on
            # the label_all_tokens flag.
            else:
                label_ids.append(label[word_idx] if label_all_tokens else -100)
            previous_word_idx = word_idx

        labels.append(label_ids)
        

    tokenized_inputs["labels"] = labels
    return tokenized_inputs
    
# create tokenized inputs
train_tokenized = tokenize_and_align_labels(train_primary)
test_tokenized = tokenize_and_align_labels(test_primary)
val_tokenized = tokenize_and_align_labels(val_primary)

class Dataset(torch.utils.data.Dataset):
    def __init__(self, encodings):
        self.encodings = encodings
        self.labels = encodings['labels']

    def __getitem__(self, idx):
        item = {key: torch.tensor(val[idx]) for key, val in self.encodings.items()}
        item['labels'] = list(self.labels[idx])
        return item

    def __len__(self):
        return len(self.labels)
        
        
train_data = Dataset(train_tokenized)
test_data = Dataset(test_tokenized)
val_data = Dataset(val_tokenized)

import datasets
metric = datasets.load_metric("seqeval")
labels = [label_list[i] for i in example[f"ner_tags"]]
metric.compute(predictions=[labels], references=[labels])

def compute_metrics(p) -> dict:
    predictions, labels = p
    predictions = np.argmax(predictions, axis=2)

    # Remove ignored index (special tokens)
    true_predictions = [
        [label_list[p] for (p, l) in zip(prediction, label) if l != -100]
        for prediction, label in zip(predictions, labels)
    ]
    true_labels = [
        [label_list[l] for (p, l) in zip(prediction, label) if l != -100]
        for prediction, label in zip(predictions, labels)
    ]

    results = metric.compute(predictions=true_predictions, references=true_labels)
    
    return results
    
    
# Model_init for hyperparameter search 
# ref: https://huggingface.co/blog/ray-tune

                                                                      
#for learning_rate in [1e-7, 1e-6, 1e-5, 1e-4, 1e-3, 1e-2]:
#for per_device_train_batch_size in [1, 4, 8, 16, 32, 64]:
#for weight_decay in [0.01, 0.04, 0.07, 0.10]:
for hidden_dropout_prob in [0.05, 0.11, 0.17, 0.23, 0.30]:

    def model_init():
        return transformers.AutoModelForTokenClassification.from_pretrained('allenai/scibert_scivocab_uncased', 
                                                            num_labels=len(label_list),
                                                            hidden_dropout_prob = hidden_dropout_prob)

    training_args = transformers.TrainingArguments(
        f"test-ner",
        evaluation_strategy = "epoch",
        learning_rate=2e-5,
        per_device_train_batch_size=1,
        per_device_eval_batch_size=1,
        num_train_epochs=15,
        weight_decay=0.01,
        load_best_model_at_end=True,
    )

    trainer = transformers.Trainer(
        args=training_args,                  # training arguments, defined above
        train_dataset=train_data,         # training dataset
        tokenizer=tokenizer,
        data_collator=transformers.DataCollatorForTokenClassification(tokenizer),
        eval_dataset=val_data,             # evaluation dataset
        compute_metrics=compute_metrics,
        model_init=model_init,
        callbacks = [EarlyStoppingCallback(early_stopping_patience = 2),
                    transformers.ProgressCallback]
    )

    print(f"trying hidden dropout of {hidden_dropout_prob}", flush=True)
    #print(f"""Trying: learning_rate = {learning_rate},
    ##    \n\t per_device_train_batch_size = {per_device_train_batch_size},
    #    \n\t hidden_dropout_prob = {hidden_dropout_prob},
     #   \n\t weight_decay = {weight_decay}""", flush=True)

    trainer.train()

    predictions = trainer.predict(test_data)
    print(compute_metrics(predictions[0:2]), flush=True)


"""ignore this ksdlfsk

def my_hp_space_ray(trial):
    from ray import tune

    return {
        "learning_rate": tune.loguniform(1e-7, 1e-2),
        # "num_train_epochs": tune.choice(range(1, 6)),
        "seed": tune.choice(range(1,10)),
        "per_device_train_batch_size": tune.choice([1, 4, 8, 16, 32, 64]),
        
        "hidden_dropout_prob": tune.choice([0.05, 0.11, 0.17, 0.23, 0.30]),
        "weight_decay": tune.choice([0.01, 0.04, 0.07, 0.10])
    }
        


best_trial = trainer.hyperparameter_search(direction="maximize",                                                hp_space=my_hp_space_ray,
                              n_trials=1# number of parallel jobs, if multiple GPUs
                             )"""
print(best_trial)
predictions = trainer.predict(test_data)
print(compute_metrics(predictions[0:2]), flush=True)