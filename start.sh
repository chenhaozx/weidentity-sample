#!/bin/bash
if [ -f "dist/logs/all.log" ];then
  rm -rf dist/logs/*
fi
 touch dist/logs/all.log
nohup java -cp dist/app/*:dist/conf/:dist/lib/* com.webank.demo.server.SampleApp &
tail -f dist/logs/all.log