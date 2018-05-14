# Workbench for Learning Similar Architectural Design Decisions(Service only)
Service for learning and retrieving similar Architectural Design Decisions

## Runtime dependencies:
JDK - `1.8.0`
MongoDB - `3.6.2`

## Core Plugin dependencies (check build.sbt)
Play - `2.5.9`
Spark (core, sql, mllib) - `2.0.1`
Morphia - `1.2.1`
Angular - `1.5.8`
Bootstrap - `3.3.6`

##### Experimental
* DL4J Version: `0.9.1`
* RapidMiner: Snapshot version to be build locally

## Deploying the project
Ensure mongodb is running on default port `27017`; else configure settings in Global.java
From the command prompt execute
> sbt run

## Running Applications
1. Frontend Application will be available at `localhost:9000`
1. Spark Web UI is available at `localhost:9090`

## Docker support
1. Change the database configurations according to the instructions in `application.conf`
1. Start the complete application stack using `docker-compose up`

Notice: the current docker uses a mongoDB image without any data. This will be dealt with in the future work.

## How to use DocClustering
To find out how to use DocClustering, please checkout out the <a href="https://github.com/sebischair/DocClusteringWorkbench/wiki" target="_blank">DocClustering wiki</a>.
