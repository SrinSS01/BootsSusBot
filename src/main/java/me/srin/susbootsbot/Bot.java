package me.srin.susbootsbot;

import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import static net.dv8tion.jda.api.requests.GatewayIntent.*;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.*;

@WebServlet(
    name = "bot",
    urlPatterns = "/bot",
    loadOnStartup = 0
)
public class Bot extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);
    public static JDA jda;
    @Override
    public void init() {
        try {
            jda = JDABuilder.createDefault(
                            System.getenv("TOKEN"),
                            GUILD_MESSAGES,
                            GUILD_MESSAGE_REACTIONS,
                            GUILD_VOICE_STATES,
                            GUILD_EMOJIS,
                            GUILD_MEMBERS,
                            GUILD_PRESENCES
                    ).setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableCache(CLIENT_STATUS)
                    .disableCache(VOICE_STATE, EMOTE).addEventListeners(Events.INSTANCE)
                    .setStatus(OnlineStatus.ONLINE)
                    .setActivity(Activity.of(Activity.ActivityType.DEFAULT, "amongus"))
                    .build();
            for (Guild guild : jda.awaitReady().getGuilds()) {
                guild
                    .upsertCommand("sus", "grants a sus point to the user mentioned")
                    .addOption(OptionType.USER, "member", "mention the user to give sus points to", true)
                    .queue();
                guild
                    .upsertCommand("leaderboard", "shows sus leaderboard")
                    .queue();
                String guild_id = guild.getId();
                for (Member member : guild.getMembers()) {
                    String memberId = member.getId();
                    Document result = Mongo.suspoints.find(
                        Filters.and(
                            Filters.eq("guild_id", guild_id),
                            Filters.eq("user_id", memberId)
                        )
                    ).first();
                    if (result == null) {
                        User user = member.getUser();
                        if (user.isBot()) continue;
                        String tag = user.getAsTag();
                        Mongo.suspoints.insertOne(
                                new Document()
                                        .append("user_id", memberId)
                                        .append("guild_id", guild_id)
                                        .append("tag", tag)
                                        .append("sus_points", 0)
                                        .append("lastCommandTime", 0L)
                        );
                        LOGGER.info("inserted " + tag + " guild " + guild_id);
                    }
                }
            }
        } catch (LoginException |InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
