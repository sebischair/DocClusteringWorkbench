# Workbench for Learning Similar Architectural Design Decisions(Service only)
Service for learning and retrieving similar Architectural Design Decisions

APIs for training and testing cluster models

<a href="https://documenter.getpostman.com/view/693941/collection/RW1aL1Dz" target="_blank">Documentation</a>

[![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/97c1ddbf0f1449724e91)

## Runtime dependencies:
JDK - `1.8.0`
MongoDB - `3.6.2`

## Core Plugin dependencies (check build.sbt)
Play - `2.5.9`
Spark (core, sql, mllib) - `2.0.1`
Morphia - `1.2.1`
Angular - `1.5.8`
Bootstrap - `3.3.6`

###### Experimental
> 1. DL4J Version: `0.9.1`
> 2. RapidMiner: Snapshot version to be build locally

## Deploying the project
Ensure mongodb is running on default port `27017`; else configure settings in Global.java
From the command prompt execute
> sbt run

### Running Applications
>1. Frontend Application will be available at `localhost:9000`
>2. Spark Web UI is available at `localhost:9090`

## Docker support
1. Change the database configurations according to the instructions in `application.conf`
1. Start the complete application stack using `docker-compose up`

#### Example Training Pipelines
##### Spark KMeans
>Word2Vec: `localhost:9000/spark/kmeans/example1`
>HashingTF: `localhost:9000/spark/kmeans/example2`

#### DL4J KMeans
>Word2Vec: `localhost:9000/dl4j/kmeans/example1`

### Results
Results are stored in "myresources" directory. two type of results are generated currently
>1. Labeled Data in json format, Stored in /myresources/results/<pipeline-name>
>2. Fitted Model from the pipeline, Stored in /myresources/models/<pipeline-name>
