#!/usr/bin/env python
# -*- coding: utf-8 -*
import sys
import time
import datetime

def read_from_file():
	with open('aout.txt', 'r') as f:
		line = f.readline()
		dic = {}
		while line:
			t, count = line.split('\t', 1)
			if t in dic.keys():
				dic[t] += int(count)
			else:
				dic[t] = int(count)
			line = f.readline()
		
		for k in dic:
			print(k+'\t'+str(dic[k]))

def read_from_stdin():
	dic = {}
	for line in sys.stdin:
		t, count = line.split('\t', 1)
		if t in dic.keys():
			dic[t] += int(count)
		else:
			dic[t] = int(count)
	
	for k in dic:
		print(k+'\t'+str(dic[k]))

read_from_stdin()	
#read_from_file()	