package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import javax.annotation.Nonnull;

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
	@Nonnull
	@Override
	public GameState build(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);	}

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
				final List<Player> detectives) {

			// Null conditions

			// Checks if Mr X is null
			if (mrX == null) throw new NullPointerException("MrX cannot be null!");

			// Checks if Detectives is null
			if (detectives == null) throw new NullPointerException(("Detectives list cannot be null!"));

			// Sets used to find duplicates
			Set<Player> players = new HashSet<>();
			Set<Integer> detectiveLocations = new HashSet<>();

			// All tests that check each detective
			for (Player detective : detectives) {

				// Check for any null detectives in the list
				if (detective == null) throw new NullPointerException("Detective in detective list cannot be null!");

				// Check if there is more than one Mr X player
				if (detective.piece().isMrX())
					throw new IllegalArgumentException("There must be no more  than 1 Mr X player!");

				// Check if any detectives have double tickets
				if (detective.has(Ticket.DOUBLE)) throw new IllegalArgumentException("Detective has double ticket.");

				// Check if any detectives have secret tickets
				if (detective.has(Ticket.SECRET)) throw new IllegalArgumentException("Detective has secret ticket.");

				// Check for duplicate detectives - .add returns false if value already exists
				if (!players.add(detective)) throw new IllegalArgumentException("There are duplicate detectives.");

				// Check for duplicate detective locations - uses similar logic to above (duplicate detectives)
				if (!detectiveLocations.add(detective.location()))
					throw new IllegalArgumentException("There are overlapping detectives.");
			}

			// Check if there is no Mr X / is not defined
			if (!mrX.piece().isMrX()) throw new IllegalArgumentException("There has to be 1 Mr X player!");

			// Check if moves are empty
			if (setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");

			// Check that the Graph isn't empty
			if (setup.graph.nodes().isEmpty()) throw new IllegalArgumentException("Graph is empty.");

			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
		}

		@Override
		public GameSetup getSetup() {
			return setup;
		}

		// Combines mrX and the list of detectives into a single immutable set
		@Override
		public ImmutableSet<Piece> getPlayers() {
			Set<Piece> pieces = new HashSet<>();

			pieces.add(mrX.piece());
			for (Player detective : detectives) {
				pieces.add(detective.piece());
			}

			return ImmutableSet.copyOf(pieces);
		}

		// Uses logic from the implementation guide
		@Override
		public Optional<Integer> getDetectiveLocation(Detective detective) {

			for (Player player : detectives) {
				if (player.piece().equals(detective)) {
					return Optional.of(player.location());
				}
			}

			return Optional.empty();
		}

		// Class to implement TicketBoard interface - Used in getPlayerTickets
		private class GameStateTicketBoard implements TicketBoard {
			private final ImmutableMap<Ticket, Integer> tickets;

			// Constructor just stores a piece's tickets
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
		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {

			// Passes Mrx's tickets through
			if (piece.isMrX()) {
				// Initialises MrX Game State TicketBoard
				TicketBoard MrXGSTicketBoard = new GameStateTicketBoard(mrX.tickets());
				return Optional.of(MrXGSTicketBoard);
			}
			// Iterates through detectives to find a match
			else if (piece.isDetective()) {
				for (Player player : detectives) {
					if (player.piece().equals(piece)) {
						// Initialises Detectives Game State TicketBoard
						TicketBoard DetGSTicketBoard = new GameStateTicketBoard(player.tickets());
						return Optional.of(DetGSTicketBoard);
					}
				}
			}
			// Returns an empty ticket board for players that don't exist
			return Optional.empty();
		}

		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			return log;
		}

		@Override
		public ImmutableSet<Piece> getWinner() {
			Set<Piece> winners = new HashSet<Piece>();

			// detective win:
			// when player's move is on MrX
			for (Player detective: detectives){
				if (detective.location() == mrX.location()){
					for (Player player : detectives){
						winners.add(player.piece());
					}
				}
			}
			// when MrX has no available moves
			// if mrx has no available moves on his go
			if (makeMrXMoves(setup, detectives, mrX, log).isEmpty() && remaining.iterator().next().isMrX()) {
				for (Player player : detectives) {
					winners.add(player.piece());
				}
			}

			// MrX win:
			// MrX fills log
			if (setup.moves.size() == (log.size()) && remaining.iterator().next().isMrX()) {
				winners.add(mrX.piece());
			}

			// Detective can no longer move any of its pieces


			return ImmutableSet.copyOf(winners);
		}

		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			Set<Move> availableMoves = new HashSet<>();

			// Check if there are remaining pieces to move & if the game is over yet
			if (!remaining.isEmpty() && getWinner().isEmpty()) {
				// Current player is the first in the remaining set
				Piece currentPlayer = remaining.iterator().next();

				if (currentPlayer.isMrX()) {

					availableMoves.addAll(makeMrXMoves(setup, detectives, mrX, log));
				}
				else {

					availableMoves.addAll(makeDetectiveMoves(setup, detectives, remaining));
				}

			}

			return ImmutableSet.copyOf(availableMoves);
		}

		private static HashSet<SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
			// Collection to store all possible single moves
			HashSet<SingleMove> moves = new HashSet<>();

			for (int destination : setup.graph.adjacentNodes(source)) {
				// Checks if destination is occupied by a detective
				boolean occupied = false;

				for (Player detective : detectives) {
					if (detective.location() == destination) { //  If the location is occupied, don't add to the collection of moves to return
						occupied = true;
						break;
					}
				}

				if (occupied) {continue;}

				for (Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {
					// Check if player has required ticket
					if (player.tickets().get(t.requiredTicket()) != 0) {
						moves.add(new SingleMove(player.piece(), source, t.requiredTicket(), destination)); // Constructs a single move
					}
				}

				// Checks if player has the required secret ticket
				if (player.tickets().get(Ticket.SECRET) != 0) {
					moves.add(new SingleMove(player.piece(), source, Ticket.SECRET, destination)); // Constructs a single move
				}
			}
			return moves;
		}

		private static Set<DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
			// Checks for double move ticket
			if (player.tickets().get(Ticket.DOUBLE) == 0) return Collections.emptySet();

			// Create collection for double moves
			HashSet<DoubleMove> moves = new HashSet<>();
			HashSet<SingleMove> firstMoves = makeSingleMoves(setup, detectives, player, source);

			for (SingleMove firstMove : firstMoves) {
				// Used to simulate first move after using a ticket
				Player playerAfterFirstMove = player.use(firstMove.ticket);

				// Get valid second moves from the destination of the first move
				HashSet<SingleMove> secondMoves = makeSingleMoves(setup, detectives, playerAfterFirstMove, firstMove.destination);

				// Generates collection of all valid double moves
				for (SingleMove secondMove : secondMoves) {
					moves.add(new DoubleMove(player.piece(), source, firstMove.ticket, firstMove.destination, secondMove.ticket, secondMove.destination
					));
				}
			}

			return moves;
		}

		private static Set<Move> makeMrXMoves(GameSetup setup, List<Player> detectives, Player mrX, ImmutableList<LogEntry> log) {
			Set<Move> availableMoves = new HashSet<>();

			// Generate all single moves for MrX
			Set<SingleMove> singleMoves = makeSingleMoves(setup, detectives, mrX, mrX.location());
			availableMoves.addAll(singleMoves);

			// Generate all double moves for MrX and checks least 2 moves left in the game
			if (setup.moves.size() - log.size() >= 2 && mrX.has(Ticket.DOUBLE)) {
				Set<DoubleMove> doubleMoves = makeDoubleMoves(setup, detectives, mrX, mrX.location());
				availableMoves.addAll(doubleMoves);
			}
			return availableMoves;
		}

		private static Set<Move> makeDetectiveMoves(GameSetup setup, List<Player> detectives, ImmutableSet<Piece> remaining) {
			Set<Move> availableMoves = new HashSet<>();

			// Generate moves for all detectives in remaining
			for (Player detective : detectives) {
				if (remaining.contains(detective.piece())) {
					// Generate all single moves for the detective
					Set<SingleMove> detectiveMoves = makeSingleMoves(setup, detectives, detective, detective.location());
					availableMoves.addAll(detectiveMoves);
				}
			}

			return availableMoves;
		}


		@Override
		public GameState advance(Move move) {
			// Check for invalid move
			if (!getAvailableMoves().contains(move)) {
				throw new IllegalArgumentException("Illegal move: " + move);
			}

			return move.accept(new Move.Visitor<GameState>() {
				@Override
				public GameState visit(SingleMove move) {
					// Handle SingleMove
					Player updatedMrX = mrX;
					List<Player> updatedDetectives = new ArrayList<>(detectives);
					ImmutableList<LogEntry> updatedLog = log;
					ImmutableSet<Piece> updatedRemaining = remaining;

					if (move.commencedBy().isMrX()) {
						// Update MrX's position and tickets
						updatedMrX = mrX.use(move.ticket).at(move.destination);

						// Update the travel log
						boolean shouldReveal = setup.moves.size() > log.size() && setup.moves.get(log.size());
						LogEntry newLogEntry;

						if (shouldReveal) {
							newLogEntry = LogEntry.reveal(move.ticket, move.destination);
						} else {
							newLogEntry = LogEntry.hidden(move.ticket);
						}

						updatedLog = ImmutableList.<LogEntry>builder()
								.addAll(log)
								.add(newLogEntry)
								.build();

						// Switch to detectives' turn
						Set<Piece> detectivePieces = new HashSet<>();
						for (Player detective : detectives) {
							detectivePieces.add(detective.piece());
						}
						updatedRemaining = ImmutableSet.copyOf(detectivePieces);
					}

					else {
						// Update detective's position and tickets
						for (int i = 0; i < detectives.size(); i++) {
							Player detective = detectives.get(i);
							if (detective.piece().equals(move.commencedBy())) {
								Player updatedDetective = detective.use(move.ticket).at(move.destination);
								updatedDetectives.set(i, updatedDetective);

								// Give the used ticket to MrX
								updatedMrX = mrX.give(move.ticket);
								break;
							}
						}

						// Remove the detective from the remaining set
						Set<Piece> newRemaining = new HashSet<>();
						for (Piece piece : remaining) {
							if (!piece.equals(move.commencedBy())) {
								newRemaining.add(piece);
							}
						}
						updatedRemaining = ImmutableSet.copyOf(newRemaining);

						// After end of detectives' turn, switch to MrX's turn
						if (updatedRemaining.isEmpty()) {
							updatedRemaining = ImmutableSet.of(MrX.MRX);
						}
					}

					// Return the new game state
					return new MyGameState(setup, updatedRemaining, updatedLog, updatedMrX, updatedDetectives);
				}

				@Override
				public GameState visit(DoubleMove move) {
					// Handle DoubleMove
					Player updatedMrX = mrX.use(Ticket.DOUBLE)
							.use(move.ticket1)
							.use(move.ticket2)
							.at(move.destination2);

					// Update the travel log for both moves
					ImmutableList.Builder<LogEntry> logBuilder = ImmutableList.builder();
					logBuilder.addAll(log);

					// First move log entry
					boolean shouldReveal1 = setup.moves.size() > log.size() && setup.moves.get(log.size()),
							shouldReveal2 = setup.moves.size() > log.size() + 1 && setup.moves.get(log.size() + 1);
					LogEntry firstLogEntry;
					if (shouldReveal1) {
						firstLogEntry = LogEntry.reveal(move.ticket1, move.destination1);
					} else {
						firstLogEntry = LogEntry.hidden(move.ticket1);
					}
					logBuilder.add(firstLogEntry);

					// Second move log entry
					LogEntry secondLogEntry;
					if (shouldReveal2) {
						secondLogEntry = LogEntry.reveal(move.ticket2, move.destination2);
					} else {
						secondLogEntry = LogEntry.hidden(move.ticket2);
					}
					logBuilder.add(secondLogEntry);

					ImmutableList<LogEntry> updatedLog = logBuilder.build();

					// Switch to detectives' turn
					Set<Piece> detectivePieces = new HashSet<>();
					for (Player detective : detectives) {
						detectivePieces.add(detective.piece());
					}
					ImmutableSet<Piece> updatedRemaining = ImmutableSet.copyOf(detectivePieces);
					// Return the new game state
					return new MyGameState(setup, updatedRemaining, updatedLog, updatedMrX, detectives);
				}
			});
		}	}
}
