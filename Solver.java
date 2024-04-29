import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

 class Board {
    public int[][] tiles;
    public int moves;
    public Board(int[][] tiles)
    {
        this.tiles=tiles;
        moves=0;
    }
    public String toString()
    {
        String s=""+dimension()+"\n";
        for(int i=0;i<tiles.length;i++) {
            for (int j = 0; j < tiles[0].length; j++)
                s=s+tiles[i][j]+" ";
            s+="\n";
        }
        return s;
    }
    public int dimension()
    {
        return this.tiles.length;
    }
    public int hamming()
    {
        int r=0;
        int n=dimension();
        for(int i=0;i<n;i++)
            for(int j=0;j<n;j++)
                if((i!=pos(tiles[i][j],n)[0] || j!=pos(tiles[i][j],n)[1]) && tiles[i][j]!=0)
                    r++;
        return r;
    }
    public int[] pos(int val,int n)
    {
        int r[]=new int[2];
        int a=val%n;
        int b=val/n;
        if(a==0)
        {
            r[0]=b-1;
            r[1]=n-1;
        }
        else
        {
            r[0]=b;
            r[1]=a-1;
        }
        return r;
    }
    public int manhattan()
    {
        int n=dimension();
        int m=0;
        for(int i=0;i<n;i++)
        {
            for(int j=0;j<n;j++)
            {
                if((i!=pos(tiles[i][j],n)[0] || j!=pos(tiles[i][j],n)[1]) && tiles[i][j]!=0)
                {
                    int a=pos(tiles[i][j],n)[0];
                    int b=pos(tiles[i][j],n)[1];
                    m=m+Math.abs(i-a)+Math.abs(j-b);
                }
            }
        }
        return m;
    }
    public boolean isGoal()
    {
        return hamming() == 0;
    }
    public int[] index(int val)
    {
        for(int i=0;i<dimension();i++)
            for(int j=0;j<dimension();j++)
                if(tiles[i][j]==val)
                    return new int[]{i, j};
        return null;
    }
    public int[] neighbourIndex(int i,int j)
    {
        int r[]={-1,-1,-1,-1};
        if(i-1>=0)
            r[0]=i-1;
        if(i+1<dimension())
            r[1]=i+1;
        if(j-1>=0)
            r[2]=j-1;
        if(j+1<dimension())
            r[3]=j+1;
        return r;
    }
}
public class Solver {
    private Board[] pq=new Board[10000];
    private HashMap<String,String> map=new HashMap<>();
    private String[] trace=new String[1000];
    public String output="";
    private int N;

    public void insert(Board key)
    {
        pq[++N]=key;
        swim(N);
    }
    public Board delMin()
    {
        Board min=pq[1];
        exch(1,N--);
        sink(1);
        pq[N+1]=null;
        return min;
    }
    private void swim(int k)
    {
        while(k>1 && less(k,k/2))
        {
            exch(k,k/2);
            k=k/2;
        }
    }
    private void sink(int k)
    {
        while(2*k<=N)
        {
            int j=2*k;
            if(j<N && less(j+1,j))j++;
            if(less(k,j))break;
            exch(k,j);
            k=j;
        }
    }
    public boolean less(int i,int j)
    {
        int priority1=pq[i].moves+pq[i].manhattan();
        int priority2=pq[j].moves+pq[j].manhattan();
        return priority1 < priority2;
    }
    private void exch(int i,int j)
    {
        Board t=pq[i];
        pq[i]=pq[j];
        pq[j]=t;
    }
    public int[][] exchange(int[][] tiles,int i,int j,int k,int l)
    {
        int t=tiles[i][j];
        tiles[i][j]=tiles[k][l];
        tiles[k][l]=t;
        return tiles;
    }
    private Board[] neighbour(Board x)
    {
        Board[] res=new Board[4];
        int t=0;
        int i=x.index(0)[0];
        int j=x.index(0)[1];
        int[] r=x.neighbourIndex(i,j);
        for(int k=0;k<r.length;k++)
        {
            if(r[k]!=-1)
            {
                int[][] tiles=new int[x.dimension()][x.dimension()];
                for(int m=0;m< x.dimension();m++)
                    for(int n=0;n<x.dimension();n++)
                        tiles[m][n]=x.tiles[m][n];
                if(k<2)
                    tiles=exchange(tiles,i,j,r[k],j);
                else
                    tiles=exchange(tiles,i,j,i,r[k]);
                Board y=new Board(tiles);
                res[t++]=y;
            }
        }
        return res;
    }
    public boolean solver(Board initial)
    {
        insert(initial);
        map.put(initial.toString(),null);
        while(true)
        {
            Board deq=delMin();
            if(deq.isGoal())
                return true;
            Board[] x=neighbour(deq);
            for(Board board:x)
            {
                if(board!=null)
                {
                    if(map.get(board.toString())==null)
                    {
                        board.moves=deq.moves+1;
                        insert(board);
                        map.put(board.toString(),deq.toString());
                    }
                }
            }
        }
    }
    private int[][] goalBoard(int n)
    {
        int[][] goal=new int[n][n];
        int val=1;
        for(int i=0;i<n;i++)
            for(int j=0;j<n;j++)
                goal[i][j]=val++;
        goal[n-1][n-1]=0;
        return goal;
    }
    public void printTrace(Board initial)
    {
        int[][] goal=goalBoard(initial.dimension());
        Board g=new Board(goal);
        int n = 0;
        trace[n++]=g.toString();
        String parent=map.get(g.toString());
        while(!parent.equals(initial.toString()))
        {
            trace[n++]=parent;
            parent=map.get(parent);
        }
        trace[n]=parent;
        while(n >=0)
        {
            output+=normal(trace[n--])+" ";
        }
    }
    public String normal(String x)
    {
        x=x.replace("\n","").replace("\r","").replace(" ","");
        return x.substring(1);
    }
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String s= in.next();
        int n=Integer.parseInt(String.valueOf(s.charAt(0)));
        int t=1;
        int[][] tiles = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                tiles[i][j] = Integer.parseInt(String.valueOf(s.charAt(t++)));
            }
        }
        Board b = new Board(tiles);
        Solver sol = new Solver();
        sol.solver(b);
        sol.printTrace(b);
        System.out.print(sol.output);
    }
}
