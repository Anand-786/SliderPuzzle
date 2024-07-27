//Importing packages
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

//For each Board created a new Class Board object is created
class Board {
    public int[][] tiles; //2d array representing the board
    public int moves;
    public Board(int[][] tiles) {
        this.tiles = tiles;
        moves = 0; //initially moves will be 0
    }

    //Converts the board to string for easier usage
    public String toString() {
        String s = "" + dimension() + "\n";
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++)
                s = s + tiles[i][j] + " ";
            s += "\n";
        }
        return s;
    }
    public int dimension() {
        return this.tiles.length;
    }

    //calculates the no of tiles out of place(Hamming distance)
    public int hamming() {
        int r = 0;
        int n = dimension();
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if ((i != pos(tiles[i][j], n)[0] || j != pos(tiles[i][j], n)[1]) && tiles[i][j] != 0)
                    r++;
        return r;
    }

    //return the approphiate index of a tile acc to its number in the goal board
    public int[] pos(int val, int n) {
        int r[] = new int[2];
        int a = val % n;
        int b = val / n;
        if (a == 0) {
            r[0] = b - 1;
            r[1] = n - 1;
        } else {
            r[0] = b;
            r[1] = a - 1;
        }
        return r;
    }

    //calculates the sum of distances of each tile from its pos in the goal board
    public int manhattan() {
        int n = dimension();
        int m = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if ((i != pos(tiles[i][j], n)[0] || j != pos(tiles[i][j], n)[1]) && tiles[i][j] != 0) {
                    int a = pos(tiles[i][j], n)[0];
                    int b = pos(tiles[i][j], n)[1];
                    m = m + Math.abs(i - a) + Math.abs(j - b);
                }
            }
        }
        return m;
    }

    /*This function checks whether the given board is solvable or not. A 3x3 board
    is solvable only if the no of inversions in the 2d array are even in number.
    This is because the row movement does not affect an already existing inversion
    and the column movement can only do one of 3 things-
    increase the no of inversions by 2, decrease the no of inversions by 2 or
    does not change it at all.
    Hence, it can be proven that since Goal Board has 0 inversions we must initially
    also have an even no of inversions.*/
    public boolean solvable(int n) {
        int inversions = 0;
        int zeroRow = -1;
        for (int i = 0; i < (n * n); i++) {
            int x = (int) Math.floor(i / n);
            int y = (int) Math.floor(i % n);
            int val = tiles[x][y];
            if (val == 0) {
                zeroRow = (int) Math.floor(i / n);
            }
            int j = i;
            while (j < (n * n)) {
                x = (int) Math.floor(j / n);
                y = (int) Math.floor(j % n);
                int pair = tiles[x][y];
                if (pair != 0 && pair < val) {
                    inversions++;
                }
                j++;
            }
        }
        if (n % 2 == 1 && inversions % 2 == 1) {
            return false;
        }
        if (n % 2 == 0 && (inversions + zeroRow) % 2 == 0) {
            return false;
        }
        return true;
    }

    //Checks if we have reached the goal board
    public boolean isGoal() {
        return hamming() == 0;
    }
    public int[] index(int val) {
        for (int i = 0; i < dimension(); i++)
            for (int j = 0; j < dimension(); j++)
                if (tiles[i][j] == val)
                    return new int[] {
                        i,
                        j
                    };
        return null;
    }

    //Calculates the indexes of neighbors
    public int[] neighbourIndex(int i, int j) {
        int r[] = {-1,
            -1,
            -1,
            -1
        };
        if (i - 1 >= 0)
            r[0] = i - 1;
        if (i + 1 < dimension())
            r[1] = i + 1;
        if (j - 1 >= 0)
            r[2] = j - 1;
        if (j + 1 < dimension())
            r[3] = j + 1;
        return r;
    }
}

//Class with main function
public class Solver {
    //Priority Queue(Min Heap) data structure is used
    private Board[] pq = new Board[10000];
    //Hash Map for making sure we don't have repeated boards(optimization)
    private HashMap < String, String > map = new HashMap < > ();
    //To trace the path of the tree where we have reached the solution in min steps
    private String[] trace = new String[1000];
    //To store the result
    public String output = "";
    private int N;

    //PQ insert function
    public void insert(Board key) {
        pq[++N] = key;
        swim(N);
    }

