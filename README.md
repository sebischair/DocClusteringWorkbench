# Workbench for Learning Similar Architectural Design Decisions(Service only)
DocClustering is a service for learning and retrieving similar architectural design decisions.

Check out the <a href="https://documenter.getpostman.com/view/4318985/docclustering/RW86LA17" target="_blank">API documentation</a>.

[![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/8a91c26ea8fdde0af599)

## Runtime dependencies
* JDK - `1.8.0`
* MongoDB - `3.6.2`

## Core Plugin dependencies (check build.sbt)
* Play - `2.5.9`
* Spark (core, sql, mllib) - `2.0.1`
* Morphia - `1.2.1`
* Angular - `1.5.8`
* Bootstrap - `3.3.6`

##### Experimental
* DL4J Version: `0.9.1`
* RapidMiner: Snapshot version to be build locally

## Configuration
1. Rename application.local.conf.back to application.local.conf
1. Fill in database credentials
1. `morphia.db.name` is the database that stores DocClustering related information
1. `morphia.amelie.db.name` is the database that sotres projects, issues, etc.

## Run the project
* From the command prompt execute `sbt run` or `activator run`
* Frontend Application is available at `localhost:9000`
* Spark Web UI is available at `localhost:9090`

## Docker support
1. Change the database configurations according to the instructions in `application.conf`
1. Start the complete application stack using `docker-compose up`

## How to use DocClustering
To find out how to use DocClustering, please checkout out the <a href="https://github.com/sebischair/DocClusteringWorkbench/wiki" target="_blank">DocClustering wiki</a>.
