package snurkabill.net;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import snurkabill.net.domain.Bin;
import snurkabill.net.domain.BinPacking;
import snurkabill.net.domain.Brick;

public class ScoreCalcul implements EasyScoreCalculator<BinPacking> {

	private static final double masterPackUnitPrice = 4.20f;
	private static final double standardUnitPrice = 4.33f;
	private static final double cutUnitPrice = 4.46f;

	public static final int STARTING_CUT_POSITION = 3; /* for our pusher to find his starting position in mm */
	public static final int CUT_LOSS = 4; /* cut width in mm */
	public static final int END_LOSS = 40; /* safety minimal rest in mm */

	private static final Set<Integer> cutLengths = new HashSet<Integer>();
	static {
		cutLengths.add(500);
		cutLengths.add(1000);
		cutLengths.add(1500);
		cutLengths.add(1980);
		cutLengths.add(6000);
	}

	private static final Set<Integer> standardLengths = new HashSet<Integer>();
	static {
		standardLengths.add(195);
		standardLengths.add(405);
		standardLengths.add(415);
		standardLengths.add(500);
		standardLengths.add(1000);
		standardLengths.add(1500);
		standardLengths.add(1980);
	}

	private static final Map<Integer, Integer> masterPacks = new HashMap<>();
	static {
		masterPacks.put(1000, 12);
		masterPacks.put(1500, 12);
		masterPacks.put(195, 20);
		masterPacks.put(1980, 12);
		masterPacks.put(405, 20);
		masterPacks.put(415, 20);
		masterPacks.put(500, 12);
	}

	public static int getUsedVolume(List<Brick> bricks) {
		return bricks.stream()//
				.collect(Collectors.summingInt(Brick::getVolume)).intValue() //
				+ STARTING_CUT_POSITION //
				+ CUT_LOSS * bricks.size() //
				+ END_LOSS;
	}

	public static int getScrapVolume(Bin bin, List<Brick> bricks) {
		return getScrapVolume(bin, getUsedVolume(bricks), bricks);
	}

	public static int getScrapVolume(Bin bin, int usedVolume, List<Brick> bricks) {
		return bin.getVolume() - usedVolume;
	}

	public Score calculateScore(BinPacking binPacking) {
		int hardScore = 0;
		int softScore = 0;
		List<Brick> bricks;
		for (Bin bin : binPacking.getBinList()) {
			bricks = binPacking.getBrickList().stream().filter(brick -> bin.equals(brick.getBin()))
					.collect(Collectors.toList());

			int usage = getUsedVolume(bricks);
			int remainingSpace = getScrapVolume(bin, usage, bricks);

			if (remainingSpace < 0) {
				hardScore += remainingSpace;
			} else {
				softScore -= remainingSpace;
				// additional malus when scrap is too small to be reused
				if (remainingSpace < 250) {
					softScore -= 250;
				}
			}
		}

		// TODO
//		softScore += (int) Math.ceil(calculatePrice(binPacking));

		return HardSoftScore.valueOf(hardScore, softScore);
	}

	public static double calculatePrice(Set<Entry<Bin, List<Brick>>> bins) {
		double price = 0;

		Map<Integer, Long> groupedBins = bins.stream()
				.collect(Collectors.groupingBy(e -> e.getKey().getVolume(), Collectors.counting()));

//		List<MasterPack> masterPacks = findMasterPacks(bins);

		// TODO try standard lengths, master packs and cut
		for (Entry<Integer, Long> entry : groupedBins.entrySet()) {
//			if (masterPacks.get(entry.getKey()) >= entry.getValue()) {
//
//			}
			if (cutLengths.contains(entry.getKey())) {
				price += entry.getValue() * (entry.getKey() / 1000f) * cutUnitPrice;
//			} else if (standardLengths.contains(entry.getKey())) {
//				price += entry.getValue() * (entry.getKey() / 1000f) * standardUnitPrice;
			} else {
				System.out
						.println("COULD NOT ESTIMATE PRICE FOR " + entry.getValue() + "(" + entry.getKey() + " units");
			}
		}

		return price;
	}

	public static double calculateApproxPrice(Bin bin) {
		return calculateApproxPrice(bin.getVolume());
	}

	public static double calculateApproxPrice(Integer length) {
		return (length / 1000f) * cutUnitPrice;
	}

	public static double calculatePrice(List<Integer> bins) {
		return (bins.stream().mapToInt(i -> i).sum() / 1000f) * standardUnitPrice;
	}

	public static double calculateStandardPrice(Bin bin) {
		return (bin.getVolume() / 1000f) * standardUnitPrice;
	}

//	private float findMasterPacks(BinPacking binPacking) {
//	}

}
