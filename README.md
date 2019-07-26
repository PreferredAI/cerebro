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

Download and extract the data file [here](https://drive.google.com/open?id=18b8qw5f1X7wEPvC8KLxrCJQ3NxWxQ7yS). 

Look into the extracted folder. It includes: 
+ File containing hashing vectors: splitVec.o 
+ File containing query vectors and the ids of their associated true top 20 vec: query_top20_10M.o
+ A folder "imdb_data" contain txt files. Each file has the following format:
    - First line is text information.
    - Second line is the associated latent vector.

Open your CLI and navigate to your build folder.

Note: in the following example assumes that the file is downloaded and extract in the directory E:\

#### Build index
```ssh
\..\cerebro\target>java -jar cerebro-1.0-jar-with-dependencies.jar -op build -idx E:\index -data E:\data\imdb_data -hsh E:\data\splitVec.o


Building index, plz wait

Build index for text successfully


\..\cerebro\target>_
```
#### Text search on an index
After building your index, you may want to check if it is functioning.

```ssh
\..\cerebro\target>java -jar cerebro-1.0-jar-with-dependencies.jar -op sText -idx E:\index -q horseman

File: E:\data\imdb_data\8694_0.txt; DocID:48547
File: E:\data\imdb_data\48661_0.txt; DocID:42955
File: E:\data\imdb_data\8688_0.txt; DocID:48540
File: E:\data\imdb_data\26062_0.txt; DocID:17846
File: E:\data\imdb_data\5144_0.txt; DocID:44604
File: E:\data\imdb_data\38439_0.txt; DocID:31597
File: E:\data\imdb_data\39343_0.txt; DocID:32602
File: E:\data\imdb_data\20746_0.txt; DocID:11939
File: E:\data\imdb_data\47139_0.txt; DocID:41264
File: E:\data\imdb_data\19349_0.txt; DocID:10386
//files containing keywords you entered

\..\cerebro\target>_
```
 
#### Vector search on an index
```ssh
\..\cerebro\target>java -jar cerebro-1.0-jar-with-dependencies.jar -op sVec -idx E:\index -hsh E:\data\splitVec.o -qV E:\data\query_top20_1M.o
Top-20 query time: 2 ms
Overlapp between brute and and hash (over top 20) is : 1

Top-20 query time: 1 ms
Overlapp between brute and and hash (over top 20) is : 0

Top-20 query time: 2 ms
Overlapp between brute and and hash (over top 20) is : 1
.....
```
 

