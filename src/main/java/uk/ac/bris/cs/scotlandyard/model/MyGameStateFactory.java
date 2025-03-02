package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import javax.annotation.Nonnull;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {
	private final class MyGameState implements GameState {
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;

		private MyGameState(
			final GameSetup setup,
			final ImmutableSet<Piece> remaining,
			final ImmutableList<LogEntry> log,
			final Player mrX,
			final List<Player> detectives ) {

			// Null conditions
			// Checks if Mr X is null
			if(mrX == null) throw new NullPointerException("MrX cannot be null!");

			// Checks if Detectives is null
			if(detectives == null) throw new NullPointerException(("Detectives list cannot be null!"));

			// Check for any null detectives in the list
			for(Player detective : detectives) {
				if (detective == null) {
					throw new NullPointerException("Detective in detective list cannot be null!");
				}
			}

			// Check if there is no Mr X is not defined
			if(!mrX.piece().isMrX()) throw new IllegalArgumentException("There has to be 1 Mr X player!");

			// Check if there is more than one Mr X player
			for (Player player : detectives) {
				if(player.piece().isMrX()) {
					throw new IllegalArgumentException("There must be no more  than 1 Mr X player!");
				}
			}

			// Check if moves are empty
			if(setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");

			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
		};



		@Override public GameSetup getSetup() {  return setup; }
		@Override public ImmutableSet<Piece> getPlayers() { return null; }
		@Override public Optional<Integer> getDetectiveLocation(Detective detective) {return null;};
		@Override public Optional<TicketBoard> getPlayerTickets(Piece piece) { return null;}
		@Override public ImmutableList<LogEntry> getMrXTravelLog() { return log;}
		@Override public ImmutableSet<Piece> getWinner() { return null;}
		@Override public ImmutableSet<Move> getAvailableMoves() { return null;}

		@Override public GameState advance(Move move) {  return null;  }
	}
	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
				return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);
	}


}
