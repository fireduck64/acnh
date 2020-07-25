package fireduck.acnh;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

// Since we don't really care how fast things happen, we can ignore the visitor status
// We can also ignore the flower stages, since new growth is in stage 3, meaning able to breed
// next day

public class FlowerSim
{
  public static void main(String args[]) throws Exception
  {
    new FlowerSim();
  }

  private double best_p=0.0;
  private Object check_lock=new Object();

  private static int KEEP_BEST = 20;

  private TreeMap<Double, Map2D<Square> > best_maps = new TreeMap<>();

  public FlowerSim() throws Exception
  {

    for(int i=4; i<=10; i++)
    {
      addBestMap(0.001, makeBoringMap(i));
    }

    for(int i=0; i<32; i++)
    {
      new FindThread().start();
    }
  }

  static Flower rose_red = new Flower(1, "2021",'R');
  static Flower rose_yellow = new Flower(1, "0220",'Y');
  static   Flower rose_white = new Flower(1, "0010",'W');
  static   Flower island_orange = new Flower(1,"2211",'O');

  ArrayList<Flower> seeds = new ArrayList<>();

  {
  seeds.add(rose_red);
  seeds.add(rose_yellow);
  seeds.add(rose_white);
  }


  public class FindThread extends Thread
  {
    public void run()
    {
      findBlue();
    }
  }
  public Map2D<Square> makeBoringMap(int sz)
  {
    Random rnd = new Random();
      Map2D<Square> map = new Map2D<Square>(new Square(false));

      int size_x = sz;
      int size_y = sz;

      for(int i=0; i<size_x; i++)
      for(int j=0; j<size_y; j++)
      {
        map.set(i,j,new Square(true));
      }

      int A = 0;
      int B = 1;
      int C = 2;
      int D = size_x - 3;
      int E = size_x - 2;
      int F = size_x - 1;

      map.set(A,A,new Square(true,rose_red.cloneF() ));
      map.set(A,B,new Square(true,rose_yellow.cloneF() ));
      map.set(A,C,new Square(true,rose_white.cloneF() ));

      map.set(F,D,new Square(true,rose_red.cloneF() ));
      map.set(F,E,new Square(true,rose_yellow.cloneF() ));
      map.set(F,F,new Square(true,rose_white.cloneF() ));

      map.set(A,F,new Square(true,rose_red.cloneF() ));
      map.set(B,F,new Square(true,rose_yellow.cloneF() ));
      map.set(C,F,new Square(true,rose_white.cloneF() ));

      map.set(F,A,new Square(true,rose_red.cloneF() ));
      map.set(E,A,new Square(true,rose_yellow.cloneF() ));
      map.set(D,A,new Square(true,rose_white.cloneF() ));



      markUnreachable(map);

      return map;
  }


  public Map2D<Square> makeMap()
  {
    Random rnd = new Random();
      Map2D<Square> map = new Map2D<Square>(new Square(false));

      int size_x = 4 + rnd.nextInt(7);
      int size_y = 4 + rnd.nextInt(7);

      for(int i=0; i<size_x; i++)
      for(int j=0; j<size_y; j++)
      {
        map.set(i,j,new Square(true));
      }

      int place = rnd.nextInt(15) + 3;
      for(int i =0; i<place;i++)
      {
        Flower f = seeds.get(rnd.nextInt(seeds.size())).cloneF();
        int x = rnd.nextInt(size_x);
        int y = rnd.nextInt(size_y);
        map.set(x,y,new Square(true, f));

      }

      int blockers = rnd.nextInt(10);
      for(int i=0; i<blockers; i++)
      {
        
        int x = rnd.nextInt(size_x);
        int y = rnd.nextInt(size_y);
        if (map.get(x,y).growable())
        {
          map.set(x,y,new Square(false));
        }
      }
      markUnreachable(map);

      return map;
  }

  public void mutateMap(Map2D<Square> map)
  {
    Random rnd = new Random();
    int size_x = (int)map.getHighX();
    int size_y = (int)map.getHighY();

    int clear_space = rnd.nextInt(2);
    int place_flower = rnd.nextInt(2);
    int add_blockers = rnd.nextInt(2);

    ArrayList<Point> points = new ArrayList<>();
    points.addAll(map.getAllPoints());

    Collections.shuffle(points);
    for(Point p : points)
    {
      Square s = map.get(p.x,p.y);
      if ((clear_space > 0) && (!s.growable()))
      {
        map.set(p.x, p.y, new Square(true));
        clear_space--;
      }
    }

    for(int i =0; i<place_flower; i++)
    {
      Flower f = seeds.get(rnd.nextInt(seeds.size())).cloneF();
      int x = rnd.nextInt(size_x);
      int y = rnd.nextInt(size_y);
      map.set(x,y,new Square(true, f));
    }

    for(int i=0; i<add_blockers; i++)
    {
      int x = rnd.nextInt(size_x);
      int y = rnd.nextInt(size_y);
      if (map.get(x,y).growable())
      {
        map.set(x,y,new Square(false));
      }
    }

    markUnreachable(map);
    
  }

  public void findBlue()
  {
    Random rnd = new Random();
    while(true)
    {

      Map2D<Square> map = null;

      if (rnd.nextDouble() < 0.9)
      {
        map = getGoodMap();
        if (map != null)
        { 
          map = cloneMap(map);
          mutateMap(map);
        }
      }
      if (map == null)
      {
        map = makeMap();
      }

      double p = doTrials(map,200);

      if (addBestMap(p, map))
      {
        synchronized(check_lock)
        {
            System.out.println();
            System.out.println(p);
            System.out.println(map.getPrintOut());

            //best_p = p;
        }
      }

    }

  }

