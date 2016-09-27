package com.feed_the_beast.ftbu;

import com.feed_the_beast.ftbl.FTBLibStats;
import com.feed_the_beast.ftbl.api.IForgePlayer;
import com.feed_the_beast.ftbl.lib.LangKey;
import com.feed_the_beast.ftbl.lib.gui.GuiLang;
import com.feed_the_beast.ftbl.lib.math.MathHelperLM;
import com.feed_the_beast.ftbl.lib.util.LMStringUtils;
import com.feed_the_beast.ftbu.api.leaderboard.ILeaderboard;
import com.feed_the_beast.ftbu.api.leaderboard.Leaderboard;
import com.feed_the_beast.ftbu.api.leaderboard.LeaderboardInst;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatBasic;
import net.minecraft.stats.StatList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;

/**
 * Created by LatvianModder on 28.06.2016.
 */
public class FTBULeaderboards
{
    public static final LangKey LANG_TOP_TITLE = new LangKey("ftbu.top.title");
    public static final StatBase DEATHS_PER_HOUR = (new StatBasic("ftbu.stat.dph", new TextComponentTranslation("ftbu.stat.dph")));

    @Leaderboard
    public static final ILeaderboard DEATHS = new LeaderboardInst(StatList.DEATHS, (o1, o2) -> Integer.compare(o2.stats().readStat(StatList.DEATHS), o1.stats().readStat(StatList.DEATHS)))
    {
        @Nullable
        @Override
        public Object getData(IForgePlayer player)
        {
            return Integer.toString(player.stats().readStat(StatList.DEATHS));
        }
    };

    @Leaderboard
    public static final ILeaderboard MOB_KILLS = new LeaderboardInst(StatList.MOB_KILLS, (o1, o2) -> Integer.compare(o2.stats().readStat(StatList.MOB_KILLS), o1.stats().readStat(StatList.MOB_KILLS)))
    {
        @Nullable
        @Override
        public Object getData(IForgePlayer player)
        {
            return Integer.toString(player.stats().readStat(StatList.MOB_KILLS));
        }
    };

    @Leaderboard
    public static final ILeaderboard DEATHS_PER_HOUR_LB = new LeaderboardInst(DEATHS_PER_HOUR, (o1, o2) -> Double.compare(FTBLibStats.getDeathsPerHour(o2.stats()), FTBLibStats.getDeathsPerHour(o1.stats())))
    {
        @Nullable
        @Override
        public Object getData(IForgePlayer player)
        {
            return MathHelperLM.toSmallDouble(FTBLibStats.getDeathsPerHour(player.stats()));
        }
    };

    @Leaderboard
    public static final ILeaderboard PLAY_ONE_MINUTE = new LeaderboardInst(StatList.PLAY_ONE_MINUTE, (o1, o2) -> Long.compare(o2.stats().readStat(StatList.PLAY_ONE_MINUTE), o1.stats().readStat(StatList.PLAY_ONE_MINUTE)))
    {
        @Nullable
        @Override
        public Object getData(IForgePlayer player)
        {
            return LMStringUtils.getTimeString(player.stats().readStat(StatList.PLAY_ONE_MINUTE)) + " [" + (player.stats().readStat(StatList.PLAY_ONE_MINUTE) / 72000L) + "h]";
        }

        @Override
        public ITextComponent getName()
        {
            return FTBLibStats.TIME_PLAYED_LANG.textComponent();
        }
    };

    @Leaderboard
    public static final ILeaderboard LAST_SEEN = new LeaderboardInst(FTBLibStats.LAST_SEEN, (o1, o2) -> Long.compare(FTBLibStats.getLastSeen(o2.stats(), o2.isOnline()), FTBLibStats.getLastSeen(o1.stats(), o1.isOnline())))
    {
        @Nullable
        @Override
        public Object getData(IForgePlayer player)
        {
            if(player.isOnline())
            {
                return GuiLang.LABEL_ONLINE.textComponent();
            }

            return LMStringUtils.getTimeString(System.currentTimeMillis() - FTBLibStats.getLastSeen(player.stats(), player.isOnline()));
        }
    };
}
