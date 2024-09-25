package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.HashSet;
import java.util.Set;

public final class MyModelFactory implements Factory<Model> {

    @Nonnull
    @Override
    public Model build(GameSetup setup,
                       Player mrX,
                       ImmutableList<Player> detectives) {
        Board.GameState gameState = new MyGameStateFactory().build(setup, mrX, detectives);
        Set<Model.Observer> observers = new HashSet<>();
        Model model = new Model() {
            @Nonnull
            @Override
            public Board getCurrentBoard() {
                return gameState;
            }

            @Override
            public void registerObserver(@Nonnull Observer observer) {
                if (observer.equals(null)) throw new NullPointerException("There is no such observer");
                if (observers.stream().anyMatch(obs -> obs == observer))
                    throw new IllegalArgumentException("This observer is already registered!");
                observers.add(observer);
            }

            @Override
            public void unregisterObserver(@Nonnull Observer observer) {
                if (observer.equals(null)) throw new NullPointerException("There is no such observer");
                if (observers.stream().noneMatch(obs -> obs == observer))
                    throw new IllegalArgumentException("This observer is not registered!");
                observers.remove(observer);
            }

            @Nonnull
            @Override
            public ImmutableSet<Observer> getObservers() {
                return ImmutableSet.copyOf(observers);
            }

            public void notify(Observer.Event event) {
                for (Observer observer : observers) {
                    observer.onModelChanged(gameState, event);
                }
            }

            // The chooseMove(...) method is called when a move has been chosen by the GUI.
            // It could call the advance(...) method, check if the game is over,
            // and inform the observers about the new state and event
            @Override
            public void chooseMove(@Nonnull Move move) {
                gameState.advance(move);
                if (!gameState.getWinner().isEmpty()) notify(Observer.Event.GAME_OVER);
                else notify(Observer.Event.MOVE_MADE);
            }
        };
        return model;
    }
}
