package fireduck.acnh;

public class Square
{

  public final Flower flower;
  public final boolean dirt;

  public Square(boolean dirt)
  {
    this.dirt = dirt;
    this.flower = null;
  }

  public Square(boolean dirt, Flower f)
  {
    this.dirt=dirt;
    this.flower = f;
  }

  public boolean growable()
  {
    if (!dirt) return false;
    if (flower!=null) return false;

    return true;
  }

  public boolean hasFlower(int breed)
  {
    if (flower == null) return false;
    if (flower.breed == breed) return true;

    return false;
  }

  @Override
  public String toString()
  {
    if (growable()) return ".";
    if (flower !=null) return "" + flower.toChar();

    return "#";
  }
}
