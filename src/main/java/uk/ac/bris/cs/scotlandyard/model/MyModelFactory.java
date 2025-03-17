package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;

import java.util.HashSet;
import java.util.Set;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	@Nonnull @Override public Model build(GameSetup setup,
										  Player mrX,
										  ImmutableList<Player> detectives) {
		// TODO
		return new MyModel(null);}
	private final class MyModel implements Model {
		private GameState state;
		private Set<Observer> observers;

		private MyModel (GameState state) {
			this.state = state;
			this.observers = new HashSet<>();
			}


		@Nonnull
		@Override
		public Board getCurrentBoard() {
			return state;
		}

		@Override
		public void registerObserver(Observer observer) {
			if(observer == null) throw new NullPointerException("Observer can not be null!");
			if(observers.contains(observer)) throw new IllegalArgumentException("Can not register same observer twice!");

			observers.add(observer);
		}

		@Override
		public void unregisterObserver(Observer observer) {
			if(observer == null) throw new NullPointerException("Observer can not be null!");
			if(!observers.contains(observer)) throw new IllegalArgumentException("Can not remove invalid observer!");

			observers.remove(observer);
		}

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {
			return ImmutableSet.copyOf(observers);
		}

		@Override
		public void chooseMove(Move move) {

		}
	}
}