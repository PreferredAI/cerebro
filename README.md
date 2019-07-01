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
\..\cerebro\target>java -jar cerebro-1.0-jar-with-dependencies.jar -op 1
Plz enter the path to the folder where you want to put the index:

E:\Index    //Enter the path to the folder where you want to build index
Plz enter the path to the folder where you put the data to be indexed:

E:\imdb_data    //Enter the path to the folder where you extract you text files

(Waiting...)

Build index for text successfully


\..\cerebro\target>_
```
#### Text search on an index
After building your index, you may want to check if it is functioning.

```ssh
\..\cerebro\target>java -jar cerebro-1.0-jar-with-dependencies.jar -op 3
Plz enter the path to the folder where you put the index files:

E:\Index    //Enter the path to the folder that you specified where to build index earlier
Plz enter your query:

Lord of the Ring    //Enter whatever keyword you want to search
File: E:\imdb_data\31327_0.txt; DocID:23696
File: E:\imdb_data\31326_0.txt; DocID:23695
File: E:\imdb_data\31315_0.txt; DocID:23683
File: E:\imdb_data\21433_0.txt; DocID:12703
File: E:\imdb_data\29690_0.txt; DocID:21876
File: E:\imdb_data\31323_0.txt; DocID:23692
File: E:\imdb_data\34899_0.txt; DocID:27663
File: E:\imdb_data\31317_0.txt; DocID:23685
File: E:\imdb_data\33447_0.txt; DocID:26051
File: E:\imdb_data\31972_0.txt; DocID:24412
File: E:\imdb_data\42686_0.txt; DocID:36316
File: E:\imdb_data\28731_0.txt; DocID:20811
File: E:\imdb_data\200_0.txt; DocID:11222
File: E:\imdb_data\21424_0.txt; DocID:12693
File: E:\imdb_data\216_0.txt; DocID:12999
File: E:\imdb_data\21439_0.txt; DocID:12709
File: E:\imdb_data\21428_0.txt; DocID:12697
File: E:\imdb_data\28207_0.txt; DocID:20229
File: E:\imdb_data\206_0.txt; DocID:11888
File: E:\imdb_data\32157_0.txt; DocID:24618
//files containing keywords you entered

\..\cerebro\target>_
```
 
 #### Build index for vector
 Cerebro supports index-building for both text and latent vector.
 It uses the ANNS(approximate nearest neighbor search) approach LSH(Locality Sensitive Hashing) to build index 
 for vector.
 
 Note that although in this example we are building indexes for text and vectors into two seperate folders, but a cerebro 
 index folder can incorporate both text and vectors. Try using the same directory for for both text and vector to see for 
 yourself.
 
 ```ssh
\..\cerebro\target>java -jar cerebro-1.0-jar-with-dependencies.jar -op 2
Plz enter the path to the folder where you want to put the index:

E:\index_Vec    //Enter the path to the folder where you want to build index
Plz enter the path to the file object containing the data to be indexed:

E:\VecData\itemVec_10M.o    //Note, this time a file objects, not a folder
Plz enter the path to the folder where you put the file object that contains hashing vectors:

E:\VecData\splitVec.o   //Path to the file containing hashing vectors splitVec.o

(Waiting...)

Build index for vector successfully


\..\cerebro\target>_
 ``` 
 
#### Vector search on an index
```ssh
\..\cerebro\target>java -jar cerebro-1.0-jar-with-dependencies.jar -op 4
Plz enter the path to the folder where you put the index files:

E:\index_Vec    \\The folder you just entered earlier to build vec index
Plz enter the path to the folder where you put the file object that contains hashing vectors:

E:\VecData\splitVec.o   \\Directory to the file containing query vectors
Plz enter the path to the query vectors file object:

E:\VecData\query_top20_10M.o
Top-20 query time: 173 ms
Overlapp between brute and and hash (over top 20) is : 0

Top-20 query time: 135 ms
Overlapp between brute and and hash (over top 20) is : 0

Top-20 query time: 101 ms
Overlapp between brute and and hash (over top 20) is : 0
.....
```
 

