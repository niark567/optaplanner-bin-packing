package snurkabill.net;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import snurkabill.net.domain.Bin;
import snurkabill.net.domain.BinPacking;
import snurkabill.net.domain.Brick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BinPackingGenerator {

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

	private static int estimateRequiredBins(int totalRequiredLength, int binVolume) {
		int bins = (int) Math.ceil(totalRequiredLength * 1.25 / binVolume);

//		// 1 Master Pack min.
//		if (masterPacks.containsKey(binVolume)) {
//			bins = Math.max(bins, masterPacks.get(binVolume));
//		}

		return bins;

	}

	private static final Map<Integer, Integer> requiredPieces = new HashMap<>();
	static {
		addBrick(requiredPieces, 1820, 1);
		addBrick(requiredPieces, 1780, 1);
		addBrick(requiredPieces, 1640, 1);
		addBrick(requiredPieces, 970, 4);
		addBrick(requiredPieces, 900, 5);
		addBrick(requiredPieces, 850, 3);
		addBrick(requiredPieces, 750, 5);
		addBrick(requiredPieces, 640, 3);
		addBrick(requiredPieces, 590, 1);
		addBrick(requiredPieces, 570, 4);
		addBrick(requiredPieces, 560, 3);
		addBrick(requiredPieces, 430, 8);
		addBrick(requiredPieces, 380, 1);
		addBrick(requiredPieces, 350, 7);
		addBrick(requiredPieces, 270, 1);
		addBrick(requiredPieces, 210, 8);
		addBrick(requiredPieces, 180, 2);
	}

	private static final <K> void addBrick(Map<K, Integer> bricks, K brickLength, Integer brickCount) {
		if (bricks == null) {
			throw new IllegalStateException("No map provided to add pieces");
		}
		if (brickCount <= 0) {
			throw new IllegalStateException("No brick to add");
		}

		Integer currentBrickCount = bricks.getOrDefault(brickLength, 0);
		bricks.put(brickLength, currentBrickCount + brickCount);
	}

	private static final Map<Integer, Integer> manualInitialPieces = new HashMap<>();
	static {
		manualInitialPieces.put(6000, 6);
		manualInitialPieces.put(1980, 1);
		manualInitialPieces.put(1500, 1);
		manualInitialPieces.put(1000, 1);
//		manualInitialPieces.put(500, 1);
	}
//	private static final Map<Integer, Integer> requiredPieces = new HashMap<>();
//	static {
//		requiredPieces.put(1820, 2);
//		requiredPieces.put(570, 3);
//		requiredPieces.put(380, 1);
//	}
//
//	private static final Map<Integer, Integer> manualInitialPieces = new HashMap<>();
//	static {
//		manualInitialPieces.put(6000, 1);
//	}

	private static Map<Integer, Integer> getInitialPieces(List<Brick> brickList) {
		// TODO Auto-generated method stub
		return manualInitialPieces;
	}

	public static BinPacking simpleSolution() {
		BinPacking binPacking = new BinPacking();
		String string = String.valueOf(Integer.MIN_VALUE) + "hard";
		string += "/";
		string += String.valueOf(Integer.MIN_VALUE) + "soft";
		binPacking.setScore(HardSoftScore.parseScore(string));

		// Bricks
		List<Brick> brickList = new ArrayList<Brick>();

		requiredPieces.entrySet().forEach(e -> {
			for (int i = 0; i < e.getValue(); i++) {
				Brick brick = new Brick();
				brick.setVolume(e.getKey());
				brickList.add(brick);
			}
		});

		binPacking.setBrickList(brickList);

//		int totalRequiredLength = brickList.stream().mapToInt(Brick::getVolume).sum();

		// Bins
		List<Bin> binList = new ArrayList<Bin>();

		getInitialPieces(brickList).entrySet().forEach(e -> {
			for (int i = 0; i < e.getValue(); i++) {
				Bin bin = new Bin();
				bin.setVolume(e.getKey());
				binList.add(bin);
			}
		});

		binPacking.setBinList(binList);
		return binPacking;
	}

//	public static BinPacking generateSolution(int binNum, int bricksNum, int seed) {
//
//		Random random = new Random(seed);
//
//		int binVolumeRange = 900;
//		int binVolumeBaseline = 500;
//
//		int brickVolumeRange = 690;
//		int brickVolumeBaseline = 10;
//
//		BinPacking binPacking = new BinPacking();
//		String string = String.valueOf(Integer.MIN_VALUE) + "hard";
//		string += "/";
//		string += String.valueOf(Integer.MIN_VALUE) + "soft";
//		binPacking.setScore(HardSoftScore.parseScore(string));
//
//		List<Bin> binList = new ArrayList<Bin>();
//		for (int i = 0; i < binNum; i++) {
//			Bin bin = new Bin();
//			bin.setVolume(random.nextInt(binVolumeRange) + binVolumeBaseline);
//			binList.add(bin);
//		}
//
//		List<Brick> brickList = new ArrayList<Brick>();
//		for (int i = 0; i < bricksNum; i++) {
//			Brick brick = new Brick();
//			brick.setVolume(random.nextInt(brickVolumeRange) + brickVolumeBaseline);
//			brickList.add(brick);
//		}
//		binPacking.setBinList(binList);
//		binPacking.setBrickList(brickList);
//		return binPacking;
//	}
}
