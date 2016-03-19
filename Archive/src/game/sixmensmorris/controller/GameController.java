package game.sixmensmorris.controller;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import game.sixmensmorris.model.Game;
import game.sixmensmorris.model.Node;
import game.sixmensmorris.model.Player;
import game.sixmensmorris.view.NodeView;
import game.sixmensmorris.view.PieceView;

public class GameController extends JPanel
{
	private static final long serialVersionUID = 5123686544982539956L;
	private static final int N_NODES = 16;
	private static final int N_PIECES = 6;
	
	private int boardStartX, boardStartY, boardCenterX, boardCenterY, boardEndX, boardEndY,
				boardSize, gridSize, redBenchX, blueBenchX;

	private Game currentGame;
	private NodeView[] nodes;
	private PieceView[] redPieces, bluePieces;
	private PieceView selectedPiece;
	private NodeView hoveredNode;
	private int currentPlayer;
	
	public GameController()
	{
		newGame();
		
		// Add mouse listener subclass BoardController, found at end of current class
		addMouseListener(new BoardController());
		
		addMouseMotionListener(new MouseMotionAdapter()
		{
			@Override
			public void mouseMoved(MouseEvent e) 
			{
				for (NodeView node : nodes)
					if (node.contains(e.getPoint()))
						hoveredNode = node;
					
				repaint();
			}
		});
	}
	
	public void newGame()
	{
		// Starts new game and goes to set board state mode
		currentGame = new Game(N_PIECES);
		currentPlayer = -1;

		// Initialize Node View and Piece View objects
		initNodes();
		initPieces();
		
		selectedPiece = null;
		
		repaint();
	}
	
	// Switch between turn-based game and setting up board state, and return current mode
	public int switchMode()
	{
		if (currentPlayer == -1)
			currentPlayer = (int)(Math.random() * 2);
		else
			currentPlayer = -1;
		
		return currentPlayer;
	}
	
	// Initialize NodeViews
	private void initNodes()
	{
		// Initialize array
		nodes = new NodeView[N_NODES];
		Node currentNode;
		
		// For each NodeView array element
		for (int i = 0; i < N_NODES; i++)
		{
			// Get Node from current Game object
			currentNode = currentGame.getNode(i);
			
			// Initialize NodeView
			nodes[i] = new NodeView(0, 0, currentNode);
		}
	}

	// Initialize PieceViews
	private void initPieces()
	{
		// Initialize piece arrays
		redPieces = new PieceView[N_PIECES];
		bluePieces = new PieceView[N_PIECES];
		
		// For each red and blue piece
		for (int i = 0; i < N_PIECES; i++)
		{
			// Initialize red and blue PieceView
			redPieces[i] = new PieceView(0, 0, 0);
			bluePieces[i] = new PieceView(0, 0, 1);
		}
	}
	
	// Calculates board dimensions and coordinates
	private void calcBoard()
	{
		int size = Math.min(getHeight() + 180, getWidth());
		
		redBenchX = 60;
		blueBenchX = size - 60;
		boardStartX = 140;
		boardEndX = size - 140;
		boardStartY = 50;
		
		boardSize = boardEndX - boardStartX;
		boardEndY = boardStartY + boardSize;
		boardCenterX = (boardStartX + boardEndX) / 2;
		boardCenterY = (boardStartY + boardEndY) / 2;
		gridSize = boardSize / 4;
	}
	
	// Calculates coordinates of nodes and pieces
	private void calcCoordinates()
	{
		calcBoard();
		
		// Declare Node, x, y values
		Node currentNode;
		int x, y;

		// For each NodeView array element
		for (int i = 0; i < N_NODES; i++)
		{
			// Get Node from current NodeView
			currentNode = nodes[i].getNode();
			
			// Calculate NodeView (x,y) coordinates from Node column and row
			x = boardStartX + gridSize * (currentNode.getColumn() - 'A');
			y = boardStartY + gridSize * currentNode.getRow();

			// Move to new coordinates
			nodes[i].moveTo(x, y);
		}

		// For each red and blue piece
		for (int i = 0; i < N_PIECES; i++)
		{
			// Calculate current piece's bench y value
			y = boardStartY + i * (boardSize / 5);

			// If on board, move to corresponding node, otherwise move to bench
			if (redPieces[i].currentNode() != null)
				redPieces[i].moveToNode(redPieces[i].currentNode());
			else
				redPieces[i].moveTo(redBenchX, y);

			// If on board, move to corresponding node, otherwise move to bench
			if (bluePieces[i].currentNode() != null)
				bluePieces[i].moveToNode(bluePieces[i].currentNode());
			else
				bluePieces[i].moveTo(blueBenchX, y);		
		}
	}
	
	// Paints current state of board
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		// Cast Graphics to Graphics2D
		Graphics2D g2d = (Graphics2D) g;
		
		calcCoordinates();
		
