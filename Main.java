import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.sasha.eventsys.SimpleListener;
import com.sasha.reminecraft.Configuration;
import com.sasha.reminecraft.api.RePlugin;
import com.sasha.reminecraft.api.event.ChatReceivedEvent;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.logging.ILogger;
import com.sasha.reminecraft.logging.LoggerBuilder;
import com.sasha.eventsys.SimpleEventHandler;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import javax.security.auth.login.LoginException;

public class Main extends RePlugin implements SimpleListener{

    static JDA jda;
    static Config CFG = new Config("GnomeversationCFG");
    static ILogger loggieTheLogger = LoggerBuilder.buildProperLogger("GnomeversationLog");

    static Main p = new Main();

    @Override
    public void onPluginInit()
    {
        Main.loggieTheLogger.log("Howdy! Thanks for using Gnomeversation!");
        Main.loggieTheLogger.log("Initialising the jda!");
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(Main.CFG.var_DiscordApiKey);
        builder.addEventListener(new DiscordHandler());
        try{
            Main.jda = builder.build();
        }catch(LoginException e){
            Main.loggieTheLogger.log("ERROR: Can't Login");
            e.printStackTrace();
        }

    }

    @Override
    public void onPluginEnable() {
        this.getReMinecraft().EVENT_BUS.registerListener(this);
    }
    @SimpleEventHandler
    public void relayMessage(ChatReceivedEvent e){
        if(e.messageText==null) return;
        if(e.messageText.equals("")) return;
       Main.jda.getTextChannelById(Main.CFG.var_BotChannelId).sendMessage(e.messageText).queue();

    }
    @Override
    public void onPluginDisable() {
        this.getReMinecraft().EVENT_BUS.deregisterListener(this);
    }

    @Override
    public void onPluginShutdown() {

    }

    @Override
    public void registerCommands() {

    }

    @Override
    public void registerConfig() {
        this.getReMinecraft().configurations.add(Main.CFG);
    }

    public void sendMinecraftMessage(String message){
        this.getReMinecraft().minecraftClient.getSession().send(new ClientChatPacket(message));
    }

}
class Config extends Configuration {
    @Configuration.ConfigSetting
    public String var_DiscordApiKey = "put your api key here";
    @Configuration.ConfigSetting
    public long var_BotChannelId = 696969696;
    @Configuration.ConfigSetting
    public boolean var_storeChatInADatabase = false;

    public Config(String configName) {
        super(configName);
    }

}
class DiscordHandler extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent e){
        String message = e.getMessage().getContentRaw();
        if(e.getAuthor().isBot()) return;
        if(e.getChannel().getIdLong()!= Main.CFG.var_BotChannelId) return;
        // Command handler
        // All messages started with '%' will be ignored
        if(message.startsWith("%")){
            if(message.startsWith("%help"))
                Main.jda.getTextChannelById(Main.CFG.var_BotChannelId).sendMessage(
                        "`Thanks for asking for help, here are the available commands: \n"
                        +"%help - displays this message\n"
                        +"%w - write something to somebody privately\n"
                        +"%l - writes message to the last person you messaged\n"
                        +"%r - replies to the last person who messaged you\n"
                        +"%a - lets you write a chat message anonymously\n"
                        +"%t - reads the tab message to you.\n"
                        +"%o - shows online players\n"
                        +"%s - shows bot status (WIP) `"

                ).queue();
            if(message.startsWith("%w"))
                Main.p.sendMinecraftMessage("/msg "+message.substring(2));

            if(message.startsWith("%l"))
                Main.p.sendMinecraftMessage("/l "+message.substring(2));

            if(message.startsWith("%r"))
                Main.p.sendMinecraftMessage("/r "+message.substring(2));

            if(message.startsWith("%a"))
                Main.p.sendMinecraftMessage(message.substring(2));

            if(message.startsWith("%t"))
                Main.jda.getTextChannelById(Main.CFG.var_BotChannelId).sendMessage(
                        ReClient.ReClientCache.INSTANCE.tabHeader.toString()
                        +ReClient.ReClientCache.INSTANCE.tabFooter.toString()
                ).queue();

            if(message.startsWith("%o"))
                Main.jda.getTextChannelById(Main.CFG.var_BotChannelId).sendMessage(
                ReClient.ReClientCache.INSTANCE.playerListEntries.toString()
            ).queue();
            return;
        }
        Main.p.sendMinecraftMessage(e.getAuthor().getAsTag()+": "+message);
}


}
