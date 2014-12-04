# ------------------------------------------------------------------------------
# Test the FireWorks factory
# ------------------------------------------------------------------------------

# -- Variables -----------------------------------------------------------------

WORKER_IDS = 1001 1002 1003 1004 1005 1006 1007 1008 1009 1010
TESTER_IDS = 2001 2002 2003 2004 2005 2006 2007 2008 2009 2010
LOGISTIC_IDS = 3001 3002 3003 3004 3005 3006

# -- Rules --------------------------------------------------------------------

run: test

compile:
	# Compile sources
	mvn compile

test: compile
	# Start factory
	mvn exec:java -PFireWorks&

	# Give factory some time to initialize
	sleep 10

	# Start workers
	$(foreach worker, $(WORKER_IDS), \
		echo Start worker $(worker); \
		(mvn exec:java -PWorker -Dworker.id=$(worker) &);)

	# Start testers
	$(foreach tester, $(TESTER_IDS), \
		echo Start tester $(tester); \
		(mvn exec:java -PTester -Dtester.id=$(tester) &);)

	# Start logistic workers
	$(foreach worker, $(LOGISTIC_IDS), \
		echo Start logistic worker $(worker); \
		(mvn exec:java -PLogistic -Dlogistic.id=$(worker) &);)

clean:
	mvn clean