  public static double doTrials(Map2D<Square> map, int trials)
  {
    int success=0;
    for(int i=0; i<trials; i++)
    {
      Map2D<Square> copy = cloneMap(map);
      if (simulateUntilDone(copy))
      {
        success++;
      }

    }

    double s = success;
    double t = trials;

    return s/t;
  }

  public double getWorstBest()
  {
    synchronized(best_maps)
    {
      if (best_maps.size() >= KEEP_BEST)
      {
        return best_maps.firstKey();
      }
    }
    return 0.0;
  }
  public Map2D<Square> getGoodMap()
  {
    synchronized(best_maps)
    {
      if (best_maps.size() > 0)
      {
        ArrayList<Map2D<Square> > maps = new ArrayList<>();
        maps.addAll(best_maps.values());
        Random rnd = new Random();

        return maps.get(rnd.nextInt(maps.size()));
      }
    }

    return null;

  }
  public boolean addBestMap(double score, Map2D<Square> map)
  {
    synchronized(best_maps)
    {
      if (score > getWorstBest())
      {
        Random rnd = new Random();
        best_maps.put(score + rnd.nextDouble() / 1e6, map);
        maintainBest();
        return true;
      }

    }
    return false;

  }

  public void maintainBest()
  {
    synchronized(best_maps)
    {
      while(best_maps.size() > KEEP_BEST)
      {
        best_maps.pollFirstEntry();
      }
    }

  }


  public static boolean simulateUntilDone(Map2D<Square> map)
  {
    while(true)
    {
      int free = simulateDay(map);
      if (free < 0) return true;
      if (free == 0) return false;
    }

  }

  public static Map2D<Square> cloneMap(Map2D<Square> source)
  {
    if (source==null)throw new RuntimeException("source null");
    Map2D<Square> map = new Map2D<Square>(source.getDefault());

    for(Point p : source.getAllPoints())
    {
      Square s = source.get(p.x, p.y);

      if (s.flower != null)
      {
        s = new Square(true, s.flower.cloneF());
      }
      map.set(p.x,p.y, s);

    }
    return map;
    
  }

  public static void markUnreachable(Map2D<Square> map)
  {
      HashSet<Point> marked = new HashSet<Point>();

      LinkedList<Point> next=new LinkedList<>();

      for(Point p : map.getAllPoints())
      {
        if (map.get(p.x, p.y).flower != null)
        {
          next.add(p);
        }
      }

      while(!next.isEmpty())
      {
        Point p = next.pop();
        if (!marked.contains(p))
        {
          marked.add(p);

          Square s = map.get(p.x,p.y);
          if (s.dirt)
          {
            next.addAll(getRndAdj(p));
          }
        }
      }

      int m=0;
      for(Point p : map.getAllPoints())
      {
        if (map.get(p.x, p.y).growable() && (!marked.contains(p)))
        {
          m++;
          map.set(p.x,p.y,new Square(false));
        }
      }
      if (m > 0)
      {
        //System.out.println(map.getPrintOut());
      }


  

  }

  /**
   * returns number of free dirt squares
   */
  public static int simulateDay(Map2D<Square> map)
  {
    Random rnd = new Random();
    List<Point> all_points = map.getAllPoints();

    Collections.shuffle(all_points);
    int free = 0;

    for(Point p : all_points)
    {
      Square s = map.get(p.x, p.y);
      if (s.flower != null)
      {
        Flower f = s.flower;
        f.breedable = true;
      }
      if (s.growable()) free++;
    }
    for(Point p : all_points)
    {
      Square s = map.get(p.x, p.y);
      if (s.flower != null)
      {
        Flower f = s.flower;
        if (f.breedable)
        {
          f.water_counter = Math.min(20, f.water_counter+1);
          
          double p2 = f.water_counter - 2 * 0.05;
          double prob = Math.max(0.05, Math.min(0.9, (f.water_counter - 2) * 0.05));
          if (rnd.nextDouble() < prob)
          {
            List<Point> adj = getRndAdj(p);
            Point target = null;
            for(Point q : adj)
            {
              Square t = map.get(q.x, q.y);
              if (t.growable())
              {
                target = q; break;
              }
            }
            if (target != null)
            {
              adj = getRndAdj(p);
              Flower partner = null;
              for(Point q : adj)
              {
                Square t =  map.get(q.x, q.y);
                if (t.hasFlower(f.breed))
                {
                  partner = t.flower;
                  break;
                }
              }

              Flower new_flower = null;
              if (partner!=null)
              {
                new_flower = Flower.breed(f, partner, rnd);
                partner.water_counter=0;
                partner.breedable=false;
              }
              else
              {
                new_flower = f.cloneF();
              }

              f.water_counter=0;
              f.breedable=false;

              
              map.set(target.x, target.y, new Square(true, new_flower));
              free--;

              if (new_flower.isBlueRose())
              {
                return -1;
              }
            }
          }
        }

      }
    }
    return free;
  }

  public static List<Point> getRndAdj(Point p)
  {
    List<Point> lst = new LinkedList<>();

    for(int i=-1; i<=1; i++)
    for(int j=-1; j<=1; j++)
    {
      if ((i!=0) || (j!=0))
      {
        Point q = new Point(p.x +i, p.y + j);
        lst.add(q);
      }

    }

    Collections.shuffle(lst);
    return lst;
  }
}
