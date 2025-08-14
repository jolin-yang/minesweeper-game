# Minesweeper Game 

## Features:
🚩 **Flagging**: Right-click cells to flag cells you suspect to be mines.   
#️⃣ **Neighboring mine count**: When a non-mine cell is clicked, reveals the number of mines neighboring that cell.   
🌊 **Floodfill effect**: Recursively reveals all adjacent non-mine cells in a cascading flood-like manner when a non-mine cell with zero neighboring mines is clicked, uncovering an entire region of connected non-mine cells until it reaches a cell with a non-zero neighboring mine count.   
📊 **Stats panel**: Bottom panel displaying player stats, including length of the current game, number of remaining mines to be uncovered, number of cells that have been uncovered, number of remaining (non-mine) cells  to be uncovered.   
🔄 **Restart**: Restarts the game with a new layout of mines when the return key is clicked after the current game is won/loss.   



##  Instructions:
***Game Objective***: Uncover all non-mine cells in the grid **WITHOUT** clicking on/"setting off" any mines.    

Each cell in the grid represents either a mine or non-mine (safe) cell, with mines randomly distributed across the grid. 

At the beginning of the game, the first cell the player clicks on is always guaranteed to be a non-mine cell. It is impossible for the player to immediately lose after their first click per the design of the game.  

Click on a non-mine cell to see the number of mines adjacent to it - and make sure you don't click on any mine, otherwise the mine will be set off and the game will be over. Based on the neighboring mine count values, you can figure out which cells are mines and which are safe non-mine cells. 

Flag cells you suspect of being mines by right-clicking them. 

Using this information, you can determine cells that are safe, and cells that contain mines. 

- Use the source code to customize the grid dimensions and number of mines (default settings are 16 × 30 with 99 mines):     
Grid must have 9–35 columns and up to 17 rows.   
  
## Code Access
As per course policy, the game's source code cannot be shared publicly, but can be provided **upon request**. 
