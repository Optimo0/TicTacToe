let stompClient = null;
let game = null;
let player = null;

/**
 * Sends a message to the server using the STOMP client.
 * @param {Object} message - The message to be sent. Must contain at least a "type" field.
 */
const sendMessage = (message) => {
    stompClient.send(`/app/${message.type}`, {}, JSON.stringify(message));
}

/**
 * Sends a move message to the server.
 * @param {Number} move - The index of the cell where the move should be made.
 */
const makeMove = (move) => {
    console.log(`Making move: ${move}`);
    console.log("Game:", game);
    sendMessage({
        type: "game.move",
        move: move,
        turn: game.turn,
        sender: player,
        gameId: game.gameId,
        lastMoveTime: game.lastMoveTime
    })
}

/**
 * An object containing functions to handle each type of message received from the server.
 */
const messagesTypes = {
    "game.join": (message) => {
        updateGame(message);
    },
    "game.gameOver": (message) => {
        console.log("Received game.gameOver message", message);
        updateGame(message);
        if (message.gameState === 'TIE') {
            toastr.success(`Game over! It's a tie!`);
        } else {
            console.log("Winner information:", message.winner);
            showWinner(message.winner);
        }
    },
    "game.joined": (message) => {
        if (game !== null && game.gameId !== message.gameId) return;
        player = localStorage.getItem("playerName");
        updateGame(message);

        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function (frame) {
            stompClient.subscribe(`/topic/game.${message.gameId}`, function (message) {
                handleMessage(JSON.parse(message.body));
            });
        });
    },
    "game.move": (message) => {
        updateGame(message);
        updateTimers();
    },
    "game.left": (message) => {
        updateGame(message);
        if (message.winner) showWinner(message.winner);
    },
    "error": (message) => {
        toastr.error(message.content);
    }
}

/**
 * Handles a message received from the server.
 * @param {Object} message - The message received.
 */
const handleMessage = (message) => {
    if (messagesTypes[message.type])
        messagesTypes[message.type](message);
}

/**
 * Converts a message received from the server into a game object.
 * @param {Object} message - The message received.
 * @returns {Object} The game object.
 */
const messageToGame = (message) => {
    return {
        gameId: message.gameId,
        board: message.board,
        turn: message.turn,
        player1: message.player1,
        player2: message.player2,
        gameState: message.gameState,
        winner: message.winner,
        lastMoveTime: new Date(message.lastMoveTime),
        startTime: new Date(message.startTime),
    }
}


/**
 * Displays a success message with the name of the winning player.
 * @param {String} winner - The name of the winning player.
 */
const showWinner = (winner) => {
    if (winner === "TIE") {
        toastr.success("Game over! It's a tie!");
    } else {
        toastr.success(`The winner is ${getPlayerName(winner)}!`);
        const winningPositions = getWinnerPositions(game.board);
        if (winningPositions.length === 5) {
            winningPositions.forEach(position => {
                const row = Math.floor(position / game.board.length);
                const cell = position % game.board.length;
                let cellElement = document.querySelector(`.row-${row} .cell-${cell} span`);
                cellElement.style.backgroundColor = '#b3e6ff';
            })
        }
    }
}

const getPlayerName = (mark) => {
    return mark === "X" ? game.player1 : game.player2;
}

/**
 * Get the winner positions from the board.
 */
const getWinnerPositions = (board) => {
    const winnerPositions = [];

    // Check horizontally
    for (let i = 0; i < board.length; i++) {
        for (let j = 0; j <= board.length - 5; j++) {
            if (board[i][j] !== ' ' && board[i][j] === board[i][j + 1] && board[i][j + 1] === board[i][j + 2] &&
                board[i][j + 2] === board[i][j + 3] && board[i][j + 3] === board[i][j + 4]) {
                for (let k = 0; k < 5; k++) {
                    winnerPositions.push(i * board.length + (j + k));
                }
            }
        }
    }

    // Check vertically
    for (let i = 0; i < board.length; i++) {
        for (let j = 0; j <= board.length - 5; j++) {
            if (board[j][i] !== ' ' && board[j][i] === board[j + 1][i] && board[j + 1][i] === board[j + 2][i] &&
                board[j + 2][i] === board[j + 3][i] && board[j + 3][i] === board[j + 4][i]) {
                for (let k = 0; k < 5; k++) {
                    winnerPositions.push((j + k) * board.length + i);
                }
            }
        }
    }

    // Check diagonally (top-left to bottom-right)
    for (let i = 0; i <= board.length - 5; i++) {
        for (let j = 0; j <= board.length - 5; j++) {
            if (board[i][j] !== ' ' && board[i][j] === board[i + 1][j + 1] && board[i + 1][j + 1] === board[i + 2][j + 2] &&
                board[i + 2][j + 2] === board[i + 3][j + 3] && board[i + 3][j + 3] === board[i + 4][j + 4]) {
                for (let k = 0; k < 5; k++) {
                    winnerPositions.push((i + k) * board.length + (j + k));
                }
            }
        }
    }

    // Check diagonally (top-right to bottom-left)
    for (let i = 0; i <= board.length - 5; i++) {
        for (let j = board.length - 1; j >= 4; j--) {
            if (board[i][j] !== ' ' && board[i][j] === board[i + 1][j - 1] && board[i + 1][j - 1] === board[i + 2][j - 2] &&
                board[i + 2][j - 2] === board[i + 3][j - 3] && board[i + 3][j - 3] === board[i + 4][j - 4]) {
                for (let k = 0; k < 5; k++) {
                    winnerPositions.push((i + k) * board.length + (j - k));
                }
            }
        }
    }

    return winnerPositions;
}

