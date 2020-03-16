# chess-optimization

This app is designed to find the best way to pair players in a [round-robin tournament](https://en.wikipedia.org/wiki/Round-robin_tournament). It maximizes the number of games being played simultaneously. It does this by representing the current state of the tournament as a graph with players as nodes and unplayed games as edges, and uses [Edmonds' Blossom Algorithm](https://en.wikipedia.org/wiki/Blossom_algorithm) to find the [maximum cardinality matching](https://en.wikipedia.org/wiki/Maximum_cardinality_matching) over this graph.

## How to use

![](/screenshots/open.png)

Click "create new" or the name of the file you want to open.

![](/screenshots/name.png)

If creating a new file, enter a name for the file.

![](/screenshots/players.png)

Then enter the names of the participants. Names can have spaces.

![](/screenshots/main_window.png)

The main window displays the current results of the tournament as a grid.

- Yellow indicates a match yet to be played.
- Blue indicates a match in progress.
- Green indicates a match already played.
- Red indicates a match that cannot be played for some reason.
- The "present/absent" buttons mark a player as present or absent. Absent players will not be matched into games.
- The bottom of the window shows the next pair to match in the optimal setup. This pairing is also marked with a dashed blue box.
 - Click "accept" to automatically mark it as in progress.
 - Click "reject" to mark this pairing as impossible.
 - The optimal pairing should automatically update. If it does not, click "update".

![](/screenshots/manual_entry.png)

Click on a square to manually mark a game being played, or the result of a game.

![](/screenshots/file_format.png)

The data is saved in a file within the same folder as the program with name `tournmament_NAME.txt`.
