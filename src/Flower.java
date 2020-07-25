
package fireduck.acnh;

import java.util.ArrayList;
import java.util.Random;

public class Flower
{
  final int breed;
  final String genes; // expressed as numbers of each allele.  Example, red seed rose is: "2021"
  final char label;
  
  boolean breedable = false;
  int water_counter = 0;

  public Flower(int breed, String genes, char label)
  {
    this.breed = breed;
    this.genes = genes;
    this.label = label;
  }

  public Flower(int breed, String genes)
  {
    this.breed = breed;
    this.genes = genes;
    if (isBlueRose())
    {
      this.label = 'B';
    }
    else
    {
      this.label = 'F';
    }
  }

  @Override
  public String toString()
  {
    return String.format("flower(%d,%s)", breed, genes);
  }

  public char toChar()
  {
    return label;
  }

  public Flower cloneF()
  {
    return new Flower(breed, genes,label);
  }

  public static Flower breed(Flower a, Flower b, Random rnd)
  {
    String new_genes = "";
    for(int x = 0; x<a.genes.length(); x++)
    {
      int allele_count = 0;
      allele_count += addAllele(rnd, a.genes, x);
      allele_count += addAllele(rnd, b.genes, x);

      new_genes += allele_count;
    

    }
    return new Flower(a.breed, new_genes);
    
  }

  private static int addAllele(Random rnd, String genes, int idx)
  {
    char x = genes.charAt(idx);
    if (x=='2') return 1;
    if (x=='0') return 0;
    
    return rnd.nextInt(2);

  }

  public boolean isBlueRose()
  {
    return ((breed==1) && (genes.equals("2200")));
  }

}
