import java.util.TimerTask;



public class BotTT extends TimerTask {

    

    private Bot bot;

    

    public BotTT(Bot bot) {

        this.bot = bot;

    }

    

    public void run( ) {

        bot.update();

    }

    

}