    //PQ delete function
    public Board delMin() {
        Board min = pq[1];
        exch(1, N--);
        sink(1);
        pq[N + 1] = null;
        return min;
    }

    //Helper function for inserting. Takes O(logn) time
    private void swim(int k) {
        while (k > 1 && less(k, k / 2)) {
            exch(k, k / 2);
            k = k / 2;
        }
    }

    //Helper function for deleting. Takes O(logn) time
    private void sink(int k) {
        while (2 * k <= N) {
            int j = 2 * k;
            if (j < N && less(j + 1, j)) j++;
            if (less(k, j)) break;
            exch(k, j);
            k = j;
        }
    }

    //Compares the priority according to the sum of both moves and the manhattan no.
    public boolean less(int i, int j) {
        int priority1 = pq[i].moves + pq[i].manhattan();
        int priority2 = pq[j].moves + pq[j].manhattan();
        return priority1 < priority2;
    }
    private void exch(int i, int j) {
        Board t = pq[i];
        pq[i] = pq[j];
        pq[j] = t;
    }
    public int[][] exchange(int[][] tiles, int i, int j, int k, int l) {
        int t = tiles[i][j];
        tiles[i][j] = tiles[k][l];
        tiles[k][l] = t;
        return tiles;
    }

    //This function returns the neighbor boards of the board which is deleted into the PQ
    private Board[] neighbour(Board x) {
        Board[] res = new Board[4];
        int t = 0;
        int i = x.index(0)[0];
        int j = x.index(0)[1];
        int[] r = x.neighbourIndex(i, j);
        for (int k = 0; k < r.length; k++) {
            if (r[k] != -1) {
                int[][] tiles = new int[x.dimension()][x.dimension()];
                for (int m = 0; m < x.dimension(); m++)
                    for (int n = 0; n < x.dimension(); n++)
                        tiles[m][n] = x.tiles[m][n];
                if (k < 2)
                    tiles = exchange(tiles, i, j, r[k], j);
                else
                    tiles = exchange(tiles, i, j, i, r[k]);
                Board y = new Board(tiles);
                res[t++] = y;
            }
        }
        return res;
    }

    //Main function where solution begins.
    //Deletes the board with min priority. Then adds its neighbors into the PQ
    //(makes sure not to add the parent board again). Then repeats the process until
    // we reach the goal board.
    public boolean solver(Board initial) {
        insert(initial);
        map.put(initial.toString(), null);
        while (true) {
            Board deq = delMin();
            if (deq.isGoal())
                return true;
            Board[] x = neighbour(deq);
            for (Board board: x) {
                if (board != null) {
                    if (map.get(board.toString()) == null) {
                        board.moves = deq.moves + 1;
                        insert(board);
                        map.put(board.toString(), deq.toString());
                    }
                }
            }
        }
    }
    private int[][] goalBoard(int n) {
        int[][] goal = new int[n][n];
        int val = 1;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                goal[i][j] = val++;
        goal[n - 1][n - 1] = 0;
        return goal;
    }

    //This function traces the path of the solution back to the initial board to print as a result.
    public int printTrace(Board initial) {
        int[][] goal = goalBoard(initial.dimension());
        Board g = new Board(goal);
        int n = 0;
        trace[n++] = g.toString();
        String parent = map.get(g.toString());
        while (!parent.equals(initial.toString())) {
            trace[n++] = parent;
            parent = map.get(parent);
        }
        trace[n] = parent;
        int n1 = n;
        while (n >= 0) {
            output += "\n" + (trace[n--]) + "\n";
        }
        return n1;
    }
    public String normal(String x) {
        x = x.replace("\n", "").replace("\r", "").replace(" ", "");
        return x.substring(1);
    }

    /*Main function which makes sure to check if provided board is solvable otherwise
    display approphiate message*/
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in .nextInt();
        String s = in .next();
        int t = 0;
        int[][] tiles = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                tiles[i][j] = Integer.parseInt(String.valueOf(s.charAt(t++)));
            }
        }
        Board b = new Board(tiles);
        if (!b.solvable(n)) {
            System.out.println("Unsolvable Puzzle");
            System.exit(0);
        }
        Solver sol = new Solver();
        sol.solver(b);
        int minm = sol.printTrace(b);
        System.out.println("Minimum no. of moves required = " + minm);
        System.out.print(sol.output);
    }
}
