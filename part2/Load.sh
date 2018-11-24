#!/bin/bash

ad = ads.txt
term = terms.txt
price = prices.txt
date = pdates.txt

sort -u ad -o ad
sort -u term -o term
sort -u price -o price
sort -u date -o date

db_load -T -f ad -t hash ad.idx
db_load -T -f term -t btree te.idx
db_load -T -f price -t btree pr.idx
db_load -T -f date -t btree da.idx
