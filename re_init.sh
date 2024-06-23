#!/bin/bash
rm -r 03results/*
rm -r sootOutput/*
cp -r 02Benchmarks/* ./sootOutput/
rm main.pid
