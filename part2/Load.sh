#!/bin/bash

sort -V -u ads.txt -o ads.txt # | uniq
sort -V -u terms.txt -o terms.txt # | uniq
sort -V -u prices.txt -o prices.txt # | uniq
sort -V -u pdates.txt -o pdates.txt # | uniq

cat ads.txt | ./break.pl | db_load -T -t hash ad.idx
cat terms.txt | ./break.pl | db_load -c duplicates=1 -T -t btree te.idx
cat prices.txt | ./break.pl | db_load -c duplicates=1 -T -t btree pr.idx
cat pdates.txt | ./break.pl | db_load -c duplicates=1 -T -t btree da.idx
