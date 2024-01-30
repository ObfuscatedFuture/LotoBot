import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class mainBot extends ListenerAdapter
{
    public static JDA jda;

    //Move all of these vars to server data file (so they are persistent across restarts)
    public int TICKET_PRICE = -1;
    public int MAX_TICKETS = -1;
    public int currentJackpot = -1;
    public int ticketsSold = -1;

    Date drawDate = new Date();
    userDataStore ds = new userDataStore();
    serverDataStore sds = new serverDataStore();
    public static void main(String []args)
    {
        jda = JDABuilder.createDefault(System.getenv("token"), Collections.emptyList())
                .addEventListeners(new mainBot())
                .disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE, CacheFlag.ACTIVITY)
                .setBulkDeleteSplittingEnabled(false)
                .setActivity(Activity.playing("Starscape"))
                .build();

        jda.updateCommands().addCommands(
                Commands.slash("buy", "Buy Tickets")
                        .setGuildOnly(true)
                        .addOption(OptionType.INTEGER, "amount", "The number of tickets to buy", false),
                Commands.slash("tickets", "Check tickets you've purchased")
                        .setGuildOnly(true),
                Commands.slash("balance", "Check current credit balance")
                        .setGuildOnly(true),
                Commands.slash("deposit", "deposit to user account (admin)")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
                        .setGuildOnly(true)
                        .addOption(OptionType.USER, "user", "what account to deposit to", true)
                        .addOption(OptionType.INTEGER, "amount", "how many credits to deposit", true),

                Commands.slash("about", "how the lottery works"),

                Commands.slash("update", "update lottery settings (admin)")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
                        .setGuildOnly(true)
                        .addOption(OptionType.INTEGER, "jackpot", "set the jackpot", false)
                        .addOption(OptionType.INTEGER, "price", "sets the ticket price", false)
                        .addOption(OptionType.INTEGER, "max", "sets the max tickets", false)
                        .addOption(OptionType.STRING, "draw", "sets the draw date (MM/dd/yy)", false),

                Commands.slash("jackpot", "current jackpot")
                        .setGuildOnly(true)
        ).queue();
    }
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "buy" -> {
                int credits = Integer.parseInt(ds.getCredits(event.getUser().getId()));
                /* CACHE TICKET PRICE & MAX TICKETS*/
                if(TICKET_PRICE==-1 || MAX_TICKETS==-1)
                {
                    try {
                        TICKET_PRICE = (sds.getTixPrice(event.getGuild().getId()));
                        MAX_TICKETS = (sds.getMaxTix(event.getGuild().getId()));
                    } catch (FileNotFoundException e) {
                        //TODO add error message that lottery is not setup on this server
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                }
                if ((credits > TICKET_PRICE * event.getOption("amount").getAsInt()) &&
                        ds.getTickets(event.getUser().getId()).size()+event.getOption("amount").getAsInt() <= (MAX_TICKETS))
                {
                    //Successfully passes purchase checks
                    ds.editAccount(event.getUser().getId(), String.valueOf(credits - (TICKET_PRICE * event.getOption("amount").getAsInt())));
                    ArrayList<Integer> tickets = ds.getTickets(event.getUser().getId());
                    for (int i = 0; i < event.getOption("amount").getAsInt(); i++) {
                        Random r = new Random();
                        tickets.add(r.nextInt(999) + 1);
                    }
                    ds.editTickets(event.getUser().getId(), tickets);
                    event.reply("Bought " + event.getOption("amount").getAsInt() + " tickets!").setEphemeral(true)
                            //TODO add ticket number to message
                            .queue();
                } else if(credits < TICKET_PRICE * event.getOption("amount").getAsInt()) {
                    event.reply("Not enough credits!").setEphemeral(true)
                            .queue();
                } else if(ds.getTickets(event.getUser().getId()).size()+event.getOption("amount").getAsInt() > (MAX_TICKETS)) {
                    event.reply("Too many tickets!").setEphemeral(true)
                            .queue();
                }
            }
            case "tickets" -> {
                String formattedTix = ds.formatArrayList(ds.getTickets(event.getUser().getId()));
                if (formattedTix == null) {
                    formattedTix = "";
                }
                event.reply("Tickets: **\n" + formattedTix + "**").setEphemeral(true)
                        .queue();
            }
            //TODO add custom emojis for each number
            case "balance" ->
                    event.reply("Balance: **" + ds.getCredits(event.getUser().getId()) + "** credits").setEphemeral(true)
                            .queue();
            case "deposit" -> {
                if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
                    event.reply("Insufficient permissions").setEphemeral(true)
                            .queue();
                    break;
                }
                User target = event.getOption("user", OptionMapping::getAsUser);
                int amount = event.getOption("amount", OptionMapping::getAsInt);
                int currentBalance = Integer.parseInt(ds.getCredits(target.getId()));
                currentBalance += amount;
                ds.editAccount(target.getId(), String.valueOf(currentBalance));
                event.reply("Deposited " + amount + " credits to " + target.getAsMention() + "'s account").setEphemeral(true)
                        .queue();
            }
            case "about" -> event.reply("**How the lottery works**: \n" +
                            "Each week players can purchase tickets to the lottery drawing \n" +
                            "Tickets cost: **" + TICKET_PRICE + "**\n" +
                            "Each player can purchase **" + MAX_TICKETS + "x** tickets each drawing \n" +
                            "If the winning number does not match any purchased ticket the jackpot will increase for next week, " +
                            "this means it is possible to have **no winners** in a drawing").setEphemeral(true)
                    .queue();
            case "jackpot" -> event.reply("Current Jackpot: **" + currentJackpot + "** credits!").setEphemeral(true)
                    .queue();
            case "update" -> {
                if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
                    event.reply("Insufficient permissions").setEphemeral(true)
                            .queue();
                    break;
                }
                if (event.getOption("jackpot") != null) {
                    //STORE IN SERVER DATA
                    currentJackpot = event.getOption("jackpot").getAsInt();
                }
                if (event.getOption("price") != null) {
                    //STORE IN SERVER DATA
                    TICKET_PRICE = event.getOption("price").getAsInt();
                }
                if (event.getOption("max") != null) {
                    //STORE IN SERVER DATA
                    MAX_TICKETS = event.getOption("max").getAsInt();
                }
                if (event.getOption("draw") != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
                    try {
                        drawDate = sdf.parse(event.getOption("draw").getAsString());
                    } catch (ParseException e) {
                        event.reply("Incorrect date format provided").setEphemeral(true).queue();
                    }
                } else {
                    event.reply("Please use option (jackpot) (price) (max) (draw)").setEphemeral(true).queue();
                }
            }
            //TODO add date of next drawing
            case "draw" -> {
                if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
                    //TODO add draw date
                    event.reply("Wait until: **" + drawDate + "**").setEphemeral(true).queue();
                    break;
                }
                event.reply("Incompleted Feature").setEphemeral(true).queue();
            }
            //TODO Posts current jackpot, drawing time, ticket price, tickets sold
            default -> System.out.printf("Unknown command %s used by %#s%n", event.getName(), event.getUser());
        }
    }
}
