#!/bin/bash
ps aux | grep com.webank.demo |  awk '{print $2}' | xargs kill -9