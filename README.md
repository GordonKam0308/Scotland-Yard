# Scotland-Yard

## Prerequisite
Please install the following environment / library:

- [Java](https://www.java.com/download/ie_manual.jsp)
- [Maven](https://maven.apache.org/)

## Introduction

This game is contributed by [**Gordon Wai Hin Kam**](https://github.com/li23179) and [**Ivan (Leung Ka Ho)**](https://github.com/nm22031)

Scotland Yard is a board game in which a team of players controlling different detectives cooperate to track down a player controlling a criminal as they move around a board representing the streets of London. 

It was first published in 1983 and is named after Scotland Yard which is the headquarters of London's Metropolitan Police Service in real-life.

## Setup

There are **two** roles in this game *detectives* and *Mr.X*. One player is assigned to *Mr.X* and the rest of it are *detectives*.

### Components
- **GameBoard**: A map of London featuring 199 stations connected by colored lines (taxi, bus, and underground).
- **Tickets**: Used for movement; there are taxi tickets, bus tickets, underground tickets, and special secret tickets for Mr. X.
- **Log Book**: Mr. X uses this to secretly record his moves.

### Movement Rules

- **Mr. X’s Moves**: Mr. X writes down his moves in the log book and shows only the type of ticket he uses (keeping his exact location secret). Every **3rd**, **8th**, **13th**, and **18th** move, Mr. X must reveal his exact location.

- **Detective Moves**: Detectives announce their moves aloud and give up a ticket of the corresponding type each time they move. They cannot move if they run out of applicable tickets.

## How do I run this game

### cw-model

This is a default model of Scotland Yard. You can enter number of players and detective to play the game locally.

To run the game, run the following command on CLI:
```
cd cw-model/src/main/java/uk/ac/bris/cs/scotlandyard
./mvnw clean compile
```

#### Test for cw-model

To run the all tests, run the following command on CLI:
```
./mvnw clean test
```

### cw-ai

This advanced model of Scotland Yard allows you to specify the number of AIs that participate as players in the game.

To run the game, run the following command on CLI:
```
cd cw-ai/src/main/java/uk/ac/bris/cs/scotlandyard/ui/ai
./mvnw clean compile
```

#### Test for cw-ai

To run the all tests, run the following command on CLI:
```
./mvnw clean test
```
### Implementation
Please visit our [Report](/Scotland-Yard/report.pdf).