		// Draw outer and inner squares
		g2d.drawRect(boardStartX, boardStartY, boardSize, boardSize);
		g2d.drawRect(boardStartX + gridSize, boardStartY + gridSize, 2 * gridSize, 2 * gridSize);
		
		// Draw vertical center lines
		g2d.drawLine(boardCenterX, boardStartY, boardCenterX, boardStartY + gridSize);
		g2d.drawLine(boardCenterX, boardEndY, boardCenterX, boardEndY - gridSize);
		
		// Draw horizontal center lines
		g2d.drawLine(boardStartX, boardCenterY, boardStartX + gridSize, boardCenterY);
		g2d.drawLine(boardEndX, boardCenterY, boardEndX - gridSize, boardCenterY);
		
		// Draw nodes
		for (NodeView node : nodes)
		{			
			g2d.fill(node);
		}
		
		// Draw red pieces
		for (PieceView piece : redPieces)
		{
			g2d.setPaint(Color.RED);
			g2d.fill(piece);
		}
		
		// Draw blue pieces
		for (PieceView piece : bluePieces)
		{
			g2d.setPaint(Color.BLUE);
			g2d.fill(piece);
		}
		
		// Change color of selected piece
		if (selectedPiece != null)
		{
			g2d.setPaint(Color.BLACK);
			g2d.fill(selectedPiece);
		}
		
		// Draw square representing whose turn it is
		if (currentPlayer == 0)
			g2d.setPaint(Color.RED);
		else if (currentPlayer == 1)
			g2d.setPaint(Color.BLUE);
		else
			g2d.setPaint(Color.BLACK);
		
		g2d.fillRect(boardCenterX - gridSize / 4, boardCenterY - gridSize / 4, gridSize / 2, gridSize / 2);
		
		// Draw circle over hovered node
		if (hoveredNode != null)
		{
			g2d.drawOval((int)hoveredNode.getCenterX() - 30, (int)hoveredNode.getCenterY() - 30, 60, 60);
		}
	}
	
	// Move selected PieceView to chosen NodeView and end turn
	private void finalizeMove(NodeView node)
	{
		// Move PieceView in display
		selectedPiece.moveToNode(node);
		selectedPiece = null;
		
		// If in turn-based mode, end turn
		if (currentPlayer != -1)
			currentPlayer = 1 - currentPlayer;
	}
	
	// Attempt selection of given piece and return whether it was successful
	private void selectPiece(PieceView piece, int pieceID)
	{
		if (selectedPiece != null)
		{
			JOptionPane.showMessageDialog(null, "A piece cannot be placed on another piece.",
					"Incorrect Placement",	JOptionPane.INFORMATION_MESSAGE);
		}
		else if (currentPlayer == 1 - pieceID)
		{
			JOptionPane.showMessageDialog(null, "This piece belongs to the wrong player.",
					"Incorrect Selection",	JOptionPane.INFORMATION_MESSAGE);
		}
		else if (currentPlayer != -1 && piece.currentNode() != null && currentGame.player(pieceID).hasPiece())
		{
			JOptionPane.showMessageDialog(null, "You must place all pieces before making moves on the board.",
					"Incorrect Selection",	JOptionPane.INFORMATION_MESSAGE);
		}
		else
		{
			selectedPiece = piece;
		}
	}
	
	// Manages all mouse input
	public class BoardController extends MouseAdapter
	{	
		// When mouse is pressed
		@Override
		public void mousePressed (MouseEvent e)
		{
			// If piece was clicked, select piece
			for (int i = 0; i < N_PIECES; i++)
			{
				if (redPieces[i].contains(e.getPoint()))
				{
					selectPiece(redPieces[i], 0);
					return;
				}
				
				if (bluePieces[i].contains(e.getPoint()))
				{
					selectPiece(bluePieces[i], 1);
					return;
				}
			}
			
			
			// If node was clicked
			for (NodeView node : nodes)
			{
				if (node.contains(e.getPoint()))
				{
					// If a piece is currently selected
					if (selectedPiece != null)
					{
						// If piece hasn't been placed yet
						if (selectedPiece.currentNode() == null)
						{
							// Get player of selected piece
							Player selectedPlayer = currentGame.player(selectedPiece.getId());

							// Attempt placement of piece on to selected node
							if (currentGame.setPiece(node.getNode(), selectedPlayer))
							{
								// If successful in model, finalize move in controller
								finalizeMove(node);
							}
						}
						else
						{
							// Get clicked node and node of selected piece
							Node origin = selectedPiece.currentNode().getNode();
							Node destination = node.getNode();
							
							// Attempt move from previous node to clicked node
							if (currentGame.movePiece(origin, destination))
							{
								// If successful in model, finalize move in controller
								finalizeMove(node);
							}
						}
					}
					else
					{
						JOptionPane.showMessageDialog(null, "You need to select a piece first.", "Wait",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
			
			// Display changes made by mouse click
			repaint();
		}
	}
}
