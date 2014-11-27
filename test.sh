#!/usr/bin/env bash

# ------------------------------------------------------------------------------
# Test the FireWorks factory
# ------------------------------------------------------------------------------

# -- Variables -----------------------------------------------------------------

first_worker_id=1001
last_worker_id=1010
first_tester_id=2001
last_tester_id=2010
first_logistic_id=3001
last_logistic_id=3006

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

# Start testers
for (( tester = first_tester_id; tester <= last_tester_id; tester++ )); do
    echo "Start tester $tester"
    mvn exec:java -PTester -Dtester.id=$tester&
done

# Start logistic workers
for (( worker = first_logistic_id; worker <= last_logistic_id; worker++ )); do
    echo "Start logistic worker $worker"
    mvn exec:java -PLogistic -Dlogistic.id=$worker&
done
