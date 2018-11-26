#!/bin/bash

sort  -u ../part1/ads.txt -o ads.txt # | uniq
sort  -u ../part1/terms.txt -o terms.txt # | uniq
sort  -u ../part1/prices.txt -o prices.txt # | uniq
sort  -u ../part1/pdates.txt -o pdates.txt # | uniq

cat ads.txt | ./break.pl | db_load -T -t hash ad.idx
cat terms.txt | ./break.pl | db_load -c duplicates=1 -T -t btree te.idx
cat prices.txt | ./break.pl | db_load -c duplicates=1 -T -t btree pr.idx
cat pdates.txt | ./break.pl | db_load -c duplicates=1 -T -t btree da.idx

cp ad.idx ../part3
cp te.idx ../part3
cp pr.idx ../part3
cp da.idx ../part3
