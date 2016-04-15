 package others;

 import java.util.ArrayList;
 import java.util.List;

 import models.Round;
 import models.Trick;

 /**
 * Round with its tricks
 *
 * @author mahmoudsakr
 */
 public class RoundTricks extends ArrayList<Trick> {
 Round round;

 public RoundTricks (Round r, List<Trick> tricksInRound) {
 round = r;
 addAll(tricksInRound);
 }
 }