#!/usr/bin/env python
# -*- coding: utf-8 -*
import sys
import time
import datetime

def read_from_file():
	with open('alog.txt', 'r') as f:
		line = f.readline()
		while line:
			t = line.split('- - [')[1].split(' -')[0]
			time = datetime.datetime.strptime(t, "%d/%b/%Y:%H:%M:%S")
			print(time.strftime('%Y-%m-%d T %H:00:00.000')+"\t1") # %b: Month as locale’s abbreviated name.
			line = f.readline()

def read_from_stdin():
	for line in sys.stdin:
		t = line.split('- - [')[1].split(' -')[0]
		time = datetime.datetime.strptime(t, "%d/%b/%Y:%H:%M:%S")
		print(time.strftime('%Y-%m-%d T %H:00:00.000')+"\t1") # %b: Month as locale’s abbreviated name.

read_from_stdin()
