# Scotland-Yard

This is the coursework that I have done in my Year One Group Project in Object-Oriented Programming(OOP) in **University of Bristol** in **2023-2024**. <br />
This coursework is done by **Gordon Wai Hin Kam** and **Leung Ka Ho (Ivan)**. <br />
The skeleton code is provided by **University of Bristol Computer Science Department** <br />
ScotlandYard is written in **Java** <br />
To see our thoughts after implement the functionality of this game please go to [report.pdf](report.pdf)

## How do I run this game 

To Run this game please install [Apache Maven](https://maven.apache.org/), since this project use Maven as build system 

There are two versions of this game

1. To run cw-model [Main.java](cw-model/src/main/java/uk/ac/bris/cs/scotlandyard/Main.java), either go on [ScotlandYard/cw-model](cw-model) (where the [pom.xml](cw-model/pom.xml) file is located) and type `./mvnw clean compile ` on CLI **OR** Open [cw-model/src/main/java/uk/ac/bris/cs/scotlandyard/Main.java](cw-model/src/main/java/uk/ac/bris/cs/scotlandyard/Main.java) in any IDE and press RUN on Main class

2. To run cw-ai [Main.java](cw-ai/src/main/java/uk/ac/bris/cs/scotlandyard/ui/ai/Main.java), either go on [ScotlandYard/cw-ai](cw-ai) (where the [pom.xml](cw-ai/pom.xml) file is located) and type `./mvnw clean compile` on CLI **OR** Open [cw-ai/src/main/java/uk/ac/bris/cs/scotlandyard/ui/ai/Main.java](cw-ai/src/main/java/uk/ac/bris/cs/scotlandyard/ui/ai/Main.java) in any IDE and press RUN on Main class

## Description of Folder Structure

cw-model is the compulsory part of our coursework.
In [ScotlandYard/cw-model/src/main/scotlandyard/model](cw-model/src/main/scotlandyard/model)
there are classes, we have to implement MyGameStateFactory and MyModelFactory class, we have used Principle of OOP that we have learnt in lectures

cw-ai is the open-end part of our coursework
In [ScotlandYard/cw-ai/src/main/java/uk/ac/bris/cs/scotlandyard/ui/ai](cw-ai/src/main/java/uk/ac/bris/cs/scotlandyard/ui/ai)
We have to design some algorithms to simulate AI to play the game and also need to enhance the performance, we have implemented [Alpha-beta prunning](https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning) and [Game Tree](https://en.wikipedia.org/wiki/Game_tree) these decision making algorithms in order to imitate an actual AI control pieces in the game

For more details, please visit our [report](report.pdf)