/**
 * Starts the process of joining a game. Asks the player to enter their name and sends a message to the server requesting to join the game.
 */
const joinGame = () => {
    const playerName = prompt("Enter your name:");
    localStorage.setItem("playerName", playerName);
    sendMessage({
        type: "game.join",
        player: playerName
    });
}

/**
 * Connects the STOMP client to the server and subscribes to the "/topic/game.state" topic.
 */
const connect = () => {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected to WebSocket');
        stompClient.subscribe('/topic/game.state', function (message) {
            console.log('Received WebSocket message:', message);
            handleMessage(JSON.parse(message.body));
            updateTimers();
        });
        loadGame();
    });
}

/**
 * Attempts to load a game by joining with the player's previously stored name, or prompts the player to enter their name if no name is stored.
 */
const loadGame = () => {
    const playerName = localStorage.getItem("playerName");
    if (playerName) {
        sendMessage({
            type: "game.join",
            player: playerName
        });
    } else {
        joinGame();
    }
}

/**
 * Updates the game state with the information received from the server.
 * @param {Object} message - The message received from the server.
 */
const updateGame = (message) => {
    game = messageToGame(message);
    updateBoard(message.board);

    const lastMoveTime = new Date(message.lastMoveTime);
    const startTime = new Date(message.startTime);

    // Update player1
    const player1Element = document.getElementById("player1");
    if (player1Element) {
        player1Element.innerHTML = game.player1;
    }

    // Update player2
    const player2Element = document.getElementById("player2");
    if (player2Element) {
        player2Element.innerHTML = game.player2 || (game.winner ? '-' : 'Waiting for player 2...');
    }

    // Update turn
    const turnElement = document.getElementById("turn");
    if (turnElement) {
        turnElement.innerHTML = game.turn;
    }

    // Update winner
    const winnerElement = document.getElementById("winner");
    if (winnerElement) {
        winnerElement.innerHTML = game.winner ? getPlayerName(game.winner) : '-';
    }

    // Update lastMoveTime
    const lastMoveTimeElement = document.getElementById("lastMoveTime");
    if (lastMoveTimeElement) {
        lastMoveTimeElement.innerHTML = formatTime(lastMoveTime);
    }

    // Update startTime
    const startTimeElement = document.getElementById("startTime");
    if (startTimeElement) {
        startTimeElement.innerHTML = formatTime(startTime);
    }

    // Update timers
    updateTimers(lastMoveTime, startTime);
}


const updateTimers = () => {
    console.log("Updating timers...");
    const moveTimerElement = document.getElementById("move-timer");
    const gameTimerElement = document.getElementById("game-timer");

    if (game !== null) {
        const moveTimeLeft = game.lastMoveTime ? Math.max(0, Math.floor((game.turn === player ? 30 : 0) - (Date.now() - game.lastMoveTime) / 1000)) : 30;
        const gameTimeLeft = game.startTime ? Math.max(0, Math.floor((15 * 60 - (Date.now() - game.startTime) / 1000))) : 900;

        moveTimerElement.innerHTML = `Move Time Left: ${formatTime(moveTimeLeft)}`;
        gameTimerElement.innerHTML = `Game Time Left: ${formatTime(gameTimeLeft)}`;

        if (moveTimeLeft <= 0 && !game.timeout) {
            switchTurn();
        }
    }
};

// Function to switch turn when move time exceeds the limit
const switchTurn = () => {
    if (game !== null && !game.timeout) {
        game.timeout = true;
        game.turn = game.turn === game.player1 ? game.player2 : game.player1;

        // Notify the server about the turn switch
        sendMessage({
            type: "game.move",
            move: -1,
            turn: game.turn,
            sender: player,
            gameId: game.gameId,
            lastMoveTime: game.lastMoveTime
        });

        // Update the UI
        updateBoard(game.board);
        updateTimers();
    }
};


window.onload = function () {
    connect();
    setInterval(updateTimers, 1000);
};


// Format time as HH:mm:ss
const formatTime = (time) => {
    if (time instanceof Date) {
        const hours = String(time.getHours()).padStart(2, '0');
        const minutes = String(time.getMinutes()).padStart(2, '0');
        const seconds = String(time.getSeconds()).padStart(2, '0');
        return `${hours}:${minutes}:${seconds}`;
    } else if (typeof time === 'number') {
        const hours = Math.floor(time / 3600);
        const minutes = Math.floor((time % 3600) / 60);
        const seconds = time % 60;
        return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
    } else {
        return 'Invalid Time';
    }
};

/**
 * Updates the game board with the information received from the server.
 * @param {Array} board - The board received from the server.
 */
const updateBoard = (board) => {
    let counter = 0;
    board.forEach((row, rowIndex) => {
        row.forEach((cell, cellIndex) => {
            const cellElement = document.querySelector(`.row-${rowIndex} .cell-${cellIndex}`);
            cellElement.innerHTML = cell === ' ' ? '<button onclick="makeMove(' + counter + ')"> </button>' : `<span class="cell-item">${cell}</span>`;
            counter++;
        });
    });
}
