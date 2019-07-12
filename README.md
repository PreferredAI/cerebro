# Cerebro
Your preferred open source personalized recommendation retrieval engine.


[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ai.preferred/cerebro/badge.svg)](https://maven-badges.herokuapp.com/maven-central/ai.preferred/cerebro)
[![Build Status](https://travis-ci.org/PreferredAI/cerebro.svg)](https://travis-ci.org/PreferredAI/cerebro)
[![Coverage Status](https://coveralls.io/repos/github/PreferredAI/cerebro/badge.svg)](https://coveralls.io/github/PreferredAI/cerebro)
[![Javadoc](https://www.javadoc.io/badge/ai.preferred/cerebro.svg)](https://www.javadoc.io/doc/ai.preferred/cerebro)

## Overview
Our goal is to create a closed-loop engine for recommendations with blazingly fast retrieval of objects.

### Features
- Session Management
- Efficient Retrieval of Personalized Search
- Closed-loop Recommendation Engine

## Getting started

### Add a dependency
If you already have a project then just add Cerebro as a dependency to your pom.xml:
```xml
<dependency>
    <!-- Cerebro: Your personalized retrieval engine @ https://cerebro.preferred.ai/ -->
    <groupId>ai.preferred</groupId>
  	<artifactId>cerebro</artifactId>
  	<version>1.0</version>
</dependency>
```
### Example
Clone the repository to your computer then build jar file with maven. Make sure in your build folder there is a file 
"cerebro-1.0-jar-with-dependencies.jar". The configuration has been tweeted to build standalone jar file.

Download the vector files [here](https://drive.google.com/open?id=1qAouLknsfU6fPlEDo1oD3uX2M9U6V7TN). 
Then the [file](https://drive.google.com/file/d/1KVFtMcmqvYsR0yTzqtfBKVupocNh0fn3/view?usp=sharing) containing text

Extract them into seperate folders.

Look into the extracted folder from the vec file. It includes: 
+ File containing set of 10 million vectors: itemVec_10M.o 
+ File containing hashing vectors: splitVec.o 
+ File containing query vectors and the ids of their associated true top 20 vec: query_top20_10M.o

Open your CLI and navigate to your build folder.

#### Build text index
```ssh
\..\cerebro\target>java -jar cerebro-1.0-jar-with-dependencies.jar -op 1 -idx E:\Index -data E:\imdb_data


Building index, plz wait

Build index for text successfully


\..\cerebro\target>_
```
#### Text search on an index
After building your index, you may want to check if it is functioning.

```ssh
\..\cerebro\target>java -jar cerebro-1.0-jar-with-dependencies.jar -op 3 -idx E:\Index -q War

File: E:\imdb_data\24400_0.txt; DocID:16000
File: E:\imdb_data\33526_0.txt; DocID:26139
File: E:\imdb_data\23356_0.txt; DocID:14839
File: E:\imdb_data\28656_0.txt; DocID:20727
File: E:\imdb_data\38337_0.txt; DocID:31484
File: E:\imdb_data\35539_0.txt; DocID:28375
File: E:\imdb_data\26895_0.txt; DocID:18770
File: E:\imdb_data\3715_0.txt; DocID:30176
File: E:\imdb_data\28653_0.txt; DocID:20724
File: E:\imdb_data\1822_0.txt; DocID:9143
File: E:\imdb_data\26921_0.txt; DocID:18800
File: E:\imdb_data\1820_0.txt; DocID:9121
File: E:\imdb_data\26912_0.txt; DocID:18790
File: E:\imdb_data\35547_0.txt; DocID:28384
File: E:\imdb_data\10079_0.txt; DocID:87
File: E:\imdb_data\24627_0.txt; DocID:16251
File: E:\imdb_data\10514_0.txt; DocID:571
File: E:\imdb_data\10533_0.txt; DocID:592
File: E:\imdb_data\20105_0.txt; DocID:11228
File: E:\imdb_data\11297_0.txt; DocID:1440
//files containing keywords you entered

\..\cerebro\target>_
```
 
 #### Build index for vector
 Cerebro supports index-building for both text and latent vector.
 It uses the ANNS(approximate nearest neighbor search) approach LSH(Locality Sensitive Hashing) to build index 
 for vector.
 
 Note that although in this example we are building indexes for text and vectors into two seperate folders, a cerebro 
 index folder can incorporate both text and vectors. Try using the same directory for for both text and vector to see for 
 yourself.
 
 ```ssh
\..\cerebro\target>java -jar cerebro-1.0-jar-with-dependencies.jar -op 2 -idx E:\index_Vec -data E:\vec_data\itemVec_10M.o -hsh E:\vec_data\splitVec.o

Building index, plz wait

Build index for vector successfully

\..\cerebro\target>_
 ``` 
 
#### Vector search on an index
```ssh
\..\cerebro\target>java -jar cerebro-1.0-jar-with-dependencies.jar -op 4 -idx E:\index_Vec -hsh E:\vec_data\splitVec.o -qV E:\vec_data\query_top20_10M.o
Top-20 query time: 173 ms
Overlapp between brute and and hash (over top 20) is : 0

Top-20 query time: 71 ms
Overlapp between brute and and hash (over top 20) is : 0

Top-20 query time: 23 ms
Overlapp between brute and and hash (over top 20) is : 2
.....
```
 

