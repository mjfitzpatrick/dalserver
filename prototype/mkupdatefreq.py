#! /usr/bin/env python
#
import sys, os, re, pdb

update = """UPDATE %(table)s SET 
       spectralstart=%(lolimit)s,
       spectrallocation=%(center)s,
       spectralstop=%(hilimit)s,
       spectralresolution=%(res)s
       WHERE archiveid='%(path)s';"""
data = { 'table': 'siav2model' }

collu = {}
cols = []
line = ''
csvfile = sys.argv.pop(1)
with open(csvfile) as csv:
    while 'lolimit' not in cols:
        line = csv.readline()
        if not line: break
        line = line.strip()
        cols = line.split(',')
    
    for i in xrange(len(cols)):
        collu[cols[i]] = i

    for line in csv:
        cols = line.strip().split(',')
        for name in "lolimit center hilimit path res".split():
            data[name] = cols[collu[name]]

        print update % data

