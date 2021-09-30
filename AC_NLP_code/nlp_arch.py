import os
import torch
import torch.nn as nn
from torch.utils.data import DataLoader
from tqdm.auto import tqdm
import transformers

class NLPTokenClassification(nn.Module):
    def __init__(self, model_ref, num_labels):
        super(NLPTokenClassification, self).__init__()
        self.model = transformers.AutoModelForTokenClassification.from_pretrained(model_ref)
        self.tokenizer = transformers.AutoTokenizer.from_pretrained(model_ref)
        if torch.cuda.is_available():
            self.device = 'cuda'
        else:
            self.device = 'cpu'
        
    def forward(self, batch):
        input_ids = batch['input_ids'].to(self.device)
        attention_mask = batch['attention_mask'].to(self.device)
        labels = torch.tensor(batch['labels']).to(self.device)
        outputs = self.model(input_ids, attention_mask=attention_mask, labels=labels)
        return outputs
    
    def train(self, data, epochs, BATCH_SIZE, save_dir):
        self.model.train()
        torch.cuda.empty_cache()
        self.model.to(device)
        dataloader = DataLoader(data, batch_size=BATCH_SIZE, shuffle=False, drop_last=True)
        optim = torch.optim.Adam(self.model.parameters(), 1e-5)
        progress_bar = tqdm(range(epochs * len(dataloader)))
        loss_history: list = list()
        for i in range(epochs):
            for j in enumerate(dataloader):
                optim.zero_grad()
                output = self.forward(j, self.device)
                loss = output[0]
                loss_history.append(loss)
                loss.backward()
                optim.step()
                progress_bar.update(1)
        self.model.save_pretrained(save_dir)
        return loss_history
    
class NLPQuestionAnswer(NLPTokenClassification):
    def __init__(self, model_ref):
        self.model = transformers.AutoModelForQuestionAnswering.from_pretrained(model_ref)
        self.tokenizer = transformers.AutoTokenizer.from_pretrained(model_ref)
        if torch.cuda.is_available():
            self.device = 'cuda'
        else:
            self.device = 'cpu'
        
class NLPNextSentencePrediction(NLPTokenClassification):
    def __init__(self, model_ref):
        self.model = transformers.AutoModelForNextSentencePrediction(model_ref)
        self.tokenizer = transformers.AutoTokenizer.from_pretrained(model_ref)
        if torch.cuda.is_available():
            self.device = 'cuda'
        else:
            self.device = 'cpu'