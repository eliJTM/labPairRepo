package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import javax.annotation.Nonnull;
import javax.crypto.spec.PSource;

import com.google.errorprone.annotations.Immutable;
import uk.ac.bris.cs.scotlandyard.model.Move.SingleMove;
import uk.ac.bris.cs.scotlandyard.model.Move.DoubleMove;
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

			// Sets used to find duplicates
			Set<Player> players = new HashSet<>();
			Set<Integer> detectiveLocations = new HashSet<>();

			// All tests that check each detective
			for(Player detective : detectives) {

				// Check for any null detectives in the list
				if (detective == null) throw new NullPointerException("Detective in detective list cannot be null!");

				// Check if there is more than one Mr X player
				if(detective.piece().isMrX()) throw new IllegalArgumentException("There must be no more  than 1 Mr X player!");

				// Check if any detectives have double tickets
				if (detective.has(Ticket.DOUBLE) ) throw new IllegalArgumentException("Detective has double ticket.");

				// Check if any detectives have secret tickets
				if (detective.has(Ticket.SECRET)) throw new IllegalArgumentException("Detective has secret ticket.");

				// Check for duplicate detectives - .add returns false if value already exists
				if(!players.add(detective)) throw new IllegalArgumentException("There are duplicate detectives.");

				// Check for duplicate detective locations - uses similar logic to above (duplicate detectives)
				if(!detectiveLocations.add(detective.location())) throw new IllegalArgumentException("There are overlapping detectives.");
			}

			// Check if there is no Mr X / is not defined
			if(!mrX.piece().isMrX()) throw new IllegalArgumentException("There has to be 1 Mr X player!");

			// Check if moves are empty
			if(setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");

			// Check that the Graph isn't empty
			if(setup.graph.nodes().isEmpty()) throw new IllegalArgumentException("Graph is empty.");

			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
		}

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
			// Returns an empty ticket board for players that don't exist
			return Optional.empty();
		}

		@Override public ImmutableList<LogEntry> getMrXTravelLog() { return log;}

		@Override public ImmutableSet<Piece> getWinner() { return null; }

		@Override public ImmutableSet<Move> getAvailableMoves() {


			Set<Move> availableMoves = new HashSet<>();



			// Check for any remaining pieces, then no moves are available
			if (!remaining.isEmpty()) {
				// Create a new piece used to determine the current player
				Piece currentPlayer = remaining.iterator().next();

				if (currentPlayer.isMrX()) {
					// Generate all single moves for MrX
					Set<SingleMove> singleMoves = makeSingleMoves(setup, detectives, mrX, mrX.location());
					availableMoves.addAll(singleMoves);

					// Generate all double moves for MrX
					if (setup.moves.size() - log.size() >= 2) { // Checks if there are atleast 2 moves of the game left
						Set<DoubleMove> doubleMoves = makeDoubleMoves(setup, detectives, mrX, mrX.location());
						availableMoves.addAll(doubleMoves);
					}
				} else {
					// Get current player's turn
					for (Player detective : detectives) {
						if (detective.piece().equals(currentPlayer)) {
							// Generate all single moves for the detective
							Set<SingleMove> detectiveMoves = makeSingleMoves(setup, detectives, detective, detective.location());
							availableMoves.addAll(detectiveMoves);
							break;
						}
					}
				}
			}

			// If no moves are available for the current player, all players get to move again
			if (availableMoves.isEmpty()) {
				for (Player detective : detectives) {
					Set<SingleMove> detectiveMoves = makeSingleMoves(setup, detectives, detective, detective.location());
					availableMoves.addAll(detectiveMoves);
				}
			}

			// Store the calculated moves and return an immutable copy
			moves = ImmutableSet.copyOf(availableMoves);
			return moves;
		}

		private static Set<SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source){

			// TODO create an empty collection of some sort, say, HashSet, to store all the SingleMove we generate
			Set<SingleMove> moves = new HashSet<>();

			for(int destination : setup.graph.adjacentNodes(source)) {
				// TODO find out if destination is occupied by a detective
				//  if the location is occupied, don't add to the collection of moves to return

				boolean occupied = false;

				for(Player detective : detectives) {
					if (detective.location() == destination) {
						occupied = true;
						break;
					}
				}

				if(occupied) { continue; }

				for(Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()) ) {
					// TODO find out if the player has the required tickets
					//  if it does, construct a SingleMove and add it the collection of moves to return
					if (player.tickets().get(t.requiredTicket()) != 0) {
						moves.add( new SingleMove(player.piece(), source, t.requiredTicket(), destination));
					}
				}

				// TODO consider the rules of secret moves here
				if (player.tickets().get(Ticket.SECRET) != 0) {
					moves.add( new SingleMove(player.piece(), source, Ticket.SECRET, destination));
				}
			}
			// TODO return the collection of moves
			return moves;
		}

		private Set<DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
			// Checks if player has double move ticket
			if (player.tickets().get(Ticket.DOUBLE) == 0) return Collections.emptySet();

			// Create collection for double moves
			Set<DoubleMove> doubleMoves = new HashSet<>();
			Set<SingleMove> firstMoves = makeSingleMoves(setup, detectives, player, source);

			for (SingleMove firstMove : firstMoves) {
				// Used to simulate player's move after using a ticket
				Player playerAfterFirstMove = player.use(firstMove.ticket);

				// Get valid second moves from the destination of the first move
				Set<SingleMove> secondMoves = makeSingleMoves(setup, detectives, playerAfterFirstMove, firstMove.destination);

				// Generates collection of all valid double moves
				for (SingleMove secondMove : secondMoves) {
					doubleMoves.add(new DoubleMove(player.piece(), source, firstMove.ticket, firstMove.destination, secondMove.ticket, secondMove.destination
					));
				}
			}

			return doubleMoves;
		}

		@Override public GameState advance(Move move) {
			// check if given move is valid
			moves = getAvailableMoves();
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);

			// TODO use Visitor pattern for defining the different behaviours or something like that
			return null;
		}
	}




	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
				return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);
	}


}
