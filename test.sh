#!/usr/bin/env bash

# ------------------------------------------------------------------------------
# Test the FireWorks factory
# ------------------------------------------------------------------------------

# -- Variables -----------------------------------------------------------------

first_worker_id=10
last_worker_id=20

# Compile sources
mvn compile

# Start factory
mvn exec:java -PFireWorks&

# Give factory some time to initialize
sleep 10

# Start workers
for (( worker = first_worker_id; worker <= last_worker_id; worker++ )); do
    echo "Start worker $worker"
    mvn exec:java -PWorker -Dworker.id=$worker&
done
