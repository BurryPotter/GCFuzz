import java.text.*;
import java.util.Random;

class TreeNode {
  public TreeNode left, right;
  public Integer val;                // will always be the height of the tree
  public static int nodes = 0;     // total tree nodes allocated
}

class LinkedList {
  public LinkedList next;
  public int length;
}

public class LongList {
  private static final int FILLER=0;
  private static int listLenth, treeHeight;
  private static final String usage
    = "Usage: java LongList <list_length> <tree_height>";
  private static final String list_length_string
    = "  where <list_length> is the length of the linked list";
  private static final String tree_height_string
    = "        <tree_height> is the height of the trees allocated before and after the linked list";
  private static int listLength;
  private static LinkedList list;
  private static LinkedList makeList(int listLength) {
    LinkedList return_val = null;
    if (listLength == 0) {
      return null;
    } else {
      LinkedList next, cur;
      return_val = new LinkedList();
      cur = return_val;
      for (int n = 0; n < listLength; n++) {
	String[] fillers = new String[FILLER];
	for (int k = 0; k < FILLER; k++) {
	  fillers[k] = new String("string " + n);
	}
        next = new LinkedList();
        cur.next = next;
        cur = next;
      }
    }
    return return_val;
  }
  public static TreeNode makeTree(int h) {
    if (h == 0) return null;
    else {
      TreeNode res = new TreeNode();
      res.nodes++;
      res.val = res.nodes;
      res.left = makeTree(h-1);
      res.right = makeTree(h-1);
      res.val = h;
      return res;
    }
  }
  public static void main(String[] args) {
    System.err.println(usage);
    System.err.println(list_length_string);
    System.err.println(tree_height_string);
    if (args.length != 2) {
      return;
    }
    listLength = Integer.parseInt(args[0]);
    treeHeight = Integer.parseInt(args[1]);

    System.out.println("  " + listLength + " list length");
    System.out.println("  " + treeHeight + " tree height");

    TreeNode tree1 = makeTree(treeHeight);
    System.out.println("### Tree built");
    tree1 = null;
    System.out.println("### Tree felled");
    LinkedList list = makeList(listLength);
    System.out.println("### List built");
    System.out.println("### Done");
  }
}
