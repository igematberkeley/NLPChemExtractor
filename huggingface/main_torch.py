#!/usr/bin/env python
# coding: utf-8

# # HuggingFace Transformers on SciBERT (NER)

# In[1]:


import transformers
import pandas as pd
import os
import numpy as np
import torch
import pandas as pd
import csv
import re
from collections import OrderedDict


# In[2]:


tokenizer = transformers.AutoTokenizer.from_pretrained('allenai/scibert_scivocab_uncased')
# model = transformers.AutoModelForTokenClassification.from_pretrained('allenai/scibert_scivocab_uncased', num_labels=len(label_list))


# # 1. Data Pre-Processing

# An overview of the data pre-processing pipeline:
# 
# 1. txt files (train, test, dev)
# 
# 2. primary data (X_primary) =  dict of (id: list), (token: list), and (ner_tag: list) for every sentence; grouped by key (not sentence).
# 
# 3. tokenized data (X_tokenized) = 'dict' of token_id, label, and attention_mask; grouped by key.
# 
# 4. Dataset objects (X_data) = a 'list' of token_id, label, and attention mask; grouped by sentence!

# In[3]:


# Input/Output Args
DATA_DIR: str = "./data/ner_chemprot/"
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


# ## 1.1-2 Loading the chemprot data from SciBERT into Primary Data
# 

# In[4]:


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


# In[5]:


train_primary: OrderedDict = txt2primary(DATA_FILES['train'])
val_primary: OrderedDict = txt2primary(DATA_FILES['val'])
test_primary: OrderedDict = txt2primary(DATA_FILES['test'])


# In[6]:


def get_entry(i, primary_data: OrderedDict) -> dict:
    out = {
        'id': primary_data['id'][i],
        'tokens': primary_data['tokens'][i],
        'ner_tags': primary_data['ner_tags'][i]
    }
    return out

example = get_entry(0, train_primary)
example


# ## 1.2-3 Tokenize the Primary Data

# We then tokenize the primary data to get their encodings, and create a Dataset object.

# ### Verifying the tokenizer, based on the reference notebook

# In[7]:


tokenized_input = tokenizer(example["tokens"], is_split_into_words=True)
tokens = tokenizer.convert_ids_to_tokens(tokenized_input["input_ids"])
print(tokens)


# In[8]:


len(example[f"ner_tags"]), len(tokenized_input["input_ids"])


# In[9]:


word_ids = tokenized_input.word_ids()
aligned_labels = [-100 if i is None else example[f"ner_tags"][i] for i in word_ids]
print(len(aligned_labels), len(tokenized_input["input_ids"]))


# ### Define and apply the tokenization function.

# In[10]:


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


# In[11]:


# create tokenized inputs
train_tokenized = tokenize_and_align_labels(train_primary)
test_tokenized = tokenize_and_align_labels(test_primary)
val_tokenized = tokenize_and_align_labels(val_primary)


# ## 1.3-4 Create Dataset Objects

# In[12]:


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


# In[13]:


train_data = Dataset(train_tokenized)
test_data = Dataset(test_tokenized)
val_data = Dataset(val_tokenized)


# # 2. Fine Tuning + Training the Model

# Overview of fine tuning and training:
# 1. Metrics function
# 2. Training
# 3. Testing

# ## 2.1 Metrics Function

# In[14]:


import datasets
metric = datasets.load_metric("seqeval")
labels = [label_list[i] for i in example[f"ner_tags"]]
metric.compute(predictions=[labels], references=[labels])


# In[15]:


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


# ## 2.2 Training

# Defining necessary functions as args.

# In[16]:
1

# Model_init for hyperparameter search 
# ref: https://huggingface.co/blog/ray-tune
def model_init():
    return transformers.AutoModelForTokenClassification.from_pretrained('allenai/scibert_scivocab_uncased', num_labels=len(label_list))


# In[ ]:


training_args = transformers.TrainingArguments(
    f"test-ner",
    evaluation_strategy = "epoch",
    learning_rate=2e-5,
    per_device_train_batch_size=1,
    per_device_eval_batch_size=1,
    num_train_epochs=3,
    weight_decay=0.01,
)

trainer = transformers.Trainer(
    args=training_args,                  # training arguments, defined above
    train_dataset=train_data,         # training dataset
    tokenizer=tokenizer,
    data_collator=transformers.DataCollatorForTokenClassification(tokenizer),
    eval_dataset=val_data,             # evaluation dataset
    compute_metrics=compute_metrics,
    model_init=model_init,
)

trainer.train()


# ### 2.3 Testing

# Log your results here: https://docs.google.com/spreadsheets/d/1jolvSI9tCqHZqBMtX1MAUjht2WuXyl_uFauhbvHMUtQ/edit?usp=sharing

# In[ ]:


predictions = trainer.predict(test_data)


# In[ ]:


compute_metrics(predictions[0:2])


# ### 2.4 Hyperparameter search??

# In[ ]:


#trainer.hyperparameter_search(direction="maximize")


# In[ ]:





# In[ ]:





# In[ ]:





# In[ ]:





# In[ ]:


# from torch.utils.data import DataLoader

# device = torch.device('cuda') if torch.cuda.is_available() else torch.device('cpu')
# print(device)

# model.train()

# torch.manual_seed(10)
# BATCH_SIZE = 64
# train_loader = DataLoader(train_data, batch_size=BATCH_SIZE, shuffle=False, drop_last=True)

# optim = transformers.AdamW(model.parameters(), lr=5e-5)

# for epoch in range(3):
#     for i, batch in enumerate(train_loader):
#         print(f'Doing epoch {epoch}, entries {i*BATCH_SIZE} to {(i+1)*BATCH_SIZE} out of {len(train_loader)}')
#         optim.zero_grad()
#         input_ids = batch['input_ids'].to(device)
#         attention_mask = batch['attention_mask'].to(device)
#         labels = batch['labels'].to(device)
#         outputs = model(input_ids, attention_mask=attention_mask, labels=labels)
#         loss = outputs[0]
#         loss.backward()
#         optim.step()

# model.eval()


# In[ ]:





# In[ ]:





# In[ ]:





# In[ ]:





# In[ ]:





# In[ ]:





# In[ ]:





# In[ ]:





# In[ ]:





# In[ ]:





# In[ ]:





# In[ ]:





# In[ ]:





# In[ ]:





# In[ ]:





# 
