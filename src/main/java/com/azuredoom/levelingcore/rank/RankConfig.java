package com.azuredoom.levelingcore.rank;

import java.util.List;

public class RankConfig {

    public List<RankEntry> ranks;

    public static class RankEntry {
        public String id;
        public String name;
        public int minLevel;
        public int maxLevel;
    }
}
