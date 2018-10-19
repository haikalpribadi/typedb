#!/usr/bin/env bash

WRITE_TO_OUTS=0
INS=()
OUTS=()
array=()
commandline_args=("$@")

for var in "$@"; do
    if [[ $var = "--outs" ]]; then
    	WRITE_TO_OUTS=1
    elif [[ WRITE_TO_OUTS -eq 0 ]]; then
    	INS+=($var)
    elif [[ WRITE_TO_OUTS -eq 1 ]]; then
    	OUTS+=($var)
    fi
done


for outfile in "${OUTS[@]}"; do
	for infile in "${INS[@]}"; do
		if [[ "$(basename $infile)" = "$(basename $outfile)" ]]; then
			cat $infile >> $outfile
		fi
	done
done
