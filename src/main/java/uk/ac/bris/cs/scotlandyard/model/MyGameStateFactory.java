package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import javax.annotation.Nonnull;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
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

			// Check if any detectives have double tickets
			// Check if any detectives have secret tickets
			for( Player detective : detectives ) {
				if (detective.has(Ticket.DOUBLE) ) {
					throw new IllegalArgumentException("Detective has double ticket.");
				}
				else if (detective.has(Ticket.SECRET)) {
					throw new IllegalArgumentException("Detective has secret ticket.");
				}
			}

			// Check for duplicate detectives
			// Atm this literally compares the entire detective - idk if it should just be checking piece colour?
			// Creates a set as sets can't contain duplicates
			// -- Does mean there's an extra set knocking around
			Set<Player> players = new HashSet<>();
			for( Player detective : detectives ) {
                if(  !players.add(detective) ) { 	// .add returns false if value already exists
					throw new IllegalArgumentException("There are duplicate detectives.");
				}
			}

			// Check for duplicate detective locations
			// Similar logic to above (duplicate detectives)
			Set<Integer> detectiveLocations = new HashSet<>();
			for( Player detective : detectives ) {
                if( !detectiveLocations.add(detective.location()) ) {
					throw new IllegalArgumentException("There are overlapping detectives.");
				}
            }

			// Check that the Graph isn't empty
			if(setup.graph.nodes().isEmpty()){
				throw new IllegalArgumentException("graph is empty.");
			}


			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
		};



		@Override public GameSetup getSetup() {  return setup; }

		// Combines mrX and the list of detectives into a single immutable set
		@Override public ImmutableSet<Piece> getPlayers() {
			Set<Piece> pieces = new HashSet<>(); // Idk if it needs to be done like this, but i assume it cant be immutable
			pieces.add(mrX.piece());
			for( Player detective : detectives) {
				pieces.add(detective.piece());
			}
			return ImmutableSet.copyOf(pieces);


		}

		// Uses logic from the implementation guide
		@Override public Optional<Integer> getDetectiveLocation(Detective detective) {
			for( Player player : detectives ) {
				if( player.piece().webColour().equals(detective.webColour())) { // This line seems real messy
					return Optional.of(player.location());
				}
			}
			return  Optional.empty();
		};


		// Class to implement TicketBoard interface - Used in getPlayerTickets
		private class GameStateTicketBoard implements TicketBoard {
			private final ImmutableMap<Ticket, Integer> tickets;

			// Constructor just stores a pieces tickets
			private GameStateTicketBoard(ImmutableMap<Ticket, Integer> tickets) {
				this.tickets = tickets;
			}

			// Returns the associated number of tickets
			@Override
			public int getCount(@Nonnull Ticket ticket) {
				return tickets.get(ticket);
			}
		}

		// Returns an object that stores a players a tickets to be read
		@Override public Optional<TicketBoard> getPlayerTickets(Piece piece) {

			// Passes Mrx's tickets through
			if( piece.isMrX() ) {
				// Initialises MrX Game State TicketBoard
				TicketBoard MrXGSTicketBoard = new GameStateTicketBoard(mrX.tickets());
                return Optional.of(MrXGSTicketBoard);
			}
			// Iterates through detectives to find a match
			else if (piece.isDetective()) {
				for( Player player : detectives ) {
					if( player.piece().equals(piece)) {
						// Initialises Detectives Game State TicketBoard
						TicketBoard DetGSTicketBoard = new GameStateTicketBoard(player.tickets());
						return Optional.of(DetGSTicketBoard);
					}
				}
			}
			return Optional.empty(); // One of these final 2 return statements probably isn't necessary, but intellij has a spasm otherwise
		}





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
