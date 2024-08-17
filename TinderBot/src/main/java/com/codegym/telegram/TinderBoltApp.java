package com.codegym.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends SimpleTelegramBot {

    public static final String TELEGRAM_BOT_TOKEN = ""; //TODO: añadir el token del bot entre comillas
    public static final String OPEN_AI_TOKEN = ""; //TODO: añadir el token de ChatGPT entre comillas

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode mode;
    private ArrayList<String> list = new ArrayList<>();

    public TinderBoltApp() {
        super(TELEGRAM_BOT_TOKEN);
    }



    //TODO: escribiremos la funcionalidad principal del bot aquí

    public void startCommand(){
        mode = DialogMode.MAIN;
        String text = loadMessage("main");
        sendPhotoMessage("main");
        sendTextMessage(text);

        showMainMenu(
                "start", "inicia el bot",
                "profile", "genera un perfil hot",
                "opener", "mensaje para chat hot \uD83E\uDD70",
                "message", "correspondencia en su nombre \uD83D\uDE08",
                "date", "correspondencia con celebridades \uD83D\uDE0E",
                "gpt", "preguntarle a GPT \uD83E\uDDE0"
        );
    }

    public void gptCommand(){
        mode = DialogMode.GPT;

        String text = loadMessage("gpt");
        sendPhotoMessage("gpt");
        sendTextMessage(text);
    }

    public void gptDialog(){
        String text = getMessageText();
        String prompt = loadPrompt("gpt");

        var myMessage = sendTextMessage("gpt is typing...");
        String answer = chatGPT.sendMessage(prompt, text);
        updateTextMessage(myMessage, answer);
    }

    public void dateCommand(){
        mode = DialogMode.DATE;

        String text = loadMessage("date");
        sendPhotoMessage("date");
        sendTextMessage(text);
        sendTextButtonsMessage(text,
                "date_Miku", "Hatsune Miku",
                "date_Rias", " Rias Gremory",
                "date_Megumin", "Megumin",
                "date_Rem", "Rem",
                "date_Zero", " Fate Zero ");
    }

    public void dateButton(){
        String key = getButtonKey();
        sendPhotoMessage(key);
        sendHtmlMessage(key);

        String prompt = loadPrompt(key);
        chatGPT.setPrompt(prompt);
    }

    public void dateDialog(){
        String text = getMessageText();

        var myMessage = sendTextMessage("user is typing...");
        String answer = chatGPT.addMessage(text);
        // sendTextMessage(answer);
        updateTextMessage(myMessage, answer);
    }


    public void messageCommand(){
        mode = DialogMode.MESSAGE;
        String text = loadMessage("message");
        sendPhotoMessage("message");
        sendTextButtonsMessage(text,
                "message_next", "write next message.",
                "message_date", "Ask the person out on a date.");

        list.clear();

    }

    public void messageButton(){
        String key = getButtonKey();
        String prompt = loadPrompt(key);
        String history = String.join("\n\n", list);

        var myMessage = sendTextMessage("Chat GPT is typing... ");
        String answer = chatGPT.sendMessage(prompt, history);
        updateTextMessage(myMessage, answer);

    }

    public void messageDialog(){

        String text = getMessageText();
        list.add(text);
    }

    public void profileCommand(){

        mode = DialogMode.PROFILE;
        String text = loadMessage("profile");
        sendPhotoMessage("profile");
        sendTextMessage(text);

        sendTextMessage("ingresa tu nombre: ");
        user = new UserInfo();
        questionCount = 0;
    }

    private  UserInfo user = new UserInfo();
    private int questionCount = 0;

    public void profileDialog() {
        String text = getMessageText();
        questionCount++;

        if (questionCount == 1) {
            user.name = text;
            sendTextMessage("Tu edad: ");

        } else if (questionCount == 2) {
            user.age = text;
            sendTextMessage("Tu adctividad favorita: ");
        } else if (questionCount == 3) {
            user.hobby = text;
            sendTextMessage("tu objetivo para interactuar con esa persona ");
        } else if (questionCount == 4) {
            user.goals = text;

            String prompt = loadPrompt("profile");
            String userinfo = user.toString();

            var myMessage = sendTextMessage("Chat GPT is typing... ");
            String answer = chatGPT.sendMessage(prompt, userinfo);
            updateTextMessage(myMessage, answer);

        }


    }


    public void openerCommand() {
        mode = DialogMode.OPENER;
        String text = loadMessage("opener");
        sendPhotoMessage("opener");
        sendTextMessage(text);

        sendTextMessage("ingresa su nombre: ");
        user = new UserInfo();
        questionCount = 0;
    }

    public void openerDialog(){
        String text = getMessageText();
        questionCount++;

        if (questionCount == 1) {
            user.name = text;
            sendTextMessage("Su edad: ");
        } else if (questionCount == 2) {
            user.age = text;
            sendTextMessage("En que trabaja? ");
        } else if (questionCount == 3) {
            user.hobby = text;
            sendTextMessage("En la escala del 1 a 10 que tan atractiva es la persona? ");
        } else if (questionCount == 4) {
            user.annoys = text;
            sendTextMessage("Que le molesta de la gente? ");
        }    else if (questionCount == 5) {
                user.goals = text;

            String prompt = loadPrompt("opener");
            String userinfo = user.toString();

            var myMessage = sendTextMessage("Chat GPT is typing... ");
            String answer = chatGPT.sendMessage(prompt, userinfo);
            updateTextMessage(myMessage, answer);

        }
    }

    public void hello() {
        if (mode == DialogMode.GPT) {
            gptDialog();
        } else if (mode == DialogMode.DATE) {
            dateDialog();
        }else if (mode == DialogMode.MESSAGE) {
            messageDialog();
        }else if (mode == DialogMode.PROFILE) {
            profileDialog();
        }else if (mode == DialogMode.OPENER) {
            openerDialog();
        }else {

            String text = getMessageText();
            sendTextMessage("Hello!");
            sendTextMessage("How are you?");
            sendTextMessage("You wrote: " + text);

            sendPhotoMessage("avatar_main");
            sendTextButtonsMessage("Launch process",
                    "start", "Start",
                    "stop", "Stop");
        }
    }

    public void helloButton(){

        String key = getButtonKey();
        if (key.equals("start")){
            sendTextMessage("The process has been launched.");
        }else {
            sendTextMessage("Process has been stopped.");
        }
    }

    @Override
    public void onInitialize() {
        //TODO: y un poco más aquí :)
        addCommandHandler("start", this::startCommand);
        addCommandHandler("gpt", this:: gptCommand);
        addCommandHandler("date", this:: dateCommand);
        addCommandHandler("message", this:: messageCommand);
        addCommandHandler("profile", this:: profileCommand);
        addCommandHandler("opener", this:: openerCommand);

        addMessageHandler(this::hello);
        // addButtonHandler("^.*", this::helloButton);
        addButtonHandler("^date_.*", this::dateButton);
        addButtonHandler("^message_.*", this::messageButton);
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
